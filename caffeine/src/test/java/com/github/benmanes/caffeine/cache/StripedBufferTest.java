
package com.github.benmanes.caffeine.cache;

import com.github.benmanes.caffeine.testing.ConcurrentTestHarness;
import com.google.common.base.MoreObjects;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static com.github.benmanes.caffeine.cache.StripedBuffer.MAXIMUM_TABLE_SIZE;
import static com.github.benmanes.caffeine.cache.StripedBuffer.NCPU;
import static com.google.common.truth.Truth.assertThat;


public final class StripedBufferTest {
    static final Integer ELEMENT = 1;

    @Test(dataProvider = "buffers")
    public void init(FakeBuffer<Integer> buffer) {
        assertThat(buffer.table).isNull();
        var result = buffer.offer(ELEMENT);
        assertThat(buffer.table).hasLength(1);
        assertThat(result).isEqualTo(Buffer.SUCCESS);
    }

    @Test
    public void expand() {
        var buffer = new FakeBuffer<Integer>(Buffer.FAILED);
        assertThat(buffer.offer(ELEMENT)).isEqualTo(Buffer.SUCCESS);
        if (IntStream.range(0, 64)
                .map(i -> buffer.offer(ELEMENT))
                .anyMatch(result -> result == Buffer.SUCCESS)) {
            return;
        }
        Assert.fail();
    }

    @Test
    @SuppressWarnings("ThreadPriorityCheck")
    public void expand_concurrent() {
        var buffer = new FakeBuffer<Boolean>(Buffer.FAILED);
        ConcurrentTestHarness.timeTasks(10 * NCPU, () -> {
            IntStream.range(0, 1000).forEach(i -> {
                buffer.offer(Boolean.TRUE);
                Thread.yield();
            });
        });
        assertThat(buffer.table).hasLength(MAXIMUM_TABLE_SIZE);
    }

    @Test(dataProvider = "buffers")
    @SuppressWarnings("ThreadPriorityCheck")
    public void produce(FakeBuffer<Integer> buffer) {
        ConcurrentTestHarness.timeTasks(NCPU, () -> {
            IntStream.range(0, 10).forEach(i -> {
                buffer.offer(ELEMENT);
                Thread.yield();
            });
        });
        assertThat(buffer.table.length).isAtMost(MAXIMUM_TABLE_SIZE);
    }

    @Test(dataProvider = "buffers")
    public void drain(FakeBuffer<Integer> buffer) {
        buffer.drainTo(e -> {
        });
        assertThat(buffer.drains).isEqualTo(0);
        buffer.offer(ELEMENT);
        buffer.drainTo(e -> {
        });
        assertThat(buffer.drains).isEqualTo(1);
    }

    @DataProvider(name = "buffers")
    public Object[] providesBuffers() {
        var results = List.of(Buffer.SUCCESS, Buffer.FAILED, Buffer.FULL);
        var buffers = new ArrayList<Buffer<Integer>>();
        for (var result : results) {
            buffers.add(new FakeBuffer<>(result));
        }
        return buffers.toArray();
    }

    static final class FakeBuffer<E> extends StripedBuffer<E> {
        final int result;
        int drains = 0;

        FakeBuffer(int result) {
            this.result = result;
        }

        @Override
        protected Buffer<E> create(E e) {
            return new Buffer<>() {
                @Override
                public int offer(E e) {
                    return result;
                }

                @Override
                public void drainTo(Consumer<E> consumer) {
                    drains++;
                }

                @Override
                public long size() {
                    return 0L;
                }

                @Override
                public long reads() {
                    return 0L;
                }

                @Override
                public long writes() {
                    return 0L;
                }
            };
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this).addValue(result).toString();
        }
    }
}
