# bindTags() Performance Bug

## Problem

`WorldPresetTagsMixin.addBiomesToStructureTags()` calls `biomeRegistry.bindTags()` after
injecting OTG biomes into vanilla structure biome tags. This causes a massive chunk generation
slowdown for presets that use `Registry()` placed features (like DefaultPreset).

## Root Cause (bisected)

Commit `76ad2ac12` ("inject OTG biomes into vanilla structure biome tags (NeoForge)") introduced
the regression. Bisected from `b8495f5ce` (fast) through `f30828b8f` (slow).

`biomeRegistry.bindTags()` replaces ALL biome tag bindings. This likely invalidates
`HolderSet.Named` instances that MC caches internally in `BiomeGenerationSettings` and
`FeatureSorter`. After the invalidation, MC must rebuild feature ordering per-chunk during
`applyBiomeDecoration()` instead of reusing the cached sort.

## Why DefaultPreset is affected but Biome Bundle is not

| | DefaultPreset | Biome Bundle |
|---|---|---|
| Biomes | 89 | 445 |
| Registry() placed features | 64 entries | 0 entries |
| TemplateBiomes (inherit vanilla features) | 15 | 0 |
| BiomeGenerationSettings content | Real feature lists | Empty |
| Feature sort cost per chunk | Expensive (O(biomes × features)) | Free (nothing to sort) |

When `bindTags()` invalidates the cache, DefaultPreset pays the full feature-sort cost every
chunk. BB has empty BiomeGenerationSettings so the sort is trivially cheap regardless.

## Current Fix

`addBiomesToStructureTags()` is commented out on both Fabric and NeoForge. This means vanilla
structures (villages, mineshafts, strongholds, etc.) won't spawn in OTG biomes.

## Possible Proper Fixes

### Option A: Avoid bindTags() entirely
Instead of collecting all tags and rebinding, directly manipulate the `Holder.Reference` objects
to add them to existing `HolderSet.Named` instances without triggering a full rebind. May require
access to internal MC fields via mixin/accessor.

### Option B: Re-bake feature cache after bindTags()
After calling `bindTags()`, trigger MC to rebuild whatever feature ordering cache was invalidated.
Need to identify exactly what MC caches and how to force a rebuild. Look at:
- `ChunkGenerator.featuresPerStep` / `BiomeSource.featuresPerStep()`
- `FeatureSorter.buildFeaturesPerStep()`
- Any lazy-init fields that check `HolderSet.Named.isBound()`

### Option C: Register biomes into structure tags BEFORE MC builds its caches
Move the tag injection to an earlier point in the lifecycle — during biome registration itself,
before `ReloadableServerResources.updateRegistryTags()` builds the initial tag map. This way
the first `bindTags()` call already includes OTG biomes, and no second call is needed.

## Files

- `platforms/neoforge/.../mixin/WorldPresetTagsMixin.java`
- `platforms/fabric/.../mixin/WorldPresetTagsMixin.java`
- `platforms/shared/.../biome/SharedLegacyBiomeLoader.java` (populates structureTagConfigs)
- `common/common-util/.../biome/StructureTagMapper.java` (maps config → tag paths)
