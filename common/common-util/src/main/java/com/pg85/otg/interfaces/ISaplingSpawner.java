package com.pg85.otg.interfaces;

import com.pg85.otg.config.ConfigFunction;
import com.pg85.otg.config.settings.biome.BiomeSettings;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.minecraft.SaplingType;

public abstract class ISaplingSpawner extends ConfigFunction<BiomeSettings>
{
	// TODO: Create interfaces for CustomStructureCache/CustomObjectManager so we can properly 
	// abstract out common-customobjects, using casts in ChunkDecorator and for Saplings atm.
	public abstract boolean hasWideTrunk();
	public abstract SaplingType getSaplingType();
	public abstract LocalMaterialData getSaplingMaterial();
}
