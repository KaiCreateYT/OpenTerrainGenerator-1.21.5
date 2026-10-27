package com.pg85.otg.config.settingtype;

import java.util.List;
import java.util.function.Function;

import com.pg85.otg.config.settings.ConfigSection;
import com.pg85.otg.interfaces.ISurfaceGenerator;
import com.pg85.otg.util.Color;
import com.pg85.otg.util.biome.ColorSet;
import com.pg85.otg.util.biome.ReplaceBlockMatrix;
import com.pg85.otg.util.biome.WeightedMobSpawnGroup;
import com.pg85.otg.util.materials.MaterialSet;

/**
 * Acts as a factory for creating settings. Classes holding settings must
 * extends this class and call the appropriate methods to create settings.
 *
 * <p>We might eventually want the class to keep track of all settings
 * created. For now, it just creates instances of the appropriate settings
 * type.
 */
public abstract class Settings
{	
	/**
	 * Creates a setting that can be {@code true} or {@code false}.
	 * @param name		 Name of the setting.
	 * @param defaultValue Default value for the setting.
	 * @return The newly created setting.
	 */
	public static Setting<Boolean> booleanSetting(String name, boolean defaultValue)
	{
		return new BooleanSetting(name, defaultValue);
	}
	public static Setting<Boolean> booleanSetting(String name, boolean defaultValue, Function<ConfigSection, Boolean> getter, String... description)
	{
		return new BooleanSetting(name, defaultValue, getter, description);
	}

	/**
	 * Creates a setting that represents a RGB color.
	 *
	 * @param name         Name of the setting.
	 * @param defaultValue Default value for the setting.
	 * @return The newly created setting.
	 */
	public static Setting<Color> colorSetting(String name, String defaultValue)
	{
		return new ColorSetting(name, defaultValue);
	}
	public static Setting<Color> colorSetting(String name, String defaultValue, Function<ConfigSection, Color> getter, String... description)
	{
		return new ColorSetting(name, defaultValue, getter, description);
	}

	/**
	 * Creates a setting that represents double-precision floating point number.
	 * @param name		 Name of the setting.
	 * @param defaultValue Default value for the setting.
	 * @param min		  Lowest allowed value.
	 * @param max		  Highest allowed value.
	 * @return The newly created setting.
	 */
	public static Setting<Double> doubleSetting(String name, double defaultValue, double min, double max)
	{
		return new DoubleSetting(name, defaultValue, min, max);
	}
	public static Setting<Double> doubleSetting(String name, double defaultValue, double min, double max, Function<ConfigSection, Double> getter, String ...description)
	{
		return new DoubleSetting(name, defaultValue, min, max, getter, description);
	}

	/**
	 * Creates a setting that represents one of the options in the provided enum.
	 * @param name		 Name of the setting.
	 * @param defaultValue Default value for the setting.
	 * @return The newly created setting.
	 */
	public static <T extends Enum<T>> Setting<T> enumSetting(String name, T defaultValue)
	{
		return new EnumSetting<T>(name, defaultValue);
	}
	public static <T extends Enum<T>> Setting<T> enumSetting(String name, T defaultValue, Function<ConfigSection, T> getter, String ...description)
	{
		return new EnumSetting<T>(name, defaultValue, getter, description);
	}

	/**
	 * Creates a setting that represents single-precision floating point number.
	 * @param name		 Name of the setting.
	 * @param defaultValue Default value for the setting.
	 * @param min		  Lowest allowed value.
	 * @param max		  Highest allowed value.
	 * @return The newly created setting.
	 */
	public static Setting<Float> floatSetting(String name, float defaultValue, float min, float max)
	{
		return new FloatSetting(name, defaultValue, min, max);
	}
	public static Setting<Float> floatSetting(String name, float defaultValue, float min, float max, Function<ConfigSection, Float> getter, String ...description)
	{
		return new FloatSetting(name, defaultValue, min, max, getter, description);
	}

