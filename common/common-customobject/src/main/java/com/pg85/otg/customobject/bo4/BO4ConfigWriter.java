package com.pg85.otg.customobject.bo4;

import com.pg85.otg.constants.settings.ConfigMode;
import com.pg85.otg.customobject.bo3.BO3Config;
import com.pg85.otg.customobject.bo4.bo4function.BO4BlockFunction;
import com.pg85.otg.customobject.bo4.bo4function.BO4BranchFunction;
import com.pg85.otg.customobject.bo4.bo4function.BO4EntityFunction;
import com.pg85.otg.customobject.bo4.bo4function.BO4RandomBlockFunction;
import com.pg85.otg.customobject.bo4.bo4function.BO4WeightedBranchFunction;
import com.pg85.otg.customobject.bofunctions.BlockFunction;
import com.pg85.otg.customobject.bofunctions.BranchFunction;
import com.pg85.otg.customobject.config.CustomObjectConfigFunction;
import com.pg85.otg.customobject.config.CustomObjectResourcesManager;
import com.pg85.otg.customobject.config.io.SettingsWriterBO4;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.util.minecraft.DefaultStructurePart;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class BO4ConfigWriter
{
	static void writeWithData(BO4Config config, SettingsWriterBO4 writer, List<BlockFunction<?>> blocksList, List<BranchFunction<?>> branchesList, IMaterialReader materialReader, CustomObjectResourcesManager manager) throws IOException
	{
		writer.setConfigMode(ConfigMode.WriteAll);
		try
		{
			writer.open();
			writeSettings(config, writer, blocksList, branchesList, materialReader, manager);
		} finally {
			writer.close();
		}
	}

	static void writeSettings(BO4Config config, SettingsWriterBO4 writer, List<BlockFunction<?>> blocksList, List<BranchFunction<?>> branchesList, IMaterialReader materialReader, CustomObjectResourcesManager manager) throws IOException
	{
		// The object
		writer.bigTitle("BO4 object");
		writer.comment("This is the config file of a custom object.");
		writer.comment("If you add this object correctly to your BiomeConfigs, it will spawn in the world.");
		writer.comment("");

		writer.comment("This is the creator of this BO4 object");
		writer.setting(BO4Settings.AUTHOR, config.author);

		writer.comment("A short description of this BO4 object");
		writer.setting(BO4Settings.DESCRIPTION, config.description);

		if(writer.getFile().getName().toUpperCase().endsWith(".BO3"))
		{
			writer.comment("Legacy setting, always true for BO4's. Only used if the file has a .BO3 extension.");
			writer.comment("Rename your file to .BO4 and remove this setting.");
			writer.setting(BO4Settings.ISOTGPLUS, true);
		}

		writer.comment("The settings mode, WriteAll, WriteWithoutComments or WriteDisable. See DimensionPresetConfig.");
		writer.setting(BO3Config.SETTINGS_MODE_BO3, config.settingsMode);

		// Main settings
		writer.bigTitle("Main settings");

		writer.comment("If this BO4 should spawn with a fixed rotation, set it here.");
		writer.comment("For example: NORTH, EAST, SOUTH or WEST. Empty by default");
		writer.setting(BO4Settings.FIXED_ROTATION, config.fixedRotation == null ? "" : config.fixedRotation.name());

		writer.comment("This BO4 can only spawn at least Frequency chunks distance away from any other BO4 with the exact same name.");
		writer.comment("You can use this to make this BO4 spawn in groups or make sure that this BO4 only spawns once every X chunks.");
		writer.setting(BO4Settings.FREQUENCY, config.frequency);

		writer.comment("The spawn height of the BO4: randomY, highestBlock or highestSolidBlock.");
		writer.setting(BO4Settings.SPAWN_HEIGHT, config.spawnHeight);

		writer.comment("When set to true, uses the center of the structure (determined by minimum structure size) when checking the highestBlock to spawn at.");
		writer.setting(BO4Settings.USE_CENTER_FOR_HIGHEST_BLOCK, config.useCenterForHighestBlock);

		writer.smallTitle("Height Limits for the BO4.");

		writer.comment("When in randomY mode used as the minimum Y or in atMinY mode as the actual Y to spawn this BO4 at.");
		writer.setting(BO4Settings.MIN_HEIGHT, config.minHeight);

		writer.comment("When in randomY mode used as the maximum Y to spawn this BO4 at.");
		writer.setting(BO4Settings.MAX_HEIGHT, config.maxHeight);

		writer.comment("Copies the blocks and branches of an existing BO4 into this BO4. You can still add blocks and branches in this BO4, they will be added on top of the inherited blocks and branches.");
		writer.setting(BO4Settings.INHERITBO3, config.inheritBO3);
		writer.comment("Rotates the inheritedBO3's resources (blocks, spawners, checks etc) and branches, defaults to NORTH (no rotation).");
		writer.setting(BO4Settings.INHERITBO3ROTATION, config.inheritBO3Rotation);

		writer.comment("Defaults to true, if true and this is the starting BO4 for this branching structure then this BO4's smoothing and height settings are used for all children (branches).");
		writer.setting(BO4Settings.OVERRIDECHILDSETTINGS, config.overrideChildSettings);
		writer.comment("Defaults to false, if true then this branch uses it's own height settings (SpawnHeight, minHeight, maxHeight, spawnAtWaterLevel) instead of those defined in the starting BO4 for this branching structure.");
		writer.setting(BO4Settings.OVERRIDEPARENTHEIGHT, config.overrideParentHeight);
		writer.comment("If this is set to true then this BO4 can spawn on top of or inside an existing BO4. If this is set to false then this BO4 will use a bounding box to detect collisions with other BO4's, if a collision is detected then this BO4 won't spawn and the current branch is rolled back.");
		writer.setting(BO4Settings.CANOVERRIDE, config.canOverride);

		writer.comment("This branch can only spawn at least branchFrequency chunks (x,z) distance away from any other branch with the exact same name.");
		writer.setting(BO4Settings.BRANCH_FREQUENCY, config.branchFrequency);
		writer.comment("Define groups that this branch belongs to along with a minimum (x,z) range in chunks that this branch must have between it and any other members of this group if it is to be allowed to spawn. Syntax is \"GroupName:Frequency, GoupName2:Frequency2\" etc so for example a branch that belongs to 3 groups: \"BranchFrequencyGroup: Ships:10, Vehicles:5, FloatingThings:3\".");
		writer.setting(BO4Settings.BRANCH_FREQUENCY_GROUP, config.branchFrequencyGroup);

		writer.comment("If this is set to true then this BO4 can only spawn underneath an existing BO4. Used to make sure that dungeons only appear underneath buildings.");
		writer.setting(BO4Settings.MUSTBEBELOWOTHER, config.mustBeBelowOther);

		writer.comment("Used with CanOverride: true. A comma-seperated list of BO4s, this BO4's bounding box must collide with one of the BO4's in the list or this BO4 fails to spawn and the current branch is rolled back. AND/OR is supported, comma is OR, space is AND, f.e: branch1, branch2 branch3, branch 4.");
		writer.setting(BO4Settings.MUSTBEINSIDE, config.mustBeInside);

		writer.comment("Used with CanOverride: true. A comma-seperated list of BO4s, this BO4's bounding box cannot collide with any of the BO4's in the list or this BO4 fails to spawn and the current branch is rolled back.");
		writer.setting(BO4Settings.CANNOTBEINSIDE, config.cannotBeInside);

		writer.comment("Used with CanOverride: true. A comma-seperated list of BO4s, if this BO4's bounding box collides with any of the BO4's in the list then those BO4's won't spawn any blocks. This does not remove or roll back any BO4's.");
		writer.setting(BO4Settings.REPLACESBO3, config.replacesBO3);

		writer.comment("If this is set to true then this BO4 can only spawn inside world borders. Used to make sure that dungeons only appear inside the world borders.");
		writer.setting(BO4Settings.MUSTBEINSIDEWORLDBORDERS, config.mustBeInsideWorldBorders);

		writer.comment("Defaults to true. Set to false if the BO4 is not allowed to spawn on a water block");
		writer.setting(BO4Settings.CANSPAWNONWATER, config.canSpawnOnWater);

		writer.comment("Defaults to false. Set to true if the BO4 is allowed to spawn only on a water block");
		writer.setting(BO4Settings.SPAWNONWATERONLY, config.spawnOnWaterOnly);

		writer.comment("Defaults to false. Set to true if the BO4 and its smoothing area should ignore water when looking for the highest block to spawn on. Defaults to false (things spawn on top of water)");
		writer.setting(BO4Settings.SPAWNUNDERWATER, config.spawnUnderWater);

		writer.comment("Defaults to false. Set to true if the BO4 should spawn at water level");
		writer.setting(BO4Settings.SPAWNATWATERLEVEL, config.spawnAtWaterLevel);

		writer.comment("Spawns the BO4 at a Y offset of this value. Handy when using highestBlock for lowering BO4s into the surrounding terrain when there are layers of ground included in the BO4, also handy when using SpawnAtWaterLevel to lower objects like ships into the water.");
		writer.setting(BO4Settings.HEIGHT_OFFSET, config.heightOffset);

		writer.comment("If set to true removes all AIR blocks from the BO4 so that it can be flooded or buried.");
		writer.setting(BO4Settings.REMOVEAIR, config.configRemoveAir);

		writer.comment("Replaces all the non-air blocks that are above this BO4 or its smoothing area with the given block material (should be WATER or AIR or NONE), also applies to smoothing areas although OTG intentionally leaves some of the terrain above them intact. WATER can be used in combination with SpawnUnderWater to fill any air blocks underneath waterlevel with water (and any above waterlevel with air).");
		writer.setting(BO4Settings.REPLACEABOVE, config.configReplaceAbove);

		writer.comment("Replaces all air blocks underneath the BO4 (but not its smoothing area) with the specified material until a solid block is found.");
		writer.setting(BO4Settings.REPLACEBELOW, config.configReplaceBelow);

		writer.comment("Defaults to true. If set to true then every block in the BO4 of the materials defined in ReplaceWithGroundBlock or ReplaceWithSurfaceBlock will be replaced by the GroundBlock or SurfaceBlock materials configured for the biome the block is spawned in.");
		writer.setting(BO4Settings.REPLACEWITHBIOMEBLOCKS, config.replaceWithBiomeBlocks);

		writer.comment("Defaults to GRASS, Replaces all the blocks of the given material in the BO4 with the SurfaceBlock configured for the biome it spawns in.");
		writer.setting(BO4Settings.REPLACEWITHSURFACEBLOCK, config.replaceWithSurfaceBlock);

		writer.comment("Defaults to DIRT, Replaces all the blocks of the given material in the BO4 with the GroundBlock configured for the biome it spawns in.");
		writer.setting(BO4Settings.REPLACEWITHGROUNDBLOCK, config.replaceWithGroundBlock);

		writer.comment("Defaults to STONE, Replaces all the blocks of the given material in the BO4 with the StoneBlock configured for the biome it spawns in.");
		writer.setting(BO4Settings.REPLACEWITHSTONEBLOCK, config.replaceWithStoneBlock);

		writer.comment("Makes the terrain around the BO4 slope evenly towards the edges of the BO4. The given value is the distance in blocks around the BO4 from where the slope should start and can be any positive number.");
		writer.setting(BO4Settings.SMOOTHRADIUS, config.smoothRadius);

		writer.comment("Moves the smoothing area up or down relative to the BO4 (at the points where the smoothing area is connected to the BO4). Handy when using SmoothStartTop: false and the BO4 has some layers of ground included, in that case we can set the HeightOffset to a negative value to lower the BO4 into the ground and we can set the SmoothHeightOffset to a positive value to move the smoothing area starting height up.");
		writer.setting(BO4Settings.SMOOTH_HEIGHT_OFFSET, config.smoothHeightOffset);

		writer.comment("Should the smoothing area be attached at the bottom or the top of the edges of the BO4? Defaults to false (bottom). Using this setting can make things slower so try to avoid using it and use SmoothHeightOffset instead if for instance you have a BO4 with some ground layers included. The only reason you should need to use this setting is if you have a BO4 with edges that have an irregular height (like some hills).");
		writer.setting(BO4Settings.SMOOTHSTARTTOP, config.smoothStartTop);

		writer.comment("Should the smoothing area attach itself to \"log\" block or ignore them? Defaults to false (ignore logs).");
		writer.setting(BO4Settings.SMOOTHSTARTWOOD, config.smoothStartWood);

		writer.comment("The block used for smoothing area surface blocks, defaults to biome SurfaceBlock.");
		writer.setting(BO4Settings.SMOOTHINGSURFACEBLOCK, config.smoothingSurfaceBlock);

		writer.comment("The block used for smoothing area ground blocks, defaults to biome GroundBlock.");
		writer.setting(BO4Settings.SMOOTHINGGROUNDBLOCK, config.smoothingGroundBlock);

		writer.comment("Define groups that this BO4 belongs to along with a minimum range in chunks that this BO4 must have between it and any other members of this group if it is to be allowed to spawn. Syntax is \"GroupName:Frequency, GoupName2:Frequency2\" etc so for example a BO4 that belongs to 3 groups: \"BO4Group: Ships:10, Vehicles:5, FloatingThings:3\".");
		writer.setting(BO4Settings.BO3GROUP, config.bo3Group);

		writer.comment("Defaults to false. Set to true if this BO4 should spawn at the player spawn point. When the server starts the spawn point is determined and the BO4's for the biome it is in are loaded, one of these BO4s that has IsSpawnPoint set to true (if any) is selected randomly and is spawned at the spawn point regardless of its rarity (so even Rarity:0, IsSpawnPoint: true BO4's can get spawned as the spawn point!).");
		writer.setting(BO4Settings.ISSPAWNPOINT, config.isSpawnPoint);

		writer.comment("Defaults to true. Set to false to make the BO4 ignore any ReplacedBlocks settings in Biome Configs.");
		writer.setting(BO4Settings.DO_REPLACE_BLOCKS, config.doReplaceBlocks);

		// Blocks and other things
		writeResources(config, writer, blocksList, branchesList, materialReader, manager);

		if(config.reader != null) // Can be true for BO4Creator?
		{
			config.reader.flushCache();
		}
	}

	private static void writeResources(BO4Config config, SettingsWriterBO4 writer, List<BlockFunction<?>> blocksList, List<BranchFunction<?>> branchesList, IMaterialReader materialReader, CustomObjectResourcesManager manager) throws IOException
	{
		writer.bigTitle("Blocks");
		writer.comment("All the blocks used in the BO4 are listed here. Possible blocks:");
		writer.comment("Block(x,y,z,id[.data][,nbtfile.nbt)");
		writer.comment("RandomBlock(x,y,z,id[:data][,nbtfile.nbt],chance[,id[:data][,nbtfile.nbt],chance[,...]])");
		writer.comment(" So RandomBlock(0,0,0,CHEST,chest.nbt,50,CHEST,anotherchest.nbt,100) will spawn a chest at");
		writer.comment(" the BO4 origin, and give it a 50% chance to have the contents of chest.nbt, or, if that");
		writer.comment(" fails, a 100% percent chance to have the contents of anotherchest.nbt.");
		writer.comment("MinecraftObject(x,y,z,name) (NOT IMPLEMENTED - will log a warning and be skipped).");
		writer.comment(" Spawns an object in the Mojang NBT structure format. For example, ");
		writer.comment(" MinecraftObject(0,0,0," + DefaultStructurePart.IGLOO_BOTTOM.getPath() + ")");
		writer.comment(" spawns the bottom part of an igloo.");

		ArrayList<BO4EntityFunction> entitiesList = new ArrayList<>();

		// Re-read the raw data, if no data was supplied. Don't save any loaded data, since it has been processed/transformed.
		if(blocksList == null || branchesList == null)
		{
			blocksList = new ArrayList<>();
			branchesList = new ArrayList<>();

			for (CustomObjectConfigFunction<BO4Config> res : config.reader.getConfigFunctions(config, true, materialReader, manager))
			{
				if (res.isValid())
				{
					switch (res) {
						case BO4RandomBlockFunction bo4RandomBlockFunction -> blocksList.add(bo4RandomBlockFunction);
						case BO4BlockFunction bo4BlockFunction -> blocksList.add(bo4BlockFunction);
						case BO4WeightedBranchFunction bo4WeightedBranchFunction -> branchesList.add(bo4WeightedBranchFunction);
						case BO4BranchFunction bo4BranchFunction -> branchesList.add(bo4BranchFunction);
						case BO4EntityFunction bo4EntityFunction -> entitiesList.add(bo4EntityFunction);
						default -> {
						}
					}
				}
			}
		}
		else {
			// The blockslist passed is not used after this,
			// so it's ok to edit the block objects.
			for(BlockFunction<?> block : blocksList)
			{
				block.x -= config.getXOffset();
				block.z -= config.getZOffset();
			}
		}
		for(BlockFunction<?> block : blocksList)
		{
			writer.function(block);
		}

		writer.bigTitle("Branches");
		writer.comment("Branches are child-BO4's that spawn if this BO4 is configured to spawn as a");
		writer.comment("CustomStructure resource in a biome config. Branches can have branches,");
		writer.comment("making complex structures possible. See the wiki for more details.");
		writer.comment("");
		writer.comment("Regular Branches spawn each branch with an independent chance of spawning.");
		writer.comment("Branch(x,y,z,isRequiredBranch,branchName,rotation,chance,branchDepth[,anotherBranchName,rotation,chance,branchDepth[,...]][IndividualChance])");
		writer.comment("branchName - name of the object to spawn.");
		writer.comment("rotation - NORTH, SOUTH, EAST or WEST.");
		writer.comment("IndividualChance - The chance each branch has to spawn, assumed to be 100 when left blank");
		writer.comment("isRequiredBranch - If this is set to true then at least one of the branches in this BO4 must spawn at these x,y,z coordinates. If no branch can spawn there then this BO4 fails to spawn and its branch is rolled back.");
		writer.comment("isRequiredBranch:true branches must spawn or the current branch is rolled back entirely. This is useful for grouping BO4's that must spawn together, for instance a single room made of multiple BO4's/branches.");
		writer.comment("If all parts of the room are connected together via isRequiredBranch:true branches then either the entire room will spawns or no part of it will spawn.");
		writer.comment("*Note: When isRequiredBranch:true only one BO4 can be added per Branch() and it will automatically have a rarity of 100.0.");
		writer.comment("isRequiredBranch:false branches are used to make optional parts of structures, for instance the middle section of a tunnel that has a beginning, middle and end BO4/branch and can have a variable length by repeating the middle BO4/branch.");
		writer.comment("By making the start and end branches isRequiredBranch:true and the middle branch isRequiredbranch:false you can make it so that either:");
		writer.comment("A. A tunnel spawns with at least a beginning and end branch");
		writer.comment("B. A tunnel spawns with a beginning and end branch and as many middle branches as will fit in the available space.");
		writer.comment("C. No tunnel spawns at all because there wasn't enough space to spawn at least a beginning and end branch.");
		writer.comment("branchDepth - When creating a chain of branches that contains optional (isRequiredBranch:false) branches branch depth is configured for the first BO4 in the chain to determine the maximum length of the chain.");
		writer.comment("branchDepth - 1 is inherited by each isRequiredBranch:false branch in the chain. When branchDepth is zero isRequiredBranch:false branches cannot spawn and the chain ends. In the case of the tunnel this means the last middle branch would be");
		writer.comment("rolled back and an IsRequiredBranch:true end branch could be spawned in its place to make sure the tunnel has a proper ending.");
		writer.comment("Instead of inheriting branchDepth - 1 from the parent branchDepth can be overridden by child branches if it is set higher than 0 (the default value).");
		writer.comment("isRequiredBranch:true branches do inherit branchDepth and pass it on to their own branches, however they cannot be prevented from spawning by it and also don't subtract 1 from branchDepth when inheriting it.");
		writer.comment("");
		writer.comment("Weighted Branches spawn branches with a dependent chance of spawning.");
		writer.comment("WeightedBranch(x,y,z,isRequiredBranch,branchName,rotation,chance,branchDepth[,anotherBranchName,rotation,chance,branchDepth[,...]][MaxChanceOutOf])");
		writer.comment("*Note: isRequiredBranch must be set to false. It is not possible to use isRequiredBranch:true with WeightedBranch() since isRequired:true branches must spawn and automatically have a rarity of 100.0.");
		writer.comment("MaxChanceOutOf - The chance all branches have to spawn out of, assumed to be 100 when left blank");

		for(BranchFunction<?> func : branchesList)
		{
			writer.function(func);
		}

		writer.bigTitle("Entities");
		writer.comment("Forge only (this may have changed, check for updates).");
		writer.comment("An EntityFunction spawns an entity instead of a block. The entity is spawned only once when the BO4 is spawned.");
		writer.comment("Entities are persistent by default so they don't de-spawn when no player is near, they are only unloaded.");
		writer.comment("Usage: Entity(x,y,z,entityName,groupSize,NameTagOrNBTFileName) or Entity(x,y,z,mobName,groupSize)");
		writer.comment("Use /otg entities to get a list of entities that can be used as entityName, this includes entities added by other mods and non-living entities.");
		writer.comment("NameTagOrNBTFileName can be either a nametag for the mob or an .txt file with nbt data (such as myentityinfo.txt).");
		writer.comment("In the text file you can use the same mob spawning parameters used with the /summon command to equip the");
		writer.comment("entity and give it custom attributes etc. You can copy the DATA part of a summon command including surrounding ");
		writer.comment("curly braces to a .txt file, for instance for: \"/summon Skeleton x y z {DATA}\"");

		for(BO4EntityFunction func : entitiesList)
		{
			writer.function(func);
		}
	}
}
