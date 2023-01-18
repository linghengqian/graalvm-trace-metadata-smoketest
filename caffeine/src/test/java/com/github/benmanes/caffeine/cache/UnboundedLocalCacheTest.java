
package com.github.benmanes.caffeine.cache;

import com.github.benmanes.caffeine.cache.testing.*;
import com.github.benmanes.caffeine.cache.testing.CacheSpec.*;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static com.google.common.truth.Truth8.assertThat;
import static uk.org.lidalia.slf4jext.Level.TRACE;

@CheckMaxLogLevel(TRACE)
@Listeners(CacheValidationListener.class)
@Test(dataProviderClass = CacheProvider.class)
public final class UnboundedLocalCacheTest {

    @CacheSpec(population = Population.EMPTY, refreshAfterWrite = Expire.DISABLED,
            expireAfterAccess = Expire.DISABLED, expireAfterWrite = Expire.DISABLED,
            maximumSize = Maximum.DISABLED, weigher = CacheWeigher.DISABLED,
            keys = ReferenceType.STRONG, values = ReferenceType.STRONG)
    @Test(dataProvider = "caches")
    public void noPolicy(Cache<Integer, Integer> cache, CacheContext context) {
        assertThat(cache.policy().eviction()).isEmpty();
        assertThat(cache.policy().expireAfterWrite()).isEmpty();
        assertThat(cache.policy().expireAfterAccess()).isEmpty();
        assertThat(cache.policy().refreshAfterWrite()).isEmpty();
    }
}
