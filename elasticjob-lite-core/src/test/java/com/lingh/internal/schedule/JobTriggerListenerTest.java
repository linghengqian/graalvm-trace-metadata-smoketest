package com.lingh.internal.schedule;

import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobTriggerListener;
import org.apache.shardingsphere.elasticjob.lite.internal.sharding.ExecutionService;
import org.apache.shardingsphere.elasticjob.lite.internal.sharding.ShardingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.Trigger;

import java.util.Collections;
import java.util.Date;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public final class JobTriggerListenerTest {

    @Mock
    private ExecutionService executionService;

    @Mock
    private ShardingService shardingService;

    @Mock
    private Trigger trigger;

    private JobTriggerListener jobTriggerListener;

    @BeforeEach
    public void setUp() {
        jobTriggerListener = new JobTriggerListener(executionService, shardingService);
    }

    @Test
    public void assertGetName() {
        assertThat(jobTriggerListener.getName(), is("JobTriggerListener"));
    }

    @Test
    public void assertTriggerMisfiredWhenPreviousFireTimeIsNull() {
        jobTriggerListener.triggerMisfired(trigger);
        verify(executionService, times(0)).setMisfire(Collections.singletonList(0));
    }

    @Test
    public void assertTriggerMisfiredWhenPreviousFireTimeIsNotNull() {
        when(shardingService.getLocalShardingItems()).thenReturn(Collections.singletonList(0));
        when(trigger.getPreviousFireTime()).thenReturn(new Date());
        jobTriggerListener.triggerMisfired(trigger);
        verify(executionService).setMisfire(Collections.singletonList(0));
    }
}
