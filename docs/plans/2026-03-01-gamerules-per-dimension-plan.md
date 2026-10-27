# GameRules Per-Dimension — Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Apply GameRules from PresetConfig.ini (+ optional DimensionConfig.yaml override) per OTG dimension via Level.getGameRules() mixin.

**Architecture:** Mixin on Level.getGameRules() dispatches to GameRuleManager (static ConcurrentHashMap). GameRuleApplier merges PresetConfig + DimensionConfig into MC GameRules. OTGWorldStorage persists per-dimension rules to otg_world_data.json.

**Tech Stack:** Mixin (shared module), Jackson (YAML + JSON), MC GameRules API, Lombok

**Design doc:** `docs/plans/2026-03-01-gamerules-per-dimension-design.md`

**No automated tests** — testing is done manually in-game per CLAUDE.md.

---

### Task 1: Fix existing bugs in GameRuleSettings and PresetWriter

Two copy-paste bugs discovered during exploration.

**Files:**
- Fix: `common/common-util/src/main/java/com/pg85/otg/config/settings/preset/GameRuleSettings.java:64`
- Fix: `common/common-core/src/main/java/com/pg85/otg/config/preset/PresetWriter.java:772`

**Step 1: Fix DO_MOB_SPAWNING getter bug**

In `GameRuleSettings.java` line 64, the getter calls `isMobGriefing()` instead of `isDoMobSpawning()`:

```java
// BEFORE (line 63-65):
public static final Setting<Boolean> DO_MOB_SPAWNING = Settings.booleanSetting(
    "DoMobSpawning", true,
    t -> ((GameRuleSettings) t).isMobGriefing()
);

// AFTER:
public static final Setting<Boolean> DO_MOB_SPAWNING = Settings.booleanSetting(
    "DoMobSpawning", true,
    t -> ((GameRuleSettings) t).isDoMobSpawning()
);
```

**Step 2: Fix DO_DAY_LIGHT_CYCLE writer bug**

In `PresetWriter.java` line 772, it writes `isNaturalRegeneration()` instead of `isDoDaylightCycle()`:

```java
// BEFORE (line 772):
writer.putSetting(GameRuleSettings.DO_DAY_LIGHT_CYCLE, gameRuleSettings.isNaturalRegeneration());

// AFTER:
writer.putSetting(GameRuleSettings.DO_DAY_LIGHT_CYCLE, gameRuleSettings.isDoDaylightCycle());
```

**Step 3: Build to verify**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

**Step 4: Commit**

```
fix: correct copy-paste bugs in GameRuleSettings and PresetWriter

DO_MOB_SPAWNING getter was reading MobGriefing instead of DoMobSpawning.
DO_DAY_LIGHT_CYCLE writer was writing NaturalRegeneration instead of DoDaylightCycle.
```

---

### Task 2: Add 20 new 1.21.1 GameRules to GameRuleSettings

**Files:**
- Modify: `common/common-util/src/main/java/com/pg85/otg/config/settings/preset/GameRuleSettings.java`

**Step 1: Add 20 new fields to the class**

Add after the existing `universalAnger` field (line 44), before the Setting constants:

```java
// New 1.21.1 rules
private final boolean projectilesCanBreakBlocks;
private final boolean reducedDebugInfo;
private final boolean doImmediateRespawn;
private final boolean freezeDamage;
private final boolean doWardenSpawning;
private final boolean blockExplosionDropDecay;
private final boolean mobExplosionDropDecay;
private final boolean tntExplosionDropDecay;
private final boolean waterSourceConversion;
private final boolean lavaSourceConversion;
private final boolean globalSoundEvents;
private final boolean doVinesSpread;
private final boolean enderPearlsVanishOnDeath;
private final int maxCommandForkCount;
private final int commandModificationBlockLimit;
private final int playersNetherPortalDefaultDelay;
private final int playersNetherPortalCreativeDelay;
private final int playersSleepingPercentage;
private final int snowAccumulationHeight;
private final int spawnChunkRadius;
```

**Step 2: Add 20 new Setting constants**

Add after the existing `MAX_COMMAND_CHAIN_LENGTH` constant (after line 173):

```java
// New 1.21.1 rules
public static final Setting<Boolean> PROJECTILES_CAN_BREAK_BLOCKS = Settings.booleanSetting(
    "ProjectilesCanBreakBlocks", true,
    t -> ((GameRuleSettings) t).isProjectilesCanBreakBlocks()
);
public static final Setting<Boolean> REDUCED_DEBUG_INFO = Settings.booleanSetting(
    "ReducedDebugInfo", false,
    t -> ((GameRuleSettings) t).isReducedDebugInfo()
);
public static final Setting<Boolean> DO_IMMEDIATE_RESPAWN = Settings.booleanSetting(
    "DoImmediateRespawn", false,
    t -> ((GameRuleSettings) t).isDoImmediateRespawn()
);
public static final Setting<Boolean> FREEZE_DAMAGE = Settings.booleanSetting(
    "FreezeDamage", true,
    t -> ((GameRuleSettings) t).isFreezeDamage()
);
public static final Setting<Boolean> DO_WARDEN_SPAWNING = Settings.booleanSetting(
    "DoWardenSpawning", true,
    t -> ((GameRuleSettings) t).isDoWardenSpawning()
);
public static final Setting<Boolean> BLOCK_EXPLOSION_DROP_DECAY = Settings.booleanSetting(
    "BlockExplosionDropDecay", true,
    t -> ((GameRuleSettings) t).isBlockExplosionDropDecay()
);
public static final Setting<Boolean> MOB_EXPLOSION_DROP_DECAY = Settings.booleanSetting(
    "MobExplosionDropDecay", true,
    t -> ((GameRuleSettings) t).isMobExplosionDropDecay()
);
public static final Setting<Boolean> TNT_EXPLOSION_DROP_DECAY = Settings.booleanSetting(
    "TntExplosionDropDecay", false,
    t -> ((GameRuleSettings) t).isTntExplosionDropDecay()
);
public static final Setting<Boolean> WATER_SOURCE_CONVERSION = Settings.booleanSetting(
    "WaterSourceConversion", true,
    t -> ((GameRuleSettings) t).isWaterSourceConversion()
);
public static final Setting<Boolean> LAVA_SOURCE_CONVERSION = Settings.booleanSetting(
    "LavaSourceConversion", false,
    t -> ((GameRuleSettings) t).isLavaSourceConversion()
);
public static final Setting<Boolean> GLOBAL_SOUND_EVENTS = Settings.booleanSetting(
    "GlobalSoundEvents", true,
    t -> ((GameRuleSettings) t).isGlobalSoundEvents()
);
public static final Setting<Boolean> DO_VINES_SPREAD = Settings.booleanSetting(
    "DoVinesSpread", true,
    t -> ((GameRuleSettings) t).isDoVinesSpread()
);
public static final Setting<Boolean> ENDER_PEARLS_VANISH_ON_DEATH = Settings.booleanSetting(
    "EnderPearlsVanishOnDeath", true,
    t -> ((GameRuleSettings) t).isEnderPearlsVanishOnDeath()
);
public static final Setting<Integer> MAX_COMMAND_FORK_COUNT = Settings.intSetting(
    "MaxCommandForkCount", 65536, 0, Integer.MAX_VALUE,
    t -> ((GameRuleSettings) t).getMaxCommandForkCount()
);
public static final Setting<Integer> COMMAND_MODIFICATION_BLOCK_LIMIT = Settings.intSetting(
    "CommandModificationBlockLimit", 32768, 0, Integer.MAX_VALUE,
    t -> ((GameRuleSettings) t).getCommandModificationBlockLimit()
);
public static final Setting<Integer> PLAYERS_NETHER_PORTAL_DEFAULT_DELAY = Settings.intSetting(
    "PlayersNetherPortalDefaultDelay", 80, 0, Integer.MAX_VALUE,
    t -> ((GameRuleSettings) t).getPlayersNetherPortalDefaultDelay()
);
public static final Setting<Integer> PLAYERS_NETHER_PORTAL_CREATIVE_DELAY = Settings.intSetting(
    "PlayersNetherPortalCreativeDelay", 1, 0, Integer.MAX_VALUE,
    t -> ((GameRuleSettings) t).getPlayersNetherPortalCreativeDelay()
);
public static final Setting<Integer> PLAYERS_SLEEPING_PERCENTAGE = Settings.intSetting(
    "PlayersSleepingPercentage", 100, 0, 100,
    t -> ((GameRuleSettings) t).getPlayersSleepingPercentage()
);
public static final Setting<Integer> SNOW_ACCUMULATION_HEIGHT = Settings.intSetting(
    "SnowAccumulationHeight", 1, 0, Integer.MAX_VALUE,
    t -> ((GameRuleSettings) t).getSnowAccumulationHeight()
);
public static final Setting<Integer> SPAWN_CHUNK_RADIUS = Settings.intSetting(
    "SpawnChunkRadius", 2, 0, 32,
    t -> ((GameRuleSettings) t).getSpawnChunkRadius()
);
```

