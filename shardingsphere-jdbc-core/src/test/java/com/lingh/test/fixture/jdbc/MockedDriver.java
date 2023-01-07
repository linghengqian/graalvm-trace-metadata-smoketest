package com.lingh.test.fixture.jdbc;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.util.Properties;
import java.util.logging.Logger;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

public final class MockedDriver implements Driver {
    
    @Override
    public Connection connect(final String url, final Properties info) {
        return mock(Connection.class, RETURNS_DEEP_STUBS);
    }
    
    @Override
    public boolean acceptsURL(final String url) {
        return url.startsWith("mock:jdbc");
    }
    
    @Override
    public DriverPropertyInfo[] getPropertyInfo(final String url, final Properties info) {
        return new DriverPropertyInfo[0];
    }
    
    @Override
    public int getMajorVersion() {
        return 0;
    }
    
    @Override
    public int getMinorVersion() {
        return 0;
    }
    
    @Override
    public boolean jdbcCompliant() {
        return true;
    }
    
    @Override
    public Logger getParentLogger() {
        return mock(Logger.class);
    }
}
