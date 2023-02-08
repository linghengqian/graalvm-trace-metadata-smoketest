

package org.apache.curator.framework.recipes.cache;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.apache.curator.test.compatibility.CuratorTestBase;
import org.apache.curator.utils.Compatibility;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestCuratorCacheBridge extends CuratorTestBase
{
    @Test
    public void testImplementationSelection()
    {
        try (CuratorFramework client = CuratorFrameworkFactory.newClient(server.getConnectString(), new RetryOneTime(1)))
        {
            CuratorCacheBridge cache = CuratorCache.bridgeBuilder(client, "/foo").build();
            if ( Compatibility.hasPersistentWatchers() )
            {
                assertTrue(cache instanceof CuratorCacheImpl);
                assertTrue(cache.isCuratorCache());
            }
            else
            {
                assertTrue(cache instanceof CompatibleCuratorCacheBridge);
                assertFalse(cache.isCuratorCache());
            }
        }
    }

    @Test
    public void testForceTreeCache()
    {
        System.setProperty("curator-cache-bridge-force-tree-cache", "true");
        try (CuratorFramework client = CuratorFrameworkFactory.newClient(server.getConnectString(), new RetryOneTime(1)))
        {
            CuratorCacheBridge cache = CuratorCache.bridgeBuilder(client, "/foo").build();
            assertTrue(cache instanceof CompatibleCuratorCacheBridge);
            assertFalse(cache.isCuratorCache());
        }
        finally
        {
            System.clearProperty("curator-cache-bridge-force-tree-cache");
        }
    }
}
