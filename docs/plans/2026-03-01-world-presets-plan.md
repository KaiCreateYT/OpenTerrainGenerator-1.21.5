# WorldPreset System — Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Rename Preset → DimensionPreset across the codebase, then add a WorldPreset system where YAML files compose full worlds from multiple DimensionPresets with per-dimension GameRules, selectable in the MC world creation GUI.

**Architecture:** Two-phase approach. Phase 1 renames all Preset-related classes, files, and folders to DimensionPreset (mechanical refactor, one big commit). Phase 2 extends the existing DimensionConfig (renamed to WorldPresetConfig) with DisplayName, per-dimension GameRules, and a new WorldPresetRegistrar that hooks into OTGRegistryHelper to register YAMLs as MC WorldPresets.

**Tech Stack:** Java 21, Minecraft 1.21.1, Architectury (Fabric + NeoForge), Jackson YAML, Mixin

**Design doc:** `docs/plans/2026-03-01-world-presets-design.md`

---

## Phase 1: Rename Preset → DimensionPreset

All Phase 1 tasks are committed together as one refactor commit (partial rename = broken build).

---

### Task 1: Rename Constants

**Files:**
- Modify: `common/common-util/src/main/java/com/pg85/otg/constants/Constants.java`

**Step 1: Update constants**

```java
// Old:
public static final String PRESETS_FOLDER = "Presets";
public static final String PRESET_CONFIG_FILE = "PresetConfig.ini";
public static final String DIMENSION_CONFIGS_FOLDER = "DimensionConfigs";

// New:
public static final String DIMENSION_PRESETS_FOLDER = "DimensionPresets";
public static final String DIMENSION_PRESET_CONFIG_FILE = "DimensionPresetConfig.ini";
public static final String WORLD_PRESETS_FOLDER = "WorldPresets";

// Keep as-is (legacy compat):
public static final String LEGACY_WORLD_CONFIG_FILE = "WorldConfig.ini";
public static final String DEFAULT_PRESET_NAME = "DefaultPreset";
```

Note: `DEFAULT_PRESET_NAME` stays because it's a folder name on disk that users already have. The folder itself is renamed from `Presets/DefaultPreset/` to `DimensionPresets/DefaultPreset/`.

**Step 2: Find and update ALL references to old constant names**

Search for: `PRESETS_FOLDER`, `PRESET_CONFIG_FILE`, `DIMENSION_CONFIGS_FOLDER`
Replace with: `DIMENSION_PRESETS_FOLDER`, `DIMENSION_PRESET_CONFIG_FILE`, `WORLD_PRESETS_FOLDER`

Files that reference these (exhaustive list):
- `common/common-core/src/main/java/com/pg85/otg/OTGEngine.java` — lines 94, 100, 193
- `common/common-core/src/main/java/com/pg85/otg/loader/PresetConfigLoader.java` — line using PRESET_CONFIG_FILE
- `common/common-core/src/main/java/com/pg85/otg/loader/DimensionConfigLoader.java` — line 21
- `common/common-core/src/main/java/com/pg85/otg/presets/LocalPresetLoader.java` — constructor

---

### Task 2: Rename Core Model Classes

**Files to rename (git mv):**

| Old Path | New Path |
|----------|----------|
| `common/common-core/.../presets/Preset.java` | `common/common-core/.../presets/DimensionPreset.java` |
| `common/common-core/.../config/preset/PresetConfig.java` | `common/common-core/.../config/preset/DimensionPresetConfig.java` |
| `common/common-core/.../config/preset/PresetWriter.java` | `common/common-core/.../config/preset/DimensionPresetWriter.java` |
| `common/common-core/.../config/preset/PresetResourcesManager.java` | `common/common-core/.../config/preset/DimensionPresetResourcesManager.java` |
| `common/common-util/.../config/settings/preset/PresetSettings.java` | `common/common-util/.../config/settings/preset/DimensionPresetSettings.java` |
| `common/common-util/.../config/settings/preset/PresetInfo.java` | `common/common-util/.../config/settings/preset/DimensionPresetInfo.java` |

**For each file:**
1. `git mv OldName.java NewName.java`
2. Update class declaration: `class Preset` → `class DimensionPreset`
3. Update constructor names
4. Update all internal self-references

**Class name mapping (for reference replacements):**

| Old Class | New Class |
|-----------|-----------|
| `Preset` | `DimensionPreset` |
| `PresetConfig` | `DimensionPresetConfig` |
| `PresetWriter` | `DimensionPresetWriter` |
| `PresetResourcesManager` | `DimensionPresetResourcesManager` |
| `PresetSettings` | `DimensionPresetSettings` |
| `PresetInfo` | `DimensionPresetInfo` |

---

### Task 3: Rename Loader & Platform Classes

**Files to rename (git mv):**

| Old Path | New Path |
|----------|----------|
| `common/common-core/.../loader/PresetConfigLoader.java` | `common/common-core/.../loader/DimensionPresetConfigLoader.java` |
| `common/common-core/.../presets/LocalPresetLoader.java` | `common/common-core/.../presets/LocalDimensionPresetLoader.java` |
| `platforms/shared/.../biome/SharedPresetBiomeLoader.java` | `platforms/shared/.../biome/SharedDimensionPresetBiomeLoader.java` |
| `platforms/shared/.../preset/DefaultPresetLoader.java` | `platforms/shared/.../preset/DefaultDimensionPresetLoader.java` |
| `common/common-test/.../test/preset/TestPresetLoader.java` | `common/common-test/.../test/preset/TestDimensionPresetLoader.java` |
| `common/common-core/.../config/dimensions/DimensionConfig.java` | `common/common-core/.../config/dimensions/WorldPresetConfig.java` |
| `common/common-core/.../loader/DimensionConfigLoader.java` | `common/common-core/.../loader/WorldPresetConfigLoader.java` |

**Class name mapping:**

| Old Class | New Class |
|-----------|-----------|
| `PresetConfigLoader` | `DimensionPresetConfigLoader` |
| `LocalPresetLoader` | `LocalDimensionPresetLoader` |
| `SharedPresetBiomeLoader` | `SharedDimensionPresetBiomeLoader` |
| `DefaultPresetLoader` | `DefaultDimensionPresetLoader` |
| `TestPresetLoader` | `TestDimensionPresetLoader` |
| `DimensionConfig` | `WorldPresetConfig` |
| `DimensionConfigLoader` | `WorldPresetConfigLoader` |

