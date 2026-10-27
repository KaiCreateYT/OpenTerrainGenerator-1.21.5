package com.pg85.otg.gen.surface;

import com.pg85.otg.config.settings.biome.SurfaceSettings;
import com.pg85.otg.util.biome.ReplaceBlockMatrix;
import com.pg85.otg.util.materials.LocalMaterialData;

class MultipleLayersSurfaceGeneratorLayer implements Comparable<MultipleLayersSurfaceGeneratorLayer>
{
	protected final LocalMaterialData surfaceBlock;
	protected final LocalMaterialData underWaterSurfaceBlock;
	protected final LocalMaterialData groundBlock;
	final float maxNoise;

	private boolean initialized = false;
	private boolean surfaceBlockIsReplaced;
	private boolean underWaterSurfaceBlockIsReplaced;
	private boolean groundBlockIsReplaced;

	MultipleLayersSurfaceGeneratorLayer(LocalMaterialData surfaceBlock, LocalMaterialData underWaterSurfaceBlock, LocalMaterialData groundBlock, float maxNoise)
	{
		this.surfaceBlock = surfaceBlock;
		this.underWaterSurfaceBlock = underWaterSurfaceBlock;
		this.groundBlock = groundBlock;
		this.maxNoise = maxNoise;
	}

	LocalMaterialData getBlockReplaced(int y, SurfaceSettings surfaceSettings, LocalMaterialData block, boolean isReplaced)
	{
		// TODO: Make this prettier?
		Init(surfaceSettings.getReplacedBlocks());
		LocalMaterialData materialData = null;
		if(isReplaced)
		{
			materialData = surfaceSettings.getReplacedBlocks().replaceBlock(y, block);
		}
		if(materialData == null)
		{
			materialData = block;
		}
		if(materialData.isAir() && y < surfaceSettings.getWaterLevelMax() && y >= surfaceSettings.getWaterLevelMin())
		{
			materialData = surfaceSettings.getWaterBlockReplaced(y);
		}
		return materialData;
	}

	LocalMaterialData getSurfaceBlockReplaced(int y, SurfaceSettings surfaceSettings)
	{
		return getBlockReplaced(y, surfaceSettings, surfaceBlock, surfaceBlockIsReplaced);
	}

	LocalMaterialData getUnderWaterSurfaceBlockReplaced(int y, SurfaceSettings surfaceSettings)
	{
		return getBlockReplaced(y, surfaceSettings, underWaterSurfaceBlock, underWaterSurfaceBlockIsReplaced);
	}
	
	LocalMaterialData getGroundBlockReplaced(int y, SurfaceSettings surfaceSettings)
	{
		return getBlockReplaced(y, surfaceSettings, groundBlock, groundBlockIsReplaced);
	}
	
	private void Init(ReplaceBlockMatrix replacedBlocks)
	{
		if(!initialized)
		{
			initialized = true;
			surfaceBlockIsReplaced = replacedBlocks.replacesBlock(surfaceBlock);
			underWaterSurfaceBlockIsReplaced = replacedBlocks.replacesBlock(underWaterSurfaceBlock);
			groundBlockIsReplaced = replacedBlocks.replacesBlock(groundBlock);
		}
	}
	
	@Override
	public int compareTo(MultipleLayersSurfaceGeneratorLayer that)
	{
		float delta = this.maxNoise - that.maxNoise;
		// The number 65565 is just randomly chosen, any positive number
		// works fine as long as it can represent the floating point delta
		// as an integer
		return (int) (delta * 65565);
	}
}
