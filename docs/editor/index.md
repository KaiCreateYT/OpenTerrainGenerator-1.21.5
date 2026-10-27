# In-Game Editor

OTG includes a built-in configuration editor accessible from Minecraft's title screen. Edit world settings, biome configs, biome groups, and browse custom objects — all without leaving the game.

## Opening the Editor

Click **OTG Editor** on the title screen. This opens the **Editor Hub** where you select a DimensionPreset and navigate to the editors.

!!! note
    The editor works on files on disk. Changes are saved to the preset's config files. To see changes in-game, you need to create a new world (or regenerate chunks).

## Editor Hub

The hub is your starting point. It shows:

- **Preset selector** — cycle through available DimensionPresets with ◀/▶ arrows
- **World Settings** — edit DimensionPresetConfig.ini
- **Biome Editor** — edit individual biome .bc files
- **Group Settings** — manage BiomeGroups and group property overrides
- **BO Store** — browse and preview custom objects (BO2/BO3/BO4)
- **Preview World** — generate a 3D terrain preview of the selected preset

## World Settings

Edit the DimensionPresetConfig.ini file for the selected preset. Properties are organized into 7 category tabs:

| Tab | What it controls |
|-----|-----------------|
| World | Height scale, water level, preset identity |
| Biome Distribution | BiomeMode, GenerationDepth, land/sea ratios |
| Terrain | Fracture settings, volatility |
| Caves & Ravines | Cave frequency, depth, noise parameters |
| Structures | Vanilla structure toggles (strongholds, villages, etc.) |
| Dimensions | Dimension-specific settings |
| Game Rules | Per-dimension GameRule overrides |

**Search** — type in the search box to filter properties by name.

**Save** — click Save to write changes to disk. The unsaved indicator (orange dot) shows when you have pending changes.

**Preview** — opens a 3D terrain preview of the preset. You must Save first — preview uses the on-disk config.

## Biome Editor

Edit individual biome .bc files. The screen has two panels:

### Left Panel — Biome List

- Scrollable list of all biomes in the preset
- **Search** — filter biomes by name
- **New** — create a new biome with default settings
- **Clone** — copy the selected biome
- **Delete** — remove the selected biome (double-click to confirm)

### Center Panel — Property Grid

Properties for the selected biome, organized by category:

| Tab | What it controls |
|-----|-----------------|
| Biome Terrain | Height, volatility, surface/ground/stone blocks |
| Placement | Biome size, rarity, isle/border biomes, map color |
| Structures | Vanilla structure toggles per biome |
| Mobs | Mob spawning lists |
| Advanced | Visual settings (sky/water/grass colors), underground biomes |

Each property row shows:

- **Name** and **value** (type-dependent editor: text input, checkbox, dropdown)
- **Override** toggle — marks this value as overriding the group default
- **Merge** toggle — for list properties, append instead of replace

### Resource Queue (Read-Only)

Below the property grid, the biome's resource queue entries (Ore, Tree, CustomObject, etc.) are displayed read-only. To edit them, modify the .bc file directly.

### Bottom Bar

- **Save** — writes resolved values to the .bc file (applies group overrides automatically)
- **Preview** — opens terrain preview
- **Browse BO3** — opens the BO Store to find custom objects

## Group Settings

Manage BiomeGroups — groups control where biomes spawn geographically and can override biome properties.

### Three-Column Layout

**Left — Group List**

- All BiomeGroups from DimensionPresetConfig.ini
- **New** / **Delete** buttons

**Center — Biome Assignment**

- **Available** list — biomes not in the selected group
- **In Group** list — biomes assigned to the group
- ▶ / ◀ buttons to move biomes between lists
- Group parameters: **Depth**, **Rarity**, **MinTemp**, **MaxTemp**

**Right — Property Overrides**

Set per-group property overrides that apply to all biomes in the group:

- **Override** — when enabled, this group's value replaces the biome's default
- **Merge** — for list properties, append the group's value instead of replacing
- **OPV** (OverrideParentValues) — cuts the inheritance chain; previous groups' overrides are discarded

!!! info "How overrides work"
    Override flags are stored in `.otg-editor.json` (editor-only file, not read by OTG engine). When you Save a biome in the Biome Editor, the editor resolves all group overrides and writes the **final values** to the .bc file. Groups are processed in their .ini file order.

### Save

Saves both:

1. BiomeGroup() lines to DimensionPresetConfig.ini (name, depth, rarity, biomes, temps)
2. Property overrides to `.otg-editor.json`

## BO Store

Browse and preview custom objects (BO2, BO3, BO4) from the preset's Objects/ folder.

### Left Panel — File Tree

- Collapsible folder tree with ▶/▼ expand/collapse
- Folders sorted before files, both alphabetical
- **Search** — filters by filename, auto-expands matching folders
- Supports `.bo2`, `.bo3`, `.bo4` files

### Right Panel — 3D Preview

Select a file to see a 3D preview:

- **Orbit** — drag to rotate the object
- **Zoom** — scroll wheel
- **Direction buttons** (N/S/E/W) — snap camera to cardinal directions

Below the preview:

- **Metadata** — object name, type, size (XxYxZ), block count

### Assign to Biome

Click **Assign to Biome** to add the selected object to a biome's resource queue. Select a biome from the picker — the editor appends `CustomObject(100, ObjectName)` to the .bc file.

## Terrain Preview

The preview system generates a 3D terrain view using a temporary world:

1. Select a preset and click **Preview World** (or Preview from any editor)
2. A temporary IntegratedServer starts and generates chunks
3. Blocks are compiled into 3D meshes and rendered in a viewport
4. **Orbit** (drag), **zoom** (scroll), and view the terrain

Options:

- **Seed** — world seed for generation
- **Size** — 4x4, 8x8, 16x16, or 32x32 chunks
- **Gen Level** — Surface, Caves, or Full generation

### Biome Terrain Preview

From the Biome Editor, you can preview a single biome's terrain heightmap. This uses OTG's actual noise pipeline (BiomeHeight, BiomeVolatility, Fracture settings) to generate a terrain surface preview without starting a full server.

## File Reference

| File | Location | Editor |
|------|----------|--------|
| DimensionPresetConfig.ini | `DimensionPresets/{preset}/` | World Settings |
| *.bc biome configs | `DimensionPresets/{preset}/Biomes/` | Biome Editor |
| BiomeGroup() lines | Inside DimensionPresetConfig.ini | Group Settings |
| .otg-editor.json | `DimensionPresets/{preset}/` | Group Settings (overrides) |
| BO2/BO3/BO4 objects | `DimensionPresets/{preset}/Objects/` | BO Store |
