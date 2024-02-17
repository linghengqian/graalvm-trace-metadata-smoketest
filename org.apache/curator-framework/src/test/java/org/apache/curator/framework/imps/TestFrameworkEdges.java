

package org.apache.curator.framework.imps;

import com.google.common.collect.Queues;
import org.apache.curator.RetryPolicy;
import org.apache.curator.RetrySleeper;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CreateBuilder;
import org.apache.curator.framework.api.CuratorEventType;
import org.apache.curator.framework.api.ErrorListenerPathAndBytesable;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.RetryForever;
import org.apache.curator.retry.RetryNTimes;
import org.apache.curator.retry.RetryOneTime;
import org.apache.curator.test.BaseClassForTests;
import org.apache.curator.test.InstanceSpec;
import org.apache.curator.test.TestingCluster;
import org.apache.curator.test.TestingServer;
import org.apache.curator.test.compatibility.CuratorTestBase;
import org.apache.curator.test.compatibility.Timing2;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.Stat;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@SuppressWarnings({"ResultOfMethodCallIgnored", "DataFlowIssue", "resource"})
@Tag(CuratorTestBase.zk35TestCompatibilityGroup)
public class TestFrameworkEdges extends BaseClassForTests {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final Timing2 timing = new Timing2();

    @BeforeAll
    public static void setUpClass() {
        System.setProperty("zookeeper.extendedTypesEnabled", "true");
    }

    @Test
    @DisplayName("test case for CURATOR-525")
    public void testValidateConnectionEventRaces() throws Exception {
        try (CuratorFramework client = CuratorFrameworkFactory.newClient(server.getConnectString(), 2000, 1000, new RetryOneTime(1))) {
            CuratorFrameworkImpl clientImpl = (CuratorFrameworkImpl) client;
            client.start();
            client.getChildren().forPath("/");
            client.create().forPath("/foo");
            BlockingQueue<ConnectionState> stateQueue = new LinkedBlockingQueue<>();
            client.getConnectionStateListenable().addListener((__, newState) -> stateQueue.add(newState));
            server.stop();
            assertEquals(timing.takeFromQueue(stateQueue), ConnectionState.SUSPENDED);
            assertEquals(timing.takeFromQueue(stateQueue), ConnectionState.LOST);
            clientImpl.debugCheckBackgroundRetryReadyLatch = new CountDownLatch(1);
            clientImpl.debugCheckBackgroundRetryLatch = new CountDownLatch(1);
            client.delete().guaranteed().inBackground().forPath("/foo");
            timing.awaitLatch(clientImpl.debugCheckBackgroundRetryReadyLatch);
            server.restart();
            assertEquals(timing.takeFromQueue(stateQueue), ConnectionState.RECONNECTED);
            clientImpl.injectedCode = KeeperException.Code.SESSIONEXPIRED;
            clientImpl.debugCheckBackgroundRetryLatch.countDown();
            assertEquals(timing.takeFromQueue(stateQueue), ConnectionState.LOST);
            assertEquals(timing.takeFromQueue(stateQueue), ConnectionState.RECONNECTED);
        }
    }

    @Test
    public void testInjectSessionExpiration() throws Exception {
        try (CuratorFramework client = CuratorFrameworkFactory.newClient(server.getConnectString(), new RetryOneTime(1))) {
            client.start();
            CountDownLatch expiredLatch = new CountDownLatch(1);
            Watcher watcher = event -> {
                if (event.getState() == Watcher.Event.KeeperState.Expired) {
                    expiredLatch.countDown();
                }
            };
            client.checkExists().usingWatcher(watcher).forPath("/foobar");
            client.getZookeeperClient().getZooKeeper().getTestable().injectSessionExpiration();
            assertTrue(timing.awaitLatch(expiredLatch));
        }
    }

