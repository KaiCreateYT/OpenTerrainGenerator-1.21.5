package com.pg85.otg.test.snapshot;

import com.pg85.otg.gen.OTGChunkGenerator;
import com.pg85.otg.interfaces.IBiome;
import com.pg85.otg.interfaces.ILayerSource;
import com.pg85.otg.presets.DimensionPreset;
import com.pg85.otg.test.gen.TestChunkBuffer;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.gen.JigsawStructureData;
import com.pg85.otg.util.gen.OTGWorldInfo;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

import java.time.Instant;
import java.util.Random;

/**
 * Generates terrain snapshots by running OTGChunkGenerator and collecting results.
 *
 * This class is used for regression testing terrain generation by capturing
 * heightmaps and block slices at specific Y levels.
 */
public class TerrainSnapshotGenerator {

    /**
     * Y levels at which to capture block slices.
     */
    public static final int[] SLICE_Y_LEVELS = {-64, 0, 64, 128, 192, 256};

    /**
     * Resolution for heightmap sampling in blocks.
     * Value of 16 means sample once per chunk (at position 0,0 of each chunk).
     */
    public static final int HEIGHTMAP_RESOLUTION = 16;

    private final DimensionPreset preset;
    private final ILayerSource biomeProvider;
    private final IBiome[] biomes;
    private final OTGWorldInfo worldInfo;
    private final long seed;
    private final String version;
    private final OTGChunkGenerator generator;

    /**
     * Creates a new TerrainSnapshotGenerator.
     *
     * @param preset        the preset configuration to use
     * @param biomeProvider the biome provider for biome lookups
     * @param biomes        array of biomes indexed by biome ID
     * @param worldInfo     world height information
     * @param seed          the world seed
     * @param version       version string for metadata
     */
    public TerrainSnapshotGenerator(
            DimensionPreset preset,
            ILayerSource biomeProvider,
            IBiome[] biomes,
            OTGWorldInfo worldInfo,
            long seed,
            String version
    ) {
        this.preset = preset;
        this.biomeProvider = biomeProvider;
        this.biomes = biomes;
        this.worldInfo = worldInfo;
        this.seed = seed;
        this.version = version;

        // Create and initialize the chunk generator
        this.generator = new OTGChunkGenerator(preset, biomeProvider, biomes, worldInfo);
        this.generator.setSeed(seed);
    }

    /**
     * Generates a terrain snapshot for the specified chunk region.
     *
     * @param fromChunkX starting chunk X coordinate (inclusive)
     * @param fromChunkZ starting chunk Z coordinate (inclusive)
     * @param toChunkX   ending chunk X coordinate (inclusive)
     * @param toChunkZ   ending chunk Z coordinate (inclusive)
     * @return the generated snapshot model
     */
    public SnapshotModel generateSnapshot(int fromChunkX, int fromChunkZ, int toChunkX, int toChunkZ) {
        SnapshotModel snapshot = new SnapshotModel();

        // Set up metadata
        snapshot.metadata = new SnapshotModel.Metadata(
                seed,
                version,
                new SnapshotModel.Region(fromChunkX, fromChunkZ, toChunkX, toChunkZ),
                Instant.now(),
                preset.getFolderName()
        );

        // Initialize heightmap
        snapshot.heightmap = new SnapshotModel.Heightmap(HEIGHTMAP_RESOLUTION);

        // Empty structures list for populateNoise
        ObjectList<JigsawStructureData> emptyStructures = new ObjectArrayList<>();

        // Random for populateNoise
        Random random = new Random(seed);

        // Loop over all chunks in the region
        for (int chunkX = fromChunkX; chunkX <= toChunkX; chunkX++) {
            for (int chunkZ = fromChunkZ; chunkZ <= toChunkZ; chunkZ++) {
                // Create buffer for this chunk
                TestChunkBuffer buffer = new TestChunkBuffer(
                        chunkX, chunkZ,
                        worldInfo.minY(), worldInfo.maxY()
                );

                // Get chunk coordinate
                ChunkCoordinate chunkCoord = ChunkCoordinate.fromChunkCoords(chunkX, chunkZ);

                // Reset random with deterministic seed for this chunk
                random.setSeed(seed ^ ((long) chunkX * 341873128712L + (long) chunkZ * 132897987541L));

                // Generate the terrain
                generator.populateNoise(worldInfo, buffer, chunkCoord, emptyStructures, random);

                // Calculate world coordinates for this chunk's (0,0) position
                int worldX = chunkX * 16;
                int worldZ = chunkZ * 16;

                // Sample heightmap at (0,0) of chunk (local coords)
                int heightY = buffer.getHighestBlockY(0, 0);
                snapshot.heightmap.setHeight(worldX, worldZ, heightY);

                // Sample each Y slice at (0,0) of chunk
                for (int yLevel : SLICE_Y_LEVELS) {
                    // Only sample if Y is within world bounds
                    if (yLevel >= worldInfo.minY() && yLevel <= worldInfo.maxY()) {
                        String blockName = buffer.getBlockNameAt(0, yLevel, 0);
                        snapshot.setBlock(yLevel, worldX, worldZ, blockName);
                    }
                }
            }
        }

        return snapshot;
    }
}
