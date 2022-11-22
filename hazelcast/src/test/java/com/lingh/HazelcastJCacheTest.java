package com.lingh;

import com.hazelcast.cache.HazelcastCachingProvider;
import com.hazelcast.cache.ICache;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.CompleteConfiguration;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.AccessedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.spi.CachingProvider;

import static org.assertj.core.api.Assertions.assertThat;

public class HazelcastJCacheTest {
    static HazelcastInstance hazelcastInstance;

    @BeforeAll
    static void before() {
        hazelcastInstance = Hazelcast.newHazelcastInstance(new Config());
    }

    @AfterAll
    static void after() {
        hazelcastInstance.shutdown();
    }

    @SuppressWarnings("unchecked")
    @Test
    @Disabled
    @DisabledInNativeImage
    void testJCache() {
        System.setProperty("hazelcast.jcache.provider.type", "client");
        CacheManager manager = Caching.getCachingProvider(HazelcastCachingProvider.class.getName()).getCacheManager();
        MutableConfiguration<String, String> configuration = new MutableConfiguration<>();
        configuration.setExpiryPolicyFactory(AccessedExpiryPolicy.factoryOf(Duration.ONE_MINUTE));
        Cache<String, String> myCache = manager.createCache("myCache", configuration);
        myCache.put("key", "value");
        assertThat(myCache.get("key")).isEqualTo("value");
        ICache<String, String> cacheAsI = myCache.unwrap(ICache.class);
        cacheAsI.getAsync("key");
        cacheAsI.putAsync("key", "value");
        cacheAsI.put("key", "newValue", AccessedExpiryPolicy.factoryOf(Duration.TEN_MINUTES).create());
        assertThat(cacheAsI.size()).isEqualTo(1);
        manager.getCachingProvider().close();
    }

    @Test
    void testJCacheOrigin() {
        CachingProvider cachingProvider = Caching.getCachingProvider(HazelcastCachingProvider.class.getName());
        CacheManager cacheManager = cachingProvider.getCacheManager();
        CompleteConfiguration<String, String> config = new MutableConfiguration<String, String>().setTypes(String.class, String.class);
        Cache<String, String> cache = cacheManager.createCache("example", config);
        cache.put("world", "Hello World");
        assertThat(cache.get("world")).isEqualTo("Hello World");
        assertThat(cacheManager.getCache("example", String.class, String.class)).isNotNull();
    }
}
