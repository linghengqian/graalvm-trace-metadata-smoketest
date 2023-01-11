package com.lingh.internal.trigger;

import com.lingh.util.ReflectionUtils;
import org.apache.shardingsphere.elasticjob.infra.handler.sharding.JobInstance;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.lite.internal.storage.JobNodeStorage;
import org.apache.shardingsphere.elasticjob.lite.internal.trigger.TriggerListenerManager;
import org.apache.shardingsphere.elasticjob.lite.internal.trigger.TriggerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public final class TriggerListenerManagerTest {

    @Mock
    private JobNodeStorage jobNodeStorage;

    @Mock
    private TriggerService triggerService;

    private TriggerListenerManager triggerListenerManager;

    @BeforeEach
    public void setUp() {
        JobRegistry.getInstance().addJobInstance("test_job", new JobInstance("127.0.0.1@-@0"));
        triggerListenerManager = new TriggerListenerManager(null, "test_job");
        ReflectionUtils.setFieldValue(triggerListenerManager, "triggerService", triggerService);
        ReflectionUtils.setSuperclassFieldValue(triggerListenerManager, "jobNodeStorage", jobNodeStorage);
    }

    @Test
    public void assertStart() {
        triggerListenerManager.start();
        verify(jobNodeStorage).addDataListener(ArgumentMatchers.any());
    }
}
