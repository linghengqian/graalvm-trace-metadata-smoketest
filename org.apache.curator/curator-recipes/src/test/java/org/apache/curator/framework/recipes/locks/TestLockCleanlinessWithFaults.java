
package org.apache.curator.framework.recipes.locks;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.TestCleanState;
import org.apache.curator.retry.RetryNTimes;
import org.apache.curator.test.BaseClassForTests;
import org.apache.zookeeper.KeeperException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class TestLockCleanlinessWithFaults extends BaseClassForTests
{
    @Test
    public void     testNodeDeleted() throws Exception
    {
        final String PATH = "/foo/bar";

        CuratorFramework        client = null;
        try
        {
            client = CuratorFrameworkFactory.newClient(server.getConnectString(), new RetryNTimes(0, 0));
            client.start();

            client.create().creatingParentsIfNeeded().forPath(PATH);
            assertEquals(client.checkExists().forPath(PATH).getNumChildren(), 0);

            LockInternals       internals = new LockInternals(client, new StandardLockInternalsDriver(), PATH, "lock-", 1)
            {
                @Override
                List<String> getSortedChildren() throws Exception
                {
                    throw new KeeperException.NoNodeException();
                }
            };
            try
            {
                internals.attemptLock(0, null, null);
                fail();
            }
            catch ( KeeperException.NoNodeException dummy )
            {
                // expected
            }

            // make sure no nodes are left lying around
            assertEquals(client.checkExists().forPath(PATH).getNumChildren(), 0);
        }
        finally
        {
            TestCleanState.closeAndTestClean(client);
        }
    }
}
