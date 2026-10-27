package com.pg85.otg.gen.biome;

import com.pg85.otg.config.settings.biome.BiomeSettings;
import com.pg85.otg.config.settings.biome.UndergroundBiomeSettings;
import com.pg85.otg.interfaces.IBiome;
import com.pg85.otg.interfaces.IUndergroundBiomeMap;
import com.pg85.otg.util.OTGLog;
import com.pg85.otg.util.logging.LogCategory;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Resolves underground biomes from a 3D noise field, independent of the surface biome
 * by default. Pre-computes per-surface-biome candidate lists at construction.
 * Thread-safe: all state immutable after construction.
 */
public class UndergroundBiomeResolver {

    /** Blocks over which the underground zone fades in below the surface threshold. */
    private static final int DEPTH_TRANSITION = 16;

    /** Number of probe samples used to build the empirical noise-value distribution. */
    private static final int QUANTILE_SAMPLES = 4096;

    /** Width (in noise-value units) of the cross-fade band straddling each region's coverage threshold. */
    private static final double BLEND_BAND = 0.12;

    /** Returned (shared, never mutated by callers) when a position has no cave-scale influence. */
    private static final float[] ALL_ONES = { 1f, 1f, 1f, 1f, 1f };

    public record UndergroundCandidate(
            int otgBiomeId, int minY, int maxY, int priority,
            float coverage, int regionSize, float verticalScale, float[] caveScale) {}

    private final Int2ObjectOpenHashMap<List<UndergroundCandidate>> candidatesBySurfaceBiome;
    private final Int2ObjectOpenHashMap<Integer> startOffsetBySurfaceBiome;
    private final Int2ObjectOpenHashMap<UndergroundRegionNoise> noiseByBiomeId;

    /** Sorted empirical samples of the noise value distribution; maps coverage -> threshold. */
    private final float[] coverageQuantiles;

    /** True if any underground biome defines a non-1.0 cave scale; when false, cave-scale resolution is skipped entirely. */
    private final boolean hasCaveScaling;

    public UndergroundBiomeResolver(IBiome[] biomesById, long worldSeed) {
        this.candidatesBySurfaceBiome = new Int2ObjectOpenHashMap<>();
        this.startOffsetBySurfaceBiome = new Int2ObjectOpenHashMap<>();
        this.noiseByBiomeId = new Int2ObjectOpenHashMap<>();

        // Collect underground biomes + build one noise field each.
        List<UndergroundBiomeInfo> undergroundBiomes = new ArrayList<>();
        for (int id = 0; id < biomesById.length; id++) {
            if (biomesById[id] == null) continue;
            BiomeSettings settings = biomesById[id].getBiomeSettings();
            if (settings.getUndergroundSettings() == null) continue;
            UndergroundBiomeSettings ubs = settings.getUndergroundSettings();
            if (!ubs.isUndergroundBiome()) continue;
            undergroundBiomes.add(new UndergroundBiomeInfo(
                    id, settings.getConfigName(),
                    ubs.getUndergroundMinY(), ubs.getUndergroundMaxY(), ubs.getUndergroundPriority(),
                    ubs.getUndergroundBiomeRarity(), ubs.getUndergroundRegionSize(), ubs.getUndergroundVerticalScale(),
                    ubs.getMinSurfaceTemperature(), ubs.getMaxSurfaceTemperature(),
                    ubs.getMinSurfaceWetness(), ubs.getMaxSurfaceWetness(),
                    ubs.getCaveScales()));
            this.noiseByBiomeId.put(id, new UndergroundRegionNoise(worldSeed, id));
        }

        this.coverageQuantiles = buildCoverageQuantiles(worldSeed);

        boolean anyScaling = false;
        for (UndergroundBiomeInfo ub : undergroundBiomes) {
            for (float s : ub.caveScale()) {
                if (s != 1.0f) { anyScaling = true; break; }
            }
            if (anyScaling) break;
        }
        this.hasCaveScaling = anyScaling;

        for (int surfaceId = 0; surfaceId < biomesById.length; surfaceId++) {
            if (biomesById[surfaceId] == null) continue;
            BiomeSettings surfaceSettings = biomesById[surfaceId].getBiomeSettings();
            UndergroundBiomeSettings surfaceUbs = surfaceSettings.getUndergroundSettings();

            int startOffset = (surfaceUbs != null) ? surfaceUbs.getUndergroundBiomeStartOffset() : 8;
            this.startOffsetBySurfaceBiome.put(surfaceId, Integer.valueOf(startOffset));

            if (surfaceUbs != null && surfaceUbs.isUndergroundBiome()) continue; // ug biomes aren't surfaces

            float surfaceTemp = surfaceSettings.getVisualSettings().getBiomeTemperature();
            float surfaceWet = surfaceSettings.getVisualSettings().getBiomeWetness();

            List<String> allowed = (surfaceUbs != null) ? surfaceUbs.getAllowedUndergroundBiomes() : List.of();
            List<String> disallowed = (surfaceUbs != null) ? surfaceUbs.getDisallowedUndergroundBiomes() : List.of();
            boolean hasAllowedFilter = allowed != null && !allowed.isEmpty();
            boolean hasDisallowedFilter = disallowed != null && !disallowed.isEmpty();

            List<UndergroundCandidate> candidates = new ArrayList<>();
            for (UndergroundBiomeInfo ub : undergroundBiomes) {
                // Optional soft coupling to the surface biome (defaults = no coupling).
                if (surfaceTemp < ub.minTemp || surfaceTemp > ub.maxTemp) continue;
                if (surfaceWet < ub.minWet || surfaceWet > ub.maxWet) continue;
                if (hasAllowedFilter && !allowed.contains(ub.biomeName)) continue;
                if (hasDisallowedFilter && disallowed.contains(ub.biomeName)) continue;
                candidates.add(new UndergroundCandidate(
                        ub.otgBiomeId, ub.minY, ub.maxY, ub.priority,
                        ub.coverage, ub.regionSize, ub.verticalScale, ub.caveScale()));
            }
            candidates.sort(Comparator.comparingInt(UndergroundCandidate::priority));
            if (!candidates.isEmpty()) {
                this.candidatesBySurfaceBiome.put(surfaceId, List.copyOf(candidates));
            }
        }

        OTGLog.info(LogCategory.BIOME_REGISTRY,
                "Underground biome resolver initialized: %d underground biomes, %d surface biomes have candidates",
                undergroundBiomes.size(), candidatesBySurfaceBiome.size());
    }

