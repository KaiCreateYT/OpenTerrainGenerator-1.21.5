package com.pg85.otg.config.biome;

import com.pg85.otg.config.ConfigFunction;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.config.settings.preset.DimensionPresetSettings;
import com.pg85.otg.util.helpers.StringHelper;
import com.pg85.otg.util.logging.LogCategory;
import java.util.*;

public final class TemplateBiome extends ConfigFunction<DimensionPresetSettings>
{
	private String name;
	private double minTemp;
	private double maxTemp;
	private final List<String> tags = new ArrayList<String>();

	public TemplateBiome(DimensionPresetSettings config, List<String> args) throws InvalidConfigException
	{
		assureSize(2, args);
		this.name = args.get(0);

		try
		{
			this.minTemp = readDouble(args.get(args.size() - 2), Integer.MIN_VALUE, Integer.MAX_VALUE);
			this.maxTemp = readDouble(args.get(args.size() - 1), Integer.MIN_VALUE, Integer.MAX_VALUE);
			args = args.subList(0, args.size() - 2);
		}
		catch(InvalidConfigException ex)
		{
			this.minTemp = 0;
			this.maxTemp = 0;
		}

		for (String biome : readTags(args, 1))
		{
			this.tags.add(biome);
		}
	}

	public List<String> getTags()
	{
		return this.tags;
	}

	public boolean temperatureAllowed(float baseTemperature)
	{
		if(this.minTemp != 0 || this.maxTemp != 0)
		{
			return Math.floor(baseTemperature * 100) >= Math.floor(this.minTemp * 100) && Math.floor(baseTemperature * 100) <= Math.floor(this.maxTemp * 100);
		}
		return true;
	}
	
	@Override
	public String toString()
	{
		return "TemplateBiome(" + this.name + ", " + StringHelper.join(this.tags, ", ") + (this.minTemp == 0 ? "" : ", " + this.minTemp)  + (this.maxTemp == 0 ? "" : ", " + this.maxTemp) + ")";
	}

	private List<String> readTags(List<String> strings, int start) throws InvalidConfigException
	{
		return new ArrayList<String>(strings.subList(start, strings.size()));
	}

	public String getName()
	{
		return this.name;
	}

	void filterBiomes(ArrayList<String> customBiomeNames, ILogger logger)
	{
		for (Iterator<String> it = this.tags.iterator(); it.hasNext();)
		{
			String biomeName = it.next();
			if(biomeName != null && biomeName.trim().length() > 0)
			{
				// Previously also checked BiomeRegistryNames.Contain() — removed,
				// was a 430-line switch frozen at 1.16.5 (missing 1.17+ biomes).
				if (
					customBiomeNames.contains(biomeName) ||
					biomeName.contains(":") || // Non-otg biome registry name
					biomeName.toLowerCase().startsWith(Constants.MOD_LABEL) ||
					biomeName.toLowerCase().startsWith(Constants.BIOME_CATEGORY_LABEL) ||
					biomeName.toLowerCase().startsWith(Constants.MOD_BIOME_CATEGORY_LABEL) ||
					biomeName.toLowerCase().startsWith(Constants.MC_BIOME_CATEGORY_LABEL) ||					
					biomeName.toLowerCase().startsWith(Constants.BIOME_DICT_TAG_LABEL) ||
					biomeName.toLowerCase().startsWith(Constants.MOD_BIOME_DICT_TAG_LABEL) ||
					biomeName.toLowerCase().startsWith(Constants.MC_BIOME_DICT_TAG_LABEL)
				)
				{
					continue;
				}
				// Invalid biome name, remove
				logger.error(LogCategory.CONFIGS, "Invalid tag name {} in TemplateBiome() {}", biomeName, this.name);
			}
			it.remove();
		}
	}
}
