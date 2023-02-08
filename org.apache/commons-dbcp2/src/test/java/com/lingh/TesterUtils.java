
package com.lingh;

import java.lang.reflect.Field;

public class TesterUtils {
    public static Object getField(final Object target, final String fieldName)
            throws Exception {
        final Class<?> clazz = target.getClass();
        final Field f = clazz.getDeclaredField(fieldName);
        f.setAccessible(true);
        return f.get(target);
    }

    private TesterUtils() {
    }
}