    /**
     * Builds an empirical, sorted sample of the noise value distribution. Perlin output is
     * not uniform, so we calibrate coverage% against the real distribution: see
     * {@link #thresholdForCoverage}. The distribution shape is seed-independent in frequency,
     * so one calibration noise represents all underground biomes.
     */
    private static float[] buildCoverageQuantiles(long worldSeed) {
        UndergroundRegionNoise calibration = new UndergroundRegionNoise(worldSeed, 0x5EED);
        java.util.Random probe = new java.util.Random(worldSeed ^ 0xA17C5EEDL);
        float[] samples = new float[QUANTILE_SAMPLES];
        for (int i = 0; i < QUANTILE_SAMPLES; i++) {
            int x = probe.nextInt(1_000_000) - 500_000;
            int y = probe.nextInt(1_000_000) - 500_000;
            int z = probe.nextInt(1_000_000) - 500_000;
            samples[i] = (float) calibration.sample(x, y, z, 1.0, 1.0);
        }
        java.util.Arrays.sort(samples);
        return samples;
    }

    /**
     * Maps a desired coverage fraction [0,1] to the noise threshold that yields approximately
     * that fraction of volume, using the empirical distribution. coverageFraction=1 fills all,
     * 0 fills none.
     */
    private float thresholdForCoverage(double coverageFraction) {
        double q = 1.0 - coverageFraction; // we want P(n >= threshold) == coverageFraction
        if (q <= 0.0) return Float.NEGATIVE_INFINITY; // fill everything
        if (q >= 1.0) return Float.POSITIVE_INFINITY; // fill nothing
        int idx = (int) (q * (this.coverageQuantiles.length - 1));
        return this.coverageQuantiles[idx];
    }

