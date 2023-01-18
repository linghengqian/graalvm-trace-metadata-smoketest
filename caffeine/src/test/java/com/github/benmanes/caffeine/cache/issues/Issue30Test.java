
package com.github.benmanes.caffeine.cache.issues;

import com.github.benmanes.caffeine.cache.AsyncCacheLoader;
import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.testing.CacheValidationListener;
import com.github.benmanes.caffeine.testing.FutureSubject;
import com.google.common.util.concurrent.MoreExecutors;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.github.benmanes.caffeine.testing.Awaits.await;
import static com.github.benmanes.caffeine.testing.FutureSubject.future;
import static com.google.common.truth.Truth.assertWithMessage;
import static java.time.ZoneOffset.UTC;
import static java.util.Locale.US;

@Test(groups = "isolated")
@Listeners(CacheValidationListener.class)
public final class Issue30Test {
    private static final boolean DEBUG = false;

    private static final String A_KEY = "foo";
    private static final String A_ORIGINAL = "foo0";
    private static final String A_UPDATE_1 = "foo1";
    private static final String A_UPDATE_2 = "foo2";

    private static final String B_KEY = "bar";
    private static final String B_ORIGINAL = "bar0";
    private static final String B_UPDATE_1 = "bar1";
    private static final String B_UPDATE_2 = "bar2";

    private static final int TTL = 100;
    private static final int EPSILON = 10;
    private static final int N_THREADS = 10;

    private final ExecutorService executor = Executors.newFixedThreadPool(N_THREADS);

    @AfterClass
    public void afterClass() {
        MoreExecutors.shutdownAndAwaitTermination(executor, 1, TimeUnit.MINUTES);
    }

    @DataProvider(name = "params")
    public Object[][] providesCache() {
        var source = new ConcurrentHashMap<String, String>();
        var lastLoad = new ConcurrentHashMap<String, Instant>();
        AsyncLoadingCache<String, String> cache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMillis(TTL))
                .executor(executor)
                .buildAsync(new Loader(source, lastLoad));
        return new Object[][]{{cache, source, lastLoad}};
    }

    @Test(dataProvider = "params", invocationCount = 100, threadPoolSize = N_THREADS)
    public void expiration(AsyncLoadingCache<String, String> cache,
                           ConcurrentMap<String, String> source, ConcurrentMap<String, Instant> lastLoad)
            throws InterruptedException {
        initialValues(cache, source, lastLoad);
        firstUpdate(cache, source);
        secondUpdate(cache, source);
    }

    private void initialValues(AsyncLoadingCache<String, String> cache,
                               ConcurrentMap<String, String> source, ConcurrentMap<String, Instant> lastLoad) {
        source.put(A_KEY, A_ORIGINAL);
        source.put(B_KEY, B_ORIGINAL);
        lastLoad.clear();

        assertThat("should serve initial value", cache.get(A_KEY)).succeedsWith(A_ORIGINAL);
        assertThat("should serve initial value", cache.get(B_KEY)).succeedsWith(B_ORIGINAL);
    }

    private void firstUpdate(AsyncLoadingCache<String, String> cache, ConcurrentMap<String, String> source) throws InterruptedException {
        source.put(A_KEY, A_UPDATE_1);
        source.put(B_KEY, B_UPDATE_1);
        assertThat("should serve cached initial value", cache.get(A_KEY)).succeedsWith(A_ORIGINAL);
        assertThat("should serve cached initial value", cache.get(B_KEY)).succeedsWith(B_ORIGINAL);
        Thread.sleep(EPSILON);
        assertThat("still serve cached initial value", cache.get(A_KEY)).succeedsWith(A_ORIGINAL);
        assertThat("still serve cached initial value", cache.get(B_KEY)).succeedsWith(B_ORIGINAL);

        Thread.sleep(TTL + EPSILON);
        assertThat("now serve first updated value", cache.get(A_KEY)).succeedsWith(A_UPDATE_1);
        await().untilAsserted(() -> {
            assertThat("now serve first updated value", cache.get(B_KEY)).succeedsWith(B_UPDATE_1);
        });
    }

    private void secondUpdate(AsyncLoadingCache<String, String> cache, ConcurrentMap<String, String> source) throws InterruptedException {
        source.put(A_KEY, A_UPDATE_2);
        source.put(B_KEY, B_UPDATE_2);
        assertThat("serve cached first updated value", cache.get(A_KEY)).succeedsWith(A_UPDATE_1);
        assertThat("serve cached first updated value", cache.get(B_KEY)).succeedsWith(B_UPDATE_1);
        Thread.sleep(EPSILON);
        assertThat("serve cached first updated value", cache.get(A_KEY)).succeedsWith(A_UPDATE_1);
        assertThat("serve cached first updated value", cache.get(A_KEY)).succeedsWith(A_UPDATE_1);
    }

    private static FutureSubject assertThat(String message, CompletableFuture<?> actual) {
        return assertWithMessage(message).about(future()).that(actual);
    }

    static final class Loader implements AsyncCacheLoader<String, String> {
        private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("hh:MM:ss.SSS", US);
        final ConcurrentMap<String, String> source;
        final ConcurrentMap<String, Instant> lastLoad;

        Loader(ConcurrentMap<String, String> source, ConcurrentMap<String, Instant> lastLoad) {
            this.source = source;
            this.lastLoad = lastLoad;
        }

        @Override
        public CompletableFuture<String> asyncLoad(String key, Executor executor) {
            reportCacheMiss(key);
            return CompletableFuture.completedFuture(source.get(key));
        }

        @SuppressWarnings("TimeZoneUsage")
        private void reportCacheMiss(String key) {
            Instant now = Instant.now();
            Instant last = lastLoad.get(key);
            lastLoad.put(key, now);

            if (DEBUG) {
                String time = FORMATTER.format(LocalDateTime.now(UTC));
                if (last == null) {
                    System.out.println(key + ": first load @ " + time);
                } else {
                    long duration = (now.toEpochMilli() - last.toEpochMilli());
                    System.out.println(key + ": " + duration + "ms after last load @ " + time);
                }
            }
        }
    }
}
