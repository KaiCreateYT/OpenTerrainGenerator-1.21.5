package com.pg85.otg.customobject.bo4;

import com.pg85.otg.constants.settings.ConfigMode;
import com.pg85.otg.customobject.CustomObjectManager;
import com.pg85.otg.customobject.bo4.bo4function.BO4BlockFunction;
import com.pg85.otg.customobject.bo4.bo4function.BO4BranchFunction;
import com.pg85.otg.customobject.bo4.bo4function.BO4EntityFunction;
import com.pg85.otg.customobject.bo4.bo4function.BO4RandomBlockFunction;
import com.pg85.otg.customobject.bo4.bo4function.BO4WeightedBranchFunction;
import com.pg85.otg.customobject.bofunctions.BlockFunction;
import com.pg85.otg.customobject.config.CustomObjectResourcesManager;
import com.pg85.otg.customobject.util.BO3Enums.SpawnHeightEnum;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IModLoadedChecker;
import com.pg85.otg.util.OTGLog;
import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.util.helpers.StreamHelper;
import com.pg85.otg.util.helpers.StringHelper;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.materials.LocalMaterialData;

import java.io.DataOutput;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.zip.DataFormatException;

class BO4DataSerializer
{
	static final int BO4_DATA_VERSION = 3;

	static void writeToStream(BO4Config config, DataOutput stream, String presetFolderName, Path otgRootFolder, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker) throws IOException
	{
		stream.writeInt(BO4_DATA_VERSION);
		// Version 3 added fixedRotation
		StreamHelper.writeStringToStream(stream, config.fixedRotation == null ? null : config.fixedRotation.toString());
		stream.writeInt(config.minimumSizeTop);
		stream.writeInt(config.minimumSizeBottom);
		stream.writeInt(config.minimumSizeLeft);
		stream.writeInt(config.minimumSizeRight);
		stream.writeInt(config.minX);
		stream.writeInt(config.maxX);
		stream.writeInt(config.minY);
		stream.writeInt(config.maxY);
		stream.writeInt(config.minZ);
		stream.writeInt(config.maxZ);
		StreamHelper.writeStringToStream(stream, config.author);
		StreamHelper.writeStringToStream(stream, config.description);
		StreamHelper.writeStringToStream(stream, config.settingsMode.name());
		stream.writeInt(config.frequency);
		StreamHelper.writeStringToStream(stream, config.spawnHeight.name());
		stream.writeInt(config.minHeight);
		stream.writeInt(config.maxHeight);
		stream.writeShort(config.inheritedBO3s.size());
		for(String inheritedBO3 : config.inheritedBO3s) {
			StreamHelper.writeStringToStream(stream, inheritedBO3);
		}
		StreamHelper.writeStringToStream(stream, config.inheritBO3);
		StreamHelper.writeStringToStream(stream, config.inheritBO3Rotation.name());
		stream.writeBoolean(config.overrideChildSettings);
		stream.writeBoolean(config.overrideParentHeight);
		stream.writeBoolean(config.canOverride);
		stream.writeInt(config.branchFrequency);
		StreamHelper.writeStringToStream(stream, config.branchFrequencyGroup);
		stream.writeBoolean(config.mustBeBelowOther);
		stream.writeBoolean(config.mustBeInsideWorldBorders);
		StreamHelper.writeStringToStream(stream, config.mustBeInside);
		StreamHelper.writeStringToStream(stream, config.cannotBeInside);
		StreamHelper.writeStringToStream(stream, config.replacesBO3);
		stream.writeBoolean(config.canSpawnOnWater);
		stream.writeBoolean(config.spawnOnWaterOnly);
		stream.writeBoolean(config.spawnUnderWater);
		stream.writeBoolean(config.spawnAtWaterLevel);
		stream.writeBoolean(config.doReplaceBlocks);
		stream.writeInt(config.heightOffset);
		stream.writeBoolean(config.removeAir);
		StreamHelper.writeStringToStream(stream, config.replaceAbove);
		StreamHelper.writeStringToStream(stream, config.replaceBelow);
		stream.writeBoolean(config.replaceWithBiomeBlocks);
		StreamHelper.writeStringToStream(stream, config.replaceWithSurfaceBlock);
		StreamHelper.writeStringToStream(stream, config.replaceWithGroundBlock);
		StreamHelper.writeStringToStream(stream, config.replaceWithStoneBlock);
		stream.writeInt(config.smoothRadius);
		stream.writeInt(config.smoothHeightOffset);
		stream.writeBoolean(config.smoothStartTop);
		stream.writeBoolean(config.smoothStartWood);
		StreamHelper.writeStringToStream(stream, config.smoothingSurfaceBlock);
		StreamHelper.writeStringToStream(stream, config.smoothingGroundBlock);
		StreamHelper.writeStringToStream(stream, config.bo3Group);
		stream.writeBoolean(config.isSpawnPoint);
		stream.writeBoolean(config.blockStorage.isCollidable());
		stream.writeBoolean(config.useCenterForHighestBlock);

		BO4BranchFunction[] branches = config.blockStorage.getbranches();
		stream.writeInt(branches.length);
		for(BO4BranchFunction func : branches)
		{
			stream.writeBoolean(func instanceof BO4WeightedBranchFunction);
			func.writeToStream(stream);
		}

		BO4EntityFunction[] entities = config.blockStorage.getEntityData();
		stream.writeInt(entities.length);
		for(BO4EntityFunction func : entities)
		{
			func.writeToStream(stream);
		}

		stream.writeInt(0); // Used to be particledata length
		stream.writeInt(0); // Used to be spawnerdata length
		stream.writeInt(0); // Used to be moddata length

		ArrayList<LocalMaterialData> materials = new ArrayList<>();
		ArrayList<String> metaDataNames = new ArrayList<>();
		int randomBlockCount = 0;
		int nonRandomBlockCount = 0;
		BO4BlockFunction[] blocks = config.getBlocks(presetFolderName, otgRootFolder, customObjectManager, materialReader, manager, modLoadedChecker);
		for(BO4BlockFunction block : blocks)
		{
			if(block instanceof BO4RandomBlockFunction)
			{
				randomBlockCount++;
				for(LocalMaterialData material : ((BO4RandomBlockFunction)block).blocks)
				{
					if(!materials.contains(material))
					{
						materials.add(material);
					}
				}
			} else {
				nonRandomBlockCount++;
			}

			if(block.material != null && !materials.contains(block.material))
			{
				materials.add(block.material);
			}
			if(block.nbtName != null && !metaDataNames.contains(block.nbtName))
			{
				metaDataNames.add(block.nbtName);
			}
		}

		String[] metaDataNamesArr = metaDataNames.toArray(new String[0]);
		LocalMaterialData[] blocksArr = materials.toArray(new LocalMaterialData[0]);

		stream.writeShort(metaDataNamesArr.length);
		for (String s : metaDataNamesArr) {
			StreamHelper.writeStringToStream(stream, s);
		}

		stream.writeShort(blocksArr.length);
		for (LocalMaterialData localMaterialData : blocksArr) {
			StreamHelper.writeStringToStream(stream, localMaterialData.getName());
		}

		// TODO: This assumes that loading blocks in a different order won't matter, which may not be true?
		// Anything that spawns on top, entities/spawners etc, should be spawned last tho, so shouldn't be a problem?
		stream.writeInt(nonRandomBlockCount);
		int nonRandomBlockIndex = 0;
		ArrayList<BO4BlockFunction> blocksInColumn;
		if(nonRandomBlockCount > 0)
		{
			for(int x = config.getminX(); x < 16; x++)
			{
				for(int z = config.getminZ(); z < 16; z++)
				{
					blocksInColumn = new ArrayList<>();
					for(BO4BlockFunction blockFunction : blocks)
					{
						if(!(blockFunction instanceof BO4RandomBlockFunction))
						{
							if(blockFunction.x == x && blockFunction.z == z)
							{
								blocksInColumn.add(blockFunction);
							}
						}
					}
					stream.writeShort(blocksInColumn.size());
					if(!blocksInColumn.isEmpty())
					{
						for(BO4BlockFunction blockFunction : blocksInColumn)
						{
							blockFunction.writeToStream(metaDataNamesArr, blocksArr, stream);
							nonRandomBlockIndex++;
						}
					}
					if(nonRandomBlockIndex == nonRandomBlockCount)
					{
						break;
					}
				}
				if(nonRandomBlockIndex == nonRandomBlockCount)
				{
					break;
				}
			}
		}

		stream.writeInt(randomBlockCount);
		int randomBlockIndex = 0;
		if(randomBlockCount > 0)
		{
			for(int x = config.getminX(); x < 16; x++)
			{
				for(int z = config.getminZ(); z < 16; z++)
				{
					blocksInColumn = new ArrayList<>();
					for(BO4BlockFunction blockFunction : blocks)
					{
						if(blockFunction instanceof BO4RandomBlockFunction)
						{
							if(blockFunction.x == x && blockFunction.z == z)
							{
								blocksInColumn.add(blockFunction);
							}
						}
					}
					stream.writeShort(blocksInColumn.size());
					if(!blocksInColumn.isEmpty())
					{
						for(BO4BlockFunction blockFunction : blocksInColumn)
						{
							blockFunction.writeToStream(metaDataNamesArr, blocksArr, stream);
							randomBlockIndex++;
						}
					}
					if(randomBlockIndex == randomBlockCount)
					{
						break;
					}
				}
				if(randomBlockIndex == randomBlockCount)
				{
					break;
				}
			}
		}
	}

