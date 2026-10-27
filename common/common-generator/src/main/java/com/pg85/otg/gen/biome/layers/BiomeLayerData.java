package com.pg85.otg.gen.biome.layers;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.pg85.otg.config.settings.preset.GenerationSettings;
import com.pg85.otg.gen.biome.BiomeData;
import com.pg85.otg.interfaces.IBiome;
import com.pg85.otg.config.settings.biome.BiomeSettings;
import com.pg85.otg.config.settings.preset.DimensionPresetSettings;
import com.pg85.otg.config.settings.preset.ImageSettings;
import com.pg85.otg.config.settings.preset.DimensionPresetInfo;

public class BiomeLayerData
{
	public final ImageSettings imageSettings;
	public final GenerationSettings biomeSettings;
	public final DimensionPresetInfo presetInfo;
	public final Path presetDir;
	public final int[] oceanTemperatures;
	public final BiomeData oceanBiomeData;
	public final Map<Integer, List<BiomeGroup>> groups = new HashMap<>();
    public final int[] groupMaxRarityPerDepth;
	public final int[] oldMaxRarities;
	public final List<Integer> biomeDepths = new ArrayList<>(); // Depths with biomes
	public final Map<Integer, BiomeGroup> groupRegistry;
	public final Map<Integer, List<BiomeData>> isleBiomesAtDepth = new HashMap<>();
	public final Map<Integer, List<BiomeData>> borderBiomesAtDepth = new HashMap<>();
	public final int[] riverBiomes;

	public final int biomeCount;

	// FromImageMode
	public HashMap<Integer, Integer> biomeColorMap;

	// TODO: imageFillBiome is not currently using its setting - always 0
	public int imageFillBiome;


	public BiomeLayerData(Path presetDir,
						  DimensionPresetSettings presetConfig,
						  BiomeSettings oceanBiomeConfig,
						  int[] oceanTemperatures,
						  Map<Integer, BiomeGroup> groupRegistry,
						  Set<Integer> biomeDepths,
						  Map<Integer, List<BiomeGroup>> groupDepth,
						  Map<Integer, List<BiomeData>> isleBiomesAtDepth,
						  Map<Integer, List<BiomeData>> borderBiomesAtDepth,
						  Map<String, List<Integer>> biomeIdsByName,
						  HashMap<Integer, Integer> biomeColorMap,
						  IBiome[] biomes)
	{
		this.presetDir = presetDir;
		this.biomeCount = biomes.length;
		this.biomeSettings = presetConfig.getGenerationSettings();
		this.imageSettings = presetConfig.getImageSettings();
		this.presetInfo = presetConfig.getPresetInfo();

		this.oceanTemperatures = oceanTemperatures;

        int[] cumulativeGroupRarities = new int[biomeSettings.getGenerationDepth() + 1];
		this.groupMaxRarityPerDepth = new int[biomeSettings.getGenerationDepth() + 1];
		this.oldMaxRarities = new int[biomeSettings.getGenerationDepth() + 1];

		if (oceanBiomeConfig == null)
		{
			this.oceanBiomeData = new BiomeData(0, 0, 0, 0, ImmutableList.of(), ImmutableList.of(), ImmutableList.of(), ImmutableList.of());
		} else {
			this.oceanBiomeData = new BiomeData(
				0,
				oceanBiomeConfig.getGenerationSettings().getBiomeRarity(),
				oceanBiomeConfig.getGenerationSettings().getBiomeSize(),
				oceanBiomeConfig.getVisualSettings().getBiomeTemperature(),
				oceanBiomeConfig.getGenerationSettings().getIsleInBiomes(),
				oceanBiomeConfig.getGenerationSettings().getBorderInBiomes(),
				oceanBiomeConfig.getGenerationSettings().getOnlyBorderNear(),
				oceanBiomeConfig.getGenerationSettings().getNotBorderNear()
			);
		}

		this.groupRegistry = groupRegistry;

		this.biomeDepths.addAll(biomeDepths);
		this.groups.putAll(groupDepth);
		this.isleBiomesAtDepth.putAll(isleBiomesAtDepth);
		this.borderBiomesAtDepth.putAll(borderBiomesAtDepth);

		for(Entry<Integer, List<BiomeGroup>> entry : this.groups.entrySet())
		{
			if(entry.getValue() != null)
			{
				int cumulativeRarity = 0;
				for(BiomeGroup group : entry.getValue())
				{
					cumulativeRarity += group.rarity;
					oldMaxRarities[entry.getKey()]++;
				}
				cumulativeGroupRarities[entry.getKey()] = cumulativeRarity;
			}
		}
		for (int depth = 0; depth < cumulativeGroupRarities.length; depth++)
		{
			for (int j = depth; j < cumulativeGroupRarities.length; j++)
			{
				this.groupMaxRarityPerDepth[depth] += cumulativeGroupRarities[j];
			}
		}

		for (int i = 0; i < oldMaxRarities.length; i++)
		{
			oldMaxRarities[i] *= 100;
		}

		// BiomeData name->ID resolution is now done in BiomePlanResolver.resolve()
		// before BiomeLayerData is constructed.

		this.biomeColorMap = biomeColorMap;
		List<Integer> imageFillIds = biomeIdsByName.get(this.imageSettings.getImageFillBiome());
		if (imageFillIds == null || imageFillIds.isEmpty())
		{
			// Fallback to ocean if configured ImageFillBiome is absent from the preset.
			this.imageFillBiome = 0;
		} else {
			this.imageFillBiome = imageFillIds.get(0);
		}

		this.riverBiomes = new int[biomes.length];
		for(int i = 0; i < biomes.length; i++)
		{
			List<Integer> ids = biomeIdsByName.getOrDefault(biomes[i].getBiomeSettings().getGenerationSettings().getRiverBiome(), null);
			this.riverBiomes[i] = ids == null ? -1 : ids.get(0);
		}
	}
}
