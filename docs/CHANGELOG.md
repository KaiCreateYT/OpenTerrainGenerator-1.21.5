## Minecraft 1.21.5 — Fabric + NeoForge

### Release: 0.5.0-dev1

**2026-07-10 — Vanilla biome cross-reference: temperature, wetness, colors, mob inheritance, freeze_top_layer fixed across all 82 DefaultPreset biomes**

- **All 82 DefaultPreset .bc files now match vanilla 1.21.11 biome JSONs**: Cross-referenced every biome against `assets.mcasset.cloud/1.21.11/data/minecraft/worldgen/biome/<id>.json`. Fixed `BiomeTemperature`, `BiomeWetness`, `SkyColor`, `WaterColor`, `FogColor`, `WaterFogColor`, `GrassColor`, `FoliageColor`, `GrassColorModifier`, and `InheritMobsBiomeName` to match vanilla values.
- **Fix scope**: 64 biomes had missing `Visuals and weather` and `Blocks` sections (using wrong code defaults) — appended full sections with correct vanilla climate/color values. 15 biomes (oceans, Hell, Sky, Void, Mushroom) had existing sections with wrong values — edited SkyColor/WaterColor/FogColor/BiomeTemperature inline. 3 underground biomes (DeepDark, DripstoneCaves, LushCaves) had partial visual configs with wrong values — fixed values and added missing mob inheritance.
- **Notable corrections**: Desert/savanna/badlands `BiomeTemperature` 0.5→2.0, `BiomeWetness` 0.5→0.0. Frozen Peaks/Jagged Peaks `BiomeTemperature` 0.5→-0.7. Hell `FogColor` 0x000000→0x330808, `WaterColor` 0xFFFFFF→0x3f76e4. Swamp/Mangrove Swamp/Dark Forest `GrassColorModifier` None→swamp/dark_forest. Pale Garden `SkyColor` 0x7BA5FF→0xB9B9B9, `FogColor` 0x000000→0x817770. Every biome now has `InheritMobsBiomeName: minecraft:<id>` delegating mob spawning to vanilla.
- **`freeze_top_layer` added to all 77 overworld biomes**: Added `Registry(minecraft:freeze_top_layer,TOP_LAYER_MODIFICATION)` to every overworld biome's resource queue. This is the vanilla feature responsible for placing snow on cold ground and freezing water into ice — without it, snowy biomes had patchy/no snow despite having correct sub-zero temperatures. Only Hell (nether), Sky (end), and The Void excluded.

**2026-07-10 — Colourmap with structure/underground overlay, vanilla density coordinate fix, cave parameter alignment**

- **`/otg map` command**: New command that generates a 512×512 PNG biome colour map centred on the player. Three modes:
  - `surface` (default): Renders each pixel using the biome's `BiomeMapColor` from its `.bc` config.
  - `structures`: Overlays structure colours per biome — each of the 30 structure types (village variants, desert pyramid, jungle temple, etc.) has a distinct colour. The highest-priority structure present in the biome's `BiomeStructureTagConfig` determines the pixel colour. Biomes with no structures render their base `BiomeMapColor`. Village variant colours reflect the exact placement logic (plains/desert/savanna/taiga/snowy).
  - `underground`: Resolves the underground biome at Y=-20 using `UndergroundBiomeResolver`. Pixels where no underground biome exists render as black (`0x000000`, the "passthrough" colour), meaning "ignore cave biomes and generate based on surface biome". Underground biomes render with their own `BiomeMapColor`.
  - Maps saved to `<OTG root>/maps/otg_map_<mode>_<timestamp>.png`.
- **Terrain shape driven by .bc BiomeHeight in all modes**: Removed the vanilla `finalDensity()` sampling path from `OTGChunkGenerator.generateNoiseColumn()`. OTG now always uses its own weighted biome height computation (reading `BiomeHeight`/`BiomeVolatility` from `.bc` files) regardless of `UseVanillaTerrain`. This ensures biome config heights (mountains high, oceans low) always drive terrain shape.
- **Cave carve skipping when UseVanillaTerrain**: `SharedOTGChunkGenerator.fillFromNoise()` skips `carveWithNoise()` when `UseVanillaTerrain: true` — vanilla's `finalDensity()` already has caves baked in, so OTG must not carve on top.
- **Cave noise parameter alignment**: `OTGNoiseRouterData.caveDensityForCarving()` updated to match vanilla 1.21.11:
-   Cheese constant changed from `0.35` to `0.27` (vanilla value) — produces slightly larger cheese caverns.
-   Pillar threshold changed from `0.15` to `0.03` (vanilla value) — more pillars survive carving, matching vanilla pillar density.
**2026-06-30 — Terrain values derived from OverworldBiomeBuilder table (continent/erosion/weirdness)**

