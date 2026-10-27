package com.pg85.otg.config.settings.biome;

import com.pg85.otg.config.ConfigFunction;
import com.pg85.otg.config.settings.ConfigSection;
import com.pg85.otg.config.settings.preset.ResourceSettings;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.constants.settings.IceSpikeType;
import com.pg85.otg.interfaces.ICustomStructureGen;
import com.pg85.otg.interfaces.ISaplingSpawner;
import com.pg85.otg.util.helpers.StringHelper;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.minecraft.PlantType;
import com.pg85.otg.util.minecraft.SaplingType;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Builder
@Getter
public class BiomeResourceSettings extends ConfigSection {
    private final ResourceSettings parent;
    private final List<ICustomStructureGen> customStructures; // Used as a cache for fast querying, not saved
    private final Map<SaplingType, ISaplingSpawner> saplingGrowers;
    private final Map<LocalMaterialData, ISaplingSpawner> customSaplingGrowers;
    private final Map<LocalMaterialData, ISaplingSpawner> customBigSaplingGrowers;
    private final List<ConfigFunction<BiomeSettings>> resourceQueue;
    @Override
    public String getSectionName() {
        return "Biome Resource Settings";
    }
    public static final String[] RESOURCE_COMMENTS = new String[]{
            "Resource queue", "This section controls all resources spawning during decoration.",
            "The resources will be placed in this order.", "",
            "Keep in mind that a high size, frequency or rarity may slow down terrain generation.", "",
            "Possible resources:", "AboveWaterRes(BlockName,Frequency,Rarity)",
            "Boulder(BlockName,Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,..]",
            "Cactus(BlockName,Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,BlockSource3.....])",
            "CustomObject(Object[,AnotherObject[,...]])",
            "CustomStructure([Object,Object_Chance[,AnotherObject,Object_Chance[,...]]])",
            "Dungeon(Rarity,MinAltitude,MaxAltitude)", "Fossil(Rarity,MinAltitude,MaxAltitude)",
            "Grass(PlantType,Grouped/NotGrouped,Frequency,Rarity,BlockSource[,BlockSource2,BlockSource3.....])",
            "IceSpike(BlockName,IceSpikeType,Frequency,Rarity,MinAltitude,MaxAltitude,Blocksource[,BlockSource2,...])",
            "Liquid(BlockName,Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,BlockSource3.....])",
            "Ore(BlockName,Size,Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,BlockSource3.....],ExtendedParams,MaxSpawn)",
            "Plant(PlantType,Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,BlockSource3.....])",
            "Reed(BlockName,Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,BlockSource3.....])",
            "SmallLake(BlockName,Frequency,Rarity,MinAltitude,MaxAltitude)",
            "SurfacePatch(BlockName,DecorationBlockName,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,BlockSource3.....])",
            "Tree(Frequency,TreeType,TreeTypeChance[,AdditionalTreeType,AdditionalTreeTypeChance.....],ExtendedParams,MaxSpawn)",
            "UnderGroundLake(MinSize,MaxSize,Frequency,Rarity,MinAltitude,MaxAltitude)",
            "UnderWaterOre(BlockName,Size,Frequency,Rarity,BlockSource[,BlockSource2,BlockSource3.....])",
            "UnderWaterPlant(PlantType,Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,BlockSource3.....])",
            "Vein(BlockName,MinRadius,MaxRadius,Rarity,OreSize,OreFrequency,OreRarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,..])",
            "Vines(Frequency,Rarity,MinAltitude,MaxAltitude)",
            "Well(BaseBlockName,HalfSlabBlockName,WaterBlockName,Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,..])",
            "Bamboo(Frequency,Rarity,PodzolChance,BlockSource[,BlockSource2,BlockSource3.....])",
            "SeaGrass(Frequency,Rarity,TallChance)",
            "Kelp(Frequency,Rarity)",
            "SeaPickle(Frequency,Rarity,Attempts)",
            "Registry(RegistryKey,DecorationStage)",
            "CoralMushroom(Frequency,Rarity)",
            "CoralTree(Frequency,Rarity)",
            "CoralClaw(Frequency,Rarity)",
            "Iceberg(BlockName1,BlockName2,Chance[,AdditionalBlockName1,AdditionalBlockName2,AdditionalChance.....],TotalChance)",
            "BasaltColumn(BlockName,Frequency,Rarity,BaseSize,SizeVariance,BaseHeight,HeightVariance,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,BlockSource3.....])", "",
            "BlockName:	  	The name of the block, can include data.",
            "BlockSource:	List of blocks the resource can spawn on/in. You can also use \"Solid\" or \"All\".",
            "Frequency:	  	Number of attempts to place this resource in each chunk.",
            "Rarity:		Chance for each attempt, Rarity:100 - mean 100% to pass, Rarity:1 - mean 1% to pass.",
            "MinAltitude and MaxAltitude: Height limits.", "TallChance:	Number between 0.0 and 1.0",
            "TreeType:		Tree (original oak tree) - BigTree - Birch - TallBirch - SwampTree -",
            "			HugeMushroom (randomly red or brown) - HugeRedMushroom - HugeBrownMushroom -",
            "			Taiga1 - Taiga2 - HugeTaiga1 - HugeTaiga2 -",
            "			JungleTree (the huge jungle tree) - GroundBush - CocoaTree (smaller jungle tree)",
            "			DarkOak (from the roofed forest biome) - Acacia",
            "			New for 1.16.5: CrimsonFungi, WarpedFungi, ChorusPlant.",
            "			You can also use your own custom objects, as long as they have Tree:true in their settings.",
            "TreeTypeChance: Similar to Rarity. Example:",
            "			Tree(10,Taiga1,35,Taiga2,100) - tries 10 times, for each attempt it tries to place Taiga1 (35% chance),",
            "			if that fails, it attempts to place Taiga2 (100% chance).",
            "PlantType:	  	One of the plant types: " + StringHelper.join(PlantType.values(), ", "),
            "			or a block name",
            "IceSpikeType:  One of the ice spike types: " + StringHelper.join(IceSpikeType.values(), ","),
            "Object:		Any custom object (bo2 or bo3) file but without the file extension. ",
            "RegistryKey:	Registry key for a (non-OTG) default or configured feature. For example: minecraft:plain_vegetation",
            "DecorationStage: Optional, one of the vanilla decoration stages.",
            "               Can be: RAW_GENERATION, LAKES, LOCAL_MODIFICATIONS, UNDERGROUND_STRUCTURES, ",
            "			SURFACE_STRUCTURES, STRONGHOLDS, UNDERGROUND_ORES, UNDERGROUND_DECORATION,",
            "			VEGETAL_DECORATION, TOP_LAYER_MODIFICATION. VEGETAL_DECORATION by default.",
            "ExtendedParams: Optional, set this to true if you want to use additional parameters, like MaxSpawn.",
            "MaxSpawn: 		Optional, used with Frequency. When MaxSpawn spawn attempts have succeeded, stop spawning (for the current chunk).",
            "			For example, you can do 100 spawn attempts per chunk but stop after 5 have succeeded.",
            "",
            "Plant and Grass resource: Both a resource of one block. Plant can place blocks underground, Grass cannot.",
            "UnderWaterPlant resource: Similar to plant, but places blocks underwater.",
            "Liquid resource: A one-block water or lava source",
            "SmallLake and UnderGroundLake resources: Small lakes of about 8x8 blocks",
            "Vein resource: Starts an area where ores will spawn. Can be slow, so use a low Rarity (smaller than 1).",
            "CustomStructure resource: Starts a BO3 or BO4 structure in the chunk if spawn requirements are met.",
            ""
    };
    public static final String[] SAPLING_COMMENTS = new String[] {
            Constants.MOD_ID + " allows you to grow your custom objects from saplings, instead",
            "of the vanilla trees. Add one or more Sapling functions here to override vanilla",
            "spawning for that sapling.", "",
            "The syntax is: Sapling(SaplingType,TreeType,TreeType_Chance[,Additional_TreeType,Additional_TreeType_Chance.....])",
            "Works like Tree resource except first parameter.",
            "For custom saplings; Sapling(Custom,SaplingMaterial,WideTrunk,TreeType,TreeType_Chance.....)",
            "SaplingMaterial is the name of the sapling block.",
            "WideTrunk is 'true' or 'false', whether or not it requires 4 saplings.", "",
            "TreeType is one of the vanilla tree types (see resource queue comments) or a BO2/BO3 name.", "",
            "Sapling types: " + StringHelper.join(SaplingType.values(), ", "),
            "All - will make the tree spawn from all saplings, but not from mushrooms.",
            "BigJungle - for when 4 jungle saplings grow at once.",
            "RedMushroom/BrownMushroom - will only grow when bonemeal is used.", ""
    };

