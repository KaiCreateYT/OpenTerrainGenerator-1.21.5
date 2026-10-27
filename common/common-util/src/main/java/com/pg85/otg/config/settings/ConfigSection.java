package com.pg85.otg.config.settings;

import com.pg85.otg.config.ConfigFile;
import com.pg85.otg.config.settingtype.Setting;
import com.pg85.otg.util.OTGLog;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A ConfigSection represents a subset of settings in a ConfigFile.
 * Allows for reuse of only some config sections between configs and templates.
 */
public abstract class ConfigSection {

    public abstract String getSectionName();

    public String[] getSectionComment() {
        return new String[]{};
    }

    /**
     * This function collects all Setting variables from a setting holder class
     *
     * @param clazz The class that contains the settings
     * @return Map of setting name to setting
     */
    public static Map<String, Setting<?>> getSettings(Class<? extends ConfigSection> clazz) {
        Map<String, Setting<?>> settingsMap = new HashMap<>();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers()) && Setting.class.isAssignableFrom(field.getType())) {
                try {
                    field.setAccessible(true);
                    Setting<?> setting = (Setting<?>) field.get(null);
                    settingsMap.put(field.getName(), setting);
                } catch (IllegalAccessException e) {
                    OTGLog.error(LogCategory.CONFIGS, "Failed to access setting field: {}", e.getMessage());
                }
            }
        }
        return settingsMap;
    }

    /**
     * Wrapper for getSettings(Class) for internal use by the config sections to get their own settings
     *
     * @return Map of setting name to setting
     */
    protected Map<String, Setting<?>> getSettings() {
        return getSettings(this.getClass());
    }

    /**
     * Wrapper for getSettings() for use by the config system, in list format
     * @return List of all settings defined in a config section
     */
    public List<Setting<?>> getSettingsList() {
        return getSettings().values().stream().toList();
    }

    /**
     * Method to get a list of all settings which are not set to their default value.
     * Intended for use with config sections like tags, where most values will be boolean and stay false.
     * @return List of settings with altered value from the default
     */
    public List<Setting<?>> getAlteredSettings() {
        return getSettings().values().stream()
                .filter(setting -> setting.getGetter().apply(this) != setting.getDefaultValue())
                .toList();
    }

}
