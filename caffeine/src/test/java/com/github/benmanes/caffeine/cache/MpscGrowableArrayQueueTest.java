
package com.github.benmanes.caffeine.cache;

import com.github.benmanes.caffeine.testing.ConcurrentTestHarness;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static com.github.benmanes.caffeine.testing.Awaits.await;
import static com.google.common.truth.Truth.assertThat;
import static org.hamcrest.Matchers.is;

public final class MpscGrowableArrayQueueTest {
    private static final int NUM_PRODUCERS = 10;
    private static final int PRODUCE = 100;

    private static final int POPULATED_SIZE = 10;
    private static final int FULL_SIZE = 32;


    @Test(expectedExceptions = IllegalArgumentException.class)
    public void constructor_initialCapacity_tooSmall() {
        new MpscGrowableArrayQueue<Integer>(1, 4);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void constructor_maxCapacity_tooSmall() {
        new MpscGrowableArrayQueue<Integer>(4, 1);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void constructor_inverted() {
        new MpscGrowableArrayQueue<Integer>(8, 4);
    }

    @Test
    public void constructor() {
        var buffer = new MpscGrowableArrayQueue<Integer>(4, 8);
        assertThat(buffer.capacity()).isEqualTo(8);
    }

    @Test(dataProvider = "empty")
    public void size_whenEmpty(MpscGrowableArrayQueue<Integer> buffer) {
        assertThat(buffer.size()).isEqualTo(0);
    }

    @Test(dataProvider = "populated")
    public void size_whenPopulated(MpscGrowableArrayQueue<Integer> buffer) {
        assertThat(buffer.size()).isEqualTo(POPULATED_SIZE);
    }

    @Test(dataProvider = "empty")
    public void offer_whenEmpty(MpscGrowableArrayQueue<Integer> buffer) {
        assertThat(buffer.offer(1)).isTrue();
        assertThat(buffer).hasSize(1);
    }

    @Test(dataProvider = "populated")
    public void offer_whenPopulated(MpscGrowableArrayQueue<Integer> buffer) {
        assertThat(buffer.offer(1)).isTrue();
        assertThat(buffer).hasSize(POPULATED_SIZE + 1);
    }

    @Test(dataProvider = "full")
    public void offer_whenFull(MpscGrowableArrayQueue<Integer> buffer) {
        assertThat(buffer.offer(1)).isFalse();
        assertThat(buffer).hasSize(FULL_SIZE);
    }

    @Test(dataProvider = "empty")
    public void relaxedOffer_whenEmpty(MpscGrowableArrayQueue<Integer> buffer) {
        assertThat(buffer.relaxedOffer(1)).isTrue();
        assertThat(buffer).hasSize(1);
    }

    @Test(dataProvider = "populated")
    public void relaxedOffer_whenPopulated(MpscGrowableArrayQueue<Integer> buffer) {
        assertThat(buffer.relaxedOffer(1)).isTrue();
        assertThat(buffer).hasSize(POPULATED_SIZE + 1);
    }

    @Test(dataProvider = "full")
    public void relaxedOffer_whenFull(MpscGrowableArrayQueue<Integer> buffer) {
        assertThat(buffer.relaxedOffer(1)).isFalse();
        assertThat(buffer).hasSize(FULL_SIZE);
    }

    @Test(dataProvider = "empty")
    public void poll_whenEmpty(MpscGrowableArrayQueue<Integer> buffer) {
        assertThat(buffer.poll()).isNull();
    }

    @Test(dataProvider = "populated")
    public void poll_whenPopulated(MpscGrowableArrayQueue<Integer> buffer) {
        assertThat(buffer.poll()).isNotNull();
        assertThat(buffer).hasSize(POPULATED_SIZE - 1);
    }

    @Test(dataProvider = "full")
    public void poll_toEmpty(MpscGrowableArrayQueue<Integer> buffer) {
        while (buffer.poll() != null) {
        }
        assertThat(buffer).isEmpty();
    }

    @Test(dataProvider = "empty")
    public void relaxedPoll_whenEmpty(MpscGrowableArrayQueue<Integer> buffer) {
        assertThat(buffer.relaxedPoll()).isNull();
    }

    @Test(dataProvider = "populated")
    public void relaxedPoll_whenPopulated(MpscGrowableArrayQueue<Integer> buffer) {
        assertThat(buffer.relaxedPoll()).isNotNull();
        assertThat(buffer).hasSize(POPULATED_SIZE - 1);
    }

    @Test(dataProvider = "full")
    public void relaxedPoll_toEmpty(MpscGrowableArrayQueue<Integer> buffer) {
        while (buffer.relaxedPoll() != null) {
        }
        assertThat(buffer).isEmpty();
    }

    @Test(dataProvider = "empty")
    public void peek_whenEmpty(MpscGrowableArrayQueue<Integer> buffer) {
        assertThat(buffer.peek()).isNull();
    }

    @Test(dataProvider = "populated")
    public void peek_whenPopulated(MpscGrowableArrayQueue<Integer> buffer) {
        assertThat(buffer.peek()).isNotNull();
        assertThat(buffer).hasSize(POPULATED_SIZE);
    }

    @Test(dataProvider = "full")
    public void peek_toEmpty(MpscGrowableArrayQueue<Integer> buffer) {
        for (int i = 0; i < FULL_SIZE; i++) {
            assertThat(buffer.peek()).isNotNull();
            buffer.poll();
        }
        assertThat(buffer.peek()).isNull();
    }

    @Test(dataProvider = "empty")
    public void relaxedPeek_whenEmpty(MpscGrowableArrayQueue<Integer> buffer) {
        assertThat(buffer.relaxedPeek()).isNull();
    }

    @Test(dataProvider = "populated")
    public void relaxedPeek_whenPopulated(MpscGrowableArrayQueue<Integer> buffer) {
        assertThat(buffer.relaxedPeek()).isNotNull();
        assertThat(buffer).hasSize(POPULATED_SIZE);
    }

    @Test(dataProvider = "full")
    public void relaxedPeek_toEmpty(MpscGrowableArrayQueue<Integer> buffer) {
        for (int i = 0; i < FULL_SIZE; i++) {
            assertThat(buffer.relaxedPeek()).isNotNull();
            buffer.poll();
        }
        assertThat(buffer.relaxedPeek()).isNull();
    }

    @SuppressWarnings("ReturnValueIgnored")
    @Test(dataProvider = "full", expectedExceptions = UnsupportedOperationException.class)
    public void iterator(MpscGrowableArrayQueue<Integer> buffer) {
        buffer.iterator();
    }

    @Test(dataProvider = "populated")
    public void inspection(MpscGrowableArrayQueue<Integer> buffer) {
        assertThat(buffer.currentConsumerIndex()).isEqualTo(0);
        assertThat(buffer.currentProducerIndex()).isEqualTo(POPULATED_SIZE);
    }

    @Test(dataProvider = "empty")
    public void oneProducer_oneConsumer(MpscGrowableArrayQueue<Integer> buffer) {
        var started = new AtomicInteger();
        var finished = new AtomicInteger();
        ConcurrentTestHarness.execute(() -> {
            started.incrementAndGet();
            await().untilAtomic(started, is(2));
            for (int i = 0; i < PRODUCE; i++) {
                while (!buffer.offer(i)) {
                }
            }
            finished.incrementAndGet();
        });
        ConcurrentTestHarness.execute(() -> {
            started.incrementAndGet();
            await().untilAtomic(started, is(2));
            for (int i = 0; i < PRODUCE; i++) {
                while (buffer.poll() == null) {
                }
            }
            finished.incrementAndGet();
        });
        await().untilAtomic(finished, is(2));
        assertThat(buffer).isEmpty();
    }

    @Test(dataProvider = "empty")
    public void manyProducers_noConsumer(MpscGrowableArrayQueue<Integer> buffer) {
        var count = new AtomicInteger();
        ConcurrentTestHarness.timeTasks(NUM_PRODUCERS, () -> {
            for (int i = 0; i < PRODUCE; i++) {
                if (buffer.offer(i)) {
                    count.incrementAndGet();
                }
            }
        });
        assertThat(buffer).hasSize(count.get());
    }

    @Test(dataProvider = "empty")
    public void manyProducers_oneConsumer(MpscGrowableArrayQueue<Integer> buffer) {
        var started = new AtomicInteger();
        var finished = new AtomicInteger();
        ConcurrentTestHarness.execute(() -> {
            started.incrementAndGet();
            await().untilAtomic(started, is(NUM_PRODUCERS + 1));
            for (int i = 0; i < (NUM_PRODUCERS * PRODUCE); i++) {
                while (buffer.poll() == null) {
                }
            }
            finished.incrementAndGet();
        });
        ConcurrentTestHarness.timeTasks(NUM_PRODUCERS, () -> {
            started.incrementAndGet();
            await().untilAtomic(started, is(NUM_PRODUCERS + 1));
            for (int i = 0; i < PRODUCE; i++) {
                while (!buffer.offer(i)) {
                }
            }
            finished.incrementAndGet();
        });

        await().untilAtomic(finished, is(NUM_PRODUCERS + 1));
        assertThat(buffer).isEmpty();
    }

    @DataProvider(name = "empty")
    public Object[][] providesEmpty() {
        return new Object[][]{{makePopulated(0)}};
    }

    @DataProvider(name = "populated")
    public Object[][] providesPopulated() {
        return new Object[][]{{makePopulated(POPULATED_SIZE)}};
    }

    @DataProvider(name = "full")
    public Object[][] providesFull() {
        return new Object[][]{{makePopulated(FULL_SIZE)}};
    }

    static MpscGrowableArrayQueue<Integer> makePopulated(int items) {
        var buffer = new MpscGrowableArrayQueue<Integer>(4, FULL_SIZE);
        for (int i = 0; i < items; i++) {
            buffer.offer(i);
        }
        return buffer;
    }
}
