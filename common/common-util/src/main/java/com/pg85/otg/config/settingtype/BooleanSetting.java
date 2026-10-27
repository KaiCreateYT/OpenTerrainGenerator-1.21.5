package com.pg85.otg.config.settingtype;

import com.pg85.otg.config.settings.ConfigSection;
import com.pg85.otg.exceptions.InvalidConfigException;

import java.util.function.Function;

/**
 * Reads and writes booleans.
 *
 * <p>It can read the values true and false, case insensitive. It will write
 * "true" or "false", always in lowercase.
 */
public class BooleanSetting extends Setting<Boolean>
{
	private final boolean defaultValue;

	public BooleanSetting(String name, boolean defaultValue)
	{
		super(name);
		this.defaultValue = defaultValue;
	}

	public BooleanSetting(String name, boolean defaultValue, Function<ConfigSection, Boolean> getter, String ...description)
	{
		super(name, getter, description);
		this.defaultValue = defaultValue;
	}

	@Override
	public Boolean getDefaultValue()
	{
		return defaultValue;
	}

	@Override
	public Boolean read(String string) throws InvalidConfigException
	{
		if (string.equalsIgnoreCase("true"))
		{
			return Boolean.TRUE;
		}
		if (string.equalsIgnoreCase("false"))
		{
			return Boolean.FALSE;
		}
		throw new InvalidConfigException(string + " is not a boolean");
	}

	@Override
	public String getTypeAsString() {
		return "boolean";
	}

}
