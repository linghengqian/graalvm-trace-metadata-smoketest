package org.apache.shardingsphere.elasticjob.lite.internal.server;

import com.lingh.util.ReflectionUtils;
import org.apache.shardingsphere.elasticjob.infra.handler.sharding.JobInstance;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobScheduleController;
import org.apache.shardingsphere.elasticjob.lite.internal.storage.JobNodeStorage;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ServerServiceTest {

    @Mock
    private CoordinatorRegistryCenter regCenter;

    @Mock
    private JobScheduleController jobScheduleController;

    @Mock
    private JobNodeStorage jobNodeStorage;

    private ServerService serverService;

    @Before
    public void setUp() {
        JobRegistry.getInstance().addJobInstance("test_job", new JobInstance("127.0.0.1@-@0", null, "127.0.0.1"));
        serverService = new ServerService(null, "test_job");
        ServerNode serverNode = new ServerNode("test_job");
        ReflectionUtils.setFieldValue(serverService, "serverNode", serverNode);
        ReflectionUtils.setFieldValue(serverService, "jobNodeStorage", jobNodeStorage);
    }

    @Test
    public void assertPersistOnlineForInstanceShutdown() {
        JobRegistry.getInstance().shutdown("test_job");
        serverService.persistOnline(false);
        verify(jobNodeStorage, times(0)).fillJobNode("servers/127.0.0.1", ServerStatus.DISABLED.name());
    }

    @Test
    public void assertPersistOnlineForDisabledServer() {
        JobRegistry.getInstance().registerRegistryCenter("test_job", regCenter);
        JobRegistry.getInstance().registerJob("test_job", jobScheduleController);
        serverService.persistOnline(false);
        verify(jobNodeStorage).fillJobNode("servers/127.0.0.1", ServerStatus.DISABLED.name());
        JobRegistry.getInstance().shutdown("test_job");
    }

    @Test
    public void assertPersistOnlineForEnabledServer() {
        JobRegistry.getInstance().registerRegistryCenter("test_job", regCenter);
        JobRegistry.getInstance().registerJob("test_job", jobScheduleController);
        serverService.persistOnline(true);
        verify(jobNodeStorage).fillJobNode("servers/127.0.0.1", ServerStatus.ENABLED.name());
        JobRegistry.getInstance().shutdown("test_job");
    }

    @Test
    public void assertHasAvailableServers() {
        when(jobNodeStorage.getJobNodeChildrenKeys("servers")).thenReturn(Arrays.asList("127.0.0.1", "127.0.0.2", "127.0.0.3"));
        when(jobNodeStorage.getJobNodeData("servers/127.0.0.1")).thenReturn(ServerStatus.DISABLED.name());
        when(jobNodeStorage.getJobNodeData("servers/127.0.0.2")).thenReturn(ServerStatus.ENABLED.name());
        when(jobNodeStorage.getJobNodeData("servers/127.0.0.3")).thenReturn(ServerStatus.ENABLED.name());
        when(jobNodeStorage.getJobNodeChildrenKeys("instances")).thenReturn(Collections.singletonList("127.0.0.3@-@0"));
        assertTrue(serverService.hasAvailableServers());
    }

    @Test
    public void assertHasNotAvailableServers() {
        when(jobNodeStorage.getJobNodeChildrenKeys("servers")).thenReturn(Arrays.asList("127.0.0.1", "127.0.0.2"));
        when(jobNodeStorage.getJobNodeData("servers/127.0.0.1")).thenReturn(ServerStatus.DISABLED.name());
        when(jobNodeStorage.getJobNodeData("servers/127.0.0.2")).thenReturn(ServerStatus.DISABLED.name());
        assertFalse(serverService.hasAvailableServers());
    }

    @Test
    public void assertIsNotAvailableServerWhenDisabled() {
        when(jobNodeStorage.getJobNodeData("servers/127.0.0.1")).thenReturn(ServerStatus.DISABLED.name());
        assertFalse(serverService.isAvailableServer("127.0.0.1"));
    }

    @Test
    public void assertIsNotAvailableServerWithoutOnlineInstances() {
        when(jobNodeStorage.getJobNodeChildrenKeys("instances")).thenReturn(Collections.singletonList("127.0.0.2@-@0"));
        when(jobNodeStorage.getJobNodeData("servers/127.0.0.1")).thenReturn(ServerStatus.ENABLED.name());
        assertFalse(serverService.isAvailableServer("127.0.0.1"));
    }

    @Test
    public void assertIsAvailableServer() {
        when(jobNodeStorage.getJobNodeChildrenKeys("instances")).thenReturn(Collections.singletonList("127.0.0.1@-@0"));
        when(jobNodeStorage.getJobNodeData("servers/127.0.0.1")).thenReturn(ServerStatus.ENABLED.name());
        assertTrue(serverService.isAvailableServer("127.0.0.1"));
    }

    @Test
    public void assertIsNotEnableServer() {
        when(jobNodeStorage.getJobNodeData("servers/127.0.0.1")).thenReturn("", ServerStatus.DISABLED.name());
        assertFalse(serverService.isEnableServer("127.0.0.1"));
    }

    @Test
    public void assertIsEnableServer() {
        when(jobNodeStorage.getJobNodeData("servers/127.0.0.1")).thenReturn("", ServerStatus.ENABLED.name());
        assertTrue(serverService.isEnableServer("127.0.0.1"));
    }

    @Test
    public void assertServerNodeAbsent() {
        assertFalse(serverService.isEnableServer("127.0.0.1"));
    }
}
