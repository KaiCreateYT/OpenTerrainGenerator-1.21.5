package com.pg85.otg.customobject.bo4;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.constants.settings.ConfigMode;
import com.pg85.otg.customobject.CustomObject;
import com.pg85.otg.customobject.CustomObjectManager;
import com.pg85.otg.customobject.bo3.BO3Config;
import com.pg85.otg.customobject.bo4.bo4function.BO4BlockFunction;
import com.pg85.otg.customobject.bo4.bo4function.BO4BranchFunction;
import com.pg85.otg.customobject.bo4.bo4function.BO4EntityFunction;
import com.pg85.otg.customobject.bo4.bo4function.BO4RandomBlockFunction;
import com.pg85.otg.customobject.bo4.bo4function.BO4WeightedBranchFunction;
import com.pg85.otg.customobject.bofunctions.BlockFunction;
import com.pg85.otg.customobject.bofunctions.BranchFunction;
import com.pg85.otg.customobject.config.CustomObjectConfigFile;
import com.pg85.otg.customobject.config.CustomObjectConfigFunction;
import com.pg85.otg.customobject.config.CustomObjectErroredFunction;
import com.pg85.otg.customobject.config.CustomObjectResourcesManager;
import com.pg85.otg.customobject.config.io.SettingsReaderBO4;
import com.pg85.otg.customobject.config.io.SettingsWriterBO4;
import com.pg85.otg.customobject.structures.bo4.BO4CustomStructureCoordinate;
import com.pg85.otg.customobject.util.BO3Enums.SpawnHeightEnum;
import com.pg85.otg.customobject.util.BoundingBox;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.interfaces.ICustomObjectManager;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IModLoadedChecker;
import com.pg85.otg.util.OTGLog;
import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.util.helpers.StringHelper;
import com.pg85.otg.util.logging.LogCategory;

