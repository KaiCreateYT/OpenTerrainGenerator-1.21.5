# GameRules Per-Dimension — Design

**Date:** 2026-03-01
**Status:** Approved

## Goal

Apply GameRules from PresetConfig.ini (+ optional DimensionConfig.yaml override) to OTG dimensions at creation time. Per-dimension GameRules. One-time at creation, persists normally.

## Architecture

```
PresetConfig.ini (GameRuleSettings)
        ↓ base rules
DimensionConfig.yaml (GameRules)     ← optional override
        ↓ merged
GameRuleManager  ←→  OTGWorldStorage (persistence)
        ↓
Level.getGameRules() mixin  →  per-dimension GameRules
```

## Components

### New Classes

| Class | Location | Responsibility |
|-------|----------|----------------|
| `GameRuleManager` | `platforms/shared/` | Static map `ResourceKey<Level> → GameRules`, CRUD, lookup for mixin |
| `GameRuleApplier` | `platforms/shared/` | Creates MC `GameRules` from `GameRuleSettings` + optional `DimensionConfig.GameRules` |
| `LevelGameRulesMixin` | `platforms/shared/mixin/` | Intercepts `Level.getGameRules()`, dispatches to GameRuleManager |

### Modified Classes

| Class | Change |
|-------|--------|
| `DimensionStorage` → `OTGWorldStorage` | Rename, expand to include gameRules section, v1→v2 migration |
| `GameRuleSettings` | Add 20 new 1.21.1 rules |
| `DimensionConfig.GameRules` | Add 20 new 1.21.1 rules |
| `DimensionManager` | Integration with GameRuleManager at create/delete/initialize |
| `PresetWriter` | Write new GameRule settings |
| `PresetConfig` | Read new GameRule settings |

## Mixin Design

```java
@Mixin(Level.class)
public abstract class LevelGameRulesMixin {
    @Inject(method = "getGameRules", at = @At("HEAD"), cancellable = true)
    private void otg$getGameRules(CallbackInfoReturnable<GameRules> cir) {
        if ((Object) this instanceof ServerLevel serverLevel) {
            GameRules custom = GameRuleManager.getGameRules(serverLevel.dimension());
            if (custom != null) {
                cir.setReturnValue(custom);
            }
        }
    }
}
```

## GameRule Hierarchy

1. **Base:** MC vanilla defaults (from `new GameRules()`)
2. **Override 1:** PresetConfig.ini `GameRuleSettings` (per-preset, always applied if `OverrideGameRules: true`)
3. **Override 2:** DimensionConfig.yaml `GameRules` section (optional, per-dimension, overrides preset)

## Dimension Handling

### Overworld (OTG preset)
- At `DimensionManager.initialize()`, detect if overworld uses OTG preset
- Load preset's `GameRuleSettings` + optional DimensionConfig override
- Register in `GameRuleManager` under `Level.OVERWORLD` key
- Mixin intercepts `Level.getGameRules()` — same mechanism as custom dimensions

### Custom Dimensions
- At `DimensionManager.createDimension()`, merge rules and register in `GameRuleManager`
- At `DimensionManager.initialize()` (restart), reload from `OTGWorldStorage`

## Persistence

### OTGWorldStorage (renamed from DimensionStorage)

Single file `otg_world_data.json`, both sections as maps:

```json
{
  "version": 2,
  "dimensions": {
    "my_dim": {
      "preset": "VanillaVistas",
      "seed": 42,
      "created": 1234567890
    }
  },
  "gameRules": {
    "minecraft:overworld": {
      "doFireTick": true,
      "keepInventory": false
    },
    "otg:my_dim": {
      "doFireTick": true,
      "keepInventory": true
    }
  }
}
```

### Migration v1 → v2
- Detect `otg_dimensions.json` (v1) → convert array to map, add empty `gameRules`, save as `otg_world_data.json`
- Keep old file as backup

### Save Triggers
- On world save events (existing hooks: NeoForge `LevelEvent.Save`, Fabric `WorldSaveCallback`)
- On dimension create/delete

## New GameRules for 1.21.1

20 rules to add to both `GameRuleSettings` and `DimensionConfig.GameRules`:

| Rule | Type | Default | MC Version |
|------|------|---------|------------|
| `projectilesCanBreakBlocks` | bool | true | 1.20.2 |
| `reducedDebugInfo` | bool | false | 1.8 |
| `maxCommandForkCount` | int | 65536 | 1.20.2 |
| `commandModificationBlockLimit` | int | 32768 | 1.20.2 |
| `doImmediateRespawn` | bool | false | 1.15 |
| `playersNetherPortalDefaultDelay` | int | 80 | 1.20.2 |
| `playersNetherPortalCreativeDelay` | int | 1 | 1.20.2 |
| `freezeDamage` | bool | true | 1.17 |
| `doWardenSpawning` | bool | true | 1.19 |
| `playersSleepingPercentage` | int | 100 | 1.17 |
| `blockExplosionDropDecay` | bool | true | 1.19.3 |
| `mobExplosionDropDecay` | bool | true | 1.19.3 |
| `tntExplosionDropDecay` | bool | false | 1.19.3 |
| `snowAccumulationHeight` | int | 1 | 1.19.3 |
| `waterSourceConversion` | bool | true | 1.19.3 |
| `lavaSourceConversion` | bool | false | 1.19.3 |
| `globalSoundEvents` | bool | true | 1.19.3 |
| `doVinesSpread` | bool | true | 1.19.3 |
| `enderPearlsVanishOnDeath` | bool | true | 1.20.2 |
| `spawnChunkRadius` | int | 2 (0-32) | 1.20.2 |

## YAGNI — Not In Scope

- GUI/commands for editing GameRules (vanilla `/gamerule` suffices)
- Hot-reload of configs (restart required)
- Per-player GameRules
- Full DimensionConfig.yaml support (dimensions, portals) — separate task
- ForceGameRules flag (re-apply on every restart) — future enhancement