**Step 3: Add 20 new builder calls in getGameRuleSettings()**

Add after the existing `universalAnger` builder call (after line 209):

```java
gameRuleSettingsBuilder.projectilesCanBreakBlocks(reader.getSetting(PROJECTILES_CAN_BREAK_BLOCKS));
gameRuleSettingsBuilder.reducedDebugInfo(reader.getSetting(REDUCED_DEBUG_INFO));
gameRuleSettingsBuilder.doImmediateRespawn(reader.getSetting(DO_IMMEDIATE_RESPAWN));
gameRuleSettingsBuilder.freezeDamage(reader.getSetting(FREEZE_DAMAGE));
gameRuleSettingsBuilder.doWardenSpawning(reader.getSetting(DO_WARDEN_SPAWNING));
gameRuleSettingsBuilder.blockExplosionDropDecay(reader.getSetting(BLOCK_EXPLOSION_DROP_DECAY));
gameRuleSettingsBuilder.mobExplosionDropDecay(reader.getSetting(MOB_EXPLOSION_DROP_DECAY));
gameRuleSettingsBuilder.tntExplosionDropDecay(reader.getSetting(TNT_EXPLOSION_DROP_DECAY));
gameRuleSettingsBuilder.waterSourceConversion(reader.getSetting(WATER_SOURCE_CONVERSION));
gameRuleSettingsBuilder.lavaSourceConversion(reader.getSetting(LAVA_SOURCE_CONVERSION));
gameRuleSettingsBuilder.globalSoundEvents(reader.getSetting(GLOBAL_SOUND_EVENTS));
gameRuleSettingsBuilder.doVinesSpread(reader.getSetting(DO_VINES_SPREAD));
gameRuleSettingsBuilder.enderPearlsVanishOnDeath(reader.getSetting(ENDER_PEARLS_VANISH_ON_DEATH));
gameRuleSettingsBuilder.maxCommandForkCount(reader.getSetting(MAX_COMMAND_FORK_COUNT));
gameRuleSettingsBuilder.commandModificationBlockLimit(reader.getSetting(COMMAND_MODIFICATION_BLOCK_LIMIT));
gameRuleSettingsBuilder.playersNetherPortalDefaultDelay(reader.getSetting(PLAYERS_NETHER_PORTAL_DEFAULT_DELAY));
gameRuleSettingsBuilder.playersNetherPortalCreativeDelay(reader.getSetting(PLAYERS_NETHER_PORTAL_CREATIVE_DELAY));
gameRuleSettingsBuilder.playersSleepingPercentage(reader.getSetting(PLAYERS_SLEEPING_PERCENTAGE));
gameRuleSettingsBuilder.snowAccumulationHeight(reader.getSetting(SNOW_ACCUMULATION_HEIGHT));
gameRuleSettingsBuilder.spawnChunkRadius(reader.getSetting(SPAWN_CHUNK_RADIUS));
```

**Step 4: Build to verify**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

**Step 5: Commit**

```
feat: add 20 new 1.21.1 GameRules to GameRuleSettings

Adds: projectilesCanBreakBlocks, reducedDebugInfo, doImmediateRespawn,
freezeDamage, doWardenSpawning, blockExplosionDropDecay, mobExplosionDropDecay,
tntExplosionDropDecay, waterSourceConversion, lavaSourceConversion,
globalSoundEvents, doVinesSpread, enderPearlsVanishOnDeath,
maxCommandForkCount, commandModificationBlockLimit,
playersNetherPortalDefaultDelay, playersNetherPortalCreativeDelay,
playersSleepingPercentage, snowAccumulationHeight, spawnChunkRadius
```

---

### Task 3: Add 20 new rules to PresetWriter + DimensionConfig.GameRules

**Files:**
- Modify: `common/common-core/src/main/java/com/pg85/otg/config/preset/PresetWriter.java:793`
- Modify: `common/common-core/src/main/java/com/pg85/otg/config/dimensions/DimensionConfig.java:146-218`

**Step 1: Update PresetWriter header comment**

Change PresetWriter line 753-756 to remove outdated "Forge" reference:

```java
writer.header1("Game rules",
    "See: https://minecraft.fandom.com/wiki/Game_rule",
    "These game rules apply per-dimension when OverrideGameRules is true.",
    "Can be overridden via a DimensionConfig YAML with a GameRules entry."
);
```

**Step 2: Add 20 new putSetting calls after line 793**

After the existing `UNIVERSAL_ANGER` line:

