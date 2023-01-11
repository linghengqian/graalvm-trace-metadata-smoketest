package org.apache.shardingsphere.elasticjob.lite.internal.setup;

import com.lingh.util.ReflectionUtils;
import org.apache.shardingsphere.elasticjob.api.ElasticJob;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.infra.handler.sharding.JobInstance;
import org.apache.shardingsphere.elasticjob.lite.internal.config.ConfigurationService;
import org.apache.shardingsphere.elasticjob.lite.internal.election.LeaderService;
import org.apache.shardingsphere.elasticjob.lite.internal.instance.InstanceService;
import org.apache.shardingsphere.elasticjob.lite.internal.listener.ListenerManager;
import org.apache.shardingsphere.elasticjob.lite.internal.reconcile.ReconcileService;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.lite.internal.server.ServerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public final class SetUpFacadeTest {
    @Mock
    private ConfigurationService configService;

    @Mock
    private LeaderService leaderService;

    @Mock
    private ServerService serverService;

    @Mock
    private InstanceService instanceService;

    @Mock
    private ReconcileService reconcileService;

    @Mock
    private ListenerManager listenerManager;

    private SetUpFacade setUpFacade;

    @BeforeEach
    public void setUp() {
        JobRegistry.getInstance().addJobInstance("test_job", new JobInstance("127.0.0.1@-@0"));
        setUpFacade = new SetUpFacade(null, "test_job", Collections.emptyList());
        ReflectionUtils.setFieldValue(setUpFacade, "configService", configService);
        ReflectionUtils.setFieldValue(setUpFacade, "leaderService", leaderService);
        ReflectionUtils.setFieldValue(setUpFacade, "serverService", serverService);
        ReflectionUtils.setFieldValue(setUpFacade, "instanceService", instanceService);
        ReflectionUtils.setFieldValue(setUpFacade, "reconcileService", reconcileService);
        ReflectionUtils.setFieldValue(setUpFacade, "listenerManager", listenerManager);
    }

    @Test
    public void assertSetUpJobConfiguration() {
        JobConfiguration jobConfig = JobConfiguration.newBuilder("test_job", 3)
                .cron("0/1 * * * * ?").setProperty("streaming.process", Boolean.TRUE.toString()).build();
        when(configService.setUpJobConfiguration(ElasticJob.class.getName(), jobConfig)).thenReturn(jobConfig);
        assertThat(setUpFacade.setUpJobConfiguration(ElasticJob.class.getName(), jobConfig), is(jobConfig));
        verify(configService).setUpJobConfiguration(ElasticJob.class.getName(), jobConfig);
    }

    @Test
    public void assertRegisterStartUpInfo() {
        setUpFacade.registerStartUpInfo(true);
        verify(listenerManager).startAllListeners();
        verify(leaderService).electLeader();
        verify(serverService).persistOnline(true);
    }

    @Test
    public void assertTearDown() {
        when(reconcileService.isRunning()).thenReturn(true);
        setUpFacade.tearDown();
        verify(reconcileService).stopAsync();
    }
}
