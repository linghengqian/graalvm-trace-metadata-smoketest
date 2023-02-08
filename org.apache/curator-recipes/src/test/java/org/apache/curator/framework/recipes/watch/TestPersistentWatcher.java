

package org.apache.curator.framework.recipes.watch;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.RetryOneTime;
import org.apache.curator.test.compatibility.CuratorTestBase;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag(CuratorTestBase.zk36Group)
public class TestPersistentWatcher extends CuratorTestBase {
    @Test
    public void testConnectionLostRecursive() throws Exception {
        internalTest(true);
    }

    @Test
    public void testConnectionLost() throws Exception {
        internalTest(false);
    }

    private void internalTest(boolean recursive) throws Exception {
        try (CuratorFramework client = CuratorFrameworkFactory.newClient(server.getConnectString(), timing.session(), timing.connection(), new RetryOneTime(1))) {
            CountDownLatch lostLatch = new CountDownLatch(1);
            CountDownLatch reconnectedLatch = new CountDownLatch(1);
            client.start();
            client.getConnectionStateListenable().addListener((__, newState) -> {
                if (newState == ConnectionState.LOST) {
                    lostLatch.countDown();
                } else if (newState == ConnectionState.RECONNECTED) {
                    reconnectedLatch.countDown();
                }
            });

            try (PersistentWatcher persistentWatcher = new PersistentWatcher(client, "/top/main", recursive)) {
                persistentWatcher.start();

                BlockingQueue<WatchedEvent> events = new LinkedBlockingQueue<>();
                persistentWatcher.getListenable().addListener(events::add);

                client.create().creatingParentsIfNeeded().forPath("/top/main/a");
                assertEquals(timing.takeFromQueue(events).getPath(), "/top/main");
                if (recursive) {
                    assertEquals(timing.takeFromQueue(events).getPath(), "/top/main/a");
                } else {
                    assertEquals(timing.takeFromQueue(events).getPath(), "/top/main");   // child added
                }

                server.stop();
                assertEquals(timing.takeFromQueue(events).getState(), Watcher.Event.KeeperState.Disconnected);
                assertTrue(timing.awaitLatch(lostLatch));

                server.restart();
                assertTrue(timing.awaitLatch(reconnectedLatch));

                timing.sleepABit();     // time to allow watcher to get reset
                events.clear();

                if (recursive) {
                    client.setData().forPath("/top/main/a", "foo".getBytes());
                    assertEquals(timing.takeFromQueue(events).getType(), Watcher.Event.EventType.NodeDataChanged);
                }
                client.setData().forPath("/top/main", "bar".getBytes());
                assertEquals(timing.takeFromQueue(events).getPath(), "/top/main");
            }
        }
    }
}