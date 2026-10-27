# In-Game OTG Editor & Preview System — Design

**Date:** 2026-03-05
**Status:** Approved

## Overview

In-game terrain and BO3/BO4 preview system with MC-native rendering. Accessible from the Title Screen as "OTG Editor". Uses a temporary IntegratedServer for 1:1 chunk generation and MC's BlockRenderDispatcher for pixel-perfect block rendering.

Future phases will add property editors for biome/world/preset configs, but this design covers the preview/rendering foundation.

## Inspirations

ReEdited (external .NET/Avalonia OTG config editor) has a dual-path terrain preview (GPU mesh + isometric fallback). This design takes the concept further by rendering inside Minecraft itself — real block models, textures, biome tinting, water transparency — instead of custom meshes with color palettes.

## Module Structure

Client source sets within existing platform modules:

```
platforms/
  shared/
    src/client/java/com.pg85.otg.client/
      preview/            PreviewScreen, OrbitCamera, PreviewRenderer
      preview/world/      TempServerManager, ChunkGenerationManager, PreviewWorld
      gui/                GUI framework (later)
      gui/editor/         Biome/World/Preset editors (later)
  fabric/
    src/client/java/
      OTGFabricClient.java     ClientModInitializer, title screen button
  neoforge/
    src/client/java/
      OTGNeoForgeClient.java   @Mod(dist=CLIENT), title screen button
```

Server-side code unchanged. Client code loads only on client (Fabric/NeoForge source set separation).

## Entry Point

Button "OTG Editor" on Minecraft Title Screen. Injected via mixin on TitleScreen or platform-specific screen event API.

## Terrain Preview

### Pipeline

```
User selects DimensionPreset, seed, chunk radius, generation level
  -> clicks "Generate Preview"
  -> TempServerManager starts IntegratedServer with chosen preset
     (temp world directory, deleted on close)
  -> ChunkGenerationManager requests chunks spirally from center
     (ServerChunkCache.getChunkFuture per chunk)
  -> Completed chunks copied to PreviewWorld (client-side BlockAndTintGetter)
  -> PreviewRenderer compiles mesh per section via BlockRenderDispatcher
  -> PreviewScreen draws meshes with orbit camera
```

### Generation Levels

Three levels — user picks one, clicks "Generate Preview":

| Level | ChunkStatus | What's visible |
|-------|-------------|----------------|
| Surface | SURFACE | Terrain shape + surface blocks (grass, sand, stone) |
| Caves | CARVERS | Above + carved caves and ravines |
| Full | FULL | Everything: structures, decorations, trees, ores, BO3/BO4 |

No live regeneration — explicit "Generate Preview" button only.

### Chunk Radius

Configurable slider: 4-32 chunks (per side). Default: 4. Chunks generated spirally from center — preview appears progressively.

### TempServerManager

Manages IntegratedServer lifecycle:

- `startServer(DimensionPreset, seed)` — creates temp directory, configures WorldCreationContext, starts server, waits for load
- `stopServer()` — shuts down server, deletes temp directory
- Server restarted on: seed change, preset change, generation level change
- Server reused on: chunk radius increase (just generate more chunks)

### PreviewWorld

Client-side `BlockAndTintGetter` implementation:

- `HashMap<ChunkPos, PreviewChunk>` — stores copied block/biome data
- `getBlockState(BlockPos)` — from stored chunks
- `getFluidState(BlockPos)` — from stored chunks
- `getBiome(BlockPos)` — for grass/leaf/water tinting
- `PreviewChunk` holds `BlockState[16][384][16]` + `Biome[4][96][4]` (quarter-res)
- Data copied from server's ChunkAccess after generation completes

### Lighting

Fake sky light — no full light engine:
- Surface blocks: light level 15
- Below solid blocks: decreasing light
- Sufficient for visual preview without computation cost

## BO3/BO4 Preview

### Pipeline

