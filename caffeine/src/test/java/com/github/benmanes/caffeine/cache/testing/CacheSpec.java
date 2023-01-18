
package com.github.benmanes.caffeine.cache.testing;

import com.github.benmanes.caffeine.cache.*;
import com.github.benmanes.caffeine.testing.ConcurrentTestHarness;
import com.github.benmanes.caffeine.testing.Int;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.testing.TestingExecutors;
import org.mockito.Mockito;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static com.github.benmanes.caffeine.cache.testing.CacheContext.intern;
import static com.github.benmanes.caffeine.testing.ConcurrentTestHarness.scheduledExecutor;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toUnmodifiableMap;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@SuppressWarnings("ImmutableEnumChecker")
@Target(METHOD)
@Retention(RUNTIME)
public @interface CacheSpec {
    Compute[] compute() default {
            Compute.ASYNC,
            Compute.SYNC
    };

    enum Compute {
        ASYNC,
        SYNC,
    }

    Implementation[] implementation() default {
            Implementation.Caffeine,
            Implementation.Guava,
    };

    enum Implementation {
        Caffeine,
        Guava
    }


    InitialCapacity[] initialCapacity() default {
            InitialCapacity.DEFAULT
    };

    enum InitialCapacity {
        DEFAULT(16),
        ZERO(0),
        ONE(1),
        FULL(50),
        EXCESSIVE(100);

        private final int size;

        InitialCapacity(int size) {
            this.size = size;
        }

        public int size() {
            return size;
        }
    }

    Stats[] stats() default {
            Stats.ENABLED,
            Stats.DISABLED
    };

    enum Stats {
        ENABLED,
        DISABLED
    }

    Maximum[] maximumSize() default {
            Maximum.DISABLED,
            Maximum.UNREACHABLE
    };

    enum Maximum {
        DISABLED(Long.MAX_VALUE),
        ZERO(0),
        ONE(1),
        TEN(10),
        ONE_FIFTY(150),
        FULL(InitialCapacity.FULL.size()),
        UNREACHABLE(Long.MAX_VALUE);

        private final long max;

        Maximum(long max) {
            this.max = max;
        }

        public long max() {
            return max;
        }
    }

    CacheWeigher[] weigher() default {
            CacheWeigher.DISABLED,
            CacheWeigher.ZERO,
            CacheWeigher.TEN
    };

    enum CacheWeigher {
        DISABLED(1),
        TEN(10),
        ZERO(0),
        NEGATIVE(-1),
        MAX_VALUE(Integer.MAX_VALUE),
        VALUE(() -> (key, value) -> Math.abs(((Int) value).intValue()), 1),
        COLLECTION(() -> (key, value) -> ((Collection<?>) value).size(), 1),
        RANDOM(Weighers::random, 1),
        @SuppressWarnings("unchecked")
        MOCKITO(() -> {
            var weigher = Mockito.mock(Weigher.class);
            when(weigher.weigh(any(), any())).thenReturn(1);
            return weigher;
        }, 1);

        private final Supplier<Weigher<Object, Object>> factory;
        private final int units;

        CacheWeigher(int units) {
            this.factory = () -> Weighers.constant(units);
            this.units = units;
        }

        CacheWeigher(Supplier<Weigher<Object, Object>> factory, int units) {
            this.factory = factory;
            this.units = units;
        }

        public int unitsPerEntry() {
            return units;
        }

        @SuppressWarnings("unchecked")
        public <K, V> Weigher<K, V> create() {
            return (Weigher<K, V>) factory.get();
        }
    }

    Expiration[] mustExpireWithAnyOf() default {};

    enum Expiration {
        AFTER_WRITE, AFTER_ACCESS, VARIABLE
    }

    Expire[] expireAfterAccess() default {
            Expire.DISABLED,
            Expire.FOREVER
    };

    Expire[] expireAfterWrite() default {
            Expire.DISABLED,
            Expire.FOREVER
    };

