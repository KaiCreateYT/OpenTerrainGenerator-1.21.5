package com.pg85.otg.gen.resource;

import com.pg85.otg.config.settings.biome.BiomeSettings;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.interfaces.IWorldGenRegion;
import com.pg85.otg.util.helpers.RandomHelper;

import java.util.List;
import java.util.Random;

public class VegetationGroupResource extends VegetationResource
{

    private final int horizontalSpread;
    private final int verticalSpread;
    private final int groupSize;
    private final int variance;

    public VegetationGroupResource(
        BiomeSettings biomeConfig,
        List<String> args
    ) throws InvalidConfigException
    {
        super(biomeConfig, args, 12);
        assureSize(13, args);
        horizontalSpread = readInt(args.get(8), 1, 15);
        verticalSpread = readInt(args.get(9), 1, 100);
        groupSize = readInt(args.get(10), 1, 100);
        variance = readInt(args.get(11), 0, 100);
    }

    @Override
    public String toString()
    {
        return "Group("
               + plant.toString() + ","
               + verticalMode.toString() + ","
               + environment.toString() + ","
               + frequency + ","
               + rarity + ","
               + minAltitude + ","
               + maxAltitude + ","
               + horizontalSpread + ","
               + verticalSpread + ","
               + groupSize + ","
               + variance + ","
               + makeMaterials(sourceBlocks)
               + ")";
    }

    @Override
    public void spawnForChunkDecoration(IWorldGenRegion world, Random random)
    {
        int outOfBounds = 0;

        for (int i = 0; i < this.frequency; i++) {
            // Rarity Check
            if (random.nextInt(100) >= this.rarity) {
                return;
            }

            int originX = world.getDecorationArea().getChunkBeingDecoratedMinX() + random.nextInt(Constants.CHUNK_SIZE);
            int originZ = world.getDecorationArea().getChunkBeingDecoratedMinZ() + random.nextInt(Constants.CHUNK_SIZE);

            int originY = (verticalMode == VerticalMode.Surface) ? world.getHighestSolidBlockAt(originX, originZ) :
                RandomHelper.numberInRange(random, this.minAltitude, this.maxAltitude);

            int size = RandomHelper.numberInRange(random, this.groupSize - variance, this.groupSize + variance);

            for (int t = 0; t < size; t++) {

                // Get placement

                // get x,z and y separately to better group our placement
                int x = originX + random.nextInt(horizontalSpread) - random.nextInt(horizontalSpread);
                int z = originZ + random.nextInt(horizontalSpread) - random.nextInt(horizontalSpread);
                int y = originY + random.nextInt(verticalSpread) - random.nextInt(verticalSpread);

                outOfBounds = spawnPlant(world, x, y, z, outOfBounds);
            }
        }

        logOutOfBounds(world, outOfBounds);

    }
}
