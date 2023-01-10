package org.apache.shardingsphere.elasticjob.lite.internal.schedule;

import org.apache.shardingsphere.elasticjob.infra.handler.sharding.JobInstance;
import org.apache.shardingsphere.elasticjob.lite.internal.election.LeaderService;
import org.apache.shardingsphere.elasticjob.lite.internal.sharding.ShardingService;
import org.apache.shardingsphere.elasticjob.lite.util.ReflectionUtils;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public final class SchedulerFacadeTest {
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    @Mock
    private JobScheduleController jobScheduleController;
    
    @Mock
    private LeaderService leaderService;
    
    @Mock
    private ShardingService shardingService;
    
    private SchedulerFacade schedulerFacade;
    
    @Before
    public void setUp() {
        JobRegistry.getInstance().addJobInstance("test_job", new JobInstance("127.0.0.1@-@0"));
        schedulerFacade = new SchedulerFacade(null, "test_job");
        ReflectionUtils.setFieldValue(schedulerFacade, "leaderService", leaderService);
        ReflectionUtils.setFieldValue(schedulerFacade, "shardingService", shardingService);
    }
    
    @Test
    public void assertShutdownInstanceIfNotLeaderAndReconcileServiceIsNotRunning() {
        JobRegistry.getInstance().registerRegistryCenter("test_job", regCenter);
        JobRegistry.getInstance().registerJob("test_job", jobScheduleController);
        schedulerFacade.shutdownInstance();
        verify(leaderService, times(0)).removeLeader();
        verify(jobScheduleController).shutdown();
    }
    
    @Test
    public void assertShutdownInstanceIfLeaderAndReconcileServiceIsRunning() {
        when(leaderService.isLeader()).thenReturn(true);
        JobRegistry.getInstance().registerRegistryCenter("test_job", regCenter);
        JobRegistry.getInstance().registerJob("test_job", jobScheduleController);
        schedulerFacade.shutdownInstance();
        verify(leaderService).removeLeader();
        verify(jobScheduleController).shutdown();
    }
}
