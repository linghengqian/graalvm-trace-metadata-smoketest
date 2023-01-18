
package com.github.benmanes.caffeine.cache.testing;

import com.google.common.util.concurrent.ForwardingExecutorService;
import com.google.common.util.concurrent.Uninterruptibles;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Objects.requireNonNull;

public final class TrackingExecutor extends ForwardingExecutorService {
    private static final CountDownLatch ZERO = new CountDownLatch(0);

    private final ExecutorService delegate;
    private final AtomicInteger submitted;
    private final AtomicInteger completed;
    private final AtomicInteger failed;

    private volatile CountDownLatch latch;

    public TrackingExecutor(ExecutorService executor) {
        delegate = requireNonNull(executor);
        submitted = new AtomicInteger();
        completed = new AtomicInteger();
        failed = new AtomicInteger();
        latch = ZERO;
    }

    @Override
    public void execute(Runnable command) {
        try {
            submitted.incrementAndGet();
            delegate.execute(() -> {
                Uninterruptibles.awaitUninterruptibly(latch);
                try {
                    command.run();
                } catch (Throwable t) {
                    failed.incrementAndGet();
                    throw t;
                } finally {
                    completed.incrementAndGet();
                }
            });
        } catch (Throwable t) {
            completed.incrementAndGet();
            failed.incrementAndGet();
            throw t;
        }
    }

    public int submitted() {
        return submitted.get();
    }

    public int completed() {
        return completed.get();
    }

    public int failed() {
        return failed.get();
    }

    public void pause() {
        latch = new CountDownLatch(1);
    }

    public void resume() {
        latch.countDown();
    }

    @Override
    public ExecutorService delegate() {
        return delegate;
    }
}
