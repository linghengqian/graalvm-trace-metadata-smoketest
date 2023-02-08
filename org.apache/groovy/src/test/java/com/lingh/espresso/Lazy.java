package com.lingh.espresso;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Represents a lazily computed value. Ensures that a single thread runs the computation.
 */
public final class Lazy<T> implements Supplier<T> {
    private volatile T ref;
    private final Supplier<T> supplier;

    private Lazy(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    /**
     * If the supplier returns <code>null</code>, {@link NullPointerException} is thrown. Exceptions
     * thrown by the supplier will be propagated. If the supplier returns a non-null object, it will
     * be cached and the computation is considered finished. The supplier is guaranteed to run on a
     * single thread. A successful computation ({@link Supplier#get()} returns a non-null object) is
     * guaranteed to be executed only once.
     *
     * @return the computed object, guaranteed to be non-null
     */
    @Override
    public T get() {
        T localRef = ref;
        if (localRef == null) {
            synchronized (this) {
                localRef = ref;
                if (localRef == null) {
                    localRef = Objects.requireNonNull(supplier.get());
                    ref = localRef;
                }
            }
        }
        return localRef;
    }

    /**
     * (Not so) Lazy value that does not run a computation.
     */
    public static <T> Lazy<T> value(T nonNullValue) {
        Lazy<T> result = new Lazy<T>(null);
        result.ref = Objects.requireNonNull(nonNullValue);
        return result;
    }

    /**
     * @param supplier if the supplier returns null, {@link #get()} will throw
     *            {@link NullPointerException}
     */
    public static <V> Lazy<V> of(Supplier<V> supplier) {
        return new Lazy<>(Objects.requireNonNull(supplier));
    }
}