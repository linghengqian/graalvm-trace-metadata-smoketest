
package com.github.benmanes.caffeine.cache.issues;

import com.github.benmanes.caffeine.cache.AsyncCacheLoader;
import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.google.common.testing.FakeTicker;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFutureTask;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static com.github.benmanes.caffeine.cache.testing.AsyncCacheSubject.assertThat;
import static com.google.common.truth.Truth.assertThat;

public final class Issue193Test {
    private final AtomicLong counter = new AtomicLong(0);
    private final FakeTicker ticker = new FakeTicker();

    private ListenableFutureTask<Long> loadingTask;

    private final AsyncCacheLoader<String, Long> loader = (key, exec) -> {
        loadingTask = ListenableFutureTask.create(counter::getAndIncrement);
        var f = new CompletableFuture<Long>();
        loadingTask.addListener(() -> f.complete(Futures.getUnchecked(loadingTask)), exec);
        return f;
    };

    private final String key = Issue193Test.class.getSimpleName();

    private long loadGet(AsyncLoadingCache<String, Long> cache, String key) {
        CompletableFuture<Long> future = cache.get(key);
        if (!loadingTask.isDone()) {
            loadingTask.run();
        }
        return future.join();
    }

    @Test
    public void invalidateDuringRefreshRemovalCheck() {
        var removed = new ArrayList<Long>();
        AsyncLoadingCache<String, Long> cache = Caffeine.newBuilder()
                .removalListener((String key, Long value, RemovalCause reason) -> removed.add(value))
                .refreshAfterWrite(10, TimeUnit.NANOSECONDS)
                .executor(Runnable::run)
                .ticker(ticker::read)
                .buildAsync(loader);
        assertThat(loadGet(cache, key)).isEqualTo(0);
        ticker.advance(11);
        assertThat(cache).containsEntry(key, 0);
        cache.synchronous().invalidate(key);
        assertThat(cache).doesNotContainKey(key);
        loadingTask.run();
        assertThat(cache).doesNotContainKey(key);
        assertThat(removed).containsExactly(0L, 1L).inOrder();
    }
}
