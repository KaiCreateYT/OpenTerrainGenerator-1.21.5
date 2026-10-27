package com.pg85.otg.gen.resource;

import com.pg85.otg.config.biome.BiomeResourceBase;
import com.pg85.otg.config.settings.biome.BiomeSettings;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.interfaces.IWorldGenRegion;
import com.pg85.otg.util.OTGMaterialReader;
import com.pg85.otg.util.helpers.MathHelper;
import com.pg85.otg.util.helpers.StringHelper;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterials;

import java.util.List;
import java.util.Random;

public class IcebergResource extends BiomeResourceBase implements IBasicResource
{

    private record IcebergContext(
        IWorldGenRegion world,
        Random random,
        int x,
        int y,
        int z,
        int icebergHeight,
        int maxRadius,
        int baseRadius,
        int variation,
        double rotationAngle,
        boolean isSmoothIceberg,
        int radius,
        boolean useBothMaterials,
        LocalMaterialData material,
        LocalMaterialData material2
    ) {}

    private final LocalMaterialData[] materials;
    private final LocalMaterialData[] materials2;
    private final double[] rarities;
    private final double totalRarity;

    public IcebergResource(BiomeSettings biomeConfig, List<String> args)
        throws InvalidConfigException
    {
        super(biomeConfig, args);
        assureSize(4, args);

        int size = (int) Math.floor(args.size() / 3.0);
        this.materials = new LocalMaterialData[size];
        this.materials2 = new LocalMaterialData[size];
        this.rarities = new double[size];

        int pos = 0;
        for (int i = 0; i < args.size() - 1; i += 3) {
            this.materials[pos] = OTGMaterialReader.get().readMaterial(args.get(i));
            this.materials2[pos] = OTGMaterialReader.get().readMaterial(args.get(i + 1));
            this.rarities[pos] = readRarity(args.get(i + 2));
            pos++;
        }
        this.totalRarity = StringHelper.readDouble(args.get(args.size() - 1), 0.000001, Integer.MAX_VALUE);
    }


    @Override
    public void spawnForChunkDecoration(IWorldGenRegion world, Random random)
    {
        double currentRarity = 0;
        int rarity = random.nextInt((int) this.totalRarity);
        int materialIndex = -1;
        for (int i = 0; i < this.rarities.length; i++) {
            currentRarity += this.rarities[i];
            if (currentRarity >= rarity) {
                materialIndex = i;
                break;
            }
        }

        if (materialIndex < 0) {
            return;
        }

        LocalMaterialData material = materials[materialIndex];
        LocalMaterialData material2 = materials2[materialIndex];

        int x = world.getDecorationArea().getChunkBeingDecorated().getBlockX();
        int z = world.getDecorationArea().getChunkBeingDecorated().getBlockZ();
        int y = world.getBiomeConfigForDecoration(x, z).getSurfaceSettings().getWaterLevelMax();
        boolean useBothMaterials = random.nextDouble() > 0.7D;
        double rotationAngle = random.nextDouble() * 2.0D * Math.PI;
        int baseRadius = 11 - random.nextInt(5);
        int variation = 3 + random.nextInt(3);
        boolean isSmoothIceberg = random.nextDouble() > 0.7D;

        int extraHeight = (random.nextDouble() > 0.9D) ? random.nextInt(19) + 7 : 0;
        int icebergHeight = isSmoothIceberg ? random.nextInt(6) + 6 + extraHeight : random.nextInt(15) + 3;

        int depthBelowWater = Math.min(icebergHeight + random.nextInt(11), 18);
        int maxRadius = Math.min(icebergHeight + random.nextInt(7) - random.nextInt(5), 11);
        int radius = isSmoothIceberg ? baseRadius : 11;

        IcebergContext ctx = new IcebergContext(
            world,
            random,
            x,
            y,
            z,
            icebergHeight,
            maxRadius,
            baseRadius,
            variation,
            rotationAngle,
            isSmoothIceberg,
            radius,
            useBothMaterials,
            material,
            material2
        );

        for (int dx = -radius; dx < radius; dx++) {
            for (int dz = -radius; dz < radius; dz++) {
                for (int dy = 0; dy < icebergHeight; dy++) {
                    int profileRadius = isSmoothIceberg ?
                        getSmoothProfileRadius(dy, icebergHeight, maxRadius) :
                        getJaggedProfileRadius(random, dy, icebergHeight, maxRadius);
                    if (isSmoothIceberg || dx < profileRadius) {
                        generateIcebergBlock(ctx, dx, dy, dz, profileRadius, radius);
                    }
                }
            }
        }

        smooth(ctx);

        for (int dx = -radius; dx < radius; dx++) {
            for (int dz = -radius; dz < radius; dz++) {
                for (int dy = -1; dy > -depthBelowWater; dy--) {
                    int underwaterHorizontalRange = isSmoothIceberg ?
                        MathHelper.ceil((float) radius * (1.0F - (float) Math.pow(dy, 2.0D) / ((float) depthBelowWater
                                                                                               * 8.0F))) :
                        radius;
                    int layerRadius = getUnderwaterTaper(random, -dy, depthBelowWater, maxRadius);
                    if (dx < layerRadius) {
                        generateIcebergBlock(ctx, dx, dy, dz, layerRadius, underwaterHorizontalRange);
                    }
                }
            }
        }
        boolean hasCutout = isSmoothIceberg ? random.nextDouble() > 0.1D : random.nextDouble() > 0.7D;
        if (hasCutout) {
            generateCutOut(ctx);
        }
    }

