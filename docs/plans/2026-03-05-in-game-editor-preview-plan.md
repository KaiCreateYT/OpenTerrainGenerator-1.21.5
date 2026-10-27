# In-Game OTG Editor & Preview — Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Build an in-game terrain and BO3/BO4 preview system accessible from Minecraft's Title Screen, using MC's native block rendering for pixel-perfect visualization.

**Architecture:** Client source sets added to existing platform modules. TempServerManager spins up IntegratedServer for chunk generation. PreviewWorld (BlockAndTintGetter) feeds BlockRenderDispatcher for mesh compilation. PreviewScreen renders compiled meshes with orbit camera.

**Tech Stack:** Minecraft 1.21.1, Mojang mappings, Architectury Loom, Fabric/NeoForge client APIs, OpenGL via MC's RenderSystem.

**Design doc:** `docs/plans/2026-03-05-in-game-editor-preview-design.md`

**Important:** OTG uses Mojang mappings (mojmap), NOT Yarn. All MC class references in this plan use mojmap names.

**Testing:** No automated tests — all verification is manual in-game. Launch the game, check that it works.

---

## Task 1: Gradle — Add Client Source Sets

**Files:**
- Modify: `platforms/shared/build.gradle.kts`
- Modify: `platforms/fabric/build.gradle.kts`
- Modify: `platforms/neoforge/build.gradle.kts`
- Modify: `platforms/fabric/src/main/resources/fabric.mod.json`
- Modify: `platforms/neoforge/src/main/resources/META-INF/neoforge.mods.toml`
- Create: `platforms/shared/src/client/java/.gitkeep`
- Create: `platforms/fabric/src/client/java/.gitkeep`
- Create: `platforms/neoforge/src/client/java/.gitkeep`
- Create: `platforms/shared/src/client/resources/otg-shared-client.mixins.json`

**Step 1: Enable Architectury Loom client source sets**

In `platforms/shared/build.gradle.kts`, add after the `architectury` block:

```kotlin
loom {
    splitEnvironmentSourceSets()
}
```

This creates `src/client/java` and `src/client/resources` source sets that only compile with client-side MC classes available.

Do the same in `platforms/fabric/build.gradle.kts` and `platforms/neoforge/build.gradle.kts`.

**Step 2: Create client source directories**

```bash
mkdir -p platforms/shared/src/client/java/com/pg85/otg/client
mkdir -p platforms/shared/src/client/resources
mkdir -p platforms/fabric/src/client/java/com/pg85/otg/fabric/client
mkdir -p platforms/neoforge/src/client/java/com/pg85/otg/neoforge/client
```

**Step 3: Create empty client mixin config**

Create `platforms/shared/src/client/resources/otg-shared-client.mixins.json`:

```json
{
  "required": true,
  "minVersion": "0.8",
  "package": "com.pg85.otg.client.mixin",
  "compatibilityLevel": "JAVA_21",
  "client": [],
  "injectors": {
    "defaultRequire": 1
  }
}
```

**Step 4: Register client mixin config in platform manifests**

In `fabric.mod.json`, add to the `mixins` array:
```json
"otg-shared-client.mixins.json"
```

In `neoforge.mods.toml`, add:
```toml
[[mixins]]
config = "otg-shared-client.mixins.json"
```

**Step 5: Verify build**

```bash
./gradlew build
```

Expected: Build succeeds. No functional changes yet.

**Step 6: Commit**

```bash
git add -A
git commit -m "build: add client source sets to platform modules"
```

---

## Task 2: Title Screen Button + Empty PreviewScreen

**Files:**
- Create: `platforms/shared/src/client/java/com/pg85/otg/client/preview/PreviewScreen.java`
- Create: `platforms/shared/src/client/java/com/pg85/otg/client/mixin/TitleScreenMixin.java`
- Modify: `platforms/shared/src/client/resources/otg-shared-client.mixins.json`

**Step 1: Create minimal PreviewScreen**

```java
package com.pg85.otg.client.preview;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class PreviewScreen extends Screen {

    public PreviewScreen() {
        super(Component.literal("OTG Editor"));
    }

    @Override
    protected void init() {
        // Back button
        addRenderableWidget(Button.builder(
            Component.literal("Back"),
            btn -> minecraft.setScreen(null)  // null = back to title
        ).bounds(width / 2 - 50, height - 30, 100, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, 15, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);
    }
}
```

**Step 2: Create TitleScreenMixin**

```java
package com.pg85.otg.client.mixin;

import com.pg85.otg.client.preview.PreviewScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    protected TitleScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void otg$addEditorButton(CallbackInfo ci) {
        addRenderableWidget(Button.builder(
            Component.literal("OTG Editor"),
            btn -> minecraft.setScreen(new PreviewScreen())
        ).bounds(width / 2 - 50, height / 4 + 132, 100, 20).build());
    }
}
```

**Step 3: Register mixin**

Update `otg-shared-client.mixins.json`:

```json
{
  "required": true,
  "minVersion": "0.8",
  "package": "com.pg85.otg.client.mixin",
  "compatibilityLevel": "JAVA_21",
  "client": [
    "TitleScreenMixin"
  ],
  "injectors": {
    "defaultRequire": 1
  }
}
```

