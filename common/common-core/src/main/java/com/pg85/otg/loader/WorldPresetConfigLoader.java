package com.pg85.otg.loader;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.pg85.otg.config.dimensions.WorldPresetConfig;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.util.OTGLog;
import com.pg85.otg.util.logging.LogCategory;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class WorldPresetConfigLoader {

    private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    /**
     * Loads all WorldPreset YAML files from the WorldPresets/ folder.
     */
    public static List<WorldPresetConfig> loadAll(Path otgRootFolder) {
        List<WorldPresetConfig> configs = new ArrayList<>();
        File worldPresetsDir = otgRootFolder.resolve(Constants.WORLD_PRESETS_FOLDER).toFile();

        if (!worldPresetsDir.exists() || !worldPresetsDir.isDirectory()) {
            return configs;
        }

        File[] yamlFiles = worldPresetsDir.listFiles((dir, name) -> name.endsWith(".yaml") || name.endsWith(".yml"));
        if (yamlFiles == null) return configs;

        for (File yamlFile : yamlFiles) {
            WorldPresetConfig config = fromFile(yamlFile);
            if (config != null) {
                configs.add(config);
            }
        }

        return configs;
    }

    /**
     * Loads a single WorldPreset YAML file.
     */
    public static @Nullable WorldPresetConfig fromFile(File yamlFile) {
        try {
            String content = Files.readString(yamlFile.toPath());
            return fromYamlString(content);
        } catch (IOException e) {
            OTGLog.error(LogCategory.CONFIGS, "Failed to read WorldPreset file {}: {}",
                yamlFile.getName(), e.getMessage());
            return null;
        }
    }

    /**
     * Parses a WorldPreset YAML string.
     */
    public static @Nullable WorldPresetConfig fromYamlString(String input) {
        try {
            return YAML_MAPPER.readValue(input, WorldPresetConfig.class);
        } catch (IOException e) {
            OTGLog.error(LogCategory.CONFIGS, "Failed to parse WorldPreset YAML: {}", e.getMessage());
            return null;
        }
    }
}