    @Test
    public void testProtectionWithKilledSession() throws Exception {
        server.stop();
        try (TestingCluster cluster = createAndStartCluster(3)) {
            InstanceSpec instanceSpec0 = cluster.getServers().get(0).getInstanceSpec();
            CountDownLatch serverStoppedLatch = new CountDownLatch(1);
            RetryPolicy retryPolicy = new RetryForever(100) {
                @Override
                public boolean allowRetry(int retryCount, long elapsedTimeMs, RetrySleeper sleeper) {
                    if (serverStoppedLatch.getCount() > 0) {
                        try {
                            cluster.killServer(instanceSpec0);
                        } catch (Exception ignored) {
                        }
                        serverStoppedLatch.countDown();
                    }
                    return super.allowRetry(retryCount, elapsedTimeMs, sleeper);
                }
            };
            try (CuratorFramework client = CuratorFrameworkFactory.newClient(instanceSpec0.getConnectString(), timing.session(), timing.connection(), retryPolicy)) {
                BlockingQueue<String> createdNode = new LinkedBlockingQueue<>();
                BackgroundCallback callback = (__, event) -> {
                    if (event.getType() == CuratorEventType.CREATE) {
                        createdNode.offer(event.getPath());
                    }
                };
                client.start();
                client.create().forPath("/test");
                ErrorListenerPathAndBytesable<String> builder = client.create().withProtection().withMode(CreateMode.EPHEMERAL).inBackground(callback);
                ((CreateBuilderImpl) builder).failNextCreateForTesting = true;
                builder.forPath("/test/hey");
                assertTrue(timing.awaitLatch(serverStoppedLatch));
                timing.forSessionSleep().sleep();
                cluster.restartServer(instanceSpec0);
                String path = timing.takeFromQueue(createdNode);
                List<String> children = client.getChildren().forPath("/test");
                assertEquals(Collections.singletonList(ZKPaths.getNodeFromPath(path)), children);
            }
        }
    }

    @Test
    public void testBackgroundLatencyUnSleep() throws Exception {
        server.stop();
        CuratorFramework client = CuratorFrameworkFactory.newClient(server.getConnectString(), new RetryOneTime(1));
        try {
            client.start();
            ((CuratorFrameworkImpl) client).sleepAndQueueOperationSeconds = Integer.MAX_VALUE;
            final CountDownLatch latch = new CountDownLatch(3);
            BackgroundCallback callback = (client1, event) -> {
                if ((event.getType() == CuratorEventType.CREATE) && (event.getResultCode() == KeeperException.Code.OK.intValue())) {
                    latch.countDown();
                }
            };
            client.create().inBackground(callback).forPath("/test");
            client.create().inBackground(callback).forPath("/test/one");
            client.create().inBackground(callback).forPath("/test/two");
            server.restart();
            assertTrue(timing.awaitLatch(latch));
        } finally {
            CloseableUtils.closeQuietly(client);
        }
    }

