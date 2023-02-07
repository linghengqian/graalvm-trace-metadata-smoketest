

package com.lingh;

import org.apache.commons.dbcp2.ListException;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ListException.
 */
public class TestListException {

    @Test
    public void testExceptionList() {
        @SuppressWarnings("unchecked")
        final
        List<Throwable> exceptions = Arrays.asList(new NullPointerException(), new RuntimeException());
        final ListException list = new ListException("Internal Error", exceptions);
        assertEquals("Internal Error", list.getMessage());
        assertArrayEquals(exceptions.toArray(), list.getExceptionList().toArray());
    }

    @Test
    public void testNulls() {
        final ListException list = new ListException(null, null);
        assertNull(list.getMessage());
        assertNull(list.getExceptionList());
    }
}
