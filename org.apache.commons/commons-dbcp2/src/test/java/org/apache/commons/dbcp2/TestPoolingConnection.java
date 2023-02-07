
package org.apache.commons.dbcp2;

import com.lingh.TesterPreparedStatement;
import org.apache.commons.pool2.KeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestPoolingConnection {

    private PoolingConnection con;

    @BeforeEach
    public void setUp() throws Exception {
        con = new PoolingConnection(new TesterConnection("test", "test"));
        final GenericKeyedObjectPoolConfig<DelegatingPreparedStatement> config = new GenericKeyedObjectPoolConfig<>();
        config.setMaxTotalPerKey(-1);
        config.setBlockWhenExhausted(false);
        config.setMaxWaitMillis(0);
        config.setMaxIdlePerKey(1);
        config.setMaxTotal(1);
        final KeyedObjectPool<PStmtKey, DelegatingPreparedStatement> stmtPool =
                new GenericKeyedObjectPool<>(con, config);
        con.setStatementPool(stmtPool);
    }

    @AfterEach
    public void tearDown() throws Exception {
        con.close();
        con = null;
    }

    @Test
    public void testPrepareCall() throws Exception {
        final String sql = "select 'a' from dual";
        final DelegatingCallableStatement statement = (DelegatingCallableStatement)con.prepareCall(sql);
        final TesterCallableStatement testStatement = (TesterCallableStatement) statement.getInnermostDelegate();
        // assert
        assertEquals(sql, testStatement.getSql());
    }

    @Test
    public void testPrepareCallWithResultSetConcurrency() throws Exception {
        final String sql = "select 'a' from dual";
        final int resultSetType = 0;
        final int resultSetConcurrency = 0;
        final DelegatingCallableStatement statement = (DelegatingCallableStatement)con.prepareCall(sql, resultSetType, resultSetConcurrency);
        final TesterCallableStatement testStatement = (TesterCallableStatement) statement.getInnermostDelegate();
        // assert
        assertEquals(sql, testStatement.getSql());
        assertEquals(resultSetType, testStatement.getResultSetType());
        assertEquals(resultSetConcurrency, testStatement.getResultSetConcurrency());
    }

    @Test
    public void testPrepareCallWithResultSetHoldability() throws Exception {
        final String sql = "select 'a' from dual";
        final int resultSetType = 0;
        final int resultSetConcurrency = 0;
        final int resultSetHoldability = 0;
        final DelegatingCallableStatement statement = (DelegatingCallableStatement)con.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        final TesterCallableStatement testStatement = (TesterCallableStatement) statement.getInnermostDelegate();
        // assert
        assertEquals(sql, testStatement.getSql());
        assertEquals(resultSetType, testStatement.getResultSetType());
        assertEquals(resultSetConcurrency, testStatement.getResultSetConcurrency());
        assertEquals(resultSetHoldability, testStatement.getResultSetHoldability());
    }

    @Test
    public void testPrepareStatement() throws Exception {
        final String sql = "select 'a' from dual";
        final DelegatingPreparedStatement statement = (DelegatingPreparedStatement)con.prepareStatement(sql);
        final TesterPreparedStatement testStatement = (TesterPreparedStatement) statement.getInnermostDelegate();
        // assert
        assertEquals(sql, testStatement.getSql());
    }

    @Test
    public void testPrepareStatementWithAutoGeneratedKeys() throws Exception {
        final String sql = "select 'a' from dual";
        final int autoGeneratedKeys = 0;
        final DelegatingPreparedStatement statement = (DelegatingPreparedStatement)con.prepareStatement(sql, autoGeneratedKeys);
        final TesterPreparedStatement testStatement = (TesterPreparedStatement) statement.getInnermostDelegate();
        // assert
        assertEquals(sql, testStatement.getSql());
        assertEquals(autoGeneratedKeys, testStatement.getAutoGeneratedKeys());
    }

    @Test
    public void testPrepareStatementWithColumnIndexes() throws Exception {
        final String sql = "select 'a' from dual";
        final int[] columnIndexes = {1};
        final DelegatingPreparedStatement statement = (DelegatingPreparedStatement)con.prepareStatement(sql, columnIndexes);
        final TesterPreparedStatement testStatement = (TesterPreparedStatement) statement.getInnermostDelegate();
        // assert
        assertEquals(sql, testStatement.getSql());
        assertArrayEquals(columnIndexes, testStatement.getColumnIndexes());
    }

    @Test
    public void testPrepareStatementWithColumnNames() throws Exception {
        final String sql = "select 'a' from dual";
        final String[] columnNames = {"columnName1"};
        final DelegatingPreparedStatement statement = (DelegatingPreparedStatement)con.prepareStatement(sql, columnNames);
        final TesterPreparedStatement testStatement = (TesterPreparedStatement) statement.getInnermostDelegate();
        // assert
        assertEquals(sql, testStatement.getSql());
        assertArrayEquals(columnNames, testStatement.getColumnNames());
    }

    @Test
    public void testPrepareStatementWithResultSetConcurrency() throws Exception {
        final String sql = "select 'a' from dual";
        final int resultSetType = 0;
        final int resultSetConcurrency = 0;
        final DelegatingPreparedStatement statement = (DelegatingPreparedStatement)con.prepareStatement(sql, resultSetType, resultSetConcurrency);
        final TesterPreparedStatement testStatement = (TesterPreparedStatement) statement.getInnermostDelegate();
        // assert
        assertEquals(sql, testStatement.getSql());
        assertEquals(resultSetType, testStatement.getResultSetType());
        assertEquals(resultSetConcurrency, testStatement.getResultSetConcurrency());
    }

    @Test
    public void testPrepareStatementWithResultSetHoldability() throws Exception {
        final String sql = "select 'a' from dual";
        final int resultSetType = 0;
        final int resultSetConcurrency = 0;
        final int resultSetHoldability = 0;
        final DelegatingPreparedStatement statement = (DelegatingPreparedStatement)con.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        final TesterPreparedStatement testStatement = (TesterPreparedStatement) statement.getInnermostDelegate();
        // assert
        assertEquals(sql, testStatement.getSql());
        assertEquals(resultSetType, testStatement.getResultSetType());
        assertEquals(resultSetConcurrency, testStatement.getResultSetConcurrency());
        assertEquals(resultSetHoldability, testStatement.getResultSetHoldability());
    }
}