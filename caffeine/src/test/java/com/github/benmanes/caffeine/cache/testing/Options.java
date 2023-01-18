
package com.github.benmanes.caffeine.cache.testing;

import com.github.benmanes.caffeine.cache.testing.CacheSpec.Compute;
import com.github.benmanes.caffeine.cache.testing.CacheSpec.Implementation;
import com.github.benmanes.caffeine.cache.testing.CacheSpec.ReferenceType;
import com.github.benmanes.caffeine.cache.testing.CacheSpec.Stats;
import com.google.common.base.Enums;

import java.util.Optional;

import static java.util.Locale.US;
import static org.apache.commons.lang3.StringUtils.capitalize;

final class Options {

    private Options() {
    }

    public static Options fromSystemProperties() {
        return new Options();
    }

    Optional<Compute> compute() {
        return Optional.ofNullable(Enums.getIfPresent(Compute.class,
                System.getProperty("compute", "").toUpperCase(US)).orNull());
    }

    Optional<Implementation> implementation() {
        return Optional.ofNullable(Enums.getIfPresent(Implementation.class,
                capitalize(System.getProperty("implementation", "").toLowerCase(US))).orNull());
    }

    Optional<Stats> stats() {
        return Optional.ofNullable(Enums.getIfPresent(Stats.class,
                System.getProperty("stats", "").toUpperCase(US)).orNull());
    }

    Optional<ReferenceType> keys() {
        return Optional.ofNullable(Enums.getIfPresent(ReferenceType.class,
                System.getProperty("keys", "").toUpperCase(US)).orNull());
    }

    Optional<ReferenceType> values() {
        return Optional.ofNullable(Enums.getIfPresent(ReferenceType.class,
                System.getProperty("values", "").toUpperCase(US)).orNull());
    }
}