- **Terrain values computed from (c, e, w) per biome**: BiomeHeight, BiomeVolatility, SmoothRadius, MaxAverageHeight, MaxAverageDepth, Volatility1/2 now derived from each biome's (continent, erosion, weirdness) position in the OverworldBiomeBuilder table rather than a flat template. Formula: `BiomeHeight = cont_base(continent) + e_height(erosion) * w_mult(weirdness)`. Erosion maps to height (peaks 1.80, slopes 0.70, highland 0.40, rolling 0.25, flat 0.12, windswept 0.20, wetland 0.0). Weirdness multiplies erosion height (valleys 0.85, ridges 1.20). Continent base: inland 0.0, coast 0.0, ocean -0.5/-1.0. Volatility/Volatility1/SmoothRadius/MaxAverageHeight all use erosion+weirdness lookup tables. 64 terrain .bc files updated.
- **Hills/plateau biomes derived from parent**: 26 variant biomes (Wooded Hills through Modified Wooded Badlands Plateau) get terrain from parent biome + height offset (+0.05 to +0.20), volatility offset, smooth radius -1, MAH multiplier. Rivers BH=-0.2 (below sea level for water channels), beaches BH=0.0, swamps BH=0.0. VolatilityWeight1/2 remain 48.0/46.8 (the proven smooth-terrain values), CHC remains 33 zeros, DisableBiomeHeight=false.
- **Biome distribution (IsleInBiomes + proportional rarity), vegetation, BiomeGroup temp range fixes**

- **Vegetation matching vanilla**: Added `Registry(minecraft:...)` vegetation features to 10 mountain/peak/slope/savanna biomes — trees_windswept_hills/forest/savanna, flower_default, patch_grass_badlands/normal, patch_pumpkin, patch_sugar_cane, patch_firefly_bush_near_water, brown_mushroom_normal, red_mushroom_normal, patch_bush, flower_meadow, patch_grass_meadow, trees_grove. Every biome now has proper flowers, mushrooms, pumpkins, sugar cane, and firefly bushes matching vanilla 1.21.5 feature lists.
- **Snowy Slopes**: Removed Taiga trees (vanilla has none at this altitude), added `patch_pumpkin`.
- **Stony Peaks**: Removed goats (vanilla Stony Peaks has no creature spawns), added `freeze_top_layer`.
- **Grove**: Replaced OTG Spruce Trees with `Registry(minecraft:trees_grove)`, added `patch_pumpkin` and `freeze_top_layer`.
- **Cherry Grove & Pale Garden**: Added missing `Visuals and weather` section with correct vanilla 1.21.5 climate settings (BiomeTemperature, BiomeWetness, SkyColor, WaterColor, WaterFogColor, FogColor), plus full vegetation `Registry()` features matching vanilla JSON biome data.
- **Cherry Grove**: temp 0.5, wetness 0.8, sky 0x7BA4FF, water 0x5DB7EF, fog 0xC0D8FF.
- **Pale Garden**: temp 0.7, wetness 0.8, sky 0xB9B9B9, water 0x76889D, fog 0x817770, waterFog 0x556980.
- **Frozen Peaks**: Added `freeze_top_layer` (was missing).
- **BiomeGroup temperature ranges fixed**: Previously ColdBiomes(0.2-0.5) contained Forest(0.7), Plains(0.8), Peaks(-0.7) — all outside the filter. HotBiomes(1.0-2.0) contained Plains(0.8) below min. Redesigned: TemperateBiomes(0.4-0.95) with all temperate forests/plains; ColdBiomes(-0.5-0.35) with taiga/snowy only; HotBiomes(0.95-2.0) with savanna/desert only; new AridBiomes(1.5-2.0) for badlands; new MountainBiomes(-0.8-0.5) and PeakBiomes(-0.8-1.5) as fallback groups for isle biomes. Removed Plains and Sunflower Plains from HotBiomes. Added Windswept Savanna, Savanna Plateau, Wooded Badlands, Eroded Badlands to appropriate groups.
- **IsleInBiomes set for all mountain/peak/slope biomes**: Meadow (Forest, Plains, Dark Forest, Birch Forest), Grove (Taiga, Snowy Taiga), Cherry Grove (Forest, Dark Forest), Pale Garden (Forest, Dark Forest), Snowy Slopes (Grove, Taiga, Snowy Taiga), Jagged Peaks (Snowy Slopes, Grove), Frozen Peaks (Snowy Slopes, Grove), Stony Peaks (Windswept Savanna, Savanna, Savanna Plateau, Windswept Hills, Meadow), Windswept Hills/Forest/Gravelly (Forest, Taiga, Dark Forest), Windswept Savanna (Savanna, Savanna Plateau). These biomes had empty `IsleInBiome` — they never spawned as isles despite being in the `IsleBiomes` list.
- **BiomeRarity proportional to vanilla frequency**: All base biomes now have `BiomeRarity` matching their proportional frequency in the OverworldBiomeBuilder table (desert 56, plains 30, forest 27, taiga 21, snowy_plains 21, dark_forest 9, swamp 9, jungle 69, bamboo_jungle 31, etc.). Previously all were 100. This fixes overrepresentation of rare biomes.
- **BiomeSize increased** for common biomes: Plains/Forest/Desert/Snowy Tundra to 5 for larger contiguous regions.
- **BiomeRarityWhenIsle/BiomeSizeWhenIsle tuned**: proportional to vanilla isle frequency rather than all at 97/6.
- **AridBiomes group name fixed**: `Wooded Badlands` → `Wooded Badlands Plateau` to match file name.

**2026-06-19 — Hills/Plateau biome configs — CHC profiles + SurfaceAndGroundControl**

