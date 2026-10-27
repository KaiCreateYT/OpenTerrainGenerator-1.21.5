package com.pg85.otg.biome;

import com.pg85.otg.config.biome.BiomeConfig;
import com.pg85.otg.config.settings.biome.BiomeSettings;
import com.pg85.otg.interfaces.IBiomeResourceLocation;
import com.pg85.otg.presets.DimensionPreset;
import com.pg85.otg.test.preset.TestDimensionPresetLoader;
import com.pg85.otg.util.biome.OTGBiomeResourceLocation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class BiomePlanResolverTest {

    private static DimensionPreset defaultPreset;

    @BeforeAll
    static void loadPreset() {
        Path otgRoot = Path.of("resources");
        defaultPreset = TestDimensionPresetLoader.loadPreset(otgRoot, "DefaultPreset");
        assertNotNull(defaultPreset, "DefaultPreset must exist in resources");
    }

    @Test
    void oceanBiomeGetsIdZero() {
        BiomePlan plan = resolveDefaultPreset();
        assertNotNull(plan.oceanBiomeConfig(), "Ocean biome should be resolved");
        BiomePlan.BiomeEntry oceanEntry = plan.biomeEntries().stream()
                .filter(BiomePlan.BiomeEntry::isOcean)
                .findFirst()
                .orElseThrow();
        assertEquals(0, oceanEntry.otgBiomeId(), "Ocean biome must have ID 0");
    }

    @Test
    void nonOceanBiomesStartAtIdOne() {
        BiomePlan plan = resolveDefaultPreset();
        List<BiomePlan.BiomeEntry> nonOcean = plan.biomeEntries().stream()
                .filter(e -> !e.isOcean())
                .toList();
        assertFalse(nonOcean.isEmpty(), "Should have non-ocean biomes");
        assertEquals(1, nonOcean.get(0).otgBiomeId());
    }

    @Test
    void biomeIdsAreSequentialWithNoGaps() {
        BiomePlan plan = resolveDefaultPreset();
        Set<Integer> ids = new HashSet<>();
        for (BiomePlan.BiomeEntry entry : plan.biomeEntries()) {
            assertTrue(ids.add(entry.otgBiomeId()),
                    "Duplicate biome ID: " + entry.otgBiomeId());
        }
        int maxId = ids.stream().max(Integer::compareTo).orElse(-1);
        assertEquals(plan.biomeEntries().size() - 1, maxId,
                "Max ID should be (count - 1)");
    }

    @Test
    void biomeIdsByNameContainsAllBiomes() {
        BiomePlan plan = resolveDefaultPreset();
        for (BiomePlan.BiomeEntry entry : plan.biomeEntries()) {
            String name = entry.settings().getIdentitySettings().getBiomeName();
            assertTrue(plan.biomeIdsByName().containsKey(name),
                    "biomeIdsByName missing: " + name);
            assertTrue(plan.biomeIdsByName().get(name).contains(entry.otgBiomeId()),
                    "biomeIdsByName[" + name + "] missing ID " + entry.otgBiomeId());
        }
    }

    @Test
    void colorMapHasEntries() {
        BiomePlan plan = resolveDefaultPreset();
        assertFalse(plan.biomeColorMap().isEmpty());
        assertTrue(plan.biomeColorMap().size() <= plan.biomeEntries().size());
    }

    @Test
    void groupRegistryIsNotEmpty() {
        BiomePlan plan = resolveDefaultPreset();
        assertFalse(plan.groupRegistry().isEmpty(),
                "Group registry should have at least one group");
    }

    @Test
    void totalBiomeSlotsMatchesEntryCount() {
        BiomePlan plan = resolveDefaultPreset();
        assertEquals(plan.biomeEntries().size(), plan.totalBiomeSlots());
    }

    private BiomePlan resolveDefaultPreset() {
        Map<IBiomeResourceLocation, BiomeSettings> byResourceLocation = new LinkedHashMap<>();
        Map<String, BiomeSettings> byName = new HashMap<>();

        for (BiomeConfig bc : defaultPreset.getBiomeConfigList()) {
            if (!bc.getIdentitySettings().isTemplateForBiome()) {
                IBiomeResourceLocation loc = new OTGBiomeResourceLocation(
                        defaultPreset.getFolder(),
                        defaultPreset.getRegistryName(),
                        bc.getIdentitySettings().getBiomeName());
                bc.setRegistryKey(loc);
                byResourceLocation.put(loc, bc);
                byName.put(bc.getIdentitySettings().getBiomeName(), bc);
            }
        }

        return BiomePlanResolver.resolve(
                defaultPreset.getConfig(),
                byResourceLocation,
                byName
        );
    }
}
