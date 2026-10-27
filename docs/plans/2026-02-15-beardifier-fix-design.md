# Beardifier Fix Design

## Problem

OTG uses a 1.16-era noise system where beard (structure terrain adaptation) is applied AFTER the `d/2 - d³/24` density compression. This compression reduces terrain density from ~±1.0 to ~±0.48, while beard contributions remain at their full ±0.4 scale. Result: negative beard above RIGID structures carves into nearby terrain, creating floating islands — particularly visible around villages.

Vanilla 1.18+ applies beard contributions via DensityFunction pipeline WITHOUT this compression, so the relative impact of beard is smaller.

## Root Cause

```
OTG pipeline:
  raw noise → interpolate → /200 → clamp(-1,1) → d/2 - d³/24 → += beard*0.8 → >0?

The d/2-d³/24 transformation (Taylor of sinh⁻¹) compresses density:
  d=0.5 → 0.245 (halved)
  d=1.0 → 0.458 (halved)

After compression, beard contribution of -0.3 easily flips 0.245 to -0.055 (AIR).
In vanilla without compression: 0.5 - 0.3 = 0.2 → still STONE.
```

## Solution

### 1. Move beard before d/2-d³/24

New pipeline:
```
raw noise → interpolate → /200 → clamp(-1,1) → += beard → clamp(-1,1) → d/2-d³/24 → >0?
```

This puts beard and terrain on the same density scale before compression.

### 2. Port vanilla 1.21.1 TerrainAdjustment types

OTG currently treats all structure pieces identically. Vanilla distinguishes:

| Type | Formula | Used By |
|------|---------|---------|
| BEARD_THIN | `getBeardContribution(dx, dy, dz, dy) * 0.8` | Villages, pillager outposts |
| BEARD_BOX | `getBeardContribution(dx, distFromBoxY, dz, dy) * 0.8` | — |
| BURY | `clampedMap(length(dx, dy/2, dz), 0→6, 1→0)` | Ancient cities, trial chambers |
| ENCAPSULATE | `getBuryContribution(dx/2, dy/2, dz/2) * 0.8` | — |

### 3. Separate kernel from directional factor

OTG's `NOISE_WEIGHT_TABLE` stores the full product (Gaussian × directional factor). Vanilla's `BEARD_KERNEL` stores only the Gaussian: `exp(-lengthSq/16)`.

The directional factor is computed per-invocation with the appropriate Y:
```java
d = rawY + 0.5
f = -d * fastInvSqrt(lengthSq(dx, d, dz) / 2) / 2
result = f * BEARD_KERNEL[kernelY][kernelX][kernelZ]
```

This separation is required for BEARD_BOX where kernel Y differs from raw Y.

### 4. Add jigsaw junction support

Vanilla applies beard contribution to jigsaw junction points (connection points between structure pieces) at 0.4x scale instead of 0.8x. This smooths transitions between pieces.

## Files Changed

### JigsawStructureData.java (common-util)
- Add `TerrainAdjustment terrainAdjustment` field (new enum or int constant)
- Add `int maxY` field (needed for BEARD_BOX)
- Keep existing fields

### New: OTGTerrainAdjustment enum (common-util)
- Values: BEARD_THIN, BEARD_BOX, BURY, ENCAPSULATE
- Only need these 4 — NONE pieces are already filtered out at collection time

### New: JigsawJunctionData record (common-util)
- Fields: sourceX, sourceGroundY, sourceZ
- Lightweight record for junction point data

### OTGChunkGenerator.java (common-core)
- Replace `NOISE_WEIGHT_TABLE` with `BEARD_KERNEL` (Gaussian only)
- New `getBeardContribution(dx, kernelY, dz, rawY)` matching vanilla
- New `getBuryContribution(dx, dy, dz)` for BURY/ENCAPSULATE
- Modify `populateNoise()`:
  - Move beard application before `d/2 - d³/24`
  - Add clamp after beard addition
  - Handle TerrainAdjustment types per vanilla's `compute()`
  - Add jigsaw junction loop with `* 0.4` scaling
- Remove old `calculateNoiseWeight()` and `getNoiseWeight()`

### OTGFabricChunkGenerator.java (fabric platform)
- Pass `TerrainAdjustment` from structure to `JigsawStructureData`
- Collect `JigsawJunction` data from RIGID pool element pieces
- Pass junction list to `populateNoise()`

### OTGNeoForgeChunkGenerator.java (neoforge platform, if active)
- Same changes as Fabric

## Risk Assessment

- **d/2-d³/24 reorder**: Affects terrain near ALL structures. Positive change — terrain becomes more resistant to beard carving. No impact on terrain without structures.
- **TerrainAdjustment types**: New behavior for BURY/ENCAPSULATE structures. Currently these get BEARD_THIN treatment (incorrect), so this is strictly better.
- **Jigsaw junctions**: New smooth transitions between structure pieces. Additive, low risk.
- **Clamp after beard**: Prevents density from exceeding ±1 before transformation. Edge case safety net.