```java
writer.putSetting(GameRuleSettings.PROJECTILES_CAN_BREAK_BLOCKS, gameRuleSettings.isProjectilesCanBreakBlocks());
writer.putSetting(GameRuleSettings.REDUCED_DEBUG_INFO, gameRuleSettings.isReducedDebugInfo());
writer.putSetting(GameRuleSettings.DO_IMMEDIATE_RESPAWN, gameRuleSettings.isDoImmediateRespawn());
writer.putSetting(GameRuleSettings.FREEZE_DAMAGE, gameRuleSettings.isFreezeDamage());
writer.putSetting(GameRuleSettings.DO_WARDEN_SPAWNING, gameRuleSettings.isDoWardenSpawning());
writer.putSetting(GameRuleSettings.BLOCK_EXPLOSION_DROP_DECAY, gameRuleSettings.isBlockExplosionDropDecay());
writer.putSetting(GameRuleSettings.MOB_EXPLOSION_DROP_DECAY, gameRuleSettings.isMobExplosionDropDecay());
writer.putSetting(GameRuleSettings.TNT_EXPLOSION_DROP_DECAY, gameRuleSettings.isTntExplosionDropDecay());
writer.putSetting(GameRuleSettings.WATER_SOURCE_CONVERSION, gameRuleSettings.isWaterSourceConversion());
writer.putSetting(GameRuleSettings.LAVA_SOURCE_CONVERSION, gameRuleSettings.isLavaSourceConversion());
writer.putSetting(GameRuleSettings.GLOBAL_SOUND_EVENTS, gameRuleSettings.isGlobalSoundEvents());
writer.putSetting(GameRuleSettings.DO_VINES_SPREAD, gameRuleSettings.isDoVinesSpread());
writer.putSetting(GameRuleSettings.ENDER_PEARLS_VANISH_ON_DEATH, gameRuleSettings.isEnderPearlsVanishOnDeath());
writer.putSetting(GameRuleSettings.MAX_COMMAND_FORK_COUNT, gameRuleSettings.getMaxCommandForkCount());
writer.putSetting(GameRuleSettings.COMMAND_MODIFICATION_BLOCK_LIMIT, gameRuleSettings.getCommandModificationBlockLimit());
writer.putSetting(GameRuleSettings.PLAYERS_NETHER_PORTAL_DEFAULT_DELAY, gameRuleSettings.getPlayersNetherPortalDefaultDelay());
writer.putSetting(GameRuleSettings.PLAYERS_NETHER_PORTAL_CREATIVE_DELAY, gameRuleSettings.getPlayersNetherPortalCreativeDelay());
writer.putSetting(GameRuleSettings.PLAYERS_SLEEPING_PERCENTAGE, gameRuleSettings.getPlayersSleepingPercentage());
writer.putSetting(GameRuleSettings.SNOW_ACCUMULATION_HEIGHT, gameRuleSettings.getSnowAccumulationHeight());
writer.putSetting(GameRuleSettings.SPAWN_CHUNK_RADIUS, gameRuleSettings.getSpawnChunkRadius());
```

**Step 3: Add 20 new fields to DimensionConfig.GameRules**

In `DimensionConfig.java`, add after `UniversalAnger` (line 178) in the `GameRules` static class:

```java
public boolean ProjectilesCanBreakBlocks;
public boolean ReducedDebugInfo;
public boolean DoImmediateRespawn;
public boolean FreezeDamage;
public boolean DoWardenSpawning;
public boolean BlockExplosionDropDecay;
public boolean MobExplosionDropDecay;
public boolean TntExplosionDropDecay;
public boolean WaterSourceConversion;
public boolean LavaSourceConversion;
public boolean GlobalSoundEvents;
public boolean DoVinesSpread;
public boolean EnderPearlsVanishOnDeath;
public int MaxCommandForkCount;
public int CommandModificationBlockLimit;
public int PlayersNetherPortalDefaultDelay;
public int PlayersNetherPortalCreativeDelay;
public int PlayersSleepingPercentage;
public int SnowAccumulationHeight;
public int SpawnChunkRadius;
```

Also add corresponding lines to the `clone()` method.

**Step 4: Update example YAML**

In `resources/DimensionConfigs/Example 4.yaml`, add the new rules after `UniversalAnger: false`:

```yaml
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
```

**Step 5: Build**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

**Step 6: Commit**

```
feat: add 20 new 1.21.1 rules to PresetWriter and DimensionConfig

Updates PresetWriter to write all new settings, DimensionConfig.GameRules
inner class to deserialize them from YAML, and Example 4 YAML template.
```

---

### Task 4: Rename DimensionStorage → OTGWorldStorage with v1→v2 migration

**Files:**
- Rename: `common/common-core/src/main/java/com/pg85/otg/dimensions/DimensionStorage.java` → `OTGWorldStorage.java`
- Modify: `common/common-core/src/main/java/com/pg85/otg/dimensions/DimensionInfo.java` (no changes needed to class, just verify)
- Modify: `platforms/shared/src/main/java/com/pg85/otg/shared/dimensions/DimensionManager.java` (update references)

**Step 1: Rename and rewrite DimensionStorage → OTGWorldStorage**

Create `common/common-core/src/main/java/com/pg85/otg/dimensions/OTGWorldStorage.java`:

