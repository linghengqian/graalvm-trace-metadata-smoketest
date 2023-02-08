
package org.apache.curator.framework.recipes.queue;

class QueueItemSerializer implements QueueSerializer<TestQueueItem>
{
    @Override
    public byte[] serialize(TestQueueItem item)
    {
        return item.str.getBytes();
    }

    @Override
    public TestQueueItem deserialize(byte[] bytes)
    {
        return new TestQueueItem(new String(bytes));
    }
}
