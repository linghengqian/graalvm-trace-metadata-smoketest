package org.apache.shardingsphere.elasticjob.lite.internal.listener;

import org.apache.shardingsphere.elasticjob.lite.internal.listener.ListenerNotifierManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.Executor;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(MockitoJUnitRunner.class)
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
