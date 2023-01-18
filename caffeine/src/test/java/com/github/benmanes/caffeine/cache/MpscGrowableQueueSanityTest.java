
package com.github.benmanes.caffeine.cache;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

@RunWith(Parameterized.class)
public final class MpscGrowableQueueSanityTest extends QueueSanityTest {

    public MpscGrowableQueueSanityTest(Queue<Integer> queue,
                                       Ordering ordering, int capacity, boolean isBounded) {
        super(queue, ordering, capacity, isBounded);
    }

    @Parameterized.Parameters
    public static List<Object[]> parameters() {
        var list = new ArrayList<Object[]>();
        list.add(new Object[]{new MpscGrowableArrayQueue<>(2, 4), Ordering.FIFO, 4, true});
        list.add(new Object[]{new MpscGrowableArrayQueue<>(8, SIZE), Ordering.FIFO, SIZE, true});
        return list;
    }
}
