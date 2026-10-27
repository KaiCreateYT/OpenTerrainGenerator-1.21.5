##  OpenTerrainGenerator by Team OTG

OpenTerrainGenerator for MC 1.21.5 (Fabric & NeoForge). Alpha builds are available in the dev-releases channel of the OTG Discord.



### Team OTG
* <a href="https://github.com/PG85">PG85</a>
* MCPitman
* <a href="https://github.com/authvin">Authvin</a>
* <a href="https://github.com/Coll1234567">Josh</a>
* <a href="https://github.com/SuperCoder7979">SuperCoder79</a>
* <a href="https://github.com/SXRWahrheit">Wahrheit</a>

We're always looking for people to contribute or collaborate with. OTG 1.21.1 supports both Fabric and NeoForge. If you'd like to contribute, collaborate or become part of Team OTG, join us on the [OTG Discord](https://discord.gg/YY2NECCBYN)!

For more information on this specific fork, join [KaiCreate's Discord](https://discord.gg/bNgExMxppJ).

## Installation / building

Download the latest JAR from the [Releases](https://github.com/KaiCreateYT/OpenTerrainGenerator-1.21/releases) tab, corresponding to the Minecraft version and mod loader you need. [Cardinal Componenets API](https://www.curseforge.com/minecraft/mc-mods/cardinal-components-api) and [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api) are both required for the Fabric version.

Alternatively, download this repo and build it yourself via command prompt or IDE.
```bash
# Build both platforms (Fabric + NeoForge)
./gradlew build

# Output:
#   build/distributions/otg-fabric-0.2.0-dev1.jar
#   build/distributions/otg-neoforge-0.2.0-dev1.jar
```

For IntelliJ: open the project folder, trust the Gradle project, done. For Eclipse: `./gradlew genEclipseRuns && ./gradlew eclipse`.

If Gradle is being a bitch: `./gradlew clean --refresh-dependencies build`.

## Commands

### Dimension Management

OTG supports creating custom dimensions from any preset:

| Command | Permission | Description |
|---------|------------|-------------|
| `/otg dimension create <preset>` | OP (level 2) | Creates a new dimension using the specified preset. Requires server restart. |
| `/otg dimension delete <name>` | OP (level 2) | Removes a dimension (keeps world data). |
| `/otg dimension delete <name> --purge --confirm` | OP (level 2) | Removes a dimension and deletes all world data permanently. |
| `/otg dimension list` | All players | Lists all OTG dimensions. |
| `/otg dimension info <name>` | All players | Shows dimension details (preset, seed, creation date). |

### Teleportation

| Command | Permission | Description |
|---------|------------|-------------|
| `/otg tp <dimension>` | All players | Teleports to the specified dimension. Automatically finds safe spawn location. |

Supported dimension names for `/otg tp`:
- `overworld`, `the_nether`, `the_end` - vanilla dimensions
- Any OTG dimension name (e.g., `void`, `biome_bundle`)

## Known Issues (1.21.5)

### Portals

OTG supports nether-style portals with configurable colors and frame blocks. Current known issues:

1. **Frame block on return to overworld** - Auto-created portals in overworld use the destination preset's frame block, not the source. If overworld is not an OTG dimension, it falls back to quartz instead of the configured block.
2. **Portals teleport to 0,0 in overworld** - Return portals to the overworld land at coordinates 0,0 instead of near the original portal location.
3. **Portal linking broken** - Portals often don't teleport back to the linked portal on return, instead creating a new portal.

### Mod Compatibility

| Mod | Status | Notes |
|-----|--------|-------|
| C2ME | Compatible | Tested on 1.21.1. |
| Lithium | Compatible | No known issues. |
| Sodium | Compatible | Client-side only. |
| Iris | Compatible | Client-side only. |

## Chunk Generation & Multithreading

OTG's chunk generation is **thread-safe** and runs on vanilla's `ForkJoinPool` (`Util.backgroundExecutor()`). OTG does not manage its own worker threads — vanilla's chunk pipeline handles parallelism.

### How it works

Vanilla's chunk pipeline runs on a `ForkJoinPool` with `cores - 1` threads. Each worker thread processes one chunk at a time through status upgrades (EMPTY → STRUCTURE → NOISE → SURFACE → etc). When a worker calls `fillFromNoise()`, OTG runs `populateNoise()` **synchronously** on that worker thread and returns a `CompletableFuture.completedFuture()`. This gives natural per-chunk parallelism: on an 8-core CPU, up to 7 chunks generate in parallel with zero deadlock risk.

This is the same approach [C2ME](https://github.com/RelativityMC/C2ME-fabric) uses (`Runnable::run` as executor).

### Thread-safety guarantees

| Component | Mechanism |
|-----------|-----------|
| Noise samplers | Immutable after `setSeed()` — safe to read from any thread |
| Noise data buffer | `ThreadLocal<double[][][]>` — each worker has its own |
| Biome cache | `ThreadSafeLRUCache` (Caffeine-backed) — concurrent reads/writes |
| Block columns cache | `ThreadSafeLRUCache` — same |
| Decoration | Per-chunk `Random` (deterministic from world seed + chunk coords) |
| Chunk decorator | `AtomicInteger` counters, `volatile` flags, region-based locking |

### Multiplayer scaling

For servers with many players in different locations, install **C2ME** alongside OTG:

- **C2ME** handles chunk pipeline scheduling (priority queue based on player distance, dedicated thread pool, async I/O)
- **OTG** handles terrain generation (noise, surface, carvers, decoration)

Without C2ME, vanilla's scheduler uses simple FIFO ordering with no player-distance priority. CPU-bound terrain generation (~13ms/chunk) is the bottleneck, not scheduling.

### Shadow chunk generation

`ShadowChunkGenerator` provides on-demand terrain generation for BO4 custom objects and `/otg mapterrain`. It generates base terrain for chunks **outside** vanilla's pipeline (no worker threads, no async). Used only when BO4 decoration needs height/material data from unloaded neighboring chunks.

## Development

### Terrain Snapshot Testing

OTG includes a headless terrain generation testing system that allows verifying terrain generation without running Minecraft. This is useful for regression testing after code changes.

**Generate a baseline snapshot:**
```bash
./gradlew :common:common-test:run --args="generate --seed 12345 --output baseline.json --preset DefaultPreset --otg-root /path/to/otg/resources"
```

**Verify generation hasn't changed:**
```bash
./gradlew :common:common-test:run --args="verify --seed 12345 --baseline baseline.json --preset DefaultPreset --otg-root /path/to/otg/resources"
```

**Compare two snapshots:**
```bash
./gradlew :common:common-test:run --args="compare --baseline old.json --current new.json"
```

The snapshot captures height values at regular intervals across multiple chunks, allowing detection of any changes to terrain generation algorithms.

### Performance Benchmark

Measure terrain generation performance:
```bash
./gradlew :common:common-test:run --args="benchmark --preset DefaultPreset --otg-root /path/to/otg/resources"
```

Options:
- `--chunks <n>` - Chunks per iteration (default: 100)
- `--warmup <n>` - Warmup iterations (default: 3)
- `--iterations <n>` - Measurement iterations (default: 5)

Example output:
```
=== RESULTS ===
Average: 243.56 ms for 100 chunks
Throughput: 410.6 chunks/sec
Per chunk: 2.436 ms
```

## Recommended Mods (1.21.1)

### Fabric

| Mod | Description | Required |
|-----|-------------|----------|
| [Fabric API](https://modrinth.com/mod/fabric-api) | Core Fabric modding library | Yes |
| [Cardinal Components API](https://modrinth.com/mod/cardinal-components-api) | Data attachment API (v6). Used by OTG for portal player data. | Yes |
| [C2ME](https://modrinth.com/mod/c2me-fabric) | Chunk generation multithreading. Highly recommended for multiplayer. | No |
| [Lithium](https://modrinth.com/mod/lithium) | Server-side optimization. | No |
| [ScalableLux](https://modrinth.com/mod/scalablelux) | Multithreaded lighting engine (Starlight successor). | No |
| [Sodium](https://modrinth.com/mod/sodium) | Client-side rendering optimization. | No (client only) |
| [Iris](https://modrinth.com/mod/iris) | Shader support compatible with Sodium. | No (client only) |

### NeoForge

| Mod | Description | Required |
|-----|-------------|----------|
| NeoForge 21.1.x | Mod loader | Yes |
| [C2ME](https://modrinth.com/mod/c2me-fabric) | Chunk generation multithreading. Highly recommended for multiplayer. | No |
| [Sodium](https://modrinth.com/mod/sodium) | Client-side rendering optimization. | No (client only) |
| [Iris](https://modrinth.com/mod/iris) | Shader support compatible with Sodium. | No (client only) |
| [ScalableLux](https://modrinth.com/mod/scalablelux) | Multithreaded lighting engine (Starlight successor). | No |

NeoForge has Data Attachments built-in (no CCA dependency needed).

**Note:** Starlight is replaced by [ScalableLux](https://modrinth.com/mod/scalablelux) in 1.21.1.

## Links
* [CurseForge](https://minecraft.curseforge.com/projects/open-terrain-generator)
* [Wiki](http://openterraingen.wikia.com/wiki/Open_Terrain_Generator_Wiki)
* [Discord](https://discord.com/invite/UXzdVTH)
* [Installation](https://openterraingen.fandom.com/wiki/Installing_OTG) for Spigot and Forge

## Original developers

OpenTerrainGenerator is a fork of Terrain Control, which is the successor to <a href="http://www.minecraftforum.net/topic/313991-phoenixterrainmod/">PhoenixTerrainMod</a>, which was based on <a href="http://www.minecraftforum.net/topic/71565-biomemod/">BiomeTerrainMod</a>. 

* Buycruss       - BiomeTerrainMod
* R-T-B          - PhoenixTerrainMod
* <a href="http://dev.bukkit.org/profiles/Khoorn/">Khoorn - TerrainControl</a> (known as <a href="https://github.com/Wickth">Wickth</a> on GitHub)
* <a href="https://github.com/oloflarsson">Oloflarsson/Cayorion - TerrainControl</a>
* <a href="https://github.com/Timethor">Timethor - TerrainControl</a>
* <a href="https://github.com/rutgerkok">Rutgerkok - TerrainControl</a>
* <a href="https://github.com/bloodmc">BloodMC - TerrainControl</a>
