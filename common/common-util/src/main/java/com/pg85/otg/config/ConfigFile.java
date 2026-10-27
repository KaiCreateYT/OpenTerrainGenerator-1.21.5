package com.pg85.otg.config;

import com.pg85.otg.config.io.SettingsMap;
import com.pg85.otg.config.io.SimpleSettingsMap;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for all configuration files. Configuration files read
 * the desired settings from a {@link SettingsMap}, and can then write the
 * settings back to such a map.
 *
 */
public interface ConfigFile
{
	/**
	 * Gets the name of this config file. For biomes, this is the biome name, for presets it's the preset folder name, etc.
	 * @return The name of this config file.
	 */
	String getConfigName();

	/**
	 * Gets the path of this config file. For biomes, this is the path to the biome file, for presets it's the path to the preset file, etc.
	 * @return The path of this config file.
	 */
	Path getConfigPath();

	/**
	 * Gets all settings of this config file.
	 * @return All settings.
	 */
	default SettingsMap getSettingsAsMap()
	{
		SettingsMap settingsMap = new SimpleSettingsMap(getConfigName(), getConfigPath());
		writeConfigSettings(settingsMap);
		return settingsMap;
	}

	/**
	 * Methods that subclasses must override to write the actual settings.
	 * @param settingsMap The map to write the settings to.
	 */
	void writeConfigSettings(SettingsMap settingsMap);

	/**
	 * rewrite configs in old formats to the modern format, so that they can be read.
	 *
	 * @param reader The settings reader.
	 */
	void renameOldSettings(SettingsMap reader);

	static List<String> filterBiomes(List<String> biomes, List<String> customBiomes)
	{
		ArrayList<String> output = new ArrayList<String>();

		for (String key : biomes)
		{
			key = key.trim();
			if (customBiomes.contains(key))
			{
				output.add(key);
				continue;
			}

			// Previously also checked BiomeRegistryNames.Contain(key) — a 430-line switch
			// of vanilla biome names frozen at 1.16.5. Removed because it was incomplete
			// (missing 1.17+ biomes) and biomes without .bc files have no OTG ID anyway.
		}
		return output;
	}
}