**Step 4: Test in-game**

```bash
./gradlew build
```

Launch MC. On title screen, "OTG Editor" button should appear. Clicking opens empty PreviewScreen with "Back" button.

**Step 5: Commit**

```bash
git commit -m "feat: add OTG Editor button on title screen with empty PreviewScreen"
```

---

## Task 3: OrbitCamera

**Files:**
- Create: `platforms/shared/src/client/java/com/pg85/otg/client/preview/OrbitCamera.java`

**Step 1: Implement OrbitCamera**

Ported from ReEdited's OrbitCamera.cs, adapted to MC's matrix system:

```java
package com.pg85.otg.client.preview;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class OrbitCamera {

    private float theta;      // azimuth (radians)
    private float phi;        // elevation (radians)
    private float distance;   // radius from target
    private final Vector3f target = new Vector3f();

    private static final float PHI_MIN = 0.1f;
    private static final float PHI_MAX = (float) (Math.PI - 0.1);
    private static final float DIST_MIN = 1f;
    private static final float DIST_MAX = 5000f;
    private static final float FOV = (float) Math.toRadians(45);

    public OrbitCamera() {
        theta = (float) (Math.PI / 4);
        phi = (float) (Math.PI / 3);
        distance = 100f;
    }

    public void rotate(float dTheta, float dPhi) {
        theta += dTheta;
        phi = Math.clamp(phi + dPhi, PHI_MIN, PHI_MAX);
    }

    public void zoom(float delta) {
        distance = Math.clamp(distance * (1f - delta * 0.001f), DIST_MIN, DIST_MAX);
    }

    public void fitTo(Vector3f center, float radius) {
        target.set(center);
        distance = Math.clamp(radius / (float) Math.sin(FOV / 2), DIST_MIN, DIST_MAX);
    }

    public Vector3f getEyePosition() {
        float x = target.x + distance * (float)(Math.sin(phi) * Math.cos(theta));
        float y = target.y + distance * (float)(Math.cos(phi));
        float z = target.z + distance * (float)(Math.sin(phi) * Math.sin(theta));
        return new Vector3f(x, y, z);
    }

    public Matrix4f getViewMatrix() {
        Vector3f eye = getEyePosition();
        return new Matrix4f().lookAt(eye, target, new Vector3f(0, 1, 0));
    }

    public Matrix4f getProjectionMatrix(float aspectRatio) {
        return new Matrix4f().perspective(FOV, aspectRatio, 0.1f, 10000f);
    }

    public Matrix4f getViewProjectionMatrix(float aspectRatio) {
        return getProjectionMatrix(aspectRatio).mul(getViewMatrix());
    }
}
```

**Step 2: Commit**

```bash
git commit -m "feat: add OrbitCamera with spherical coordinates for preview"
```

---

## Task 4: PreviewWorld (BlockAndTintGetter)

**Files:**
- Create: `platforms/shared/src/client/java/com/pg85/otg/client/preview/world/PreviewChunk.java`
- Create: `platforms/shared/src/client/java/com/pg85/otg/client/preview/world/PreviewWorld.java`

**Step 1: Implement PreviewChunk**

Stores block and biome data copied from a generated ChunkAccess:

```java
package com.pg85.otg.client.preview.world;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class PreviewChunk {

    private final int minY;
    private final int height;
    private final BlockState[] blocks;        // flat array: [x + z*16 + (y-minY)*256]
    private final Holder<Biome>[] biomes;     // quarter-res: [bx + bz*4 + by*16]

    public PreviewChunk(int minY, int height) {
        this.minY = minY;
        this.height = height;
        this.blocks = new BlockState[16 * 16 * height];
        this.biomes = new Holder[4 * 4 * (height / 4)];
        java.util.Arrays.fill(blocks, Blocks.AIR.defaultBlockState());
    }

    public BlockState getBlockState(int x, int y, int z) {
        int ry = y - minY;
        if (x < 0 || x >= 16 || z < 0 || z >= 16 || ry < 0 || ry >= height) {
            return Blocks.AIR.defaultBlockState();
        }
        return blocks[x + z * 16 + ry * 256];
    }

    public void setBlockState(int x, int y, int z, BlockState state) {
        int ry = y - minY;
        if (x >= 0 && x < 16 && z >= 0 && z < 16 && ry >= 0 && ry < height) {
            blocks[x + z * 16 + ry * 256] = state;
        }
    }

    public Holder<Biome> getBiome(int x, int y, int z) {
        int bx = (x & 15) >> 2;
        int bz = (z & 15) >> 2;
        int by = (y - minY) >> 2;
        by = Math.clamp(by, 0, (height / 4) - 1);
        int idx = bx + bz * 4 + by * 16;
        return idx >= 0 && idx < biomes.length && biomes[idx] != null
            ? biomes[idx] : null;
    }

    public void setBiome(int bx, int by, int bz, Holder<Biome> biome) {
        int idx = bx + bz * 4 + by * 16;
        if (idx >= 0 && idx < biomes.length) {
            biomes[idx] = biome;
        }
    }

    public int getMinY() { return minY; }
    public int getHeight() { return height; }
}
```

**Step 2: Implement PreviewWorld**

