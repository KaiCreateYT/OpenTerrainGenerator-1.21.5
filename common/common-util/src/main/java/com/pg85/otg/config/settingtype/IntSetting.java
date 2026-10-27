package com.pg85.otg.config.settingtype;

import com.pg85.otg.config.settings.ConfigSection;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.util.helpers.StringHelper;

import java.util.function.Function;

/**
 * Reads and writes a single integer.
 *
 * <p>Numbers are limited to the given min and max values.
 */
public class IntSetting extends Setting<Integer>
{
	private final int defaultValue;
	private final int minValue;
	private final int maxValue;

	public IntSetting(String name, int defaultValue, int minValue, int maxValue)
	{
		super(name);
		this.defaultValue = defaultValue;
		this.minValue = minValue;
		this.maxValue = maxValue;
	}
	public IntSetting(String name, int defaultValue, int minValue, int maxValue, Function<ConfigSection, Integer> getter, String ... description)
	{
		super(name, getter, description);
		this.defaultValue = defaultValue;
		this.minValue = minValue;
		this.maxValue = maxValue;
	}

	@Override
	public Integer getDefaultValue()
	{
		return defaultValue;
	}

	@Override
	public Integer read(String string) throws InvalidConfigException
	{
		return StringHelper.readInt(string, minValue, maxValue);
	}

	@Override
	public String getTypeAsString() {
		return "integer";
	}

	@Override
	public Integer getMinValue()
	{
		return minValue;
	}

	@Override
	public Integer getMaxValue()
	{
		return maxValue;
	}
}
