
package com.github.benmanes.caffeine.cache;

import com.github.benmanes.caffeine.cache.LocalAsyncCache.AsyncBulkCompleter.NullMapCompletionException;
import com.github.benmanes.caffeine.cache.testing.*;
import com.github.benmanes.caffeine.cache.testing.CacheSpec.CacheExecutor;
import com.github.benmanes.caffeine.cache.testing.CacheSpec.ExecutorFailure;
import com.github.benmanes.caffeine.cache.testing.CacheSpec.Listener;
import com.github.benmanes.caffeine.cache.testing.CacheSpec.Population;
import com.github.benmanes.caffeine.testing.Int;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.primitives.Ints;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.IntStream;

import static com.github.benmanes.caffeine.cache.RemovalCause.EXPLICIT;
import static com.github.benmanes.caffeine.cache.RemovalCause.REPLACED;
import static com.github.benmanes.caffeine.cache.testing.AsyncCacheSubject.assertThat;
import static com.github.benmanes.caffeine.cache.testing.CacheContextSubject.assertThat;
import static com.github.benmanes.caffeine.testing.Awaits.await;
import static com.github.benmanes.caffeine.testing.CollectionSubject.assertThat;
import static com.github.benmanes.caffeine.testing.FutureSubject.assertThat;
import static com.github.benmanes.caffeine.testing.MapSubject.assertThat;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.truth.Truth.assertThat;
import static java.util.function.Function.identity;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.org.lidalia.slf4jext.Level.WARN;

@CheckNoEvictions
@CheckMaxLogLevel(WARN)
@Listeners(CacheValidationListener.class)
@Test(dataProviderClass = CacheProvider.class)
@SuppressWarnings("FutureReturnValueIgnored")
public final class AsyncCacheTest {
    @CacheSpec(removalListener = {Listener.DISABLED, Listener.REJECTING})
    @Test(dataProvider = "caches", expectedExceptions = NullPointerException.class)
    public void getIfPresent_nullKey(AsyncCache<Int, Int> cache, CacheContext context) {
        cache.getIfPresent(null);
    }

    @Test(dataProvider = "caches")
    @CacheSpec(removalListener = {Listener.DISABLED, Listener.REJECTING})
    public void getIfPresent_absent(AsyncCache<Int, Int> cache, CacheContext context) {
        assertThat(cache.getIfPresent(context.absentKey())).isNull();
        assertThat(context).stats().hits(0).misses(1).success(0).failures(0);
    }

    @Test(dataProvider = "caches")
    @CacheSpec(removalListener = {Listener.DISABLED, Listener.REJECTING},
            population = {Population.SINGLETON, Population.PARTIAL, Population.FULL})
    public void getIfPresent_present(AsyncCache<Int, Int> cache, CacheContext context) {
        assertThat(cache.getIfPresent(context.firstKey())).isNotNull();
        assertThat(cache.getIfPresent(context.middleKey())).isNotNull();
        assertThat(cache.getIfPresent(context.lastKey())).isNotNull();
        assertThat(context).stats().hits(3).misses(0).success(0).failures(0);
    }

    @CacheSpec
    @Test(dataProvider = "caches", expectedExceptions = NullPointerException.class)
    public void getFunc_nullKey(AsyncCache<Int, Int> cache, CacheContext context) {
        cache.get(null, key -> null);
    }

    @CacheSpec
    @Test(dataProvider = "caches", expectedExceptions = NullPointerException.class)
    public void getFunc_nullLoader(AsyncCache<Int, Int> cache, CacheContext context) {
        cache.get(context.absentKey(), (Function<Int, Int>) null);
    }

    @CacheSpec
    @Test(dataProvider = "caches", expectedExceptions = NullPointerException.class)
    public void getFunc_nullKeyAndLoader(AsyncCache<Int, Int> cache, CacheContext context) {
        cache.get(null, (Function<Int, Int>) null);
    }

    @CacheSpec
    @Test(dataProvider = "caches")
    public void getFunc_absent_null(AsyncCache<Int, Int> cache, CacheContext context) {
        Int key = context.absentKey();
        var valueFuture = cache.get(key, k -> null);
        assertThat(context).stats().hits(0).misses(1).success(0).failures(1);
        assertThat(valueFuture).isDone();
        assertThat(cache).doesNotContainKey(key);
    }

    @Test(dataProvider = "caches")
    @CacheSpec(executor = CacheExecutor.THREADED, executorFailure = ExecutorFailure.IGNORED)
    public void getFunc_absent_null_async(AsyncCache<Int, Int> cache, CacheContext context) {
        Int key = context.absentKey();
        var ready = new AtomicBoolean();
        var done = new AtomicBoolean();
        var valueFuture = cache.get(key, k -> {
            await().untilTrue(ready);
            return null;
        });
        valueFuture.whenComplete((r, e) -> done.set(true));
        ready.set(true);
        await().untilTrue(done);
        await().untilAsserted(() -> assertThat(cache).doesNotContainKey(context.absentKey()));
        assertThat(context).stats().hits(0).misses(1).success(0).failures(1);

        assertThat(valueFuture).isDone();
        assertThat(cache).doesNotContainKey(key);
    }

    @CacheSpec
    @Test(dataProvider = "caches")
    public void getFunc_absent_failure(AsyncCache<Int, Int> cache, CacheContext context) {
        var valueFuture = cache.get(context.absentKey(), k -> {
            throw new IllegalStateException();
        });
        assertThat(context).stats().hits(0).misses(1).success(0).failures(1);
        assertThat(valueFuture).hasCompletedExceptionally();
        assertThat(cache).doesNotContainKey(context.absentKey());
    }

