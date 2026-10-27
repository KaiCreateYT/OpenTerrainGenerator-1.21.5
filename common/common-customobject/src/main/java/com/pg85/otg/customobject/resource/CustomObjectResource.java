package com.pg85.otg.customobject.resource;

import com.pg85.otg.config.biome.BiomeResourceBase;
import com.pg85.otg.customobject.CustomObject;
import com.pg85.otg.customobject.CustomObjectManager;
import com.pg85.otg.customobject.config.CustomObjectResourcesManager;
import com.pg85.otg.customobject.structures.CustomStructureCache;
import com.pg85.otg.config.settings.biome.BiomeSettings;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IModLoadedChecker;
import com.pg85.otg.interfaces.IWorldGenRegion;
import com.pg85.otg.util.helpers.StringHelper;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CustomObjectResource extends BiomeResourceBase implements ICustomObjectResource
{	
	private volatile List<CustomObject> objects;
	private final List<String> objectNames = new ArrayList<>();

	public CustomObjectResource(BiomeSettings biomeConfig, List<String> args) {
		super(biomeConfig, args);
		if (args.isEmpty() || (args.size() == 1 && args.get(0).trim().isEmpty()))
		{
			// Backwards compatibility
			args = new ArrayList<>();
			args.add("UseWorld");
		}
        this.objectNames.addAll(args);
	}
	
	@Override
	public void spawnForChunkDecoration(CustomStructureCache structureCache, IWorldGenRegion worldGenRegion, Random random, Path otgRootFolder, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		for (CustomObject object : getObjects(worldGenRegion.getPresetFolderName(), otgRootFolder, customObjectManager, materialReader, manager, modLoadedChecker))
		{
			if(object != null) // if null then BO2/BO3 file could not be found
			{
				object.process(structureCache, worldGenRegion, random);
			}
		}
	}	
	
	private List<CustomObject> getObjects(String presetFolderName, Path otgRootFolder,  CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		List<CustomObject> result = this.objects;
		if (result == null) {
			synchronized (this) {
				result = this.objects;
				if (result == null) {
					result = new ArrayList<>();
					for (String objectName : this.objectNames) {
						CustomObject obj = customObjectManager.getGlobalObjects().getObjectByName(objectName, presetFolderName, otgRootFolder, customObjectManager, materialReader, manager, modLoadedChecker);
						if (obj != null) {
							result.add(obj);
						}
					}
					this.objects = List.copyOf(result);
					result = this.objects;
				}
			}
		}
		return result;
	}
	
	@Override
	public String toString()
	{
		return "CustomObject(" + StringHelper.join(this.objectNames, ",") + ")";
	}
}
