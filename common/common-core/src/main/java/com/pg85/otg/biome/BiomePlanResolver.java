package com.pg85.otg.biome;

import com.pg85.otg.config.biome.BiomeGroupFunction;
import com.pg85.otg.config.settings.biome.BiomeSettings;
import com.pg85.otg.config.settings.preset.DimensionPresetSettings;
import com.pg85.otg.gen.biome.BiomeData;
import com.pg85.otg.gen.biome.layers.BiomeGroup;
import com.pg85.otg.interfaces.IBiomeResourceLocation;
import com.pg85.otg.util.OTGLog;
import com.pg85.otg.util.logging.LogCategory;

import java.util.*;

/**
 * Pure-logic biome plan resolver. Takes parsed configs and produces a
 * fully resolved BiomePlan with IDs, groups, relationships.
 * NO Minecraft dependency. NO side effects (except logging). Fully testable.
 */
public final class BiomePlanResolver {

    private BiomePlanResolver() {}

    /**
     * Resolves a biome plan from preset config and ordered biome settings.
     *
     * @param presetConfig the preset configuration
     * @param biomeConfigsByResourceLocation ordered map of resource location -> biome settings
     *        (template biomes must already be resolved and included)
     * @param biomeConfigsByName biome name -> biome settings lookup
     * @return a fully resolved BiomePlan
     */
    public static BiomePlan resolve(
            DimensionPresetSettings presetConfig,
            Map<IBiomeResourceLocation, BiomeSettings> biomeConfigsByResourceLocation,
            Map<String, BiomeSettings> biomeConfigsByName
    ) {
        int currentId = 1; // 0 is reserved for ocean
        int totalSlots = biomeConfigsByResourceLocation.size();

        List<BiomePlan.BiomeEntry> biomeEntries = new ArrayList<>();
        BiomeSettings oceanBiomeConfig = null;
        int[] oceanTemperatures = new int[]{0, 0, 0, 0};
        boolean hasOceanBiome = false;

        Map<Integer, List<BiomeData>> isleBiomesAtDepth = new HashMap<>();
        Map<Integer, List<BiomeData>> borderBiomesAtDepth = new HashMap<>();
        Map<String, List<Integer>> biomeIdsByName = new HashMap<>();
        HashMap<Integer, Integer> biomeColorMap = new HashMap<>();

        String defaultOcean = presetConfig.getGenerationSettings().getDefaultOceanBiome();

        for (Map.Entry<IBiomeResourceLocation, BiomeSettings> entry : biomeConfigsByResourceLocation.entrySet()) {
            BiomeSettings biomeSettings = entry.getValue();
            String biomeName = biomeSettings.getIdentitySettings().getBiomeName();

            boolean isOceanBiome = biomeName.equals(defaultOcean);
            if (isOceanBiome) {
                oceanBiomeConfig = biomeSettings;
                hasOceanBiome = true;
            }

            int otgBiomeId = isOceanBiome ? 0 : currentId;

            // Legacy fallback: force last biome to ID 0 if no explicit ocean
            if (otgBiomeId == totalSlots && !hasOceanBiome) {
                otgBiomeId = 0;
            }

            if (otgBiomeId > totalSlots) {
                OTGLog.fatal(LogCategory.CONFIGS,
                        "Fatal error: biome ID " + otgBiomeId + " exceeds total slots " + totalSlots);
                throw new RuntimeException("Biome ID overflow: " + otgBiomeId + " > " + totalSlots);
            }

            biomeSettings.setOTGBiomeId(otgBiomeId);
            biomeEntries.add(new BiomePlan.BiomeEntry(biomeSettings, otgBiomeId, isOceanBiome));

            // Ocean temperature mappings
            resolveOceanTemperature(presetConfig, biomeSettings, otgBiomeId, oceanTemperatures);

            // Track name -> ID
            biomeIdsByName.computeIfAbsent(biomeName, k -> new ArrayList<>()).add(otgBiomeId);

            // Isle/border biome data (skip underground biomes)
            boolean isUnderground = biomeSettings.getUndergroundSettings() != null
                    && biomeSettings.getUndergroundSettings().isUndergroundBiome();
            if (!isUnderground) {
                collectIsleBorderData(biomeSettings, otgBiomeId, isleBiomesAtDepth, borderBiomesAtDepth);
            }

            // Color map (surface only — underground biomes must not appear in FromImage land placement)
            if (!isUnderground) {
                biomeColorMap.put(biomeSettings.getGenerationSettings().getBiomeMapColor().getColor(), otgBiomeId);
            }

            currentId += isOceanBiome ? 0 : 1;
        }

        // Resolve biome groups
        Set<Integer> biomeDepths = new HashSet<>();
        Map<Integer, List<BiomeGroup>> groupDepths = new HashMap<>();
        Map<Integer, BiomeGroup> groupRegistry = processBiomeGroups(
                presetConfig, biomeConfigsByName, biomeDepths, groupDepths);

        // Resolve all BiomeData string name references to int IDs.
        // Must happen here (after the full biomeIdsByName map is built), not
        // scattered in BiomeLayerData constructor where it used to live.
        resolveAllBiomeData(isleBiomesAtDepth, biomeIdsByName);
        resolveAllBiomeData(borderBiomesAtDepth, biomeIdsByName);
        for (List<BiomeGroup> groups : groupDepths.values()) {
            for (BiomeGroup group : groups) {
                for (BiomeData biome : group.biomes) {
                    biome.init(biomeIdsByName);
                }
            }
        }

        // Bit packing validation: BiomeLayers uses GROUP_SHIFT=20, GROUP_BITS=7
        // Biome IDs must fit in 20 bits, group IDs must fit in 7 bits
        int maxBiomeId = (1 << 20) - 1; // 1,048,575
        if (totalSlots > maxBiomeId) {
            throw new IllegalStateException(
                    "Preset has " + totalSlots + " biomes, exceeding the 20-bit maximum of " + maxBiomeId);
        }
        if (groupRegistry.size() > 127) {
            OTGLog.fatal(LogCategory.CONFIGS,
                    "Preset has " + groupRegistry.size() + " biome groups, exceeding the 7-bit maximum of 127. Terrain will be corrupted.");
        }

        return new BiomePlan(
                Collections.unmodifiableList(biomeEntries),
                oceanBiomeConfig,
                oceanTemperatures,
                isleBiomesAtDepth,
                borderBiomesAtDepth,
                biomeIdsByName,
                biomeColorMap,
                groupRegistry,
                biomeDepths,
                groupDepths,
                totalSlots
        );
    }

