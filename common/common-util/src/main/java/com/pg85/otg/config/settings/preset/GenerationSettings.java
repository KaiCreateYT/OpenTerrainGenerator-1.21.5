package com.pg85.otg.config.settings.preset;

import com.pg85.otg.config.ConfigFile;
import com.pg85.otg.config.ConfigFunction;
import com.pg85.otg.config.biome.BiomeGroupFunction;
import com.pg85.otg.config.biome.BiomeGroupManager;
import com.pg85.otg.config.biome.TemplateBiome;
import com.pg85.otg.config.io.IConfigFunctionProvider;
import com.pg85.otg.config.io.SettingsMap;
import com.pg85.otg.config.settingtype.Setting;
import com.pg85.otg.config.settingtype.Settings;
import com.pg85.otg.config.settings.ConfigSection;
import com.pg85.otg.constants.settings.BiomeMode;
import com.pg85.otg.util.OTGLog;
import com.pg85.otg.util.logging.LogCategory;
import lombok.Builder;
import lombok.Getter;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
public class GenerationSettings extends ConfigSection {
    private final List<String> worldBiomes;
    private final List<String> blackListedBiomes;
    private final int biomeRarityScale;
    private final boolean oldGroupRarity;
    private final int generationDepth;
    private final int landFuzzy;
    private final int landRarity;
    private final int landSize;
    private final int oceanBiomeSize;
    private final String defaultOceanBiome;
    private final String defaultWarmOceanBiome;
    private final String defaultLukewarmOceanBiome;
    private final String defaultColdOceanBiome;
    private final String defaultFrozenOceanBiome;
    private final BiomeMode biomeMode;
    private final double frozenOceanTemperature;
    private final boolean frozenOcean;
    private final List<String> isleBiomes;
    private final List<String> borderBiomes;
    private final boolean randomRivers;
    private final int riverRarity;
    private final int riverSize;
    private final boolean riversEnabled;
    private final boolean oldLandRarity;
    private final boolean forceLandAtSpawn;
    private final BiomeGroupManager biomeGroupManager;
    private final List<TemplateBiome> templateBiomes;

