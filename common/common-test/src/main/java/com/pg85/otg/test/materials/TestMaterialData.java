package com.pg85.otg.test.materials;

import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterialTag;
import com.pg85.otg.util.materials.MaterialProperty;

import java.util.Objects;

/**
 * Lightweight LocalMaterialData implementation for headless testing.
 * Stores only name and basic flags - no Minecraft dependencies required.
 */
public class TestMaterialData extends LocalMaterialData {

    private final String registryName;
    private final boolean solid;
    private final boolean liquid;
    private final boolean air;

    private TestMaterialData(String registryName, boolean solid, boolean liquid, boolean air) {
        super(registryName);
        this.registryName = registryName;
        this.solid = solid;
        this.liquid = liquid;
        this.air = air;
    }

    // Factory methods

    /**
     * Creates a solid block material.
     */
    public static TestMaterialData solid(String name) {
        return new TestMaterialData(name, true, false, false);
    }

    /**
     * Creates a liquid material (water, lava).
     */
    public static TestMaterialData liquid(String name) {
        return new TestMaterialData(name, false, true, false);
    }

    /**
     * Creates an air material.
     */
    public static TestMaterialData air(String name) {
        return new TestMaterialData(name, false, false, true);
    }

    /**
     * Creates a non-solid, non-liquid, non-air material (e.g., plants, flowers).
     */
    public static TestMaterialData nonSolid(String name) {
        return new TestMaterialData(name, false, false, false);
    }

    // Abstract method implementations

    @Override
    public <T extends Comparable<T>> LocalMaterialData withProperty(MaterialProperty<T> state, T value) {
        // Test implementation ignores block states - just return self
        return this;
    }

    @Override
    public String getName() {
        return registryName;
    }

    @Override
    public String getRegistryName() {
        return registryName;
    }

    @Override
    public boolean canSnowFallOn() {
        return solid;
    }

    @Override
    public boolean canFall() {
        // Only sand/gravel fall - for testing we return false
        return false;
    }

    @Override
    public boolean isMaterial(LocalMaterialData material) {
        if (material == null) {
            return false;
        }
        return this.registryName.equals(material.getRegistryName());
    }

    @Override
    public boolean isBlockTag(LocalMaterialTag tag) {
        // Test implementation doesn't support block tags
        return false;
    }

    @Override
    public boolean isLiquid() {
        return liquid;
    }

    @Override
    public boolean isSolid() {
        return solid;
    }

    @Override
    public boolean isEmptyOrAir() {
        return air || isEmpty();
    }

    @Override
    public boolean isNonCaveAir() {
        // Cave air is "minecraft:cave_air", regular air is "minecraft:air"
        return air && !"minecraft:cave_air".equals(registryName);
    }

    @Override
    public boolean isAir() {
        return air;
    }

    @Override
    public boolean isEmpty() {
        return isBlank;
    }

    @Override
    public LocalMaterialData rotate(int rotateTimes) {
        // Test implementation doesn't support rotation - return self
        return this;
    }

    @Override
    public LocalMaterialData legalOrPersistentLeaves(boolean leaveIllegalLeaves) {
        // Test implementation returns self unchanged
        return this;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        TestMaterialData that = (TestMaterialData) other;
        return solid == that.solid &&
               liquid == that.liquid &&
               air == that.air &&
               Objects.equals(registryName, that.registryName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(registryName, solid, liquid, air);
    }

    @Override
    public String toString() {
        return registryName;
    }
}
