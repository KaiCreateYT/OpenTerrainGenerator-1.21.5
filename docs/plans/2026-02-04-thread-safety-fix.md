# Thread-Safety Fix for Parallel Chunk Generation

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Fix critical thread-safety issues to enable safe parallel chunk generation in ShadowChunkGenerator.

**Architecture:** The existing ShadowChunkGenerator already uses worker threads but without proper synchronization. This plan fixes the data races.

**Tech Stack:** Java 17, ConcurrentHashMap, volatile keyword

**Risk:** HIGH - incorrect synchronization can cause crashes, data corruption, or deadlocks. Test thoroughly.

---

## Pre-Implementation

**Verify current behavior:**
```bash
./gradlew :common:common-test:run --args="verify --seed 12345 --baseline /var/home/jmc/baseline-before-optim.json --preset DefaultPreset --otg-root /var/home/jmc/IdeaProjects/OpenTerrainGenerator-jmc/resources"
```

---

## Task 1: Replace FifoMap with Thread-Safe LRUCache in CachedBiomeProvider

**Problem:** FifoMap extends LinkedHashMap which is not thread-safe. Multiple threads accessing caches cause ConcurrentModificationException or data corruption.

**File:** `common/common-generator/src/main/java/com/pg85/otg/gen/biome/CachedBiomeProvider.java`

### Step 1: Check current imports and fields

Look for:
```java
import com.pg85.otg.util.FifoMap;
// ...
private FifoMap<ChunkCoordinate, ...> biomeConfigsCache;
private FifoMap<ChunkCoordinate, ...> biomesCache;
private FifoMap<ChunkCoordinate, ...> noiseBiomeConfigsCache;
```

### Step 2: Create ThreadSafeLRUCache utility class

**File:** `common/common-util/src/main/java/com/pg85/otg/util/ThreadSafeLRUCache.java`

```java
package com.pg85.otg.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Thread-safe LRU cache using Collections.synchronizedMap wrapper.
 * All operations are synchronized, safe for concurrent access.
 */
public class ThreadSafeLRUCache<K, V> {
    private final Map<K, V> cache;

    public ThreadSafeLRUCache(int maxSize) {
        // accessOrder=true for LRU behavior
        LinkedHashMap<K, V> lruMap = new LinkedHashMap<K, V>(maxSize + 1, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > maxSize;
            }
        };
        this.cache = Collections.synchronizedMap(lruMap);
    }

    public V get(K key) {
        return cache.get(key);
    }

    public void put(K key, V value) {
        cache.put(key, value);
    }

    public V computeIfAbsent(K key, java.util.function.Function<? super K, ? extends V> mappingFunction) {
        // Need to synchronize the whole compute operation
        synchronized (cache) {
            V value = cache.get(key);
            if (value == null) {
                value = mappingFunction.apply(key);
                if (value != null) {
                    cache.put(key, value);
                }
            }
            return value;
        }
    }

    public void clear() {
        cache.clear();
    }

    public int size() {
        return cache.size();
    }
}
```

### Step 3: Update CachedBiomeProvider to use ThreadSafeLRUCache

Replace FifoMap declarations with ThreadSafeLRUCache:
```java
import com.pg85.otg.util.ThreadSafeLRUCache;

// Replace:
private FifoMap<ChunkCoordinate, BiomeSettings[]> biomeConfigsCache;
// With:
private final ThreadSafeLRUCache<ChunkCoordinate, BiomeSettings[]> biomeConfigsCache;
```

Do the same for `biomesCache` and `noiseBiomeConfigsCache`.

### Step 4: Remove broken `locked` boolean flags

Find and remove:
```java
private boolean locked = false;
// ...
this.locked = true;
// ...
this.locked = false;
```

These provide no synchronization benefit.

### Step 5: Remove explicit synchronized blocks that wrap cache access

The ThreadSafeLRUCache handles synchronization internally. Remove:
```java
synchronized(this.lock) {
    // cache access
}
```

But keep any synchronized blocks that protect OTHER shared state.

### Step 6: Remove unused lock objects if no longer needed

```java
// Remove if unused:
private Object lock = new Object();
private Object noiseLock = new Object();
```

### Step 7: Compile and verify

```bash
./gradlew :common:common-generator:compileJava
./gradlew :common:common-test:run --args="verify --seed 12345 --baseline /var/home/jmc/baseline-before-optim.json --preset DefaultPreset --otg-root /var/home/jmc/IdeaProjects/OpenTerrainGenerator-jmc/resources"
```

### Step 8: Commit

```bash
git add common/common-util/src/main/java/com/pg85/otg/util/ThreadSafeLRUCache.java
git add common/common-generator/src/main/java/com/pg85/otg/gen/biome/CachedBiomeProvider.java
git commit -m "fix(biome): make CachedBiomeProvider thread-safe

Replace FifoMap with ThreadSafeLRUCache for safe concurrent access.
Remove broken locked flag pattern that provided no real synchronization."
```

---

## Task 2: Add volatile to OTGChunkGenerator Noise Samplers

**Problem:** Noise sampler fields are written in `setSeed()` and read in `populateNoise()`. Without volatile, threads may see stale values.

**File:** `common/common-core/src/main/java/com/pg85/otg/gen/OTGChunkGenerator.java`

### Step 1: Find noise sampler field declarations (around lines 73-76, 90)

Current:
```java
private OctavePerlinNoiseSampler interpolationNoise;
private OctavePerlinNoiseSampler lowerInterpolatedNoise;
private OctavePerlinNoiseSampler upperInterpolatedNoise;
private OctavePerlinNoiseSampler depthNoise;
// ...
private NoiseGeneratorPerlinMesaBlocks biomeBlocksNoiseGen;
```

