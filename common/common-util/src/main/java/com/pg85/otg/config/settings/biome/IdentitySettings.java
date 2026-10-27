package com.pg85.otg.config.settings.biome;

import com.pg85.otg.config.io.SettingsMap;
import com.pg85.otg.config.settings.preset.DimensionPresetSettings;
import com.pg85.otg.config.settingtype.Setting;
import com.pg85.otg.config.settingtype.Settings;
import com.pg85.otg.config.settings.ConfigSection;
import com.pg85.otg.util.biome.OTGBiomeResourceLocation;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class IdentitySettings extends ConfigSection {
    private final String biomeName;
    private final String displayName;
    private final String registryPath;
    private final boolean isTemplateForBiome;

    @Override
    public String getSectionName() {
        return "Biome Identity Settings";
    }

    public static final Setting<String> DISPLAY_NAME = Settings.stringSetting(
            "DisplayName",
            "",
            t -> ((IdentitySettings)t).getDisplayName(),
            "Used for generating language files, which determines what will show up in f3 and similar info screens.",
            "Defaults to the biome name if left blank."
    );

    public static IdentitySettings buildIdentitySettings(SettingsMap reader, DimensionPresetSettings presetSettings) {
        var builder = builder();

        builder.biomeName(reader.getName());
        builder.displayName(reader.getSetting(DISPLAY_NAME));
        if (builder.displayName.isBlank()) {
            builder.displayName(OTGBiomeResourceLocation.addSpaceToCamelCase(builder.biomeName));
        }

        builder.isTemplateForBiome(reader.getSetting(OutdatedSettings.IS_TEMPLATE_FOR_BIOME));


        String namespace = presetSettings.getPresetInfo().getRegistryName();
        String path = OTGBiomeResourceLocation.stringToPath(reader.getName()); // Remove any illegal characters

        builder.registryPath(String.format("%s:%s", namespace, path));

        return builder.build();
    }

}
