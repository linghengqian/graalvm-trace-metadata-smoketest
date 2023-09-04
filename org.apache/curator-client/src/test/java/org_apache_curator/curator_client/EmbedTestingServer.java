package org_apache_curator.curator_client;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.apache.zookeeper.KeeperException.ConnectionLossException;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.KeeperException.NodeExistsException;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public final class EmbedTestingServer {
    private static final int PORT = 3181;
    private static volatile TestingServer testingServer;
    private static final Object INIT_LOCK = new Object();

    private EmbedTestingServer() {
    }

    public static void start() {
        if (null != testingServer) {
            return;
        }
        synchronized (INIT_LOCK) {
            if (null != testingServer) {
                return;
            }
            try {
                testingServer = new TestingServer(PORT, true);
            } catch (final Exception ex) {
                if (!(ex instanceof ConnectionLossException || ex instanceof NoNodeException || ex instanceof NodeExistsException)) {
                    throw new RuntimeException(ex);
                }
            } finally {
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    try {
                        testingServer.close();
                    } catch (final IOException ignored) {
                    }
                }));
            }
            try (CuratorFramework client = CuratorFrameworkFactory.builder().connectString(getConnectString())
                    .retryPolicy(new ExponentialBackoffRetry(500, 3, 500 * 3))
                    .namespace("test")
                    .sessionTimeoutMs(60 * 1000)
                    .connectionTimeoutMs(500)
                    .build()) {
                client.start();
                int round = 0;
                while (round < 60) {
                    try {
                        if (client.getZookeeperClient().isConnected()) {
                            break;
                        }
                        if (client.blockUntilConnected(500, TimeUnit.MILLISECONDS)) {
                            break;
                        }
                    } catch (final Exception ignored) {
                    }
                    ++round;
                }
            }
        }
    }

    public static String getConnectString() {
        return "localhost:" + PORT;
    }
}
