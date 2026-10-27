package com.pg85.otg.util.biome;

import com.pg85.otg.interfaces.IBiomeResourceLocation;

public record OTGBiomeID(int id, IBiomeResourceLocation registryName, String biomeName) implements Comparable<Integer> {
    @Override
    public int compareTo(Integer o) {
        return Integer.compare(id, o);
    }
}
