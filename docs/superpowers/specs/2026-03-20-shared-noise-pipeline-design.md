# Shared Terrain Noise Pipeline — Design Spec

## Problem

OTGChunkGenerator (main world gen) and BiomeHeightmapGenerator (in-game editor terrain preview) duplicate the same noise computation formulas: `sampleNoise`, `getInterpolationNoise`, `getInterpolatedNoise`, `getExtraHeightAt`. This causes divergence — mountainous terrain looks different in preview vs actual world gen due to:

1. **Sampler creation order mismatch** — OTG creates `interp → lower → upper → depth`, preview creates `lower → upper → interp → depth`. Different Random consumption order → different Perlin offsets → different terrain shape.
2. **Formula drift risk** — any change in OTGChunkGenerator's noise math must be manually replicated in BiomeHeightmapGenerator.
3. **Volatility weight blending divergence** — OTG uses `MathHelper.lerp(delta, lower, upper)` with raw delta as blend factor. Preview normalizes delta into `[volW1, volW2]` range before blending (`t = (delta - volW1) / (volW2 - volW1)`). These produce different results and OTG's form is canonical.

## Solution

Extract noise computation into a shared static utility class `TerrainNoiseComputer` in `common-util`, used by both OTGChunkGenerator and BiomeHeightmapGenerator.

## Design Decisions

- **Preview matches OTG** — OTG's sampler creation order is canonical. Preview adapts, not the other way around.
- **No biome blending in preview** — accepted trade-off. Single-biome preview will always show more extreme terrain than OTG (no neighbor smoothing). This is by design.
- **No CHC in preview** — CustomHeightControl is biome-specific per-Y-layer data not available in single-biome preview context.

## Architecture

### New File: `TerrainNoiseComputer.java`

**Location**: `common/common-util/src/main/java/com/pg85/otg/gen/noise/TerrainNoiseComputer.java`

Placed alongside existing `OctavePerlinNoiseSampler.java` and `PerlinNoiseSampler.java` in the noise package.

**Constants**:
- `WORLD_GEN_CONSTANT = 684.412`
- `REFERENCE_Y_SECTIONS = 33.5f`
- `INTERPOLATION_OCTAVES = 8`
- `TERRAIN_OCTAVES = 16` (replaces coincidental use of `Constants.CHUNK_SIZE` in OTG's loop bound)

**Record**:
```java
public record NoiseSamplers(
    OctavePerlinNoiseSampler interpolation,  // 8 octaves (-7..0)
    OctavePerlinNoiseSampler lower,          // 16 octaves (-15..0)
    OctavePerlinNoiseSampler upper,          // 16 octaves (-15..0)
    OctavePerlinNoiseSampler depth           // 16 octaves (-15..0)
) {}
```

**Factory method**:
```java
public static NoiseSamplers createNoiseSamplers(Random rng)
```
Creates all 4 samplers in OTG's canonical order: `interp → lower → upper → depth`. Single source of truth for creation order.

**Important**: In `OTGChunkGenerator.setSeed()`, a 5th sampler (`biomeBlocksNoiseGen`) follows the 4 noise samplers and consumes the same `Random`. The factory must be called at the same position in `setSeed()` — before `biomeBlocksNoiseGen` — to preserve Random state.

**4 pure static methods** (all parameters, no instance state):

1. `sampleNoise(int x, y, z, double hScale, vScale, hStretch, vStretch, vol1, vol2, volW1, volW2, OctavePerlinNoiseSampler interp, lower, upper)` → `double`
   - Computes interpolation delta, branches on volatility weights, blends lower/upper noise.
   - Uses `MathHelper.lerp(delta, lower, upper)` — raw delta as blend factor, exactly like OTG. No `volW1 == volW2` guard needed (when equal, the else branch is unreachable because `delta < volW1` or `delta > volW2` always holds).

2. `getInterpolationNoise(OctavePerlinNoiseSampler sampler, int x, y, z, double hStretch, vStretch)` → `double`
   - 8-octave FBM, normalized to [0, 1] via `(interp / 10.0 + 1.0) / 2.0`.

3. `getInterpolatedNoise(OctavePerlinNoiseSampler sampler, int x, y, z, double hScale, vScale)` → `double`
   - 16-octave FBM for terrain shape.

4. `getExtraHeightAt(OctavePerlinNoiseSampler depthNoise, int x, z, double maxAvgDepth, maxAvgHeight)` → `double`
   - Depth noise variation, samples at `x*200, 10, z*200`.

### Modified: `OTGChunkGenerator.java`

**Minimal changes**:
- `setSeed()`: uses `TerrainNoiseComputer.createNoiseSamplers(random)` then unpacks into instance fields (preserving existing field names for compatibility).
- `sampleNoise()`, `getInterpolationNoise()`, `getInterpolatedNoise()`, `getExtraHeightAt()`: become one-line delegations to `TerrainNoiseComputer.*`, passing instance samplers as parameters.
- `generateNoiseColumn()`: untouched (biome blending, CHC, falloff loop stay as-is).

**Zero behavioral change** in world generation.

### Modified: `BiomeHeightmapGenerator.java`

- Sampler creation: `TerrainNoiseComputer.createNoiseSamplers(new Random(seed))` — fixes order to match OTG.
- Remove local noise methods: `sampleNoise`, `getInterpolationNoise`, `getInterpolatedNoise`, `getExtraHeightAt` — replaced by `TerrainNoiseComputer.*` calls.
- Keep: noise grid caching, trilinear interpolation, async generation, block placement, property extraction. These are preview-specific, not shared.

## File Impact Summary

| File | Change | LOC Impact |
|------|--------|------------|
| `TerrainNoiseComputer.java` | NEW | ~120 LOC |
| `OTGChunkGenerator.java` | Delegate 4 methods + factory | ~-80 LOC (remove method bodies, add delegations) |
| `BiomeHeightmapGenerator.java` | Remove 4 methods, use factory | ~-100 LOC |

Net: ~120 new shared, ~180 removed duplicate = ~60 LOC reduction.

## What Does NOT Change

- `OTGChunkGenerator.generateNoiseColumn()` — biome blending, CHC smoothing, falloff loop
- `OTGChunkGenerator.populateNoise()` — density normalization, block placement
- `BiomeHeightmapGenerator.generate()` — noise grid caching, trilinear interpolation, async pattern, block placement
- World generation output — identical terrain for same seed
- No new dependencies between modules
