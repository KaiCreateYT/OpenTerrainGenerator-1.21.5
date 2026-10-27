package com.pg85.otg.biome;

import com.pg85.otg.config.settings.biome.BiomeSettings;
import com.pg85.otg.gen.biome.BiomeData;
import com.pg85.otg.gen.biome.layers.BiomeGroup;

import java.util.*;

/**
 * Immutable result of biome plan resolution. Contains all resolved IDs,
 * group assignments, and relationships needed for layer generation and
 * MC biome registration. No MC dependency.
 */
public record BiomePlan(
    /** Ordered list of biome entries with resolved OTG IDs */
    List<BiomeEntry> biomeEntries,
    /** Ocean biome settings, or null if no ocean defined */
    BiomeSettings oceanBiomeConfig,
    /** Ocean temperature variant IDs: [warm, lukewarm, cold, frozen] */
    int[] oceanTemperatures,
    /** Isle biomes grouped by generation depth */
    Map<Integer, List<BiomeData>> isleBiomesAtDepth,
    /** Border biomes grouped by generation depth */
    Map<Integer, List<BiomeData>> borderBiomesAtDepth,
    /** Biome name -> list of OTG IDs (for name->ID resolution) */
    Map<String, List<Integer>> biomeIdsByName,
    /** Biome color (int) -> OTG biome ID (for FromImage mode) */
    HashMap<Integer, Integer> biomeColorMap,
    /** Group ID -> BiomeGroup data */
    Map<Integer, BiomeGroup> groupRegistry,
    /** Set of all biome depths that have biomes */
    Set<Integer> biomeDepths,
    /** Group depth -> list of groups at that depth */
    Map<Integer, List<BiomeGroup>> groupDepths,
    /** Total number of biome slots (array size for IBiome[]) */
    int totalBiomeSlots
) {
    /**
     * A single biome entry in the plan - pairs a BiomeSettings with its resolved OTG ID.
     */
    public record BiomeEntry(
        BiomeSettings settings,
        int otgBiomeId,
        boolean isOcean
    ) {}
}
