
package org.apache.curator.framework.recipes.queue;

import java.util.concurrent.Callable;

public class QueueTestProducer implements Callable<Void>
{
    private final DistributedQueue<TestQueueItem> queue;
    private final int itemQty;
    private final int startIndex;

    public QueueTestProducer(DistributedQueue<TestQueueItem> queue, int itemQty, int startIndex)
    {
        this.queue = queue;
        this.itemQty = itemQty;
        this.startIndex = startIndex;
    }

    @Override
    public Void call() throws Exception
    {
        int     count = 0;
        while ( !Thread.currentThread().isInterrupted() && (count < itemQty) )
        {
            queue.put(new TestQueueItem(Integer.toString(count + startIndex)));
            ++count;
        }
        return null;
    }
}
