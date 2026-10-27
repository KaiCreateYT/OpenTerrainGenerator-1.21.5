package com.pg85.otg.customobject;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.customobject.config.CustomObjectResourcesManager;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IModLoadedChecker;
import com.pg85.otg.util.OTGLog;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.minecraft.TreeType;
import java.io.File;
import java.nio.file.Path;
import java.util.*;

/**
 * Represents a collection of custom objects. Those objects can be loaded from a
 * directory, or can be loaded manually and then added to this collection.
 *
 */
public class CustomObjectCollection {
    private final Object indexingFilesLock = new Object();

    private final ArrayList<CustomObject> objectsGlobalObjects = new ArrayList<>();
    private final HashMap<String, CustomObject> objectsByNameGlobalObjects = new HashMap<>();
    private final HashSet<String> objectsNotFoundGlobalObjects = new HashSet<>();

    private final HashMap<String, ArrayList<CustomObject>> objectsPerPreset = new HashMap<>();
    private final HashMap<String, HashMap<String, CustomObject>> objectsByNamePerPreset = new HashMap<>();
    private final HashMap<String, HashSet<String>> objectsNotFoundPerPreset = new HashMap<>();

    private HashMap<String, File> customObjectFilesGlobalObjects = null;
    private HashMap<String, File> globalTemplates = null;
    private final HashMap<String, HashMap<String, File>> customObjectFilesPerPreset = new HashMap<>();
    private final HashMap<String, HashMap<String, File>> boTemplateFilesPerPreset = new HashMap<>();

    private static final Set<String> CUSTOM_OBJECT_EXTENSIONS = Set.of(".bo2", ".bo3", ".bo4", ".bo4data");
    private static final Set<String> LEGACY_OBJECT_NAMES = Set.of("useworld", "useworldall", "usebiome", "usebiomeall");
    private final HashSet<String> loggedLegacyObjects = new HashSet<>();

    public CustomObject loadObject(File file, String presetFolderName, Path otgRootFolder, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker) {
        synchronized (this.indexingFilesLock) {
            if (!file.isFile()) {
                throw new RuntimeException("Given path does not exist: " + file.getAbsolutePath());
            }

            String fileName = file.getName();
            int index = fileName.lastIndexOf('.');
            if (index == -1) return null;

            String objectType = fileName.substring(index + 1);
            String objectName = fileName.substring(0, index);

            // Try native loader for file extension
            CustomObjectLoader loader = customObjectManager.getObjectLoaders().get(objectType.toLowerCase());
            if (loader == null) return null;

            CustomObject object = getCustomObject(file, presetFolderName, objectName, loader);
            if (object.onEnable(presetFolderName, otgRootFolder, customObjectManager, materialReader, manager, modLoadedChecker) && object.loadChecks(modLoadedChecker)) {
                return object;
            }
            removeLoadedObject(presetFolderName, object);

            // Fallback: try as BO4 (handles .bo3 with IsOTGPlus:true)
            loader = customObjectManager.getObjectLoaders().get("bo4");
            if (loader == null) return null;

            object = getCustomObject(file, presetFolderName, objectName, loader);
            if (object.onEnable(presetFolderName, otgRootFolder, customObjectManager, materialReader, manager, modLoadedChecker) && object.loadChecks(modLoadedChecker)) {
                return object;
            }
            removeLoadedObject(presetFolderName, object);
            OTGLog.error(LogCategory.CUSTOM_OBJECTS, "Failed to load custom object '{}' — rejected by both {} and BO4 loaders", objectName, objectType.toUpperCase());
            return null;
        }
    }

    private CustomObject getCustomObject(File file, String presetFolderName, String objectName, CustomObjectLoader loader) {
        CustomObject object;
        object = loader.loadFromFile(objectName, file);
        if (presetFolderName != null) {
            ArrayList<CustomObject> presetObjects = this.objectsPerPreset.computeIfAbsent(presetFolderName, k -> new ArrayList<>());
            presetObjects.add(object);
        } else {
            this.objectsGlobalObjects.add(object);
        }
        return object;
    }

    private void removeLoadedObject(String presetFolderName, CustomObject object) {

        if (presetFolderName == null)  {
            this.objectsGlobalObjects.remove(object);
            return;
        }

        HashMap<String, CustomObject> presetObjectsByName = this.objectsByNamePerPreset.get(presetFolderName);
        if (presetObjectsByName != null) {
            presetObjectsByName.remove(object.getName());
            if (presetObjectsByName.isEmpty()) {
                this.objectsByNamePerPreset.remove(presetFolderName, presetObjectsByName);
            }
        }

        ArrayList<CustomObject> worldObjects = this.objectsPerPreset.get(presetFolderName);
        worldObjects.remove(object);
        if (worldObjects.isEmpty()) {
            this.objectsPerPreset.remove(presetFolderName, worldObjects);
        }
    }

