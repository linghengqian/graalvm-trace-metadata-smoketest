

package com.lingh;

import org.apache.commons.dbcp2.Utils;
import org.junit.jupiter.api.Test;

public class TestUtils {

    @Test
    public void testClassLoads() {
        Utils.closeQuietly((AutoCloseable) null);
    }
}
