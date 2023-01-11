package org.apache.shardingsphere.elasticjob.lite.internal.listener;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.Executor;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(MockitoExtension.class)
public class ListenerNotifierManagerTest {

    @Test
    public void assertRegisterAndGetJobNotifyExecutor() {
        String jobName = "test_job";
        ListenerNotifierManager.getInstance().registerJobNotifyExecutor(jobName);
        assertThat(ListenerNotifierManager.getInstance().getJobNotifyExecutor(jobName), notNullValue(Executor.class));
    }

    @Test
    public void assertRemoveAndShutDownJobNotifyExecutor() {
        String jobName = "test_job";
        ListenerNotifierManager.getInstance().registerJobNotifyExecutor(jobName);
        ListenerNotifierManager.getInstance().removeJobNotifyExecutor(jobName);
    }
}