    /**
     * Adds an object to the list of loaded objects. If an object with the same name
     * (case insensitive) already exists, nothing happens.
     *
     * @param object The object to add to the list of loaded objects.
     */
    void addLoadedGlobalObject(CustomObject object) {
        synchronized (this.indexingFilesLock) {
            String name = object.getName();
            if (!this.objectsByNameGlobalObjects.containsKey(name.toLowerCase())) {
                this.objectsByNameGlobalObjects.put(name.toLowerCase(), object);
                this.objectsGlobalObjects.add(object);
            }
        }
    }

    void addGlobalObjectFile(String name, File file) {
        synchronized (this.indexingFilesLock) {
            if (this.customObjectFilesGlobalObjects != null && !this.customObjectFilesGlobalObjects.containsKey(name.toLowerCase())) {
                this.customObjectFilesGlobalObjects.put(name.toLowerCase(), file);
            }
        }
    }

    public void unloadCustomObjectFiles() {
        synchronized (this.indexingFilesLock) {
            this.objectsGlobalObjects.clear();
            this.objectsByNameGlobalObjects.clear();
            this.objectsNotFoundGlobalObjects.clear();

            this.objectsPerPreset.clear();
            this.objectsByNamePerPreset.clear();
            this.objectsNotFoundPerPreset.clear();
        }
    }

    void reloadCustomObjectFiles() {
        synchronized (indexingFilesLock) {
            this.objectsGlobalObjects.clear();
            this.objectsByNameGlobalObjects.clear();
            this.objectsNotFoundGlobalObjects.clear();

            this.objectsPerPreset.clear();
            this.objectsByNamePerPreset.clear();
            this.objectsNotFoundPerPreset.clear();

            this.customObjectFilesGlobalObjects = null;
            this.globalTemplates = null;
            this.customObjectFilesPerPreset.clear();
            this.boTemplateFilesPerPreset.clear();
        }
    }

    public ArrayList<String> getAllBONamesForPreset(String presetFolderName, Path otgRootPath) {
        HashMap<String, File> files = this.customObjectFilesPerPreset.get(presetFolderName);
        if (files == null) {
            indexPresetObjectsFolder(presetFolderName, otgRootPath);
            files = this.customObjectFilesPerPreset.get(presetFolderName);
        }
        return files == null ? null : fileNamesWithoutExtension(files);
    }

    public ArrayList<String> getTemplatesForPreset(String presetFolderName, Path otgRootPath) {
        HashMap<String, File> files = this.boTemplateFilesPerPreset.get(presetFolderName);
        if (files == null) {
            indexPresetObjectsFolder(presetFolderName, otgRootPath);
            files = this.boTemplateFilesPerPreset.get(presetFolderName);
        }
        return files == null ? null : fileNamesWithoutExtension(files);
    }

    public File getTemplateFileForPreset(String presetFolderName, String templateName, Path otgRootPath) {
        HashMap<String, File> files = this.boTemplateFilesPerPreset.get(presetFolderName);
        if (files == null) {
            indexPresetObjectsFolder(presetFolderName, otgRootPath);
            files = this.boTemplateFilesPerPreset.get(presetFolderName);
        }
        return files == null ? null : files.get(templateName.toLowerCase());
    }

    private static ArrayList<String> fileNamesWithoutExtension(HashMap<String, File> files) {
        ArrayList<String> names = new ArrayList<>(files.size());
        for (File file : files.values()) {
            String fileName = file.getName();
            int dot = fileName.lastIndexOf('.');
            names.add(dot == -1 ? fileName : fileName.substring(0, dot));
        }
        return names;
    }

    public ArrayList<String> getGlobalObjectNames(Path otgRootPath) {
        if (this.customObjectFilesGlobalObjects == null) {
            indexGlobalObjectsFolder(otgRootPath);
        }
        return this.customObjectFilesGlobalObjects == null
                ? null
                : new ArrayList<>(this.customObjectFilesGlobalObjects.keySet());
    }

    public ArrayList<String> getGlobalTemplates(Path otgRootPath) {
        if (this.globalTemplates == null) {
            indexGlobalObjectsFolder(otgRootPath);
        }
        return this.globalTemplates == null
                ? null
                : new ArrayList<>(this.globalTemplates.keySet());
    }

