# BO3

BO3 (Block Object 3) is OTG's primary custom object format. It supports block placement, spawn conditions, entity spawning, branching structures, and tree growth from saplings.

---

## File Structure

A `.bo3` file is a text config with settings and functions:

```ini
# Settings
Author=MyName
Description=A custom oak tree
Tree=true
RotateRandomly=true
SpawnHeight=highestSolidBlock

# Functions
Block(0,0,0,minecraft:oak_log)
Block(0,1,0,minecraft:oak_log)
Block(0,2,0,minecraft:oak_leaves)
BlockCheck(0,-1,0,GRASS_BLOCK,DIRT)
```

---

## Settings

### Identity

| Setting | Type | Default | Description |
|---------|------|---------|-------------|
| `Author` | `string` | `Unknown` | Creator name |
| `Description` | `string` | `No description given` | Short description |
| `Version` | `string` | `3` | Format version (don't change) |
| `SettingsMode` | `enum` | `WriteDisable` | `WriteAll`, `WriteWithoutComments`, `WriteDisable` |

### Spawning

| Setting | Type | Default | Description |
|---------|------|---------|-------------|
| `Tree` | `boolean` | `true` | Can spawn as `Tree()` resource / from saplings |
| `RotateRandomly` | `boolean` | `false` | Random rotation (0/90/180/270) on spawn |
| `Frequency` | `int` | `0` | Spawn attempts per chunk (for `CustomObject()`) |
| `Rarity` | `double` | `100.0` | Chance per attempt, 0.000001–100 (for `CustomObject()`) |
| `MaxSpawn` | `int` | `0` | Stop after N successful spawns (0 = unlimited) |

### Height

| Setting | Type | Default | Description |
|---------|------|---------|-------------|
| `SpawnHeight` | `enum` | `highestBlock` | `randomY`, `highestBlock`, `highestSolidBlock` |
| `SpawnHeightOffset` | `int` | `0` | Blocks above/below calculated height |
| `SpawnHeightVariance` | `int` | `0` | Random variance added to offset |
| `MinHeight` | `int` | `0` | Minimum Y |
| `MaxHeight` | `int` | `256` | Maximum Y |

**SpawnHeight modes:**

- **randomY** — random Y between MinHeight and MaxHeight
- **highestBlock** — highest block including leaves, snow, etc.
- **highestSolidBlock** — highest solid block (excludes leaves, snow)

### Source Blocks

| Setting | Type | Default | Description |
|---------|------|---------|-------------|
| `SourceBlocks` | `block list` | `AIR` | Blocks the BO3 can replace when spawning |
| `MaxPercentageOutsideSourceBlock` | `int` | `100` | Max % of blocks outside SourceBlocks |
| `OutsideSourceBlock` | `enum` | `placeAnyway` | `placeAnyway` or `dontPlace` |
| `DoReplaceBlocks` | `boolean` | `true` | Apply biome's ReplacedBlocks |

### Extrusion

Automatically fills terrain gaps under/above the structure.

| Setting | Type | Default | Description |
|---------|------|---------|-------------|
| `ExtrudeMode` | `enum` | `None` | `None`, `BottomDown`, `TopUp` |
| `ExtrudeThroughBlocks` | `block list` | `AIR` | Blocks to extrude through |

### Branching

| Setting | Type | Default | Description |
|---------|------|---------|-------------|
| `MaxBranchDepth` | `int` | `10` | Max nesting depth for Branch() functions |

---

## Functions

Functions define what the BO3 places and where. Coordinates are relative to the BO3 origin (0,0,0).

### Block Placement

**Block(x, y, z, blockName [, nbtFile])**

Places a single block. Optional `.nbt` file for tile entity data (chests, signs, etc.).

```
Block(0,0,0,minecraft:oak_log)
Block(3,1,2,minecraft:chest,loot_chest.nbt)
```

Alias: `B(...)`

**RandomBlock(x, y, z, block1, chance1 [, block2, chance2, ...])**

Places one random block from a weighted list. Chances are evaluated in order — first match wins.

```
RandomBlock(0,0,0,minecraft:oak_log,50,minecraft:stone,100)
```

50% oak log, 50% stone. Alias: `RB(...)`

### Entity Spawning

**Entity(x, y, z, entityName, groupSize [, nbtFile.txt])**

Spawns entities. NBT data uses `.txt` files (not `.nbt`) with `/summon` command format.

```
Entity(0,2,0,minecraft:creeper,1)
Entity(5,1,5,minecraft:skeleton,2,boss_skeleton.txt)
```

Alias: `E(...)`

### Condition Checks

Checks run before the BO3 spawns. If any check fails, the entire BO3 is skipped.

**BlockCheck(x, y, z, block1 [, block2, ...])**

Requires one of the listed blocks at the position. Special values: `Solid` (any solid), `All` (any non-air).

```
BlockCheck(0,-1,0,GRASS_BLOCK,DIRT)
```

Alias: `BC(...)`

**BlockCheckNot(x, y, z, block1 [, block2, ...])**

Fails if any listed block is present (inverse of BlockCheck).

```
BlockCheckNot(0,-1,0,WATER,LAVA)
```

Alias: `BCN(...)`

**LightCheck(x, y, z, minLight, maxLight)**

Requires light level within range (0–16).

```
LightCheck(0,0,0,8,16)
```

Alias: `LC(...)`

**ModCheck(mod1 [, mod2, ...])**

Requires all listed mods to be loaded. `ModCheckNot(...)` is the inverse.

```
ModCheck(biomesoplenty)
```

### Branching

**Branch(x, y, z, object1, rotation1, chance1 [, object2, rotation2, chance2, ...] [, maxChance])**

Spawns child BO3 objects. Each branch has an independent chance. First match wins.

```
Branch(5,0,5,OakBranch,NORTH,50,SpruceBranch,NORTH,50)
```

Alias: `BR(...)`

**WeightedBranch(x, y, z, object1, rotation1, chance1 [, ...] [, maxChance])**

Cumulative chances — exactly one branch spawns. Chances sum up: first=20, second=50 means 20% first, 30% second, 50% nothing.

```
WeightedBranch(0,0,0,SmallHouse,NORTH,30,LargeHouse,NORTH,60,100)
```

Alias: `WBR(...)`

Rotations: `NORTH`, `EAST`, `SOUTH`, `WEST`

---

## NBT Files

**Block NBT** (`.nbt` binary format) — for tile entities like chests, banners, signs. Stored relative to the `.bo3` file.

**Entity NBT** (`.txt` text format) — JSON-like NBT from `/summon` command format:

```
{CustomName:"\"Boss\"",Health:30.0f}
```

---

## Example

```ini
Author=TreeMaker
Description=Custom oak with branches
Tree=true
RotateRandomly=true
SpawnHeight=highestSolidBlock
SpawnHeightOffset=0
MinHeight=50
MaxHeight=256
SourceBlocks=GRASS_BLOCK,DIRT
MaxPercentageOutsideSourceBlock=10
MaxBranchDepth=5

# Trunk
Block(0,0,0,minecraft:oak_log)
Block(0,1,0,minecraft:oak_log)
Block(0,2,0,minecraft:oak_log)
Block(0,3,0,minecraft:oak_log)

# Canopy
Block(0,4,0,minecraft:oak_leaves)
Block(1,3,0,minecraft:oak_leaves)
Block(-1,3,0,minecraft:oak_leaves)
Block(0,3,1,minecraft:oak_leaves)
Block(0,3,-1,minecraft:oak_leaves)

# Random leaf variation
RandomBlock(1,4,0,minecraft:oak_leaves,70,minecraft:air,100)

# Ground check
BlockCheck(0,-1,0,GRASS_BLOCK,DIRT)

# Optional branch structure
Branch(3,2,0,OakBranch,NORTH,50)
```