    private static void resolveOceanTemperature(
            DimensionPresetSettings presetConfig, BiomeSettings settings, int id, int[] temps) {
        String name = settings.getIdentitySettings().getBiomeName();
        if (name.equals(presetConfig.getGenerationSettings().getDefaultWarmOceanBiome())) temps[0] = id;
        if (name.equals(presetConfig.getGenerationSettings().getDefaultLukewarmOceanBiome())) temps[1] = id;
        // OceanTemperatureLayer reads [2]=frozen (noise < -0.4), [3]=cold (noise < -0.2)
        if (name.equals(presetConfig.getGenerationSettings().getDefaultFrozenOceanBiome())) temps[2] = id;
        if (name.equals(presetConfig.getGenerationSettings().getDefaultColdOceanBiome())) temps[3] = id;
    }

    private static void collectIsleBorderData(
            BiomeSettings settings, int otgBiomeId,
            Map<Integer, List<BiomeData>> isleBiomesAtDepth,
            Map<Integer, List<BiomeData>> borderBiomesAtDepth) {

        if (settings.getGenerationSettings().isIsleBiome()) {
            int depth = settings.getGenerationSettings().getBiomeSizeWhenIsle();
            isleBiomesAtDepth.computeIfAbsent(depth, k -> new ArrayList<>()).add(
                    new BiomeData(
                            otgBiomeId,
                            settings.getGenerationSettings().getBiomeRarityWhenIsle(),
                            depth,
                            settings.getVisualSettings().getBiomeTemperature(),
                            settings.getGenerationSettings().getIsleInBiomes(),
                            settings.getGenerationSettings().getBorderInBiomes(),
                            settings.getGenerationSettings().getOnlyBorderNear(),
                            settings.getGenerationSettings().getNotBorderNear()
                    ));
        }

        if (settings.getGenerationSettings().isBorderBiome()) {
            int depth = settings.getGenerationSettings().getBiomeSizeWhenBorder();
            borderBiomesAtDepth.computeIfAbsent(depth, k -> new ArrayList<>()).add(
                    new BiomeData(
                            otgBiomeId,
                            settings.getGenerationSettings().getBiomeRarity(),
                            depth,
                            settings.getVisualSettings().getBiomeTemperature(),
                            settings.getGenerationSettings().getIsleInBiomes(),
                            settings.getGenerationSettings().getBorderInBiomes(),
                            settings.getGenerationSettings().getOnlyBorderNear(),
                            settings.getGenerationSettings().getNotBorderNear()
                    ));
        }
    }

