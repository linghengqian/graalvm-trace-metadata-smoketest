
package com.github.benmanes.caffeine.cache.testing;

import com.github.benmanes.caffeine.cache.Weigher;

import java.io.Serializable;
import java.util.concurrent.ThreadLocalRandom;

import static java.util.Objects.requireNonNull;

public final class Weighers {

    private Weighers() {
    }

    public static <K, V> Weigher<K, V> constant(int weight) {
        return new ConstantWeigher<>(weight);
    }

    @SuppressWarnings("unchecked")
    public static <K, V> Weigher<K, V> random() {
        return (Weigher<K, V>) RandomWeigher.INSTANCE;
    }

    static final class ConstantWeigher<K, V> implements Weigher<K, V>, Serializable {
        private static final long serialVersionUID = 1L;

        private final int weight;

        ConstantWeigher(int weight) {
            this.weight = weight;
        }

        @Override
        public int weigh(Object key, Object value) {
            requireNonNull(key);
            requireNonNull(value);
            return weight;
        }
    }

    enum RandomWeigher implements Weigher<Object, Object> {
        INSTANCE;

        @Override
        public int weigh(Object key, Object value) {
            requireNonNull(key);
            requireNonNull(value);
            return ThreadLocalRandom.current().nextInt(1, 10);
        }
    }
}
