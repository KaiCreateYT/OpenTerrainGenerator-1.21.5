# BO2/3/4 Commands Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Port all 5 BO-related commands (spawn, export, structure, flushcache, exportbo4data) from OTG 1.12.2 to 1.21.1.

**Architecture:** Shared command logic in `platforms/shared/commands/` using Brigadier (available via Minecraft transitive dependency). Platform-specific code limited to: (1) registration hooks, (2) `LocalWorldGenRegion` factory for live-world block access, (3) `LocalNBTHelper` creation. A `CommandWorldAccessor` interface bridges platform-specific world access.

**Tech Stack:** Brigadier (Minecraft), OTG CustomObjectManager, ObjectCreator, WorldEdit API (optional runtime dep for export)

---

## Key Reference Files

| Purpose | Path |
|---------|------|
| Existing dimension commands (Fabric) | `platforms/fabric/.../dimensions/FabricDimensionCommands.java` |
| Existing dimension commands (NeoForge) | `platforms/neoforge/.../dimensions/NeoForgeDimensionCommands.java` |
| Fabric command registration | `platforms/fabric/.../OTGPlugin.java:60-65` |
| NeoForge command registration | `platforms/neoforge/.../events/NeoForgeEventHandler.java:27-31` |
| CustomObjectManager | `common/common-customobject/.../CustomObjectManager.java` |
| CustomObjectCollection | `common/common-customobject/.../CustomObjectCollection.java` |
| ObjectCreator | `common/common-customobject/.../creator/ObjectCreator.java` |
| FabricWorldGenRegion | `platforms/fabric/.../gen/FabricWorldGenRegion.java` |
| NeoForgeWorldGenRegion | `platforms/neoforge/.../gen/NeoForgeWorldGenRegion.java` |
| OTGFabricChunkGenerator | `platforms/fabric/.../gen/OTGFabricChunkGenerator.java` |
| Old Forge SpawnCommand | `platforms/forge/.../commands/SpawnCommand.java` |
| Old Forge ExportCommand | `platforms/forge/.../commands/ExportCommand.java` |
| Old Forge FlushCommand | `platforms/forge/.../commands/FlushCommand.java` |
| Old Forge StructureCommand | `platforms/forge/.../commands/StructureCommand.java` |
| Old Forge ExportBO4DataCommand | `platforms/forge/.../commands/ExportBO4DataCommand.java` |
| FabricNBTHelper | `platforms/fabric/.../util/FabricNBTHelper.java` |
| NeoForgeNBTHelper | `platforms/neoforge/.../util/NeoForgeNBTHelper.java` |

---

## Task 1: Command Infrastructure — Shared Module

**Files:**
- Create: `platforms/shared/src/main/java/com/pg85/otg/shared/commands/CommandWorldAccessor.java`
- Create: `platforms/shared/src/main/java/com/pg85/otg/shared/commands/OTGCommandRegistrar.java`

### Step 1: Create `CommandWorldAccessor` interface

This interface lets shared command code access platform-specific world operations.

```java
package com.pg85.otg.shared.commands;

import com.pg85.otg.customobject.structures.CustomStructureCache;
import com.pg85.otg.util.gen.LocalWorldGenRegion;
import com.pg85.otg.util.nbt.LocalNBTHelper;
import net.minecraft.server.level.ServerLevel;

/**
 * Platform-specific bridge for command-time world access.
 * Implemented by Fabric and NeoForge to provide WorldGenRegion and NBT helpers
 * from a live ServerLevel (not during chunk generation).
 */
public interface CommandWorldAccessor {
    /**
     * Create a LocalWorldGenRegion wrapping a live ServerLevel for command-time
     * block read/write. Returns null if the world is not OTG-managed.
     */
    LocalWorldGenRegion createCommandRegion(ServerLevel level, int chunkX, int chunkZ);

    /**
     * Get the structure cache for the given level, or null if not OTG.
     */
    CustomStructureCache getStructureCache(ServerLevel level);

    /**
     * Create a platform-specific NBT helper.
     */
    LocalNBTHelper createNBTHelper();
}
```

### Step 2: Create `OTGCommandRegistrar`

This builds the full `/otg` Brigadier command tree. For now, only register `flushcache` (simplest command) to prove the infrastructure works. Other commands added in subsequent tasks.

