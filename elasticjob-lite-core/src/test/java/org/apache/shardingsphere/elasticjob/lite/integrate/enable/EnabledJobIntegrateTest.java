package org.apache.shardingsphere.elasticjob.lite.integrate.enable;

import org.apache.shardingsphere.elasticjob.api.ElasticJob;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.infra.env.IpUtils;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.infra.yaml.YamlEngine;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.ScheduleJobBootstrap;
import org.apache.shardingsphere.elasticjob.lite.integrate.BaseIntegrateTest;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.lite.internal.server.ServerStatus;
import org.junit.Before;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public abstract class EnabledJobIntegrateTest extends BaseIntegrateTest {
    protected EnabledJobIntegrateTest(final TestType type, final ElasticJob elasticJob) {
        super(type, elasticJob);
    }

    @Before
    public final void assertEnabledRegCenterInfo() {
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
        assertThat(getREGISTRY_CENTER().get("/" + getJobName() + "/servers/" + JobRegistry.getInstance().getJobInstance(getJobName()).getServerIp()), is(ServerStatus.ENABLED.name()));
        assertThat(getREGISTRY_CENTER().get("/" + getJobName() + "/leader/election/instance"), is(JobRegistry.getInstance().getJobInstance(getJobName()).getJobInstanceId()));
        assertTrue(getREGISTRY_CENTER().isExisted("/" + getJobName() + "/instances/" + JobRegistry.getInstance().getJobInstance(getJobName()).getJobInstanceId()));
        getREGISTRY_CENTER().remove("/" + getJobName() + "/leader/election");
        assertTrue(getLeaderService().isLeaderUntilBlock());
    }
}
