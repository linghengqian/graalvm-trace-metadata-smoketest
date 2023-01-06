package com.lingh.utils;

import org.apache.curator.utils.CloseableExecutorService;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestCloseableExecutorService {
    private static final int QTY = 10;

    private volatile ExecutorService executorService;

    @BeforeEach
    public void setup() {
        executorService = Executors.newFixedThreadPool(QTY * 2);
    }

    @AfterEach
    public void tearDown() {
        executorService.shutdownNow();
    }

    @Test
    public void testBasicRunnable() {
        try {
            CloseableExecutorService service = new CloseableExecutorService(executorService);
            CountDownLatch startLatch = new CountDownLatch(QTY);
            CountDownLatch latch = new CountDownLatch(QTY);
            IntStream.range(0, QTY).forEach(i -> submitRunnable(service, startLatch, latch));
            assertTrue(startLatch.await(3, TimeUnit.SECONDS));
            service.close();
            assertTrue(latch.await(3, TimeUnit.SECONDS));
        } catch (AssertionError e) {
            throw e;
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testBasicCallable() throws InterruptedException {
        CloseableExecutorService service = new CloseableExecutorService(executorService);
        final CountDownLatch startLatch = new CountDownLatch(QTY);
        final CountDownLatch latch = new CountDownLatch(QTY);
        IntStream.range(0, QTY).mapToObj(i -> (Callable<Void>) () -> {
            try {
                startLatch.countDown();
                Thread.currentThread().join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                latch.countDown();
            }
            return null;
        }).forEach(service::submit);
        assertTrue(startLatch.await(3, TimeUnit.SECONDS));
        service.close();
        assertTrue(latch.await(3, TimeUnit.SECONDS));
    }

    @Test
    public void testListeningRunnable() throws InterruptedException {
        CloseableExecutorService service = new CloseableExecutorService(executorService);
        List<Future<?>> futures;
        final CountDownLatch startLatch = new CountDownLatch(QTY);
        futures = IntStream.range(0, QTY).mapToObj(i -> service.submit(() -> {
            try {
                startLatch.countDown();
                Thread.currentThread().join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        })).collect(Collectors.toList());
        assertTrue(startLatch.await(3, TimeUnit.SECONDS));
        for (Future<?> future : futures) {
            future.cancel(true);
        }
        assertEquals(getSize(service), 0);
    }

    @Test
    public void testListeningCallable() throws InterruptedException {
        CloseableExecutorService service = new CloseableExecutorService(executorService);
        final CountDownLatch startLatch = new CountDownLatch(QTY);
        List<Future<?>> futures = IntStream.range(0, QTY).mapToObj(i -> service.submit
                ((Callable<Void>) () -> {
                    try {
                        startLatch.countDown();
                        Thread.currentThread().join();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return null;
                })).collect(Collectors.toList());
        assertTrue(startLatch.await(3, TimeUnit.SECONDS));
        for (Future<?> future : futures) {
            future.cancel(true);
        }
        assertEquals(getSize(service), 0);
    }

    @Test
    public void testPartialRunnable() throws InterruptedException {
        final CountDownLatch outsideLatch = new CountDownLatch(1);
        executorService.submit(() -> {
            try {
                Thread.currentThread().join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                outsideLatch.countDown();
            }
        });
        CloseableExecutorService service = new CloseableExecutorService(executorService);
        CountDownLatch startLatch = new CountDownLatch(QTY);
        CountDownLatch latch = new CountDownLatch(QTY);
        IntStream.range(0, QTY).forEach(i -> submitRunnable(service, startLatch, latch));
        Awaitility.await().until(() -> getSize(service) >= QTY);
        assertTrue(startLatch.await(3, TimeUnit.SECONDS));
        service.close();
        assertTrue(latch.await(3, TimeUnit.SECONDS));
        assertEquals(outsideLatch.getCount(), 1);
    }

    private void submitRunnable(CloseableExecutorService service, final CountDownLatch startLatch, final CountDownLatch latch) {
        service.submit(() -> {
            try {
                startLatch.countDown();
                Thread.sleep(100000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                latch.countDown();
            }
        });
    }

    private int getSize(CloseableExecutorService service) {
        try {
            Method method = CloseableExecutorService.class.getDeclaredMethod("size");
            method.setAccessible(true);
            return (int) method.invoke(service, new Object[]{});
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
