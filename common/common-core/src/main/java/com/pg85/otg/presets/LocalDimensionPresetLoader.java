package com.pg85.otg.presets;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import com.pg85.otg.config.biome.BiomeConfig;
import com.pg85.otg.config.biome.BiomeResourcesManager;
import com.pg85.otg.config.biome.BiomeTemplate;
import com.pg85.otg.config.settings.biome.BiomeSettings;
import com.pg85.otg.loader.BiomeConfigLoader;
import com.pg85.otg.config.preset.DimensionPresetConfig;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.gen.biome.layers.BiomeLayerData;
import com.pg85.otg.interfaces.IBiome;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.loader.DimensionPresetConfigLoader;
import com.pg85.otg.util.OTGLog;
import com.pg85.otg.util.OTGMaterialReader;
import com.pg85.otg.util.logging.LogCategory;

/**
 * Base class for preset loading. Loads presets from disk and provides
 * global ID mapping and generation data. Subclassed by SharedDimensionPresetBiomeLoader
 * in the shared platform module for MC-dependent biome registration.
 *
 * Not abstract — can be used directly for testing or headless operation.
 */
public class LocalDimensionPresetLoader {
    private static final int MAX_INHERITANCE_DEPTH = 15;
    protected final File presetsDir;
    protected final HashMap<String, DimensionPreset> presets = new HashMap<>();
    protected final HashMap<String, String> aliasMap = new HashMap<>();
    private Map<String, IBiome[]> globalIdMapping = new java.util.concurrent.ConcurrentHashMap<>();
    private Map<String, BiomeLayerData> presetGenerationData = new java.util.concurrent.ConcurrentHashMap<>();

    public LocalDimensionPresetLoader(Path otgRootFolder) {
        this.presetsDir = getPresetsDir(otgRootFolder).toFile();
    }

    private static Path getPresetsDir(Path otgRootFolder) {
        return Paths.get(otgRootFolder.toString(), File.separator + Constants.DIMENSION_PRESETS_FOLDER);
    }

    public IMaterialReader getMaterialReader() {
        return OTGMaterialReader.get();
    }


    public DimensionPreset getDimensionPresetByShortNameOrFolderName(String name) {
        // Example: preset is stored as "Biome Bundle v7", but also accepts "Biome Bundle"
        if (aliasMap.containsKey(name)) {
            return this.presets.get(aliasMap.get(name));
        }
        return this.presets.get(name);
    }

    public DimensionPreset getDimensionPresetByFolderName(String name) {
        return this.presets.get(name);
    }

    public ArrayList<DimensionPreset> getAllDimensionPresets() {
        return new ArrayList<DimensionPreset>(presets.values());
    }

    public Set<String> getAllDimensionPresetFolderNames() {
        return presets.keySet();
    }

    public String getDefaultDimensionPresetFolderName() {
        return this.presets.keySet().isEmpty() ? Constants.DEFAULT_PRESET_NAME
                : this.presets.containsKey(Constants.DEFAULT_PRESET_NAME)
                ? Constants.DEFAULT_PRESET_NAME
                : (String) this.presets.keySet().toArray()[0];
    }

    public void loadDimensionPresetsFromDisk() {
        // Clear existing presets and aliases before reloading (important for developer mode reload)
        this.presets.clear();
        this.aliasMap.clear();

        if (this.presetsDir.exists() && this.presetsDir.isDirectory()) {
            OTGLog.info(LogCategory.CONFIGS, "Loading presets from {}", this.presetsDir);
            loadPresetsFromParentDirectory(this.presetsDir);
        } else {
            OTGLog.info(LogCategory.CONFIGS, "No presets directory at {}", this.presetsDir);
        }

        if (this.presets.isEmpty() && this.presetsDir.getParentFile() != null) {
            File legacyDir = new File(this.presetsDir.getParentFile(), "Presets");
            if (legacyDir.isDirectory()) {
                OTGLog.info(LogCategory.CONFIGS,
                        "No presets in {}; loading from legacy Presets/ (OTG 1.16-style layout)", this.presetsDir);
                loadPresetsFromParentDirectory(legacyDir);
            }
        }

        if (this.presets.isEmpty()) {
            OTGLog.info(LogCategory.CONFIGS, "No presets found in {} (or legacy Presets/)", this.presetsDir);
        }
    }

