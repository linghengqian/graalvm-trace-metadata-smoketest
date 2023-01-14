package org.apache.shardingsphere.elasticjob.lite.internal.failover;

import com.lingh.util.ReflectionUtils;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.infra.handler.sharding.JobInstance;
import org.apache.shardingsphere.elasticjob.lite.internal.config.ConfigurationService;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobScheduleController;
import org.apache.shardingsphere.elasticjob.lite.internal.sharding.ShardingService;
import org.apache.shardingsphere.elasticjob.lite.internal.storage.JobNodeStorage;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public final class FailoverServiceTest {
    @Mock
    private CoordinatorRegistryCenter regCenter;

    @Mock
    private JobScheduleController jobScheduleController;

    @Mock
    private JobNodeStorage jobNodeStorage;

    @Mock
    private ShardingService shardingService;

    @Mock
    private ConfigurationService configService;

    private final FailoverService failoverService = new FailoverService(null, "test_job");

    @BeforeEach
    public void setUp() {
        ReflectionUtils.setFieldValue(failoverService, "jobNodeStorage", jobNodeStorage);
        ReflectionUtils.setFieldValue(failoverService, "shardingService", shardingService);
        ReflectionUtils.setFieldValue(failoverService, "jobName", "test_job");
        ReflectionUtils.setFieldValue(failoverService, "configService", configService);
        JobRegistry.getInstance().addJobInstance("test_job", new JobInstance("127.0.0.1@-@0"));
    }

    @Test
    public void assertSetCrashedFailoverFlagWhenItemIsNotAssigned() {
        when(jobNodeStorage.isJobNodeExisted("sharding/0/failover")).thenReturn(true);
        failoverService.setCrashedFailoverFlag(0);
        verify(jobNodeStorage).isJobNodeExisted("sharding/0/failover");
        verify(jobNodeStorage, times(0)).createJobNodeIfNeeded("leader/failover/items/0");
    }

    @Test
    public void assertSetCrashedFailoverFlagWhenItemIsAssigned() {
        when(jobNodeStorage.isJobNodeExisted("sharding/0/failover")).thenReturn(false);
        failoverService.setCrashedFailoverFlag(0);
        verify(jobNodeStorage).isJobNodeExisted("sharding/0/failover");
        verify(jobNodeStorage).createJobNodeIfNeeded("leader/failover/items/0");
    }

    @Test
    public void assertSetCrashedFailoverFlagDirectly() {
        failoverService.setCrashedFailoverFlagDirectly(0);
        verify(jobNodeStorage).createJobNodeIfNeeded("leader/failover/items/0");
    }

    @Test
    public void assertFailoverIfUnnecessaryWhenItemsRootNodeNotExisted() {
        when(jobNodeStorage.isJobNodeExisted("leader/failover/items")).thenReturn(false);
        failoverService.failoverIfNecessary();
        verify(jobNodeStorage).isJobNodeExisted("leader/failover/items");
        verify(jobNodeStorage, times(0)).executeInLeader(eq("leader/failover/latch"), ArgumentMatchers.<FailoverService.FailoverLeaderExecutionCallback>any());
    }

    @Test
    public void assertFailoverIfUnnecessaryWhenItemsRootNodeIsEmpty() {
        when(jobNodeStorage.isJobNodeExisted("leader/failover/items")).thenReturn(true);
        when(jobNodeStorage.getJobNodeChildrenKeys("leader/failover/items")).thenReturn(Collections.emptyList());
        failoverService.failoverIfNecessary();
        verify(jobNodeStorage).isJobNodeExisted("leader/failover/items");
        verify(jobNodeStorage).getJobNodeChildrenKeys("leader/failover/items");
        verify(jobNodeStorage, times(0)).executeInLeader(eq("leader/failover/latch"), ArgumentMatchers.<FailoverService.FailoverLeaderExecutionCallback>any());
    }

    @Test
    public void assertFailoverIfUnnecessaryWhenServerIsNotReady() {
        JobRegistry.getInstance().setJobRunning("test_job", true);
        when(jobNodeStorage.isJobNodeExisted("leader/failover/items")).thenReturn(true);
        when(jobNodeStorage.getJobNodeChildrenKeys("leader/failover/items")).thenReturn(Arrays.asList("0", "1", "2"));
        failoverService.failoverIfNecessary();
        verify(jobNodeStorage).isJobNodeExisted("leader/failover/items");
        verify(jobNodeStorage).getJobNodeChildrenKeys("leader/failover/items");
        verify(jobNodeStorage, times(0)).executeInLeader(eq("leader/failover/latch"), ArgumentMatchers.<FailoverService.FailoverLeaderExecutionCallback>any());
    }

    @Test
    public void assertFailoverIfNecessary() {
        JobRegistry.getInstance().setJobRunning("test_job", false);
        when(jobNodeStorage.isJobNodeExisted("leader/failover/items")).thenReturn(true);
        when(jobNodeStorage.getJobNodeChildrenKeys("leader/failover/items")).thenReturn(Arrays.asList("0", "1", "2"));
        failoverService.failoverIfNecessary();
        verify(jobNodeStorage).isJobNodeExisted("leader/failover/items");
        verify(jobNodeStorage).getJobNodeChildrenKeys("leader/failover/items");
        verify(jobNodeStorage).executeInLeader(eq("leader/failover/latch"), ArgumentMatchers.<FailoverService.FailoverLeaderExecutionCallback>any());
        JobRegistry.getInstance().setJobRunning("test_job", false);
    }

    @Test
    public void assertFailoverLeaderExecutionCallbackIfNotNecessary() {
        JobRegistry.getInstance().registerRegistryCenter("test_job", regCenter);
        JobRegistry.getInstance().registerJob("test_job", jobScheduleController);
        JobRegistry.getInstance().setJobRunning("test_job", false);
        when(jobNodeStorage.isJobNodeExisted("leader/failover/items")).thenReturn(false);
        failoverService.new FailoverLeaderExecutionCallback().execute();
        verify(jobNodeStorage).isJobNodeExisted("leader/failover/items");
        verify(jobNodeStorage, times(0)).getJobNodeChildrenKeys("leader/failover/items");
        JobRegistry.getInstance().shutdown("test_job");
    }

    @Test
    public void assertFailoverLeaderExecutionCallbackIfNecessary() {
        JobRegistry.getInstance().setJobRunning("test_job", false);
        when(jobNodeStorage.isJobNodeExisted("leader/failover/items")).thenReturn(true);
        when(jobNodeStorage.getJobNodeChildrenKeys("leader/failover/items")).thenReturn(Arrays.asList("0", "1", "2"));
        JobRegistry.getInstance().registerRegistryCenter("test_job", regCenter);
        JobRegistry.getInstance().registerJob("test_job", jobScheduleController);
        failoverService.new FailoverLeaderExecutionCallback().execute();
        verify(jobNodeStorage).isJobNodeExisted("leader/failover/items");
        verify(jobNodeStorage, times(2)).getJobNodeChildrenKeys("leader/failover/items");
        verify(jobNodeStorage).fillEphemeralJobNode("sharding/0/failover", "127.0.0.1@-@0");
        verify(jobNodeStorage).fillJobNode("sharding/0/failovering", "127.0.0.1@-@0");
        verify(jobNodeStorage).removeJobNodeIfExisted("leader/failover/items/0");
        verify(jobScheduleController).triggerJob();
        JobRegistry.getInstance().setJobRunning("test_job", false);
        JobRegistry.getInstance().shutdown("test_job");
    }

    @Test
    public void assertGetFailoveringItems() {
        JobRegistry.getInstance().registerJob("test_job", jobScheduleController);
        when(jobNodeStorage.getJobNodeChildrenKeys("sharding")).thenReturn(Arrays.asList("0", "1", "2"));
        when(jobNodeStorage.isJobNodeExisted("sharding/0/failovering")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("sharding/1/failovering")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("sharding/2/failovering")).thenReturn(false);
        when(jobNodeStorage.getJobNodeDataDirectly("sharding/0/failovering")).thenReturn("127.0.0.1@-@0");
        when(jobNodeStorage.getJobNodeDataDirectly("sharding/1/failovering")).thenReturn("127.0.0.1@-@1");
        assertThat(failoverService.getFailoveringItems("127.0.0.1@-@1"), is(Collections.singletonList(1)));
        verify(jobNodeStorage).getJobNodeChildrenKeys("sharding");
        verify(jobNodeStorage).isJobNodeExisted("sharding/0/failovering");
        verify(jobNodeStorage).isJobNodeExisted("sharding/1/failovering");
        verify(jobNodeStorage).getJobNodeDataDirectly("sharding/0/failovering");
        verify(jobNodeStorage).getJobNodeDataDirectly("sharding/1/failovering");
        JobRegistry.getInstance().shutdown("test_job");
    }

    @Test
    public void assertUpdateFailoverComplete() {
        failoverService.updateFailoverComplete(Arrays.asList(0, 1));
        verify(jobNodeStorage).removeJobNodeIfExisted("sharding/0/failover");
        verify(jobNodeStorage).removeJobNodeIfExisted("sharding/1/failover");
    }

    @Test
    public void assertGetFailoverItems() {
        JobRegistry.getInstance().registerRegistryCenter("test_job", regCenter);
        JobRegistry.getInstance().registerJob("test_job", jobScheduleController);
        when(jobNodeStorage.getJobNodeChildrenKeys("sharding")).thenReturn(Arrays.asList("0", "1", "2"));
        when(jobNodeStorage.isJobNodeExisted("sharding/0/failover")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("sharding/1/failover")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("sharding/2/failover")).thenReturn(false);
        when(jobNodeStorage.getJobNodeDataDirectly("sharding/0/failover")).thenReturn("127.0.0.1@-@0");
        when(jobNodeStorage.getJobNodeDataDirectly("sharding/1/failover")).thenReturn("127.0.0.1@-@1");
        assertThat(failoverService.getFailoverItems("127.0.0.1@-@1"), is(Collections.singletonList(1)));
        verify(jobNodeStorage).getJobNodeChildrenKeys("sharding");
        verify(jobNodeStorage).isJobNodeExisted("sharding/0/failover");
        verify(jobNodeStorage).isJobNodeExisted("sharding/1/failover");
        verify(jobNodeStorage).getJobNodeDataDirectly("sharding/0/failover");
        verify(jobNodeStorage).getJobNodeDataDirectly("sharding/1/failover");
        JobRegistry.getInstance().shutdown("test_job");
    }

    @Test
    public void assertGetLocalFailoverItemsIfShutdown() {
        assertThat(failoverService.getLocalFailoverItems(), is(Collections.<Integer>emptyList()));
        verify(jobNodeStorage, times(0)).getJobNodeChildrenKeys("sharding");
    }

    @Test
    public void assertGetLocalFailoverItems() {
        JobRegistry.getInstance().registerRegistryCenter("test_job", regCenter);
        JobRegistry.getInstance().registerJob("test_job", jobScheduleController);
        when(jobNodeStorage.getJobNodeChildrenKeys("sharding")).thenReturn(Arrays.asList("0", "1", "2"));
        when(jobNodeStorage.isJobNodeExisted("sharding/0/failover")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("sharding/1/failover")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("sharding/2/failover")).thenReturn(false);
        when(jobNodeStorage.getJobNodeDataDirectly("sharding/0/failover")).thenReturn("127.0.0.1@-@0");
        when(jobNodeStorage.getJobNodeDataDirectly("sharding/1/failover")).thenReturn("127.0.0.1@-@1");
        assertThat(failoverService.getLocalFailoverItems(), is(Collections.singletonList(0)));
        verify(jobNodeStorage).getJobNodeChildrenKeys("sharding");
        verify(jobNodeStorage).isJobNodeExisted("sharding/0/failover");
        verify(jobNodeStorage).isJobNodeExisted("sharding/1/failover");
        verify(jobNodeStorage).getJobNodeDataDirectly("sharding/0/failover");
        verify(jobNodeStorage).getJobNodeDataDirectly("sharding/1/failover");
        JobRegistry.getInstance().shutdown("test_job");
    }

    @Test
    public void assertGetLocalTakeOffItems() {
        when(shardingService.getLocalShardingItems()).thenReturn(Arrays.asList(0, 1, 2));
        when(jobNodeStorage.isJobNodeExisted("sharding/0/failover")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("sharding/1/failover")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("sharding/2/failover")).thenReturn(false);
        assertThat(failoverService.getLocalTakeOffItems(), is(Arrays.asList(0, 1)));
        verify(shardingService).getLocalShardingItems();
        verify(jobNodeStorage).isJobNodeExisted("sharding/0/failover");
        verify(jobNodeStorage).isJobNodeExisted("sharding/1/failover");
        verify(jobNodeStorage).isJobNodeExisted("sharding/2/failover");
    }

    @Test
    @Disabled //TODO
    public void assertGetAllFailoveringItems() {
        when(configService.load(true)).thenReturn(JobConfiguration.newBuilder("test_job", 3).build());
        String jobInstanceId = "127.0.0.1@-@1";
        when(jobNodeStorage.getJobNodeData("sharding/0/failovering")).thenReturn(jobInstanceId);
        when(jobNodeStorage.getJobNodeData("sharding/2/failovering")).thenReturn(jobInstanceId);
        Map<Integer, JobInstance> actual = failoverService.getAllFailoveringItems();
        assertThat(actual.size(), is(2));
        assertThat(actual.get(0), is(new JobInstance(jobInstanceId)));
        assertThat(actual.get(2), is(new JobInstance(jobInstanceId)));
    }

    @Test
    public void assertClearFailoveringItem() {
        failoverService.clearFailoveringItem(0);
        verify(jobNodeStorage).removeJobNodeIfExisted("sharding/0/failovering");
    }

    @Test
    public void assertRemoveFailoverInfo() {
        when(jobNodeStorage.getJobNodeChildrenKeys("sharding")).thenReturn(Arrays.asList("0", "1", "2"));
        failoverService.removeFailoverInfo();
        verify(jobNodeStorage).getJobNodeChildrenKeys("sharding");
        verify(jobNodeStorage).removeJobNodeIfExisted("sharding/0/failover");
        verify(jobNodeStorage).removeJobNodeIfExisted("sharding/1/failover");
        verify(jobNodeStorage).removeJobNodeIfExisted("sharding/2/failover");
    }
}