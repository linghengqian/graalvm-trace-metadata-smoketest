

package org.apache.shardingsphere.driver.jdbc.core.resultset;

import org.apache.shardingsphere.driver.jdbc.base.AbstractShardingSphereDataSourceForEncryptTest;
import org.junit.Ignore;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

@Ignore
public final class EncryptResultSetTest extends AbstractShardingSphereDataSourceForEncryptTest {
    
    private static final String SELECT_SQL_TO_ASSERT = "SELECT id, cipher_pwd, plain_pwd FROM t_encrypt";
    
    @Test
    public void assertResultSetIsBeforeFirst() throws SQLException {
        try (
                Statement statement = getEncryptConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ResultSet resultSet = statement.executeQuery(SELECT_SQL_TO_ASSERT)) {
            assertTrue(resultSet.isBeforeFirst());
            resultSet.next();
            assertThat(resultSet.getRow(), is(1));
            resultSet.afterLast();
            assertTrue(resultSet.isAfterLast());
            resultSet.beforeFirst();
            assertTrue(resultSet.isBeforeFirst());
        }
    }
    
    @Test
    public void assertResultSetGetRow() throws SQLException {
        try (
                Statement statement = getEncryptConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ResultSet resultSet = statement.executeQuery(SELECT_SQL_TO_ASSERT)) {
            resultSet.next();
            assertThat(resultSet.getRow(), is(1));
        }
    }
    
    @Test
    public void assertResultSetAfterLast() throws SQLException {
        try (
                Statement statement = getEncryptConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ResultSet resultSet = statement.executeQuery(SELECT_SQL_TO_ASSERT)) {
            resultSet.afterLast();
            assertTrue(resultSet.isAfterLast());
        }
    }
    
    @Test
    public void assertResultSetBeforeFirst() throws SQLException {
        try (
                Statement statement = getEncryptConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ResultSet resultSet = statement.executeQuery(SELECT_SQL_TO_ASSERT)) {
            assertTrue(resultSet.isBeforeFirst());
        }
    }
    
    @Test
    public void assertResultSetPrevious() throws SQLException {
        try (
                Statement statement = getEncryptConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ResultSet resultSet = statement.executeQuery(SELECT_SQL_TO_ASSERT)) {
            resultSet.next();
            assertThat(resultSet.getRow(), is(1));
            resultSet.previous();
            assertThat(resultSet.getRow(), is(0));
        }
    }
    
    @Test
    public void assertRelative() throws SQLException {
        try (
                Statement statement = getEncryptConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ResultSet resultSet = statement.executeQuery(SELECT_SQL_TO_ASSERT)) {
            resultSet.next();
            resultSet.relative(1);
            assertThat(resultSet.getRow(), is(2));
        }
    }
    
    @Test
    public void assertAbsolute() throws SQLException {
        try (
                Statement statement = getEncryptConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ResultSet resultSet = statement.executeQuery(SELECT_SQL_TO_ASSERT)) {
            resultSet.absolute(2);
            assertThat(resultSet.getRow(), is(2));
        }
    }
}