    @Test(dataProvider = "caches")
    @CacheSpec(executor = CacheExecutor.THREADED, executorFailure = ExecutorFailure.IGNORED)
    public void getFunc_absent_failure_async(AsyncCache<Int, Int> cache, CacheContext context) {
        Int key = context.absentKey();
        var done = new AtomicBoolean();
        var valueFuture = cache.get(key, k -> {
            throw new IllegalStateException();
        });
        valueFuture.whenComplete((r, e) -> done.set(true));
        await().untilTrue(done);
        await().untilAsserted(() -> assertThat(cache).doesNotContainKey(key));
        await().untilAsserted(() -> assertThat(cache).hasSize(context.initialSize()));
        assertThat(valueFuture).hasCompletedExceptionally();
        assertThat(context).stats().hits(0).misses(1).success(0).failures(1);
    }

    @Test(dataProvider = "caches")
    @CacheSpec(executor = CacheExecutor.THREADED, executorFailure = ExecutorFailure.IGNORED)
    public void getFunc_absent_cancelled(AsyncCache<Int, Int> cache, CacheContext context) {
        var done = new AtomicBoolean();
        var valueFuture = cache.get(context.absentKey(), k -> {
            await().until(done::get);
            return null;
        });
        valueFuture.whenComplete((r, e) -> done.set(true));
        valueFuture.cancel(true);
        await().untilTrue(done);
        assertThat(context).stats().hits(0).misses(1).success(0).failures(1);
        assertThat(valueFuture).isDone();
        await().untilAsserted(() -> assertThat(cache).doesNotContainKey(context.absentKey()));
    }

    @CacheSpec
    @Test(dataProvider = "caches")
    public void getFunc_absent(AsyncCache<Int, Int> cache, CacheContext context) {
        Int key = context.absentKey();
        var value = cache.get(key, k -> context.absentValue());
        assertThat(value).succeedsWith(context.absentValue());
        assertThat(context).stats().hits(0).misses(1).success(1).failures(0);
    }

    @Test(dataProvider = "caches")
    @CacheSpec(population = {Population.SINGLETON, Population.PARTIAL, Population.FULL})
    public void getFunc_present(AsyncCache<Int, Int> cache, CacheContext context) {
        Function<Int, Int> loader = key -> {
            throw new RuntimeException();
        };
        assertThat(cache.get(context.firstKey(), loader))
                .succeedsWith(context.original().get(context.firstKey()));
        assertThat(cache.get(context.middleKey(), loader))
                .succeedsWith(context.original().get(context.middleKey()));
        assertThat(cache.get(context.lastKey(), loader))
                .succeedsWith(context.original().get(context.lastKey()));
        assertThat(context).stats().hits(3).misses(0).success(0).failures(0);
    }

    @CacheSpec
    @Test(dataProvider = "caches", expectedExceptions = NullPointerException.class)
    public void getBiFunc_nullKey(AsyncCache<Int, Int> cache, CacheContext context) {
        cache.get(null, (key, executor) -> CompletableFuture.completedFuture(null));
    }

    @CacheSpec
    @Test(dataProvider = "caches", expectedExceptions = NullPointerException.class)
    public void getBiFunc_nullLoader(AsyncCache<Int, Int> cache, CacheContext context) {
        BiFunction<Int, Executor, CompletableFuture<Int>> mappingFunction = null;
        cache.get(context.absentKey(), mappingFunction);
    }

    @CacheSpec
    @Test(dataProvider = "caches", expectedExceptions = NullPointerException.class)
    public void getBiFunc_nullKeyAndLoader(AsyncCache<Int, Int> cache, CacheContext context) {
        BiFunction<Int, Executor, CompletableFuture<Int>> mappingFunction = null;
        cache.get(null, mappingFunction);
    }

    @CacheSpec
    @Test(dataProvider = "caches", expectedExceptions = IllegalStateException.class)
    public void getBiFunc_throwsException(AsyncCache<Int, Int> cache, CacheContext context) {
        try {
            cache.get(context.absentKey(), (key, executor) -> {
                throw new IllegalStateException();
            });
        } finally {
            assertThat(context).stats().hits(0).misses(1).success(0).failures(1);
            assertThat(cache).doesNotContainKey(context.absentKey());
        }
    }

    @CacheSpec
    @Test(dataProvider = "caches", expectedExceptions = UnknownError.class)
    public void getBiFunc_throwsError(AsyncCache<Int, Int> cache, CacheContext context) {
        try {
            cache.get(context.absentKey(), (key, executor) -> {
                throw new UnknownError();
            });
        } finally {
            assertThat(context).stats().hits(0).misses(1).success(0).failures(1);
            assertThat(cache).doesNotContainKey(context.absentKey());
        }
    }

    @CacheSpec
    @Test(dataProvider = "caches", expectedExceptions = NullPointerException.class)
    public void getBiFunc_absent_null(AsyncCache<Int, Int> cache, CacheContext context) {
        cache.get(context.absentKey(), (k, executor) -> null);
    }

    @CacheSpec
    @Test(dataProvider = "caches")
    public void getBiFunc_absent_failure_before(AsyncCache<Int, Int> cache, CacheContext context) {
        var failedFuture = new CompletableFuture<Int>();
        failedFuture.completeExceptionally(new IllegalStateException());

        Int key = context.absentKey();
        var valueFuture = cache.get(key, (k, executor) -> failedFuture);
        assertThat(context).stats().hits(0).misses(1).success(0).failures(1);

        assertThat(valueFuture).hasCompletedExceptionally();
        assertThat(cache).doesNotContainKey(key);
    }