Implements `BlockAndTintGetter` for MC's `BlockRenderDispatcher`:

```java
package com.pg85.otg.client.preview.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.HashMap;
import java.util.Map;

public class PreviewWorld implements BlockAndTintGetter {

    private final Map<Long, PreviewChunk> chunks = new HashMap<>();
    private int minY = -64;
    private int maxY = 320;

    public void addChunkFromAccess(ChunkAccess access) {
        int cx = access.getPos().x;
        int cz = access.getPos().z;
        int chunkMinY = access.getMinBuildHeight();
        int chunkHeight = access.getHeight();
        PreviewChunk chunk = new PreviewChunk(chunkMinY, chunkHeight);

        // Copy block states
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = chunkMinY; y < chunkMinY + chunkHeight; y++) {
                    BlockPos pos = new BlockPos(cx * 16 + x, y, cz * 16 + z);
                    chunk.setBlockState(x, y, z, access.getBlockState(pos));
                }
            }
        }

        // Copy biomes (quarter resolution)
        for (int bx = 0; bx < 4; bx++) {
            for (int bz = 0; bz < 4; bz++) {
                for (int by = 0; by < chunkHeight / 4; by++) {
                    // QuartPos: biome stored at 4-block intervals
                    int worldY = chunkMinY + by * 4;
                    BlockPos pos = new BlockPos(cx * 16 + bx * 4, worldY, cz * 16 + bz * 4);
                    Holder<Biome> biome = access.getNoiseBiome(
                        bx + cx * 4, by + (chunkMinY >> 2), bz + cz * 4
                    );
                    chunk.setBiome(bx, by, bz, biome);
                }
            }
        }

        chunks.put(chunkKey(cx, cz), chunk);
        minY = Math.min(minY, chunkMinY);
        maxY = Math.max(maxY, chunkMinY + chunkHeight);
    }

    public void clear() {
        chunks.clear();
    }

    public boolean hasChunk(int cx, int cz) {
        return chunks.containsKey(chunkKey(cx, cz));
    }

    private PreviewChunk getChunk(BlockPos pos) {
        return chunks.get(chunkKey(pos.getX() >> 4, pos.getZ() >> 4));
    }

    private static long chunkKey(int cx, int cz) {
        return ((long) cx << 32) | (cz & 0xFFFFFFFFL);
    }

    // --- BlockAndTintGetter implementation ---

    @Override
    public BlockState getBlockState(BlockPos pos) {
        PreviewChunk chunk = getChunk(pos);
        if (chunk == null) return Blocks.AIR.defaultBlockState();
        return chunk.getBlockState(pos.getX() & 15, pos.getY(), pos.getZ() & 15);
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return getBlockState(pos).getFluidState();
    }

    @Override
    public int getHeight() {
        return maxY - minY;
    }

    @Override
    public int getMinBuildHeight() {
        return minY;
    }

    @Override
    public float getShade(Direction direction, boolean shade) {
        // MC's standard directional shading
        return switch (direction) {
            case DOWN -> 0.5f;
            case NORTH, SOUTH -> 0.8f;
            case EAST, WEST -> 0.6f;
            case UP -> 1.0f;
        };
    }

    @Override
    public LevelLightEngine getLightEngine() {
        // Return null — we handle lighting via getShade() only
        // BlockRenderDispatcher uses getShade() for face brightness
        // and block/sky light for per-vertex light
        throw new UnsupportedOperationException("PreviewWorld has no light engine");
    }

    @Override
    public int getBrightness(LightLayer layer, BlockPos pos) {
        // Full brightness everywhere — fake sky light
        return 15;
    }

    @Override
    public int getRawBrightness(BlockPos pos, int ambientDarkening) {
        return 15;
    }

    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        return null;
    }

    @Override
    public int getBlockTint(BlockPos pos, ColorResolver colorResolver) {
        // For biome-tinted blocks (grass, leaves, water)
        PreviewChunk chunk = getChunk(pos);
        if (chunk == null) return -1;
        Holder<Biome> biome = chunk.getBiome(pos.getX() & 15, pos.getY(), pos.getZ() & 15);
        if (biome == null) return -1;
        return colorResolver.getColor(biome.value(), pos.getX(), pos.getZ());
    }
}
```

**Note:** The exact `BlockAndTintGetter` interface may require additional method stubs depending on MC 1.21.1 internals. If build fails with unimplemented methods, add stubs that return sensible defaults (null for BlockEntity, 15 for light, AIR for out-of-bounds blocks).

**Step 3: Commit**

```bash
git commit -m "feat: add PreviewWorld and PreviewChunk as BlockAndTintGetter for rendering"
```

---

## Task 5: TempServerManager

**Files:**
- Create: `platforms/shared/src/client/java/com/pg85/otg/client/preview/world/TempServerManager.java`

**Step 1: Implement TempServerManager**

This is the trickiest part. MC's `Minecraft.createWorldOpenFlows()` and `WorldOpenFlows` handle world creation. We need to replicate the singleplayer start flow in a headless way.

Strategy: Use `Minecraft.getInstance()` to access the world creation pipeline. Create a temp world with the selected OTG preset, let MC spin up IntegratedServer normally, grab the ServerLevel, generate chunks, then tear down.

