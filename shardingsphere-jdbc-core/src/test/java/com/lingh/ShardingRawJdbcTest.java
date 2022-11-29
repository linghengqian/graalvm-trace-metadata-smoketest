package com.lingh;

import com.lingh.config.ShardingDatabasesAndTablesConfigurationPrecise;
import com.lingh.config.ShardingDatabasesAndTablesConfigurationRange;
import com.lingh.config.ShardingDatabasesConfigurationPrecise;
import com.lingh.config.ShardingDatabasesConfigurationRange;
import com.lingh.config.ShardingTablesConfigurationPrecise;
import com.lingh.config.ShardingTablesConfigurationRange;
import com.lingh.entity.Account;
import com.lingh.entity.Address;
import com.lingh.entity.Order;
import com.lingh.entity.OrderItem;
import com.lingh.entity.OrderStatisticsInfo;
import com.lingh.type.ShardingType;
import org.apache.shardingsphere.driver.api.yaml.YamlShardingSphereDataSourceFactory;
import org.apache.shardingsphere.infra.hint.HintManager;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings({"SqlResolve", "SqlNoDataSourceInspection"})
public class ShardingRawJdbcTest {

    @Test   // todo fail
    @Disabled
    @DisabledInNativeImage
    void testShardingSQLCommentHintRaw() throws SQLException, IOException {
        URL resource = getClass().getResource("/META-INF/sharding-sql-comment-hint.yaml");
        assertThat(resource).isNotNull();
        DataSource dataSource = YamlShardingSphereDataSourceFactory.createDataSource(new File(resource.getFile()));
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS t_order (order_id BIGINT NOT NULL AUTO_INCREMENT, user_id INT NOT NULL, address_id BIGINT NOT NULL, status VARCHAR(50), PRIMARY KEY (order_id))");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS t_order_item (order_item_id BIGINT NOT NULL AUTO_INCREMENT, order_id BIGINT NOT NULL, user_id INT NOT NULL, status VARCHAR(50), PRIMARY KEY (order_item_id))");
            statement.executeUpdate("TRUNCATE TABLE t_order");
            statement.executeUpdate("TRUNCATE TABLE t_order_item");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS t_address (address_id BIGINT NOT NULL, address_name VARCHAR(100) NOT NULL, PRIMARY KEY (address_id))");
            statement.executeUpdate("TRUNCATE TABLE t_address");
        }
        for (int i = 0; i < 10; i++) {
            Address address = new Address();
            address.setAddressId((long) i);
            address.setAddressName("address_" + i);
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(
                         "INSERT INTO t_address (address_id, address_name) VALUES (?, ?)")) {
                preparedStatement.setLong(1, address.getAddressId());
                preparedStatement.setString(2, address.getAddressName());
                preparedStatement.executeUpdate();
            }
        }
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("select * from t_order");
            statement.execute("SELECT i.* FROM t_order o, t_order_item i WHERE o.order_id = i.order_id");
            statement.execute("select * from t_order_item");
            statement.execute("INSERT INTO t_order (user_id, address_id, status) VALUES (1, 1, 'init')");
            statement.executeUpdate("DROP TABLE t_order");
            statement.executeUpdate("DROP TABLE t_order_item");
            statement.executeUpdate("DROP TABLE t_address");
        }
    }

    // todo fail ShardingType.SHARDING_TABLES, ShardingType.SHARDING_DATABASES_AND_TABLES
    @SuppressWarnings("ConstantConditions")
    @Test
    void testShardingRawYamlRangeConfiguration() {
        Stream.of(ShardingType.SHARDING_DATABASES)
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
                        try {
                            try (Connection connection = dataSource.getConnection();
                                 Statement statement = connection.createStatement()) {
                                statement.executeUpdate("CREATE TABLE IF NOT EXISTS t_order (order_id BIGINT NOT NULL AUTO_INCREMENT, user_id INT NOT NULL, address_id BIGINT NOT NULL, status VARCHAR(50), PRIMARY KEY (order_id))");
                                statement.executeUpdate("CREATE TABLE IF NOT EXISTS t_order_item (order_item_id BIGINT NOT NULL AUTO_INCREMENT, order_id BIGINT NOT NULL, user_id INT NOT NULL, status VARCHAR(50), PRIMARY KEY (order_item_id))");
                                statement.executeUpdate("TRUNCATE TABLE t_order");
                                statement.executeUpdate("TRUNCATE TABLE t_order_item");
                                statement.executeUpdate("CREATE TABLE IF NOT EXISTS t_address (address_id BIGINT NOT NULL, address_name VARCHAR(100) NOT NULL, PRIMARY KEY (address_id))");
                                statement.executeUpdate("TRUNCATE TABLE t_address");
                            }
                            for (int i = 0; i < 10; i++) {
                                Address address = new Address();
                                address.setAddressId((long) i);
                                address.setAddressName("address_" + i);
                                String sql = "INSERT INTO t_address (address_id, address_name) VALUES (?, ?)";
                                try (Connection connection = dataSource.getConnection();
                                     PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                                    preparedStatement.setLong(1, address.getAddressId());
                                    preparedStatement.setString(2, address.getAddressName());
                                    preparedStatement.executeUpdate();
                                }
                            }
                            System.out.println("-------------- Process Success Begin ---------------");
                            System.out.println("---------------------------- Insert Data ----------------------------");
                            List<Long> orderIds = new ArrayList<>(10);
                            for (int i = 1; i <= 10; i++) {
                                Order order = new Order();
                                order.setUserId(i);
                                order.setAddressId(i);
                                order.setStatus("INSERT_TEST");
                                try (Connection connection = dataSource.getConnection();
                                     PreparedStatement preparedStatement = connection.prepareStatement(
                                             "INSERT INTO t_order (user_id, address_id, status) VALUES (?, ?, ?)",
                                             Statement.RETURN_GENERATED_KEYS)) {
                                    preparedStatement.setInt(1, order.getUserId());
                                    preparedStatement.setLong(2, order.getAddressId());
                                    preparedStatement.setString(3, order.getStatus());
                                    preparedStatement.executeUpdate();
                                    try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                                        if (resultSet.next()) {
                                            order.setOrderId(resultSet.getLong(1));
                                        }
                                    }
                                }
                                OrderItem item = new OrderItem();
                                item.setOrderId(order.getOrderId());
                                item.setUserId(i);
                                item.setStatus("INSERT_TEST");
                                try (Connection connection = dataSource.getConnection();
                                     PreparedStatement preparedStatement = connection.prepareStatement(
                                             "INSERT INTO t_order_item (order_id, user_id, status) VALUES (?, ?, ?)",
                                             Statement.RETURN_GENERATED_KEYS)) {
                                    preparedStatement.setLong(1, item.getOrderId());
                                    preparedStatement.setInt(2, item.getUserId());
                                    preparedStatement.setString(3, item.getStatus());
                                    preparedStatement.executeUpdate();
                                    try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                                        if (resultSet.next()) {
                                            item.setOrderItemId(resultSet.getLong(1));
                                        }
                                    }
                                }
                                orderIds.add(order.getOrderId());
                            }
                            System.out.println("---------------------------- Print Order Data -----------------------");
                            List<Order> result = new LinkedList<>();
                            try (Connection connection = dataSource.getConnection();
                                 PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM t_order");
                                 ResultSet resultSet = preparedStatement.executeQuery()) {
                                while (resultSet.next()) {
                                    Order order = new Order();
                                    order.setOrderId(resultSet.getLong(1));
                                    order.setUserId(resultSet.getInt(2));
                                    order.setAddressId(resultSet.getLong(3));
                                    order.setStatus(resultSet.getString(4));
                                    result.add(order);
                                }
                            }
                            result.forEach(System.out::println);
                            System.out.println("---------------------------- Print OrderItem Data -------------------");