---

### Task 4: Update All References Across Codebase

This is the biggest task — update imports, type references, method calls, and variable declarations across all files.

**Strategy:** For each renamed class, do a global find-and-replace across all `.java` files (excluding `.worktrees/`). Order matters — replace longer names first to avoid partial matches.

**Replacement order (longest first to avoid collisions):**

```
PresetResourcesManager     → DimensionPresetResourcesManager
SharedPresetBiomeLoader    → SharedDimensionPresetBiomeLoader
DefaultPresetLoader        → DefaultDimensionPresetLoader
PresetConfigLoader         → DimensionPresetConfigLoader
LocalPresetLoader          → LocalDimensionPresetLoader
TestPresetLoader           → TestDimensionPresetLoader
DimensionConfigLoader      → WorldPresetConfigLoader
DimensionConfig            → WorldPresetConfig
PresetSettings             → DimensionPresetSettings
PresetConfig               → DimensionPresetConfig
PresetWriter               → DimensionPresetWriter
PresetInfo                 → DimensionPresetInfo
```

**IMPORTANT: Do NOT blindly replace `Preset` → `DimensionPreset`!** This would break:
- `WorldPreset` (MC class) → `WorldDimensionPreset` (WRONG)
- `WorldPresetConfig` (our new class) → `WorldDimensionPresetConfig` (WRONG)
- `PresetFolderName` (YAML field) — leave as-is for now
- String literals like `"preset"` in log messages

Instead, replace the SPECIFIC class names listed above, then handle remaining `Preset` references manually:
- `import ...presets.Preset;` → `import ...presets.DimensionPreset;`
- `Preset preset` → `DimensionPreset preset` (variable declarations)
- `HashMap<String, Preset>` → `HashMap<String, DimensionPreset>`
- `ArrayList<Preset>` → `ArrayList<DimensionPreset>`
- `getPresetConfig()` method on DimensionPreset → rename to `getConfig()` (avoid `getDimensionPresetConfig()` redundancy)
- `getPresetLoader()` on OTGEngine → rename to `getDimensionPresetLoader()`
- `getPresetByFolderName()` → `getDimensionPresetByFolderName()` (on loader)
- `getAllPresets()` → `getAllDimensionPresets()` (on loader)
- `loadPresetsFromDisk()` → `loadDimensionPresetsFromDisk()` (on loader)
- `getPresetRegistryName()` → `getRegistryName()` (on DimensionPreset, shorter)
- `getPresetFolder()` → `getFolder()` (on DimensionPreset, shorter)

**Files to update (grouped by module — exhaustive list from exploration):**

**common-util (~15 files):**
- `Constants.java`, `PresetSettings.java`→renamed, `PresetInfo.java`→renamed
- `BiomeGroupFunction.java`, `BiomeSettings.java`, `BiomePlacementConfig.java`
- `IdentitySettings.java`, `OutdatedSettings.java`, `GenerationSettings.java`
- `ImageSettings.java`, `SimpleSettingsMap.java`, `PluginConfigStandardValues.java`
- `SettingsSchemaGenerator.java`, `IPluginConfig.java`, `LocalWorldGenRegion.java`
- `FrozenSurfaceHelper.java`, `DimensionNameUtils.java`
- `MCBiomeResourceLocation.java`, `OTGBiomeResourceLocation.java`

**common-core (~19 files):**
- `OTGEngine.java`, `OTG.java`
- `Preset.java`→renamed, `LocalPresetLoader.java`→renamed
- `PresetConfig.java`→renamed, `PresetWriter.java`→renamed
- `PresetResourcesManager.java`→renamed, `PresetConfigLoader.java`→renamed
- `DimensionConfig.java`→renamed, `DimensionConfigLoader.java`→renamed
- `BiomeConfig.java`, `BiomeConfigWriter.java`, `BiomeResourcesManager.java`
- `BiomeTemplate.java`, `BiomeConfigLoader.java`, `BiomePlanResolver.java`
- `PluginConfig.java`, `PluginConfigBase.java`
- `DimensionDatapack.java`, `OTGChunkGenerator.java`, `OTGChunkDecorator.java`

**common-customobject (~16 files):**
- `CustomObjectCollection.java` (43 occurrences — HIGH IMPACT)
- `CustomObjectManager.java`, `CustomObjectResourcesManager.java`
- `CustomObjectResource.java`, `ICustomStructureResource.java`, `TreeResource.java`
- `BO3.java`, `BO3Config.java`, `BO4ConfigWriter.java`
- `BO3CustomStructure.java`, `BO4CustomStructure.java`
- `BranchDataItem.java`, `CustomStructurePlotter.java`
- `SmoothingAreaGenerator.java`, `CustomStructureCache.java`
- `FileSettingsReaderBO4.java`

**common-generator (~8 files):**
- `ApplyOceanLayer.java`, `BiomeLayerData.java`
- `Carver.java`, `CaveCarver.java`, `RavineCarver.java`
- `OreResource.java`, `UnderWaterOreResource.java`, `VeinResource.java`

**common-test (~5 files):**
- `TestPresetLoader.java`→renamed, `TestOTGEngine.java`, `TestLogger.java`
- `SnapshotCli.java` (29 occurrences), `TerrainSnapshotGenerator.java`

**platforms/shared (~20 files):**
- `OTGRegistryHelper.java`, `SharedOTGChunkGenerator.java`
- `SharedPresetBiomeLoader.java`→renamed, `DefaultPresetLoader.java`→renamed
- `SharedWorldGenRegion.java`, `SharedDimensionHelper.java`
- `SharedOTGBiomeProvider.java`, `BiomeFactory.java`, `BiomeRegistrar.java`
- `DimensionManager.java`, `DimensionCommands.java`
- `ExportCommand.java`, `ExportBO4DataCommand.java`, `SpawnCommand.java`, `StructureCommand.java`
- `CommandWorldAccessor.java`, `IOTGBiomeProvider.java`
- `SharedPortalConfigResolver.java`, `SharedPortalIgnitionHandler.java`
- `SharedOTGPortalBlock.java`, `PortalConfigLookup.java`
- `OTGLogger.java`, `OTGNoiseParamRegistry.java`
- `BiomeDataMixin.java`, `GameRuleApplier.java`

