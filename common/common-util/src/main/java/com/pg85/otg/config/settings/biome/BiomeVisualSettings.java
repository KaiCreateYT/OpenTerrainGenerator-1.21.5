package com.pg85.otg.config.settings.biome;

import com.pg85.otg.config.io.SettingsMap;
import com.pg85.otg.config.settingtype.Setting;
import com.pg85.otg.config.settingtype.Settings;
import com.pg85.otg.config.settings.ConfigSection;
import com.pg85.otg.config.settings.preset.VisualSettings;
import com.pg85.otg.constants.settings.GrassColorModifier;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.util.Color;
import com.pg85.otg.util.OTGLog;
import com.pg85.otg.util.biome.ColorSet;
import com.pg85.otg.util.biome.SimpleColorSet;
import com.pg85.otg.util.logging.LogCategory;
import lombok.Builder;
import lombok.Getter;

import java.util.Objects;
import java.util.Optional;

@Builder
@Getter
public class BiomeVisualSettings extends ConfigSection {
    private final VisualSettings parent;
    private final Color skyColor;
    private final Color waterColor;
    private final ColorSet waterColorControl;
    private final Color grassColor;
    private final ColorSet grassColorControl;
    private final GrassColorModifier grassColorModifier;
    private final Color foliageColor;
    private final ColorSet foliageColorControl;
    private final Color fogColor;
    private final float fogDensity;
    private final Color waterFogColor;
    private final String particleType;
    private final String music;
    private final int musicMinDelay;
    private final int musicMaxDelay;
    private final boolean replaceCurrentMusic;
    private final String ambientSound;
    private final String moodSound;
    private final int moodSoundDelay;
    private final int moodSearchRange;
    private final double moodOffset;
    private final String additionsSound;
    private final double additionsTickChance;
    private final float particleProbability;
    private final float biomeTemperature;
    private final float biomeWetness;

    @Override
    public String getSectionName() {
        return "Biome Visual Settings";
    }