    // Adds an object to a preset, if it has been loaded
    // Does not add the object if the preset's BO's have not yet been indexed
    public void addObjectToPreset(String presetFolderName, String objectName, File boFile, CustomObject object) {
        HashMap<String, CustomObject> objectsByName = this.objectsByNamePerPreset.get(presetFolderName);
        if (objectsByName != null) objectsByName.put(objectName.toLowerCase(), object);
        HashMap<String, File> customObjectFiles = this.customObjectFilesPerPreset.get(presetFolderName);
        if (customObjectFiles != null) customObjectFiles.put(objectName.toLowerCase(), boFile);
    }

    public CustomObject getObjectByName(String name, String presetFolderName, Path otgRootFolder, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker) {
        synchronized (this.indexingFilesLock) {
            return getObjectByName(name, presetFolderName, true, otgRootFolder, customObjectManager, materialReader, manager, modLoadedChecker);
        }
    }

    void indexGlobalObjectsFolder(Path otgRootFolder) {
        synchronized (this.indexingFilesLock) {
            if (this.customObjectFilesGlobalObjects != null) {
                return;
            }

            OTGLog.info(LogCategory.CUSTOM_OBJECTS, "Indexing GlobalObjects folder.");

            this.customObjectFilesGlobalObjects = new HashMap<>();
            this.globalTemplates = new HashMap<>();
            File globalObjectsDir = otgRootFolder.resolve(Constants.GLOBAL_OBJECTS_FOLDER).toFile();
            if (globalObjectsDir.exists()) {
                indexAllCustomObjectFilesInDir(globalObjectsDir, this.customObjectFilesGlobalObjects, this.globalTemplates);
            }

            // Add vanilla custom objects
            for (TreeType type : TreeType.values()) {
                addLoadedGlobalObject(new TreeObject(type));
            }

            OTGLog.info(LogCategory.CUSTOM_OBJECTS, "GlobalObjects folder indexed.");
        }
    }

    void indexPresetObjectsFolder(String presetFolderName, Path otgRootFolder) {
        synchronized (this.indexingFilesLock) {
            if (presetFolderName == null || this.customObjectFilesPerPreset.containsKey(presetFolderName)) {
                return;
            }

            OTGLog.info(LogCategory.CUSTOM_OBJECTS, "Indexing Objects folder for preset {}", presetFolderName);

            HashMap<String, File> presetCustomObjectFiles = new HashMap<>();
            this.customObjectFilesPerPreset.put(presetFolderName, presetCustomObjectFiles);
            HashMap<String, File> templateFiles = new HashMap<>();
            this.boTemplateFilesPerPreset.put(presetFolderName, templateFiles);

            // TODO: Rename folders
            Path presetPath = otgRootFolder.resolve(Constants.DIMENSION_PRESETS_FOLDER).resolve(presetFolderName);
            File objectsDir = presetPath.resolve(Constants.OBJECTS_FOLDER).toFile();
            if (!objectsDir.exists()) {
                objectsDir = presetPath.resolve(Constants.LEGACY_WORLD_OBJECTS_FOLDER).toFile();
            }
            if (objectsDir.exists()) {
                indexAllCustomObjectFilesInDir(objectsDir, presetCustomObjectFiles, templateFiles);
            }

            OTGLog.info(LogCategory.CUSTOM_OBJECTS, "Objects folder for preset {} indexed.", presetFolderName);
        }
    }