// TODO Associated query with encrypt may query and decrypt failed. see https://github.com/apache/shardingsphere/issues/3352
//        String sql = "SELECT i.* FROM t_order o, t_order_item i WHERE o.order_id = i.order_id";
//        String sql = "SELECT * FROM t_order_item";
                            List<OrderItem> anotherResult = new LinkedList<>();
                            try (Connection connection = dataSource.getConnection();
                                 PreparedStatement preparedStatement = connection.prepareStatement("SELECT i.* FROM t_order o, t_order_item i WHERE o.order_id = i.order_id");
                                 ResultSet resultSet = preparedStatement.executeQuery()) {
                                while (resultSet.next()) {
                                    OrderItem orderItem = new OrderItem();
                                    orderItem.setOrderItemId(resultSet.getLong(1));
                                    orderItem.setOrderId(resultSet.getLong(2));
                                    orderItem.setUserId(resultSet.getInt(3));
                                    orderItem.setStatus(resultSet.getString(4));
                                    anotherResult.add(orderItem);
                                }
                            }
                            anotherResult.forEach(System.out::println);
                            System.out.println("---------------------------- Delete Data ----------------------------");
                            for (Long each : orderIds) {
                                try (Connection connection = dataSource.getConnection();
                                     PreparedStatement firstPreparedStatement = connection.prepareStatement("DELETE FROM t_order WHERE order_id=?");
                                     PreparedStatement secondPreparedStatement = connection.prepareStatement("DELETE FROM t_order_item WHERE order_id=?")) {
                                    firstPreparedStatement.setLong(1, each);
                                    firstPreparedStatement.executeUpdate();
                                    secondPreparedStatement.setLong(1, each);
                                    secondPreparedStatement.executeUpdate();
                                }
                            }
                            System.out.println("---------------------------- Print Order Data -----------------------");
                            List<Order> secondResult = new LinkedList<>();
                            try (Connection connection = dataSource.getConnection();
                                 PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM t_order");
                                 ResultSet resultSet = preparedStatement.executeQuery()) {
                                while (resultSet.next()) {
                                    Order order = new Order();
                                    order.setOrderId(resultSet.getLong(1));
                                    order.setUserId(resultSet.getInt(2));
                                    order.setAddressId(resultSet.getLong(3));
                                    order.setStatus(resultSet.getString(4));
                                    secondResult.add(order);
                                }
                            }
                            secondResult.forEach(System.out::println);
                            System.out.println("---------------------------- Print OrderItem Data -------------------");
                            // TODO Associated query with encrypt may query and decrypt failed. see https://github.com/apache/shardingsphere/issues/3352
