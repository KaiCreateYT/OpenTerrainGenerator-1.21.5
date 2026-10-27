package com.pg85.otg.gen.biome;

import com.pg85.otg.interfaces.IBiome;
import com.pg85.otg.interfaces.ILayerSampler;
import com.pg85.otg.interfaces.IUndergroundBiomeMap;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.gen.OTGWorldInfo;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

import java.util.function.ToIntBiFunction;

/**
 * Pre-computed per-chunk map of underground biome ids at quart resolution
 * (4x4 columns in XZ, quart steps in Y). Built once via {@link #build}, then read
 * cheaply per-block by block placement and per-region by decoration.
 */
public final class UndergroundBiomeMap implements IUndergroundBiomeMap {

    private final IBiome[] biomesById;
    private final int originBlockX;
    private final int originBlockZ;
    private final int minY;
    private final int quartsY;
    private final int[] ids; // index: (qx*4 + qz) * quartsY + qy ; value: ug id or -1
    private final float[] caveScales; // flat: ((qx*4+qz)*quartsY + qy)*CAVE_SCALE_COUNT + caveType; null = all 1.0
    private final boolean empty;

    private UndergroundBiomeMap(IBiome[] biomesById, int originBlockX, int originBlockZ,
                               int minY, int quartsY, int[] ids, float[] caveScales, boolean empty) {
        this.biomesById = biomesById;
        this.originBlockX = originBlockX;
        this.originBlockZ = originBlockZ;
        this.minY = minY;
        this.quartsY = quartsY;
        this.ids = ids;
        this.caveScales = caveScales;
        this.empty = empty;
    }

    private static final UndergroundBiomeMap EMPTY =
            new UndergroundBiomeMap(new IBiome[0], 0, 0, 0, 0, new int[0], null, true);

    /** Shared empty map (no underground biomes) — used for shadow/height-probe chunks. */
    public static UndergroundBiomeMap empty() {
        return EMPTY;
    }

    public static UndergroundBiomeMap build(UndergroundBiomeResolver resolver, ILayerSampler sampler,
                                            ToIntBiFunction<Integer, Integer> heightEstimator,
                                            ChunkCoordinate chunkCoord, OTGWorldInfo worldInfo,
                                            IBiome[] biomesById) {
        int originBlockX = chunkCoord.getBlockX();
        int originBlockZ = chunkCoord.getBlockZ();
        int minY = worldInfo.minY();
        int quartsY = (worldInfo.maxY() - minY + 1 + 3) / 4;
        int[] ids = new int[16 * quartsY];
        float[] caveScales = null; // lazily allocated (filled with 1.0) on first non-default quart
        boolean empty = true;

        for (int qx = 0; qx < 4; qx++) {
            for (int qz = 0; qz < 4; qz++) {
                int worldX = originBlockX + (qx << 2);
                int worldZ = originBlockZ + (qz << 2);
                int surfaceId = sampler.sample(worldX >> 2, worldZ >> 2);
                int estSurfaceY = heightEstimator != null
                        ? heightEstimator.applyAsInt(Integer.valueOf(worldX), Integer.valueOf(worldZ))
                        : 64;
                int base = (qx * 4 + qz) * quartsY;
                for (int qy = 0; qy < quartsY; qy++) {
                    int worldY = minY + (qy << 2);
                    int id = resolver.resolve(surfaceId, worldX, worldY, worldZ, estSurfaceY);
                    ids[base + qy] = id;
                    if (id >= 0) empty = false;
                    float[] scales = resolver.resolveCaveScales(surfaceId, worldX, worldY, worldZ, estSurfaceY);
                    if (notAllOnes(scales)) {
                        if (caveScales == null) {
                            caveScales = new float[16 * quartsY * CAVE_SCALE_COUNT];
                            java.util.Arrays.fill(caveScales, 1.0f);
                        }
                        System.arraycopy(scales, 0, caveScales, (base + qy) * CAVE_SCALE_COUNT, CAVE_SCALE_COUNT);
                    }
                }
            }
        }
        return new UndergroundBiomeMap(biomesById, originBlockX, originBlockZ, minY, quartsY, ids, caveScales, empty);
    }

    @Override
    public int getUndergroundBiomeId(int worldX, int worldY, int worldZ) {
        if (this.empty) return -1;
        int qx = (worldX - this.originBlockX) >> 2;
        int qz = (worldZ - this.originBlockZ) >> 2;
        if (qx < 0 || qx > 3 || qz < 0 || qz > 3) return -1;
        int qy = (worldY - this.minY) >> 2;
        if (qy < 0 || qy >= this.quartsY) return -1;
        return this.ids[(qx * 4 + qz) * this.quartsY + qy];
    }

    @Override
    public float caveScaleAt(int worldX, int worldY, int worldZ, int caveType) {
        if (this.caveScales == null) return 1.0f;
        int qx = (worldX - this.originBlockX) >> 2;
        int qz = (worldZ - this.originBlockZ) >> 2;
        if (qx < 0 || qx > 3 || qz < 0 || qz > 3) return 1.0f;
        int qy = (worldY - this.minY) >> 2;
        if (qy < 0 || qy >= this.quartsY) return 1.0f;
        return this.caveScales[((qx * 4 + qz) * this.quartsY + qy) * CAVE_SCALE_COUNT + caveType];
    }

    private static boolean notAllOnes(float[] s) {
        for (float v : s) {
            if (v != 1.0f) return true;
        }
        return false;
    }

    @Override
    public boolean isEmpty() {
        return this.empty;
    }

    /** Distinct underground biome ids present in this chunk (for decoration). */
    public IntList getPresentBiomeIds() {
        IntList present = new IntArrayList();
        if (this.empty) return present;
        for (int id : this.ids) {
            if (id >= 0 && !present.contains(id)) present.add(id);
        }
        return present;
    }

    public IBiome getBiome(int ugId) {
        return this.biomesById[ugId];
    }
}
