package com.pg85.otg.util;

import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.util.materials.LocalMaterialBase;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterialTag;
import com.pg85.otg.util.materials.MaterialProperty;

public final class OTGMaterialReader {
    private static IMaterialReader materialReader = new DummyMaterialReader();
    public static void set(IMaterialReader materialReader)
    {
        OTGMaterialReader.materialReader = materialReader;
    }

    /**
     * Gets the material reader - assumes there is only one MaterialReader shared by every preset
     * After removing hardcoded block fallbacks, we don't see a need for preset-specific material readers
     * @return The material reader
     */
    public static IMaterialReader get()
    {
        return OTGMaterialReader.materialReader;
    }

    private static class DummyMaterialReader implements IMaterialReader {

        @Override
        public LocalMaterialData readMaterial(String material) throws InvalidConfigException {
            return new DummyMaterialData(material);
        }

        @Override
        public LocalMaterialTag readTag(String tag) throws InvalidConfigException {
            return new DummyMaterialTag(tag);
        }

        @Override
        public LocalMaterialBase read(String input) throws InvalidConfigException {
            if (input.startsWith("otg")) {
                return readTag(input);
            }
            return readMaterial(input);
        }
    }

    private static class DummyMaterialData extends LocalMaterialData {

        private final String name;

        public DummyMaterialData(String name) {
            super(name);
            this.name = name;
        }

        @Override
        public <T extends Comparable<T>> LocalMaterialData withProperty(MaterialProperty<T> state, T value) {
            return null;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getRegistryName() {
            return "minecraft:"+ name;
        }

        @Override
        public boolean canSnowFallOn() {
            return false;
        }

        @Override
        public boolean canFall() {
            return false;
        }

        @Override
        public boolean isMaterial(LocalMaterialData material) {
            return false;
        }

        @Override
        public boolean isBlockTag(LocalMaterialTag tag) {
            return false;
        }

        @Override
        public boolean isLiquid() {
            return false;
        }

        @Override
        public boolean isSolid() {
            return false;
        }

        @Override
        public boolean isEmptyOrAir() {
            return false;
        }

        @Override
        public boolean isNonCaveAir() {
            return false;
        }

        @Override
        public boolean isAir() {
            return false;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public LocalMaterialData rotate(int rotateTimes) {
            return this;
        }

        @Override
        public boolean equals(Object other) {
            return false;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public LocalMaterialData legalOrPersistentLeaves(boolean leaveIllegalLeaves) {
            return this;
        }
    }
    private static class DummyMaterialTag extends LocalMaterialTag {

        public DummyMaterialTag(String name) {
            super(name);
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