    public static final Setting<Color> SKY_COLOR = Settings.colorSetting(
            "SkyColor", "0x7BA5FF",
            t -> ((BiomeVisualSettings) t).getSkyColor(),
            "The color of the sky in this biome. Can be any hexadecimal color value."
    );
    public static final Setting<String> PARTICLE_TYPE = Settings.stringSetting(
            "ParticleType", "",
            t -> ((BiomeVisualSettings) t).getParticleType(),
            "Biome particle type, for example minecraft:white_ash.",
            "Use the \"otg particles\" console command to get a list of particles."
    );
    public static final Setting<String> MUSIC = Settings.stringSetting(
            "Music", "",
            t -> ((BiomeVisualSettings) t).getMusic(),
            "Music for the biome, takes a resource location. Leave empty to disable. Examples: ",
            "Music: minecraft:music_disc.cat", "Music: minecraft:music.nether.basalt_deltas"
    );
    public static final Setting<String> AMBIENT_SOUND = Settings.stringSetting(
            "AmbientSound", "",
            t -> ((BiomeVisualSettings) t).getAmbientSound(),
            "Ambient sound for the biome. Leave empty to disable. Example:",
            "AmbientSound: minecraft:ambient.cave"
    );
    public static final Setting<String> MOOD_SOUND = Settings.stringSetting(
            "MoodSound", "minecraft:ambient.cave",
            t -> ((BiomeVisualSettings) t).getMoodSound(),
            "Mood sound for the biome. Leave empty to disable. Example:",
            "MoodSound: minecraft:ambient.crimson_forest.mood"
    );
    public static final Setting<String> ADDITIONS_SOUND = Settings.stringSetting(
            "AdditionsSound", "",
            t -> ((BiomeVisualSettings) t).getAdditionsSound(),
            "Additions sound for the biome. Leave empty to disable. Example:",
            "AdditionsSound: minecraft:ambient.soul_sand_valley.additions"
    );
    public static final Setting<Integer> MUSIC_MIN_DELAY = Settings.intSetting(
            "MusicMinDelay", 0, 0, Integer.MAX_VALUE,
            t -> ((BiomeVisualSettings) t).getMusicMinDelay(),
            "Minimum delay in ticks before the music starts playing again."
    );
    public static final Setting<Integer> MUSIC_MAX_DELAY = Settings.intSetting(
            "MusicMaxDelay", 0, 0, Integer.MAX_VALUE,
            t -> ((BiomeVisualSettings) t).getMusicMaxDelay(),
            "Maximum delay in ticks before the music starts playing again."
    );
    public static final Setting<Integer> MOOD_SOUND_DELAY = Settings.intSetting(
            "MoodSoundDelay", 6000, 0, Integer.MAX_VALUE,
            t -> ((BiomeVisualSettings) t).getMoodSoundDelay(),
            "Delay in ticks before the mood sound starts playing again."
    );
    public static final Setting<Integer> MOOD_SEARCH_RANGE = Settings.intSetting(
            "MoodSearchRange", 8, 0, Integer.MAX_VALUE,
            t -> ((BiomeVisualSettings) t).getMoodSearchRange(),
            "How far from the player a mood sound can play"
    );
    public static final Setting<Color> WATER_COLOR = Settings.colorSetting(
            "WaterColor", "0xFFFFFF",
            t -> ((BiomeVisualSettings) t).getWaterColor(),
            "The color of the water in this biome. Can be any hexadecimal color value."
    );
    public static final Setting<Color> GRASS_COLOR = Settings.colorSetting(
            "GrassColor", "0xFFFFFF",
            t -> ((BiomeVisualSettings) t).getGrassColor(),
            "The color of the grass in this biome. Can be any hexadecimal color value."
    );
    public static final Setting<Color> FOLIAGE_COLOR = Settings.colorSetting(
            "FoliageColor", "0xFFFFFF",
            t -> ((BiomeVisualSettings) t).getFoliageColor(),
            "The color of the foliage in this biome. Can be any hexadecimal color value."
    );
    public static final Setting<Color> FOG_COLOR = Settings.colorSetting(
            "FogColor", "0x000000",
            t -> ((BiomeVisualSettings) t).getFogColor(),
            "The color of the fog in this biome. Can be any hexadecimal color value."
    );
    public static final Setting<Color> WATER_FOG_COLOR = Settings.colorSetting(
            "WaterFogColor", "0x000000",
            t -> ((BiomeVisualSettings) t).getWaterFogColor(),
            "The color of the fog above water in this biome. Can be any hexadecimal color value."
    );
    public static final Setting<Double> MOOD_OFFSET = Settings.doubleSetting(
            "MoodOffset", 2.0, 0, Double.MAX_VALUE,
            t -> ((BiomeVisualSettings) t).getMoodOffset(),
            "The offset of the sound event"
    );
    public static final Setting<Double> ADDITIONS_TICK_CHANCE = Settings.doubleSetting(
            "AdditionsTickChance", 0, 0, Double.MAX_VALUE,
            t -> ((BiomeVisualSettings) t).getAdditionsTickChance(),
            "The tick chance that the additions sound plays"
    );
    public static final Setting<Float> BIOME_TEMPERATURE = Settings.floatSetting(
            "BiomeTemperature", 0.5f, 0, 2,
            t -> ((BiomeVisualSettings) t).getBiomeTemperature(),
            "Biome temperature. Float value from 0.0 to 2.0.",
            "When this value is around 0.2, snow will fall on mountain peaks above y=90.",
            "When this value is around 0.1, the whole biome will be covered in snow and ice."
    );
    public static final Setting<Float> BIOME_WETNESS = Settings.floatSetting(
            "BiomeWetness", 0.5f, 0, 1,
            t -> ((BiomeVisualSettings) t).getBiomeWetness(),
            "Biome wetness. Float value from 0.0 to 1.0. Affects rain and snow."
    );
    public static final Setting<Float> PARTICLE_PROBABILITY = Settings.floatSetting(
            "ParticleProbability", 0f, 0, 1f,
            t -> ((BiomeVisualSettings) t).getParticleProbability(),
            "The probability that a particle will spawn in this biome."
    );
    public static final Setting<Float> FOG_DENSITY = Settings.floatSetting(
            "FogDensity", 0.0f, 0f, 1f,
            t -> ((BiomeVisualSettings) t).getFogDensity(),
            "The density of the fog in this biome. 0.0 will mimic vanilla, 1.0 is opaque fog."
    );
    public static final Setting<ColorSet> WATER_COLOR_CONTROL = Settings.colorSetSetting(
            "WaterColorControl",
            t -> ((BiomeVisualSettings) t).getWaterColorControl(),
            "Setting for biomes with more complex colors.",
            "Each column in the world has a noise value from what appears to be -1 to 1.",
            "Values near 0 are more common than values near -1 and 1. This setting is",
            "used to change the water color based on the noise value for the column.",
            "Syntax: Color,MaxNoise,[AnotherColor,MaxNoise[,...]]",
            "Example: " + BiomeVisualSettings.WATER_COLOR_CONTROL + ": 0xFFFFFF,-0.8,0x000000,0.0",
            "  When the noise is below -0.8, the water will be white, between -0.8 and 0",
            "  the water will be black, and above 0 the water will be the normal " + BiomeVisualSettings.WATER_COLOR + "."
    );
    public static final Setting<ColorSet> GRASS_COLOR_CONTROL = Settings.colorSetSetting(
            "GrassColorControl",
            t -> ((BiomeVisualSettings) t).getGrassColorControl(),
            "Setting for biomes with more complex colors. See "+WATER_COLOR_CONTROL+" for more info."
    );
    public static final Setting<ColorSet> FOLIAGE_COLOR_CONTROL = Settings.colorSetSetting(
            "FoliageColorControl",
            t -> ((BiomeVisualSettings) t).getFoliageColorControl(),
            "Setting for biomes with more complex colors. See "+WATER_COLOR_CONTROL+" for more info."
    );
    public static final Setting<GrassColorModifier> GRASS_COLOR_MODIFIER = Settings.enumSetting(
            "GrassColorModifier", GrassColorModifier.None,
            t -> ((BiomeVisualSettings) t).getGrassColorModifier(),
            "The modifier for the grass color. Can be None, Swamp or DarkForest."
    );
    public static final Setting<Boolean> REPLACE_CURRENT_MUSIC = Settings.booleanSetting(
            "ReplaceCurrentMusic", false,
            t -> ((BiomeVisualSettings) t).isReplaceCurrentMusic(),
            "If true, the music in this biome will replace the current music."
    );


