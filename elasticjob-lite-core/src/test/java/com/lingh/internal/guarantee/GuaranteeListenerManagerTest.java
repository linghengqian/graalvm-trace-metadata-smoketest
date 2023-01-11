package com.lingh.internal.guarantee;

import com.lingh.util.ReflectionUtils;
import org.apache.shardingsphere.elasticjob.infra.listener.ElasticJobListener;
import org.apache.shardingsphere.elasticjob.lite.api.listener.AbstractDistributeOnceElasticJobListener;
import org.apache.shardingsphere.elasticjob.lite.internal.guarantee.GuaranteeListenerManager;
import org.apache.shardingsphere.elasticjob.lite.internal.storage.JobNodeStorage;
import org.apache.shardingsphere.elasticjob.reg.listener.DataChangedEventListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public final class GuaranteeListenerManagerTest {
    @Mock
    private JobNodeStorage jobNodeStorage;
    @Mock
    private ElasticJobListener elasticJobListener;
    @Mock
    private AbstractDistributeOnceElasticJobListener distributeOnceElasticJobListener;
    private GuaranteeListenerManager guaranteeListenerManager;

    @BeforeEach
    public void setUp() {
        guaranteeListenerManager = new GuaranteeListenerManager(null, "test_job", Arrays.asList(elasticJobListener, distributeOnceElasticJobListener));
        ReflectionUtils.setSuperclassFieldValue(guaranteeListenerManager, "jobNodeStorage", jobNodeStorage);
    }

    @Test
    public void assertStart() {
        guaranteeListenerManager.start();
        verify(jobNodeStorage, times(2)).addDataListener(any(DataChangedEventListener.class));
    }
}