	static BO4Config readFromBO4DataFile(BO4Config config, boolean getBlocks, IMaterialReader materialReader) throws InvalidConfigException
	{
		try (FileInputStream fis = new FileInputStream(config.getFile());
			 FileChannel channel = fis.getChannel())
		{
			ByteBuffer bufferCompressed = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
			byte[] compressedBytes = new byte[(int) channel.size()];
			bufferCompressed.get(compressedBytes);

			ByteBuffer bufferDecompressed;
			try {
				byte[] decompressedBytes = com.pg85.otg.util.CompressionUtils.decompress(compressedBytes);
				bufferDecompressed = ByteBuffer.wrap(decompressedBytes);
			} catch (DataFormatException e) {
				throw new InvalidConfigException("Failed to decompress BO4 data for " + config.getName() + ": " + e.getMessage());
			}

			int bo4DataVersion = bufferDecompressed.getInt();
			if(bo4DataVersion < 2)
			{
				throw new InvalidConfigException("Could not read BO4Data file " + config.getName() + ", it is outdated. Delete and re-export BO4Data files to fix this, or delete and reinstall your OTG preset.");
			}
			// Version 3 added fixedRotation
			if(bo4DataVersion > 2)
			{
				String rotationString = StreamHelper.readStringFromBuffer(bufferDecompressed);
				config.fixedRotation = Rotation.getRotation(rotationString);
			}

			config.isBO4Data = true;
			config.inheritedBO3Loaded = true;
			config.minimumSizeTop = bufferDecompressed.getInt();
			config.minimumSizeBottom = bufferDecompressed.getInt();
			config.minimumSizeLeft = bufferDecompressed.getInt();
			config.minimumSizeRight = bufferDecompressed.getInt();

			config.minX = bufferDecompressed.getInt();
			config.maxX = bufferDecompressed.getInt();
			config.minY = bufferDecompressed.getInt();
			config.maxY = bufferDecompressed.getInt();
			config.minZ = bufferDecompressed.getInt();
			config.maxZ = bufferDecompressed.getInt();

			config.author = StreamHelper.readStringFromBuffer(bufferDecompressed);
			config.description = StreamHelper.readStringFromBuffer(bufferDecompressed);
			config.settingsMode = ConfigMode.valueOf(StreamHelper.readStringFromBuffer(bufferDecompressed));
			config.frequency = bufferDecompressed.getInt();
			config.spawnHeight = SpawnHeightEnum.valueOf(StreamHelper.readStringFromBuffer(bufferDecompressed));
			config.minHeight = bufferDecompressed.getInt();
			config.maxHeight = bufferDecompressed.getInt();
			short inheritedBO3sSize = bufferDecompressed.getShort();
			config.inheritedBO3s = new ArrayList<>();
			for(int i = 0; i < inheritedBO3sSize; i++)
			{
				config.inheritedBO3s.add(StreamHelper.readStringFromBuffer(bufferDecompressed));
			}

			config.inheritBO3 = StreamHelper.readStringFromBuffer(bufferDecompressed);
			config.inheritBO3Rotation = Rotation.valueOf(StreamHelper.readStringFromBuffer(bufferDecompressed));
			config.overrideChildSettings = bufferDecompressed.get() != 0;
			config.overrideParentHeight = bufferDecompressed.get() != 0;
			config.canOverride = bufferDecompressed.get() != 0;
			config.branchFrequency = bufferDecompressed.getInt();
			config.branchFrequencyGroup = StreamHelper.readStringFromBuffer(bufferDecompressed);
			config.mustBeBelowOther = bufferDecompressed.get() != 0;
			config.mustBeInsideWorldBorders = bufferDecompressed.get() != 0;
			config.mustBeInside = StreamHelper.readStringFromBuffer(bufferDecompressed);
			config.cannotBeInside = StreamHelper.readStringFromBuffer(bufferDecompressed);
			config.replacesBO3 = StreamHelper.readStringFromBuffer(bufferDecompressed);
			config.canSpawnOnWater = bufferDecompressed.get() != 0;
			config.spawnOnWaterOnly = bufferDecompressed.get() != 0;
			config.spawnUnderWater = bufferDecompressed.get() != 0;
			config.spawnAtWaterLevel = bufferDecompressed.get() != 0;
			config.doReplaceBlocks = bufferDecompressed.get() != 0;
			config.heightOffset = bufferDecompressed.getInt();
			config.removeAir = bufferDecompressed.get() != 0;
			config.replaceAbove = StreamHelper.readStringFromBuffer(bufferDecompressed);
			config.replaceBelow = StreamHelper.readStringFromBuffer(bufferDecompressed);
			config.replaceWithBiomeBlocks = bufferDecompressed.get() != 0;
			config.replaceWithSurfaceBlock = StreamHelper.readStringFromBuffer(bufferDecompressed);
			config.replaceWithGroundBlock = StreamHelper.readStringFromBuffer(bufferDecompressed);
			config.replaceWithStoneBlock = StreamHelper.readStringFromBuffer(bufferDecompressed);
			config.smoothRadius = bufferDecompressed.getInt();
			config.smoothHeightOffset = bufferDecompressed.getInt();
			config.smoothStartTop = bufferDecompressed.get() != 0;
			config.smoothStartWood = bufferDecompressed.get() != 0;
			config.smoothingSurfaceBlock = StreamHelper.readStringFromBuffer(bufferDecompressed);
			config.smoothingGroundBlock = StreamHelper.readStringFromBuffer(bufferDecompressed);
			config.bo3Group = StreamHelper.readStringFromBuffer(bufferDecompressed);
			config.isSpawnPoint = bufferDecompressed.get() != 0;
			config.blockStorage.setCollidable(bufferDecompressed.get() != 0);
			config.useCenterForHighestBlock = bufferDecompressed.get() != 0;

			config.branchFrequencyGroups = StringHelper.parseGroupMap(config.branchFrequencyGroup);
			config.bo4Groups = StringHelper.parseGroupMap(config.bo3Group);
			config.mustBeInsideBranches = StringHelper.splitTrimmedList(config.mustBeInside);
			config.cannotBeInsideBranches = StringHelper.splitTrimmedList(config.cannotBeInside);
			config.replacesBO3Branches = StringHelper.splitTrimmedList(config.replacesBO3);

			int branchesOTGPlusLength = bufferDecompressed.getInt();
			BO4BranchFunction[] branchesArray = new BO4BranchFunction[branchesOTGPlusLength];
			for(int i = 0; i < branchesOTGPlusLength; i++)
			{
				boolean branchType = bufferDecompressed.get() != 0;
				if(branchType)
				{
					branchesArray[i] = BO4WeightedBranchFunction.fromStream(config, bufferDecompressed, materialReader);
				} else {
					branchesArray[i] = BO4BranchFunction.fromStream(config, bufferDecompressed, materialReader);
				}
			}
			config.blockStorage.setBranchesArray(branchesArray);

			int entityDataOTGPlusLength = bufferDecompressed.getInt();
			BO4EntityFunction[] entitiesArray = new BO4EntityFunction[entityDataOTGPlusLength];
			for(int i = 0; i < entityDataOTGPlusLength; i++)
			{
				entitiesArray[i] = BO4EntityFunction.fromStream(config, bufferDecompressed);
			}
			config.blockStorage.setEntityData(entitiesArray);

			// Legacy settings, hoping they were always 0 and noone actually used them :/.
			bufferDecompressed.getInt(); // Used to be particles
			bufferDecompressed.getInt(); // Used to be spawners
			bufferDecompressed.getInt(); // Used to be moddata

			// Reconstruct blocks
			if(getBlocks)
			{
				short metaDataNamesArrLength = bufferDecompressed.getShort();
				String[] metaDataNamesArr = new String[metaDataNamesArrLength];
				for(int i = 0; i < metaDataNamesArrLength; i++)
				{
					metaDataNamesArr[i] = StreamHelper.readStringFromBuffer(bufferDecompressed);
				}

				short blocksArrArrLength = bufferDecompressed.getShort();
				LocalMaterialData[] blocksArr = new LocalMaterialData[blocksArrArrLength];
				for(int i = 0; i < blocksArrArrLength; i++)
				{
					String materialName = StreamHelper.readStringFromBuffer(bufferDecompressed);
					try {
						blocksArr[i] = materialReader.readMaterial(materialName);
					} catch (InvalidConfigException e) {
						OTGLog.error(LogCategory.CUSTOM_OBJECTS, "Could not read material \"{}\" for BO4 \"{}\"", materialName, config.getName(), e);
					}
				}

				short[][] columnSizes = new short[16][16];

				// TODO: This assumes that loading blocks in a different order won't matter, which may not be true?
				// Anything that spawns on top, entities/spawners etc, should be spawned last tho, so shouldn't be a problem?
				int nonRandomBlockCount = bufferDecompressed.getInt();
				int nonRandomBlockIndex = 0;
				ArrayList<BO4BlockFunction> nonRandomBlocks = new ArrayList<>();
				if(nonRandomBlockCount > 0)
				{
					for(int x = config.getminX(); x < 16; x++)
					{
						for(int z = config.getminZ(); z < 16; z++)
						{
							short blocksInColumnSize = bufferDecompressed.getShort();
							for(int j = 0; j < blocksInColumnSize; j++)
							{
								columnSizes[x][z]++;
								nonRandomBlocks.add(BO4BlockFunction.fromStream(x, z, metaDataNamesArr, blocksArr, config, bufferDecompressed));
								nonRandomBlockIndex++;
								if(nonRandomBlockCount == nonRandomBlockIndex)
								{
									break;
								}
							}
							if(nonRandomBlockCount == nonRandomBlockIndex)
							{
								break;
							}
						}
						if(nonRandomBlockCount == nonRandomBlockIndex)
						{
							break;
						}
					}
				}

				int randomBlockCount = bufferDecompressed.getInt();
				int randomBlockIndex = 0;
				ArrayList<BO4RandomBlockFunction> randomBlocks = new ArrayList<>();
				if(randomBlockCount > 0)
				{
					for(int x = config.getminX(); x < 16; x++)
					{
						for(int z = config.getminZ(); z < 16; z++)
						{
							short blocksInColumnSize = bufferDecompressed.getShort();
							for(int j = 0; j < blocksInColumnSize; j++)
							{
								columnSizes[x][z]++;
								randomBlocks.add(BO4RandomBlockFunction.fromStream(x, z, metaDataNamesArr, blocksArr, config, bufferDecompressed));
								randomBlockIndex++;
								if(randomBlockCount == randomBlockIndex)
								{
									break;
								}
							}
							if(randomBlockCount == randomBlockIndex)
							{
								break;
							}
						}
						if(randomBlockCount == randomBlockIndex)
						{
							break;
						}
					}
				}

				ArrayList<BlockFunction<?>> newBlocks = new ArrayList<>();
				newBlocks.addAll(nonRandomBlocks);
				newBlocks.addAll(randomBlocks);
				config.blockStorage.loadBlockArrays(newBlocks, columnSizes);
			}
		}
		catch (FileNotFoundException e)
		{
			OTGLog.error(LogCategory.CUSTOM_OBJECTS, "BO4 data file not found for {}: {}", config.getName(), e.getMessage());
			return null;
		}
		catch (InvalidConfigException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			OTGLog.error(LogCategory.CUSTOM_OBJECTS, "Exception reading BO4Data file", e);
			throw new InvalidConfigException("Could not read BO4Data file " + config.getName() + ", it may be outdated or corrupted. Delete and re-export BO4Data files to fix this, or delete and reinstall your OTG preset.");
		}

		return config;
	}
}
