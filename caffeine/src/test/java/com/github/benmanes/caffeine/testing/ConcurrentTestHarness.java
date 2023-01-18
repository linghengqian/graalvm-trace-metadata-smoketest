
package com.github.benmanes.caffeine.testing;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.common.util.concurrent.Uninterruptibles;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicReferenceArray;

public final class ConcurrentTestHarness {
    public static final ThreadFactory DAEMON_FACTORY = new ThreadFactoryBuilder().setPriority(Thread.MIN_PRIORITY).setDaemon(true).build();
    public static final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor(DAEMON_FACTORY);
    public static final ExecutorService executor = Executors.newCachedThreadPool(DAEMON_FACTORY);

    private ConcurrentTestHarness() {
    }

    public static void execute(Runnable task) {
        executor.execute(task);
    }

    public static long timeTasks(int nThreads, Runnable task) {
        return timeTasks(nThreads, Executors.callable(task)).executionTime();
    }

    public static <T> TestResult<T> timeTasks(int nThreads, Callable<T> task) {
        var startGate = new CountDownLatch(1);
        var endGate = new CountDownLatch(nThreads);
        var results = new AtomicReferenceArray<T>(nThreads);

        for (int i = 0; i < nThreads; i++) {
            final int index = i;
            executor.execute(() -> {
                try {
                    startGate.await();
                    try {
                        results.set(index, task.call());
                    } finally {
                        endGate.countDown();
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }

        long start = System.nanoTime();
        startGate.countDown();
        Uninterruptibles.awaitUninterruptibly(endGate);
        long end = System.nanoTime();
        return new TestResult<>(end - start, toList(results));
    }

    private static <T> List<T> toList(AtomicReferenceArray<T> data) {
        var list = new ArrayList<T>(data.length());
        for (int i = 0; i < data.length(); i++) {
            list.add(data.get(i));
        }
        return list;
    }

    public record TestResult<T>(long executionTime, List<T> results) {
    }
}
