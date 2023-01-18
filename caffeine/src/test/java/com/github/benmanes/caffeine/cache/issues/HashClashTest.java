
package com.github.benmanes.caffeine.cache.issues;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.testing.CacheContext;
import com.github.benmanes.caffeine.cache.testing.CacheProvider;
import com.github.benmanes.caffeine.cache.testing.CacheSpec;
import com.github.benmanes.caffeine.cache.testing.CacheSpec.Maximum;
import com.github.benmanes.caffeine.cache.testing.CacheSpec.Population;
import com.github.benmanes.caffeine.cache.testing.CacheSpec.Stats;
import com.github.benmanes.caffeine.cache.testing.CacheValidationListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Set;
import java.util.stream.LongStream;

import static com.google.common.truth.Truth.assertThat;
import static java.util.function.Function.identity;

@Listeners(CacheValidationListener.class)
@Test(dataProviderClass = CacheProvider.class)
public final class HashClashTest {
    private static final int STEP = 5;
    private static final Long LONG_1 = 1L;
    private static final long ITERS = 200_000;
    private static final boolean debug = false;

    @Test(dataProvider = "caches")
    @CacheSpec(population = Population.EMPTY, maximumSize = Maximum.ONE_FIFTY, stats = Stats.ENABLED)
    public void testCache(Cache<Long, Long> cache, CacheContext context) {
        LongStream.range(0, 300).forEach(j -> {
            cache.get(1L, identity());
            cache.get(j, identity());
        });
        printStats(cache);
        printKeys(cache);
        Long CLASH = (ITERS << 32) ^ ITERS ^ 1;
        assertThat(CLASH.hashCode()).isEqualTo(LONG_1.hashCode());
        cache.get(CLASH, identity());
        printKeys(cache);
        LongStream.range(0, 300).forEach(j -> {
            cache.get(1L, identity());
            cache.get(j, identity());
        });
        printKeys(cache);
        LongStream.iterate(0, i -> i < ITERS, i -> (long) (i + STEP)).forEach(i -> {
            cache.get(1L, identity());
            LongStream.range(0, STEP).forEach(j -> cache.get(-j, identity()));
        });
        printKeys(cache);
        printStats(cache);
        assertThat(cache.stats().hitRate()).isGreaterThan(0.99d);
    }

    static void printStats(Cache<Long, Long> cache) {
        if (debug) {
            System.out.printf("size %,d requests %,d hit ratio %f%n", cache.estimatedSize(), cache.stats().requestCount(), cache.stats().hitRate());
        }
    }

    static void printKeys(Cache<Long, Long> cache) {
        if (debug) {
            Set<Long> keys = cache.policy().eviction().map(policy -> policy.hottest(Integer.MAX_VALUE).keySet()).orElseGet(cache.asMap()::keySet);
            System.out.println(keys);
        }
    }
}
