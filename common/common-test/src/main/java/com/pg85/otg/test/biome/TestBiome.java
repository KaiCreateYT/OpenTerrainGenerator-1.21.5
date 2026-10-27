package com.pg85.otg.test.biome;

import com.pg85.otg.config.settings.biome.BiomeSettings;
import com.pg85.otg.interfaces.IBiome;

/**
 * A simple test implementation of IBiome for unit testing purposes.
 * Provides predictable temperature calculations based on a base temperature
 * with height-based penalty applied above y=64.
 */
public class TestBiome implements IBiome {

    private final BiomeSettings biomeSettings;
    private final float baseTemperature;

    /**
     * Creates a new TestBiome with the given settings and base temperature.
     *
     * @param biomeSettings    the biome settings to use
     * @param baseTemperature  the base temperature value for this biome
     */
    public TestBiome(BiomeSettings biomeSettings, float baseTemperature) {
        this.biomeSettings = biomeSettings;
        this.baseTemperature = baseTemperature;
    }

    @Override
    public BiomeSettings getBiomeSettings() {
        return biomeSettings;
    }

    @Override
    public float getTemperatureAt(int x, int y, int z) {
        if (y > 64) {
            // Apply height penalty: reduce temperature by 0.0016 per block above y=64
            // This mimics Minecraft's vanilla temperature decrease at higher altitudes
            float heightPenalty = (y - 64) * 0.0016f;
            return baseTemperature - heightPenalty;
        }
        return baseTemperature;
    }
}
