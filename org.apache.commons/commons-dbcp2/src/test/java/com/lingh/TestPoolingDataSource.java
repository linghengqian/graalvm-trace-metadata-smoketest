

package com.lingh;

import org.apache.commons.dbcp2.*;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TestSuite for PoolingDataSource
 */
public class TestPoolingDataSource extends TestConnectionPool {

    protected PoolingDataSource<PoolableConnection> ds;

    private GenericObjectPool<PoolableConnection> pool;
    @Override
    protected Connection getConnection() throws Exception {
        return ds.getConnection();
    }

    @BeforeEach
    public void setUp() throws Exception {
        final Properties properties = new Properties();
        properties.setProperty(Constants.KEY_USER, "userName");
        properties.setProperty(Constants.KEY_PASSWORD, "password");
        final PoolableConnectionFactory factory =
            new PoolableConnectionFactory(
                    new DriverConnectionFactory(new TesterDriver(),
                            "jdbc:apache:commons:testdriver", properties),
                    null);
        factory.setValidationQuery("SELECT DUMMY FROM DUAL");
        factory.setDefaultReadOnly(Boolean.TRUE);
        factory.setDefaultAutoCommit(Boolean.TRUE);
        pool = new GenericObjectPool<>(factory);
        factory.setPool(pool);
        pool.setMaxTotal(getMaxTotal());
        pool.setMaxWaitMillis(getMaxWaitMillis());
        ds = new PoolingDataSource<>(pool);
        ds.setAccessToUnderlyingConnectionAllowed(true);
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception {
        ds.close();
        super.tearDown();
    }

    @Test
    public void testClose() throws Exception {

        final Properties properties = new Properties();
        properties.setProperty(Constants.KEY_USER, "userName");
        properties.setProperty(Constants.KEY_PASSWORD, "password");
        final PoolableConnectionFactory f =
            new PoolableConnectionFactory(
                    new DriverConnectionFactory(new TesterDriver(),
                            "jdbc:apache:commons:testdriver", properties),
                    null);
        f.setValidationQuery("SELECT DUMMY FROM DUAL");
        f.setDefaultReadOnly(Boolean.TRUE);
        f.setDefaultAutoCommit(Boolean.TRUE);
        final GenericObjectPool<PoolableConnection> p = new GenericObjectPool<>(f);
        p.setMaxTotal(getMaxTotal());
        p.setMaxWaitMillis(getMaxWaitMillis());

        try ( PoolingDataSource<PoolableConnection> dataSource = new PoolingDataSource<>(p) ) {
            final Connection connection = dataSource.getConnection();
            assertNotNull(connection);
            connection.close();
        }

        assertTrue(p.isClosed());
        assertEquals(0, p.getNumIdle());
        assertEquals(0, p.getNumActive());
    }

    /**
     * DBCP-412
     * Verify that omitting factory.setPool(pool) when setting up PDS does not
     * result in NPE.
     */
    @Test
    public void testFixFactoryConfig() throws Exception {
        final Properties properties = new Properties();
        properties.setProperty(Constants.KEY_USER, "userName");
        properties.setProperty(Constants.KEY_PASSWORD, "password");
        final PoolableConnectionFactory f =
            new PoolableConnectionFactory(
                    new DriverConnectionFactory(new TesterDriver(),
                            "jdbc:apache:commons:testdriver", properties),
                    null);
        f.setValidationQuery("SELECT DUMMY FROM DUAL");
        f.setDefaultReadOnly(Boolean.TRUE);
        f.setDefaultAutoCommit(Boolean.TRUE);
        final GenericObjectPool<PoolableConnection> p = new GenericObjectPool<>(f);
        p.setMaxTotal(getMaxTotal());
        p.setMaxWaitMillis(getMaxWaitMillis());
        ds = new PoolingDataSource<>(p);
        assertEquals(f.getPool(), p);
        ds.getConnection();
    }

    @Test
    public void testIsWrapperFor() throws Exception {
        assertTrue(ds.isWrapperFor(PoolingDataSource.class));
        assertTrue(ds.isWrapperFor(AutoCloseable.class));
        assertFalse(ds.isWrapperFor(String.class));
        assertFalse(ds.isWrapperFor(null));
    }

    @Test
    public void testPoolGuardConnectionWrapperEqualInnermost() throws Exception {
        ds.setAccessToUnderlyingConnectionAllowed(true);
        final DelegatingConnection<?> con = (DelegatingConnection<?>) ds.getConnection();
        final Connection inner = con.getInnermostDelegate();
        ds.setAccessToUnderlyingConnectionAllowed(false);
        final DelegatingConnection<Connection> con2 = new DelegatingConnection<>(inner);
        assertNotEquals(con2, con);
        assertTrue(con.innermostDelegateEquals(con2.getInnermostDelegate()));
        assertTrue(con2.innermostDelegateEquals(inner));
        assertNotEquals(con, con2);
    }

    @Test
    public void testPoolGuardConnectionWrapperEqualsFail() throws Exception {
        final Connection con1 = ds.getConnection();
        final Connection con2 = ds.getConnection();
        assertNotEquals(con1, con2);
        con1.close();
        con2.close();
    }

    @Test
    public void testPoolGuardConnectionWrapperEqualsNull() throws Exception {
        final Connection con1 = ds.getConnection();
        final Connection con2 = null;
        assertNotEquals(con2, con1);
        con1.close();
    }

    /*
     * JIRA: DBCP-198
     */
    @Test
    public void testPoolGuardConnectionWrapperEqualsReflexive()
        throws Exception {
        final Connection con = ds.getConnection();
        final Connection con2 = con;
        assertEquals(con2, con);
        assertEquals(con, con2);
        con.close();
    }

    @Test
    public void testPoolGuardConnectionWrapperEqualsSameDelegate() throws Exception {
        // Get a maximal set of connections from the pool
        final Connection[] c = new Connection[getMaxTotal()];
        for (int i = 0; i < c.length; i++) {
            c[i] = newConnection();
        }
        // Close the delegate of one wrapper in the pool
        ((DelegatingConnection<?>) c[0]).getDelegate().close();

        // Grab a new connection - should get c[0]'s closed connection
        // so should be delegate-equivalent
        final Connection con = newConnection();
        Assertions.assertNotEquals(c[0], con);
        Assertions.assertEquals(
                ((DelegatingConnection<?>) c[0]).getInnermostDelegateInternal(),
                ((DelegatingConnection<?>) con).getInnermostDelegateInternal());
        for (final Connection element : c) {
            element.close();
        }
    }

    @Test
    public void testPoolGuardConnectionWrapperEqualsType() throws Exception {
        final Connection con1 = ds.getConnection();
        final Integer con2 = 0;
        assertNotEquals(con2, con1);
        con1.close();
    }

    @Test
    public void testUnwrap() throws Exception {
        assertSame(ds.unwrap(PoolingDataSource.class), ds);
        assertSame(ds.unwrap(AutoCloseable.class), ds);
        assertThrows(SQLException.class, () -> ds.unwrap(String.class));
        assertThrows(SQLException.class, () -> ds.unwrap(null));
    }
}
