package com.pg85.otg.util;

import com.pg85.otg.config.settings.preset.DimensionPresetInfo;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.util.logging.LogCategory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Installs bundled DefaultPreset and example WorldPresets from the mod artifact into the OTG config
 * directory. Supports real JARs, nested JARs on disk, and exploded mod paths (Fabric Loom / dev).
 * <p>
 * This mirrors historic OTG (e.g. 1.16.x) behaviour: scan all mod content roots and merge bundled
 * {@code resources/} trees instead of assuming a single {@link JarFile} path.
 */
public final class BundledPresetSync {

    private static final String RESOURCES_PREFIX = "resources/";

    private BundledPresetSync() {
    }

    public static void syncBundledResourcesIfStale(List<Path> modRoots, Path otgRoot, ILogger logger) {
        if (modRoots == null || modRoots.isEmpty()) {
            logger.warn(LogCategory.MAIN, "No mod content roots to scan for bundled OTG presets.");
            return;
        }

        File presetsDir = otgRoot.resolve(Constants.DIMENSION_PRESETS_FOLDER).toFile();
        File defaultPresetDir = new File(presetsDir, Constants.DEFAULT_PRESET_NAME);
        File presetConfigFile = new File(defaultPresetDir, Constants.DIMENSION_PRESET_CONFIG_FILE);

        int[] bundledVersion = readBundledPresetVersion(modRoots);
        if (bundledVersion == null) {
            logger.warn(LogCategory.MAIN,
                    "Bundled DefaultPreset not found under resources/ on the mod classpath. "
                            + "Add presets under config/{}/DimensionPresets or fix mod packaging.",
                    Constants.MOD_ID);
            return;
        }

        if (shouldSkipUnpack(presetConfigFile, bundledVersion)) {
            logger.info(LogCategory.MAIN, "Default preset is up-to-date. Skipping bundled resource install.");
            return;
        }

        logger.info(LogCategory.MAIN, "Installing bundled OTG resources from mod classpath ({} roots).", modRoots.size());
        for (Path root : modRoots) {
            try {
                tryInstallFromRoot(root, otgRoot, logger);
            } catch (IOException e) {
                logger.warn(LogCategory.MAIN, "Could not read bundled resources from {}: {}", root, e.getMessage());
            }
        }

        if (!presetConfigFile.exists()) {
            logger.warn(LogCategory.MAIN,
                    "After scanning classpath, {} is still missing. Presets may need to be installed manually.",
                    presetConfigFile);
        }
    }

    private static boolean shouldSkipUnpack(File presetConfigFile, int[] bundledVersion) {
        if (!presetConfigFile.exists()) {
            return false;
        }
        try (BufferedReader existingConfigReader = new BufferedReader(new FileReader(presetConfigFile))) {
            int existingMajor = parseVersion(existingConfigReader, DimensionPresetInfo.MAJOR_VERSION.getName());
            int existingMinor = parseVersion(existingConfigReader, DimensionPresetInfo.MINOR_VERSION.getName());
            int bundledMajor = bundledVersion[0];
            int bundledMinor = bundledVersion[1];
            return (bundledMajor < existingMajor)
                    || (bundledMajor == existingMajor && bundledMinor <= existingMinor);
        } catch (IOException e) {
            return false;
        }
    }

    private static int[] readBundledPresetVersion(List<Path> modRoots) {
        String entryPath = RESOURCES_PREFIX + Constants.DIMENSION_PRESETS_FOLDER + "/" + Constants.DEFAULT_PRESET_NAME + "/"
                + Constants.DIMENSION_PRESET_CONFIG_FILE;
        for (Path root : modRoots) {
            try {
                if (Files.isDirectory(root)) {
                    Path ini = root.resolve(entryPath);
                    if (Files.isRegularFile(ini)) {
                        try (BufferedReader r = Files.newBufferedReader(ini)) {
                            int major = parseVersion(r, DimensionPresetInfo.MAJOR_VERSION.getName());
                            int minor = parseVersion(r, DimensionPresetInfo.MINOR_VERSION.getName());
                            return new int[] { major, minor };
                        }
                    }
                } else if (Files.isRegularFile(root)) {
                    JarFile jar = openJarIfZip(root);
                    if (jar == null) {
                        continue;
                    }
                    try (jar) {
                        JarEntry je = jar.getJarEntry(entryPath.replace('\\', '/'));
                        if (je != null && !je.isDirectory()) {
                            try (BufferedReader r = new BufferedReader(new InputStreamReader(jar.getInputStream(je)))) {
                                int major = parseVersion(r, DimensionPresetInfo.MAJOR_VERSION.getName());
                                int minor = parseVersion(r, DimensionPresetInfo.MINOR_VERSION.getName());
                                return new int[] { major, minor };
                            }
                        }
                    }
                }
            } catch (IOException ignored) {
                // try next root
            }
        }
        return null;
    }

