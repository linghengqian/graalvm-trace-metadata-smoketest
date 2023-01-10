package org.apache.shardingsphere.elasticjob.lite.internal.reconcile;

import com.google.common.collect.Lists;
import com.lingh.util.ReflectionUtils;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.infra.handler.sharding.JobInstance;
import org.apache.shardingsphere.elasticjob.lite.internal.config.ConfigurationService;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.lite.internal.sharding.ShardingService;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ReconcileServiceTest {

    @Mock
    private ConfigurationService configService;

    @Mock
    private ShardingService shardingService;

    @Mock
    private CoordinatorRegistryCenter regCenter;

    private ReconcileService reconcileService;

    @Before
    public void setup() {
        JobRegistry.getInstance().addJobInstance("test_job", new JobInstance("127.0.0.1@-@0"));
        reconcileService = new ReconcileService(regCenter, "test_job");
        ReflectionUtils.setFieldValue(reconcileService, "lastReconcileTime", 1L);
        ReflectionUtils.setFieldValue(reconcileService, "configService", configService);
        ReflectionUtils.setFieldValue(reconcileService, "shardingService", shardingService);
    }

    @Test
    public void assertReconcile() {
        when(configService.load(true)).thenReturn(JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").reconcileIntervalMinutes(1).build());
        when(shardingService.isNeedSharding()).thenReturn(false);
        when(shardingService.hasShardingInfoInOfflineServers()).thenReturn(true);
        reconcileService.runOneIteration();
        verify(shardingService).isNeedSharding();
        verify(shardingService).hasShardingInfoInOfflineServers();
        verify(shardingService).setReshardingFlag();
    }

    @Test
    public void assertReconcileWithStaticSharding() {
        when(configService.load(true)).thenReturn(JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").reconcileIntervalMinutes(1).staticSharding(true).build());
        when(shardingService.isNeedSharding()).thenReturn(false);
        when(shardingService.hasShardingInfoInOfflineServers()).thenReturn(true);
        when(regCenter.getChildrenKeys("/test_job/sharding")).thenReturn(Lists.newArrayList("0"));
        reconcileService.runOneIteration();
        verify(shardingService).isNeedSharding();
        verify(shardingService).hasShardingInfoInOfflineServers();
        verify(shardingService, times(0)).setReshardingFlag();
    }
}
