
package com.github.benmanes.caffeine.cache.testing;

import com.github.benmanes.caffeine.cache.RemovalCause;
import com.google.errorprone.annotations.Immutable;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.AbstractMap.SimpleImmutableEntry;

import static java.util.Objects.requireNonNull;


@Immutable(containerOf = {"K", "V"})
public final class RemovalNotification<K, V> extends SimpleImmutableEntry<K, V> {
    private static final long serialVersionUID = 1L;

    private final RemovalCause cause;

    public RemovalNotification(@Nullable K key, @Nullable V value, RemovalCause cause) {
        super(key, value);
        this.cause = requireNonNull(cause);
    }

    public RemovalCause getCause() {
        return cause;
    }

    public boolean wasEvicted() {
        return cause.wasEvicted();
    }

    @Override
    public String toString() {
        return getKey() + "=" + getValue() + " [" + cause + "]";
    }
}