    /**
     * Process biome groups from preset config into resolved group data.
     * Extracted from SharedLegacyBiomeLoader.processBiomeGroups() (lines 547-629).
     */
    static Map<Integer, BiomeGroup> processBiomeGroups(
            DimensionPresetSettings presetConfig,
            Map<String, BiomeSettings> biomeConfigsByName,
            Set<Integer> biomeDepths,
            Map<Integer, List<BiomeGroup>> groupDepths) {

        int genDepth = presetConfig.getGenerationSettings().getGenerationDepth();
        Map<Integer, BiomeGroup> groupRegistry = new HashMap<>();

        for (BiomeGroupFunction group : presetConfig.getGenerationSettings().getBiomeGroupManager().getGroups()) {
            List<BiomeData> biomes = new ArrayList<>();
            var totalDepthRarity = new int[genDepth + 1];
            var maxRarityPerDepth = new int[genDepth + 1];
            float totalTemp = 0;

            HashMap<String, BiomeSettings> groupBiomes = new LinkedHashMap<>();
            for (String biomeGroupEntry : group.getBiomes()) {
                BiomeSettings biomeSettings = biomeConfigsByName.get(biomeGroupEntry);
                if (biomeSettings == null) {
                    OTGLog.error(LogCategory.BIOME_REGISTRY,
                            "Could not find biome " + biomeGroupEntry + " in biome group " + group.getGroupId());
                    continue;
                }
                if (biomeSettings.getUndergroundSettings() != null
                        && biomeSettings.getUndergroundSettings().isUndergroundBiome()) {
                    OTGLog.warn(LogCategory.BIOME_REGISTRY,
                            "Skipping underground biome '{}' in biome group {} — not valid for surface placement",
                            biomeGroupEntry, group.getGroupId());
                    continue;
                }
                groupBiomes.put(biomeGroupEntry, biomeSettings);
            }

            if (groupBiomes.isEmpty()) {
                OTGLog.warn(LogCategory.BIOME_REGISTRY,
                        "Biome group {} has no valid surface biomes (missing configs or only underground biomes), skipping",
                        group.getGroupId());
                continue;
            }

            for (Map.Entry<String, BiomeSettings> biome : groupBiomes.entrySet()) {
                if (biome.getValue() != null) {
                    BiomeSettings config = biome.getValue();
                    BiomeData newBiomeData = new BiomeData(
                            config.getOTGBiomeID().id(),
                            config.getGenerationSettings().getBiomeRarity(),
                            config.getGenerationSettings().getBiomeSize(),
                            config.getVisualSettings().getBiomeTemperature(),
                            config.getGenerationSettings().getIsleInBiomes(),
                            config.getGenerationSettings().getBorderInBiomes(),
                            config.getGenerationSettings().getOnlyBorderNear(),
                            config.getGenerationSettings().getNotBorderNear()
                    );
                    biomes.add(newBiomeData);
                    biomeDepths.add(config.getGenerationSettings().getBiomeSize());
                    totalTemp += config.getVisualSettings().getBiomeTemperature();
                    totalDepthRarity[config.getGenerationSettings().getBiomeSize()] += config.getGenerationSettings().getBiomeRarity();
                }
            }

            for (int depth = 0; depth < totalDepthRarity.length; depth++) {
                for (int j = depth; j < totalDepthRarity.length; j++) {
                    maxRarityPerDepth[depth] += totalDepthRarity[j];
                }
            }

            float avgTemp = groupBiomes.isEmpty() ? 0 : totalTemp / groupBiomes.size();
            BiomeGroup bg = new BiomeGroup(group.getGroupId(), group.getGroupRarity(), biomes, avgTemp, totalDepthRarity, maxRarityPerDepth);

            int groupSize = group.getGenerationDepth();
            groupDepths.computeIfAbsent(groupSize, k -> new ArrayList<>()).add(bg);
            groupRegistry.put(bg.id, bg);
        }

        return groupRegistry;
    }

    private static void resolveAllBiomeData(
            Map<Integer, List<BiomeData>> biomeDataByDepth,
            Map<String, List<Integer>> biomeIdsByName) {
        for (List<BiomeData> biomes : biomeDataByDepth.values()) {
            for (BiomeData biome : biomes) {
                biome.init(biomeIdsByName);
            }
        }
    }
}
