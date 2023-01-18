
package com.github.benmanes.caffeine.cache.buffer;

import com.google.errorprone.annotations.concurrent.GuardedBy;
import org.jctools.queues.MessagePassingQueue;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class ReadBuffer<E> {
    public static final int FULL = 1;
    public static final int FAILED = -1;
    public static final int SUCCESS = 0;

    public static final int BUFFER_SIZE = 16;
    public static final int BUFFER_MASK = BUFFER_SIZE - 1;

    final Consumer<E> consumer = any -> {
    };
    final Lock lock = new ReentrantLock();

    public abstract long reads();

    public abstract long writes();

    public abstract int offer(E e);

    @GuardedBy("lock")
    protected abstract void drainTo(Consumer<E> consumer);

    public void drain() {
        if (lock.tryLock()) {
            try {
                drainTo(consumer);
            } finally {
                lock.unlock();
            }
        }
    }

    public interface Consumer<E> extends java.util.function.Consumer<E>, MessagePassingQueue.Consumer<E> {
    }
}
