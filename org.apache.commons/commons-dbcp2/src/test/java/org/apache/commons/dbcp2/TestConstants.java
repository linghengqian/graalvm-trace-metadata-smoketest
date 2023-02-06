

package org.apache.commons.dbcp2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for Constants.
 */
public class TestConstants {

    @Test
    public void testConstants() {
        assertNotNull(new Constants());
        assertEquals(",connectionpool=", Constants.JMX_CONNECTION_POOL_BASE_EXT);
        assertEquals("connections", Constants.JMX_CONNECTION_POOL_PREFIX);
        assertEquals(",connectionpool=connections,connection=", Constants.JMX_CONNECTION_BASE_EXT);
        assertEquals(",connectionpool=connections,connection=", Constants.JMX_STATEMENT_POOL_BASE_EXT);
        assertEquals(",statementpool=statements", Constants.JMX_STATEMENT_POOL_PREFIX);
    }
}
