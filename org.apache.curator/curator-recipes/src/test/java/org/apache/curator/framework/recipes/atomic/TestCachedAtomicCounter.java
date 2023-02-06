
package org.apache.curator.framework.recipes.atomic;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.apache.curator.test.BaseClassForTests;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class TestCachedAtomicCounter extends BaseClassForTests
{
    @Test
    public void         testWithError() throws Exception
    {
        final int        FACTOR = 100;

        CuratorFramework client = CuratorFrameworkFactory.newClient(server.getConnectString(), new RetryOneTime(1));
        client.start();
        try
        {
            AtomicValue<Long>                           value = new MutableAtomicValue<Long>(0L, (long)FACTOR, true);
            final AtomicReference<AtomicValue<Long>>    fakeValueRef = new AtomicReference<AtomicValue<Long>>(value);
            DistributedAtomicLong dal = new DistributedAtomicLong(client, "/", null, null)
            {
                @Override
                public AtomicValue<Long> trySet(Long newValue) throws Exception
                {
                    return fakeValueRef.get();
                }

                @Override
                public AtomicValue<Long> get() throws Exception
                {
                    return fakeValueRef.get();
                }

                @Override
                public AtomicValue<Long> increment() throws Exception
                {
                    return fakeValueRef.get();
                }

                @Override
                public AtomicValue<Long> decrement() throws Exception
                {
                    return fakeValueRef.get();
                }

                @Override
                public AtomicValue<Long> add(Long delta) throws Exception
                {
                    return fakeValueRef.get();
                }

                @Override
                public AtomicValue<Long> subtract(Long delta) throws Exception
                {
                    return fakeValueRef.get();
                }

                @Override
                public void forceSet(Long newValue) throws Exception
                {
                }

                @Override
                public AtomicValue<Long> compareAndSet(Long expectedValue, Long newValue) throws Exception
                {
                    return fakeValueRef.get();
                }
            };
            CachedAtomicLong cachedLong = new CachedAtomicLong(dal, FACTOR);
            for ( int i = 0; i < FACTOR; ++i )
            {
                value = cachedLong.next();
                assertTrue(value.succeeded());
                assertEquals(value.preValue().longValue(), i);
                assertEquals(value.postValue().longValue(), i + 1);

                if ( i == 0 )
                {
                    MutableAtomicValue<Long> badValue = new MutableAtomicValue<Long>(0L, 0L);
                    badValue.succeeded = false;
                    fakeValueRef.set(badValue);
                }
            }

            value = cachedLong.next();
            assertFalse(value.succeeded());
        }
        finally
        {
            client.close();
        }
        }

    @Test
    public void         testBasic() throws Exception
    {
        CuratorFramework client = CuratorFrameworkFactory.newClient(server.getConnectString(), new RetryOneTime(1));
        client.start();
        try
        {
            DistributedAtomicLong dal = new DistributedAtomicLong(client, "/counter", new RetryOneTime(1));
            CachedAtomicLong cachedLong = new CachedAtomicLong(dal, 100);
            for ( long i = 0; i < 200; ++i )
            {
                AtomicValue<Long>       value = cachedLong.next();
                assertTrue(value.succeeded());
                assertEquals(value.preValue().longValue(), i);
                assertEquals(value.postValue().longValue(), i + 1);
            }
        }
        finally
        {
            client.close();
        }
    }
}
