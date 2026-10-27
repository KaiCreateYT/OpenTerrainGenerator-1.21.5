package com.pg85.otg.customobject.bo4;

import com.pg85.otg.customobject.bo4.bo4function.BO4BlockFunction;
import com.pg85.otg.customobject.bo4.bo4function.BO4BranchFunction;
import com.pg85.otg.customobject.bo4.bo4function.BO4EntityFunction;
import com.pg85.otg.customobject.bo4.bo4function.BO4RandomBlockFunction;
import com.pg85.otg.customobject.bofunctions.BlockFunction;
import com.pg85.otg.customobject.bofunctions.BranchFunction;
import com.pg85.otg.customobject.config.CustomObjectResourcesManager;
import com.pg85.otg.customobject.CustomObjectManager;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IModLoadedChecker;
import com.pg85.otg.util.OTGLog;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.nbt.NamedBinaryTag;

import java.nio.file.Path;
import java.util.List;

class BO4BlockStorage
{
	static final int X_SIZE = 16;
	static final int Z_SIZE = 16;

	private short[][][] blocks;
	private LocalMaterialData[] blocksMaterial;
	private String[] blocksMetaDataName;
	private NamedBinaryTag[] blocksMetaDataTag;

	private LocalMaterialData[][] randomBlocksBlocks;
	private byte[][] randomBlocksBlockChances;
	private String[][] randomBlocksMetaDataNames;
	private NamedBinaryTag[][] randomBlocksMetaDataTags;
	private byte[] randomBlocksBlockCount;

	private BO4BranchFunction[] branchesBO4;
	private BO4EntityFunction[] entityDataBO4;

	private boolean isCollidable = false;
	private BO4BlockFunction[][] heightMap;

	BO4BranchFunction[] getbranches()
	{
		return this.branchesBO4;
	}

	BO4EntityFunction[] getEntityData()
	{
		return this.entityDataBO4;
	}

	boolean isCollidable()
	{
		return this.isCollidable;
	}

	void setCollidable(boolean collidable)
	{
		this.isCollidable = collidable;
	}

	void setBranches(List<BranchFunction<?>> branches)
	{
		this.branchesBO4 = branches.toArray(new BO4BranchFunction[0]);
	}

	void setBranchesArray(BO4BranchFunction[] branches)
	{
		this.branchesBO4 = branches;
	}

	void setEntityData(BO4EntityFunction[] entities)
	{
		this.entityDataBO4 = entities;
	}

	void setBlocks(List<BlockFunction<?>> newBlocks)
	{
		short[][] columnSizes = new short[16][16];
		for(BlockFunction<?> block : newBlocks)
		{
			columnSizes[block.x][block.z]++;
		}
		loadBlockArrays(newBlocks, columnSizes);
	}

	void loadBlockArrays(List<BlockFunction<?>> newBlocks, short[][] columnSizes)
	{
		// Store blocks in arrays instead of BO4BlockFunctions,
		// since that gives way too much overhead memory wise.
		// We may have tens of millions of blocks, java doesn't handle lots of small classes well.
		this.blocks = new short[X_SIZE][Z_SIZE][];
		this.blocksMaterial = new LocalMaterialData[newBlocks.size()];
		this.blocksMetaDataName = new String[newBlocks.size()];
		this.blocksMetaDataTag = new NamedBinaryTag[newBlocks.size()];

		this.randomBlocksBlocks = new LocalMaterialData[newBlocks.size()][];
		this.randomBlocksBlockChances = new byte[newBlocks.size()][];
		this.randomBlocksMetaDataNames = new String[newBlocks.size()][];
		this.randomBlocksMetaDataTags = new NamedBinaryTag[newBlocks.size()][];
		this.randomBlocksBlockCount = new byte[newBlocks.size()];

		BO4BlockFunction block;
		short[][] columnBlockIndex = new short[X_SIZE][Z_SIZE];
		for(int x = 0; x < X_SIZE; x++)
		{
			for(int z = 0; z < Z_SIZE; z++)
			{
				if(this.blocks[x][z] == null)
				{
					this.blocks[x][z] = new short[columnSizes[x][z]];
				}
			}
		}
		for (BlockFunction<?> newBlock : newBlocks)
		{
			block = (BO4BlockFunction) newBlock;

			this.blocks[block.x][block.z][columnBlockIndex[block.x][block.z]] = (short) block.y;

			int blockIndex = columnBlockIndex[block.x][block.z] + getColumnBlockIndex(columnSizes, block.x, block.z);

			this.blocksMaterial[blockIndex] = block.material;
			this.blocksMetaDataName[blockIndex] = block.nbtName;
			this.blocksMetaDataTag[blockIndex] = block.nbt;

			if (block instanceof BO4RandomBlockFunction)
			{
				this.randomBlocksBlocks[blockIndex] = ((BO4RandomBlockFunction) block).blocks;
				this.randomBlocksBlockChances[blockIndex] = ((BO4RandomBlockFunction) block).blockChances;
				this.randomBlocksMetaDataNames[blockIndex] = ((BO4RandomBlockFunction) block).metaDataNames;
				this.randomBlocksMetaDataTags[blockIndex] = ((BO4RandomBlockFunction) block).metaDataTags;
				this.randomBlocksBlockCount[blockIndex] = ((BO4RandomBlockFunction) block).blockCount;
			}
			columnBlockIndex[block.x][block.z]++;
		}
	}

