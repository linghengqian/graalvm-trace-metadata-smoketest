package org.apache.shardingsphere.elasticjob.lite.integrate.enable;

import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.infra.concurrent.BlockUtils;
import org.apache.shardingsphere.elasticjob.lite.fixture.job.DetailedFooJob;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public final class ScheduleEnabledJobIntegrateTest extends EnabledJobIntegrateTest {
    public ScheduleEnabledJobIntegrateTest() {
        super(TestType.SCHEDULE, new DetailedFooJob());
    }
    
    @Override
    protected JobConfiguration getJobConfiguration(final String jobName) {
        return JobConfiguration.newBuilder(jobName, 3).cron("0/1 * * * * ?").shardingItemParameters("0=A,1=B,2=C")
                .jobListenerTypes("INTEGRATE-TEST", "INTEGRATE-DISTRIBUTE").overwrite(true).build();
    }
    
    @Test
    public void assertJobInit() {
        while (!((DetailedFooJob) getElasticJob()).isCompleted()) {
            BlockUtils.waitingShortTime();
        }
        assertTrue(getREGISTRY_CENTER().isExisted("/" + getJobName() + "/sharding"));
    }
}