- **10 Hills biome configs**: Wooded Hills, Birch Forest Hills, Tall Birch Hills, Desert Hills, Jungle Hills, Bamboo Jungle Hills, Taiga Hills, Snowy Taiga Hills, Dark Forest Hills, Swamp Hills — all had modern-format terrain section (correct 1.18+ headers) but zero `CustomHeightControl` (flat terrain) and empty `SurfaceAndGroundControl` (no block transitions at altitude). Fixed: added 17-point HILLS CHC profile (peaking at 3.0/2.0), `MaxAverageHeight: 30.0`, proper `VolatilityWeight` values, `SurfaceAndGroundControl` with grass→dirt→stone transition at Y=64, and `ReplacedBlocks` for stone at high altitude. Desert Hills uses sand→sandstone surface instead.
- **6 Plateau biome configs**: Savanna Plateau, Badlands Plateau, Wooded Badlands Plateau, Modified Badlands Plateau, Modified Wooded Badlands Plateau, Eroded Badlands — same zero-CHC issue. Fixed: added 17-point PLATEAU CHC profile (peaking at 2.0 with flat top), `MaxAverageHeight: 20.0`, low `Volatility1/2` for stable plateaus. Mesa SGC modes (`Mesa`/`MesaForest`/`MesaBryce`) preserved. Shattered Savanna Plateau already had SGC — added ReplacedBlocks only.
- **Shattered Savanna Plateau**: Added `ReplacedBlocks: (GRASS,STONE,100,319)` for stone exposure at altitude.

**2026-06-19 — Registry duplicate-key fixes + template write fix**

- **WorldPresetTags.NORMAL/EXTENDED**: After registering OTG DimensionPresets and YAML WorldPresets in the registry, rebind the `WorldPresetTags.NORMAL` and `EXTENDED` tags to include them via `WritableRegistry.bindTag()`. MC 1.21.5 `WorldCreationUiState.updatePresetLists()` filters by these tags — presets not in the tag are hidden from the GUI.
- **`registerWorldPresets()`**: Now returns `Holder.Reference<WorldPreset>` (was `void`) for potential callers.
- **Duplicate registry key protection**: `BiomeRegistrar.register()`, `registerDimensionTypes()`, `registerNoiseGenSettings()`, and `registerWorldPresets()` now check if a key is already registered before calling `registry.register()`. This fixes a `Network Protocol Error` disconnect caused by `RegistryDataCollector.loadNewElementsAndTags()` re-firing the mixin during client-side registry sync, where OTG biomes were already present from received server data.
- **TemplateForBiome silently stripped on write**: `IdentitySettings.getSettingsList()` uses reflection to find static `Setting` fields — `OutdatedSettings.IS_TEMPLATE_FOR_BIOME` lives in `OutdatedSettings`, not `IdentitySettings`, so `writeConfigSection()` never wrote it. On the next load, template `.bc` files missing `TemplateForBiome: true` were parsed as `BiomeConfig` and registered as regular OTG biomes, polluting the registry with entries like `otg_default:tag_lush`. Fix: explicitly write `IS_TEMPLATE_FOR_BIOME` in `BiomeSettings.writeConfigSettings()` after the identity section. Defense-in-depth: `BiomeConfigLoader.readSettings()` also checks the file path for `/templates/` directory to infer template status as a fallback for already-corrupted files.

**2026-06-16 — 1.21.5 API migration (compile fixes)**

- **GenerationStep**: Removed `Carving` enum — `applyCarvers()` and `getOrCreateCarvingMask()` no longer take a carving step; `getCarvers()` takes no arguments
- **RegistryAccess methods**: `registryOrThrow()`/`registry()` → `lookupOrThrow()`/`lookup()`, `Holder` access via `Holder.get()` instead of `Holder.value()` where applicable
- **Registry methods**: `getHolder()` → `get()`, `getHolderOrThrow()` → `get(key).orElseThrow()`, `asLookup()` removed (Registry implements HolderGetter directly)
- **CompoundTag**: `getString()`/`getBoolean()`/`getInt()` return `Optional`; `getAllKeys()` → `keySet()`
- **LevelHeightAccessor**: `getMinBuildHeight()`/`getMaxBuildHeight()` → `getMinY()`/`getMaxY()`
- **NoiseGeneratorSettings**: Constructor added `aquifersEnabled` + `oreVeinsEnabled` booleans
- **NoiseRouter**: `aquiferBarrier()`/`aquiferFluidLevelFloodedness()`/`aquiferFluidLevelSpread()` → `barrierNoise()`/`fluidLevelFloodednessNoise()`/`fluidLevelSpreadNoise()`
- **Mob spawning**: `SpawnerData` constructor takes 3 args (`EntityType`, minCount, maxCount); weight passed to `MobSpawnSettings.Builder.addSpawn(category, weight, data)`; `MobSpawnType` → `EntitySpawnReason`; `EntityType.create(ServerLevel)` → `create(Level, EntitySpawnReason)`; `EntityType.loadEntityRecursive` requires `EntitySpawnReason` parameter
- **Entity movement**: `Entity.moveTo(x, int, z, yRot, xRot)` → Y is now `double`
- **Teleportation**: `ServerPlayer.teleportTo`/`Entity.teleportTo` now require `Set<Relative>`, yRot, xRot, boolean (8 params)
- **Block methods**: `entityInside(BlockState, Level, BlockPos, Entity)` → `stepOn(BlockState, Level, BlockPos, Entity)`
- **GameRules**: No-arg constructor removed — requires `FeatureFlagSet`
- **TagParser**: Static `parseTag(String)` removed — use `new TagParser<>(NbtOps.INSTANCE, TagParser.GRAMMAR).parse(new StringReader(...)).getOrThrow()`
- **Registry.bindTags**: Takes `Map<TagKey, HolderSet>` instead of `Map<TagKey, List<Holder>>`
- **ReloadableServerResources.fullRegistries()**: Returns `Holder<ReloadableServerRegistries>` — use `.get().registryAccess()`
- **BlockRenderDispatcher.renderBatched**: `RandomSource` param replaced with `List<BlockModelPart>`
- **Client rendering**: `VertexBuffer` → `MeshData`, `ShaderInstance`/`ShaderProgram` removed, `RenderSystem.viewport/enableDepthTest/depthMask/enableBlend/disableBlend/disableDepthTest` all removed — preview rendering simplified
- **NBT tag getters**: `getAsByte()`/`getAsShort()`/`getAsInt()`/etc → `byteValue()`/`shortValue()`/`intValue()`/etc; `ListTag.getElementType()` removed
- **Build configuration**: Bumped Minecraft version to 1.21.5 (gradle.properties)

