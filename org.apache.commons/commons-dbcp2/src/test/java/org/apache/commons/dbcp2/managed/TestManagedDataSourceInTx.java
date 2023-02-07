
package org.apache.commons.dbcp2.managed;

import com.lingh.managed.TestManagedDataSource;
import org.apache.commons.dbcp2.DelegatingConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.transaction.Synchronization;
import javax.transaction.Transaction;
import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests ManagedDataSource with an active transaction in progress.
 */
public class TestManagedDataSourceInTx extends TestManagedDataSource {

    // can't actually test close in a transaction
    @Override
    protected void assertBackPointers(final Connection conn, final Statement statement) throws SQLException {
        assertFalse(conn.isClosed());
        assertFalse(isClosed(statement));

        assertSame(conn, statement.getConnection(),
                "statement.getConnection() should return the exact same connection instance that was used to create the statement");

        final ResultSet resultSet = statement.getResultSet();
        assertFalse(isClosed(resultSet));
        assertSame(statement, resultSet.getStatement(),
                "resultSet.getStatement() should return the exact same statement instance that was used to create the result set");

        final ResultSet executeResultSet = statement.executeQuery("select * from dual");
        assertFalse(isClosed(executeResultSet));
        assertSame(statement, executeResultSet.getStatement(),
                "resultSet.getStatement() should return the exact same statement instance that was used to create the result set");

        final ResultSet keysResultSet = statement.getGeneratedKeys();
        assertFalse(isClosed(keysResultSet));
        assertSame(statement, keysResultSet.getStatement(),
                "resultSet.getStatement() should return the exact same statement instance that was used to create the result set");

        ResultSet preparedResultSet = null;
        if (statement instanceof PreparedStatement) {
            final PreparedStatement preparedStatement = (PreparedStatement) statement;
            preparedResultSet = preparedStatement.executeQuery();
            assertFalse(isClosed(preparedResultSet));
            assertSame(statement, preparedResultSet.getStatement(),
                    "resultSet.getStatement() should return the exact same statement instance that was used to create the result set");
        }


        resultSet.getStatement().getConnection().close();
    }

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        transactionManager.begin();
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception {
        if (transactionManager.getTransaction() != null) {
            transactionManager.commit();
        }
        super.tearDown();
    }

    @Override
    @Test
    public void testAutoCommitBehavior() throws Exception {
        final Connection connection = newConnection();

        // auto commit should be off
        assertFalse(connection.getAutoCommit(), "Auto-commit should be disabled");

        // attempt to set auto commit
        try {
            connection.setAutoCommit(true);
            fail("setAutoCommit method should be disabled while enlisted in a transaction");
        } catch (final SQLException e) {
            // expected
        }

        // make sure it is still disabled
        assertFalse(connection.getAutoCommit(), "Auto-commit should be disabled");

        // close connection
        connection.close();
    }

    @Override
    @Test
    public void testClearWarnings() throws Exception {
        // open a connection
        Connection connection = newConnection();
        assertNotNull(connection);

        // generate SQLWarning on connection
        final CallableStatement statement = connection.prepareCall("warning");
        assertNotNull(connection.getWarnings());

        // create a new shared connection
        final Connection sharedConnection = newConnection();

        // shared connection should see warning
        assertNotNull(sharedConnection.getWarnings());

        // close and allocate a new (original) connection
        connection.close();
        connection = newConnection();

        // warnings should not have been cleared by closing the connection
        assertNotNull(connection.getWarnings());
        assertNotNull(sharedConnection.getWarnings());

        statement.close();
        connection.close();
        sharedConnection.close();
    }

    @Test
    public void testCloseInTransaction() throws Exception {
        final DelegatingConnection<?> connectionA = (DelegatingConnection<?>) newConnection();
        final DelegatingConnection<?> connectionB = (DelegatingConnection<?>) newConnection();

        assertNotEquals(connectionA, connectionB);
        assertNotEquals(connectionB, connectionA);
        assertTrue(connectionA.innermostDelegateEquals(connectionB.getInnermostDelegate()));
        assertTrue(connectionB.innermostDelegateEquals(connectionA.getInnermostDelegate()));

        connectionA.close();
        connectionB.close();

        final Connection connection = newConnection();

        assertFalse(connection.isClosed(), "Connection should be open");

        connection.close();

        assertTrue(connection.isClosed(), "Connection should be closed");
    }

    @Test
    public void testCommit() throws Exception {
        final Connection connection = newConnection();

        // connection should be open
        assertFalse(connection.isClosed(), "Connection should be open");

        // attempt commit directly
        try {
            connection.commit();
            fail("commit method should be disabled while enlisted in a transaction");
        } catch (final SQLException e) {
            // expected
        }

        // make sure it is still open
        assertFalse(connection.isClosed(), "Connection should be open");

        // close connection
        connection.close();
    }

    @Override
    @Test
    public void testConnectionReturnOnCommit() throws Exception {
       // override with no-op test
    }

    @Override
    @Test
    public void testConnectionsAreDistinct() throws Exception {
        final Connection[] conn = new Connection[getMaxTotal()];
        for(int i=0;i<conn.length;i++) {
            conn[i] = newConnection();
            for(int j=0;j<i;j++) {
                // two connections should be distinct instances
                Assertions.assertNotSame(conn[j], conn[i]);
                // neither should they should be equivalent even though they are
                // sharing the same underlying connection
                Assertions.assertNotEquals(conn[j], conn[i]);
                // Check underlying connection is the same
                Assertions.assertEquals(((DelegatingConnection<?>) conn[j]).getInnermostDelegateInternal(),
                        ((DelegatingConnection<?>) conn[i]).getInnermostDelegateInternal());
            }
        }
        for (final Connection element : conn) {
            element.close();
        }
    }