```java
package com.pg85.otg.dimensions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.pg85.otg.util.OTGLog;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class OTGWorldStorage {
    private static final String STORAGE_FILE = "otg_world_data.json";
    private static final String LEGACY_FILE = "otg_dimensions.json";
    private static final ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    @Data
    @NoArgsConstructor
    public static class StorageData {
        @JsonProperty("version")
        private int version = 2;

        @JsonProperty("dimensions")
        private Map<String, DimensionInfo> dimensions = new LinkedHashMap<>();

        @JsonProperty("gameRules")
        private Map<String, Map<String, Object>> gameRules = new LinkedHashMap<>();
    }

    private final Path worldPath;
    private StorageData data;

    public OTGWorldStorage(Path worldPath) {
        this.worldPath = worldPath;
        this.data = new StorageData();
    }

    public void load() {
        Path storagePath = worldPath.resolve(STORAGE_FILE);
        Path legacyPath = worldPath.resolve(LEGACY_FILE);

        if (Files.exists(storagePath)) {
            loadV2(storagePath);
        } else if (Files.exists(legacyPath)) {
            migrateFromV1(legacyPath);
        } else {
            data = new StorageData();
        }
    }

    private void loadV2(Path storagePath) {
        try {
            String json = Files.readString(storagePath);
            data = mapper.readValue(json, StorageData.class);
            OTGLog.info("Loaded {} OTG dimensions from storage", data.getDimensions().size());
        } catch (IOException e) {
            OTGLog.error("Failed to load world storage, starting fresh: {}", e.getMessage());
            try {
                Files.move(storagePath, storagePath.resolveSibling(STORAGE_FILE + ".backup"));
            } catch (IOException ignored) {
                OTGLog.error("Failed to backup corrupt storage file: {}", ignored.getMessage());
            }
            data = new StorageData();
        }
    }

    private void migrateFromV1(Path legacyPath) {
        OTGLog.info("Migrating from v1 (otg_dimensions.json) to v2 (otg_world_data.json)");
        try {
            String json = Files.readString(legacyPath);
            var legacyData = mapper.readTree(json);
            data = new StorageData();

            var dimsNode = legacyData.get("dimensions");
            if (dimsNode != null && dimsNode.isArray()) {
                for (var dimNode : dimsNode) {
                    DimensionInfo info = mapper.treeToValue(dimNode, DimensionInfo.class);
                    data.getDimensions().put(info.getName(), info);
                }
            }

            save();
            // Keep legacy file as backup
            Files.move(legacyPath, legacyPath.resolveSibling(LEGACY_FILE + ".v1backup"));
            OTGLog.info("Migration complete: {} dimensions migrated", data.getDimensions().size());
        } catch (IOException e) {
            OTGLog.error("Failed to migrate v1 storage: {}", e.getMessage());
            data = new StorageData();
        }
    }

    public void save() {
        Path storagePath = worldPath.resolve(STORAGE_FILE);
        try {
            String json = mapper.writeValueAsString(data);
            Files.writeString(storagePath, json);
        } catch (IOException e) {
            OTGLog.error("Failed to save world storage: {}", e.getMessage());
        }
    }

    // --- Dimension operations ---

    public void addDimension(DimensionInfo info) {
        data.getDimensions().put(info.getName(), info);
        save();
    }

    public boolean removeDimension(String name) {
        boolean removed = data.getDimensions().remove(name) != null;
        if (removed) {
            save();
        }
        return removed;
    }

    public Optional<DimensionInfo> getDimension(String name) {
        return Optional.ofNullable(data.getDimensions().get(name));
    }

    public List<DimensionInfo> getAllDimensions() {
        return new ArrayList<>(data.getDimensions().values());
    }

    public boolean exists(String name) {
        return data.getDimensions().containsKey(name);
    }

    // --- GameRules operations ---

    public void putGameRules(String dimensionKey, Map<String, Object> rules) {
        data.getGameRules().put(dimensionKey, new LinkedHashMap<>(rules));
        save();
    }

    public Optional<Map<String, Object>> getGameRules(String dimensionKey) {
        return Optional.ofNullable(data.getGameRules().get(dimensionKey));
    }

    public Map<String, Map<String, Object>> getAllGameRules() {
        return Collections.unmodifiableMap(data.getGameRules());
    }

    public void removeGameRules(String dimensionKey) {
        if (data.getGameRules().remove(dimensionKey) != null) {
            save();
        }
    }
}
```

**Step 2: Delete old DimensionStorage.java**

Delete `common/common-core/src/main/java/com/pg85/otg/dimensions/DimensionStorage.java`

**Step 3: Update DimensionManager references**

In `platforms/shared/src/main/java/com/pg85/otg/shared/dimensions/DimensionManager.java`:
- Change import from `DimensionStorage` to `OTGWorldStorage`
- Change field type from `DimensionStorage` to `OTGWorldStorage`
- Change constructor call from `new DimensionStorage(...)` to `new OTGWorldStorage(...)`

```java
// Line 6 import:
import com.pg85.otg.dimensions.OTGWorldStorage;

// Line 20 field:
private OTGWorldStorage storage;

// Line 30 constructor:
this.storage = new OTGWorldStorage(helper.getWorldPath(server));
```

**Step 4: Build**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

**Step 5: Commit**

```
refactor: rename DimensionStorage → OTGWorldStorage with v2 format

Converts dimensions array to map, adds gameRules section.
Includes automatic v1 migration from otg_dimensions.json.
```

---

### Task 5: Create GameRuleManager

**Files:**
- Create: `platforms/shared/src/main/java/com/pg85/otg/shared/gamerules/GameRuleManager.java`

**Step 1: Create GameRuleManager**

```java
package com.pg85.otg.shared.gamerules;

import com.pg85.otg.util.OTGLog;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds per-dimension GameRules. The mixin on Level.getGameRules()
 * dispatches here for OTG dimensions.
 */
public final class GameRuleManager {
    private static final Map<ResourceKey<Level>, GameRules> dimensionRules = new ConcurrentHashMap<>();

    private GameRuleManager() {}

    public static @Nullable GameRules getGameRules(ResourceKey<Level> dimension) {
        return dimensionRules.get(dimension);
    }

    public static void register(ResourceKey<Level> dimension, GameRules rules) {
        dimensionRules.put(dimension, rules);
        OTGLog.info("Registered custom GameRules for dimension {}", dimension.location());
    }

    public static void unregister(ResourceKey<Level> dimension) {
        if (dimensionRules.remove(dimension) != null) {
            OTGLog.info("Unregistered custom GameRules for dimension {}", dimension.location());
        }
    }

    public static boolean hasCustomRules(ResourceKey<Level> dimension) {
        return dimensionRules.containsKey(dimension);
    }

    public static void clear() {
        int count = dimensionRules.size();
        dimensionRules.clear();
        if (count > 0) {
            OTGLog.info("Cleared {} dimension GameRules entries", count);
        }
    }
}
```

**Step 2: Build**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```
feat: add GameRuleManager for per-dimension GameRules

Static ConcurrentHashMap holding ResourceKey<Level> → GameRules.
Used by the Level.getGameRules() mixin to dispatch per-dimension rules.
```

---

### Task 6: Create GameRuleApplier

**Files:**
- Create: `platforms/shared/src/main/java/com/pg85/otg/shared/gamerules/GameRuleApplier.java`

**Step 1: Create GameRuleApplier**

This class creates MC `GameRules` instances from OTG config. Mapping from OTG setting names to MC `GameRules.Key<>` constants.