**2026-03-30 — Editor codebase refactoring**

- **Viewport3DRenderer**: Extract shared 3D viewport GL code (scissor, viewport calc, depth clear, 4 RenderType draws, state restore) from PreviewScreen, BOBrowserScreen, and BiomeTerrainPreviewScreen into single static utility
- **ScrollablePanel**: Abstract base class for ScrollableListWidget and TreeListWidget — shared scrollbar rendering, scroll clamping, drag handling, mouse wheel
- **PresetReloader**: Static `reload()` utility replaces 3 identical `reloadPresets()` methods across editor screens
- **PropertyGridMode**: Enum (PRESET_EDITOR, BIOME_EDITOR, GROUP_EDITOR) replaces 3-boolean constructor args on PropertyGridWidget
- **BiomeHeightmapGenerator**: Split 155-line `generate()` into `readTerrainParams()` + `computeNoiseColumns()` + `interpolateAndPlaceBlocks()` + `findSurfaceY()` + `placeColumnBlocks()`, added TerrainParams record; generic `getProperty()` replaces 3 near-identical typed helpers
- **BiomeEditorScreen/GroupSettingsScreen**: Split long `init()` methods into focused helpers (initBiomeList, initPropertyGrid, initBottomBar, etc.)
- **SharedWorldGenRegion**: Move identical `fromBlockState`/`toBlockState`/`convertNBT` from Fabric/NeoForge into concrete shared implementations
- **SharedNBTHelper**: Move identical `getNBTFromLocation` into shared base — both platform NBTHelper subclasses now empty
- **Error logging**: Add LOG.warn/error for previously silent failures in BiomeEditorScreen (ini read), WorldSettingsScreen (config fallback), BiomeTerrainPreviewScreen (buffer release), PreviewScreen (preset loading)
- **DRY EditBox registration**: Unify init-time PropertyGrid EditBox registration via `refreshPropertyEditBoxes()` across all 3 editor screens

**2026-03-21 — In-game editor Phase 3: Group Settings**

- **GroupSettingsScreen**: Three-column BiomeGroup editor — group list (left), group params + biome assignment (center), PropertyGridWidget with Override/Merge/OPV flags (right)
- **Group CRUD**: New/Delete groups, edit depth/rarity/temperature range, assign/remove biomes via dual-list with arrow buttons
- **Property overrides**: Per-group biome property overrides stored in `.otg-editor.json` via GroupOverrideStore, loaded into PropertyGrid with Override/Merge/OPV toggles
- **Save**: Writes group lines to .ini via `ConfigWriter.saveBiomeGroups()` + overrides to JSON via `GroupOverrideStore.save()`
- **EditorHubScreen**: Group Settings button now active
- **BiomeEditorScreen override integration**: Save resolves group overrides via `OverrideResolver` before writing .bc — Override/Merge/OPV flags from GroupSettings flow through to biome files

**2026-03-20 — In-game editor Phase 4: BO Store with 3D Preview**

- **BOBrowserScreen 3D viewport**: Embedded 3D BO2/BO3/BO4 preview — select object in tree, renders in right panel via PreviewRenderer/PreviewWorld/OrbitCamera. Drag to orbit, scroll to zoom.
- **Direction buttons**: N/S/E/W snap buttons for camera orientation in BO preview
- **Metadata panel**: Object name, type, size (XxYxZ), block count displayed below viewport
- **Assign to Biome**: BiomeSelectDialog picker + appends `CustomObject(100, name)` to selected biome's .bc file
- **BOBounds expanded**: Now includes blockCount, sizeX/Y/Z for metadata display
- **OrbitCamera setters**: `setTheta()`/`setPhi()` for direction snapping
- **BO Store button**: Enabled in EditorHubScreen, opens BOBrowserScreen from hub
- **BOBrowserScreen parent navigation**: Constructor takes `Screen parent` for correct back-nav from both Hub and BiomeEditor

**2026-03-20 — Editor polish & review fixes**

- **TreeListWidget**: Collapsible folder tree for BO browser — folders expand/collapse on click (▶/▼), sorted folders-first, search auto-expands matching branches
- **Scrollbar**: Visual scrollbar with click-to-jump and drag support on ScrollableListWidget (biome list) and TreeListWidget (BO browser)
- **PropertyGridWidget.syncEditBoxes()**: DRY helper eliminates duplicate `refreshPropertyEditBoxes()` in WorldSettingsScreen and BiomeEditorScreen
- **Resource queue display**: BiomeEditorScreen shows collected ConfigFunction lines (Ore, Tree, etc.) read-only below property grid with scroll
- **PropertyCategory**: Unique display names — `BIOME_TERRAIN` → "Biome Terrain", `BIOME_STRUCTURES` → "Biome Structures"
- **BiomeEditorScreen**: Index-based biome lookup via `filteredBiomeEntries` — fixes wrong file loaded/deleted when subdirectory biomes share names
- **BiomeEditorScreen**: Delete confirmation — double-click required, resets on biome select/new/clone/save
- **PropertyExtractor**: DRY — `extractPresetDefinitions()`/`extractBiomeDefinitions()` delegate to shared `extractDefinitions()` helper
- **BOBrowserScreen**: Rescan guard + legacy `WorldObjects/` folder fallback
- **PropertyGridWidget**: Null guard for uninitialized tabs (no biome selected crash fix)