```java
package com.pg85.otg.client.preview.world;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class TempServerManager {

    private final AtomicReference<MinecraftServer> server = new AtomicReference<>();
    private Path tempWorldDir;

    public boolean isRunning() {
        return server.get() != null;
    }

    public ServerLevel getOverworld() {
        MinecraftServer srv = server.get();
        return srv != null ? srv.getLevel(Level.OVERWORLD) : null;
    }

    /**
     * Start a temporary IntegratedServer with the given OTG preset.
     *
     * Implementation approach:
     * Use Minecraft's existing world creation flow. The key classes are:
     * - WorldOpenFlows (accessible via Minecraft.getInstance().createWorldOpenFlows())
     * - CreateWorldScreen / WorldCreationContext
     *
     * The exact API to programmatically create and start a world with a specific
     * WorldPreset needs to be determined from MC source. The flow is roughly:
     *
     * 1. Create temp save directory
     * 2. Configure WorldCreationContext with OTG WorldPreset
     * 3. Call Minecraft.getInstance().createWorldOpenFlows().createFreshLevel(...)
     * 4. Wait for server to start (poll Minecraft.getInstance().getSingleplayerServer())
     * 5. Store reference
     *
     * This is a skeleton — exact implementation depends on MC 1.21.1 internals
     * that must be verified at implementation time by reading MC source.
     */
    public CompletableFuture<ServerLevel> startServer(String presetName, long seed,
                                                       Consumer<String> statusCallback) {
        return CompletableFuture.supplyAsync(() -> {
            statusCallback.accept("Starting preview server...");

            // Create temp directory for world
            try {
                tempWorldDir = Files.createTempDirectory("otg-preview-");
            } catch (IOException e) {
                throw new RuntimeException("Failed to create temp directory", e);
            }

            // TODO: Use MC's world creation API to start IntegratedServer
            // with the specified OTG preset and seed.
            // This requires reading MC 1.21.1 source for:
            // - WorldOpenFlows.createFreshLevel()
            // - WorldCreationContext setup with custom WorldPreset
            // - Programmatic equivalent of Create World screen "Create" button
            //
            // Key MC classes to examine:
            // - net.minecraft.client.gui.screens.worldselection.CreateWorldScreen
            // - net.minecraft.client.gui.screens.worldselection.WorldOpenFlows
            // - net.minecraft.client.gui.screens.worldselection.WorldCreationContext
            // - net.minecraft.server.WorldLoader

            throw new UnsupportedOperationException(
                "TempServerManager.startServer() skeleton — implement with MC world creation API"
            );
        });
    }

    public void stopServer() {
        MinecraftServer srv = server.getAndSet(null);
        if (srv != null) {
            srv.halt(true);
        }
        // Delete temp directory
        if (tempWorldDir != null) {
            deleteTempDir(tempWorldDir);
            tempWorldDir = null;
        }
    }

    private static void deleteTempDir(Path dir) {
        try {
            Files.walkFileTree(dir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }
                @Override
                public FileVisitResult postVisitDirectory(Path d, IOException exc) throws IOException {
                    Files.delete(d);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ignored) {}
    }
}
```

**Important note:** The `startServer()` method is intentionally a skeleton with TODO. The exact MC world creation API calls must be determined at implementation time by reading MC 1.21.1 decompiled source for `CreateWorldScreen`, `WorldOpenFlows`, and related classes. This is the highest-risk task in the project — MC's world creation flow is complex and not designed for programmatic use.

**Alternative approach if IntegratedServer proves too complex:** Generate chunks directly without a server. Create `RegistryAccess` from MC data, instantiate `OTGChunkGenerator` manually, generate `ProtoChunk` objects. This loses FEATURES/STRUCTURES but gives terrain+caves. Can be added as a "quick preview" mode alongside full server preview.

**Step 2: Commit**

```bash
git commit -m "feat: add TempServerManager skeleton for preview server lifecycle"
```

---

## Task 6: ChunkGenerationManager

**Files:**
- Create: `platforms/shared/src/client/java/com/pg85/otg/client/preview/world/ChunkGenerationManager.java`

**Step 1: Implement ChunkGenerationManager**

Requests chunks from the server in spiral order and copies them to PreviewWorld:

```java
package com.pg85.otg.client.preview.world;

import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

public class ChunkGenerationManager {

    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private final AtomicInteger completedChunks = new AtomicInteger(0);
    private int totalChunks;

    /**
     * Generate chunks in spiral order from center and feed them to PreviewWorld.
     *
     * @param level          ServerLevel from TempServerManager
     * @param previewWorld   Target PreviewWorld to populate
     * @param radiusChunks   Radius in chunks (4-32)
     * @param status         Target ChunkStatus (SURFACE, CARVERS, or FULL)
     * @param onProgress     Callback: (completed, total)
     * @param onChunkReady   Callback per completed chunk (for progressive rendering)
     */
    public void generate(ServerLevel level, PreviewWorld previewWorld, int radiusChunks,
                         ChunkStatus status, BiConsumer<Integer, Integer> onProgress,
                         Runnable onChunkReady) {
        cancelled.set(false);
        completedChunks.set(0);

        List<ChunkPos> positions = spiralOrder(radiusChunks);
        totalChunks = positions.size();

        ServerChunkCache chunkCache = level.getChunkSource();

        for (ChunkPos pos : positions) {
            if (cancelled.get()) break;

            // Request chunk generation to target status
            ChunkAccess chunk = chunkCache.getChunk(pos.x, pos.z, status, true);
            if (chunk != null) {
                previewWorld.addChunkFromAccess(chunk);
                int done = completedChunks.incrementAndGet();
                onProgress.accept(done, totalChunks);
                onChunkReady.run();
            }
        }
    }

    public void cancel() {
        cancelled.set(true);
    }

    public float getProgress() {
        return totalChunks > 0 ? (float) completedChunks.get() / totalChunks : 0f;
    }

    /**
     * Generate chunk positions in spiral order from (0,0) outward.
     */
    private static List<ChunkPos> spiralOrder(int radius) {
        List<ChunkPos> result = new ArrayList<>();
        int x = 0, z = 0;
        int dx = 0, dz = -1;
        int side = radius * 2;
        int total = side * side;

        for (int i = 0; i < total; i++) {
            if (-radius < x && x <= radius && -radius < z && z <= radius) {
                result.add(new ChunkPos(x, z));
            }
            if (x == z || (x < 0 && x == -z) || (x > 0 && x == 1 - z)) {
                int temp = dx;
                dx = -dz;
                dz = temp;
            }
            x += dx;
            z += dz;
        }
        return result;
    }
}
```

**Note:** `chunkCache.getChunk()` is synchronous and blocks until the chunk is ready. This must be called from a non-render thread. The `generate()` method should be invoked on a background thread, with `onProgress` and `onChunkReady` callbacks dispatching to the render thread for UI updates.

**Step 2: Commit**

```bash
git commit -m "feat: add ChunkGenerationManager with spiral chunk loading"
```

---

## Task 7: PreviewRenderer — Mesh Compilation

**Files:**
- Create: `platforms/shared/src/client/java/com/pg85/otg/client/preview/PreviewRenderer.java`
- Create: `platforms/shared/src/client/java/com/pg85/otg/client/preview/PreviewSection.java`

**Step 1: Implement PreviewSection**

Holds compiled vertex buffers for one 16x16x16 section:

```java
package com.pg85.otg.client.preview;

import com.mojang.blaze3d.vertex.VertexBuffer;
import net.minecraft.client.renderer.RenderType;

import java.util.HashMap;
import java.util.Map;

public class PreviewSection {

    private final Map<RenderType, VertexBuffer> buffers = new HashMap<>();

    public void setBuffer(RenderType type, VertexBuffer buffer) {
        VertexBuffer old = buffers.put(type, buffer);
        if (old != null) old.close();
    }

    public VertexBuffer getBuffer(RenderType type) {
        return buffers.get(type);
    }

    public void close() {
        buffers.values().forEach(VertexBuffer::close);
        buffers.clear();
    }
}
```

**Step 2: Implement PreviewRenderer**

Compiles sections from PreviewWorld using MC's BlockRenderDispatcher:

```java
package com.pg85.otg.client.preview;

import com.mojang.blaze3d.vertex.*;
import com.pg85.otg.client.preview.world.PreviewWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PreviewRenderer {

    private final Map<Long, PreviewSection> sections = new ConcurrentHashMap<>();
    private final PreviewWorld world;

    public PreviewRenderer(PreviewWorld world) {
        this.world = world;
    }

    /**
     * Compile mesh for a single section. Call on background thread,
     * then upload buffers on render thread.
     */
    public void compileSection(SectionPos sectionPos) {
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        RandomSource random = RandomSource.create();

        // One BufferBuilder per RenderType
        Map<RenderType, BufferBuilder> builders = new HashMap<>();

        int baseX = sectionPos.minBlockX();
        int baseY = sectionPos.minBlockY();
        int baseZ = sectionPos.minBlockZ();

        PoseStack poseStack = new PoseStack();

        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    BlockPos pos = new BlockPos(baseX + x, baseY + y, baseZ + z);
                    BlockState state = world.getBlockState(pos);

                    if (state.isAir()) continue;
                    if (state.getRenderShape() == RenderShape.INVISIBLE) continue;

                    // Determine which RenderType this block uses
                    // MC 1.21.1: ItemBlockRenderTypes.getChunkRenderType(state)
                    RenderType renderType = RenderType.solid(); // TODO: get correct type

                    BufferBuilder builder = builders.computeIfAbsent(renderType, rt ->
                        new BufferBuilder(
                            new ByteBufferBuilder(256 * 1024),
                            VertexFormat.Mode.QUADS,
                            DefaultVertexFormat.BLOCK
                        )
                    );

                    poseStack.pushPose();
                    poseStack.translate(x, baseY + y - world.getMinBuildHeight(), z);

                    dispatcher.renderBatched(
                        state, pos, world, poseStack, builder, true, random
                    );

                    poseStack.popPose();
                }
            }
        }

        // Upload to GPU (must happen on render thread)
        Minecraft.getInstance().execute(() -> {
            PreviewSection section = new PreviewSection();

            for (var entry : builders.entrySet()) {
                MeshData meshData = entry.getValue().build();
                if (meshData != null) {
                    VertexBuffer buffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
                    buffer.bind();
                    buffer.upload(meshData);
                    VertexBuffer.unbind();
                    section.setBuffer(entry.getKey(), buffer);
                }
            }

            sections.put(sectionKey(sectionPos), section);
        });
    }

    /**
     * Compile all sections that have data.
     */
    public void compileAll() {
        int minSection = world.getMinBuildHeight() >> 4;
        int maxSection = (world.getMinBuildHeight() + world.getHeight()) >> 4;

        // TODO: iterate over all loaded chunk positions in PreviewWorld
        // For each chunk column, compile each section that has non-air blocks
    }

    /**
     * Draw all compiled sections for a given render type.
     */
    public void draw(RenderType renderType, PoseStack.Pose pose) {
        for (PreviewSection section : sections.values()) {
            VertexBuffer buffer = section.getBuffer(renderType);
            if (buffer != null) {
                buffer.bind();
                buffer.drawWithShader(
                    pose.pose(),
                    // TODO: projection matrix from camera
                    new org.joml.Matrix4f(),
                    renderType.format()
                );
            }
        }
        VertexBuffer.unbind();
    }

    public void releaseBuffers() {
        sections.values().forEach(PreviewSection::close);
        sections.clear();
    }

    private static long sectionKey(SectionPos pos) {
        return SectionPos.asLong(pos.x(), pos.y(), pos.z());
    }
}
```