**platforms/fabric (~7 files):**
- `FabricEngine.java`, `OTGPlugin.java`
- `OTGFabricBiomeProvider.java`, `OTGFabricChunkGenerator.java`
- `FabricWorldGenRegion.java`, `FabricCommandWorldAccessor.java`
- `RegistryLoaderMixin.java`

**platforms/neoforge (~7 files):**
- `NeoForgeEngine.java`, `NeoForgeEventHandler.java`
- `OTGNeoForgeBiomeProvider.java`, `OTGNeoForgeChunkGenerator.java`
- `NeoForgeWorldGenRegion.java`, `NeoForgeCommandWorldAccessor.java`
- `RegistryLoaderMixin.java`

**Step: Build and verify**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

**Step: Commit**

```bash
git add -A
git commit -m "refactor: rename Preset → DimensionPreset, DimensionConfig → WorldPresetConfig"
```

---

### Task 5: Update Resources (Folders, Files, Example YAMLs)

**Files:**
- Rename: `resources/Presets/` → `resources/DimensionPresets/`
- Rename: `resources/DimensionPresets/DefaultPreset/PresetConfig.ini` → `resources/DimensionPresets/DefaultPreset/DimensionPresetConfig.ini`
- Rename: `resources/DimensionConfigs/` → `resources/WorldPresets/`
- Modify: All 4 example YAML files (update comments)

**Step 1: Rename folders and files**

```bash
git mv resources/Presets resources/DimensionPresets
git mv "resources/DimensionPresets/DefaultPreset/PresetConfig.ini" "resources/DimensionPresets/DefaultPreset/DimensionPresetConfig.ini"
git mv resources/DimensionConfigs resources/WorldPresets
```

**Step 2: Add startup detection for old paths**

In `OTGEngine.onStart()`, before creating folders, add checks:

```java
// Detect old folder names and warn users
Path oldPresetsDir = Paths.get(getOTGRootFolder().toString(), "Presets");
if (oldPresetsDir.toFile().exists() && !Paths.get(getOTGRootFolder().toString(), Constants.DIMENSION_PRESETS_FOLDER).toFile().exists()) {
    this.logger.error(LogCategory.MAIN, "==============================================");
    this.logger.error(LogCategory.MAIN, "BREAKING CHANGE: 'Presets/' has been renamed to 'DimensionPresets/'");
    this.logger.error(LogCategory.MAIN, "Please rename your Presets folder to DimensionPresets");
    this.logger.error(LogCategory.MAIN, "==============================================");
}

Path oldDimConfigsDir = Paths.get(getOTGRootFolder().toString(), "DimensionConfigs");
if (oldDimConfigsDir.toFile().exists() && !Paths.get(getOTGRootFolder().toString(), Constants.WORLD_PRESETS_FOLDER).toFile().exists()) {
    this.logger.error(LogCategory.MAIN, "==============================================");
    this.logger.error(LogCategory.MAIN, "BREAKING CHANGE: 'DimensionConfigs/' has been renamed to 'WorldPresets/'");
    this.logger.error(LogCategory.MAIN, "Please rename your DimensionConfigs folder to WorldPresets");
    this.logger.error(LogCategory.MAIN, "==============================================");
}
```

Also update the `UnpackDefaultPresetAndExamples` method paths:
- `"resources/" + Constants.PRESETS_FOLDER` → `"resources/" + Constants.DIMENSION_PRESETS_FOLDER`
- `"resources/" + Constants.DIMENSION_CONFIGS_FOLDER` → `"resources/" + Constants.WORLD_PRESETS_FOLDER`

**Step 3: Update example YAML comments**

Replace header in all 4 YAMLs:
```yaml
# WorldPreset: defines OTG dimensions for servers and modpacks.
# Use:
# Server: Place this file in WorldPresets/ to define your world configuration.
# Modpack: Name this file Modpack.yaml for auto-detection.
```

**Step 4: Build and verify**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

**Step 5: Commit**

```bash
git add -A
git commit -m "refactor: rename resource folders Presets/ → DimensionPresets/, DimensionConfigs/ → WorldPresets/"
```

---

## Phase 2: WorldPreset System

---

### Task 6: Extend WorldPresetConfig Model

**Files:**
- Modify: `common/common-core/.../config/dimensions/WorldPresetConfig.java` (renamed from DimensionConfig)

**Step 1: Add new fields and per-dimension GameRules**

Add to `WorldPresetConfig`:
```java
public String DisplayName;    // NEW: required for GUI registration
public String Description;    // NEW: optional GUI description
```

Add `GameRules` field to `OTGDimension`:
```java
public static class OTGDimension {
    public String PresetFolderName;
    public long Seed;
    public String PortalBlocks;
    public String PortalColor;
    public String PortalMob;
    public String PortalIgnitionSource;
    public GameRules GameRules;  // NEW: per-dimension override

    // ... existing constructors ...

    public OTGDimension clone() {
        OTGDimension otgDimension = new OTGDimension(this.PresetFolderName, this.Seed);
        otgDimension.PortalBlocks = this.PortalBlocks;
        otgDimension.PortalColor = this.PortalColor;
        otgDimension.PortalMob = this.PortalMob;
        otgDimension.PortalIgnitionSource = this.PortalIgnitionSource;
        otgDimension.GameRules = this.GameRules == null ? null : this.GameRules.clone();
        return otgDimension;
    }
}
```

Also add `GameRules` to `OTGOverWorld.clone()`.

Update top-level `clone()` to copy `DisplayName` and `Description`.

**Step 2: Build and verify**

Run: `./gradlew build`

**Step 3: Commit**

```bash
git add -A
git commit -m "feat: extend WorldPresetConfig with DisplayName, Description, per-dimension GameRules"
```

---

### Task 7: Implement WorldPresetConfigLoader

**Files:**
- Modify: `common/common-core/.../loader/WorldPresetConfigLoader.java` (renamed from DimensionConfigLoader)

**Step 1: Add loadAll() method**

Replace the old `fromDisk(String fileName, Path otgRootFolder)` method (which searched by preset name and never worked) with a new method that loads all YAMLs from the WorldPresets folder:

```java
package com.pg85.otg.loader;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.pg85.otg.config.dimensions.WorldPresetConfig;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.util.OTGLog;
import com.pg85.otg.util.logging.LogCategory;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class WorldPresetConfigLoader {

    /**
     * Loads all WorldPreset YAML files from the WorldPresets/ folder.
     * Only returns configs that have a DisplayName set (required for GUI registration).
     */
    public static List<WorldPresetConfig> loadAll(Path otgRootFolder) {
        List<WorldPresetConfig> configs = new ArrayList<>();
        File worldPresetsDir = otgRootFolder.resolve(Constants.WORLD_PRESETS_FOLDER).toFile();

        if (!worldPresetsDir.exists() || !worldPresetsDir.isDirectory()) {
            return configs;
        }

        File[] yamlFiles = worldPresetsDir.listFiles((dir, name) -> name.endsWith(".yaml") || name.endsWith(".yml"));
        if (yamlFiles == null) return configs;

        for (File yamlFile : yamlFiles) {
            WorldPresetConfig config = fromFile(yamlFile);
            if (config != null) {
                configs.add(config);
            }
        }

        return configs;
    }

    /**
     * Loads a single WorldPreset YAML file.
     */
    public static @Nullable WorldPresetConfig fromFile(File yamlFile) {
        try {
            String content = Files.readString(yamlFile.toPath());
            return fromYamlString(content);
        } catch (IOException e) {
            OTGLog.error(LogCategory.CONFIGS, "Failed to read WorldPreset file {}: {}",
                yamlFile.getName(), e.getMessage());
            return null;
        }
    }

    /**
     * Parses a WorldPreset YAML string.
     */
    public static @Nullable WorldPresetConfig fromYamlString(String input) {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            return mapper.readValue(input, WorldPresetConfig.class);
        } catch (IOException e) {
            OTGLog.error(LogCategory.CONFIGS, "Failed to parse WorldPreset YAML: {}", e.getMessage());
            return null;
        }
    }
}
```

**Step 2: Build and verify**

Run: `./gradlew build`

**Step 3: Commit**

```bash
git add -A
git commit -m "feat: rewrite WorldPresetConfigLoader to load all YAMLs from WorldPresets/ folder"
```

---

### Task 8: Implement WorldPresetRegistrar

**Files:**
- Create: `platforms/shared/src/main/java/com/pg85/otg/shared/registry/WorldPresetRegistrar.java`

**Step 1: Create the registrar class**

