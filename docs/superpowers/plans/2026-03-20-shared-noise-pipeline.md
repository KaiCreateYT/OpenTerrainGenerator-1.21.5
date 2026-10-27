# Shared Terrain Noise Pipeline — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Extract duplicated noise computation from OTGChunkGenerator and BiomeHeightmapGenerator into a shared `TerrainNoiseComputer` class so both use identical math.

**Architecture:** New static utility class `TerrainNoiseComputer` in `common-util` holds 4 pure noise methods + sampler factory. OTGChunkGenerator delegates to it. BiomeHeightmapGenerator replaces its local copies with calls to it + fixes sampler creation order.

**Tech Stack:** Java 21, OTG noise package (`com.pg85.otg.gen.noise`)

**Spec:** `docs/superpowers/specs/2026-03-20-shared-noise-pipeline-design.md`

**CRITICAL:** All builds must run from the worktree:
```bash
cd /var/home/jmc/IdeaProjects/OpenTerrainGenerator/.worktrees/1.21.1-shared-noise-pipeline
./gradlew build
```

**No automated tests** — OTG has no test framework. Build verification only.

---

## File Structure

```
common/common-util/src/main/java/com/pg85/otg/gen/noise/
└── TerrainNoiseComputer.java     # NEW: shared noise methods + sampler factory

common/common-core/src/main/java/com/pg85/otg/gen/
└── OTGChunkGenerator.java        # MODIFY: delegate 4 methods + use factory in setSeed()

platforms/shared/src/client/java/com/pg85/otg/client/editor/data/
└── BiomeHeightmapGenerator.java  # MODIFY: remove 4 local methods, use factory + shared methods
```

---

## Task 1: Create TerrainNoiseComputer

**Files:**
- Create: `common/common-util/src/main/java/com/pg85/otg/gen/noise/TerrainNoiseComputer.java`

- [ ] **Step 1: Create the class with constants, record, and factory**

```java
package com.pg85.otg.gen.noise;

import com.pg85.otg.util.helpers.MathHelper;

import java.util.Random;
import java.util.stream.IntStream;

/**
 * Shared terrain noise computation used by both OTGChunkGenerator (world gen)
 * and BiomeHeightmapGenerator (editor preview). All methods are pure static
 * functions — no instance state, inherently thread-safe.
 */
public class TerrainNoiseComputer {

    public static final double WORLD_GEN_CONSTANT = 684.412;
    public static final float REFERENCE_Y_SECTIONS = 33.5f;
    public static final int INTERPOLATION_OCTAVES = 8;
    public static final int TERRAIN_OCTAVES = 16;

    public record NoiseSamplers(
        OctavePerlinNoiseSampler interpolation,
        OctavePerlinNoiseSampler lower,
        OctavePerlinNoiseSampler upper,
        OctavePerlinNoiseSampler depth
    ) {}

    /**
     * Creates all 4 noise samplers in OTG's canonical order.
     * IMPORTANT: In OTGChunkGenerator.setSeed(), biomeBlocksNoiseGen follows
     * these 4 samplers and consumes the same Random. This factory must be called
     * at the same position — before biomeBlocksNoiseGen — to preserve state.
     */
    public static NoiseSamplers createNoiseSamplers(Random rng) {
        var interpolation = new OctavePerlinNoiseSampler(rng, IntStream.rangeClosed(-7, 0));
        var lower = new OctavePerlinNoiseSampler(rng, IntStream.rangeClosed(-15, 0));
        var upper = new OctavePerlinNoiseSampler(rng, IntStream.rangeClosed(-15, 0));
        var depth = new OctavePerlinNoiseSampler(rng, IntStream.rangeClosed(-15, 0));
        return new NoiseSamplers(interpolation, lower, upper, depth);
    }
}
```

- [ ] **Step 2: Add sampleNoise()**

```java
/**
 * Samples terrain noise with volatility weight blending.
 * Identical to OTGChunkGenerator.sampleNoise() — uses MathHelper.lerp(delta, ...) with raw delta.
 */
public static double sampleNoise(
        int x, int y, int z,
        double horizontalScale, double verticalScale,
        double horizontalStretch, double verticalStretch,
        double volatility1, double volatility2,
        double volatilityWeight1, double volatilityWeight2,
        OctavePerlinNoiseSampler interpolationNoise,
        OctavePerlinNoiseSampler lowerInterpolatedNoise,
        OctavePerlinNoiseSampler upperInterpolatedNoise
) {
    double delta = getInterpolationNoise(interpolationNoise, x, y, z, horizontalStretch, verticalStretch);

    if (delta < volatilityWeight1) {
        return getInterpolatedNoise(lowerInterpolatedNoise, x, y, z, horizontalScale, verticalScale) / 512.0D
               * volatility1;
    } else if (delta > volatilityWeight2) {
        return getInterpolatedNoise(upperInterpolatedNoise, x, y, z, horizontalScale, verticalScale) / 512.0D
               * volatility2;
    } else {
        return MathHelper.lerp(
                delta,
                getInterpolatedNoise(lowerInterpolatedNoise, x, y, z, horizontalScale, verticalScale) / 512.0D
                * volatility1,
                getInterpolatedNoise(upperInterpolatedNoise, x, y, z, horizontalScale, verticalScale) / 512.0D
                * volatility2
        );
    }
}
```

