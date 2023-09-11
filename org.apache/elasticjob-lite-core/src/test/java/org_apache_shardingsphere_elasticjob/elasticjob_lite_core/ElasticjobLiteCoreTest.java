package org_apache_shardingsphere_elasticjob.elasticjob_lite_core;

import org.apache.curator.CuratorZookeeperClient;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.dataflow.job.DataflowJob;
import org.apache.shardingsphere.elasticjob.dataflow.props.DataflowJobProperties;
import org.apache.shardingsphere.elasticjob.http.props.HttpJobProperties;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.OneOffJobBootstrap;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.ScheduleJobBootstrap;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperConfiguration;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperRegistryCenter;
import org.apache.shardingsphere.elasticjob.script.props.ScriptJobProperties;
import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org_apache_shardingsphere_elasticjob.elasticjob_lite_core.entity.TOrderPOJO;
import org_apache_shardingsphere_elasticjob.elasticjob_lite_core.repository.TOrderRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ElasticjobLiteCoreTest {
    private static final int PORT = 4181;
    private static volatile TestingServer testingServer;
    private static CoordinatorRegistryCenter registryCenter;
    private static final TOrderRepository tOrderRepository = new TOrderRepository();

    @BeforeAll
    static void beforeAll() throws Exception {
        testingServer = new TestingServer(PORT, true);
        try (CuratorZookeeperClient client = new CuratorZookeeperClient(testingServer.getConnectString(),
                60 * 1000, 500, null,
                new ExponentialBackoffRetry(500, 3, 500 * 3))) {
            client.start();
            Awaitility.await()
                    .atMost(Duration.ofMillis(500 * 60))
                    .untilAsserted(() -> assertTrue(client.isConnected()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        registryCenter = new ZookeeperRegistryCenter(new ZookeeperConfiguration(testingServer.getConnectString(), "elasticjob-example-lite-java"));
        registryCenter.init();
    }

    @AfterAll
    static void afterAll() throws IOException {
        registryCenter.close();
        testingServer.close();
    }

    @Test
    void testJavaHttpJob() {
        ScheduleJobBootstrap jobBootstrap = new ScheduleJobBootstrap(registryCenter, "HTTP",
                JobConfiguration.newBuilder("javaHttpJob", 3)
                        .setProperty(HttpJobProperties.URI_KEY, "https://github.com")
                        .setProperty(HttpJobProperties.METHOD_KEY, "GET")
                        .cron("0/5 * * * * ?")
                        .shardingItemParameters("0=Beijing,1=Shanghai,2=Guangzhou")
                        .build()
        );
        jobBootstrap.schedule();
        jobBootstrap.shutdown();

    }

    @Test
    void testJavaSimpleJob() {
        ScheduleJobBootstrap jobBootstrap = new ScheduleJobBootstrap(registryCenter,
                (SimpleJob) shardingContext -> {
                    assertThat(shardingContext.getShardingItem()).isEqualTo(3);
                    tOrderRepository.findTodoData(shardingContext.getShardingParameter(), 10).forEach(each -> tOrderRepository.setCompleted(each.id()));
                },
                JobConfiguration.newBuilder("javaSimpleJob", 3)
                        .cron("0/5 * * * * ?")
                        .shardingItemParameters("0=Beijing,1=Shanghai,2=Guangzhou")
                        .build()
        );
        jobBootstrap.schedule();
        jobBootstrap.shutdown();
    }

    @Test
    void testJavaDataflowElasticJob() {
        ScheduleJobBootstrap jobBootstrap = new ScheduleJobBootstrap(registryCenter, new DataflowJob<TOrderPOJO>() {
            @Override
            public List<TOrderPOJO> fetchData(ShardingContext shardingContext) {
                assertThat(shardingContext.getShardingItem()).isEqualTo(3);
                return tOrderRepository.findTodoData(shardingContext.getShardingParameter(), 10);
            }

            @Override
            public void processData(ShardingContext shardingContext, List<TOrderPOJO> data) {
                assertThat(shardingContext.getShardingItem()).isEqualTo(3);
                data.stream().mapToLong(TOrderPOJO::id).forEach(tOrderRepository::setCompleted);
            }
        },
                JobConfiguration.newBuilder("javaDataflowElasticJob", 3)
                        .cron("0/5 * * * * ?")
                        .shardingItemParameters("0=Beijing,1=Shanghai,2=Guangzhou")
                        .setProperty(DataflowJobProperties.STREAM_PROCESS_KEY, Boolean.TRUE.toString())
                        .build()
        );
        jobBootstrap.schedule();
        jobBootstrap.shutdown();
    }

    // TODO
    @Test
    @EnabledOnOs({OS.LINUX, OS.WINDOWS})
    @Disabled("It looks like `Files.setPosixFilePermissions` cannot be used under Native Image.")
    void testScriptElasticJob() throws IOException {
        ScheduleJobBootstrap jobBootstrap = new ScheduleJobBootstrap(registryCenter, "SCRIPT",
                JobConfiguration.newBuilder("scriptElasticJob", 3)
                        .cron("0/5 * * * * ?")
                        .setProperty(ScriptJobProperties.SCRIPT_KEY, buildScriptCommandLine())
                        .build());
        jobBootstrap.schedule();
        jobBootstrap.shutdown();
    }

    @Test
    void testJavaOneOffSimpleJob() {
        OneOffJobBootstrap jobBootstrap = new OneOffJobBootstrap(registryCenter,
                (SimpleJob) shardingContext -> {
                    assertThat(shardingContext.getShardingItem()).isEqualTo(3);
                    tOrderRepository.findTodoData(shardingContext.getShardingParameter(), 10).forEach(each -> tOrderRepository.setCompleted(each.id()));
                },
                JobConfiguration.newBuilder("javaOneOffSimpleJob", 3)
                        .shardingItemParameters("0=Beijing,1=Shanghai,2=Guangzhou")
                        .build()
        );
        jobBootstrap.execute();
        jobBootstrap.shutdown();
    }

    private String buildScriptCommandLine() throws IOException {
        if (System.getProperties().getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("win")) {
            return Paths.get(getClass().getResource("/script/demo.bat").getPath().substring(1)).toString();
        }
        Path result = Paths.get(getClass().getResource("/script/demo.sh").getPath());
        Files.setPosixFilePermissions(result, Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_EXECUTE,
                PosixFilePermission.GROUP_READ, PosixFilePermission.GROUP_EXECUTE,
                PosixFilePermission.OTHERS_READ, PosixFilePermission.OTHERS_EXECUTE
        ));
        return result.toString();
    }
}