```java
package com.pg85.otg.shared.registry;

import com.pg85.otg.config.dimensions.WorldPresetConfig;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.presets.DimensionPreset;
import com.pg85.otg.util.OTGLog;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

import java.util.*;

/**
 * Registers WorldPreset YAML configs as MC WorldPresets in the registry.
 * Called from OTGRegistryHelper.loadOTGPresets() after individual DimensionPresets
 * are registered.
 */
public class WorldPresetRegistrar {

    /**
     * Registers WorldPreset YAML configs as MC WorldPresets.
     *
     * @param configs         loaded WorldPresetConfig objects from YAML files
     * @param loadedPresets   map of presetFolderName → DimensionPreset (already loaded)
     * @param worldPresets    the MC WorldPreset writable registry
     * @param factory         platform-specific chunk generator factory
     * @param dimensionTypes  holder getter for dimension types
     * @param noiseSettings   holder getter for noise generator settings
     * @param biomeRegistry   the biome registry
     */
    public static void register(
            List<WorldPresetConfig> configs,
            Map<String, DimensionPreset> loadedPresets,
            WritableRegistry<WorldPreset> worldPresets,
            OTGRegistryHelper.ChunkGeneratorFactory factory,
            HolderGetter<DimensionType> dimensionTypes,
            HolderGetter<NoiseGeneratorSettings> noiseSettings,
            Registry<Biome> biomeRegistry
    ) {
        Set<String> registeredNames = new HashSet<>();

        for (WorldPresetConfig config : configs) {
            if (config.DisplayName == null || config.DisplayName.isBlank()) {
                OTGLog.warn("WorldPreset YAML has no DisplayName, skipping registration");
                continue;
            }

            String normalizedId = normalizeId(config.DisplayName);
            if (registeredNames.contains(normalizedId)) {
                OTGLog.warn("Duplicate WorldPreset DisplayName '{}', skipping", config.DisplayName);
                continue;
            }

            Map<ResourceKey<LevelStem>, LevelStem> levelStems = buildLevelStems(
                config, loadedPresets, factory, dimensionTypes, noiseSettings, biomeRegistry);

            if (levelStems.isEmpty()) {
                OTGLog.error("WorldPreset '{}' produced no valid dimensions, skipping", config.DisplayName);
                continue;
            }

            WorldPreset preset = new WorldPreset(levelStems);
            ResourceKey<WorldPreset> key = ResourceKey.create(
                Registries.WORLD_PRESET,
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID_SHORT, normalizedId));

            worldPresets.register(key, preset,
                net.minecraft.core.RegistrationInfo.BUILT_IN);

            registeredNames.add(normalizedId);
            OTGLog.info("Registered WorldPreset '{}' as otg:{}", config.DisplayName, normalizedId);
        }
    }

    private static Map<ResourceKey<LevelStem>, LevelStem> buildLevelStems(
            WorldPresetConfig config,
            Map<String, DimensionPreset> loadedPresets,
            OTGRegistryHelper.ChunkGeneratorFactory factory,
            HolderGetter<DimensionType> dimensionTypes,
            HolderGetter<NoiseGeneratorSettings> noiseSettings,
            Registry<Biome> biomeRegistry
    ) {
        Map<ResourceKey<LevelStem>, LevelStem> stems = new LinkedHashMap<>();

        // Overworld
        if (config.Overworld != null) {
            if (config.Overworld.NonOTGWorldType != null && !config.Overworld.NonOTGWorldType.isBlank()) {
                // Vanilla/modded overworld — create vanilla LevelStem
                LevelStem vanillaOverworld = createVanillaLevelStem(
                    LevelStem.OVERWORLD, dimensionTypes, noiseSettings, biomeRegistry);
                if (vanillaOverworld != null) {
                    stems.put(LevelStem.OVERWORLD, vanillaOverworld);
                }
            } else if (config.Overworld.PresetFolderName != null) {
                LevelStem stem = createOTGLevelStem(
                    config.Overworld.PresetFolderName, LevelStem.OVERWORLD,
                    loadedPresets, factory, dimensionTypes, noiseSettings, biomeRegistry);
                if (stem != null) {
                    stems.put(LevelStem.OVERWORLD, stem);
                }
            }
        }

        // Nether
        if (config.Nether != null && config.Nether.PresetFolderName != null) {
            LevelStem stem = createOTGLevelStem(
                config.Nether.PresetFolderName, LevelStem.NETHER,
                loadedPresets, factory, dimensionTypes, noiseSettings, biomeRegistry);
            if (stem != null) {
                stems.put(LevelStem.NETHER, stem);
            }
        } else {
            // Vanilla nether
            LevelStem vanillaNether = createVanillaLevelStem(
                LevelStem.NETHER, dimensionTypes, noiseSettings, biomeRegistry);
            if (vanillaNether != null) {
                stems.put(LevelStem.NETHER, vanillaNether);
            }
        }

        // End
        if (config.End != null && config.End.PresetFolderName != null) {
            LevelStem stem = createOTGLevelStem(
                config.End.PresetFolderName, LevelStem.END,
                loadedPresets, factory, dimensionTypes, noiseSettings, biomeRegistry);
            if (stem != null) {
                stems.put(LevelStem.END, stem);
            }
        } else {
            // Vanilla end
            LevelStem vanillaEnd = createVanillaLevelStem(
                LevelStem.END, dimensionTypes, noiseSettings, biomeRegistry);
            if (vanillaEnd != null) {
                stems.put(LevelStem.END, vanillaEnd);
            }
        }

        // Custom dimensions
        if (config.Dimensions != null) {
            for (WorldPresetConfig.OTGDimension dim : config.Dimensions) {
                if (dim.PresetFolderName == null) continue;
                String normalizedName = dim.PresetFolderName.toLowerCase(Locale.ROOT)
                    .replaceAll("[^a-z0-9_.-]", "_");
                ResourceKey<LevelStem> key = ResourceKey.create(
                    Registries.LEVEL_STEM,
                    ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID_SHORT, normalizedName));
                LevelStem stem = createOTGLevelStem(
                    dim.PresetFolderName, key,
                    loadedPresets, factory, dimensionTypes, noiseSettings, biomeRegistry);
                if (stem != null) {
                    stems.put(key, stem);
                }
            }
        }

        return stems;
    }

    private static LevelStem createOTGLevelStem(
            String presetFolderName,
            ResourceKey<LevelStem> stemKey,
            Map<String, DimensionPreset> loadedPresets,
            OTGRegistryHelper.ChunkGeneratorFactory factory,
            HolderGetter<DimensionType> dimensionTypes,
            HolderGetter<NoiseGeneratorSettings> noiseSettings,
            Registry<Biome> biomeRegistry
    ) {
        DimensionPreset preset = loadedPresets.get(presetFolderName);
        if (preset == null) {
            OTGLog.error("WorldPreset references unknown DimensionPreset '{}', skipping dimension", presetFolderName);
            return null;
        }

        // Determine dimension type key based on stem key
        ResourceKey<DimensionType> dimTypeKey;
        if (stemKey.equals(LevelStem.OVERWORLD)) {
            dimTypeKey = preset.getConfig().getDimensionSettings().isOtgDimensionType()
                ? ResourceKey.create(Registries.DIMENSION_TYPE,
                    ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID_SHORT, preset.getRegistryName()))
                : net.minecraft.world.level.dimension.BuiltinDimensionTypes.OVERWORLD;
        } else if (stemKey.equals(LevelStem.NETHER)) {
            dimTypeKey = net.minecraft.world.level.dimension.BuiltinDimensionTypes.NETHER;
        } else if (stemKey.equals(LevelStem.END)) {
            dimTypeKey = net.minecraft.world.level.dimension.BuiltinDimensionTypes.END;
        } else {
            // Custom dimension — use OTG dimension type if available, else overworld type
            dimTypeKey = preset.getConfig().getDimensionSettings().isOtgDimensionType()
                ? ResourceKey.create(Registries.DIMENSION_TYPE,
                    ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID_SHORT, preset.getRegistryName()))
                : net.minecraft.world.level.dimension.BuiltinDimensionTypes.OVERWORLD;
        }

        Optional<Holder.Reference<DimensionType>> dimType = dimensionTypes.get(dimTypeKey);
        if (dimType.isEmpty()) {
            OTGLog.error("DimensionType {} not found for preset {}", dimTypeKey.location(), presetFolderName);
            return null;
        }

        // Get noise settings
        ResourceKey<NoiseGeneratorSettings> noiseKey = ResourceKey.create(
            Registries.NOISE_SETTINGS,
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID_SHORT,
                preset.getRegistryName().toLowerCase(Locale.ROOT)));
        Optional<Holder.Reference<NoiseGeneratorSettings>> noiseRef = noiseSettings.get(noiseKey);
        if (noiseRef.isEmpty()) {
            OTGLog.error("NoiseGeneratorSettings {} not found for preset {}", noiseKey.location(), presetFolderName);
            return null;
        }

        ChunkGenerator generator = factory.create(presetFolderName, noiseRef.get(), biomeRegistry);
        return new LevelStem(dimType.get(), generator);
    }

    /**
     * Creates a vanilla LevelStem for overworld/nether/end.
     * This reuses MC's built-in generators.
     */
    private static LevelStem createVanillaLevelStem(
            ResourceKey<LevelStem> stemKey,
            HolderGetter<DimensionType> dimensionTypes,
            HolderGetter<NoiseGeneratorSettings> noiseSettings,
            Registry<Biome> biomeRegistry
    ) {
        // Delegate to OTGRegistryHelper's existing vanilla dimension creation logic
        // This method mirrors what createLevelStems does for vanilla dimensions
        return OTGRegistryHelper.createVanillaLevelStem(stemKey, dimensionTypes, noiseSettings, biomeRegistry);
    }

    private static String normalizeId(String displayName) {
        return displayName.toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9_.-]", "_")
            .replaceAll("_+", "_")
            .replaceAll("^_|_$", "");
    }
}
```

**Note:** The `createVanillaLevelStem` method delegates to `OTGRegistryHelper`. You'll need to extract the vanilla dimension creation logic from `OTGRegistryHelper.createLevelStems()` into a static helper method.

**Step 2: Build and verify**

Run: `./gradlew build`

**Step 3: Commit**

```bash
git add -A
git commit -m "feat: add WorldPresetRegistrar for registering YAML configs as MC WorldPresets"
```

---

### Task 9: Hook WorldPresetRegistrar into OTGRegistryHelper

**Files:**
- Modify: `platforms/shared/src/main/java/com/pg85/otg/shared/registry/OTGRegistryHelper.java`

