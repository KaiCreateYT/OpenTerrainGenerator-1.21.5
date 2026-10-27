package com.pg85.otg.config.settings.biome;

import com.pg85.otg.config.io.SettingsMap;
import com.pg85.otg.config.settingtype.MaterialSetting;
import com.pg85.otg.config.settingtype.Setting;
import com.pg85.otg.config.settingtype.Settings;
import com.pg85.otg.config.settings.ConfigSection;
import com.pg85.otg.config.settings.preset.BlockSettings;
import com.pg85.otg.config.settings.preset.TerrainSettings;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.interfaces.IBiome;
import com.pg85.otg.interfaces.ISurfaceGenerator;
import com.pg85.otg.interfaces.ISurfaceGeneratorNoiseProvider;
import com.pg85.otg.util.biome.ReplaceBlockMatrix;
import com.pg85.otg.util.gen.ChunkBuffer;
import com.pg85.otg.util.gen.GeneratingChunk;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterials;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class SurfaceSettings extends ConfigSection {
    private final ISurfaceGenerator surfaceGenerator;
    private final ReplaceBlockMatrix replacedBlocks;
    private final BlockSettings blockSettings;
    private final int configWaterLevelMax;
    private final int waterLevelMax;
    private final int configWaterLevelMin;
    private final int waterLevelMin;
    private final boolean useWorldWaterLevel;
    private final boolean useFrozenOceanTemperature;
    private final LocalMaterialData stoneBlock;
    private final LocalMaterialData surfaceBlock;
    private final LocalMaterialData underWaterSurfaceBlock;
    private final LocalMaterialData groundBlock;
    private final LocalMaterialData sandStoneBlock;
    private final LocalMaterialData redSandStoneBlock;
    private final LocalMaterialData waterBlock;
    private final LocalMaterialData iceBlock;
    private final LocalMaterialData packedIceBlock;
    private final LocalMaterialData snowBlock;
    private final LocalMaterialData cooledLavaBlock;

    @Override
    public String getSectionName() {
        return "Biome Surface Settings";
    }

    public static final Setting<Integer> WATER_LEVEL_MAX = Settings.intSetting(
            "WaterLevelMax", 63, Constants.WORLD_START_MIN_Y, Constants.WORLD_END_MAX_Y,
            t -> ((SurfaceSettings) t).getWaterLevelMax(),
            "Set water level. Every empty block under this level down to min will be fill water or another block from WaterBlock."
    );
    public static final Setting<Integer> WATER_LEVEL_MIN = Settings.intSetting(
            "WaterLevelMin", 0, Constants.WORLD_START_MIN_Y, Constants.WORLD_END_MAX_Y,
            t -> ((SurfaceSettings) t).getWaterLevelMin(),
            "Set water level. Every empty block over this level up to max will be fill water or another block from WaterBlock."
    );

    public static final Setting<Boolean> USE_WORLD_WATER_LEVEL = Settings.booleanSetting(
            "UseWorldWaterLevel", true,
            t -> ((SurfaceSettings)t).isUseWorldWaterLevel(),
            "If this setting is set to true, the water level of the world will be used instead of the water level of the biome."
    );
    public static final Setting<LocalMaterialData> STONE_BLOCK = new MaterialSetting(
            "StoneBlock", LocalMaterials.STONE_NAME,
            t -> ((SurfaceSettings)t).getStoneBlock(),
            "The block used for the stone"
    );
    public static final Setting<LocalMaterialData> SURFACE_BLOCK = new MaterialSetting(
            "SurfaceBlock", LocalMaterials.GRASS_NAME,
            t -> ((SurfaceSettings)t).getSurfaceBlock(),
            "The block used for the surface"
    );
    public static final Setting<LocalMaterialData> UNDER_WATER_SURFACE_BLOCK = new MaterialSetting(
            "UnderWaterSurfaceBlock", LocalMaterials.DIRT_NAME,
            t -> ((SurfaceSettings)t).getUnderWaterSurfaceBlock(),
            "The block used for the surface in the biome when under water."
    );
    public static final Setting<LocalMaterialData> GROUND_BLOCK = new MaterialSetting(
            "GroundBlock", LocalMaterials.DIRT_NAME,
            t -> ((SurfaceSettings)t).getGroundBlock(),
            "The block used for the ground"
    );
    public static final Setting<LocalMaterialData> SANDSTONE_BLOCK = new MaterialSetting(
            "SandstoneBlock", LocalMaterials.SANDSTONE_NAME,
            t -> ((SurfaceSettings)t).getSandStoneBlock(),
            "The block used for the sandstone"
    );
    public static final Setting<LocalMaterialData> RED_SANDSTONE_BLOCK = new MaterialSetting(
            "RedSandstoneBlock", LocalMaterials.RED_SANDSTONE_NAME,
            t -> ((SurfaceSettings)t).getRedSandStoneBlock(),
            "The block used for the red sandstone"
    );
    public static final Setting<LocalMaterialData> WATER_BLOCK = new MaterialSetting(
            "WaterBlock", LocalMaterials.WATER_NAME,
            t -> ((SurfaceSettings) t).getWaterBlock(),
            "Block used as water in WaterLevel."
    );
    public static final Setting<LocalMaterialData> ICE_BLOCK = new MaterialSetting(
            "IceBlock", LocalMaterials.ICE_NAME,
            t -> ((SurfaceSettings) t).getIceBlock(),
            "Block used as water in WaterLevel."
    );
    public static final Setting<LocalMaterialData> COOLED_LAVA_BLOCK = new MaterialSetting(
            "CooledLavaBlock", LocalMaterials.LAVA_NAME,
            t -> ((SurfaceSettings) t).getCooledLavaBlock(),
            "Block used as cooled or frozen lava.",
            "Set this to OBSIDIAN for \"frozen\" lava lakes in cold biomes"
    );
    public static final Setting<LocalMaterialData> PACKED_ICE_BLOCK = new MaterialSetting(
            "PackedIceBlock", LocalMaterials.PACKED_ICE_NAME,
            t -> ((SurfaceSettings)t).getPackedIceBlock(),
            "The block used for the packed ice."
    );
    public static final Setting<LocalMaterialData> SNOW_BLOCK = new MaterialSetting(
            "SnowBlock", LocalMaterials.SNOW_BLOCK_NAME,
            t -> ((SurfaceSettings)t).getSnowBlock(),
            "The block used for the snow."
    );
    public static final Setting<ReplaceBlockMatrix> REPLACED_BLOCKS = Settings.replacedBlocksSetting(
            "ReplacedBlocks",
            t -> ((SurfaceSettings)t).getReplacedBlocks(),
            "Replace Variable: (blockFrom,blockTo[:blockDataTo][,minHeight,maxHeight])", "Example :",
            "  ReplacedBlocks: (GRASS,DIRT,100,127),(GRAVEL,GLASS)",
            "Replace grass block to dirt from 100 to 127 height and replace gravel to glass on all height ",
            "Only the following biome resources are affected: CustomObject, CustomStructure, Ore, UnderWaterOre, ",
            "Vein, SurfacePatch, Boulder, IceSpike.",
            "BO's used as CustomObject/CustomStructure may have DoReplaceBlocks:false to save performance."
    );
    public static final Setting<Boolean> USE_FROZEN_OCEAN_TEMPERATURE = Settings.booleanSetting(
            "UseFrozenOceanTemperature", false,
            t -> ((SurfaceSettings)t).isUseFrozenOceanTemperature(),
            "Set this to true to use variable temperatures within the biome based on noise.",
            "Used for vanilla Frozen Ocean and Deep Frozen Ocean biomes to create patches of water/ice."
    );

    public static final Setting<ISurfaceGenerator> SURFACE_GENERATOR = Settings.surfaceGeneratorSetting(
            "SurfaceAndGroundControl",
            section -> ((SurfaceSettings) section).getSurfaceGenerator(),
            "Setting for biomes with more complex surface and ground blocks.",
            "Each column in the world has a noise value from what appears to be -7 to 7.",
            "Values near 0 are more common than values near -7 and 7. This setting is",
            "used to change the surface block based on the noise value for the column.",
            "1.12.2 Syntax: SurfaceBlockName,GroundBlockName,MaxNoise[,AnotherSurfaceBlockName,AnotherGroundBlockName,MaxNoise][,...]",
            "Example: " + SurfaceSettings.SURFACE_GENERATOR + ": STONE,STONE,-0.8,GRAVEL,STONE,0.0,DIRT,DIRT,10.0",
            "1.16.x Syntax: SurfaceBlockName,UnderWaterSurfaceBlockName,GroundBlockName,MaxNoise,[AnotherSurfaceBlockName,AnotherUnderWaterSurfaceBlockName,AnotherGroundBlockName,MaxNoise[,...]]",
            "  When the noise is below -0.8, stone is the surface and ground block, between -0.8 and 0",
            "  gravel with stone just below and between 0.0 and 10.0 there's only dirt.",
            "  Because 10.0 is higher than the noise can ever get, the normal " + SurfaceSettings.SURFACE_BLOCK,
            "  and " + SurfaceSettings.GROUND_BLOCK + " will never appear in this biome.", "",
            "Alternatively, you can use Mesa, MesaForest or MesaBryce to get blocks",
            "like the blocks found in the Mesa biomes.",
            "You can also use Iceberg to get iceberg generation like in vanilla frozen oceans. Iceberg accepts a normal SAGC string: \"Iceberg <SAGC>\", so you can use normal SAGC with it."
    );

    public static SurfaceSettings getSurfaceSettings(SettingsMap settingsReader,
                                                     BlockSettings presetBlocks, TerrainSettings presetTerrain) {
        SurfaceSettingsBuilder builder = SurfaceSettings.builder();

        builder.surfaceGenerator(settingsReader.getSetting(SURFACE_GENERATOR));
        builder.replacedBlocks(settingsReader.getSetting(REPLACED_BLOCKS));
        builder.blockSettings(presetBlocks);
        builder.useWorldWaterLevel(settingsReader.getSetting(USE_WORLD_WATER_LEVEL));
        builder.configWaterLevelMax(settingsReader.getSetting(WATER_LEVEL_MAX));
        builder.configWaterLevelMin(settingsReader.getSetting(WATER_LEVEL_MIN));

        if (builder.useWorldWaterLevel) {
            builder.waterLevelMin(presetTerrain.getWaterLevelMin());
            builder.waterLevelMax(presetTerrain.getWaterLevelMax());
        } else {
            builder.waterLevelMin(builder.configWaterLevelMin);
            builder.waterLevelMax(builder.configWaterLevelMax);
        }

        builder.useFrozenOceanTemperature(settingsReader.getSetting(USE_FROZEN_OCEAN_TEMPERATURE));
        builder.stoneBlock(settingsReader.getSetting(STONE_BLOCK));
        builder.surfaceBlock(settingsReader.getSetting(SURFACE_BLOCK));
        builder.underWaterSurfaceBlock(settingsReader.getSetting(UNDER_WATER_SURFACE_BLOCK));
        builder.groundBlock(settingsReader.getSetting(GROUND_BLOCK));
        builder.sandStoneBlock(settingsReader.getSetting(SANDSTONE_BLOCK));
        builder.redSandStoneBlock(settingsReader.getSetting(RED_SANDSTONE_BLOCK));
        builder.waterBlock(settingsReader.getSetting(WATER_BLOCK));
        builder.iceBlock(settingsReader.getSetting(ICE_BLOCK));
        builder.packedIceBlock(settingsReader.getSetting(PACKED_ICE_BLOCK));
        builder.snowBlock(settingsReader.getSetting(SNOW_BLOCK));
        builder.cooledLavaBlock(settingsReader.getSetting(COOLED_LAVA_BLOCK));

        return builder.fixSettings().build();
    }

    public static class SurfaceSettingsBuilder {
        public SurfaceSettingsBuilder fixSettings() {
            checkWaterLevelMax();
            if (this.underWaterSurfaceBlock == null) {
                this.underWaterSurfaceBlock = this.surfaceBlock;
            }
            return this;
        }
        public void checkWaterLevelMax() {
            configWaterLevelMax = Math.max(configWaterLevelMax, configWaterLevelMin);
        }
    }

    // Any blocks spawned/checked during base terrain gen that use the biomeconfig materials
    // call getXXXBlockReplaced to get the replaced blocks.
    // Any blocks spawned during decoration will have their materials parsed before spawning
    // them via world.setBlock(), so they use the default biomeconfig materials.

    // Note: getSurfaceBlockReplaced / getGroundBlockReplaced don't take into
    // account SAGC, so they should only be used by surfacegenerators.

    public LocalMaterialData getSurfaceBlockAtHeight(ISurfaceGeneratorNoiseProvider noiseProvider, int x, int y, int z) {
        return surfaceGenerator.getSurfaceBlockAtHeight(noiseProvider,this,  x, y, z);
    }
    public LocalMaterialData getGroundBlockAtHeight(ISurfaceGeneratorNoiseProvider noiseProvider, int x, int y, int z) {
        return surfaceGenerator.getGroundBlockAtHeight(noiseProvider,this,  x, y, z);
    }

    public LocalMaterialData getSurfaceBlockReplaced(int y) {
        if (replacedBlocks.replacesSurface) {
            return replacedBlocks.replaceBlock(y, surfaceBlock);
        }
        return surfaceBlock;
    }
    public LocalMaterialData getUnderWaterSurfaceBlockReplaced(int y) {
        if (replacedBlocks.replacesUnderWaterSurface) {
            return replacedBlocks.replaceBlock(y, underWaterSurfaceBlock);
        }
        return underWaterSurfaceBlock;
    }
    public LocalMaterialData getGroundBlockReplaced(int y) {
        if (replacedBlocks.replacesGround) {
            return replacedBlocks.replaceBlock(y, groundBlock);
        }
        return groundBlock;
    }
    public LocalMaterialData getSandStoneBlockReplaced(int y) {
        if (replacedBlocks.replacesSandStone) {
            return replacedBlocks.replaceBlock(y, sandStoneBlock);
        }
        return sandStoneBlock;
    }
    public LocalMaterialData getRedSandStoneBlockReplaced(int y) {
        if (replacedBlocks.replacesRedSandStone) {
            return replacedBlocks.replaceBlock(y, redSandStoneBlock);
        }
        return redSandStoneBlock;
    }
    public LocalMaterialData getWaterBlockReplaced(int y) {
        if (replacedBlocks.replacesWater) {
            return replacedBlocks.replaceBlock(y, waterBlock);
        }
        return waterBlock;
    }
    public LocalMaterialData getIceBlockReplaced(int y) {
        if (replacedBlocks.replacesIce) {
            return replacedBlocks.replaceBlock(y, iceBlock);
        }
        return iceBlock;
    }
    public LocalMaterialData getPackedIceBlockReplaced(int y) {
        if (replacedBlocks.replacesPackedIce) {
            return replacedBlocks.replaceBlock(y, packedIceBlock);
        }
        return packedIceBlock;
    }
    public LocalMaterialData getSnowBlockReplaced(int y) {
        if (replacedBlocks.replacesSnow) {
            return replacedBlocks.replaceBlock(y, snowBlock);
        }
        return snowBlock;
    }
    public LocalMaterialData getCooledLavaBlockReplaced(int y) {
        if (replacedBlocks.replacesCooledLava) {
            return replacedBlocks.replaceBlock(y, cooledLavaBlock);
        }
        return cooledLavaBlock;
    }
    public LocalMaterialData getStoneBlockReplaced(int y) {
        // Use deepslate below Y=0 (1.18+ terrain generation)
        LocalMaterialData baseBlock = (y < 0) ? LocalMaterials.DEEPSLATE : stoneBlock;
        if (replacedBlocks.replacesStone) {
            return replacedBlocks.replaceBlock(y, baseBlock);
        }
        return baseBlock;
    }

    public LocalMaterialData getBedrockBlockReplaced(int y) {
        if (replacedBlocks.replacesBedrock) {
            return replacedBlocks.replaceBlock(y, blockSettings.getBedrockBlock());
        }
        return blockSettings.getBedrockBlock();
    }

    public LocalMaterialData getDefaultGroundBlock() {
        return groundBlock;
    }

    public void doSurfaceAndGroundControl(long worldSeed, GeneratingChunk generatingChunk, ChunkBuffer chunkBuffer, int x, int z, IBiome biome) {
        this.surfaceGenerator.spawn(worldSeed, generatingChunk, chunkBuffer, biome, x, z);
    }


    public static int getSnowHeight(float temp) {
        // OTG biome temperature is between 0.0 and 2.0.
        // Judging by WorldStandardValues.SNOW_AND_ICE_MAX_TEMP, snow should appear below 0.15.
        // According to the configs, snow and ice should appear between 0.2 (at y > 90) and 0.1 (entire biome covered in ice).
        // Let's make sure that at 0.2, snow layers start with thickness 0 at y 90 and thickness 7 around y 255.
        // In a 0.2 temp biome, y90 temp is 0.156, y255 temp is -0.12

        float snowTemp = Constants.SNOW_AND_ICE_TEMP;
        if (temp <= snowTemp) {
            float maxColdTemp = Constants.SNOW_AND_ICE_MAX_TEMP;
            float maxThickness = 7.0f;
            if (temp < maxColdTemp) {
                return (int) maxThickness;
            }
            float range = Math.abs(maxColdTemp - snowTemp);
            float fraction = Math.abs(maxColdTemp - temp);
            return (int) Math.floor((1.0f - (fraction / range)) * maxThickness);
        }

        return 0;
    }

    public ReplaceBlockMatrix getReplacedBlocks() {
        initReplaceBlocks();
        return this.replacedBlocks;
    }

    private void initReplaceBlocks() {
        if (!this.replacedBlocks.initialised) {
            // Multiple threads may be working with
            // the same biome configs async, lock.
            synchronized (this) {
                if (!this.replacedBlocks.initialised) {
                    this.replacedBlocks.init(
                            this.useWorldWaterLevel ? this.getBlockSettings().getCooledLavaBlock() : this.cooledLavaBlock,
                            this.useWorldWaterLevel ? this.getBlockSettings().getIceBlock() : this.iceBlock,
                            this.packedIceBlock,
                            this.snowBlock,
                            this.useWorldWaterLevel ? this.getBlockSettings().getWaterBlock() : this.waterBlock,
                            this.stoneBlock,
                            this.groundBlock,
                            this.surfaceBlock,
                            this.underWaterSurfaceBlock,
                            this.getBlockSettings().getDefaultBedrockBlock(),
                            this.sandStoneBlock,
                            this.redSandStoneBlock
                    );
                }
                this.replacedBlocks.initialised = true;
            }
        }
    }
}
