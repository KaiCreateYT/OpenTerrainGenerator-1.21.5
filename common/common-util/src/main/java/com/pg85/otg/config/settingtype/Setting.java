package com.pg85.otg.config.settingtype;

import com.pg85.otg.config.settings.ConfigSection;
import com.pg85.otg.exceptions.InvalidConfigException;
import lombok.Getter;

import java.util.List;
import java.util.function.Function;

/**
 * Represents a setting. Can parse from a string and save to a string.
 *
 * @param <T> The type of the setting. Int/String/Etc.
 */
@Getter
public abstract class Setting<T>
{
    /**
     * -- GETTER --
     *  The name of this setting, like BiomeHeight.
     */
    private final String name;
	private Function<ConfigSection, T> getter;
	private String[] description;

	public String fetch(ConfigSection section) {
		return write(getter.apply(section));
	}
	
	protected Setting(String name)
	{
		this.name = name;
	}

	protected Setting(String name, Function<ConfigSection, T> getter, String ... description)
	{
		this.name = name;
		this.getter = getter;
		this.description = description;
	}

	/**
	 * Gets the default value of the setting.
	 * @return The default value.
	 */
	public abstract T getDefaultValue();

    /**
	 * Reads the given setting from a string.
	 *
	 * @param string The value of the setting.
	 * @return The parsed setting.
	 * @throws InvalidConfigException If the setting is invalid.
	 */
	public abstract T read(String string) throws InvalidConfigException;

	/**
	 * Returns the {@link #getName() name}.
	 */
	@Override
	public final String toString()
	{
		return getName();
	}

	/**
	 * Gets the value of this setting as a string. The {@link #read(String)}
	 * method must accept all possible values returned by this method, and
	 * a round trip <code>T value = setting.read(setting.write(oldValue))
	 * </code> must produce a value that is equal to oldValue.
	 *
	 * <p>The default implementation simply calls <code>String.valueOf(value)
	 * </code>, but more sophisticated approaches can be made.
	 * @param value The setting.
	 * @return The value.
	 */
	public String write(T value)
	{
		return String.valueOf(value);
	}



	/**
	 * Get the default value as a string - used for YAML schema generation
	 * @return The default value as a string
	 */
	public String getDefaultValueAsString() {
		return write(getDefaultValue());
	}

	/**
	 * Get the type of the setting as a string - used for YAML schema generation
	 * @return The type of the setting as a string
	 */
	public abstract String getTypeAsString();

	/**
	 * Get the format of the setting as a string - used for YAML schema generation
	 * @return The format of the setting as a string, or null if not present
	 */
	public String getStringFormat() {
		return null; // Override in subclasses if applicable
	}

	/**
	 * Get the minimum value of the setting, if present - used for YAML schema generation
	 * @return The minimum value of the setting, or null if not present
	 */
	public Number getMinValue() {
		return null; // Override in subclasses if applicable
	}

	/**
	 * Get the maximum value of the setting, if present - used for YAML schema generation
	 * @return The maximum value of the setting, or null if not present
	 */
	public Number getMaxValue() {
		return null; // Override in subclasses if applicable
	}

	/**
	 * Get the enum values of the setting, if present - used for YAML schema generation
	 * @return The enum values of the setting, or null if not present
	 */
	public List<String> getEnumValues() {
		return null; // Override in subclasses if applicable
	}

	/**
	 * Get the schema for the complex type of the setting, if present - used for YAML schema generation
	 * @return The schema for the complex type of the setting, or null if not present
	 */
	public String getComplexTypeSchema() {
		return null; // Override in subclasses if applicable
	}

}
