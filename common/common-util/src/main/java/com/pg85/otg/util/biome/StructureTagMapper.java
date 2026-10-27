package com.pg85.otg.util.biome;

import com.pg85.otg.config.settings.biome.BiomeStructureTagConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Maps {@link BiomeStructureTagConfig} boolean fields to Minecraft structure biome tag paths.
 * Used by platform mixins to inject OTG biomes into the correct vanilla structure tags.
 */
public final class StructureTagMapper {

    private StructureTagMapper() {}

    /**
     * Returns Minecraft biome tag paths (e.g. "minecraft:has_structure/ancient_city")
     * that the given biome should belong to, based on its structure tag config.
     */
    public static List<String> getStructureTags(BiomeStructureTagConfig config) {
        List<String> tags = new ArrayList<>();

        if (config.isAncientCity()) tags.add("minecraft:has_structure/ancient_city");
        if (config.isBastionRemnant()) tags.add("minecraft:has_structure/bastion_remnant");
        if (config.isBuriedTreasure()) tags.add("minecraft:has_structure/buried_treasure");
        if (config.isDesertPyramid()) tags.add("minecraft:has_structure/desert_pyramid");
        if (config.isEndCity()) tags.add("minecraft:has_structure/end_city");
        if (config.isIgloo()) tags.add("minecraft:has_structure/igloo");
        if (config.isJungleTemple()) tags.add("minecraft:has_structure/jungle_temple");
        if (config.isMineshaft()) tags.add("minecraft:has_structure/mineshaft");
        if (config.isMineshaftMesa()) tags.add("minecraft:has_structure/mineshaft_mesa");
        if (config.isNetherFortress()) tags.add("minecraft:has_structure/nether_fortress");
        if (config.isNetherFossil()) tags.add("minecraft:has_structure/nether_fossil");
        if (config.isOceanMonument()) tags.add("minecraft:has_structure/ocean_monument");
        if (config.isOceanRuinCold()) tags.add("minecraft:has_structure/ocean_ruin_cold");
        if (config.isOceanRuinWarm()) tags.add("minecraft:has_structure/ocean_ruin_warm");
        if (config.isPillagerOutpost()) tags.add("minecraft:has_structure/pillager_outpost");
        if (config.isRuinedPortalDesert()) tags.add("minecraft:has_structure/ruined_portal_desert");
        if (config.isRuinedPortalJungle()) tags.add("minecraft:has_structure/ruined_portal_jungle");
        if (config.isRuinedPortalMountain()) tags.add("minecraft:has_structure/ruined_portal_mountain");
        if (config.isRuinedPortalNether()) tags.add("minecraft:has_structure/ruined_portal_nether");
        if (config.isRuinedPortalOcean()) tags.add("minecraft:has_structure/ruined_portal_ocean");
        if (config.isRuinedPortalStandard()) tags.add("minecraft:has_structure/ruined_portal_standard");
        if (config.isRuinedPortalSwamp()) tags.add("minecraft:has_structure/ruined_portal_swamp");
        if (config.isShipwreck()) tags.add("minecraft:has_structure/shipwreck");
        if (config.isShipwreckBeached()) tags.add("minecraft:has_structure/shipwreck_beached");
        if (config.isStronghold()) tags.add("minecraft:has_structure/stronghold");
        if (config.isSwampHut()) tags.add("minecraft:has_structure/swamp_hut");
        if (config.isTrailRuins()) tags.add("minecraft:has_structure/trail_ruins");
        if (config.isTrialChambers()) tags.add("minecraft:has_structure/trial_chambers");
        if (config.isVillageDesert()) tags.add("minecraft:has_structure/village_desert");
        if (config.isVillagePlains()) tags.add("minecraft:has_structure/village_plains");
        if (config.isVillageSavanna()) tags.add("minecraft:has_structure/village_savanna");
        if (config.isVillageSnowy()) tags.add("minecraft:has_structure/village_snowy");
        if (config.isVillageTaiga()) tags.add("minecraft:has_structure/village_taiga");
        if (config.isWoodlandMansion()) tags.add("minecraft:has_structure/woodland_mansion");
        // Note: woodlandHouse has no matching MC structure tag — intentionally skipped.

        return tags;
    }
}
