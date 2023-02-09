

package org.apache.commons.dbcp2.datasources;

import com.lingh.datasources.ConnectionPoolDataSourceProxy;
import com.lingh.datasources.PooledConnectionProxy;
import org.apache.commons.dbcp2.cpdsadapter.DriverAdapterCPDS;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.PooledConnection;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class TestCPDSConnectionFactory {

    protected ConnectionPoolDataSourceProxy cpds;

    @BeforeEach
    public void setUp() throws Exception {
        cpds = new ConnectionPoolDataSourceProxy(new DriverAdapterCPDS());
        final DriverAdapterCPDS delegate = (DriverAdapterCPDS) cpds.getDelegate();
        delegate.setDriver("org.apache.commons.dbcp2.TesterDriver");
        delegate.setUrl("jdbc:apache:commons:testdriver");
        delegate.setUser("userName");
        delegate.setPassword("password");
    }

    @Test
    public void testConnectionErrorCleanup() throws Exception {
        final CPDSConnectionFactory factory = new CPDSConnectionFactory(
                cpds, null, -1, false, "userName", "password");
        final GenericObjectPool<PooledConnectionAndInfo> pool =
                new GenericObjectPool<>(factory);
        factory.setPool(pool);
        final PooledConnection pcon1 = pool.borrowObject().getPooledConnection();
        final Connection con1 = pcon1.getConnection();
        final PooledConnection pcon2 = pool.borrowObject().getPooledConnection();
        assertEquals(2, pool.getNumActive());
        assertEquals(0, pool.getNumIdle());
        final PooledConnectionProxy pc = (PooledConnectionProxy) pcon1;
        assertTrue(pc.getListeners().contains(factory));
        pc.throwConnectionError();
        assertEquals(1, pool.getNumActive());
        assertEquals(0, pool.getNumIdle());
        pc.throwConnectionError();
        assertEquals(1, pool.getNumActive());
        assertEquals(0, pool.getNumIdle());
        final PooledConnection pcon3 = pool.borrowObject().getPooledConnection();
        assertNotEquals(pcon3, pcon1);
        assertFalse(pc.getListeners().contains(factory));
        assertEquals(2, pool.getNumActive());
        assertEquals(0, pool.getNumIdle());
        pcon2.getConnection().close();
        pcon3.getConnection().close();
        assertEquals(2, pool.getNumIdle());
        assertEquals(0, pool.getNumActive());
        try {
            pc.getConnection();
            fail("Expecting SQLException using closed PooledConnection");
        } catch (final SQLException ex) {
        }
        con1.close();
        assertEquals(2, pool.getNumIdle());
        assertEquals(0, pool.getNumActive());
        factory.getPool().clear();
        assertEquals(0, pool.getNumIdle());
    }

    @Test
    public void testNullValidationQuery() throws Exception {
        final CPDSConnectionFactory factory =
                new CPDSConnectionFactory(cpds, null, -1, false, "userName", "password");
        final GenericObjectPool<PooledConnectionAndInfo> pool = new GenericObjectPool<>(factory);
        factory.setPool(pool);
        pool.setTestOnBorrow(true);
        final PooledConnection pcon = pool.borrowObject().getPooledConnection();
        final Connection con = pcon.getConnection();
        con.close();
    }

    @Test
    public void testSetPasswordThenModCharArray() {
        final CPDSConnectionFactory factory = new CPDSConnectionFactory(cpds, null, -1, false, "userName", "password");
        final char[] pwd = {'a'};
        factory.setPassword(pwd);
        assertEquals("a", String.valueOf(factory.getPasswordCharArray()));
        pwd[0] = 'b';
        assertEquals("a", String.valueOf(factory.getPasswordCharArray()));
    }

    @Test
    public void testSharedPoolDSDestroyOnReturn() throws Exception {
        final PerUserPoolDataSource ds = new PerUserPoolDataSource();
        ds.setConnectionPoolDataSource(cpds);
        ds.setPerUserMaxTotal("userName", 10);
        ds.setPerUserMaxWaitMillis("userName", 50L);
        ds.setPerUserMaxIdle("userName", 2);
        final Connection conn1 = ds.getConnection("userName", "password");
        final Connection conn2 = ds.getConnection("userName", "password");
        final Connection conn3 = ds.getConnection("userName", "password");
        assertEquals(3, ds.getNumActive("userName"));
        conn1.close();
        assertEquals(1, ds.getNumIdle("userName"));
        conn2.close();
        assertEquals(2, ds.getNumIdle("userName"));
        conn3.close();
        assertEquals(2, ds.getNumIdle("userName"));
        ds.close();
    }

}