```java
package com.pg85.otg.shared.gamerules;

import com.pg85.otg.config.dimensions.DimensionConfig;
import com.pg85.otg.config.settings.preset.GameRuleSettings;
import com.pg85.otg.util.OTGLog;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.GameRules;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Creates MC GameRules from OTG config (PresetConfig + optional DimensionConfig override).
 */
public final class GameRuleApplier {

    private GameRuleApplier() {}

    /**
     * Creates a new GameRules instance populated from PresetConfig settings
     * with optional DimensionConfig overrides.
     */
    public static GameRules createGameRules(
            GameRuleSettings presetRules,
            @Nullable DimensionConfig.GameRules overrides,
            MinecraftServer server
    ) {
        GameRules rules = new GameRules();

        if (!presetRules.isOverrideGameRules()) {
            OTGLog.info("OverrideGameRules is false — using vanilla defaults");
            return rules;
        }

        applyFromPreset(rules, presetRules, server);

        if (overrides != null) {
            OTGLog.info("Applying DimensionConfig GameRules overrides");
            applyFromDimensionConfig(rules, overrides, server);
        }

        return rules;
    }

    private static void applyFromPreset(GameRules rules, GameRuleSettings s, MinecraftServer server) {
        // Boolean rules
        rules.getRule(GameRules.RULE_DOFIRETICK).set(s.isDoFireTick(), server);
        rules.getRule(GameRules.RULE_MOBGRIEFING).set(s.isMobGriefing(), server);
        rules.getRule(GameRules.RULE_KEEPINVENTORY).set(s.isKeepInventory(), server);
        rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(s.isDoMobSpawning(), server);
        rules.getRule(GameRules.RULE_DOMOBLOOT).set(s.isDoMobLoot(), server);
        rules.getRule(GameRules.RULE_DOBLOCKDROPS).set(s.isDoTileDrops(), server);
        rules.getRule(GameRules.RULE_DOENTITYDROPS).set(s.isDoEntityDrops(), server);
        rules.getRule(GameRules.RULE_COMMANDBLOCKOUTPUT).set(s.isCommandBlockOutput(), server);
        rules.getRule(GameRules.RULE_NATURAL_REGENERATION).set(s.isNaturalRegeneration(), server);
        rules.getRule(GameRules.RULE_DAYLIGHT).set(s.isDoDaylightCycle(), server);
        rules.getRule(GameRules.RULE_LOGADMINCOMMANDS).set(s.isLogAdminCommands(), server);
        rules.getRule(GameRules.RULE_SHOWDEATHMESSAGES).set(s.isShowDeathMessages(), server);
        rules.getRule(GameRules.RULE_SENDCOMMANDFEEDBACK).set(s.isSendCommandFeedback(), server);
        rules.getRule(GameRules.RULE_SPECTATORSGENERATECHUNKS).set(s.isSpectatorsGenerateChunks(), server);
        rules.getRule(GameRules.RULE_DISABLE_ELYTRA_MOVEMENT_CHECK).set(s.isDisableElytraMovementCheck(), server);
        rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(s.isDoWeatherCycle(), server);
        rules.getRule(GameRules.RULE_LIMITED_CRAFTING).set(s.isDoLimitedCrafting(), server);
        rules.getRule(GameRules.RULE_ANNOUNCE_ADVANCEMENTS).set(s.isAnnounceAdvancements(), server);
        rules.getRule(GameRules.RULE_DISABLE_RAIDS).set(s.isDisableRaids(), server);
        rules.getRule(GameRules.RULE_DOINSOMNIA).set(s.isDoInsomnia(), server);
        rules.getRule(GameRules.RULE_DROWNING_DAMAGE).set(s.isDrowningDamage(), server);
        rules.getRule(GameRules.RULE_FALL_DAMAGE).set(s.isFallDamage(), server);
        rules.getRule(GameRules.RULE_FIRE_DAMAGE).set(s.isFireDamage(), server);
        rules.getRule(GameRules.RULE_DO_PATROL_SPAWNING).set(s.isDoPatrolSpawning(), server);
        rules.getRule(GameRules.RULE_DO_TRADER_SPAWNING).set(s.isDoTraderSpawning(), server);
        rules.getRule(GameRules.RULE_FORGIVE_DEAD_PLAYERS).set(s.isForgiveDeadPlayers(), server);
        rules.getRule(GameRules.RULE_UNIVERSAL_ANGER).set(s.isUniversalAnger(), server);
        // New 1.21.1 boolean rules
        rules.getRule(GameRules.RULE_PROJECTILESCANBREAKBLOCKS).set(s.isProjectilesCanBreakBlocks(), server);
        rules.getRule(GameRules.RULE_REDUCEDDEBUGINFO).set(s.isReducedDebugInfo(), server);
        rules.getRule(GameRules.RULE_DO_IMMEDIATE_RESPAWN).set(s.isDoImmediateRespawn(), server);
        rules.getRule(GameRules.RULE_FREEZE_DAMAGE).set(s.isFreezeDamage(), server);
        rules.getRule(GameRules.RULE_DO_WARDEN_SPAWNING).set(s.isDoWardenSpawning(), server);
        rules.getRule(GameRules.RULE_BLOCK_EXPLOSION_DROP_DECAY).set(s.isBlockExplosionDropDecay(), server);
        rules.getRule(GameRules.RULE_MOB_EXPLOSION_DROP_DECAY).set(s.isMobExplosionDropDecay(), server);
        rules.getRule(GameRules.RULE_TNT_EXPLOSION_DROP_DECAY).set(s.isTntExplosionDropDecay(), server);
        rules.getRule(GameRules.RULE_WATER_SOURCE_CONVERSION).set(s.isWaterSourceConversion(), server);
        rules.getRule(GameRules.RULE_LAVA_SOURCE_CONVERSION).set(s.isLavaSourceConversion(), server);
        rules.getRule(GameRules.RULE_GLOBAL_SOUND_EVENTS).set(s.isGlobalSoundEvents(), server);
        rules.getRule(GameRules.RULE_DO_VINES_SPREAD).set(s.isDoVinesSpread(), server);
        rules.getRule(GameRules.RULE_ENDER_PEARLS_VANISH_ON_DEATH).set(s.isEnderPearlsVanishOnDeath(), server);

        // Integer rules
        rules.getRule(GameRules.RULE_RANDOMTICKING).set(s.getRandomTickSpeed(), server);
        rules.getRule(GameRules.RULE_SPAWN_RADIUS).set(s.getSpawnRadius(), server);
        rules.getRule(GameRules.RULE_MAX_ENTITY_CRAMMING).set(s.getMaxEntityCramming(), server);
        rules.getRule(GameRules.RULE_MAX_COMMAND_CHAIN_LENGTH).set(s.getMaxCommandChainLength(), server);
        // New 1.21.1 integer rules
        rules.getRule(GameRules.RULE_MAX_COMMAND_FORK_COUNT).set(s.getMaxCommandForkCount(), server);
        rules.getRule(GameRules.RULE_COMMAND_MODIFICATION_BLOCK_LIMIT).set(s.getCommandModificationBlockLimit(), server);
        rules.getRule(GameRules.RULE_PLAYERS_NETHER_PORTAL_DEFAULT_DELAY).set(s.getPlayersNetherPortalDefaultDelay(), server);
        rules.getRule(GameRules.RULE_PLAYERS_NETHER_PORTAL_CREATIVE_DELAY).set(s.getPlayersNetherPortalCreativeDelay(), server);
        rules.getRule(GameRules.RULE_PLAYERS_SLEEPING_PERCENTAGE).set(s.getPlayersSleepingPercentage(), server);
        rules.getRule(GameRules.RULE_SNOW_ACCUMULATION_HEIGHT).set(s.getSnowAccumulationHeight(), server);
        rules.getRule(GameRules.RULE_SPAWN_CHUNK_RADIUS).set(s.getSpawnChunkRadius(), server);
    }

    private static void applyFromDimensionConfig(GameRules rules, DimensionConfig.GameRules dc, MinecraftServer server) {
        // DimensionConfig.GameRules fields are non-null primitives (bool/int),
        // so they always override. If present in YAML, they replace preset values.
        rules.getRule(GameRules.RULE_DOFIRETICK).set(dc.DoFireTick, server);
        rules.getRule(GameRules.RULE_MOBGRIEFING).set(dc.MobGriefing, server);
        rules.getRule(GameRules.RULE_KEEPINVENTORY).set(dc.KeepInventory, server);
        rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(dc.DoMobSpawning, server);
        rules.getRule(GameRules.RULE_DOMOBLOOT).set(dc.DoMobLoot, server);
        rules.getRule(GameRules.RULE_DOBLOCKDROPS).set(dc.DoTileDrops, server);
        rules.getRule(GameRules.RULE_DOENTITYDROPS).set(dc.DoEntityDrops, server);
        rules.getRule(GameRules.RULE_COMMANDBLOCKOUTPUT).set(dc.CommandBlockOutput, server);
        rules.getRule(GameRules.RULE_NATURAL_REGENERATION).set(dc.NaturalRegeneration, server);
        rules.getRule(GameRules.RULE_DAYLIGHT).set(dc.DoDaylightCycle, server);
        rules.getRule(GameRules.RULE_LOGADMINCOMMANDS).set(dc.LogAdminCommands, server);
        rules.getRule(GameRules.RULE_SHOWDEATHMESSAGES).set(dc.ShowDeathMessages, server);
        rules.getRule(GameRules.RULE_SENDCOMMANDFEEDBACK).set(dc.SendCommandFeedback, server);
        rules.getRule(GameRules.RULE_SPECTATORSGENERATECHUNKS).set(dc.SpectatorsGenerateChunks, server);
        rules.getRule(GameRules.RULE_DISABLE_ELYTRA_MOVEMENT_CHECK).set(dc.DisableElytraMovementCheck, server);
        rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(dc.DoWeatherCycle, server);
        rules.getRule(GameRules.RULE_LIMITED_CRAFTING).set(dc.DoLimitedCrafting, server);
        rules.getRule(GameRules.RULE_ANNOUNCE_ADVANCEMENTS).set(dc.AnnounceAdvancements, server);
        rules.getRule(GameRules.RULE_DISABLE_RAIDS).set(dc.DisableRaids, server);
        rules.getRule(GameRules.RULE_DOINSOMNIA).set(dc.DoInsomnia, server);
        rules.getRule(GameRules.RULE_DROWNING_DAMAGE).set(dc.DrowningDamage, server);
        rules.getRule(GameRules.RULE_FALL_DAMAGE).set(dc.FallDamage, server);
        rules.getRule(GameRules.RULE_FIRE_DAMAGE).set(dc.FireDamage, server);
        rules.getRule(GameRules.RULE_DO_PATROL_SPAWNING).set(dc.DoPatrolSpawning, server);
        rules.getRule(GameRules.RULE_DO_TRADER_SPAWNING).set(dc.DoTraderSpawning, server);
        rules.getRule(GameRules.RULE_FORGIVE_DEAD_PLAYERS).set(dc.ForgiveDeadPlayers, server);
        rules.getRule(GameRules.RULE_UNIVERSAL_ANGER).set(dc.UniversalAnger, server);
        // New 1.21.1 rules
        rules.getRule(GameRules.RULE_PROJECTILESCANBREAKBLOCKS).set(dc.ProjectilesCanBreakBlocks, server);
        rules.getRule(GameRules.RULE_REDUCEDDEBUGINFO).set(dc.ReducedDebugInfo, server);
        rules.getRule(GameRules.RULE_DO_IMMEDIATE_RESPAWN).set(dc.DoImmediateRespawn, server);
        rules.getRule(GameRules.RULE_FREEZE_DAMAGE).set(dc.FreezeDamage, server);
        rules.getRule(GameRules.RULE_DO_WARDEN_SPAWNING).set(dc.DoWardenSpawning, server);
        rules.getRule(GameRules.RULE_BLOCK_EXPLOSION_DROP_DECAY).set(dc.BlockExplosionDropDecay, server);
        rules.getRule(GameRules.RULE_MOB_EXPLOSION_DROP_DECAY).set(dc.MobExplosionDropDecay, server);
        rules.getRule(GameRules.RULE_TNT_EXPLOSION_DROP_DECAY).set(dc.TntExplosionDropDecay, server);
        rules.getRule(GameRules.RULE_WATER_SOURCE_CONVERSION).set(dc.WaterSourceConversion, server);
        rules.getRule(GameRules.RULE_LAVA_SOURCE_CONVERSION).set(dc.LavaSourceConversion, server);
        rules.getRule(GameRules.RULE_GLOBAL_SOUND_EVENTS).set(dc.GlobalSoundEvents, server);
        rules.getRule(GameRules.RULE_DO_VINES_SPREAD).set(dc.DoVinesSpread, server);
        rules.getRule(GameRules.RULE_ENDER_PEARLS_VANISH_ON_DEATH).set(dc.EnderPearlsVanishOnDeath, server);
        // Integers
        rules.getRule(GameRules.RULE_RANDOMTICKING).set(dc.RandomTickSpeed, server);
        rules.getRule(GameRules.RULE_SPAWN_RADIUS).set(dc.SpawnRadius, server);
        rules.getRule(GameRules.RULE_MAX_ENTITY_CRAMMING).set(dc.MaxEntityCramming, server);
        rules.getRule(GameRules.RULE_MAX_COMMAND_CHAIN_LENGTH).set(dc.MaxCommandChainLength, server);
        rules.getRule(GameRules.RULE_MAX_COMMAND_FORK_COUNT).set(dc.MaxCommandForkCount, server);
        rules.getRule(GameRules.RULE_COMMAND_MODIFICATION_BLOCK_LIMIT).set(dc.CommandModificationBlockLimit, server);
        rules.getRule(GameRules.RULE_PLAYERS_NETHER_PORTAL_DEFAULT_DELAY).set(dc.PlayersNetherPortalDefaultDelay, server);
        rules.getRule(GameRules.RULE_PLAYERS_NETHER_PORTAL_CREATIVE_DELAY).set(dc.PlayersNetherPortalCreativeDelay, server);
        rules.getRule(GameRules.RULE_PLAYERS_SLEEPING_PERCENTAGE).set(dc.PlayersSleepingPercentage, server);
        rules.getRule(GameRules.RULE_SNOW_ACCUMULATION_HEIGHT).set(dc.SnowAccumulationHeight, server);
        rules.getRule(GameRules.RULE_SPAWN_CHUNK_RADIUS).set(dc.SpawnChunkRadius, server);
    }

    /**
     * Serializes a GameRules instance to a map for persistence.
     */
    public static Map<String, Object> toMap(GameRules rules) {
        Map<String, Object> map = new LinkedHashMap<>();
        // Use CompoundTag serialization and convert
        var tag = rules.createTag();
        for (String key : tag.getAllKeys()) {
            String value = tag.getString(key);
            // Try parsing as int, fallback to boolean string
            try {
                map.put(key, Integer.parseInt(value));
            } catch (NumberFormatException e) {
                map.put(key, Boolean.parseBoolean(value));
            }
        }
        return map;
    }

    /**
     * Deserializes a GameRules instance from a persisted map.
     */
    public static GameRules fromMap(Map<String, Object> map) {
        GameRules rules = new GameRules();
        // Build a CompoundTag from the map and load
        var tag = new net.minecraft.nbt.CompoundTag();
        for (var entry : map.entrySet()) {
            tag.putString(entry.getKey(), String.valueOf(entry.getValue()));
        }
        // Use copy() trick: create rules, assign from tag-loaded rules
        var loaded = new GameRules(net.minecraft.nbt.NbtOps.INSTANCE.withParser(
                com.mojang.serialization.Dynamic::new).apply(tag));
        rules.assignFrom(loaded, null);
        return rules;
    }
}
```