**Step 1: Extract vanilla LevelStem creation**

Extract the vanilla dimension creation from `createLevelStems()` into a new public static method:

```java
/**
 * Creates a vanilla LevelStem for standard MC dimensions.
 * Used by both individual preset registration and WorldPresetRegistrar.
 */
public static LevelStem createVanillaLevelStem(
        ResourceKey<LevelStem> stemKey,
        HolderGetter<DimensionType> dimensionTypes,
        HolderGetter<NoiseGeneratorSettings> noiseSettings,
        Registry<Biome> biomeRegistry
) {
    // Move existing vanilla dimension creation logic here
    // (the else branch in createLevelStems that handles minecraft:* dimensions)
}
```

**Step 2: Add WorldPreset YAML loading at end of loadOTGPresets()**

At the end of `loadOTGPresets()`, after the existing loop that registers individual presets:

```java
// Register WorldPreset YAMLs as MC WorldPresets
List<WorldPresetConfig> worldPresetConfigs = WorldPresetConfigLoader.loadAll(
    OTG.getEngine().getOTGRootFolder());

if (!worldPresetConfigs.isEmpty()) {
    // Build lookup map: presetFolderName → DimensionPreset
    Map<String, DimensionPreset> presetMap = new HashMap<>();
    for (DimensionPreset p : OTG.getEngine().getDimensionPresetLoader().getAllDimensionPresets()) {
        presetMap.put(p.getFolderName(), p);
    }

    WritableRegistry<WorldPreset> wpRegistry = getRegistryOrThrow(loaders, Registries.WORLD_PRESET);
    HolderGetter<DimensionType> dimTypes = getRegistryOrThrow(loaders, Registries.DIMENSION_TYPE).asLookup();
    HolderGetter<NoiseGeneratorSettings> noiseSettings = getRegistryOrThrow(loaders, Registries.NOISE_SETTINGS).asLookup();
    WritableRegistry<Biome> biomeReg = getRegistryOrThrow(loaders, Registries.BIOME);

    WorldPresetRegistrar.register(
        worldPresetConfigs, presetMap, wpRegistry, chunkGeneratorFactory,
        dimTypes, noiseSettings, biomeReg);
}
```

**Step 3: Build and verify**

Run: `./gradlew build`

**Step 4: Commit**

```bash
git add -A
git commit -m "feat: hook WorldPresetRegistrar into OTGRegistryHelper registration pipeline"
```

---

### Task 10: Extend GameRuleApplier for 3-Layer Merge

**Files:**
- Modify: `platforms/shared/src/main/java/com/pg85/otg/shared/gamerules/GameRuleApplier.java`

**Step 1: Add 3-layer createGameRules overload**

```java
/**
 * Creates GameRules with 3-layer override hierarchy:
 * 1. DimensionPresetConfig.ini GameRules (base)
 * 2. WorldPreset YAML world-level GameRules (override)
 * 3. WorldPreset YAML per-dimension GameRules (override)
 *
 * Each layer only overrides non-null fields.
 */
public static GameRules createGameRules(
        GameRuleSettings presetRules,
        @Nullable WorldPresetConfig.GameRules worldLevelOverrides,
        @Nullable WorldPresetConfig.GameRules dimensionOverrides,
        MinecraftServer server
) {
    GameRules rules = new GameRules();
    if (!presetRules.isOverrideGameRules()) return rules;

    // Layer 1: base from DimensionPresetConfig.ini
    applyFromPreset(rules, presetRules, server);

    // Layer 2: world-level overrides from WorldPreset YAML
    if (worldLevelOverrides != null) {
        applyFromWorldPresetConfig(rules, worldLevelOverrides, server);
    }

    // Layer 3: per-dimension overrides from WorldPreset YAML
    if (dimensionOverrides != null) {
        applyFromWorldPresetConfig(rules, dimensionOverrides, server);
    }

    return rules;
}

// Rename existing applyFromDimensionConfig → applyFromWorldPresetConfig
// (same logic, just renamed for clarity since the class was renamed)
```

**Step 2: Build and verify**

Run: `./gradlew build`

**Step 3: Commit**

```bash
git add -A
git commit -m "feat: extend GameRuleApplier with 3-layer merge for WorldPreset support"
```

---

### Task 11: Extend DimensionManager + OTGWorldStorage

**Files:**
- Modify: `platforms/shared/src/main/java/com/pg85/otg/shared/dimensions/DimensionManager.java`
- Modify: `common/common-core/src/main/java/com/pg85/otg/dimensions/OTGWorldStorage.java`

**Step 1: Add worldPreset field to OTGWorldStorage**

In `OTGWorldStorage.StorageData`:
```java
private static class StorageData {
    public Map<String, DimensionInfo> dimensions = new LinkedHashMap<>();
    public Map<String, Map<String, Object>> gameRules = new LinkedHashMap<>();
    public String worldPreset;  // NEW: name of WorldPreset YAML used to create this world
}
```

Add getter/setter methods:
```java
public @Nullable String getWorldPreset() { return data.worldPreset; }
public void setWorldPreset(String name) { data.worldPreset = name; dirty = true; }
```

**Step 2: Update DimensionManager.initialize() for WorldPreset GameRules**

Replace the existing `applyOverworldGameRulesIfOTG` approach with a more comprehensive WorldPreset-aware initialization:

