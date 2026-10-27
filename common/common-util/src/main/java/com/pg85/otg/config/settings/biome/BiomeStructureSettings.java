package com.pg85.otg.config.settings.biome;

import com.pg85.otg.config.io.SettingsMap;
import com.pg85.otg.config.settingtype.Setting;
import com.pg85.otg.config.settingtype.Settings;
import com.pg85.otg.config.settings.ConfigSection;
import com.pg85.otg.config.settings.preset.StructureSettings;
import com.pg85.otg.constants.settings.structure.*;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class BiomeStructureSettings extends ConfigSection {
    private final StructureSettings parent;
    private final boolean strongholdsEnabled;
    private final boolean oceanMonumentsEnabled;
    private final boolean woodlandMansionsEnabled;
    private final boolean netherFortressesEnabled;
    private final VillageType villageType;
    // TODO: Dead on Fabric/NeoForge - parsed and stored but never passed to MC.
    // The tag system (BiomeStructureTagConfig) is binary on/off, sizes are ignored.
    // Wiring these would require custom ConfiguredStructure per biome.
    private final int villageSize;
    private final MineshaftType mineshaftType;
    private final RareBuildingType rareBuildingType;
    private final boolean buriedTreasureEnabled;
    private final boolean shipWreckEnabled;
    private final boolean shipWreckBeachedEnabled;
    private final boolean pillagerOutpostEnabled;
    private final boolean bastionRemnantEnabled;
    private final boolean netherFossilEnabled;
    private final boolean endCityEnabled;
    // TODO: Dead on Fabric/NeoForge - see villageSize comment above.
    private final float mineshaftProbability;
    private final RuinedPortalType ruinedPortalType;
    private final OceanRuinsType oceanRuinsType;
    // TODO: Dead on Fabric/NeoForge - see villageSize comment above.
    private final float oceanRuinsLargeProbability;
    private final float oceanRuinsClusterProbability;
    private final float buriedTreasureProbability;
    private final int pillagerOutpostSize;
    private final int bastionRemnantSize;

    @Override
    public String getSectionName() {
        return "Biome Structure Settings";
    }

    public static final Setting<Boolean> STRONGHOLDS_ENABLED = Settings.booleanSetting(
            "StrongholdsEnabled", true,
            t -> ((BiomeStructureSettings) t).isStrongholdsEnabled(),
            "Toggles strongholds spawning in this biome."
    );
    public static final Setting<Boolean> BASTION_REMNANT_ENABLED = Settings.booleanSetting(
            "BastionRemnantEnabled", false,
            t -> ((BiomeStructureSettings) t).isBastionRemnantEnabled(),
            "Toggles bastion remnants spawning in this biome."
    );
    public static final Setting<Boolean> NETHER_FOSSIL_ENABLED = Settings.booleanSetting(
            "NetherFossilEnabled", false,
            t -> ((BiomeStructureSettings) t).isNetherFossilEnabled(),
            "Toggles nether fossils spawning in this biome."
    );
    public static final Setting<Boolean> END_CITY_ENABLED = Settings.booleanSetting(
            "EndCityEnabled", false,
            t -> ((BiomeStructureSettings) t).isEndCityEnabled(),
            "Toggles end cities spawning in this biome."
    );
    public static final Setting<Integer> VILLAGE_SIZE = Settings.intSetting(
            "VillageSize", 6, 0, Integer.MAX_VALUE,
            t -> ((BiomeStructureSettings) t).getVillageSize(),
"The size of the village that can spawn in this biome."
    );
    public static final Setting<Integer> PILLAGER_OUTPOST_SIZE = Settings.intSetting(
            "PillagerOutpostSize", 7, 0, Integer.MAX_VALUE,
            t -> ((BiomeStructureSettings) t).getPillagerOutpostSize(),
            "The size of the pillager outpost that can spawn in this biome."
    );
    public static final Setting<Integer> BASTION_REMNANT_SIZE = Settings.intSetting(
            "BastionRemnantSize", 6, 0, Integer.MAX_VALUE,
            t -> ((BiomeStructureSettings) t).getBastionRemnantSize(),
            "The size of the bastion remnant that can spawn in this biome."
    );
    public static final Setting<Float>  MINESHAFT_PROBABILITY = Settings.floatSetting(
            "MineshaftProbability", 0.004f, 0f, 1f,
            t -> ((BiomeStructureSettings) t).getMineshaftProbability(),
            "The probability of a mineshaft spawning in this biome."
    );
    public static final Setting<Float> OCEAN_RUINS_LARGE_PROBABILITY = Settings.floatSetting(
            "OceanRuinsLargeProbability", 0.3f, 0f, 1f,
            t -> ((BiomeStructureSettings) t).getOceanRuinsLargeProbability(),
            "The probability of a large ocean ruin spawning in this biome."
    );
    public static final Setting<Float> OCEAN_RUINS_CLUSTER_PROBABILITY = Settings.floatSetting(
            "OceanRuinsClusterProbability", 0.9f, 0f, 1f,
            t -> ((BiomeStructureSettings) t).getOceanRuinsClusterProbability(),
            "The probability of a cluster of ocean ruins spawning in this biome."
    );
    public static final Setting<Float> BURIED_TREASURE_PROBABILITY = Settings.floatSetting(
            "BuriedTreasureProbability", 0.01f, 0f, 1f,
            t -> ((BiomeStructureSettings) t).getBuriedTreasureProbability(),
            "The probability of buried treasure spawning in this biome."
    );
    public static final Setting<VillageType> VILLAGE_TYPE = Settings.enumSetting(
            "VillageType", VillageType.disabled,
            t -> ((BiomeStructureSettings) t).getVillageType(),
            "The type of village that can spawn in this biome."
            );
    public static final Setting<MineshaftType> MINESHAFT_TYPE = Settings.enumSetting(
            "MineshaftType", MineshaftType.normal,
            t -> ((BiomeStructureSettings) t).getMineshaftType(),
            "The type of mineshaft that can spawn in this biome."
    );
    public static final Setting<RareBuildingType> RARE_BUILDING_TYPE = Settings.enumSetting(
            "RareBuildingType", RareBuildingType.disabled,
            t -> ((BiomeStructureSettings) t).getRareBuildingType(),
            "The type of rare building that can spawn in this biome."
    );
    public static final Setting<RuinedPortalType> RUINED_PORTAL_TYPE = Settings.enumSetting(
            "RuinedPortalType", RuinedPortalType.disabled,
            t -> ((BiomeStructureSettings) t).getRuinedPortalType(),
            "The type of ruined portal that can spawn in this biome."
    );
    public static final Setting<OceanRuinsType> OCEAN_RUINS_TYPE = Settings.enumSetting(
            "OceanRuinsType", OceanRuinsType.disabled,
            t -> ((BiomeStructureSettings) t).getOceanRuinsType(),
            "The type of ocean ruins that can spawn in this biome."
    );
    public static final Setting<Boolean> NETHER_FORTRESSES_ENABLED = Settings.booleanSetting(
            "NetherFortressesEnabled", false,
            t -> ((BiomeStructureSettings) t).isNetherFortressesEnabled(),
            "Toggles nether fortresses spawning in this biome."
    );
    public static final Setting<Boolean> OCEAN_MONUMENTS_ENABLED = Settings.booleanSetting(
            "OceanMonumentsEnabled", false,
            t -> ((BiomeStructureSettings) t).isOceanMonumentsEnabled(),
            "Toggles ocean monuments spawning in this biome."
    );
    public static final Setting<Boolean> WOODLAND_MANSIONS_ENABLED = Settings.booleanSetting(
            "WoodlandMansionsEnabled", false,
            t -> ((BiomeStructureSettings) t).isWoodlandMansionsEnabled(),
            "Toggles woodland mansions spawning in this biome."
    );
    public static final Setting<Boolean> BURIED_TREASURE_ENABLED = Settings.booleanSetting(
            "BuriedTreasureEnabled", false,
            t -> ((BiomeStructureSettings) t).isBuriedTreasureEnabled(),
            "Toggles buried treasure spawning in this biome."
    );
    public static final Setting<Boolean> SHIP_WRECK_ENABLED = Settings.booleanSetting(
            "ShipWreckEnabled", false,
            t -> ((BiomeStructureSettings) t).isShipWreckEnabled(),
            "Toggles shipwrecks spawning in this biome."
    );
    public static final Setting<Boolean> SHIP_WRECK_BEACHED_ENABLED = Settings.booleanSetting(
            "ShipWreckBeachedEnabled", false,
            t -> ((BiomeStructureSettings) t).isShipWreckBeachedEnabled(),
            "Toggles beached shipwrecks spawning in this biome."
    );
    public static final Setting<Boolean> PILLAGER_OUTPOST_ENABLED = Settings.booleanSetting(
            "PillagerOutpostEnabled", false,
            t -> ((BiomeStructureSettings) t).isPillagerOutpostEnabled(),
            "Toggles pillager outposts spawning in this biome."
    );

    // New structures from 1.16 to 1.20.1




    public static BiomeStructureSettings getBiomeStructureSettings(SettingsMap reader, StructureSettings parent) {
        BiomeStructureSettingsBuilder builder = BiomeStructureSettings.builder();

        builder.parent(parent);
        builder.strongholdsEnabled(reader.getSetting(STRONGHOLDS_ENABLED));
        builder.oceanMonumentsEnabled(reader.getSetting(OCEAN_MONUMENTS_ENABLED));
        builder.woodlandMansionsEnabled(reader.getSetting(WOODLAND_MANSIONS_ENABLED));
        builder.netherFortressesEnabled(reader.getSetting(NETHER_FORTRESSES_ENABLED));
        builder.villageType(reader.getSetting(VILLAGE_TYPE));
        builder.villageSize(reader.getSetting(VILLAGE_SIZE));
        builder.mineshaftType(reader.getSetting(MINESHAFT_TYPE));
        builder.rareBuildingType(reader.getSetting(RARE_BUILDING_TYPE));
        builder.buriedTreasureEnabled(reader.getSetting(BURIED_TREASURE_ENABLED));
        builder.shipWreckEnabled(reader.getSetting(SHIP_WRECK_ENABLED));
        builder.shipWreckBeachedEnabled(reader.getSetting(SHIP_WRECK_BEACHED_ENABLED));
        builder.pillagerOutpostEnabled(reader.getSetting(PILLAGER_OUTPOST_ENABLED));
        builder.bastionRemnantEnabled(reader.getSetting(BASTION_REMNANT_ENABLED));
        builder.netherFossilEnabled(reader.getSetting(NETHER_FOSSIL_ENABLED));
        builder.endCityEnabled(reader.getSetting(END_CITY_ENABLED));
        builder.mineshaftProbability(reader.getSetting(MINESHAFT_PROBABILITY));
        builder.ruinedPortalType(reader.getSetting(RUINED_PORTAL_TYPE));
        builder.oceanRuinsType(reader.getSetting(OCEAN_RUINS_TYPE));
        builder.oceanRuinsLargeProbability(reader.getSetting(OCEAN_RUINS_LARGE_PROBABILITY));
        builder.oceanRuinsClusterProbability(reader.getSetting(OCEAN_RUINS_CLUSTER_PROBABILITY));
        builder.buriedTreasureProbability(reader.getSetting(BURIED_TREASURE_PROBABILITY));
        builder.pillagerOutpostSize(reader.getSetting(PILLAGER_OUTPOST_SIZE));
        builder.bastionRemnantSize(reader.getSetting(BASTION_REMNANT_SIZE));

        return builder.build();
    }
}