**Note:** The `fromMap` method may need adjustment during implementation — MC's `GameRules(DynamicLike<?>)` constructor takes a `DynamicLike` which wraps NBT. The exact API for constructing a `DynamicLike<CompoundTag>` should be verified against the actual MC source. The alternative approach is:

```java
public static GameRules fromMap(Map<String, Object> map) {
    var tag = new net.minecraft.nbt.CompoundTag();
    for (var entry : map.entrySet()) {
        tag.putString(entry.getKey(), String.valueOf(entry.getValue()));
    }
    return new GameRules(new com.mojang.serialization.Dynamic<>(
            net.minecraft.nbt.NbtOps.INSTANCE, tag));
}
```

**Step 2: Build**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```
feat: add GameRuleApplier for merging PresetConfig + DimensionConfig rules

Creates MC GameRules from OTG settings with serialization/deserialization
for persistence in OTGWorldStorage.
```

---

### Task 7: Create LevelGameRulesMixin (shared)

**Files:**
- Create: `platforms/shared/src/main/java/com/pg85/otg/shared/mixin/LevelGameRulesMixin.java`
- Modify: `platforms/shared/src/main/resources/otg-shared.mixins.json`

**Step 1: Create the mixin**

```java
package com.pg85.otg.shared.mixin;

import com.pg85.otg.shared.gamerules.GameRuleManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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

**Step 2: Register mixin in otg-shared.mixins.json**

Add `"LevelGameRulesMixin"` to the mixins array:

```json
{
  "required": true,
  "minVersion": "0.8",
  "package": "com.pg85.otg.shared.mixin",
  "compatibilityLevel": "JAVA_21",
  "mixins": [
    "BiomeDataMixin",
    "ChunkAccessAccessor",
    "CocoaDecoratorMixin",
    "LevelGameRulesMixin",
    "MappedRegistryAccessor",
    "MinecraftServerAccessor",
    "WorldPresetTagsMixin"
  ]
}
```

**Step 3: Build**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

**Step 4: Commit**

```
feat: add LevelGameRulesMixin to intercept per-dimension GameRules

