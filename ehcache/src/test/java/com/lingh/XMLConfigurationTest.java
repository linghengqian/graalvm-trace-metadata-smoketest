package com.lingh;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.ResourceType;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.xml.XmlConfiguration;
import org.junit.jupiter.api.Test;

import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

public class XMLConfigurationTest {
    @Test
    void testXMLProgrammaticParsingAndProgrammaticConfigurationToXML() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        URL resource = getClass().getResource("/xml-programmatic-parsing.xml");
        assertThat(resource).isNotNull();
        CacheConfigurationBuilder<Long, String> configurationBuilder = new XmlConfiguration(resource)
                .newCacheConfigurationBuilderFromTemplate("example", Long.class, String.class)
                .withResourcePools(ResourcePoolsBuilder.heap(1000L));
        try (CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true)) {
            Cache<Long, String> myCache = cacheManager.createCache("myCache", configurationBuilder);
            myCache.put(1L, "da one!");
            assertThat(myCache.get(1L)).isEqualTo("da one!");
            assertThat(myCache.getRuntimeConfiguration().getResourcePools().getPoolForResource(ResourceType.Core.HEAP).getSize()).isEqualTo(1000L);
            assertThat(new XmlConfiguration(cacheManager.getRuntimeConfiguration()).toString())
                    .startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
        }
    }
}
