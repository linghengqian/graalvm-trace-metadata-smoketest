
package com.github.benmanes.caffeine.cache.testing;

import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.RejectedExecutionException;

import static java.util.Objects.requireNonNull;


public final class RemovalListeners {

    private RemovalListeners() {
    }


    public static <K, V> ConsumingRemovalListener<K, V> consuming() {
        return new ConsumingRemovalListener<>();
    }

    public static <K, V> RemovalListener<K, V> rejecting() {
        return new RejectingRemovalListener<>();
    }

    private static void validate(Object key, Object value, RemovalCause cause) {
        if (cause != RemovalCause.COLLECTED) {
            requireNonNull(key);
            requireNonNull(value);
        }
        requireNonNull(cause);
    }

    public static final class RejectingRemovalListener<K, V>
            implements RemovalListener<K, V>, Serializable {
        private static final long serialVersionUID = 1L;
        public boolean reject = true;
        public int rejected;

        @Override
        public void onRemoval(K key, V value, RemovalCause cause) {
            validate(key, value, cause);
            if (reject) {
                rejected++;
                throw new RejectedExecutionException("Rejected eviction of " +
                        new RemovalNotification<>(key, value, cause));
            }
        }
    }

    public static final class ConsumingRemovalListener<K, V>
            implements RemovalListener<K, V>, Serializable {
        private static final long serialVersionUID = 1L;
        private final CopyOnWriteArrayList<RemovalNotification<K, V>> removed;

        public ConsumingRemovalListener() {
            this.removed = new CopyOnWriteArrayList<>();
        }

        @Override
        public void onRemoval(K key, V value, RemovalCause cause) {
            validate(key, value, cause);
            removed.add(new RemovalNotification<>(key, value, cause));
        }

        public List<RemovalNotification<K, V>> removed() {
            return removed;
        }
    }
}
