package com.lingh;

import com.hazelcast.cache.ICache;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.multimap.MultiMap;
import com.hazelcast.replicatedmap.ReplicatedMap;
import com.hazelcast.topic.ITopic;
import com.hazelcast.topic.MessageListener;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.AccessedExpiryPolicy;
import javax.cache.expiry.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

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
        IMap<String, String> map = client.getMap("my-distributed-map");
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

    @Test
    void testMultiMap() {
        HazelcastInstance client = HazelcastClient.newHazelcastClient();
        MultiMap<String, String> multiMap = client.getMultiMap("my-distributed-multimap");
        multiMap.put("my-key", "value1");
        multiMap.put("my-key", "value2");
        multiMap.put("my-key", "value3");
        Collection<String> values = multiMap.get("my-key");
        assertThat(values.toString()).contains("value2", "value1", "value3");
        multiMap.remove("my-key", "value2");
        client.shutdown();
    }

    @SuppressWarnings("OverwrittenKey")
    @Test
    void testSet() {
        HazelcastInstance client = HazelcastClient.newHazelcastClient();
        Set<String> set = client.getSet("my-distributed-set");
        set.add("item1");
        set.add("item1");
        set.add("item2");
        set.add("item2");
        set.add("item2");
        set.add("item3");
        assertThat(set).contains("item1", "item2", "item3");
        client.shutdown();
    }

    @Test
    void testList() {
        HazelcastInstance client = HazelcastClient.newHazelcastClient();
        List<Object> list = client.getList("my-distributed-list");
        list.add("item1");
        list.add("item2");
        assertThat(list.remove(0)).isEqualTo("item1");
        assertThat(list.size()).isEqualTo(1);
        list.clear();
        client.shutdown();
    }

    @Test
    void testQueue() throws InterruptedException {
        HazelcastInstance hz = HazelcastClient.newHazelcastClient();
        BlockingQueue<String> queue = hz.getQueue("my-distributed-queue");
        assertThat(queue.offer("item")).isTrue();
        queue.poll();
        assertThat(queue.offer("anotheritem", 500, TimeUnit.MILLISECONDS)).isTrue();
        queue.poll(5, TimeUnit.SECONDS);
        queue.put("yetanotheritem");
        assertThat(queue.take()).isEqualTo("yetanotheritem");
        hz.shutdown();
    }

    @SuppressWarnings("rawtypes")
    @Test
    void testTopic() {
        HazelcastInstance hz = HazelcastClient.newHazelcastClient();
        ITopic<Object> topic = hz.getTopic("my-distributed-topic");
        MessageListener topicSample = message -> System.out.println("Got message " + message.getMessageObject());
        topic.addMessageListener(topicSample);
        topic.publish("Hello to distributed world");
        hz.shutdown();
    }
}
