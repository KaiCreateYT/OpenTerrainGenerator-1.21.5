package com.pg85.otg.util.biome;

import java.nio.file.Path;
import java.util.Locale;

import com.pg85.otg.interfaces.IBiomeResourceLocation;

public class OTGBiomeResourceLocation implements IBiomeResourceLocation
{
	private static final String BIOME_RESOURCE_LOCATION_SEPARATOR = ".";

	private final String presetFolder;
	private final String presetRegistryName;
	private final String biomeName;
	private final String resourceName;
	private final String resourceLocationString;

	public OTGBiomeResourceLocation(Path presetFolder, String presetRegistryName, String biomeName, String resourceName)
	{
		this.presetFolder = presetFolder.toFile().getName();
		String presetShortName = presetRegistryName != null && !presetRegistryName.trim().isEmpty() ? presetRegistryName : this.presetFolder;
		this.presetRegistryName = stringToPath(presetShortName);
		this.biomeName = stringToPath(biomeName);
		this.resourceName = stringToPath(resourceName);
		if (resourceName == null) {
			resourceLocationString = String.format("%s%s%s", getResourceDomain(), ":", getResourcePath());
		} else {
			resourceLocationString = String.format("%s%s%s%s%s", getResourceDomain(), ":", getResourcePath(), BIOME_RESOURCE_LOCATION_SEPARATOR, resourceName);
		}
	}

	public OTGBiomeResourceLocation(Path presetFolder, String presetRegistryName, String biomeName)
	{
		this(presetFolder, presetRegistryName, biomeName, null);
	}

	public static String stringToPath(String input) {
		if (input == null) {
			return null;
		}
		return input
				.replaceAll("([a-z])([A-Z])", "$1_$2") // snake_case from CamelCase
				.toLowerCase(Locale.ROOT) // All registry keys must be lower case
				.replaceAll(" ", "_") // replace spaces with underscores
				.replaceAll("\\+", "plus")
				.replaceAll("[^a-z0-9_\\-/.]", "");
	}

	public static String addSpaceToCamelCase(String input) {
		if (input == null) {
			return null;
		}
		//capitalize first letter and add space before each capital letter after a lower case letter
		return (input.substring(0, 1).toUpperCase() + input.substring(1))
				.replaceAll("([a-z])([A-Z])", "$1 $2");
	}

	@Override
	public String getPresetFolderName()
	{
		return this.presetFolder;
	}
	
	@Override
	public String toResourceLocationString()
	{
		return resourceLocationString;
	}
	
	private String getResourceDomain()
	{
		return presetRegistryName;
	}

	private String getResourcePath()
	{
		if(this.resourceName != null)
		{
			return String.format("%s%s%s", this.biomeName, BIOME_RESOURCE_LOCATION_SEPARATOR, this.resourceName);
		} else {			
			return this.biomeName;
		}
	}
	
	@Override
	public boolean equals(Object other)
	{
		if (this == other)
		{
			return true;
		}
		if (!(other instanceof OTGBiomeResourceLocation))
		{
			return false;
		}
		return ((OTGBiomeResourceLocation)other).toResourceLocationString().equals(this.toResourceLocationString());
	}

	@Override
	public int hashCode()
	{
		return toResourceLocationString().hashCode();
	}	
}
