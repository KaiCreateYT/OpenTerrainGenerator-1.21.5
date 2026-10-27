package com.pg85.otg.test.preset;

import com.pg85.otg.OTG;
import com.pg85.otg.presets.LocalDimensionPresetLoader;
import com.pg85.otg.presets.DimensionPreset;
import com.pg85.otg.test.engine.TestOTGEngine;
import com.pg85.otg.test.materials.TestMaterialReader;
import com.pg85.otg.test.materials.TestMaterials;
import com.pg85.otg.util.OTGMaterialReader;

import java.nio.file.Path;
import java.util.List;

/**
 * Utility for loading OTG presets in headless (no Minecraft) mode.
 * Initializes TestMaterials and TestMaterialReader before loading.
 */
public class TestDimensionPresetLoader {

    private static boolean initialized = false;
    private static Path currentOtgRoot = null;

    /**
     * Initializes the headless material system.
     * Safe to call multiple times - subsequent calls are no-ops.
     */
    public static void initHeadless() {
        initHeadless(null);
    }

    /**
     * Initializes the headless mode with OTG engine.
     * Safe to call multiple times - subsequent calls are no-ops.
     *
     * @param otgRootFolder optional OTG root folder for engine initialization
     */
    public static void initHeadless(Path otgRootFolder) {
        if (initialized) {
            return;
        }

        // Initialize all LocalMaterials static fields with TestMaterialData
        TestMaterials.init();

        // Register TestMaterialReader as the global material reader
        OTGMaterialReader.set(new TestMaterialReader());

        // Start TestOTGEngine if otgRootFolder is provided
        if (otgRootFolder != null) {
            currentOtgRoot = otgRootFolder;
            try {
                TestOTGEngine engine = new TestOTGEngine(otgRootFolder);
                OTG.startEngine(engine);
            } catch (IllegalStateException e) {
                // Engine already started - that's fine
            }
        }

        initialized = true;
    }

    /**
     * Loads all presets from the specified OTG root folder.
     * Automatically initializes headless mode if not already done.
     *
     * @param otgRootFolder the root folder containing the Presets/ directory
     * @return list of loaded presets
     */
    public static List<DimensionPreset> loadPresets(Path otgRootFolder) {
        initHeadless(otgRootFolder);
        return LocalDimensionPresetLoader.loadDimensionPresetsFromDisk(otgRootFolder);
    }

    /**
     * Loads a single preset by name from the specified OTG root folder.
     *
     * @param otgRootFolder the root folder containing the Presets/ directory
     * @param presetName    the preset folder name (e.g., "DefaultPreset", "Biome Bundle")
     * @return the loaded preset, or null if not found
     */
    public static DimensionPreset loadPreset(Path otgRootFolder, String presetName) {
        List<DimensionPreset> presets = loadPresets(otgRootFolder);
        return presets.stream()
                .filter(p -> p.getFolderName().equals(presetName) ||
                             p.getConfig().getConfigName().equals(presetName))
                .findFirst()
                .orElse(null);
    }

    /**
     * Returns true if headless mode has been initialized.
     */
    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * Resets initialization state (useful for testing).
     */
    public static void reset() {
        initialized = false;
    }
}
