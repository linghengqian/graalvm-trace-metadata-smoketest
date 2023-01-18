
package com.github.benmanes.caffeine.cache.buffer;

import com.github.benmanes.caffeine.testing.ConcurrentTestHarness;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Iterator;

import static com.google.common.truth.Truth.assertThat;

public final class BufferTest {
    @DataProvider
    public Iterator<Object[]> buffers() {
        return Arrays.stream(BufferType.values())
                .map(factory -> new Object[]{factory.create()})
                .iterator();
    }

    @Test(dataProvider = "buffers")
    @SuppressWarnings("ThreadPriorityCheck")
    public void record(ReadBuffer<Boolean> buffer) {
        ConcurrentTestHarness.timeTasks(100, () -> {
            for (int i = 0; i < 1000; i++) {
                buffer.offer(Boolean.TRUE);
                Thread.yield();
            }
        });
        long recorded = buffer.writes();
        assertThat(recorded).isEqualTo(ReadBuffer.BUFFER_SIZE);
    }

    @Test(dataProvider = "buffers")
    public void drain(ReadBuffer<Boolean> buffer) {
        for (int i = 0; i < 2 * ReadBuffer.BUFFER_SIZE; i++) {
            buffer.offer(Boolean.TRUE);
        }
        buffer.drain();
        long drained = buffer.reads();
        long recorded = buffer.writes();
        assertThat(drained).isEqualTo(recorded);
    }

    @Test(dataProvider = "buffers")
    @SuppressWarnings("ThreadPriorityCheck")
    public void recordAndDrain(ReadBuffer<Boolean> buffer) {
        ConcurrentTestHarness.timeTasks(100, () -> {
            for (int i = 0; i < 1000; i++) {
                int result = buffer.offer(Boolean.TRUE);
                if (result == ReadBuffer.FULL) {
                    buffer.drain();
                }
                Thread.yield();
            }
        });
        buffer.drain();
        long drained = buffer.reads();
        long recorded = buffer.writes();
        assertThat(drained).isEqualTo(recorded);
    }
}
