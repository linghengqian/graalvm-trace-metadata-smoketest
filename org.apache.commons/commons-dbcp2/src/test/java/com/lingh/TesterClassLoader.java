
package com.lingh;

import java.util.HashSet;
import java.util.Set;

/**
 * Simple class loader that just records the classes it was asked to load.
 */
public class TesterClassLoader extends ClassLoader {

    private final Set<String> loadedClasses = new HashSet<>();

    public boolean didLoad(final String className) {
        return loadedClasses.contains(className);
    }

    @Override
    protected synchronized Class<?> loadClass(final String name, final boolean resolve)
            throws ClassNotFoundException {
        final Class<?> clazz =  super.loadClass(name, resolve);
        loadedClasses.add(name);
        return clazz;
    }
}
