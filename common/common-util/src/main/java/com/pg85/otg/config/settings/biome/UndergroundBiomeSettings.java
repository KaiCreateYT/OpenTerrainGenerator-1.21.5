package com.pg85.otg.config.settings.biome;

import com.pg85.otg.config.io.SettingsMap;
import com.pg85.otg.config.settingtype.Setting;
import com.pg85.otg.config.settingtype.Settings;
import com.pg85.otg.config.settings.ConfigSection;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class UndergroundBiomeSettings extends ConfigSection {

    private final boolean isUndergroundBiome;
    private final int undergroundMinY;
    private final int undergroundMaxY;
    private final int undergroundPriority;
    private final float undergroundBiomeRarity;
    private final int undergroundRegionSize;
    private final float undergroundVerticalScale;
    private final float caveCheeseScale;
    private final float caveSpaghetti3dScale;
    private final float caveSpaghetti2dScale;
    private final float caveNoodleScale;
    private final float cavePillarScale;
    private final float minSurfaceTemperature;
    private final float maxSurfaceTemperature;
    private final float minSurfaceWetness;
    private final float maxSurfaceWetness;

    // Surface biome settings for underground biome control
    private final int undergroundBiomeStartOffset;
    private final List<String> allowedUndergroundBiomes;
    private final List<String> disallowedUndergroundBiomes;

    @Override
    public String getSectionName() {
        return "Underground Biome Settings";
    }

    @Override
    public String[] getSectionComment() {
        return new String[]{
            "Settings for 3D underground biome placement.",
            "Set IsUndergroundBiome: true to mark this biome as an underground biome.",
            "Underground biomes appear below surface biomes based on Y-range, priority, and surface biome conditions.",
            "",
            "For surface biomes: use UndergroundBiomeStartOffset, AllowedUndergroundBiomes, DisallowedUndergroundBiomes",
            "to control which underground biomes can appear below this surface biome."
        };
    }

    // === Underground biome identity ===

    public static final Setting<Boolean> IS_UNDERGROUND_BIOME = Settings.booleanSetting(
            "IsUndergroundBiome", false,
            t -> ((UndergroundBiomeSettings) t).isUndergroundBiome(),
            "Set to true to mark this biome as an underground biome.",
            "Underground biomes appear below surface biomes based on conditions defined here."
    );

    public static final Setting<Integer> UNDERGROUND_MIN_Y = Settings.intSetting(
            "UndergroundMinY", -64, -2048, 2048,
            t -> ((UndergroundBiomeSettings) t).getUndergroundMinY(),
            "Minimum Y coordinate where this underground biome can appear."
    );

    public static final Setting<Integer> UNDERGROUND_MAX_Y = Settings.intSetting(
            "UndergroundMaxY", 320, -2048, 2048,
            t -> ((UndergroundBiomeSettings) t).getUndergroundMaxY(),
            "Maximum Y coordinate where this underground biome can appear."
    );

    public static final Setting<Integer> UNDERGROUND_PRIORITY = Settings.intSetting(
            "UndergroundPriority", 10, 0, 1000,
            t -> ((UndergroundBiomeSettings) t).getUndergroundPriority(),
            "Priority for this underground biome. Lower value = higher priority.",
            "When multiple underground biomes match the same position, the one with lowest priority wins."
    );

    public static final Setting<Float> UNDERGROUND_BIOME_RARITY = Settings.floatSetting(
            "UndergroundBiomeRarity", 100.0f, 0.0f, 100.0f,
            t -> ((UndergroundBiomeSettings) t).getUndergroundBiomeRarity(),
            "Percentage of the qualifying underground volume this biome fills (coverage).",
            "Driven by a 3D noise field: 100 = fills all qualifying volume,",
            "50 = roughly half (organic blobs with gaps of normal cave between them),",
            "0 = never. Default 100."
    );

    public static final Setting<Integer> UNDERGROUND_REGION_SIZE = Settings.intSetting(
            "UndergroundRegionSize", 96, 8, 1024,
            t -> ((UndergroundBiomeSettings) t).getUndergroundRegionSize(),
            "Approximate size in blocks of the organic regions this biome forms.",
            "Larger = bigger continuous blobs, smaller = more scattered patches. Default 96."
    );

    public static final Setting<Float> UNDERGROUND_VERTICAL_SCALE = Settings.floatSetting(
            "UndergroundVerticalScale", 0.7f, 0.05f, 4.0f,
            t -> ((UndergroundBiomeSettings) t).getUndergroundVerticalScale(),
            "Vertical stretch of the regions.",
            "Below 1.0 stretches blobs vertically (taller columns); above 1.0 flattens them.",
            "Default 0.7."
    );

    // === Per-region cave shaping (Phase 2) ===

    public static final Setting<Float> CAVE_CHEESE_SCALE = Settings.floatSetting(
            "CaveCheeseScale", 1.0f, 0.0f, 8.0f,
            t -> ((UndergroundBiomeSettings) t).getCaveCheeseScale(),
            "Density multiplier for cheese (large cavern) caves inside this biome's region.",
            "1.0 = unchanged, >1 = bigger/more open caverns, <1 = smaller, 0 = none. Default 1.0.");

    public static final Setting<Float> CAVE_SPAGHETTI3D_SCALE = Settings.floatSetting(
            "CaveSpaghetti3dScale", 1.0f, 0.0f, 8.0f,
            t -> ((UndergroundBiomeSettings) t).getCaveSpaghetti3dScale(),
            "Density multiplier for 3D spaghetti tunnels and cave entrances inside this biome's region.",
            "1.0 = unchanged, >1 = more/wider tunnels, 0 = none. Default 1.0.");

    public static final Setting<Float> CAVE_SPAGHETTI2D_SCALE = Settings.floatSetting(
            "CaveSpaghetti2dScale", 1.0f, 0.0f, 8.0f,
            t -> ((UndergroundBiomeSettings) t).getCaveSpaghetti2dScale(),
            "Density multiplier for 2D spaghetti tunnels inside this biome's region.",
            "1.0 = unchanged, >1 = more/wider tunnels, 0 = none. Default 1.0.");

    public static final Setting<Float> CAVE_NOODLE_SCALE = Settings.floatSetting(
            "CaveNoodleScale", 1.0f, 0.0f, 8.0f,
            t -> ((UndergroundBiomeSettings) t).getCaveNoodleScale(),
            "Density multiplier for noodle (thin) caves inside this biome's region.",
            "1.0 = unchanged, >1 = more thin caves, 0 = none. Default 1.0.");

    public static final Setting<Float> CAVE_PILLAR_SCALE = Settings.floatSetting(
            "CavePillarScale", 1.0f, 0.0f, 8.0f,
            t -> ((UndergroundBiomeSettings) t).getCavePillarScale(),
            "Density multiplier for pillars (solid columns that block carving) inside this biome's region.",
            "1.0 = unchanged, >1 = more/thicker pillars, 0 = none. Default 1.0.");

    // === Conditions based on surface biome ===

    public static final Setting<Float> MIN_SURFACE_TEMPERATURE = Settings.floatSetting(
            "MinSurfaceTemperature", 0.0f, 0.0f, 2.0f,
            t -> ((UndergroundBiomeSettings) t).getMinSurfaceTemperature(),
            "Minimum BiomeTemperature of the surface biome above for this underground biome to appear."
    );

    public static final Setting<Float> MAX_SURFACE_TEMPERATURE = Settings.floatSetting(
            "MaxSurfaceTemperature", 2.0f, 0.0f, 2.0f,
            t -> ((UndergroundBiomeSettings) t).getMaxSurfaceTemperature(),
            "Maximum BiomeTemperature of the surface biome above for this underground biome to appear."
    );

    public static final Setting<Float> MIN_SURFACE_WETNESS = Settings.floatSetting(
            "MinSurfaceWetness", 0.0f, 0.0f, 1.0f,
            t -> ((UndergroundBiomeSettings) t).getMinSurfaceWetness(),
            "Minimum BiomeWetness of the surface biome above for this underground biome to appear."
    );

    public static final Setting<Float> MAX_SURFACE_WETNESS = Settings.floatSetting(
            "MaxSurfaceWetness", 1.0f, 0.0f, 1.0f,
            t -> ((UndergroundBiomeSettings) t).getMaxSurfaceWetness(),
            "Maximum BiomeWetness of the surface biome above for this underground biome to appear."
    );

    // === Surface biome control over underground ===

    public static final Setting<Integer> UNDERGROUND_BIOME_START_OFFSET = Settings.intSetting(
            "UndergroundBiomeStartOffset", 8, 0, 256,
            t -> ((UndergroundBiomeSettings) t).getUndergroundBiomeStartOffset(),
            "How many blocks below the estimated surface height underground biomes start.",
            "Only applies to surface biomes (non-underground). Default: 8."
    );

    public static final Setting<List<String>> ALLOWED_UNDERGROUND_BIOMES = Settings.stringListSetting(
            "AllowedUndergroundBiomes", new String[]{},
            t -> ((UndergroundBiomeSettings) t).getAllowedUndergroundBiomes(),
            "If set, ONLY these underground biomes can appear below this surface biome.",
            "Leave empty to allow all underground biomes that match conditions.",
            "Example: AllowedUndergroundBiomes: LushCaves, DripstoneCaves"
    );

    public static final Setting<List<String>> DISALLOWED_UNDERGROUND_BIOMES = Settings.stringListSetting(
            "DisallowedUndergroundBiomes", new String[]{},
            t -> ((UndergroundBiomeSettings) t).getDisallowedUndergroundBiomes(),
            "Underground biomes listed here will NEVER appear below this surface biome.",
            "Example: DisallowedUndergroundBiomes: DeepDark"
    );

    /** @return cave-type density multipliers ordered {cheese, spaghetti3d, spaghetti2d, noodle, pillar}. */
    public float[] getCaveScales() {
        return new float[] { caveCheeseScale, caveSpaghetti3dScale, caveSpaghetti2dScale, caveNoodleScale, cavePillarScale };
    }

    // === Factory method ===

    public static UndergroundBiomeSettings getUndergroundBiomeSettings(SettingsMap reader) {
        UndergroundBiomeSettingsBuilder builder = UndergroundBiomeSettings.builder();

        builder.isUndergroundBiome(reader.getSetting(IS_UNDERGROUND_BIOME));
        builder.undergroundMinY(reader.getSetting(UNDERGROUND_MIN_Y));
        builder.undergroundMaxY(reader.getSetting(UNDERGROUND_MAX_Y));
        builder.undergroundPriority(reader.getSetting(UNDERGROUND_PRIORITY));
        builder.undergroundBiomeRarity(reader.getSetting(UNDERGROUND_BIOME_RARITY));
        builder.undergroundRegionSize(reader.getSetting(UNDERGROUND_REGION_SIZE));
        builder.undergroundVerticalScale(reader.getSetting(UNDERGROUND_VERTICAL_SCALE));
        builder.caveCheeseScale(reader.getSetting(CAVE_CHEESE_SCALE));
        builder.caveSpaghetti3dScale(reader.getSetting(CAVE_SPAGHETTI3D_SCALE));
        builder.caveSpaghetti2dScale(reader.getSetting(CAVE_SPAGHETTI2D_SCALE));
        builder.caveNoodleScale(reader.getSetting(CAVE_NOODLE_SCALE));
        builder.cavePillarScale(reader.getSetting(CAVE_PILLAR_SCALE));
        builder.minSurfaceTemperature(reader.getSetting(MIN_SURFACE_TEMPERATURE));
        builder.maxSurfaceTemperature(reader.getSetting(MAX_SURFACE_TEMPERATURE));
        builder.minSurfaceWetness(reader.getSetting(MIN_SURFACE_WETNESS));
        builder.maxSurfaceWetness(reader.getSetting(MAX_SURFACE_WETNESS));
        builder.undergroundBiomeStartOffset(reader.getSetting(UNDERGROUND_BIOME_START_OFFSET));
        builder.allowedUndergroundBiomes(reader.getSetting(ALLOWED_UNDERGROUND_BIOMES));
        builder.disallowedUndergroundBiomes(reader.getSetting(DISALLOWED_UNDERGROUND_BIOMES));

        return builder.build();
    }
}
