
package org.apache.commons.dbcp2;

import java.lang.reflect.Field;

public class TesterUtils {

    /**
     * Access a private field. Do it this way rather than increasing the
     * visibility of the field in the public API.
     */
    public static Object getField(final Object target, final String fieldName)
            throws Exception {
        final Class<?> clazz = target.getClass();
        final Field f = clazz.getDeclaredField(fieldName);
        f.setAccessible(true);
        return f.get(target);
    }

    private TesterUtils() {
        // Utility class - hide default constructor
    }
}
