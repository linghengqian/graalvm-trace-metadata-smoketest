package org.apache.shardingsphere.elasticjob.lite.internal.snapshot;

import com.lingh.fixture.EmbedTestingServer;
import com.lingh.util.ReflectionUtils;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.elasticjob.api.ElasticJob;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.ScheduleJobBootstrap;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperConfiguration;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperRegistryCenter;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

@SuppressWarnings("FieldMayBeFinal")
public abstract class BaseSnapshotServiceTest {
    
    static final int DUMP_PORT = 9000;
    
    private static final ZookeeperConfiguration ZOOKEEPER_CONFIG = new ZookeeperConfiguration(EmbedTestingServer.getConnectionString(), "zkRegTestCenter");
    
    @Getter(value = AccessLevel.PROTECTED)
    private static final CoordinatorRegistryCenter REG_CENTER = new ZookeeperRegistryCenter(ZOOKEEPER_CONFIG);
    
    @Getter(value = AccessLevel.PROTECTED)
    private static SnapshotService snapshotService = new SnapshotService(REG_CENTER, DUMP_PORT);
    
    private final ScheduleJobBootstrap bootstrap;
    
    @Getter(value = AccessLevel.PROTECTED)
    private final String jobName = System.nanoTime() + "_test_job";
    
    public BaseSnapshotServiceTest(final ElasticJob elasticJob) {
        bootstrap = new ScheduleJobBootstrap(REG_CENTER, elasticJob, JobConfiguration.newBuilder(jobName, 3).cron("0/1 * * * * ?").overwrite(true).build());
    }
    
    @BeforeClass
    public static void init() {
        EmbedTestingServer.start();
        ZOOKEEPER_CONFIG.setConnectionTimeoutMilliseconds(30000);
        REG_CENTER.init();
    }
    
    @Before
    public final void setUp() {
        REG_CENTER.init();
        bootstrap.schedule();
    }
    
    @After
    public final void tearDown() {
        bootstrap.shutdown();
        ReflectionUtils.setFieldValue(JobRegistry.getInstance(), "instance", null);
    }
}