    @Test
    public void testDoubleReturn() throws Exception {
        transactionManager.getTransaction().registerSynchronization(new Synchronization() {
            private ManagedConnection<?> conn;

            @Override
            public void afterCompletion(final int i) {
                final int numActive = pool.getNumActive();
                try {
                    conn.checkOpen();
                } catch (final Exception e) {
                    // Ignore
                }
                assertEquals(numActive, pool.getNumActive());
                try {
                    conn.close();
                } catch (final Exception e) {
                    fail("Should have been able to close the connection");
                }
                // TODO Requires DBCP-515 assertTrue(numActive -1 == pool.getNumActive());
            }

            @Override
            public void beforeCompletion() {
                try {
                    conn = (ManagedConnection<?>) ds.getConnection();
                    assertNotNull(conn);
                } catch (final SQLException e) {
                    fail("Could not get connection");
                }
            }
        });
        transactionManager.commit();
    }

    @Test
    public void testGetConnectionInAfterCompletion() throws Exception {

        final DelegatingConnection<?> connection = (DelegatingConnection<?>) newConnection();
        // Don't close so we can check it for warnings in afterCompletion
        transactionManager.getTransaction().registerSynchronization(new Synchronization() {
            @Override
            public void afterCompletion(final int i) {
                try {
                    final Connection connection1 = ds.getConnection();
                    try {
                        connection1.getWarnings();
                        fail("Could operate on closed connection");
                    } catch (final SQLException e) {
                        // This is expected
                    }
                } catch (final SQLException e) {
                    fail("Should have been able to get connection");
                }
            }

            @Override
            public void beforeCompletion() {
            }
        });
        connection.close();
        transactionManager.commit();
    }

    @Override
    @Test
    public void testHashCode() throws Exception {
        final Connection conn1 = newConnection();
        assertNotNull(conn1);
        final Connection conn2 = newConnection();
        assertNotNull(conn2);
        Assertions.assertNotEquals(conn1.hashCode(), conn2.hashCode());
    }
    @Override
    @Test
    public void testManagedConnectionEqualsFail() {
    }

    @Override
    @Test
    public void testMaxTotal() throws Exception {
        final Transaction[] transactions = new Transaction[getMaxTotal()];
        final Connection[] c = new Connection[getMaxTotal()];
        for (int i = 0; i < c.length; i++) {
            c[i] = newConnection();
            assertNotNull(c[i]);
            transactions[i] = transactionManager.suspend();
            assertNotNull(transactions[i]);
            transactionManager.begin();
        }

        try {
            newConnection();
            fail("Allowed to open more than DefaultMaxTotal connections.");
        } catch (final SQLException e) {
        } finally {
            transactionManager.commit();
            for (int i = 0; i < c.length; i++) {
                transactionManager.resume(transactions[i]);
                c[i].close();
                transactionManager.commit();
            }
        }
    }

    @Override
    @Test
    public void testNestedConnections() {
    }

    @Test
    public void testReadOnly() throws Exception {
        final Connection connection = newConnection();
        assertTrue(connection.isReadOnly(), "Connection be read-only");
        try {
            connection.setReadOnly(true);
            fail("setReadOnly method should be disabled while enlisted in a transaction");
        } catch (final SQLException e) {
        }
        assertTrue(connection.isReadOnly(), "Connection be read-only");
        try {
            connection.setReadOnly(false);
            fail("setReadOnly method should be disabled while enlisted in a transaction");
        } catch (final SQLException e) {
        }
        assertTrue(connection.isReadOnly(), "Connection be read-only");
        connection.close();
    }

    @Override
    @Test
    public void testSharedConnection() throws Exception {
        final DelegatingConnection<?> connectionA = (DelegatingConnection<?>) newConnection();
        final DelegatingConnection<?> connectionB = (DelegatingConnection<?>) newConnection();
        assertNotEquals(connectionA, connectionB);
        assertNotEquals(connectionB, connectionA);
        assertTrue(connectionA.innermostDelegateEquals(connectionB.getInnermostDelegate()));
        assertTrue(connectionB.innermostDelegateEquals(connectionA.getInnermostDelegate()));
        connectionA.close();
        connectionB.close();
    }

    @Test
    public void testSharedTransactionConversion() throws Exception {
        final DelegatingConnection<?> connectionA = (DelegatingConnection<?>) newConnection();
        final DelegatingConnection<?> connectionB = (DelegatingConnection<?>) newConnection();
        assertNotEquals(connectionA, connectionB);
        assertNotEquals(connectionB, connectionA);
        assertTrue(connectionA.innermostDelegateEquals(connectionB.getInnermostDelegate()));
        assertTrue(connectionB.innermostDelegateEquals(connectionA.getInnermostDelegate()));
        transactionManager.commit();
        connectionA.getAutoCommit();
        connectionB.getAutoCommit();
        assertNotEquals(connectionA, connectionB);
        assertNotEquals(connectionB, connectionA);
        assertFalse(connectionA.innermostDelegateEquals(connectionB.getInnermostDelegate()));
        assertFalse(connectionB.innermostDelegateEquals(connectionA.getInnermostDelegate()));
        transactionManager.begin();
        connectionA.getAutoCommit();
        connectionB.getAutoCommit();

        // back in a transaction so inner connections should be equal again
        assertNotEquals(connectionA, connectionB);
        assertNotEquals(connectionB, connectionA);
        assertTrue(connectionA.innermostDelegateEquals(connectionB.getInnermostDelegate()));
        assertTrue(connectionB.innermostDelegateEquals(connectionA.getInnermostDelegate()));

        connectionA.close();
        connectionB.close();
    }
}
