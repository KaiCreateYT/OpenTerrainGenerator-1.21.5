package com.pg85.otg.config.settings.preset;

import com.pg85.otg.config.io.SettingsMap;
import com.pg85.otg.config.settingtype.Setting;
import com.pg85.otg.config.settingtype.Settings;
import com.pg85.otg.config.settings.ConfigSection;
import com.pg85.otg.constants.Constants;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class TerrainSettings extends ConfigSection {
    private final double fractureHorizontal;
    private final double fractureVertical;
    private final int worldHeightCap;
    private final int worldHeightScale;
    private final boolean betterSnowFall;
    private final int waterLevelMax;
    private final int waterLevelMin;
    private final int carverLavaBlockHeight;
    private final boolean useVanillaTerrain;
    private final double continentalScale;
    private final double erosionScale;
    private final double weirdnessScale;
    private final double baseHeightFraction;
    private final double biomeHeightWeight;
    private final double falloffSteepness;
    private final double noiseAmplitude;
    private final boolean useVanillaCaves;

    public static final Setting<Boolean> BETTER_SNOW_FALL = Settings.booleanSetting(
            "BetterSnowFall", false,
            t -> ((TerrainSettings) t).isBetterSnowFall(),
            "When set to false, 1 layer of snow falls on the highest block only.",
            "When set to true, the number of layers (1-8) is dependent on biome temperature.",
            "Higher altitudes have lower temperatures, so snow becomes deeper higher up.",
            "Also causes snow to fall through leaves, leaves can carry 3 layers while the rest falls through."
    );
    public static final Setting<Integer> WORLD_HEIGHT_SCALE_BITS = Settings.intSetting(
            "WorldHeightScaleBits", 8, 5, 9,
            t -> ((TerrainSettings) t).getWorldHeightScale(),
            "The height scale of the world. Increasing this by one doubles the terrain height of the world,",
            "substracting one halves the terrain height. Values must be between 5 and 9, inclusive.",
            "For 1.18+ worlds with 384 height, use 8 (256) or 9 (512)."
    );
    public static final Setting<Integer> WORLD_HEIGHT_CAP_BITS = Settings.intSetting(
            "WorldHeightCapBits", 9, 5, 9,
            t -> ((TerrainSettings) t).getWorldHeightCap(),
            "The height cap of the world. A cap of 7 will make sure that there is no terrain above 128 (y=2^7). Near this cap less and less terrain generates with no terrain above this cap.",
            "Values must be between 5 and 9 (inclusive), and may not be lower that WorldHeightScaleBits.",
            "For 1.18+ worlds with 384 height, use 9 (512)."
    );
    public static final Setting<Integer> WATER_LEVEL_MAX = Settings.intSetting(
            "WaterLevelMax", 63, Constants.WORLD_START_MIN_Y, Constants.WORLD_END_MAX_Y,
            t -> ((TerrainSettings) t).getWaterLevelMax(),
            "Set water level. Every empty block under this level down to min will be fill water or another block from WaterBlock."
    );
    public static final Setting<Integer> WATER_LEVEL_MIN = Settings.intSetting(
            "WaterLevelMin", 0, Constants.WORLD_START_MIN_Y, Constants.WORLD_END_MAX_Y,
            t -> ((TerrainSettings) t).getWaterLevelMin(),
            "Set water level. Every empty block over this level up to max will be fill water or another block from WaterBlock."
    );
    public static final Setting<Integer> CARVER_LAVA_BLOCK_HEIGHT = Settings.intSetting(
            "CarverLavaBlockHeight", 10, Constants.WORLD_START_MIN_Y, Constants.WORLD_END_MAX_Y,
            t -> ((TerrainSettings) t).getCarverLavaBlockHeight(),
            "All air blocks are replaced to CarverLavaBlock from world bottom up to CarverLavaBlockHeight.",
            "For example, vanilla replaces air in caves with lava up to Y10.",
            "Defaults to: 10"
    );
    public static final Setting<Boolean> USE_VANILLA_TERRAIN = Settings.booleanSetting(
            "UseVanillaTerrain", false,
            t -> ((TerrainSettings) t).isUseVanillaTerrain(),
            "When true, uses vanilla's finalDensity noise for terrain generation instead of OTG's own noise/falloff formula.",
            "Terrain shape, height, and caves are determined by vanilla noise dimensions (continentalness, erosion, etc.)."
    );
    public static final Setting<Boolean> USE_VANILLA_CAVES = Settings.booleanSetting(
            "UseVanillaCaves", true,
            t -> ((TerrainSettings) t).isUseVanillaCaves(),
            "When true and UseVanillaTerrain is also true, vanilla caves baked into finalDensity are used.",
            "Set to false to use OTG's separate noise cave carver pass instead (UseModernCaves must also be true).",
            "Only applies when UseVanillaTerrain is true."
    );
    public static final Setting<Double> CONTINENTAL_SCALE = Settings.doubleSetting(
            "ContinentalScale", 0.2, 0.0, 10.0,
            t -> ((TerrainSettings) t).getContinentalScale(),
            "Weight of vanilla continentalness noise in terrain height (UseVanillaTerrain only).",
            "Continentalness controls the land/ocean split at the largest scale.",
            "Higher values = more dramatic continent/ocean height differences."
    );
    public static final Setting<Double> EROSION_SCALE = Settings.doubleSetting(
            "ErosionScale", 0.1, 0.0, 10.0,
            t -> ((TerrainSettings) t).getErosionScale(),
            "Weight of vanilla erosion noise in terrain height (UseVanillaTerrain only).",
            "Erosion controls the flatness vs steepness of terrain.",
            "Higher values = more dramatic flat-to-steep transitions."
    );
    public static final Setting<Double> WEIRDNESS_SCALE = Settings.doubleSetting(
            "WeirdnessScale", 0.05, 0.0, 10.0,
            t -> ((TerrainSettings) t).getWeirdnessScale(),
            "Weight of vanilla weirdness (ridges) noise in terrain height (UseVanillaTerrain only).",
            "Weirdness adds local variation — hills, bumps, and valleys within a biome region.",
            "Higher values = more local terrain variation."
    );
    public static final Setting<Double> BASE_HEIGHT_FRACTION = Settings.doubleSetting(
            "BaseHeightFraction", 0.46875, 0.0, 1.0,
            t -> ((TerrainSettings) t).getBaseHeightFraction(),
            "Where the terrain surface sits as a fraction of world height when all noise values are 0.",
            "0.47 = surface roughly at half world height (default).",
            "0.25 = low surface with lots of sky, 0.75 = high surface with deep underground."
    );
    public static final Setting<Double> BIOME_HEIGHT_WEIGHT = Settings.doubleSetting(
            "BiomeHeightWeight", 0.125, 0.0, 1.0,
            t -> ((TerrainSettings) t).getBiomeHeightWeight(),
            "How much OTG biome height config shifts the terrain surface (UseVanillaTerrain only).",
            "0 = all biomes at same baseline, 0.125 = default.",
            "Higher values create more dramatic height differences between biomes."
    );
    public static final Setting<Double> FALLOFF_STEEPNESS = Settings.doubleSetting(
            "FalloffSteepness", 6.0, 0.1, 100.0,
            t -> ((TerrainSettings) t).getFalloffSteepness(),
            "Controls how sharply terrain transitions from solid to air.",
            "Higher values = thinner transition zone = sharper terrain edges.",
            "Lower values = thicker transition zone = smoother, more blobby terrain.",
            "Default 6.0 produces ~80 block transition at default biome volatility (0.3)."
    );
    public static final Setting<Double> NOISE_AMPLITUDE = Settings.doubleSetting(
            "NoiseAmplitude", 1.0, 0.0, 1000.0,
            t -> ((TerrainSettings) t).getNoiseAmplitude(),
            "Global multiplier for terrain noise contribution.",
            "Scales the effect of Volatility1/Volatility2 from all biomes uniformly.",
            "1.0 = noise at face value, higher = more chaotic terrain, 0 = falloff-only terrain."
    );

    public static final Setting<Double> FRACTURE_HORIZONTAL = Settings.doubleSetting(
            "FractureHorizontal", 0, -500, 500,
            t -> ((TerrainSettings) t).getFractureHorizontal(),
            "Can increase (values greater than 0) or decrease (values less than 0) how much the landscape is fractured horizontally.",
            "Values less than 0 will 'relax' the terrain, leading to more gradual and smoother height transitions."
    );
    public static final Setting<Double> FRACTURE_VERTICAL = Settings.doubleSetting(
            "FractureVertical", 0, -500, 500,
            t -> ((TerrainSettings) t).getFractureVertical(),
            "Can increase (values greater than 0) or decrease (values less than 0) how much the landscape is fractured vertically.",
            "Values above 0 will lead to large cliffs/overhangs, floating islands, and/or a cavern world depending on other settings.",
            "Values less than 0 will make terrain volatility more 'spiky' but lessen the likelihood of overhangs and floating terrain."
    );

    public static TerrainSettings getTerrainSettings(SettingsMap reader) {
        var builder = builder();

        builder.fractureHorizontal(reader.getSetting(FRACTURE_HORIZONTAL));
        builder.fractureVertical(reader.getSetting(FRACTURE_VERTICAL));
        builder.worldHeightCap(1 << reader.getSetting(WORLD_HEIGHT_CAP_BITS));
        builder.worldHeightScale(1 << reader.getSetting(WORLD_HEIGHT_SCALE_BITS));
        builder.betterSnowFall(reader.getSetting(BETTER_SNOW_FALL));
        builder.waterLevelMax(reader.getSetting(WATER_LEVEL_MAX));
        builder.waterLevelMin(reader.getSetting(WATER_LEVEL_MIN));
        builder.carverLavaBlockHeight(reader.getSetting(CARVER_LAVA_BLOCK_HEIGHT));
        builder.useVanillaTerrain(reader.getSetting(USE_VANILLA_TERRAIN));
        builder.useVanillaCaves(reader.getSetting(USE_VANILLA_CAVES));
        builder.continentalScale(reader.getSetting(CONTINENTAL_SCALE));
        builder.erosionScale(reader.getSetting(EROSION_SCALE));
        builder.weirdnessScale(reader.getSetting(WEIRDNESS_SCALE));
        builder.baseHeightFraction(reader.getSetting(BASE_HEIGHT_FRACTION));
        builder.biomeHeightWeight(reader.getSetting(BIOME_HEIGHT_WEIGHT));
        builder.falloffSteepness(reader.getSetting(FALLOFF_STEEPNESS));
        builder.noiseAmplitude(reader.getSetting(NOISE_AMPLITUDE));

        int configVersion = reader.getVersion();
        if (configVersion < 2) {
            // In older configs, the values were stored as negative values and then converted
            // to positive values in the getter. This is no longer necessary.
            builder.fractureHorizontal(builder.fractureHorizontal < 0.0D
                    ? 1.0D / (Math.abs(builder.fractureHorizontal) + 1.0D)
                    : builder.fractureHorizontal + 1.0D);
            builder.fractureVertical(builder.fractureVertical < 0.0D
                    ? 1.0D / (Math.abs(builder.fractureVertical) + 1.0D)
                    : builder.fractureVertical + 1.0D);
        }

        return builder.fixSettings().build();
    }

    @Override
    public String getSectionName() {
        return "Terrain Settings";
    }

    public static class TerrainSettingsBuilder {
        public TerrainSettingsBuilder fixSettings() {
            checkWaterLevelMax();
            checkFractionHorizontal();
            checkFractionVertical();
            return this;
        }
        private void checkWaterLevelMax() {
            waterLevelMax = Math.max(waterLevelMax, waterLevelMin);
        }
        private void checkFractionHorizontal() {
            if (fractureHorizontal < 0) {
                fractureHorizontal = 1.0D / Math.abs(fractureHorizontal);
            }
        }
        private void checkFractionVertical() {
            if (fractureVertical < 0) {
                fractureVertical = 1.0D / Math.abs(fractureVertical);
            }
        }
    }
}