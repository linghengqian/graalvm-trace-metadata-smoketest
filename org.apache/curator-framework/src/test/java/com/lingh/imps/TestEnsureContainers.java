
package com.lingh.imps;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.EnsureContainers;
import org.apache.curator.retry.RetryOneTime;
import org.apache.curator.test.BaseClassForTests;
import org.apache.curator.utils.CloseableUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TestEnsureContainers extends BaseClassForTests {
    @Test
    public void testBasic() throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.newClient(server.getConnectString(), new RetryOneTime(1));
        try {
            client.start();
            EnsureContainers ensureContainers = new EnsureContainers(client, "/one/two/three");
            ensureContainers.ensure();
            assertNotNull(client.checkExists().forPath("/one/two/three"));
        } finally {
            CloseableUtils.closeQuietly(client);
        }
    }

    @Test
    public void testSingleExecution() throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.newClient(server.getConnectString(), new RetryOneTime(1));
        try {
            client.start();
            EnsureContainers ensureContainers = new EnsureContainers(client, "/one/two/three");
            ensureContainers.ensure();
            assertNotNull(client.checkExists().forPath("/one/two/three"));
            client.delete().forPath("/one/two/three");
            ensureContainers.ensure();
            assertNull(client.checkExists().forPath("/one/two/three"));
        } finally {
            CloseableUtils.closeQuietly(client);
        }
    }
}
