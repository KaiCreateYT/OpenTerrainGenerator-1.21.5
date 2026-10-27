package com.pg85.otg.config.settings.biome;

import com.pg85.otg.config.ConfigFile;
import com.pg85.otg.config.annotation.*;
import com.pg85.otg.config.io.SettingsMap;
import com.pg85.otg.config.settings.ConfigSection;
import com.pg85.otg.config.settings.preset.GenerationSettings;
import com.pg85.otg.config.settings.biome.generated.BiomePlacementSettings;
import com.pg85.otg.util.Color;
import com.pg85.otg.util.OTGLog;
import com.pg85.otg.util.logging.LogCategory;
import lombok.Builder;
import lombok.Getter;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Builder
@Getter
@Config
public class BiomePlacementConfig extends ConfigSection {
    @Override
    public Map<String, com.pg85.otg.config.settingtype.Setting<?>> getSettings() {
        return ConfigSection.getSettings(BiomePlacementSettings.class);
    }

    @Ignore
    private final GenerationSettings parent;
    @Ignore
    private final boolean isleBiome;
    @Ignore
    private final boolean borderBiome;
    
    @IntSetting(value=4, min=0, max=20)
    @LongDescription({
            "Biome size from 0 to GenerationDepth. Defines in which biome layer this biome will be generated (see GenerationDepth).",
            "Higher numbers result in a smaller biome, lower numbers a larger biome.",
            "How this setting is used depends on the value of BiomeMode in the DimensionPresetConfig.",
            "It will be used for:",
            "- normal biomes, ice biomes, isle biomes and border biomes when BiomeMode is set to NoGroups",
            "- biomes spawned as part of a BiomeGroup when BiomeMode is set to Normal.",
            "  For biomes spawned as isles, borders or rivers other settings are available.",
            "  Isle biomes:	BiomeSizeWhenIsle (see below)",
            "  Border biomes: BiomeSizeWhenBorder (see below)",
            "  River biomes:  RiverSize (see DimensionPresetConfig)"
    })
    private final int biomeSize;

    @IntSetting(value=100, min=0)
    @LongDescription({
            "Biome rarity from 100 to 1. If this is normal or ice biome - chance to spawn this biome, then others.",
            "Example for normal biome :",
            "  100 rarity mean 1/6 chance than other ( with 6 default normal biomes).",
            "  50 rarity mean 1/11 chance than other",
            "For isle biomes see the BiomeRarityWhenIsle setting below.",
            "Doesn`t work on Ocean and River (frozen versions too) biomes when not added as normal biome."
    })
    private final int biomeRarity;

    @ColorSetting
    @LongDescription({
            "The hexadecimal color value of this biome. Used in the output of the /otg map command,",
            "and used in the input of BiomeMode: FromImage."
    })
    private final Color biomeMapColor;

    @StringSetting("River")
    @Description("The biome to use for rivers")
    private final String riverBiome;

    @StringListSetting({"Ocean"})
    @LongDescription({
            "List of biomes in which this biome will spawn as an isle.",
            "For example, Mushroom Isles spawn inside the Ocean biome.",
            "To spawn a biome as an isle, first add it to the",
            "IsleBiomes list in the DimensionPresetConfig."
    })
    private final List<String> isleInBiomes;

    @IntSetting(value=6, min=0, max=20)
    @LongDescription({
            "Size of this biome when spawned as an isle biome in BiomeMode: Normal.",
            "Valid values range from 0 to GenerationDepth.",
            "Larger numbers give *smaller* islands. The biome must be smaller than the biome it's going",
            "to spawn in, so the BiomeSizeWhenIsle number must be larger than the BiomeSize of the other biome."
    })
    private final int biomeSizeWhenIsle;

    @IntSetting(value=97, min=0)
    @Description("Rarity of this biome when spawned as an isle biome in BiomeMode: Normal.")
    private final int biomeRarityWhenIsle;

    @StringListSetting
    @LongDescription({
            "List of biomes this biome can be a border of.",
            "For example, the Beach biome is a border on the Ocean biome, so",
            "it can spawn anywhere on the border of an ocean.",
            "To spawn a biome as a border, first add it to the",
            "BorderBiomes list in the DimensionPresetConfig."
    })
    private final List<String> borderInBiomes;

    @StringListSetting
    @Description("Whitelist of neighouring biomes that allow this border biome to spawn.")
    private final List<String> onlyBorderNear;

