

package org.apache.commons.dbcp2;

import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 */
public class TestDelegatingConnection {

    /**
     * Delegate that doesn't support read-only or auto-commit.
     * It will merely take the input value of setReadOnly and
     * setAutoCommit and discard it, to keep false.
     */
    static class NoReadOnlyOrAutoCommitConnection extends TesterConnection {
        private final boolean readOnly = false;
        private final boolean autoCommit = false;

        public NoReadOnlyOrAutoCommitConnection() {
            super("","");
        }

        @Override
        public boolean getAutoCommit() {
            return autoCommit;
        }

        @Override
        public boolean isReadOnly() throws SQLException {
            return readOnly;
        }

        @Override
        public void setAutoCommit(final boolean autoCommit) {
            // Do nothing
        }

        @Override
        public void setReadOnly(final boolean readOnly) {
            // Do nothing
        }
    }

    /**
     * Delegate that will throw RTE on toString
     * Used to validate fix for DBCP-241
     */
    static class RTEGeneratingConnection extends TesterConnection {

        public RTEGeneratingConnection() {
            super("","");
        }

        @Override
        public String toString() {
            throw new RuntimeException("bang!");
        }

    }

    private DelegatingConnection<? extends Connection> delegatingConnection;
    private Connection connection;
    private Connection connection2;
    private TesterStatement testerStatement;
    private TesterResultSet testerResultSet;

    @BeforeEach
    public void setUp() throws Exception {
        connection = new TesterConnection("test", "test");
        connection2 = new TesterConnection("test", "test");
        delegatingConnection = new DelegatingConnection<>(connection);
        testerStatement = new TesterStatement(delegatingConnection);
        testerResultSet = new TesterResultSet(testerStatement);
    }

    @Test
    public void testAutoCommitCaching() throws SQLException {
        final Connection con = new NoReadOnlyOrAutoCommitConnection();
        final DelegatingConnection<Connection> delCon = new DelegatingConnection<>(con);

        delCon.setAutoCommit(true);

        assertFalse(con.getAutoCommit());
        assertFalse(delCon.getAutoCommit());
    }

    @Test
    public void testCheckOpen() throws Exception {
        delegatingConnection.checkOpen();
        delegatingConnection.close();
        try {
            delegatingConnection.checkOpen();
            fail("Expecting SQLException");
        } catch (final SQLException ex) {
            // expected
        }
    }

    /**
     * Verify fix for DBCP-241
     */
    @Test
    public void testCheckOpenNull() throws Exception {
        try {
            delegatingConnection.close();
            delegatingConnection.checkOpen();
            fail("Expecting SQLException");
        } catch (final SQLException ex) {
            assertTrue(ex.getMessage().endsWith("is closed."));
        }

        try {
            delegatingConnection = new DelegatingConnection<>(null);
            delegatingConnection.setClosedInternal(true);
            delegatingConnection.checkOpen();
            fail("Expecting SQLException");
        } catch (final SQLException ex) {
            assertTrue(ex.getMessage().endsWith("is null."));
        }

        try {
            final PoolingConnection pc = new PoolingConnection(connection2);
            pc.setStatementPool(new GenericKeyedObjectPool<>(pc));
            delegatingConnection = new DelegatingConnection<>(pc);
            pc.close();
            delegatingConnection.close();
            try (PreparedStatement ps = delegatingConnection.prepareStatement("")){}
            fail("Expecting SQLException");
        } catch (final SQLException ex) {
            assertTrue(ex.getMessage().endsWith("is closed."));
        }

        try {
            delegatingConnection = new DelegatingConnection<>(new RTEGeneratingConnection());
            delegatingConnection.close();
            delegatingConnection.checkOpen();
            fail("Expecting SQLException");
        } catch (final SQLException ex) {
            assertTrue(ex.getMessage().endsWith("is closed."));
        }
    }

    @Test
    public void testConnectionToString() throws Exception {
        final String s = delegatingConnection.toString();
        assertNotNull(s);
        assertFalse(s.isEmpty());
    }

    @Test
    public void testGetDelegate() throws Exception {
        assertEquals(connection,delegatingConnection.getDelegate());
    }

    @Test
    public void testIsClosed() throws Exception {
        delegatingConnection.checkOpen();
        assertFalse(delegatingConnection.isClosed());
        delegatingConnection.close();
        assertTrue(delegatingConnection.isClosed());
    }

    @Test
    public void testIsClosedNullDelegate() throws Exception {
        delegatingConnection.checkOpen();
        assertFalse(delegatingConnection.isClosed());
        delegatingConnection.setDelegate(null);
        assertTrue(delegatingConnection.isClosed());
    }

    @Test
    public void testPassivateWithResultSetCloseException() {
        try {
            testerResultSet.setSqlExceptionOnClose(true);
            delegatingConnection.addTrace(testerResultSet);
            delegatingConnection.passivate();
            Assertions.fail("Expected SQLExceptionList");
        } catch (final SQLException e) {
            Assertions.assertTrue(e instanceof SQLExceptionList);
            Assertions.assertEquals(1, ((SQLExceptionList) e).getCauseList().size());
        } finally {
            testerResultSet.setSqlExceptionOnClose(false);
        }
    }

    @Test
    public void testPassivateWithResultSetCloseExceptionAndStatementCloseException() {
        try {
            testerStatement.setSqlExceptionOnClose(true);
            testerResultSet.setSqlExceptionOnClose(true);
            delegatingConnection.addTrace(testerStatement);
            delegatingConnection.addTrace(testerResultSet);
            delegatingConnection.passivate();
            Assertions.fail("Expected SQLExceptionList");
        } catch (final SQLException e) {
            Assertions.assertTrue(e instanceof SQLExceptionList);
            Assertions.assertEquals(2, ((SQLExceptionList) e).getCauseList().size());
        } finally {
            testerStatement.setSqlExceptionOnClose(false);
        testerResultSet.setSqlExceptionOnClose(false);
        }
    }

    @Test
    public void testPassivateWithStatementCloseException() {
        try {
            testerStatement.setSqlExceptionOnClose(true);
            delegatingConnection.addTrace(testerStatement);
            delegatingConnection.passivate();
            Assertions.fail("Expected SQLExceptionList");
        } catch (final SQLException e) {
            Assertions.assertTrue(e instanceof SQLExceptionList);
            Assertions.assertEquals(1, ((SQLExceptionList) e).getCauseList().size());
        } finally {
            testerStatement.setSqlExceptionOnClose(false);
        }
    }

    @Test
    public void testReadOnlyCaching() throws SQLException {
        final Connection con = new NoReadOnlyOrAutoCommitConnection();
        final DelegatingConnection<Connection> delCon = new DelegatingConnection<>(con);

        delCon.setReadOnly(true);

        assertFalse(con.isReadOnly());
        assertFalse(delCon.isReadOnly());
    }

}