    Expire[] refreshAfterWrite() default {
            Expire.DISABLED,
            Expire.FOREVER
    };

    CacheExpiry[] expiry() default {
            CacheExpiry.DISABLED,
            CacheExpiry.ACCESS
    };

    Expire expiryTime() default Expire.FOREVER;

    enum CacheExpiry {
        DISABLED {
            @Override
            public <K, V> Expiry<K, V> createExpiry(Expire expiryTime) {
                return null;
            }
        },
        MOCKITO {
            @Override
            public <K, V> Expiry<K, V> createExpiry(Expire expiryTime) {
                @SuppressWarnings("unchecked")
                Expiry<K, V> mock = Mockito.mock(Expiry.class);
                when(mock.expireAfterCreate(any(), any(), anyLong()))
                        .thenReturn(expiryTime.timeNanos());
                when(mock.expireAfterUpdate(any(), any(), anyLong(), anyLong()))
                        .thenReturn(expiryTime.timeNanos());
                when(mock.expireAfterRead(any(), any(), anyLong(), anyLong()))
                        .thenReturn(expiryTime.timeNanos());
                return mock;
            }
        },
        CREATE {
            @Override
            public <K, V> Expiry<K, V> createExpiry(Expire expiryTime) {
                return ExpiryBuilder
                        .expiringAfterCreate(expiryTime.timeNanos())
                        .build();
            }
        },
        WRITE {
            @Override
            public <K, V> Expiry<K, V> createExpiry(Expire expiryTime) {
                return ExpiryBuilder
                        .expiringAfterCreate(expiryTime.timeNanos())
                        .expiringAfterUpdate(expiryTime.timeNanos())
                        .build();
            }
        },
        ACCESS {
            @Override
            public <K, V> Expiry<K, V> createExpiry(Expire expiryTime) {
                return ExpiryBuilder
                        .expiringAfterCreate(expiryTime.timeNanos())
                        .expiringAfterUpdate(expiryTime.timeNanos())
                        .expiringAfterRead(expiryTime.timeNanos())
                        .build();
            }
        };

        public abstract <K, V> Expiry<K, V> createExpiry(Expire expiryTime);
    }

    enum Expire {
        DISABLED(Long.MIN_VALUE),
        IMMEDIATELY(0),
        ONE_MILLISECOND(TimeUnit.MILLISECONDS.toNanos(1)),
        ONE_MINUTE(TimeUnit.MINUTES.toNanos(1)),
        FOREVER(Long.MAX_VALUE);

        private final long timeNanos;

        Expire(long timeNanos) {
            this.timeNanos = timeNanos;
        }

        public long timeNanos() {
            return timeNanos;
        }
    }

    boolean requiresWeakOrSoft() default false;

    ReferenceType[] keys() default {
            ReferenceType.STRONG,
            ReferenceType.WEAK
    };

    ReferenceType[] values() default {
            ReferenceType.STRONG,
            ReferenceType.WEAK,
            ReferenceType.SOFT
    };

    enum ReferenceType {
        STRONG,
        WEAK,
        SOFT
    }

    Listener[] removalListener() default {
            Listener.CONSUMING,
            Listener.DISABLED,
    };

    Listener[] evictionListener() default {
            Listener.CONSUMING,
            Listener.DISABLED,
    };

    @SuppressWarnings("unchecked")
    enum Listener {
        DISABLED(() -> null),
        REJECTING(RemovalListeners::rejecting),
        CONSUMING(RemovalListeners::consuming),
        MOCKITO(() -> Mockito.mock(RemovalListener.class));

        private final Supplier<RemovalListener<Object, Object>> factory;

        Listener(Supplier<RemovalListener<Object, Object>> factory) {
            this.factory = factory;
        }

        public <K, V> RemovalListener<K, V> create() {
            return (RemovalListener<K, V>) factory.get();
        }
    }

    Loader[] loader() default {
            Loader.DISABLED,
            Loader.NEGATIVE,
    };