**Important notes for implementation:**

1. **RenderType detection**: `ItemBlockRenderTypes.getChunkRenderType(BlockState)` gives the correct RenderType per block. Verify this method exists in 1.21.1 mojmap — it may need access widening.

2. **BufferBuilder API**: The constructor and `build()` return type changed across MC versions. The exact 1.21.1 API must be verified from MC source. Key difference: 1.21.1 uses `MeshData` from `build()` instead of `BufferBuilder.RenderedBuffer`.

3. **PoseStack translation**: Position must be relative to section origin for correct rendering. The exact coordinate system needs tuning at implementation time.

4. **Thread safety**: `compileSection()` reads PreviewWorld (thread-safe reads) and creates BufferBuilders (thread-local). VBO upload dispatched to render thread via `Minecraft.getInstance().execute()`.

**Step 3: Commit**

```bash
git commit -m "feat: add PreviewRenderer for block mesh compilation via BlockRenderDispatcher"
```

---

## Task 8: Wire PreviewScreen Together

**Files:**
- Modify: `platforms/shared/src/client/java/com/pg85/otg/client/preview/PreviewScreen.java`

**Step 1: Build full PreviewScreen**

Wire all components: controls panel, viewport, generation, rendering.

```java
package com.pg85.otg.client.preview;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.pg85.otg.client.preview.world.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.concurrent.CompletableFuture;

public class PreviewScreen extends Screen {

    // Components
    private final PreviewWorld previewWorld = new PreviewWorld();
    private final PreviewRenderer renderer = new PreviewRenderer(previewWorld);
    private final OrbitCamera camera = new OrbitCamera();
    private final TempServerManager serverManager = new TempServerManager();
    private final ChunkGenerationManager chunkManager = new ChunkGenerationManager();

    // UI state
    private String statusText = "Ready";
    private boolean isGenerating = false;
    private long seed = 12345L;
    private int radiusChunks = 4;
    private GenerationLevel generationLevel = GenerationLevel.SURFACE;

    // Controls
    private EditBox seedInput;

    // Viewport bounds
    private int viewportX, viewportY, viewportW, viewportH;
    private static final int PANEL_WIDTH = 140;

    // Mouse drag state
    private boolean dragging = false;

    enum GenerationLevel {
        SURFACE("Surface", ChunkStatus.SURFACE),
        CAVES("Caves", ChunkStatus.CARVERS),
        FULL("Full", ChunkStatus.FULL);

        final String label;
        final ChunkStatus status;
        GenerationLevel(String label, ChunkStatus status) {
            this.label = label;
            this.status = status;
        }
    }

    public PreviewScreen() {
        super(Component.literal("OTG Editor — Preview"));
    }

    @Override
    protected void init() {
        int panelX = 10;
        int y = 40;

        // Seed input
        seedInput = new EditBox(font, panelX, y, PANEL_WIDTH - 20, 20,
            Component.literal("Seed"));
        seedInput.setValue(String.valueOf(seed));
        seedInput.setResponder(s -> {
            try { seed = Long.parseLong(s); } catch (NumberFormatException ignored) {}
        });
        addRenderableWidget(seedInput);
        y += 30;

        // Size buttons (4, 8, 16, 32)
        for (int size : new int[]{4, 8, 16, 32}) {
            addRenderableWidget(Button.builder(
                Component.literal(size + "x" + size),
                btn -> radiusChunks = size / 2
            ).bounds(panelX, y, 30, 20).build());
            panelX += 35;
        }
        panelX = 10;
        y += 30;

        // Generation level radio buttons
        for (GenerationLevel level : GenerationLevel.values()) {
            final GenerationLevel lvl = level;
            addRenderableWidget(Button.builder(
                Component.literal(level.label),
                btn -> generationLevel = lvl
            ).bounds(panelX, y, PANEL_WIDTH - 20, 20).build());
            y += 25;
        }
        y += 10;

        // Generate button
        addRenderableWidget(Button.builder(
            Component.literal("Generate Preview"),
            btn -> startGeneration()
        ).bounds(panelX, y, PANEL_WIDTH - 20, 20).build());
        y += 30;

        // Back button
        addRenderableWidget(Button.builder(
            Component.literal("Back"),
            btn -> onClose()
        ).bounds(panelX, height - 30, PANEL_WIDTH - 20, 20).build());

        // Viewport bounds
        viewportX = PANEL_WIDTH;
        viewportY = 0;
        viewportW = width - PANEL_WIDTH;
        viewportH = height;
    }

    private void startGeneration() {
        if (isGenerating) return;
        isGenerating = true;
        statusText = "Starting server...";

        // Clear previous data
        renderer.releaseBuffers();
        previewWorld.clear();

        CompletableFuture.runAsync(() -> {
            try {
                // Start server (or reuse if same preset/seed)
                serverManager.startServer("DefaultPreset", seed, s ->
                    minecraft.execute(() -> statusText = s)
                ).join();

                var level = serverManager.getOverworld();
                if (level == null) {
                    minecraft.execute(() -> {
                        statusText = "Failed: no overworld";
                        isGenerating = false;
                    });
                    return;
                }

                // Generate chunks
                minecraft.execute(() -> statusText = "Generating chunks...");
                chunkManager.generate(
                    level, previewWorld, radiusChunks,
                    generationLevel.status,
                    (done, total) -> minecraft.execute(() ->
                        statusText = "Generating: " + done + "/" + total),
                    () -> {
                        // Trigger progressive mesh compilation
                        // TODO: compile newly added sections
                    }
                );

                // Compile all meshes
                minecraft.execute(() -> {
                    statusText = "Compiling mesh...";
                    renderer.compileAll();
                    camera.fitTo(
                        new Vector3f(radiusChunks * 8, 100, radiusChunks * 8),
                        radiusChunks * 16f
                    );
                    statusText = "Ready";
                    isGenerating = false;
                });
            } catch (Exception e) {
                minecraft.execute(() -> {
                    statusText = "Error: " + e.getMessage();
                    isGenerating = false;
                });
            }
        });
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Background
        renderBackground(graphics, mouseX, mouseY, partialTick);

        // Title
        graphics.drawCenteredString(font, title, width / 2, 5, 0xFFFFFF);

        // Status
        graphics.drawString(font, statusText, 10, height - 50, 0xAAAAAA);

        // 3D viewport
        renderViewport(graphics, partialTick);

        // UI widgets on top
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderViewport(GuiGraphics graphics, float partialTick) {
        // Scissor to viewport area
        graphics.enableScissor(viewportX, viewportY, viewportX + viewportW, viewportY + viewportH);

        float aspect = (float) viewportW / viewportH;
        Matrix4f viewProj = camera.getViewProjectionMatrix(aspect);

        // Setup GL state
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);

        PoseStack poseStack = new PoseStack();
        // TODO: Apply viewProj matrix and render sections per RenderType:
        // 1. renderer.draw(RenderType.solid(), ...)
        // 2. renderer.draw(RenderType.cutout(), ...)
        // 3. RenderSystem.depthMask(false); RenderSystem.enableBlend();
        // 4. renderer.draw(RenderType.translucent(), ...)
        // 5. Cleanup GL state

        graphics.disableScissor();
    }

    // --- Input handling ---

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button,
                                 double deltaX, double deltaY) {
        if (isInViewport(mouseX, mouseY)) {
            camera.rotate((float) (-deltaX * 0.01), (float) (deltaY * 0.01));
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY,
                                  double deltaH, double deltaV) {
        if (isInViewport(mouseX, mouseY)) {
            camera.zoom((float) (deltaV * 120));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, deltaH, deltaV);
    }

    private boolean isInViewport(double mouseX, double mouseY) {
        return mouseX >= viewportX && mouseX < viewportX + viewportW
            && mouseY >= viewportY && mouseY < viewportY + viewportH;
    }

    @Override
    public void onClose() {
        chunkManager.cancel();
        serverManager.stopServer();
        renderer.releaseBuffers();
        previewWorld.clear();
        super.onClose();
    }
}
```

