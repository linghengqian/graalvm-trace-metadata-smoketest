
package org.apache.commons.dbcp2.datasources;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests {@link CharArray}.
 */
public class CharArrayTest {

    @Test
    public void testAsString() {
        assertEquals("foo", new CharArray("foo").asString());
    }

    @Test
    public void testEquals() {
        assertEquals(new CharArray("foo"), new CharArray("foo"));
        assertNotEquals(new CharArray("foo"), new CharArray("bar"));
    }

    @Test
    public void testGet() {
        assertArrayEquals("foo".toCharArray(), new CharArray("foo").get());
    }

    @Test
    public void testHashCode() {
        assertEquals(new CharArray("foo").hashCode(), new CharArray("foo").hashCode());
        assertNotEquals(new CharArray("foo").hashCode(), new CharArray("bar").hashCode());
    }

    @Test
    public void testToString() {
        assertFalse(new CharArray("foo").toString().contains("foo"));
    }
}
