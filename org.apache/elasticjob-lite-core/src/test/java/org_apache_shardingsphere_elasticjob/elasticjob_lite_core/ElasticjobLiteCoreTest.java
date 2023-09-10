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
    }

    @AfterAll
    static void afterAll() {
        Awaitility.await().atMost(Duration.ofSeconds(5)).until(() -> {
            testingServer.close();
            return true;
        });
    }

    @Test
    @EnabledOnOs({OS.LINUX, OS.WINDOWS})
    void testCreateJob() throws IOException {
        CoordinatorRegistryCenter regCenter = createCoordinatorRegistryCenter();
//        HikariConfig hikariConfig = new HikariConfig();
//        hikariConfig.setDriverClassName("org.h2.Driver");
//        hikariConfig.setJdbcUrl("jdbc:h2:mem:job_event_storage");
//        hikariConfig.setUsername("sa");
//        hikariConfig.setPassword("");
//        TracingConfiguration<DataSource> dataSourceTracingConfiguration = new TracingConfiguration<>("RDB", new HikariDataSource(hikariConfig));

        OneOffJobBootstrap javaOneOffSimpleJob = new OneOffJobBootstrap(regCenter,
                (SimpleJob) shardingContext -> {
                    assertThat(shardingContext.getShardingItem()).isEqualTo(3);
                    tOrderRepository.findTodoData(shardingContext.getShardingParameter(), 10).forEach(each -> tOrderRepository.setCompleted(each.id()));
                },
                JobConfiguration.newBuilder("javaOneOffSimpleJob", 3)
                        .shardingItemParameters("0=Beijing,1=Shanghai,2=Guangzhou")
//                        .addExtraConfigurations(dataSourceTracingConfiguration)
                        .build()
        );
        javaOneOffSimpleJob.execute();
        javaOneOffSimpleJob.shutdown();
        regCenter.close();
    }

    private CoordinatorRegistryCenter createCoordinatorRegistryCenter() {
        CoordinatorRegistryCenter result = new ZookeeperRegistryCenter(
                new ZookeeperConfiguration(testingServer.getConnectString(), "elasticjob-example-lite-java")
        );
        result.init();
        return result;
    }

    private String getMultipleOSShellCommand() throws IOException {
        if (System.getProperties().getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("win")) {
            return Paths.get(Objects.requireNonNull(getClass().getResource("/script/demo.bat")).getPath().substring(1)).toString();
        }
        Path result = Paths.get(Objects.requireNonNull(getClass().getResource("/script/demo.sh")).getPath());
        Files.setPosixFilePermissions(result, Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_EXECUTE,
                PosixFilePermission.GROUP_READ, PosixFilePermission.GROUP_EXECUTE,
                PosixFilePermission.OTHERS_READ, PosixFilePermission.OTHERS_EXECUTE
        ));
        return result.toString();
    }
}