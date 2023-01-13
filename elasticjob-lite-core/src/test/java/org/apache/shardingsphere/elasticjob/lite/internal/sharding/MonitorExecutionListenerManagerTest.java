package org.apache.shardingsphere.elasticjob.lite.internal.sharding;

import com.lingh.fixture.LiteYamlConstants;
import com.lingh.util.ReflectionUtils;
import org.apache.shardingsphere.elasticjob.lite.internal.storage.JobNodeStorage;
import org.apache.shardingsphere.elasticjob.reg.listener.DataChangedEvent;
import org.apache.shardingsphere.elasticjob.reg.listener.DataChangedEvent.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public final class MonitorExecutionListenerManagerTest {

    @Mock
    private JobNodeStorage jobNodeStorage;

    @Mock
    private ExecutionService executionService;

    private final MonitorExecutionListenerManager monitorExecutionListenerManager = new MonitorExecutionListenerManager(null, "test_job");

    @BeforeEach
    public void setUp() {
        ReflectionUtils.setSuperclassFieldValue(monitorExecutionListenerManager, "jobNodeStorage", jobNodeStorage);
        ReflectionUtils.setFieldValue(monitorExecutionListenerManager, "executionService", executionService);
    }

    @Test
    public void assertMonitorExecutionSettingsChangedJobListenerWhenIsNotFailoverPath() {
        monitorExecutionListenerManager.new MonitorExecutionSettingsChangedJobListener().onChange(new DataChangedEvent(Type.ADDED, "/test_job/other", LiteYamlConstants.getJobYaml()));
        verify(executionService, times(0)).clearAllRunningInfo();
    }

    @Test
    public void assertMonitorExecutionSettingsChangedJobListenerWhenIsFailoverPathButNotUpdate() {
        monitorExecutionListenerManager.new MonitorExecutionSettingsChangedJobListener().onChange(new DataChangedEvent(Type.ADDED, "/test_job/config", ""));
        verify(executionService, times(0)).clearAllRunningInfo();
    }

    @Test
    public void assertMonitorExecutionSettingsChangedJobListenerWhenIsFailoverPathAndUpdateButEnableFailover() {
        monitorExecutionListenerManager.new MonitorExecutionSettingsChangedJobListener().onChange(new DataChangedEvent(Type.UPDATED, "/test_job/config", LiteYamlConstants.getJobYaml()));
        verify(executionService, times(0)).clearAllRunningInfo();
    }

    @Test
    public void assertMonitorExecutionSettingsChangedJobListenerWhenIsFailoverPathAndUpdateButDisableFailover() {
        DataChangedEvent event = new DataChangedEvent(Type.UPDATED, "/test_job/config", LiteYamlConstants.getJobYamlWithMonitorExecution(false));
        monitorExecutionListenerManager.new MonitorExecutionSettingsChangedJobListener().onChange(event);
        verify(executionService).clearAllRunningInfo();
    }
}
