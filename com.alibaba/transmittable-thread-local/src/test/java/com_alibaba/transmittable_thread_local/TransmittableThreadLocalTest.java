package com_alibaba.transmittable_thread_local;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.alibaba.ttl.TtlRunnable;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

public class TransmittableThreadLocalTest {

    @Test
    void testSimple() {
        TransmittableThreadLocal<String> stringTransmittableThreadLocal = new TransmittableThreadLocal<>();
        stringTransmittableThreadLocal.set("parent-thread-value");
        new Thread(() -> assertThat(stringTransmittableThreadLocal.get()).isEqualTo("parent-thread-value")).start();
        assertThat(stringTransmittableThreadLocal.get()).isEqualTo("parent-thread-value");
    }

    @Test
    void testTtlWrapper() throws ExecutionException, InterruptedException {
        ExecutorService executorService = Executors.newCachedThreadPool();
        TransmittableThreadLocal<String> stringTransmittableThreadLocal = new TransmittableThreadLocal<>();
        stringTransmittableThreadLocal.set("parent-thread-value");
        Runnable runnable = () -> assertThat(stringTransmittableThreadLocal.get()).isEqualTo("parent-thread-value");
        TtlRunnable ttlRunnable = TtlRunnable.get(runnable);
        executorService.submit(ttlRunnable).get();
        executorService.shutdown();
    }
}
