package com.pg85.otg.config.settings.preset;

import com.pg85.otg.config.io.SettingsMap;
import com.pg85.otg.config.settingtype.Setting;
import com.pg85.otg.config.settingtype.Settings;
import com.pg85.otg.config.settings.ConfigSection;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.util.minecraft.OTGDimensionType;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.OptionalLong;

@Builder
@Getter
public class DimensionSettings extends ConfigSection {
    private final OTGDimensionType dimensionType;
    private final List<String> defaultDimensions;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private final OptionalLong fixedTime;
    private final boolean hasSkyLight;
    private final boolean hasCeiling;
    private final boolean ultraWarm;
    private final boolean natural;
    private final double coordinateScale;
    private final boolean createDragonFight;
    private final boolean piglinSafe;
    private final boolean bedWorks;
    private final boolean respawnAnchorWorks;
    private final boolean hasRaids;
    private final int minY;
    private final int height;
    private final int logicalHeight;
    private final String infiniburn;
    private final String effectsLocation;
    private final double ambientLight;
    private final int monsterSpawnLightLimit;
    private final int monsterSpawnLightVariationMin;
    private final int monsterSpawnLightVariationMax;

    public static final Setting<List<String>> DEFAULT_DIMENSIONS = Settings.stringListSetting(
            "DefaultDimensions", new String[]{"this", "minecraft:the_nether", "minecraft:the_end"},
            t -> ((DimensionSettings) t).getDefaultDimensions(),
            "The default dimensions for the world, overworld, nether, end by default.",
            "Order of entries matter; first will be loaded as overworld, second as nether, third as the end.",
            "Any additional dimensions will be loaded as OTG dimensions.",
            "To specify another preset, use the format \"otg:my_preset\".",
            "The current preset can be referenced either with its full name, or \"this\"."
    );
    public static final Setting<OTGDimensionType> DIMENSION_TYPE = Settings.enumSetting(
            "DimensionType", OTGDimensionType.OTG,
            t -> ((DimensionSettings) t).getDimensionType(),
            "The dimension type, either OVERWORLD, NETHER, END, or OTG.",
            "Defaults to OTG.",
            "To use the settings below, must be set to OTG. Otherwise, dimension settings of the given dimensions apply."
    );
    public static final Setting<String> INFINIBURN = Settings.stringSetting(
            "InfiniBurn", "minecraft:infiniburn_overworld",
            t -> ((DimensionSettings) t).getInfiniburn(),
            "Infiniburn block tag registry key, minecraft:infiniburn_overworld by default.",
            "Can be either overworld/nether/end (or potentially modded)."
    );
    public static final Setting<String> EFFECTS_LOCATION = Settings.stringSetting(
            "EffectsLocation", "minecraft:overworld",
            t -> ((DimensionSettings) t).getEffectsLocation(),
            "Effects registry key, minecraft:overworld by default.",
            "Can be either overworld/nether/end (or potentially modded)."
    );
    public static final Setting<Integer> MIN_Y = Settings.intSetting(
            "MinY", -64, Constants.WORLD_START_MIN_Y, Constants.WORLD_END_MAX_Y-15,
            t -> ((DimensionSettings) t).getMinY(),
            "Minimum Y value for this dimension, -64 by default (1.18+ Overworld).",
            "Must be a multiple of 16."
    );
    public static final Setting<Integer> HEIGHT = Settings.intSetting(
            "Height", 384, 16, Constants.WORLD_MAX_HEIGHT,
            t -> ((DimensionSettings) t).getHeight(),
            "Total height of this dimension, 384 by default (1.18+ Overworld: -64 to 319).",
            "Must be a multiple of 16."
    );
    public static final Setting<Integer> LOGICAL_HEIGHT = Settings.intSetting(
            "LogicalHeight", 384, 16, Constants.WORLD_MAX_HEIGHT,
            t -> ((DimensionSettings) t).getLogicalHeight(),
            "The max height at which nether portals and chorus fruits can safely teleport players in this dimension.",
            "Does not affect existing portals.",
            "Cannot be greater than "+HEIGHT.getName()+"."
    );
    public static final Setting<Long> FIXED_TIME = Settings.longSetting(
            "FixedTime", -1L, -1L, 24000L,
            t -> {
                DimensionSettings ds = (DimensionSettings) t;
                if (ds.getFixedTime().isEmpty()) {
                    return -1L;
                }
                return ds.getFixedTime().getAsLong();
            },
            "The time this dimension is fixed at, from 0 to 24000.",
            "-1 by default, meaning disabled, so time passes normally.",
            "Vanilla Nether uses 18000, End uses 6000."
    );
    public static final Setting<Boolean> HAS_SKYLIGHT = Settings.booleanSetting(
            "HasSkylight", true,
            t -> ((DimensionSettings) t).isHasSkyLight(),
            "Whether this dimension uses a skylight, defaults to true.",
            "Vanilla nether and end use false, nether combines this with AmbientLight:0.1."
    );
    public static final Setting<Boolean> HAS_CEILING = Settings.booleanSetting(
            "HasCeiling", false,
            t -> ((DimensionSettings) t).isHasCeiling(),
            "Whether this dimension has a ceiling, affects mob spawning, weather (thunder), maps.",
            "Defaults to false, vanilla nether uses true."
    );
    public static final Setting<Boolean> ULTRA_WARM = Settings.booleanSetting(
            "UltraWarm", false,
            t -> ((DimensionSettings) t).isUltraWarm(),
            "Whether water evaporates in this dimension. Also appears to affect lava/lava flow.",
            "Defaults to false. Vanilla nether uses true."
    );
    public static final Setting<Boolean> NATURAL = Settings.booleanSetting(
            "Natural", true,
            t -> ((DimensionSettings) t).isNatural(),
            "When set to false, mobs do not spawn from portals and players cannot use beds in this dimension.",
            "Defaults to true."
    );
    public static final Setting<Boolean> CREATE_DRAGON_FLIGHT = Settings.booleanSetting(
            "CreateDragonFight", false,
            t -> ((DimensionSettings) t).isCreateDragonFight(),
            "Probably starts a dragon fight, we think. Try it, what could possibly go wrong?"
    );
    public static final Setting<Boolean> PIGLIN_SAFE = Settings.booleanSetting(
            "PiglinSafe", false,
            t -> ((DimensionSettings) t).isPiglinSafe(),
            "Whether this dimension can spawn piglins, false by default."
    );
    public static final Setting<Boolean> BED_WORKS = Settings.booleanSetting(
            "BedWorks", true,
            t -> ((DimensionSettings) t).isBedWorks(),
            "Whether beds can be used to sleep and skip time in this dimension, true by default."
    );
    public static final Setting<Boolean> RESPAWN_ANCHOR_WORKS = Settings.booleanSetting(
            "RespawnAnchorWorks", true,
            t -> ((DimensionSettings) t).isRespawnAnchorWorks(),
            "Whether RespawnAnchorBlocks can be used, false by default."
    );
    public static final Setting<Boolean> HAS_RAIDS = Settings.booleanSetting(
            "HasRaids", true,
            t -> ((DimensionSettings) t).isHasRaids(),
            "Whether the dimension has raids, true by default."
    );
    public static final Setting<Double> COORDINATE_SCALE = Settings.doubleSetting(
            "CoordinateScale", 1.0D, 0.0D, Integer.MAX_VALUE,
            t -> ((DimensionSettings) t).getCoordinateScale(),
            "The amount of blocks traveled compared to other dimensions.",
            "1 by default, same as vanilla overworld, nether uses 8."
    );
    public static final Setting<Double> AMBIENT_LIGHT = Settings.doubleSetting(
            "AmbientLight", 0.0D, 0.0D, Integer.MAX_VALUE,
            t -> ((DimensionSettings) t).getAmbientLight(),
            "The base ambient light level for the world, 0.0 for overworld/end, 0.1 for nether."
    );
    public static final Setting<Integer> MONSTER_SPAWN_LIGHT_LIMIT = Settings.intSetting(
            "MonsterSpawnLightLevel", 7, 0, 15,
            t -> ((DimensionSettings) t).getMonsterSpawnLightLimit(),
            "The light level at which monsters can spawn, 7 by default."
    );
    public static final Setting<Integer> MONSTER_SPAWN_LIGHT_VARIATION_MIN = Settings.intSetting(
            "MonsterSpawnLightVariationMin", 0, 0, 15,
            t -> ((DimensionSettings) t).getMonsterSpawnLightVariationMin(),
            "The minimum light level variation for monster spawning, 0 by default."
    );
    public static final Setting<Integer> MONSTER_SPAWN_LIGHT_VARIATION_MAX = Settings.intSetting(
            "MonsterSpawnLightVariationMax", 7, 0, 15,
            t -> ((DimensionSettings) t).getMonsterSpawnLightVariationMax(),
            "The maximum light level variation for monster spawning, 7 by default.",
            "Must be greater than or equal to "+MONSTER_SPAWN_LIGHT_VARIATION_MIN.getName()+"."
    );

