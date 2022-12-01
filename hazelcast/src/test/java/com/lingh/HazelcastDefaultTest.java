package com.lingh;

import com.hazelcast.cache.HazelcastCachingProvider;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.CompleteConfiguration;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.spi.CachingProvider;

import static org.assertj.core.api.Assertions.assertThat;

public class HazelcastDefaultTest {
    static HazelcastInstance hazelcastInstance;

    @BeforeAll
    static void before() {
        hazelcastInstance = Hazelcast.newHazelcastInstance();
    }

    @AfterAll
    static void after() {
        hazelcastInstance.shutdown();
    }

    @Test
    void testJCacheOrigin() {
        CachingProvider cachingProvider = Caching.getCachingProvider(HazelcastCachingProvider.class.getName());
        CacheManager cacheManager = cachingProvider.getCacheManager();
        CompleteConfiguration<String, String> config = new MutableConfiguration<String, String>().setTypes(String.class, String.class);
        Cache<String, String> cache = cacheManager.createCache("default-example", config);
        cache.put("world", "Hello World");
        assertThat(cache.get("world")).isEqualTo("Hello World");
        assertThat(cacheManager.getCache("default-example", String.class, String.class)).isNotNull();
    }
}