    public static final Setting<BiomeMode> BIOME_MODE = Settings.enumSetting(
            "BiomeMode", BiomeMode.Normal,
            t -> ((GenerationSettings) t).getBiomeMode(),
            "Possible biome modes:",
            "Normal - standard random generation with biome groups, uses all features.",
            "FromImage - biome layout defined by an image file."
    );
    public static final Setting<String> DEFAULT_OCEAN_BIOME = Settings.stringSetting(
            "DefaultOceanBiome", "Ocean",
            t -> ((GenerationSettings) t).getDefaultOceanBiome(),
            "Set the default Ocean biome for this world."
    );
    public static final Setting<String> DEFAULT_FROZEN_OCEAN_BIOME = Settings.stringSetting(
            "DefaultFrozenOceanBiome", "Ocean",
            t -> ((GenerationSettings) t).getDefaultFrozenOceanBiome(),
            "The default Frozen Ocean biome for this world."
    );
    public static final Setting<String> DEFAULT_WARM_OCEAN_BIOME = Settings.stringSetting(
            "DefaultWarmOceanBiome", "Ocean",
            t -> ((GenerationSettings) t).getDefaultWarmOceanBiome(),
            "Set the default Warm Ocean biome for this world."
    );
    public static final Setting<String> DEFAULT_LUKEWARM_OCEAN_BIOME = Settings.stringSetting(
            "DefaultLukewarmOceanBiome", "Ocean",
            t -> ((GenerationSettings) t).getDefaultLukewarmOceanBiome(),
            "Set the default Lukewarm Ocean biome for this world."
    );
    public static final Setting<String> DEFAULT_COLD_OCEAN_BIOME = Settings.stringSetting(
            "DefaultColdOceanBiome", "Ocean",
            t -> ((GenerationSettings) t).getDefaultColdOceanBiome(),
            "Set the default Cold Ocean biome for this world."
    );
    public static final Setting<Integer> GENERATION_DEPTH = Settings.intSetting(
            "GenerationDepth", 10, 1, 20,
            t -> ((GenerationSettings) t).getGenerationDepth(),
            "Defines the maximum number BiomeSize, RiverSize and LandSize can be set to.",
            "All size settings such as Biome Group Size, RiverSize, LandSize (in the DimensionPresetConfig.ini), and BiomeSize (in Biome Configs) must be between 0 (largest) and GenerationDepth (smallest).",
            "Increasing GenerationDepth by one will roughly double the size of all biomes, similarly decreasing it by 1 will half the size of all biomes.",
            "Small values (1-2) and Large values (20+) may affect generator performance.",
            "This setting is also used in BiomeMode:FromImage when ImageMode is set to ContinueNormal"
    );
    public static final Setting<Integer> BIOME_RARITY_SCALE = Settings.intSetting(
            "BiomeRarityScale", 100, 1, Integer.MAX_VALUE,
            t -> ((GenerationSettings) t).getBiomeRarityScale(),
            "Max biome rarity from 1 to infinity.",
            "By default this is 100, but you can raise it for fine-grained control,",
            "or to create biomes with a chance of occurring smaller than 1/100."
    );
    public static final Setting<Integer> LAND_RARITY = Settings.intSetting(
            "LandRarity", 99, 0, 100,
            t -> ((GenerationSettings) t).getLandRarity(),
            "Land rarity from 100 to 1. Higher numbers result in more land."
    );
    public static final Setting<Integer> LAND_SIZE = Settings.intSetting(
            "LandSize", 0, 0, 20,
            t -> ((GenerationSettings) t).getLandSize(),
            "Land size from 0 to GenerationDepth.",
            "Higher LandSize numbers will make the size of the land smaller.",
            "Landsize number should always be lower than any biome groups."
    );
    public static final Setting<Integer> OCEAN_BIOME_SIZE = Settings.intSetting(
            "OceanBiomeSize", 6, 0, 20,
            t -> ((GenerationSettings) t).getOceanBiomeSize(),
            "Ocean biome size 0 to GenerationDepth. ",
                    "Higher OceanBiomeSize numbers will make the size of the ocean biomes smaller."
    );
    public static final Setting<Integer> LAND_FUZZY = Settings.intSetting(
            "LandFuzzy", 5, 0, 20,
            t -> ((GenerationSettings) t).getLandFuzzy(),
            "Generates more lakes (via small ocean biomes) at the edges of continents.",
            "As a side effect, the continent will also get a bit larger. ",
            "Must be from 0 to GenerationDepth minus LandSize."
    );
    public static final Setting<Integer> RIVER_RARITY = Settings.intSetting(
            "RiverRarity", 4, 0, 20,
            t -> ((GenerationSettings) t).getRiverRarity(),
            "Controls the rarity of rivers. Must be from 0 to GenerationDepth.",
            "A higher number means more rivers.",
            "To define which rivers flow through which biomes see the individual biome configs."
    );
    public static final Setting<Integer> RIVER_SIZE = Settings.intSetting(
            "RiverSize", 0, 0, 20,
            t -> ((GenerationSettings) t).getRiverSize(),
            "Controls the size of rivers. Can range from 0 to GenerationDepth minus RiverRarity.",
            "Making this larger will make the rivers larger, without affecting how often rivers will spawn."
    );
    public static final Setting<Boolean> RIVERS_ENABLED = Settings.booleanSetting(
            "RiversEnabled", true,
            t -> ((GenerationSettings) t).isRiversEnabled(),
            "Set this to false to prevent the river generator from doing anything."
    );
    public static final Setting<Boolean> RANDOM_RIVERS = Settings.booleanSetting(
            "RandomRivers", false,
            t -> ((GenerationSettings) t).isRandomRivers(),
            "When this setting is false, rivers follow the biome borders most of the time.",
            "Set this setting to true to disable this behavior."
    );
    public static final Setting<Double> FROZEN_OCEAN_TEMPERATURE = Settings.doubleSetting(
            "OceanFreezingTemperature", 0.15, 0, 2,
            t -> ((GenerationSettings) t).getFrozenOceanTemperature(),
            "This is the maximum biome temperature when a biome is still considered cold.",
            "Water in oceans nearby cold biomes freezes if FrozenOcean is set to true.",
            "Temperature reference from vanilla Minecraft: < 0.15 for snow, 0.15 - 0.95 for rain, or > 1.0 for dry."
    );
    public static final Setting<Boolean> FROZEN_OCEAN = Settings.booleanSetting(
            "FrozenOcean", true,
            t -> ((GenerationSettings) t).isFrozenOcean(),
            "Can be true or false, makes the water of the oceans near a cold biome frozen.",
            "The definition of 'cold' is controlled by " + FROZEN_OCEAN_TEMPERATURE,
            "Set this to false to stop the ocean from freezing near when an \"ice area\" intersects with an ocean."
    );
    public static final Setting<Boolean> FORCE_LAND_AT_SPAWN = Settings.booleanSetting(
            "ForceLandAtSpawn", true,
            t -> ((GenerationSettings) t).isForceLandAtSpawn(),
            "If enabled, land will always spawn at or near 0,0"
    );
    public static final Setting<Boolean> OLD_GROUP_RARITY = Settings.booleanSetting(
            "OldGroupRarity", false,
            t -> ((GenerationSettings) t).isOldGroupRarity(),
            "Whether or not OTG should use the old group rarity (pre-1.16)."
    );
    public static final Setting<Boolean> OLD_LAND_RARITY = Settings.booleanSetting(
            "OldLandRarity", false,
            t -> ((GenerationSettings) t).isOldLandRarity(),
            "Whether or not OTG should use the old land rarity.",
            "Disabling this will make LandRarity work as a percentage"
    );
    public static final Setting<List<String>> ISLE_BIOMES = Settings.stringListSetting(
            "IsleBiomes", new String[]{"Deep Ocean", "MushroomIsland",
                    "Ice Mountains", "DesertHills", "ForestHills", "Forest", "TaigaHills",
                    "JungleHills", "Cold Taiga Hills", "Birch Forest Hills", "Extreme Hills+",
                    "Mesa Plateau", "Mesa Plateau F", "Mesa Plateau M", "Mesa Plateau F M",
                    "Mesa (Bryce)", "Mega Taiga Hills", "Mega Spruce Taiga Hills"},
            t -> ((GenerationSettings) t).getIsleBiomes(),
            "Isle biomes are biomes which spawn inside another biome (e.g. an island in an ocean).",
            "As well as listing every isle biome here, you must set IsleInBiome in each biome config too.",
            "Biome name is case sensitive."
    );
    public static final Setting<List<String>> BORDER_BIOMES = Settings.stringListSetting(
            "BorderBiomes", new String[]{"JungleEdge", "JungleEdge M", "MushroomIslandShore", "Beach",
                    "Extreme Hills Edge", "Desert", "Taiga"},
            t -> ((GenerationSettings) t).getBorderBiomes()
    );
    public static final Setting<List<String>> BLACKLISTED_BIOMES = Settings.stringListSetting(
            "BlacklistedBiomes", new String[]{""},
            t -> ((GenerationSettings) t).getBlackListedBiomes()
    );



