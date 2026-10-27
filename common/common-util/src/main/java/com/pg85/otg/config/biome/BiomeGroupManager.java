package com.pg85.otg.config.biome;

import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.util.OTGLog;
import com.pg85.otg.util.logging.LogCategory;
import java.util.*;

/**
 * Manages a collection of biome groups that are accesible by their name and id.
 */
public final class BiomeGroupManager
{
	static final int MAX_BIOME_GROUP_COUNT = 127;
    private final Map<String, BiomeGroupFunction> nameToGroup = new LinkedHashMap<String, BiomeGroupFunction>(4);
	private final Map<Integer, BiomeGroupFunction> idToGroup = new LinkedHashMap<Integer, BiomeGroupFunction>(4);

	public BiomeGroupManager() { }

	/**
	 * Registers a new group. If the group could not be added (for example
	 * because the maximum amount of biomes has been reached) a message is
	 * logged and the group is not registered.
	 *
	 * @param newGroup The group to register.
	 */
	public void registerGroup(BiomeGroupFunction newGroup)
	{
		ILogger logger = OTGLog.getLogger();
		if (isRoomForMoreGroups())
		{
			BiomeGroupFunction existingWithSameName = nameToGroup.get(newGroup.getName());
			if (existingWithSameName != null)
			{
				logger.error(LogCategory.CONFIGS, "Two biome groups have the same name \"{}\". Removing the second one.", newGroup.getName());
			} else {
				int newGroupId = getNextGroupId();
				newGroup.setGroupId(newGroupId);

				nameToGroup.put(newGroup.getName(), newGroup);
				idToGroup.put(newGroupId, newGroup);
			}
		} else {
			logger.error(LogCategory.CONFIGS, "Biome group \"{}\" could not be added. Max biome group count reached.", newGroup.getName());
		}
	}

	/**
	 * Gets the next group id. This group id is based on which group ids are
	 * currently in used.
	 * @return The next group id.
	 */
	private int getNextGroupId()
	{
		// Adding +1 ensures that the id 0 will never be in use. The id 0
		// seems to be used as a null value by the biome generator
		return getGroupCount() + 1;
	}

	/**
	 * Checks if the next group id will still fit in the group limit.
	 * @return True if the next group id will fit, false otherwise.
	 */
	private boolean isRoomForMoreGroups()
	{
		return getNextGroupId() < MAX_BIOME_GROUP_COUNT;
	}

	/**
	 * Gets the group with the given group id.
	 * @param groupId Id of the group.
	 * @return The group, or null if no such group exists.
	 */
	public BiomeGroupFunction getGroupById(int groupId)
	{
		return idToGroup.get(groupId);
	}

	/**
	 * Gets the group with the given name.
	 * @param name Name of the group, case sensitive.
	 * @return The group.
	 */
	public BiomeGroupFunction getGroupByName(String name)
	{
		return nameToGroup.get(name);
	}

	/**
	 * Gets all groups.
	 * @return All groups.
	 */
	public Collection<BiomeGroupFunction> getGroups()
	{
		return idToGroup.values();
	}

	/**
	 * Gets the amount of groups currently registered. Calling this method is
	 * equivalent to calling {@code getGroups().size()}.
	 * @return The amount of groups.
	 */
	public int getGroupCount()
	{
		return idToGroup.size();
	}

	// TODO: Turn into array?
	private final HashMap<Integer, TreeMap<Integer, BiomeGroupFunction>> cachedGroupDepthMaps = new HashMap<Integer, TreeMap<Integer, BiomeGroupFunction>>();
	public SortedMap<Integer, BiomeGroupFunction> getGroupDepthMap(int depth)
	{
		TreeMap<Integer, BiomeGroupFunction> map = cachedGroupDepthMaps.get(depth);
		if(map != null)
		{
			return map;
		}
		
		map = new TreeMap<Integer, BiomeGroupFunction>();
        int cumulativeGroupRarity = 0;
		for (BiomeGroupFunction group : getGroups())
		{
			if (group.getGenerationDepth() == depth)
			{
				cumulativeGroupRarity += group.getGroupRarity();
				map.put(cumulativeGroupRarity, group);
			}
		}
		if (cumulativeGroupRarity < map.size() * 100)
		{
			map.put(map.size() * 100, null);
		}
		
		cachedGroupDepthMaps.put(depth, map);
		
		return map;
	}

	public boolean isGroupDepthMapEmpty(int depth)
	{
		for (BiomeGroupFunction group : getGroups())
		{
			if (group.getGenerationDepth() == depth)
			{
				return false;
			}
		}
		return true;
	}

	public static int getMaxRarityFromPossibles(Map<Integer, ?> map)
	{
		if (map instanceof TreeMap<Integer, ?> treeMap)
		{
			return treeMap.lastKey();
		}
		// Fallback for non-TreeMap (shouldn't happen in practice)
		return map.keySet().stream().max(Integer::compareTo).orElse(0);
	}

	public void clampGenerationDepth(int maxDepth)
	{
		for (BiomeGroupFunction group : getGroups())
		{
			if (group.getGenerationDepth() > maxDepth)
			{
				group.setGenerationDepth(maxDepth);
			}
		}
	}

	/**
	 * Filters all biome names in the groups. Invalid biomes names will be
	 * removed.
	 *
	 * @param customBiomeNames Set of all custom biomes in the world.
	 */
	public void filterBiomes(List<String> customBiomeNames)
	{
		for (Iterator<BiomeGroupFunction> it = this.idToGroup.values().iterator(); it.hasNext();)
		{
			BiomeGroupFunction group = it.next();
			group.filterBiomes(customBiomeNames);
			if (group.hasNoBiomes())
			{
				it.remove();
			}
		}
	}
}
