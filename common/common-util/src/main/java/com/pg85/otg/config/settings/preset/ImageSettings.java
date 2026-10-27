package com.pg85.otg.config.settings.preset;

import com.pg85.otg.config.io.SettingsMap;
import com.pg85.otg.config.settingtype.Setting;
import com.pg85.otg.config.settingtype.Settings;
import com.pg85.otg.config.settings.ConfigSection;
import com.pg85.otg.constants.settings.ImageMode;
import com.pg85.otg.constants.settings.ImageOrientation;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class ImageSettings extends ConfigSection {
    private final ImageOrientation imageOrientation;
    private final String imageFile;
    private final String imageFillBiome;
    private final ImageMode imageMode;
    private final int imageXOffset;
    private final int imageZOffset;

    public static final Setting<String> IMAGE_FILL_BIOME = Settings.stringSetting(
            "ImageFillBiome", "Ocean",
            t -> ((ImageSettings) t).getImageFillBiome(),
            "Biome name for filling outside image boundaries with FillEmpty mode."
    );
    public static final Setting<ImageMode> IMAGE_MODE = Settings.enumSetting(
            "ImageMode", ImageMode.Mirror,
            t -> ((ImageSettings) t).getImageMode(),
            "Defines what to do when terrain is generated outside the boundaries of the image:",
            "Repeat - repeats the image",
            "Mirror - repeats and mirrors the image",
            "ContinueNormal - continues with random generation, using settings for BiomeMode: Normal",
            "FillEmpty - fills the space with the Image Fill Biome"
    );
    public static final Setting<ImageOrientation> IMAGE_ORIENTATION = Settings.enumSetting(
            "ImageOrientation", ImageOrientation.West,
            t -> ((ImageSettings) t).getImageOrientation(),
            "How the image is oriented: North, South, East or West. When this is set to North, the top of your picture is north (no rotation).",
            "When it is set to East, the image is rotated 90 degrees counter-clockwise, therefore what is on the east in the image becomes north in the world.",
            "Possible values: North, East, South, West."
    );
    public static final Setting<String> IMAGE_FILE = Settings.stringSetting(
            "ImageFile", "map.png",
            t -> ((ImageSettings) t).getImageFile(),
            "The image which will provide the Biomes must be a PNG file without transparency, once placed in the same folder as DimensionPresetConfig.ini OTG will use it as a reference for the Biomes generation.",
            "Source png file name for FromImage biome mode."
    );
    public static final Setting<Integer> IMAGE_X_OFFSET = Settings.intSetting(
            "ImageXOffset", 0, Integer.MIN_VALUE, Integer.MAX_VALUE,
            t -> ((ImageSettings) t).getImageXOffset(),
            "Translates the map origin. This number needs to be multiplied by -1 when using FillEmpty."
    );
    public static final Setting<Integer> IMAGE_Z_OFFSET = Settings.intSetting(
            "ImageZOffset", 0, Integer.MIN_VALUE, Integer.MAX_VALUE,
            t -> ((ImageSettings) t).getImageZOffset(),
            "Translates the map origin. This number needs to be multiplied by -1 when using FillEmpty."
    );

    public static ImageSettings getImageSettings(SettingsMap reader, List<String> biomes) {
        var imageSettingsBuilder = builder();
        String imageFillBiome = reader.getSetting(IMAGE_FILL_BIOME);

        imageSettingsBuilder.imageOrientation(reader.getSetting(IMAGE_ORIENTATION));
        imageSettingsBuilder.imageFile(reader.getSetting(IMAGE_FILE));
        // Previously also checked BiomeRegistryNames.Contain() — removed (frozen at 1.16.5).
        imageSettingsBuilder.imageFillBiome(biomes.contains(imageFillBiome) ? imageFillBiome : IMAGE_FILL_BIOME.getDefaultValue());
        imageSettingsBuilder.imageMode(reader.getSetting(IMAGE_MODE));
        imageSettingsBuilder.imageXOffset(reader.getSetting(IMAGE_X_OFFSET));
        imageSettingsBuilder.imageZOffset(reader.getSetting(IMAGE_Z_OFFSET));
        return imageSettingsBuilder.build();
    }

    @Override
    public String getSectionName() {
        return "Image Settings";
    }
}