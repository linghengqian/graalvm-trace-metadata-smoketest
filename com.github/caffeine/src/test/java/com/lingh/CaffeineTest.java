package com.lingh;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.Scheduler;
import com.google.common.testing.FakeTicker;
import org.junit.jupiter.api.Test;

import java.lang.ref.Cleaner;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class CaffeineTest {
    @Test
    void testRefresh() {
        LoadingCache<String, String> graphs = Caffeine.newBuilder().maximumSize(10_000).refreshAfterWrite(1, TimeUnit.MINUTES)
                .build(key -> key.equals("Hello") ? "World" : "Universe");
        assertThat(graphs.get("Hello")).isEqualTo("World");
        assertThat(graphs.getAll(List.of("Hi", "Aloha"))).isEqualTo(Map.of("Hi", "Universe", "Aloha", "Universe"));
    }

    @Test
    void testStatistics() {
        Cache<String, String> graphs = Caffeine.newBuilder().maximumSize(10_000).recordStats().build();
        graphs.put("Hello", "World");
        assertThat(graphs.getIfPresent("Hello")).isEqualTo("World");
    }

    @Test
    void testSpecification() {
        CaffeineSpec spec = CaffeineSpec.parse("maximumWeight=1000, expireAfterWrite=10m, recordStats");
        LoadingCache<String, String> graphs = Caffeine.from(spec).weigher((String key, String graph) -> graph.length())
                .build(key -> key.equals("Hello") ? "World" : "Universe");
        assertThat(graphs.get("Hello")).isEqualTo("World");
    }

    @Test
    void testCleanup() {
        LoadingCache<String, String> firstGraphs = Caffeine.newBuilder().scheduler(Scheduler.systemScheduler()).expireAfterWrite(10, TimeUnit.MINUTES)
                .build(key -> key.equals("Hello") ? "World" : "Universe");
        firstGraphs.put("Hello", "World");
        assertThat(firstGraphs.getIfPresent("Hello")).isEqualTo("World");
        Cache<String, String> secondGraphs = Caffeine.newBuilder().weakValues().build();
        Cleaner cleaner = Cleaner.create();
        cleaner.register("World", secondGraphs::cleanUp);
        secondGraphs.put("Hello", "World");
        assertThat(secondGraphs.getIfPresent("Hello")).isEqualTo("World");
    }

    @Test
    void testTesting() {
        FakeTicker ticker = new FakeTicker();
        Cache<String, String> cache = Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .executor(Runnable::run)
                .ticker(ticker::read)
                .maximumSize(10)
                .build();
        cache.put("Hello", "World");
        ticker.advance(30, TimeUnit.MINUTES);
        assertThat(cache.getIfPresent("Hello")).isNull();
    }
}
