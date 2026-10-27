package com.pg85.otg.config.settingtype;

import com.pg85.otg.config.settings.ConfigSection;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.util.helpers.StringHelper;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * Reads and writes a list of strings. Strings are read using
 * {@link StringHelper#readCommaSeperatedString(String)}, and written with a
 * ", " between each string.
 *
 */
public class StringListSetting extends Setting<List<String>>
{
	private final String[] defaultValue;

	public StringListSetting(String name, String... defaultValue)
	{
		super(name);
		this.defaultValue = defaultValue;
	}

	public StringListSetting(String name, String[] defaultValue, Function<ConfigSection, List<String>> getter, String ...description)
	{
		super(name, getter, description);
		this.defaultValue = defaultValue;
	}

	@Override
	public List<String> getDefaultValue()
	{
		return Arrays.asList(defaultValue);
	}

	@Override
	public List<String> read(String string) throws InvalidConfigException
	{
		return Arrays.asList(StringHelper.readCommaSeperatedString(string));
	}

	@Override
	public String write(List<String> value)
	{
		return StringHelper.join(value, ", ");
	}

	@Override
	public String getTypeAsString() {
		return "array";
	}

	@Override
	public String getComplexTypeSchema() {
		return "string";
	}

	@Override
	public String getDefaultValueAsString() {
		if (defaultValue.length == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (String s : defaultValue) {
			sb.append('"');
			sb.append(s);
			sb.append("\", ");
		}
		sb.setLength(sb.length() - 2);
		return sb.toString();
	}
}
