
package org.apache.curator.framework.imps;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.retry.RetryOneTime;
import org.apache.curator.test.InstanceSpec;
import org.apache.curator.test.TestingCluster;
import org.apache.curator.test.Timing;
import org.apache.curator.test.compatibility.CuratorTestBase;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.CreateMode;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestWithCluster extends CuratorTestBase {
    @Test
    public void testSessionSurvives() throws Exception {
        Timing timing = new Timing();
        CuratorFramework client = null;
        TestingCluster cluster = createAndStartCluster(3);
        try {
            client = CuratorFrameworkFactory.newClient(cluster.getConnectString(), timing.session(), timing.connection(), new ExponentialBackoffRetry(100, 3));
            client.start();
            final CountDownLatch reconnectedLatch = new CountDownLatch(1);
            ConnectionStateListener listener = (client1, newState) -> {
                if (newState == ConnectionState.RECONNECTED) {
                    reconnectedLatch.countDown();
                }
            };
            client.getConnectionStateListenable().addListener(listener);
            client.create().withMode(CreateMode.EPHEMERAL).forPath("/temp", "value".getBytes());
            assertNotNull(client.checkExists().forPath("/temp"));
            for (InstanceSpec spec : cluster.getInstances()) {
                cluster.killServer(spec);
                timing.sleepABit();
                cluster.restartServer(spec);
                timing.sleepABit();
            }
            assertTrue(timing.awaitLatch(reconnectedLatch));
            assertNotNull(client.checkExists().forPath("/temp"));
        } finally {
            CloseableUtils.closeQuietly(client);
            CloseableUtils.closeQuietly(cluster);
        }
    }

    @Test
    public void testSplitBrain() throws Exception {
        Timing timing = new Timing();
        CuratorFramework client = null;
        TestingCluster cluster = createAndStartCluster(3);
        try {
            for (InstanceSpec instanceSpec : cluster.getInstances()) {
                client = CuratorFrameworkFactory.newClient(instanceSpec.getConnectString(), new RetryOneTime(1));
                client.start();
                client.checkExists().forPath("/");
                client.close();
                client = null;
            }
            client = CuratorFrameworkFactory.newClient(cluster.getConnectString(), timing.session(), timing.connection(), new RetryOneTime(1));
            client.start();
            final CountDownLatch latch = new CountDownLatch(2);
            client.getConnectionStateListenable().addListener((client1, newState) -> {
                        if ((newState == ConnectionState.SUSPENDED) || (newState == ConnectionState.LOST)) {
                            latch.countDown();
                        }
                    }
            );
            client.checkExists().forPath("/");
            for (InstanceSpec instanceSpec : cluster.getInstances()) {
                if (!instanceSpec.equals(cluster.findConnectionInstance(client.getZookeeperClient().getZooKeeper()))) {
                    assertTrue(cluster.killServer(instanceSpec));
                }
            }
            assertTrue(timing.awaitLatch(latch));
        } finally {
            CloseableUtils.closeQuietly(client);
            CloseableUtils.closeQuietly(cluster);
        }
    }

    @Override
    protected void createServer() {
    }
}
