

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

import static org.junit.jupiter.api.Assertions.*;

/**
 */
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

    /**
     * JIRA DBCP-216
     *
     * Verify that pool counters are maintained properly and listeners are
     * cleaned up when a PooledConnection throws a connectionError event.
     */
    @Test
    public void testConnectionErrorCleanup() throws Exception {
        // Setup factory
        final CPDSConnectionFactory factory = new CPDSConnectionFactory(
                cpds, null, -1, false, "userName", "password");
        final GenericObjectPool<PooledConnectionAndInfo> pool =
                new GenericObjectPool<>(factory);
        factory.setPool(pool);

        // Checkout a pair of connections
        final PooledConnection pcon1 = pool.borrowObject().getPooledConnection();
        final Connection con1 = pcon1.getConnection();
        final PooledConnection pcon2 = pool.borrowObject().getPooledConnection();
        assertEquals(2, pool.getNumActive());
        assertEquals(0, pool.getNumIdle());

        // Verify listening
        final PooledConnectionProxy pc = (PooledConnectionProxy) pcon1;
        assertTrue(pc.getListeners().contains(factory));

        // Throw connectionError event
        pc.throwConnectionError();

        // Active count should be reduced by 1 and no idle increase
        assertEquals(1, pool.getNumActive());
        assertEquals(0, pool.getNumIdle());

        // Throw another one - should be ignored
        pc.throwConnectionError();
        assertEquals(1, pool.getNumActive());
        assertEquals(0, pool.getNumIdle());

        // Ask for another connection
        final PooledConnection pcon3 = pool.borrowObject().getPooledConnection();
        assertNotEquals(pcon3, pcon1); // better not get baddie back
        assertFalse(pc.getListeners().contains(factory)); // verify cleanup
        assertEquals(2, pool.getNumActive());
        assertEquals(0, pool.getNumIdle());

        // Return good connections back to pool
        pcon2.getConnection().close();
        pcon3.getConnection().close();
        assertEquals(2, pool.getNumIdle());
        assertEquals(0, pool.getNumActive());

        // Verify pc is closed
        try {
           pc.getConnection();
           fail("Expecting SQLException using closed PooledConnection");
        } catch (final SQLException ex) {
            // expected
        }

        // Back from the dead - ignore the ghost!
        con1.close();
        assertEquals(2, pool.getNumIdle());
        assertEquals(0, pool.getNumActive());

        // Clear pool
        factory.getPool().clear();
        assertEquals(0, pool.getNumIdle());
    }

    /**
     * JIRA: DBCP-442
     */
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
        final char[] pwd = {'a' };
        factory.setPassword(pwd);
        assertEquals("a", String.valueOf(factory.getPasswordCharArray()));
        pwd[0] = 'b';
        assertEquals("a", String.valueOf(factory.getPasswordCharArray()));
    }

    /**
     * JIRA DBCP-216
     *
     * Check PoolableConnection close triggered by destroy is handled
     * properly. PooledConnectionProxy (dubiously) fires connectionClosed
     * when PooledConnection itself is closed.
     */
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
       conn3.close(); // Return to pool will trigger destroy -> close sequence
       assertEquals(2, ds.getNumIdle("userName"));
       ds.close();
    }

}