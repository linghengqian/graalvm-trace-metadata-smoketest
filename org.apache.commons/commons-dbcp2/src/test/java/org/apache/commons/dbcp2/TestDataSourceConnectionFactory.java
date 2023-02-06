

package org.apache.commons.dbcp2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Test for DataSourceConnectionFactory.
 */
public class TestDataSourceConnectionFactory {

    private static class TestDataSource implements DataSource {

        @Override
        public Connection getConnection() throws SQLException {
            return new TesterConnection(null, null);
        }

        @Override
        public Connection getConnection(final String username, final String password) throws SQLException {
            return new TesterConnection(username, password);
        }

        @Override
        public int getLoginTimeout() throws SQLException {
            return 0;
        }

        @Override
        public PrintWriter getLogWriter() throws SQLException {
            return null;
        }

        @Override
        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            return null;
        }

        @Override
        public boolean isWrapperFor(final Class<?> iface) throws SQLException {
            return false;
        }

        @Override
        public void setLoginTimeout(final int seconds) throws SQLException {
        }

        @Override
        public void setLogWriter(final PrintWriter out) throws SQLException {
        }

        @Override
        public <T> T unwrap(final Class<T> iface) throws SQLException {
            return null;
        }
    }
    private DataSource datasource;

    private DataSourceConnectionFactory factory;

    @BeforeEach
    public void setUp() {
        datasource = new TestDataSource();
        factory = new DataSourceConnectionFactory(datasource);
    }

    @Test
    public void testCredentials() throws SQLException {
        final DataSourceConnectionFactory factory = new DataSourceConnectionFactory(datasource, "foo", "bar");
        final Connection conn = factory.createConnection();
        assertEquals("foo", ((TesterConnection) conn).getUserName());
    }

    @Test
    public void testDefaultValues() throws SQLException {
        final Connection conn = factory.createConnection();
        assertNull(((TesterConnection) conn).getUserName());
    }

    @Test
    public void testEmptyPassword() throws SQLException {
        final DataSourceConnectionFactory factory = new DataSourceConnectionFactory(datasource, "foo", (char[]) null);
        final Connection conn = factory.createConnection();
        assertEquals("foo", ((TesterConnection) conn).getUserName());
    }

    @Test
    public void testEmptyUser() throws SQLException {
        final DataSourceConnectionFactory factory = new DataSourceConnectionFactory(datasource, null, new char[] {'a'});
        final Connection conn = factory.createConnection();
        assertNull(((TesterConnection) conn).getUserName());
    }
}
