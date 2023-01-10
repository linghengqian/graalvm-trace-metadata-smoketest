package org.apache.shardingsphere.elasticjob.lite.internal.instance;

import com.lingh.util.ReflectionUtils;
import org.apache.shardingsphere.elasticjob.infra.handler.sharding.JobInstance;
import org.apache.shardingsphere.elasticjob.lite.internal.instance.InstanceService;
import org.apache.shardingsphere.elasticjob.lite.internal.instance.ShutdownListenerManager;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobScheduleController;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.SchedulerFacade;
import org.apache.shardingsphere.elasticjob.lite.internal.storage.JobNodeStorage;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.listener.DataChangedEvent;
import org.apache.shardingsphere.elasticjob.reg.listener.DataChangedEvent.Type;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public final class ShutdownListenerManagerTest {
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    @Mock
    private JobScheduleController jobScheduleController;
    
    @Mock
    private JobNodeStorage jobNodeStorage;
    
    @Mock
    private InstanceService instanceService;
    
    @Mock
    private SchedulerFacade schedulerFacade;
    
    private ShutdownListenerManager shutdownListenerManager;
    
    @Before
    public void setUp() {
        JobRegistry.getInstance().addJobInstance("test_job", new JobInstance("127.0.0.1@-@0"));
        shutdownListenerManager = new ShutdownListenerManager(null, "test_job");
        ReflectionUtils.setFieldValue(shutdownListenerManager, "instanceService", instanceService);
        ReflectionUtils.setFieldValue(shutdownListenerManager, "schedulerFacade", schedulerFacade);
        ReflectionUtils.setSuperclassFieldValue(shutdownListenerManager, "jobNodeStorage", jobNodeStorage);
    }

    @After
    public void tearDown() {
        JobRegistry.getInstance().shutdown("test_job");
    }
    
    @Test
    public void assertStart() {
        shutdownListenerManager.start();
        verify(jobNodeStorage).addDataListener(ArgumentMatchers.any());
    }
    
    @Test
    public void assertIsShutdownAlready() {
        shutdownListenerManager.new InstanceShutdownStatusJobListener().onChange(new DataChangedEvent(Type.DELETED, "/test_job/instances/127.0.0.1@-@0", ""));
        verify(schedulerFacade, times(0)).shutdownInstance();
    }
    
    @Test
    public void assertIsNotLocalInstancePath() {
        JobRegistry.getInstance().registerRegistryCenter("test_job", regCenter);
        JobRegistry.getInstance().registerJob("test_job", jobScheduleController);
        shutdownListenerManager.new InstanceShutdownStatusJobListener().onChange(new DataChangedEvent(Type.DELETED, "/test_job/instances/127.0.0.2@-@0", ""));
        verify(schedulerFacade, times(0)).shutdownInstance();
    }
    
    @Test
    public void assertUpdateLocalInstancePath() {
        JobRegistry.getInstance().registerRegistryCenter("test_job", regCenter);
        JobRegistry.getInstance().registerJob("test_job", jobScheduleController);
        shutdownListenerManager.new InstanceShutdownStatusJobListener().onChange(new DataChangedEvent(Type.UPDATED, "/test_job/instances/127.0.0.1@-@0", ""));
        verify(schedulerFacade, times(0)).shutdownInstance();
    }
    
    @Test
    public void assertRemoveLocalInstancePathForPausedJob() {
        JobRegistry.getInstance().registerRegistryCenter("test_job", regCenter);
        JobRegistry.getInstance().registerJob("test_job", jobScheduleController);
        when(jobScheduleController.isPaused()).thenReturn(true);
        shutdownListenerManager.new InstanceShutdownStatusJobListener().onChange(new DataChangedEvent(Type.DELETED, "/test_job/instances/127.0.0.1@-@0", ""));
        verify(schedulerFacade, times(0)).shutdownInstance();
    }
    
    @Test
    public void assertRemoveLocalInstancePathForReconnectedRegistryCenter() {
        JobRegistry.getInstance().registerRegistryCenter("test_job", regCenter);
        JobRegistry.getInstance().registerJob("test_job", jobScheduleController);
        when(instanceService.isLocalJobInstanceExisted()).thenReturn(true);
        shutdownListenerManager.new InstanceShutdownStatusJobListener().onChange(new DataChangedEvent(Type.DELETED, "/test_job/instances/127.0.0.1@-@0", ""));
        verify(schedulerFacade, times(0)).shutdownInstance();
    }
    
    @Test
    public void assertRemoveLocalInstancePath() {
        JobRegistry.getInstance().registerRegistryCenter("test_job", regCenter);
        JobRegistry.getInstance().registerJob("test_job", jobScheduleController);
        shutdownListenerManager.new InstanceShutdownStatusJobListener().onChange(new DataChangedEvent(Type.DELETED, "/test_job/instances/127.0.0.1@-@0", ""));
        verify(schedulerFacade).shutdownInstance();
    }
}