**2026-03-19 — In-game editor Phase 2: Biome Editor**

- **BiomeEditorScreen**: Split-pane biome editor — left panel with searchable biome list, right panel with PropertyGridWidget for .bc file editing (category tabs, search, Override/Merge toggles)
- **Biome CRUD**: New (generates .bc from defaults), Clone (file copy), Delete — all update biome list immediately
- **BiomeFileScanner**: Discovers .bc/.biome files in preset's Biomes/ folder with legacy WorldBiomes/ fallback
- **Biome property extraction**: `PropertyExtractor.extractBiomeDefinitions()` scans 10 biome ConfigSection subclasses including annotation-generated BiomePlacementSettings/BiomeStructureTagSettings
- **ConfigLoader resource queue**: Collects ConfigFunction lines (Ore, Tree, CustomObject) for read-only display
- **ConfigWriter.createFromDefaults()**: Generates fresh .bc files grouped by category for new biome creation
- **BOBrowserScreen**: Simple BO3/BO4 file browser with search and scrollable list
- **Enum dropdowns**: Click on enum property opens overlay dropdown with value list instead of cycling
- **EditorHubScreen**: Biome Editor button now active

**2026-03-19 — In-game editor Phase 1: UI framework + World Settings**

- **EditorHubScreen**: Hub screen with DimensionPreset selector (◀/▶ cycling), 4 navigation cards (World Settings active, Biome/Group/BO Store disabled for future phases), Preview World button
- **WorldSettingsScreen**: Full property grid editor for `DimensionPresetConfig.ini` — category tabs, search filter, type-dependent editors (boolean toggle, enum cycler, text input), save to disk with comment/structure preservation
- **Data layer**: `PropertyType`/`PropertyCategory` enums, `PropertyDefinition` record, `PropertyValue` with dirty tracking, `PropertyExtractor` (reflects OTG Setting classes), `ConfigLoader`/`ConfigWriter` (round-trip .ini parsing)
- **Widget layer**: `ScrollableListWidget`, `CategoryTabsWidget`, `SearchBoxWidget`, `PropertyRowWidget`, `PropertyGridWidget` — reusable components for future editor phases
- **TitleScreenMixin**: "OTG Editor" button now opens EditorHubScreen instead of PreviewScreen

---

### Release: 0.4.0-dev2

**2026-03-05 — In-game editor & preview system (WIP)**

- **Client source sets**: Added `splitEnvironmentSourceSets()` to shared and fabric modules; client-only code compiles separately from server code
- **OTG Editor button**: TitleScreenMixin injects "OTG Editor" button on the title screen, opens empty PreviewScreen
- **OrbitCamera**: Spherical coordinate camera with rotate/zoom/fitTo for 3D terrain preview
- **PreviewWorld**: `BlockAndTintGetter` implementation backed by `PreviewChunk` array — stores blocks + biomes copied from generated chunks, full brightness fake lighting, biome tint support
- **TempServerManager**: Uses MC's native `createFreshLevel` to spin up IntegratedServer with selected OTG preset; auto-cleans temp saves on stop
- **ChunkGenerationManager**: Spiral-order chunk generation from ServerLevel, feeds chunks into PreviewWorld with progress callbacks
- **PreviewRenderer**: Compiles block meshes via MC's `BlockRenderDispatcher.renderBatched()` into per-section VBOs, renders using `CHUNK_OFFSET` uniform pattern matching MC's `LevelRenderer.renderSectionLayer`
- **PreviewState**: Static state machine (IDLE → WAITING → GENERATING → COMPILING → DONE) that survives screen transitions during `createFreshLevel` flow
- **PreviewScreen**: Full UI with seed input, size/generation level selectors, 3D viewport with orbit camera, generate/clear/back controls
- **ClientTickMixin**: Polls `PreviewState.tick()` to detect when server is ready for chunk generation
- **BO3/BO4 preview**: `BOPreviewHelper` loads custom objects via `OTG.getEngine()` managers, iterates block functions, resolves `IBlockStateMaterial` → `BlockState`, places into PreviewWorld with direct `setBlockState`. Camera auto-fits to object bounds.
- **Preset selector**: Cycles through available DimensionPreset folder names in PreviewScreen
- **Server lifecycle**: TempServerManager stays alive after terrain generation — reused between preview modes, only stops on reset()/screen close
- **Viewport fixes**: `glViewport` set to panel area (was projecting on full window), depth buffer cleared before 3D render, depth test restored after pass, `onClose()` calls `PreviewState.reset()`, `compileSection()` wrapped in try-finally for ByteBufferBuilder leak prevention, `phase`/`statusText` marked volatile
- **Progress bar**: Visual progress bar in viewport during chunk generation
- **Error handling**: OOM catch with cleanup, 60s server start timeout, JVM shutdown hook cleans temp world saves on crash

**2026-03-03–04 — Portal overrides & GameRule fixes**

