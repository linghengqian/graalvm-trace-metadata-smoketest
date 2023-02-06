

package com.lingh.imps;

import com.google.common.collect.Queues;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.retry.RetryNTimes;
import org.apache.curator.retry.RetryOneTime;
import org.apache.curator.test.BaseClassForTests;
import org.apache.curator.test.InstanceSpec;
import org.apache.curator.test.TestingCluster;
import org.apache.curator.test.Timing;
import org.apache.curator.utils.CloseableUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestReadOnly extends BaseClassForTests {
    @BeforeEach
    public void setup() {
        System.setProperty("readonlymode.enabled", "true");
    }

    @AfterEach
    public void tearDown() {
        System.setProperty("readonlymode.enabled", "false");
    }

    @Test
    public void testConnectionStateNewClient() throws Exception {
        Timing timing = new Timing();
        CuratorFramework client = null;
        TestingCluster cluster = createAndStartCluster(3);
        try {
            final CountDownLatch lostLatch = new CountDownLatch(1);
            client = CuratorFrameworkFactory.newClient(cluster.getConnectString(), timing.session(), timing.connection(), new RetryOneTime(100));
            client.start();
            client.checkExists().forPath("/");
            client.getConnectionStateListenable().addListener((client1, newState) -> {
                if (newState == ConnectionState.LOST) {
                    lostLatch.countDown();
                }
            });
            Iterator<InstanceSpec> iterator = cluster.getInstances().iterator();
            for (int i = 0; i < 2; ++i) {
                cluster.killServer(iterator.next());
            }
            timing.awaitLatch(lostLatch);
            client.close();
            client = CuratorFrameworkFactory.builder()
                    .connectString(cluster.getConnectString())
                    .sessionTimeoutMs(timing.session())
                    .connectionTimeoutMs(timing.connection())
                    .retryPolicy(new RetryNTimes(3, timing.milliseconds()))
                    .canBeReadOnly(true)
                    .build();
            final BlockingQueue<ConnectionState> states = Queues.newLinkedBlockingQueue();
            client.getConnectionStateListenable().addListener((client12, newState) -> states.add(newState));
            client.start();
            client.checkExists().forPath("/");
            ConnectionState state = states.poll(timing.forWaiting().milliseconds(), TimeUnit.MILLISECONDS);
            assertThat(state).isEqualTo(ConnectionState.READ_ONLY);
        } finally {
            CloseableUtils.closeQuietly(client);
            CloseableUtils.closeQuietly(cluster);
        }
    }

    @Test
    public void testReadOnly() throws Exception {
        Timing timing = new Timing();
        CuratorFramework client = null;
        TestingCluster cluster = createAndStartCluster(2);
        try {
            client = CuratorFrameworkFactory.builder().connectString(cluster.getConnectString()).canBeReadOnly(true).connectionTimeoutMs(timing.connection()).sessionTimeoutMs(timing.session()).retryPolicy(new ExponentialBackoffRetry(100, 3)).build();
            client.start();
            client.create().forPath("/test");
            final CountDownLatch readOnlyLatch = new CountDownLatch(1);
            final CountDownLatch reconnectedLatch = new CountDownLatch(1);
            ConnectionStateListener listener = (client1, newState) -> {
                switch (newState) {
                    case READ_ONLY -> readOnlyLatch.countDown();
                    case RECONNECTED -> reconnectedLatch.countDown();
                }
            };
            client.getConnectionStateListenable().addListener(listener);
            InstanceSpec ourInstance = cluster.findConnectionInstance(client.getZookeeperClient().getZooKeeper());
            Iterator<InstanceSpec> iterator = cluster.getInstances().iterator();
            InstanceSpec killInstance = iterator.next();
            if (killInstance.equals(ourInstance)) {
                killInstance = iterator.next();
            }
            cluster.killServer(killInstance);
            assertEquals(reconnectedLatch.getCount(), 1);
            assertTrue(timing.awaitLatch(readOnlyLatch));
            assertEquals(reconnectedLatch.getCount(), 1);
            cluster.restartServer(killInstance);
            assertTrue(timing.awaitLatch(reconnectedLatch));
        } finally {
            CloseableUtils.closeQuietly(client);
            CloseableUtils.closeQuietly(cluster);
        }
    }

    @Override
    protected void createServer() {
    }
}
