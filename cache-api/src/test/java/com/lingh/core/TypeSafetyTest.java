package com.lingh.core;

import org.junit.jupiter.api.Test;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

public class TypeSafetyTest {
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void runtimeTypeEnforcement() {
        CacheManager cacheManager = Caching.getCachingProvider().getCacheManager();
        MutableConfiguration<String, Integer> config = new MutableConfiguration<String, Integer>().setTypes(String.class, Integer.class);
        Cache<String, Integer> simpleCache = cacheManager.createCache("simpleCache5", config);
        simpleCache.put("key1", 3);
        assertThat(simpleCache.get("key1")).isEqualTo(3);
        try {
            ((Cache) simpleCache).put(123, "String");
        } catch (ClassCastException ignored) {
        }
    }
}
