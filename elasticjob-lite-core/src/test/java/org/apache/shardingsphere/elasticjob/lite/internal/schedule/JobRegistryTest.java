package org.apache.shardingsphere.elasticjob.lite.internal.schedule;

import com.lingh.util.ReflectionUtils;
import org.apache.shardingsphere.elasticjob.infra.handler.sharding.JobInstance;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class JobRegistryTest {

    @Test
    public void assertRegisterJob() {
        JobScheduleController jobScheduleController = mock(JobScheduleController.class);
        JobRegistry.getInstance().registerJob("test_job_scheduler_for_add", jobScheduleController);
        assertThat(JobRegistry.getInstance().getJobScheduleController("test_job_scheduler_for_add"), is(jobScheduleController));
    }

    @Test
    public void assertGetJobInstance() {
        JobRegistry.getInstance().addJobInstance("exist_job_instance", new JobInstance("127.0.0.1@-@0"));
        assertThat(JobRegistry.getInstance().getJobInstance("exist_job_instance"), is(new JobInstance("127.0.0.1@-@0")));
    }

    @Test
    public void assertGetRegCenter() {
        CoordinatorRegistryCenter regCenter = mock(CoordinatorRegistryCenter.class);
        JobRegistry.getInstance().registerRegistryCenter("test_job_scheduler_for_add", regCenter);
        assertThat(JobRegistry.getInstance().getRegCenter("test_job_scheduler_for_add"), is(regCenter));
    }

    @Test
    public void assertIsJobRunningIfNull() {
        assertFalse(JobRegistry.getInstance().isJobRunning("null_job_instance"));
    }

    @Test
    public void assertIsJobRunningIfNotNull() {
        JobRegistry.getInstance().setJobRunning("exist_job_instance", true);
        assertTrue(JobRegistry.getInstance().isJobRunning("exist_job_instance"));
    }

    @Test
    public void assertGetCurrentShardingTotalCountIfNull() {
        assertThat(JobRegistry.getInstance().getCurrentShardingTotalCount("exist_job_instance"), is(0));
    }

    @Test
    public void assertGetCurrentShardingTotalCountIfNotNull() {
        JobRegistry.getInstance().setCurrentShardingTotalCount("exist_job_instance", 10);
        assertThat(JobRegistry.getInstance().getCurrentShardingTotalCount("exist_job_instance"), is(10));
        ReflectionUtils.setFieldValue(JobRegistry.getInstance(), "instance", null);
    }

    @Test
    public void assertShutdown() {
        JobScheduleController jobScheduleController = mock(JobScheduleController.class);
        CoordinatorRegistryCenter regCenter = mock(CoordinatorRegistryCenter.class);
        JobRegistry.getInstance().registerRegistryCenter("test_job_for_shutdown", regCenter);
        JobRegistry.getInstance().registerJob("test_job_for_shutdown", jobScheduleController);
        JobRegistry.getInstance().shutdown("test_job_for_shutdown");
        verify(jobScheduleController).shutdown();
        verify(regCenter).evictCacheData("/test_job_for_shutdown");
    }

    @Test
    public void assertIsShutdownForJobSchedulerNull() {
        assertTrue(JobRegistry.getInstance().isShutdown("test_job_for_job_scheduler_null"));
    }

    @Test
    public void assertIsShutdownForJobInstanceNull() {
        JobScheduleController jobScheduleController = mock(JobScheduleController.class);
        CoordinatorRegistryCenter regCenter = mock(CoordinatorRegistryCenter.class);
        JobRegistry.getInstance().registerRegistryCenter("test_job_for_job_instance_null", regCenter);
        JobRegistry.getInstance().registerJob("test_job_for_job_instance_null", jobScheduleController);
        assertTrue(JobRegistry.getInstance().isShutdown("test_job_for_job_instance_null"));
    }

    @Test
    public void assertIsNotShutdown() {
        JobScheduleController jobScheduleController = mock(JobScheduleController.class);
        CoordinatorRegistryCenter regCenter = mock(CoordinatorRegistryCenter.class);
        JobRegistry.getInstance().registerRegistryCenter("test_job_for_job_not_shutdown", regCenter);
        JobRegistry.getInstance().registerJob("test_job_for_job_not_shutdown", jobScheduleController);
        JobRegistry.getInstance().addJobInstance("test_job_for_job_not_shutdown", new JobInstance("127.0.0.1@-@0"));
        assertFalse(JobRegistry.getInstance().isShutdown("test_job_for_job_not_shutdown"));
    }
}
