package com.pg85.otg.config.settings.preset;

import com.pg85.otg.config.io.SettingsMap;
import com.pg85.otg.config.settingtype.Setting;
import com.pg85.otg.config.settingtype.Settings;
import com.pg85.otg.config.settings.ConfigSection;
import com.pg85.otg.constants.settings.structure.CustomStructureType;
import lombok.Builder;
import lombok.Getter;

import static com.pg85.otg.config.settingtype.Settings.booleanSetting;

@Builder
@Getter
public class ResourceSettings extends ConfigSection {
    private final boolean disableOreGen;
    private final String BO3AtSpawn;
    private final CustomStructureType customStructureType;
    private final boolean useOldBO3StructureRarity;
    private final boolean decorationBoundsCheck;
    private final int maximumCustomStructureRadius;

    public static final Setting<Boolean> USE_OLD_BO3_STRUCTURE_RARITY = Settings.booleanSetting(
            "UseOldBO3StructureRarity", true,
            t -> ((ResourceSettings) t).isUseOldBO3StructureRarity(),
            "For 1.12.2 v9.0_r11 and earlier, BO3 customstructures used 2 rarity rolls,",
            "one for the rarity in the CustomStructure() tag, one for the rarity in the BO3 itself.",
            "For 1.16, we use only the rarity roll from the CustomStructure() tag. Set this to true",
            "to use the old system."
    );
    public static final Setting<Boolean> DECORATION_BOUNDS_CHECK = Settings.booleanSetting(
            "DecorationBoundsCheck", true,
            t -> ((ResourceSettings) t).isDecorationBoundsCheck(),
            "Set this to false to disable the bounds check during chunk decoration.",
            "While this allows you to spawn objects larger than 32x32,",
            "it also makes terrain generation dependent on the direction you explored the world in."
    );
    public static final Setting<Boolean> DISABLE_OREGEN = Settings.booleanSetting(
            "DisableOreGen", false,
            t -> ((ResourceSettings) t).isDisableOreGen(),
            "Disables Ore(), UnderWaterOre() and Vein() biome resources that use any type of ore block."
    );
    public static final Setting<CustomStructureType> CUSTOM_STRUCTURE_TYPE = Settings.enumSetting(
            "CustomStructureType", CustomStructureType.BO3,
            t -> ((ResourceSettings) t).getCustomStructureType(),
            "Sets the type of structures the world should spawn, BO3 or BO4.",
            "Allowed values: BO3/BO4.",
            "BO4's allow for collision detection, fine control over structure distribution, advanced branching mechanics for",
            "procedurally generated structures, smoothing areas, extremely large structures, settings for blending structures",
            "with surrounding terrain, etc. BO3's are simpler, seed based CustomStructures, more like vanilla mc structures.",
            "Worlds currently can only use one type of structure."
    );
    public static final Setting<String> BO3_AT_SPAWN = Settings.stringSetting(
            "BO3AtSpawn", "",
            t -> ((ResourceSettings) t).getBO3AtSpawn(),
            "This BO3 will be spawned at the world's spawn point as a CustomObject (Max size 32x32)."
    );
    public static final Setting<Integer> MAXIMUM_CUSTOM_STRUCTURE_RADIUS = Settings.intSetting(
            "MaximumCustomStructureRadius", 5, 1, 100,
            t -> ((ResourceSettings) t).getMaximumCustomStructureRadius(),
            "Maximum radius of custom structures in chunks. Custom structures are spawned by",
            "the CustomStructure resource in the biome configuration files. Not used for BO4's."
    );

    // For legacy support, remove in future versions
    public static final Setting<Boolean> ISOTGPLUS = booleanSetting("IsOTGPlus", false);


    public static ResourceSettings getResourceSettings(SettingsMap reader) {
        var resourceSettingsBuilder = builder();
        resourceSettingsBuilder.disableOreGen(reader.getSetting(DISABLE_OREGEN));

        boolean isOTGPlus = reader.getSetting(ISOTGPLUS);
        if(isOTGPlus) {
            resourceSettingsBuilder.customStructureType(CustomStructureType.BO4);
        } else {
            resourceSettingsBuilder.customStructureType(reader.getSetting(CUSTOM_STRUCTURE_TYPE));
        }
        resourceSettingsBuilder.useOldBO3StructureRarity(reader.getSetting(USE_OLD_BO3_STRUCTURE_RARITY));
        resourceSettingsBuilder.decorationBoundsCheck(reader.getSetting(DECORATION_BOUNDS_CHECK));
        resourceSettingsBuilder.maximumCustomStructureRadius(reader.getSetting(MAXIMUM_CUSTOM_STRUCTURE_RADIUS));
        resourceSettingsBuilder.BO3AtSpawn(reader.getSetting(BO3_AT_SPAWN));
        return resourceSettingsBuilder.build();
    }

    @Override
    public String getSectionName() {
        return "Resource Settings";
    }
}