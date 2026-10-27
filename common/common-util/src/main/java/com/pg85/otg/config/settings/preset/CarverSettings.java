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
public class CarverSettings extends ConfigSection {
    // Toggle between legacy OTG carvers and modern 1.18+ style pipeline
    private final boolean useModernCaves;
    // Whether to allow vanilla-style aquifers when modern caves are enabled
    private final boolean aquifersEnabled;
    // Whether large ore veins (1.18+) should be generated
    private final boolean oreVeinsEnabled;
    // Keep legacy OTG carvers available even if modern caves are on
    private final boolean legacyCarversEnabled;
    private final boolean cavesEnabled;
    private final int caveFrequency;
    private final int caveRarity;
    private final boolean evenCaveDistribution;
    private final int caveMinAltitude;
    private final int caveMaxAltitude;
    private final int caveSystemFrequency;
    private final int individualCaveRarity;
    private final int caveSystemPocketMinSize;
    private final int caveSystemPocketChance;
    private final int caveSystemPocketMaxSize;
    private final boolean ravinesEnabled;
    private final int ravineRarity;
    private final int ravineMinLength;
    private final int ravineMaxLength;
    private final double ravineDepth;
    private final int ravineMinAltitude;
    private final int ravineMaxAltitude;

    public static final Setting<Boolean> USE_MODERN_CAVES = Settings.booleanSetting(
            "UseModernCaves", false,
            t -> ((CarverSettings) t).isUseModernCaves(),
            "Enables the 1.18+ style noise-cave pipeline and aquifers. When false, OTG uses only legacy carvers."
    );
    public static final Setting<Boolean> AQUIFERS_ENABLED = Settings.booleanSetting(
            "AquifersEnabled", true,
            t -> ((CarverSettings) t).isAquifersEnabled(),
            "Controls vanilla-style aquifers (water/lava filling) used by noise caves."
    );
    public static final Setting<Boolean> ORE_VEINS_ENABLED = Settings.booleanSetting(
            "OreVeinsEnabled", true,
            t -> ((CarverSettings) t).isOreVeinsEnabled(),
            "Generates large 1.18+ ore veins. Has no effect when ore generation is disabled globally."
    );
    public static final Setting<Boolean> LEGACY_CARVERS_ENABLED = Settings.booleanSetting(
            "LegacyCarversEnabled", true,
            t -> ((CarverSettings) t).isLegacyCarversEnabled(),
            "If true, legacy OTG carvers (caves/ravines) can run when modern caves are disabled."
    );

