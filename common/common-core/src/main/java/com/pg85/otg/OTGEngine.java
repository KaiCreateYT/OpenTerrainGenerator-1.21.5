package com.pg85.otg;

import com.pg85.otg.config.PluginConfig;
import com.pg85.otg.config.io.FileSettingsReader;
import com.pg85.otg.config.io.FileSettingsWriter;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.customobject.CustomObjectManager;
import com.pg85.otg.customobject.config.CustomObjectResourcesManager;
import com.pg85.otg.customobject.structures.CustomStructureCache;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IModLoadedChecker;
import com.pg85.otg.interfaces.IPluginConfig;
import com.pg85.otg.presets.LocalDimensionPresetLoader;
import com.pg85.otg.util.BundledPresetSync;
import com.pg85.otg.util.OTGMaterialReader;
import com.pg85.otg.util.logging.LogCategory;
import lombok.Getter;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

/**
 * Implemented and provided by the platform-specific layer on app start and accessed via OTG.startEngine()/OTG.getEngine(),
 * this class holds any objects and methods used during an app session.
 * 
 * Constructor parameters are platform-specific implementations of wrapper classes, such as a logger, material reader, 
 * preset loader etc. Implement these to provide support for a platform (Forge, Spigot etc).
 *  
 * OTGEngine.onStart() should be called on mod/plugin start, creates all OTG files and folders and registers all presets 
 * and biomes via the platform-specific preset loader provided as a constructor parameter. 
 */
public abstract class OTGEngine
{
	// Classes implemented/provided by the platform-specific layer.
	
	@Getter
    protected final LocalDimensionPresetLoader dimensionPresetLoader;
	@Getter
    protected final ILogger logger;
	@Getter
    private final IModLoadedChecker modLoadedChecker;

	// Common classes
	
	private final Path otgRootFolder;
	@Getter
    private final Path globalObjectsFolder;
	protected PluginConfig pluginConfig;

    // Create manager objects

    @Getter
    private final CustomObjectResourcesManager customObjectResourcesManager = new CustomObjectResourcesManager();
	@Getter
    private CustomObjectManager customObjectManager;
	
	protected OTGEngine(ILogger logger, Path otgRootFolder, IModLoadedChecker modLoadedChecker, LocalDimensionPresetLoader dimensionPresetLoader)
	{
		this.logger = logger;
		this.otgRootFolder = otgRootFolder;
		this.globalObjectsFolder = otgRootFolder.resolve(Constants.GLOBAL_OBJECTS_FOLDER);
		this.dimensionPresetLoader = dimensionPresetLoader;
		this.modLoadedChecker = modLoadedChecker;
	}
	
	// Get jar file that's running OTG, where we will find our default preset
	public abstract File getJarFileFromModLoader();

	/**
	 * All filesystem locations (JAR files and/or exploded directories) that may contain bundled
	 * {@code resources/DimensionPresets} and {@code resources/WorldPresets}. Override on loaders
	 * that expose multiple roots (e.g. Fabric).
	 */
	protected List<Path> getModBundledResourceRoots() {
		File f = getJarFileFromModLoader();
		if (f == null) {
			return Collections.emptyList();
		}
		return Collections.singletonList(f.toPath());
	}
	
	// Startup / shutdown

	public void onStart()
	{
		// Load plugin config

		File pluginConfigFile = Paths.get(getOTGRootFolder().toString(), Constants.PluginConfigFilename).toFile();
		this.pluginConfig = new PluginConfig(
				FileSettingsReader.read(Constants.PluginConfigFilename, pluginConfigFile),
				pluginConfigFile.toPath()
		);
		this.logger.init(
			this.pluginConfig.getLogLevel().getLevel(),
			this.pluginConfig.getEnabledLogCategories(),
			this.pluginConfig.logPresets()
		);
		FileSettingsWriter.writeToFile(this.pluginConfig.getSettingsAsMap(), pluginConfigFile, this.pluginConfig.getSettingsMode());

		// Detect old folder names and warn users
		Path oldPresetsDir = Paths.get(getOTGRootFolder().toString(), "Presets");
		if (oldPresetsDir.toFile().exists() && !Paths.get(getOTGRootFolder().toString(), Constants.DIMENSION_PRESETS_FOLDER).toFile().exists()) {
			this.logger.error(LogCategory.MAIN, "==============================================");
			this.logger.error(LogCategory.MAIN, "BREAKING CHANGE: 'Presets/' has been renamed to 'DimensionPresets/'");
			this.logger.error(LogCategory.MAIN, "Please rename your Presets folder to DimensionPresets");
			this.logger.error(LogCategory.MAIN, "==============================================");
		}

		Path oldDimConfigsDir = Paths.get(getOTGRootFolder().toString(), "DimensionConfigs");
		if (oldDimConfigsDir.toFile().exists() && !Paths.get(getOTGRootFolder().toString(), Constants.WORLD_PRESETS_FOLDER).toFile().exists()) {
			this.logger.error(LogCategory.MAIN, "==============================================");
			this.logger.error(LogCategory.MAIN, "BREAKING CHANGE: 'DimensionConfigs/' has been renamed to 'WorldPresets/'");
			this.logger.error(LogCategory.MAIN, "Please rename your DimensionConfigs folder to WorldPresets");
			this.logger.error(LogCategory.MAIN, "==============================================");
		}

		// Create OTG folders

		File presetsDir = Paths.get(getOTGRootFolder().toString(), Constants.DIMENSION_PRESETS_FOLDER).toFile();
		if(!presetsDir.exists())
		{
			presetsDir.mkdirs();
		}

		File dimensionConfigsDir = Paths.get(getOTGRootFolder().toString(), Constants.WORLD_PRESETS_FOLDER).toFile();
		if(!dimensionConfigsDir.exists())
		{
			dimensionConfigsDir.mkdirs();
		}

		File globalObjectsDir = this.globalObjectsFolder.toFile();
		if(!globalObjectsDir.exists())
		{
			globalObjectsDir.mkdirs();
		}

		BundledPresetSync.syncBundledResourcesIfStale(getModBundledResourceRoots(), getOTGRootFolder(), this.logger);

        this.customObjectManager = new CustomObjectManager(
			getPluginConfig().getDeveloperModeEnabled(),
			this.otgRootFolder, 
			getPresetsDirectory(), 
			this.customObjectResourcesManager
		);

		// Load presets

		this.dimensionPresetLoader.loadDimensionPresetsFromDisk();
	}

	public void onShutdown()
	{
		// Shutdown all loaders
		this.customObjectManager.shutdown();
	}

    // OTG Configs
	
	public IPluginConfig getPluginConfig()
	{
		return this.pluginConfig;
	}

	// OTG dirs

	public Path getOTGRootFolder()
	{
		return this.otgRootFolder;
	}

    public Path getPresetsDirectory()
	{
		return Paths.get(this.getOTGRootFolder().toString(), Constants.DIMENSION_PRESETS_FOLDER);
	}

	// Logging

    // Builders/Factories
	
	public CustomStructureCache createCustomStructureCache(String presetFolderName, Path worldSavepath, long worldSeed, boolean isBo4Enabled)
	{
		// TODO: ModLoadedChecker
		return new CustomStructureCache(
			presetFolderName, 
			worldSavepath, 
			worldSeed, 
			isBo4Enabled, 
			getOTGRootFolder(),
			getCustomObjectManager(),
			OTGMaterialReader.get(),
			getCustomObjectResourcesManager(), 
			null
		);
	}
}
