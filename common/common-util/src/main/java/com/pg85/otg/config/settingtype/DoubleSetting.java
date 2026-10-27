package com.pg85.otg.config.settingtype;

import com.pg85.otg.config.settings.ConfigSection;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.util.helpers.StringHelper;

import java.util.function.Function;

/**
 * Reads and writes a single double number.
 *
 * <p>Numbers are limited to the given min and max values.
 */
public class DoubleSetting extends Setting<Double>
{
	private final double defaultValue;
	private final double minValue;
	private final double maxValue;

	public DoubleSetting(String name, double defaultValue, double minValue, double maxValue)
	{
		super(name);
		this.defaultValue = defaultValue;
		this.minValue = minValue;
		this.maxValue = maxValue;
	}

	public DoubleSetting(String name, double defaultValue, double minValue, double maxValue, Function<ConfigSection, Double> getter, String ...description)
	{
		super(name, getter, description);
		this.defaultValue = defaultValue;
		this.minValue = minValue;
		this.maxValue = maxValue;
	}

	@Override
	public Double getDefaultValue()
	{
		return defaultValue;
	}

	@Override
	public Double read(String string) throws InvalidConfigException
	{
		return StringHelper.readDouble(string, minValue, maxValue);
	}

	@Override
	public String getTypeAsString() {
		return "number";
	}

	@Override
	public Double getMinValue()
	{
		return minValue;
	}

	@Override
	public Double getMaxValue()
	{
		return maxValue;
	}	
}
