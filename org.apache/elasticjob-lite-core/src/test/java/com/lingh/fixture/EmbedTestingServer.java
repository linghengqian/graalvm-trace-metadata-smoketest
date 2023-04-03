package com.lingh.fixture;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.shardingsphere.elasticjob.reg.exception.RegExceptionHandler;
import org.awaitility.Awaitility;

import java.io.File;
import java.time.Duration;

public final class EmbedTestingServer {
    private static final int PORT = 7181;
    private static Process process;

    private EmbedTestingServer() {
    }

    public static String getConnectionString() {
        return "localhost:" + PORT;
    }

    public static void start() {
        if (process != null && process.isAlive()) {
            return;
        }
        try {
            System.out.println("Starting Zookeeper ...");
            process = new ProcessBuilder("docker", "run", "--rm", "-p", PORT + ":2181", "-e", "JVMFLAGS=-Xmx1024m", "zookeeper:3.8.1")
                    .redirectOutput(new File("zookeeper-stdout.txt"))
                    .redirectError(new File("zookeeper-stderr.txt"))
                    .start();
            Awaitility.await().atMost(Duration.ofMinutes(1)).ignoreExceptions().until(() -> {
                CuratorFramework client = CuratorFrameworkFactory.builder()
                        .connectString(getConnectionString())
                        .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                        .build();
                client.start();
                client.close();
                return true;
            });
        } catch (final Exception ex) {
            RegExceptionHandler.handleException(ex);
        } finally {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    if (process != null && process.isAlive()) {
                        System.out.println("Shutting down Zookeeper");
                        process.destroy();
                    }
                } catch (final Exception ex) {
                    RegExceptionHandler.handleException(ex);
                }
            }));
        }
    }
}