    /**
     * Gets the object with the given name.
     *
     * @param name Name of the object.
     * @return The object, or null if not found.
     */
    private CustomObject getObjectByName(String name, String presetFolderName, boolean searchGlobalObjects, Path otgRootFolder, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker) {
        synchronized (this.indexingFilesLock) {
            // UseWorld/UseBiome are legacy special objects not used by OTG, skip them.
            if (LEGACY_OBJECT_NAMES.contains(name.toLowerCase())) {
                if (this.loggedLegacyObjects.add(name.toLowerCase())) {
                    OTGLog.info(LogCategory.CUSTOM_OBJECTS, "{} is not used by OTG, skipping.", name);
                }
                return null;
            }

            String nameLower = name.toLowerCase();

            // Check preset object cache
            boolean presetSearched = false;
            if (presetFolderName != null) {
                HashMap<String, CustomObject> cached = this.objectsByNamePerPreset.get(presetFolderName);
                if (cached != null) {
                    CustomObject object = cached.get(nameLower);
                    if (object != null) return object;
                }
                // TODO: If a user adds a new object while the game is running, it won't be picked up, even when developermode:true.
                HashSet<String> notFound = this.objectsNotFoundPerPreset.get(presetFolderName);
                presetSearched = notFound != null && notFound.contains(name);
            }

            // Check global object cache (only once preset search is exhausted)
            if (searchGlobalObjects && (presetFolderName == null || presetSearched)) {
                CustomObject object = this.objectsByNameGlobalObjects.get(nameLower);
                if (object != null) return object;
            }

            // All relevant searches already done — give up
            boolean globalSearched = this.objectsNotFoundGlobalObjects.contains(name);
            if ((!searchGlobalObjects || globalSearched) && (presetFolderName == null || presetSearched)) {
                return null;
            }

            // Ensure directories are indexed
            indexGlobalObjectsFolder(otgRootFolder);
            indexPresetObjectsFolder(presetFolderName, otgRootFolder);

            // Search preset Objects
            if (presetFolderName != null && !presetSearched) {
                HashMap<String, File> presetFiles = this.customObjectFilesPerPreset.get(presetFolderName);
                File file = presetFiles != null ? presetFiles.get(nameLower) : null;

                if (file != null) {
                    CustomObject object = loadObject(file, presetFolderName, otgRootFolder, customObjectManager, materialReader, manager, modLoadedChecker);
                    if (object != null) {
                        this.objectsByNamePerPreset.computeIfAbsent(presetFolderName, k -> new HashMap<>()).put(nameLower, object);
                        return object;
                    }
                    OTGLog.error(LogCategory.CUSTOM_OBJECTS, "Could not load custom object, it likely contains errors: {}", file);
                    return null;
                }

                this.objectsNotFoundPerPreset.computeIfAbsent(presetFolderName, k -> new HashSet<>()).add(name);
            }

            // Search GlobalObjects
            if (searchGlobalObjects && !globalSearched) {
                CustomObject object = this.objectsByNameGlobalObjects.get(nameLower);
                if (object != null) return object;

                File file = this.customObjectFilesGlobalObjects.get(nameLower);
                if (file != null) {
                    object = loadObject(file, presetFolderName, otgRootFolder, customObjectManager, materialReader, manager, modLoadedChecker);
                    if (object != null) {
                        this.objectsByNameGlobalObjects.put(nameLower, object);
                        return object;
                    }
                    OTGLog.error(LogCategory.CUSTOM_OBJECTS, "Could not load custom object, it likely contains errors: {}", file);
                    return null;
                }

                this.objectsNotFoundGlobalObjects.add(name);
            }

            OTGLog.error(LogCategory.CUSTOM_OBJECTS,
                    "Could not find BO2/BO3 {} in {} directory{}.",
                    name,
                    presetFolderName != null ? "Objects and GlobalObjects" : "GlobalObjects",
                    presetFolderName != null ? " for preset " + presetFolderName : "");
            return null;
        }
    }

    private void indexAllCustomObjectFilesInDir(File searchDir, HashMap<String, File> customObjectFiles, HashMap<String, File> templateFiles) {
        if (!searchDir.exists()) return;

        if (!searchDir.isDirectory()) {
            indexCustomObjectFile(searchDir, customObjectFiles, templateFiles);
            return;
        }

        for (File file : Objects.requireNonNull(searchDir.listFiles())) {
            if (file.isDirectory()) {
                indexAllCustomObjectFilesInDir(file, customObjectFiles, templateFiles);
            } else {
                indexCustomObjectFile(file, customObjectFiles, templateFiles);
            }
        }
    }

    private void indexCustomObjectFile(File file, HashMap<String, File> customObjectFiles, HashMap<String, File> templateFiles) {
        int dotIndex = file.getName().lastIndexOf('.');
        if (dotIndex == -1) return;

        String name = file.getName().substring(0, dotIndex).toLowerCase();
        String ext = file.getName().substring(dotIndex).toLowerCase();

        if (CUSTOM_OBJECT_EXTENSIONS.contains(ext)) {
            // .bo4data gets priority — it's the pre-compiled format
            if (ext.equals(".bo4data") || !customObjectFiles.containsKey(name)) {
                customObjectFiles.put(name, file);
            } else {
                OTGLog.warn(LogCategory.CUSTOM_OBJECTS, "Duplicate file found: {}", file.getName());
            }
        } else if (ext.equals(".bo3template") || ext.equals(".bo4template")) {
            templateFiles.put(name, file);
        }
    }
}
