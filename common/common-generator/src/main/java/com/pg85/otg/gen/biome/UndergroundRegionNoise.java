package com.pg85.otg.gen.biome;

import com.pg85.otg.gen.noise.PerlinNoiseSampler;

import java.util.Random;

/**
 * 3D presence-noise for a single underground biome. Seeded per biome (worldSeed ^ biomeId)
 * so different underground biome types form independent, non-overlapping patterns.
 * {@link #sample} returns a value in [0,1]. Immutable / thread-safe after construction.
 */
public final class UndergroundRegionNoise {

    private final PerlinNoiseSampler sampler;

    public UndergroundRegionNoise(long worldSeed, int biomeId) {
        // Distinct, well-mixed seed per biome so patterns don't coincide.
        // The additive constant keeps biomeId 0 from seeding straight from worldSeed.
        this.sampler = new PerlinNoiseSampler(
                new Random(worldSeed ^ (biomeId * 0x9E3779B97F4A7C15L + 0xC4CEB9FE1A85EC53L)));
    }

    /**
     * @param regionSize    approximate blob size in blocks (larger = bigger blobs)
     * @param verticalScale Y multiplier inside the noise (&lt;1 stretches vertically)
     * @return value in [0,1]
     */
    public double sample(int worldX, int worldY, int worldZ, double regionSize, double verticalScale) {
        double f = 1.0 / Math.max(1.0, regionSize);
        double raw = this.sampler.sample(worldX * f, worldY * f * verticalScale, worldZ * f, 0.0, 0.0);
        double n = raw * 0.5 + 0.5; // Perlin output is within (-1,1); map to [0,1], clamp defensively
        if (n < 0.0) return 0.0;
        if (n > 1.0) return 1.0;
        return n;
    }
}
