

package org.apache.commons.dbcp2;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * This test *must* execute before all other tests to be effective as it tests
 * the initialisation of DriverManager.
 * Based on the test case for DBCP-212 written by Marcos Sanz
 */
public class TestDriverManagerConnectionFactory {

    private static final class ConnectionThread implements Runnable {
        private final DataSource ds;
        private volatile boolean result = true;

        private ConnectionThread(final DataSource ds) {
            this.ds = ds;
        }

        public boolean getResult() {
            return result;
        }

        @Override
        public void run() {
            Connection conn = null;
            try {
                conn = ds.getConnection();
            } catch (final Exception e) {
                e.printStackTrace();
                result = false;
            } finally {
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (final Exception e) {
                        e.printStackTrace();
                        result = false;
                    }
                }
            }
        }

        @Override
        public String toString() {
            return "ConnectionThread [ds=" + ds + ", result=" + result + "]";
        }
    }

    private static final String KEY_JDBC_DRIVERS = "jdbc.drivers";

    @AfterAll
    public static void afterClass() {
        System.clearProperty(KEY_JDBC_DRIVERS);
    }

    @BeforeAll
    public static void beforeClass() {
        System.setProperty(KEY_JDBC_DRIVERS, "org.apache.commons.dbcp2.TesterDriver");
    }

    @Test
    public void testDriverManagerCredentialsInUrl() throws SQLException {
        final DriverManagerConnectionFactory cf = new DriverManagerConnectionFactory("jdbc:apache:commons:testdriver;user=foo;password=bar", null,  (char[]) null);
        cf.createConnection();
    }

    public void testDriverManagerInit(final boolean withProperties) throws Exception {
        final GenericObjectPoolConfig<PoolableConnection> config = new GenericObjectPoolConfig<>();
        config.setMaxTotal(10);
        config.setMaxIdle(0);
        final Properties properties = new Properties();
        // The names "user" and "password" are specified in java.sql.DriverManager.getConnection(String, String, String)
        properties.setProperty(Constants.KEY_USER, "foo");
        properties.setProperty(Constants.KEY_PASSWORD, "bar");
        final ConnectionFactory connectionFactory = withProperties ?
                new DriverManagerConnectionFactory("jdbc:apache:commons:testdriver", properties) :
                new DriverManagerConnectionFactory("jdbc:apache:commons:testdriver", "foo", "bar");
        final PoolableConnectionFactory poolableConnectionFactory =
            new PoolableConnectionFactory(connectionFactory, null);
        poolableConnectionFactory.setDefaultReadOnly(Boolean.FALSE);
        poolableConnectionFactory.setDefaultAutoCommit(Boolean.TRUE);

        final GenericObjectPool<PoolableConnection> connectionPool =
                new GenericObjectPool<>(poolableConnectionFactory, config);
        poolableConnectionFactory.setPool(connectionPool);
        final PoolingDataSource<PoolableConnection> dataSource =
                new PoolingDataSource<>(connectionPool);

        final ConnectionThread[] connectionThreads = new ConnectionThread[10];
        final Thread[] threads = new Thread[10];

        for (int i = 0; i < 10; i++) {
            connectionThreads[i] = new ConnectionThread(dataSource);
            threads[i] = new Thread(connectionThreads[i]);
        }
        for (int i = 0; i < 10; i++) {
            threads[i].start();
        }
        for (int i = 0; i < 10; i++) {
            while (threads[i].isAlive()){//JDK1.5: getState() != Thread.State.TERMINATED) {
                Thread.sleep(100);
            }
            if (!connectionThreads[i].getResult()) {
                fail("Exception during getConnection(): " + connectionThreads[i]);
            }
        }
    }

    @Test
    public void testDriverManagerInitWithCredentials() throws Exception {
        testDriverManagerInit(false);
    }

    @Test
    public void testDriverManagerInitWithEmptyProperties() throws Exception {
        final ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(
                "jdbc:apache:commons:testdriver;user=foo;password=bar");
        connectionFactory.createConnection();
    }

    @Test
    public void testDriverManagerInitWithProperties() throws Exception {
        testDriverManagerInit(true);
    }

    @Test
    public void testDriverManagerWithoutCredentials() {
        final DriverManagerConnectionFactory cf = new DriverManagerConnectionFactory("jdbc:apache:commons:testdriver", null,  (char[]) null);
        assertThrows(ArrayIndexOutOfBoundsException.class, cf::createConnection); // thrown by TestDriver due to missing user
    }

    @Test
    public void testDriverManagerWithoutPassword() {
        final DriverManagerConnectionFactory cf = new DriverManagerConnectionFactory("jdbc:apache:commons:testdriver", "user", (char[]) null);
        assertThrows(SQLException.class, cf::createConnection); // thrown by TestDriver due to invalid password
    }

    @Test
    public void testDriverManagerWithoutUser() {
        final DriverManagerConnectionFactory cf = new DriverManagerConnectionFactory("jdbc:apache:commons:testdriver", null, "pass");
        assertThrows(IndexOutOfBoundsException.class, cf::createConnection); // thrown by TestDriver due to missing user
    }

}