    @Test
    public void testCreateContainersForBadConnect() throws Exception {
        final int serverPort = server.getPort();
        server.close();

        CuratorFramework client = CuratorFrameworkFactory.newClient(server.getConnectString(), 1000, 1000, new RetryNTimes(10, timing.forSleepingABit().milliseconds()));
        try {
            new Thread(() -> {
                try {
                    Thread.sleep(3000);
                    server = new TestingServer(serverPort, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
            client.start();
            client.createContainers("/this/does/not/exist");
            assertNotNull(client.checkExists().forPath("/this/does/not/exist"));
        } finally {
            CloseableUtils.closeQuietly(client);
        }
    }

    @Test
    public void testQuickClose() {
        CuratorFramework client = CuratorFrameworkFactory.newClient(server.getConnectString(), timing.session(), 1, new RetryNTimes(0, 0));
        try {
            client.start();
            client.close();
        } finally {
            CloseableUtils.closeQuietly(client);
        }
    }

    @Test
    public void testProtectedCreateNodeDeletion() throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.newClient(server.getConnectString(), timing.session(), 1, new RetryNTimes(0, 0));
        try {
            client.start();
            for (int i = 0; i < 2; ++i) {
                CuratorFramework localClient = (i == 0) ? client : client.usingNamespace("nm");
                localClient.create().forPath("/parent");
                assertEquals(localClient.getChildren().forPath("/parent").size(), 0);
                CreateBuilderImpl createBuilder = (CreateBuilderImpl) localClient.create();
                createBuilder.failNextCreateForTesting = true;
                FindAndDeleteProtectedNodeInBackground.debugInsertError.set(true);
                try {
                    createBuilder.withProtection().forPath("/parent/test");
                    fail("failNextCreateForTesting should have caused a ConnectionLossException");
                } catch (KeeperException.ConnectionLossException ignored) {
                }
                timing.sleepABit();
                List<String> children = localClient.getChildren().forPath("/parent");
                assertEquals(children.size(), 0, children.toString());
                localClient.delete().forPath("/parent");
            }
        } finally {
            CloseableUtils.closeQuietly(client);
        }
    }

    @Test
    public void testPathsFromProtectingInBackground() throws Exception {
        for (CreateMode mode : CreateMode.values()) {
            internalTestPathsFromProtectingInBackground(mode);
        }
    }

    private void internalTestPathsFromProtectingInBackground(CreateMode mode) throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.newClient(server.getConnectString(), timing.session(), 1, new RetryOneTime(1));
        try {
            client.start();
            client.create().creatingParentsIfNeeded().forPath("/a/b/c");
            final BlockingQueue<String> paths = new ArrayBlockingQueue<>(2);
            BackgroundCallback callback = (client1, event) -> {
                paths.put(event.getName());
                paths.put(event.getPath());
            };
            final String TEST_PATH = "/a/b/c/test-";
            long ttl = timing.forWaiting().milliseconds() * 1000L;
            CreateBuilder firstCreateBuilder = client.create();
            if (mode.isTTL()) {
                firstCreateBuilder.withTtl(ttl);
            }
            firstCreateBuilder.withMode(mode).inBackground(callback).forPath(TEST_PATH);
            String name1 = timing.takeFromQueue(paths);
            String path1 = timing.takeFromQueue(paths);
            client.close();
            client = CuratorFrameworkFactory.newClient(server.getConnectString(), timing.session(), 1, new RetryOneTime(1));
            client.start();
            CreateBuilderImpl createBuilder = (CreateBuilderImpl) client.create();
            createBuilder.withProtection();
            if (mode.isTTL()) {
                createBuilder.withTtl(ttl);
            }
            client.create().forPath(createBuilder.adjustPath(TEST_PATH));
            createBuilder.debugForceFindProtectedNode = true;
            createBuilder.withMode(mode).inBackground(callback).forPath(TEST_PATH);
            String name2 = timing.takeFromQueue(paths);
            String path2 = timing.takeFromQueue(paths);
            assertEquals(ZKPaths.getPathAndNode(name1).getPath(), ZKPaths.getPathAndNode(TEST_PATH).getPath());
            assertEquals(ZKPaths.getPathAndNode(name2).getPath(), ZKPaths.getPathAndNode(TEST_PATH).getPath());
            assertEquals(ZKPaths.getPathAndNode(path1).getPath(), ZKPaths.getPathAndNode(TEST_PATH).getPath());
            assertEquals(ZKPaths.getPathAndNode(path2).getPath(), ZKPaths.getPathAndNode(TEST_PATH).getPath());
            client.delete().deletingChildrenIfNeeded().forPath("/a/b/c");
            client.delete().forPath("/a/b");
            client.delete().forPath("/a");
        } finally {
            CloseableUtils.closeQuietly(client);
        }
    }

    @Test
    public void connectionLossWithBackgroundTest() throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.newClient(server.getConnectString(), timing.session(), 1, new RetryOneTime(1));
        try {
            final CountDownLatch latch = new CountDownLatch(1);
            client.start();
            client.getZookeeperClient().blockUntilConnectedOrTimedOut();
            server.close();
            client.getChildren().inBackground((client1, event) -> latch.countDown()).forPath("/");
            assertTrue(timing.awaitLatch(latch));
        } finally {
            CloseableUtils.closeQuietly(client);
        }
    }

    @Test
    public void testReconnectAfterLoss() throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.newClient(server.getConnectString(), timing.session(), timing.connection(), new RetryOneTime(1));
        try {
            client.start();
            final CountDownLatch lostLatch = new CountDownLatch(1);
            ConnectionStateListener listener = (client1, newState) -> {
                if (newState == ConnectionState.LOST) {
                    lostLatch.countDown();
                }
            };
            client.getConnectionStateListenable().addListener(listener);
            client.checkExists().forPath("/");
            server.stop();
            assertTrue(timing.awaitLatch(lostLatch));
            try {
                client.checkExists().forPath("/");
                fail();
            } catch (KeeperException.ConnectionLossException ignored) {
            }
            server.restart();
            client.checkExists().forPath("/");
        } finally {
            CloseableUtils.closeQuietly(client);
        }
    }