    private void generateCutOut(
        IcebergContext ctx
    )
    {
        Random random = ctx.random;
        int maxRadius = ctx.maxRadius;
        int directionX = random.nextBoolean() ? -1 : 1;
        int directionZ = random.nextBoolean() ? -1 : 1;
        int offsetX = random.nextInt(Math.max(maxRadius / 2 - 2, 1));
        if (random.nextBoolean()) {
            offsetX = maxRadius / 2 + 1 - random.nextInt(Math.max(maxRadius - maxRadius / 2 - 1, 1));
        }

        int offsetZ = random.nextInt(Math.max(maxRadius / 2 - 2, 1));
        if (random.nextBoolean()) {
            offsetZ = maxRadius / 2 + 1 - random.nextInt(Math.max(maxRadius - maxRadius / 2 - 1, 1));
        }

        if (ctx.isSmoothIceberg) {
            offsetX = offsetZ = random.nextInt(Math.max(ctx.baseRadius - 5, 1));
        }

        int dx = directionX * offsetX;
        int dz = directionZ * offsetZ;
        double cutoutAngle =
            ctx.isSmoothIceberg ? ctx.rotationAngle + (Math.PI / 2D) : random.nextDouble() * 2.0D * Math.PI;
        //double drandom2 = flag ? (drandom1 + 1.5707963267948966D) : (random.nextDouble() * 2.0D * Math.PI);

        for (int dy = 0; dy < ctx.icebergHeight - 3; ++dy) {
            int profileRadius = getJaggedProfileRadius(random, dy, ctx.icebergHeight, maxRadius);
            carve(ctx, profileRadius, dx, dy, dx, false, cutoutAngle);
        }
        for (int dy = -1; dy > -ctx.icebergHeight + random.nextInt(5); --dy) {
            int underwaterTaper = getUnderwaterTaper(random, -dy, ctx.icebergHeight, maxRadius);
            carve(ctx, underwaterTaper, dx, dy, dz, true, cutoutAngle);
        }
    }

