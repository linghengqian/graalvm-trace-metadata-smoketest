package com.lingh;

import com.lingh.config.*;
import com.lingh.repository.AddressRepositoryImpl;
import com.lingh.repository.OrderItemRepositoryImpl;
import com.lingh.repository.RangeOrderRepositoryImpl;
import com.lingh.service.AccountServiceImpl;
import com.lingh.service.ExampleService;
import com.lingh.service.OrderServiceImpl;
import com.lingh.service.OrderStatisticsInfoServiceImpl;
import com.lingh.type.ShardingType;
import org.apache.shardingsphere.driver.api.yaml.YamlShardingSphereDataSourceFactory;
import org.apache.shardingsphere.infra.hint.HintManager;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class ShardingRawJdbcTest {

    @SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection"})
    @Test
    void testShardingSQLCommentHintRaw() throws SQLException, IOException {
        URL resource = getClass().getResource("/META-INF/sharding-sql-comment-hint.yaml");
        assertThat(resource).isNotNull();
        DataSource dataSource = YamlShardingSphereDataSourceFactory.createDataSource(new File(resource.getFile()));
        ExampleService exampleService = new OrderServiceImpl(dataSource);
        exampleService.initEnvironment();
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("/* ShardingSphere hint: dataSourceName=ds_1 */select * from t_order");
            statement.execute("/* ShardingSphere hint: dataSourceName=ds_1 */SELECT i.* FROM t_order o, t_order_item i WHERE o.order_id = i.order_id");
            statement.execute("/* ShardingSphere hint: dataSourceName=ds_1 */select * from t_order_item");
            statement.execute("/* ShardingSphere hint: dataSourceName=ds_1 */INSERT INTO t_order (user_id, address_id, status) VALUES (1, 1, 'init')");
        }
        exampleService.cleanEnvironment();
    }

    /*
     * Please make sure primary replica data replication sync on MySQL is running correctly. Otherwise this example will query empty data from replica.
     */
    @SuppressWarnings("ConstantConditions")
    @Test
    void testShardingRawYamlRangeConfiguration() {
        Stream.of(ShardingType.SHARDING_DATABASES, ShardingType.SHARDING_TABLES, ShardingType.SHARDING_DATABASES_AND_TABLES)
                .forEach(shardingType -> {
                    try {
                        DataSource dataSource;
                        switch (shardingType) {
                            case SHARDING_DATABASES:
                                dataSource = YamlShardingSphereDataSourceFactory.createDataSource(DataSourceUtil.getFile("/META-INF/sharding-databases-range.yaml"));
                                break;
                            case SHARDING_TABLES:
                                dataSource = YamlShardingSphereDataSourceFactory.createDataSource(DataSourceUtil.getFile("/META-INF/sharding-tables-range.yaml"));
                                break;
                            case SHARDING_DATABASES_AND_TABLES:
                                dataSource = YamlShardingSphereDataSourceFactory.createDataSource(DataSourceUtil.getFile("/META-INF/sharding-databases-tables-range.yaml"));
                                break;
                            case SHARDING_DATABASES_INTERVAL:
                                dataSource = YamlShardingSphereDataSourceFactory.createDataSource(DataSourceUtil.getFile("/META-INF/sharding-databases-interval.yaml"));
                                break;
                            default:
                                throw new UnsupportedOperationException(shardingType.name());
                        }
                        ExampleService orderService = new OrderServiceImpl(dataSource);
                        try {
                            orderService.initEnvironment();
                            orderService.processSuccess();
                        } finally {
                            orderService.cleanEnvironment();
                        }
                        ExampleService accountService = new AccountServiceImpl(dataSource);
                        try {
                            accountService.initEnvironment();
                            accountService.processSuccess();
                        } finally {
                            accountService.cleanEnvironment();
                        }
                    } catch (SQLException | IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @Test
    void testShardingRawYamlIntervalConfiguration() throws SQLException, IOException {
        DataSource dataSource = YamlShardingSphereDataSourceFactory.createDataSource(DataSourceUtil.getFile("/META-INF/sharding-databases-interval.yaml"));
        ExampleService exampleService = new OrderStatisticsInfoServiceImpl(dataSource);
        try {
            exampleService.initEnvironment();
            exampleService.processSuccess();
        } finally {
            exampleService.cleanEnvironment();
        }
    }

    /*
     * Please make sure primary replica data replication sync on MySQL is running correctly. Otherwise this example will query empty data from replica.
     */
    @Test
    void testShardingRawYamlConfiguration() {
        Stream.of(ShardingType.SHARDING_DATABASES, ShardingType.SHARDING_TABLES, ShardingType.SHARDING_DATABASES_AND_TABLES, ShardingType.SHARDING_AUTO_TABLES)
                .forEach(shardingType -> {
                    try {
                        DataSource dataSource;
                        switch (shardingType) {
                            case SHARDING_DATABASES:
                                dataSource = YamlShardingSphereDataSourceFactory.createDataSource(DataSourceUtil.getFile("/META-INF/sharding-databases.yaml"));
                                break;
                            case SHARDING_TABLES:
                                dataSource = YamlShardingSphereDataSourceFactory.createDataSource(DataSourceUtil.getFile("/META-INF/sharding-tables.yaml"));
                                break;
                            case SHARDING_DATABASES_AND_TABLES:
                                dataSource = YamlShardingSphereDataSourceFactory.createDataSource(DataSourceUtil.getFile("/META-INF/sharding-databases-tables.yaml"));
                                break;
                            case SHARDING_AUTO_TABLES:
                                dataSource = YamlShardingSphereDataSourceFactory.createDataSource(DataSourceUtil.getFile("/META-INF/sharding-auto-tables.yaml"));
                                break;
                            default:
                                throw new UnsupportedOperationException(shardingType.name());
                        }
                        ExampleService orderService = new OrderServiceImpl(dataSource);
                        try {
                            orderService.initEnvironment();
                            orderService.processSuccess();
                        } finally {
                            orderService.cleanEnvironment();
                        }
                        ExampleService accountService = new AccountServiceImpl(dataSource);
                        try {
                            accountService.initEnvironment();
                            accountService.processSuccess();
                        } finally {
                            accountService.cleanEnvironment();
                        }
                    } catch (SQLException | IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    /*
     * Please make sure primary replica data replication sync on MySQL is running correctly. Otherwise this example will query empty data from replica.
     */
    @Test
    void testShardingRawJavaRangeConfiguration() {
        Stream.of(ShardingType.SHARDING_DATABASES, ShardingType.SHARDING_TABLES, ShardingType.SHARDING_DATABASES_AND_TABLES)
                .forEach(shardingType -> {
                    try {
                        DataSource dataSource;
                        switch (shardingType) {
                            case SHARDING_DATABASES:
                                dataSource = new ShardingDatabasesConfigurationRange().getDataSource();
                                break;
                            case SHARDING_TABLES:
                                dataSource = new ShardingTablesConfigurationRange().getDataSource();
                                break;
                            case SHARDING_DATABASES_AND_TABLES:
                                dataSource = new ShardingDatabasesAndTablesConfigurationRange().getDataSource();
                                break;
                            default:
                                throw new UnsupportedOperationException(shardingType.name());
                        }

                        ExampleService orderService = new OrderServiceImpl(new RangeOrderRepositoryImpl(dataSource), new OrderItemRepositoryImpl(dataSource), new AddressRepositoryImpl(dataSource));
                        try {
                            orderService.initEnvironment();
                            orderService.processSuccess();
                        } finally {
                            orderService.cleanEnvironment();
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    /*
     * Please make sure primary replica data replication sync on MySQL is running correctly. Otherwise this example will query empty data from replica.
     */
    @Test
    void testShardingRawJavaConfiguration() {
        Stream.of(ShardingType.SHARDING_DATABASES, ShardingType.SHARDING_TABLES, ShardingType.SHARDING_DATABASES_AND_TABLES)
                .forEach(shardingType -> {
                    try {
                        DataSource dataSource;
                        switch (shardingType) {
                            case SHARDING_DATABASES:
                                dataSource = new ShardingDatabasesConfigurationPrecise().getDataSource();
                                break;
                            case SHARDING_TABLES:
                                dataSource = new ShardingTablesConfigurationPrecise().getDataSource();
                                break;
                            case SHARDING_DATABASES_AND_TABLES:
                                dataSource = new ShardingDatabasesAndTablesConfigurationPrecise().getDataSource();
                                break;
                            default:
                                throw new UnsupportedOperationException(shardingType.name());
                        }

                        ExampleService exampleService = new OrderServiceImpl(dataSource);
                        try {
                            exampleService.initEnvironment();
                            exampleService.processSuccess();
                        } finally {
                            exampleService.cleanEnvironment();
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection"})
    @Test
    void testShardingHintRaw() {
        Stream.of(ShardingType.SHARDING_HINT_DATABASES_ONLY, ShardingType.SHARDING_HINT_DATABASES_TABLES)
                .forEach(shardingType -> {
                    try {
                        URL resource;
                        DataSource dataSource;
                        switch (shardingType) {
                            case SHARDING_HINT_DATABASES_ONLY:
                                resource = getClass().getResource("/META-INF/sharding-hint-databases-only.yaml");
                                assertThat(resource).isNotNull();
                                dataSource = YamlShardingSphereDataSourceFactory.createDataSource(new File(resource.getFile()));
                                break;
                            case SHARDING_HINT_DATABASES_TABLES:
                                resource = getClass().getResource("/META-INF/sharding-hint-databases-tables.yaml");
                                assertThat(resource).isNotNull();
                                dataSource = YamlShardingSphereDataSourceFactory.createDataSource(new File(resource.getFile()));
                                break;
                            default:
                                throw new UnsupportedOperationException("unsupported type");
                        }
                        ExampleService exampleService = new OrderServiceImpl(dataSource);
                        exampleService.initEnvironment();
                        try (HintManager hintManager = HintManager.getInstance();
                             Connection connection = dataSource.getConnection();
                             Statement statement = connection.createStatement()) {
                            switch (shardingType) {
                                case SHARDING_HINT_DATABASES_ONLY:
                                    hintManager.setDatabaseShardingValue(1L);
                                    break;
                                case SHARDING_HINT_DATABASES_TABLES:
                                    hintManager.addDatabaseShardingValue("t_order", 2L);
                                    hintManager.addTableShardingValue("t_order", 1L);
                                    break;
                                default:
                                    throw new UnsupportedOperationException("unsupported type");
                            }
                            statement.execute("select * from t_order");
                            statement.execute("SELECT i.* FROM t_order o, t_order_item i WHERE o.order_id = i.order_id");
                            statement.execute("select * from t_order_item");
                            statement.execute("INSERT INTO t_order (user_id, address_id, status) VALUES (1, 1, 'init')");
                        }
                        exampleService.cleanEnvironment();
                    } catch (SQLException | IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}
