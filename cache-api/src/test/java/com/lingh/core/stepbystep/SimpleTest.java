package com.lingh.core.stepbystep;

import org.junit.jupiter.api.Test;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.configuration.MutableCacheEntryListenerConfiguration;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.event.CacheEntryCreatedListener;
import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryListenerException;
import javax.cache.event.CacheEntryUpdatedListener;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.MutableEntry;
import javax.cache.spi.CachingProvider;

import static org.assertj.core.api.Assertions.assertThat;

public class SimpleTest {
    @Test
    void testSimple() {
        CachingProvider provider = Caching.getCachingProvider();
        CacheManager manager = provider.getCacheManager();
        MutableConfiguration<String, String> configuration = new MutableConfiguration<String, String>()
                .setStoreByValue(false).setTypes(String.class, String.class);
        Cache<String, String> cache = manager.createCache("my-cache", configuration);
        CacheEntryListenerConfiguration<String, String> listenerConfiguration = new MutableCacheEntryListenerConfiguration<>(
                FactoryBuilder.factoryOf(MyCacheEntryListener.class),
                null, false, true
        );
        cache.registerCacheEntryListener(listenerConfiguration);
        cache.put("message", "hello");
        cache.put("message", "g'day");
        cache.put("message", "bonjour");
        String result = cache.invoke("message", new AbstractEntryProcessor<>() {
            @Override
            public String process(MutableEntry<String, String> entry,
                                  Object... arguments) throws EntryProcessorException {
                return entry.exists() ? entry.getValue().toUpperCase() : null;
            }
        });
        assertThat(result).isEqualTo("BONJOUR");
        assertThat(cache.get("message")).isEqualTo("bonjour");
    }

    public static class MyCacheEntryListener implements CacheEntryCreatedListener<String, String>, CacheEntryUpdatedListener<String, String> {
        @Override
        public void onCreated(Iterable<CacheEntryEvent<? extends String, ? extends String>> cacheEntryEvents)
                throws CacheEntryListenerException {
            for (CacheEntryEvent<? extends String, ? extends String> entryEvent : cacheEntryEvents) {
                System.out.println("Created: " + entryEvent.getKey() + " with value: " + entryEvent.getValue());
            }
        }

        @Override
        public void onUpdated(Iterable<CacheEntryEvent<? extends String, ? extends String>> cacheEntryEvents)
                throws CacheEntryListenerException {
            for (CacheEntryEvent<? extends String, ? extends String> entryEvent : cacheEntryEvents) {
                System.out.println("Updated: " + entryEvent.getKey() + " with value: " + entryEvent.getValue());
            }
        }
    }
}
