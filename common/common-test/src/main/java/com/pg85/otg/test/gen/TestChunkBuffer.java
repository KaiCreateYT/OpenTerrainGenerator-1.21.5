package com.pg85.otg.test.gen;

import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.gen.ChunkBuffer;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterials;

import java.util.HashMap;
import java.util.Map;

/**
 * ChunkBuffer implementation for headless testing.
 * Stores blocks in memory using packed coordinates as keys.
 */
public class TestChunkBuffer extends ChunkBuffer {

    private final ChunkCoordinate chunkCoordinate;
    private final int minY;
    private final int maxY;
    private final Map<Long, LocalMaterialData> blocks;

    /**
     * Creates a new TestChunkBuffer.
     *
     * @param chunkX chunk X coordinate
     * @param chunkZ chunk Z coordinate
     * @param minY   minimum Y value (e.g., -64 for 1.18+)
     * @param maxY   maximum Y value (e.g., 319 for 1.18+)
     */
    public TestChunkBuffer(int chunkX, int chunkZ, int minY, int maxY) {
        this.chunkCoordinate = ChunkCoordinate.fromChunkCoords(chunkX, chunkZ);
        this.minY = minY;
        this.maxY = maxY;
        this.blocks = new HashMap<>();
    }

    @Override
    public ChunkCoordinate getChunkCoordinate() {
        return chunkCoordinate;
    }

    @Override
    public void setBlock(int blockX, int blockY, int blockZ, LocalMaterialData material) {
        if (blockY < minY || blockY > maxY) {
            return;
        }

        // Convert block coords to local (0-15)
        int localX = blockX & 0xF;
        int localZ = blockZ & 0xF;

        long key = packCoordinates(localX, localZ, blockY);
        if (material == null || material.isEmptyOrAir()) {
            blocks.remove(key);
        } else {
            blocks.put(key, material);
        }

        // Update highest block tracking in parent class
        if (material != null && !material.isEmptyOrAir()) {
            setHighestBlockForColumn(localX, localZ, blockY);
        }
    }

    @Override
    public LocalMaterialData getBlock(int blockX, int blockY, int blockZ) {
        if (blockY < minY || blockY > maxY) {
            return LocalMaterials.AIR;
        }

        int localX = blockX & 0xF;
        int localZ = blockZ & 0xF;

        long key = packCoordinates(localX, localZ, blockY);
        LocalMaterialData material = blocks.get(key);
        return material != null ? material : LocalMaterials.AIR;
    }

    /**
     * Returns the highest Y coordinate with a solid block at the given local position.
     *
     * @param localX local X coordinate (0-15)
     * @param localZ local Z coordinate (0-15)
     * @return highest Y with solid block, or minY-1 if no solid blocks
     */
    public int getHighestBlockY(int localX, int localZ) {
        int highest = minY - 1;
        for (Map.Entry<Long, LocalMaterialData> entry : blocks.entrySet()) {
            long key = entry.getKey();
            int[] coords = unpackCoordinates(key);
            if (coords[0] == localX && coords[1] == localZ) {
                LocalMaterialData material = entry.getValue();
                if (material != null && material.isSolid() && coords[2] > highest) {
                    highest = coords[2];
                }
            }
        }
        return highest;
    }

    /**
     * Returns the registry name of the block at the given local position.
     *
     * @param localX local X coordinate (0-15)
     * @param y      Y coordinate
     * @param localZ local Z coordinate (0-15)
     * @return registry name (e.g., "minecraft:stone") or "minecraft:air" if empty
     */
    public String getBlockNameAt(int localX, int y, int localZ) {
        long key = packCoordinates(localX, localZ, y);
        LocalMaterialData material = blocks.get(key);
        if (material != null) {
            return material.getRegistryName();
        }
        return LocalMaterials.AIR_NAME;
    }

    /**
     * Returns the number of non-air blocks in this buffer.
     */
    public int getBlockCount() {
        return blocks.size();
    }

    /**
     * Packs local X (0-15), local Z (0-15), and Y into a long.
     * Uses bit layout: [unused][Y 24 bits][Z 4 bits][X 4 bits]
     */
    private static long packCoordinates(int localX, int localZ, int y) {
        // Y can be negative (e.g., -64), shift to positive range for storage
        // MC 1.18+ range is -64 to 319, shift by 2048 to handle all possible Y values
        long shiftedY = y + 2048;
        return ((shiftedY & 0xFFFFL) << 8) | ((localZ & 0xFL) << 4) | (localX & 0xFL);
    }

    /**
     * Unpacks coordinates from a long key.
     *
     * @return array of [localX, localZ, y]
     */
    private static int[] unpackCoordinates(long key) {
        int localX = (int) (key & 0xF);
        int localZ = (int) ((key >> 4) & 0xF);
        int shiftedY = (int) ((key >> 8) & 0xFFFF);
        int y = shiftedY - 2048;
        return new int[]{localX, localZ, y};
    }
}
