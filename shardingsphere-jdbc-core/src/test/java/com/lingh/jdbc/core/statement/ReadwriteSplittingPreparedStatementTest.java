

package com.lingh.jdbc.core.statement;

import com.lingh.jdbc.base.AbstractShardingSphereDataSourceForReadwriteSplittingTest;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ReadwriteSplittingPreparedStatementTest extends AbstractShardingSphereDataSourceForReadwriteSplittingTest {
    
    @Test(expected = SQLException.class)
    public void assertQueryWithNull() throws SQLException {
        try (PreparedStatement preparedStatement = getReadwriteSplittingDataSource().getConnection().prepareStatement(null)) {
            preparedStatement.executeQuery();
        }
    }
    
    @Test(expected = SQLException.class)
    public void assertQueryWithEmptyString() throws SQLException {
        try (PreparedStatement preparedStatement = getReadwriteSplittingDataSource().getConnection().prepareStatement("")) {
            preparedStatement.executeQuery();
        }
    }
    
    @Test
    public void assertGetParameterMetaData() throws SQLException {
        try (PreparedStatement preparedStatement = getReadwriteSplittingDataSource().getConnection().prepareStatement("SELECT * FROM t_config where id = ?")) {
            assertThat(preparedStatement.getParameterMetaData().getParameterCount(), is(1));
        }
    }
    
    @Test
    public void assertGetGeneratedKeys() throws SQLException {
        try (
                PreparedStatement preparedStatement = getReadwriteSplittingDataSource()
                        .getConnection().prepareStatement("INSERT INTO t_config(status) VALUES(?);", Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, "OK");
            preparedStatement.executeUpdate();
            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            assertTrue(generatedKeys.next());
            int columnCount = generatedKeys.getMetaData().getColumnCount();
            for (int index = 0; index < columnCount; index++) {
                assertNotNull(generatedKeys.getObject(index + 1));
                assertNotNull(generatedKeys.getMetaData().getColumnLabel(index + 1));
                assertNotNull(generatedKeys.getMetaData().getColumnName(index + 1));
            }
            assertFalse(generatedKeys.next());
        }
    }
    
    @Test
    public void assertGetGeneratedKeysWithPrimaryKeyIsNull() throws SQLException {
        try (
                PreparedStatement preparedStatement = getReadwriteSplittingDataSource()
                        .getConnection().prepareStatement("INSERT INTO t_config(id, status) VALUES(?, ?);", Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setObject(1, null);
            preparedStatement.setString(2, "OK");
            preparedStatement.executeUpdate();
            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            assertTrue(generatedKeys.next());
            int columnCount = generatedKeys.getMetaData().getColumnCount();
            for (int index = 0; index < columnCount; index++) {
                assertNotNull(generatedKeys.getObject(index + 1));
                assertNotNull(generatedKeys.getMetaData().getColumnLabel(index + 1));
                assertNotNull(generatedKeys.getMetaData().getColumnName(index + 1));
            }
            assertFalse(generatedKeys.next());
        }
    }
    
    @Test
    public void assertGetGeneratedKeysWithPrimaryKeyIsNullInTransactional() throws SQLException {
        try (
                Connection connection = getReadwriteSplittingDataSource()
                        .getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO t_config(id, status) VALUES(?, ?);", Statement.RETURN_GENERATED_KEYS)) {
            connection.setAutoCommit(false);
            Object lastGeneratedId = null;
            Object generatedId;
            for (int i = 1; i <= 3; i++) {
                preparedStatement.setObject(1, null);
                preparedStatement.setString(2, "OK");
                preparedStatement.executeUpdate();
                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                assertTrue(generatedKeys.next());
                generatedId = generatedKeys.getObject(1);
                assertThat(generatedId, not(lastGeneratedId));
                lastGeneratedId = generatedId;
                assertFalse(generatedKeys.next());
            }
            connection.commit();
        }
    }
}
