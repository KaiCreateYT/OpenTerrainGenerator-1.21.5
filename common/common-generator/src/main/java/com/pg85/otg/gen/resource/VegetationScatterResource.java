package com.pg85.otg.gen.resource;

import com.pg85.otg.config.settings.biome.BiomeSettings;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.interfaces.IWorldGenRegion;
import com.pg85.otg.util.helpers.RandomHelper;

import java.util.List;
import java.util.Random;

public class VegetationScatterResource extends VegetationResource
{

    public VegetationScatterResource(
        BiomeSettings biomeConfig,
        List<String> args
    ) throws InvalidConfigException
    {
        super(biomeConfig, args, 8);
        assureSize(9, args);

    }

    @Override
    public void spawnForChunkDecoration(IWorldGenRegion world, Random random)
    {
        int outOfBounds = 0;

        for (int t = 0; t < this.frequency; t++) {

            // Rarity Check
            if (random.nextInt(100) >= this.rarity) {
                return;
            }

            // Get placement

            // get x,z and y separately to better group our placement
            int x = world.getDecorationArea().getChunkBeingDecoratedMinX() + random.nextInt(Constants.CHUNK_SIZE);
            int z = world.getDecorationArea().getChunkBeingDecoratedMinZ() + random.nextInt(Constants.CHUNK_SIZE);
            int y = (verticalMode == VerticalMode.Surface) ? world.getHighestSolidBlockAt(x, z) :
                RandomHelper.numberInRange(random, this.minAltitude, this.maxAltitude);

            outOfBounds = spawnPlant(world, x, y, z, outOfBounds);
        }
        logOutOfBounds(world, outOfBounds);
    }

    @Override
    public String toString()
    {
        return "Scatter("
               + plant.toString() + ","
               + verticalMode.toString() + ","
               + environment.toString() + ","
               + frequency + ","
               + rarity + ","
               + minAltitude + ","
               + maxAltitude + ","
               + makeMaterials(sourceBlocks)
               + ")";
    }
}