    /**
     * @return OTG biome id of the underground biome at this position, or -1 for none.
     */
    public int resolve(int surfaceBiomeId, int worldX, int worldY, int worldZ, int estimatedSurfaceY) {
        Integer startOffset = this.startOffsetBySurfaceBiome.get(surfaceBiomeId);
        if (startOffset == null) return -1;

        // Cheap early exit before any arithmetic for surfaces with no candidates.
        List<UndergroundCandidate> candidates = this.candidatesBySurfaceBiome.get(surfaceBiomeId);
        if (candidates == null) return -1;

        int undergroundStart = estimatedSurfaceY - startOffset.intValue();
        if (worldY >= undergroundStart) return -1;

        // Smooth fade-in: 0 at the threshold, ramping to 1 over DEPTH_TRANSITION blocks below.
        double depthWeight = (undergroundStart - worldY) / (double) DEPTH_TRANSITION;
        if (depthWeight > 1.0) depthWeight = 1.0;

        for (UndergroundCandidate c : candidates) {
            if (worldY < c.minY() || worldY > c.maxY()) continue;
            if (c.coverage() <= 0.0f) continue;
            UndergroundRegionNoise noise = this.noiseByBiomeId.get(c.otgBiomeId());
            if (noise == null) continue;
            double effectiveCoverage = (c.coverage() / 100.0) * depthWeight; // [0,1] target volume fraction
            float threshold = thresholdForCoverage(effectiveCoverage);
            double n = noise.sample(worldX, worldY, worldZ, c.regionSize(), c.verticalScale());
            if (n >= threshold) return c.otgBiomeId();
        }
        return -1;
    }

    /**
     * Cross-faded per-cave-type density multipliers at this position. Each candidate region
     * contributes by its membership weight (a fade band straddling its coverage threshold);
     * any remaining weight defaults to 1.0 (normal caves). When total region weight exceeds
     * 1.0 (overlapping regions) the region weights are renormalised so they sum to 1.0.
     *
     * @return length-{@link IUndergroundBiomeMap#CAVE_SCALE_COUNT} array; all 1.0 when no region applies.
     */
    public float[] resolveCaveScales(int surfaceBiomeId, int worldX, int worldY, int worldZ, int estimatedSurfaceY) {
        if (!this.hasCaveScaling) return ALL_ONES;

        Integer startOffset = this.startOffsetBySurfaceBiome.get(surfaceBiomeId);
        if (startOffset == null) return ALL_ONES;
        List<UndergroundCandidate> candidates = this.candidatesBySurfaceBiome.get(surfaceBiomeId);
        if (candidates == null) return ALL_ONES;

        int undergroundStart = estimatedSurfaceY - startOffset.intValue();
        if (worldY >= undergroundStart) return ALL_ONES;

        double depthWeight = (undergroundStart - worldY) / (double) DEPTH_TRANSITION;
        if (depthWeight > 1.0) depthWeight = 1.0;

        int n = candidates.size();
        double[] weights = null;
        double total = 0.0;
        for (int i = 0; i < n; i++) {
            UndergroundCandidate c = candidates.get(i);
            if (worldY < c.minY() || worldY > c.maxY()) continue;
            if (c.coverage() <= 0.0f) continue;
            UndergroundRegionNoise noise = this.noiseByBiomeId.get(c.otgBiomeId());
            if (noise == null) continue;
            double effectiveCoverage = (c.coverage() / 100.0) * depthWeight;
            float threshold = thresholdForCoverage(effectiveCoverage);
            double sample = noise.sample(worldX, worldY, worldZ, c.regionSize(), c.verticalScale());
            double margin = sample - threshold;             // >0 = core, ~0 = edge
            double w = (margin + BLEND_BAND) / BLEND_BAND;  // 1 at/above threshold, ramps to 0 a BLEND_BAND below
            if (w <= 0.0) continue;
            if (w > 1.0) w = 1.0;
            if (weights == null) weights = new double[n];
            weights[i] = w;
            total += w;
        }
        if (weights == null) return ALL_ONES;

        double scaleFactor;
        double normalWeight;
        if (total > 1.0) { scaleFactor = 1.0 / total; normalWeight = 0.0; }
        else { scaleFactor = 1.0; normalWeight = 1.0 - total; }

        float[] out = new float[] { 1f, 1f, 1f, 1f, 1f };
        for (int t = 0; t < IUndergroundBiomeMap.CAVE_SCALE_COUNT; t++) {
            double acc = normalWeight; // normal contributes a multiplier of 1.0
            for (int i = 0; i < n; i++) {
                double w = weights[i];
                if (w == 0.0) continue;
                acc += (w * scaleFactor) * candidates.get(i).caveScale()[t];
            }
            out[t] = (float) acc;
        }
        return out;
    }

    public boolean hasUndergroundBiomes() {
        return !this.candidatesBySurfaceBiome.isEmpty();
    }

    private record UndergroundBiomeInfo(
            int otgBiomeId, String biomeName,
            int minY, int maxY, int priority,
            float coverage, int regionSize, float verticalScale,
            float minTemp, float maxTemp, float minWet, float maxWet,
            float[] caveScale) {}
}
