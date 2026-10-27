# Performance Optimizations Round 2 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Eliminate unnecessary object allocations and repeated method calls in terrain generation hot paths.

**Architecture:** These optimizations target inner loops in OTGChunkGenerator and Carver where allocations and redundant calls have cumulative impact.

**Tech Stack:** Java 17

**Baseline Performance:** 466.3 chunks/sec, 2.145 ms/chunk (see `docs/benchmarks/2026-02-04-baseline-round2.txt`)

---

## Pre-Implementation: Verify Baseline

Ensure snapshot testing works before changes:
```bash
./gradlew :common:common-test:run --args="verify --seed 12345 --baseline /var/home/jmc/baseline-before-optim.json --preset DefaultPreset --otg-root /var/home/jmc/IdeaProjects/OpenTerrainGenerator-jmc/resources"
```

---

## Task 1: Cache getSurfaceSettings() in Inner Loop (EASY - HIGH IMPACT)

**Problem:** `biomeConfig.getSurfaceSettings()` called 3 times per block in innermost loop (~200k calls/chunk).

**File:** `common/common-core/src/main/java/com/pg85/otg/gen/OTGChunkGenerator.java:672-688`

**Current Code:**
```java
if (density > 0.0) {
    buffer.setBlock(
            localX,
            realY,
            localZ,
            biomeConfig.getSurfaceSettings().getStoneBlockReplaced(realY)
    );
    // ...
} else if (realY < waterLevel[localX * 16 + localZ]
           && realY > biomeConfig.getSurfaceSettings().getWaterLevelMin()) {
    buffer.setBlock(
            localX,
            realY,
            localZ,
            biomeConfig.getSurfaceSettings().getWaterBlockReplaced(realY)
    );
    // ...
}
```

**Step 1: Find the biomeConfig assignment (around line 657)**

Look for:
```java
biomeConfig = biomeConfigCache[localX * 16 + localZ];
```

**Step 2: Add SurfaceSettings cache after biomeConfig assignment**

Add after the biomeConfig line:
```java
biomeConfig = biomeConfigCache[localX * 16 + localZ];
SurfaceSettings surfaceSettings = biomeConfig.getSurfaceSettings();
```

**Step 3: Replace all getSurfaceSettings() calls with cached variable**

Change:
```java
biomeConfig.getSurfaceSettings().getStoneBlockReplaced(realY)
```
To:
```java
surfaceSettings.getStoneBlockReplaced(realY)
```

And:
```java
biomeConfig.getSurfaceSettings().getWaterLevelMin()
biomeConfig.getSurfaceSettings().getWaterBlockReplaced(realY)
```
To:
```java
surfaceSettings.getWaterLevelMin()
surfaceSettings.getWaterBlockReplaced(realY)
```

**Step 4: Add import if needed**
```java
import com.pg85.otg.config.biome.SurfaceSettings;
```

**Step 5: Compile and verify**
```bash
./gradlew :common:common-core:compileJava
./gradlew :common:common-test:run --args="verify --seed 12345 --baseline /var/home/jmc/baseline-before-optim.json --preset DefaultPreset --otg-root /var/home/jmc/IdeaProjects/OpenTerrainGenerator-jmc/resources"
```

**Step 6: Commit**
```bash
git add common/common-core/src/main/java/com/pg85/otg/gen/OTGChunkGenerator.java
git commit -m "perf(generator): cache SurfaceSettings in populateNoise inner loop

Cache biomeConfig.getSurfaceSettings() result to avoid 3 method calls
per block in innermost loop. Reduces ~200k calls to ~65k per chunk."
```

---

## Task 2: Hoist MutableBoolean Allocation in Carver (EASY - HIGH IMPACT)

**Problem:** `new MutableBoolean(false)` created inside double-nested loop (400-900 allocations per carve).

**File:** `common/common-generator/src/main/java/com/pg85/otg/gen/carver/Carver.java:115`

**Current Code (around line 105-115):**
```java
for (int currentX = minX; currentX < maxX; ++currentX) {
    // ...
    for (int currentZ = minZ; currentZ < maxZ; ++currentZ) {
        // ...
        if (!(scaledX * scaledX + scaledZ * scaledZ >= 1.0D)) {
            foundSurface = new MutableBoolean(false);  // <-- ALLOCATION IN LOOP
            for (int currentY = maxY; currentY > minY; --currentY) {
                // ...
            }
        }
    }
}
```

**Step 1: Find MutableBoolean field declaration**

Look for field near top of class or method. If `foundSurface` is a local variable, we need to hoist it.

**Step 2: Move allocation before outer loop and reset instead of reallocate**

Change to:
```java
MutableBoolean foundSurface = new MutableBoolean(false);  // Before loops
for (int currentX = minX; currentX < maxX; ++currentX) {
    // ...
    for (int currentZ = minZ; currentZ < maxZ; ++currentZ) {
        // ...
        if (!(scaledX * scaledX + scaledZ * scaledZ >= 1.0D)) {
            foundSurface.setFalse();  // Reset instead of allocate
            for (int currentY = maxY; currentY > minY; --currentY) {
                // ...
            }
        }
    }
}
```

