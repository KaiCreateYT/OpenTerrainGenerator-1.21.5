package com.pg85.otg.config.biome;

import java.util.ArrayList;
import java.util.List;

import com.pg85.otg.config.ConfigFunction;
import com.pg85.otg.config.settings.biome.BiomeSettings;
import com.pg85.otg.util.gen.OTGWorldInfo;

/** Represents a BiomeConfig ResourceQueue resource. */
public abstract class BiomeResourceBase extends ConfigFunction<BiomeSettings>
{
    protected final BiomeSettings biomeSettings;

    static BiomeResourceBase createResource (
			BiomeSettings config,
			Class<? extends BiomeResourceBase> clazz,
			OTGWorldInfo otgWorldInfo,
			Object... args
	) {
		List<String> stringArgs = new ArrayList<String>(args.length);
		for (Object arg : args)
		{
			stringArgs.add("" + arg);
		}

		try
		{
			return clazz.getConstructor(BiomeSettings.class, List.class, OTGWorldInfo.class)
						.newInstance(config, stringArgs, otgWorldInfo);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	// We're using reflection to match constructors for resources, so resource classes must implement this 
	// constructor or createResource / com.pg85.otg.config.biome.BiomeResourcesManager.getConfigFunction() will fail. 
	public BiomeResourceBase(BiomeSettings biomeSettings, List<String> args) {
        this.biomeSettings = biomeSettings;
    }
}
