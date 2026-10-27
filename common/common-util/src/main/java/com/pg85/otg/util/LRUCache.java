package com.pg85.otg.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A simple LRU (Least Recently Used) cache based on LinkedHashMap.
 * When the cache exceeds maxSize, the least recently accessed entry is removed.
 *
 * Unlike FifoMap which removes the oldest entry, LRUCache removes the entry
 * that hasn't been accessed for the longest time, providing better cache hit rates
 * for access patterns with temporal locality.
 *
 * @param <K> Key type
 * @param <V> Value type
 */
public class LRUCache<K, V> extends LinkedHashMap<K, V>
{
	private static final long serialVersionUID = 1L;
	private final int maxSize;

	public LRUCache(int maxSize)
	{
		// accessOrder=true makes LinkedHashMap reorder on access (LRU behavior)
		super(maxSize + 1, 0.75f, true);
		this.maxSize = maxSize;
	}

	@Override
	protected boolean removeEldestEntry(Map.Entry<K, V> eldest)
	{
		return size() > maxSize;
	}
}