**Step 3: Compile and verify**
```bash
./gradlew :common:common-generator:compileJava
./gradlew :common:common-test:run --args="verify --seed 12345 --baseline /var/home/jmc/baseline-before-optim.json --preset DefaultPreset --otg-root /var/home/jmc/IdeaProjects/OpenTerrainGenerator-jmc/resources"
```

**Step 4: Commit**
```bash
git add common/common-generator/src/main/java/com/pg85/otg/gen/carver/Carver.java
git commit -m "perf(carver): hoist MutableBoolean allocation outside loops

Move MutableBoolean creation before loops and reset with setFalse()
instead of allocating new object. Eliminates 400-900 allocations per carve."
```

---

## Task 3: Cache getTerrainSettings() Calls (EASY - MEDIUM IMPACT)

**Problem:** `center.getTerrainSettings()` called twice in succession.

**File:** `common/common-core/src/main/java/com/pg85/otg/gen/OTGChunkGenerator.java:334-335`

**Current Code:**
```java
int smoothRadius = center.getTerrainSettings().getSmoothRadius();
int chcSmoothRadius = center.getTerrainSettings().getCHCSmoothRadius();
```

**Step 1: Cache TerrainSettings**

Change to:
```java
TerrainSettings centerTerrainSettings = center.getTerrainSettings();
int smoothRadius = centerTerrainSettings.getSmoothRadius();
int chcSmoothRadius = centerTerrainSettings.getCHCSmoothRadius();
```

Note: There's already `TerrainSettings terrainSettings = this.preset.getPresetConfig().getTerrainSettings();` at line 345 - this is for preset, not center biome. Keep both.

**Step 2: Check for other getTerrainSettings() calls in same method**

Search for other `getTerrainSettings()` calls that could use the cached variable.

**Step 3: Compile and verify**
```bash
./gradlew :common:common-core:compileJava
./gradlew :common:common-test:run --args="verify --seed 12345 --baseline /var/home/jmc/baseline-before-optim.json --preset DefaultPreset --otg-root /var/home/jmc/IdeaProjects/OpenTerrainGenerator-jmc/resources"
```

**Step 4: Commit**
```bash
git add common/common-core/src/main/java/com/pg85/otg/gen/OTGChunkGenerator.java
git commit -m "perf(generator): cache getTerrainSettings() result

Avoid calling getTerrainSettings() twice in getNoiseColumn() when
both smoothRadius and chcSmoothRadius are needed."
```

---

## Task 4: Reuse Array in getBiomeBlocksNoiseValue (MEDIUM - MEDIUM-HIGH IMPACT)

**Problem:** `new double[1]` allocated on every cache miss.

**File:** `common/common-core/src/main/java/com/pg85/otg/gen/OTGChunkGenerator.java:830-849`

**Current Code:**
```java
public double getBiomeBlocksNoiseValue(int blockX, int blockZ) {
    double noise = this.lastNoise.get();
    if (this.lastX.get() != blockX || this.lastZ.get() != blockZ) {
        double d1 = 0.03125D;
        noise = this.biomeBlocksNoiseGen.getRegion(
                new double[1],  // <-- ALLOCATION ON CACHE MISS
                blockX,
                blockZ,
                1,
                1,
                d1 * 2.0D,
                d1 * 2.0D,
                1.0D
        )[0];
        // ...
    }
    return noise;
}
```

**Step 1: Add ThreadLocal for reusable array (near other ThreadLocals around line 95-99)**

Add field:
```java
private final ThreadLocal<double[]> singleValueBuffer = ThreadLocal.withInitial(() -> new double[1]);
```

**Step 2: Use cached array instead of allocation**

Change:
```java
noise = this.biomeBlocksNoiseGen.getRegion(
        new double[1],
```
To:
```java
double[] buffer = this.singleValueBuffer.get();
noise = this.biomeBlocksNoiseGen.getRegion(
        buffer,
```

**Step 3: Compile and verify**
```bash
./gradlew :common:common-core:compileJava
./gradlew :common:common-test:run --args="verify --seed 12345 --baseline /var/home/jmc/baseline-before-optim.json --preset DefaultPreset --otg-root /var/home/jmc/IdeaProjects/OpenTerrainGenerator-jmc/resources"
```

**Step 4: Commit**
```bash
git add common/common-core/src/main/java/com/pg85/otg/gen/OTGChunkGenerator.java
git commit -m "perf(generator): reuse array buffer in getBiomeBlocksNoiseValue

Add ThreadLocal<double[]> to avoid allocating new double[1] on every
cache miss during surface/ground control."
```

---

## Task 5: Eliminate Boxing in ThreadLocal (MEDIUM - HIGH IMPACT)

**Problem:** `ThreadLocal<Integer>` and `ThreadLocal<Double>` cause boxing/unboxing on every get/set.

