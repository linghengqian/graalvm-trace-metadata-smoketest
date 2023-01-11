package com.lingh.internal.failover;

import com.lingh.util.ReflectionUtils;
import org.apache.shardingsphere.elasticjob.lite.internal.config.ConfigurationService;
import org.apache.shardingsphere.elasticjob.lite.internal.failover.FailoverListenerManager;
import org.apache.shardingsphere.elasticjob.lite.internal.failover.FailoverService;
import org.apache.shardingsphere.elasticjob.lite.internal.instance.InstanceNode;
import org.apache.shardingsphere.elasticjob.lite.internal.instance.InstanceService;
import org.apache.shardingsphere.elasticjob.lite.internal.sharding.ExecutionService;
import org.apache.shardingsphere.elasticjob.lite.internal.sharding.ShardingService;
import org.apache.shardingsphere.elasticjob.lite.internal.storage.JobNodeStorage;
import org.apache.shardingsphere.elasticjob.reg.listener.DataChangedEventListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public final class FailoverListenerManagerTest {
    @Mock
    private JobNodeStorage jobNodeStorage;
    @Mock
    private ConfigurationService configService;
    @Mock
    private ShardingService shardingService;
    @Mock
    private FailoverService failoverService;
    @Mock
    private InstanceService instanceService;
    @Mock
    private ExecutionService executionService;
    @Mock
    private InstanceNode instanceNode;
    private final FailoverListenerManager failoverListenerManager = new FailoverListenerManager(null, "test_job");

    @BeforeEach
    public void setUp() {
        ReflectionUtils.setSuperclassFieldValue(failoverListenerManager, "jobNodeStorage", jobNodeStorage);
        ReflectionUtils.setFieldValue(failoverListenerManager, "configService", configService);
        ReflectionUtils.setFieldValue(failoverListenerManager, "shardingService", shardingService);
        ReflectionUtils.setFieldValue(failoverListenerManager, "failoverService", failoverService);
        ReflectionUtils.setFieldValue(failoverListenerManager, "instanceService", instanceService);
        ReflectionUtils.setFieldValue(failoverListenerManager, "executionService", executionService);
        ReflectionUtils.setFieldValue(failoverListenerManager, "instanceNode", instanceNode);
    }

    @Test
    public void assertStart() {
        failoverListenerManager.start();
        verify(jobNodeStorage, times(3)).addDataListener(ArgumentMatchers.any(DataChangedEventListener.class));
    }
}
