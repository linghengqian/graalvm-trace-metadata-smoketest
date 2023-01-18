
package com.github.benmanes.caffeine.cache.issues;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.testing.ConcurrentTestHarness;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

@Test(groups = "isolated")
public class Issue568Test {
    @Test
    public void intermittentNull() throws InterruptedException {
        Cache<String, String> cache = Caffeine.newBuilder().executor(ConcurrentTestHarness.executor).weakValues().build();
        String key = "key";
        String val = "val";
        cache.put("key", "val");
        var error = new AtomicReference<RuntimeException>();
        var threads = new ArrayList<Thread>();
        IntStream.range(0, 10).mapToObj(name -> new Thread(() -> {
            for (int j = 0; j < 1000; j++) {
                if (Math.random() < .5) {
                    cache.put(key, val);
                } else if (cache.getIfPresent(key) == null) {
                    error.compareAndSet(null, new IllegalStateException(
                            "Thread " + name + " observed null on iteration " + j));
                    break;
                }
            }
        })).forEach(thread -> {
            threads.add(thread);
            thread.start();
        });
        for (Thread thread : threads) {
            thread.join();
        }
        if (error.get() != null) {
            throw error.get();
        }
    }
    @Test
    public void resurrect() throws InterruptedException {
        var error = new AtomicReference<RuntimeException>();
        Cache<String, Object> cache = Caffeine.newBuilder()
                .weakValues()
                .executor(ConcurrentTestHarness.executor)
                .removalListener((k, v, cause) -> {
                    if (cause == RemovalCause.COLLECTED && (v != null)) {
                        error.compareAndSet(null, new IllegalStateException("Evicted a live value: " + v));
                    }
                }).build();
        String key = "key";
        cache.put(key, new Object());
        var missing = new AtomicBoolean();
        var threads = new ArrayList<Thread>();
        IntStream.range(0, 100).mapToObj(i -> new Thread(() -> {
            IntStream.range(0, 1000).takeWhile(j -> error.get() == null).forEach(j -> {
                if (Math.random() < .01) {
                    System.gc();
                    cache.cleanUp();
                } else if ((cache.getIfPresent(key) == null) && !missing.getAndSet(true)) {
                    cache.put(key, new Object());
                    missing.set(false);
                }
            });
        })).forEach(thread -> {
            threads.add(thread);
            thread.start();
        });
        for (var thread : threads) {
            thread.join();
        }
        if (error.get() != null) {
            throw error.get();
        }
    }
}
