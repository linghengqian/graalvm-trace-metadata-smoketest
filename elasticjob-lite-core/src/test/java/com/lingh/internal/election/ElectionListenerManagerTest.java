package com.lingh.internal.election;

import com.lingh.util.ReflectionUtils;
import org.apache.shardingsphere.elasticjob.infra.handler.sharding.JobInstance;
import org.apache.shardingsphere.elasticjob.lite.internal.election.ElectionListenerManager;
import org.apache.shardingsphere.elasticjob.lite.internal.election.LeaderService;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.lite.internal.server.ServerService;
import org.apache.shardingsphere.elasticjob.lite.internal.storage.JobNodeStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public final class ElectionListenerManagerTest {
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
        verify(jobNodeStorage, times(2)).addDataListener(ArgumentMatchers.any());
    }
}