	private int getColumnBlockIndex(short[][] columnSizes, int columnX, int columnZ)
	{
		int blockIndex = 0;
		for(int x = 0; x < 16; x++)
		{
			for(int z = 0; z < 16; z++)
			{
				if(columnX == x && columnZ == z)
				{
					return blockIndex;
				}
				blockIndex += columnSizes[x][z];
			}
		}
		return blockIndex;
	}

	BO4BlockFunction[] getBlocks(BO4Config config, boolean fromFile, String presetFolderName, Path otgRootFolder, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		if(fromFile && config.isBO4Data)
		{
			BO4Config bo4Config = config.createBlankCopy();
			try
			{
				bo4Config.readFromBO4DataFile(true, materialReader);
			}
			catch (InvalidConfigException e)
			{
				OTGLog.error(LogCategory.CUSTOM_OBJECTS, "Error fetching blocks for BO4Data {}: {}", config.getName(), e.getMessage());
				return null;
			}
			return bo4Config.blockStorage.getBlocks(bo4Config, false, presetFolderName, otgRootFolder, customObjectManager, materialReader, manager, modLoadedChecker);
		}

		BO4BlockFunction[] blocksOTGPlus = new BO4BlockFunction[this.blocksMaterial.length];

		BO4BlockFunction block;
		int blockIndex = 0;
		for(int x = 0; x < X_SIZE; x++)
		{
			for(int z = 0; z < Z_SIZE; z++)
			{
				if(this.blocks[x][z] != null)
				{
					for(int i = 0; i < this.blocks[x][z].length; i++)
					{
						if(this.randomBlocksBlocks[blockIndex] != null)
						{
							block = new BO4RandomBlockFunction(config);
							((BO4RandomBlockFunction)block).blocks = this.randomBlocksBlocks[blockIndex];
							((BO4RandomBlockFunction)block).blockChances = this.randomBlocksBlockChances[blockIndex];
							((BO4RandomBlockFunction)block).metaDataNames = this.randomBlocksMetaDataNames[blockIndex];
							((BO4RandomBlockFunction)block).metaDataTags = this.randomBlocksMetaDataTags[blockIndex];
							((BO4RandomBlockFunction)block).blockCount = this.randomBlocksBlockCount[blockIndex];
						} else {
							block = new BO4BlockFunction(config);
						}

						block.x = x;
						block.y = this.blocks[x][z][i];
						block.z = z;
						block.material = this.blocksMaterial[blockIndex];
						block.nbtName = this.blocksMetaDataName[blockIndex];
						block.nbt = this.blocksMetaDataTag[blockIndex];

						blocksOTGPlus[blockIndex] = block;
						blockIndex++;
					}
				}
			}
		}

		return blocksOTGPlus;
	}

