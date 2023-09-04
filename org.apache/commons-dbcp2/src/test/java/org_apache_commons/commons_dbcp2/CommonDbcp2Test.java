package org_apache_commons.commons_dbcp2;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DriverManagerConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.dbcp2.PoolingDriver;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SuppressWarnings({"SqlNoDataSourceInspection", "deprecation", "SqlDialectInspection"})
public class CommonDbcp2Test {

    /**
     * We cannot write unit tests for the `registerConnectionMBean` property because the version
     * containing <a href="https://issues.apache.org/jira/browse/DBCP-585">Connection level JMX queries result in concurrent access to connection objects, causing errors</a>
     * has not yet been released.
     *
     * @see org.apache.commons.dbcp2.BasicDataSource
     */
    @Test
    void testBasicDataSource() {
        try (BasicDataSource dataSource = new BasicDataSource()) {
            dataSource.setUsername("sa");
            dataSource.setPassword("");
            dataSource.setUrl("jdbc:h2:mem:test");
            dataSource.setDriverClassName("org.h2.Driver");
            dataSource.setCacheState(true);
            dataSource.setDefaultQueryTimeout(null);
            dataSource.setEnableAutoCommitOnReturn(true);
            dataSource.setRollbackOnReturn(true);
            dataSource.setInitialSize(0);
            dataSource.setMaxTotal(8);
            dataSource.setMaxIdle(8);
            dataSource.setMinIdle(0);
            dataSource.setMaxWaitMillis(-1);
            dataSource.setTestOnCreate(false);
            dataSource.setTestOnBorrow(true);
            dataSource.setTestOnReturn(false);
            dataSource.setTestWhileIdle(false);
            dataSource.setTimeBetweenEvictionRunsMillis(-1);
            dataSource.setNumTestsPerEvictionRun(3);
            dataSource.setMinEvictableIdleTimeMillis(1000 * 60 * 30);
            dataSource.setSoftMinEvictableIdleTimeMillis(-1);
            dataSource.setMaxConnLifetimeMillis(-1);
            dataSource.setLogExpiredConnections(true);
            dataSource.setConnectionInitSqls(null);
            dataSource.setLifo(true);
            dataSource.setPoolPreparedStatements(false);
            dataSource.setMaxOpenPreparedStatements(-1);
            dataSource.setAccessToUnderlyingConnectionAllowed(false);
            dataSource.setRemoveAbandonedOnMaintenance(false);
            dataSource.setRemoveAbandonedOnBorrow(false);
            dataSource.setRemoveAbandonedTimeout(300);
            dataSource.setLogAbandoned(false);
            dataSource.setAbandonedUsageTracking(false);
            dataSource.setFastFailValidation(false);
            dataSource.setDisconnectionSqlCodes(null);
            assertThat(dataSource.getUsername()).isEqualTo("sa");
            assertThat(dataSource.getPassword()).isEqualTo("");
            assertThat(dataSource.getUrl()).isEqualTo("jdbc:h2:mem:test");
            assertThat(dataSource.getDriverClassName()).isEqualTo("org.h2.Driver");
            assertThat(dataSource.getDefaultAutoCommit()).isNull();
            assertThat(dataSource.getDefaultReadOnly()).isNull();
            assertThat(dataSource.getDefaultTransactionIsolation()).isEqualTo(-1);
            assertThat(dataSource.getDefaultCatalog()).isNull();
            assertThat(dataSource.getCacheState()).isEqualTo(true);
            assertThat(dataSource.getDefaultQueryTimeout()).isEqualTo(null);
            assertThat(dataSource.getEnableAutoCommitOnReturn()).isEqualTo(true);
            assertThat(dataSource.getRollbackOnReturn()).isEqualTo(true);
            assertThat(dataSource.getInitialSize()).isEqualTo(0);
            assertThat(dataSource.getMaxTotal()).isEqualTo(8);
            assertThat(dataSource.getMaxIdle()).isEqualTo(8);
            assertThat(dataSource.getMinIdle()).isEqualTo(0);
            assertThat(dataSource.getMaxWaitMillis()).isEqualTo(-1);
            assertThat(dataSource.getValidationQuery()).isNull();
            assertThat(dataSource.getValidationQueryTimeout()).isEqualTo(-1);
            assertThat(dataSource.getTestOnCreate()).isEqualTo(false);
            assertThat(dataSource.getTestOnBorrow()).isEqualTo(true);
            assertThat(dataSource.getTestOnReturn()).isEqualTo(false);
            assertThat(dataSource.getTestWhileIdle()).isEqualTo(false);
            assertThat(dataSource.getTimeBetweenEvictionRunsMillis()).isEqualTo(-1);
            assertThat(dataSource.getNumTestsPerEvictionRun()).isEqualTo(3);
            assertThat(dataSource.getMinEvictableIdleTimeMillis()).isEqualTo(1000 * 60 * 30);
            assertThat(dataSource.getSoftMinEvictableIdleTimeMillis()).isEqualTo(-1);
            assertThat(dataSource.getMaxConnLifetimeMillis()).isEqualTo(-1);
            assertThat(dataSource.getLogExpiredConnections()).isEqualTo(true);
            assertThat(dataSource.getConnectionInitSqls()).isEqualTo(new ArrayList<>());
            assertThat(dataSource.getLifo()).isEqualTo(true);
            assertThat(dataSource.isPoolPreparedStatements()).isEqualTo(false);
            assertThat(dataSource.getMaxOpenPreparedStatements()).isEqualTo(-1);
            assertThat(dataSource.isAccessToUnderlyingConnectionAllowed()).isEqualTo(false);
            assertThat(dataSource.getRemoveAbandonedOnMaintenance()).isEqualTo(false);
            assertThat(dataSource.getRemoveAbandonedOnBorrow()).isEqualTo(false);
            assertThat(dataSource.getRemoveAbandonedTimeout()).isEqualTo(300);
            assertThat(dataSource.getLogAbandoned()).isEqualTo(false);
            assertThat(dataSource.getAbandonedUsageTracking()).isEqualTo(false);
            assertThat(dataSource.getFastFailValidation()).isEqualTo(false);
            assertThat(dataSource.getDisconnectionSqlCodes()).isEqualTo(new HashSet<>());
            assertDoesNotThrow(() -> {
                try (Connection connection = dataSource.getConnection();
                     Statement statement = connection.createStatement()) {
                    statement.executeQuery("select 1");
                }
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testPoolingDataSource() {
        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory("jdbc:h2:mem:test", null);
        PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory, null);
        ObjectPool<PoolableConnection> poolableConnectionGenericObjectPool = new GenericObjectPool<>(poolableConnectionFactory);
        poolableConnectionFactory.setPool(poolableConnectionGenericObjectPool);
        PoolingDataSource<PoolableConnection> dataSource = new PoolingDataSource<>(poolableConnectionGenericObjectPool);
        assertThat(dataSource.isAccessToUnderlyingConnectionAllowed()).isEqualTo(false);
        assertDoesNotThrow(() -> {
            try (Connection connection = dataSource.getConnection();
                 Statement statement = connection.createStatement()) {
                statement.executeQuery("select 1");
            }
        });
    }

    @Test
    void testPoolingDriver() throws ClassNotFoundException, SQLException {
        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory("jdbc:h2:mem:test", null);
        PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory, null);
        ObjectPool<PoolableConnection> poolableConnectionGenericObjectPool = new GenericObjectPool<>(poolableConnectionFactory);
        poolableConnectionFactory.setPool(poolableConnectionGenericObjectPool);
        Class.forName("org.apache.commons.dbcp2.PoolingDriver");
        PoolingDriver driver = (PoolingDriver) DriverManager.getDriver("jdbc:apache:commons:dbcp:");
        driver.registerPool("testPoolingDriver", poolableConnectionGenericObjectPool);
        assertDoesNotThrow(() -> {
            try (Connection connection = DriverManager.getConnection("jdbc:apache:commons:dbcp:testPoolingDriver");
                 Statement statement = connection.createStatement()) {
                statement.executeQuery("select 1");
            }
        });
    }
}