    enum Loader implements CacheLoader<Int, Int> {
        DISABLED {
            @Override
            public Int load(Int key) {
                throw new AssertionError();
            }
        },
        NULL {
            @Override
            public Int load(Int key) {
                return null;
            }
        },
        IDENTITY {
            @Override
            public Int load(Int key) {
                requireNonNull(key);
                return intern(key);
            }
        },
        NEGATIVE {
            @Override
            public Int load(Int key) {

                return intern(key, k -> new Int(-k.intValue()));
            }
        },
        EXCEPTIONAL {
            @Override
            public Int load(Int key) {
                throw new IllegalStateException();
            }
        },
        CHECKED_EXCEPTIONAL {
            @Override
            public Int load(Int key) throws ExecutionException {
                throw new ExecutionException(null);
            }
        },
        INTERRUPTED {
            @Override
            public Int load(Int key) throws InterruptedException {
                throw new InterruptedException();
            }
        },
        BULK_NULL {
            @Override
            public Int load(Int key) {
                throw new UnsupportedOperationException();
            }

            @SuppressWarnings("ReturnsNullCollection")
            @Override
            public Map<Int, Int> loadAll(Set<? extends Int> keys) {
                return null;
            }
        },
        BULK_IDENTITY {
            @Override
            public Int load(Int key) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Map<Int, Int> loadAll(Set<? extends Int> keys) {
                var result = new HashMap<Int, Int>(keys.size());
                for (Int key : keys) {
                    result.put(key, key);
                    intern(key);
                }
                return result;
            }
        },
        BULK_NEGATIVE {
            @Override
            public Int load(Int key) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Map<Int, Int> loadAll(Set<? extends Int> keys) throws Exception {
                var result = new HashMap<Int, Int>(keys.size());
                for (Int key : keys) {
                    result.put(key, NEGATIVE.load(key));
                    intern(key);
                }
                return result;
            }
        },
        BULK_DIFFERENT {
            @Override
            public Int load(Int key) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Map<Int, Int> loadAll(Set<? extends Int> keys) {
                return keys.stream().collect(toUnmodifiableMap(
                        key -> intern(intern(key).negate()), identity()));
            }
        },
        BULK_NEGATIVE_EXCEEDS {
            @Override
            public Int load(Int key) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Map<? extends Int, ? extends Int> loadAll(
                    Set<? extends Int> keys) throws Exception {
                var moreKeys = new LinkedHashSet<Int>(keys.size() + 10);
                moreKeys.addAll(keys);
                IntStream.range(0, 10).mapToObj(i -> Int.valueOf(ThreadLocalRandom.current().nextInt())).forEach(moreKeys::add);
                return BULK_NEGATIVE.loadAll(moreKeys);
            }
        },
        BULK_EXCEPTIONAL {
            @Override
            public Int load(Int key) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Map<Int, Int> loadAll(Set<? extends Int> keys) {
                throw new IllegalStateException();
            }
        },
        BULK_CHECKED_EXCEPTIONAL {
            @Override
            public Int load(Int key) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Map<Int, Int> loadAll(Set<? extends Int> keys) throws ExecutionException {
                throw new ExecutionException(null);
            }
        },
        BULK_INTERRUPTED {
            @Override
            public Int load(Int key) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Map<Int, Int> loadAll(Set<? extends Int> keys) throws InterruptedException {
                throw new InterruptedException();
            }
        },
        BULK_MODIFY_KEYS {
            @Override
            public Int load(Int key) {
                return key;
            }

            @Override
            public Map<Int, Int> loadAll(Set<? extends Int> keys) {
                keys.clear();
                return Map.of();
            }
        },
        ASYNC_EXCEPTIONAL {
            @Override
            public Int load(Int key) {
                throw new UnsupportedOperationException();
            }

            @Override
            public CompletableFuture<Int> asyncLoad(Int key, Executor executor) {
                throw new IllegalStateException();
            }
        },
        ASYNC_CHECKED_EXCEPTIONAL {
            @Override
            public Int load(Int key) {
                throw new UnsupportedOperationException();
            }

            @Override
            public CompletableFuture<Int> asyncLoad(
                    Int key, Executor executor) throws ExecutionException {
                throw new ExecutionException(null);
            }
        },
        ASYNC_INCOMPLETE {
            @Override
            public Int load(Int key) {
                throw new UnsupportedOperationException();
            }

            @Override
            public CompletableFuture<Int> asyncLoad(Int key, Executor executor) {
                executor.execute(() -> {
                });
                return new CompletableFuture<>();
            }

            @Override
            public CompletableFuture<Int> asyncReload(
                    Int key, Int oldValue, Executor executor) {
                executor.execute(() -> {
                });
                return new CompletableFuture<>();
            }
        },
        ASYNC_INTERRUPTED {
            @Override
            public Int load(Int key) {
                throw new UnsupportedOperationException();
            }

            @Override
            public CompletableFuture<Int> asyncLoad(
                    Int key, Executor executor) throws InterruptedException {
                throw new InterruptedException();
            }
        },
        ASYNC_BULK_EXCEPTIONAL {
            @Override
            public Int load(Int key) {
                throw new UnsupportedOperationException();
            }

            @Override
            public CompletableFuture<Map<Int, Int>> asyncLoadAll(
                    Set<? extends Int> keys, Executor executor) {
                throw new IllegalStateException();
            }
        },
        ASYNC_BULK_CHECKED_EXCEPTIONAL {
            @Override
            public Int load(Int key) {
                throw new UnsupportedOperationException();
            }

            @Override
            public CompletableFuture<Map<Int, Int>> asyncLoadAll(
                    Set<? extends Int> keys, Executor executor) throws ExecutionException {
                throw new ExecutionException(null);
            }
        },
        ASYNC_BULK_MODIFY_KEYS {
            @Override
            public Int load(Int key) {
                throw new UnsupportedOperationException();
            }

            @Override
            public CompletableFuture<Map<Int, Int>> asyncLoadAll(
                    Set<? extends Int> keys, Executor executor) {
                keys.clear();
                return CompletableFuture.completedFuture(Map.of());
            }
        },
        ASYNC_BULK_INTERRUPTED {
            @Override
            public Int load(Int key) {
                throw new UnsupportedOperationException();
            }

            @Override
            public CompletableFuture<Map<Int, Int>> asyncLoadAll(
                    Set<? extends Int> keys, Executor executor) throws InterruptedException {
                throw new InterruptedException();
            }
        },

