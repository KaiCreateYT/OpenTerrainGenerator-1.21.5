package com.pg85.otg.customobject.resource;

import com.pg85.otg.customobject.CustomObject;
import com.pg85.otg.customobject.CustomObjectManager;
import com.pg85.otg.customobject.config.CustomObjectResourcesManager;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.config.settings.biome.BiomeSettings;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IModLoadedChecker;
import com.pg85.otg.interfaces.ISaplingSpawner;
import com.pg85.otg.interfaces.IWorldGenRegion;
import com.pg85.otg.util.OTGLog;
import com.pg85.otg.util.OTGMaterialReader;
import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.minecraft.SaplingType;

import java.nio.file.Path;
import java.util.*;

/**
 * Represents a custom sapling generator, which can grow vanilla trees or custom objects.
 */
public class SaplingResource extends ISaplingSpawner
{
	private static final Map<Rotation, int[]> TREE_OFFSET;
	static
	{
		TREE_OFFSET = new EnumMap<>(Rotation.class);
		TREE_OFFSET.put(Rotation.NORTH, new int[] {0, 0});
		TREE_OFFSET.put(Rotation.EAST, new int[] {1, 0});
		TREE_OFFSET.put(Rotation.SOUTH, new int[] {1, 1});
		TREE_OFFSET.put(Rotation.WEST, new int[] {0, 1});
	}

	// wideTrunk and saplingMaterial are used by custom saplings only
	private final String biomeName;
	private List<Double> treeChances;
	private List<String> treeNames;
	private List<CustomObject> trees;
	private boolean treesLoaded = false;

	public final SaplingType saplingType;
	public LocalMaterialData saplingMaterial;
	public boolean wideTrunk;

	public SaplingResource(BiomeSettings biomeConfig, List<String> args) throws InvalidConfigException
	{
		assureSize(3, args);

		this.saplingType = SaplingType.get(args.get(0));
		if (this.saplingType == SaplingType.Custom)
		{
			try {
				this.saplingMaterial = OTGMaterialReader.get().readMaterial(args.get(1));
			} catch (InvalidConfigException e) {
				OTGLog.error(LogCategory.DECORATION,
						"Invalid custom sapling configuration! Syntax: Sapling(Custom, material, widetrunk, TreeName, TreeChance, ...)");
			}
		}
		if (this.saplingType == null && this.saplingMaterial == null)
		{
			throw new InvalidConfigException("Unknown sapling type " + args.get(0));
		}

		this.trees = new ArrayList<>();
		this.treeNames = new ArrayList<>();
		this.treeChances = new ArrayList<>();
		this.biomeName = biomeConfig.getIdentitySettings().getBiomeName();
		int ind = 1;
		if (this.saplingType == SaplingType.Custom)
		{
			ind = 3;
			if (args.get(2).equalsIgnoreCase("true"))
			{
				this.wideTrunk = true;
			}
			else if (args.get(2).equalsIgnoreCase("false"))
			{
				this.wideTrunk = false;
			} else {
				this.wideTrunk = false;
				ind = 2;
			}
		}
		for (int i = ind; i < args.size() - 1; i += 2)
		{
			String treeName = args.get(i);
			this.treeNames.add(treeName);
			this.treeChances.add(readDouble(args.get(i + 1), 1, 100));
		}
	}

	@Override
	public boolean hasWideTrunk()
	{
		return this.wideTrunk;
	}

	@Override
	public SaplingType getSaplingType() {
		return saplingType;
	}

	@Override
	public LocalMaterialData getSaplingMaterial() {
		return saplingMaterial;
	}

	private static CustomObject getTreeObject(String objectName, String presetFolderName, Path otgRootFolder,  CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker) throws InvalidConfigException
	{
		CustomObject maybeTree = customObjectManager.getGlobalObjects().getObjectByName(objectName, presetFolderName, otgRootFolder,  customObjectManager, materialReader, manager, modLoadedChecker);
		if (maybeTree == null)
		{
			throw new InvalidConfigException("Unknown object " + objectName);
		}
		if (!maybeTree.canSpawnAsTree())
		{
			throw new InvalidConfigException("Cannot spawn " + objectName + " as tree");
		}
		return maybeTree;
	}

	/**
	 * Grows a tree from this sapling.
	 * @param isWideTree Whether the tree is a wide (2x2 trunk) tree. Used to
	 *					correctly rotate those trees.
	 * @param x		  X to spawn. For wide trees, this is the lowest x of
	 *					the trunk.
	 * @param y		  Y to spawn.
	 * @param z		  Z to spawn. For wide trees, this is the lowest z of
	 *					the trunk.
	 * @return Whether a tree was grown.
	 */
	public boolean growSapling(IWorldGenRegion worldGenRegion, Random random, boolean isWideTree, int x, int y, int z, String presetFolderName, Path otgRootFolder,  CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		loadTreeObjects(presetFolderName, otgRootFolder,  customObjectManager, materialReader, manager, modLoadedChecker);

		CustomObject tree;
		Rotation rotation;
		int spawnX;
		int spawnZ;
		int[] offset;
		for (int treeNumber = 0; treeNumber < this.trees.size(); treeNumber++)
		{
			if (random.nextInt(100) < this.treeChances.get(treeNumber))
			{
				tree = this.trees.get(treeNumber);
				rotation = tree.canRotateRandomly() ? Rotation.getRandomRotation(random) : Rotation.NORTH;

				// Correct spawn location for rotated wide trees
				spawnX = x;
				spawnZ = z;
				if (isWideTree)
				{
					offset = TREE_OFFSET.get(rotation);
					spawnX += offset[0];
					spawnZ += offset[1];
				}

				if (tree.spawnFromSapling(worldGenRegion,
                                          random, rotation, spawnX, y, spawnZ))
				{
					// Success!
					return true;
				}
			}
		}
		return false;
	}

	private void loadTreeObjects(String presetFolderName, Path otgRootFolder,  CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		if(!this.treesLoaded)
		{
			this.treesLoaded = true;

			for(String treeName : this.treeNames)
			{
				CustomObject tree;
				try {
					tree = getTreeObject(treeName, presetFolderName, otgRootFolder,  customObjectManager, materialReader, manager, modLoadedChecker);
					this.trees.add(tree);
				} catch (InvalidConfigException e) {
					this.trees.add(null);
					OTGLog.error(LogCategory.CUSTOM_OBJECTS, "Could not find Object {} for Sapling() resource in biome {}", treeName, this.biomeName);
				}
			}

			ArrayList<CustomObject> newTrees = new ArrayList<>();
			ArrayList<String> newTreeNames = new ArrayList<>();
			ArrayList<Double> newTreeChances = new ArrayList<>();

			for(int i = 0; i < this.trees.size(); i++)
			{
				if(this.trees.get(i) != null)
				{
					newTrees.add(this.trees.get(i));
					newTreeNames.add(this.treeNames.get(i));
					newTreeChances.add(this.treeChances.get(i));
				}
			}

			this.trees = newTrees;
			this.treeNames = newTreeNames;
			this.treeChances = newTreeChances;
		}
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder("Sapling(").append(this.saplingType);
		if (this.saplingType == SaplingType.Custom)
		{
			sb.append(",").append(this.saplingMaterial.getName()).append(",").append(this.wideTrunk);
		}
		for (int i = 0; i < this.treeNames.size(); i++)
		{
			sb.append(",").append(this.treeNames.get(i)).append(",").append(this.treeChances.get(i));
		}
		return sb.append(')').toString();
	}
}
