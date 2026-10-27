package com.pg85.otg.config;

import com.pg85.otg.config.io.SettingsMap;
import com.pg85.otg.config.settings.preset.DimensionPresetInfo;
import com.pg85.otg.config.standard.PluginConfigStandardValues;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.util.OTGLog;

import java.nio.file.Path;

/**
 * OTG.ini / PluginConfig classes
 * 
 * IPluginConfig defines anything that's used/exposed between projects.
 * PluginConfigBase implements anything needed for IPresetConfig. 
 * PluginConfig contains only fields/methods used for io/serialisation/instantiation.
 * 
 * PluginConfig should be used only in common-core and platform-specific layers, when reading/writing settings on app start.
 * IPluginConfig should be used wherever settings are used in code.
 */
public final class PluginConfig extends PluginConfigBase
{
	private final Path path;

	public PluginConfig(SettingsMap settingsReader, Path path)
	{
		super(settingsReader.getName());
		this.path = path;
		readConfigSettings(settingsReader, OTGLog.getLogger());
	}

	@Override
	public void renameOldSettings(SettingsMap reader) { }

	private void readConfigSettings(SettingsMap reader, ILogger logger)
	{
		this.settingsMode = reader.getSetting(DimensionPresetInfo.SETTINGS_MODE);
		this.logLevel = reader.getSetting(PluginConfigStandardValues.LOG_LEVEL);
		this.logCustomObjects = reader.getSetting(PluginConfigStandardValues.LOG_CUSTOM_OBJECTS);
		this.logStructurePlotting = reader.getSetting(PluginConfigStandardValues.LOG_BO4_PLOTTING);
		this.logConfigs = reader.getSetting(PluginConfigStandardValues.LOG_CONFIGS);
		this.logBiomeRegistry = reader.getSetting(PluginConfigStandardValues.LOG_BIOME_REGISTRY);
		this.logPerformance = reader.getSetting(PluginConfigStandardValues.LOG_PERFORMANCE);
		this.logDecoration = reader.getSetting(PluginConfigStandardValues.LOG_DECORATION);
		this.logMobs = reader.getSetting(PluginConfigStandardValues.LOG_MOBS);
		this.logPresets = reader.getSetting(PluginConfigStandardValues.LOG_PRESETS);
		this.decorationEnabled = reader.getSetting(PluginConfigStandardValues.DECORATION_ENABLED);
		this.developerMode = reader.getSetting(PluginConfigStandardValues.DEVELOPER_MODE);
		this.workerThreads = reader.getSetting(PluginConfigStandardValues.WORKER_THREADS);
	}

	@Override
	public Path getConfigPath() {
		return path;
	}

	@Override
	public void writeConfigSettings(SettingsMap writer)
	{
		writer.header1("Open Terrain Generator Config");
		writer.putSetting(Constants.ConfigVersionSetting, Constants.ConfigVersion);

		writer.putSetting(DimensionPresetInfo.SETTINGS_MODE, this.settingsMode,
			"Possible Config Write Modes:",
			"WriteAll			 - Write config files with help comments.",
			"WriteWithoutComments - Write config files without help comments.",
			"WriteDisable		 - Don't write config files, read-only.",
			"Defaults to: WriteAll",
			"Writing updates your configs to the currently installed version of OTG."
		);

		writer.putSetting(PluginConfigStandardValues.WORKER_THREADS, this.workerThreads,
			"Number of worker threads for parallel chunk generation.",
			"Higher values may improve performance on multi-core CPUs.",
			"Set to 0 to disable (single-threaded).",
			"Default: min(4, CPU cores - 1). Max: CPU cores - 1."
		);
		
		writer.header2("Logging");

		writer.putSetting(PluginConfigStandardValues.LOG_LEVEL, this.logLevel,
			"Possible Log Levels",
			"Off			- Shows FATAL and ERROR logs.",
			"Quiet		- Shows FATAL, ERROR and WARNING logs.",
			"Standard	- Shows FATAL, ERROR, WARNING and INFO logs.",
			"",
			"Defaults to: Standard"
		);

		writer.putSetting(PluginConfigStandardValues.LOG_CUSTOM_OBJECTS, this.logCustomObjects,
			"Logs information about BO2/BO3/BO4 config errors and spawning.",
			"Defaults to: false"
		);

		writer.putSetting(PluginConfigStandardValues.LOG_BO4_PLOTTING, this.logStructurePlotting,
			"Logs information about BO4 customstructures plotting branches.",
			"Defaults to: false"
		);		

		writer.putSetting(PluginConfigStandardValues.LOG_CONFIGS, this.logConfigs,
			"Logs information about invalid settings in configs.",
			"Defaults to: false"
		);

		writer.putSetting(PluginConfigStandardValues.LOG_BIOME_REGISTRY, this.logBiomeRegistry,
			"Logs information about biome registration.",
			"Defaults to: false"
		);		

		writer.putSetting(PluginConfigStandardValues.LOG_DECORATION, this.logDecoration,
			"Logs information about resources spawned during decoration.",
			"Defaults to: false"
		);

		writer.putSetting(PluginConfigStandardValues.LOG_MOBS, this.logMobs,
			"Logs information about mob config errors and spawning.",
			"Defaults to: false"
		);

		writer.putSetting(PluginConfigStandardValues.LOG_PRESETS, this.logPresets,
			"Set the name of a preset or \"all\" to log warnings and errors",
			"for specified presets only.",
			"Default to: all"
		);

		writer.putSetting(PluginConfigStandardValues.LOG_PERFORMANCE, this.logPerformance,
			"Logs information about any feature that is taking more than 50 milliseconds.",
			"Includes: base terrain gen, decoration, resources, bo4 plotting, bo3/bo4 spawning.",
			"Use this to find performance bottlenecks and optimise your world.",
			"Defaults to: false"
		);		
		
		writer.header2("Developer settings");

		writer.putSetting(PluginConfigStandardValues.DECORATION_ENABLED, this.decorationEnabled,
			"Set this to false to disable chunk decoration and generate only base terrain.",
			"Defaults to: true"
		);
		
		writer.putSetting(PluginConfigStandardValues.DEVELOPER_MODE, this.developerMode,
			"Clears the BO2/BO3 cache and reloads DimensionPresetConfig/BiomeConfigs on exit/rejoin.",
			"Use this if you're creating a preset and want to do trial/error quickly.",
			"Defaults to: false"
		);
	}
}