- [ ] **Step 3: Add getInterpolationNoise()**

```java
/**
 * 8-octave interpolation noise, normalized to [0, 1].
 * Determines which volatility layer to use in sampleNoise().
 */
public static double getInterpolationNoise(
        OctavePerlinNoiseSampler sampler,
        int x, int y, int z,
        double horizontalStretch, double verticalStretch
) {
    double interpolation = 0.0D;
    double amplitude = 1.0D;
    PerlinNoiseSampler octave;
    for (int i = 0; i < INTERPOLATION_OCTAVES; i++) {
        octave = sampler.getOctave(i);
        if (octave != null) {
            interpolation += octave.sample(
                    OctavePerlinNoiseSampler.maintainPrecision((double) x * horizontalStretch * amplitude),
                    OctavePerlinNoiseSampler.maintainPrecision((double) y * verticalStretch * amplitude),
                    OctavePerlinNoiseSampler.maintainPrecision((double) z * horizontalStretch * amplitude),
                    verticalStretch * amplitude,
                    (double) y * verticalStretch * amplitude
            ) / amplitude;
        }
        amplitude /= 2.0D;
    }
    return (interpolation / 10.0D + 1.0D) / 2.0D;
}
```

- [ ] **Step 4: Add getInterpolatedNoise()**

```java
/**
 * 16-octave terrain shape noise.
 */
public static double getInterpolatedNoise(
        OctavePerlinNoiseSampler sampler,
        int x, int y, int z,
        double horizontalScale, double verticalScale
) {
    double noise = 0.0D;
    double amplitude = 1.0D;
    PerlinNoiseSampler octave;
    for (int i = 0; i < TERRAIN_OCTAVES; ++i) {
        double scaledX = OctavePerlinNoiseSampler.maintainPrecision((double) x * horizontalScale * amplitude);
        double scaledY = OctavePerlinNoiseSampler.maintainPrecision((double) y * verticalScale * amplitude);
        double scaledZ = OctavePerlinNoiseSampler.maintainPrecision((double) z * horizontalScale * amplitude);
        double scaledVerticalScale = verticalScale * amplitude;

        octave = sampler.getOctave(i);
        if (octave != null) {
            noise += octave.sample(
                    scaledX, scaledY, scaledZ,
                    scaledVerticalScale,
                    (double) y * scaledVerticalScale
            ) / amplitude;
        }
        amplitude /= 2.0D;
    }
    return noise;
}
```

- [ ] **Step 5: Add getExtraHeightAt()**

```java
/**
 * Depth noise variation — provides terrain height variation independent of Fracture settings.
 * Samples at x*200, 10, z*200.
 */
public static double getExtraHeightAt(
        OctavePerlinNoiseSampler depthNoise,
        int x, int z,
        double maxAverageDepth, double maxAverageHeight
) {
    double noiseHeight = depthNoise.sample(x * 200, 10.0D, z * 200, 1.0D, 0.0D, true) * 65535.0 / 8000.0;

    if (noiseHeight < 0.0D) {
        noiseHeight = -noiseHeight * 0.3D;
    }
    noiseHeight = noiseHeight * 3.0D - 2.0D;

    if (noiseHeight < 0.0D) {
        noiseHeight /= 2.0D;
        if (noiseHeight < -1.0D) {
            noiseHeight = -1.0D;
        }
        if (maxAverageDepth > 0.1) {
            noiseHeight /= maxAverageDepth;
        }
        noiseHeight /= 1.4D;
        noiseHeight /= 2.0D;
    } else {
        if (noiseHeight > 1.0D) {
            noiseHeight = 1.0D;
        }
        noiseHeight *= maxAverageHeight;
        noiseHeight /= 8.0D;
    }

    return noiseHeight;
}
```

- [ ] **Step 6: Build and verify**

```bash
cd /var/home/jmc/IdeaProjects/OpenTerrainGenerator/.worktrees/1.21.1-shared-noise-pipeline
./gradlew build
```

- [ ] **Step 7: Commit**

```bash
git add common/common-util/src/main/java/com/pg85/otg/gen/noise/TerrainNoiseComputer.java
git commit -m "feat: add TerrainNoiseComputer — shared noise pipeline for OTG and editor preview"
```

---

## Task 2: Refactor OTGChunkGenerator to Use TerrainNoiseComputer

