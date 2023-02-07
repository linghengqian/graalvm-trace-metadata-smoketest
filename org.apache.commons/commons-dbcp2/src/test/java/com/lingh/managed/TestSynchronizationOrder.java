
package com.lingh.managed;

import com.lingh.TesterClassLoader;
import com.lingh.transaction.TransactionAdapter;
import com.lingh.transaction.TransactionManagerAdapter;
import com.lingh.transaction.TransactionSynchronizationRegistryAdapter;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbcp2.DelegatingConnection;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.managed.BasicManagedDataSource;
import org.apache.commons.dbcp2.managed.DataSourceXAConnectionFactory;
import org.apache.commons.dbcp2.managed.ManagedDataSource;
import org.apache.commons.dbcp2.managed.TesterBasicXAConnection;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.*;
import javax.transaction.xa.XAResource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestSynchronizationOrder {

    private boolean transactionManagerRegistered;
    private boolean transactionSynchronizationRegistryRegistered;
    private TransactionManager transactionManager;
    private TransactionSynchronizationRegistry transactionSynchronizationRegistry;
    private XADataSource xads;
    private BasicManagedDataSource bmds;
    private BasicDataSource bds;

    @BeforeEach
    public void setup() {
        transactionManager = new TransactionManagerAdapter() {

            private Transaction transaction;

            @Override
            public void begin() throws NotSupportedException, SystemException {
                transaction = new TransactionAdapter() {

                    @Override
                    public boolean enlistResource(final XAResource xaResource) throws IllegalStateException, RollbackException, SystemException {
                        // Called and used
                        return true;
                    }

                    @Override
                    public void registerSynchronization(final Synchronization synchronization) throws IllegalStateException, RollbackException, SystemException {
                        transactionManagerRegistered = true;
                    }
                };
            }

            @Override
            public Transaction getTransaction() throws SystemException {
                return transaction;
            }

        };

        transactionSynchronizationRegistry = new TransactionSynchronizationRegistryAdapter() {

            @Override
            public void registerInterposedSynchronization(final Synchronization synchronization) {
                transactionSynchronizationRegistryRegistered = true;
            }

        };

        bmds = new BasicManagedDataSource();
        bmds.setTransactionManager(transactionManager);
        bmds.setTransactionSynchronizationRegistry(transactionSynchronizationRegistry);
        bmds.setXADataSource("notnull");
        bds = new BasicDataSource();
        bds.setDriverClassName("org.apache.commons.dbcp2.TesterDriver");
        bds.setUrl("jdbc:apache:commons:testdriver");
        bds.setMaxTotal(10);
        bds.setMaxWaitMillis(100L);
        bds.setDefaultAutoCommit(Boolean.TRUE);
        bds.setDefaultReadOnly(Boolean.FALSE);
        bds.setDefaultTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        bds.setDefaultCatalog("test catalog");
        bds.setUsername("userName");
        bds.setPassword("password");
        bds.setValidationQuery("SELECT DUMMY FROM DUAL");
        bds.setConnectionInitSqls(Arrays.asList("SELECT 1", "SELECT 2"));
        bds.setDriverClassLoader(new TesterClassLoader());
        bds.setJmxName("org.apache.commons.dbcp2:name=test");
        final AtomicInteger closeCounter = new AtomicInteger();
        final InvocationHandler handle = new InvocationHandler() {
            protected XAConnection getXAConnection() throws SQLException {
                return new TesterBasicXAConnection(bds.getConnection(), closeCounter);
            }

            @Override
            public Object invoke(final Object proxy, final Method method, final Object[] args)
                    throws Throwable {
                final String methodName = method.getName();
                if (methodName.equals("hashCode")) {
                    return System.identityHashCode(proxy);
                }
                if (methodName.equals("equals")) {
                    return proxy == args[0];
                }
                if (methodName.equals("getXAConnection")) {
                    // both zero and 2-arg signatures
                    return getXAConnection();
                }
                try {
                    return method.invoke(bds, args);
                } catch (final InvocationTargetException e) {
                    throw e.getTargetException();
                }
            }
        };
        xads = (XADataSource) Proxy.newProxyInstance(
                TestSynchronizationOrder.class.getClassLoader(),
                new Class[]{XADataSource.class}, handle);
        bmds.setXaDataSourceInstance(xads);

    }

    @AfterEach
    public void tearDown() throws SQLException {
        bds.close();
        bmds.close();
    }

    @Test
    public void testInterposedSynchronization() throws Exception {
        final DataSourceXAConnectionFactory xaConnectionFactory = new DataSourceXAConnectionFactory(transactionManager,
            xads, transactionSynchronizationRegistry);

        final PoolableConnectionFactory factory = new PoolableConnectionFactory(xaConnectionFactory, null);
        factory.setValidationQuery("SELECT DUMMY FROM DUAL");
        factory.setDefaultReadOnly(Boolean.TRUE);
        factory.setDefaultAutoCommit(Boolean.TRUE);

        // create the pool
        try (final GenericObjectPool<PoolableConnection> pool = new GenericObjectPool<>(factory)) {
            factory.setPool(pool);
            pool.setMaxTotal(10);
            pool.setMaxWaitMillis(1000);

            // finally create the datasource
            try (final ManagedDataSource<PoolableConnection> ds = new ManagedDataSource<>(pool,
                xaConnectionFactory.getTransactionRegistry())) {
                ds.setAccessToUnderlyingConnectionAllowed(true);

                transactionManager.begin();
                try (final DelegatingConnection<?> connectionA = (DelegatingConnection<?>) ds.getConnection()) {
                    // Close right away.
                }
                transactionManager.commit();
                assertFalse(transactionManagerRegistered);
                assertTrue(transactionSynchronizationRegistryRegistered);
            }
        }
    }

    @Test
    public void testSessionSynchronization() throws Exception {
        final DataSourceXAConnectionFactory xaConnectionFactory = new DataSourceXAConnectionFactory(transactionManager,
            xads);

        final PoolableConnectionFactory factory = new PoolableConnectionFactory(xaConnectionFactory, null);
        factory.setValidationQuery("SELECT DUMMY FROM DUAL");
        factory.setDefaultReadOnly(Boolean.TRUE);
        factory.setDefaultAutoCommit(Boolean.TRUE);

        // create the pool
        try (final GenericObjectPool<PoolableConnection> pool = new GenericObjectPool<>(factory)) {
            factory.setPool(pool);
            pool.setMaxTotal(10);
            pool.setMaxWaitMillis(1000);

            // finally create the datasource
            try (final ManagedDataSource<PoolableConnection> ds = new ManagedDataSource<>(pool,
                xaConnectionFactory.getTransactionRegistry())) {
                ds.setAccessToUnderlyingConnectionAllowed(true);

                transactionManager.begin();
                try (final DelegatingConnection<?> connectionA = (DelegatingConnection<?>) ds.getConnection()) {
                    // close right away.
                }
                transactionManager.commit();
                assertTrue(transactionManagerRegistered);
                assertFalse(transactionSynchronizationRegistryRegistered);
            }
        }
    }
}