    public static GenerationSettings getGenerationSettings(DimensionPresetSettings presetConfig, SettingsMap reader, IConfigFunctionProvider biomeResourcesManager, List<String> biomes, Path settingsDir) {
        var generationSettingsBuilder = builder();

        generationSettingsBuilder.worldBiomes(biomes);

        generationSettingsBuilder.biomeRarityScale(reader.getSetting(BIOME_RARITY_SCALE));
        generationSettingsBuilder.generationDepth(reader.getSetting(GENERATION_DEPTH));
        generationSettingsBuilder.oldGroupRarity(reader.getSetting(OLD_GROUP_RARITY));
        generationSettingsBuilder.oldLandRarity(reader.getSetting(OLD_LAND_RARITY));
        generationSettingsBuilder.landFuzzy(reader.getSetting(LAND_FUZZY));
        generationSettingsBuilder.landRarity(reader.getSetting(LAND_RARITY));
        generationSettingsBuilder.landSize(reader.getSetting(LAND_SIZE));
        generationSettingsBuilder.forceLandAtSpawn(reader.getSetting(FORCE_LAND_AT_SPAWN));
        generationSettingsBuilder.oceanBiomeSize(reader.getSetting(OCEAN_BIOME_SIZE));
        generationSettingsBuilder.defaultOceanBiome(reader.getSetting(DEFAULT_OCEAN_BIOME));
        generationSettingsBuilder.defaultWarmOceanBiome(reader.getSetting(DEFAULT_WARM_OCEAN_BIOME));
        generationSettingsBuilder.defaultLukewarmOceanBiome(reader.getSetting(DEFAULT_LUKEWARM_OCEAN_BIOME));
        generationSettingsBuilder.defaultColdOceanBiome(reader.getSetting(DEFAULT_COLD_OCEAN_BIOME));
        generationSettingsBuilder.defaultFrozenOceanBiome(reader.getSetting(DEFAULT_FROZEN_OCEAN_BIOME));
        generationSettingsBuilder.frozenOcean(reader.getSetting(FROZEN_OCEAN));
        generationSettingsBuilder.frozenOceanTemperature(reader.getSetting(FROZEN_OCEAN_TEMPERATURE));
        generationSettingsBuilder.randomRivers(reader.getSetting(RANDOM_RIVERS));
        generationSettingsBuilder.riverRarity(reader.getSetting(RIVER_RARITY));
        generationSettingsBuilder.riverSize(reader.getSetting(RIVER_SIZE));
        generationSettingsBuilder.riversEnabled(reader.getSetting(RIVERS_ENABLED));

        // Keep the configured mode here; FromImageLayer performs strict runtime validation with
        // the actual preset directory and throws a clear error if the image is missing.
        generationSettingsBuilder.biomeMode(reader.getSetting(BIOME_MODE));

        // BiomeGroups requires that values like genDepth are initialized
        generationSettingsBuilder.templateBiomes(readTemplateBiomes(reader, presetConfig, biomeResourcesManager));
        generationSettingsBuilder.biomeGroupManager(readBiomeGroups(reader, presetConfig, biomeResourcesManager));
        // Temporary fix since we can't access generationDepth inside the biome group constructor without separating GenerationSettings into two classes
        generationSettingsBuilder.biomeGroupManager.clampGenerationDepth(generationSettingsBuilder.generationDepth);

        generationSettingsBuilder.biomeGroupManager.filterBiomes(biomes);

        generationSettingsBuilder.isleBiomes(ConfigFile.filterBiomes(reader.getSetting(ISLE_BIOMES), biomes));
        generationSettingsBuilder.borderBiomes(ConfigFile.filterBiomes(reader.getSetting(BORDER_BIOMES), biomes));
        generationSettingsBuilder.blackListedBiomes(reader.getSetting(BLACKLISTED_BIOMES));
        return generationSettingsBuilder.fixSettings().build();
    }

