package com.pg85.otg.test.snapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Compares two terrain snapshots and reports differences.
 * Useful for regression testing terrain generation changes.
 */
public class TerrainSnapshotComparator {

    /**
     * Section name for heightmap comparison results.
     */
    public static final String SECTION_HEIGHTMAP = "heightmap";

    /**
     * Result of comparing two snapshots.
     */
    public static class ComparisonResult {
        /**
         * True if snapshots are identical.
         */
        public boolean identical;

        /**
         * Total number of values compared (heightmap entries + slice entries).
         */
        public int totalValues;

        /**
         * Total number of differences found.
         */
        public int totalDifferences;

        /**
         * Differences organized by section (heightmap, y64, y-32, etc.).
         */
        public Map<String, SectionDiff> sections;

        public ComparisonResult() {
            this.sections = new LinkedHashMap<>();
        }

        /**
         * Returns the percentage of values that differ.
         *
         * @return difference percentage (0.0 to 100.0)
         */
        public double getDifferencePercentage() {
            if (totalValues == 0) {
                return 0.0;
            }
            return (totalDifferences * 100.0) / totalValues;
        }

        /**
         * Returns a summary string for this comparison result.
         */
        public String getSummary() {
            if (identical) {
                return String.format("Identical (%d values compared)", totalValues);
            }
            return String.format("%d differences out of %d values (%.2f%%)",
                    totalDifferences, totalValues, getDifferencePercentage());
        }
    }

    /**
     * Differences within a single section (heightmap or Y-slice).
     */
    public static class SectionDiff {
        /**
         * Name of the section (e.g., "heightmap", "y64", "y-32").
         */
        public String sectionName;

        /**
         * Total number of values compared in this section.
         */
        public int totalValues;

        /**
         * Number of differences in this section.
         */
        public int differences;

        /**
         * List of individual value differences.
         */
        public List<ValueDiff> diffs;

        public SectionDiff(String sectionName) {
            this.sectionName = sectionName;
            this.diffs = new ArrayList<>();
        }

        /**
         * Returns the percentage of values that differ in this section.
         *
         * @return difference percentage (0.0 to 100.0)
         */
        public double getDifferencePercentage() {
            if (totalValues == 0) {
                return 0.0;
            }
            return (differences * 100.0) / totalValues;
        }

        /**
         * Adds a difference to this section.
         */
        public void addDiff(String coord, String oldValue, String newValue) {
            diffs.add(new ValueDiff(coord, oldValue, newValue));
            differences++;
        }
    }

    /**
     * A single value difference at a coordinate.
     */
    public static class ValueDiff {
        /**
         * Coordinate string (e.g., "0,0" or "16,32").
         */
        public String coord;

        /**
         * Value in the baseline snapshot.
         */
        public String oldValue;

        /**
         * Value in the current snapshot.
         */
        public String newValue;

        public ValueDiff() {
        }

        public ValueDiff(String coord, String oldValue, String newValue) {
            this.coord = coord;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }

        @Override
        public String toString() {
            return String.format("%s: %s -> %s", coord, oldValue, newValue);
        }
    }

    /**
     * Compares two terrain snapshots and returns detailed results.
     *
     * @param baseline the baseline (expected) snapshot
     * @param current  the current (actual) snapshot
     * @return comparison result with all differences
     */
    public ComparisonResult compare(SnapshotModel baseline, SnapshotModel current) {
        ComparisonResult result = new ComparisonResult();

        // Compare heightmaps
        SectionDiff heightmapDiff = compareHeightmaps(baseline, current);
        if (heightmapDiff.totalValues > 0) {
            result.sections.put(SECTION_HEIGHTMAP, heightmapDiff);
            result.totalValues += heightmapDiff.totalValues;
            result.totalDifferences += heightmapDiff.differences;
        }

        // Compare slices
        Set<String> allSliceKeys = new HashSet<>();
        if (baseline.slices != null) {
            allSliceKeys.addAll(baseline.slices.keySet());
        }
        if (current.slices != null) {
            allSliceKeys.addAll(current.slices.keySet());
        }

        for (String sliceKey : allSliceKeys) {
            Map<String, String> baselineSlice = baseline.slices != null ? baseline.slices.get(sliceKey) : null;
            Map<String, String> currentSlice = current.slices != null ? current.slices.get(sliceKey) : null;

            SectionDiff sliceDiff = compareSlices(sliceKey, baselineSlice, currentSlice);
            if (sliceDiff.totalValues > 0) {
                result.sections.put(sliceKey, sliceDiff);
                result.totalValues += sliceDiff.totalValues;
                result.totalDifferences += sliceDiff.differences;
            }
        }

        result.identical = result.totalDifferences == 0;
        return result;
    }

    /**
     * Compares heightmap data from two snapshots.
     */
    private SectionDiff compareHeightmaps(SnapshotModel baseline, SnapshotModel current) {
        SectionDiff diff = new SectionDiff(SECTION_HEIGHTMAP);

        Map<String, Integer> baselineData = getHeightmapData(baseline);
        Map<String, Integer> currentData = getHeightmapData(current);

        Set<String> allCoords = new HashSet<>();
        allCoords.addAll(baselineData.keySet());
        allCoords.addAll(currentData.keySet());

        diff.totalValues = allCoords.size();

        for (String coord : allCoords) {
            Integer baselineHeight = baselineData.get(coord);
            Integer currentHeight = currentData.get(coord);

            if (!Objects.equals(baselineHeight, currentHeight)) {
                String oldVal = baselineHeight != null ? baselineHeight.toString() : "null";
                String newVal = currentHeight != null ? currentHeight.toString() : "null";
                diff.addDiff(coord, oldVal, newVal);
            }
        }

        return diff;
    }

    /**
     * Compares slice data for a single Y level.
     */
    private SectionDiff compareSlices(String sliceKey, Map<String, String> baseline, Map<String, String> current) {
        SectionDiff diff = new SectionDiff(sliceKey);

        Map<String, String> baselineData = baseline != null ? baseline : Map.of();
        Map<String, String> currentData = current != null ? current : Map.of();

        Set<String> allCoords = new HashSet<>();
        allCoords.addAll(baselineData.keySet());
        allCoords.addAll(currentData.keySet());

        diff.totalValues = allCoords.size();

        for (String coord : allCoords) {
            String baselineBlock = baselineData.get(coord);
            String currentBlock = currentData.get(coord);

            if (!Objects.equals(baselineBlock, currentBlock)) {
                String oldVal = baselineBlock != null ? baselineBlock : "null";
                String newVal = currentBlock != null ? currentBlock : "null";
                diff.addDiff(coord, oldVal, newVal);
            }
        }

        return diff;
    }

    /**
     * Safely extracts heightmap data from a snapshot.
     */
    private Map<String, Integer> getHeightmapData(SnapshotModel snapshot) {
        if (snapshot == null || snapshot.heightmap == null || snapshot.heightmap.data == null) {
            return Map.of();
        }
        return snapshot.heightmap.data;
    }
}
