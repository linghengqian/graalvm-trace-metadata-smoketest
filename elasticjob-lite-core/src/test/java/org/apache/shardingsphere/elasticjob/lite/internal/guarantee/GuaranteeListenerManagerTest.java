package org.apache.shardingsphere.elasticjob.lite.internal.guarantee;

import com.lingh.util.ReflectionUtils;
import org.apache.shardingsphere.elasticjob.infra.listener.ElasticJobListener;
import org.apache.shardingsphere.elasticjob.lite.api.listener.AbstractDistributeOnceElasticJobListener;
import org.apache.shardingsphere.elasticjob.lite.internal.guarantee.GuaranteeListenerManager;
import org.apache.shardingsphere.elasticjob.lite.internal.storage.JobNodeStorage;
import org.apache.shardingsphere.elasticjob.reg.listener.DataChangedEvent;
import org.apache.shardingsphere.elasticjob.reg.listener.DataChangedEvent.Type;
import org.apache.shardingsphere.elasticjob.reg.listener.DataChangedEventListener;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class GuaranteeListenerManagerTest {
    @Mock
    private JobNodeStorage jobNodeStorage;
    @Mock
    private ElasticJobListener elasticJobListener;
    @Mock
    private AbstractDistributeOnceElasticJobListener distributeOnceElasticJobListener;
    private GuaranteeListenerManager guaranteeListenerManager;

    @Before
    public void setUp() {
        guaranteeListenerManager = new GuaranteeListenerManager(null, "test_job", Arrays.asList(elasticJobListener, distributeOnceElasticJobListener));
        ReflectionUtils.setSuperclassFieldValue(guaranteeListenerManager, "jobNodeStorage", jobNodeStorage);
    }

    @Test
    public void assertStart() {
        guaranteeListenerManager.start();
        verify(jobNodeStorage, times(2)).addDataListener(any(DataChangedEventListener.class));
    }

    @Test
    public void assertStartedNodeRemovedJobListenerWhenIsNotRemoved() {
        guaranteeListenerManager.new StartedNodeRemovedJobListener().onChange(new DataChangedEvent(Type.UPDATED, "/test_job/guarantee/started", ""));
        verify(distributeOnceElasticJobListener, times(0)).notifyWaitingTaskStart();
    }

    @Test
    public void assertStartedNodeRemovedJobListenerWhenIsNotStartedNode() {
        guaranteeListenerManager.new StartedNodeRemovedJobListener().onChange(new DataChangedEvent(Type.DELETED, "/other_job/guarantee/started", ""));
        verify(distributeOnceElasticJobListener, times(0)).notifyWaitingTaskStart();
    }

    @Test
    public void assertStartedNodeRemovedJobListenerWhenIsRemovedAndStartedNode() {
        guaranteeListenerManager.new StartedNodeRemovedJobListener().onChange(new DataChangedEvent(Type.DELETED, "/test_job/guarantee/started", ""));
        verify(distributeOnceElasticJobListener).notifyWaitingTaskStart();
    }

    @Test
    public void assertCompletedNodeRemovedJobListenerWhenIsNotRemoved() {
        guaranteeListenerManager.new CompletedNodeRemovedJobListener().onChange(new DataChangedEvent(Type.UPDATED, "/test_job/guarantee/completed", ""));
        verify(distributeOnceElasticJobListener, times(0)).notifyWaitingTaskStart();
    }

    @Test
    public void assertCompletedNodeRemovedJobListenerWhenIsNotCompletedNode() {
        guaranteeListenerManager.new CompletedNodeRemovedJobListener().onChange(new DataChangedEvent(Type.DELETED, "/other_job/guarantee/completed", ""));
        verify(distributeOnceElasticJobListener, times(0)).notifyWaitingTaskStart();
    }

    @Test
    public void assertCompletedNodeRemovedJobListenerWhenIsRemovedAndCompletedNode() {
        guaranteeListenerManager.new CompletedNodeRemovedJobListener().onChange(new DataChangedEvent(Type.DELETED, "/test_job/guarantee/completed", ""));
        verify(distributeOnceElasticJobListener).notifyWaitingTaskComplete();
    }
}
