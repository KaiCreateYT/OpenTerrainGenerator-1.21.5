package com.pg85.otg.loader;

import com.pg85.otg.config.biome.BiomeConfig;
import com.pg85.otg.config.biome.BiomeTemplate;
import com.pg85.otg.config.io.FileSettingsReader;
import com.pg85.otg.config.io.FileSettingsWriter;
import com.pg85.otg.config.io.SettingsMap;
import com.pg85.otg.config.preset.DimensionPresetConfig;
import com.pg85.otg.config.settings.biome.BiomeSettings;
import com.pg85.otg.config.settings.biome.OutdatedSettings;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.util.OTGLog;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class searches for the appropriate file for each biome.
 * You give it a list of folders to search in, and it will find the location of
 * all BiomeConfigs. Files will be created for non-existing BiomeConfigs.
 * 
 */
public final class BiomeConfigLoader {
	// >> Biome Extensions & Related
	public static final Collection<String> BiomeConfigExtensions = Arrays.asList(
		"BiomeConfig.ini",
		".biome",
		".bc",
		".bc.ini",
		".biome.ini"
	);

	public static final Collection<String> BiomeTemplateExtensions = Arrays.asList(
		".bct",
		".bt"
	);

	public enum BiomeSettingType {
		CONFIG,
		TEMPLATE
	};

	private BiomeConfigLoader() { }

	/**
	 * Finds the biomes in the given directories.
	 *
	 * @param directory The directory to search for biomes in.
	 * @param type The type of biome settings file to read
	 * @return A map of biome name --> location on disk.
	 */
	public static Map<String, SettingsMap> readAllBiomeFiles(File directory, BiomeSettingType type)
	{
		Map<String, SettingsMap> biomeSettingsStore = new HashMap<>();

		// Account for the possibility that folder creation failed
		if (directory.exists())
		{
			readBiomeFilesRecursive(biomeSettingsStore, directory, type);
		} else {
			OTGLog.error(LogCategory.CONFIGS, "Biome directory {} does not exist.", directory);
		}

		return biomeSettingsStore;
	}

	/**
	 * Loads the biomes from the given directory.
	 * 
	 * @param biomeSettingsStore Map to store all the found biome configs in.
	 * @param directory		 The directory to load from.
	 */
	private static void readBiomeFilesRecursive(Map<String, SettingsMap> biomeSettingsStore, File directory, BiomeSettingType type)
	{
		for (File file : Objects.requireNonNull(directory.listFiles()))
		{
			// Search recursively
			if (file.isDirectory())
			{
				readBiomeFilesRecursive(biomeSettingsStore, file, type);
				continue;
			}
			// Which type of biome settings are we reading?
			switch (type) {
				case CONFIG -> {
					String biomeName = toBiomeName(file);
					if (biomeName == null) {
						continue;
					}
					File renamedFile = renameBiomeFile(file, biomeName);
					SettingsMap settings = FileSettingsReader.read(biomeName, renamedFile);
					biomeSettingsStore.put(biomeName, settings);
				}
                case TEMPLATE -> {
					String templateName = toTemplateName(file);
					if (templateName == null) {
						continue;
					}
					SettingsMap settings = FileSettingsReader.read(templateName, file);
					biomeSettingsStore.put(templateName, settings);
                }
            }
		}
	}


	/**
	 * Tries to rename the config file so that it has the correct extension.
	 * Does nothing if the config file already has the correct extension. If
	 * the rename fails, a message is printed.
	 *
	 * @param toRename  The file that should be renamed.
	 * @param biomeName The biome that the file has settings for.
	 * @return The renamed file.
	 */
	private static File renameBiomeFile(File toRename, String biomeName)
	{
		String preferredFileName = BiomeConfigLoader.toFileName(biomeName);
		if (toRename.getName().equalsIgnoreCase(preferredFileName))
		{
			// No need to rename
			return toRename;
		}

		// Wrong extension, rename
		File newFile = new File(toRename.getParentFile(), preferredFileName);
		if (toRename.renameTo(newFile))
		{
			return newFile;
		} else {
			OTGLog.error(LogCategory.CONFIGS, "Failed to rename biome file {} to {}", toRename.getAbsolutePath(), newFile.getAbsolutePath());
			return toRename;
		}
	}

	/**
	 * Extracts the biome name out of the file name.
	 * 
	 * @param file The file to extract the biome name out of.
	 * @return The biome name, or null if the file is not a biome config file.
	 */
	private static String toBiomeName(File file)
	{
		return checkExtension(file, BiomeConfigExtensions);
	}

	/**
	 * Extracts the template name out of the file name
	 *
	 * @param file The file to extract the template name out of
	 * @return The template name, or null if the file is not a biome template file
	 */
	private static String toTemplateName(File file)
	{
		return checkExtension(file, BiomeTemplateExtensions);
	}

	private static String checkExtension(File file, Collection<String> extensions)
	{
		String fileName = file.getName();
		for (String extension : extensions)
		{
			if (fileName.endsWith(extension))
			{
				return fileName.substring(0, fileName.lastIndexOf(extension));
			}
		}

		// Invalid file name
		return null;
	}

	/**
	 * Gets the name of the file the biome should be saved in. This will use
	 * the extension as defined in the PluginConfig.ini file.
	 * 
	 * @param biomeName The biome.
	 * @return The name of the file the biome should be saved in.
	 */
	private static String toFileName(String biomeName)
	{
		return biomeName + Constants.BiomeConfigFileExtension;
	}