```java
private void applyWorldPresetGameRules(MinecraftServer server) {
    String worldPresetName = storage.getWorldPreset();
    if (worldPresetName == null) return;

    // Load the WorldPreset YAML
    List<WorldPresetConfig> configs = WorldPresetConfigLoader.loadAll(
        OTG.getEngine().getOTGRootFolder());
    WorldPresetConfig config = configs.stream()
        .filter(c -> worldPresetName.equals(c.DisplayName))
        .findFirst().orElse(null);

    if (config == null) {
        OTGLog.warn("WorldPreset '{}' not found on disk, skipping GameRules", worldPresetName);
        return;
    }

    // Apply GameRules for each dimension defined in the WorldPreset
    applyWorldPresetDimensionGameRules(config, config.Overworld, Level.OVERWORLD, "minecraft:overworld", server);

    if (config.Nether != null && config.Nether.PresetFolderName != null) {
        applyWorldPresetDimensionGameRules(config, config.Nether, Level.NETHER, "minecraft:the_nether", server);
    }

    if (config.End != null && config.End.PresetFolderName != null) {
        applyWorldPresetDimensionGameRules(config, config.End, Level.END, "minecraft:the_end", server);
    }

    if (config.Dimensions != null) {
        for (WorldPresetConfig.OTGDimension dim : config.Dimensions) {
            if (dim.PresetFolderName == null) continue;
            String normalizedName = DimensionNameUtils.normalizeName(dim.PresetFolderName);
            ResourceKey<Level> levelKey = DimensionKeys.otg(normalizedName);
            applyWorldPresetDimensionGameRules(config, dim, levelKey, "otg:" + normalizedName, server);
        }
    }
}

private void applyWorldPresetDimensionGameRules(
        WorldPresetConfig config,
        WorldPresetConfig.OTGDimension dimEntry,
        ResourceKey<Level> levelKey,
        String storageKey,
        MinecraftServer server
) {
    // Skip if already persisted (not first start)
    if (!storage.getGameRules(storageKey).isEmpty()) return;

    if (dimEntry == null || dimEntry.PresetFolderName == null) return;

    DimensionPreset preset = OTG.getEngine().getDimensionPresetLoader()
        .getDimensionPresetByFolderName(dimEntry.PresetFolderName);
    if (preset == null) return;

    GameRuleSettings gameRuleSettings = preset.getConfig().getGameRuleSettings();
    GameRules rules = GameRuleApplier.createGameRules(
        gameRuleSettings,
        config.GameRules,         // world-level overrides
        dimEntry.GameRules,       // per-dimension overrides
        server);

    GameRuleManager.register(levelKey, rules);
    storage.putGameRules(storageKey, GameRuleApplier.toMap(rules));
    OTGLog.info("Applied WorldPreset GameRules for dimension {}", storageKey);
}
```

**Step 3: Update initialize() to call WorldPreset GameRules**

In `initialize()`, after restoring persisted GameRules:

```java
// Apply WorldPreset GameRules (first-time only, per-dimension)
applyWorldPresetGameRules(server);
```

Keep the existing `applyOverworldGameRulesIfOTG` as a fallback for worlds NOT created from a WorldPreset YAML (old flow).

**Step 4: Build and verify**

Run: `./gradlew build`

**Step 5: Commit**

```bash
git add -A
git commit -m "feat: WorldPreset-aware GameRules in DimensionManager + OTGWorldStorage"
```

---

### Task 12: Create Example WorldPreset YAML

**Files:**
- Modify: `resources/WorldPresets/Example 4.yaml` — convert to full WorldPreset format with rich comments

**Step 1: Rewrite Example 4.yaml as a proper WorldPreset**

