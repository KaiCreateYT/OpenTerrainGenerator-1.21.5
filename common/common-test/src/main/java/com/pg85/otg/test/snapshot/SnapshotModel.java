package com.pg85.otg.test.snapshot;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * POJO for terrain snapshots, serialized to/from JSON using Jackson.
 *
 * A snapshot captures the terrain state for a region of chunks, including:
 * - Metadata (seed, version, region bounds, timestamp)
 * - Heightmap data at configurable resolution
 * - Block slices at specific Y levels
 */
public class SnapshotModel {

    @JsonProperty("metadata")
    public Metadata metadata;

    @JsonProperty("heightmap")
    public Heightmap heightmap;

    /**
     * Block data slices at specific Y levels.
     * Key format: "y64" for Y=64, "y-32" for Y=-32
     * Value: Map of "x,z" -> block registry name
     */
    @JsonProperty("slices")
    public Map<String, Map<String, String>> slices;

    public SnapshotModel() {
        this.slices = new LinkedHashMap<>();
    }

    /**
     * Snapshot metadata including world seed, version, and generation info.
     */
    public static class Metadata {

        @JsonProperty("seed")
        public long seed;

        @JsonProperty("version")
        public String version;

        @JsonProperty("region")
        public Region region;

        @JsonProperty("generatedAt")
        public Instant generatedAt;

        @JsonProperty("presetName")
        public String presetName;

        public Metadata() {
        }

        public Metadata(long seed, String version, Region region, Instant generatedAt, String presetName) {
            this.seed = seed;
            this.version = version;
            this.region = region;
            this.generatedAt = generatedAt;
            this.presetName = presetName;
        }
    }

    /**
     * Region bounds in chunk coordinates.
     */
    public static class Region {

        /**
         * Starting chunk coordinates [x, z] (inclusive).
         */
        @JsonProperty("fromChunk")
        public int[] fromChunk;

        /**
         * Ending chunk coordinates [x, z] (inclusive).
         */
        @JsonProperty("toChunk")
        public int[] toChunk;

        public Region() {
        }

        public Region(int fromX, int fromZ, int toX, int toZ) {
            this.fromChunk = new int[]{fromX, fromZ};
            this.toChunk = new int[]{toX, toZ};
        }

        public int fromX() {
            return fromChunk[0];
        }

        public int fromZ() {
            return fromChunk[1];
        }

        public int toX() {
            return toChunk[0];
        }

        public int toZ() {
            return toChunk[1];
        }
    }

    /**
     * Heightmap data for the snapshot region.
     */
    public static class Heightmap {

        /**
         * Resolution in blocks (e.g., 4 means sample every 4th block).
         */
        @JsonProperty("resolution")
        public int resolution;

        /**
         * Height data keyed by "x,z" coordinate string.
         */
        @JsonProperty("data")
        public Map<String, Integer> data;

        public Heightmap() {
            this.data = new LinkedHashMap<>();
        }

        public Heightmap(int resolution) {
            this.resolution = resolution;
            this.data = new LinkedHashMap<>();
        }

        public void setHeight(int x, int z, int height) {
            data.put(coordKey(x, z), height);
        }

        public Integer getHeight(int x, int z) {
            return data.get(coordKey(x, z));
        }
    }

    /**
     * Creates a coordinate key string from x and z values.
     *
     * @param x the X coordinate
     * @param z the Z coordinate
     * @return formatted key "x,z"
     */
    public static String coordKey(int x, int z) {
        return x + "," + z;
    }

    /**
     * Gets or creates a slice map for the given Y level.
     *
     * @param y the Y level
     * @return the slice map for this Y level
     */
    public Map<String, String> getOrCreateSlice(int y) {
        String key = "y" + y;
        return slices.computeIfAbsent(key, k -> new LinkedHashMap<>());
    }

    /**
     * Sets a block at the given coordinates in the specified Y slice.
     *
     * @param y the Y level
     * @param x the X coordinate
     * @param z the Z coordinate
     * @param blockName the block registry name
     */
    public void setBlock(int y, int x, int z, String blockName) {
        getOrCreateSlice(y).put(coordKey(x, z), blockName);
    }

    /**
     * Gets a block at the given coordinates from the specified Y slice.
     *
     * @param y the Y level
     * @param x the X coordinate
     * @param z the Z coordinate
     * @return the block registry name, or null if not present
     */
    public String getBlock(int y, int x, int z) {
        Map<String, String> slice = slices.get("y" + y);
        if (slice == null) {
            return null;
        }
        return slice.get(coordKey(x, z));
    }
}