//        String sql = "SELECT i.* FROM t_order o, t_order_item i WHERE o.order_id = i.order_id";
//        String sql = "SELECT * FROM t_order_item";
                            List<OrderItem> thirdResult = new LinkedList<>();
                            try (Connection connection = dataSource.getConnection();
                                 PreparedStatement preparedStatement = connection.prepareStatement("SELECT i.* FROM t_order o, t_order_item i WHERE o.order_id = i.order_id");
                                 ResultSet resultSet = preparedStatement.executeQuery()) {
                                while (resultSet.next()) {
                                    OrderItem orderItem = new OrderItem();
                                    orderItem.setOrderItemId(resultSet.getLong(1));
                                    orderItem.setOrderId(resultSet.getLong(2));
                                    orderItem.setUserId(resultSet.getInt(3));
                                    orderItem.setStatus(resultSet.getString(4));
                                    thirdResult.add(orderItem);
                                }
                            }
                            thirdResult.forEach(System.out::println);
                            System.out.println("-------------- Process Success Finish --------------");
                        } finally {
                            try (Connection connection = dataSource.getConnection();
                                 Statement statement = connection.createStatement()) {
                                statement.executeUpdate("DROP TABLE if exists t_order");
                                statement.executeUpdate("DROP TABLE if exists t_order_item");
                                statement.executeUpdate("DROP TABLE if exists t_address");
                            }
                        }
                        try {
                            try (Connection connection = dataSource.getConnection();
                                 Statement statement = connection.createStatement()) {
                                statement.executeUpdate("CREATE TABLE IF NOT EXISTS t_account (account_id BIGINT NOT NULL AUTO_INCREMENT, user_id INT NOT NULL, status VARCHAR(50), PRIMARY KEY (account_id))");
                                statement.executeUpdate("TRUNCATE TABLE t_account");
                            }
                            System.out.println("-------------- Process Success Begin ---------------");
                            System.out.println("---------------------------- Insert Data ----------------------------");
                            List<Long> result = new ArrayList<>(10);
                            for (int i = 1; i <= 10; i++) {
                                Account account = new Account();
                                account.setUserId(i);
                                account.setStatus("INSERT_TEST");
                                try (Connection connection = dataSource.getConnection();
                                     PreparedStatement preparedStatement = connection.prepareStatement(
                                             "INSERT INTO t_account (user_id, status) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                                    preparedStatement.setInt(1, account.getUserId());
                                    preparedStatement.setString(2, account.getStatus());
                                    preparedStatement.executeUpdate();
                                    try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                                        if (resultSet.next()) {
                                            account.setAccountId(resultSet.getLong(1));
                                        }
                                    }
                                }
                                result.add(account.getAccountId());
                            }
                            System.out.println("---------------------------- Print Account Data -----------------------");
                            List<Account> anotherResult = new LinkedList<>();
                            try (Connection connection = dataSource.getConnection();
                                 PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM t_account");
                                 ResultSet resultSet = preparedStatement.executeQuery()) {
                                while (resultSet.next()) {
                                    Account account = new Account();
                                    account.setAccountId(resultSet.getLong(1));
                                    account.setUserId(resultSet.getInt(2));
                                    account.setStatus(resultSet.getString(3));
                                    anotherResult.add(account);
                                }
                            }
                            anotherResult.forEach(System.out::println);
                            System.out.println("---------------------------- Delete Data ----------------------------");
                            for (Long each : result) {
                                try (Connection connection = dataSource.getConnection();
                                     PreparedStatement preparedStatement = connection.prepareStatement(
                                             "DELETE FROM t_account WHERE account_id=?")) {
                                    preparedStatement.setLong(1, each);
                                    preparedStatement.executeUpdate();
                                }
                            }
                            System.out.println("---------------------------- Print Account Data -----------------------");
                            List<Account> oneResult = new LinkedList<>();
                            try (Connection connection = dataSource.getConnection();
                                 PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM t_account");
                                 ResultSet resultSet = preparedStatement.executeQuery()) {
                                while (resultSet.next()) {
                                    Account account = new Account();
                                    account.setAccountId(resultSet.getLong(1));
                                    account.setUserId(resultSet.getInt(2));
                                    account.setStatus(resultSet.getString(3));
                                    oneResult.add(account);
                                }
                            }
                            oneResult.forEach(System.out::println);
                            System.out.println("-------------- Process Success Finish --------------");
                        } finally {
                            try (Connection connection = dataSource.getConnection();
                                 Statement statement = connection.createStatement()) {
                                statement.executeUpdate("DROP TABLE if exists t_account");
                            }
                        }
                    } catch (SQLException | IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @Test // todo fail ,need to fix INTERVAL java.sql.Date
    @Disabled
    @DisabledInNativeImage
    void testShardingRawYamlIntervalConfiguration() throws SQLException, IOException {
        DataSource dataSource = YamlShardingSphereDataSourceFactory.createDataSource(DataSourceUtil.getFile("/META-INF/sharding-databases-interval.yaml"));
        try {
            try (Connection connection = dataSource.getConnection();
                 Statement statement = connection.createStatement()) {
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS order_statistics_info (id BIGINT NOT NULL AUTO_INCREMENT, user_id BIGINT NOT NULL, order_date DATE NOT NULL, order_num INT, PRIMARY KEY (id))");
                statement.executeUpdate("TRUNCATE TABLE order_statistics_info");
            }
            System.out.println("-------------- Process Success Begin ---------------");
            System.out.println("------------------- Insert Data --------------------");
            Collection<Long> result = new ArrayList<>(10);
            for (int i = 1; i <= 10; i++) {
                OrderStatisticsInfo tempResult = new OrderStatisticsInfo();
                tempResult.setUserId((long) i);
                if (0 == i % 2) {
                    tempResult.setOrderDate(LocalDate.now().plusYears(-1));
                } else {
                    tempResult.setOrderDate(LocalDate.now());
                }
                tempResult.setOrderNum(i * 10);
                try (Connection connection = dataSource.getConnection();
                     PreparedStatement preparedStatement = connection.prepareStatement(
                             "INSERT INTO order_statistics_info (user_id, order_date, order_num) VALUES (?, ?, ?)"
                             , Statement.RETURN_GENERATED_KEYS)) {
                    preparedStatement.setLong(1, tempResult.getUserId());
                    preparedStatement.setDate(2, Date.valueOf(tempResult.getOrderDate()));
                    preparedStatement.setInt(3, tempResult.getOrderNum());
                    preparedStatement.executeUpdate();
                    try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                        if (resultSet.next()) {
                            tempResult.setId(resultSet.getLong(1));
                        }
                    }
                }
                result.add(tempResult.getId());
            }
            System.out.println("---------------- Print Order Data ------------------");
            List<OrderStatisticsInfo> oneResult = new LinkedList<>();
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM order_statistics_info");
                 ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    OrderStatisticsInfo orderStatisticsInfo = new OrderStatisticsInfo();
                    orderStatisticsInfo.setId(resultSet.getLong(1));
                    orderStatisticsInfo.setUserId(resultSet.getLong(2));
                    orderStatisticsInfo.setOrderDate(resultSet.getDate(3).toLocalDate());
                    orderStatisticsInfo.setOrderNum(resultSet.getInt(4));
                    oneResult.add(orderStatisticsInfo);
                }
            }
            oneResult.forEach(System.out::println);
            System.out.println("-------------------- Delete Data -------------------");
            for (Long each : result) {
                try (Connection connection = dataSource.getConnection();
                     PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM order_statistics_info WHERE id=?")) {
                    preparedStatement.setLong(1, each);
                    preparedStatement.executeUpdate();
                }
            }
            System.out.println("---------------- Print Order Data ------------------");
            List<OrderStatisticsInfo> anotherResult = new LinkedList<>();
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM order_statistics_info");
                 ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    OrderStatisticsInfo orderStatisticsInfo = new OrderStatisticsInfo();
                    orderStatisticsInfo.setId(resultSet.getLong(1));
                    orderStatisticsInfo.setUserId(resultSet.getLong(2));
                    orderStatisticsInfo.setOrderDate(resultSet.getDate(3).toLocalDate());
                    orderStatisticsInfo.setOrderNum(resultSet.getInt(4));
                    anotherResult.add(orderStatisticsInfo);
                }
            }
            anotherResult.forEach(System.out::println);

            System.out.println("-------------- Process Success Finish --------------");
        } finally {
            try (Connection connection = dataSource.getConnection();
                 Statement statement = connection.createStatement()) {
                statement.executeUpdate("DROP TABLE if exists order_statistics_info");
            }
        }
    }

    // todo fail ShardingType.SHARDING_TABLES, ShardingType.SHARDING_DATABASES_AND_TABLES, ShardingType.SHARDING_AUTO_TABLES
    @SuppressWarnings("ConstantConditions")
    @Test
    void testShardingRawYamlConfiguration() {
        Stream.of(ShardingType.SHARDING_DATABASES)
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
                        try {
                            try (Connection connection = dataSource.getConnection();
                                 Statement statement = connection.createStatement()) {
                                statement.executeUpdate("CREATE TABLE IF NOT EXISTS t_order (order_id BIGINT NOT NULL AUTO_INCREMENT, user_id INT NOT NULL, address_id BIGINT NOT NULL, status VARCHAR(50), PRIMARY KEY (order_id))");
                                statement.executeUpdate("CREATE TABLE IF NOT EXISTS t_order_item (order_item_id BIGINT NOT NULL AUTO_INCREMENT, order_id BIGINT NOT NULL, user_id INT NOT NULL, status VARCHAR(50), PRIMARY KEY (order_item_id))");
                                statement.executeUpdate("TRUNCATE TABLE t_order");
                                statement.executeUpdate("TRUNCATE TABLE t_order_item");
                                statement.executeUpdate("CREATE TABLE IF NOT EXISTS t_address (address_id BIGINT NOT NULL, address_name VARCHAR(100) NOT NULL, PRIMARY KEY (address_id))");
                                statement.executeUpdate("TRUNCATE TABLE t_address");
                            }

                            for (int i = 0; i < 10; i++) {
                                Address address = new Address();
                                address.setAddressId((long) i);
                                address.setAddressName("address_" + i);
                                try (Connection connection = dataSource.getConnection();
                                     PreparedStatement preparedStatement = connection.prepareStatement(
                                             "INSERT INTO t_address (address_id, address_name) VALUES (?, ?)")) {
                                    preparedStatement.setLong(1, address.getAddressId());
                                    preparedStatement.setString(2, address.getAddressName());
                                    preparedStatement.executeUpdate();
                                }
                            }
                            System.out.println("-------------- Process Success Begin ---------------");
                            System.out.println("---------------------------- Insert Data ----------------------------");
                            List<Long> orderIds = new ArrayList<>(10);
                            for (int i = 1; i <= 10; i++) {
                                Order order = new Order();
                                order.setUserId(i);
                                order.setAddressId(i);
                                order.setStatus("INSERT_TEST");
                                try (Connection connection = dataSource.getConnection();
                                     PreparedStatement preparedStatement = connection.prepareStatement(
                                             "INSERT INTO t_order (user_id, address_id, status) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                                    preparedStatement.setInt(1, order.getUserId());
                                    preparedStatement.setLong(2, order.getAddressId());
                                    preparedStatement.setString(3, order.getStatus());
                                    preparedStatement.executeUpdate();
                                    try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                                        if (resultSet.next()) {
                                            order.setOrderId(resultSet.getLong(1));
                                        }
                                    }
                                }
                                OrderItem item = new OrderItem();
                                item.setOrderId(order.getOrderId());
                                item.setUserId(i);
                                item.setStatus("INSERT_TEST");
                                try (Connection connection = dataSource.getConnection();
                                     PreparedStatement preparedStatement = connection.prepareStatement(
                                             "INSERT INTO t_order_item (order_id, user_id, status) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                                    preparedStatement.setLong(1, item.getOrderId());
                                    preparedStatement.setInt(2, item.getUserId());
                                    preparedStatement.setString(3, item.getStatus());
                                    preparedStatement.executeUpdate();
                                    try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                                        if (resultSet.next()) {
                                            item.setOrderItemId(resultSet.getLong(1));
                                        }
                                    }
                                }
                                orderIds.add(order.getOrderId());
                            }
                            System.out.println("---------------------------- Print Order Data -----------------------");
                            List<Order> result = new LinkedList<>();
                            try (Connection connection = dataSource.getConnection();
                                 PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM t_order");
                                 ResultSet resultSet = preparedStatement.executeQuery()) {
                                while (resultSet.next()) {
                                    Order order = new Order();
                                    order.setOrderId(resultSet.getLong(1));
                                    order.setUserId(resultSet.getInt(2));
                                    order.setAddressId(resultSet.getLong(3));
                                    order.setStatus(resultSet.getString(4));
                                    result.add(order);
                                }
                            }
                            result.forEach(System.out::println);
                            System.out.println("---------------------------- Print OrderItem Data -------------------");
                            List<OrderItem> thirdResult = new LinkedList<>();
                            try (Connection connection = dataSource.getConnection();
                                 PreparedStatement preparedStatement = connection.prepareStatement("SELECT i.* FROM t_order o, t_order_item i WHERE o.order_id = i.order_id");
                                 ResultSet resultSet = preparedStatement.executeQuery()) {
                                while (resultSet.next()) {
                                    OrderItem orderItem = new OrderItem();
                                    orderItem.setOrderItemId(resultSet.getLong(1));
                                    orderItem.setOrderId(resultSet.getLong(2));
                                    orderItem.setUserId(resultSet.getInt(3));
                                    orderItem.setStatus(resultSet.getString(4));
                                    thirdResult.add(orderItem);
                                }
                            }
                            thirdResult.forEach(System.out::println);
                            System.out.println("---------------------------- Delete Data ----------------------------");
                            for (Long each : orderIds) {
                                try (Connection connection = dataSource.getConnection();
                                     PreparedStatement firstPreparedStatement = connection.prepareStatement("DELETE FROM t_order WHERE order_id=?");
                                     PreparedStatement secondPreparedStatement = connection.prepareStatement("DELETE FROM t_order_item WHERE order_id=?")) {
                                    firstPreparedStatement.setLong(1, each);
                                    firstPreparedStatement.executeUpdate();
                                    secondPreparedStatement.setLong(1, each);
                                    secondPreparedStatement.executeUpdate();
                                }
                            }
                            System.out.println("---------------------------- Print Order Data -----------------------");
                            List<Order> secondResult = new LinkedList<>();
                            try (Connection connection = dataSource.getConnection();
                                 PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM t_order");
                                 ResultSet resultSet = preparedStatement.executeQuery()) {
                                while (resultSet.next()) {
                                    Order order = new Order();
                                    order.setOrderId(resultSet.getLong(1));
                                    order.setUserId(resultSet.getInt(2));
                                    order.setAddressId(resultSet.getLong(3));
                                    order.setStatus(resultSet.getString(4));
                                    secondResult.add(order);
                                }
                            }
                            secondResult.forEach(System.out::println);
                            System.out.println("---------------------------- Print OrderItem Data -------------------");
                            // TODO Associated query with encrypt may query and decrypt failed. see https://github.com/apache/shardingsphere/issues/3352
//        String sql = "SELECT i.* FROM t_order o, t_order_item i WHERE o.order_id = i.order_id";
//        String sql = "SELECT * FROM t_order_item";
                            List<OrderItem> anotherResult = new LinkedList<>();
                            try (Connection connection = dataSource.getConnection();
                                 PreparedStatement preparedStatement = connection.prepareStatement("SELECT i.* FROM t_order o, t_order_item i WHERE o.order_id = i.order_id");
                                 ResultSet resultSet = preparedStatement.executeQuery()) {
                                while (resultSet.next()) {
                                    OrderItem orderItem = new OrderItem();
                                    orderItem.setOrderItemId(resultSet.getLong(1));
                                    orderItem.setOrderId(resultSet.getLong(2));
                                    orderItem.setUserId(resultSet.getInt(3));
                                    orderItem.setStatus(resultSet.getString(4));
                                    anotherResult.add(orderItem);
                                }
                            }
                            anotherResult.forEach(System.out::println);
                            System.out.println("-------------- Process Success Finish --------------");
                        } finally {
                            try (Connection connection = dataSource.getConnection();
                                 Statement statement = connection.createStatement()) {
                                statement.executeUpdate("DROP TABLE if exists t_order");
                                statement.executeUpdate("DROP TABLE if exists t_order_item");
                                statement.executeUpdate("DROP TABLE if exists t_address");
                            }
                        }
                        try {
                            try (Connection connection = dataSource.getConnection();
                                 Statement statement = connection.createStatement()) {
                                statement.executeUpdate("CREATE TABLE IF NOT EXISTS t_account (account_id BIGINT NOT NULL AUTO_INCREMENT, user_id INT NOT NULL, status VARCHAR(50), PRIMARY KEY (account_id))");
                                statement.executeUpdate("TRUNCATE TABLE t_account");
                            }
                            System.out.println("-------------- Process Success Begin ---------------");
                            System.out.println("---------------------------- Insert Data ----------------------------");
                            List<Long> result = new ArrayList<>(10);
                            for (int i = 1; i <= 10; i++) {
                                Account account = new Account();
                                account.setUserId(i);
                                account.setStatus("INSERT_TEST");
                                try (Connection connection = dataSource.getConnection();
                                     PreparedStatement preparedStatement = connection.prepareStatement(
                                             "INSERT INTO t_account (user_id, status) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                                    preparedStatement.setInt(1, account.getUserId());
                                    preparedStatement.setString(2, account.getStatus());
                                    preparedStatement.executeUpdate();
                                    try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                                        if (resultSet.next()) {
                                            account.setAccountId(resultSet.getLong(1));
                                        }
                                    }
                                }
                                result.add(account.getAccountId());
                            }
                            System.out.println("---------------------------- Print Account Data -----------------------");
                            List<Account> anotherResult = new LinkedList<>();
                            try (Connection connection = dataSource.getConnection();
                                 PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM t_account");
                                 ResultSet resultSet = preparedStatement.executeQuery()) {
                                while (resultSet.next()) {
                                    Account account = new Account();
                                    account.setAccountId(resultSet.getLong(1));
                                    account.setUserId(resultSet.getInt(2));
                                    account.setStatus(resultSet.getString(3));
                                    anotherResult.add(account);
                                }
                            }
                            anotherResult.forEach(System.out::println);
                            System.out.println("---------------------------- Delete Data ----------------------------");
                            for (Long each : result) {
                                try (Connection connection = dataSource.getConnection();
                                     PreparedStatement preparedStatement = connection.prepareStatement(
                                             "DELETE FROM t_account WHERE account_id=?")) {
                                    preparedStatement.setLong(1, each);
                                    preparedStatement.executeUpdate();
                                }
                            }
                            System.out.println("---------------------------- Print Account Data -----------------------");
                            List<Account> oneResult = new LinkedList<>();
                            try (Connection connection = dataSource.getConnection();
                                 PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM t_account");
                                 ResultSet resultSet = preparedStatement.executeQuery()) {
                                while (resultSet.next()) {
                                    Account account = new Account();
                                    account.setAccountId(resultSet.getLong(1));
                                    account.setUserId(resultSet.getInt(2));
                                    account.setStatus(resultSet.getString(3));
                                    oneResult.add(account);
                                }
                            }
                            oneResult.forEach(System.out::println);
                            System.out.println("-------------- Process Success Finish --------------");

                        } finally {
                            try (Connection connection = dataSource.getConnection();
                                 Statement statement = connection.createStatement()) {
                                statement.executeUpdate("DROP TABLE if exists t_account");
                            }
                        }
                    } catch (SQLException | IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    // todo fail ShardingType.SHARDING_TABLES, ShardingType.SHARDING_DATABASES_AND_TABLES
    @SuppressWarnings("ConstantConditions")
    @Test
    void testShardingRawJavaRangeConfiguration() {
        Stream.of(ShardingType.SHARDING_DATABASES)
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
                        try {
                            try (Connection connection = dataSource.getConnection();
                                 Statement statement = connection.createStatement()) {
                                statement.executeUpdate("CREATE TABLE IF NOT EXISTS t_order (order_id BIGINT NOT NULL AUTO_INCREMENT, user_id INT NOT NULL, address_id BIGINT NOT NULL, status VARCHAR(50), PRIMARY KEY (order_id))");
                                statement.executeUpdate("CREATE TABLE IF NOT EXISTS t_order_item (order_item_id BIGINT NOT NULL AUTO_INCREMENT, order_id BIGINT NOT NULL, user_id INT NOT NULL, status VARCHAR(50), PRIMARY KEY (order_item_id))");
                                statement.executeUpdate("TRUNCATE TABLE t_order");
                                statement.executeUpdate("TRUNCATE TABLE t_order_item");
                                statement.executeUpdate("CREATE TABLE IF NOT EXISTS t_address (address_id BIGINT NOT NULL, address_name VARCHAR(100) NOT NULL, PRIMARY KEY (address_id))");
                                statement.executeUpdate("TRUNCATE TABLE t_address");
                            }
                            for (int i = 0; i < 10; i++) {
                                Address address = new Address();
                                address.setAddressId((long) i);
                                address.setAddressName("address_" + i);
                                try (Connection connection = dataSource.getConnection();
                                     PreparedStatement preparedStatement = connection.prepareStatement(
                                             "INSERT INTO t_address (address_id, address_name) VALUES (?, ?)")) {
                                    preparedStatement.setLong(1, address.getAddressId());
                                    preparedStatement.setString(2, address.getAddressName());
                                    preparedStatement.executeUpdate();
                                }
                            }

                            System.out.println("-------------- Process Success Begin ---------------");
                            System.out.println("---------------------------- Insert Data ----------------------------");
                            List<Long> orderIds = new ArrayList<>(10);
                            for (int i = 1; i <= 10; i++) {
                                Order order = new Order();
                                order.setUserId(i);
                                order.setAddressId(i);
                                order.setStatus("INSERT_TEST");
                                try (Connection connection = dataSource.getConnection();
                                     PreparedStatement preparedStatement = connection.prepareStatement(
                                             "INSERT INTO t_order (user_id, address_id, status) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                                    preparedStatement.setInt(1, order.getUserId());
                                    preparedStatement.setLong(2, order.getAddressId());
                                    preparedStatement.setString(3, order.getStatus());
                                    preparedStatement.executeUpdate();
                                    try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                                        if (resultSet.next()) {
                                            order.setOrderId(resultSet.getLong(1));
                                        }
                                    }
                                }
                                OrderItem item = new OrderItem();
                                item.setOrderId(order.getOrderId());
                                item.setUserId(i);
                                item.setStatus("INSERT_TEST");
                                try (Connection connection = dataSource.getConnection();
                                     PreparedStatement preparedStatement = connection.prepareStatement(
                                             "INSERT INTO t_order_item (order_id, user_id, status) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                                    preparedStatement.setLong(1, item.getOrderId());
                                    preparedStatement.setInt(2, item.getUserId());
                                    preparedStatement.setString(3, item.getStatus());
                                    preparedStatement.executeUpdate();
                                    try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                                        if (resultSet.next()) {
                                            item.setOrderItemId(resultSet.getLong(1));
                                        }
                                    }
                                }
                                orderIds.add(order.getOrderId());
                            }
                            System.out.println("---------------------------- Print Order Data -----------------------");
                            List<Order> result = new LinkedList<>();
                            try (Connection connection = dataSource.getConnection();
                                 PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM t_order WHERE order_id BETWEEN 200000000000000000 AND 400000000000000000");
                                 ResultSet resultSet = preparedStatement.executeQuery()) {
                                while (resultSet.next()) {
                                    Order order = new Order();
                                    order.setOrderId(resultSet.getLong(1));
                                    order.setUserId(resultSet.getInt(2));
                                    order.setAddressId(resultSet.getLong(3));
                                    order.setStatus(resultSet.getString(4));
                                    result.add(order);
                                }
                            }
                            result.forEach(System.out::println);
                            System.out.println("---------------------------- Print OrderItem Data -------------------");
                            // TODO Associated query with encrypt may query and decrypt failed. see https://github.com/apache/shardingsphere/issues/3352
//        String sql = "SELECT i.* FROM t_order o, t_order_item i WHERE o.order_id = i.order_id";
//        String sql = "SELECT * FROM t_order_item";
                            List<OrderItem> anotherResult = new LinkedList<>();
                            try (Connection connection = dataSource.getConnection();
                                 PreparedStatement preparedStatement = connection.prepareStatement("SELECT i.* FROM t_order o, t_order_item i WHERE o.order_id = i.order_id");
                                 ResultSet resultSet = preparedStatement.executeQuery()) {
                                while (resultSet.next()) {
                                    OrderItem orderItem = new OrderItem();
                                    orderItem.setOrderItemId(resultSet.getLong(1));
                                    orderItem.setOrderId(resultSet.getLong(2));
                                    orderItem.setUserId(resultSet.getInt(3));
                                    orderItem.setStatus(resultSet.getString(4));
                                    anotherResult.add(orderItem);
                                }
                            }
                            anotherResult.forEach(System.out::println);
                            System.out.println("---------------------------- Delete Data ----------------------------");
                            for (Long each : orderIds) {
                                try (Connection connection = dataSource.getConnection();
                                     PreparedStatement firstPreparedStatement = connection.prepareStatement("DELETE FROM t_order WHERE order_id=?");
                                     PreparedStatement secondPreparedStatement = connection.prepareStatement("DELETE FROM t_order_item WHERE order_id=?")) {
                                    firstPreparedStatement.setLong(1, each);
                                    firstPreparedStatement.executeUpdate();
                                    secondPreparedStatement.setLong(1, each);
                                    secondPreparedStatement.executeUpdate();
                                }
                            }
                            System.out.println("---------------------------- Print Order Data -----------------------");
                            List<Order> secondResult = new LinkedList<>();
                            try (Connection connection = dataSource.getConnection();
                                 PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM t_order WHERE order_id BETWEEN 200000000000000000 AND 400000000000000000");
                                 ResultSet resultSet = preparedStatement.executeQuery()) {
                                while (resultSet.next()) {
                                    Order order = new Order();
                                    order.setOrderId(resultSet.getLong(1));
                                    order.setUserId(resultSet.getInt(2));
                                    order.setAddressId(resultSet.getLong(3));
                                    order.setStatus(resultSet.getString(4));
                                    secondResult.add(order);
                                }
                            }
                            secondResult.forEach(System.out::println);
                            System.out.println("---------------------------- Print OrderItem Data -------------------");
                            // TODO Associated query with encrypt may query and decrypt failed. see https://github.com/apache/shardingsphere/issues/3352
//        String sql = "SELECT i.* FROM t_order o, t_order_item i WHERE o.order_id = i.order_id";
//        String sql = "SELECT * FROM t_order_item";
                            List<OrderItem> thirdResult = new LinkedList<>();
                            try (Connection connection = dataSource.getConnection();
                                 PreparedStatement preparedStatement = connection.prepareStatement("SELECT i.* FROM t_order o, t_order_item i WHERE o.order_id = i.order_id");
                                 ResultSet resultSet = preparedStatement.executeQuery()) {
                                while (resultSet.next()) {
                                    OrderItem orderItem = new OrderItem();
                                    orderItem.setOrderItemId(resultSet.getLong(1));
                                    orderItem.setOrderId(resultSet.getLong(2));
                                    orderItem.setUserId(resultSet.getInt(3));
                                    orderItem.setStatus(resultSet.getString(4));
                                    thirdResult.add(orderItem);
                                }
                            }
                            thirdResult.forEach(System.out::println);
                            System.out.println("-------------- Process Success Finish --------------");
                        } finally {
                            try (Connection connection = dataSource.getConnection();
                                 Statement statement = connection.createStatement()) {
                                statement.executeUpdate("DROP TABLE if exists t_order");
                                statement.executeUpdate("DROP TABLE if exists t_order_item");
                                statement.executeUpdate("DROP TABLE if exists t_address");
                            }
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    // todo fail ShardingType.SHARDING_TABLES, ShardingType.SHARDING_DATABASES_AND_TABLES
    @SuppressWarnings("ConstantConditions")
    @Test
    void testShardingRawJavaConfiguration() {
        Stream.of(ShardingType.SHARDING_DATABASES)
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
                        try {
                            try (Connection connection = dataSource.getConnection();
                                 Statement statement = connection.createStatement()) {
                                statement.executeUpdate("CREATE TABLE IF NOT EXISTS t_order (order_id BIGINT NOT NULL AUTO_INCREMENT, user_id INT NOT NULL, address_id BIGINT NOT NULL, status VARCHAR(50), PRIMARY KEY (order_id))");
                                statement.executeUpdate("CREATE TABLE IF NOT EXISTS t_order_item (order_item_id BIGINT NOT NULL AUTO_INCREMENT, order_id BIGINT NOT NULL, user_id INT NOT NULL, status VARCHAR(50), PRIMARY KEY (order_item_id))");
                                statement.executeUpdate("TRUNCATE TABLE t_order");
                                statement.executeUpdate("TRUNCATE TABLE t_order_item");
                                statement.executeUpdate("CREATE TABLE IF NOT EXISTS t_address (address_id BIGINT NOT NULL, address_name VARCHAR(100) NOT NULL, PRIMARY KEY (address_id))");
                                statement.executeUpdate("TRUNCATE TABLE t_address");
                            }

                            for (int i = 0; i < 10; i++) {
                                Address address = new Address();
                                address.setAddressId((long) i);
                                address.setAddressName("address_" + i);
                                try (Connection connection = dataSource.getConnection();
                                     PreparedStatement preparedStatement = connection.prepareStatement(
                                             "INSERT INTO t_address (address_id, address_name) VALUES (?, ?)")) {
                                    preparedStatement.setLong(1, address.getAddressId());
                                    preparedStatement.setString(2, address.getAddressName());
                                    preparedStatement.executeUpdate();
                                }
                            }
                            System.out.println("-------------- Process Success Begin ---------------");
                            System.out.println("---------------------------- Insert Data ----------------------------");
                            List<Long> orderIds = new ArrayList<>(10);
                            for (int i = 1; i <= 10; i++) {
                                Order order = new Order();
                                order.setUserId(i);
                                order.setAddressId(i);
                                order.setStatus("INSERT_TEST");
                                try (Connection connection = dataSource.getConnection();
                                     PreparedStatement preparedStatement = connection.prepareStatement(
                                             "INSERT INTO t_order (user_id, address_id, status) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                                    preparedStatement.setInt(1, order.getUserId());
                                    preparedStatement.setLong(2, order.getAddressId());
                                    preparedStatement.setString(3, order.getStatus());
                                    preparedStatement.executeUpdate();
                                    try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                                        if (resultSet.next()) {
                                            order.setOrderId(resultSet.getLong(1));
                                        }
                                    }
                                }
                                OrderItem item = new OrderItem();
                                item.setOrderId(order.getOrderId());
                                item.setUserId(i);
                                item.setStatus("INSERT_TEST");
                                try (Connection connection = dataSource.getConnection();
                                     PreparedStatement preparedStatement = connection.prepareStatement(
                                             "INSERT INTO t_order_item (order_id, user_id, status) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                                    preparedStatement.setLong(1, item.getOrderId());
                                    preparedStatement.setInt(2, item.getUserId());
                                    preparedStatement.setString(3, item.getStatus());
                                    preparedStatement.executeUpdate();
                                    try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                                        if (resultSet.next()) {
                                            item.setOrderItemId(resultSet.getLong(1));
                                        }
                                    }
                                }
                                orderIds.add(order.getOrderId());
                            }
                            System.out.println("---------------------------- Print Order Data -----------------------");
                            List<Order> result = new LinkedList<>();
                            try (Connection connection = dataSource.getConnection();
                                 PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM t_order");
                                 ResultSet resultSet = preparedStatement.executeQuery()) {
                                while (resultSet.next()) {
                                    Order order = new Order();
                                    order.setOrderId(resultSet.getLong(1));
                                    order.setUserId(resultSet.getInt(2));
                                    order.setAddressId(resultSet.getLong(3));
                                    order.setStatus(resultSet.getString(4));
                                    result.add(order);
                                }
                            }
                            result.forEach(System.out::println);
                            System.out.println("---------------------------- Print OrderItem Data -------------------");
                            // TODO Associated query with encrypt may query and decrypt failed. see https://github.com/apache/shardingsphere/issues/3352
//        String sql = "SELECT i.* FROM t_order o, t_order_item i WHERE o.order_id = i.order_id";
//        String sql = "SELECT * FROM t_order_item";
                            List<OrderItem> anotherResult = new LinkedList<>();
                            try (Connection connection = dataSource.getConnection();
                                 PreparedStatement preparedStatement = connection.prepareStatement("SELECT i.* FROM t_order o, t_order_item i WHERE o.order_id = i.order_id");
                                 ResultSet resultSet = preparedStatement.executeQuery()) {
                                while (resultSet.next()) {
                                    OrderItem orderItem = new OrderItem();
                                    orderItem.setOrderItemId(resultSet.getLong(1));
                                    orderItem.setOrderId(resultSet.getLong(2));
                                    orderItem.setUserId(resultSet.getInt(3));
                                    orderItem.setStatus(resultSet.getString(4));
                                    anotherResult.add(orderItem);
                                }
                            }
                            anotherResult.forEach(System.out::println);
                            System.out.println("---------------------------- Delete Data ----------------------------");
                            for (Long each : orderIds) {
                                try (Connection connection = dataSource.getConnection();
                                     PreparedStatement firstPreparedStatement = connection.prepareStatement("DELETE FROM t_order WHERE order_id=?");
                                     PreparedStatement secondPreparedStatement = connection.prepareStatement("DELETE FROM t_order_item WHERE order_id=?")) {
                                    firstPreparedStatement.setLong(1, each);
                                    firstPreparedStatement.executeUpdate();
                                    secondPreparedStatement.setLong(1, each);
                                    secondPreparedStatement.executeUpdate();
                                }
                            }
                            System.out.println("---------------------------- Print Order Data -----------------------");
                            List<Order> secondResult = new LinkedList<>();
                            try (Connection connection = dataSource.getConnection();
                                 PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM t_order");
                                 ResultSet resultSet = preparedStatement.executeQuery()) {
                                while (resultSet.next()) {
                                    Order order = new Order();
                                    order.setOrderId(resultSet.getLong(1));
                                    order.setUserId(resultSet.getInt(2));
                                    order.setAddressId(resultSet.getLong(3));
                                    order.setStatus(resultSet.getString(4));
                                    secondResult.add(order);
                                }
                            }
                            secondResult.forEach(System.out::println);
                            System.out.println("---------------------------- Print OrderItem Data -------------------");
                            // TODO Associated query with encrypt may query and decrypt failed. see https://github.com/apache/shardingsphere/issues/3352
//        String sql = "SELECT i.* FROM t_order o, t_order_item i WHERE o.order_id = i.order_id";
//        String sql = "SELECT * FROM t_order_item";
                            List<OrderItem> thirdResult = new LinkedList<>();
                            try (Connection connection = dataSource.getConnection();
                                 PreparedStatement preparedStatement = connection.prepareStatement("SELECT i.* FROM t_order o, t_order_item i WHERE o.order_id = i.order_id");
                                 ResultSet resultSet = preparedStatement.executeQuery()) {
                                while (resultSet.next()) {
                                    OrderItem orderItem = new OrderItem();
                                    orderItem.setOrderItemId(resultSet.getLong(1));
                                    orderItem.setOrderId(resultSet.getLong(2));
                                    orderItem.setUserId(resultSet.getInt(3));
                                    orderItem.setStatus(resultSet.getString(4));
                                    thirdResult.add(orderItem);
                                }
                            }
                            thirdResult.forEach(System.out::println);
                            System.out.println("-------------- Process Success Finish --------------");
                        } finally {
                            try (Connection connection = dataSource.getConnection();
                                 Statement statement = connection.createStatement()) {
                                statement.executeUpdate("DROP TABLE if exists t_order");
                                statement.executeUpdate("DROP TABLE if exists t_order_item");
                                statement.executeUpdate("DROP TABLE if exists t_address");
                            }
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

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
                        try (Connection connection = dataSource.getConnection();
                             Statement statement = connection.createStatement()) {
                            statement.executeUpdate("CREATE TABLE IF NOT EXISTS t_order (order_id BIGINT NOT NULL AUTO_INCREMENT, user_id INT NOT NULL, address_id BIGINT NOT NULL, status VARCHAR(50), PRIMARY KEY (order_id))");
                            statement.executeUpdate("CREATE TABLE IF NOT EXISTS t_order_item (order_item_id BIGINT NOT NULL AUTO_INCREMENT, order_id BIGINT NOT NULL, user_id INT NOT NULL, status VARCHAR(50), PRIMARY KEY (order_item_id))");
                            statement.executeUpdate("TRUNCATE TABLE t_order");
                            statement.executeUpdate("TRUNCATE TABLE t_order_item");
                            statement.executeUpdate("CREATE TABLE IF NOT EXISTS t_address (address_id BIGINT NOT NULL, address_name VARCHAR(100) NOT NULL, PRIMARY KEY (address_id))");
                            statement.executeUpdate("TRUNCATE TABLE t_address");
                        }
                        for (int i = 0; i < 10; i++) {
                            Address address = new Address();
                            address.setAddressId((long) i);
                            address.setAddressName("address_" + i);
                            try (Connection connection = dataSource.getConnection();
                                 PreparedStatement preparedStatement = connection.prepareStatement(
                                         "INSERT INTO t_address (address_id, address_name) VALUES (?, ?)")) {
                                preparedStatement.setLong(1, address.getAddressId());
                                preparedStatement.setString(2, address.getAddressName());
                                preparedStatement.executeUpdate();
                            }
                        }
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
                            }
                            statement.execute("select * from t_order");
                            statement.execute("SELECT i.* FROM t_order o, t_order_item i WHERE o.order_id = i.order_id");
                            statement.execute("select * from t_order_item");
                            statement.execute("INSERT INTO t_order (user_id, address_id, status) VALUES (1, 1, 'init')");
                            statement.executeUpdate("DROP TABLE if exists t_order");
                            statement.executeUpdate("DROP TABLE if exists t_order_item");
                            statement.executeUpdate("DROP TABLE if exists t_address");
                        }
                    } catch (SQLException | IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}