	BO4BlockFunction[][] getSmoothingHeightMap(BO4Config config, BO4 start, boolean fromFile, String presetFolderName, Path otgRootFolder, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		// TODO: Caching the heightmap will mean this BO4 can only be used with 1 master BO4,
		// it won't pick up smoothing area settings if it is also used in another structure.
		if(this.heightMap == null)
		{
			if(config.isBO4Data && fromFile)
			{
				BO4Config bo4Config = config.createBlankCopy();
				try
				{
					bo4Config.readFromBO4DataFile(true, materialReader);
				}
				catch (InvalidConfigException e)
				{
					OTGLog.error(LogCategory.CUSTOM_OBJECTS, "Error fetching smoothing heightmap for BO4Data {}: {}", start.getName(), e.getMessage());
					this.heightMap = new BO4BlockFunction[16][16];
					return this.heightMap;
				}
				this.heightMap = bo4Config.blockStorage.getSmoothingHeightMap(bo4Config, start, false, presetFolderName, otgRootFolder, customObjectManager, materialReader, manager, modLoadedChecker);
				return this.heightMap;
			}

			this.heightMap = new BO4BlockFunction[16][16];

			// make heightmap containing the highest or lowest blocks in this chunk
			int blockIndex = 0;
			LocalMaterialData material;
			boolean isSmoothAreaAnchor;
			boolean isRandomBlock;
			int y;
			for(int x = 0; x < X_SIZE; x++)
			{
				for(int z = 0; z < Z_SIZE; z++)
				{
					if(blocks[x][z] != null)
					{
						for(int i = 0; i < blocks[x][z].length; i++)
						{
							isSmoothAreaAnchor = false;
							isRandomBlock = this.randomBlocksBlocks[blockIndex] != null;
							y = blocks[x][z][i];

							if(isRandomBlock)
							{
								for(LocalMaterialData randomMaterial : this.randomBlocksBlocks[blockIndex])
								{
									// TODO: Material should never be null, fix the code in RandomBlockFunction.load() that causes this.
									if(randomMaterial == null)
									{
										continue;
									}
									if(randomMaterial.isSmoothAreaAnchor(resolveSmoothing(config, start.getConfig(), start.getConfig().smoothStartWood, config.smoothStartWood), start.getConfig().spawnUnderWater))
									{
										isSmoothAreaAnchor = true;
										break;
									}
								}
							}

							material = this.blocksMaterial[blockIndex];
							if(
								isSmoothAreaAnchor ||
								(
									!isRandomBlock &&
									material.isSmoothAreaAnchor(resolveSmoothing(config, start.getConfig(), start.getConfig().smoothStartWood, config.smoothStartWood), start.getConfig().spawnUnderWater)
								)
							)
							{
								if(
									(!resolveSmoothing(config, start.getConfig(), start.getConfig().smoothStartTop, config.smoothStartTop) && y == config.getminY()) ||
									(resolveSmoothing(config, start.getConfig(), start.getConfig().smoothStartTop, config.smoothStartTop) && (this.heightMap[x][z] == null || y > this.heightMap[x][z].y))
								)
								{
									BO4BlockFunction blockFunction;
									if(isRandomBlock)
									{
										blockFunction = new BO4RandomBlockFunction();
										((BO4RandomBlockFunction)blockFunction).blocks = this.randomBlocksBlocks[blockIndex];
										((BO4RandomBlockFunction)blockFunction).blockChances = this.randomBlocksBlockChances[blockIndex];
										((BO4RandomBlockFunction)blockFunction).metaDataNames = this.randomBlocksMetaDataNames[blockIndex];
										((BO4RandomBlockFunction)blockFunction).metaDataTags = this.randomBlocksMetaDataTags[blockIndex];
										((BO4RandomBlockFunction)blockFunction).blockCount = this.randomBlocksBlockCount[blockIndex];
									} else {
										blockFunction = new BO4BlockFunction();
									}
									blockFunction.material = material;
									blockFunction.x = x;
									blockFunction.y = (short) y;
									blockFunction.z = z;
									blockFunction.nbtName = this.blocksMetaDataName[blockIndex];
									blockFunction.nbt = this.blocksMetaDataTag[blockIndex];

									this.heightMap[x][z] = blockFunction;
								}
							}

							blockIndex++;
						}
					}
				}
			}
		}
		return this.heightMap;
	}

	private <T> T resolveSmoothing(BO4Config config, BO4Config startConfig, T startValue, T childValue)
	{
		return startConfig.overrideChildSettings && config.overrideChildSettings ? startValue : childValue;
	}
}
