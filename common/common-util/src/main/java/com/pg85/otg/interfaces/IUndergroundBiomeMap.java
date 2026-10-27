package com.pg85.otg.interfaces;

/** Per-chunk lookup of underground biome OTG ids at quart (4-block) resolution. */
public interface IUndergroundBiomeMap {

    // Cave-type indices for per-region cave-scale arrays. Order is fixed across the codebase.
    int CAVE_CHEESE = 0;
    int CAVE_SPAGHETTI3D = 1;
    int CAVE_SPAGHETTI2D = 2;
    int CAVE_NOODLE = 3;
    int CAVE_PILLAR = 4;
    int CAVE_SCALE_COUNT = 5;

    /** @return underground biome OTG id at this world position, or -1 if none. */
    int getUndergroundBiomeId(int worldX, int worldY, int worldZ);

    /** @return true if no underground biome occupies this chunk (fast path). */
    boolean isEmpty();

    /**
     * @return cross-faded per-cave-type density multiplier at this position for the given
     * cave type index (one of the CAVE_* constants), or 1.0 when no underground biome
     * influences this point.
     */
    default float caveScaleAt(int worldX, int worldY, int worldZ, int caveType) {
        return 1.0f;
    }
}
