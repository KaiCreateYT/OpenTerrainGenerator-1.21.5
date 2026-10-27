package com.pg85.otg.test.biome;

import com.pg85.otg.interfaces.ILayerSampler;
import com.pg85.otg.interfaces.ILayerSource;

/**
 * A test implementation of ILayerSource for unit testing biome-related functionality.
 * Provides two modes of operation:
 * - Single biome mode: returns the same biome ID for all coordinates
 * - Multi biome mode: uses a deterministic hash to select from available biome IDs
 */
public class TestBiomeProvider implements ILayerSource {

    private final int defaultBiomeId;
    private final long seed;
    private final int[] availableBiomeIds;
    private final boolean singleBiomeMode;

    /**
     * Creates a provider that returns the same biome ID for all coordinates.
     *
     * @param defaultBiomeId the biome ID to return for all queries
     */
    public TestBiomeProvider(int defaultBiomeId) {
        this.defaultBiomeId = defaultBiomeId;
        this.seed = 0;
        this.availableBiomeIds = null;
        this.singleBiomeMode = true;
    }

    /**
     * Creates a provider that deterministically selects biomes based on coordinates.
     * Uses a hash function combining seed and coordinates to select from available biomes.
     *
     * @param seed               the seed for deterministic selection
     * @param availableBiomeIds  array of biome IDs to select from
     */
    public TestBiomeProvider(long seed, int[] availableBiomeIds) {
        this.defaultBiomeId = availableBiomeIds.length > 0 ? availableBiomeIds[0] : 0;
        this.seed = seed;
        this.availableBiomeIds = availableBiomeIds.clone();
        this.singleBiomeMode = false;
    }

    @Override
    public ILayerSampler getSampler() {
        if (singleBiomeMode) {
            return (x, z) -> defaultBiomeId;
        } else {
            return (x, z) -> {
                if (availableBiomeIds == null || availableBiomeIds.length == 0) {
                    return defaultBiomeId;
                }
                // Deterministic hash based on seed and coordinates
                int hash = computeHash(seed, x, z);
                int index = Math.abs(hash) % availableBiomeIds.length;
                return availableBiomeIds[index];
            };
        }
    }

    /**
     * Computes a deterministic hash from seed and coordinates.
     * Uses a simple but effective mixing function for test predictability.
     */
    private int computeHash(long seed, int x, int z) {
        long hash = seed;
        hash ^= x * 0x5DEECE66DL;
        hash ^= z * 0xB3C6EF3720L;
        hash ^= (hash >>> 33);
        hash *= 0xFF51AFD7ED558CCDL;
        hash ^= (hash >>> 33);
        return (int) hash;
    }
}
