package com.lingh.fixture;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.curator.test.TestingServer;
import org.apache.shardingsphere.elasticjob.reg.exception.RegExceptionHandler;

import java.io.File;
import java.io.IOException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EmbedTestingServer {
    private static final int PORT = 7181;
    
    private static volatile TestingServer testingServer;

    public static String getConnectionString() {
        return "localhost:" + PORT;
    }

    public static void start() {
        if (null != testingServer) {
            return;
        }
        try {
            testingServer = new TestingServer(PORT, new File(String.format("build/resources/test/test_zk_data/%s/", System.nanoTime())));
        } catch (final Exception ex) {
            RegExceptionHandler.handleException(ex);
        } finally {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    testingServer.close();
                } catch (final IOException ex) {
                    RegExceptionHandler.handleException(ex);
                }
            }));
        }
    }
}

