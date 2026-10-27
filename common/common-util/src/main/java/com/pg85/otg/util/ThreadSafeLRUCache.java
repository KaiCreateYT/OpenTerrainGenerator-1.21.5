package com.pg85.otg.util;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.Map;
import java.util.function.Function;

/**
 * High-performance thread-safe LRU cache backed by Caffeine.
 * Lock-free reads, minimal contention on writes.
 */
public class ThreadSafeLRUCache<K, V> {
    private final Cache<K, V> cache;

    public ThreadSafeLRUCache(int maxSize) {
        this.cache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .build();
    }

    public V get(K key) {
        return cache.getIfPresent(key);
    }

    public void put(K key, V value) {
        cache.put(key, value);
    }

    /**
     * Atomically get or compute value. Lock-free for cache hits.
     */
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        return cache.get(key, mappingFunction);
    }

    public void putAll(Map<? extends K, ? extends V> m) {
        cache.putAll(m);
    }

    public boolean containsKey(K key) {
        return cache.getIfPresent(key) != null;
    }

    public V remove(K key) {
        V value = cache.getIfPresent(key);
        cache.invalidate(key);
        return value;
    }

    public void clear() {
        cache.invalidateAll();
    }

    public int size() {
        return (int) cache.estimatedSize();
    }
}
