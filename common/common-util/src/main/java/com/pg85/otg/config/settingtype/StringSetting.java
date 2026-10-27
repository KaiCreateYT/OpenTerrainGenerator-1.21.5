package com.pg85.otg.config.settingtype;

import com.pg85.otg.config.settings.ConfigSection;
import com.pg85.otg.exceptions.InvalidConfigException;

import java.util.function.Function;

/**
 * Reads and writes a string. Surrounding whitespace is stripped using
 * {@link String#trim()}.
 *
 */
public class StringSetting extends Setting<String>
{
	private final String defaultValue;

	public StringSetting(String name, String defaultValue)
	{
		super(name);
		this.defaultValue = defaultValue;
	}

	public StringSetting(String name, String defaultValue, Function<ConfigSection, String> getter, String ...description)
	{
		super(name, getter, description);
		this.defaultValue = defaultValue;
	}

	@Override
	public String getDefaultValue()
	{
		return defaultValue;
	}

	@Override
	public String read(String string) throws InvalidConfigException
	{
		return string.trim();
	}

	@Override
	public String getTypeAsString() {
		return "string";
	}

}
