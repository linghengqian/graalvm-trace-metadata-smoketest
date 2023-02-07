
package com.lingh.managed;

import org.apache.commons.dbcp2.*;
import org.apache.commons.dbcp2.managed.LocalXAConnectionFactory;
import org.apache.commons.dbcp2.managed.ManagedDataSource;
import org.apache.commons.dbcp2.managed.XAConnectionFactory;
import org.apache.commons.pool2.SwallowedExceptionListener;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.geronimo.transaction.manager.TransactionManagerImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.transaction.TransactionManager;
import javax.transaction.xa.XAException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test for ManagedConnection cached state.
 */
public class TestManagedConnectionCachedState {

    private static class SwallowedExceptionRecorder implements SwallowedExceptionListener {

        private final List<Exception> exceptions = new ArrayList<>();

        public List<Exception> getExceptions() {
            return exceptions;
        }

        @Override
        public void onSwallowException(final Exception e) {
            exceptions.add(e);
        }
    }

    private PoolingDataSource<PoolableConnection> ds;

    private GenericObjectPool<PoolableConnection> pool;

    private TransactionManager transactionManager;

    private SwallowedExceptionRecorder swallowedExceptionRecorder;

    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    @BeforeEach
    public void setUp() throws XAException {
        // create a GeronimoTransactionManager for testing
        transactionManager = new TransactionManagerImpl();

        // create a driver connection factory
        final Properties properties = new Properties();
        properties.setProperty(Constants.KEY_USER, "userName");
        properties.setProperty(Constants.KEY_PASSWORD, "password");
        final ConnectionFactory connectionFactory = new DriverConnectionFactory(new TesterDriver(), "jdbc:apache:commons:testdriver", properties);

        // wrap it with a LocalXAConnectionFactory
        final XAConnectionFactory xaConnectionFactory = new LocalXAConnectionFactory(transactionManager, connectionFactory);

        // create the pool object factory
        // make sure we ask for state caching
        final PoolableConnectionFactory factory = new PoolableConnectionFactory(xaConnectionFactory, null);
        factory.setValidationQuery("SELECT DUMMY FROM DUAL");
        factory.setCacheState(true);

        // create the pool
        pool = new GenericObjectPool<>(factory);
        factory.setPool(pool);
        // record swallowed exceptions
        swallowedExceptionRecorder = new SwallowedExceptionRecorder();
        pool.setSwallowedExceptionListener(swallowedExceptionRecorder);

        // finally create the datasource
        ds = new ManagedDataSource<>(pool, xaConnectionFactory.getTransactionRegistry());
        ds.setAccessToUnderlyingConnectionAllowed(true);
    }

    @AfterEach
    public void tearDown() {
        pool.close();
    }

    @Test
    public void testConnectionCachedState() throws Exception {
        // see DBCP-568

        // begin a transaction
        transactionManager.begin();
        // acquire a connection enlisted in the transaction
        try (final Connection conn = getConnection()) {
            // check the autocommit status to trigger internal caching
            conn.getAutoCommit();
            // ask the transaction manager to rollback
            transactionManager.rollback();
        }
        // check that no exceptions about failed rollback during close were logged
        assertEquals(0, swallowedExceptionRecorder.getExceptions().size());
    }

}
