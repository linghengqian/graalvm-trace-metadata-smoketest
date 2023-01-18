
package com.github.benmanes.caffeine.cache.stats;

import com.google.common.testing.AbstractPackageSanityTests;

public final class PackageSanityTests extends AbstractPackageSanityTests {

    public PackageSanityTests() {
        ignoreClasses(clazz -> clazz.getSimpleName().endsWith("Test"));
    }
}