    private void carve(
        IcebergContext ctx,
        int heightDependentRadius,
        int xOffset,
        int yOffset,
        int zOffset,
        boolean isBelowWater,
        double cutoutAngle
    )
    {
        int radiusX = heightDependentRadius + 1 + ctx.baseRadius / 3;
        int radiusZ = Math.min(heightDependentRadius - 3, 3) + ctx.variation / 2 - 1;
        int targetX;
        int targetY;
        int targetZ;
        BiomeSettings biomeConfig;
        LocalMaterialData replacedMaterial;
        LocalMaterialData replacedMaterial2;
        LocalMaterialData current;
        for (int dx = -radiusX; dx < radiusX; dx++) {
            for (int dz = -radiusX; dz < radiusX; dz++) {
                double signedDistanceEllipse =
                    getRotatedOvalPerimeter(dx, dz, radiusX, radiusZ, xOffset, zOffset, cutoutAngle);
                if (signedDistanceEllipse < 0.0D) {
                    targetX = ctx.x + dx;
                    targetY = ctx.y + yOffset;
                    targetZ = ctx.z + dz;
                    current = ctx.world.getMaterialDirect(targetX, targetY, targetZ);
                    // TODO: Request all up front instead of using cacheChunk true?
                    biomeConfig = ctx.world.getCachedBiomeProvider().getBiomeConfig(targetX, targetZ, true);
                    replacedMaterial = ctx.material;
                    replacedMaterial2 = ctx.material2;
                    if (biomeConfig.getSurfaceSettings().getReplacedBlocks() != null) {
                        replacedMaterial =
                            biomeConfig.getSurfaceSettings().getReplacedBlocks().replaceBlock(targetY, ctx.material);
                        replacedMaterial2 =
                            biomeConfig.getSurfaceSettings().getReplacedBlocks().replaceBlock(targetY, ctx.material2);
                    }
                    if (isIcebergBlock(current, replacedMaterial, replacedMaterial2) || current.isMaterial(
                        LocalMaterials.SNOW_BLOCK))
                    {
                        if (isBelowWater) {
                            ctx.world.setBlockDirect(targetX, targetY, targetZ, LocalMaterials.WATER);
                        } else {
                            ctx.world.setBlockDirect(targetX, targetY, targetZ, LocalMaterials.AIR);
                            removeFloatingSnowLayer(ctx.world, targetX, targetY, targetZ);
                        }
                    }
                }
            }
        }
    }

    private void removeFloatingSnowLayer(IWorldGenRegion world, int x, int y, int z)
    {
        LocalMaterialData materialAbove = world.getMaterialDirect(x, y + 1, z);
        if (materialAbove.isMaterial(LocalMaterials.SNOW)) {
            world.setBlockDirect(x, y + 1, z, LocalMaterials.AIR);
        }
    }

    private void generateIcebergBlock(IcebergContext ctx, int dx, int dy, int dz, int layerRadius, int maxRadius)
    {
        double perimeter = ctx.isSmoothIceberg ?
            getRotatedOvalPerimeter(
                dx,
                dz,
                maxRadius,
                getSmoothPinch(dy, ctx.icebergHeight, ctx.variation),
                0,
                0,
                ctx.rotationAngle
            ) :
            getCraggyPerimeter(dx, dz, layerRadius, ctx.random);

        if (perimeter < 0.0D) {
            int targetX = ctx.x + dx;
            int targetY = ctx.y + dy;
            int targetZ = ctx.z + dz;
            double edgeThreshold = ctx.isSmoothIceberg ? -0.5D : (double) (-6 - ctx.random.nextInt(3));
            if (perimeter > edgeThreshold && ctx.random.nextDouble() > 0.9D) {
                return;
            }
            setIcebergBlock(ctx, targetX, targetY, targetZ, ctx.icebergHeight - dy);
        }
    }

