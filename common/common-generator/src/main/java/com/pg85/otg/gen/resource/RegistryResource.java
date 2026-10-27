package com.pg85.otg.gen.resource;

import com.pg85.otg.config.biome.BiomeResourceBase;
import com.pg85.otg.config.settings.biome.BiomeSettings;
import com.pg85.otg.exceptions.InvalidConfigException;

import java.util.List;

public class RegistryResource extends BiomeResourceBase
{
    private final String registryKey;
    private final String decorationStage;

    public RegistryResource(BiomeSettings biomeConfig, List<String> args) throws InvalidConfigException
    {
        super(biomeConfig, args);
        assureSize(1, args);

        this.registryKey = args.get(0);
        if (args.size() > 1) {
            this.decorationStage = args.get(1);
        } else {
            this.decorationStage = "VEGETAL_DECORATION";
        }
    }

    public String getDecorationStage()
    {
        return this.decorationStage;
    }

    public String getFeatureKey()
    {
        return this.registryKey;
    }

    @Override
    public String toString()
    {
        return "Registry(" + this.registryKey + "," + this.decorationStage + ")";
    }
}