    @CacheSpec
    @Test(dataProvider = "caches")
    public void getBiFunc_absent_failure_after(AsyncCache<Int, Int> cache, CacheContext context) {
        var failedFuture = new CompletableFuture<Int>();

        Int key = context.absentKey();
        var valueFuture = cache.get(key, (k, executor) -> failedFuture);
        failedFuture.completeExceptionally(new IllegalStateException());
        assertThat(context).stats().hits(0).misses(1).success(0).failures(1);

        assertThat(valueFuture).hasCompletedExceptionally();
        assertThat(cache).doesNotContainKey(key);
    }

    @CacheSpec
    @Test(dataProvider = "caches")
    public void getBiFunc_absent_cancelled(AsyncCache<Int, Int> cache, CacheContext context) {
        var cancelledFuture = new CompletableFuture<Int>();
        cache.get(context.absentKey(), (k, executor) -> cancelledFuture);
        cancelledFuture.cancel(true);

        assertThat(context).stats().hits(0).misses(1).success(0).failures(1);
        assertThat(cache).doesNotContainKey(context.absentKey());
    }

    @CacheSpec
    @Test(dataProvider = "caches")
    public void getBiFunc_absent(AsyncCache<Int, Int> cache, CacheContext context) {
        Int key = context.absentKey();
        var value = cache.get(key, (k, executor) -> context.absentValue().asFuture());
        assertThat(value).succeedsWith(context.absentValue());
        assertThat(context).stats().hits(0).misses(1).success(1).failures(0);
    }

    @Test(dataProvider = "caches")
    @CacheSpec(population = {Population.SINGLETON, Population.PARTIAL, Population.FULL})
    public void getBiFunc_present(AsyncCache<Int, Int> cache, CacheContext context) {
        BiFunction<Int, Executor, CompletableFuture<Int>> loader =
                (key, executor) -> {
                    throw new RuntimeException();
                };
        assertThat(cache.get(context.firstKey(), loader))
                .succeedsWith(context.original().get(context.firstKey()));
        assertThat(cache.get(context.middleKey(), loader))
                .succeedsWith(context.original().get(context.middleKey()));
        assertThat(cache.get(context.lastKey(), loader))
                .succeedsWith(context.original().get(context.lastKey()));
        assertThat(context).stats().hits(3).misses(0).success(0).failures(0);
    }

    @CheckNoStats
    @Test(dataProvider = "caches", expectedExceptions = NullPointerException.class)
    @CacheSpec(removalListener = {Listener.DISABLED, Listener.REJECTING})
    public void getAllFunction_nullKeys(AsyncCache<Int, Int> cache, CacheContext context) {
        cache.getAll(null, keys -> {
            throw new AssertionError();
        });
    }

    @CheckNoStats
    @Test(dataProvider = "caches", expectedExceptions = NullPointerException.class)
    @CacheSpec(removalListener = {Listener.DISABLED, Listener.REJECTING})
    public void getAllFunction_nullKeys_nullFunction(
            AsyncCache<Int, Int> cache, CacheContext context) {
        cache.getAll(null, (Function<Set<? extends Int>, Map<Int, Int>>) null);
    }

    @CheckNoStats
    @Test(dataProvider = "caches", expectedExceptions = NullPointerException.class)
    @CacheSpec(removalListener = {Listener.DISABLED, Listener.REJECTING})
    public void getAllFunction_nullFunction(AsyncCache<Int, Int> cache, CacheContext context) {
        cache.getAll(context.original().keySet(), (Function<Set<? extends Int>, Map<Int, Int>>) null);
    }

    @CheckNoStats
    @Test(dataProvider = "caches", expectedExceptions = NullPointerException.class)
    @CacheSpec(removalListener = {Listener.DISABLED, Listener.REJECTING})
    public void getAllFunction_nullKey(AsyncCache<Int, Int> cache, CacheContext context) {
        cache.getAll(Collections.singletonList(null), keys -> {
            throw new AssertionError();
        });
    }

    @CheckNoStats
    @Test(dataProvider = "caches")
    @CacheSpec(removalListener = {Listener.DISABLED, Listener.REJECTING})
    public void getAllFunction_iterable_empty(AsyncCache<Int, Int> cache, CacheContext context) {
        var result = cache.getAll(List.of(), keys -> {
            throw new AssertionError();
        }).join();
        assertThat(result).isExhaustivelyEmpty();
    }

    @CacheSpec
    @Test(dataProvider = "caches")
    public void getAllFunction_immutable_keys(AsyncCache<Int, Int> cache, CacheContext context) {
        var future = cache.getAll(context.absentKeys(), keys -> {
            keys.clear();
            return Map.of();
        });
        assertThat(future).failsWith(CompletionException.class)
                .hasCauseThat().isInstanceOf(UnsupportedOperationException.class);
    }

    @CacheSpec
    @Test(dataProvider = "caches", expectedExceptions = UnsupportedOperationException.class)
    public void getAllFunction_immutable_result(AsyncCache<Int, Int> cache, CacheContext context) {
        var result = cache.getAll(context.absentKeys(),
                keys -> keys.stream().collect(toImmutableMap(identity(), identity()))).join();
        result.clear();
    }

    @CacheSpec
    @Test(dataProvider = "caches")
    public void getAllFunction_absent_null(AsyncCache<Int, Int> cache, CacheContext context) {
        assertThat(cache.getAll(context.absentKeys(), keys -> null))
                .failsWith(NullMapCompletionException.class);
        assertThat(context).stats().hits(0).misses(context.absentKeys().size()).success(0).failures(1);
    }