    private static JarFile openJarIfZip(Path path) {
        try {
            return new JarFile(path.toFile());
        } catch (IOException e) {
            return null;
        }
    }

    private static void tryInstallFromRoot(Path root, Path otgRoot, ILogger logger) throws IOException {
        if (Files.isDirectory(root)) {
            Path defaultSrc = root.resolve(RESOURCES_PREFIX + Constants.DIMENSION_PRESETS_FOLDER + "/" + Constants.DEFAULT_PRESET_NAME);
            Path worldSrc = root.resolve(RESOURCES_PREFIX + Constants.WORLD_PRESETS_FOLDER);
            if (Files.isDirectory(defaultSrc)) {
                Path dest = otgRoot.resolve(Constants.DIMENSION_PRESETS_FOLDER).resolve(Constants.DEFAULT_PRESET_NAME);
                copyDirectory(defaultSrc, dest);
                logger.info(LogCategory.CONFIGS, "Merged DefaultPreset from exploded mod path: {}", root);
            }
            if (Files.isDirectory(worldSrc)) {
                Path dest = otgRoot.resolve(Constants.WORLD_PRESETS_FOLDER);
                copyDirectory(worldSrc, dest);
                logger.info(LogCategory.CONFIGS, "Merged WorldPresets from exploded mod path: {}", root);
            }
        } else if (Files.isRegularFile(root)) {
            JarFile jar = openJarIfZip(root);
            if (jar == null) {
                return;
            }
            try (jar) {
                String defaultPrefix = RESOURCES_PREFIX + Constants.DIMENSION_PRESETS_FOLDER + "/" + Constants.DEFAULT_PRESET_NAME + "/";
                String worldPrefix = RESOURCES_PREFIX + Constants.WORLD_PRESETS_FOLDER + "/";
                boolean wrote = false;
                Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String name = entry.getName().replace('\\', '/');
                    if (name.startsWith(defaultPrefix) || name.startsWith(worldPrefix)) {
                        writeJarEntry(jar, entry, name, otgRoot);
                        wrote = true;
                    }
                }
                if (wrote) {
                    logger.info(LogCategory.CONFIGS, "Merged bundled resources from JAR: {}", root);
                }
            }
        }
    }

    private static void writeJarEntry(JarFile jar, JarEntry entry, String entryName, Path otgRoot) throws IOException {
        if (entryName.length() <= RESOURCES_PREFIX.length()) {
            return;
        }
        Path relativeUnderOtg = Path.of(entryName.substring(RESOURCES_PREFIX.length()));
        Path target = otgRoot.resolve(relativeUnderOtg);
        if (entry.isDirectory()) {
            Files.createDirectories(target);
            return;
        }
        Files.createDirectories(target.getParent());
        try (InputStream is = jar.getInputStream(entry)) {
            Files.copy(is, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void copyDirectory(Path source, Path target) throws IOException {
        try (var stream = Files.walk(source)) {
            for (Path src : stream.toList()) {
                Path dest = target.resolve(source.relativize(src));
                if (Files.isDirectory(src)) {
                    Files.createDirectories(dest);
                } else {
                    Files.createDirectories(dest.getParent());
                    Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    private static int parseVersion(BufferedReader reader, String name) throws IOException {
        int version = -1;
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.contains(name)) {
                break;
            }
        }
        if (line != null) {
            String v = line.split(":", 2)[1].trim();
            version = Integer.parseInt(v);
        }
        return version;
    }
}
