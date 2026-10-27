package com.pg85.otg.config.settingtype;

import com.pg85.otg.config.settings.ConfigSection;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.util.helpers.StringHelper;

import java.util.function.Function;

/**
 * Reads and writes a single float number.
 *
 * <p>Numbers are limited to the given min and max values.
 */
public class FloatSetting extends Setting<Float>
{
	private final float defaultValue;
	private final float minValue;
	private final float maxValue;

	public FloatSetting(String name, float defaultValue, float minValue, float maxValue)
	{
		super(name);
		this.defaultValue = defaultValue;
		this.minValue = minValue;
		this.maxValue = maxValue;
	}
	public FloatSetting(String name, float defaultValue, float minValue, float maxValue, Function<ConfigSection, Float> getter, String ...description)
	{
		super(name, getter, description);
		this.defaultValue = defaultValue;
		this.minValue = minValue;
		this.maxValue = maxValue;
	}


	@Override
	public Float getDefaultValue()
	{
		return defaultValue;
	}

	@Override
	public Float read(String string) throws InvalidConfigException
	{
		return (float) StringHelper.readDouble(string, minValue, maxValue);
	}

	@Override
	public String getTypeAsString() {
		return "number";
	}

	@Override
	public Float getMinValue()
	{
		return minValue;
	}

	@Override
	public Float getMaxValue()
	{
		return maxValue;
	}	  
}
