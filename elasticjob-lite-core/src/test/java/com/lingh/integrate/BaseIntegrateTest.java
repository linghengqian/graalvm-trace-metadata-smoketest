package com.lingh.integrate;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.elasticjob.api.ElasticJob;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.JobBootstrap;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.OneOffJobBootstrap;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.ScheduleJobBootstrap;
import com.lingh.fixture.EmbedTestingServer;
import org.apache.shardingsphere.elasticjob.lite.internal.election.LeaderService;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobRegistry;
import com.lingh.util.ReflectionUtils;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperConfiguration;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperRegistryCenter;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

@Getter(AccessLevel.PROTECTED)
public abstract class BaseIntegrateTest {
    private static final ZookeeperConfiguration ZOOKEEPER_CONFIG = new ZookeeperConfiguration(EmbedTestingServer.getConnectionString(), "zkRegTestCenter");
    @Getter(AccessLevel.PROTECTED)
    private static final CoordinatorRegistryCenter REGISTRY_CENTER = new ZookeeperRegistryCenter(ZOOKEEPER_CONFIG);
    private final ElasticJob elasticJob;
    private final JobConfiguration jobConfiguration;
    private final JobBootstrap jobBootstrap;
    private final LeaderService leaderService;
    private final String jobName = System.nanoTime() + "_test_job";

    protected BaseIntegrateTest(final TestType type, final ElasticJob elasticJob) {
        this.elasticJob = elasticJob;
        jobConfiguration = getJobConfiguration(jobName);
        jobBootstrap = createJobBootstrap(type, elasticJob);
        leaderService = new LeaderService(REGISTRY_CENTER, jobName);
    }

    protected abstract JobConfiguration getJobConfiguration(String jobName);

    @SuppressWarnings("UnnecessaryDefault")
    private JobBootstrap createJobBootstrap(final TestType type, final ElasticJob elasticJob) {
        return switch (type) {
            case SCHEDULE -> new ScheduleJobBootstrap(REGISTRY_CENTER, elasticJob, jobConfiguration);
            case ONE_OFF -> new OneOffJobBootstrap(REGISTRY_CENTER, elasticJob, jobConfiguration);
            default -> throw new RuntimeException(String.format("Cannot support `%s`", type));
        };
    }

    @BeforeClass
    public static void init() {
        EmbedTestingServer.start();
        ZOOKEEPER_CONFIG.setConnectionTimeoutMilliseconds(30000);
        REGISTRY_CENTER.init();
    }

    @Before
    public void setUp() {
        if (jobBootstrap instanceof ScheduleJobBootstrap) {
            ((ScheduleJobBootstrap) jobBootstrap).schedule();
        } else {
            ((OneOffJobBootstrap) jobBootstrap).execute();
        }
    }

    @After
    public void tearDown() {
        jobBootstrap.shutdown();
        ReflectionUtils.setFieldValue(JobRegistry.getInstance(), "instance", null);
    }

    public enum TestType {

        SCHEDULE, ONE_OFF
    }
}
