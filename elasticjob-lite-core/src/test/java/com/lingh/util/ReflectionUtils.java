package com.lingh.util;

import java.lang.reflect.Field;

public final class ReflectionUtils {
    private ReflectionUtils() {
    }

    public static void setFieldValue(final Object target, final String fieldName, final Object fieldValue) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, fieldValue);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setSuperclassFieldValue(final Object target, final String fieldName, final Object fieldValue) {
        try {
            Field field = target.getClass().getSuperclass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, fieldValue);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
