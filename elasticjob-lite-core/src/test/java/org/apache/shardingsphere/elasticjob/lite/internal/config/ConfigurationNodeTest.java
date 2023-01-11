package org.apache.shardingsphere.elasticjob.lite.internal.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class ConfigurationNodeTest {
    private final ConfigurationNode configurationNode = new ConfigurationNode("test_job");

    @Test
    public void assertIsConfigPath() {
        Assertions.assertTrue(configurationNode.isConfigPath("/test_job/config"));
    }
}
