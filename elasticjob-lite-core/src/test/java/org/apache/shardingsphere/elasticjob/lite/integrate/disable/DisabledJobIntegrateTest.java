package org.apache.shardingsphere.elasticjob.lite.integrate.disable;

import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.infra.concurrent.BlockUtils;
import org.apache.shardingsphere.elasticjob.infra.env.IpUtils;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.infra.yaml.YamlEngine;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.ScheduleJobBootstrap;
import org.apache.shardingsphere.elasticjob.lite.fixture.job.DetailedFooJob;
import org.apache.shardingsphere.elasticjob.lite.integrate.BaseIntegrateTest;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.lite.internal.server.ServerStatus;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;

public abstract class DisabledJobIntegrateTest extends BaseIntegrateTest {
    
    public DisabledJobIntegrateTest(final TestType type) {
        super(type, new DetailedFooJob());
    }
    
    protected final void assertDisabledRegCenterInfo() {
        assertThat(JobRegistry.getInstance().getCurrentShardingTotalCount(getJobName()), is(3));
        assertThat(JobRegistry.getInstance().getJobInstance(getJobName()).getServerIp(), is(IpUtils.getIp()));
        JobConfiguration jobConfig = YamlEngine.unmarshal(getREGISTRY_CENTER().get("/" + getJobName() + "/config"), JobConfigurationPOJO.class).toJobConfiguration();
        assertThat(jobConfig.getShardingTotalCount(), is(3));
        if (getJobBootstrap() instanceof ScheduleJobBootstrap) {
            assertThat(jobConfig.getCron(), is("0/1 * * * * ?"));
        } else {
            assertNull(jobConfig.getCron());
        }
        assertThat(jobConfig.getShardingItemParameters(), is("0=A,1=B,2=C"));
        assertThat(getREGISTRY_CENTER().get("/" + getJobName() + "/servers/" + JobRegistry.getInstance().getJobInstance(getJobName()).getServerIp()), is(ServerStatus.DISABLED.name()));
        while (null != getREGISTRY_CENTER().get("/" + getJobName() + "/leader/election/instance")) {
            BlockUtils.waitingShortTime();
        }
    }
}