```
User selects BO3/BO4 file
  -> Parse via existing BO3/BO4 parser (common-customobject)
  -> Insert blocks directly into PreviewWorld (no server needed)
  -> PreviewRenderer compiles mesh
  -> PreviewScreen renders with orbit camera (auto-fit to bounding box)
```

Standalone only — object on empty background, no terrain context.

### Shared Components

Same rendering stack as terrain preview: PreviewWorld, PreviewRenderer, PreviewScreen, OrbitCamera. Only difference is data source (chunk generator vs BO parser).

## Rendering

### PreviewRenderer

Compiles vertex buffers per section (16x16x16):

```java
for each block in section:
    if block.isAir() -> skip
    blockRenderDispatcher.renderBatched(state, pos, previewWorld, poseStack, consumer, random)
```

MC's `BlockRenderDispatcher` handles:
- Block model lookup and vertex generation
- Face culling (checks neighbors via BlockAndTintGetter)
- Biome tinting (grass, leaves, water color)
- UV mapping and texture coordinates

Output: `VertexBuffer` per `RenderType` per section.

Compilation on background threads, VBO upload on render thread.

### Render Passes (PreviewScreen)

```
1. Solid pass      — depth write ON, blend OFF
2. Cutout pass     — alpha test ON (flowers, grass, saplings)
3. Translucent pass — depth write OFF, blend ON (water, glass, ice)
```

### OrbitCamera

Spherical coordinate camera (adapted from ReEdited):

- Theta (azimuth): mouse drag X -> rotate around Y axis
- Phi (elevation): mouse drag Y -> tilt, clamped [0.1, pi-0.1]
- Distance: scroll wheel -> zoom, clamped [1, 5000]
- Target: center of generated terrain / BO bounding box
- FitTo: auto-adjust distance to fit content
- FOV: 45 degrees, near=0.1, far=10000

Input: drag in viewport = rotate, scroll = zoom.

## Screen Layout

```
+--------------------------------------------------+
|  Title Bar: "OTG Editor - Preview"        [Back]  |
+------------+-------------------------------------+
|            |                                     |
|  Controls  |         3D Viewport                 |
|            |                                     |
| Preset: [v]|     (orbit camera render)           |
| Seed: [___]|                                     |
| Size: [=4=]|                                     |
|            |                                     |
| ( ) Surface|                                     |
| ( ) Caves  |                                     |
| (*) Full   |                                     |
|            |                                     |
| [Generate] |                                     |
|            |                                     |
| Status:    |                                     |
| "Ready"    |                                     |
|            |                                     |
+------------+-------------------------------------+
|  [Back to Menu]                                  |
+--------------------------------------------------+
```

## Lifecycle & Cleanup

### Server lifecycle
- Created on first "Generate Preview" click
- Restarted on seed/preset/toggle change
- Reused on size increase (generate additional chunks)
- Destroyed on screen close

### Resource cleanup (PreviewScreen.onClose)
- TempServerManager.stopServer()
- PreviewRenderer.releaseBuffers() — free all VBOs
- PreviewWorld.clear() — release chunk data
- Temp directory deleted

### Error handling
- Server start failure: status message + "Retry" button
- Chunk generation failure: skip chunk, show gap, log warning
- Mesh compilation failure: skip section, log warning
- OutOfMemoryError: catch on chunk copy, show "Reduce preview size"

## Memory Estimates

| Size | Block data | VRAM (est.) |
|------|-----------|-------------|
| 4x4 chunks | ~2-5 MB | ~10-20 MB |
| 16x16 chunks | ~30-60 MB | ~100-200 MB |
| 32x32 chunks | ~50-100 MB | ~200-400 MB |

## Out of Scope (future phases)

- Property editor (biome/world/preset config editing in-game)
- GUI widget framework
- In-context BO preview (object placed on terrain)
- Multiplayer / dedicated server support
- Live edit (change config -> auto-regenerate preview)
