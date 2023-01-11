package com.lingh.internal.instance;

import com.lingh.util.ReflectionUtils;
import org.apache.shardingsphere.elasticjob.infra.handler.sharding.JobInstance;
import org.apache.shardingsphere.elasticjob.lite.internal.instance.InstanceNode;
import org.apache.shardingsphere.elasticjob.lite.internal.instance.InstanceService;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.lite.internal.server.ServerService;
import org.apache.shardingsphere.elasticjob.lite.internal.storage.JobNodeStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public final class InstanceServiceTest {
    @Mock
    private JobNodeStorage jobNodeStorage;
    @Mock
    private ServerService serverService;
    private InstanceService instanceService;

    @BeforeEach
    public void setUp() {
        JobRegistry.getInstance().addJobInstance("test_job", new JobInstance("127.0.0.1@-@0", null, "127.0.0.1"));
        instanceService = new InstanceService(null, "test_job");
        InstanceNode instanceNode = new InstanceNode("test_job");
        ReflectionUtils.setFieldValue(instanceService, "instanceNode", instanceNode);
        ReflectionUtils.setFieldValue(instanceService, "jobNodeStorage", jobNodeStorage);
        ReflectionUtils.setFieldValue(instanceService, "serverService", serverService);
    }

    @Test
    public void assertPersistOnline() {
        instanceService.persistOnline();
        verify(jobNodeStorage).fillEphemeralJobNode("instances/127.0.0.1@-@0", "jobInstanceId: 127.0.0.1@-@0\nserverIp: 127.0.0.1\n");
    }

    @Test
    public void assertRemoveInstance() {
        instanceService.removeInstance();
        verify(jobNodeStorage).removeJobNodeIfExisted("instances/127.0.0.1@-@0");
    }

    @Test
    public void assertGetAvailableJobInstances() {
        when(jobNodeStorage.getJobNodeChildrenKeys(InstanceNode.ROOT)).thenReturn(Arrays.asList("127.0.0.1@-@0", "127.0.0.2@-@0"));
        when(jobNodeStorage.getJobNodeData("instances/127.0.0.1@-@0")).thenReturn("jobInstanceId: 127.0.0.1@-@0\nlabels: labels\nserverIp: 127.0.0.1\n");
        when(jobNodeStorage.getJobNodeData("instances/127.0.0.2@-@0")).thenReturn("jobInstanceId: 127.0.0.2@-@0\nlabels: labels\nserverIp: 127.0.0.2\n");
        when(serverService.isEnableServer("127.0.0.1")).thenReturn(true);
        assertThat(instanceService.getAvailableJobInstances(), is(Collections.singletonList(new JobInstance("127.0.0.1@-@0"))));
    }

    @Test
    public void assertTriggerAllInstances() {
        when(jobNodeStorage.getJobNodeChildrenKeys(InstanceNode.ROOT)).thenReturn(Arrays.asList("127.0.0.1@-@0", "127.0.0.2@-@0"));
        instanceService.triggerAllInstances();
        verify(jobNodeStorage).createJobNodeIfNeeded("trigger/127.0.0.1@-@0");
        verify(jobNodeStorage).createJobNodeIfNeeded("trigger/127.0.0.2@-@0");
    }
}