    @CacheSpec
    @Test(dataProvider = "caches")
    public void getAllFunction_absent_failure(AsyncCache<Int, Int> cache, CacheContext context) {
        assertThat(cache.getAll(context.absentKeys(), keys -> {
            throw new IllegalStateException();
        }))
                .failsWith(CompletionException.class)
                .hasCauseThat().isInstanceOf(IllegalStateException.class);
        int misses = context.absentKeys().size();
        assertThat(context).stats().hits(0).misses(misses).success(0).failures(1);
    }

    @Test(dataProvider = "caches")
    @CacheSpec(removalListener = {Listener.DISABLED, Listener.REJECTING})
    public void getAllFunction_absent(AsyncCache<Int, Int> cache, CacheContext context) {
        var result = cache.getAll(context.absentKeys(), keys -> context.absent()).join();
        int count = context.absentKeys().size();
        assertThat(result).hasSize(count);
        assertThat(context).stats().hits(0).misses(count).success(1).failures(0);
    }

    @Test(dataProvider = "caches")
    @CacheSpec(population = {Population.SINGLETON, Population.PARTIAL, Population.FULL},
            removalListener = {Listener.DISABLED, Listener.REJECTING})
    public void getAllFunction_present_partial(AsyncCache<Int, Int> cache, CacheContext context) {
        var expect = new HashMap<Int, Int>();
        expect.put(context.firstKey(), context.firstKey().negate());
        expect.put(context.middleKey(), context.middleKey().negate());
        expect.put(context.lastKey(), context.lastKey().negate());
        var result = cache.getAll(expect.keySet(), keys -> {
            assertThat(keys).hasSizeLessThan(expect.size());
            return keys.stream().collect(toImmutableMap(identity(), Int::negate));
        }).join();

        assertThat(result).isEqualTo(expect);
        assertThat(context).stats().hits(expect.size()).misses(0).success(0).failures(0);
    }

    @Test(dataProvider = "caches")
    @CacheSpec(removalListener = {Listener.DISABLED, Listener.REJECTING})
    public void getAllFunction_exceeds(AsyncCache<Int, Int> cache, CacheContext context) {
        var result = cache.getAll(context.absentKeys(), keys -> {
            var moreKeys = new ArrayList<Int>(keys);
            for (int i = 0; i < 10; i++) {
                moreKeys.add(Int.valueOf(ThreadLocalRandom.current().nextInt()));
            }
            return moreKeys.stream().collect(toImmutableMap(identity(), Int::negate));
        }).join();

        assertThat(result).containsExactlyKeys(context.absentKeys());
        assertThat(cache).hasSizeGreaterThan(context.initialSize() + context.absentKeys().size());
        assertThat(context).stats().hits(0).misses(result.size()).success(1).failures(0);
    }

    @Test(dataProvider = "caches")
    @CacheSpec(removalListener = {Listener.DISABLED, Listener.REJECTING})
    public void getAllFunction_different(AsyncCache<Int, Int> cache, CacheContext context) {
        var actual = context.absentKeys().stream().collect(toImmutableMap(Int::negate, identity()));
        var result = cache.getAll(context.absentKeys(), keys -> actual).join();

        assertThat(result).isEmpty();
        assertThat(cache).hasSize(context.initialSize() + actual.size());
        assertThat(cache.synchronous().asMap()).containsAtLeastEntriesIn(actual);
        assertThat(context).stats().hits(0).misses(actual.size()).success(1).failures(0);
    }

    @Test(dataProvider = "caches")
    @CacheSpec(population = {Population.SINGLETON, Population.PARTIAL, Population.FULL},
            removalListener = {Listener.DISABLED, Listener.REJECTING})
    public void getAllFunction_duplicates(AsyncCache<Int, Int> cache, CacheContext context) {
        var absentKeys = ImmutableSet.copyOf(Iterables.limit(context.absentKeys(),
                Ints.saturatedCast(context.maximum().max() - context.initialSize())));
        var keys = Iterables.concat(absentKeys, absentKeys,
                context.original().keySet(), context.original().keySet());
        var result = cache.getAll(keys, keysToLoad -> {
            assertThat(keysToLoad).containsNoDuplicates();
            return keysToLoad.stream().collect(toImmutableMap(identity(), Int::negate));
        }).join();

        assertThat(context).stats().hits(context.initialSize())
                .misses(absentKeys.size()).success(1).failures(0);
        assertThat(result).containsExactlyKeys(keys);
    }

    @Test(dataProvider = "caches")
    @CacheSpec(population = {Population.SINGLETON, Population.PARTIAL, Population.FULL},
            removalListener = {Listener.DISABLED, Listener.REJECTING})
    public void getAllFunction_present_ordered_absent(
            AsyncCache<Int, Int> cache, CacheContext context) {
        var keys = new ArrayList<>(context.absentKeys());
        Collections.shuffle(keys);
        var result = cache.getAll(keys, keysToLoad -> keysToLoad.stream().collect(toImmutableMap(identity(), Int::negate))).join();
        assertThat(result).containsExactlyKeys(keys).inOrder();
    }

    @Test(dataProvider = "caches")
    @CacheSpec(population = {Population.SINGLETON, Population.PARTIAL},
            removalListener = {Listener.DISABLED, Listener.REJECTING})
    public void getAllFunction_present_ordered_partial(
            AsyncCache<Int, Int> cache, CacheContext context) {
        var keys = new ArrayList<>(context.original().keySet());
        keys.addAll(context.absentKeys());
        Collections.shuffle(keys);
        var result = cache.getAll(keys, keysToLoad -> keysToLoad.stream().collect(toImmutableMap(identity(), Int::negate))).join();
        assertThat(result).containsExactlyKeys(keys).inOrder();
    }

