

package com.lingh.datasources;

import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.PooledConnection;
import javax.sql.StatementEventListener;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/**
 * PooledConnection implementation that wraps a driver-supplied
 * PooledConnection and proxies events, allowing behavior to be
 * modified to simulate behavior of different implementations.
 */
public class PooledConnectionProxy implements PooledConnection,
    ConnectionEventListener {

    protected PooledConnection delegate;

    /**
     * ConnectionEventListeners
     */
    private final List<EventListener> eventListeners = Collections.synchronizedList(new ArrayList<>());

    /**
     * True means we will (dubiously) notify listeners with a
     * ConnectionClosed event when this (i.e. the PooledConnection itself)
     * is closed
     */
    private boolean notifyOnClose;

    public PooledConnectionProxy(final PooledConnection pooledConnection) {
        this.delegate = pooledConnection;
        pooledConnection.addConnectionEventListener(this);
    }

    /**
     * Add event listeners.
     */
    @Override
    public void addConnectionEventListener(final ConnectionEventListener listener) {
        if (!eventListeners.contains(listener)) {
            eventListeners.add(listener);
        }
    }

    @Override
    public void addStatementEventListener(final StatementEventListener listener) {
        if (!eventListeners.contains(listener)) {
            eventListeners.add(listener);
        }
    }
    /* JDBC_4_ANT_KEY_END */

    /**
     * If notifyOnClose is on, notify listeners
     */
    @Override
    public void close() throws SQLException {
        delegate.close();
        if (isNotifyOnClose()) {
           notifyListeners();
        }
    }

    /**
     * Pass closed events on to listeners
     */
    @Override
    public void connectionClosed(final ConnectionEvent event) {
        notifyListeners();
    }

    /**
     * Pass error events on to listeners
     */
    @Override
    public void connectionErrorOccurred(final ConnectionEvent event) {
        final Object[] listeners = eventListeners.toArray();
        for (final Object listener : listeners) {
            ((ConnectionEventListener) listener).connectionErrorOccurred(event);
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        return delegate.getConnection();
    }

    /**
     * Expose listeners
     */
    public Collection<EventListener> getListeners() {
        return eventListeners;
    }

    public boolean isNotifyOnClose() {
        return notifyOnClose;
    }

    /**
     * sends a connectionClosed event to listeners.
     */
    void notifyListeners() {
        final ConnectionEvent event = new ConnectionEvent(this);
        final Object[] listeners = eventListeners.toArray();
        for (final Object listener : listeners) {
            ((ConnectionEventListener) listener).connectionClosed(event);
        }
    }

    /**
     * Remove event listeners.
     */
    @Override
    public void removeConnectionEventListener(final ConnectionEventListener listener) {
        eventListeners.remove(listener);
    }

    @Override
    public void removeStatementEventListener(final StatementEventListener listener) {
        eventListeners.remove(listener);
    }
    /* JDBC_4_ANT_KEY_END */

    public void setNotifyOnClose(final boolean notifyOnClose) {
        this.notifyOnClose = notifyOnClose;
    }

    /**
     * Generate a connection error event
     */
    public void throwConnectionError() {
        final ConnectionEvent event = new ConnectionEvent(this);
        connectionErrorOccurred(event);
    }

}