    @StringListSetting
    @LongDescription({
            "Blacklist of neighbouring biomes that do not allow this border biome to spawn.",
            "For example, the Beach biome will never spawn next to an Extreme Hills biome.",
            "Only used when OnlyBorderNear is empty / not used."
    })
    private final List<String> notBorderNear;

    @IntSetting(value=8, min=0, max=20)
    @LongDescription({
            "Size of this biome when spawned as a border biome in BiomeMode: Normal.",
            "Valid values range from 0 to GenerationDepth.",
            "Larger numbers give *smaller* borders. The biome must be smaller than the biome it's going",
            "to spawn in, so the BiomeSizeWhenBorder number must be larger than the BiomeSize of the other biome."
    })
    private final int biomeSizeWhenBorder;

    @Override
    public String getSectionName() {
        return "Biome Placement Settings";
    }

    public static BiomePlacementConfig getGenerationSettings(SettingsMap reader, GenerationSettings parent) {
        BiomePlacementConfigBuilder builder = BiomePlacementSettings.getBuilder(reader);

        builder.parent(parent);
        builder.isleBiome(!builder.isleInBiomes.isEmpty());
        builder.borderBiome(!builder.borderInBiomes.isEmpty());

        // worldBiomes is empty, fill it

        return builder.fixSettings().build();
    }

    public static class BiomePlacementConfigBuilder {
        public BiomePlacementConfigBuilder fixSettings() {
            checkBiomeSize();
            checkBiomeSizeWhenIsle();
            checkBiomeSizeWhenBorder();
            checkBiomeRarity();
            checkBiomeRarityWhenIsle();
            checkIsleInBiomes();
            checkBorderInBiomes();
            checkOnlyBorderNear();
            checkNotBorderNear();
            return this;
        }
        private void checkBiomeSize() {
            biomeSize = Math.min(biomeSize, this.parent.getGenerationDepth());
        }
        private void checkBiomeSizeWhenIsle() {
            biomeSizeWhenIsle = Math.min(biomeSizeWhenIsle, this.parent.getGenerationDepth());
        }
        private void checkBiomeSizeWhenBorder() {
            biomeSizeWhenBorder = Math.min(biomeSizeWhenBorder, this.parent.getGenerationDepth());
        }
        private void checkBiomeRarity() {
            biomeRarity = Math.min(biomeRarity, this.parent.getBiomeRarityScale());
        }
        private void checkBiomeRarityWhenIsle() {
            biomeRarityWhenIsle = Math.min(biomeRarityWhenIsle, this.parent.getBiomeRarityScale());
        }
        private void checkIsleInBiomes() {
            var output = ConfigFile.filterBiomes(isleInBiomes, this.parent.getWorldBiomes());
            if (output.size() != isleInBiomes.size()) {
                // TODO: Add context to log messages like this, perhaps by making the file extend ConfigFile and adding context there
                var invalid = new HashSet<>(isleInBiomes);
                output.forEach(invalid::remove);
                OTGLog.warn(LogCategory.CONFIGS, "invalid value(s) in isleInBiomes: {}", invalid);
            }
            isleInBiomes = output;
        }
        private void checkBorderInBiomes() {
            var output = ConfigFile.filterBiomes(borderInBiomes, this.parent.getWorldBiomes());
            if (output.size() != borderInBiomes.size()) {
                var invalid = new HashSet<>(borderInBiomes);
                output.forEach(invalid::remove);
                OTGLog.warn(LogCategory.CONFIGS, "invalid value(s) in borderInBiomes: {}", invalid);
            }
            borderInBiomes = output;
        }
        private void checkOnlyBorderNear() {
            var output = ConfigFile.filterBiomes(onlyBorderNear, this.parent.getWorldBiomes());
            if (output.size() != onlyBorderNear.size()) {
                var invalid = new HashSet<>(onlyBorderNear);
                output.forEach(invalid::remove);
                OTGLog.warn(LogCategory.CONFIGS, "invalid value(s) in onlyBorderNear: {}", invalid);
            }
            onlyBorderNear = output;
        }
        private void checkNotBorderNear() {
            var output = ConfigFile.filterBiomes(notBorderNear, this.parent.getWorldBiomes());
            if (output.size() != notBorderNear.size()) {
                var invalid = new HashSet<>(notBorderNear);
                output.forEach(invalid::remove);
                OTGLog.warn(LogCategory.CONFIGS, "invalid value(s) in notBorderNear: {}", invalid);
            }
            notBorderNear = output;
        }
    }
}
