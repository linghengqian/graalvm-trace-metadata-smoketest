package com.lingh.jdbc.core.statement;

import com.lingh.jdbc.base.AbstractShardingSphereDataSourceForShardingTest;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.junit.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ShardingSphereStatementTest extends AbstractShardingSphereDataSourceForShardingTest {
    
    @Test
    public void assertGetGeneratedKeys() throws SQLException {
        String sql = "INSERT INTO t_order_item(order_id, user_id, status) VALUES (%d, %d, '%s')";
        try (
                Connection connection = getShardingSphereDataSource().getConnection();
                Statement statement = connection.createStatement()) {
            assertFalse(statement.execute(String.format(sql, 1, 1, "init")));
            assertFalse(statement.execute(String.format(sql, 1, 1, "init"), Statement.NO_GENERATED_KEYS));
            assertFalse(statement.execute(String.format(sql, 1, 1, "init"), Statement.RETURN_GENERATED_KEYS));
            ResultSet generatedKeysResultSet = statement.getGeneratedKeys();
            assertTrue(generatedKeysResultSet.next());
            assertThat(generatedKeysResultSet.getLong(1), is(3L));
            assertFalse(statement.execute(String.format(sql, 1, 1, "init"), new int[]{1}));
            generatedKeysResultSet = statement.getGeneratedKeys();
            assertTrue(generatedKeysResultSet.next());
            assertThat(generatedKeysResultSet.getLong(1), is(4L));
            assertFalse(statement.execute(String.format(sql, 1, 1, "init"), new String[]{"user_id"}));
            generatedKeysResultSet = statement.getGeneratedKeys();
            assertTrue(generatedKeysResultSet.next());
            assertThat(generatedKeysResultSet.getLong(1), is(5L));
            assertFalse(statement.execute(String.format(sql, 1, 1, "init"), new int[]{2}));
            generatedKeysResultSet = statement.getGeneratedKeys();
            assertTrue(generatedKeysResultSet.next());
            assertThat(generatedKeysResultSet.getLong(1), is(6L));
            assertFalse(statement.execute(String.format(sql, 1, 1, "init"), new String[]{"status"}));
            generatedKeysResultSet = statement.getGeneratedKeys();
            assertTrue(generatedKeysResultSet.next());
            assertThat(generatedKeysResultSet.getLong(1), is(7L));
        }
    }
    
    @Test
    public void assertAddGetGeneratedKeysForNoGeneratedValues() throws SQLException {
        String sql = "INSERT INTO t_product (product_name) VALUES ('%s')";
        try (Statement statement = getShardingSphereDataSource().getConnection().createStatement()) {
            statement.execute(String.format(sql, "cup"), Statement.RETURN_GENERATED_KEYS);
            ResultSet generatedKeysResultSet = statement.getGeneratedKeys();
            assertTrue(generatedKeysResultSet.next());
            assertThat(generatedKeysResultSet.getInt(1), is(1));
        }
    }
    
    @Test(expected = SQLException.class)
    public void assertQueryWithNull() throws SQLException {
        try (Statement statement = getShardingSphereDataSource().getConnection().createStatement()) {
            statement.executeQuery(null);
        }
    }
    
    @Test(expected = SQLException.class)
    public void assertQueryWithEmptyString() throws SQLException {
        try (Statement statement = getShardingSphereDataSource().getConnection().createStatement()) {
            statement.executeQuery("");
        }
    }
    
    @Test
    public void assertExecuteGetResultSet() throws SQLException {
        String sql = "UPDATE t_order_item SET status = '%s' WHERE user_id = %d AND order_id = %d";
        try (Statement statement = getShardingSphereDataSource().getConnection().createStatement()) {
            assertFalse(statement.execute(String.format(sql, "OK", 1, 1)));
            assertNull(statement.getResultSet());
        }
    }
    
    @Test
    public void assertExecuteUpdateGetResultSet() throws SQLException {
        String sql = "UPDATE t_order_item SET status = '%s' WHERE user_id = %d AND order_id = %d";
        try (Statement statement = getShardingSphereDataSource().getConnection().createStatement()) {
            statement.executeUpdate(String.format(sql, "OK", 1, 1));
            assertNull(statement.getResultSet());
        }
    }
    
    @Test(expected = SQLException.class)
    public void assertColumnNotFoundException() throws SQLException {
        String sql = "UPDATE t_order_item SET error_column = '%s'";
        try (Statement statement = getShardingSphereDataSource().getConnection().createStatement()) {
            statement.executeUpdate(String.format(sql, "OK"));
        }
    }
    
    @Test
    public void assertShowDatabases() throws SQLException {
        String sql = "SHOW DATABASES";
        try (Statement statement = getShardingSphereDataSource().getConnection().createStatement()) {
            ResultSet resultSet = statement.executeQuery(sql);
            assertTrue(resultSet.next());
            assertThat(resultSet.getString(1), is(DefaultDatabase.LOGIC_NAME));
        }
    }
}