    private static ArrayList<TemplateBiome> readTemplateBiomes(SettingsMap reader, DimensionPresetSettings presetSettings, IConfigFunctionProvider biomeResourcesManager) {
        var templateBiomes = new ArrayList<TemplateBiome>();
        for (ConfigFunction<DimensionPresetSettings> res : reader.getConfigFunctions(presetSettings, biomeResourcesManager)) {
            if (res != null) {
                if (res instanceof TemplateBiome tb) {
                    templateBiomes.add(tb);
                }
            }
        }
        return templateBiomes;
    }

    private static BiomeGroupManager readBiomeGroups(SettingsMap reader, DimensionPresetSettings presetSettings, IConfigFunctionProvider biomeResourcesManager) {
        var biomeGroupManager = new BiomeGroupManager();
        for (ConfigFunction<DimensionPresetSettings> res : reader.getConfigFunctions(presetSettings, biomeResourcesManager)) {
            if (res != null) {
                if (res instanceof BiomeGroupFunction) {
                    biomeGroupManager.registerGroup((BiomeGroupFunction) res);
                }
            }
        }
        return biomeGroupManager;
    }

    @Override
    public String getSectionName() {
        return "Generation Settings";
    }

    public static class GenerationSettingsBuilder {
        public GenerationSettingsBuilder fixSettings() {
            checkLandSize();
            checkLandFuzzy();
            checkRiverRarity();
            checkRiverSize();
            return this;
        }
        private void checkLandSize() {
            landSize = Math.min(landSize, generationDepth);
        }
        private void checkLandFuzzy() {
            landFuzzy = Math.min(landFuzzy, generationDepth - landSize);
        }
        private void checkRiverRarity() {
            riverRarity = Math.min(riverRarity, generationDepth);
        }
        private void checkRiverSize() {
            riverSize = Math.min(riverSize, generationDepth - riverRarity);
        }
    }
}