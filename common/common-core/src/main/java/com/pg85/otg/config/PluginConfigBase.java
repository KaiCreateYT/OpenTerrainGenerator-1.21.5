package com.pg85.otg.config;

import com.pg85.otg.constants.settings.ConfigMode;
import com.pg85.otg.constants.settings.LogLevels;
import com.pg85.otg.interfaces.IPluginConfig;
import com.pg85.otg.util.logging.LogCategory;

import java.util.EnumSet;

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
public abstract class PluginConfigBase implements IPluginConfig, ConfigFile {
	protected LogLevels logLevel;
	protected ConfigMode settingsMode;
	protected int workerThreads;
	protected boolean developerMode;
	protected boolean logCustomObjects;
	protected boolean logStructurePlotting;
	protected boolean logConfigs;
	protected boolean logPerformance;	
	protected boolean logDecoration;
	protected boolean logBiomeRegistry;
	protected boolean decorationEnabled;
	protected boolean logMobs;
	protected String logPresets;
	protected String configName;
	
	public PluginConfigBase(String configName)
	{
		this.configName = configName;
	}

	@Override
	public String getConfigName() {
		return configName;
	}

	@Override
	public LogLevels getLogLevel()
	{
		return this.logLevel;
	}
	
	@Override
	public int getMaxWorkerThreads()
	{
		return this.workerThreads;
	}

	@Override
	public boolean getDeveloperModeEnabled()
	{
		return this.developerMode;
	}

	@Override
	public boolean logCustomObjects()
	{
		return this.logCustomObjects;
	}
	
	@Override
	public boolean logStructurePlotting()
	{
		return this.logStructurePlotting;
	}
	
	@Override
	public boolean logConfigs()
	{
		return this.logConfigs;
	}	

	@Override
	public boolean logDecoration()
	{
		return this.logDecoration;
	}

	@Override
	public boolean logBiomeRegistry()
	{
		return this.logBiomeRegistry;
	}
	
	@Override
	public boolean logPerformance()
	{
		return this.logPerformance;
	}	

	@Override
	public boolean getDecorationEnabled()
	{
		return this.decorationEnabled;
	}

	@Override
	public boolean logMobs()
	{
		return this.logMobs;
	}

	@Override
	public String logPresets()
	{
		return this.logPresets;
	}
	
	@Override
	public ConfigMode getSettingsMode()
	{
		return this.settingsMode;
	}

	public EnumSet<LogCategory> getEnabledLogCategories() {
		EnumSet<LogCategory> categories = EnumSet.of(LogCategory.MAIN);
		if (logCustomObjects) categories.add(LogCategory.CUSTOM_OBJECTS);
		if (logStructurePlotting) categories.add(LogCategory.STRUCTURE_PLOTTING);
		if (logConfigs) categories.add(LogCategory.CONFIGS);
		if (logBiomeRegistry) categories.add(LogCategory.BIOME_REGISTRY);
		if (logPerformance) categories.add(LogCategory.PERFORMANCE);
		if (logDecoration) categories.add(LogCategory.DECORATION);
		if (logMobs) categories.add(LogCategory.MOBS);
		return categories;
	}
}