import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class BO4Config extends CustomObjectConfigFile
{
	public String author;
	public String description;
	public boolean doReplaceBlocks;
	public int frequency;
	public Rotation fixedRotation;

	private final int xSize = 16;
	private final int zSize = 16;
	public int minHeight;
	public int maxHeight;

	public SpawnHeightEnum spawnHeight;
	public boolean useCenterForHighestBlock;
	public final BoundingBox[] boundingBoxes = new BoundingBox[4];

	boolean inheritedBO3Loaded;

	// These are used in CustomObjectStructure when determining the minimum area in chunks that
	// this branching structure needs to be able to spawn
	public int minimumSizeTop = -1;
	public int minimumSizeBottom = -1;
	public int minimumSizeLeft = -1;
	public int minimumSizeRight = -1;

	public int timesSpawned = 0;

	public int branchFrequency;
	// Define groups that this BO3 belongs to with a range in chunks that members of each group should have to each other
	String branchFrequencyGroup;
	public HashMap<String, Integer> branchFrequencyGroups;

	int minX;
	int maxX;
	int minY;
	int maxY;
	int minZ;
	int maxZ;

	ArrayList<String> inheritedBO3s;

	// Adjusts the height by this number before spawning. Handy when using "highestblock" for lowering BO3s that have a lot of ground under them included
	public int heightOffset;
	Rotation inheritBO3Rotation;
	// If this is set to true then any air blocks in the bo3 will not be spawned
	boolean removeAir;
	boolean configRemoveAir;
	// Defaults to false. Set to true if this BO3 should spawn at the player spawn point. When the server starts one of the structures that has IsSpawnPoint set to true is selected randomly and is spawned, the others never get spawned.)
	public boolean isSpawnPoint;

	// Replaces all the non-air blocks that are above this BO3 or its smoothing area with the given block material (should be WATER or AIR or NONE), also applies to smoothing areas although it intentionally leaves some of the terrain above them intact. WATER can be used in combination with SpawnUnderWater to fill any air blocks underneath waterlevel with water (and any above waterlevel with air).
	public String replaceAbove;
	public String configReplaceAbove;
	// Replaces all non-air blocks underneath the BO3 (but not its smoothing area) with the designated material until a solid block is found.
	public String replaceBelow;
	public String configReplaceBelow;
	// Defaults to true. If set to true then every block in the BO3 of the materials defined in ReplaceWithGroundBlock or ReplaceWithSurfaceBlock will be replaced by the GroundBlock or SurfaceBlock materials configured for the biome the block is spawned in.
	public boolean replaceWithBiomeBlocks;
	// Replaces all the blocks of the given material in the BO3 with the GroundBlock configured for the biome it spawns in
	public String replaceWithGroundBlock;
	// Replaces all the blocks of the given material in the BO3 with the SurfaceBlock configured for the biome it spawns in
	public String replaceWithSurfaceBlock;
	// Replaces all the blocks of the given material in the BO3 with the StoneBlock configured for the biome it spawns in
	public String replaceWithStoneBlock;
	// Define a group that this BO3 belongs to and a range in chunks that members of this group should have to each other
	String bo3Group;
	public HashMap<String, Integer> bo4Groups;
	// If this is set to true then this BO3 can spawn on top of or inside other BO3's
	public boolean canOverride;

	// Copies the blocks and branches of an existing BO3 into this one
	String inheritBO3;
	// Should the smoothing area go to the top or the bottom blocks in the bo3?
	public boolean smoothStartTop;
	public boolean smoothStartWood;
	// The size of the smoothing area
	public int smoothRadius;
	// The materials used for the smoothing area
	public String smoothingSurfaceBlock;
	public String smoothingGroundBlock;
	// If true then root BO3 smoothing and height settings are used for all children
	public boolean overrideChildSettings;
	public boolean overrideParentHeight;

	// Used to make sure that dungeons can only spawn underneath other structures
	public boolean mustBeBelowOther;

	// Used to make sure that dungeons can only spawn inside worldborders
	public boolean mustBeInsideWorldBorders;

	String replacesBO3;
	public ArrayList<String> replacesBO3Branches;
	String mustBeInside;
	public ArrayList<String> mustBeInsideBranches;
	String cannotBeInside;
	public ArrayList<String> cannotBeInsideBranches;

	public int smoothHeightOffset;
	public boolean canSpawnOnWater;
	public boolean spawnOnWaterOnly;
	public boolean spawnUnderWater;
	public boolean spawnAtWaterLevel;

	private String presetFolderName;

	final BO4BlockStorage blockStorage = new BO4BlockStorage();

	boolean isBO4Data = false;

	/**
	 * Creates a BO4Config from a file.
	 *
	 * @param reader		The settings of the BO4.
	 */
	public BO4Config(SettingsReaderBO4 reader, boolean init, String presetFolderName, Path otgRootFolder,  CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker) throws InvalidConfigException
	{
		super(reader);
		if(init)
		{
			init(presetFolderName, otgRootFolder,  customObjectManager, materialReader, manager, modLoadedChecker);
		}
	}

	private BO4Config(SettingsReaderBO4 reader)
	{
		super(reader);
	}

	BO4Config createBlankCopy()
	{
		return new BO4Config((SettingsReaderBO4) this.reader);
	}

	private void init(String presetFolderName, Path otgRootFolder,  CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker) throws InvalidConfigException
	{
		this.minX = Integer.MAX_VALUE;
		this.maxX = Integer.MIN_VALUE;
		this.minY = Integer.MAX_VALUE;
		this.maxY = Integer.MIN_VALUE;
		this.minZ = Integer.MAX_VALUE;
		this.maxZ = Integer.MIN_VALUE;
		if(!this.reader.getFile().getAbsolutePath().toLowerCase().endsWith(".bo4data"))
		{
			readConfigSettings(presetFolderName, otgRootFolder,  customObjectManager, materialReader, manager, modLoadedChecker);
		} else {
			this.readFromBO4DataFile(false,  materialReader);
		}

		// When writing, we'll need to read some raw data from the file,
		// so can't flush the cache yet. Flush after writing.
		if(this.settingsMode == ConfigMode.WriteDisable)
		{
			this.reader.flushCache();
		}
	}

	public int getXOffset()
	{
		return minX < -8 ? -minX : maxX > 7 ? -minX : 8;
	}

	public int getZOffset()
	{
		return minZ < -7 ? -minZ : maxZ > 8 ? -minZ : 7;
	}

	public int getminX()
	{
		return minX + this.getXOffset(); // + xOffset makes sure that the value returned is never negative which is necessary for the collision detection code for CustomStructures in OTG (it assumes the furthest top and left blocks are at => 0 x or >= 0 z in the BO3)
	}

	public int getmaxX()
	{
		return maxX + this.getXOffset(); // + xOffset makes sure that the value returned is never negative which is necessary for the collision detection code for CustomStructures in OTG (it assumes the furthest top and left blocks are at => 0 x or >= 0 z in the BO3)
	}

	public int getminY()
	{
		return minY;
	}

	public int getmaxY()
	{
		return maxY;
	}

	public int getminZ()
	{
		return minZ + this.getZOffset(); // + zOffset makes sure that the value returned is never negative which is necessary for the collision detection code for CustomStructures in OTG (it assumes the furthest top and left blocks are at => 0 x or >= 0 z in the BO3)
	}

	public int getmaxZ()
	{
		return maxZ + this.getZOffset(); // + zOffset makes sure that the value returned is never negative which is necessary for the collision detection code for CustomStructures in OTG (it assumes the furthest top and left blocks are at => 0 x or >= 0 z in the BO3)
	}

	public ArrayList<String> getInheritedBO3s()
	{
		return this.inheritedBO3s;
	}

	public BO4BlockFunction[][] getSmoothingHeightMap(BO4 start, String presetFolderName, Path otgRootFolder,  CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		return blockStorage.getSmoothingHeightMap(this, start, true, presetFolderName, otgRootFolder, customObjectManager, materialReader, manager, modLoadedChecker);
	}

	BO4BlockFunction[] getBlocks(String presetFolderName, Path otgRootFolder,  CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		return blockStorage.getBlocks(this, true, presetFolderName, otgRootFolder, customObjectManager, materialReader, manager, modLoadedChecker);
	}

	public BO4BranchFunction[] getbranches()
	{
		return blockStorage.getbranches();
	}

	public BO4EntityFunction[] getEntityData()
	{
		return blockStorage.getEntityData();
	}

	public boolean isCollidable()
	{
		return blockStorage.isCollidable();
	}

	public void setBlocks(List<BlockFunction<?>> newBlocks)
	{
		blockStorage.setBlocks(newBlocks);
	}

	public void setBranches(List<BranchFunction<?>> branches)
	{
		blockStorage.setBranches(branches);
	}

	void loadInheritedBO3(String presetFolderName, Path otgRootFolder,  ICustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		if(this.inheritBO3 != null && !this.inheritBO3.trim().isEmpty() && !this.inheritedBO3Loaded)
		{
			File currentFile = this.getFile().getParentFile();
			this.presetFolderName = currentFile.getName();
			while(currentFile.getParentFile() != null && !currentFile.getName().equals(Constants.DIMENSION_PRESETS_FOLDER))
			{
				this.presetFolderName = currentFile.getName();
				currentFile = currentFile.getParentFile();
				if(this.presetFolderName.equals(Constants.GLOBAL_OBJECTS_FOLDER))
				{
					this.presetFolderName = null;
					break;
				}
			}

			// TODO: Re-wire this so we don't have to cast CustomObjectManager :(
			CustomObjectManager customObjectManager2 = (CustomObjectManager)customObjectManager;
			CustomObject parentBO3 = customObjectManager2.getGlobalObjects().getObjectByName(this.inheritBO3, this.presetFolderName, otgRootFolder,  customObjectManager2, materialReader, manager, modLoadedChecker);
			if(parentBO3 != null)
			{
				BO4BlockFunction[] blocks = getBlocks(this.presetFolderName, otgRootFolder,  customObjectManager2, materialReader, manager, modLoadedChecker);

				this.inheritedBO3Loaded = true;

				this.inheritedBO3s.addAll(((BO4)parentBO3).getConfig().getInheritedBO3s());

				this.removeAir = ((BO4)parentBO3).getConfig().removeAir;
				this.replaceAbove = this.replaceAbove == null || this.replaceAbove.isEmpty() ? ((BO4)parentBO3).getConfig().replaceAbove : this.replaceAbove;
				this.replaceBelow = this.replaceBelow == null || this.replaceBelow.isEmpty() ? ((BO4)parentBO3).getConfig().replaceBelow : this.replaceBelow;

				BO4CustomStructureCoordinate rotatedParentMaxCoords = BO4CustomStructureCoordinate.getRotatedBO3Coords(((BO4)parentBO3).getConfig().maxX, ((BO4)parentBO3).getConfig().maxY, ((BO4)parentBO3).getConfig().maxZ, this.inheritBO3Rotation);
				BO4CustomStructureCoordinate rotatedParentMinCoords = BO4CustomStructureCoordinate.getRotatedBO3Coords(((BO4)parentBO3).getConfig().minX, ((BO4)parentBO3).getConfig().minY, ((BO4)parentBO3).getConfig().minZ, this.inheritBO3Rotation);

				int parentMaxX = Math.max(rotatedParentMaxCoords.getX(), rotatedParentMinCoords.getX());
				int parentMinX = Math.min(rotatedParentMaxCoords.getX(), rotatedParentMinCoords.getX());

				int parentMaxY = rotatedParentMaxCoords.getY() > rotatedParentMinCoords.getY() ? rotatedParentMaxCoords.getY() : rotatedParentMinCoords.getY();
				int parentMinY = rotatedParentMaxCoords.getY() < rotatedParentMinCoords.getY() ? rotatedParentMaxCoords.getY() : rotatedParentMinCoords.getY();

				int parentMaxZ = Math.max(rotatedParentMaxCoords.getZ(), rotatedParentMinCoords.getZ());
				int parentMinZ = Math.min(rotatedParentMaxCoords.getZ(), rotatedParentMinCoords.getZ());

				if(parentMaxX > this.maxX)
				{
					this.maxX = parentMaxX;
				}
				if(parentMinX < this.minX)
				{
					this.minX = parentMinX;
				}
				if(parentMaxY > this.maxY)
				{
					this.maxY = parentMaxY;
				}
				if(parentMinY < this.minY)
				{
					this.minY = parentMinY;
				}
				if(parentMaxZ > this.maxZ)
				{
					this.maxZ = parentMaxZ;
				}
				if(parentMinZ < this.minZ)
				{
					this.minZ = parentMinZ;
				}

				BO4BlockFunction[] parentBlocks = ((BO4)parentBO3).getConfig().getBlocks(presetFolderName, otgRootFolder,  customObjectManager2, materialReader, manager, modLoadedChecker);
				ArrayList<BlockFunction<?>> newBlocks = new ArrayList<>();
				newBlocks.addAll(new ArrayList<>(Arrays.asList(parentBlocks)));
				newBlocks.addAll(new ArrayList<>(Arrays.asList(blocks)));

				short[][] columnSizes = new short[16][16];
				for(BlockFunction<?> block : newBlocks)
				{
					columnSizes[block.x][block.z]++;
				}

				blockStorage.loadBlockArrays(newBlocks, columnSizes);
				blockStorage.setCollidable(!newBlocks.isEmpty());

				ArrayList<BO4BranchFunction> newBranches = new ArrayList<>();
				if(blockStorage.getbranches() != null)
				{
                    newBranches.addAll(Arrays.asList(blockStorage.getbranches()));
				}
				for(BO4BranchFunction branch : ((BO4)parentBO3).getConfig().blockStorage.getbranches())
				{
					newBranches.add(branch.rotate(this.inheritBO3Rotation, presetFolderName, otgRootFolder,  customObjectManager2, materialReader, manager, modLoadedChecker));
				}
				blockStorage.setBranchesArray(newBranches.toArray(new BO4BranchFunction[0]));

				ArrayList<BO4EntityFunction> newEntityData = new ArrayList<>();
				if(blockStorage.getEntityData() != null)
				{
                    newEntityData.addAll(Arrays.asList(blockStorage.getEntityData()));
				}
				for(BO4EntityFunction entityData : ((BO4)parentBO3).getConfig().blockStorage.getEntityData())
				{
					newEntityData.add(entityData.rotate(this.inheritBO3Rotation));
				}
				blockStorage.setEntityData(newEntityData.toArray(new BO4EntityFunction[0]));

				this.inheritedBO3s.addAll(((BO4)parentBO3).getConfig().getInheritedBO3s());
			}
			if(!this.inheritedBO3Loaded)
			{
				OTGLog.error(LogCategory.CUSTOM_OBJECTS, "could not load BO4 parent for InheritBO3: {} in BO4 {}", this.inheritBO3, this.getName());
			}
		}
	}

	private void readResources( IMaterialReader materialReader, CustomObjectResourcesManager manager) throws InvalidConfigException
	{
		List<BO4BlockFunction> tempBlocksList = new ArrayList<>();
		List<BO4BranchFunction> tempBranchesList = new ArrayList<>();
		List<BO4EntityFunction> tempEntitiesList = new ArrayList<>();

		short[][] columnSizes = new short[xSize][zSize];

		ArrayList<CustomObjectConfigFunction<BO4Config>> resources = new ArrayList<>();
		int minX = 0;
		int maxX = 0;
		int minZ = 0;
		int maxZ = 0;
		for (CustomObjectConfigFunction<BO4Config> res : reader.getConfigFunctions(this, true,  materialReader, manager))
		{
			if (res.isValid())
			{
				resources.add(res);
				if( // TODO: Add interface instead?
					!(res instanceof BranchFunction) &&
					!(res instanceof CustomObjectErroredFunction)
				)
				{
					if(res.x < minX)
					{
						minX = res.x;
					}
					if(res.x > maxX)
					{
						maxX = res.x;
					}
					if(res.z < minZ)
					{
						minZ = res.z;
					}
					if(res.z > maxZ)
					{
						maxZ = res.z;
					}
				}
			}
		}

		int xSize = Math.abs(minX - maxX);
		int zSize = Math.abs(minZ - maxZ);
		if(xSize > 15 || zSize > 15)
		{
			OTGLog.error(LogCategory.CUSTOM_OBJECTS, "BO4 {} was too large ({}x{}), BO4's can be max 16x16 blocks.", this.getName(), xSize, zSize);
			throw new InvalidConfigException("BO4 " + this.getName() + " was too large, BO4's can be max 16x16 blocks.");
		}

		int xOffset = 0;
		int zOffset = 0;

		if(minX < -8)
		{
			xOffset = -minX - 8;
		}
		if(maxX > 7)
		{
			xOffset = -(maxX - 7);
		}
		if(minZ < -7)
		{
			zOffset = -minZ - 7;
		}
		if(maxZ > 8)
		{
			zOffset = -(maxZ - 8);
		}

		boolean hasBlocks = false;
		for (CustomObjectConfigFunction<BO4Config> res : resources)
		{
			if( // TODO: Add interface instead?
				!(res instanceof BranchFunction) &&
				!(res instanceof CustomObjectErroredFunction)
				)
			{
				res.x += xOffset;
				res.z += zOffset;
			}

			if (res instanceof BO4BlockFunction)
			{
				hasBlocks = true;

				if(res instanceof BO4RandomBlockFunction)
				{
					tempBlocksList.add((BO4RandomBlockFunction)res);
					columnSizes[res.x + (this.xSize / 2)][res.z + (this.zSize / 2) - 1]++;
				} else {
					if(!this.removeAir || !((BO4BlockFunction)res).material.isAir())
					{
						tempBlocksList.add((BO4BlockFunction)res);
						columnSizes[res.x + (this.xSize / 2)][res.z + (this.zSize / 2) - 1]++;
					}
				}

				// Get the real size of this BO3
				if(res.x < this.minX)
				{
					this.minX = res.x;
				}
				if(res.x > this.maxX)
				{
					this.maxX = res.x;
				}
				if(((BO4BlockFunction)res).y < this.minY)
				{
					this.minY = ((BO4BlockFunction)res).y;
				}
				if(((BO4BlockFunction)res).y > this.maxY)
				{
					this.maxY = ((BO4BlockFunction)res).y;
				}
				if(res.z < this.minZ)
				{
					this.minZ = res.z;
				}
				if(res.z > this.maxZ)
				{
					this.maxZ = res.z;
				}
			} else {
                switch (res) {
                    case BO4WeightedBranchFunction bo4WeightedBranchFunction -> tempBranchesList.add(bo4WeightedBranchFunction);
                    case BO4BranchFunction bo4BranchFunction -> tempBranchesList.add(bo4BranchFunction);
                    case BO4EntityFunction bo4EntityFunction -> tempEntitiesList.add(bo4EntityFunction);
                    default -> {
                    }
                }
			}
		}

		if(this.minX == Integer.MAX_VALUE)
		{
			this.minX = -8;
		}
		if(this.maxX == Integer.MIN_VALUE)
		{
			this.maxX = -8;
		}
		if(this.minY == Integer.MAX_VALUE)
		{
			this.minY = 0;
		}
		if(this.maxY == Integer.MIN_VALUE)
		{
			this.maxY = 0;
		}
		if(this.minZ == Integer.MAX_VALUE)
		{
			this.minZ = -7;
		}
		if(this.maxZ == Integer.MIN_VALUE)
		{
			this.maxZ = -7;
		}

		// TODO: OTG+ Doesn't do CustomObject BO3's, only check for 16x16, not 32x32?
		boolean illegalBlock = false;
		for(BO4BlockFunction block1 : tempBlocksList)
		{
			block1.x += this.getXOffset();
			block1.z += this.getZOffset();

			if(block1.x > 15 || block1.z > 15)
			{
				illegalBlock = true;
			}

			if(block1.x < 0 || block1.z < 0)
			{
				illegalBlock = true;
			}
		}

		blockStorage.loadBlockArrays(new ArrayList<>(tempBlocksList), columnSizes);
		blockStorage.setCollidable(hasBlocks);

		boolean illegalEntityData = false;
		for(BO4EntityFunction entityData : tempEntitiesList)
		{
			entityData.x += this.getXOffset();
			entityData.z += this.getZOffset();

			if(entityData.x > 15 || entityData.z > 15)
			{
				illegalEntityData = true;
			}

			if(entityData.x < 0 || entityData.z < 0)
			{
				illegalEntityData = true;
			}
		}
		blockStorage.setEntityData(tempEntitiesList.toArray(new BO4EntityFunction[0]));

		if(illegalBlock)
		{
			OTGLog.warn(LogCategory.CUSTOM_OBJECTS, "Warning: BO4 contains Blocks or RandomBlocks that are placed outside the chunk(s) that the BO3 will be placed in. This can slow down world generation. BO4: {}", this.getName());
		}
		if(illegalEntityData)
		{
			OTGLog.warn(LogCategory.CUSTOM_OBJECTS, "Warning: BO4 contains an Entity() that may be placed outside the chunk(s) that the BO3 will be placed in. This can slow down world generation. BO4: {}", this.getName());
		}

		blockStorage.setBranchesArray(tempBranchesList.toArray(new BO4BranchFunction[0]));
    }

	/**
	 * Gets the file this config will be written to. May be null if the config
	 * will never be written.
	 * @return The file.
	 */
	public File getFile()
	{
		return this.reader.getFile();
	}

	@Override
	public BlockFunction<?>[] getBlockFunctions(String presetFolderName, Path otgRootFolder,  ICustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		return getBlocks(presetFolderName, otgRootFolder,  (CustomObjectManager) customObjectManager, materialReader, manager, modLoadedChecker);
	}

	@Override
	protected void writeConfigSettings(SettingsWriterBO4 writer, IMaterialReader materialReader, CustomObjectResourcesManager manager) throws IOException
	{
		BO4ConfigWriter.writeSettings(this, writer, null, null, materialReader, manager);
	}

	public void writeWithData(SettingsWriterBO4 writer, List<BlockFunction<?>> blocksList, List<BranchFunction<?>> branchesList, IMaterialReader materialReader, CustomObjectResourcesManager manager) throws IOException
	{
		BO4ConfigWriter.writeWithData(this, writer, blocksList, branchesList, materialReader, manager);
	}

	@Override
	protected void readConfigSettings(String presetFolderName, Path otgRootFolder, ICustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker) throws InvalidConfigException
	{
		this.branchFrequency = readSettings(BO4Settings.BRANCH_FREQUENCY,  materialReader, manager);

		this.branchFrequencyGroup = readSettings(BO4Settings.BRANCH_FREQUENCY_GROUP,  materialReader, manager);
		this.branchFrequencyGroups = StringHelper.parseGroupMap(this.branchFrequencyGroup);

		this.heightOffset = readSettings(BO4Settings.HEIGHT_OFFSET,  materialReader, manager);
		this.inheritBO3Rotation = readSettings(BO4Settings.INHERITBO3ROTATION,  materialReader, manager);

		this.configRemoveAir = readSettings(BO4Settings.REMOVEAIR,  materialReader, manager);
		this.removeAir = this.configRemoveAir;
		this.isSpawnPoint = readSettings(BO4Settings.ISSPAWNPOINT,  materialReader, manager);
		this.useCenterForHighestBlock = readSettings(BO4Settings.USE_CENTER_FOR_HIGHEST_BLOCK,  materialReader, manager);
		this.configReplaceAbove = readSettings(BO4Settings.REPLACEABOVE,  materialReader, manager);
		this.replaceAbove = this.configReplaceAbove;
		this.configReplaceBelow = readSettings(BO4Settings.REPLACEBELOW,  materialReader, manager);
		this.replaceBelow = this.configReplaceBelow;
		this.replaceWithBiomeBlocks = readSettings(BO4Settings.REPLACEWITHBIOMEBLOCKS,  materialReader, manager);
		this.replaceWithGroundBlock = readSettings(BO4Settings.REPLACEWITHGROUNDBLOCK,  materialReader, manager);
		this.replaceWithSurfaceBlock = readSettings(BO4Settings.REPLACEWITHSURFACEBLOCK,  materialReader, manager);
		this.replaceWithStoneBlock = readSettings(BO4Settings.REPLACEWITHSTONEBLOCK,  materialReader, manager);

		this.bo3Group = readSettings(BO4Settings.BO3GROUP,  materialReader, manager);
		this.bo4Groups = StringHelper.parseGroupMap(this.bo3Group);

		this.canOverride = readSettings(BO4Settings.CANOVERRIDE,  materialReader, manager);
		this.mustBeBelowOther = readSettings(BO4Settings.MUSTBEBELOWOTHER,  materialReader, manager);
		this.mustBeInsideWorldBorders = readSettings(BO4Settings.MUSTBEINSIDEWORLDBORDERS,  materialReader, manager);

		this.mustBeInside = readSettings(BO4Settings.MUSTBEINSIDE,  materialReader, manager);
		this.mustBeInsideBranches = StringHelper.splitTrimmedList(this.mustBeInside);

		this.cannotBeInside =  readSettings(BO4Settings.CANNOTBEINSIDE,  materialReader, manager);
		this.cannotBeInsideBranches = StringHelper.splitTrimmedList(this.cannotBeInside);

		this.replacesBO3 = readSettings(BO4Settings.REPLACESBO3,  materialReader, manager);
		this.replacesBO3Branches = StringHelper.splitTrimmedList(this.replacesBO3);

		//smoothHeightOffset = readSettings(BO3Settings.SMOOTH_HEIGHT_OFFSET).equals("HeightOffset") ? heightOffset : Integer.parseInt(readSettings(BO3Settings.SMOOTH_HEIGHT_OFFSET));
		this.smoothHeightOffset = readSettings(BO4Settings.SMOOTH_HEIGHT_OFFSET,  materialReader, manager);
		this.canSpawnOnWater = readSettings(BO4Settings.CANSPAWNONWATER,  materialReader, manager);
		this.spawnOnWaterOnly = readSettings(BO4Settings.SPAWNONWATERONLY,  materialReader, manager);
		this.spawnUnderWater = readSettings(BO4Settings.SPAWNUNDERWATER,  materialReader, manager);
		this.spawnAtWaterLevel = readSettings(BO4Settings.SPAWNATWATERLEVEL,  materialReader, manager);
		this.inheritBO3 = readSettings(BO4Settings.INHERITBO3,  materialReader, manager);
		this.overrideChildSettings = readSettings(BO4Settings.OVERRIDECHILDSETTINGS,  materialReader, manager);
		this.overrideParentHeight = readSettings(BO4Settings.OVERRIDEPARENTHEIGHT,  materialReader, manager);
		this.smoothRadius = readSettings(BO4Settings.SMOOTHRADIUS,  materialReader, manager);
		this.smoothStartTop = readSettings(BO4Settings.SMOOTHSTARTTOP,  materialReader, manager);
		this.smoothStartWood = readSettings(BO4Settings.SMOOTHSTARTWOOD,  materialReader, manager);
		this.smoothingSurfaceBlock = readSettings(BO4Settings.SMOOTHINGSURFACEBLOCK,  materialReader, manager);
		this.smoothingGroundBlock = readSettings(BO4Settings.SMOOTHINGGROUNDBLOCK,  materialReader, manager);

		// Make sure that the BO3 wont try to spawn below Y 0 because of the height offset
		if(this.heightOffset < 0 && this.minHeight < -this.heightOffset)
		{
			this.minHeight = -this.heightOffset;
		}

		this.inheritedBO3s = new ArrayList<>();
		this.inheritedBO3s.add(this.getName()); // TODO: Make this cleaner?
		if(this.inheritBO3 != null && !this.inheritBO3.trim().isEmpty())
		{
			this.inheritedBO3s.add(this.inheritBO3);
		}

		this.author = readSettings(BO4Settings.AUTHOR,  materialReader, manager);
		this.description = readSettings(BO4Settings.DESCRIPTION,  materialReader, manager);
		this.settingsMode = readSettings(BO3Config.SETTINGS_MODE_BO3,  materialReader, manager);

		this.frequency = readSettings(BO4Settings.FREQUENCY,  materialReader, manager);
		this.spawnHeight = readSettings(BO4Settings.SPAWN_HEIGHT,  materialReader, manager);
		this.minHeight = readSettings(BO4Settings.MIN_HEIGHT,  materialReader, manager);
		this.maxHeight = readSettings(BO4Settings.MAX_HEIGHT,  materialReader, manager);
		this.maxHeight = Math.max(this.maxHeight, this.minHeight);

		this.doReplaceBlocks = readSettings(BO4Settings.DO_REPLACE_BLOCKS,  materialReader, manager);

		String fixedRotation = readSettings(BO4Settings.FIXED_ROTATION,  materialReader, manager);
		this.fixedRotation = Rotation.getRotation(fixedRotation);

		// Read the resources
		readResources( materialReader, manager);
	}

	void writeToStream(DataOutput stream, String presetFolderName, Path otgRootFolder,  CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker) throws IOException
	{
		BO4DataSerializer.writeToStream(this, stream, presetFolderName, otgRootFolder, customObjectManager, materialReader, manager, modLoadedChecker);
	}

	BO4Config readFromBO4DataFile(boolean getBlocks,  IMaterialReader materialReader) throws InvalidConfigException
	{
		return BO4DataSerializer.readFromBO4DataFile(this, getBlocks, materialReader);
	}

	@Override
	protected void correctSettings() { }

	@Override
	protected void renameOldSettings() { }
}
