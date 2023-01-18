
package com.github.benmanes.caffeine.cache.testing;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.testing.CacheSpec.*;
import com.github.benmanes.caffeine.testing.Int;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.mockito.Mockito;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class CacheGenerator {
    private static final ImmutableList<Map.Entry<Int, Int>> INTS = makeInts();
    private static final int BASE = 1_000;
    private final Options options;
    private final CacheSpec cacheSpec;
    private final boolean isAsyncOnly;
    private final boolean isLoadingOnly;
    private final boolean isGuavaCompatible;

    public CacheGenerator(CacheSpec cacheSpec) {
        this(cacheSpec, Options.fromSystemProperties(), false,  false,  true);
    }

    public CacheGenerator(CacheSpec cacheSpec, Options options,
                          boolean isLoadingOnly, boolean isAsyncOnly, boolean isGuavaCompatible) {
        this.isGuavaCompatible = isGuavaCompatible;
        this.isLoadingOnly = isLoadingOnly;
        this.isAsyncOnly = isAsyncOnly;
        this.cacheSpec = cacheSpec;
        this.options = options;
    }

    public Stream<CacheContext> generate() {
        return combinations().stream().map(this::newCacheContext).filter(this::isCompatible);
    }


    public static void initialize(CacheContext context) {
        populate(context, newCache(context));
    }

    @SuppressWarnings("IdentityConversion")
    private Set<List<Object>> combinations() {
        var asyncLoader = ImmutableSet.of(true, false);
        var loaders = ImmutableSet.copyOf(cacheSpec.loader());
        var keys = filterTypes(options.keys(), cacheSpec.keys());
        var values = filterTypes(options.values(), cacheSpec.values());
        var statistics = filterTypes(options.stats(), cacheSpec.stats());
        var computations = filterTypes(options.compute(), cacheSpec.compute());
        var implementations = filterTypes(options.implementation(), cacheSpec.implementation());
        if (isAsyncOnly) {
            values = values.contains(ReferenceType.STRONG)
                    ? ImmutableSet.of(ReferenceType.STRONG)
                    : ImmutableSet.of();
            computations = Sets.intersection(computations, Set.of(Compute.ASYNC));
        }
        if (!isGuavaCompatible || isAsyncOnly || computations.equals(ImmutableSet.of(Compute.ASYNC))) {
            implementations = Sets.difference(implementations, Set.of(Implementation.Guava));
        }
        if (computations.equals(ImmutableSet.of(Compute.SYNC))) {
            asyncLoader = ImmutableSet.of(false);
        }
        if (isLoadingOnly) {
            loaders = Sets.difference(loaders, Set.of(Loader.DISABLED)).immutableCopy();
        }
        if (computations.isEmpty() || implementations.isEmpty()
                || keys.isEmpty() || values.isEmpty()) {
            return ImmutableSet.of();
        }
        return Sets.cartesianProduct(
                ImmutableSet.copyOf(cacheSpec.initialCapacity()),
                ImmutableSet.copyOf(statistics),
                ImmutableSet.copyOf(cacheSpec.weigher()),
                ImmutableSet.copyOf(cacheSpec.maximumSize()),
                ImmutableSet.copyOf(cacheSpec.expiry()),
                ImmutableSet.copyOf(cacheSpec.expireAfterAccess()),
                ImmutableSet.copyOf(cacheSpec.expireAfterWrite()),
                ImmutableSet.copyOf(cacheSpec.refreshAfterWrite()),
                ImmutableSet.copyOf(keys),
                ImmutableSet.copyOf(values),
                ImmutableSet.copyOf(cacheSpec.executor()),
                ImmutableSet.copyOf(cacheSpec.scheduler()),
                ImmutableSet.copyOf(cacheSpec.removalListener()),
                ImmutableSet.copyOf(cacheSpec.evictionListener()),
                ImmutableSet.copyOf(cacheSpec.population()),
                ImmutableSet.copyOf(asyncLoader),
                ImmutableSet.copyOf(computations),
                ImmutableSet.copyOf(loaders),
                ImmutableSet.copyOf(implementations));
    }

    private static <T> Set<T> filterTypes(Optional<T> type, T[] options) {
        return type.isPresent()
                ? Sets.intersection(Set.of(options), Set.of(type.orElseThrow()))
                : ImmutableSet.copyOf(options);
    }

    private CacheContext newCacheContext(List<Object> combination) {
        int index = 0;
        return new CacheContext(
                (InitialCapacity) combination.get(index++),
                (Stats) combination.get(index++),
                (CacheWeigher) combination.get(index++),
                (Maximum) combination.get(index++),
                (CacheExpiry) combination.get(index++),
                (Expire) combination.get(index++),
                (Expire) combination.get(index++),
                (Expire) combination.get(index++),
                (ReferenceType) combination.get(index++),
                (ReferenceType) combination.get(index++),
                (CacheExecutor) combination.get(index++),
                (CacheScheduler) combination.get(index++),
                (Listener) combination.get(index++),
                (Listener) combination.get(index++),
                (Population) combination.get(index++),
                (Boolean) combination.get(index++),
                (Compute) combination.get(index++),
                (Loader) combination.get(index++),
                (Implementation) combination.get(index++),
                cacheSpec);
    }

    private boolean isCompatible(CacheContext context) {
        boolean asyncIncompatible = context.isAsync()
                && (!context.isCaffeine() || !context.isStrongValues());
        boolean asyncLoaderIncompatible = context.isAsyncLoader()
                && (!context.isAsync() || !context.isLoading());
        boolean refreshIncompatible = context.refreshes() && !context.isLoading();
        boolean weigherIncompatible = (context.maximum() == Maximum.DISABLED) && context.isWeighted();
        boolean referenceIncompatible = cacheSpec.requiresWeakOrSoft()
                && context.isStrongKeys() && context.isStrongValues();
        boolean expiryIncompatible = (context.expiryType() != CacheExpiry.DISABLED)
                && (!context.isCaffeine()
                || (context.expireAfterAccess() != Expire.DISABLED)
                || (context.expireAfterWrite() != Expire.DISABLED));
        boolean expirationIncompatible = (cacheSpec.mustExpireWithAnyOf().length > 0)
                && Arrays.stream(cacheSpec.mustExpireWithAnyOf()).noneMatch(context::expires);
        boolean schedulerIgnored = (context.cacheScheduler != CacheScheduler.DISABLED)
                && (!context.expires() || context.isGuava());
        boolean evictionListenerIncompatible = (context.evictionListenerType() != Listener.DISABLED)
                && (!context.isCaffeine() || (context.isAsync() && context.isWeakKeys()));

        boolean skip = asyncIncompatible || asyncLoaderIncompatible || evictionListenerIncompatible
                || refreshIncompatible || weigherIncompatible || expiryIncompatible
                || expirationIncompatible || referenceIncompatible || schedulerIgnored;
        return !skip;
    }

    private static <K, V> Cache<K, V> newCache(CacheContext context) {
        if (context.isCaffeine()) {
            return CaffeineCacheFromContext.newCaffeineCache(context);
        } else if (context.isGuava()) {
            return GuavaCacheFromContext.newGuavaCache(context);
        }
        throw new IllegalStateException();
    }

    @SuppressWarnings({"PreferJavaTimeOverload", "unchecked"})
    private static void populate(CacheContext context, Cache<Int, Int> cache) {
        if (context.population.size() == 0) {
            return;
        }
        int maximum = (int) Math.min(context.maximumSize(), context.population.size());
        int first = BASE + (int) Math.min(0, context.population.size());
        int last = BASE + maximum - 1;
        int middle = Math.max(first, BASE + ((last - first) / 2));
        IntStream.range(0, maximum).forEach(i -> {
            Map.Entry<Int, Int> entry = INTS.get(i);
            var key = context.isStrongKeys() ? entry.getKey() : new Int(BASE + i);
            var value = context.isStrongValues() ? entry.getValue() : new Int(-key.intValue());
            if (key.intValue() == first) {
                context.firstKey = key;
            }
            if (key.intValue() == middle) {
                context.middleKey = key;
            }
            if (key.intValue() == last) {
                context.lastKey = key;
            }
            cache.put(key, value);
            context.original.put(key, value);
        });
        if (context.executorType() != CacheExecutor.DIRECT) {
            cache.cleanUp();
        }
        if (context.expiryType() == CacheExpiry.MOCKITO) {
            Mockito.clearInvocations(context.expiry());
        }
    }

    private static ImmutableList<Map.Entry<Int, Int>> makeInts() {
        int size = Stream.of(Population.values()).mapToInt(population -> Math.toIntExact(population.size())).max().getAsInt();
        var builder = new ImmutableList.Builder<Map.Entry<Int, Int>>();
        IntStream.range(0, size).mapToObj(i -> Int.valueOf(BASE + i)).map(value -> Map.entry(value, value.negate())).forEach(builder::add);
        return builder.build();
    }
}
