package com.pg85.otg.config.settingtype;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.pg85.otg.config.settings.ConfigSection;
import com.pg85.otg.config.yaml.ColorSettingDeserializer;
import com.pg85.otg.config.yaml.ColorSettingSerializer;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.util.Color;

import java.util.function.Function;

/**
 * Reads and writes colors. The colors are represented as integers internally,
 * but are written as hexadecimal colors in upper case, starting with a #.
 *
 * <p>Color reading allows multiple formats. Colors starting with 0x or
 * # are interpreted as hexadecimal numbers, colors starting with 0 as octal
 * numbers and other colors as decimal numbers. Colors are case insensitive.
 *
 */
@JsonSerialize(using = ColorSettingSerializer.class)
@JsonDeserialize(using = ColorSettingDeserializer.class)
public class ColorSetting extends Setting<Color>
{
	private final Color defaultValue;

	public ColorSetting(String name, String defaultValue) {
		super(name);
		this.defaultValue = new Color(defaultValue);
	}

	public ColorSetting(String name, String defaultValue, Function<ConfigSection, Color> getter, String... description) {
		super(name, getter, description);
		this.defaultValue = new Color(defaultValue);
	}

	@Override
	public Color getDefaultValue()
	{
		return defaultValue;
	}

	@Override
	public Color read(String string) throws InvalidConfigException
	{
		return new Color(string);
	}

	@Override
	public String getTypeAsString() {
		return "string";
	}

	@Override
	public String getStringFormat() {
		return "color";
	}

}