- **SharedMaterialData interface**: Now implements IBlockStateMaterial for cross-module material comparison
- **WorldPreset portal overrides**: Portal configuration (frame block, ignition item, color) can be overridden per-dimension in WorldPreset YAMLs
- **Portal gating**: Dimensions can disable portal creation/travel entirely via YAML config
- **Respawn-in-dimension mixin**: Players respawn in the OTG dimension they died in (if configured) instead of always respawning in the overworld
- **GameRules YAML override fix**: Fixed bug where world-level GameRule overrides in YAML weren't being applied
- **Default.yaml**: Added default WorldPreset YAML that ships with the mod

**2026-03-03 — Compatibility & display names**

- **Legacy custom objects**: UseWorld/UseBiome custom objects now handled gracefully instead of crashing
- **FeatureSorter cycle crash**: Deduplicated Registry() features to prevent cycle in MC's FeatureSorter (caused infinite loop during biome feature ordering)
- **Portal ClassCastException**: Fixed crash when portal frame block resolution encounters non-block materials
- **WorldPreset display names**: Dynamic display names via Language mixin — WorldPreset names show localized in the MC world creation GUI instead of raw YAML filenames

**2026-03-02 — Code review fixes**

- Nullable DimensionConfig overrides, error handling improvements
- Various correctness fixes from two rounds of code review

**2026-03-01 — WorldPreset system**

Renamed Preset → DimensionPreset, DimensionConfig → WorldPresetConfig across 188 files, 13 classes.

- **WorldPreset YAML**: New config format that composes a full world from multiple DimensionPresets. Defines which presets go in which dimensions, with per-dimension overrides.
- **WorldPresetRegistrar**: YAML configs registered as Minecraft WorldPresets — appear in the world creation GUI alongside vanilla presets.
- **WorldPresetConfigLoader**: Loads all YAMLs from `WorldPresets/` folder.
- **3-layer GameRules**: DimensionPresetConfig.ini → YAML world-level → YAML per-dimension. Each layer can override individual rules.
- **Folder renames**: `Presets/` → `DimensionPresets/`, `DimensionConfigs/` → `WorldPresets/`, `PresetConfig.ini` → `DimensionPresetConfig.ini`

---

### Release: 0.4.0-dev1

**2026-03-01 — GameRules per-dimension**

Full per-dimension GameRules system:

- **LevelGameRulesMixin**: Intercepts `Level.getGameRules()` to return dimension-specific rules. Works on both Fabric and NeoForge via shared mixin.
- **GameRuleManager**: Static map of dimension → GameRules, populated during dimension creation, cleared on server stop.
- **GameRuleApplier**: Merges GameRules from 3 layers — DimensionPresetConfig.ini (base) → WorldPreset YAML world-level → WorldPreset YAML per-dimension.
- **52 GameRule settings**: 32 original + 20 new 1.21.1 rules (ENDER_PEARLS_VANISH_ON_DEATH, DO_VINES_SPREAD, PLAYERS_SLEEPING_PERCENTAGE, etc.)
- **OTGWorldStorage**: Renamed from DimensionStorage, v2 format persists dimensions + GameRules to `otg_world_data.json`.
- Fixed copy-paste bug where DO_MOB_SPAWNING getter returned DO_MOB_LOOT value.

**2026-02-19 — BO4Config split**

Split monolithic BO4Config.java (1769 LOC) into focused components:

- **BO4BlockStorage** (326 LOC): Block data management and material resolution
- **BO4DataSerializer** (485 LOC): Binary serialization/deserialization
- **BO4ConfigWriter** (292 LOC): INI file writing
- BO4Config reduced to 733 LOC — just config loading and field access

**2026-02-18 — Logger refactor & BO4Config cleanup**

- **Unified logger**: Merged 3 separate logger implementations (Logger, OTGLogger, BasicLogger) into single OTGLogger. Added LogFormatter with `{}` placeholder support and lazy evaluation. ILogger.init() changed from 8-boolean to EnumSet<LogCategory>. ~416 call sites migrated.
- **BO4Config cleanup**: Extracted helpers, added try-with-resources, removed dead code. Preparation for BO4Config split.
- **BLANK material null-guard**: Fixed NPE when SharedWorldGenRegion.setBlock encounters BLANK material data.

**2026-02-17 — Mixin deduplication & more shared extraction**

- Moved 6 shared mixins to `platforms/shared/` (BiomeDataMixin, WorldPresetTagsMixin, LevelGameRulesMixin, ChunkAccessAccessor, MappedRegistryAccessor, MinecraftServerAccessor)
- Extracted SharedNBTHelper (~270 lines deduplicated), SharedOTGBiomeProvider (~160 lines)
- Collapsed BiomePlatformAdapter into shared — both platform implementations were identical
- Deleted dead code: OTGTemplateHandler, ShowWorldPresetsCommand, BiomeSyncWrapper
- Swamp biome flattened to 50/50 water/land ratio

**2026-02-16 — Biome loading redesign**

Decomposed the monolithic SharedLegacyBiomeLoader into clean, testable components:

- **BiomePlanResolver**: Pure logic for resolving biome assignments from preset config. 7 unit tests.
- **BiomePlan**: Immutable data class for biome resolution output.
- **BiomeFactory**: Creates MC Biome objects from OTG biome configs.
- **BiomeRegistrar**: Isolates MC registry mutation.
- **SharedDimensionPresetBiomeLoader**: Orchestrates the pipeline via composition.
- Deleted 430-line BiomeRegistryNames class frozen at 1.16.5 biome names.
- Fixed bit packing validation, ocean temperature index inversion, dynamic biome array sizing.

