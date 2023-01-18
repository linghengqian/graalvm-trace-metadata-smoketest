
package com.github.benmanes.caffeine.cache;

import com.google.common.testing.AbstractPackageSanityTests;

public final class PackageSanityTests extends AbstractPackageSanityTests {
    public PackageSanityTests() {
        publicApiOnly();
        setDefault(CacheLoader.class, key -> key);
        setDefault(Caffeine.class, Caffeine.newBuilder());
        ignoreClasses(clazz -> clazz == CaffeineSpec.class ||
                clazz.getSimpleName().startsWith("Is") ||
                clazz.getSimpleName().endsWith("Test") ||
                clazz.getSimpleName().contains("Stresser") ||
                clazz.getSimpleName().endsWith("Generator") ||
                clazz.getSimpleName().endsWith("Benchmark")
        );
    }
}