Mixin on Level.getGameRules() dispatches to GameRuleManager for OTG
dimensions. Only active server-side (instanceof ServerLevel check).
```

---

### Task 8: Integrate GameRules into DimensionManager lifecycle

**Files:**
- Modify: `platforms/shared/src/main/java/com/pg85/otg/shared/dimensions/DimensionManager.java`

**Step 1: Add imports and GameRuleManager integration**

Add to DimensionManager:
- Import `GameRuleManager`, `GameRuleApplier`, `GameRuleSettings`
- Import `DimensionConfigLoader`, `DimensionConfig`
- Import MC `Level`, `ResourceKey`, `GameRules`

**Step 2: Modify initialize() to restore GameRules from storage**

After existing storage.load() and datapack regeneration, add:

```java
// Restore persisted GameRules for all dimensions
for (var entry : storage.getAllGameRules().entrySet()) {
    String dimKeyStr = entry.getKey();
    ResourceKey<Level> levelKey;
    if (dimKeyStr.startsWith("minecraft:")) {
        levelKey = ResourceKey.create(
            net.minecraft.core.registries.Registries.DIMENSION,
            net.minecraft.resources.ResourceLocation.parse(dimKeyStr));
    } else {
        // OTG dimension — extract name after "otg:"
        String name = dimKeyStr.substring(dimKeyStr.indexOf(':') + 1);
        levelKey = DimensionKeys.otg(name);
    }
    GameRules rules = GameRuleApplier.fromMap(entry.getValue());
    GameRuleManager.register(levelKey, rules);
}
OTGLog.info("Restored GameRules for {} dimensions", storage.getAllGameRules().size());
```

**Step 3: Modify createDimension() to create and persist GameRules**

After `storage.addDimension(info)` and before `helper.createDimensionRuntime(...)`:

```java
// Apply GameRules from preset (+ optional DimensionConfig override)
GameRuleSettings gameRuleSettings = preset.getPresetConfig().getGameRuleSettings();
DimensionConfig.GameRules dimConfigOverrides = loadDimensionConfigGameRules(presetName);
GameRules gameRules = GameRuleApplier.createGameRules(gameRuleSettings, dimConfigOverrides, server);
ResourceKey<Level> levelKey = DimensionKeys.otg(normalizedName);
GameRuleManager.register(levelKey, gameRules);
storage.putGameRules("otg:" + normalizedName, GameRuleApplier.toMap(gameRules));
```

**Step 4: Modify deleteDimension() to clean up GameRules**

After `storage.removeDimension(normalizedName)`:

```java
GameRuleManager.unregister(DimensionKeys.otg(normalizedName));
storage.removeGameRules("otg:" + normalizedName);
```

**Step 5: Add helper method for DimensionConfig loading**

```java
private @Nullable DimensionConfig.GameRules loadDimensionConfigGameRules(String presetName) {
    DimensionConfig dimConfig = DimensionConfigLoader.fromDisk(
            presetName, helper.getWorldPath(server).getParent());
    if (dimConfig != null && dimConfig.GameRules != null) {
        return dimConfig.GameRules;
    }
    return null;
}
```

**Step 6: Add server stop cleanup**

Add a public method:

```java
public void shutdown() {
    GameRuleManager.clear();
}
```

**Step 7: Build**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

**Step 8: Commit**

```
feat: integrate GameRules into DimensionManager lifecycle

