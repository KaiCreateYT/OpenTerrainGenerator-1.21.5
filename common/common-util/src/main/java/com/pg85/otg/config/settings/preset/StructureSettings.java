package com.pg85.otg.config.settings.preset;

import com.pg85.otg.config.io.SettingsMap;
import com.pg85.otg.config.settingtype.Setting;
import com.pg85.otg.config.settingtype.Settings;
import com.pg85.otg.config.settings.ConfigSection;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class StructureSettings extends ConfigSection {
    public static final Setting<Boolean> MINESHAFTS_ENABLED = Settings.booleanSetting(
            "MineshaftsEnabled", true,
            t -> ((StructureSettings) t).isMineshaftsEnabled()
    );
    public static final Setting<Boolean> OCEAN_MONUMENTS_ENABLED = Settings.booleanSetting(
            "OceanMonumentsEnabled", true,
            t -> ((StructureSettings) t).isOceanMonumentsEnabled()
    );
    public static final Setting<Boolean> RARE_BUILDINGS_ENABLED = Settings.booleanSetting(
            "RareBuildingsEnabled", true,
            t -> ((StructureSettings) t).isRareBuildingsEnabled()
    );
    public static final Setting<Boolean> STRONGHOLDS_ENABLED = Settings.booleanSetting(
            "StrongholdsEnabled", true,
            t -> ((StructureSettings) t).isStrongholdsEnabled()
    );
    public static final Setting<Boolean> WOODLAND_MANSIONS_ENABLED = Settings.booleanSetting(
            "WoodlandsMansionsEnabled", true,
            t -> ((StructureSettings) t).isWoodlandMansionsEnabled()
    );
    public static final Setting<Boolean> NETHER_FORTRESSES_ENABLED = Settings.booleanSetting(
            "NetherFortressesEnabled", true,
            t -> ((StructureSettings) t).isNetherFortressesEnabled()
    );
    public static final Setting<Boolean> BURIED_TREASURE_ENABLED = Settings.booleanSetting(
            "BuriedTreasureEnabled", true,
            t -> ((StructureSettings) t).isBuriedTreasureEnabled()
    );
    public static final Setting<Boolean> OCEAN_RUINS_ENABLED = Settings.booleanSetting(
            "OceanRuinsEnabled", true,
            t -> ((StructureSettings) t).isOceanRuinsEnabled()
    );
    public static final Setting<Boolean> PILLAGER_OUTPOSTS_ENABLED = Settings.booleanSetting(
            "PillagerOutpostsEnabled", true,
            t -> ((StructureSettings) t).isPillagerOutpostsEnabled()
    );
    public static final Setting<Boolean> BASTION_REMNANTS_ENABLED = Settings.booleanSetting(
            "BastionRemnantsEnabled", true,
            t -> ((StructureSettings) t).isBastionRemnantsEnabled()
    );
    public static final Setting<Boolean> NETHER_FOSSILS_ENABLED = Settings.booleanSetting(
            "NetherFossilsEnabled", true,
            t -> ((StructureSettings) t).isNetherFossilsEnabled()
    );
    public static final Setting<Boolean> END_CITIES_ENABLED = Settings.booleanSetting(
            "EndCitiesEnabled", true,
            t -> ((StructureSettings) t).isEndCitiesEnabled()
    );
    public static final Setting<Boolean> RUINED_PORTALS_ENABLED = Settings.booleanSetting(
            "RuinedPortalsEnabled", true,
            t -> ((StructureSettings) t).isRuinedPortalsEnabled()
    );
    public static final Setting<Boolean> SHIPWRECKS_ENABLED = Settings.booleanSetting(
            "ShipwrecksEnabled", true,
            t -> ((StructureSettings) t).isShipwrecksEnabled()
    );
    public static final Setting<Boolean> VILLAGES_ENABLED = Settings.booleanSetting(
            "VillagesEnabled", true,
            t -> ((StructureSettings) t).isVillagesEnabled()
    );
    public static final Setting<Integer> VILLAGE_SPACING = Settings.intSetting(
            "VillageSpacing", 32, Integer.MIN_VALUE, Integer.MAX_VALUE,
            t -> ((StructureSettings) t).getVillageSpacing()
    );
    public static final Setting<Integer> VILLAGE_SEPARATION = Settings.intSetting(
            "VillageSeparation", 8, Integer.MIN_VALUE, Integer.MAX_VALUE,
            t -> ((StructureSettings) t).getVillageSeparation()
    );
    public static final Setting<Integer> DESERTPYRAMID_SPACING = Settings.intSetting(
            "DesertPyramidSpacing", 32, Integer.MIN_VALUE, Integer.MAX_VALUE,
            t -> ((StructureSettings) t).getDesertPyramidSeparation()
    );
    public static final Setting<Integer> DESERTPYRAMID_SEPARATION = Settings.intSetting(
            "DesertPyramidSeparation", 8, Integer.MIN_VALUE, Integer.MAX_VALUE,
            t -> ((StructureSettings) t).getDesertPyramidSeparation()
    );
    public static final Setting<Integer> IGLOO_SPACING = Settings.intSetting(
            "IglooSpacing", 32, Integer.MIN_VALUE, Integer.MAX_VALUE,
            t -> ((StructureSettings) t).getIglooSpacing()
    );
    public static final Setting<Integer> IGLOO_SEPARATION = Settings.intSetting(
            "IglooSeparation", 8, Integer.MIN_VALUE, Integer.MAX_VALUE,
            t -> ((StructureSettings) t).getIglooSeparation()
    );
    public static final Setting<Integer> JUNGLETEMPLE_SPACING = Settings.intSetting(
            "JungleTempleSpacing", 32, Integer.MIN_VALUE, Integer.MAX_VALUE,
            t -> ((StructureSettings) t).getJungleTempleSpacing()
    );
    public static final Setting<Integer> JUNGLETEMPLE_SEPARATION = Settings.intSetting(
            "JungleTempleSeparation", 8, Integer.MIN_VALUE, Integer.MAX_VALUE,
            t -> ((StructureSettings) t).getJungleTempleSeparation()
    );
    public static final Setting<Integer> SWAMPHUT_SPACING = Settings.intSetting(
            "SwampHutSpacing", 32, Integer.MIN_VALUE, Integer.MAX_VALUE,
            t -> ((StructureSettings) t).getSwampHutSpacing()
    );
    public static final Setting<Integer> SWAMPHUT_SEPARATION = Settings.intSetting(
            "SwampHutSeparation", 8, Integer.MIN_VALUE, Integer.MAX_VALUE,
            t -> ((StructureSettings) t).getSwampHutSeparation()
    );
    public static final Setting<Integer> PILLAGEROUTPOST_SPACING = Settings.intSetting(
            "PillagerOutpostSpacing", 32, Integer.MIN_VALUE, Integer.MAX_VALUE,
            t -> ((StructureSettings) t).getPillagerOutpostSpacing()
    );
    public static final Setting<Integer> PILLAGEROUTPOST_SEPARATION = Settings.intSetting(
            "PillagerOutpostSeparation", 8, Integer.MIN_VALUE, Integer.MAX_VALUE,
            t -> ((StructureSettings) t).getPillagerOutpostSeparation()
    );
    public static final Setting<Integer> STRONGHOLD_SPACING = Settings.intSetting(
            "StrongholdSpacing", 1, Integer.MIN_VALUE, Integer.MAX_VALUE,
            t -> ((StructureSettings) t).getStrongholdSpacing()
    );
    public static final Setting<Integer> STRONGHOLD_SEPARATION = Settings.intSetting(
            "StrongholdSeparation", 0, Integer.MIN_VALUE, Integer.MAX_VALUE,
            t -> ((StructureSettings) t).getStrongholdSeparation()
    );
    public static final Setting<Integer> STRONGHOLD_DISTANCE = Settings.intSetting(
            "StrongholdDistance", 32, Integer.MIN_VALUE, Integer.MAX_VALUE,
            t -> ((StructureSettings) t).getStrongholdDistance()
    );
    public static final Setting<Integer> STRONGHOLD_SPREAD = Settings.intSetting(
            "StrongholdSpread", 3, Integer.MIN_VALUE, Integer.MAX_VALUE,
            t -> ((StructureSettings) t).getStrongholdSpread()
    );
    public static final Setting<Integer> STRONGHOLD_COUNT = Settings.intSetting(
            "StrongholdCount", 128, Integer.MIN_VALUE, Integer.MAX_VALUE,
            t -> ((StructureSettings) t).getStrongholdCount()
    );
    public static final Setting<Integer> OCEANMONUMENT_SPACING = Settings.intSetting(
            "OceanMonumentSpacing", 32, Integer.MIN_VALUE, Integer.MAX_VALUE,
            t -> ((StructureSettings) t).getOceanMonumentSpacing()
    );
    public static final Setting<Integer> OCEANMONUMENT_SEPARATION = Settings.intSetting(
            "OceanMonumentSeparation", 5, Integer.MIN_VALUE, Integer.MAX_VALUE,
            t -> ((StructureSettings) t).getOceanMonumentSeparation()
    );
    public static final Setting<Integer> ENDCITY_SPACING = Settings.intSetting(
            "EndCitySpacing", 20, Integer.MIN_VALUE, Integer.MAX_VALUE,
            t -> ((StructureSettings) t).getEndCitySpacing()
    );
    public static final Setting<Integer> ENDCITY_SEPARATION = Settings.intSetting(
            "EndCitySeparation", 11, Integer.MIN_VALUE, Integer.MAX_VALUE,
            t -> ((StructureSettings) t).getEndCitySeparation()
    );
    public static final Setting<Integer> WOODLANDMANSION_SPACING = Settings.intSetting(
            "WoodlandMansionSpacing", 80, Integer.MIN_VALUE, Integer.MAX_VALUE,
            t -> ((StructureSettings) t).getWoodlandMansionSpacing()
    );
    public static final Setting<Integer> WOODLANDMANSION_SEPARATION = Settings.intSetting(
            "WoodlandMansionSeparation", 20, Integer.MIN_VALUE, Integer.MAX_VALUE,
            t -> ((StructureSettings) t).getWoodlandMansionSeparation()
    );
    public static final Setting<Integer> BURIEDTREASURE_SPACING = Settings.intSetting(
            "BuriedTreasureSpacing", 1, Integer.MIN_VALUE, Integer.MAX_VALUE,
            t -> ((StructureSettings) t).getBuriedTreasureSpacing()
    );
    public static final Setting<Integer> BURIEDTREASURE_SEPARATION = Settings.intSetting(
            "BuriedTreasureSeparation", 0, Integer.MIN_VALUE, Integer.MAX_VALUE,
            t -> ((StructureSettings) t).getBuriedTreasureSeparation()
    );
    public static final Setting<Integer> MINESHAFT_SPACING = Settings.intSetting(
            "MineshaftSpacing", 1, Integer.MIN_VALUE, Integer.MAX_VALUE,
            t -> ((StructureSettings) t).getMineshaftSpacing()
    );
    public static final Setting<Integer> MINESHAFT_SEPARATION = Settings.intSetting(
            "MineshaftSeparation", 0, Integer.MIN_VALUE, Integer.MAX_VALUE,
            t -> ((StructureSettings) t).getMineshaftSeparation()
    );
    public static final Setting<Integer> RUINEDPORTAL_SPACING = Settings.intSetting(
            "RuinedPortalSpacing", 40, Integer.MIN_VALUE, Integer.MAX_VALUE,
            t -> ((StructureSettings) t).getRuinedPortalSpacing()
    );
    public static final Setting<Integer> RUINEDPORTAL_SEPARATION = Settings.intSetting(
            "RuinedPortalSeparation", 15, Integer.MIN_VALUE, Integer.MAX_VALUE,
            t -> ((StructureSettings) t).getRuinedPortalSeparation()
    );
    public static final Setting<Integer> SHIPWRECK_SPACING = Settings.intSetting(
            "ShipwreckSpacing", 24, Integer.MIN_VALUE, Integer.MAX_VALUE,
            t -> ((StructureSettings) t).getShipwreckSpacing()
    );
    public static final Setting<Integer> SHIPWRECK_SEPARATION = Settings.intSetting(
            "ShipwreckSeparation", 4, Integer.MIN_VALUE, Integer.MAX_VALUE,
            t -> ((StructureSettings) t).getShipwreckSeparation()
    );
    public static final Setting<Integer> OCEANRUIN_SPACING = Settings.intSetting(
            "OceanRuinSpacing", 20, Integer.MIN_VALUE, Integer.MAX_VALUE,
            t -> ((StructureSettings) t).getOceanRuinSpacing()
    );
    public static final Setting<Integer> OCEANRUIN_SEPARATION = Settings.intSetting(
            "OceanRuinSeparation", 8, Integer.MIN_VALUE, Integer.MAX_VALUE,
            t -> ((StructureSettings) t).getOceanRuinSeparation()
    );
    public static final Setting<Integer> BASTIONREMNANT_SPACING = Settings.intSetting(
            "BastionRemnantSpacing", 27, Integer.MIN_VALUE, Integer.MAX_VALUE,
            t -> ((StructureSettings) t).getBastionRemnantSpacing()
    );
    public static final Setting<Integer> BASTIONREMNANT_SEPARATION = Settings.intSetting(
            "BastionRemnantSeparation", 4, Integer.MIN_VALUE, Integer.MAX_VALUE,
            t -> ((StructureSettings) t).getBastionRemnantSeparation()
    );
    public static final Setting<Integer> NETHERFORTRESS_SPACING = Settings.intSetting(
            "NetherFortressSpacing", 27, Integer.MIN_VALUE, Integer.MAX_VALUE,
            t -> ((StructureSettings) t).getNetherFortressSpacing()
    );
    public static final Setting<Integer> NETHERFORTRESS_SEPARATION = Settings.intSetting(
            "NetherFortressSeparation", 4, Integer.MIN_VALUE, Integer.MAX_VALUE,
            t -> ((StructureSettings) t).getNetherFortressSeparation()
    );
    public static final Setting<Integer> NETHERFOSSIL_SPACING = Settings.intSetting(
            "NetherFossilSpacing", 2, Integer.MIN_VALUE, Integer.MAX_VALUE,
            t -> ((StructureSettings) t).getNetherFossilSpacing()
    );
    public static final Setting<Integer> NETHERFOSSIL_SEPARATION = Settings.intSetting(
            "NetherFossilSeparation", 1, Integer.MIN_VALUE, Integer.MAX_VALUE,
            t -> ((StructureSettings) t).getNetherFossilSeparation()
    );
    private final boolean woodlandMansionsEnabled;
    private final boolean netherFortressesEnabled;
    private final boolean buriedTreasureEnabled;
    private final boolean oceanRuinsEnabled;
    private final boolean pillagerOutpostsEnabled;
    private final boolean bastionRemnantsEnabled;
    private final boolean netherFossilsEnabled;
    private final boolean endCitiesEnabled;
    private final boolean ruinedPortalsEnabled;
    private final boolean shipwrecksEnabled;
    private final boolean strongholdsEnabled;
    private final boolean villagesEnabled;
    private final boolean mineshaftsEnabled;
    private final boolean oceanMonumentsEnabled;
    private final boolean rareBuildingsEnabled;

    private final int villageSpacing;
    private final int villageSeparation;
    private final int desertPyramidSpacing;
    private final int desertPyramidSeparation;
    private final int iglooSpacing;
    private final int iglooSeparation;
    private final int jungleTempleSpacing;
    private final int jungleTempleSeparation;
    private final int swampHutSpacing;
    private final int swampHutSeparation;
    private final int pillagerOutpostSpacing;
    private final int pillagerOutpostSeparation;
    private final int strongholdSpacing;
    private final int strongholdSeparation;
    private final int strongholdDistance;
    private final int strongholdSpread;
    private final int strongholdCount;
    private final int oceanMonumentSpacing;
    private final int oceanMonumentSeparation;
    private final int woodlandMansionSpacing;
    private final int woodlandMansionSeparation;
    private final int buriedTreasureSpacing;
    private final int buriedTreasureSeparation;
    private final int mineshaftSpacing;
    private final int mineshaftSeparation;
    private final int ruinedPortalSpacing;
    private final int ruinedPortalSeparation;
    private final int shipwreckSpacing;
    private final int shipwreckSeparation;
    private final int oceanRuinSpacing;
    private final int oceanRuinSeparation;
    private final int endCitySpacing;
    private final int endCitySeparation;
    private final int bastionRemnantSpacing;
    private final int bastionRemnantSeparation;
    private final int netherFortressSpacing;
    private final int netherFortressSeparation;
    private final int netherFossilSpacing;
    private final int netherFossilSeparation;

    public static StructureSettings getStructureSettings(SettingsMap reader) {
        var structureSettingsBuilder = builder();
        structureSettingsBuilder.villageSpacing(reader.getSetting(VILLAGE_SPACING));
        structureSettingsBuilder.villageSeparation(reader.getSetting(VILLAGE_SEPARATION));
        structureSettingsBuilder.desertPyramidSpacing(reader.getSetting(DESERTPYRAMID_SPACING));
        structureSettingsBuilder.desertPyramidSeparation(reader.getSetting(DESERTPYRAMID_SEPARATION));
        structureSettingsBuilder.iglooSpacing(reader.getSetting(IGLOO_SPACING));
        structureSettingsBuilder.iglooSeparation(reader.getSetting(IGLOO_SEPARATION));
        structureSettingsBuilder.jungleTempleSpacing(reader.getSetting(JUNGLETEMPLE_SPACING));
        structureSettingsBuilder.jungleTempleSeparation(reader.getSetting(JUNGLETEMPLE_SEPARATION));
        structureSettingsBuilder.swampHutSpacing(reader.getSetting(SWAMPHUT_SPACING));
        structureSettingsBuilder.swampHutSeparation(reader.getSetting(SWAMPHUT_SEPARATION));
        structureSettingsBuilder.pillagerOutpostSpacing(reader.getSetting(PILLAGEROUTPOST_SPACING));
        structureSettingsBuilder.pillagerOutpostSeparation(reader.getSetting(PILLAGEROUTPOST_SEPARATION));
        structureSettingsBuilder.strongholdSpacing(reader.getSetting(STRONGHOLD_SPACING));
        structureSettingsBuilder.strongholdSeparation(reader.getSetting(STRONGHOLD_SEPARATION));
        structureSettingsBuilder.strongholdDistance(reader.getSetting(STRONGHOLD_DISTANCE));
        structureSettingsBuilder.strongholdSpread(reader.getSetting(STRONGHOLD_SPREAD));
        structureSettingsBuilder.strongholdCount(reader.getSetting(STRONGHOLD_COUNT));
        structureSettingsBuilder.oceanMonumentSpacing(reader.getSetting(OCEANMONUMENT_SPACING));
        structureSettingsBuilder.oceanMonumentSeparation(reader.getSetting(OCEANMONUMENT_SEPARATION));
        structureSettingsBuilder.endCitySpacing(reader.getSetting(ENDCITY_SPACING));
        structureSettingsBuilder.endCitySeparation(reader.getSetting(ENDCITY_SEPARATION));
        structureSettingsBuilder.woodlandMansionSpacing(reader.getSetting(WOODLANDMANSION_SPACING));
        structureSettingsBuilder.woodlandMansionSeparation(reader.getSetting(WOODLANDMANSION_SEPARATION));
        structureSettingsBuilder.buriedTreasureSpacing(reader.getSetting(BURIEDTREASURE_SPACING));
        structureSettingsBuilder.buriedTreasureSeparation(reader.getSetting(BURIEDTREASURE_SEPARATION));
        structureSettingsBuilder.mineshaftSpacing(reader.getSetting(MINESHAFT_SPACING));
        structureSettingsBuilder.mineshaftSeparation(reader.getSetting(MINESHAFT_SEPARATION));
        structureSettingsBuilder.ruinedPortalSpacing(reader.getSetting(RUINEDPORTAL_SPACING));
        structureSettingsBuilder.ruinedPortalSeparation(reader.getSetting(RUINEDPORTAL_SEPARATION));
        structureSettingsBuilder.shipwreckSpacing(reader.getSetting(SHIPWRECK_SPACING));
        structureSettingsBuilder.shipwreckSeparation(reader.getSetting(SHIPWRECK_SEPARATION));
        structureSettingsBuilder.oceanRuinSpacing(reader.getSetting(OCEANRUIN_SPACING));
        structureSettingsBuilder.oceanRuinSeparation(reader.getSetting(OCEANRUIN_SEPARATION));
        structureSettingsBuilder.bastionRemnantSpacing(reader.getSetting(BASTIONREMNANT_SPACING));
        structureSettingsBuilder.bastionRemnantSeparation(reader.getSetting(BASTIONREMNANT_SEPARATION));
        structureSettingsBuilder.netherFortressSpacing(reader.getSetting(NETHERFORTRESS_SPACING));
        structureSettingsBuilder.netherFortressSeparation(reader.getSetting(NETHERFORTRESS_SEPARATION));
        structureSettingsBuilder.netherFossilSpacing(reader.getSetting(NETHERFOSSIL_SPACING));
        structureSettingsBuilder.netherFossilSeparation(reader.getSetting(NETHERFOSSIL_SEPARATION));

        structureSettingsBuilder.woodlandMansionsEnabled(reader.getSetting(WOODLAND_MANSIONS_ENABLED));
        structureSettingsBuilder.netherFortressesEnabled(reader.getSetting(NETHER_FORTRESSES_ENABLED));
        structureSettingsBuilder.buriedTreasureEnabled(reader.getSetting(BURIED_TREASURE_ENABLED));
        structureSettingsBuilder.oceanRuinsEnabled(reader.getSetting(OCEAN_RUINS_ENABLED));
        structureSettingsBuilder.pillagerOutpostsEnabled(reader.getSetting(PILLAGER_OUTPOSTS_ENABLED));
        structureSettingsBuilder.bastionRemnantsEnabled(reader.getSetting(BASTION_REMNANTS_ENABLED));
        structureSettingsBuilder.netherFossilsEnabled(reader.getSetting(NETHER_FOSSILS_ENABLED));
        structureSettingsBuilder.endCitiesEnabled(reader.getSetting(END_CITIES_ENABLED));
        structureSettingsBuilder.ruinedPortalsEnabled(reader.getSetting(RUINED_PORTALS_ENABLED));
        structureSettingsBuilder.shipwrecksEnabled(reader.getSetting(SHIPWRECKS_ENABLED));
        structureSettingsBuilder.strongholdsEnabled(reader.getSetting(STRONGHOLDS_ENABLED));
        structureSettingsBuilder.villagesEnabled(reader.getSetting(VILLAGES_ENABLED));
        structureSettingsBuilder.mineshaftsEnabled(reader.getSetting(MINESHAFTS_ENABLED));
        structureSettingsBuilder.oceanMonumentsEnabled(reader.getSetting(OCEAN_MONUMENTS_ENABLED));
        structureSettingsBuilder.rareBuildingsEnabled(reader.getSetting(RARE_BUILDINGS_ENABLED));
        return structureSettingsBuilder.build();
    }

    @Override
    public String getSectionName() {
        return "Structure Settings";
    }
    
    public static final String[] COMMENT = new String[]{
            "These are global on/off toggles and spacing/separation settings for the entire world for each",
            "vanilla structure type. Spacing/separation work the same way as they do for datapacks.",
            "When set to true, structures configured in biome configs are able to spawn.",
            "Check the biome configs for customisation options per structure type per biome (size etc)."
    };
}