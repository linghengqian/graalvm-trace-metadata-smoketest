package com.lingh.internal.server;

import org.apache.shardingsphere.elasticjob.infra.handler.sharding.JobInstance;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.lite.internal.server.ServerNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class ServerNodeTest {
    private final ServerNode serverNode = new ServerNode("test_job");

    @BeforeAll
    public static void setUp() {
        JobRegistry.getInstance().addJobInstance("test_job", new JobInstance("127.0.0.1@-@0", null, "127.0.0.1"));
    }

    @Test
    public void assertIsServerPath() {
        assertTrue(serverNode.isServerPath("/test_job/servers/127.0.0.1"));
    }

    @Test
    public void assertIsNotServerPath() {
        assertFalse(serverNode.isServerPath("/test_job/servers/255.255.255.256"));
    }

    @Test
    public void assertIsLocalServerPath() {
        assertTrue(serverNode.isLocalServerPath("/test_job/servers/127.0.0.1"));
    }

    @Test
    public void assertIsNotLocalServerPath() {
        assertFalse(serverNode.isLocalServerPath("/test_job/servers/127.0.0.2"));
    }
}
