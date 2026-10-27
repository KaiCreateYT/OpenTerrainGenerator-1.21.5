package com.pg85.otg.config.settingtype;

import com.pg85.otg.config.settings.ConfigSection;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.gen.surface.IcebergSurfaceGenerator;
import com.pg85.otg.gen.surface.MesaSurfaceGenerator;
import com.pg85.otg.gen.surface.MultipleLayersSurfaceGenerator;
import com.pg85.otg.gen.surface.SimpleSurfaceGenerator;
import com.pg85.otg.interfaces.ISurfaceGenerator;
import com.pg85.otg.util.OTGMaterialReader;
import com.pg85.otg.util.helpers.StringHelper;

import java.util.function.Function;

/**
 * Setting that handles the {@link ISurfaceGenerator}.
 *
 */
public class SurfaceGeneratorSetting extends Setting<ISurfaceGenerator>
{
	SurfaceGeneratorSetting(String name, Function<ConfigSection, ISurfaceGenerator> getter, String ...comments)
	{
		super(name, getter, comments);
	}

	@Override
	public ISurfaceGenerator getDefaultValue()
	{
		return new SimpleSurfaceGenerator();
	}

	@Override
	public ISurfaceGenerator read(String string) throws InvalidConfigException
	{
		if (!string.isEmpty())
		{
			ISurfaceGenerator mesa = MesaSurfaceGenerator.getFor(string);
			if (mesa != null)
			{
				return mesa;
			}

			ISurfaceGenerator iceberg = IcebergSurfaceGenerator.getFor(string, OTGMaterialReader.get());
			if (iceberg != null)
			{
				return iceberg;
			}

			String[] parts = StringHelper.readCommaSeperatedString(string);
			return new MultipleLayersSurfaceGenerator(parts, OTGMaterialReader.get());
		}
		return new SimpleSurfaceGenerator();
	}

	@Override
	public String getTypeAsString() {
		return "string";
	}
}