**Files:**
- Modify: `common/common-core/src/main/java/com/pg85/otg/gen/OTGChunkGenerator.java`

- [ ] **Step 1: Update setSeed() to use factory**

Replace lines 159-162:
```java
// Before:
this.interpolationNoise = new OctavePerlinNoiseSampler(random, IntStream.rangeClosed(-7, 0));
this.lowerInterpolatedNoise = new OctavePerlinNoiseSampler(random, IntStream.rangeClosed(-15, 0));
this.upperInterpolatedNoise = new OctavePerlinNoiseSampler(random, IntStream.rangeClosed(-15, 0));
this.depthNoise = new OctavePerlinNoiseSampler(random, IntStream.rangeClosed(-15, 0));

// After:
var samplers = TerrainNoiseComputer.createNoiseSamplers(random);
this.interpolationNoise = samplers.interpolation();
this.lowerInterpolatedNoise = samplers.lower();
this.upperInterpolatedNoise = samplers.upper();
this.depthNoise = samplers.depth();
```

Add import: `import com.pg85.otg.gen.noise.TerrainNoiseComputer;`

Note: `this.biomeBlocksNoiseGen = new NoiseGeneratorPerlinMesaBlocks(random, 4);` on line 163 MUST stay AFTER the factory call.

- [ ] **Step 2: Delegate sampleNoise()**

Replace lines 212-246 (the full method body) with delegation:
```java
private double sampleNoise(
        int x, int y, int z,
        double horizontalScale, double verticalScale,
        double horizontalStretch, double verticalStretch,
        double volatility1, double volatility2,
        double volatilityWeight1, double volatilityWeight2
) {
    return TerrainNoiseComputer.sampleNoise(
            x, y, z,
            horizontalScale, verticalScale,
            horizontalStretch, verticalStretch,
            volatility1, volatility2,
            volatilityWeight1, volatilityWeight2,
            this.interpolationNoise, this.lowerInterpolatedNoise, this.upperInterpolatedNoise
    );
}
```

- [ ] **Step 3: Delegate getInterpolationNoise()**

Replace lines 248-268 with:
```java
private double getInterpolationNoise(int x, int y, int z, double horizontalStretch, double verticalStretch) {
    return TerrainNoiseComputer.getInterpolationNoise(this.interpolationNoise, x, y, z, horizontalStretch, verticalStretch);
}
```

Note: This method is still called from `sampleNoise()` in OTGChunkGenerator — but since `sampleNoise()` now delegates to `TerrainNoiseComputer.sampleNoise()` which calls `TerrainNoiseComputer.getInterpolationNoise()` directly, this local method is only needed if called elsewhere. Check `generateNoiseColumn()` — it calls `sampleNoise()` only, not `getInterpolationNoise()` directly. So this method becomes dead code but keep it for now (safe to remove later).

- [ ] **Step 4: Delegate getInterpolatedNoise()**

Replace lines 270-306 with:
```java
private double getInterpolatedNoise(OctavePerlinNoiseSampler sampler, int x, int y, int z, double horizontalScale, double verticalScale) {
    return TerrainNoiseComputer.getInterpolatedNoise(sampler, x, y, z, horizontalScale, verticalScale);
}
```

Same note — may become dead code since `sampleNoise()` delegates entirely now.

- [ ] **Step 5: Delegate getExtraHeightAt()**

Replace lines 308-337 with:
```java
private double getExtraHeightAt(int x, int z, double maxAverageDepth, double maxAverageHeight) {
    return TerrainNoiseComputer.getExtraHeightAt(this.depthNoise, x, z, maxAverageDepth, maxAverageHeight);
}
```

- [ ] **Step 6: Remove unused imports**

Remove `import java.util.stream.IntStream;` if no longer used (check — it was used for sampler creation, now handled by factory).

- [ ] **Step 7: Build and verify**

```bash
cd /var/home/jmc/IdeaProjects/OpenTerrainGenerator/.worktrees/1.21.1-shared-noise-pipeline
./gradlew build
```

- [ ] **Step 8: Commit**

```bash
git add common/common-core/src/main/java/com/pg85/otg/gen/OTGChunkGenerator.java
git commit -m "refactor: OTGChunkGenerator delegates noise methods to TerrainNoiseComputer"
```

---

## Task 3: Refactor BiomeHeightmapGenerator to Use TerrainNoiseComputer

**Files:**
- Modify: `platforms/shared/src/client/java/com/pg85/otg/client/editor/data/BiomeHeightmapGenerator.java`

- [ ] **Step 1: Replace sampler creation with factory**

