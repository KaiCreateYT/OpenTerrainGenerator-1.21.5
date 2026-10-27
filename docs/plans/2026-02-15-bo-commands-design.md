# BO2/3/4 Commands for OTG 1.21.1

## Goal

Port all 5 BO-related commands from OTG 1.12.2 to 1.21.1: spawn, export, structure, flushcache, exportbo4data.

## Architecture

Shared command logic in `platforms/shared/commands/`, thin platform adapters for registration.

```
platforms/shared/commands/
├── OTGCommandRegistrar.java       # Builds full Brigadier command tree
├── SpawnCommand.java              # /otg spawn <name> [rotation]
├── ExportCommand.java             # /otg export <name> [-a] [-t] [-o] [-b] [-bo4]
├── StructureCommand.java          # /otg structure
├── FlushCacheCommand.java         # /otg flushcache
└── ExportBO4DataCommand.java      # /otg exportbo4data

platforms/fabric/commands/
└── FabricCommandRegistrar.java    # CommandRegistrationCallback → OTGCommandRegistrar.register()

platforms/neoforge/commands/
└── NeoForgeCommandRegistrar.java  # @SubscribeEvent → OTGCommandRegistrar.register()
```

Existing dimension commands migrate into the same tree under OTGCommandRegistrar.

## Commands

### /otg spawn <name> [rotation]
- **Perm:** 2 (OP)
- **Args:** `name` (tab-complete from object list), `rotation` (NORTH/SOUTH/EAST/WEST, default NORTH)
- **Logic:** Raycast from player eye → find target block → `CustomObjectManager.getGlobalObjects().getObjectByName()` → `spawnForced()` at hit coords
- **BO4:** For BO4 structures, search nearby unpopulated chunks and plot structure

### /otg export <name> [flags]
- **Perm:** 2
- **Args:** `name`, flags: `-a` (air), `-t` (tile entities), `-o` (overwrite), `-b` (force branch), `-bo4` (BO4 format)
- **Logic:** Get WorldEdit selection → `ObjectCreator.create()` → save to GlobalObjects → reload cache
- **WorldEdit:** Optional runtime dependency. Check via `Class.forName()`. Error if absent.
- **Auto-branching:** BO4 > 16x16 or BO3 > 32x32 → automatic split

### /otg structure
- **Perm:** 0 (all)
- **Args:** none
- **Logic:** Player position → structure cache lookup → display author, description, object name

### /otg flushcache
- **Perm:** 2
- **Args:** none
- **Logic:** `OTG.getEngine().getCustomObjectManager().reloadCustomObjectFiles()` + clear chunk generator cache

### /otg exportbo4data
- **Perm:** 2
- **Args:** none
- **Logic:** Iterate biomes → for each BO4 in resource lists → generate .BO4Data → unload between exports

## WorldEdit Integration

WorldEdit Fabric API:
- `WorldEdit.getInstance().getSessionManager().get(actor)` → `LocalSession`
- `session.getSelection()` → `Region` (min/max)

Optional dependency - `ExportCommand` checks class presence at runtime.

## Key APIs (already in 1.21.1 codebase)

- `OTG.getEngine().getCustomObjectManager()` - object registry and cache
- `CustomObjectCollection.getObjectByName()` - lazy-load objects
- `CustomObject.spawnForced()` - force-place object at coordinates
- `ObjectCreator.create()` - export world region to BO3/BO4 file
- `IWorldGenRegion` - block placement abstraction

## Error Handling

- Object not found → message with tab-complete hint
- WorldEdit missing → "Install WorldEdit/FAWE"
- No selection → "Use //pos1 and //pos2 first"
- Player looking at sky → "No block in range"
- File exists without -o → "Use -o to overwrite"
