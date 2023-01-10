package com.lingh.integrate.disable;

import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.infra.concurrent.BlockUtils;
import com.lingh.fixture.job.DetailedFooJob;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.lite.internal.server.ServerStatus;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public final class ScheduleDisabledJobIntegrateTest extends DisabledJobIntegrateTest {
    public ScheduleDisabledJobIntegrateTest() {
        super(TestType.SCHEDULE);
    }
    
    @Override
    protected JobConfiguration getJobConfiguration(final String jobName) {
        return JobConfiguration.newBuilder(jobName, 3).cron("0/1 * * * * ?").shardingItemParameters("0=A,1=B,2=C")
                .jobListenerTypes("INTEGRATE-TEST", "INTEGRATE-DISTRIBUTE").disabled(true).overwrite(true).build();
    }
    
    @Test
    public void assertJobRunning() {
        BlockUtils.waitingShortTime();
        assertDisabledRegCenterInfo();
        setJobEnable();
        while (!((DetailedFooJob) getElasticJob()).isCompleted()) {
            BlockUtils.waitingShortTime();
        }
        assertEnabledRegCenterInfo();
    }
    
    private void setJobEnable() {
        getREGISTRY_CENTER().persist("/" + getJobName() + "/servers/" + JobRegistry.getInstance().getJobInstance(getJobName()).getServerIp(), ServerStatus.ENABLED.name());
    }
    
    private void assertEnabledRegCenterInfo() {
        assertTrue(getREGISTRY_CENTER().isExisted("/" + getJobName() + "/instances/" + JobRegistry.getInstance().getJobInstance(getJobName()).getJobInstanceId()));
        getREGISTRY_CENTER().remove("/" + getJobName() + "/leader/election");
        assertTrue(getREGISTRY_CENTER().isExisted("/" + getJobName() + "/sharding"));
    }
}
