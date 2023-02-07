

package com.lingh;

import org.apache.commons.dbcp2.DriverConnectionFactory;
import org.apache.commons.dbcp2.TesterDriver;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for DriverConnectionFactory.
 */
public class TestDriverConnectionFactory {

    @Test
    public void testCreateConnection() throws SQLException {
        final DriverConnectionFactory cf = new DriverConnectionFactory(
                new TesterDriver(), "jdbc:apache:commons:testdriver", null);
        final Connection conn = cf.createConnection();
        assertEquals(0, conn.getMetaData().getDriverMajorVersion());
    }

    @Test
    public void testDriverConnectionFactoryToString() {
        final DriverConnectionFactory cf = new DriverConnectionFactory(
                new TesterDriver(), "jdbc:apache:commons:testdriver", null);
        final String toString = cf.toString();
        assertTrue(toString.contains("jdbc:apache:commons:testdriver"));
    }
}
