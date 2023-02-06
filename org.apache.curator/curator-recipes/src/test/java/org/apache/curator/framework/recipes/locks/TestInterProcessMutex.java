

package org.apache.curator.framework.recipes.locks;

import com.google.common.collect.Lists;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.TestCleanState;
import org.apache.curator.framework.schema.Schema;
import org.apache.curator.framework.schema.SchemaSet;
import org.apache.curator.retry.RetryOneTime;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.CreateMode;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

public class TestInterProcessMutex extends TestInterProcessMutexBase
{
    private static final String LOCK_PATH = LOCK_BASE_PATH + "/our-lock";

    @Override
    protected InterProcessLock makeLock(CuratorFramework client)
    {
        return new InterProcessMutex(client, LOCK_PATH);
    }

    @Test
    public void testWithSchema() throws Exception
    {
        Schema schemaRoot = Schema.builderForRecipeParent("/foo").name("root").build();
        Schema schemaLocks = Schema.builderForRecipe("/foo").name("locks").build();
        SchemaSet schemaSet = new SchemaSet(Lists.newArrayList(schemaRoot, schemaLocks), false);
        CuratorFramework client = CuratorFrameworkFactory.builder()
            .connectString(server.getConnectString())
            .retryPolicy(new RetryOneTime(1))
            .schemaSet(schemaSet)
            .build();
        try
        {
            client.start();

            InterProcessMutex lock = new InterProcessMutex(client, "/foo");
            lock.acquire();
            lock.release();
        }
        finally
        {
            CloseableUtils.closeQuietly(client);
        }
    }

    @Test
    public void testRevoking() throws Exception
    {
        final CuratorFramework client = CuratorFrameworkFactory.newClient(server.getConnectString(), new RetryOneTime(1));
        try
        {
            client.start();
            final InterProcessMutex lock = new InterProcessMutex(client, LOCK_PATH);

            ExecutorService executorService = Executors.newCachedThreadPool();

            final CountDownLatch revokeLatch = new CountDownLatch(1);
            final CountDownLatch lockLatch = new CountDownLatch(1);
            Future<Void> f1 = executorService.submit
                (
                    new Callable<Void>()
                    {
                        @Override
                        public Void call() throws Exception
                        {
                            RevocationListener<InterProcessMutex> listener = new RevocationListener<InterProcessMutex>()
                            {
                                @Override
                                public void revocationRequested(InterProcessMutex lock)
                                {
                                    revokeLatch.countDown();
                                }
                            };
                            lock.makeRevocable(listener);
                            lock.acquire();
                            lockLatch.countDown();
                            revokeLatch.await();
                            lock.release();
                            return null;
                        }
                    }
                );

            Future<Void> f2 = executorService.submit
                (
                    new Callable<Void>()
                    {
                        @Override
                        public Void call() throws Exception
                        {
                            assertTrue(lockLatch.await(10, TimeUnit.SECONDS));
                            Collection<String> nodes = lock.getParticipantNodes();
                            assertEquals(nodes.size(), 1);
                            Revoker.attemptRevoke(client, nodes.iterator().next());

                            InterProcessMutex l2 = new InterProcessMutex(client, LOCK_PATH);
                            assertTrue(l2.acquire(5, TimeUnit.SECONDS));
                            l2.release();
                            return null;
                        }
                    }
                );

            f2.get();
            f1.get();
        }
        finally
        {
            TestCleanState.closeAndTestClean(client);
        }
    }

    @Test
    public void testPersistentLock() throws Exception
    {
        final CuratorFramework client = CuratorFrameworkFactory.newClient(server.getConnectString(), new RetryOneTime(1));
        client.start();

        try
        {
            final InterProcessMutex lock = new InterProcessMutex(client, LOCK_PATH, new StandardLockInternalsDriver()
            {
                @Override
                public String createsTheLock(CuratorFramework client, String path, byte[] lockNodeBytes) throws Exception
                {
                    String ourPath;
                    if ( lockNodeBytes != null )
                    {
                        ourPath = client.create().creatingParentsIfNeeded().withProtection().withMode(CreateMode.PERSISTENT).forPath(path, lockNodeBytes);
                    }
                    else
                    {
                        ourPath = client.create().creatingParentsIfNeeded().withProtection().withMode(CreateMode.PERSISTENT).forPath(path);
                    }
                    return ourPath;
                }
            });

            // Get a persistent lock
            lock.acquire(10, TimeUnit.SECONDS);
            assertTrue(lock.isAcquiredInThisProcess());

            // Kill the session, check that lock node still exists
            client.getZookeeperClient().getZooKeeper().getTestable().injectSessionExpiration();
            assertNotNull(client.checkExists().forPath(LOCK_PATH));

            // Release the lock and verify that the actual lock node created no longer exists
            String actualLockPath = lock.getLockPath();
            lock.release();
            assertNull(client.checkExists().forPath(actualLockPath));
        }
        finally
        {
            TestCleanState.closeAndTestClean(client);
        }
    }
}
