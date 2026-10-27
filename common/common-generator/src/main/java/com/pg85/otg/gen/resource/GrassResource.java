package com.pg85.otg.gen.resource;

import com.pg85.otg.config.biome.BiomeResourceBase;
import com.pg85.otg.config.settings.biome.BiomeSettings;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.interfaces.IWorldGenRegion;
import com.pg85.otg.util.OTGMaterialReader;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.MaterialSet;
import com.pg85.otg.util.minecraft.PlantType;

import java.util.List;
import java.util.Random;

public class GrassResource extends BiomeResourceBase implements IBasicResource
{
    private enum GroupOption
    {
        Grouped,
        NotGrouped
    }

    private final int frequency;
    private final double rarity;
    private GroupOption groupOption;
    private PlantType plant;
    private final MaterialSet sourceBlocks;

    public GrassResource(BiomeSettings biomeConfig, List<String> args) throws InvalidConfigException
    {
        super(biomeConfig, args);
        assureSize(5, args);

        // The syntax for the first two arguments used to be blockId,blockData
        // Then it became plantType,unusedParam (plantType can still be blockId:blockData)
        // Now it is plantType,groupOption
        this.groupOption = GroupOption.NotGrouped;
        String secondArgument = args.get(1);
        try {
            // Test whether the second argument is the data value (deprecated)
            readInt(secondArgument, 0, 16);
            // If so, parse it
            this.plant = PlantType.getPlant(args.get(0) + ":" + secondArgument, OTGMaterialReader.get());
        } catch (InvalidConfigException e) {
            // Nope, second argument is not a number
            this.plant = PlantType.getPlant(args.get(0), OTGMaterialReader.get());
            if (secondArgument.equalsIgnoreCase(GroupOption.Grouped.toString())) {
                this.groupOption = GroupOption.Grouped;
                // For backwards compatibility, the second argument is not checked further
            }
        }

        this.frequency = readInt(args.get(2), 1, 500);
        this.rarity = readRarity(args.get(3));
        this.sourceBlocks = readMaterials(args, 4);
    }

    @Override
    public void spawnForChunkDecoration(IWorldGenRegion worldGenRegion, Random random)
    {
        switch (this.groupOption) {
            case Grouped:
                spawnGrouped(worldGenRegion, random);
                break;
            case NotGrouped:
                spawnNotGrouped(worldGenRegion, random);
                break;
        }
    }

    private void spawnGrouped(IWorldGenRegion world, Random random)
    {
        if (random.nextDouble() * 100.0 <= this.rarity) {
            // Passed Rarity test, place about Frequency grass in this chunk
            int centerX = world.getDecorationArea().getChunkBeingDecoratedMinX() + random.nextInt(Constants.CHUNK_SIZE);
            int centerZ = world.getDecorationArea().getChunkBeingDecoratedMinZ() + random.nextInt(Constants.CHUNK_SIZE);
            int centerY = world.getHighestBlockAboveYAt(centerX, centerZ);

            if (centerY < world.getWorldInfo().minY()) {
                return;
            }

            LocalMaterialData worldMaterial;

            // Fix y position
            while (
                (
                    (centerY >= world.getWorldInfo().minY() && centerY <= world.getWorldInfo().maxY()) &&
                    (worldMaterial = world.getMaterial(centerX, centerY, centerZ)) != null &&
                    (
                        worldMaterial.isAir() ||
                        worldMaterial.isLeaves()
                    ) &&
                    world.getMaterial(centerX, centerY - 1, centerZ) != null
                ) && (
                    centerY > 0
                )
            ) {
                centerY--;
            }
            centerY++;

            // Try to place grass
            // Because of the changed y position, only one in four attempts
            // will have success
            int x;
            int y;
            int z;
            for (int i = 0; i < this.frequency * 4; i++) {
                x = centerX + random.nextInt(8) - random.nextInt(8);
                y = centerY + random.nextInt(4) - random.nextInt(4);
                z = centerZ + random.nextInt(8) - random.nextInt(8);
                if (
                    (worldMaterial = world.getMaterial(x, y, z)) != null &&
                    worldMaterial.isAir() &&
                    (
                        (worldMaterial = world.getMaterial(x, y - 1, z)) != null &&
                        this.sourceBlocks.contains(worldMaterial)
                    )
                )
                {
                    this.plant.spawn(world, x, y, z);
                }
            }
        }
    }

    private void spawnNotGrouped(IWorldGenRegion world, Random random)
    {
        LocalMaterialData worldMaterial;
        int x;
        int z;
        int y;
        for (int t = 0; t < this.frequency; t++) {
            if (random.nextInt(100) >= this.rarity) {
                continue;
            }

            x = world.getDecorationArea().getChunkBeingDecoratedMinX() + random.nextInt(Constants.CHUNK_SIZE);
            z = world.getDecorationArea().getChunkBeingDecoratedMinZ() + random.nextInt(Constants.CHUNK_SIZE);
            y = world.getHighestBlockAboveYAt(x, z);

            if (y < world.getWorldInfo().minY()) {
                return;
            }

            while (
                (
                    (worldMaterial = world.getMaterial(x, y, z)) != null &&
                    (
                        worldMaterial.isAir() ||
                        worldMaterial.isLeaves()
                    ) &&
                    world.getMaterial(x, y - 1, z) != null
                ) &&
                y > 0
            ) {
                y--;
            }

            if (
                (
                    (worldMaterial = world.getMaterial(x, y + 1, z)) == null ||
                    !worldMaterial.isAir()
                ) || (
                    (worldMaterial = world.getMaterial(x, y, z)) == null ||
                    !this.sourceBlocks.contains(worldMaterial)
                )
            )
            {
                continue;
            }
            this.plant.spawn(world, x, y + 1, z);
        }
    }

    @Override
    public String toString()
    {
        return "Grass("
               + this.plant.getName()
               + ","
               + this.groupOption
               + ","
               + this.frequency
               + ","
               + this.rarity
               + makeMaterials(this.sourceBlocks)
               + ")";
    }
}