    public static final Setting<Integer> CAVE_RARITY = Settings.intSetting(
            "CaveRarity", 14, 0, 100,
            t -> ((CarverSettings) t).getCaveRarity(),
            "This controls the odds that a given chunk will host a single cave and/or the start of a cave system."
    );
    public static final Setting<Integer> CAVE_FREQUENCY = Settings.intSetting(
            "CaveFrequency", 15, 0, 200,
            t -> ((CarverSettings) t).getCaveFrequency(),
            "The number of times the cave generation algorithm will attempt to create single caves and cave",
            "systems in the given chunk. This value is larger because the likelihood for the cave generation",
            "algorithm to bailout is fairly high and it is used in a randomizer that trends towards lower",
            "random numbers. With an input of 40 (default) the randomizer will result in an average random",
            "result of 5 to 6. This can be turned off by setting evenCaveDistribution (below) to true."
    );
    public static final Setting<Integer> CAVE_MIN_ALTITUDE = Settings.intSetting(
            "CaveMinAltitude", 8, Constants.WORLD_START_MIN_Y, Constants.WORLD_END_MAX_Y,
            t -> ((CarverSettings) t).getCaveMinAltitude(),
            "Sets the minimum and maximum altitudes at which caves will be generated. These values are",
            "used in a randomizer that trends towards lower numbers so that caves become more frequent",
            "the closer you get to the bottom of the map. Setting even cave distribution (above) to true",
            "will turn off this randomizer and use a flat random number generator that will create an even",
            "density of caves at all altitudes."
    );
    public static final Setting<Integer> CAVE_MAX_ALTITUDE = Settings.intSetting(
            "CaveMaxAltitude", 128, Constants.WORLD_START_MIN_Y, Constants.WORLD_END_MAX_Y,
            t -> ((CarverSettings) t).getCaveMaxAltitude(),
            "See " + CAVE_MIN_ALTITUDE
    );
    public static final Setting<Integer> INDIVIDUAL_CAVE_RARITY = Settings.intSetting(
            "IndividualCaveRarity", 25, 0, 100,
            t -> ((CarverSettings) t).getIndividualCaveRarity(),
            "The odds that the cave generation algorithm will generate a single cavern without an accompanying",
            "cave system. Note that whenever the algorithm generates an individual cave it will also attempt to",
            "generate a pocket of cave systems in the vicinity (no guarantee of connection or that the cave system",
            "will actually be created)."
    );
    public static final Setting<Integer> CAVE_SYSTEM_FREQUENCY = Settings.intSetting(
            "CaveSystemFrequency", 1, 0, 200,
            t -> ((CarverSettings) t).getCaveSystemFrequency(),
            "The number of times the algorithm will attempt to start a cave system in a given chunk per cycle of",
            "the cave generation algorithm (see cave frequency setting above). Note that setting this value too",
            "high with an accompanying high cave frequency value can cause extremely long world generation time."
    );
    public static final Setting<Integer> CAVE_SYSTEM_POCKET_CHANCE = Settings.intSetting(
            "CaveSystemPocketChance", 0, 0, 100,
            t -> ((CarverSettings) t).getCaveSystemPocketChance(),
            "This can be set to create an additional chance that a cave system pocket (a higher than normal",
            "density of cave systems) being started in a given chunk. Normally, a cave pocket will only be",
            "attempted if an individual cave is generated, but this will allow more cave pockets to be generated",
            "in addition to the individual cave trigger."
    );
    public static final Setting<Integer> CAVE_SYSTEM_POCKET_MIN_SIZE = Settings.intSetting(
            "CaveSystemPocketMinSize", 0, 0, 100,
            t -> ((CarverSettings) t).getCaveSystemPocketMinSize(),
            "The minimum and maximum size that a cave system pocket can be. This modifies/overrides the",
            "cave system frequency setting (above) when triggered."
    );
    public static final Setting<Integer> CAVE_SYSTEM_POCKET_MAX_SIZE = Settings.intSetting(
            "CaveSystemPocketMaxSize", 3, 0, 100,
            t -> ((CarverSettings) t).getCaveSystemPocketMaxSize(),
            "See " + CAVE_SYSTEM_POCKET_MIN_SIZE
    );
    public static final Setting<Integer> RAVINE_RARITY = Settings.intSetting(
            "RavineRarity", 2, 0, 100,
            t -> ((CarverSettings) t).getRavineRarity()
    );
    public static final Setting<Integer> RAVINE_MIN_ALTITUDE = Settings.intSetting(
            "RavineMinAltitude", 20, Constants.WORLD_START_MIN_Y, Constants.WORLD_END_MAX_Y,
            t -> ((CarverSettings) t).getRavineMinAltitude()
    );
    public static final Setting<Integer> RAVINE_MAX_ALTITUDE = Settings.intSetting(
            "RavineMaxAltitude", 68, Constants.WORLD_START_MIN_Y, Constants.WORLD_END_MAX_Y,
            t -> ((CarverSettings) t).getRavineMaxAltitude()
    );
    public static final Setting<Integer> RAVINE_MIN_LENGTH = Settings.intSetting(
            "RavineMinLength", 84, 1, 500,
            t -> ((CarverSettings) t).getRavineMinLength()
    );
    public static final Setting<Integer> RAVINE_MAX_LENGTH = Settings.intSetting(
            "RavineMaxLength", 112, 1, 500,
            t -> ((CarverSettings) t).getRavineMaxLength()
    );
    public static final Setting<Boolean> CAVES_ENABLED = Settings.booleanSetting(
            "CavesEnabled", true,
            t -> ((CarverSettings) t).isCavesEnabled(),
            "Enables/disables OTG caves. OTG should automatically disable caves/carvers for biomes when modded carvers are detected."
    );
    public static final Setting<Boolean> EVEN_CAVE_DISTRIBUTION = Settings.booleanSetting(
            "EvenCaveDistribution", false,
            t -> ((CarverSettings) t).isEvenCaveDistribution(),
            "Setting this to true will turn off the randomizer for cave frequency (above). Do note that",
            "if you turn this on you will probably want to adjust the cave frequency down to avoid long",
            "load times at world creation."
    );
    public static final Setting<Boolean> RAVINES_ENABLED = Settings.booleanSetting(
            "RavinesEnabled", true,
            t -> ((CarverSettings) t).isRavinesEnabled(),
            "Enables/disables OTG ravines. OTG should automatically disable ravines/carvers for biomes when modded carvers are detected."
    );
    public static final Setting<Double> RAVINE_DEPTH = Settings.doubleSetting(
            "RavineDepth", 3, 0.1, 15,
            t -> ((CarverSettings) t).getRavineDepth()
    );