**Step 2: Commit**

```bash
git commit -m "feat: wire PreviewScreen with controls, generation, and rendering pipeline"
```

---

## Task 9: BO3/BO4 Standalone Preview

**Files:**
- Create: `platforms/shared/src/client/java/com/pg85/otg/client/preview/BOPreviewHelper.java`
- Modify: PreviewScreen to add BO preview mode (or create separate BOPreviewScreen)

**Step 1: Implement BOPreviewHelper**

Loads a BO3/BO4 file and populates PreviewWorld with its blocks:

```java
package com.pg85.otg.client.preview;

import com.pg85.otg.client.preview.world.PreviewWorld;
import com.pg85.otg.customobject.bo3.BO3;
import com.pg85.otg.customobject.bo3.BO3Config;
import com.pg85.otg.customobject.bo3.bo3function.BO3BlockFunction;
import com.pg85.otg.customobject.bo4.BO4;
import com.pg85.otg.customobject.bo4.BO4Config;
import com.pg85.otg.customobject.CustomObject;
import net.minecraft.core.BlockPos;
import org.joml.Vector3f;

import java.nio.file.Path;

public class BOPreviewHelper {

    public record BOBounds(Vector3f center, float radius) {}

    /**
     * Load a BO3/BO4 file and place its blocks into PreviewWorld.
     * Returns bounding info for camera fitting.
     */
    public static BOBounds loadIntoWorld(CustomObject object, PreviewWorld world) {
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;

        if (object instanceof BO3 bo3) {
            BO3Config config = bo3.getConfig();
            // TODO: iterate over BO3 block functions
            // For each block:
            //   BlockPos pos = new BlockPos(block.x, block.y, block.z);
            //   BlockState state = resolve block.material to MC BlockState
            //   world.setBlockState(pos, state);  // needs method added to PreviewWorld
            //   update min/max bounds
        } else if (object instanceof BO4 bo4) {
            // Similar for BO4
        }

        Vector3f center = new Vector3f(
            (minX + maxX) / 2f, (minY + maxY) / 2f, (minZ + maxZ) / 2f
        );
        float radius = Math.max(
            Math.max(maxX - minX, maxY - minY), maxZ - minZ
        ) / 2f + 2f;

        return new BOBounds(center, radius);
    }
}
```

