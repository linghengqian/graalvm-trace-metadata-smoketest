

package org.apache.commons.dbcp2.datasources;

import org.apache.commons.dbcp2.Jdbc41Bridge;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * ConnectionPoolDataSource implementation that proxies another ConnectionPoolDataSource.
 */
public class ConnectionPoolDataSourceProxy implements ConnectionPoolDataSource {

    protected ConnectionPoolDataSource delegate;

    public ConnectionPoolDataSourceProxy(final ConnectionPoolDataSource cpds) {
        this.delegate = cpds;
    }

    public ConnectionPoolDataSource getDelegate() {
        return delegate;
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return delegate.getLoginTimeout();
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return delegate.getLogWriter();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return Jdbc41Bridge.getParentLogger(delegate);
    }

    /**
     * Gets a TesterPooledConnection with notifyOnClose turned on
     */
    @Override
    public PooledConnection getPooledConnection() throws SQLException {
        return wrapPooledConnection(delegate.getPooledConnection());
    }

    /**
     * Gets a TesterPooledConnection with notifyOnClose turned on
     */
    @Override
    public PooledConnection getPooledConnection(final String user, final String password)
            throws SQLException {
        return wrapPooledConnection(delegate.getPooledConnection(user, password));
    }

    @Override
    public void setLoginTimeout(final int seconds) throws SQLException {
        delegate.setLoginTimeout(seconds);
    }

    @Override
    public void setLogWriter(final PrintWriter out) throws SQLException {
        delegate.setLogWriter(out);
    }

    /**
     * Creates a TesterPooledConnection with notifyOnClose turned on
     */
    protected PooledConnection wrapPooledConnection(final PooledConnection pc) {
        final PooledConnectionProxy tpc = new PooledConnectionProxy(pc);
        tpc.setNotifyOnClose(true);
        return tpc;
    }
}
