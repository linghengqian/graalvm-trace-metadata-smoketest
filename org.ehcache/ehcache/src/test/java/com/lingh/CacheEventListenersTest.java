package com.lingh;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CacheEventListenersTest {

    @Test
    void testEventProcessingQueues() {
        CacheConfiguration<Long, String> cacheConfiguration = CacheConfigurationBuilder.newCacheConfigurationBuilder(
                        Long.class, String.class, ResourcePoolsBuilder.heap(5L))
                .withDispatcherConcurrency(10)
                .withEventListenersThreadPool("listeners-pool")
                .build();
        try (CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true)) {
            Cache<Long, String> myCache = cacheManager.createCache("myCache", cacheConfiguration);
            myCache.put(1L, "da one!");
            assertThat(myCache.get(1L)).isEqualTo("da one!");
        }
    }
}