GameRules are created at dimension creation (from PresetConfig + optional
DimensionConfig override), persisted in OTGWorldStorage, and restored
on server restart.
```

---

### Task 9: Wire up server stop + save events on both platforms

**Files:**
- Modify: `platforms/neoforge/src/main/java/com/pg85/otg/neoforge/events/NeoForgeEventHandler.java`
- Modify: `platforms/fabric/src/main/java/com/pg85/otg/fabric/OTGPlugin.java`

**Step 1: NeoForge — add shutdown call in onServerStopping**

In `NeoForgeEventHandler.java` line 44, modify `onServerStopping`:

```java
@SubscribeEvent
public static void onServerStopping(ServerStoppingEvent event) {
    if (dimensionManager != null) {
        dimensionManager.shutdown();
    }
    OTGLog.info("Server stopping");
}
```

**Step 2: Fabric — add shutdown call in SERVER_STOPPING**

In `OTGPlugin.java` line 70, modify `registerServerEvents`:

```java
ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
    if (dimensionManager != null) {
        dimensionManager.shutdown();
    }
    OTGLog.info("Server stopping");
});
```

**Step 3: Build**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

**Step 4: Commit**

```
feat: wire GameRuleManager.clear() to server stop on both platforms

Ensures per-dimension GameRules map is cleaned up when server shuts down.
```

---

### Task 10: Handle overworld GameRules at initialization

**Files:**
- Modify: `platforms/shared/src/main/java/com/pg85/otg/shared/dimensions/DimensionManager.java`

This is the tricky part — detecting if the overworld uses an OTG preset and applying its GameRules.

**Step 1: Add overworld detection to initialize()**

After the existing GameRules restoration code, add:

```java
// Apply GameRules for overworld if using OTG preset (first-time only)
if (!storage.getGameRules("minecraft:overworld").isPresent()) {
    ServerLevel overworld = server.overworld();
    if (overworld.getChunkSource().getGenerator() instanceof /* OTG chunk generator check */) {
        // Detect which preset the overworld uses
        // This requires checking the chunk generator's preset name
        applyOverworldGameRules(server, overworld);
    }
}
```

The exact OTG chunk generator check depends on platform. Add an abstract method to `PlatformDimensionHelper`:

```java
@Nullable String getOverworldPresetName(MinecraftServer server);
```

Implement in both platform helpers — check if overworld's chunk generator is OTG and return the preset name.

**Step 2: Add applyOverworldGameRules method**

```java
private void applyOverworldGameRules(MinecraftServer server, String presetName) {
    Preset preset = OTG.getEngine().getPresetLoader().getPresetByFolderName(presetName);
    if (preset == null) return;

    GameRuleSettings gameRuleSettings = preset.getPresetConfig().getGameRuleSettings();
    if (!gameRuleSettings.isOverrideGameRules()) return;

    DimensionConfig.GameRules overrides = loadDimensionConfigGameRules(presetName);
    GameRules rules = GameRuleApplier.createGameRules(gameRuleSettings, overrides, server);
    GameRuleManager.register(Level.OVERWORLD, rules);
    storage.putGameRules("minecraft:overworld", GameRuleApplier.toMap(rules));
    OTGLog.info("Applied GameRules for OTG overworld (preset: {})", presetName);
}
```

**Step 3: Add getOverworldPresetName to PlatformDimensionHelper interface**

In `PlatformDimensionHelper.java`:

```java
@Nullable String getOverworldPresetName(MinecraftServer server);
```

**Step 4: Implement in both platform helpers**

Both `FabricDimensionHelper` and `NeoForgeDimensionHelper` extend `SharedDimensionHelper`. Add to `SharedDimensionHelper`:

```java
@Override
public @Nullable String getOverworldPresetName(MinecraftServer server) {
    ServerLevel overworld = server.overworld();
    ChunkGenerator gen = overworld.getChunkSource().getGenerator();
    // Check if it's an OTG chunk generator and extract preset name
    // The exact class name is platform-specific (OTGFabricChunkGenerator / OTGNeoForgeChunkGenerator)
    // Use the shared base: check for method getPreset()
    if (gen instanceof com.pg85.otg.shared.gen.SharedOTGChunkGenerator otgGen) {
        return otgGen.getPreset().getFolderName();
    }
    return null;
}
```

**Note:** The exact class hierarchy should be verified during implementation. `SharedOTGChunkGenerator` may not implement `ChunkGenerator` directly — the platform-specific subclasses do. This may need to be an abstract method with platform-specific implementations instead.

**Step 5: Build**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

**Step 6: Commit**

```
feat: apply GameRules for OTG overworld at first initialization

Detects if overworld uses OTG preset, applies GameRuleSettings + optional
DimensionConfig overrides, and persists for subsequent restarts.
```

---

### Task 11: Update DefaultPreset PresetConfig.ini with new rules

**Files:**
- Modify: `resources/Presets/DefaultPreset/PresetConfig.ini`

**Step 1: Add new GameRule settings**

After the existing `UniversalAnger: false` line (around line 595), add:

```ini
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
```

**Step 2: Build**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```
feat: add new 1.21.1 GameRule defaults to DefaultPreset PresetConfig.ini
```

---

### Task 12: Final build + manual verification checklist

**Step 1: Full clean build**

Run: `./gradlew clean build`
Expected: BUILD SUCCESSFUL with JAR at `build/distributions/`

**Step 2: Manual testing checklist**

Test in-game (both Fabric and NeoForge):

1. **Fresh world creation with OTG overworld:**
   - Set `OverrideGameRules: true` and `KeepInventory: true` in DefaultPreset's PresetConfig.ini
   - Create new OTG world
   - Verify `/gamerule keepInventory` returns `true`
   - Verify `otg_world_data.json` exists with `minecraft:overworld` entry in gameRules

2. **Custom dimension creation:**
   - `/otg dimension create VanillaVistas` (or other preset)
   - Verify that dimension's GameRules match its preset
   - Verify `otg_world_data.json` has `otg:vanillavistas` entry

3. **Server restart persistence:**
   - Stop server, start again
   - Verify GameRules in custom dimension are still applied
   - Verify `/gamerule` in custom dimension shows custom values

4. **DimensionConfig override:**
   - Create a DimensionConfig YAML with `GameRules: { KeepInventory: true }`
   - Create dimension, verify KeepInventory is overridden

5. **v1 migration:**
   - Create a world with existing `otg_dimensions.json` (v1 format)
   - Start server, verify migration to `otg_world_data.json`
   - Verify old file renamed to `.v1backup`

6. **Dimension deletion:**
   - Delete a custom dimension
   - Verify its GameRules removed from `otg_world_data.json`
   - Verify GameRuleManager no longer has entry

**Step 3: Commit (if any fixes needed)**

```
fix: [description of any fixes from testing]
```
