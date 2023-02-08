

package org.apache.curator.framework.recipes.leader;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.framework.api.UnhandledErrorListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.BaseClassForTests;
import org.apache.curator.test.Timing;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestLeaderAcls extends BaseClassForTests
{
    private final Timing timing = new Timing();

    @Test
    @DisplayName("Validation test for CURATOR-365")
    public void testAclErrorWithLeader() throws Exception
    {
        ACLProvider provider = new ACLProvider()
        {
            @Override
            public List<ACL> getDefaultAcl()
            {
                return ZooDefs.Ids.OPEN_ACL_UNSAFE;
            }

            @Override
            public List<ACL> getAclForPath(String path)
            {
                if ( path.equals("/base") )
                {
                    try
                    {
                        String testDigest = DigestAuthenticationProvider.generateDigest("test:test");
                        return Collections.singletonList(new ACL(ZooDefs.Perms.ALL, new Id("digest", testDigest)));
                    }
                    catch ( NoSuchAlgorithmException e )
                    {
                        e.printStackTrace();
                    }
                }
                return getDefaultAcl();
            }
        };

        RetryPolicy retryPolicy = new ExponentialBackoffRetry(timing.milliseconds(), 3);
        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
            .connectString(server.getConnectString())
            .retryPolicy(retryPolicy)
            .aclProvider(provider)
            .authorization("digest", "test:test".getBytes())
            ;
        CuratorFramework client = builder.build();
        LeaderLatch latch = null;
        try
        {
            client.start();

            latch = new LeaderLatch(client, "/base");
            latch.start();
            assertTrue(latch.await(timing.forWaiting().seconds(), TimeUnit.SECONDS));
            latch.close();
            latch = null;

            CuratorFramework noAuthClient = CuratorFrameworkFactory.newClient(server.getConnectString(), retryPolicy);
            try
            {
                noAuthClient.start();

                final CountDownLatch noAuthLatch = new CountDownLatch(1);
                UnhandledErrorListener listener = new UnhandledErrorListener()
                {
                    @Override
                    public void unhandledError(String message, Throwable e)
                    {
                        if ( e instanceof KeeperException.NoAuthException )
                        {
                            noAuthLatch.countDown();
                        }
                    }
                };
                noAuthClient.getUnhandledErrorListenable().addListener(listener);

                // use a path below "base" as noAuthClient is not authorized to create nodes in "/base"
                // but also making sure that the code goes through the backgroundCreateParentsThenNode() codepath
                latch = new LeaderLatch(noAuthClient, "/base/second");
                latch.start();
                assertTrue(timing.awaitLatch(noAuthLatch));
            }
            finally
            {
                CloseableUtils.closeQuietly(noAuthClient);
            }
        }
        finally
        {
            CloseableUtils.closeQuietly(latch);
            CloseableUtils.closeQuietly(client);
        }
    }
}