    @Test(dataProvider = "caches")
    @CacheSpec(population = {Population.SINGLETON, Population.PARTIAL, Population.FULL},
            removalListener = {Listener.DISABLED, Listener.REJECTING})
    public void getAllFunction_present_ordered_present(
            AsyncCache<Int, Int> cache, CacheContext context) {
        var keys = new ArrayList<>(context.original().keySet());
        Collections.shuffle(keys);
        var result = cache.getAll(keys, keysToLoad -> {
            throw new AssertionError();
        }).join();
        assertThat(result).containsExactlyKeys(keys).inOrder();
    }

    @Test(dataProvider = "caches")
    @CacheSpec(removalListener = {Listener.DISABLED, Listener.REJECTING})
    public void getAllFunction_present_ordered_exceeds(
            AsyncCache<Int, Int> cache, CacheContext context) {
        var keys = new ArrayList<>(context.original().keySet());
        keys.addAll(context.absentKeys());
        Collections.shuffle(keys);
        var result = cache.getAll(keys, keysToLoad -> {
            var moreKeys = new ArrayList<Int>(keysToLoad);
            for (int i = 0; i < 10; i++) {
                moreKeys.add(Int.valueOf(ThreadLocalRandom.current().nextInt()));
            }
            return moreKeys.stream().collect(toImmutableMap(identity(), Int::negate));
        }).join();
        assertThat(result).containsExactlyKeys(keys).inOrder();
    }

    @Test(dataProvider = "caches")
    @CacheSpec(removalListener = {Listener.DISABLED, Listener.REJECTING})
    public void getAllFunction_badLoader(AsyncCache<Int, Int> cache, CacheContext context) {
        assertThat(cache.getAll(context.absentKeys(), keysToLoad -> {
            throw new LoadAllException();
        }))
                .failsWith(CompletionException.class).hasCauseThat().isInstanceOf(LoadAllException.class);
        assertThat(cache).hasSize(context.initialSize());
    }

    @CheckNoStats
    @Test(dataProvider = "caches", expectedExceptions = NullPointerException.class)
    @CacheSpec(removalListener = {Listener.DISABLED, Listener.REJECTING})
    public void getAllBifunction_nullKeys(AsyncCache<Int, Int> cache, CacheContext context) {
        cache.getAll(null, (keys, executor) -> {
            throw new AssertionError();
        });
    }

    @CheckNoStats
    @Test(dataProvider = "caches", expectedExceptions = NullPointerException.class)
    @CacheSpec(removalListener = {Listener.DISABLED, Listener.REJECTING})
    public void getAllBifunction_nullKeys_nullBifunction(
            AsyncCache<Int, Int> cache, CacheContext context) {
        BiFunction<Set<? extends Int>, Executor, CompletableFuture<Map<Int, Int>>> f = null;
        cache.getAll(null, f);
    }

    @CheckNoStats
    @Test(dataProvider = "caches", expectedExceptions = NullPointerException.class)
    @CacheSpec(removalListener = {Listener.DISABLED, Listener.REJECTING})
    public void getAllBifunction_nullBifunction(AsyncCache<Int, Int> cache, CacheContext context) {
        BiFunction<Set<? extends Int>, Executor, CompletableFuture<Map<Int, Int>>> f = null;
        cache.getAll(context.original().keySet(), f);
    }

    @CheckNoStats
    @Test(dataProvider = "caches", expectedExceptions = NullPointerException.class)
    @CacheSpec(removalListener = {Listener.DISABLED, Listener.REJECTING})
    public void getAllBifunction_nullKey(AsyncCache<Int, Int> cache, CacheContext context) {
        cache.getAll(Collections.singletonList(null),
                (keys, executor) -> {
                    throw new AssertionError();
                });
    }

    @CheckNoStats
    @Test(dataProvider = "caches")
    @CacheSpec(removalListener = {Listener.DISABLED, Listener.REJECTING})
    public void getAllBifunction_iterable_empty(AsyncCache<Int, Int> cache, CacheContext context) {
        var result = cache.getAll(List.of(),
                (keys, executor) -> {
                    throw new AssertionError();
                }).join();
        assertThat(result).isExhaustivelyEmpty();
    }

    @CacheSpec
    @Test(dataProvider = "caches", expectedExceptions = UnsupportedOperationException.class)
    public void getAllBifunction_immutable_keys(AsyncCache<Int, Int> cache, CacheContext context) {
        cache.getAll(context.absentKeys(), (keys, executor) -> {
            keys.clear();
            return CompletableFuture.completedFuture(Map.of());
        });
    }

    @CacheSpec
    @Test(dataProvider = "caches", expectedExceptions = UnsupportedOperationException.class)
    public void getAllBifunction_immutable_result(AsyncCache<Int, Int> cache, CacheContext context) {
        var result = cache.getAll(context.absentKeys(), (keys, executor) -> CompletableFuture.completedFuture(
                keys.stream().collect(toImmutableMap(identity(), identity())))).join();
        result.clear();
    }

    @CacheSpec
    @Test(dataProvider = "caches")
    public void getAllBifunction_absent_null(AsyncCache<Int, Int> cache, CacheContext context) {
        var future = CompletableFuture.completedFuture((Map<Int, Int>) null);
        assertThat(cache.getAll(context.absentKeys(), (keys, executor) -> future))
                .failsWith(NullMapCompletionException.class);
    }

    @CacheSpec
    @Test(dataProvider = "caches", expectedExceptions = NullPointerException.class)
    public void getAllBifunction_absent_nullValue(AsyncCache<Int, Int> cache, CacheContext context) {
        cache.getAll(context.absentKeys(), (keys, executor) -> null);
    }

