package com.pg85.otg.config.biome;

import java.util.*;

import com.pg85.otg.config.ConfigFunction;
import com.pg85.otg.config.io.SettingsMap;
import com.pg85.otg.config.settings.biome.*;
import com.pg85.otg.config.preset.DimensionPresetConfig;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.customobject.resource.CustomObjectResource;
import com.pg85.otg.customobject.resource.CustomStructureResource;
import com.pg85.otg.customobject.resource.SaplingResource;
import com.pg85.otg.customobject.resource.TreeResource;
import com.pg85.otg.gen.resource.*;
import com.pg85.otg.interfaces.*;
import lombok.Getter;
import lombok.Setter;

/**
 * BiomeConfig (*.bc) classes
 * <p>
 * BiomeSettings defines and implements anything needed for BiomeConfig. BiomeConfig
 * contains mainly io/serialisation/instantiation, or details specific to the file of origin.
 * <p>
 * BiomeConfig should be used only in common-core and platform-specific layers,
 * when reading/writing settings on app start. BiomeSettings should be used
 * wherever settings are used in code.
 */
public class BiomeConfig extends BiomeSettings
{
	public static final HashMap<String, Class<? extends ConfigFunction<?>>> RESOURCE_QUEUE_RESOURCES = new HashMap<>();
	static
	{
		RESOURCE_QUEUE_RESOURCES.put("AboveWaterRes", AboveWaterResource.class);
		RESOURCE_QUEUE_RESOURCES.put("Boulder", BoulderResource.class);
		RESOURCE_QUEUE_RESOURCES.put("Cactus", CactusResource.class);
		RESOURCE_QUEUE_RESOURCES.put("Dungeon", DungeonResource.class);
		RESOURCE_QUEUE_RESOURCES.put("Grass", GrassResource.class);
		RESOURCE_QUEUE_RESOURCES.put("Fossil", FossilResource.class);
		RESOURCE_QUEUE_RESOURCES.put("IceSpike", IceSpikeResource.class);
		RESOURCE_QUEUE_RESOURCES.put("Liquid", LiquidResource.class);
		RESOURCE_QUEUE_RESOURCES.put("Ore", OreResource.class);
		RESOURCE_QUEUE_RESOURCES.put("Plant", PlantResource.class);
		RESOURCE_QUEUE_RESOURCES.put("UnderWaterPlant", UnderWaterPlantResource.class);
		RESOURCE_QUEUE_RESOURCES.put("Reed", ReedResource.class);
		RESOURCE_QUEUE_RESOURCES.put("SmallLake", SmallLakeResource.class);
		RESOURCE_QUEUE_RESOURCES.put("SurfacePatch", SurfacePatchResource.class);
		RESOURCE_QUEUE_RESOURCES.put("UndergroundLake", UndergroundLakeResource.class);
		RESOURCE_QUEUE_RESOURCES.put("UnderWaterOre", UnderWaterOreResource.class);
		RESOURCE_QUEUE_RESOURCES.put("Vein", VeinResource.class);
		RESOURCE_QUEUE_RESOURCES.put("Vines", VinesResource.class);
		RESOURCE_QUEUE_RESOURCES.put("Well", WellResource.class);
		RESOURCE_QUEUE_RESOURCES.put("CustomObject", CustomObjectResource.class);
		RESOURCE_QUEUE_RESOURCES.put("CustomStructure", CustomStructureResource.class);
		RESOURCE_QUEUE_RESOURCES.put("Sapling", SaplingResource.class);
		RESOURCE_QUEUE_RESOURCES.put("Tree", TreeResource.class);
		RESOURCE_QUEUE_RESOURCES.put("Bamboo", BambooResource.class);
		RESOURCE_QUEUE_RESOURCES.put("SeaGrass", SeaGrassResource.class);
		RESOURCE_QUEUE_RESOURCES.put("Kelp", KelpResource.class);
		RESOURCE_QUEUE_RESOURCES.put("SeaPickle", SeaPickleResource.class);
		RESOURCE_QUEUE_RESOURCES.put("Registry", RegistryResource.class);
		RESOURCE_QUEUE_RESOURCES.put("CoralMushroom", CoralMushroomResource.class);
		RESOURCE_QUEUE_RESOURCES.put("CoralTree", CoralTreeResource.class);
		RESOURCE_QUEUE_RESOURCES.put("CoralClaw", CoralClawResource.class);
		RESOURCE_QUEUE_RESOURCES.put("Iceberg", IcebergResource.class);
		RESOURCE_QUEUE_RESOURCES.put("BasaltColumn", BasaltColumnResource.class);
		RESOURCE_QUEUE_RESOURCES.put("Group", VegetationGroupResource.class);
		RESOURCE_QUEUE_RESOURCES.put("Scatter", VegetationScatterResource.class);
	}

	private final DimensionPresetConfig parent;
	@Setter
	private MobSettings mergedMobSettings = null;

	public BiomeConfig(SettingsMap settingsMap, DimensionPresetConfig presetSettings)
	{
		super(settingsMap, presetSettings, new BiomeResourcesManager(presetSettings.getWorldInfo()));
		parent = presetSettings;
	}

	@Override
	public void writeConfigSettings(SettingsMap writer) {
		writer.putSetting(Constants.ConfigVersionSetting, Constants.ConfigVersion);
		//BiomeConfigWriter.writeConfigSettings(this, writer);
		super.writeConfigSettings(writer);
	}

	@Override
	public boolean biomeConfigsHaveReplacement() {
		return this.parent.isBiomeConfigsHaveReplacement();
	}

    public MobSettings getMergedMobSettings() {
		if (mergedMobSettings == null) {
			mergedMobSettings = mobSettings;
		}
        return mergedMobSettings;
    }
}
