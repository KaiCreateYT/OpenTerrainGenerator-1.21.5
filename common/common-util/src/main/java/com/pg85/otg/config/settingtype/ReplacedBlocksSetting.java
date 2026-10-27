package com.pg85.otg.config.settingtype;

import com.pg85.otg.config.settings.ConfigSection;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.util.biome.ReplaceBlockMatrix;

import java.util.function.Function;

/**
 * Setting that handles {@link ReplaceBlockMatrix}.
 *
 */
public class ReplacedBlocksSetting extends Setting<ReplaceBlockMatrix>
{

	public ReplacedBlocksSetting(String name, Function<ConfigSection, ReplaceBlockMatrix> getter, String ...description)
	{
		super(name, getter, description);
	}

	@Override
	public ReplaceBlockMatrix getDefaultValue()
	{
		return ReplaceBlockMatrix.createEmptyMatrix();
	}

	@Override
	public ReplaceBlockMatrix read(String string) throws InvalidConfigException
	{
		return new ReplaceBlockMatrix(string);
	}

	@Override
	public String getTypeAsString() {
		return "string";
	}
}
