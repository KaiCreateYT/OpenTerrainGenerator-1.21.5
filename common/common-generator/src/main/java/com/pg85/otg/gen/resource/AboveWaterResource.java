package com.pg85.otg.gen.resource;

import com.pg85.otg.config.settings.biome.BiomeSettings;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.interfaces.IWorldGenRegion;
import com.pg85.otg.util.OTGMaterialReader;
import com.pg85.otg.util.materials.LocalMaterialData;

import java.util.List;
import java.util.Random;

public class AboveWaterResource extends FrequencyResourceBase
{
    private final LocalMaterialData material;

    public AboveWaterResource(BiomeSettings config, List<String> args) throws InvalidConfigException
    {
        super(config, args);
        assureSize(3, args);

        this.material = OTGMaterialReader.get().readMaterial(args.get(0));
        this.frequency = readInt(args.get(1), 1, 100);
        this.rarity = readRarity(args.get(2));
    }

    @Override
    public void spawn(IWorldGenRegion world, Random rand, int x, int z)
    {
        int y = world.getBlockAboveLiquidHeight(x, z);
        if (y <= world.getWorldInfo().getEmptyChunkIndex()) {
            return;
        }

        LocalMaterialData worldMaterial;
        LocalMaterialData worldMaterialBeneath;
        int localX;
        int localY;
        int localZ;
        for (int i = 0; i < 10; i++) {
            localX = x + rand.nextInt(8) - rand.nextInt(8);
            localY = y + rand.nextInt(4) - rand.nextInt(4);
            localZ = z + rand.nextInt(8) - rand.nextInt(8);

            worldMaterial = world.getMaterial(localX, localY, localZ);
            if (worldMaterial == null || !worldMaterial.isAir()) {
                continue;
            }

            worldMaterialBeneath = world.getMaterial(localX, localY - 1, localZ);
            if (worldMaterialBeneath != null && !worldMaterialBeneath.isLiquid()) {
                continue;
            }

            world.setBlock(localX, localY, localZ, this.material);
        }
    }

    @Override
    public String toString()
    {
        return "AboveWaterRes(" + this.material + "," + this.frequency + "," + this.rarity + ")";
    }
}
