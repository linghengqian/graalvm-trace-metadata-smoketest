
package com.github.benmanes.caffeine.cache.testing;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Reset;
import com.github.benmanes.caffeine.cache.Ticker;
import com.github.benmanes.caffeine.cache.testing.CacheSpec.*;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("PreferJavaTimeOverload")
public final class CaffeineCacheFromContext {
    interface SerializableTicker extends Ticker, Serializable {
    }

    private CaffeineCacheFromContext() {
    }

    public static <K, V> Cache<K, V> newCaffeineCache(CacheContext context) {
        Caffeine<Object, Object> builder = Caffeine.newBuilder();
        context.caffeine = builder;

        if (context.initialCapacity() != InitialCapacity.DEFAULT) {
            builder.initialCapacity(context.initialCapacity().size());
        }
        if (context.isRecordingStats()) {
            builder.recordStats();
        }
        if (context.maximum() != Maximum.DISABLED) {
            if (context.cacheWeigher() == CacheWeigher.DISABLED) {
                builder.maximumSize(context.maximum().max());
            } else {
                builder.weigher(context.weigher());
                builder.maximumWeight(context.maximumWeight());
            }
        }
        if (context.expiryType() != CacheExpiry.DISABLED) {
            builder.expireAfter(context.expiry());
        }
        if (context.expiresAfterAccess()) {
            builder.expireAfterAccess(context.expireAfterAccess().timeNanos(), TimeUnit.NANOSECONDS);
        }
        if (context.expiresAfterWrite()) {
            builder.expireAfterWrite(context.expireAfterWrite().timeNanos(), TimeUnit.NANOSECONDS);
        }
        if (context.refreshes()) {
            builder.refreshAfterWrite(context.refreshAfterWrite().timeNanos(), TimeUnit.NANOSECONDS);
        }
        if (context.expires() || context.refreshes()) {
            SerializableTicker ticker = context.ticker()::read;
            builder.ticker(ticker);
        }
        if (context.isWeakKeys()) {
            builder.weakKeys();
        } else if (context.keyStrength == ReferenceType.SOFT) {
            throw new IllegalStateException();
        }
        if (context.isWeakValues()) {
            builder.weakValues();
        } else if (context.isSoftValues()) {
            builder.softValues();
        }
        if (context.executorType() != CacheExecutor.DEFAULT) {
            builder.executor(context.executor());
        }
        if (context.cacheScheduler != CacheScheduler.DISABLED) {
            builder.scheduler(context.scheduler());
        }
        if (context.removalListenerType() != Listener.DISABLED) {
            builder.removalListener(context.removalListener());
        }
        if (context.evictionListenerType() != Listener.DISABLED) {
            builder.evictionListener(context.evictionListener());
        }
        if (context.isAsync()) {
            if (context.loader() == Loader.DISABLED) {
                context.asyncCache = builder.buildAsync();
            } else {
                context.asyncCache = builder.buildAsync(
                        context.isAsyncLoader() ? context.loader().async() : context.loader());
            }
            context.cache = context.asyncCache.synchronous();
        } else if (context.loader() == Loader.DISABLED) {
            context.cache = builder.build();
        } else {
            context.cache = builder.build(context.loader());
        }

        @SuppressWarnings("unchecked")
        var castedCache = (Cache<K, V>) context.cache;
        Reset.resetThreadLocalRandom();
        return castedCache;
    }
}
