

package org.apache.curator.framework.recipes.locks;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.framework.imps.TestCleanState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.BaseClassForTests;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestLockACLs extends BaseClassForTests
{
    private static final List<ACL> ACLS1 = Collections.singletonList(new ACL(ZooDefs.Perms.ALL, new Id("ip", "127.0.0.1")));
    private static final List<ACL> ACLS2 = Collections.singletonList(new ACL(ZooDefs.Perms.CREATE | ZooDefs.Perms.READ, new Id("ip", "127.0.0.1")));

    private CuratorFramework createClient(ACLProvider provider) throws Exception
    {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory.builder()
            .namespace("ns")
            .connectString(server.getConnectString())
            .retryPolicy(retryPolicy)
            .aclProvider(provider)
            .build();
        client.start();
        return client;
    }

    @Test
    public void testLockACLs() throws Exception
    {
        CuratorFramework client = createClient(new TestLockACLsProvider());
        try
        {
            client.create().forPath("/foo");
            assertNotNull(client.checkExists().forPath("/foo"));
            assertEquals(ZooDefs.Perms.ALL, client.getACL().forPath("/foo").get(0).getPerms());
            assertEquals("ip", client.getACL().forPath("/foo").get(0).getId().getScheme());
            assertEquals("127.0.0.1", client.getACL().forPath("/foo").get(0).getId().getId());

            InterProcessReadWriteLock lock = new InterProcessReadWriteLock(client, "/bar");
            InterProcessMutex writeLock = lock.writeLock();
            writeLock.acquire();
            assertNotNull(client.checkExists().forPath("/bar"));
            assertEquals(ZooDefs.Perms.ALL, client.getACL().forPath("/bar").get(0).getPerms());
            assertEquals("ip", client.getACL().forPath("/bar").get(0).getId().getScheme());
            assertEquals("127.0.0.1", client.getACL().forPath("/bar").get(0).getId().getId());
        }
        finally
        {
            TestCleanState.closeAndTestClean(client);
        }
    }

    @Test
    public void testACLsCreatingParents() throws Exception
    {
        CuratorFramework client = createClient(new TestACLsCreatingParentsProvider());
        try
        {
            client.create().creatingParentsIfNeeded().forPath("/parent/foo");
            assertEquals(ZooDefs.Perms.CREATE | ZooDefs.Perms.READ, client.getACL().forPath("/parent").get(0).getPerms());
            assertEquals(ZooDefs.Perms.ALL, client.getACL().forPath("/parent/foo").get(0).getPerms());
        }
        finally
        {
            CloseableUtils.closeQuietly(client);
        }
    }

    private class TestACLsCreatingParentsProvider implements ACLProvider
    {
        @Override
        public List<ACL> getDefaultAcl()
        {
            return ACLS1;
        }

        @Override
        public List<ACL> getAclForPath(String path)
        {
            if ( path.equals("/ns/parent") )
            {
                return ACLS2;
            }
            return ACLS1;
        }
    }

    private class TestLockACLsProvider implements ACLProvider
    {
        @Override
        public List<ACL> getDefaultAcl()
        {
            return ACLS1;
        }

        @Override
        public List<ACL> getAclForPath(String path)
        {
            return ACLS1;
        }
    }
}