    @Test
    public void testGetAclNoStat() throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.newClient(server.getConnectString(), timing.session(), timing.connection(), new RetryOneTime(1));
        client.start();
        try {
            try {
                client.getACL().forPath("/");
            } catch (NullPointerException e) {
                fail();
            }
        } finally {
            CloseableUtils.closeQuietly(client);
        }
    }

    @Test
    public void testMissedResponseOnBackgroundESCreate() throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.newClient(server.getConnectString(), timing.session(), timing.connection(), new RetryOneTime(1));
        client.start();
        try {
            CreateBuilderImpl createBuilder = (CreateBuilderImpl) client.create();
            createBuilder.failNextCreateForTesting = true;

            final BlockingQueue<String> queue = Queues.newArrayBlockingQueue(1);
            BackgroundCallback callback = (client1, event) -> queue.put(event.getPath());
            createBuilder.withProtection().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).inBackground(callback).forPath("/");
            String ourPath = queue.poll(timing.forWaiting().seconds(), TimeUnit.SECONDS);
            assertTrue(ourPath.startsWith(ZKPaths.makePath("/", ProtectedUtils.PROTECTED_PREFIX)));
            assertFalse(createBuilder.failNextCreateForTesting);
        } finally {
            CloseableUtils.closeQuietly(client);
        }
    }

    @Test
    public void testMissedResponseOnESCreate() throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.newClient(server.getConnectString(), timing.session(), timing.connection(), new RetryOneTime(1));
        client.start();
        try {
            CreateBuilderImpl createBuilder = (CreateBuilderImpl) client.create();
            createBuilder.failNextCreateForTesting = true;
            String ourPath = createBuilder.withProtection().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath("/");
            assertTrue(ourPath.startsWith(ZKPaths.makePath("/", ProtectedUtils.PROTECTED_PREFIX)));
            assertFalse(createBuilder.failNextCreateForTesting);
        } finally {
            CloseableUtils.closeQuietly(client);
        }
    }

    @Test
    public void testSessionKilled() throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.newClient(server.getConnectString(), timing.session(), timing.connection(), new RetryOneTime(1));
        client.start();
        try {
            client.create().forPath("/sessionTest");
            CountDownLatch sessionDiedLatch = new CountDownLatch(1);
            Watcher watcher = event -> {
                if (event.getState() == Watcher.Event.KeeperState.Expired) {
                    sessionDiedLatch.countDown();
                }
            };
            client.checkExists().usingWatcher(watcher).forPath("/sessionTest");
            client.getZookeeperClient().getZooKeeper().getTestable().injectSessionExpiration();
            assertTrue(timing.awaitLatch(sessionDiedLatch));
            assertNotNull(client.checkExists().forPath("/sessionTest"));
        } finally {
            CloseableUtils.closeQuietly(client);
        }
    }

    @Test
    public void testNestedCalls() throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.newClient(server.getConnectString(), timing.session(), timing.connection(), new RetryOneTime(1));
        client.start();
        try {
            client.getCuratorListenable().addListener((client1, event) -> {
                if (event.getType() == CuratorEventType.EXISTS) {
                    Stat stat = client1.checkExists().forPath("/yo/yo/yo");
                    assertNull(stat);

                    client1.create().inBackground(event.getContext()).forPath("/what");
                } else if (event.getType() == CuratorEventType.CREATE) {
                    ((CountDownLatch) event.getContext()).countDown();
                }
            });
            CountDownLatch latch = new CountDownLatch(1);
            client.checkExists().inBackground(latch).forPath("/hey");
            assertTrue(latch.await(10, TimeUnit.SECONDS));
        } finally {
            CloseableUtils.closeQuietly(client);
        }
    }

    @Test
    public void testBackgroundFailure() throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.newClient(server.getConnectString(), timing.session(), timing.connection(), new RetryOneTime(1));
        client.start();
        try {
            final CountDownLatch latch = new CountDownLatch(1);
            client.getConnectionStateListenable().addListener((client1, newState) -> {
                if (newState == ConnectionState.LOST) {
                    latch.countDown();
                }
            });
            client.checkExists().forPath("/hey");
            client.checkExists().inBackground().forPath("/hey");
            server.stop();
            client.checkExists().inBackground().forPath("/hey");
            assertTrue(timing.awaitLatch(latch));
        } finally {
            CloseableUtils.closeQuietly(client);
        }
    }

    @Test
    public void testFailure() throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.newClient(server.getConnectString(), 100, 100, new RetryOneTime(1));
        client.start();
        try {
            client.checkExists().forPath("/hey");
            client.checkExists().inBackground().forPath("/hey");
            server.stop();
            client.checkExists().forPath("/hey");
            fail();
        } catch (KeeperException.SessionExpiredException | KeeperException.ConnectionLossException ignored) {
        } finally {
            CloseableUtils.closeQuietly(client);
        }
    }

    @Test
    public void testRetry() throws Exception {
        final int MAX_RETRIES = 3;
        final CuratorFramework client = CuratorFrameworkFactory.newClient(server.getConnectString(), timing.session(), timing.connection(), new RetryOneTime(10));
        client.start();
        try {
            final AtomicInteger retries = new AtomicInteger(0);
            final Semaphore semaphore = new Semaphore(0);
            RetryPolicy policy = (retryCount, elapsedTimeMs, sleeper) -> {
                semaphore.release();
                if (retries.incrementAndGet() == MAX_RETRIES) {
                    try {
                        server.restart();
                    } catch (Exception e) {
                        throw new Error(e);
                    }
                }
                try {
                    sleeper.sleepFor(100, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return true;
            };
            client.getZookeeperClient().setRetryPolicy(policy);
            server.stop();
            client.checkExists().forPath("/hey");
            assertTrue(semaphore.tryAcquire(MAX_RETRIES, timing.forWaiting().seconds(), TimeUnit.SECONDS), "Remaining leases: " + semaphore.availablePermits());
            client.getZookeeperClient().setRetryPolicy(new RetryOneTime(100));
            client.checkExists().forPath("/hey");
            client.getZookeeperClient().setRetryPolicy(policy);
            semaphore.drainPermits();
            retries.set(0);
            server.stop();
            client.checkExists().inBackground().forPath("/hey");
            assertTrue(semaphore.tryAcquire(MAX_RETRIES, timing.forWaiting().seconds(), TimeUnit.SECONDS), "Remaining leases: " + semaphore.availablePermits());
        } finally {
            CloseableUtils.closeQuietly(client);
        }
    }

    @Test
    public void testNotStarted() {
        CuratorFramework client = CuratorFrameworkFactory.newClient(server.getConnectString(), new RetryOneTime(1));
        try {
            client.getData();
            fail();
        } catch (Exception ignored) {
        } catch (Throwable e) {
            fail("", e);
        }
    }

    @Test
    public void testStopped() {
        CuratorFramework client = CuratorFrameworkFactory.newClient(server.getConnectString(), new RetryOneTime(1));
        try {
            client.start();
            client.getData();
        } finally {
            CloseableUtils.closeQuietly(client);
        }
        try {
            client.getData();
            fail();
        } catch (Exception ignored) {
        }
    }

    @Test
    public void testDeleteChildrenConcurrently() throws Exception {
        final CuratorFramework client = CuratorFrameworkFactory.newClient(server.getConnectString(), new RetryOneTime(1));
        CuratorFramework client2 = CuratorFrameworkFactory.newClient(server.getConnectString(), new RetryOneTime(1));
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        try {
            client.start();
            client2.start();
            int childCount = 500;
            for (int i = 0; i < childCount; i++) {
                client.create().creatingParentsIfNeeded().forPath("/parent/child" + i);
            }
            final CountDownLatch latch = new CountDownLatch(1);
            executorService.submit(() -> {
                try {
                    client.delete().deletingChildrenIfNeeded().forPath("/parent");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    if (e instanceof KeeperException.NoNodeException) {
                        fail("client delete failed, shouldn't throw NoNodeException", e);
                    } else {
                        fail("unexpected exception", e);
                    }
                } finally {
                    latch.countDown();
                }
            });
            boolean threadDeleted = false;
            Random random = new Random();
            for (int i = 0; i < childCount; i++) {
                String child = "/parent/child" + random.nextInt(childCount);
                try {
                    if (!threadDeleted) {
                        Stat stat = client2.checkExists().forPath(child);
                        if (stat == null) {
                            threadDeleted = true;
                            log.info("client has deleted the child {}", child);
                        }
                    } else {
                        try {
                            client2.delete().forPath(child);
                            log.info("client2 deleted the child {} successfully", child);
                            break;
                        } catch (KeeperException.NoNodeException ignore) {
                        } catch (Exception e) {
                            fail("unexpected exception", e);
                        }
                    }
                } catch (Exception e) {
                    fail("unexpected exception", e);
                }
            }
            assertTrue(timing.awaitLatch(latch));
            assertNull(client2.checkExists().forPath("/parent"));
        } finally {
            try {
                executorService.shutdownNow();
                executorService.awaitTermination(timing.milliseconds(), TimeUnit.MILLISECONDS);
            } finally {
                CloseableUtils.closeQuietly(client);
                CloseableUtils.closeQuietly(client2);
            }
        }
    }
}