    public static BiomeVisualSettings getBiomeVisualSettings(SettingsMap reader, VisualSettings parent) {
        BiomeVisualSettingsBuilder builder = BiomeVisualSettings.builder();

        builder.parent(parent);
        builder.skyColor(reader.getSetting(SKY_COLOR));
        builder.waterColor(reader.getSetting(WATER_COLOR));
        builder.waterColorControl(reader.getSetting(WATER_COLOR_CONTROL));
        builder.grassColor(reader.getSetting(GRASS_COLOR));
        builder.grassColorControl(reader.getSetting(GRASS_COLOR_CONTROL));
        builder.grassColorModifier(reader.getSetting(GRASS_COLOR_MODIFIER));
        builder.foliageColor(reader.getSetting(FOLIAGE_COLOR));
        builder.foliageColorControl(reader.getSetting(FOLIAGE_COLOR_CONTROL));
        builder.fogColor(reader.getSetting(FOG_COLOR));
        builder.fogDensity(reader.getSetting(FOG_DENSITY));
        builder.waterFogColor(reader.getSetting(WATER_FOG_COLOR));
        builder.particleType(reader.getSetting(PARTICLE_TYPE));
        builder.music(reader.getSetting(MUSIC));
        builder.musicMinDelay(reader.getSetting(MUSIC_MIN_DELAY));
        builder.musicMaxDelay(reader.getSetting(MUSIC_MAX_DELAY));
        builder.replaceCurrentMusic(reader.getSetting(REPLACE_CURRENT_MUSIC));
        builder.ambientSound(reader.getSetting(AMBIENT_SOUND));
        builder.moodSound(reader.getSetting(MOOD_SOUND));
        builder.moodSoundDelay(reader.getSetting(MOOD_SOUND_DELAY));
        builder.moodSearchRange(reader.getSetting(MOOD_SEARCH_RANGE));
        builder.moodOffset(reader.getSetting(MOOD_OFFSET));
        builder.additionsSound(reader.getSetting(ADDITIONS_SOUND));
        builder.additionsTickChance(reader.getSetting(ADDITIONS_TICK_CHANCE));
        builder.particleProbability(reader.getSetting(PARTICLE_PROBABILITY));
        builder.biomeTemperature(reader.getSetting(BIOME_TEMPERATURE));
        builder.biomeWetness(reader.getSetting(BIOME_WETNESS));
        if (builder.fogDensity == 0.5 && Objects.equals(builder.fogColor, FOG_COLOR.getDefaultValue()))
        {
            builder.fogDensity = 0.0f;
        }
        Setting<String> LEGACY_GRASS_COLOR2 = Settings.stringSetting("GrassColor2", "0xFFFFFF");
        Setting<String> LEGACY_FOLIAGE_COLOR2 = Settings.stringSetting("FoliageColor2", "0xFFFFFF");
        readLegacyColorSetting(reader, LEGACY_GRASS_COLOR2).ifPresent(set -> builder.grassColorControl = set);
        readLegacyColorSetting(reader, LEGACY_FOLIAGE_COLOR2).ifPresent(set -> builder.foliageColorControl = set);

        return builder.build();
    }

    private static Optional<ColorSet> readLegacyColorSetting(SettingsMap reader, Setting<String> oldSetting) {
        ColorSet colorSet = null;
        if (reader.hasSetting(oldSetting)) {
            String color = reader.getSetting(oldSetting);
            if (!color.equals(oldSetting.getDefaultValue())) {
                try {
                    colorSet = new SimpleColorSet(new String[]
                            { color, "-0.1" });
                } catch (InvalidConfigException e) {
                    OTGLog.error(LogCategory.CONFIGS,
                            "Encountered an invalid value while reading legacy setting {} with value {}: {}",
                            oldSetting.getName(), color, e.getMessage());
                }
            }
        }
        return Optional.ofNullable(colorSet);
    }
}