    @CacheSpec
    @Test(dataProvider = "caches")
    public void getAllBifunction_absent_failure(AsyncCache<Int, Int> cache, CacheContext context) {
        var future = cache.getAll(context.absentKeys(),
                (keys, executor) -> CompletableFuture.failedFuture(new IllegalStateException()));
        assertThat(future).failsWith(CompletionException.class)
                .hasCauseThat().isInstanceOf(IllegalStateException.class);
        assertThat(context).stats().hits(0).misses(context.absentKeys().size()).success(0).failures(1);
    }

    @CacheSpec
    @Test(dataProvider = "caches", expectedExceptions = IllegalStateException.class)
    public void getAllBifunction_absent_throwsException(
            AsyncCache<Int, Int> cache, CacheContext context) {
        try {
            cache.getAll(context.absentKeys(),
                    (keys, executor) -> {
                        throw new IllegalStateException();
                    });
        } finally {
            int misses = context.absentKeys().size();
            assertThat(context).stats().hits(0).misses(misses).success(0).failures(1);
            assertThat(cache).doesNotContainKey(context.absentKey());
        }
    }

    @CacheSpec
    @Test(dataProvider = "caches", expectedExceptions = UnknownError.class)
    public void getAllBifunction_absent_throwsError(AsyncCache<Int, Int> cache, CacheContext context) {
        try {
            cache.getAll(context.absentKeys(), (keys, executor) -> {
                throw new UnknownError();
            });
        } finally {
            int misses = context.absentKeys().size();
            assertThat(context).stats().hits(0).misses(misses).success(0).failures(1);
            assertThat(cache).doesNotContainKey(context.absentKey());
        }
    }

    @Test(dataProvider = "caches")
    @CacheSpec(removalListener = {Listener.DISABLED, Listener.REJECTING})
    public void getAllBifunction_absent(AsyncCache<Int, Int> cache, CacheContext context) {
        var future = cache.getAll(context.absentKeys(),
                (keys, executor) -> CompletableFuture.completedFuture(context.absent()));
        assertThat(future).succeedsWith(context.absent());
        int count = context.absentKeys().size();
        assertThat(context).stats().hits(0).misses(count).success(1).failures(0);
    }

    @Test(dataProvider = "caches")
    @CacheSpec(population = {Population.SINGLETON, Population.PARTIAL, Population.FULL},
            removalListener = {Listener.DISABLED, Listener.REJECTING})
    public void getAllBifunction_present_partial(AsyncCache<Int, Int> cache, CacheContext context) {
        var expect = new HashMap<Int, Int>();
        expect.put(context.firstKey(), context.firstKey().negate());
        expect.put(context.middleKey(), context.middleKey().negate());
        expect.put(context.lastKey(), context.lastKey().negate());
        var result = cache.getAll(expect.keySet(), (keys, executor) -> {
            assertThat(keys.size()).isLessThan(expect.size());
            return CompletableFuture.completedFuture(keys.stream().collect(toImmutableMap(identity(), Int::negate)));
        }).join();
        assertThat(result).containsExactlyEntriesIn(expect);
        assertThat(context).stats().hits(expect.size()).misses(0).success(0).failures(0);
    }

    @Test(dataProvider = "caches")
    @CacheSpec(removalListener = {Listener.DISABLED, Listener.REJECTING})
    public void getAllBifunction_exceeds(AsyncCache<Int, Int> cache, CacheContext context) {
        var result = cache.getAll(context.absentKeys(), (keys, executor) -> {
            var moreKeys = new ArrayList<Int>(keys);
            IntStream.range(0, 10).mapToObj(i -> Int.valueOf(ThreadLocalRandom.current().nextInt())).forEach(moreKeys::add);
            return CompletableFuture.completedFuture(
                    moreKeys.stream().collect(toImmutableMap(identity(), Int::negate)));
        }).join();
        assertThat(result).containsExactlyKeys(context.absentKeys());
        assertThat(cache).hasSizeGreaterThan(context.initialSize() + context.absentKeys().size());
        assertThat(context).stats().hits(0).misses(result.size()).success(1).failures(0);
    }

    @Test(dataProvider = "caches")
    @CacheSpec(removalListener = {Listener.DISABLED, Listener.REJECTING})
    public void getAllBifunction_different(AsyncCache<Int, Int> cache, CacheContext context) {
        var actual = context.absentKeys().stream().collect(toImmutableMap(Int::negate, identity()));
        var result = cache.getAll(context.absentKeys(), (keys, executor) -> CompletableFuture.completedFuture(actual)).join();
        assertThat(result).isEmpty();
        assertThat(cache).hasSize(context.initialSize() + actual.size());
        assertThat(cache.synchronous().asMap()).containsAtLeastEntriesIn(actual);
        assertThat(context).stats().hits(0).misses(actual.size()).success(1).failures(0);
    }

    @Test(dataProvider = "caches")
    @CacheSpec(population = {Population.SINGLETON, Population.PARTIAL, Population.FULL},
            removalListener = {Listener.DISABLED, Listener.REJECTING})
    public void getAllBifunction_duplicates(AsyncCache<Int, Int> cache, CacheContext context) {
        var absentKeys = ImmutableSet.copyOf(Iterables.limit(context.absentKeys(), Ints.saturatedCast(context.maximum().max() - context.initialSize())));
        var keys = Iterables.concat(absentKeys, absentKeys, context.original().keySet(), context.original().keySet());
        var result = cache.getAll(keys, (keysToLoad, executor) -> {
            assertThat(keysToLoad).containsNoDuplicates();
            return CompletableFuture.completedFuture(keysToLoad.stream().collect(toImmutableMap(identity(), Int::negate)));
        }).join();
        assertThat(result).containsExactlyKeys(keys);
        assertThat(context).stats().hits(context.initialSize()).misses(absentKeys.size()).success(1).failures(0);
    }

