

package org.apache.curator.framework.client;

import com.google.common.collect.Queues;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.RetryOneTime;
import org.apache.curator.test.BaseClassForTests;
import org.apache.curator.test.Timing;
import org.apache.curator.utils.CloseableUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestResetConnectionWithBackgroundFailure extends BaseClassForTests
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Test
    public void testConnectionStateListener() throws Exception
    {
        server.stop();

        LeaderSelector selector = null;
        Timing timing = new Timing();
        CuratorFramework client = CuratorFrameworkFactory.newClient(server.getConnectString(), timing.session(), timing.connection(), new RetryOneTime(1));
        try
        {
            client.start();
            timing.sleepABit();

            LeaderSelectorListener listenerLeader = new LeaderSelectorListenerAdapter()
            {
                @Override
                public void takeLeadership(CuratorFramework client) throws Exception
                {
                    Thread.currentThread().join();
                }
            };
            selector = new LeaderSelector(client, "/leader", listenerLeader);
            selector.autoRequeue();
            selector.start();

            final BlockingQueue<ConnectionState> listenerSequence = Queues.newLinkedBlockingQueue();
            ConnectionStateListener listener1 = new ConnectionStateListener()
            {
                @Override
                public void stateChanged(CuratorFramework client, ConnectionState newState)
                {
                    listenerSequence.add(newState);
                }
            };

            Timing forWaiting = timing.forWaiting();

            client.getConnectionStateListenable().addListener(listener1);
            log.debug("Starting ZK server");
            server.restart();
            assertEquals(listenerSequence.poll(forWaiting.milliseconds(), TimeUnit.MILLISECONDS), ConnectionState.CONNECTED);

            log.debug("Stopping ZK server");
            server.stop();
            assertEquals(listenerSequence.poll(forWaiting.milliseconds(), TimeUnit.MILLISECONDS), ConnectionState.SUSPENDED);
            assertEquals(listenerSequence.poll(forWaiting.milliseconds(), TimeUnit.MILLISECONDS), ConnectionState.LOST);

            log.debug("Starting ZK server");
            server.restart();
            assertEquals(listenerSequence.poll(forWaiting.milliseconds(), TimeUnit.MILLISECONDS), ConnectionState.RECONNECTED);

            log.debug("Stopping ZK server");
            server.close();
            assertEquals(listenerSequence.poll(forWaiting.milliseconds(), TimeUnit.MILLISECONDS), ConnectionState.SUSPENDED);
            assertEquals(listenerSequence.poll(forWaiting.milliseconds(), TimeUnit.MILLISECONDS), ConnectionState.LOST);
        }
        finally
        {
            CloseableUtils.closeQuietly(selector);
            CloseableUtils.closeQuietly(client);
        }
    }

}