package com.pg85.otg.config.settingtype;

import com.pg85.otg.config.settings.ConfigSection;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.util.biome.ColorSet;
import com.pg85.otg.util.biome.SimpleColorSet;
import com.pg85.otg.util.helpers.StringHelper;

import java.util.function.Function;

public class ColorSetSetting extends Setting<ColorSet>
{

	@Override
	public ColorSet getDefaultValue()
	{
		return new ColorSet();
	}

	public ColorSetSetting(String name, Function<ConfigSection, ColorSet> getter, String ...description)
	{
		super(name, getter, description);
	}

	@Override
	public ColorSet read(String string) throws InvalidConfigException
	{
		return new SimpleColorSet(StringHelper.readCommaSeperatedString(string));
	}

	@Override
	public String getTypeAsString() {
		return "object";
	}

	@Override
	public String getComplexTypeSchema() {
		return "ColorSet";
	}
}