**Note:** The exact block iteration API depends on BO3Config/BO4Config internals. Read `BO3Config.java` and `BO4Config.java` at implementation time to determine how to iterate blocks and resolve materials to `BlockState`.

The rendering pipeline is identical to terrain preview — same PreviewWorld, PreviewRenderer, OrbitCamera. The only difference: no server needed, blocks inserted directly.

**Step 2: Add BO preview mode to PreviewScreen or create BOPreviewScreen**

Add a toggle or tab in the UI to switch between "Terrain Preview" and "BO Preview" modes. In BO mode:
- Hide server-related controls (seed, size, generation level)
- Show file picker for BO3/BO4 files
- Load button calls `BOPreviewHelper.loadIntoWorld()` + `renderer.compileAll()` + `camera.fitTo(bounds)`

**Step 3: Commit**

```bash
git commit -m "feat: add BO3/BO4 standalone preview via shared rendering pipeline"
```

---

## Task 10: Polish and Integration

**Files:** Various — cleanup and edge cases.

**Step 1: Add preset selector to PreviewScreen**

Read available DimensionPresets from OTG config directories. Populate dropdown/selector.

**Step 2: Handle edge cases**

- Empty PreviewWorld (no chunks yet) — don't crash in renderer
- Out of memory — catch and show user message
- Server start timeout — timeout after 30s, show error
- Clean temp directories on crash (shutdown hook)

**Step 3: Add progress bar for generation**

Replace text status with a visual progress bar widget.

**Step 4: Final commit**

```bash
git commit -m "feat: polish preview UI with preset selector, progress bar, error handling"
```

---

## Execution Order & Dependencies

```
Task 1 (Gradle setup)
  └→ Task 2 (TitleScreen button + empty screen)
      └→ Task 3 (OrbitCamera) — independent, can parallel with 4
      └→ Task 4 (PreviewWorld) — independent, can parallel with 3
          └→ Task 5 (TempServerManager) — needs PreviewWorld design
          └→ Task 6 (ChunkGenerationManager) — needs PreviewWorld
          └→ Task 7 (PreviewRenderer) — needs PreviewWorld
              └→ Task 8 (Wire PreviewScreen) — needs all above
                  └→ Task 9 (BO preview) — needs renderer working
                      └→ Task 10 (Polish)
```

## Risk Register

| Risk | Impact | Mitigation |
|------|--------|------------|
| IntegratedServer API too complex to use programmatically | HIGH | Fallback: headless generation to CARVERS without server |
| BlockRenderDispatcher API changed in 1.21.1 | MEDIUM | Read MC source at implementation time, adjust method calls |
| Memory usage too high for 32x32 chunks | MEDIUM | Default to 4x4, warn user at higher sizes |
| BufferBuilder/MeshData API mismatch with plan | MEDIUM | API is skeleton — must be verified from MC source |
| Client source set breaks existing build | LOW | Test build immediately after Task 1 |

## Notes for Implementer

1. **Read MC source first.** Before implementing Tasks 5 and 7, decompile and read:
   - `CreateWorldScreen.java` — how MC starts a world
   - `WorldOpenFlows.java` — programmatic world creation
   - `BlockRenderDispatcher.java` — renderBatched signature
   - `SectionRenderDispatcher.RenderSection.compile()` — how MC compiles chunk sections (reference impl)

2. **Mojang mappings.** All MC class names in this plan use mojmap. If you see Yarn names (e.g. `BlockRenderManager`), translate to mojmap (`BlockRenderDispatcher`).

3. **No automated tests.** All testing is manual — launch the game, click buttons, check visually.

4. **Skeleton code.** Tasks 5 and 7 have TODO sections. These are intentional — the exact MC API calls need verification from source before writing final code.
