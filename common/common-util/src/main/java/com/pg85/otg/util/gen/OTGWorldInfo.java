package com.pg85.otg.util.gen;

import java.util.Random;

/**
 * @param minY The minimum and maximum Y values for the world.
 *             In modern minecraft, the max y is 320 but not inclusive, meaning max build height is 319.
 *             The min y is -64, which is inclusive
 *             This gives a combined height of 382 blocks, not including Y 320
 * @param maxY maxY here is *inclusive*, meaning 255 in old MC, 319 in new MC
 */
public record OTGWorldInfo(int minY, int maxY) {

    /**
     * @return The inclusive height of the world
     */
    public int getInclusiveHeight() {
        return maxY - minY;
    }

    /**
     * @return The full height of the world, which is inclusive height + 1
     */
    public int getHeight() {
        return getInclusiveHeight() + 1;
    }

    public int getXAboveMin(int offset) {
        return offset + minY;
    }

    public int getXBelowMax(int offset) {
        return maxY - offset;
    }

    // Return a random height within X above the min height
    public int getXAboveMin(int bound, Random random) {
        return random.nextInt(bound) + minY;
    }

    public int getXBelowMax(int bound, Random random) {
        return maxY - random.nextInt(bound);
    }

    /**
     * @return The index used to specify an empty chunk, or index not found
     */
    public int getEmptyChunkIndex() {
        // Old implementation used "-1" to specify empty chunk, or index not found
        // Let's use one below min Y for the same here
        return minY - 1;
    }

    @Override
    public String toString() {
        return "minY: " + minY + ", maxY: " + maxY;
    }
}