**File:** `common/common-core/src/main/java/com/pg85/otg/gen/OTGChunkGenerator.java:97-99`

**Current Code:**
```java
private final ThreadLocal<Integer> lastX = ThreadLocal.withInitial(() -> Integer.MAX_VALUE);
private final ThreadLocal<Integer> lastZ = ThreadLocal.withInitial(() -> Integer.MAX_VALUE);
private final ThreadLocal<Double> lastNoise = ThreadLocal.withInitial(() -> 0d);
```

**Step 1: Create a primitive holder class (inner class in OTGChunkGenerator)**

Add before the constructor:
```java
/**
 * Holder for primitive values to avoid ThreadLocal boxing overhead.
 */
private static class BiomeBlocksNoiseCache {
    int lastX = Integer.MAX_VALUE;
    int lastZ = Integer.MAX_VALUE;
    double lastNoise = 0.0;
    final double[] buffer = new double[1];  // Also include buffer from Task 4
}
```

**Step 2: Replace three ThreadLocals with one**

Change from:
```java
private final ThreadLocal<Integer> lastX = ThreadLocal.withInitial(() -> Integer.MAX_VALUE);
private final ThreadLocal<Integer> lastZ = ThreadLocal.withInitial(() -> Integer.MAX_VALUE);
private final ThreadLocal<Double> lastNoise = ThreadLocal.withInitial(() -> 0d);
```

To:
```java
private final ThreadLocal<BiomeBlocksNoiseCache> biomeBlocksNoiseCache =
    ThreadLocal.withInitial(BiomeBlocksNoiseCache::new);
```

**Step 3: Update getBiomeBlocksNoiseValue to use holder**

Change from:
```java
public double getBiomeBlocksNoiseValue(int blockX, int blockZ) {
    double noise = this.lastNoise.get();
    if (this.lastX.get() != blockX || this.lastZ.get() != blockZ) {
        double d1 = 0.03125D;
        noise = this.biomeBlocksNoiseGen.getRegion(
                new double[1],
                blockX,
                // ...
        )[0];
        this.lastX.set(blockX);
        this.lastZ.set(blockZ);
        this.lastNoise.set(noise);
    }
    return noise;
}
```

To:
```java
public double getBiomeBlocksNoiseValue(int blockX, int blockZ) {
    BiomeBlocksNoiseCache cache = this.biomeBlocksNoiseCache.get();
    if (cache.lastX != blockX || cache.lastZ != blockZ) {
        double d1 = 0.03125D;
        cache.lastNoise = this.biomeBlocksNoiseGen.getRegion(
                cache.buffer,
                blockX,
                blockZ,
                1,
                1,
                d1 * 2.0D,
                d1 * 2.0D,
                1.0D
        )[0];
        cache.lastX = blockX;
        cache.lastZ = blockZ;
    }
    return cache.lastNoise;
}
```

**Step 4: Remove old ThreadLocals and singleValueBuffer if Task 4 was done separately**

**Step 5: Compile and verify**
```bash
./gradlew :common:common-core:compileJava
./gradlew :common:common-test:run --args="verify --seed 12345 --baseline /var/home/jmc/baseline-before-optim.json --preset DefaultPreset --otg-root /var/home/jmc/IdeaProjects/OpenTerrainGenerator-jmc/resources"
```

**Step 6: Commit**
```bash
git add common/common-core/src/main/java/com/pg85/otg/gen/OTGChunkGenerator.java
git commit -m "perf(generator): eliminate ThreadLocal boxing overhead

Replace ThreadLocal<Integer> and ThreadLocal<Double> with single
ThreadLocal holding primitive fields. Eliminates boxing/unboxing
on every get/set call in getBiomeBlocksNoiseValue()."
```

---

## Post-Implementation: Benchmark and Compare

**Run benchmark:**
```bash
./gradlew :common:common-test:run --args="benchmark --preset DefaultPreset --otg-root /var/home/jmc/IdeaProjects/OpenTerrainGenerator-jmc/resources --chunks 200 --warmup 5 --iterations 10"
```

**Save results:**
```bash
./gradlew :common:common-test:run --args="benchmark ..." | tee docs/benchmarks/2026-02-04-after-round2.txt
```

**Compare with baseline:**
- Baseline: 466.3 chunks/sec, 2.145 ms/chunk
- Expected improvement: 5-15% additional throughput

---

## Summary

| Task | Optimization | Complexity | Expected Impact |
|------|-------------|------------|-----------------|
| 1 | Cache getSurfaceSettings() | Easy | High |
| 2 | Hoist MutableBoolean | Easy | High |
| 3 | Cache getTerrainSettings() | Easy | Medium |
| 4 | Reuse array buffer | Medium | Medium-High |
| 5 | Eliminate ThreadLocal boxing | Medium | High |

**Note:** Tasks 4 and 5 can be combined - Task 5's holder class includes the buffer from Task 4.
