package com.pg85.otg.config.settings.preset;

import com.pg85.otg.config.settingtype.Setting;
import com.pg85.otg.config.settingtype.Settings;
import com.pg85.otg.config.settings.ConfigSection;
import com.pg85.otg.util.Color;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class VisualSettings extends ConfigSection {
    public static final Setting<Color> PRESET_FOG_COLOR = Settings.colorSetting(
            "WorldFog", "0xC0D8FF",
            t -> ((VisualSettings) t).getFogColor(),
            "Color of the distance fog, can be overridden per biome."
    );

    private final Color fogColor;

    @Override
    public String getSectionName() {
        return "Visual Settings";
    }
}