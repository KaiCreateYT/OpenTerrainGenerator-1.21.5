package com.pg85.otg.config.settingtype;

import java.util.ArrayList;
import java.util.function.Function;

import com.pg85.otg.config.settings.ConfigSection;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.util.OTGMaterialReader;
import com.pg85.otg.util.OTGLog;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;
import com.pg85.otg.util.helpers.StringHelper;
import com.pg85.otg.util.materials.LocalMaterialData;

/**
 * Reads and writes a material. Materials are read using
 * {@link OTG#readMaterial(String)} and written using
 * {@link LocalMaterialData#toString()}.
 */
public class MaterialListSetting extends Setting<ArrayList<LocalMaterialData>>
{
	private final String[] defaultValue;
	private boolean processedMaterials;
	private ArrayList<LocalMaterialData> defaultMaterials;

	public MaterialListSetting(String name, String[] defaultValue)
	{
		super(name);
		this.defaultValue = defaultValue;
	}

	public MaterialListSetting(String name, String[] defaultValue, Function<ConfigSection, ArrayList<LocalMaterialData>> getter, String ...comments)
	{
		super(name, getter, comments);
		this.defaultValue = defaultValue;
	}

	@Override
	public ArrayList<LocalMaterialData> getDefaultValue()
	{
		if(!this.processedMaterials)
		{
			this.processedMaterials = true;
			ArrayList<LocalMaterialData> materials = new ArrayList<LocalMaterialData>();
			for(String defaultMaterial : this.defaultValue)
			{
				LocalMaterialData material = null;
				try {
					material = OTGMaterialReader.get().readMaterial(defaultMaterial);
				} catch (InvalidConfigException e) {
					OTGLog.error(LogCategory.CONFIGS, "Failed to read default material: {}", e.getMessage());
				}
				if(material != null)
				{
					materials.add(material);
				}
			}
			this.defaultMaterials = materials;
		}
		return this.defaultMaterials;
	}

	@Override
	public ArrayList<LocalMaterialData> read(String string) throws InvalidConfigException
	{
		String[] materialNames = string.split(",(?![^\\(\\[]*[\\]\\)])"); // Splits on any comma not inside brackets
		ArrayList<LocalMaterialData> materials = new ArrayList<LocalMaterialData>();
		for(String materialName : materialNames)
		{
			LocalMaterialData material = OTGMaterialReader.get().readMaterial(materialName.trim());
			materials.add(material);
		}
		return materials;
	}
	
	@Override
	public String write(ArrayList<LocalMaterialData> value)
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
