package com.lingh.api.listener;

import com.google.common.collect.Sets;
import com.lingh.api.listener.fixture.ElasticJobListenerCaller;
import com.lingh.api.listener.fixture.TestDistributeOnceElasticJobListener;
import com.lingh.util.ReflectionUtils;
import org.apache.shardingsphere.elasticjob.infra.env.TimeService;
import org.apache.shardingsphere.elasticjob.infra.exception.JobSystemException;
import org.apache.shardingsphere.elasticjob.infra.listener.ShardingContexts;
import org.apache.shardingsphere.elasticjob.lite.internal.guarantee.GuaranteeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public final class DistributeOnceElasticJobListenerTest {

    @Mock
    private GuaranteeService guaranteeService;

    @Mock
    private TimeService timeService;

    @Mock
    private ElasticJobListenerCaller elasticJobListenerCaller;

    private ShardingContexts shardingContexts;

    private TestDistributeOnceElasticJobListener distributeOnceElasticJobListener;

    @BeforeEach
    public void setUp() {
        distributeOnceElasticJobListener = new TestDistributeOnceElasticJobListener(elasticJobListenerCaller);
        distributeOnceElasticJobListener.setGuaranteeService(guaranteeService);
        ReflectionUtils.setSuperclassFieldValue(distributeOnceElasticJobListener, "timeService", timeService);
        Map<Integer, String> map = new HashMap<>(2, 1);
        map.put(0, "");
        map.put(1, "");
        shardingContexts = new ShardingContexts("fake_task_id", "test_job", 10, "", map);
    }

    @Test
    public void assertBeforeJobExecutedWhenIsAllStarted() {
        when(guaranteeService.isRegisterStartSuccess(Sets.newHashSet(0, 1))).thenReturn(true);
        when(guaranteeService.isAllStarted()).thenReturn(true);
        distributeOnceElasticJobListener.beforeJobExecuted(shardingContexts);
        verify(guaranteeService).registerStart(Sets.newHashSet(0, 1));
        verify(elasticJobListenerCaller).before();
        verify(guaranteeService).clearAllStartedInfo();
    }

    @Test
    public void assertBeforeJobExecutedWhenIsNotAllStartedAndNotTimeout() {
        when(guaranteeService.isRegisterStartSuccess(Sets.newHashSet(0, 1))).thenReturn(true);
        when(guaranteeService.isAllStarted()).thenReturn(false);
        when(timeService.getCurrentMillis()).thenReturn(0L);
        distributeOnceElasticJobListener.beforeJobExecuted(shardingContexts);
        verify(guaranteeService).registerStart(Sets.newHashSet(0, 1));
        verify(guaranteeService, times(0)).clearAllStartedInfo();
    }

    @Test
    public void assertBeforeJobExecutedWhenIsNotAllStartedAndTimeout() {
        assertThrows(JobSystemException.class, () -> {
            when(guaranteeService.isRegisterStartSuccess(Sets.newHashSet(0, 1))).thenReturn(true);
            when(guaranteeService.isAllStarted()).thenReturn(false);
            when(timeService.getCurrentMillis()).thenReturn(0L, 2L);
            distributeOnceElasticJobListener.beforeJobExecuted(shardingContexts);
            verify(guaranteeService).registerStart(Arrays.asList(0, 1));
            verify(guaranteeService, times(0)).clearAllStartedInfo();
        });
    }

    @Test
    public void assertAfterJobExecutedWhenIsAllCompleted() {
        when(guaranteeService.isRegisterCompleteSuccess(Sets.newHashSet(0, 1))).thenReturn(true);
        when(guaranteeService.isAllCompleted()).thenReturn(true);
        distributeOnceElasticJobListener.afterJobExecuted(shardingContexts);
        verify(guaranteeService).registerComplete(Sets.newHashSet(0, 1));
        verify(elasticJobListenerCaller).after();
        verify(guaranteeService).clearAllCompletedInfo();
    }

    @Test
    public void assertAfterJobExecutedWhenIsAllCompletedAndNotTimeout() {
        when(guaranteeService.isRegisterCompleteSuccess(Sets.newHashSet(0, 1))).thenReturn(true);
        when(guaranteeService.isAllCompleted()).thenReturn(false);
        when(timeService.getCurrentMillis()).thenReturn(0L);
        distributeOnceElasticJobListener.afterJobExecuted(shardingContexts);
        verify(guaranteeService).registerComplete(Sets.newHashSet(0, 1));
        verify(guaranteeService, times(0)).clearAllCompletedInfo();
    }

    @Test
    public void assertAfterJobExecutedWhenIsAllCompletedAndTimeout() {
        assertThrows(JobSystemException.class, () -> {
            when(guaranteeService.isRegisterCompleteSuccess(Sets.newHashSet(0, 1))).thenReturn(true);
            when(guaranteeService.isAllCompleted()).thenReturn(false);
            when(timeService.getCurrentMillis()).thenReturn(0L, 2L);
            distributeOnceElasticJobListener.afterJobExecuted(shardingContexts);
            verify(guaranteeService).registerComplete(Arrays.asList(0, 1));
            verify(guaranteeService, times(0)).clearAllCompletedInfo();
        });
    }
}