    /**
     * Loads every subdirectory of {@code parentOfPresetFolders} that contains a dimension preset config
     * ({@link Constants#DIMENSION_PRESET_CONFIG_FILE} or legacy world config).
     */
    private void loadPresetsFromParentDirectory(File parentOfPresetFolders) {
        if (!parentOfPresetFolders.isDirectory()) {
            return;
        }
        File[] presetDirs = parentOfPresetFolders.listFiles();
        if (presetDirs == null) {
            return;
        }
        for (File presetDir : presetDirs) {
            if (!presetDir.isDirectory()) {
                continue;
            }
            File[] files = presetDir.listFiles();
            if (files == null) {
                continue;
            }
            for (File file : files) {
                if (file.getName().equals(Constants.DIMENSION_PRESET_CONFIG_FILE)
                        || file.getName().equals(Constants.LEGACY_WORLD_CONFIG_FILE)) {
                    DimensionPreset preset = loadPreset(presetDir.toPath());
                    if (this.aliasMap.containsKey(preset.getRegistryName())) {
                        OTGLog.error(LogCategory.MAIN,
                                "Duplicate preset registry name found: {}. DimensionPreset {} will be ignored.",
                                preset.getRegistryName(), preset.getFolderName());
                    } else {
                        this.presets.put(preset.getFolderName(), preset);
                        this.aliasMap.put(preset.getRegistryName(), preset.getFolderName());
                    }
                    break;
                }
            }
        }
    }

    public static DimensionPreset loadPreset(Path presetDir) {
        DimensionPresetConfig presetConfig = DimensionPresetConfigLoader.loadPresetConfig(presetDir);
        List<BiomeTemplate> biomeTemplatesImmutable = BiomeConfigLoader.loadBiomeTemplates(presetDir, presetConfig);
        List<BiomeSettings> biomeSettingsImmutable = BiomeConfigLoader.loadBiomeConfigs(presetDir, presetConfig);
        List<BiomeTemplate> biomeTemplates = new ArrayList<>(biomeTemplatesImmutable);
        List<BiomeConfig> biomeConfigs = new ArrayList<>();
        biomeSettingsImmutable.forEach(bs -> {
            if (bs instanceof BiomeTemplate bt) biomeTemplates.add(bt);
            if (bs instanceof BiomeConfig bc) biomeConfigs.add(bc);
        });

        return new DimensionPreset(presetDir, presetConfig, biomeConfigs, biomeTemplates);
    }

    public static List<DimensionPreset> loadDimensionPresetsFromDisk(Path otgRootFolder) {
        Path presetsDir = getPresetsDir(otgRootFolder);
        if (!presetsDir.toFile().exists()) {
            OTGLog.info(LogCategory.CONFIGS, "No presets found in {}", presetsDir);
            return Collections.emptyList();
        }
        List<Path> presetDirectories = DimensionPresetConfigLoader.findPresetDirectories(presetsDir);
        List<DimensionPreset> presets = new ArrayList<>();
        for (Path presetDir : presetDirectories) {
            presets.add(loadPreset(presetDir));
        }
        return presets;
    }

    public IBiome[] getGlobalIdMapping(String presetFolderName) {
        return globalIdMapping.get(presetFolderName);
    }

    public Map<String, BiomeLayerData> getPresetGenerationData() {
        return this.presetGenerationData;
    }

    protected void putGlobalIdMapping(String presetFolderName, IBiome[] mapping) {
        this.globalIdMapping.put(presetFolderName, mapping);
    }

    protected void putPresetGenerationData(String presetFolderName, BiomeLayerData data) {
        this.presetGenerationData.put(presetFolderName, data);
    }

    protected void clearBiomeData() {
        this.globalIdMapping = new java.util.concurrent.ConcurrentHashMap<>();
        this.presetGenerationData = new java.util.concurrent.ConcurrentHashMap<>();
    }

    protected void removeBiomeData(String presetFolderName) {
        this.globalIdMapping.remove(presetFolderName);
        this.presetGenerationData.remove(presetFolderName);
    }

}
