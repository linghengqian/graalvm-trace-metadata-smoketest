
package org.apache.curator.framework.recipes.cache;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.test.compatibility.CuratorTestBase;
import org.junit.jupiter.api.Tag;

import java.util.concurrent.BlockingQueue;

@Tag(CuratorTestBase.zk36Group)
public class TestCuratorCacheEventOrdering extends TestEventOrdering<CuratorCache>
{
    @Override
    protected int getActualQty(CuratorCache cache)
    {
        return cache.size();
    }

    @Override
    protected CuratorCache newCache(CuratorFramework client, String path, BlockingQueue<Event> events)
    {
        CuratorCache cache = CuratorCache.build(client, path);
        cache.listenable().addListener((type, oldNode, node) -> {
            if ( type == CuratorCacheListener.Type.NODE_CREATED )
            {
                events.add(new Event(EventType.ADDED, node.getPath()));
            }
            else if ( type == CuratorCacheListener.Type.NODE_DELETED )
            {
                events.add(new Event(EventType.DELETED, oldNode.getPath()));
            }
        });
        cache.start();
        return cache;
    }
}
