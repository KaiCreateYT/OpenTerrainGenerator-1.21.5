package com.pg85.otg.config.settings.preset;

import com.pg85.otg.config.io.SettingsMap;
import com.pg85.otg.config.settingtype.MaterialSetting;
import com.pg85.otg.config.settingtype.Setting;
import com.pg85.otg.config.settingtype.Settings;
import com.pg85.otg.config.settings.ConfigSection;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterials;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class BlockSettings extends ConfigSection {
    public static final Setting<Boolean> DISABLE_BEDROCK = Settings.booleanSetting(
            "DisableBedrock", false,
            t -> ((BlockSettings) t).isDisableBedrock(),
            "Disable bottom of map bedrock generation. Doesn't affect bedrock on the ceiling of the map."
    );
    public static final Setting<Boolean> CEILING_BEDROCK = Settings.booleanSetting(
            "CeilingBedrock", false,
            t -> ((BlockSettings) t).isCeilingBedrock(),
            "Enable ceiling of map bedrock generation."
    );
    public static final Setting<Boolean> FLAT_BEDROCK = Settings.booleanSetting(
            "FlatBedrock", false,
            t -> ((BlockSettings) t).isFlatBedrock(),
            "Make a single flat layer of bedrock."
    );
    public static final Setting<Boolean> REMOVE_SURFACE_STONE = Settings.booleanSetting(
            "RemoveSurfaceStone", false,
            t -> ((BlockSettings) t).isRemoveSurfaceStone(),
            "Set this to true to place the biome surface block on top of all exposed stone."
    );

    public static final Setting<LocalMaterialData> DEFAULT_STONE_BLOCK = new MaterialSetting(
            "DefaultStoneBlock", LocalMaterials.STONE_NAME,
            t -> ((BlockSettings) t).getDefaultStoneBlock(),
            "Block used as stone in biomes where stone block is not specified."
    );

    public static final Setting<LocalMaterialData> WATER_BLOCK = new MaterialSetting(
            "WaterBlock", LocalMaterials.WATER_NAME,
            t -> ((BlockSettings) t).getWaterBlock(),
            "Block used as water in WaterLevel."
    );
    public static final Setting<LocalMaterialData> ICE_BLOCK = new MaterialSetting(
            "IceBlock", LocalMaterials.ICE_NAME,
            t -> ((BlockSettings) t).getIceBlock(),
            "Block used as water in WaterLevel."
    );
    public static final Setting<LocalMaterialData> COOLED_LAVA_BLOCK = new MaterialSetting(
            "CooledLavaBlock", LocalMaterials.LAVA_NAME,
            t -> ((BlockSettings) t).getCooledLavaBlock(),
            "Block used as cooled or frozen lava.",
            "Set this to OBSIDIAN for \"frozen\" lava lakes in cold biomes"
    );
    public static final Setting<LocalMaterialData> BEDROCK_BLOCK = new MaterialSetting(
            "BedrockBlock", LocalMaterials.BEDROCK_NAME,
            t -> ((BlockSettings) t).getBedrockBlock(),
            "Block used as bedrock."
    );
    public static final Setting<LocalMaterialData> CARVER_LAVA_BLOCK = new MaterialSetting(
            "CarverLavaBlock", LocalMaterials.LAVA_NAME,
            t -> ((BlockSettings) t).getCarverLavaBlock(),
            "Block that replaces all air blocks from Y0 up to CarverLavaBlockHeight.",
            "For example, vanilla replaces air in caves with lava up to Y10.",
            "Defaults to: LAVA"
    );
    private final boolean removeSurfaceStone;
    private final LocalMaterialData defaultStoneBlock;
    private final LocalMaterialData waterBlock;
    private final LocalMaterialData bedrockBlock;
    private final LocalMaterialData defaultBedrockBlock;
    private final LocalMaterialData cooledLavaBlock;
    private final LocalMaterialData iceBlock;
    private final LocalMaterialData carverLavaBlock;
    private final boolean ceilingBedrock;
    private final boolean flatBedrock;
    private final boolean disableBedrock;

    public static BlockSettings getBlockSettings(SettingsMap reader) {
        var blockSettingsBuilder = builder();

        blockSettingsBuilder.removeSurfaceStone(reader.getSetting(REMOVE_SURFACE_STONE));
        blockSettingsBuilder.defaultStoneBlock(reader.getSetting(DEFAULT_STONE_BLOCK));
        blockSettingsBuilder.waterBlock(reader.getSetting(WATER_BLOCK));
        blockSettingsBuilder.bedrockBlock(reader.getSetting(BEDROCK_BLOCK));
        blockSettingsBuilder.defaultBedrockBlock(LocalMaterials.BEDROCK);
        blockSettingsBuilder.cooledLavaBlock(reader.getSetting(COOLED_LAVA_BLOCK));
        blockSettingsBuilder.iceBlock(reader.getSetting(ICE_BLOCK));
        blockSettingsBuilder.carverLavaBlock(reader.getSetting(CARVER_LAVA_BLOCK));
        blockSettingsBuilder.disableBedrock(reader.getSetting(DISABLE_BEDROCK));
        blockSettingsBuilder.ceilingBedrock(reader.getSetting(CEILING_BEDROCK));
        blockSettingsBuilder.flatBedrock(reader.getSetting(FLAT_BEDROCK));
        return blockSettingsBuilder.build();
    }

    @Override
    public String getSectionName() {
        return "Block Settings";
    }
}