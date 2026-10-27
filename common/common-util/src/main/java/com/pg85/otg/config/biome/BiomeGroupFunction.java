package com.pg85.otg.config.biome;

import com.pg85.otg.config.ConfigFunction;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.config.settings.preset.DimensionPresetSettings;
import com.pg85.otg.util.OTGLog;
import com.pg85.otg.util.helpers.StringHelper;
import com.pg85.otg.util.logging.LogCategory;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

/**
 * Biomes are spawned in groups so that biomes of the same type are near each
 * other, and completely different biomes (desert vs taiga) don't spawn next
 * to each other.
 *
 * <p>This class represents such a biome group.
 */
public final class BiomeGroupFunction extends ConfigFunction<DimensionPresetSettings>
{
    /**
     * -- GETTER --
     *  Gets the numerical id for this group. Group ids are sequential and
     *  based on the order they are placed in the configuration files.
     *
     * @return The numerical id.
     */
    @Getter
    private int groupId;
    /**
     * -- GETTER --
     *  Gets the name of this biome group.
     *
     * @return The name.
     */
    @Getter
    private String name;
	@Getter
    private int groupRarity;
	@Getter
	@Setter
    private int generationDepth = 0;
	private double minTemp = 0;
	private double maxTemp = 0;
	@Getter
    private final List<String> biomes = new ArrayList<String>();

	/**
	 * Loads the biome group using the provided settings.
	 *
	 * @param config The world config.
	 * @param args   The settings to be parsed.
	 * @throws InvalidConfigException When the config is invalid.
	 * @see #BiomeGroupFunction(DimensionPresetSettings, String, int, int, List) Constructor to
	 * properly initialize this biome group manually.
	 */
	public BiomeGroupFunction(DimensionPresetSettings config, List<String> args) throws InvalidConfigException
	{
		// Must have at least a GroupName and a Biome that belongs to it
		assureSize(4, args);
		this.name = args.get(0);
		// In future, would want to read the generation depth from the config
		// For now, since it is currently being initialized, we instead clamp it later using BiomeGroupManager::clampGenerationDepth
		this.generationDepth = readInt(args.get(1), 0, Integer.MAX_VALUE);//config.getGenerationSettings().getGenerationDepth());
		this.groupRarity = readInt(args.get(2), 1, Integer.MAX_VALUE);
		
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
		
		for (String biome : readBiomes(args, 3))
		{
			this.biomes.add(biome);
		}
	}

	/**
	 * Creates a new <code>BiomeGroup</code>.
	 * @param config	DimensionPresetConfig this biome group is part of.
	 * @param groupName The name of this group.
	 * @param size	  Size value of this biome group.
	 * @param rarity	Rarity value of this biome group.
	 * @param biomes	List of names of the biomes that spawn in this group.
	 */
	public BiomeGroupFunction(DimensionPresetSettings config, String groupName, int size, int rarity, List<String> biomes)
	{
		this.name = groupName;
		this.generationDepth = size;
		this.groupRarity = rarity;
		this.minTemp = 0;
		this.maxTemp = 0;
        this.biomes.addAll(biomes);
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
		return "BiomeGroup(" + name + ", " + generationDepth + ", " + groupRarity + ", " + StringHelper.join(biomes, ", ") + (this.minTemp == 0 ? "" : ", " + this.minTemp)  + (this.maxTemp == 0 ? "" : ", " + this.maxTemp) + ")";
	}

	/**
	 * Reads all biomes from the start position until the end of the
	 * list.
	 * @param strings The input strings.
	 * @param start	The position to start. The first element in the list has index 0, the last one size() - 1.
	 * @return All biome names.
	 * @throws InvalidConfigException If one of the elements in the list is not a valid block id.
	 */
	private List<String> readBiomes(List<String> strings, int start) throws InvalidConfigException
	{
		return new ArrayList<String>(strings.subList(start, strings.size()));
	}

    /**
	 * Filters the biomes in this group, removing all biomes that have an
	 * unrecognized name.
	 *
	 * @param customBiomeNames Set of known custom biomes.
	 */
	void filterBiomes(List<String> customBiomeNames)
	{
		for (Iterator<String> it = this.biomes.iterator(); it.hasNext();)
		{
			String biomeName = it.next();
			if(biomeName != null && !biomeName.trim().isEmpty())
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
				OTGLog.error(LogCategory.CONFIGS, "Invalid biome name {} in biome group {}", biomeName, this.name);
			}
			it.remove();
		}
	}

	/**
	 * Gets whether this group contains the given biome. Biome name is case
	 * sensitive.
	 * @param name Name of the biome.
	 * @return True if this group contains the given biome, false otherwise.
	 */
	public boolean containsBiome(String name)
	{
		return this.biomes.contains(name);
	}

	/**
	 * Sets the group id to the given value. Don't use this if the group is
	 * already registered in a collection.
	 * @param groupId The new group id.
	 * @throws IllegalArgumentException If the group id is larger than
	 * {@link BiomeGroupManager#MAX_BIOME_GROUP_COUNT}.
	 */
	void setGroupId(int groupId)
	{
		if (groupId > BiomeGroupManager.MAX_BIOME_GROUP_COUNT)
		{
			throw new IllegalArgumentException("Tried to set group id to " + groupId
					+ ", max allowed is " + BiomeGroupManager.MAX_BIOME_GROUP_COUNT);
		}

		this.groupId = groupId;
	}

    /**
	 * Gets whether this group has any biomes.
	 * @return True if the group has no biomes and is thus empty, false
	 * if the group has biomes.
	 */
	boolean hasNoBiomes()
	{
		return biomes.isEmpty();
	}
}
