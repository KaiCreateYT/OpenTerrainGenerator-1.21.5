# Dynamic Dimensions for OTG Fabric

## Overview

Runtime creation and deletion of OTG dimensions via commands, with persistence across server restarts.

## Requirements

- Create dimensions via `/otg dimension create <preset>`
- Delete dimensions via `/otg dimension delete <dimension> [--purge] [--confirm]`
- List dimensions via `/otg dimension list`
- Show dimension info via `/otg dimension info <dimension>`
- Teleport via `/otg tp <dimension>`
- Hot-reload (no server restart required)
- Persistence via datapack JSON files
- Multi-platform ready (Fabric now, Forge later)

## Architecture

```
common/common-core/src/main/java/com/pg85/otg/
└── dimensions/
    ├── DimensionDatapack.java       # Generowanie JSON (pure Java)
    ├── DimensionInfo.java           # POJO: name, preset, seed, created
    └── DimensionStorage.java        # Load/save otg_dimensions.json

platforms/shared/src/main/java/com/pg85/otg/shared/
└── dimensions/
    ├── OTGDimensionManager.java     # Main logic (abstract)
    └── PlatformDimensionHelper.java # Interface for platform-specific ops

platforms/fabric/src/main/java/com/pg85/otg/fabric/
└── dimensions/
    ├── FabricDimensionManager.java  # OTGDimensionManager implementation
    ├── FabricDimensionHelper.java   # PlatformDimensionHelper implementation
    └── FabricDimensionCommands.java # Command registration

platforms/forge/src/main/java/com/pg85/otg/forge/
└── dimensions/
    ├── ForgeDimensionManager.java   # Future
    ├── ForgeDimensionHelper.java    # Future
    └── ForgeDimensionCommands.java  # Future
```

## PlatformDimensionHelper Interface

```java
public interface PlatformDimensionHelper {
    void reloadDatapacks(MinecraftServer server);
    void teleportPlayer(ServerPlayer player, ResourceKey<Level> dimension);
    void teleportToOverworldSpawn(ServerPlayer player);
    List<ServerPlayer> getPlayersInDimension(ResourceKey<Level> dimension);
    Path getWorldDatapackPath(MinecraftServer server);
    boolean isDimensionLoaded(MinecraftServer server, String name);
    void createLevelRuntime(MinecraftServer server, String name, Preset preset, long seed);
    void deleteLevelRuntime(MinecraftServer server, String name);
    void purgeWorldData(MinecraftServer server, String name);
}
```

## Datapack Structure

```
<world>/datapacks/otg/
├── pack.mcmeta
└── data/otg/
    ├── dimension_type/
    │   └── <name>.json      # DimensionType properties
    └── dimension/
        └── <name>.json      # Generator config
```

### dimension_type/<name>.json

```json
{
  "ultrawarm": false,
  "natural": true,
  "coordinate_scale": 1.0,
  "has_skylight": true,
  "has_ceiling": false,
  "ambient_light": 0.0,
  "piglin_safe": false,
  "bed_works": true,
  "respawn_anchor_works": false,
  "has_raids": true,
  "logical_height": 384,
  "min_y": -64,
  "height": 384,
  "infiniburn": "#minecraft:infiniburn_overworld",
  "effects": "minecraft:overworld",
  "monster_spawn_light_level": 0,
  "monster_spawn_block_light_limit": 0
}
```

### dimension/<name>.json

```json
{
  "type": "otg:<name>",
  "generator": {
    "type": "otg:otg",
    "preset": "<PresetName>",
    "seed": 123456789
  }
}
```

## Persistence File

`<world>/otg_dimensions.json`:

```json
{
  "version": 1,
  "dimensions": [
    {
      "name": "biome_bundle",
      "preset": "BiomeBundle",
      "seed": 847291635,
      "created": 1706793120000
    }
  ]
}
```

## Commands

| Command | Permission | Description |
|---------|------------|-------------|
| `/otg dimension create <preset>` | OP (level 2) | Create dimension |
| `/otg dimension delete <dim>` | OP (level 2) | Unload dimension, keep data |
| `/otg dimension delete <dim> --purge --confirm` | OP (level 2) | Delete dimension and all data |
| `/otg dimension list` | All | List OTG dimensions |
| `/otg dimension info <dim>` | All | Show dimension details |
| `/otg tp <dim>` | All | Teleport to dimension |

## Hot-Reload Implementation

### Required Mixins

```java
// Access to private levels map
@Mixin(MinecraftServer.class)
public interface MinecraftServerAccessor {
    @Accessor("levels")
    Map<ResourceKey<Level>, ServerLevel> getLevels();
}

// Unfreeze registry for runtime registration
@Mixin(MappedRegistry.class)
public class MappedRegistryMixin {
    @Shadow private boolean frozen;

    public void otg_unfreeze() {
        this.frozen = false;
    }
}
```

### Create Dimension Flow

1. Validate preset exists
2. Generate random seed
3. Write datapack JSONs
4. Save to otg_dimensions.json
5. Unfreeze registries
6. Register DimensionType
7. Register LevelStem
8. Create ServerLevel
9. Add to MinecraftServer.levels
10. Sync to clients
11. Re-freeze registries

### Delete Dimension Flow

1. Validate dimension exists and is OTG
2. Teleport all players to overworld spawn
3. Save dimension
4. Unload all chunks
5. Remove from MinecraftServer.levels
6. Delete datapack JSONs
7. Update otg_dimensions.json
8. Sync removal to clients
9. If --purge: delete dimensions/otg/<name>/ folder

## Error Handling

### Create Errors

| Condition | Response |
|-----------|----------|
| Preset not found | `Unknown preset 'X'. Use /otg preset list` |
| Dimension exists | `Dimension otg:x already exists` |
| No permission | `You need operator permissions` |
| Disk write failed | `Failed to create dimension: disk write error` + rollback |
| Registry frozen | `Failed to register. Server restart required.` |

### Delete Errors

| Condition | Response |
|-----------|----------|
| Dimension not found | `Unknown dimension 'x'. Use /otg dimension list` |
| Vanilla dimension | `Cannot delete vanilla dimensions` |
| Non-OTG dimension | `Cannot delete non-OTG dimension` |

### Edge Cases

| Condition | Handling |
|-----------|----------|
| Crash during create | Cleanup orphaned JSONs on restart |
| JSON manually deleted | Regenerate from storage on restart |
| Preset deleted after creation | Warning in logs, dimension works with cached data |

## Risks

1. **Registry unfreezing** - May conflict with other mods that expect frozen registries
2. **Hot-reload** - Untested code path in Minecraft, potential for crashes
3. **Client sync** - Edge cases with dimension packets

## Out of Scope

- Portals to custom dimensions (future feature)
- GUI for dimension management
- Forge implementation (structure ready, code later)
- Custom seed configuration per dimension

## Estimated Size

- ~800-1200 lines new code
- ~100 lines mixins