```yaml
# ============================================================
# WorldPreset Configuration — Example
# ============================================================
#
# A WorldPreset defines a complete Minecraft world: which
# DimensionPresets to use for each dimension, plus optional
# per-dimension GameRules overrides.
#
# WorldPresets appear as selectable world types in the Minecraft
# world creation GUI alongside individual DimensionPresets.
#
# ---- How to use ----
#
# 1. Place this file in the WorldPresets/ folder inside your
#    OTG config directory (typically config/OpenTerrainGenerator/).
#
# 2. Set a unique DisplayName — this is what appears in the
#    world creation GUI.
#
# 3. Under Overworld/Nether/End, set PresetFolderName to the
#    name of the DimensionPreset folder you want to use.
#    Leave blank or omit for vanilla generation.
#
# 4. Use the Dimensions list to add custom OTG dimensions
#    with portal configuration.
#
# ---- GameRules ----
#
# GameRules use a 3-layer override hierarchy:
#
#   Layer 1: DimensionPresetConfig.ini GameRules
#            (base rules from the dimension's preset)
#
#   Layer 2: Top-level GameRules in this file
#            (world-level defaults, override layer 1)
#
#   Layer 3: Per-dimension GameRules
#            (dimension-specific overrides, override layer 2)
#
# Only rules explicitly listed are overridden — omitted rules
# keep their value from the previous layer.
#
# GameRules are applied once at world creation and persisted.
# Players can still change them via /gamerule in-game.
#
# ---- Available GameRules ----
#
# Boolean rules:
#   DoFireTick, MobGriefing, KeepInventory, DoMobSpawning,
#   DoMobLoot, DoTileDrops, DoEntityDrops, CommandBlockOutput,
#   NaturalRegeneration, DoDaylightCycle, LogAdminCommands,
#   ShowDeathMessages, SendCommandFeedback,
#   SpectatorsGenerateChunks, DisableElytraMovementCheck,
#   DoWeatherCycle, DoLimitedCrafting, AnnounceAdvancements,
#   DisableRaids, DoInsomnia, DrowningDamage, FallDamage,
#   FireDamage, DoPatrolSpawning, DoTraderSpawning,
#   ForgiveDeadPlayers, UniversalAnger,
#   ProjectilesCanBreakBlocks, ReducedDebugInfo,
#   DoImmediateRespawn, FreezeDamage, DoWardenSpawning,
#   BlockExplosionDropDecay, MobExplosionDropDecay,
#   TntExplosionDropDecay, WaterSourceConversion,
#   LavaSourceConversion, GlobalSoundEvents, DoVinesSpread,
#   EnderPearlsVanishOnDeath
#
# Integer rules:
#   RandomTickSpeed, SpawnRadius, MaxEntityCramming,
#   MaxCommandChainLength, MaxCommandForkCount,
#   CommandModificationBlockLimit,
#   PlayersNetherPortalDefaultDelay,
#   PlayersNetherPortalCreativeDelay,
#   PlayersSleepingPercentage, SnowAccumulationHeight,
#   SpawnChunkRadius
#
# ============================================================
---
Version: 1

# DisplayName: shown in the MC world creation GUI.
# REQUIRED — YAMLs without DisplayName are not registered in the GUI.
DisplayName: "Example World"

# Description: optional, shown in the world creation GUI tooltip.
Description: "An example WorldPreset with a flat overworld, OTG nether and end, plus two custom dimensions."

# ModpackName: optional, for modpack identification.
ModpackName: "My Awesome Modpack"

# ============================================================
# World-level GameRules
# ============================================================
# Applied to ALL dimensions in this world unless overridden
# per-dimension below.
GameRules:
  DoFireTick: true
  MobGriefing: true
  KeepInventory: false
  DoMobSpawning: true
  DoMobLoot: true
  DoTileDrops: true
  DoEntityDrops: true
  CommandBlockOutput: true
  NaturalRegeneration: true
  DoDaylightCycle: true
  LogAdminCommands: true
  ShowDeathMessages: true
  RandomTickSpeed: 3
  SendCommandFeedback: true
  SpectatorsGenerateChunks: true
  SpawnRadius: 10
  DisableElytraMovementCheck: false
  MaxEntityCramming: 24
  DoWeatherCycle: true
  DoLimitedCrafting: false
  MaxCommandChainLength: 65536
  AnnounceAdvancements: true
  DisableRaids: false
  DoInsomnia: true
  DrowningDamage: true
  FallDamage: true
  FireDamage: true
  DoPatrolSpawning: true
  DoTraderSpawning: true
  ForgiveDeadPlayers: true
  UniversalAnger: false
  ProjectilesCanBreakBlocks: true
  ReducedDebugInfo: false
  DoImmediateRespawn: false
  FreezeDamage: true
  DoWardenSpawning: true
  BlockExplosionDropDecay: true
  MobExplosionDropDecay: true
  TntExplosionDropDecay: false
  WaterSourceConversion: true
  LavaSourceConversion: false
  GlobalSoundEvents: true
  DoVinesSpread: true
  EnderPearlsVanishOnDeath: true
  MaxCommandForkCount: 65536
  CommandModificationBlockLimit: 32768
  PlayersNetherPortalDefaultDelay: 80
  PlayersNetherPortalCreativeDelay: 1
  PlayersSleepingPercentage: 100
  SnowAccumulationHeight: 1
  SpawnChunkRadius: 2

# ============================================================
# Overworld
# ============================================================
# PresetFolderName: which DimensionPreset to use for the overworld.
#
# For a vanilla overworld, use NonOTGWorldType instead:
#   NonOTGWorldType: "flat"
#   NonOTGGeneratorSettings: (optional JSON string)
Overworld:
  NonOTGWorldType: "flat"
  NonOTGGeneratorSettings:

# ============================================================
# Nether
# ============================================================
# Set PresetFolderName to an OTG DimensionPreset name.
# Omit the entire section or set PresetFolderName to null
# for vanilla nether generation.
Nether:
  PresetFolderName: "AlienJungle"

# ============================================================
# End
# ============================================================
# Same as Nether — set PresetFolderName or omit for vanilla.
End:
  PresetFolderName: "Skylands"

# ============================================================
# Custom Dimensions
# ============================================================
# Each entry creates an additional OTG dimension.
#
# Required:
#   PresetFolderName — which DimensionPreset to use
#
# Optional:
#   Seed — world seed for this dimension (random if omitted)
#   PortalColor — portal frame color
#   PortalMob — mob that spawns from portal
#   PortalIgnitionSource — item to ignite portal
#   PortalBlocks — block type for portal frame
#   GameRules — per-dimension GameRules override
Dimensions:
- PresetFolderName: "Wildlands"
  Seed: 14
  PortalColor: "beige"
  PortalMob: "minecraft:zombified_piglin"
  PortalIgnitionSource: "minecraft:flint_and_steel"
  PortalBlocks: "minecraft:redstone_block"
  # Per-dimension GameRules: only these rules are overridden
  GameRules:
    DoWeatherCycle: false
    DoFireTick: false

- PresetFolderName: "VanillaVistas"
  Seed: 14
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

**Step 2: Update other example YAMLs (1, 2, 3) with new header comment**

Replace old Forge-only header with new WorldPreset header (shorter version, reference Example 4 for full docs).

**Step 3: Build and verify**

Run: `./gradlew build`

**Step 4: Commit**

```bash
git add -A
git commit -m "feat: rewrite example YAMLs as WorldPreset format with comprehensive comments"
```

---

### Task 13: Update OTGEngine (Folder Creation + Unpacking)

**Files:**
- Modify: `common/common-core/src/main/java/com/pg85/otg/OTGEngine.java`

**Step 1: Update folder creation in onStart()**

```java
// Replace old folder creation code:
File presetsDir = Paths.get(getOTGRootFolder().toString(), Constants.DIMENSION_PRESETS_FOLDER).toFile();
if (!presetsDir.exists()) {
    presetsDir.mkdirs();
}

File worldPresetsDir = Paths.get(getOTGRootFolder().toString(), Constants.WORLD_PRESETS_FOLDER).toFile();
if (!worldPresetsDir.exists()) {
    worldPresetsDir.mkdirs();
}
```

**Step 2: Update UnpackDefaultPresetAndExamples()**

Update the path strings:
```java
String defaultPresetPath = "resources/" + Constants.DIMENSION_PRESETS_FOLDER + "/" + Constants.DEFAULT_PRESET_NAME + "/";
String worldPresetsPath = "resources/" + Constants.WORLD_PRESETS_FOLDER + "/";
```

And the `startsWith` check:
```java
if (entry.getName().startsWith(worldPresetsPath) ||
    entry.getName().startsWith(defaultPresetPath))
```

**Step 3: Build and verify**

Run: `./gradlew build`

**Step 4: Commit**

```bash
git add -A
git commit -m "refactor: update OTGEngine for renamed folders DimensionPresets/ + WorldPresets/"
```

---

### Task 14: Final Build + Integration Verification

**Step 1: Clean build**

```bash
./gradlew clean build
```

Expected: BUILD SUCCESSFUL

**Step 2: Verify JAR contents**

Check that the built JAR contains the correct resource paths:

```bash
jar tf build/distributions/*.jar | grep -E "(DimensionPresets|WorldPresets)" | head -20
```

Expected: paths under `resources/DimensionPresets/` and `resources/WorldPresets/`

**Step 3: Verify no stale references**

```bash
grep -rn "PRESETS_FOLDER\|PRESET_CONFIG_FILE\|\"PresetConfig.ini\"\|\"Presets/\"" --include="*.java" | grep -v ".worktrees/" | grep -v "DIMENSION_PRESETS_FOLDER\|DIMENSION_PRESET_CONFIG_FILE\|WORLD_PRESETS_FOLDER\|LEGACY"
```

Expected: no output (all old references replaced)

```bash
grep -rn "class Preset \|class PresetConfig \|class PresetWriter \|class PresetSettings \|class PresetInfo " --include="*.java" | grep -v ".worktrees/"
```

Expected: no output (all old class names replaced)

**Step 4: Code review**

Use `superpowers:requesting-code-review` skill to review the branch.
