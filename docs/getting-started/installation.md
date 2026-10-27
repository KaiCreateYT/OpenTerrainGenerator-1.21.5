# Installation

## Requirements

- **Minecraft** 1.21.1
- **Java** 21 or newer
- **Fabric Loader** 0.16.14+ or **NeoForge** 21.1+

## Install the Mod

=== "Fabric"

    1. Install [Fabric Loader](https://fabricmc.net/use/installer/) for Minecraft 1.21.1
    2. Download and place in your `mods/` folder:
        - [Fabric API](https://modrinth.com/mod/fabric-api) — core Fabric modding library
        - [Cardinal Components API](https://modrinth.com/mod/cardinal-components-api) — data attachment API (v6), used by OTG for portal player data
        - The OTG Fabric JAR (`otg-fabric-*.jar`)

=== "NeoForge"

    1. Install [NeoForge](https://neoforged.net/) 21.1 or newer for Minecraft 1.21.1
    2. Download the OTG NeoForge JAR (`otg-neoforge-*.jar`) and place it in your `mods/` folder

!!! warning "Platform mismatch"
    The Fabric and NeoForge JARs are **not interchangeable**. Make sure you download the correct one for your mod loader.

## Install a DimensionPreset

OTG ships with a built-in **DefaultPreset** — no extra downloads required. To use a custom preset:

1. Locate your Minecraft instance folder (where `mods/` lives)
2. Navigate to `.minecraft/config/OpenTerrainGenerator/DimensionPresets/`
   (OTG creates this folder structure on first launch)
3. Place the preset folder inside it (e.g. `DimensionPresets/MyPreset/`)


## Verify Installation

1. Launch Minecraft with the mod installed
2. Click **Singleplayer** → **Create New World**
3. Under **World Type**, you should see OTG presets listed (e.g. "Default Preset")
4. If OTG presets appear in the list, the mod is installed correctly

## Recommended Mods (1.21.1)

=== "Fabric"

    | Mod | Description |
    |-----|-------------|
    | [C2ME](https://modrinth.com/mod/c2me-fabric) | Chunk generation multithreading. Highly recommended for multiplayer. |
    | [Lithium](https://modrinth.com/mod/lithium) | Server-side optimization. |
    | [ScalableLux](https://modrinth.com/mod/scalablelux) | Multithreaded lighting engine (Starlight successor). |
    | [Sodium](https://modrinth.com/mod/sodium) | Client-side rendering optimization. (client only) |
    | [Iris](https://modrinth.com/mod/iris) | Shader support compatible with Sodium. (client only) |

=== "NeoForge"

    | Mod | Description |
    |-----|-------------|
    | [C2ME](https://modrinth.com/mod/c2me-fabric) | Chunk generation multithreading. Highly recommended for multiplayer. |
    | [Sodium](https://modrinth.com/mod/sodium) | Client-side rendering optimization. (client only) |
    | [Iris](https://modrinth.com/mod/iris) | Shader support compatible with Sodium. (client only) |
    | [ScalableLux](https://modrinth.com/mod/scalablelux) | Multithreaded lighting engine (Starlight successor). |

## Troubleshooting

| Problem | Solution |
|---------|----------|
| OTG presets don't appear | Check that the JAR matches your mod loader (Fabric vs NeoForge) |
| Crash on startup | Verify you're running Java 21+ and MC 1.21.1 |
| Fabric: missing dependencies | Make sure Fabric API is installed alongside OTG |
| Custom preset not showing | Check that the folder structure is `.minecraft/config/OpenTerrainGenerator/DimensionPresets/<name>/DimensionPresetConfig.ini` |
