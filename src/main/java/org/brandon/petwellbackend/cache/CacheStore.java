package org.brandon.petwellbackend.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class CacheStore<K, V> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CacheStore.class);
    
    private final Cache<K, V> cache;

    public CacheStore(int expiryDuration, TimeUnit timeUnit) {
        cache = CacheBuilder.newBuilder()
                .expireAfterWrite(expiryDuration, timeUnit)
                .concurrencyLevel(Runtime.getRuntime().availableProcessors())
                .build();
    }

    public V get(@NotNull K key) {
        LOGGER.debug("Getting record from cache with key {}", key.toString());
        return cache.getIfPresent(key);
    }

    public void put(@NotNull K key, @NotNull V value) {
        LOGGER.debug("Storing record in cache for key {}", key.toString());
        cache.put(key, value);
    }

    public void evict(@NotNull K key) {
        LOGGER.debug("Evicting record from cache with key {}", key.toString());
        cache.invalidate(key);
    }
}
