package com.lingh;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.ResourceType;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.xml.XmlConfiguration;
import org.ehcache.xml.multi.XmlMultiConfiguration;
import org.junit.jupiter.api.Test;

import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

public class XMLConfigurationTest {
    @Test
    void testXMLProgrammaticParsingAndProgrammaticConfigurationToXML() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        URL resource = getClass().getResource("/template-sample.xml");
        assertThat(resource).isNotNull();
        CacheConfigurationBuilder<Long, String> configurationBuilder = new XmlConfiguration(resource)
                .newCacheConfigurationBuilderFromTemplate("example", Long.class, String.class)
                .withResourcePools(ResourcePoolsBuilder.heap(1000L));
        try (CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                .withCache("myCache", configurationBuilder)
                .build(true)) {
            Cache<Long, String> myCache = cacheManager.getCache("myCache", Long.class, String.class);
            myCache.put(1L, "da one!");
            assertThat(myCache.get(1L)).isEqualTo("da one!");
            assertThat(myCache.getRuntimeConfiguration().getResourcePools().getPoolForResource(ResourceType.Core.HEAP).getSize()).isEqualTo(1000L);
            assertThat(new XmlConfiguration(cacheManager.getRuntimeConfiguration()).toString())
                    .startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
        }
    }

    @Test
    void testMultipleXMLConfigurationsInOneDocument() {
        URL resource = getClass().getResource("/multiple-managers.xml");
        assertThat(resource).isNotNull();
        assertThat(XmlMultiConfiguration.from(resource).build().configuration("foo-manager")).isNotNull();
    }

    @Test
    void testMultipleEhcacheManagerConfigurations() {
        URL resource = getClass().getResource("/multiple-variants.xml");
        assertThat(resource).isNotNull();
        assertThat(XmlMultiConfiguration.from(resource).build().configuration("foo-manager", "offHeap")).isNotNull();
    }
}