        REFRESH_EXCEPTIONAL {
            @Override
            public Int load(Int key) {
                throw new IllegalStateException();
            }

            @Override
            public CompletableFuture<Int> asyncLoad(Int key, Executor executor) {
                throw new IllegalStateException();
            }

            @Override
            public CompletableFuture<Int> asyncReload(
                    Int key, Int oldValue, Executor executor) {
                throw new IllegalStateException();
            }
        },
        REFRESH_CHECKED_EXCEPTIONAL {
            @Override
            public Int load(Int key) throws ExecutionException {
                throw new ExecutionException(null);
            }

            @Override
            public CompletableFuture<Int> asyncLoad(
                    Int key, Executor executor) throws ExecutionException {
                throw new ExecutionException(null);
            }

            @Override
            public CompletableFuture<Int> asyncReload(
                    Int key, Int oldValue, Executor executor) throws ExecutionException {
                throw new ExecutionException(null);
            }
        },
        REFRESH_INTERRUPTED {
            @Override
            public Int load(Int key) throws InterruptedException {
                throw new InterruptedException();
            }

            @Override
            public CompletableFuture<Int> asyncLoad(
                    Int key, Executor executor) throws InterruptedException {
                throw new InterruptedException();
            }

            @Override
            public CompletableFuture<Int> asyncReload(
                    Int key, Int oldValue, Executor executor) throws InterruptedException {
                throw new InterruptedException();
            }
        };

