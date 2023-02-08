
package org.apache.curator.framework.recipes.locks;

import org.apache.curator.framework.CuratorFramework;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class TestInterProcessSemaphoreMutex extends TestInterProcessMutexBase
{
    private static final String LOCK_PATH = LOCK_BASE_PATH + "/our-lock";

    @Override
    @Test
    @Disabled
    public void testReentrant()
    {
    }

    @Override
    @Test
    @Disabled
    public void testReentrant2Threads()
    {
    }

    @Override
    @Test
    @Disabled
    public void testReentrantSingleLock()
    {
    }

    @Override
    protected InterProcessLock makeLock(CuratorFramework client)
    {
        return new InterProcessSemaphoreMutex(client, LOCK_PATH);
    }
}
