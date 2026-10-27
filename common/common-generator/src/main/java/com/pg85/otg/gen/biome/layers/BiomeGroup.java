package com.pg85.otg.gen.biome.layers;

import java.util.List;

import com.pg85.otg.gen.biome.BiomeData;

/**
 * New biome group data for testing purposes
 */
public class BiomeGroup
{
	public BiomeGroup(int id, int rarity, List<BiomeData> biomes, float avgTemp, int[] totalDepthRarity, int[] maxRarityPerDepth) {
        this.id = id;
        this.rarity = rarity;
        this.biomes = biomes;
        this.avgTemp = avgTemp;
        this.totalDepthRarity = totalDepthRarity;
        this.maxRarityPerDepth = maxRarityPerDepth;
    }
	public final int id;
	public final int rarity;
	public final List<BiomeData> biomes;
	
	public final float avgTemp;

	// Used for NormalMode. Both of these arrays should be initialized to genDepth
	// int array with the total depth rarity per depth of this biome group
	public final int[] totalDepthRarity;

	// int array of the max rarity at a given depth in the biome group
	// max rarity is a sum of the total rarity of this depth and all subsequent depths
	public final int[] maxRarityPerDepth;
	
}
