package com.pg85.otg.config.settings.biome.generated;

import com.pg85.otg.config.settings.ConfigSection;
import com.pg85.otg.config.settingtype.Settings;
import com.pg85.otg.config.settingtype.Setting;
import com.pg85.otg.config.io.SettingsMap;
import java.util.List;
import com.pg85.otg.config.settings.biome.BiomePlacementConfig;
import com.pg85.otg.util.Color;
import java.util.List;
import java.util.List;
import java.util.List;
import java.util.List;

public class BiomePlacementSettings extends ConfigSection {

	@Override
	public String getSectionName() {
		return "Biome Placement Settings";
	}


	public static final Setting<Integer> BIOME_SIZE = Settings.intSetting("BiomeSize", 4, 0, 20, t -> ((BiomePlacementConfig) t).getBiomeSize(), "Biome size from 0 to GenerationDepth. Defines in which biome layer this biome will be generated (see GenerationDepth).", "Higher numbers result in a smaller biome, lower numbers a larger biome.", "How this setting is used depends on the value of BiomeMode in the DimensionPresetConfig.", "It will be used for:", "- normal biomes, ice biomes, isle biomes and border biomes when BiomeMode is set to NoGroups", "- biomes spawned as part of a BiomeGroup when BiomeMode is set to Normal.", "  For biomes spawned as isles, borders or rivers other settings are available.", "  Isle biomes:	BiomeSizeWhenIsle (see below)", "  Border biomes: BiomeSizeWhenBorder (see below)", "  River biomes:  RiverSize (see DimensionPresetConfig)");
	public static final Setting<Integer> BIOME_RARITY = Settings.intSetting("BiomeRarity", 100, 0, 2147483647, t -> ((BiomePlacementConfig) t).getBiomeRarity(), "Biome rarity from 100 to 1. If this is normal or ice biome - chance to spawn this biome, then others.", "Example for normal biome :", "  100 rarity mean 1/6 chance than other ( with 6 default normal biomes).", "  50 rarity mean 1/11 chance than other", "For isle biomes see the BiomeRarityWhenIsle setting below.", "Doesn`t work on Ocean and River (frozen versions too) biomes when not added as normal biome.");
	public static final Setting<Color> BIOME_MAP_COLOR = Settings.colorSetting("BiomeMapColor", "0xFFFFFF", t -> ((BiomePlacementConfig) t).getBiomeMapColor(), "The hexadecimal color value of this biome. Used in the output of the /otg map command,", "and used in the input of BiomeMode: FromImage.");
	public static final Setting<String> RIVER_BIOME = Settings.stringSetting("RiverBiome", "River", t -> ((BiomePlacementConfig) t).getRiverBiome(), "The biome to use for rivers");
	public static final Setting<List<String>> ISLE_IN_BIOMES = Settings.stringListSetting("IsleInBiomes", new String[] {"Ocean"}, t -> ((BiomePlacementConfig) t).getIsleInBiomes(), "List of biomes in which this biome will spawn as an isle.", "For example, Mushroom Isles spawn inside the Ocean biome.", "To spawn a biome as an isle, first add it to the", "IsleBiomes list in the DimensionPresetConfig.");
	public static final Setting<Integer> BIOME_SIZE_WHEN_ISLE = Settings.intSetting("BiomeSizeWhenIsle", 6, 0, 20, t -> ((BiomePlacementConfig) t).getBiomeSizeWhenIsle(), "Size of this biome when spawned as an isle biome in BiomeMode: Normal.", "Valid values range from 0 to GenerationDepth.", "Larger numbers give *smaller* islands. The biome must be smaller than the biome it's going", "to spawn in, so the BiomeSizeWhenIsle number must be larger than the BiomeSize of the other biome.");
	public static final Setting<Integer> BIOME_RARITY_WHEN_ISLE = Settings.intSetting("BiomeRarityWhenIsle", 97, 0, 2147483647, t -> ((BiomePlacementConfig) t).getBiomeRarityWhenIsle(), "Rarity of this biome when spawned as an isle biome in BiomeMode: Normal.");
	public static final Setting<List<String>> BORDER_IN_BIOMES = Settings.stringListSetting("BorderInBiomes", new String[] {}, t -> ((BiomePlacementConfig) t).getBorderInBiomes(), "List of biomes this biome can be a border of.", "For example, the Beach biome is a border on the Ocean biome, so", "it can spawn anywhere on the border of an ocean.", "To spawn a biome as a border, first add it to the", "BorderBiomes list in the DimensionPresetConfig.");
	public static final Setting<List<String>> ONLY_BORDER_NEAR = Settings.stringListSetting("OnlyBorderNear", new String[] {}, t -> ((BiomePlacementConfig) t).getOnlyBorderNear(), "Whitelist of neighouring biomes that allow this border biome to spawn.");
	public static final Setting<List<String>> NOT_BORDER_NEAR = Settings.stringListSetting("NotBorderNear", new String[] {}, t -> ((BiomePlacementConfig) t).getNotBorderNear(), "Blacklist of neighbouring biomes that do not allow this border biome to spawn.", "For example, the Beach biome will never spawn next to an Extreme Hills biome.", "Only used when OnlyBorderNear is empty / not used.");
	public static final Setting<Integer> BIOME_SIZE_WHEN_BORDER = Settings.intSetting("BiomeSizeWhenBorder", 8, 0, 20, t -> ((BiomePlacementConfig) t).getBiomeSizeWhenBorder(), "Size of this biome when spawned as a border biome in BiomeMode: Normal.", "Valid values range from 0 to GenerationDepth.", "Larger numbers give *smaller* borders. The biome must be smaller than the biome it's going", "to spawn in, so the BiomeSizeWhenBorder number must be larger than the BiomeSize of the other biome.");

	public static BiomePlacementConfig.BiomePlacementConfigBuilder getBuilder(SettingsMap settingsMap) {
		var builder = BiomePlacementConfig.builder();
		builder.biomeSize(settingsMap.getSetting(BIOME_SIZE));
		builder.biomeRarity(settingsMap.getSetting(BIOME_RARITY));
		builder.biomeMapColor(settingsMap.getSetting(BIOME_MAP_COLOR));
		builder.riverBiome(settingsMap.getSetting(RIVER_BIOME));
		builder.isleInBiomes(settingsMap.getSetting(ISLE_IN_BIOMES));
		builder.biomeSizeWhenIsle(settingsMap.getSetting(BIOME_SIZE_WHEN_ISLE));
		builder.biomeRarityWhenIsle(settingsMap.getSetting(BIOME_RARITY_WHEN_ISLE));
		builder.borderInBiomes(settingsMap.getSetting(BORDER_IN_BIOMES));
		builder.onlyBorderNear(settingsMap.getSetting(ONLY_BORDER_NEAR));
		builder.notBorderNear(settingsMap.getSetting(NOT_BORDER_NEAR));
		builder.biomeSizeWhenBorder(settingsMap.getSetting(BIOME_SIZE_WHEN_BORDER));
		return builder;
	}


	public static List <Setting<?>> list = List.of(BIOME_SIZE, BIOME_RARITY, BIOME_MAP_COLOR, RIVER_BIOME, ISLE_IN_BIOMES, BIOME_SIZE_WHEN_ISLE, BIOME_RARITY_WHEN_ISLE, BORDER_IN_BIOMES, ONLY_BORDER_NEAR, NOT_BORDER_NEAR, BIOME_SIZE_WHEN_BORDER);
}