    public static BiomeResourceSettings getResourceSettings(ResourceSettings parent, List<ConfigFunction<BiomeSettings>> resources) {
        BiomeResourceSettingsBuilder builder = BiomeResourceSettings.builder();

        builder.parent(parent);
        builder.saplingGrowers(new HashMap<>());
        builder.customSaplingGrowers(new HashMap<>());
        builder.customBigSaplingGrowers(new HashMap<>());
        builder.customStructures(new ArrayList<>());
        builder.resourceQueue(new ArrayList<>());

        BiomeResourceSettings settings = builder.build();
        settings.readResourceSettings(resources);

        return settings;
    }

    protected void readResourceSettings(List<ConfigFunction<BiomeSettings>> resources)
    {
        for (ConfigFunction<BiomeSettings> res : resources)
        {
            if (res != null)
            {
                if (res instanceof ISaplingSpawner sapling)
                {
                    if (sapling.getSaplingType() == SaplingType.Custom)
                    {
                        // Puts big custom saplings in the big list and small in the small list
                        if (sapling.hasWideTrunk())
                        {
                            this.customBigSaplingGrowers.put(sapling.getSaplingMaterial(), sapling);
                        } else {
                            this.customSaplingGrowers.put(sapling.getSaplingMaterial(), sapling);
                        }
                    } else {
                        this.saplingGrowers.put(sapling.getSaplingType(), sapling);
                    }
                } else {
                    this.resourceQueue.add(res);
                    if (res instanceof ICustomStructureGen)
                    {
                        this.customStructures.add((ICustomStructureGen) res);
                    }
                }
            }
        }
    }
}
