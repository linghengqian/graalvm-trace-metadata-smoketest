
package org.apache.curator.framework.recipes.locks;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.TestCleanState;
import org.apache.curator.retry.RetryOneTime;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

public class TestInterProcessMultiMutex extends TestInterProcessMutexBase
{
    private static final String     LOCK_PATH_1 = LOCK_BASE_PATH + "/our-lock-1";
    private static final String     LOCK_PATH_2 = LOCK_BASE_PATH + "/our-lock-2";

    @Override
    protected InterProcessLock makeLock(CuratorFramework client)
    {
        return new InterProcessMultiLock(client, Arrays.asList(LOCK_PATH_1, LOCK_PATH_2));
    }

    @Test
    public void testSomeReleasesFail() throws IOException
    {
        CuratorFramework client = CuratorFrameworkFactory.newClient(server.getConnectString(), new RetryOneTime(1));
        client.start();
        try
        {
            InterProcessLock        goodLock = new InterProcessMutex(client, LOCK_PATH_1);
            final InterProcessLock  otherGoodLock = new InterProcessMutex(client, LOCK_PATH_2);
            InterProcessLock        badLock = new InterProcessLock()
            {
                @Override
                public void acquire() throws Exception
                {
                    otherGoodLock.acquire();
                }

                @Override
                public boolean acquire(long time, TimeUnit unit) throws Exception
                {
                    return otherGoodLock.acquire(time, unit);
                }

                @Override
                public void release() throws Exception
                {
                    throw new Exception("foo");
                }

                @Override
                public boolean isAcquiredInThisProcess()
                {
                    return otherGoodLock.isAcquiredInThisProcess();
                }
            };

            InterProcessMultiLock       lock = new InterProcessMultiLock(Arrays.asList(goodLock, badLock));
            try
            {
                lock.acquire();
                lock.release();
                fail();
            }
            catch ( Exception e )
            {
                // ignore
            }
            assertFalse(goodLock.isAcquiredInThisProcess());
            assertTrue(otherGoodLock.isAcquiredInThisProcess());
        }
        finally
        {
            TestCleanState.closeAndTestClean(client);
        }
    }

    @Test
    public void testSomeLocksFailToLock() throws IOException
    {
        CuratorFramework client = CuratorFrameworkFactory.newClient(server.getConnectString(), new RetryOneTime(1));
        client.start();
        try
        {
            final AtomicBoolean     goodLockWasLocked = new AtomicBoolean(false);
            final InterProcessLock  goodLock = new InterProcessMutex(client, LOCK_PATH_1);
            InterProcessLock        badLock = new InterProcessLock()
            {
                @Override
                public void acquire() throws Exception
                {
                    if ( goodLock.isAcquiredInThisProcess() )
                    {
                        goodLockWasLocked.set(true);
                    }
                    throw new Exception("foo");
                }

                @Override
                public boolean acquire(long time, TimeUnit unit) throws Exception
                {
                    throw new Exception("foo");
                }

                @Override
                public void release() throws Exception
                {
                    throw new Exception("foo");
                }

                @Override
                public boolean isAcquiredInThisProcess()
                {
                    return false;
                }
            };

            InterProcessMultiLock       lock = new InterProcessMultiLock(Arrays.asList(goodLock, badLock));
            try
            {
                lock.acquire();
                fail();
            }
            catch ( Exception e )
            {
                // ignore
            }
            assertFalse(goodLock.isAcquiredInThisProcess());
            assertTrue(goodLockWasLocked.get());
        }
        finally
        {
            TestCleanState.closeAndTestClean(client);
        }
    }
}