    public static CarverSettings getCarverSettings(SettingsMap reader, TerrainSettings terrainSettings) {
        var carverSettingsBuilder = builder();

        carverSettingsBuilder.useModernCaves(reader.getSetting(USE_MODERN_CAVES));
        carverSettingsBuilder.aquifersEnabled(reader.getSetting(AQUIFERS_ENABLED));
        carverSettingsBuilder.oreVeinsEnabled(reader.getSetting(ORE_VEINS_ENABLED));
        carverSettingsBuilder.legacyCarversEnabled(reader.getSetting(LEGACY_CARVERS_ENABLED));

        carverSettingsBuilder.cavesEnabled(reader.getSetting(CAVES_ENABLED));
        carverSettingsBuilder.caveFrequency(reader.getSetting(CAVE_FREQUENCY));
        carverSettingsBuilder.caveRarity(reader.getSetting(CAVE_RARITY));
        carverSettingsBuilder.evenCaveDistribution(reader.getSetting(EVEN_CAVE_DISTRIBUTION));
        carverSettingsBuilder.caveMinAltitude(reader.getSetting(CAVE_MIN_ALTITUDE));
        carverSettingsBuilder.caveMaxAltitude(reader.getSetting(CAVE_MAX_ALTITUDE));
        carverSettingsBuilder.caveSystemFrequency(reader.getSetting(CAVE_SYSTEM_FREQUENCY));
        carverSettingsBuilder.individualCaveRarity(reader.getSetting(INDIVIDUAL_CAVE_RARITY));
        carverSettingsBuilder.caveSystemPocketChance(reader.getSetting(CAVE_SYSTEM_POCKET_CHANCE));
        carverSettingsBuilder.caveSystemPocketMinSize(reader.getSetting(CAVE_SYSTEM_POCKET_MIN_SIZE));
        carverSettingsBuilder.caveSystemPocketMaxSize(reader.getSetting(CAVE_SYSTEM_POCKET_MAX_SIZE));

        carverSettingsBuilder.ravinesEnabled(reader.getSetting(RAVINES_ENABLED));
        carverSettingsBuilder.ravineRarity(reader.getSetting(RAVINE_RARITY));
        carverSettingsBuilder.ravineMinLength(reader.getSetting(RAVINE_MIN_LENGTH));
        carverSettingsBuilder.ravineMaxLength(reader.getSetting(RAVINE_MAX_LENGTH));
        carverSettingsBuilder.ravineDepth(reader.getSetting(RAVINE_DEPTH));
        carverSettingsBuilder.ravineMinAltitude(reader.getSetting(RAVINE_MIN_ALTITUDE));
        carverSettingsBuilder.ravineMaxAltitude(reader.getSetting(RAVINE_MAX_ALTITUDE));
        return carverSettingsBuilder.fixSettings().build();
    }

    @Override
    public String getSectionName() {
        return "Carver Settings";
    }

    public static class CarverSettingsBuilder {
        public CarverSettingsBuilder fixSettings() {
            checkCaveMaxAltitude();
            checkCaveSystemPocketMaxSize();
            checkRavineMaxAltitude();
            checkRavineMaxLength();
            return this;
        }
        private void checkCaveMaxAltitude() {
            caveMaxAltitude = Math.max(caveMaxAltitude, caveMinAltitude);
        }
        private void checkCaveSystemPocketMaxSize() {
            caveSystemPocketMaxSize = Math.max(caveSystemPocketMaxSize, caveSystemPocketMinSize);
        }
        private void checkRavineMaxAltitude() {
            ravineMaxAltitude = Math.max(ravineMaxAltitude, ravineMinAltitude);
        }
        private void checkRavineMaxLength() {
            ravineMaxLength = Math.max(ravineMaxLength, ravineMinLength);
        }
    }
}
