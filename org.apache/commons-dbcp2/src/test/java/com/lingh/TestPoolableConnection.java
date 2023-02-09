
package com.lingh;

import org.apache.commons.dbcp2.DelegatingConnection;
import org.apache.commons.dbcp2.DriverConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.TesterConnection;
import org.apache.commons.dbcp2.TesterDriver;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@SuppressWarnings("CastCanBeRemovedNarrowingVariableType")
public class TestPoolableConnection {
    private GenericObjectPool<PoolableConnection> pool;

    @BeforeEach
    public void setUp() throws Exception {
        final PoolableConnectionFactory factory = new PoolableConnectionFactory(
                new DriverConnectionFactory(new TesterDriver(), "jdbc:apache:commons:testdriver", null),
                null);
        factory.setDefaultAutoCommit(Boolean.TRUE);
        factory.setDefaultReadOnly(Boolean.TRUE);
        pool = new GenericObjectPool<>(factory);
        factory.setPool(pool);
    }

    @AfterEach
    public void tearDown() {
        pool.close();
    }

    @Test
    public void testClosingWrappedInDelegate() throws Exception {
        assertEquals(0, pool.getNumActive());
        final Connection conn = pool.borrowObject();
        final DelegatingConnection<Connection> outer = new DelegatingConnection<>(conn);
        assertFalse(outer.isClosed());
        assertFalse(conn.isClosed());
        assertEquals(1, pool.getNumActive());
        outer.close();
        assertTrue(outer.isClosed());
        assertTrue(conn.isClosed());
        assertEquals(0, pool.getNumActive());
        assertEquals(1, pool.getNumIdle());
    }

    @Test
    public void testConnectionPool() throws Exception {
        final Connection c = pool.borrowObject();
        assertNotNull(c, "Connection should be created and should not be null");
        assertEquals(1, pool.getNumActive(), "There should be exactly one active object in the pool");
        c.close();
        assertEquals(0, pool.getNumActive(), "There should now be zero active objects in the pool");
    }

    @Test
    public void testFastFailValidation() throws Exception {
        pool.setTestOnReturn(true);
        final PoolableConnectionFactory factory = (PoolableConnectionFactory) pool.getFactory();
        factory.setFastFailValidation(true);
        final PoolableConnection conn = pool.borrowObject();
        final TesterConnection nativeConnection = (TesterConnection) conn.getInnermostDelegate();
        nativeConnection.setFailure(new SQLException("Not fatal error.", "Invalid syntax."));
        try {
            conn.createStatement();
            fail("Should throw SQL exception.");
        } catch (final SQLException ignored) {
            nativeConnection.setFailure(null);
        }
        conn.validate("SELECT 1", 1000);
        nativeConnection.setFailure(new SQLException("Fatal connection error.", "01002"));
        try {
            conn.createStatement();
            fail("Should throw SQL exception.");
        } catch (final SQLException ignored) {
            nativeConnection.setFailure(null);
        }
        try {
            conn.validate("SELECT 1", 1000);
            fail("Should throw SQL exception on validation.");
        } catch (final SQLException notValid) {
        }
        conn.close();
        assertEquals(0, pool.getNumActive(), "The pool should have no active connections");
        assertEquals(0, pool.getNumIdle(), "The pool should have no idle connections");
    }

    @Test
    public void testFastFailValidationCustomCodes() throws Exception {
        pool.setTestOnReturn(true);
        final PoolableConnectionFactory factory = (PoolableConnectionFactory) pool.getFactory();
        factory.setFastFailValidation(true);
        final ArrayList<String> disconnectionSqlCodes = new ArrayList<>();
        disconnectionSqlCodes.add("XXX");
        factory.setDisconnectionSqlCodes(disconnectionSqlCodes);
        final PoolableConnection conn = pool.borrowObject();
        final TesterConnection nativeConnection = (TesterConnection) conn.getInnermostDelegate();
        nativeConnection.setFailure(new SQLException("Fatal connection error.", "XXX"));
        try {
            conn.createStatement();
            fail("Should throw SQL exception.");
        } catch (final SQLException ignored) {
            nativeConnection.setFailure(null);
        }
        conn.close();
        assertEquals(0, pool.getNumActive(), "The pool should have no active connections");
        assertEquals(0, pool.getNumIdle(), "The pool should have no idle connections");
    }

    @Test
    public void testPoolableConnectionLeak() throws Exception {
        final Connection conn = pool.borrowObject();
        ((PoolableConnection) conn).getInnermostDelegate().close();
        try {
            conn.close();
        } catch (final SQLException e) {
        }
        assertEquals(0, pool.getNumActive(), "The pool should have no active connections");
    }
}