        private final boolean bulk;
        private final AsyncCacheLoader<Int, Int> asyncLoader;

        Loader() {
            bulk = name().contains("BULK");
            asyncLoader = bulk
                    ? new BulkSeriazableAsyncCacheLoader(this)
                    : new SeriazableAsyncCacheLoader(this);
        }

        public boolean isBulk() {
            return bulk;
        }

        public AsyncCacheLoader<Int, Int> async() {
            return asyncLoader;
        }

        private static class SeriazableAsyncCacheLoader
                implements AsyncCacheLoader<Int, Int>, Serializable {
            private static final long serialVersionUID = 1L;

            final Loader loader;

            SeriazableAsyncCacheLoader(Loader loader) {
                this.loader = loader;
            }

            @Override
            public CompletableFuture<? extends Int> asyncLoad(
                    Int key, Executor executor) throws Exception {
                return loader.asyncLoad(key, executor);
            }

            private Object readResolve() {
                return loader.asyncLoader;
            }
        }

        private static final class BulkSeriazableAsyncCacheLoader extends SeriazableAsyncCacheLoader {
            private static final long serialVersionUID = 1L;

            BulkSeriazableAsyncCacheLoader(Loader loader) {
                super(loader);
            }

            @Override
            public CompletableFuture<Int> asyncLoad(Int key, Executor executor) {
                throw new IllegalStateException();
            }

            @Override
            public CompletableFuture<? extends Map<? extends Int, ? extends Int>> asyncLoadAll(
                    Set<? extends Int> keys, Executor executor) throws Exception {
                return loader.asyncLoadAll(keys, executor);
            }
        }
    }

    CacheExecutor[] executor() default {
            CacheExecutor.DIRECT,
    };

    ExecutorFailure executorFailure() default ExecutorFailure.DISALLOWED;

    enum ExecutorFailure {
        EXPECTED, DISALLOWED, IGNORED
    }

    enum CacheExecutor {
        DEFAULT(() -> null),
        DIRECT(() -> new TrackingExecutor(MoreExecutors.newDirectExecutorService())),
        DISCARDING(() -> new TrackingExecutor(TestingExecutors.noOpScheduledExecutor())),
        THREADED(() -> new TrackingExecutor(ConcurrentTestHarness.executor)),
        REJECTING(() -> new TrackingExecutor(new ForkJoinPool() {
            @Override
            public void execute(Runnable task) {
                throw new RejectedExecutionException();
            }
        }));

        private final Supplier<TrackingExecutor> executor;

        CacheExecutor(Supplier<TrackingExecutor> executor) {
            this.executor = requireNonNull(executor);
        }

        public TrackingExecutor create() {
            return executor.get();
        }
    }

    CacheScheduler[] scheduler() default {
            CacheScheduler.DISABLED,
    };

    enum CacheScheduler {
        DISABLED(() -> null),
        SYSTEM(Scheduler::systemScheduler),
        THREADED(() -> Scheduler.forScheduledExecutorService(scheduledExecutor)),
        MOCKITO(() -> Mockito.mock(Scheduler.class));

        private final Supplier<Scheduler> scheduler;

        CacheScheduler(Supplier<Scheduler> scheduler) {
            this.scheduler = requireNonNull(scheduler);
        }

        public Scheduler create() {
            return scheduler.get();
        }
    }

    Population[] population() default {
            Population.EMPTY,
            Population.SINGLETON,
            Population.PARTIAL,
            Population.FULL
    };

    enum Population {
        EMPTY(0),
        SINGLETON(1),
        PARTIAL(InitialCapacity.FULL.size() / 2),
        FULL(InitialCapacity.FULL.size());
        private final long size;

        Population(long size) {
            this.size = size;
        }

        public long size() {
            return size;
        }
    }
}
