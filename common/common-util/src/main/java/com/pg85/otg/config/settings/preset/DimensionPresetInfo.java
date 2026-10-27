package com.pg85.otg.config.settings.preset;

import com.pg85.otg.config.io.SettingsMap;
import com.pg85.otg.config.settingtype.Setting;
import com.pg85.otg.config.settingtype.Settings;
import com.pg85.otg.config.settings.ConfigSection;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.constants.settings.ConfigMode;
import com.pg85.otg.util.biome.OTGBiomeResourceLocation;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class DimensionPresetInfo extends ConfigSection {
    private final ConfigMode settingsMode;
    private final String displayName;
    private final String registryName;
    private final int majorVersion;
    private final int minorVersion;
    private final String author;
    private final String description;
    private final boolean selectableInWorldCreation;

    public static final Setting<ConfigMode> SETTINGS_MODE = Settings.enumSetting(
            "SettingsMode", ConfigMode.WriteAll,
            t -> ((DimensionPresetInfo) t).getSettingsMode(),
            "Each time " + Constants.MOD_ID + " reads the config files it can also write to them. With this setting you can change how this behaves. Possible modes:",
            "WriteAll - Auto-update settings from old versions, order them, add comments, reset invalid settings and remove custom comments. (Recommended)",
            "WriteWithoutComments - Same as WriteAll, but removes all comments, both the ones added by OTG and custom ones. Removing comments is a recommended optimization for release versions of presets.",
            "WriteDisable - Doesn't write to the config files. Errors are not corrected, old settings are read but are not corrected. Custom comments won't be removed with this mode."
    );
    public static final Setting<String> AUTHOR = Settings.stringSetting(
            "Author", "Unknown",
            t -> ((DimensionPresetInfo) t).getAuthor(),
            "The author of this preset"
    );
    public static final Setting<String> REGISTRY_NAME = Settings.stringSetting(
            "RegistryName", "",
            t -> ((DimensionPresetInfo) t).getRegistryName(),
            "The shortened name for the preset, used in biome resource locations and similar"
    );
    public static final Setting<String> DISPLAY_NAME = Settings.stringSetting(
            "DisplayName", "",
            t -> ((DimensionPresetInfo) t).getDisplayName(),
            "The display name for this preset, used in the world creation screen and similar. ",
            "Defaults to the preset folder name if left blank"
    );
    public static final Setting<String> DESCRIPTION = Settings.stringSetting(
            "Description", "No description given",
            t -> ((DimensionPresetInfo) t).getDescription(),
            "A short description of this preset"
    );
    public static final Setting<Integer> MAJOR_VERSION = Settings.intSetting(
            "MajorVersion", 0 , 0, Integer.MAX_VALUE,
            t -> ((DimensionPresetInfo) t).getMajorVersion(),
            "The preset major version. Increasing the minor version makes the PresetPacker overwrite,",
            "while increasing the major version will make the PresetPacker save a new copy"
    );
    public static final Setting<Integer> MINOR_VERSION = Settings.intSetting(
            "MinorVersion", 0 , 0, Integer.MAX_VALUE,
            t -> ((DimensionPresetInfo) t).getMinorVersion(),
            "The preset minor version. Increasing the minor version makes the PresetPacker overwrite,",
            "while increasing the major version will make the PresetPacker save a new copy"
    );

    public static final Setting<Boolean> SELECTABLE_IN_WORLD_CREATION = Settings.booleanSetting(
            "SelectableInWorldCreation", true,
            t -> ((DimensionPresetInfo) t).isSelectableInWorldCreation(),
            "Whether this preset should be selectable in the world creation screen"
    );

    public static DimensionPresetInfo buildPresetInfo(SettingsMap reader) {
        DimensionPresetInfoBuilder presetInfoBuilder = builder();

        presetInfoBuilder.displayName(reader.getSetting(DISPLAY_NAME));
        presetInfoBuilder.settingsMode(reader.getSetting(SETTINGS_MODE));
        presetInfoBuilder.author(reader.getSetting(AUTHOR));
        presetInfoBuilder.description(reader.getSetting(DESCRIPTION));
        presetInfoBuilder.registryName(reader.getSetting(REGISTRY_NAME));
        presetInfoBuilder.majorVersion(reader.getSetting(MAJOR_VERSION));
        presetInfoBuilder.minorVersion(reader.getSetting(MINOR_VERSION));
        presetInfoBuilder.selectableInWorldCreation(reader.getSetting(SELECTABLE_IN_WORLD_CREATION));

        return presetInfoBuilder.fixSettings(reader.getName()).build();
    }

    public static class DimensionPresetInfoBuilder {
        public DimensionPresetInfoBuilder fixSettings(String presetFolderName) {
            if (this.registryName.isBlank() || this.registryName.equalsIgnoreCase("default")) {
                this.registryName = presetFolderName;
            }
            if (this.displayName.isBlank()) {
                this.displayName = presetFolderName;
            }
            this.registryName = OTGBiomeResourceLocation.stringToPath(this.registryName);
            return this;
        }
    }

    @Override
    public String getSectionName() {
        return "Preset Info";
    }
}