Replace the 4 manual sampler lines:
```java
// Before:
Random rng = new Random(seed);
OctavePerlinNoiseSampler lowerNoise = new OctavePerlinNoiseSampler(rng, IntStream.rangeClosed(-15, 0));
OctavePerlinNoiseSampler upperNoise = new OctavePerlinNoiseSampler(rng, IntStream.rangeClosed(-15, 0));
OctavePerlinNoiseSampler interpNoise = new OctavePerlinNoiseSampler(rng, IntStream.rangeClosed(-7, 0));
OctavePerlinNoiseSampler depthNoiseSampler = new OctavePerlinNoiseSampler(rng, IntStream.rangeClosed(-15, 0));

// After:
var samplers = TerrainNoiseComputer.createNoiseSamplers(new Random(seed));
```

This fixes the creation order to match OTG (`interp → lower → upper → depth`).

- [ ] **Step 2: Update noise column generation to use shared methods**

Replace `sampleNoise(...)` call with `TerrainNoiseComputer.sampleNoise(...)`:
```java
double noise = TerrainNoiseComputer.sampleNoise(
    nx, y, nz,
    horizontalScale, verticalScale,
    horizontalStretch, verticalStretch,
    volatility1, volatility2,
    volatilityWeight1, volatilityWeight2,
    samplers.interpolation(), samplers.lower(), samplers.upper()
);
```

Replace `getExtraHeightAt(...)` call with `TerrainNoiseComputer.getExtraHeightAt(...)`:
```java
float extraHeight = (float)(TerrainNoiseComputer.getExtraHeightAt(
    samplers.depth(), nx, nz, maxAverageDepth, maxAverageHeight) * 0.2);
```

- [ ] **Step 3: Delete local noise methods**

Remove these methods entirely from BiomeHeightmapGenerator:
- `sampleNoise()` (~20 lines)
- `getInterpolationNoise()` (~18 lines)
- `getInterpolatedNoise()` (~18 lines)
- `getExtraHeightAt()` (~18 lines)

- [ ] **Step 4: Update imports**

Add: `import com.pg85.otg.gen.noise.TerrainNoiseComputer;`
Remove: `import com.pg85.otg.gen.noise.OctavePerlinNoiseSampler;` and `import com.pg85.otg.gen.noise.PerlinNoiseSampler;` (no longer directly used).
Remove: `import java.util.stream.IntStream;` (factory handles it).

- [ ] **Step 5: Use constants from TerrainNoiseComputer**

Replace local constants:
```java
// Before:
private static final double WORLD_GEN_CONSTANT = 684.412;
private static final float REFERENCE_Y_SECTIONS = 33.5f;

// After: use TerrainNoiseComputer.WORLD_GEN_CONSTANT and TerrainNoiseComputer.REFERENCE_Y_SECTIONS
```

- [ ] **Step 6: Build and verify**

```bash
cd /var/home/jmc/IdeaProjects/OpenTerrainGenerator/.worktrees/1.21.1-shared-noise-pipeline
./gradlew build
```

- [ ] **Step 7: Commit**

```bash
git add platforms/shared/src/client/java/com/pg85/otg/client/editor/data/BiomeHeightmapGenerator.java
git commit -m "refactor: BiomeHeightmapGenerator uses TerrainNoiseComputer — fixes sampler order + lerp divergence"
```

---

## Task 4: Deploy, Test, and Cleanup

- [ ] **Step 1: Build and deploy**

```bash
cd /var/home/jmc/IdeaProjects/OpenTerrainGenerator/.worktrees/1.21.1-shared-noise-pipeline
./gradlew build
rm /var/home/jmc/Games/minecraft/active_mc/mods/otg-*.jar
cp build/distributions/otg-neoforge-*.jar /var/home/jmc/Games/minecraft/active_mc/mods/
```

- [ ] **Step 2: Test world generation**

1. Create a new world with DefaultPreset → verify terrain generates normally
2. Create a world with Biome Bundle → verify terrain looks identical to before

- [ ] **Step 3: Test terrain preview**

1. Biome Editor → select a mountain biome → Terrain 3D
2. Compare shape to actual world gen (terrain should now be closer than before)
3. Try different seeds and sizes

- [ ] **Step 4: Remove dead code in OTGChunkGenerator (optional)**

After confirming everything works, the local `getInterpolationNoise()` and `getInterpolatedNoise()` in OTGChunkGenerator are dead code (only called from `sampleNoise()` which now delegates entirely). They can be safely removed. But this is optional — leaving them as thin delegates is also fine.

- [ ] **Step 5: Update changelog**

```markdown
**2026-03-XX**

- refactor: Extract shared terrain noise pipeline — `TerrainNoiseComputer` in common-util used by both OTGChunkGenerator and editor preview. Fixes sampler creation order mismatch and volatility weight blending divergence between preview and world gen.
```

- [ ] **Step 6: Final commit**

```bash
git add -A
git commit -m "refactor: shared noise pipeline complete — TerrainNoiseComputer used by OTG and editor"
```
