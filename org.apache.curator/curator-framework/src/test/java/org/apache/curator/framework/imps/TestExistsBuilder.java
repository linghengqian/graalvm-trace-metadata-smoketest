
package org.apache.curator.framework.imps;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.retry.RetryOneTime;
import org.apache.curator.test.BaseClassForTests;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.apache.zookeeper.ZooDefs.Ids.ANYONE_ID_UNSAFE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestExistsBuilder extends BaseClassForTests {
    @Test
    public void testExistsWithParentsWithAclApplyToParents() throws Exception {
        CuratorFramework client = createClient(new DefaultACLProvider());
        try {
            client.start();
            String path = "/bar/foo/test";
            List<ACL> acl = Collections.singletonList(new ACL(ZooDefs.Perms.CREATE | ZooDefs.Perms.READ, ANYONE_ID_UNSAFE));
            assertNull(client.checkExists().creatingParentsIfNeeded().withACL(acl).forPath(path));
            List<ACL> actual_bar = client.getACL().forPath("/bar");
            assertEquals(actual_bar, acl);
            List<ACL> actual_bar_foo = client.getACL().forPath("/bar/foo");
            assertEquals(actual_bar_foo, acl);
        } finally {
            CloseableUtils.closeQuietly(client);
        }
    }

    @Test
    public void testExistsWithParentsWithAclApplyToParentsInBackground() throws Exception {
        CuratorFramework client = createClient(new DefaultACLProvider());
        try {
            client.start();
            final CountDownLatch latch = new CountDownLatch(1);
            String path = "/bar/foo/test";
            List<ACL> acl = Collections.singletonList(new ACL(ZooDefs.Perms.CREATE | ZooDefs.Perms.READ, ANYONE_ID_UNSAFE));
            BackgroundCallback callback = (client1, event) -> latch.countDown();
            client.checkExists().creatingParentsIfNeeded().withACL(acl).inBackground(callback).forPath(path);
            assertTrue(latch.await(2000, TimeUnit.MILLISECONDS), "Callback not invoked");
            List<ACL> actual_bar = client.getACL().forPath("/bar");
            assertEquals(actual_bar, acl);
            List<ACL> actual_bar_foo = client.getACL().forPath("/bar/foo");
            assertEquals(actual_bar_foo, acl);
        } finally {
            CloseableUtils.closeQuietly(client);
        }
    }

    private CuratorFramework createClient(ACLProvider aclProvider) {
        return CuratorFrameworkFactory.builder().
                aclProvider(aclProvider).
                connectString(server.getConnectString()).
                retryPolicy(new RetryOneTime(1)).
                build();
    }
}
