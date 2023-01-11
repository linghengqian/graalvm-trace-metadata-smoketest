package org.apache.shardingsphere.elasticjob.lite.internal.election;

import com.lingh.util.ReflectionUtils;
import org.apache.shardingsphere.elasticjob.infra.handler.sharding.JobInstance;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobScheduleController;
import org.apache.shardingsphere.elasticjob.lite.internal.server.ServerService;
import org.apache.shardingsphere.elasticjob.lite.internal.server.ServerStatus;
import org.apache.shardingsphere.elasticjob.lite.internal.storage.JobNodeStorage;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.listener.DataChangedEvent;
import org.apache.shardingsphere.elasticjob.reg.listener.DataChangedEvent.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public final class ElectionListenerManagerTest {
    @Mock
    private CoordinatorRegistryCenter regCenter;
    @Mock
    private JobScheduleController jobScheduleController;
    @Mock
    private JobNodeStorage jobNodeStorage;
    @Mock
    private LeaderService leaderService;
    @Mock
    private ServerService serverService;
    private final ElectionListenerManager electionListenerManager = new ElectionListenerManager(null, "test_job");

    @BeforeEach
    public void setUp() {
        JobRegistry.getInstance().addJobInstance("test_job", new JobInstance("127.0.0.1@-@0", null, "127.0.0.1"));
        ReflectionUtils.setSuperclassFieldValue(electionListenerManager, "jobNodeStorage", jobNodeStorage);
        ReflectionUtils.setFieldValue(electionListenerManager, "leaderService", leaderService);
        ReflectionUtils.setFieldValue(electionListenerManager, "serverService", serverService);
    }

    @Test
    public void assertStart() {
        electionListenerManager.start();
        verify(jobNodeStorage, times(2)).addDataListener(ArgumentMatchers.<ElectionListenerManager.LeaderElectionJobListener>any());
    }

    @Test
    public void assertIsNotLeaderInstancePathAndServerPath() {
        electionListenerManager.new LeaderElectionJobListener().onChange(new DataChangedEvent(Type.DELETED, "/test_job/leader/election/other", "127.0.0.1"));
        verify(leaderService, times(0)).electLeader();
    }

    @Test
    public void assertLeaderElectionWhenAddLeaderInstancePath() {
        electionListenerManager.new LeaderElectionJobListener().onChange(new DataChangedEvent(Type.ADDED, "/test_job/leader/election/instance", "127.0.0.1"));
        verify(leaderService, times(0)).electLeader();
    }

    @Test
    public void assertLeaderElectionWhenRemoveLeaderInstancePathWithoutAvailableServers() {
        electionListenerManager.new LeaderElectionJobListener().onChange(new DataChangedEvent(Type.DELETED, "/test_job/leader/election/instance", "127.0.0.1"));
        verify(leaderService, times(0)).electLeader();
    }

    @Test
    public void assertLeaderElectionWhenRemoveLeaderInstancePathWithAvailableServerButJobInstanceIsShutdown() {
        electionListenerManager.new LeaderElectionJobListener().onChange(new DataChangedEvent(Type.DELETED, "/test_job/leader/election/instance", "127.0.0.1"));
        verify(leaderService, times(0)).electLeader();
    }

    @Test
    public void assertLeaderElectionWhenRemoveLeaderInstancePathWithAvailableServer() {
        JobRegistry.getInstance().registerRegistryCenter("test_job", regCenter);
        JobRegistry.getInstance().registerJob("test_job", jobScheduleController);
        when(serverService.isAvailableServer("127.0.0.1")).thenReturn(true);
        electionListenerManager.new LeaderElectionJobListener().onChange(new DataChangedEvent(Type.DELETED, "/test_job/leader/election/instance", "127.0.0.1"));
        verify(leaderService).electLeader();
        JobRegistry.getInstance().shutdown("test_job");
    }

    @Test
    public void assertLeaderElectionWhenServerDisableWithoutLeader() {
        electionListenerManager.new LeaderElectionJobListener().onChange(new DataChangedEvent(Type.DELETED, "/test_job/servers/127.0.0.1", ServerStatus.DISABLED.name()));
        verify(leaderService, times(0)).electLeader();
    }

    @Test
    public void assertLeaderElectionWhenServerEnableWithLeader() {
        electionListenerManager.new LeaderElectionJobListener().onChange(new DataChangedEvent(Type.UPDATED, "/test_job/servers/127.0.0.1", ""));
        verify(leaderService, times(0)).electLeader();
    }

    @Test
    public void assertLeaderElectionWhenServerEnableWithoutLeader() {
        JobRegistry.getInstance().registerRegistryCenter("test_job", regCenter);
        JobRegistry.getInstance().registerJob("test_job", jobScheduleController);
        electionListenerManager.new LeaderElectionJobListener().onChange(new DataChangedEvent(Type.UPDATED, "/test_job/servers/127.0.0.1", ""));
        verify(leaderService).electLeader();
        JobRegistry.getInstance().shutdown("test_job");
    }

    @Test
    public void assertLeaderAbdicationWhenFollowerDisable() {
        electionListenerManager.new LeaderAbdicationJobListener().onChange(new DataChangedEvent(Type.UPDATED, "/test_job/servers/127.0.0.1", ServerStatus.DISABLED.name()));
        verify(leaderService, times(0)).removeLeader();
    }

    @Test
    public void assertLeaderAbdicationWhenLeaderDisable() {
        when(leaderService.isLeader()).thenReturn(true);
        electionListenerManager.new LeaderAbdicationJobListener().onChange(new DataChangedEvent(Type.UPDATED, "/test_job/servers/127.0.0.1", ServerStatus.DISABLED.name()));
        verify(leaderService).removeLeader();
    }
}
