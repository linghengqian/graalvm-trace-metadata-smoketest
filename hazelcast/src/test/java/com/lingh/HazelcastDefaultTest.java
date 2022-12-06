package com.lingh;

import com.hazelcast.cache.HazelcastCachingProvider;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import org.junit.jupiter.api.Test;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.CompleteConfiguration;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.spi.CachingProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class HazelcastDefaultTest {
    @Test
    void testMapOrigin() {
        HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance();
        HazelcastInstance client = HazelcastClient.newHazelcastClient();
        IMap<String, String> map = client.getMap("origin-distributed-map");
        map.put("key", "value");
        map.get("key");
        map.putIfAbsent("someKey", "someValue");
        map.replace("key", "value", "newValue");
        assertThat(map.get("someKey")).isEqualTo("someValue");
        assertThat(map.get("key")).isEqualTo("newValue");
        assertDoesNotThrow(() -> {
            client.shutdown();
            hazelcastInstance.shutdown();
        });
    }

    @Test
    void testJCacheOrigin() {
        HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(new Config());
        CachingProvider cachingProvider = Caching.getCachingProvider(HazelcastCachingProvider.class.getName());
        CacheManager cacheManager = cachingProvider.getCacheManager();
        CompleteConfiguration<String, String> config = new MutableConfiguration<String, String>().setTypes(String.class, String.class);
        Cache<String, String> cache = cacheManager.createCache("example", config);
        cache.put("world", "Hello World");
        assertThat(cache.get("world")).isEqualTo("Hello World");
        assertThat(cacheManager.getCache("example", String.class, String.class)).isNotNull();
        hazelcastInstance.shutdown();
    }
}

