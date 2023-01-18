
package com.github.benmanes.caffeine.cache;

import com.github.benmanes.caffeine.testing.ConcurrentTestHarness;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import static com.google.common.truth.Truth.assertThat;

public final class BoundedBufferTest {

    @DataProvider
    public Object[][] buffer() {
        return new Object[][]{{new BoundedBuffer<Boolean>()}};
    }

    @Test(dataProvider = "buffer")
    public void offer(BoundedBuffer<Boolean> buffer) {
        ConcurrentTestHarness.timeTasks(10, () -> {
            for (int i = 0; i < 100; i++) {
                buffer.offer(Boolean.TRUE);
            }
        });
        assertThat(buffer.writes()).isGreaterThan(0);
        assertThat(buffer.writes()).isEqualTo(buffer.size());
    }

    @Test(dataProvider = "buffer")
    public void drain(BoundedBuffer<Boolean> buffer) {
        for (int i = 0; i < BoundedBuffer.BUFFER_SIZE; i++) {
            buffer.offer(Boolean.TRUE);
        }
        long[] read = new long[1];
        buffer.drainTo(e -> read[0]++);
        assertThat(read[0]).isEqualTo(buffer.reads());
        assertThat(read[0]).isEqualTo(buffer.writes());
    }

    @Test(dataProvider = "buffer")
    @SuppressWarnings("ThreadPriorityCheck")
    public void offerAndDrain(BoundedBuffer<Boolean> buffer) {
        var lock = new ReentrantLock();
        var reads = new AtomicInteger();
        ConcurrentTestHarness.timeTasks(10, () -> {
            for (int i = 0; i < 1000; i++) {
                boolean shouldDrain = (buffer.offer(Boolean.TRUE) == Buffer.FULL);
                if (shouldDrain && lock.tryLock()) {
                    buffer.drainTo(e -> reads.incrementAndGet());
                    lock.unlock();
                }
                Thread.yield();
            }
        });
        buffer.drainTo(e -> reads.incrementAndGet());
        assertThat(reads.longValue()).isEqualTo(buffer.reads());
        assertThat(reads.longValue()).isEqualTo(buffer.writes());
    }

    @Test
    public void overflow() {
        var buffer = new BoundedBuffer.RingBuffer<Boolean>(null);
        buffer.writeCounter = Long.MAX_VALUE;
        buffer.readCounter = Long.MAX_VALUE;

        buffer.offer(Boolean.TRUE);
        var data = new ArrayList<>();
        buffer.drainTo(data::add);

        for (var e : buffer.buffer) {
            assertThat(e).isNull();
        }
        assertThat(data).containsExactly(Boolean.TRUE);
        assertThat(buffer.readCounter).isEqualTo(Long.MIN_VALUE);
        assertThat(buffer.writeCounter).isEqualTo(Long.MIN_VALUE);
    }
}
