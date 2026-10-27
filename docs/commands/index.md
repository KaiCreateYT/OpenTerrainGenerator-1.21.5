# Commands

All OTG commands start with `/otg`. Most require operator permissions (level 2).

## Command Tree

```
/otg
├── dimension
│   ├── create <preset>
│   ├── delete <dimension> [--purge [--confirm]]
│   ├── list
│   └── info <dimension>
├── tp <dimension>
├── spawn <preset> <object> [rotation]
├── export <name> <preset> [template] [flags...]
├── structure
├── flushcache
└── exportbo4data
```

---

## Dimension Management

### /otg dimension create

Creates a new OTG dimension at runtime — no restart required.

```
/otg dimension create <preset>
```

| Argument | Description |
|----------|-------------|
| `preset` | Name of a DimensionPreset folder |

The dimension is registered as `otg:<name>` with a random seed and is immediately playable. Use `/otg tp` to visit it.

**Permission:** Op (level 2)

### /otg dimension delete

Removes an OTG dimension. Players in the dimension are teleported to the overworld.

```
/otg dimension delete <dimension>
/otg dimension delete <dimension> --purge --confirm
```

Without `--purge`, the dimension is unregistered but world data remains on disk. With `--purge --confirm`, world data is permanently deleted.

**Permission:** Op (level 2)

### /otg dimension list

Lists all OTG-managed dimensions with their preset and creation date.

```
/otg dimension list
```

**Permission:** All players

### /otg dimension info

Shows detailed information about a specific OTG dimension.

```
/otg dimension info <dimension>
```

Displays: dimension key, preset used, seed, creation date.

**Permission:** All players

---

## Teleportation

### /otg tp

Teleports to any dimension (vanilla or OTG).

```
/otg tp <dimension>
```

Supports shorthand names: `overworld`, `nether`, `the_nether`, `end`, `the_end`, or any OTG dimension name.

**Permission:** All players (player-only, no console)

---

## Custom Objects

### /otg spawn

Spawns a custom object at the player's location.

```
/otg spawn <preset> <object> [rotation]
```

| Argument | Description |
|----------|-------------|
| `preset` | DimensionPreset containing the object |
| `object` | BO3/BO4 object name |
| `rotation` | `NORTH`, `SOUTH`, `EAST`, `WEST` (default: `NORTH`) |

For BO3: spawns at the block the player is looking at (200 block range).

For BO4 structures: searches outward in a spiral for unpopulated chunks to plot the structure.

**Permission:** Op (level 2)

### /otg export

Exports a WorldEdit selection as a BO3 or BO4 object.

```
/otg export <name> <preset> [template] [flags...]
```

| Argument | Description |
|----------|-------------|
| `name` | Name for the exported object |
| `preset` | Target DimensionPreset folder (or `global`) |
| `template` | Template BO3/BO4 for config defaults (or `default`) |

**Flags:**

| Flag | Description |
|------|-------------|
| `-a` | Include air blocks |
| `-o` | Overwrite existing file |
| `-b` | Force as branching structure |
| `-t` | Include tile entities |
| `-bo4` | Export as BO4 instead of BO3 |
| `-e <blocks>` | Exclude blocks (comma-separated, e.g. `-e stone,dirt`) |

Requires a WorldEdit selection (`//wand`, `//pos1`, `//pos2`).

**Permission:** Op (level 2)

### /otg structure

Displays information about the OTG structure at the player's current chunk.

```
/otg structure
```

Shows: object type, name, author, description. For BO4, also lists branches in the current chunk.

**Permission:** All players

### /otg flushcache

Reloads the custom object cache from disk. Use after manually adding or editing BO3/BO4 files.

```
/otg flushcache
```

**Permission:** Op (level 2)

### /otg exportbo4data

Generates `.BO4Data` files for all BO4 structures in the current preset. Runs in the background — run again to check progress.

```
/otg exportbo4data
```

Only works in worlds with `CustomStructureType: BO4`. Can take several minutes for large presets.

**Permission:** Op (level 2)
