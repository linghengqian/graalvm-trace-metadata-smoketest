package com.lingh;

import com.hazelcast.cache.ICache;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.replicatedmap.ReplicatedMap;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.AccessedExpiryPolicy;
import javax.cache.expiry.Duration;

import static org.assertj.core.api.Assertions.assertThat;

public class HazelcastTest {
    static HazelcastInstance hazelcastInstance;

    @BeforeAll
    static void before() {
        hazelcastInstance = Hazelcast.newHazelcastInstance(new Config());
    }

    @AfterAll
    static void after() {
        hazelcastInstance.shutdown();
    }

    @Test
    void testMap() {
        HazelcastInstance client = HazelcastClient.newHazelcastClient();
        IMap<Object, Object> map = client.getMap("my-distributed-map");
        map.put("key", "value");
        map.get("key");
        map.putIfAbsent("somekey", "somevalue");
        map.replace("key", "value", "newvalue");
        client.shutdown();
    }

    @SuppressWarnings("unchecked")
    @Test
    void testJCache() {
        System.setProperty("hazelcast.jcache.provider.type", "client");
        CacheManager manager = Caching.getCachingProvider().getCacheManager();
        MutableConfiguration<String, String> configuration = new MutableConfiguration<>();
        configuration.setExpiryPolicyFactory(AccessedExpiryPolicy.factoryOf(Duration.ONE_MINUTE));
        Cache<String, String> myCache = manager.createCache("myCache", configuration);
        myCache.put("key", "value");
        assertThat(myCache.get("key")).isEqualTo("value");
        ICache<String, String> icache = myCache.unwrap(ICache.class);
        icache.getAsync("key");
        icache.putAsync("key", "value");
        icache.put("key", "newValue", AccessedExpiryPolicy.factoryOf(Duration.TEN_MINUTES).create());
        icache.size();
        manager.getCachingProvider().close();
    }

    @Test
    void testReplicatedMap() {
        HazelcastInstance client = HazelcastClient.newHazelcastClient();
        ReplicatedMap<String, String> map = client.getReplicatedMap("my-replicated-map");
        String replacedValue = map.put("key", "value");
        assertThat(replacedValue).isNull();
        String value = map.get("key");
        assertThat(value).isEqualTo("value");
        client.shutdown();
    }
}