**2026-02-16 — Platform deduplication & cleanup**

Massive refactoring day — extracted most runtime logic from Fabric/NeoForge into `platforms/shared/`:

- SharedOTGChunkGenerator, SharedWorldGenRegion, SharedLegacyBiomeLoader
- Portal system, material classes (MaterialData, MaterialReader, Materials, MaterialTag, LegacyMaterials)
- ShadowChunkGenerator, ChunkBuffer, Biome, DimensionHelper
- **Deleted legacy Forge platform**: 74 files, ~17k lines of dead code targeting old Forge (not NeoForge). Fabric + NeoForge only going forward.
- Cleaned up dead interfaces, abandoned event hooks, dead EntityCategory enum, unused biomeColorMap, debug code

**Bug fixes**

- **Dynamic world bounds**: Replaced remaining deprecated WORLD_DEPTH/WORLD_HEIGHT with runtime world bounds
- **printStackTrace cleanup**: Replaced all bare `printStackTrace()` calls with structured OTGLog logging
- **NeoForge backports**: 3 bugfixes that were Fabric-only ported to NeoForge, removed duplicate NoiseParamRegistry
- **RuntimeException bombs**: Replaced 4 RuntimeException throws in BO4CustomStructure with OTGLog.error (mod no longer crashes server on recoverable BO4 errors)
- **River generation**: Fixed rivers not generating when RandomRivers=false
- **Structure tag injection disabled**: Temporarily disabled biome→structure tag injection due to bindTags() performance bug on NeoForge (~60s stall)

---

### Release: 0.2.0-dev5

**2026-02-15 (cont.) — FromImage fix, BO connection states**

- **FromImage OOM**: Fixed OutOfMemoryError when using FromImage biome mode — biome feature ordering cycle caused exponential memory growth.
- **BO connection states**: Updated block connection states for glass panes, iron bars, fences, and walls in BO objects. Objects placed in-world now correctly connect to adjacent blocks instead of floating as standalone pillars.

---

### Release: 0.2.0-dev4

**2026-02-15 — Commands, structures, terrain fixes**

- **UndergroundBiomeRarity**: New config setting to control underground biome spawn frequency.

**BO2/3/4 Commands**

- `/otg flushcache` — clear all cached custom objects
- `/otg spawn <object>` — spawn a BO2/BO3/BO4 at player position
- `/otg structure <object>` — start BO4 structure from branch
- `/otg export [template] [-e excludes] [-t tileentities]` — export selection to BO3 (WorldEdit integration)
- `/otg exportbo4data` — export BO4 data files for all custom objects
- Command infrastructure with CommandWorldAccessor for cross-platform world access

**Vanilla structures in OTG biomes**

- Inject OTG biomes into vanilla structure biome tags on both Fabric and NeoForge
- Villages, strongholds, witch huts, etc. now generate in OTG biomes that match the right temperature/category
- BiomeStructureTagConfig stored during biome registration, StructureTagMapper maps biomes to structure tags

**Bug fixes**

- **Village buildings missing**: JigsawStructureData delta parameter was using bounding box maxY instead of ground level delta, causing massively wrong terrain density around structures — villages only generated paths/farmland, no buildings
- **Steep biome borders**: Implemented vanilla weight halving for biome height blending. Neighbors with higher BiomeHeight get blending weight halved, creating softer transitions instead of cliffs
- **BO3 spawn cache**: spawnForced now properly registers objects in structure cache
- **BO4 structure plotting**: Fixed spiral chunk search and plotBo4Structure() cache integration
- **Beard fill (structure terrain)**: Fixed terrain adaptation around structures — ground under buildings was being carved instead of filled
- **Swamp flattening**: Swamp biome terrain now properly flattened

---

### Release: 0.2.0-dev3

**2026-02-13–14 — 3D underground biomes & cave improvements**

- **3D underground biomes**: Full system for assigning different biomes underground based on Y level and cheese cave noise. Biomes like Lush Caves, Dripstone Caves, Deep Dark appear only inside actual cave voids, not in solid rock.
  - UndergroundBiomeResolver with pre-computed lookup tables
  - Surface height estimation to determine underground threshold
  - Underground biomes excluded from 2D surface layer system
  - Example configs included (LushCaves, DripstoneCaves, DeepDark)
- **Surface-relative cave suppression**: Caves now thin out gradually near the surface using quadratic falloff instead of a hard cutoff. Prevents cheese holes in mountain tops while still allowing cave breakthroughs in valleys.
- **NeoForge carver unification**: Both platforms now use identical noise evaluation for cave carving.

**2026-02-12 — Noise cave carving**

- **1.18+ noise caves**: Added configurable noise cave density carving. Caves now use 3D noise sampling instead of just legacy carvers.
- **Thread-safe CustomObjectResource**: Fixed lazy init race condition that caused crashes with C2ME parallel chunk loading.

---

### Release: 0.2.0-dev2

**2026-02-08 — NeoForge platform & compatibility fixes**

- **NeoForge 1.21.1 support**: Multi-loader build from single codebase via Architectury. NeoForge platform with DeferredRegister, Data Attachments (replacing CCA), NeoForge event bus wiring.
- **C2ME compatibility**: Replaced FifoMap with ThreadSafeLRUCache to fix ConcurrentModificationException when C2ME workers access CustomStructureCache concurrently.
- **RegistryLoaderMixin fix**: Added @Local(ordinal=1) to disambiguate List type erasure, guard against client-side registry sync.