    @Test(dataProvider = "caches")
    @CacheSpec(population = {Population.SINGLETON, Population.PARTIAL, Population.FULL},
            removalListener = {Listener.DISABLED, Listener.REJECTING})
    public void getAllBifunction_present_ordered_absent(AsyncCache<Int, Int> cache, CacheContext context) {
        var keys = new ArrayList<>(context.absentKeys());
        Collections.shuffle(keys);
        var result = cache.getAll(keys, (keysToLoad, executor) -> {
            assertThat(keysToLoad).containsExactlyElementsIn(context.absentKeys());
            return CompletableFuture.completedFuture(keysToLoad.stream().collect(toImmutableMap(identity(), Int::negate)));
        }).join();
        assertThat(result).containsExactlyKeys(keys).inOrder();
    }

    @Test(dataProvider = "caches")
    @CacheSpec(population = {Population.SINGLETON, Population.PARTIAL},
            removalListener = {Listener.DISABLED, Listener.REJECTING})
    public void getAllBifunction_present_ordered_partial(
            AsyncCache<Int, Int> cache, CacheContext context) {
        var keys = new ArrayList<>(context.original().keySet());
        keys.addAll(context.absentKeys());
        Collections.shuffle(keys);
        var result = cache.getAll(keys, (keysToLoad, executor) -> {
            assertThat(keysToLoad).containsExactlyElementsIn(context.absentKeys());
            return CompletableFuture.completedFuture(
                    keysToLoad.stream().collect(toImmutableMap(identity(), Int::negate)));
        }).join();
        assertThat(result).containsExactlyKeys(keys).inOrder();
    }

    @Test(dataProvider = "caches")
    @CacheSpec(population = {Population.SINGLETON, Population.PARTIAL, Population.FULL}, removalListener = {Listener.DISABLED, Listener.REJECTING})
    public void getAllBifunction_present_ordered_present(AsyncCache<Int, Int> cache, CacheContext context) {
        var keys = new ArrayList<>(context.original().keySet());
        Collections.shuffle(keys);
        var result = cache.getAll(keys, (keysToLoad, executor) -> {
            throw new AssertionError();
        }).join();
        assertThat(result).containsExactlyKeys(keys).inOrder();
    }

    @Test(dataProvider = "caches")
    @CacheSpec(removalListener = {Listener.DISABLED, Listener.REJECTING})
    public void getAllBifunction_present_ordered_exceeds(
            AsyncCache<Int, Int> cache, CacheContext context) {
        var keys = new ArrayList<>(context.original().keySet());
        keys.addAll(context.absentKeys());
        Collections.shuffle(keys);
        var result = cache.getAll(keys, (keysToLoad, executor) -> {
            var moreKeys = new ArrayList<Int>(keysToLoad);
            IntStream.range(0, 10).mapToObj(i -> Int.valueOf(ThreadLocalRandom.current().nextInt())).forEach(moreKeys::add);
            return CompletableFuture.completedFuture(moreKeys.stream().collect(toImmutableMap(identity(), Int::negate)));
        }).join();
        assertThat(result).containsExactlyKeys(keys).inOrder();
    }

    @Test(dataProvider = "caches")
    @CacheSpec(removalListener = {Listener.DISABLED, Listener.REJECTING})
    public void getAllBifunction_badLoader(AsyncCache<Int, Int> cache, CacheContext context) {
        try {
            cache.getAll(context.absentKeys(), (keysToLoad, executor) -> {
                throw new LoadAllException();
            });
            Assert.fail();
        } catch (LoadAllException e) {
            assertThat(cache).hasSize(context.initialSize());
        }
    }

    @Test(dataProvider = "caches")
    @CacheSpec(removalListener = {Listener.DISABLED, Listener.REJECTING})
    public void getAllBifunction_early_success(AsyncCache<Int, Int> cache, CacheContext context) {
        var key = context.absentKeys().iterator().next();
        var value = Int.valueOf(Integer.MAX_VALUE);
        var bulk = new CompletableFuture<Map<Int, Int>>();
        var result = cache.getAll(context.absentKeys(), (keysToLoad, executor) -> bulk);
        var future = cache.asMap().get(key);
        future.complete(value);
        bulk.complete(context.absent());
        assertThat(future).succeedsWith(context.absent().get(key));
        assertThat(result.join()).containsExactlyEntriesIn(context.absent());
        assertThat(cache.synchronous().asMap()).containsAtLeastEntriesIn(context.absent());
    }

    @Test(dataProvider = "caches")
    @CacheSpec(removalListener = {Listener.DISABLED, Listener.REJECTING})
    public void getAllBifunction_early_failure(AsyncCache<Int, Int> cache, CacheContext context) {
        var key = context.absentKeys().iterator().next();
        var error = new IllegalStateException();
        var bulk = new CompletableFuture<Map<Int, Int>>();
        var result = cache.getAll(context.absentKeys(), (keysToLoad, executor) -> bulk);
        var future = cache.asMap().get(key);
        future.completeExceptionally(error);
        bulk.complete(context.absent());
        assertThat(future).succeedsWith(context.absent().get(key));
        assertThat(cache.synchronous().asMap()).containsAtLeastEntriesIn(context.absent());
        if (result.isCompletedExceptionally()) {
            assertThat(result).failsWith(CompletionException.class).hasCauseThat().isSameInstanceAs(error);
        } else {
            assertThat(result.join()).containsExactlyEntriesIn(context.absent());
        }
    }

    @CacheSpec(removalListener = {Listener.DISABLED, Listener.REJECTING})
    @Test(dataProvider = "caches", expectedExceptions = NullPointerException.class)
    public void put_nullKey(AsyncCache<Int, Int> cache, CacheContext context) {
        var value = context.absentValue().asFuture();
        cache.put(null, value);
    }

