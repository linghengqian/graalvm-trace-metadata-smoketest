
package org.apache.curator.framework.recipes.queue;

import com.google.common.collect.Lists;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.RetryOneTime;
import org.apache.curator.test.BaseClassForTests;
import org.apache.curator.utils.CloseableUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings({"SynchronizationOnLocalVariableOrMethodParameter"})
public class TestDistributedIdQueue extends BaseClassForTests
{
    private static final String     QUEUE_PATH = "/a/queue";

    private static final QueueSerializer<TestQueueItem>  serializer = new QueueItemSerializer();

    @Test
    public void testDeletingWithLock() throws Exception
    {
        DistributedIdQueue<TestQueueItem>  queue = null;
        CuratorFramework client = CuratorFrameworkFactory.newClient(server.getConnectString(), new RetryOneTime(1));
        client.start();
        try
        {
            final CountDownLatch        consumingLatch = new CountDownLatch(1);
            final CountDownLatch        waitLatch = new CountDownLatch(1);
            QueueConsumer<TestQueueItem> consumer = new QueueConsumer<TestQueueItem>()
            {
                @Override
                public void consumeMessage(TestQueueItem message) throws Exception
                {
                    consumingLatch.countDown();
                    waitLatch.await();
                }

                @Override
                public void stateChanged(CuratorFramework client, ConnectionState newState)
                {
                }
            };

            queue = QueueBuilder.builder(client, consumer, serializer, QUEUE_PATH).lockPath("/locks").buildIdQueue();
            queue.start();

            queue.put(new TestQueueItem("test"), "id");
            
            assertTrue(consumingLatch.await(10, TimeUnit.SECONDS));  // wait until consumer has it
            assertEquals(queue.remove("id"), 0);

            waitLatch.countDown();
        }
        finally
        {
            CloseableUtils.closeQuietly(queue);
            CloseableUtils.closeQuietly(client);
        }
    }

    @Test
    public void testOrdering() throws Exception
    {
        final int                   ITEM_QTY = 100;

        DistributedIdQueue<TestQueueItem>  queue = null;
        CuratorFramework client = CuratorFrameworkFactory.newClient(server.getConnectString(), new RetryOneTime(1));
        client.start();
        try
        {
            BlockingQueueConsumer<TestQueueItem> consumer = new BlockingQueueConsumer<TestQueueItem>(Mockito.mock(ConnectionStateListener.class));

            queue = QueueBuilder.builder(client, consumer, serializer, QUEUE_PATH).buildIdQueue();
            queue.start();
            
            List<String>        ids = Lists.newArrayList();
            for ( int i = 0; i < ITEM_QTY; ++i )
            {
                String  id = Double.toString(Math.random());
                ids.add(id);
                queue.put(new TestQueueItem(id), id);
            }

            int                 iteration = 0;
            while ( consumer.size() < ITEM_QTY )
            {
                assertTrue(++iteration < ITEM_QTY);
                Thread.sleep(1000);
            }

            int                 i = 0;
            for ( TestQueueItem item : consumer.getItems() )
            {
                assertEquals(item.str, ids.get(i++));
            }
        }
        finally
        {
            CloseableUtils.closeQuietly(queue);
            CloseableUtils.closeQuietly(client);
        }
    }

    @Test
    public void testRequeuingWithLock() throws Exception
    {
        DistributedIdQueue<TestQueueItem>  queue = null;
        CuratorFramework client = CuratorFrameworkFactory.newClient(server.getConnectString(), new RetryOneTime(1));
        client.start();
        try
        {
            final CountDownLatch        consumingLatch = new CountDownLatch(1);

            QueueConsumer<TestQueueItem> consumer = new QueueConsumer<TestQueueItem>()
            {
                @Override
                public void consumeMessage(TestQueueItem message) throws Exception
                {
                    consumingLatch.countDown();
                    // Throw an exception so requeuing occurs
                    throw new Exception("Consumer failed");
                }

                @Override
                public void stateChanged(CuratorFramework client, ConnectionState newState)
                {
                }
            };

            queue = QueueBuilder.builder(client, consumer, serializer, QUEUE_PATH).lockPath("/locks").buildIdQueue();
            queue.start();

            queue.put(new TestQueueItem("test"), "id");

            assertTrue(consumingLatch.await(10, TimeUnit.SECONDS));  // wait until consumer has it

            // Sleep one more second

            Thread.sleep(1000);

            assertTrue(queue.debugIsQueued("id"));

        }
        finally
        {
            CloseableUtils.closeQuietly(queue);
            CloseableUtils.closeQuietly(client);
        }
    }
}
