
package com.github.benmanes.caffeine.cache;

import com.github.benmanes.caffeine.cache.LocalAsyncCache.AbstractCacheView;
import org.jctools.util.UnsafeAccess;

public final class Reset {
    static final long PROBE = UnsafeAccess.fieldOffset(Thread.class, "threadLocalRandomProbe");
    static final long SEED = UnsafeAccess.fieldOffset(Thread.class, "threadLocalRandomSeed");
    static final int RANDOM_PROBE = 0x9e3779b9;
    static final int RANDOM_SEED = 1033096058;

    private Reset() {
    }

    public static void resetThreadLocalRandom() {
        setThreadLocalRandom(RANDOM_PROBE, RANDOM_SEED);
    }

    public static void setThreadLocalRandom(int probe, int seed) {
        UnsafeAccess.UNSAFE.putInt(Thread.currentThread(), PROBE, probe);
        UnsafeAccess.UNSAFE.putLong(Thread.currentThread(), SEED, seed);
    }

    public static void destroy(Cache<?, ?> cache) {
        var local = (cache instanceof AbstractCacheView<?, ?>)
                ? ((AbstractCacheView<?, ?>) cache).asyncCache().cache()
                : (LocalCache<?, ?>) cache.asMap();
        if (local instanceof UnboundedLocalCache<?, ?> unbounded) {
            unbounded.data.clear();
        } else if (local instanceof BoundedLocalCache) {
            @SuppressWarnings("unchecked")
            var bounded = (BoundedLocalCache<Object, Object>) local;
            bounded.evictionLock.lock();
            try {
                for (var node : bounded.data.values()) {
                    destroyNode(bounded, node);
                }
                if (bounded.expiresVariable()) {
                    destroyTimerWheel(bounded);
                }
                bounded.data.clear();
            } finally {
                bounded.evictionLock.unlock();
            }
        }
    }

    @SuppressWarnings("GuardedBy")
    private static void destroyNode(BoundedLocalCache<?, ?> bounded, Node<?, ?> node) {
        if (bounded.expiresAfterAccess()) {
            node.setPreviousInAccessOrder(null);
            node.setNextInAccessOrder(null);
        }
        if (bounded.expiresAfterWrite()) {
            node.setPreviousInWriteOrder(null);
            node.setNextInWriteOrder(null);
        }
        if (bounded.expiresVariable()) {
            node.setPreviousInVariableOrder(null);
            node.setNextInVariableOrder(null);
        }
        node.die();
    }

    private static void destroyTimerWheel(BoundedLocalCache<Object, Object> bounded) {
        for (int i = 0; i < bounded.timerWheel().wheel.length; i++) {
            for (var sentinel : bounded.timerWheel().wheel[i]) {
                sentinel.setPreviousInVariableOrder(sentinel);
                sentinel.setNextInVariableOrder(sentinel);
            }
        }
    }
}
