package com.pg85.otg.util.materials;

import lombok.Getter;

/**
 * A MaterialGroup is an enum that represents the special values that can be present in a MaterialSet
 */
@Getter
public enum MaterialGroup {
    ALL_MATERIALS("All"), // ALL Materials
    SOLID_MATERIALS("Solid"), // Solid Materials
    NON_SOLID_MATERIALS("NonSolid"), // Non-Solid Materials
    LIQUIDS("Liquid"), // Liquid Materials
    AIR("Air"), // Just Air
    NONE("None");

    private final String keyword;

    MaterialGroup(String keyword) {
        this.keyword = keyword;
    }
    public static MaterialGroup ofKeyword(String keyword) {
        for (MaterialGroup group : values()) {
            if (group.getKeyword().equalsIgnoreCase(keyword)) {
                return group;
            }
        }
        return null;
    }

    public boolean contains(LocalMaterialData material) {
        if (material == null || material.isEmpty()) {
            return false;
        }
        return switch (this) {
            case ALL_MATERIALS -> true;
            case SOLID_MATERIALS -> material.isSolid();
            case NON_SOLID_MATERIALS -> !material.isSolid();
            case LIQUIDS -> material.isLiquid();
            case AIR -> material.isAir();
            case NONE -> false;
        };
    }
}