	public static File getBiomeDirectory(Path presetDir) {
		File biomesDirectory = new File(presetDir.toString(), Constants.BIOMES_FOLDER);
        if (biomesDirectory.exists()) {
            return biomesDirectory;
        } else {
			return new File(presetDir.toString(), Constants.LEGACY_WORLD_BIOMES_FOLDER);
		}
	}

	public static List<BiomeTemplate> loadBiomeTemplates(Path presetDir, DimensionPresetConfig presetConfig) {
		List<BiomeSettings> list = loadBiomeSettings(presetDir, presetConfig, BiomeSettingType.TEMPLATE);
		return list.stream().map(t -> (BiomeTemplate) t).toList();
	}

	/**
	 * Loads the biome configs from the given preset directory. May return biome templates if legacy TemplateForBiome setting is set.
	 */
	public static List<BiomeSettings> loadBiomeConfigs(Path presetDir, DimensionPresetConfig presetConfig) {
		return loadBiomeSettings(presetDir, presetConfig, BiomeSettingType.CONFIG);
	}

	public static List<BiomeSettings> loadBiomeSettings(Path presetDir, DimensionPresetConfig presetConfig, BiomeSettingType type)
	{
		File biomesDirectory = getBiomeDirectory(presetDir);
		// Load all files
        Map<String, SettingsMap> biomeConfigStore = readAllBiomeFiles(biomesDirectory, type);

		// Read all settings
		List<BiomeSettings> biomeSettings = readSettings(presetConfig, biomeConfigStore, type);

		// Write normalized settings back to disk (adds new settings to old configs)
		writeSettings(biomeSettings, presetConfig);

		// Update settings dynamically, these changes don't get written back to the file
		processSettings(presetConfig, biomeSettings);

		ILogger logger = OTGLog.getLogger();
		if (logger.isEnabled(LogLevel.INFO, LogCategory.CONFIGS) && logger.canLogForPreset(presetDir.getFileName().toString()))
		{
			logger.info(LogCategory.CONFIGS, "{} {} loaded for preset {}",
				biomeSettings.size(),
				type == BiomeSettingType.CONFIG ? "biome configs" : "biome templates",
				presetConfig.getConfigName()
			);
			if (!biomeSettings.isEmpty()) {
				logger.info(LogCategory.CONFIGS,
						biomeSettings.stream().map(
								item -> item.getIdentitySettings().getBiomeName()
						).collect(
								Collectors.joining(", ")
						)
				);
			}
		}
		return biomeSettings;
	}


	private static ArrayList<BiomeSettings> readSettings(
			DimensionPresetConfig presetConfig,
			Map<String, SettingsMap> biomeSettingStore,
			BiomeSettingType type
	) {
		ArrayList<BiomeSettings> biomeSettingList = new ArrayList<>();

		for (SettingsMap settingsMap : biomeSettingStore.values())
		{
			switch (type) {
                case CONFIG -> {
					Path configPath = settingsMap.getPath();
					boolean isTemplatePath = configPath != null &&
						configPath.toString().replace('\\', '/').contains("/templates/");
					boolean hasTemplate = settingsMap.hasSetting(OutdatedSettings.IS_TEMPLATE_FOR_BIOME);
					boolean isTemplate = isTemplatePath ||
						(hasTemplate && settingsMap.getSetting(OutdatedSettings.IS_TEMPLATE_FOR_BIOME));
					if (isTemplate) {
						if (!hasTemplate) {
							OTGLog.warn(LogCategory.CONFIGS,
								"File {} missing TemplateForBiome setting, inferred from templates/ directory",
								configPath);
						}
						biomeSettingList.add(new BiomeTemplate(settingsMap, presetConfig));
					} else {
						biomeSettingList.add(new BiomeConfig(settingsMap, presetConfig));
					}
                }
                case TEMPLATE -> {
					BiomeTemplate biomeTemplate = new BiomeTemplate(settingsMap, presetConfig);
					biomeSettingList.add(biomeTemplate);
                }
                default -> OTGLog.warn("Could not read; unknown setting type: "+settingsMap.getName());
            }
        }

		return biomeSettingList;
	}

	private static void writeSettings(
			List<BiomeSettings> biomeSettings,
			DimensionPresetConfig presetConfig
	) {
		for (BiomeSettings settings : biomeSettings) {
			SettingsMap updatedMap = settings.getSettingsAsMap();
			Path writeFile = updatedMap.getPath();
			if (writeFile != null) {
				FileSettingsWriter.writeToFile(updatedMap, writeFile.toFile(), presetConfig.getPresetInfo().getSettingsMode());
			}
		}
	}

	private static void processSettings(DimensionPresetConfig presetConfig, List<BiomeSettings> biomeSettingMaps)
	{
		for(BiomeSettings biomeSettings : biomeSettingMaps)
		{
			// Index ReplacedBlocks
			if (!presetConfig.isBiomeConfigsHaveReplacement())
			{
				presetConfig.setBiomeConfigsHaveReplacement(biomeSettings.getSurfaceSettings().getReplacedBlocks().hasReplaceSettings());
			}

			// Index maxSmoothRadius
			if (presetConfig.getMaxSmoothRadius() < biomeSettings.getTerrainSettings().getSmoothRadius())
			{
				presetConfig.setMaxSmoothRadius(biomeSettings.getTerrainSettings().getSmoothRadius());
			}
			if (presetConfig.getMaxSmoothRadius() < biomeSettings.getTerrainSettings().getCHCSmoothRadius())
			{
				presetConfig.setMaxSmoothRadius(biomeSettings.getTerrainSettings().getCHCSmoothRadius());
			}
		}
	}
}
