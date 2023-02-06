

package org.apache.commons.dbcp2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests for LifetimeExceededException.
 */
public class TestLifetimeExceededException {

    @Test
    public void testLifetimeExceededException() {
        final LifetimeExceededException exception = new LifetimeExceededException("car");
        assertEquals("car", exception.getMessage());
    }

    @Test
    public void testLifetimeExceededExceptionNoMessage() {
        final LifetimeExceededException exception = new LifetimeExceededException();
        assertNull(exception.getMessage());
    }
}