	/**
	 * Creates a setting that represents a whole number.
	 * @param name		 Name of the setting.
	 * @param defaultValue Default value for the setting.
	 * @param min		  Lowest allowed value.
	 * @param max		  Highest allowed value.
	 * @return The newly created setting.
	 */
	public static Setting<Integer> intSetting(String name, int defaultValue, int min, int max)
	{
		return new IntSetting(name, defaultValue, min, max);
	}
	public static Setting<Integer> intSetting(String name, int defaultValue, int min, int max, Function<ConfigSection, Integer> getter, String ... description)
	{
		return new IntSetting(name, defaultValue, min, max, getter, description);
	}

	/**
	 * Creates a setting that represents a whole number as a {@code long}.
	 * @param name		 Name of the setting.
	 * @param defaultValue Default value for the setting.
	 * @param min		  Lowest allowed value.
	 * @param max		  Highest allowed value.
	 * @return The newly created setting.
	 */
	public static Setting<Long> longSetting(String name, long defaultValue, long min, long max)
	{
		return new LongSetting(name, defaultValue, min, max);
	}
	public static Setting<Long> longSetting(String name, long defaultValue, long min, long max, Function<ConfigSection, Long> getter, String ...description)
	{
		return new LongSetting(name, defaultValue, min, max, getter, description);
	}

	/**
	 * Creates a setting that represents a set of block materials.
	 * Warning: you will get an AssertionError later on (during config
	 * reading) if you provide invalid materials.
	 * @param name		  Name of the setting.
	 * @param defaultValues Default values for the setting.
	 * @return The newly created setting.
	 */
	protected static Setting<MaterialSet> materialSetSetting(String name, String... defaultValues)
	{
		return new MaterialSetSetting(name, defaultValues);
	}
	protected static Setting<MaterialSet> materialSetSetting(String name, String[] defaultValues, Function<ConfigSection, MaterialSet> getter, String ...description)
	{
		return new MaterialSetSetting(name, defaultValues, getter, description);
	}

	public static Setting<ColorSet> colorSetSetting(String name, Function<ConfigSection, ColorSet> getter, String ...description)
	{
		return new ColorSetSetting(name, getter, description);
	}

	/**
	 * Creates a setting that represents a list of possible mob spawns.
	 * @param name Name of the setting.
	 * @return The newly created setting.
	 */
	public static Setting<List<WeightedMobSpawnGroup>> mobGroupListSetting(String name, Function<ConfigSection, List<WeightedMobSpawnGroup>> getter, String ...description)
	{
		return new MobGroupListSetting(name, getter, description);
	}

	/**
	 * Creates a setting that represents a {@link ReplaceBlockMatrix}.
	 * @param name Name of the setting.
	 * @return The newly created setting.
	 */
	public static Setting<ReplaceBlockMatrix> replacedBlocksSetting(String name, Function<ConfigSection, ReplaceBlockMatrix> getter, String ...description)
	{
		return new ReplacedBlocksSetting(name, getter, description);
	}

	/**
	 * Creates a setting that represents a string of text.
	 * @param name		 Name of the setting.
	 * @param defaultValue Default value for the setting.
	 * @return The newly created setting.
	 */
	public static Setting<String> stringSetting(String name, String defaultValue)
	{
		return new StringSetting(name, defaultValue, null);
	}
	public static Setting<String> stringSetting(String name, String defaultValue, Function<ConfigSection, String> getter, String ...description)
	{
		return new StringSetting(name, defaultValue, getter, description);
	}

	/**
	 * Creates a setting that represents a list of text strings.
	 * @param name		  Name of the setting.
	 * @param defaultValues Default values for the setting.
	 * @return The newly created setting.
	 */
	public static Setting<List<String>> stringListSetting(String name, String... defaultValues)
	{
		return new StringListSetting(name, defaultValues);
	}
	public static Setting<List<String>> stringListSetting(String name, String[] defaultValues, Function<ConfigSection, List<String>> getter, String ...description)
	{
		return new StringListSetting(name, defaultValues, getter, description);
	}

	/**
	 * Creates a setting that represents a {@link ISurfaceGenerator}.
	 * @param name Name of the setting.
	 * @return The newly created setting.
	 */
	public static Setting<ISurfaceGenerator> surfaceGeneratorSetting(String name, Function<ConfigSection, ISurfaceGenerator> getter, String ...comments)
	{
		return new SurfaceGeneratorSetting(name, getter, comments);
	}
}
