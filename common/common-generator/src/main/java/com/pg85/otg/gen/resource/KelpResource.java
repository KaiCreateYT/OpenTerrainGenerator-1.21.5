package com.pg85.otg.gen.resource;

import com.pg85.otg.config.settings.biome.BiomeSettings;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.interfaces.IWorldGenRegion;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterials;
import com.pg85.otg.util.materials.MaterialProperties;
import com.pg85.otg.util.materials.MaterialSet;

import java.util.List;
import java.util.Random;

public class KelpResource extends FrequencyResourceBase
{
    private final int minHeight;
    private final int maxHeight;
    private final MaterialSet sourceBlocks;


    public KelpResource(BiomeSettings biomeConfig, List<String> args) throws InvalidConfigException
    {
        super(biomeConfig, args);
        assureSize(2, args);

        this.frequency = readInt(args.get(0), 1, 500);
        this.rarity = readRarity(args.get(1));
        if (args.size() >= 4) {
            this.minHeight = readInt(args.get(2), 1, 100);
            this.maxHeight = readInt(args.get(3), 1, 200);
        } else {
            this.minHeight = 1;
            this.maxHeight = 11;
        }
        if (args.size() >= 5) {
            sourceBlocks = readMaterials(args, 4);
        } else {
            sourceBlocks = MaterialSet.of(LocalMaterials.DIRT, LocalMaterials.GRAVEL, LocalMaterials.SAND);
        }
    }

    @Override
    public void spawn(IWorldGenRegion world, Random random, int x, int z)
    {
        int y = world.getBlockAboveSolidHeight(x, z);

        // TODO: sourceblocks
        LocalMaterialData below = world.getMaterial(x, y - 1, z);
        if (below == null || !sourceBlocks.contains(below)) {
            return;
        }

        int height = minHeight + random.nextInt(maxHeight - minHeight);

        // Iterate upwards
        int dy;
        for (int y1 = 0; y1 <= height; y1++) {
            dy = y + y1;

            // Stop if we hit non-water
            if (!world.getMaterial(x, dy, +z).isLiquid()) {
                break;
            }

            // If we hit the surface of the water, place the top and return
            if (!world.getMaterial(x, dy + 1, +z).isLiquid()) {
                if (y1 > 0) {
                    world.setBlock(
                        x,
                        dy,
                        z,
                        LocalMaterials.KELP.withProperty(MaterialProperties.AGE_0_25, 20 + random.nextInt(4))
                    );
                }

                break;
            }

            // Place the top if we're at the top of the column
            if (y1 == height) {
                world.setBlock(
                    x,
                    dy,
                    z,
                    LocalMaterials.KELP.withProperty(MaterialProperties.AGE_0_25, 20 + random.nextInt(4))
                );
            } else {
                world.setBlock(x, dy, z, LocalMaterials.KELP_PLANT);
            }
        }
    }

    @Override
    public String toString()
    {
        return "Kelp(" + this.frequency + ", " + this.rarity + ")";
    }
}
