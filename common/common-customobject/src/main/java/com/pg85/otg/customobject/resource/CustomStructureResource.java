package com.pg85.otg.customobject.resource;

import com.pg85.otg.config.biome.BiomeResourceBase;
import com.pg85.otg.customobject.CustomObject;
import com.pg85.otg.customobject.CustomObjectManager;
import com.pg85.otg.customobject.config.CustomObjectResourcesManager;
import com.pg85.otg.customobject.structures.StructuredCustomObject;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.config.settings.biome.BiomeSettings;
import com.pg85.otg.interfaces.ICustomObjectManager;
import com.pg85.otg.interfaces.ICustomObjectResourcesManager;
import com.pg85.otg.interfaces.ICustomStructureGen;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IModLoadedChecker;
import com.pg85.otg.interfaces.IStructuredCustomObject;
import com.pg85.otg.util.gen.OTGWorldInfo;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


public class CustomStructureResource extends BiomeResourceBase implements ICustomStructureResource, ICustomStructureGen
{
	private final List<Double> objectChances;
	public final List<String> objectNames;

	public CustomStructureResource(BiomeSettings biomeConfig, List<String> args) throws InvalidConfigException
	{
		super(biomeConfig, args);
		this.objectNames = new ArrayList<>();
		this.objectChances = new ArrayList<>();
		for (int i = 0; i < args.size() - 1; i += 2)
		{
			this.objectNames.add(args.get(i));
			this.objectChances.add(readRarity(args.get(i + 1)));
		}
		biomeConfig.setStructureGen(this);
	}

	@Override
	public Double getObjectChance(int i)
	{
		return this.objectChances.size() < i + 1 ? null : this.objectChances.get(i);
	}
	
	@Override
	public String getObjectName(int i)
	{
		return this.objectNames.size() < i + 1 ? null : this.objectNames.get(i);
	}

	@Override
	public String[] getObjectNames() {
		return objectNames.toArray(new String[0]);
	}

	@Override
	public Double[] getObjectChances() {
		return objectChances.toArray(new Double[0]);
	}

	@Override
	public boolean isEmpty()
	{
		return this.objectNames.isEmpty();
	}
	
	@Override
	public List<IStructuredCustomObject> getObjects(String presetFolderName, Path otgRootFolder,  ICustomObjectManager customObjectManager, IMaterialReader materialReader, ICustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		List<IStructuredCustomObject> objects = new ArrayList<>();
		if(!this.objectNames.isEmpty())
		{
            for (String objectName : this.objectNames) {
                // TODO: Refactor this so we don't have to cast CustomObjectManager/CustomObjectResourcesManager :(
                // TODO: Remove any dependency on common-customobjects, interfaces only?
                CustomObject object = ((CustomObjectManager) customObjectManager)
					 .getGlobalObjects()
					 .getObjectByName(
						 objectName,
						 presetFolderName,
						 otgRootFolder,
						 (CustomObjectManager) customObjectManager,
						 materialReader,
						 (CustomObjectResourcesManager) manager,
						 modLoadedChecker
					 );
                objects.add((StructuredCustomObject) object);
            }
		}
		return objects;
	}	
	
	@Override
	public String toString()
	{
		if (objectNames.isEmpty())
		{
			return "CustomStructure()";
		}
		StringBuilder output = new StringBuilder("CustomStructure(" + objectNames.get(0) + "," + objectChances.get(0));
		for (int i = 1; i < objectNames.size(); i++)
		{
			output.append(",").append(objectNames.get(i)).append(",").append(objectChances.get(i));
		}
		return output + ")";
	}
}
