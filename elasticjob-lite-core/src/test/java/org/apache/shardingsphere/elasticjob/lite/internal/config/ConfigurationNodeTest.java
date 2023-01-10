package org.apache.shardingsphere.elasticjob.lite.internal.config;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public final class ConfigurationNodeTest {
    
    private final ConfigurationNode configurationNode = new ConfigurationNode("test_job");
    
    @Test
    public void assertIsConfigPath() {
        assertTrue(configurationNode.isConfigPath("/test_job/config"));
    }
}
