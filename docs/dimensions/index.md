# Dimensions

OTG supports creating custom dimensions at runtime with their own terrain, portals, and GameRules. Dimensions can be added via commands, WorldPreset YAML, or DimensionPresetConfig.ini — no server restart required.

---

## Adding Dimensions

### Via Command (Runtime)

```
/otg dimension create <preset>
```

Creates a dimension immediately from any installed DimensionPreset. The dimension is registered as `otg:<name>` and is playable right away.

### Via WorldPreset YAML

List dimensions in the `Dimensions` array:

```yaml
Dimensions:
  - PresetFolderName: "CrystalCaves"
    PortalBlocks: "minecraft:diamond_block"
    PortalColor: "crystalblue"
  - PresetFolderName: "Skylands"
    PortalColor: "gold"
```

These dimensions are created when the world is first loaded.

### Via DimensionPresetConfig.ini

Each DimensionPreset defines its own portal configuration. When installed, it can be accessed in-game by building and igniting a portal with the matching frame blocks.

---

## Portals

OTG dimensions are connected through colored portals, similar to Nether portals but fully configurable.

### How Portals Work

1. Build a rectangular frame from the dimension's **portal block** (e.g. diamond blocks)
2. Right-click the frame with the **ignition source** (e.g. flint and steel)
3. The portal activates with the dimension's **color**
4. Step through to teleport

Portal routing is color-based — each dimension has a unique color, and entering a portal of that color takes you to that dimension. Returning through the same portal sends you back.

### Portal Configuration

Each DimensionPreset defines its portal properties in `DimensionPresetConfig.ini`:

| Setting | Default | Description |
|---------|---------|-------------|
| `PortalBlocks` | `minecraft:quartz_block` | Block(s) forming the portal frame |
| `PortalColor` | `default` | Portal particle color |
| `PortalIgnitionSource` | `minecraft:flint_and_steel` | Item used to light the portal |
| `PortalMob` | `minecraft:zombified_piglin` | Entity that spawns from the portal |
| `PortalMinWidth` | `2` | Minimum interior width |
| `PortalMaxWidth` | `21` | Maximum interior width |
| `PortalMinHeight` | `3` | Minimum interior height |
| `PortalMaxHeight` | `21` | Maximum interior height |

These can be overridden per-dimension in a [WorldPreset YAML](../config/world-presets.md).

### Available Portal Colors

`default`, `beige`, `black`, `blue`, `crystalblue`, `darkblue`, `darkgreen`, `darkred`, `emerald`, `flame`, `gold`, `green`, `grey`, `lightblue`, `lightgreen`, `orange`, `pink`, `red`, `white`, `yellow`

If two dimensions share the same color, OTG auto-increments the second one to avoid conflicts.

### Teleportation Details

- Coordinates are scaled by the dimension's `CoordinateScale` setting (e.g. 8.0 for Nether-style 1:8 ratio)
- Existing portals are reused within a 128-block radius of the destination
- If no portal exists at the destination, one is automatically created

### Portal Gating

When a WorldPreset YAML is active, only dimensions listed in the YAML's `Dimensions` array get portals. Without a YAML, all installed DimensionPresets with portal configuration are accessible.

---

## Per-Dimension GameRules

Each OTG dimension can have its own GameRules, independent of other dimensions. See [DimensionPresetConfig.ini — GameRules](../config/dimension-preset-config.md#gamerules) for all available rules.

### Override Hierarchy

GameRules follow a 3-layer system:

1. **DimensionPresetConfig.ini** — base values (requires `OverrideGameRules: true`)
2. **WorldPreset YAML — world-level `GameRules`** — overrides for all dimensions
3. **WorldPreset YAML — per-dimension `GameRules`** — overrides for a specific dimension

Each layer only overrides what it explicitly sets. Unspecified values fall through to the previous layer.

### Example

```yaml
# World-level: all dimensions keep inventory
GameRules:
  KeepInventory: true
  DoFireTick: true

Dimensions:
  - PresetFolderName: "Hellscape"
    GameRules:
      DoFireTick: false     # Only Hellscape disables fire
      # KeepInventory not set → inherits true from world-level
```

### How It Works

OTG uses a mixin on `Level.getGameRules()` to return dimension-specific rules instead of the world default. The `GameRuleManager` stores a separate `GameRules` instance per dimension in a static map. Rules are persisted in `otg_world_data.json` and restored on server restart.

---

## Dimension Persistence

OTG stores dimension metadata in `otg_world_data.json` inside the world folder:

```json
{
  "version": 2,
  "dimensions": {
    "customdim": {
      "name": "customdim",
      "preset": "CustomPreset",
      "seed": 12345678,
      "created": 1709251234567
    }
  },
  "gameRules": {
    "otg:customdim": {
      "doFireTick": "true",
      "keepInventory": "true"
    }
  },
  "worldPreset": "MyWorld"
}
```

On server restart, all OTG dimensions are automatically recreated from this file. World data (chunks, entities) is stored in `dimensions/otg/<name>/` inside the world folder.

---

## Dimension Settings

Each dimension's properties (lighting, physics, height) are configured in `DimensionPresetConfig.ini`. See [Dimension Settings](../config/dimension-preset-config.md#dimension-settings) for all options.

Key settings:

| Setting | Description | Example |
|---------|-------------|---------|
| `MinY` | Minimum Y coordinate | `-64` (overworld), `0` (nether) |
| `Height` | Total world height | `384` (overworld), `256` (nether) |
| `HasSkylight` | Sky and sunlight | `false` for nether-like |
| `HasCeiling` | Bedrock ceiling | `true` for nether-like |
| `UltraWarm` | Water evaporates | `true` for nether-like |
| `CoordinateScale` | Travel ratio vs overworld | `8.0` for nether-style |
| `FixedTime` | Lock time of day | `18000` for perpetual midnight |
| `AmbientLight` | Base light level | `0.1` for nether-like |

---

## Managing Dimensions

| Task | Method |
|------|--------|
| Create at runtime | `/otg dimension create <preset>` |
| List all | `/otg dimension list` |
| Get info | `/otg dimension info <name>` |
| Teleport to | `/otg tp <name>` |
| Delete (keep data) | `/otg dimension delete <name>` |
| Delete (purge data) | `/otg dimension delete <name> --purge --confirm` |
