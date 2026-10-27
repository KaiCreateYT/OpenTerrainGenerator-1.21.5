package com.pg85.otg.config.settingtype;

import com.pg85.otg.config.settings.ConfigSection;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.util.biome.WeightedMobSpawnGroup;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Reads and writes a list of mobs. Mobs are read using
 * {@link WeightedMobSpawnGroup#fromJson(String)} and written using
 * {@link WeightedMobSpawnGroup#toJson(List)}.
 *
 */
public class MobGroupListSetting extends Setting<List<WeightedMobSpawnGroup>>
{

	public MobGroupListSetting(String name)
	{
		super(name);
	}

	public MobGroupListSetting(String name, Function<ConfigSection, List<WeightedMobSpawnGroup>> getter, String ...description)
	{
		super(name, getter, description);
	}

	@Override
	public List<WeightedMobSpawnGroup> getDefaultValue()
	{
		return Collections.emptyList();
	}

	@Override
	public List<WeightedMobSpawnGroup> read(String string) throws InvalidConfigException
	{
		return WeightedMobSpawnGroup.fromJson(string);
	}

	@Override
	public String write(List<WeightedMobSpawnGroup> groups)
	{
		return WeightedMobSpawnGroup.toJson(groups);
	}

	@Override
	public String getTypeAsString() {
		return "array";
	}

	@Override
	public String getComplexTypeSchema() {
		return "WeightedMobSpawnGroup";
	}
}
