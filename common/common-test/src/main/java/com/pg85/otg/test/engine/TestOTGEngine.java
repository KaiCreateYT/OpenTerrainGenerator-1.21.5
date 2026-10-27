package com.pg85.otg.test.engine;

import com.pg85.otg.OTGEngine;
import com.pg85.otg.gen.biome.layers.BiomeLayerData;
import com.pg85.otg.interfaces.IBiome;
import com.pg85.otg.interfaces.IModLoadedChecker;
import com.pg85.otg.presets.LocalDimensionPresetLoader;

import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;

/**
 * Minimal OTGEngine implementation for headless testing.
 * Provides just enough functionality for terrain generation without Minecraft.
 */
public class TestOTGEngine extends OTGEngine {

    public TestOTGEngine(Path otgRootFolder) {
        super(
                new TestLogger(),
                otgRootFolder,
                mod -> false,  // No mods loaded in test mode
                new TestLocalPresetLoader(otgRootFolder)
        );
    }

    @Override
    public File getJarFileFromModLoader() {
        // No jar file in headless mode
        return null;
    }

    @Override
    public void onStart() {
        // Minimal startup - skip preset unpacking and file creation
        getLogger().init(LogLevel.INFO, EnumSet.of(LogCategory.MAIN, LogCategory.CONFIGS), "");
        // Note: We don't call dimensionPresetLoader.loadDimensionPresetsFromDisk() here
        // because TestDimensionPresetLoader.loadPresets() does this separately
    }

    /**
     * Minimal LocalDimensionPresetLoader for test engine.
     */
    private static class TestLocalPresetLoader extends LocalDimensionPresetLoader {
        public TestLocalPresetLoader(Path otgRootFolder) {
            super(otgRootFolder);
        }

        @Override
        public IBiome[] getGlobalIdMapping(String presetFolderName) {
            return new IBiome[0];
        }

        @Override
        public Map<String, BiomeLayerData> getPresetGenerationData() {
            return Collections.emptyMap();
        }
    }
}
