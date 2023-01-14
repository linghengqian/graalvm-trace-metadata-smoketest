package com.lingh.internal.listener;

import com.lingh.util.ReflectionUtils;
import org.apache.shardingsphere.elasticjob.infra.handler.sharding.JobInstance;
import org.apache.shardingsphere.elasticjob.lite.internal.instance.InstanceService;
import org.apache.shardingsphere.elasticjob.lite.internal.listener.RegistryCenterConnectionStateListener;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobScheduleController;
import org.apache.shardingsphere.elasticjob.lite.internal.server.ServerService;
import org.apache.shardingsphere.elasticjob.lite.internal.sharding.ExecutionService;
import org.apache.shardingsphere.elasticjob.lite.internal.sharding.ShardingService;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.listener.ConnectionStateChangedEventListener.State;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public final class RegistryCenterConnectionStateListenerTest {

    @Mock
    private CoordinatorRegistryCenter regCenter;

    @Mock
    private ServerService serverService;

    @Mock
    private InstanceService instanceService;

    @Mock
    private ShardingService shardingService;

    @Mock
    private ExecutionService executionService;

    @Mock
    private JobScheduleController jobScheduleController;

    private RegistryCenterConnectionStateListener regCenterConnectionStateListener;

    @BeforeEach
    public void setUp() {
        JobRegistry.getInstance().addJobInstance("test_job", new JobInstance("127.0.0.1@-@0", null, "127.0.0.1"));
        regCenterConnectionStateListener = new RegistryCenterConnectionStateListener(null, "test_job");
        ReflectionUtils.setFieldValue(regCenterConnectionStateListener, "serverService", serverService);
        ReflectionUtils.setFieldValue(regCenterConnectionStateListener, "instanceService", instanceService);
        ReflectionUtils.setFieldValue(regCenterConnectionStateListener, "shardingService", shardingService);
        ReflectionUtils.setFieldValue(regCenterConnectionStateListener, "executionService", executionService);
    }

    @Test
    public void assertConnectionLostListenerWhenConnectionStateIsLost() {
        JobRegistry.getInstance().registerRegistryCenter("test_job", regCenter);
        JobRegistry.getInstance().registerJob("test_job", jobScheduleController);
        regCenterConnectionStateListener.onStateChanged(null, State.UNAVAILABLE);
        verify(jobScheduleController).pauseJob();
        JobRegistry.getInstance().shutdown("test_job");
    }

    @Test
    public void assertConnectionLostListenerWhenConnectionStateIsLostButIsShutdown() {
        regCenterConnectionStateListener.onStateChanged(null, State.UNAVAILABLE);
        verify(jobScheduleController, times(0)).pauseJob();
        verify(jobScheduleController, times(0)).resumeJob();
    }

    @Test
    public void assertConnectionLostListenerWhenConnectionStateIsReconnected() {
        JobRegistry.getInstance().registerRegistryCenter("test_job", regCenter);
        JobRegistry.getInstance().registerJob("test_job", jobScheduleController);
        when(shardingService.getLocalShardingItems()).thenReturn(Arrays.asList(0, 1));
        when(serverService.isEnableServer("127.0.0.1")).thenReturn(true);
        regCenterConnectionStateListener.onStateChanged(null, State.RECONNECTED);
        verify(serverService).persistOnline(true);
        verify(executionService).clearRunningInfo(Arrays.asList(0, 1));
        verify(jobScheduleController).resumeJob();
        JobRegistry.getInstance().shutdown("test_job");
    }

    @Test
    public void assertConnectionLostListenerWhenConnectionStateIsReconnectedButIsShutdown() {
        regCenterConnectionStateListener.onStateChanged(null, State.RECONNECTED);
        verify(jobScheduleController, times(0)).pauseJob();
        verify(jobScheduleController, times(0)).resumeJob();
    }

    @Test
    public void assertConnectionLostListenerWhenConnectionStateIsOther() {
        JobRegistry.getInstance().registerRegistryCenter("test_job", regCenter);
        JobRegistry.getInstance().registerJob("test_job", jobScheduleController);
        regCenterConnectionStateListener.onStateChanged(null, State.CONNECTED);
        verify(jobScheduleController, times(0)).pauseJob();
        verify(jobScheduleController, times(0)).resumeJob();
        JobRegistry.getInstance().shutdown("test_job");
    }
}