### Step 2: Add volatile keyword

Change to:
```java
private volatile OctavePerlinNoiseSampler interpolationNoise;
private volatile OctavePerlinNoiseSampler lowerInterpolatedNoise;
private volatile OctavePerlinNoiseSampler upperInterpolatedNoise;
private volatile OctavePerlinNoiseSampler depthNoise;
// ...
private volatile NoiseGeneratorPerlinMesaBlocks biomeBlocksNoiseGen;
```

### Step 3: Compile and verify

```bash
./gradlew :common:common-core:compileJava
./gradlew :common:common-test:run --args="verify --seed 12345 --baseline /var/home/jmc/baseline-before-optim.json --preset DefaultPreset --otg-root /var/home/jmc/IdeaProjects/OpenTerrainGenerator-jmc/resources"
```

### Step 4: Commit

```bash
git add common/common-core/src/main/java/com/pg85/otg/gen/OTGChunkGenerator.java
git commit -m "fix(generator): add volatile to noise sampler fields

Ensure visibility of noise sampler assignments across threads.
Required for safe parallel chunk generation."
```

---

## Task 3: Add Synchronized Wrapper for populateNoise in Critical Sections

**Problem:** Multiple worker threads in ShadowChunkGenerator call populateNoise() without synchronization.

**File:** `platforms/fabric/src/main/java/com/pg85/otg/fabric/gen/ShadowChunkGenerator.java`

### Step 1: Find where worker threads call populateNoise()

Look for worker thread code that calls the generator.

### Step 2: Option A - Synchronize on generator instance

If there's a single shared generator:
```java
synchronized (this.generator) {
    this.generator.populateNoise(...);
}
```

**Note:** This serializes chunk generation which defeats parallelism. Only use if other options fail.

### Step 3: Option B - Use thread-local random (preferred)

The main issue is that Random instances may be shared. Check if each worker has its own Random.

If workers share Random, change to:
```java
// Each worker should have its own Random seeded deterministically
Random workerRandom = new Random(baseSeed ^ (workerId * 341873128712L));
```

### Step 4: Review other shared state access

Check for any other shared mutable state accessed without synchronization.

### Step 5: Compile and test

```bash
./gradlew :platforms:fabric:compileJava
```

### Step 6: Commit

```bash
git add platforms/fabric/src/main/java/com/pg85/otg/fabric/gen/ShadowChunkGenerator.java
git commit -m "fix(fabric): ensure thread-safe chunk generation in workers

Add proper synchronization for parallel chunk generation."
```

---

## Task 4: Stress Test Parallel Generation

### Step 1: Create stress test in SnapshotCli

Add a new command `stress-test` that generates many chunks in parallel:

**File:** `common/common-test/src/main/java/com/pg85/otg/test/cli/SnapshotCli.java`

Add method:
```java
public int runStressTest(String[] args) {
    // Parse args for thread count, chunks, iterations
    int threads = 4;
    int chunksPerThread = 100;
    int iterations = 10;

    ExecutorService executor = Executors.newFixedThreadPool(threads);
    AtomicInteger errors = new AtomicInteger(0);
    AtomicInteger completed = new AtomicInteger(0);

    // Submit tasks
    for (int t = 0; t < threads; t++) {
        final int threadId = t;
        executor.submit(() -> {
            try {
                // Generate chunks for this thread
                for (int i = 0; i < chunksPerThread; i++) {
                    int chunkX = threadId * 100 + i;
                    int chunkZ = threadId * 100 + i;
                    // Generate chunk...
                }
                completed.incrementAndGet();
            } catch (Exception e) {
                errors.incrementAndGet();
                e.printStackTrace();
            }
        });
    }

    executor.shutdown();
    executor.awaitTermination(5, TimeUnit.MINUTES);

    System.out.println("Completed: " + completed.get() + "/" + threads);
    System.out.println("Errors: " + errors.get());

    return errors.get() > 0 ? EXIT_ERROR : EXIT_SUCCESS;
}
```

### Step 2: Run stress test

```bash
./gradlew :common:common-test:run --args="stress-test --threads 4 --chunks 100 --iterations 10 --preset DefaultPreset --otg-root /var/home/jmc/IdeaProjects/OpenTerrainGenerator-jmc/resources"
```

### Step 3: Verify no exceptions

- No ConcurrentModificationException
- No NullPointerException
- No data corruption (compare outputs)

### Step 4: Commit

```bash
git add common/common-test/src/main/java/com/pg85/otg/test/cli/SnapshotCli.java
git commit -m "test: add parallel generation stress test

Verify thread-safety of chunk generation with multiple concurrent threads."
```

---

## Post-Implementation: Performance Benchmark

Compare parallel vs sequential performance:

```bash
# Sequential (baseline)
./gradlew :common:common-test:run --args="benchmark --chunks 200 --iterations 5 ..."

# Parallel stress test timing
./gradlew :common:common-test:run --args="stress-test --threads 4 --chunks 200 ..."
```

Expected: Near-linear speedup with thread count (minus synchronization overhead).

---

## Summary

| Task | Description | Effort | Risk |
|------|-------------|--------|------|
| 1 | ThreadSafeLRUCache + fix CachedBiomeProvider | Medium | Medium |
| 2 | Add volatile to noise samplers | Easy | Low |
| 3 | Synchronize ShadowChunkGenerator workers | Medium | Medium |
| 4 | Stress test | Medium | Low |

**Total estimated time:** 2-4 hours

**After completion:** Parallel chunk generation should be safe and provide speedup on multi-core systems.
