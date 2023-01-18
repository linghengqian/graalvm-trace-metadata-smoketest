package com.github.benmanes.caffeine.cache.testing;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;

import java.util.Map;
import java.util.concurrent.Future;

import static com.github.benmanes.caffeine.cache.LocalCacheSubject.asyncLocal;
import static com.github.benmanes.caffeine.cache.ReserializableSubject.asyncReserializable;
import static com.github.benmanes.caffeine.cache.testing.CacheSubject.cache;
import static com.github.benmanes.caffeine.testing.MapSubject.map;
import static com.google.common.truth.Truth.assertAbout;

public final class AsyncCacheSubject extends Subject {
    private final AsyncCache<?, ?> actual;

    private AsyncCacheSubject(FailureMetadata metadata, AsyncCache<?, ?> subject) {
        super(metadata, subject);
        this.actual = subject;
    }

    public static Factory<AsyncCacheSubject, AsyncCache<?, ?>> asyncCache() {
        return AsyncCacheSubject::new;
    }

    public static AsyncCacheSubject assertThat(AsyncCache<?, ?> actual) {
        return assertAbout(asyncCache()).that(actual);
    }

    public void isEmpty() {
        check("cache").about(map()).that(actual.asMap()).isExhaustivelyEmpty();
        check("cache").about(cache()).that(actual.synchronous()).isEmpty();
    }

    public void hasSize(long expectedSize) {
        check("estimatedSize()").about(cache()).that(actual.synchronous()).hasSize(expectedSize);
    }

    public void hasSizeLessThan(long other) {
        check("estimatedSize()").about(cache()).that(actual.synchronous()).hasSizeLessThan(other);
    }


    public void hasSizeGreaterThan(long other) {
        check("estimatedSize()").about(cache()).that(actual.synchronous()).hasSizeGreaterThan(other);
    }


    public void containsKey(Object key) {
        check("cache").that(actual.asMap()).containsKey(key);
    }


    public void doesNotContainKey(Object key) {
        check("cache").that(actual.asMap()).doesNotContainKey(key);
    }


    public void containsValue(Object value) {
        if (value instanceof Future<?>) {
            check("cache").about(map()).that(actual.asMap()).containsValue(value);
        } else {
            check("cache").about(cache()).that(actual.synchronous()).containsValue(value);
        }
    }

    public void containsEntry(Object key, Object value) {
        if (value instanceof Future<?>) {
            check("cache").that(actual.asMap()).containsEntry(key, value);
        } else {
            check("cache").about(cache()).that(actual.synchronous()).containsEntry(key, value);
        }
    }

    public void containsExactlyEntriesIn(Map<?, ?> expectedMap) {
        if (expectedMap.values().stream().anyMatch(value -> value instanceof Future<?>)) {
            check("cache").that(actual.asMap()).containsExactlyEntriesIn(expectedMap);
        } else {
            check("cache").about(cache())
                    .that(actual.synchronous()).containsExactlyEntriesIn(expectedMap);
        }
    }

    public void isReserialize() {
        check("reserializable").about(asyncReserializable()).that(actual).isReserialize();
    }

    public void isValid() {
        check("cache").about(asyncLocal()).that(actual).isValid();
    }
}
