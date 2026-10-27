package com.pg85.otg.util.gen;

import com.pg85.otg.interfaces.ICachedBiomeProvider;
import com.pg85.otg.interfaces.IPluginConfig;
import com.pg85.otg.config.settings.preset.DimensionPresetSettings;
import com.pg85.otg.interfaces.IWorldGenRegion;
import com.pg85.otg.util.ChunkCoordinate;
import lombok.Getter;

// TODO: Split up worldgenregion into separate classes, one for decoration/worldgen, one for non-worldgen.
public abstract class LocalWorldGenRegion implements IWorldGenRegion
{
	@Getter
	protected final String presetFolderName;
	@Getter
	private final IPluginConfig pluginConfig;
	@Getter
	private final DimensionPresetSettings config;
	protected final DecorationBiomeCache decorationBiomeCache;
	@Getter
	protected final DecorationArea decorationArea;

	/** Creates a LocalChunkAccess to be used during chunk decoration */
	protected LocalWorldGenRegion(String presetFolderName, IPluginConfig pluginConfig, DimensionPresetSettings config, int chunkPosX, int chunkPosZ, ICachedBiomeProvider cachedBiomeProvider)
	{
		this.presetFolderName = presetFolderName;
		this.pluginConfig = pluginConfig;
		this.config = config;
		this.decorationArea = new DecorationArea(ChunkCoordinate.fromChunkCoords(chunkPosX, chunkPosZ));
		this.decorationBiomeCache = new DecorationBiomeCache(chunkPosX, chunkPosZ, cachedBiomeProvider);
	}
}
