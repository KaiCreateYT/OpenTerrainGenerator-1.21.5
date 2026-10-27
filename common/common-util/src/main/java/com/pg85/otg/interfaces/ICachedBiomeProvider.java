package com.pg85.otg.interfaces;

import com.pg85.otg.config.settings.biome.BiomeSettings;
import com.pg85.otg.util.ChunkCoordinate;

public interface ICachedBiomeProvider
{
	public BiomeSettings[] getBiomeConfigsForChunk(ChunkCoordinate chunkCoordinate);
	public IBiome[] getBiomesForChunk(ChunkCoordinate chunkCoordinate);
	public IBiome[] getBiomesForChunks(ChunkCoordinate chunkCoord, int widthHeightInChunks);	
	public BiomeSettings getBiomeConfig(int x, int z, boolean cacheChunk);
	public BiomeSettings getBiomeConfig(int x, int z);
	public IBiome getBiome(int x, int z);

	public BiomeSettings[] getNoiseBiomeConfigsForRegion(int noiseStartX, int noiseStartZ, int widthHeight);
	public BiomeSettings getNoiseBiomeConfig(int x, int z, boolean cacheChunk);
	public IBiome getNoiseBiome(int x, int z);
}
