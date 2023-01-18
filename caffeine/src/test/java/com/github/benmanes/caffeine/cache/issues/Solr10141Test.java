
package com.github.benmanes.caffeine.cache.issues;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalListener;
import com.github.benmanes.caffeine.testing.ConcurrentTestHarness;
import org.testng.annotations.Test;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static com.github.benmanes.caffeine.testing.Awaits.await;
import static com.google.common.truth.Truth.assertThat;
import static java.util.Locale.US;

@Test(groups = "isolated")
public final class Solr10141Test {
    static final int blocksInTest = 400;
    static final int maxEntries = blocksInTest / 2;
    static final int nThreads = 64;
    static final int nReads = 10000000;
    static final int readsPerThread = nReads / nThreads;
    static final int readLastBlockOdds = 10;
    static final boolean updateAnyway = true;
    final Random rnd = new Random();

    @Test
    public void eviction() {
        var hits = new AtomicLong();
        var inserts = new AtomicLong();
        var removals = new AtomicLong();
        RemovalListener<Long, Val> listener = (k, v, removalCause) -> {
            assertThat(v.key).isEqualTo(k);
            if (!v.live.compareAndSet(true, false)) {
                throw new RuntimeException(String.format(US,
                        "listener called more than once! k=%s, v=%s, removalCause=%s", k, v, removalCause));
            }
            removals.incrementAndGet();
        };
        Cache<Long, Val> cache = Caffeine.newBuilder()
                .executor(ConcurrentTestHarness.executor)
                .removalListener(listener)
                .maximumSize(maxEntries)
                .build();
        var lastBlock = new AtomicLong();
        var failed = new AtomicBoolean();
        var maxObservedSize = new AtomicLong();
        ConcurrentTestHarness.timeTasks(nThreads, new Runnable() {
            @Override
            public void run() {
                try {
                    var r = new Random(rnd.nextLong());
                    for (int i = 0; i < readsPerThread; i++) {
                        test(r);
                    }
                } catch (Throwable e) {
                    failed.set(true);
                    e.printStackTrace();
                }
            }

            void test(Random r) {
                long block = r.nextInt(blocksInTest);
                if (readLastBlockOdds > 0 && r.nextInt(readLastBlockOdds) == 0) {
                    block = lastBlock.get();
                }
                lastBlock.set(block);
                Long k = block;
                Val v = cache.getIfPresent(k);
                if (v != null) {
                    hits.incrementAndGet();
                    assertThat(k).isEqualTo(v.key);
                }
                if ((v == null) || (updateAnyway && r.nextBoolean())) {
                    v = new Val();
                    v.key = k;
                    cache.put(k, v);
                    inserts.incrementAndGet();
                }
                long sz = cache.estimatedSize();
                if (sz > maxObservedSize.get()) {
                    maxObservedSize.set(sz);
                }
            }
        });
        await().until(() -> (inserts.get() - removals.get()) == cache.estimatedSize());
        System.out.printf(US, "Done!%n" + "entries=%,d inserts=%,d removals=%,d hits=%,d maxEntries=%,d maxObservedSize=%,d%n",
                cache.estimatedSize(), inserts.get(), removals.get(),
                hits.get(), maxEntries, maxObservedSize.get());
        assertThat(failed.get()).isFalse();
    }

    @Test
    public void clear() {
        var inserts = new AtomicLong();
        var removals = new AtomicLong();
        var failed = new AtomicBoolean();
        RemovalListener<Long, Val> listener = (k, v, removalCause) -> {
            assertThat(v.key).isEqualTo(k);
            if (!v.live.compareAndSet(true, false)) {
                throw new RuntimeException(String.format(US,
                        "listener called more than once! k=%s, v=%s, removalCause=%s", k, v, removalCause));
            }
            removals.incrementAndGet();
        };
        Cache<Long, Val> cache = Caffeine.newBuilder().maximumSize(Integer.MAX_VALUE).removalListener(listener).build();
        ConcurrentTestHarness.timeTasks(nThreads, new Runnable() {
            @Override
            public void run() {
                try {
                    var r = new Random(rnd.nextLong());
                    for (int i = 0; i < readsPerThread; i++) {
                        test(r);
                    }
                } catch (Throwable e) {
                    failed.set(true);
                    e.printStackTrace();
                }
            }

            void test(Random r) {
                Long k = (long) r.nextInt(blocksInTest);
                Val v = cache.getIfPresent(k);
                if (v != null) {
                    assertThat(k).isEqualTo(v.key);
                }
                if ((v == null) || (updateAnyway && r.nextBoolean())) {
                    v = new Val();
                    v.key = k;
                    cache.put(k, v);
                    inserts.incrementAndGet();
                }
                if (r.nextInt(10) == 0) {
                    cache.asMap().clear();
                }
            }
        });
        cache.asMap().clear();
        await().until(() -> inserts.get() == removals.get());
        assertThat(failed.get()).isFalse();
    }

    static class Val {
        long key;
        AtomicBoolean live = new AtomicBoolean(true);
    }
}
