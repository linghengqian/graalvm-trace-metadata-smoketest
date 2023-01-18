
package com.github.benmanes.caffeine.cache.testing;

import com.github.benmanes.caffeine.cache.Expiry;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

import java.io.Serializable;

import static java.util.Objects.requireNonNull;

public final class ExpiryBuilder {
    private static final int UNSET = -1;
    private final long createNanos;
    private long updateNanos;
    private long readNanos;

    private ExpiryBuilder(long createNanos) {
        this.createNanos = createNanos;
        this.updateNanos = UNSET;
        this.readNanos = UNSET;
    }

    public static ExpiryBuilder expiringAfterCreate(long nanos) {
        return new ExpiryBuilder(nanos);
    }

    @CanIgnoreReturnValue
    public ExpiryBuilder expiringAfterUpdate(long nanos) {
        updateNanos = nanos;
        return this;
    }

    @CanIgnoreReturnValue
    public ExpiryBuilder expiringAfterRead(long nanos) {
        readNanos = nanos;
        return this;
    }

    public <K, V> Expiry<K, V> build() {
        return new FixedExpiry<>(createNanos, updateNanos, readNanos);
    }

    private record FixedExpiry<K, V>(long createNanos, long updateNanos,
                                     long readNanos) implements Expiry<K, V>, Serializable {
        private static final long serialVersionUID = 1L;

        @Override
        public long expireAfterCreate(K key, V value, long currentTime) {
            requireNonNull(key);
            requireNonNull(value);
            return createNanos;
        }

        @Override
        public long expireAfterUpdate(K key, V value, long currentTime, long currentDuration) {
            requireNonNull(key);
            requireNonNull(value);
            return (updateNanos == UNSET) ? currentDuration : updateNanos;
        }

        @Override
        public long expireAfterRead(K key, V value, long currentTime, long currentDuration) {
            requireNonNull(key);
            requireNonNull(value);
            return (readNanos == UNSET) ? currentDuration : readNanos;
        }
    }
}