    private void setIcebergBlock(IcebergContext ctx, int targetX, int targetY, int targetZ, int distanceFromTop)
    {
        LocalMaterialData current = ctx.world.getMaterialDirect(targetX, targetY, targetZ);
        if (current.isAir()
            || current.isMaterial(LocalMaterials.SNOW_BLOCK)
            || current.isMaterial(LocalMaterials.ICE)
            || current.isMaterial(LocalMaterials.WATER))
        {
            boolean useSecondaryMaterial = !ctx.isSmoothIceberg || ctx.random.nextDouble() > 0.05D;
            int layerDensity = ctx.isSmoothIceberg ? 3 : 2;
            if (ctx.useBothMaterials
                && !current.isMaterial(LocalMaterials.WATER)
                && (double) distanceFromTop
                   <= (double) ctx.random.nextInt(Math.max(1, ctx.icebergHeight / layerDensity))
                      + (double) ctx.icebergHeight * 0.6D
                && useSecondaryMaterial)
            {
                ctx.world.setBlockDirect(targetX, targetY, targetZ, ctx.material2);
            } else {
                ctx.world.setBlockDirect(targetX, targetY, targetZ, ctx.material);
            }
        }
    }

    private int getSmoothPinch(int dy, int icebergHeight, int variation)
    {
        int pinch = variation;
        if (dy > 0 && icebergHeight - dy <= 3) {
            pinch -= (4 - (icebergHeight - dy));
        }
        return pinch;
    }

    private double getCraggyPerimeter(int dx, int dz, int radius, Random random)
    {
        float frandom = 10.0F * MathHelper.clamp(random.nextFloat(), 0.2F, 0.8F) / (float) radius;
        return (double) frandom + Math.pow(dx, 2.0D) + Math.pow(dz, 2.0D) - Math.pow(radius, 2.0D);
    }

    private double getRotatedOvalPerimeter(
        int dx, int dz, int radiusX, int radiusZ, int offsetX, int offsetZ,
        double random
    )
    {
        return Math.pow(
            ((double) (dx - offsetX) * Math.cos(random) - (double) (dz - offsetZ) * Math.sin(random))
            / (double) radiusX,
            2.0D
        )
               + Math.pow(
            ((double) (dx - offsetX) * Math.sin(random) + (double) (dz - offsetZ) * Math.cos(random))
            / (double) radiusZ,
            2.0D
        ) - 1.0D;
    }

    private int getJaggedProfileRadius(Random random, int dy, int icebergHeight, int maxRadius)
    {
        float frandom1 = 3.5F - random.nextFloat();
        float frandom2 = (1.0F - (float) Math.pow(dy, 2.0D) / ((float) icebergHeight * frandom1)) * (float) maxRadius;
        if (icebergHeight > 15 + random.nextInt(5)) {
            int irandom4 = dy < 3 + random.nextInt(6) ? dy / 2 : dy;
            frandom2 = (1.0F - (float) irandom4 / ((float) icebergHeight * frandom1 * 0.4F)) * (float) maxRadius;
        }
        return MathHelper.ceil(frandom2 / 2.0F);
    }

    private int getSmoothProfileRadius(int dy, int icebergHeight, int maxRadius)
    {
        float frandom = (1.0F - (float) Math.pow(dy, 2.0D) / ((float) icebergHeight)) * (float) maxRadius;
        return MathHelper.ceil(frandom / 2.0F);
    }

    private int getUnderwaterTaper(Random random, int depth, int totalDepth, int maxRadius)
    {
        float verticality = 1.0F + random.nextFloat() / 2.0F;
        float diameter = (1.0F - (float) depth / ((float) totalDepth * verticality)) * (float) maxRadius;
        return MathHelper.ceil(diameter / 2.0F);
    }

    private boolean isIcebergBlock(LocalMaterialData target, LocalMaterialData material, LocalMaterialData material2)
    {
        return target.isMaterial(material) || target.isMaterial(material2);
    }

    private boolean belowIsAir(IWorldGenRegion world, int x, int y, int z)
    {
        return world.getMaterialDirect(x, y - 1, z).isAir();
    }

