

package com.lingh.jdbc.core.statement;

import com.lingh.jdbc.base.AbstractShardingSphereDataSourceForReadwriteSplittingTest;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public final class ReadwriteSplittingStatementTest extends AbstractShardingSphereDataSourceForReadwriteSplittingTest {
    
    @Test(expected = SQLException.class)
    public void assertQueryWithNull() throws SQLException {
        try (Statement statement = getReadwriteSplittingDataSource().getConnection().createStatement()) {
            statement.executeQuery(null);
        }
    }
    
    @Test(expected = SQLException.class)
    public void assertQueryWithEmptyString() throws SQLException {
        try (Statement statement = getReadwriteSplittingDataSource().getConnection().createStatement()) {
            statement.executeQuery("");
        }
    }
    
    @Test
    public void assertGetGeneratedKeys() throws SQLException {
        try (Statement statement = getReadwriteSplittingDataSource().getConnection().createStatement()) {
            statement.executeUpdate("INSERT INTO t_config(status) VALUES('OK');", Statement.RETURN_GENERATED_KEYS);
            ResultSet generatedKeys = statement.getGeneratedKeys();
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
}
