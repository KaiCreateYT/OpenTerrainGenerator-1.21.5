# WorldPreset System — Design

## Goal

Introduce first-class WorldPresets: YAML files that define composed worlds (multiple dimensions with per-dimension GameRules), selectable as world types in the MC GUI. Rename existing "presets" to "DimensionPresets" to clarify the conceptual model.

## Core Concepts

- **DimensionPreset** (formerly "Preset"): defines terrain generation for a single dimension (biomes, noise, blocks). Lives in `DimensionPresets/<FolderName>/DimensionPresetConfig.ini`.
- **WorldPreset**: YAML file that composes a full world from multiple DimensionPresets + world-level and per-dimension GameRules. Lives in `WorldPresets/<Name>.yaml`. Registered as an MC `WorldPreset` and visible in the world creation GUI.

## Naming Rename Map

| Old | New |
|-----|-----|
| `Preset.java` | `DimensionPreset.java` |
| `PresetConfig.java` | `DimensionPresetConfig.java` |
| `PresetWriter.java` | `DimensionPresetWriter.java` |
| `LocalPresetLoader.java` | `LocalDimensionPresetLoader.java` |
| `PresetConfigLoader.java` | `DimensionPresetConfigLoader.java` |
| `PresetInfo.java` | `DimensionPresetInfo.java` |
| `PresetConfig.ini` (file) | `DimensionPresetConfig.ini` (file) |
| `Presets/` (folder) | `DimensionPresets/` (folder) |
| `DimensionConfig.java` | `WorldPresetConfig.java` |
| `DimensionConfigLoader.java` | `WorldPresetConfigLoader.java` |
| `DimensionConfigs/` (folder) | `WorldPresets/` (folder) |
| `Constants.PRESETS_FOLDER` | `Constants.DIMENSION_PRESETS_FOLDER` |
| `Constants.PRESET_CONFIG_FILE` | `Constants.DIMENSION_PRESET_CONFIG_FILE` |
| `Constants.DIMENSION_CONFIGS_FOLDER` | `Constants.WORLD_PRESETS_FOLDER` |

~25 Java files, ~99 occurrences of `Preset` to rename. All renames are breaking changes — no automatic migration, clear error messages when old paths detected.

## WorldPreset YAML Format

```yaml
# ============================================================
# WorldPreset Configuration
# ============================================================
# A WorldPreset defines a complete world: which DimensionPresets
# to use for each dimension, plus optional per-dimension GameRules.
#
# WorldPresets appear as selectable world types in the MC GUI
# alongside individual DimensionPresets.
#
# GameRules hierarchy (3 layers, each overrides the previous):
#   1. DimensionPresetConfig.ini GameRules (base from the dimension's preset)
#   2. This file's top-level GameRules (world-level defaults)
#   3. Per-dimension GameRules (dimension-specific overrides)
#
# Only rules explicitly listed are overridden — omitted rules
# keep their value from the previous layer.
# ============================================================
---
Version: 1

# DisplayName: shown in the MC world creation GUI.
# Required — YAMLs without DisplayName are not registered.
DisplayName: "JMc's World"

# Description: optional, shown in the world creation GUI.
Description: "A custom world with BiomeBundle overworld, AlienJungle nether, and extra dimensions."

# ModpackName: legacy field for modpack identification.
# ModpackName: "My Awesome Modpack"

# ============================================================
# World-level GameRules
# ============================================================
# Applied to ALL dimensions in this world unless overridden
# per-dimension below. Only set rules you want to change from
# the DimensionPreset defaults.
GameRules:
  KeepInventory: true
  DoDaylightCycle: false

# ============================================================
# Overworld
# ============================================================
# PresetFolderName: which DimensionPreset to use for the overworld.
# Use NonOTGWorldType for vanilla/modded overworld types (e.g., "flat").
Overworld:
  PresetFolderName: "BiomeBundle"
  # Per-dimension GameRules override for the overworld only.
  GameRules:
    DoDaylightCycle: true    # re-enable daylight cycle in overworld

# ============================================================
# Nether
# ============================================================
# Omit or set PresetFolderName to null for vanilla nether.
Nether:
  PresetFolderName: "AlienJungle"
  # No GameRules here = uses world-level defaults

# ============================================================
# End
# ============================================================
# Omit or set PresetFolderName to null for vanilla end.
End:
  PresetFolderName: "Skylands"
  GameRules:
    DoMobSpawning: false

# ============================================================
# Custom Dimensions
# ============================================================
# Each entry creates an additional OTG dimension with its own
# DimensionPreset and optional portal/GameRules configuration.
Dimensions:
- PresetFolderName: "Wildlands"
  Seed: 14
  PortalColor: "beige"
  PortalMob: "minecraft:zombified_piglin"
  PortalIgnitionSource: "minecraft:flint_and_steel"
  PortalBlocks: "minecraft:redstone_block"
  GameRules:
    DoWeatherCycle: false
    DoFireTick: false

- PresetFolderName: "VanillaVistas"
  Seed: 42
  PortalColor: "gold"
  PortalMob: "minecraft:zombified_piglin"
  PortalIgnitionSource: "minecraft:flint_and_steel"
  PortalBlocks: "minecraft:diamond_block"
  # No GameRules = uses world-level defaults

# ============================================================
# World Settings
# ============================================================
Settings:
  GenerateStructures: true
  BonusChest: false
```

