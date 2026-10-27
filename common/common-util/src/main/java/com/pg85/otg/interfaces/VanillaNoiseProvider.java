package com.pg85.otg.interfaces;

@Deprecated
public interface VanillaNoiseProvider {
    VanillaNoiseValues sample(int blockX, int blockZ);

    record VanillaNoiseValues(double continentalness, double erosion, double weirdness) {}
}
