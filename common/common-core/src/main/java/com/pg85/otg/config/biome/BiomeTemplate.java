package com.pg85.otg.config.biome;

import com.pg85.otg.config.io.SettingsMap;
import com.pg85.otg.config.settings.biome.*;
import com.pg85.otg.config.settings.preset.DimensionPresetSettings;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.interfaces.IBiomeResourceLocation;
import lombok.Getter;
import lombok.Setter;

import java.nio.file.Path;

public class BiomeTemplate extends BiomeSettings {

    public BiomeTemplate(SettingsMap reader, DimensionPresetSettings parent) {
        super(reader, parent, new BiomeResourcesManager(parent.getWorldInfo()));
        this.configPath = reader.getPath();
        this.parent = parent;
    }

    @Getter
    @Setter
    private IBiomeResourceLocation registryKey;

    @Getter
    private final Path configPath;
    private final DimensionPresetSettings parent;

    @Override
    public boolean biomeConfigsHaveReplacement() {
        throw new UnsupportedOperationException(
                "Cannot query a template for whether biome configs have replacements"
        );
    }

    @Override
    public void writeConfigSettings(SettingsMap writer) {
        writer.putSetting(Constants.ConfigVersionSetting, Constants.ConfigVersion);
        super.writeConfigSettings(writer);
    }
}