    private void smooth(
        IcebergContext ctx
    )
    {
        int radius = ctx.isSmoothIceberg ? ctx.baseRadius : ctx.maxRadius / 2;
        IWorldGenRegion world = ctx.world;

        int targetX;
        int targetY;
        int targetZ;
        BiomeSettings biomeConfig;
        LocalMaterialData replacedMaterial;
        LocalMaterialData replacedMaterial2;
        LocalMaterialData current;
        int nonIcebergNeighbours;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = 0; dy <= ctx.icebergHeight; dy++) {
                    targetX = ctx.x + dx;
                    targetY = ctx.y + dy;
                    targetZ = ctx.z + dz;
                    // TODO: Request all up front instead of using cacheChunk true?
                    biomeConfig = world.getCachedBiomeProvider().getBiomeConfig(targetX, targetZ, true);
                    replacedMaterial = ctx.material;
                    replacedMaterial2 = ctx.material2;
                    if (biomeConfig.getSurfaceSettings().getReplacedBlocks() != null) {
                        replacedMaterial =
                            biomeConfig.getSurfaceSettings().getReplacedBlocks().replaceBlock(targetY, ctx.material);
                        replacedMaterial2 =
                            biomeConfig.getSurfaceSettings().getReplacedBlocks().replaceBlock(targetY, ctx.material2);
                    }
                    current = world.getMaterialDirect(targetX, targetY, targetZ);
                    if (isIcebergBlock(current, replacedMaterial, replacedMaterial2) || current.isMaterial(
                        LocalMaterials.SNOW))
                    {
                        if (belowIsAir(world, targetX, targetY, targetZ)) {
                            world.setBlockDirect(targetX, targetY, targetZ, LocalMaterials.AIR);
                            world.setBlockDirect(targetX, targetY + 1, targetZ, LocalMaterials.AIR);
                        } else if (isIcebergBlock(current, replacedMaterial, replacedMaterial2)) {
                            LocalMaterialData[] materials = {
                                world.getMaterialDirect(targetX - 1, targetY, targetZ),
                                world.getMaterialDirect(targetX + 1, targetY, targetZ),
                                world.getMaterialDirect(targetX, targetY, targetZ - 1),
                                world.getMaterialDirect(targetX, targetY, targetZ + 1)
                            };
                            BiomeSettings[] biomeConfigs = {
                                world.getCachedBiomeProvider().getBiomeConfig(targetX - 1, targetZ, true),
                                world.getCachedBiomeProvider().getBiomeConfig(targetX + 1, targetZ, true),
                                world.getCachedBiomeProvider().getBiomeConfig(targetX, targetZ - 1, true),
                                world.getCachedBiomeProvider().getBiomeConfig(targetX, targetZ + 1, true)
                            };
                            nonIcebergNeighbours = 0;
                            int i = 0;
                            for (LocalMaterialData mat : materials) {
                                if (biomeConfigs[i].getSurfaceSettings().getReplacedBlocks() != null) {
                                    replacedMaterial = biomeConfigs[i].getSurfaceSettings()
                                                                      .getReplacedBlocks()
                                                                      .replaceBlock(targetY, ctx.material);
                                    replacedMaterial2 = biomeConfigs[i].getSurfaceSettings()
                                                                       .getReplacedBlocks()
                                                                       .replaceBlock(targetY, ctx.material2);
                                }
                                if (!isIcebergBlock(mat, replacedMaterial, replacedMaterial2)) {
                                    nonIcebergNeighbours++;
                                }
                                i++;
                            }
                            if (nonIcebergNeighbours >= 3) {
                                world.setBlockDirect(targetX, targetY, targetZ, LocalMaterials.AIR);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder("Iceberg(");
        for (int i = 0; i < this.materials.length; i++) {
            builder.append(this.materials[i] + ", ");
            builder.append(this.materials2[i] + ", ");
            builder.append(this.rarities[i] + ", ");
        }
        builder.append(this.totalRarity + ")");

        return builder.toString();
    }
}