    public static DimensionSettings getDimensionSettings(SettingsMap reader) {
        var dimensionSettingsBuilder = builder();
        dimensionSettingsBuilder.defaultDimensions(reader.getSetting(DEFAULT_DIMENSIONS));
        dimensionSettingsBuilder.dimensionType(reader.getSetting(DIMENSION_TYPE));
        long fixedTime = reader.getSetting(FIXED_TIME);
        dimensionSettingsBuilder.fixedTime(fixedTime == -1L ? OptionalLong.empty() : OptionalLong.of(fixedTime));
        dimensionSettingsBuilder.hasSkyLight(reader.getSetting(HAS_SKYLIGHT));
        dimensionSettingsBuilder.hasCeiling(reader.getSetting(HAS_CEILING));
        dimensionSettingsBuilder.ultraWarm(reader.getSetting(ULTRA_WARM));
        dimensionSettingsBuilder.natural(reader.getSetting(NATURAL));
        dimensionSettingsBuilder.coordinateScale(reader.getSetting(COORDINATE_SCALE));
        dimensionSettingsBuilder.createDragonFight(reader.getSetting(CREATE_DRAGON_FLIGHT));
        dimensionSettingsBuilder.piglinSafe(reader.getSetting(PIGLIN_SAFE));
        dimensionSettingsBuilder.bedWorks(reader.getSetting(BED_WORKS));
        dimensionSettingsBuilder.respawnAnchorWorks(reader.getSetting(RESPAWN_ANCHOR_WORKS));
        dimensionSettingsBuilder.hasRaids(reader.getSetting(HAS_RAIDS));
        dimensionSettingsBuilder.minY(reader.getSetting(MIN_Y));
        dimensionSettingsBuilder.height(reader.getSetting(HEIGHT));
        dimensionSettingsBuilder.logicalHeight(reader.getSetting(LOGICAL_HEIGHT));
        dimensionSettingsBuilder.infiniburn(reader.getSetting(INFINIBURN));
        dimensionSettingsBuilder.effectsLocation(reader.getSetting(EFFECTS_LOCATION));
        dimensionSettingsBuilder.ambientLight(reader.getSetting(AMBIENT_LIGHT).floatValue());
        dimensionSettingsBuilder.monsterSpawnLightLimit(reader.getSetting(MONSTER_SPAWN_LIGHT_LIMIT));
        dimensionSettingsBuilder.monsterSpawnLightVariationMin(reader.getSetting(MONSTER_SPAWN_LIGHT_VARIATION_MIN));
        dimensionSettingsBuilder.monsterSpawnLightVariationMax(reader.getSetting(MONSTER_SPAWN_LIGHT_VARIATION_MAX));

        return dimensionSettingsBuilder.build();
    }
    