## Java Model

```java
public class WorldPresetConfig {
    public int Version;
    public String DisplayName;         // NEW — required for GUI registration
    public String Description;         // NEW — optional GUI description
    public String ModpackName;         // legacy compat
    public boolean isModpackConfig;    // internal flag
    public OTGOverWorld Overworld;
    public OTGDimension Nether;
    public OTGDimension End;
    public List<OTGDimension> Dimensions = new ArrayList<>();
    public Settings Settings;
    public GameRules GameRules;        // world-level defaults

    public static class OTGOverWorld extends OTGDimension {
        public String NonOTGWorldType;
        public String NonOTGGeneratorSettings;
    }

    public static class OTGDimension {
        public String PresetFolderName;
        public long Seed;
        public String PortalBlocks;
        public String PortalColor;
        public String PortalMob;
        public String PortalIgnitionSource;
        public GameRules GameRules;    // NEW: per-dimension override
    }

    public static class GameRules {
        // 51 nullable Boolean/Integer fields (unchanged from previous work)
    }
}
```

## Registration Flow

### Existing flow (preserved, renamed)

```
OTGEngine.onStart()
  → LocalDimensionPresetLoader.loadPresetsFromDisk()
  → RegistryLoaderMixin
  → OTGRegistryHelper.loadOTGPresets()
      → for each DimensionPreset with SelectableInWorldCreation:
          → createLevelStems() → register MC WorldPreset
      → WorldPresetTagsMixin adds to NORMAL tag
  → User selects "OTG: Biome Bundle" in GUI → single-dimension world
```

### New flow (added)

```
OTGEngine.onStart()
  → ALSO loads WorldPreset YAMLs from WorldPresets/ folder
  → RegistryLoaderMixin
  → OTGRegistryHelper.loadOTGPresets()
      → [existing] individual DimensionPresets registered
      → [NEW] WorldPresetRegistrar.register()
          → for each YAML with DisplayName:
              → resolve PresetFolderName → loaded DimensionPreset
              → createLevelStems() from YAML's Overworld/Nether/End/Dimensions
              → register as MC WorldPreset: otg:<normalized_display_name>
      → WorldPresetTagsMixin adds ALL to NORMAL tag
  → User sees both "OTG: Biome Bundle" AND "JMc's World" in GUI
```

### GameRules application

```
World created from WorldPreset YAML
  → DimensionManager.initialize()
      → detect: was this world created from a WorldPreset YAML?
          → OTGWorldStorage stores "worldPreset" field
      → if first start (no persisted GameRules):
          → load WorldPresetConfig YAML
          → for each dimension:
              1. DimensionPresetConfig.ini GameRules (base)
              2. YAML world-level GameRules (override)
              3. YAML per-dimension GameRules (override)
          → merge via GameRuleApplier (extended to 3 layers)
          → register in GameRuleManager + persist to OTGWorldStorage
      → if subsequent start:
          → restore from OTGWorldStorage (already implemented)
```

## New Classes

| Class | Purpose |
|-------|---------|
| `WorldPresetRegistrar` | Loads YAMLs, creates LevelStems, registers MC WorldPresets |
| `WorldPresetConfigLoader` | Renamed DimensionConfigLoader, loads all YAMLs from folder |

## Edge Cases

| Scenario | Behavior |
|----------|----------|
| YAML references nonexistent DimensionPreset | Log error, skip that dimension |
| YAML without `DisplayName` | Not registered in GUI (treated as draft) |
| YAML without `Overworld` | Error — overworld is required |
| `NonOTGWorldType` overworld + GameRules | GameRules applied but OTG doesn't control chunk generation |
| `/otg dimension create` on WorldPreset world | Works normally, dim added to OTGWorldStorage |
| Two YAMLs with same `DisplayName` | Warn, second is skipped (first wins) |
| Old `Presets/` folder exists | Clear error log with rename instructions |
| Old `DimensionConfigs/` folder exists | Clear error log with rename instructions |
| Old `PresetConfig.ini` exists | Clear error log with rename instructions |

## Breaking Changes

1. `Presets/` → `DimensionPresets/` — users must move their preset folders
2. `PresetConfig.ini` → `DimensionPresetConfig.ini` — users must rename
3. `DimensionConfigs/` → `WorldPresets/` — old YAMLs must be moved
4. All external tooling/docs referencing old names must update

No automatic migration. Clear error messages at startup when old paths detected.