    @CacheSpec(removalListener = {Listener.DISABLED, Listener.REJECTING})
    @Test(dataProvider = "caches", expectedExceptions = NullPointerException.class)
    public void put_nullValue(AsyncCache<Int, Int> cache, CacheContext context) {
        cache.put(context.absentKey(), null);
    }

    @CacheSpec(removalListener = {Listener.DISABLED, Listener.REJECTING})
    @Test(dataProvider = "caches", expectedExceptions = NullPointerException.class)
    public void put_nullKeyAndValue(AsyncCache<Int, Int> cache, CacheContext context) {
        cache.put(null, null);
    }

    @Test(dataProvider = "caches")
    @CacheSpec(removalListener = {Listener.DISABLED, Listener.REJECTING})
    public void put_insert_failure_before(AsyncCache<Int, Int> cache, CacheContext context) {
        var failedFuture = CompletableFuture.completedFuture((Int) null);
        failedFuture.completeExceptionally(new IllegalStateException());
        cache.put(context.absentKey(), failedFuture);
        assertThat(cache).hasSize(context.initialSize());
        assertThat(cache).doesNotContainKey(context.absentKey());
    }

    @Test(dataProvider = "caches")
    @CacheSpec(removalListener = {Listener.DISABLED, Listener.REJECTING})
    public void put_insert_failure_after(AsyncCache<Int, Int> cache, CacheContext context) {
        var failedFuture = new CompletableFuture<Int>();

        cache.put(context.absentKey(), failedFuture);
        failedFuture.completeExceptionally(new IllegalStateException());
        assertThat(cache).doesNotContainKey(context.absentKey());
        assertThat(cache).hasSize(context.initialSize());
    }

    @Test(dataProvider = "caches")
    @CacheSpec(removalListener = {Listener.DISABLED, Listener.REJECTING})
    public void put_insert(AsyncCache<Int, Int> cache, CacheContext context) {
        var value = context.absentValue().asFuture();
        cache.put(context.absentKey(), value);
        assertThat(cache).hasSize(context.initialSize() + 1);
        assertThat(context).stats().hits(0).misses(0).success(1).failures(0);
        assertThat(cache).containsEntry(context.absentKey(), context.absentValue());
    }

    @Test(dataProvider = "caches")
    @CacheSpec(population = {Population.SINGLETON, Population.FULL},
            removalListener = {Listener.DISABLED, Listener.REJECTING})
    public void put_replace_failure_before(AsyncCache<Int, Int> cache, CacheContext context) {
        var failedFuture = CompletableFuture.completedFuture((Int) null);
        failedFuture.completeExceptionally(new IllegalStateException());

        cache.put(context.middleKey(), failedFuture);
        assertThat(cache).hasSize(context.initialSize() - 1);
        assertThat(cache).doesNotContainKey(context.absentKey());
    }

    @Test(dataProvider = "caches")
    @CacheSpec(population = {Population.SINGLETON, Population.FULL},
            removalListener = {Listener.DISABLED, Listener.REJECTING})
    public void put_replace_failure_after(AsyncCache<Int, Int> cache, CacheContext context) {
        var failedFuture = CompletableFuture.completedFuture((Int) null);

        cache.put(context.middleKey(), failedFuture);
        failedFuture.completeExceptionally(new IllegalStateException());
        assertThat(cache).doesNotContainKey(context.absentKey());
        assertThat(cache).hasSize(context.initialSize() - 1);
    }

    @Test(dataProvider = "caches")
    @CacheSpec(population = {Population.SINGLETON, Population.PARTIAL, Population.FULL})
    public void put_replace_nullValue(AsyncCache<Int, Int> cache, CacheContext context) {
        var removed = new HashMap<Int, Int>();
        var value = CompletableFuture.completedFuture((Int) null);
        for (Int key : context.firstMiddleLastKeys()) {
            cache.put(key, value);
            assertThat(cache).doesNotContainKey(key);
            removed.put(key, context.original().get(key));
        }
        int count = context.firstMiddleLastKeys().size();
        assertThat(cache).hasSize(context.initialSize() - count);
        assertThat(context).removalNotifications().withCause(EXPLICIT)
                .contains(removed).exclusively();
    }

    @Test(dataProvider = "caches")
    @CacheSpec(population = {Population.SINGLETON, Population.PARTIAL, Population.FULL})
    public void put_replace_differentValue(AsyncCache<Int, Int> cache, CacheContext context) {
        var replaced = new HashMap<Int, Int>();
        context.firstMiddleLastKeys().forEach(key -> {
            var newValue = context.absentValue().asFuture();
            cache.put(key, newValue);
            assertThat(cache).containsEntry(key, newValue);
            replaced.put(key, context.original().get(key));
        });
        assertThat(cache).hasSize(context.initialSize());
        assertThat(context).removalNotifications().withCause(REPLACED)
                .contains(replaced).exclusively();
    }

    @Test(dataProvider = "caches")
    @CacheSpec(population = Population.EMPTY, removalListener = Listener.MOCKITO)
    public void removalListener_nullValue(AsyncCache<Int, Int> cache, CacheContext context) {
        var future = new CompletableFuture<Int>();
        cache.put(context.absentKey(), future);
        future.complete(null);
        verify(context.removalListener(), never()).onRemoval(any(Int.class), any(Int.class), any(RemovalCause.class));
    }

    @CacheSpec
    @Test(dataProvider = "caches")
    public void serialize(AsyncCache<Int, Int> cache, CacheContext context) {
        assertThat(cache).isReserialize();
    }

    private static final class LoadAllException extends RuntimeException {
    }
}