    public void writeSettings(SettingsMap writer) {
        writer.putSetting(DimensionSettings.DEFAULT_DIMENSIONS, this);
        writer.putSetting(DimensionSettings.DIMENSION_TYPE, this);
        writer.putSetting(DimensionSettings.FIXED_TIME, this);
        writer.putSetting(DimensionSettings.HAS_SKYLIGHT, this);
        writer.putSetting(DimensionSettings.HAS_CEILING, this);
        writer.putSetting(DimensionSettings.ULTRA_WARM, this);
        writer.putSetting(DimensionSettings.NATURAL, this);
        writer.putSetting(DimensionSettings.COORDINATE_SCALE, this);
        writer.putSetting(DimensionSettings.CREATE_DRAGON_FLIGHT, this);
        writer.putSetting(DimensionSettings.PIGLIN_SAFE, this);
        writer.putSetting(DimensionSettings.BED_WORKS, this);
        writer.putSetting(DimensionSettings.RESPAWN_ANCHOR_WORKS, this);
        writer.putSetting(DimensionSettings.HAS_RAIDS, this);
        writer.putSetting(DimensionSettings.MIN_Y, this);
        writer.putSetting(DimensionSettings.HEIGHT, this);
        writer.putSetting(DimensionSettings.LOGICAL_HEIGHT, this);
        writer.putSetting(DimensionSettings.INFINIBURN, this);
        writer.putSetting(DimensionSettings.EFFECTS_LOCATION, this);
        writer.putSetting(DimensionSettings.AMBIENT_LIGHT, this);
        writer.putSetting(DimensionSettings.MONSTER_SPAWN_LIGHT_LIMIT, this);
        writer.putSetting(DimensionSettings.MONSTER_SPAWN_LIGHT_VARIATION_MIN, this);
        writer.putSetting(DimensionSettings.MONSTER_SPAWN_LIGHT_VARIATION_MAX, this);
    }

    @Override
    public String getSectionName() {
        return "Dimension Settings";
    }
}