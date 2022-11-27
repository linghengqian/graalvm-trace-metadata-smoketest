package com.lingh;

import com.lingh.factory.DataSourceFactory;
import com.lingh.factory.RangeDataSourceFactory;
import com.lingh.factory.YamlDataSourceFactory;
import com.lingh.factory.YamlRangeDataSourceFactory;
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

public class ShardingSphereJDBCCoreTest {

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
    @Test
    void testShardingRawYamlRangeConfiguration() {
        Stream.of(ShardingType.SHARDING_DATABASES, ShardingType.SHARDING_TABLES, ShardingType.SHARDING_DATABASES_AND_TABLES)
                .forEach(shardingType -> {
                    try {
                        DataSource dataSource = YamlRangeDataSourceFactory.newInstance(shardingType);
                        ExampleExecuteTemplate.run(new OrderServiceImpl(dataSource));
                        ExampleExecuteTemplate.run(new AccountServiceImpl(dataSource));
                    } catch (SQLException | IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @Test
    void testShardingRawYamlIntervalConfiguration() throws SQLException, IOException {
        DataSource dataSource = YamlRangeDataSourceFactory.newInstance(ShardingType.SHARDING_DATABASES_INTERVAL);
        ExampleExecuteTemplate.run(new OrderStatisticsInfoServiceImpl(dataSource));
    }

    /*
     * Please make sure primary replica data replication sync on MySQL is running correctly. Otherwise this example will query empty data from replica.
     */
    @Test
    void testShardingRawYamlConfiguration() {
        Stream.of(ShardingType.SHARDING_DATABASES, ShardingType.SHARDING_TABLES, ShardingType.SHARDING_DATABASES_AND_TABLES, ShardingType.SHARDING_AUTO_TABLES)
                .forEach(shardingType -> {
                    try {
                        DataSource dataSource = YamlDataSourceFactory.newInstance(shardingType);
                        ExampleExecuteTemplate.run(new OrderServiceImpl(dataSource));
                        ExampleExecuteTemplate.run(new AccountServiceImpl(dataSource));
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
                        DataSource dataSource = RangeDataSourceFactory.newInstance(shardingType);
                        OrderServiceImpl orderService = new OrderServiceImpl(new RangeOrderRepositoryImpl(dataSource), new OrderItemRepositoryImpl(dataSource), new AddressRepositoryImpl(dataSource));
                        ExampleExecuteTemplate.run(orderService);
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
                        DataSource dataSource = DataSourceFactory.newInstance(shardingType);
                        ExampleExecuteTemplate.run(new OrderServiceImpl(dataSource));
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
                        DataSource dataSource = null;
                        switch (shardingType) {
                            case SHARDING_HINT_DATABASES_ONLY:
                                resource = getClass().getResource("/META-INF/sharding-hint-databases-only.yaml");
                                assertThat(resource).isNotNull();
                                dataSource = YamlShardingSphereDataSourceFactory.createDataSource(new File(resource.getFile()));
                            case SHARDING_HINT_DATABASES_TABLES:
                                resource = getClass().getResource("/META-INF/sharding-hint-databases-tables.yaml");
                                assertThat(resource).isNotNull();
                                dataSource = YamlShardingSphereDataSourceFactory.createDataSource(new File(resource.getFile()));
//                            default:
//                                throw new UnsupportedOperationException("unsupported type");
                        }
                        ExampleService exampleService = new OrderServiceImpl(dataSource);
                        exampleService.initEnvironment();
                        try (HintManager hintManager = HintManager.getInstance();
                             Connection connection = dataSource.getConnection();
                             Statement statement = connection.createStatement()) {
                            switch (shardingType) {
                                case SHARDING_HINT_DATABASES_ONLY:
                                    hintManager.setDatabaseShardingValue(1L);
                                    return;
                                case SHARDING_HINT_DATABASES_TABLES:
                                    hintManager.addDatabaseShardingValue("t_order", 2L);
                                    hintManager.addTableShardingValue("t_order", 1L);
                                    return;
//                                default:
//                                    throw new UnsupportedOperationException("unsupported type");
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
