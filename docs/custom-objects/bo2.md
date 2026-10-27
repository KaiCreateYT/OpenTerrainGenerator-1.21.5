# BO2

BO2 is OTG's legacy custom object format — simpler than BO3 with fewer features, but still fully supported. BO2 objects can be used anywhere BO3 objects can — OTG handles them transparently.

---

## File Format

Settings as key-value pairs, blocks as `x,z,y=material`:

```ini
spawnSunlight=true
spawnDarkness=false
spawnAboveGround=true
randomRotation=true
tree=true
needsFoundation=true
rarity=75
spawnElevationMin=64
spawnElevationMax=256
spawnOnBlockType=grass_block,dirt

0,0,0=oak_log
0,0,1=oak_log
1,0,0=oak_leaves
-1,0,0=oak_leaves
```

Note: BO2 coordinates are `x,z,y` (not `x,y,z` like BO3).

---

## Settings

| Setting | Type | Default | Description |
|---------|------|---------|-------------|
| `spawnSunlight` | `boolean` | `true` | Spawn in lit areas |
| `spawnDarkness` | `boolean` | `true` | Spawn in dark areas |
| `spawnWater` | `boolean` | `false` | Spawn in water |
| `spawnLava` | `boolean` | `false` | Spawn in lava |
| `spawnAboveGround` | `boolean` | `false` | Spawn on surface |
| `spawnUnderGround` | `boolean` | `false` | Spawn underground |
| `randomRotation` | `boolean` | `true` | Random rotation |
| `dig` | `boolean` | `false` | Replace non-air blocks |
| `tree` | `boolean` | `false` | Can spawn as tree |
| `needsFoundation` | `boolean` | `true` | Solid ground 5 blocks below |
| `doReplaceBlocks` | `boolean` | `true` | Apply biome surface replacements |
| `collisionPercentage` | `int` | `2` | Max % blocked blocks |
| `rarity` | `int` | `100` | Spawn chance (1–1,000,000) |
| `spawnElevationMin` | `int` | `0` | Min Y |
| `spawnElevationMax` | `int` | `128` | Max Y |
| `spawnOnBlockType` | `block list` | `GRASS_BLOCK` | Required ground blocks |

---

## Limitations

- No branching
- No entity spawning
- No NBT support
- No block/light/mod checks
- No extrusion
- Basic collision detection (percentage threshold only)
