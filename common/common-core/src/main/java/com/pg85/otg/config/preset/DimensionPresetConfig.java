package com.pg85.otg.config.preset;

import com.pg85.otg.config.ConfigFunction;
import com.pg85.otg.config.biome.BiomeGroupFunction;
import com.pg85.otg.config.biome.BiomeResourcesManager;
import com.pg85.otg.config.biome.TemplateBiome;
import com.pg85.otg.config.io.SettingsMap;
import com.pg85.otg.config.settingtype.Setting;
import com.pg85.otg.config.settingtype.Settings;
import com.pg85.otg.config.settings.preset.*;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.util.biome.ReplaceBlockMatrix;
import com.pg85.otg.util.materials.LocalMaterialData;
import lombok.Getter;
import lombok.Setter;

import java.nio.file.Path;
import java.util.*;

/**
 * DimensionPresetConfig.ini classes
 * <p>
 * DimensionPresetSettings defines anything that's used/exposed between projects.
 * DimensionPresetConfig contains only fields/methods used for io/serialisation/instantiation.
 * <p>
 * DimensionPresetConfig should be used only in common-core and platform-specific layers, when reading/writing settings on app start.
 * DimensionPresetSettings should be used wherever settings are used in code.
 */
@Getter
@Setter
public class DimensionPresetConfig extends DimensionPresetSettings {
    public static final HashMap<String, Class<? extends ConfigFunction<?>>> CONFIG_FUNCTIONS = new HashMap<>();
    public static final Setting<List<String>> NORMAL_BIOMES = Settings.stringListSetting(
        "NormalBiomes", "Desert", "Forest", "Extreme Hills", "Swampland", "Plains", "Taiga", "Jungle", "River"
    );
    public static final Setting<List<String>> ICE_BIOMES = Settings.stringListSetting("IceBiomes", "Ice Plains");

    static {
        CONFIG_FUNCTIONS.put("BiomeGroup", BiomeGroupFunction.class);
        CONFIG_FUNCTIONS.put("TemplateBiome", TemplateBiome.class);
    }

    protected boolean biomeConfigsHaveReplacement = false;
    protected int maxSmoothRadius = 2;
    protected NoiseCaveSettings noiseCaveSettings;

    public DimensionPresetConfig(Path settingsDir, SettingsMap settingsReader, ArrayList<String> biomes) {
        super(settingsReader.getName());
        this.renameOldSettings(settingsReader);
        presetInfo = DimensionPresetInfo.buildPresetInfo(settingsReader);
        visualSettings = VisualSettings.builder().fogColor(settingsReader.getSetting(VisualSettings.PRESET_FOG_COLOR)).build();
        blockSettings = BlockSettings.getBlockSettings(settingsReader);
        dimensionSettings = DimensionSettings.getDimensionSettings(settingsReader);
        generationSettings = GenerationSettings.getGenerationSettings(
                this, settingsReader, DimensionPresetResourcesManager.get(), biomes, settingsDir);
        resourceSettings = ResourceSettings.getResourceSettings(settingsReader);
        terrainSettings = TerrainSettings.getTerrainSettings(settingsReader);
        noiseCaveSettings = NoiseCaveSettings.getNoiseCaveSettings(settingsReader);
        imageSettings = ImageSettings.getImageSettings(settingsReader, biomes);
        structureSettings = StructureSettings.getStructureSettings(settingsReader);
        carverSettings = CarverSettings.getCarverSettings(settingsReader, terrainSettings);
        spawnSettings = SpawnSettings.getSpawnSettings(settingsReader);
        portalSettings = PortalSettings.getPortalSettings(settingsReader);
        gameRuleSettings = GameRuleSettings.getGameRuleSettings(settingsReader);
    }

    public LocalMaterialData getBedrockBlockReplaced(ReplaceBlockMatrix replaceBlocks, int y) {
        if (replaceBlocks.replacesBedrock) {
            return this.getBlockSettings().getBedrockBlock().parseWithBiomeAndHeight(this.biomeConfigsHaveReplacement, replaceBlocks, y);
        }
        return this.getBlockSettings().getBedrockBlock();
    }

    @Override
    public void renameOldSettings(SettingsMap reader) {
        // Put BiomeMode in compatibility mode when NormalBiomes is found and create default groups
        if (reader.hasSetting(NORMAL_BIOMES)) {
            int landSize = reader.getSetting(GenerationSettings.LAND_SIZE);
            int landRarity = reader.getSetting(GenerationSettings.LAND_RARITY);
            List<String> normalBiomes = reader.getSetting(NORMAL_BIOMES);

            BiomeGroupFunction normalGroup = new BiomeGroupFunction(this, BiomeGroupNames.NORMAL, landSize, landRarity, normalBiomes);

            List<String> iceBiomes = reader.getSetting(ICE_BIOMES);
            BiomeGroupFunction iceGroup = new BiomeGroupFunction(this, BiomeGroupNames.ICE, 3, 90, iceBiomes);

            reader.addConfigFunctions(Arrays.asList(normalGroup, iceGroup));
        }

        // Rename old settings

        reader.renameOldSetting("SpawnPointSet", SpawnSettings.FIXED_SPAWN_POINT);
        reader.renameOldSetting("PopulationBoundsCheck", ResourceSettings.DECORATION_BOUNDS_CHECK);
        reader.renameOldSetting("EvenCaveDistrubution", CarverSettings.EVEN_CAVE_DISTRIBUTION);
        reader.renameOldSetting("WorldFog", VisualSettings.PRESET_FOG_COLOR);
        reader.renameOldSetting("BedrockobBlock", BlockSettings.BEDROCK_BLOCK);
        reader.renameOldSetting("DimensionPortalMaterials", PortalSettings.PORTAL_BLOCKS);
        reader.renameOldSetting("ShortPresetName", DimensionPresetInfo.REGISTRY_NAME);
    }

    @Override
    public Path getConfigPath() {
        return null;
    }

    @Override
    public void writeConfigSettings(SettingsMap writer) {
        writer.putSetting(Constants.ConfigVersionSetting, Constants.ConfigVersion);
        DimensionPresetWriter.writePresetConfig(this, writer);
    }

    public static class BiomeGroupNames
    {
        public static final String NORMAL = "NormalBiomes";
        public static final String ICE = "IceBiomes";
        public static final String COLD = "ColdBiomes";
        public static final String HOT = "HotBiomes";
        public static final String MESA = "MesaBiomes";
        public static final String JUNGLE = "JungleBiomes";
        public static final String MEGA_TAIGA = "Mega TaigaBiomes";
    }
}