```java
package com.pg85.otg.shared.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class OTGCommandRegistrar {

    private static CommandWorldAccessor worldAccessor;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandWorldAccessor accessor) {
        worldAccessor = accessor;

        var otgCommand = Commands.literal("otg");

        // Commands will be added here in subsequent tasks
        FlushCacheCommand.register(otgCommand);

        dispatcher.register(otgCommand);
    }

    public static CommandWorldAccessor getWorldAccessor() {
        return worldAccessor;
    }
}
```

### Step 3: Build and verify compilation

Run: `./gradlew build` from worktree root.
Expected: Compilation succeeds (FlushCacheCommand doesn't exist yet, so comment out the line or create a stub).

### Step 4: Commit

```bash
git add platforms/shared/src/main/java/com/pg85/otg/shared/commands/
git commit -m "feat: add command infrastructure - CommandWorldAccessor and OTGCommandRegistrar"
```

---

## Task 2: FlushCacheCommand (Simplest Command)

**Files:**
- Create: `platforms/shared/src/main/java/com/pg85/otg/shared/commands/FlushCacheCommand.java`

### Step 1: Implement FlushCacheCommand

```java
package com.pg85.otg.shared.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.pg85.otg.OTG;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class FlushCacheCommand {

    public static void register(LiteralArgumentBuilder<CommandSourceStack> otgCommand) {
        otgCommand.then(
            Commands.literal("flushcache")
                .requires(source -> source.hasPermission(2))
                .executes(ctx -> {
                    OTG.getEngine().getCustomObjectManager().reloadCustomObjectFiles();
                    ctx.getSource().sendSuccess(
                        () -> Component.literal("OTG custom object cache flushed. Objects will reload on next use."),
                        true
                    );
                    return 1;
                })
        );
    }
}
```

### Step 2: Uncomment FlushCacheCommand.register() in OTGCommandRegistrar

The line `FlushCacheCommand.register(otgCommand);` should now compile.

### Step 3: Build and verify

Run: `./gradlew build`
Expected: Compilation succeeds.

### Step 4: Commit

```bash
git add platforms/shared/src/main/java/com/pg85/otg/shared/commands/FlushCacheCommand.java
git commit -m "feat: add /otg flushcache command"
```

---

## Task 3: Platform Registration Hooks

**Files:**
- Create: `platforms/fabric/src/main/java/com/pg85/otg/fabric/commands/FabricCommandWorldAccessor.java`
- Create: `platforms/neoforge/src/main/java/com/pg85/otg/neoforge/commands/NeoForgeCommandWorldAccessor.java`
- Modify: `platforms/fabric/src/main/java/com/pg85/otg/fabric/OTGPlugin.java:60-65`
- Modify: `platforms/neoforge/src/main/java/com/pg85/otg/neoforge/events/NeoForgeEventHandler.java:27-31`
- Modify: `platforms/fabric/src/main/java/com/pg85/otg/fabric/gen/FabricWorldGenRegion.java` (make constructor public)
- Modify: `platforms/neoforge/src/main/java/com/pg85/otg/neoforge/gen/NeoForgeWorldGenRegion.java` (make constructor public)

### Step 1: Make WorldGenRegion constructors public

In `FabricWorldGenRegion.java` line 65, change `protected` to `public`:
```java
public FabricWorldGenRegion(
```

Same in `NeoForgeWorldGenRegion.java`.

### Step 2: Create FabricCommandWorldAccessor

```java
package com.pg85.otg.fabric.commands;

import com.pg85.otg.OTG;
import com.pg85.otg.customobject.structures.CustomStructureCache;
import com.pg85.otg.fabric.gen.FabricWorldGenRegion;
import com.pg85.otg.fabric.gen.OTGFabricChunkGenerator;
import com.pg85.otg.fabric.util.FabricNBTHelper;
import com.pg85.otg.shared.commands.CommandWorldAccessor;
import com.pg85.otg.util.gen.LocalWorldGenRegion;
import com.pg85.otg.util.nbt.LocalNBTHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;

public class FabricCommandWorldAccessor implements CommandWorldAccessor {

    @Override
    public LocalWorldGenRegion createCommandRegion(ServerLevel level, int chunkX, int chunkZ) {
        if (!(level.getChunkSource().getGenerator() instanceof OTGFabricChunkGenerator otgGen)) {
            return null;
        }
        var preset = otgGen.getPreset();
        var otgWorldInfo = otgGen.getOtgWorldInfo();
        var chunkAccess = level.getChunk(chunkX, chunkZ);
        return new FabricWorldGenRegion(
            preset.getFolderName(),
            OTG.getEngine().getPluginConfig(),
            preset.getPresetConfig(),
            otgWorldInfo,
            level,
            chunkAccess,
            otgGen
        );
    }

    @Override
    public CustomStructureCache getStructureCache(ServerLevel level) {
        if (!(level.getChunkSource().getGenerator() instanceof OTGFabricChunkGenerator otgGen)) {
            return null;
        }
        return otgGen.getStructureCache(
            level.getServer().getWorldPath(LevelResource.ROOT)
        );
    }

    @Override
    public LocalNBTHelper createNBTHelper() {
        return new FabricNBTHelper();
    }
}
```

### Step 3: Create NeoForgeCommandWorldAccessor

Same pattern as Fabric but using NeoForge classes:

```java
package com.pg85.otg.neoforge.commands;

import com.pg85.otg.OTG;
import com.pg85.otg.customobject.structures.CustomStructureCache;
import com.pg85.otg.neoforge.gen.NeoForgeWorldGenRegion;
import com.pg85.otg.neoforge.gen.OTGNeoForgeChunkGenerator;
import com.pg85.otg.neoforge.util.NeoForgeNBTHelper;
import com.pg85.otg.shared.commands.CommandWorldAccessor;
import com.pg85.otg.util.gen.LocalWorldGenRegion;
import com.pg85.otg.util.nbt.LocalNBTHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;

public class NeoForgeCommandWorldAccessor implements CommandWorldAccessor {

    @Override
    public LocalWorldGenRegion createCommandRegion(ServerLevel level, int chunkX, int chunkZ) {
        if (!(level.getChunkSource().getGenerator() instanceof OTGNeoForgeChunkGenerator otgGen)) {
            return null;
        }
        var preset = otgGen.getPreset();
        var otgWorldInfo = otgGen.getOtgWorldInfo();
        var chunkAccess = level.getChunk(chunkX, chunkZ);
        return new NeoForgeWorldGenRegion(
            preset.getFolderName(),
            OTG.getEngine().getPluginConfig(),
            preset.getPresetConfig(),
            otgWorldInfo,
            level,
            chunkAccess,
            otgGen
        );
    }

    @Override
    public CustomStructureCache getStructureCache(ServerLevel level) {
        if (!(level.getChunkSource().getGenerator() instanceof OTGNeoForgeChunkGenerator otgGen)) {
            return null;
        }
        return otgGen.getStructureCache(
            level.getServer().getWorldPath(LevelResource.ROOT)
        );
    }

    @Override
    public LocalNBTHelper createNBTHelper() {
        return new NeoForgeNBTHelper();
    }
}
```

### Step 4: Wire OTGCommandRegistrar into Fabric registration

In `OTGPlugin.java`, modify `registerDimensionCommands()`:

```java
void registerDimensionCommands() {
    CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
        FabricDimensionCommands.register(dispatcher);
        OTGCommandRegistrar.register(dispatcher, new FabricCommandWorldAccessor());
        OTGLog.info("Registered OTG commands");
    });
}
```

Add imports:
```java
import com.pg85.otg.shared.commands.OTGCommandRegistrar;
import com.pg85.otg.fabric.commands.FabricCommandWorldAccessor;
```

### Step 5: Wire OTGCommandRegistrar into NeoForge registration

In `NeoForgeEventHandler.java`, modify `onRegisterCommands()`:

```java
@SubscribeEvent
public static void onRegisterCommands(RegisterCommandsEvent event) {
    NeoForgeDimensionCommands.register(event.getDispatcher());
    OTGCommandRegistrar.register(event.getDispatcher(), new NeoForgeCommandWorldAccessor());
    OTGLog.info("Registered OTG commands");
}
```

Add imports:
```java
import com.pg85.otg.shared.commands.OTGCommandRegistrar;
import com.pg85.otg.neoforge.commands.NeoForgeCommandWorldAccessor;
```

### Step 6: Verify getPreset() and getOtgWorldInfo() are accessible

Check that `OTGFabricChunkGenerator` has public `getPreset()` and `getOtgWorldInfo()` methods (should have `@Getter` via Lombok). If not, verify access patterns from the old code. Same for NeoForge.

### Step 7: Build and verify

Run: `./gradlew build`
Expected: Compilation succeeds on both platforms.

### Step 8: Commit

```bash
git add platforms/fabric/src/main/java/com/pg85/otg/fabric/commands/ \
      platforms/neoforge/src/main/java/com/pg85/otg/neoforge/commands/ \
      platforms/fabric/src/main/java/com/pg85/otg/fabric/OTGPlugin.java \
      platforms/neoforge/src/main/java/com/pg85/otg/neoforge/events/NeoForgeEventHandler.java \
      platforms/fabric/src/main/java/com/pg85/otg/fabric/gen/FabricWorldGenRegion.java \
      platforms/neoforge/src/main/java/com/pg85/otg/neoforge/gen/NeoForgeWorldGenRegion.java
git commit -m "feat: wire command registration on Fabric and NeoForge with CommandWorldAccessor"
```

---

## Task 4: SpawnCommand

**Files:**
- Create: `platforms/shared/src/main/java/com/pg85/otg/shared/commands/SpawnCommand.java`
- Modify: `platforms/shared/src/main/java/com/pg85/otg/shared/commands/OTGCommandRegistrar.java`

### Step 1: Implement SpawnCommand

Reference: `platforms/forge/.../commands/SpawnCommand.java` for BO3 vs BO4 logic.

Key behavior:
- `/otg spawn <preset> <object> [rotation]`
- `preset` arg with tab completion from loaded presets
- `object` arg with tab completion from `getGlobalObjectNames()` + `getAllBONamesForPreset()`
- `rotation` optional: NORTH (default), SOUTH, EAST, WEST
- Raycast from player eye position to find target block (use `level.clip()`)
- For BO3: `spawnForced()` at raycast hit
- For BO4 structures: find unpopulated chunks nearby and plot via structure cache

```java
package com.pg85.otg.shared.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.pg85.otg.OTG;
import com.pg85.otg.customobject.CustomObject;
import com.pg85.otg.customobject.CustomObjectManager;
import com.pg85.otg.customobject.bo4.BO4;
import com.pg85.otg.customobject.structures.CustomStructureCache;
import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.util.gen.LocalWorldGenRegion;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SpawnCommand {

    private static final SuggestionProvider<CommandSourceStack> PRESET_SUGGESTIONS = (ctx, builder) -> {
        var presetLoader = OTG.getEngine().getPresetLoader();
        return SharedSuggestionProvider.suggest(
            presetLoader.getAllPresetFolderNames(), builder
        );
    };

    private static final SuggestionProvider<CommandSourceStack> OBJECT_SUGGESTIONS = (ctx, builder) -> {
        String preset = StringArgumentType.getString(ctx, "preset");
        var collection = OTG.getEngine().getCustomObjectManager().getGlobalObjects();
        var otgRootPath = OTG.getEngine().getOTGRootFolder();
        List<String> names = new ArrayList<>();
        var globalNames = collection.getGlobalObjectNames(otgRootPath);
        if (globalNames != null) names.addAll(globalNames);
        var presetNames = collection.getAllBONamesForPreset(preset, otgRootPath);
        if (presetNames != null) names.addAll(presetNames);
        return SharedSuggestionProvider.suggest(names, builder);
    };

    private static final SuggestionProvider<CommandSourceStack> ROTATION_SUGGESTIONS = (ctx, builder) ->
        SharedSuggestionProvider.suggest(new String[]{"NORTH", "SOUTH", "EAST", "WEST"}, builder);

    public static void register(LiteralArgumentBuilder<CommandSourceStack> otgCommand) {
        otgCommand.then(
            Commands.literal("spawn")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("preset", StringArgumentType.string())
                    .suggests(PRESET_SUGGESTIONS)
                    .then(Commands.argument("object", StringArgumentType.string())
                        .suggests(OBJECT_SUGGESTIONS)
                        .executes(ctx -> execute(ctx, "NORTH"))
                        .then(Commands.argument("rotation", StringArgumentType.string())
                            .suggests(ROTATION_SUGGESTIONS)
                            .executes(ctx -> execute(ctx, StringArgumentType.getString(ctx, "rotation")))
                        )
                    )
                )
        );
    }

    private static int execute(CommandContext<CommandSourceStack> ctx, String rotationStr) {
        CommandSourceStack source = ctx.getSource();
        ServerLevel level = source.getLevel();
        String presetName = StringArgumentType.getString(ctx, "preset");
        String objectName = StringArgumentType.getString(ctx, "object");

        Rotation rotation;
        try {
            rotation = Rotation.valueOf(rotationStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            source.sendFailure(Component.literal("Invalid rotation: " + rotationStr + ". Use NORTH, SOUTH, EAST, or WEST."));
            return 0;
        }

        // Find the object
        CustomObjectManager manager = OTG.getEngine().getCustomObjectManager();
        CustomObject object = manager.getGlobalObjects().getObjectByName(
            objectName, presetName, OTG.getEngine().getOTGRootFolder(),
            manager, OTG.getEngine().getPresetLoader().getMaterialReader(presetName),
            OTG.getEngine().getCustomObjectResourcesManager(),
            OTG.getEngine().getModLoadedChecker()
        );

        if (object == null) {
            source.sendFailure(Component.literal("Object '" + objectName + "' not found. Check the name and preset."));
            return 0;
        }

        // Raycast to find target block
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("This command can only be run by a player."));
            return 0;
        }

        Vec3 eyePos = player.getEyePosition(1.0f);
        Vec3 lookVec = player.getViewVector(1.0f);
        Vec3 endPos = eyePos.add(lookVec.scale(200.0));
        BlockHitResult hit = level.clip(new ClipContext(
            eyePos, endPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player
        ));

        if (hit.getType() == HitResult.Type.MISS) {
            source.sendFailure(Component.literal("No block in range. Look at a block to place the object."));
            return 0;
        }

        int x = hit.getBlockPos().getX();
        int y = hit.getBlockPos().getY() + 1; // Place on top of hit block
        int z = hit.getBlockPos().getZ();

        // Create command-time world region
        CommandWorldAccessor accessor = OTGCommandRegistrar.getWorldAccessor();
        LocalWorldGenRegion region = accessor.createCommandRegion(level, x >> 4, z >> 4);
        if (region == null) {
            source.sendFailure(Component.literal("This world is not using OTG. Cannot spawn objects here."));
            return 0;
        }

        CustomStructureCache structureCache = accessor.getStructureCache(level);

        // Spawn the object
        boolean success = object.spawnForced(
            structureCache, region, new Random(), rotation, x, y, z, false
        );

        if (success) {
            source.sendSuccess(
                () -> Component.literal("Spawned '" + objectName + "' at " + x + ", " + y + ", " + z + " (rotation: " + rotation + ")"),
                true
            );
            return 1;
        } else {
            source.sendFailure(Component.literal("Failed to spawn '" + objectName + "' at " + x + ", " + y + ", " + z));
            return 0;
        }
    }
}
```

**Important notes for implementation:**
- Check that `OTG.getEngine().getPresetLoader().getAllPresetFolderNames()` exists. If not, find the equivalent method.
- Check that `OTG.getEngine().getPresetLoader().getMaterialReader(presetName)` exists. If not, find the equivalent.
- Check that `OTG.getEngine().getModLoadedChecker()` exists. If not, find the equivalent.
- The old Forge SpawnCommand had special BO4 handling (plotting structures in unpopulated chunks). Start with BO3 spawning. BO4 plotting can be added later if needed.

### Step 2: Register SpawnCommand in OTGCommandRegistrar

Add to `register()` method:
```java
SpawnCommand.register(otgCommand);
```

### Step 3: Build and verify

Run: `./gradlew build`
Expected: Compilation succeeds. Fix any API mismatches (method names, parameters).

### Step 4: Commit

```bash
git add platforms/shared/src/main/java/com/pg85/otg/shared/commands/SpawnCommand.java \
      platforms/shared/src/main/java/com/pg85/otg/shared/commands/OTGCommandRegistrar.java
git commit -m "feat: add /otg spawn command for BO2/BO3/BO4 objects"
```

---

## Task 5: StructureCommand

**Files:**
- Create: `platforms/shared/src/main/java/com/pg85/otg/shared/commands/StructureCommand.java`
- Modify: `platforms/shared/src/main/java/com/pg85/otg/shared/commands/OTGCommandRegistrar.java`

### Step 1: Implement StructureCommand

Reference: `platforms/forge/.../commands/StructureCommand.java`

The old Forge version gets structure info from `CustomStructureCache` at the player's coordinates. It shows name, author, description, and branches.

```java
package com.pg85.otg.shared.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.pg85.otg.customobject.structures.CustomStructureCache;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

public class StructureCommand {

    public static void register(LiteralArgumentBuilder<CommandSourceStack> otgCommand) {
        otgCommand.then(
            Commands.literal("structure")
                .executes(ctx -> {
                    CommandSourceStack source = ctx.getSource();
                    ServerLevel level = source.getLevel();
                    BlockPos pos = BlockPos.containing(source.getPosition());

                    CommandWorldAccessor accessor = OTGCommandRegistrar.getWorldAccessor();
                    CustomStructureCache cache = accessor.getStructureCache(level);

                    if (cache == null) {
                        source.sendFailure(Component.literal("This world is not using OTG."));
                        return 0;
                    }

                    // Get structure info at player coordinates
                    // Check CustomStructureCache for getStructureInfoAt or similar method
                    // The old code used: world.getWorldSession().getStructureInfoAt(x, z)
                    // In 1.21.1, check what methods CustomStructureCache exposes for querying
                    // spawned structures at a coordinate.
                    //
                    // TODO: Verify exact API - the method name may differ in 1.21.1
                    // Fallback: iterate cache entries to find structures containing this chunk

                    source.sendSuccess(
                        () -> Component.literal("Structure info at " + pos.getX() + ", " + pos.getZ() + ": [implement lookup]"),
                        false
                    );
                    return 1;
                })
        );
    }
}
```

**Implementation note:** The exact API for querying structure info from `CustomStructureCache` needs investigation. Check what methods it exposes. The old Forge code used `world.getWorldSession().getStructureInfoAt()`. In 1.21.1, this might be on `CustomStructureCache` directly or via a different path. Search for `getStructureInfoAt` or `getBo4StructureAt` in the codebase.

### Step 2: Register in OTGCommandRegistrar

Add: `StructureCommand.register(otgCommand);`

### Step 3: Build, verify, commit

```bash
git commit -m "feat: add /otg structure command"
```

---

## Task 6: ExportCommand (WorldEdit Integration)

**Files:**
- Create: `platforms/shared/src/main/java/com/pg85/otg/shared/commands/ExportCommand.java`
- Modify: `platforms/shared/src/main/java/com/pg85/otg/shared/commands/OTGCommandRegistrar.java`

### Step 1: Implement ExportCommand

Reference: `platforms/forge/.../commands/ExportCommand.java`

Key complexity:
- WorldEdit is an **optional runtime dependency** — use reflection to check availability
- Supports flags: `-a` (air), `-t` (tile entities), `-o` (overwrite), `-b` (force branch/structure), `-bo4` (BO4 format)
- Uses `ObjectCreator.create()` from `common-customobject`

```java
package com.pg85.otg.shared.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.pg85.otg.OTG;
import com.pg85.otg.customobject.creator.ObjectCreator;
import com.pg85.otg.customobject.creator.ObjectType;
import com.pg85.otg.customobject.structures.StructuredCustomObject;
import com.pg85.otg.customobject.util.Corner;
import com.pg85.otg.util.gen.LocalWorldGenRegion;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.nio.file.Path;
import java.util.ArrayList;

public class ExportCommand {

    public static void register(LiteralArgumentBuilder<CommandSourceStack> otgCommand) {
        otgCommand.then(
            Commands.literal("export")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("name", StringArgumentType.string())
                    .then(Commands.argument("preset", StringArgumentType.string())
                        // TODO: Add suggestion providers for presets
                        .then(Commands.argument("flags", StringArgumentType.greedyString())
                            .executes(ctx -> execute(ctx, StringArgumentType.getString(ctx, "flags")))
                        )
                        .executes(ctx -> execute(ctx, ""))
                    )
                )
        );
    }

    private static int execute(CommandContext<CommandSourceStack> ctx, String flagsStr) {
        CommandSourceStack source = ctx.getSource();
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("This command can only be run by a player."));
            return 0;
        }

        String objectName = StringArgumentType.getString(ctx, "name");
        String presetName = StringArgumentType.getString(ctx, "preset");
        boolean includeAir = flagsStr.contains("-a");
        boolean overwrite = flagsStr.contains("-o");
        boolean forceStructure = flagsStr.contains("-b");
        boolean bo4Format = flagsStr.contains("-bo4");
        ObjectType type = bo4Format ? ObjectType.BO4 : ObjectType.BO3;

        // Check WorldEdit availability
        Corner min, max;
        try {
            Class.forName("com.sk89q.worldedit.WorldEdit");
            // WorldEdit is available - get selection
            // Use WorldEdit API to get player's selection region
            // This requires careful integration - see implementation notes below
            source.sendFailure(Component.literal("WorldEdit export not yet fully implemented."));
            return 0;
        } catch (ClassNotFoundException e) {
            source.sendFailure(Component.literal("WorldEdit is required for /otg export. Install WorldEdit or FAWE."));
            return 0;
        }

        // After getting min/max from WorldEdit:
        // ServerLevel level = source.getLevel();
        // CommandWorldAccessor accessor = OTGCommandRegistrar.getWorldAccessor();
        // LocalWorldGenRegion region = accessor.createCommandRegion(level, ...);
        // LocalNBTHelper nbtHelper = accessor.createNBTHelper();
        //
        // Path exportPath = OTG.getEngine().getGlobalObjectsDirectory();
        // Path existingFile = type.getObjectFilePathFromName(objectName, exportPath);
        // if (existingFile.toFile().exists() && !overwrite) {
        //     source.sendFailure("File exists. Use -o to overwrite.");
        //     return 0;
        // }
        //
        // Corner center = new Corner(player.getBlockX(), player.getBlockY(), player.getBlockZ());
        // StructuredCustomObject result = ObjectCreator.create(
        //     type, min, max, center, null, objectName, includeAir,
        //     forceStructure, false, exportPath, region, nbtHelper,
        //     null, null, presetName, OTG.getEngine().getOTGRootFolder(),
        //     OTG.getEngine().getCustomObjectManager(),
        //     OTG.getEngine().getPresetLoader().getMaterialReader(presetName),
        //     OTG.getEngine().getCustomObjectResourcesManager(),
        //     OTG.getEngine().getModLoadedChecker()
        // );
        //
        // if (result != null) {
        //     OTG.getEngine().getCustomObjectManager().reloadCustomObjectFiles();
        //     source.sendSuccess(() -> Component.literal("Exported '" + objectName + "' as " + type.getType()), true);
        //     return 1;
        // }
    }
}
```

**WorldEdit integration notes:**
The WorldEdit selection retrieval requires platform-specific code because WorldEdit's Fabric adapter uses different player wrapping. The general pattern is:

```java
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.fabric.FabricAdapter; // Fabric-specific
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.SessionManager;

// Get selection
var wePlayer = FabricAdapter.adaptPlayer(player); // Platform-specific!
var session = WorldEdit.getInstance().getSessionManager().get(wePlayer);
var selection = session.getSelection(wePlayer.getWorld());
BlockVector3 weMin = selection.getMinimumPoint();
BlockVector3 weMax = selection.getMaximumPoint();
Corner min = new Corner(weMin.x(), weMin.y(), weMin.z());
Corner max = new Corner(weMax.x(), weMax.y(), weMax.z());
```

Since `FabricAdapter` is Fabric-specific, either:
1. Add WorldEdit selection to `CommandWorldAccessor` interface
2. Use reflection to avoid compile-time dependency
3. Accept coordinates as arguments instead of WorldEdit (alternative approach)

**Recommended: Add to CommandWorldAccessor:**
```java
Corner[] getWorldEditSelection(ServerPlayer player); // returns [min, max] or null
```

### Step 2: Register, build, verify, commit

```bash
git commit -m "feat: add /otg export command skeleton with WorldEdit detection"
```

---

## Task 7: ExportBO4DataCommand

**Files:**
- Create: `platforms/shared/src/main/java/com/pg85/otg/shared/commands/ExportBO4DataCommand.java`
- Modify: `platforms/shared/src/main/java/com/pg85/otg/shared/commands/OTGCommandRegistrar.java`

### Step 1: Implement ExportBO4DataCommand

Reference: `platforms/forge/.../commands/ExportBO4DataCommand.java`

This runs as a background thread, iterating all BO4s and generating optimized `.BO4Data` files.

```java
package com.pg85.otg.shared.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.pg85.otg.OTG;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class ExportBO4DataCommand {

    public static void register(LiteralArgumentBuilder<CommandSourceStack> otgCommand) {
        otgCommand.then(
            Commands.literal("exportbo4data")
                .requires(source -> source.hasPermission(2))
                .executes(ctx -> {
                    CommandSourceStack source = ctx.getSource();

                    // Reference the old Forge ExportBO4DataCommand for the full logic:
                    // 1. Get all presets
                    // 2. For each preset, get all biome configs
                    // 3. For each biome, get CustomStructureGen resources
                    // 4. For each BO4 structure start, call getMinimumSize() then generateBO4Data()
                    // 5. Unload between exports to manage memory
                    // 6. Report progress

                    source.sendSuccess(
                        () -> Component.literal("ExportBO4Data: Starting export..."),
                        true
                    );

                    // Run in background thread
                    new Thread(() -> {
                        try {
                            // TODO: Port the export logic from old Forge ExportBO4DataCommand
                            // Key APIs:
                            // - OTG.getEngine().getPresetLoader()
                            // - preset.getPresetConfig().getResourceSettings()
                            // - OTG.generateBO4Data()
                            // - manager.getGlobalObjects().unloadCustomObjectFiles()
                        } catch (Exception e) {
                            source.sendFailure(Component.literal("ExportBO4Data failed: " + e.getMessage()));
                        }
                    }, "OTG-ExportBO4Data").start();

                    return 1;
                })
        );
    }
}
```

**Implementation note:** The full export logic is complex. Port it method-by-method from the old Forge `ExportBO4DataCommand`. The core loop is:
1. Get preset biome configs
2. For each `CustomStructureGen` resource, get the BO4 start object
3. Call `structure.getMinimumSize()` to compute structure data
4. Call `OTG.generateBO4Data()` to write the `.BO4Data` file
5. Unload objects between exports

### Step 2: Register, build, verify, commit

```bash
git commit -m "feat: add /otg exportbo4data command skeleton"
```

---

## Task 8: Migrate Dimension Commands to Shared (Optional)

**Files:**
- Create: `platforms/shared/src/main/java/com/pg85/otg/shared/commands/DimensionCommand.java`
- Modify: `platforms/fabric/.../dimensions/FabricDimensionCommands.java` (extract shared logic)
- Modify: `platforms/neoforge/.../dimensions/NeoForgeDimensionCommands.java` (extract shared logic)
- Modify: `platforms/shared/.../commands/OTGCommandRegistrar.java`

This task consolidates all `/otg` commands under a single Brigadier tree. Currently dimension commands register separately. Move the shared logic to `DimensionCommand.java` in shared, keeping platform-specific dimension manager access via `CommandWorldAccessor` (add dimension methods to the interface).

**This is optional and can be done in a separate branch.** The BO commands work independently of this migration.

### Step 1: Add dimension management methods to CommandWorldAccessor

```java
// In CommandWorldAccessor interface, add:
Object getDimensionManager(ServerLevel level); // returns platform DimensionManager
```

### Step 2: Extract shared dimension logic, register, build, verify, commit

---

## Task 9: Build Verification and Polish

### Step 1: Full build

Run: `./gradlew clean build` from worktree root.
Expected: Both Fabric and NeoForge JARs build successfully.

### Step 2: Verify JAR contents

```bash
jar tf build/distributions/otg-fabric-*.jar | grep -i command
```

Expected: All command classes present in the JAR.

### Step 3: Review all commands registered

Verify `OTGCommandRegistrar.register()` calls all commands:
- FlushCacheCommand.register()
- SpawnCommand.register()
- StructureCommand.register()
- ExportCommand.register()
- ExportBO4DataCommand.register()

### Step 4: Final commit

```bash
git commit -m "feat: complete BO2/3/4 command infrastructure for OTG 1.21.1"
```

---

## Implementation Priority

1. **Task 1-3** (Infrastructure + FlushCache + Platform Hooks) — Foundation, must work first
2. **Task 4** (SpawnCommand) — Most important command for testing objects in-game
3. **Task 5** (StructureCommand) — Useful diagnostic tool
4. **Task 6** (ExportCommand) — Complex due to WorldEdit, can be a stub initially
5. **Task 7** (ExportBO4DataCommand) — Specialized tool, lower priority
6. **Task 8** (Dimension migration) — Optional cleanup

## Known Risks

1. **Brigadier access in shared module** — Should work (Minecraft transitive dep), but verify with a build early
2. **WorldGenRegion at command-time** — The existing constructors expect chunk-gen context. Using `ServerLevel` as `WorldGenLevel` should work since `ServerLevel implements WorldGenLevel`, but block placement behavior may differ from chunk-gen time (lighting updates, neighbor notifications, etc.)
3. **WorldEdit API** — Optional runtime dep. The `FabricAdapter` class is Fabric-specific, so either add WE selection to `CommandWorldAccessor` or use reflection
4. **Thread safety** — `ExportBO4DataCommand` runs on a background thread. Ensure `CustomObjectManager` operations are thread-safe (they use `synchronized(indexingFilesLock)`)
5. **BO4 structure plotting** — The old `structureCache.plotBo4Structure()` may have changed. Verify API availability in 1.21.1 `CustomStructureCache`
