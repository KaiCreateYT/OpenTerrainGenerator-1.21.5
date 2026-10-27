package com.pg85.otg.config.settingtype;

import com.pg85.otg.config.settings.ConfigSection;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.util.OTGMaterialReader;
import com.pg85.otg.util.OTGLog;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;
import com.pg85.otg.util.materials.LocalMaterialData;

import java.util.function.Function;

/**
 * Reads and writes a material. Materials are read using
 * {@link OTG#readMaterial(String)} and written using
 * {@link LocalMaterialData#toString()}.
 *
 */
public class MaterialSetting extends Setting<LocalMaterialData>
{
	private final String defaultValue;
	private boolean processedMaterial = false;
	private LocalMaterialData defaultMaterial;

	public MaterialSetting(String name, String defaultValue)
	{
		super(name);
		this.defaultValue = defaultValue;
	}

	public MaterialSetting(String name, String defaultValue, Function<ConfigSection, LocalMaterialData> getter, String ...comments)
	{
		super(name, getter, comments);
		this.defaultValue = defaultValue;
	}

	@Override
	public LocalMaterialData getDefaultValue()
	{		
		if(!processedMaterial)
		{
			processedMaterial = true;
			try {
				defaultMaterial = OTGMaterialReader.get().readMaterial(defaultValue);
			} catch (InvalidConfigException e) {
				OTGLog.error(LogCategory.CONFIGS, "Failed to read default material: {}", e.getMessage());
			}
		}
		return defaultMaterial;
	}

	@Override
	public LocalMaterialData read(String string) throws InvalidConfigException
	{
		return OTGMaterialReader.get().readMaterial(string);
	}

	@Override
	public String getTypeAsString() {
		return "string";
	}
}