---

### Release: 0.2.0-dev1

**2026-02-07 — MC 1.21.1 port**

Full port from 1.20.1 to 1.21.1 (Java 21, Fabric API 0.116.7, Architectury 13.0.8):

- Migrated ResourceLocation constructors to static factory methods (MC 1.21 change)
- Migrated ChunkGenerator/BiomeSource codecs to MapCodec (MC 1.20.5+ change)
- Removed Executor parameter from fillFromNoise (MC 1.21 change)
- Rewrote RegistryLoaderMixin for 1.21.1 RegistryDataLoader changes — now uses @Local from MixinExtras instead of LocalCapture
- Updated WorldPresetTagsMixin for updateRegistryTags() signature change
- Fixed BootstapContext → BootstrapContext typo (MC fixed their own typo in 1.20.5)

---

## Minecraft 1.20.1

### Release: 0.2.0-dev10

**2026-02-06 — Thread-safe chunk generation**

- **Caffeine caching**: Replaced all FifoMap/LinkedHashMap caches with Caffeine-backed ThreadSafeLRUCache. Fixes ConcurrentModificationException with C2ME.
- **Synchronous fillFromNoise**: Removed ShadowChunkGenerator worker threads. Vanilla's ForkJoinPool already parallelizes chunk generation — same approach C2ME uses. Shadow gen kept only for BO4 objects.
- **C2ME compatibility**: OTG now works alongside C2ME for multiplayer chunk generation scaling.

**2026-02-04 — Performance optimization (benchmark-driven)**

Built a headless terrain snapshot testing system with benchmark command for measuring generation throughput without running Minecraft.

Optimizations applied (with before/after measurements):

- **Flatten GRAD array**: Converted 2D int[][] to flat int[] in SimplexNoiseSampler for better CPU cache locality.
- **BiomeSettings cache**: Cache per-chunk instead of per-block — reduces getBiomeSettings() calls from 98,304 to 256 per chunk (384x reduction).
- **Noise buffer reuse**: Pre-allocate double[][][] via ThreadLocal instead of 4KB allocation per chunk.
- **Bit shifts in ChunkCoordinate**: Replace `* 16` with `<< 4`, use Math.floorDiv for region coords. (Credit: Meldexun/OTG)
- **LRU cache**: Replaced FifoMap with LRUCache that evicts least-recently-used instead of oldest entries. Better hit rates for temporal locality patterns. (Credit: Meldexun/OTG)
- **O(log n) biome selection**: TreeMap.higherEntry() instead of linear iteration. (Credit: Meldexun/OTG)
- **SurfaceSettings cache**: Avoid 3 redundant method calls per block in populateNoise inner loop.
- **Hoist MutableBoolean**: Move allocation outside carver loops — eliminates 400-900 allocations per carve call.
- **Eliminate ThreadLocal boxing**: Replace ThreadLocal<Integer>/ThreadLocal<Double> with single ThreadLocal holding primitive fields.

**2026-02-03 — Portals, CI/CD, mob spawning**

- **Nether-style portals**: Implemented portal system for OTG dimensions — build a portal frame, light it, teleport between dimensions. Portal frame blocks and linking logic are configurable per preset.
- **Portal refactors**: Extracted PortalConfigResolver, PortalConfigLookup, DimensionKeys, DimensionNameUtils to shared modules for future NeoForge reuse.
- **CI/CD**: GitHub Actions workflow with auto-release on push. Builds both platforms, uploads artifacts.
- **Mob spawning**: Implemented mob spawning during chunk generation on Fabric (was previously missing).

**2026-02-02 — Dynamic dimensions system**

- **Runtime dimension creation**: Full system for creating OTG dimensions at runtime via datapack generation. Core types, platform interfaces, Fabric dimension manager, safe spawn logic, teleport command.
- **Dimension JSON handling**: Streamlined dimension type and noise settings JSON generation.

**2026-02-01 — Terrain tuning & deepslate**

- **Terrain constant tuning**: Adjusted BiomeHeight, BiomeVolatility, and noise parameters for 1.18+ terrain that no longer looks like a jagged mess.
- **VolatilityWeight fix**: Increased VolatilityWeight values from 0-1 range to ~48 range — the low values caused horrible jagged terrain.
- **DEEPSLATE material**: Added deepslate to the material registry for proper resource generation below Y=0.

**2026-01-24 — Terrain smoothing**

- **SmoothRadius increase**: Bumped smoothing radius for better terrain transitions between biomes.
- **Biome tag mapping**: Added FabricBiomeTagMapper to map OTG biomes to vanilla structure tags, enabling villages/strongholds/etc. in OTG biomes.

**2026-01-23 — World height & crash fixes**

- **Dynamic world height**: Replaced hardcoded WORLD_DEPTH/WORLD_HEIGHT constants with runtime MinY/MaxY from the world. Fixes terrain generation on custom-height worlds (1.18+ changed overworld to Y -64..319).
- **Crash safeguards**: Added null-checks and bounds validation throughout chunk generation to prevent empty chunks and NPEs during terrain population.
- **Tree spawning materials**: Added MaterialSet support so tree objects can validate block placement against a set of allowed materials.
- **OTG dimension type**: Expanded PresetSettings with dimension type fields (fixed time, respawn anchor, etc.) for custom OTG dimensions.
