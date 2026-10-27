package com.pg85.otg.test.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pg85.otg.config.biome.BiomeConfig;
import com.pg85.otg.interfaces.IBiome;
import com.pg85.otg.presets.DimensionPreset;
import com.pg85.otg.test.biome.TestBiome;
import com.pg85.otg.test.biome.TestBiomeProvider;
import com.pg85.otg.test.preset.TestDimensionPresetLoader;
import com.pg85.otg.test.snapshot.SnapshotModel;
import com.pg85.otg.test.snapshot.TerrainSnapshotComparator;
import com.pg85.otg.test.snapshot.TerrainSnapshotComparator.ComparisonResult;
import com.pg85.otg.test.snapshot.TerrainSnapshotComparator.SectionDiff;
import com.pg85.otg.test.snapshot.TerrainSnapshotComparator.ValueDiff;
import com.pg85.otg.test.snapshot.TerrainSnapshotGenerator;
import com.pg85.otg.util.gen.OTGWorldInfo;
import com.pg85.otg.util.OTGLog;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.pg85.otg.gen.OTGChunkGenerator;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.gen.JigsawStructureData;
import com.pg85.otg.test.gen.TestChunkBuffer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Command-line interface for terrain snapshot generation and comparison.
 *
 * Commands:
 * - generate --seed <seed> --output <file.json> [--preset <name>] [--otg-root <path>]
 * - compare --baseline <file.json> --current <file.json> [--verbose] [--fail-threshold <percent>]
 * - verify --seed <seed> --baseline <file.json> [--preset <name>] [--otg-root <path>]
 *
 * Exit codes:
 * - 0: Success (identical or within threshold)
 * - 1: Differences found (above threshold)
 * - 2: Error (invalid arguments, file not found, etc.)
 */
public class SnapshotCli {

    private static final int EXIT_SUCCESS = 0;
    private static final int EXIT_DIFFERS = 1;
    private static final int EXIT_ERROR = 2;

    private static final int DEFAULT_MAX_DIFFS_PER_SECTION = 10;
    private static final String DEFAULT_OTG_ROOT = "config/OpenTerrainGenerator";
    private static final String DEFAULT_PRESET = "DefaultPreset";
    private static final String VERSION = "0.2.0";

    // Default chunk region: 17x17 chunks (-8,-8 to 8,8)
    private static final int DEFAULT_FROM_CHUNK = -8;
    private static final int DEFAULT_TO_CHUNK = 8;

    private final ObjectMapper mapper;

    public SnapshotCli() {
        this.mapper = createObjectMapper();
    }

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            System.exit(EXIT_ERROR);
        }

        SnapshotCli cli = new SnapshotCli();
        String command = args[0];

        int exitCode;
        switch (command) {
            case "generate":
                exitCode = cli.runGenerate(args);
                break;
            case "compare":
                exitCode = cli.runCompare(args);
                break;
            case "verify":
                exitCode = cli.runVerify(args);
                break;
            case "benchmark":
                exitCode = cli.runBenchmark(args);
                break;
            case "stress-test":
                exitCode = cli.runStressTest(args);
                break;
            case "shadow-stress-test":
                exitCode = cli.runShadowStressTest(args);
                break;
            case "--help":
            case "-h":
                printUsage();
                exitCode = EXIT_SUCCESS;
                break;
            default:
                System.err.println("Unknown command: " + command);
                printUsage();
                exitCode = EXIT_ERROR;
                break;
        }

        System.exit(exitCode);
    }

    private static void printUsage() {
        System.out.println("Usage: SnapshotCli <command> [options]");
        System.out.println();
        System.out.println("Commands:");
        System.out.println("  generate --seed <seed> --output <file.json> [--preset <name>] [--otg-root <path>]");
        System.out.println("      Generate a terrain snapshot for the given seed");
        System.out.println();
        System.out.println("  compare --baseline <file.json> --current <file.json> [--verbose] [--fail-threshold <percent>]");
        System.out.println("      Compare two snapshot files");
        System.out.println();
        System.out.println("  verify --seed <seed> --baseline <file.json> [--preset <name>] [--otg-root <path>] [--verbose] [--fail-threshold <percent>]");
        System.out.println("      Generate snapshot and compare against baseline");
        System.out.println();
        System.out.println("  benchmark [--preset <name>] [--otg-root <path>] [--chunks <n>] [--warmup <n>] [--iterations <n>]");
        System.out.println("      Benchmark terrain generation performance");
        System.out.println();
        System.out.println("  stress-test [--preset <name>] [--otg-root <path>] [--threads <n>] [--chunks <n>]");
        System.out.println("      Test thread-safety of parallel chunk generation");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  --seed <n>            World seed (default: 12345)");
        System.out.println("  --preset <name>       DimensionPreset name (default: DefaultPreset)");
        System.out.println("  --otg-root <path>     OTG config directory (default: config/OpenTerrainGenerator)");
        System.out.println("  --verbose             Show all differences (default: max 10 per section)");
        System.out.println("  --fail-threshold <n>  Fail only if difference exceeds n% (default: 0)");
        System.out.println("  --chunks <n>          Chunks per iteration for benchmark/stress-test (default: 100)");
        System.out.println("  --warmup <n>          Warmup iterations (default: 3)");
        System.out.println("  --iterations <n>      Measurement iterations (default: 5)");
        System.out.println("  --threads <n>         Number of threads for stress-test (default: 4)");
    }

    /**
     * Runs the generate command.
     */
    public int runGenerate(String[] args) {
        long seed = 12345;
        String output = null;
        String presetName = DEFAULT_PRESET;
        String otgRoot = DEFAULT_OTG_ROOT;

        // Parse arguments
        for (int i = 1; i < args.length; i++) {
            switch (args[i]) {
                case "--seed":
                    if (i + 1 < args.length) {
                        try {
                            seed = Long.parseLong(args[++i]);
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid seed value: " + args[i]);
                            return EXIT_ERROR;
                        }
                    }
                    break;
                case "--output":
                    if (i + 1 < args.length) {
                        output = args[++i];
                    }
                    break;
                case "--preset":
                    if (i + 1 < args.length) {
                        presetName = args[++i];
                    }
                    break;
                case "--otg-root":
                    if (i + 1 < args.length) {
                        otgRoot = args[++i];
                    }
                    break;
            }
        }

        if (output == null) {
            System.err.println("Error: --output is required");
            return EXIT_ERROR;
        }

        System.out.println("Generating snapshot...");
        System.out.println("  Seed: " + seed);
        System.out.println("  Preset: " + presetName);
        System.out.println("  OTG Root: " + otgRoot);

        try {
            SnapshotModel snapshot = generateSnapshot(seed, presetName, otgRoot);

            // Write to file
            File outputFile = new File(output);
            outputFile.getParentFile().mkdirs();
            mapper.writeValue(outputFile, snapshot);

            System.out.println("Snapshot written to: " + output);
            System.out.println("  Region: chunks (" + DEFAULT_FROM_CHUNK + "," + DEFAULT_FROM_CHUNK +
                             ") to (" + DEFAULT_TO_CHUNK + "," + DEFAULT_TO_CHUNK + ")");
            System.out.println("  Heightmap entries: " + snapshot.heightmap.data.size());
            System.out.println("  Slices: " + snapshot.slices.size());

            return EXIT_SUCCESS;
        } catch (Exception e) {
            System.err.println("Error generating snapshot: " + e.getMessage());
            OTGLog.error("Exception", e);
            return EXIT_ERROR;
        }
    }

    /**
     * Runs the compare command.
     */
    public int runCompare(String[] args) {
        String baselinePath = null;
        String currentPath = null;
        boolean verbose = false;
        double failThreshold = 0.0;

        // Parse arguments
        for (int i = 1; i < args.length; i++) {
            switch (args[i]) {
                case "--baseline":
                    if (i + 1 < args.length) {
                        baselinePath = args[++i];
                    }
                    break;
                case "--current":
                    if (i + 1 < args.length) {
                        currentPath = args[++i];
                    }
                    break;
                case "--verbose":
                    verbose = true;
                    break;
                case "--fail-threshold":
                    if (i + 1 < args.length) {
                        try {
                            failThreshold = Double.parseDouble(args[++i]);
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid threshold value: " + args[i]);
                            return EXIT_ERROR;
                        }
                    }
                    break;
            }
        }

        if (baselinePath == null || currentPath == null) {
            System.err.println("Error: --baseline and --current are required");
            return EXIT_ERROR;
        }

        // Load snapshots
        SnapshotModel baseline;
        SnapshotModel current;

        try {
            baseline = loadSnapshot(baselinePath);
        } catch (IOException e) {
            System.err.println("Error loading baseline: " + e.getMessage());
            return EXIT_ERROR;
        }

        try {
            current = loadSnapshot(currentPath);
        } catch (IOException e) {
            System.err.println("Error loading current: " + e.getMessage());
            return EXIT_ERROR;
        }

        return compareSnapshots(baseline, current, verbose, failThreshold);
    }

    /**
     * Runs the verify command - generates snapshot and compares against baseline.
     */
    public int runVerify(String[] args) {
        long seed = 12345;
        String baselinePath = null;
        String presetName = DEFAULT_PRESET;
        String otgRoot = DEFAULT_OTG_ROOT;
        boolean verbose = false;
        double failThreshold = 0.0;

        // Parse arguments
        for (int i = 1; i < args.length; i++) {
            switch (args[i]) {
                case "--seed":
                    if (i + 1 < args.length) {
                        try {
                            seed = Long.parseLong(args[++i]);
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid seed value: " + args[i]);
                            return EXIT_ERROR;
                        }
                    }
                    break;
                case "--baseline":
                    if (i + 1 < args.length) {
                        baselinePath = args[++i];
                    }
                    break;
                case "--preset":
                    if (i + 1 < args.length) {
                        presetName = args[++i];
                    }
                    break;
                case "--otg-root":
                    if (i + 1 < args.length) {
                        otgRoot = args[++i];
                    }
                    break;
                case "--verbose":
                    verbose = true;
                    break;
                case "--fail-threshold":
                    if (i + 1 < args.length) {
                        try {
                            failThreshold = Double.parseDouble(args[++i]);
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid threshold value: " + args[i]);
                            return EXIT_ERROR;
                        }
                    }
                    break;
            }
        }

        if (baselinePath == null) {
            System.err.println("Error: --baseline is required");
            return EXIT_ERROR;
        }

        System.out.println("Verifying terrain generation...");
        System.out.println("  Seed: " + seed);
        System.out.println("  Preset: " + presetName);
        System.out.println("  Baseline: " + baselinePath);

        // Load baseline
        SnapshotModel baseline;
        try {
            baseline = loadSnapshot(baselinePath);
        } catch (IOException e) {
            System.err.println("Error loading baseline: " + e.getMessage());
            return EXIT_ERROR;
        }

        // Generate current snapshot
        SnapshotModel current;
        try {
            current = generateSnapshot(seed, presetName, otgRoot);
        } catch (Exception e) {
            System.err.println("Error generating snapshot: " + e.getMessage());
            OTGLog.error("Exception", e);
            return EXIT_ERROR;
        }

        System.out.println("Comparing...");
        return compareSnapshots(baseline, current, verbose, failThreshold);
    }

    /**
     * Generates a terrain snapshot for the given parameters.
     */
    private SnapshotModel generateSnapshot(long seed, String presetName, String otgRoot) throws Exception {
        Path otgRootPath = Paths.get(otgRoot);

        // Initialize headless mode with OTG engine
        TestDimensionPresetLoader.initHeadless(otgRootPath);

        // Load the specified preset
        DimensionPreset preset = TestDimensionPresetLoader.loadPreset(otgRootPath, presetName);
        if (preset == null) {
            // List available presets
            List<DimensionPreset> available = TestDimensionPresetLoader.loadPresets(otgRootPath);
            StringBuilder sb = new StringBuilder("Preset not found: " + presetName);
            if (!available.isEmpty()) {
                sb.append("\nAvailable presets:");
                for (DimensionPreset p : available) {
                    sb.append("\n  - ").append(p.getFolderName());
                }
            } else {
                sb.append("\nNo presets found in: ").append(otgRootPath.resolve("Presets"));
            }
            throw new IllegalArgumentException(sb.toString());
        }

        System.out.println("  Loaded preset: " + preset.getFolderName());

        // Get world info from preset
        OTGWorldInfo worldInfo = preset.getConfig().getWorldInfo();
        System.out.println("  World height: " + worldInfo.minY() + " to " + worldInfo.maxY());

        // Build IBiome array from preset's BiomeConfigs
        List<BiomeConfig> biomeConfigs = preset.getBiomeConfigList();
        System.out.println("  Biomes: " + biomeConfigs.size());

        // Assign OTG biome IDs (LocalDimensionPresetLoader doesn't do this - only platform loaders do)
        // ID 0 is reserved for ocean, start from 1
        int currentId = 1;
        for (BiomeConfig bc : biomeConfigs) {
            bc.setOTGBiomeId(currentId++);
        }
        int maxBiomeId = currentId - 1;

        // Create IBiome array indexed by biome ID
        IBiome[] biomes = new IBiome[maxBiomeId + 1];
        int[] availableBiomeIds = new int[biomeConfigs.size()];
        int idx = 0;
        for (BiomeConfig bc : biomeConfigs) {
            int id = bc.getOTGBiomeID().id();
            float temperature = bc.getVisualSettings().getBiomeTemperature();
            biomes[id] = new TestBiome(bc, temperature);
            availableBiomeIds[idx++] = id;
        }

        // Create biome provider with seed-based selection
        TestBiomeProvider biomeProvider = new TestBiomeProvider(seed, availableBiomeIds);

        // Create generator and generate snapshot
        TerrainSnapshotGenerator generator = new TerrainSnapshotGenerator(
                preset,
                biomeProvider,
                biomes,
                worldInfo,
                seed,
                VERSION
        );

        return generator.generateSnapshot(
                DEFAULT_FROM_CHUNK, DEFAULT_FROM_CHUNK,
                DEFAULT_TO_CHUNK, DEFAULT_TO_CHUNK
        );
    }

    /**
     * Compares two snapshots and prints results.
     */
    private int compareSnapshots(SnapshotModel baseline, SnapshotModel current,
                                  boolean verbose, double failThreshold) {
        TerrainSnapshotComparator comparator = new TerrainSnapshotComparator();
        ComparisonResult result = comparator.compare(baseline, current);

        long seed = baseline.metadata != null ? baseline.metadata.seed : 0;

        if (result.identical) {
            System.out.println("Snapshots identical (seed: " + seed + ", " + result.totalValues + " values compared)");
            return EXIT_SUCCESS;
        }

        // Snapshots differ
        System.out.println("Snapshots differ!");
        System.out.println();

        int maxDiffsPerSection = verbose ? Integer.MAX_VALUE : DEFAULT_MAX_DIFFS_PER_SECTION;

        for (SectionDiff section : result.sections.values()) {
            if (section.differences == 0) {
                continue;
            }

            System.out.printf("%s differences (%d of %d): %.1f%%%n",
                    section.sectionName,
                    section.differences,
                    section.totalValues,
                    section.getDifferencePercentage());

            List<ValueDiff> diffs = section.diffs;
            int showCount = Math.min(diffs.size(), maxDiffsPerSection);

            for (int i = 0; i < showCount; i++) {
                ValueDiff diff = diffs.get(i);
                System.out.printf("  [%s]: %s -> %s%n", diff.coord, diff.oldValue, diff.newValue);
            }

            int remaining = diffs.size() - showCount;
            if (remaining > 0) {
                System.out.printf("  ... (%d more)%n", remaining);
            }

            System.out.println();
        }

        System.out.printf("Total: %d differences across %d values (%.1f%%)%n",
                result.totalDifferences,
                result.totalValues,
                result.getDifferencePercentage());

        // Check threshold
        if (result.getDifferencePercentage() <= failThreshold) {
            System.out.printf("Within threshold (%.1f%% <= %.1f%%)%n",
                    result.getDifferencePercentage(), failThreshold);
            return EXIT_SUCCESS;
        }

        return EXIT_DIFFERS;
    }

    /**
     * Loads a snapshot from a JSON file.
     */
    private SnapshotModel loadSnapshot(String path) throws IOException {
        File file = new File(path);
        if (!file.exists()) {
            throw new IOException("File not found: " + path);
        }
        return mapper.readValue(file, SnapshotModel.class);
    }

    /**
     * Runs the benchmark command.
     */
    public int runBenchmark(String[] args) {
        long seed = 12345;
        String presetName = DEFAULT_PRESET;
        String otgRoot = DEFAULT_OTG_ROOT;
        int chunksPerIteration = 100;
        int warmupIterations = 3;
        int measureIterations = 5;

        // Parse arguments
        for (int i = 1; i < args.length; i++) {
            switch (args[i]) {
                case "--seed":
                    if (i + 1 < args.length) {
                        try {
                            seed = Long.parseLong(args[++i]);
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid seed value: " + args[i]);
                            return EXIT_ERROR;
                        }
                    }
                    break;
                case "--preset":
                    if (i + 1 < args.length) {
                        presetName = args[++i];
                    }
                    break;
                case "--otg-root":
                    if (i + 1 < args.length) {
                        otgRoot = args[++i];
                    }
                    break;
                case "--chunks":
                    if (i + 1 < args.length) {
                        try {
                            chunksPerIteration = Integer.parseInt(args[++i]);
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid chunks value: " + args[i]);
                            return EXIT_ERROR;
                        }
                    }
                    break;
                case "--warmup":
                    if (i + 1 < args.length) {
                        try {
                            warmupIterations = Integer.parseInt(args[++i]);
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid warmup value: " + args[i]);
                            return EXIT_ERROR;
                        }
                    }
                    break;
                case "--iterations":
                    if (i + 1 < args.length) {
                        try {
                            measureIterations = Integer.parseInt(args[++i]);
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid iterations value: " + args[i]);
                            return EXIT_ERROR;
                        }
                    }
                    break;
            }
        }

        System.out.println("Benchmark Configuration:");
        System.out.println("  Preset: " + presetName);
        System.out.println("  Seed: " + seed);
        System.out.println("  Chunks per iteration: " + chunksPerIteration);
        System.out.println("  Warmup iterations: " + warmupIterations);
        System.out.println("  Measurement iterations: " + measureIterations);
        System.out.println();

        try {
            return executeBenchmark(seed, presetName, otgRoot, chunksPerIteration, warmupIterations, measureIterations);
        } catch (Exception e) {
            System.err.println("Benchmark failed: " + e.getMessage());
            OTGLog.error("Exception", e);
            return EXIT_ERROR;
        }
    }

    private int executeBenchmark(long seed, String presetName, String otgRoot,
                                  int chunksPerIteration, int warmupIterations, int measureIterations) throws Exception {
        Path otgRootPath = Paths.get(otgRoot);

        // Initialize headless mode
        TestDimensionPresetLoader.initHeadless(otgRootPath);

        // Load preset
        DimensionPreset preset = TestDimensionPresetLoader.loadPreset(otgRootPath, presetName);
        if (preset == null) {
            throw new IllegalArgumentException("Preset not found: " + presetName);
        }

        OTGWorldInfo worldInfo = preset.getConfig().getWorldInfo();

        // Build IBiome array
        List<BiomeConfig> biomeConfigs = preset.getBiomeConfigList();
        int currentId = 1;
        for (BiomeConfig bc : biomeConfigs) {
            bc.setOTGBiomeId(currentId++);
        }
        int maxBiomeId = currentId - 1;

        IBiome[] biomes = new IBiome[maxBiomeId + 1];
        int[] availableBiomeIds = new int[biomeConfigs.size()];
        int idx = 0;
        for (BiomeConfig bc : biomeConfigs) {
            int id = bc.getOTGBiomeID().id();
            float temperature = bc.getVisualSettings().getBiomeTemperature();
            biomes[id] = new TestBiome(bc, temperature);
            availableBiomeIds[idx++] = id;
        }

        TestBiomeProvider biomeProvider = new TestBiomeProvider(seed, availableBiomeIds);

        // Create generator
        OTGChunkGenerator generator = new OTGChunkGenerator(preset, biomeProvider, biomes, worldInfo);
        generator.setSeed(seed);

        ObjectArrayList<JigsawStructureData> emptyStructures = new ObjectArrayList<>();
        Random random = new Random(seed);

        System.out.println("Loaded preset: " + preset.getFolderName());
        System.out.println("World height: " + worldInfo.minY() + " to " + worldInfo.maxY());
        System.out.println("Biomes: " + biomeConfigs.size());
        System.out.println();

        // Warmup
        System.out.println("Warming up (" + warmupIterations + " iterations)...");
        for (int iter = 0; iter < warmupIterations; iter++) {
            runChunkGeneration(generator, worldInfo, emptyStructures, random, seed, chunksPerIteration, iter);
            System.out.print(".");
        }
        System.out.println(" done");
        System.out.println();

        // Measurement
        System.out.println("Measuring (" + measureIterations + " iterations)...");
        long[] times = new long[measureIterations];
        for (int iter = 0; iter < measureIterations; iter++) {
            long start = System.nanoTime();
            runChunkGeneration(generator, worldInfo, emptyStructures, random, seed, chunksPerIteration, iter + warmupIterations);
            times[iter] = System.nanoTime() - start;
            System.out.printf("  Iteration %d: %d chunks in %.2f ms (%.1f chunks/sec)%n",
                    iter + 1,
                    chunksPerIteration,
                    times[iter] / 1_000_000.0,
                    chunksPerIteration * 1_000_000_000.0 / times[iter]);
        }
        System.out.println();

        // Calculate statistics
        long sum = 0;
        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;
        for (long t : times) {
            sum += t;
            min = Math.min(min, t);
            max = Math.max(max, t);
        }
        double avgNs = (double) sum / measureIterations;
        double avgMs = avgNs / 1_000_000.0;
        double chunksPerSec = chunksPerIteration * 1_000_000_000.0 / avgNs;
        double msPerChunk = avgMs / chunksPerIteration;

        System.out.println("=== RESULTS ===");
        System.out.printf("Average: %.2f ms for %d chunks%n", avgMs, chunksPerIteration);
        System.out.printf("Throughput: %.1f chunks/sec%n", chunksPerSec);
        System.out.printf("Per chunk: %.3f ms%n", msPerChunk);
        System.out.printf("Min: %.2f ms, Max: %.2f ms%n", min / 1_000_000.0, max / 1_000_000.0);

        return EXIT_SUCCESS;
    }

    private void runChunkGeneration(OTGChunkGenerator generator, OTGWorldInfo worldInfo,
                                     ObjectArrayList<JigsawStructureData> structures,
                                     Random random, long baseSeed, int numChunks, int iteration) {
        // Generate chunks in a grid pattern
        int gridSize = (int) Math.ceil(Math.sqrt(numChunks));
        int chunkOffset = iteration * gridSize; // Different chunks each iteration

        int generated = 0;
        for (int cx = 0; cx < gridSize && generated < numChunks; cx++) {
            for (int cz = 0; cz < gridSize && generated < numChunks; cz++) {
                int chunkX = cx + chunkOffset;
                int chunkZ = cz + chunkOffset;

                TestChunkBuffer buffer = new TestChunkBuffer(
                        chunkX, chunkZ,
                        worldInfo.minY(), worldInfo.maxY()
                );

                ChunkCoordinate chunkCoord = ChunkCoordinate.fromChunkCoords(chunkX, chunkZ);
                random.setSeed(baseSeed ^ ((long) chunkX * 341873128712L + (long) chunkZ * 132897987541L));

                generator.populateNoise(worldInfo, buffer, chunkCoord, structures, random);
                generated++;
            }
        }
    }

    /**
     * Runs the stress-test command - tests thread-safety of parallel chunk generation.
     */
    public int runStressTest(String[] args) {
        long seed = 12345;
        String presetName = DEFAULT_PRESET;
        String otgRoot = DEFAULT_OTG_ROOT;
        int threads = 4;
        int chunksPerThread = 100;

        // Parse arguments
        for (int i = 1; i < args.length; i++) {
            switch (args[i]) {
                case "--seed":
                    if (i + 1 < args.length) {
                        try {
                            seed = Long.parseLong(args[++i]);
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid seed value: " + args[i]);
                            return EXIT_ERROR;
                        }
                    }
                    break;
                case "--preset":
                    if (i + 1 < args.length) {
                        presetName = args[++i];
                    }
                    break;
                case "--otg-root":
                    if (i + 1 < args.length) {
                        otgRoot = args[++i];
                    }
                    break;
                case "--threads":
                    if (i + 1 < args.length) {
                        try {
                            threads = Integer.parseInt(args[++i]);
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid threads value: " + args[i]);
                            return EXIT_ERROR;
                        }
                    }
                    break;
                case "--chunks":
                    if (i + 1 < args.length) {
                        try {
                            chunksPerThread = Integer.parseInt(args[++i]);
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid chunks value: " + args[i]);
                            return EXIT_ERROR;
                        }
                    }
                    break;
            }
        }

        System.out.println("Stress Test Configuration:");
        System.out.println("  Preset: " + presetName);
        System.out.println("  Seed: " + seed);
        System.out.println("  Threads: " + threads);
        System.out.println("  Chunks per thread: " + chunksPerThread);
        System.out.println();

        try {
            return executeStressTest(seed, presetName, otgRoot, threads, chunksPerThread);
        } catch (Exception e) {
            System.err.println("Stress test failed: " + e.getMessage());
            OTGLog.error("Exception", e);
            return EXIT_ERROR;
        }
    }

    private int executeStressTest(long seed, String presetName, String otgRoot,
                                   int threadCount, int chunksPerThread) throws Exception {
        Path otgRootPath = Paths.get(otgRoot);

        // Initialize headless mode
        TestDimensionPresetLoader.initHeadless(otgRootPath);

        // Load preset
        DimensionPreset preset = TestDimensionPresetLoader.loadPreset(otgRootPath, presetName);
        if (preset == null) {
            throw new IllegalArgumentException("Preset not found: " + presetName);
        }

        OTGWorldInfo worldInfo = preset.getConfig().getWorldInfo();

        // Build IBiome array
        List<BiomeConfig> biomeConfigs = preset.getBiomeConfigList();
        int currentId = 1;
        for (BiomeConfig bc : biomeConfigs) {
            bc.setOTGBiomeId(currentId++);
        }
        int maxBiomeId = currentId - 1;

        IBiome[] biomes = new IBiome[maxBiomeId + 1];
        int[] availableBiomeIds = new int[biomeConfigs.size()];
        int idx = 0;
        for (BiomeConfig bc : biomeConfigs) {
            int id = bc.getOTGBiomeID().id();
            float temperature = bc.getVisualSettings().getBiomeTemperature();
            biomes[id] = new TestBiome(bc, temperature);
            availableBiomeIds[idx++] = id;
        }

        TestBiomeProvider biomeProvider = new TestBiomeProvider(seed, availableBiomeIds);

        // Create SHARED generator - this is the key: multiple threads hit the same instance
        OTGChunkGenerator generator = new OTGChunkGenerator(preset, biomeProvider, biomes, worldInfo);
        generator.setSeed(seed);

        System.out.println("Loaded preset: " + preset.getFolderName());
        System.out.println("Testing thread-safety with shared generator instance...");
        System.out.println();

        AtomicInteger errors = new AtomicInteger(0);
        AtomicInteger completed = new AtomicInteger(0);
        AtomicInteger chunksGenerated = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        long startTime = System.nanoTime();

        // Submit tasks for each thread
        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            final long threadSeed = seed ^ (threadId * 341873128712L);

            executor.submit(() -> {
                try {
                    ObjectArrayList<JigsawStructureData> structures = new ObjectArrayList<>();
                    Random random = new Random(threadSeed);

                    // Each thread generates different chunks (offset by threadId)
                    int gridSize = (int) Math.ceil(Math.sqrt(chunksPerThread));
                    int chunkOffset = threadId * gridSize * 2; // Non-overlapping regions

                    for (int i = 0; i < chunksPerThread; i++) {
                        int cx = (i % gridSize) + chunkOffset;
                        int cz = (i / gridSize) + chunkOffset;

                        TestChunkBuffer buffer = new TestChunkBuffer(
                                cx, cz,
                                worldInfo.minY(), worldInfo.maxY()
                        );

                        ChunkCoordinate chunkCoord = ChunkCoordinate.fromChunkCoords(cx, cz);
                        random.setSeed(threadSeed ^ ((long) cx * 341873128712L + (long) cz * 132897987541L));

                        // This is where thread-safety issues would manifest
                        generator.populateNoise(worldInfo, buffer, chunkCoord, structures, random);
                        chunksGenerated.incrementAndGet();
                    }
                    completed.incrementAndGet();
                } catch (Exception e) {
                    errors.incrementAndGet();
                    System.err.println("Thread " + threadId + " error: " + e.getMessage());
                    OTGLog.error("Exception", e);
                }
            });
        }

        executor.shutdown();
        boolean finished = executor.awaitTermination(5, TimeUnit.MINUTES);

        long elapsed = System.nanoTime() - startTime;
        double elapsedMs = elapsed / 1_000_000.0;
        int totalChunks = chunksGenerated.get();
        double chunksPerSec = totalChunks * 1_000_000_000.0 / elapsed;

        System.out.println();
        System.out.println("=== STRESS TEST RESULTS ===");
        System.out.println("Threads completed: " + completed.get() + "/" + threadCount);
        System.out.println("Errors: " + errors.get());
        System.out.println("Chunks generated: " + totalChunks);
        System.out.printf("Time: %.2f ms%n", elapsedMs);
        System.out.printf("Throughput: %.1f chunks/sec%n", chunksPerSec);

        if (!finished) {
            System.err.println("WARNING: Executor did not finish within timeout - possible deadlock!");
            return EXIT_ERROR;
        }

        if (errors.get() > 0) {
            System.err.println("FAILED: " + errors.get() + " thread(s) encountered errors");
            return EXIT_ERROR;
        }

        System.out.println();
        System.out.println("SUCCESS: No concurrency errors detected");
        return EXIT_SUCCESS;
    }

    /**
     * Runs the shadow-stress-test command - simulates ShadowChunkGenerator architecture.
     * Tests: BlockingQueue + worker threads + main thread waiting via CountDownLatch.
     */
    public int runShadowStressTest(String[] args) {
        long seed = 12345;
        String presetName = DEFAULT_PRESET;
        String otgRoot = DEFAULT_OTG_ROOT;
        int workers = 4;
        int totalChunks = 400;

        // Parse arguments
        for (int i = 1; i < args.length; i++) {
            switch (args[i]) {
                case "--seed":
                    if (i + 1 < args.length) {
                        try {
                            seed = Long.parseLong(args[++i]);
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid seed value: " + args[i]);
                            return EXIT_ERROR;
                        }
                    }
                    break;
                case "--preset":
                    if (i + 1 < args.length) {
                        presetName = args[++i];
                    }
                    break;
                case "--otg-root":
                    if (i + 1 < args.length) {
                        otgRoot = args[++i];
                    }
                    break;
                case "--workers":
                    if (i + 1 < args.length) {
                        try {
                            workers = Integer.parseInt(args[++i]);
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid workers value: " + args[i]);
                            return EXIT_ERROR;
                        }
                    }
                    break;
                case "--chunks":
                    if (i + 1 < args.length) {
                        try {
                            totalChunks = Integer.parseInt(args[++i]);
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid chunks value: " + args[i]);
                            return EXIT_ERROR;
                        }
                    }
                    break;
            }
        }

        System.out.println("Shadow Stress Test Configuration:");
        System.out.println("  Preset: " + presetName);
        System.out.println("  Seed: " + seed);
        System.out.println("  Workers: " + workers);
        System.out.println("  Total chunks: " + totalChunks);
        System.out.println();

        try {
            return executeShadowStressTest(seed, presetName, otgRoot, workers, totalChunks);
        } catch (Exception e) {
            System.err.println("Shadow stress test failed: " + e.getMessage());
            OTGLog.error("Exception", e);
            return EXIT_ERROR;
        }
    }

    private int executeShadowStressTest(long seed, String presetName, String otgRoot,
                                         int workerCount, int totalChunks) throws Exception {
        Path otgRootPath = Paths.get(otgRoot);

        // Initialize headless mode
        TestDimensionPresetLoader.initHeadless(otgRootPath);

        // Load preset
        DimensionPreset preset = TestDimensionPresetLoader.loadPreset(otgRootPath, presetName);
        if (preset == null) {
            throw new IllegalArgumentException("Preset not found: " + presetName);
        }

        OTGWorldInfo worldInfo = preset.getConfig().getWorldInfo();

        // Build IBiome array
        List<BiomeConfig> biomeConfigs = preset.getBiomeConfigList();
        int currentId = 1;
        for (BiomeConfig bc : biomeConfigs) {
            bc.setOTGBiomeId(currentId++);
        }
        int maxBiomeId = currentId - 1;

        IBiome[] biomes = new IBiome[maxBiomeId + 1];
        int[] availableBiomeIds = new int[biomeConfigs.size()];
        int idx = 0;
        for (BiomeConfig bc : biomeConfigs) {
            int id = bc.getOTGBiomeID().id();
            float temperature = bc.getVisualSettings().getBiomeTemperature();
            biomes[id] = new TestBiome(bc, temperature);
            availableBiomeIds[idx++] = id;
        }

        TestBiomeProvider biomeProvider = new TestBiomeProvider(seed, availableBiomeIds);

        // Create SHARED generator
        OTGChunkGenerator generator = new OTGChunkGenerator(preset, biomeProvider, biomes, worldInfo);
        generator.setSeed(seed);

        System.out.println("Loaded preset: " + preset.getFolderName());
        System.out.println("Simulating ShadowChunkGenerator architecture...");
        System.out.println();

        // === ShadowChunkGenerator-like architecture ===
        // Queue for chunks to generate (like ShadowChunkGenerator.chunksToLoad)
        LinkedBlockingDeque<ChunkCoordinate> chunksToLoad = new LinkedBlockingDeque<>(512);
        // Cache for generated chunks (like ShadowChunkGenerator.unloadedChunksCache)
        ConcurrentHashMap<ChunkCoordinate, TestChunkBuffer> chunkCache = new ConcurrentHashMap<>();
        // Track chunks being processed (like ShadowChunkGenerator.chunksBeingLoaded)
        ConcurrentHashMap<ChunkCoordinate, Integer> chunksBeingLoaded = new ConcurrentHashMap<>();
        // Latches for main thread to wait (like ShadowChunkGenerator.chunkLatches)
        ConcurrentHashMap<ChunkCoordinate, CountDownLatch> chunkLatches = new ConcurrentHashMap<>();

        AtomicInteger errors = new AtomicInteger(0);
        AtomicInteger workerChunksGenerated = new AtomicInteger(0);
        AtomicInteger mainThreadChunksGenerated = new AtomicInteger(0);
        AtomicInteger cacheHits = new AtomicInteger(0);
        AtomicBoolean stopWorkers = new AtomicBoolean(false);

        // Start worker threads
        Thread[] workers = new Thread[workerCount];
        for (int w = 0; w < workerCount; w++) {
            final int workerId = w;
            workers[w] = new Thread(() -> {
                ObjectArrayList<JigsawStructureData> structures = new ObjectArrayList<>();
                while (!stopWorkers.get()) {
                    ChunkCoordinate coord = null;
                    try {
                        coord = chunksToLoad.pollLast(50, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        if (stopWorkers.get()) return;
                        continue;
                    }

                    if (coord != null) {
                        // Mark as being processed
                        chunksBeingLoaded.put(coord, workerId);
                        CountDownLatch latch = chunkLatches.computeIfAbsent(coord, k -> new CountDownLatch(1));

                        try {
                            // Generate chunk
                            TestChunkBuffer buffer = new TestChunkBuffer(
                                    coord.getChunkX(), coord.getChunkZ(),
                                    worldInfo.minY(), worldInfo.maxY()
                            );
                            Random random = new Random(seed ^ ((long) coord.getChunkX() * 341873128712L + (long) coord.getChunkZ() * 132897987541L));
                            generator.populateNoise(worldInfo, buffer, coord, structures, random);

                            // Store in cache
                            chunkCache.put(coord, buffer);
                            workerChunksGenerated.incrementAndGet();
                        } catch (Exception e) {
                            errors.incrementAndGet();
                            System.err.println("Worker " + workerId + " error: " + e.getMessage());
                        } finally {
                            chunksBeingLoaded.remove(coord);
                            chunkLatches.remove(coord);
                            latch.countDown();
                        }
                    }
                }
            }, "OTG-TestWorker-" + w);
            workers[w].setDaemon(true);
            workers[w].start();
        }

        long startTime = System.nanoTime();

        // Main thread: queue chunks and wait for results (simulates server thread)
        int gridSize = (int) Math.ceil(Math.sqrt(totalChunks));
        int chunksQueued = 0;
        int chunksReceived = 0;

        // Queue first batch
        for (int i = 0; i < Math.min(totalChunks, 256); i++) {
            int cx = i % gridSize;
            int cz = i / gridSize;
            ChunkCoordinate coord = ChunkCoordinate.fromChunkCoords(cx, cz);
            chunksToLoad.offerFirst(coord);
            chunksQueued++;
        }

        // Process chunks as they complete, queue more
        ObjectArrayList<JigsawStructureData> mainStructures = new ObjectArrayList<>();
        while (chunksReceived < totalChunks) {
            int cx = chunksReceived % gridSize;
            int cz = chunksReceived / gridSize;
            ChunkCoordinate coord = ChunkCoordinate.fromChunkCoords(cx, cz);

            // Try to get from cache first (like getChunkWithWait)
            TestChunkBuffer cached = chunkCache.get(coord);
            if (cached != null) {
                cacheHits.incrementAndGet();
                chunkCache.remove(coord);
                chunksReceived++;
            } else if (chunksBeingLoaded.containsKey(coord)) {
                // Worker is generating - wait via latch
                CountDownLatch latch = chunkLatches.computeIfAbsent(coord, k -> new CountDownLatch(1));
                try {
                    if (!latch.await(5000, TimeUnit.MILLISECONDS)) {
                        // Timeout - generate ourselves
                        TestChunkBuffer buffer = new TestChunkBuffer(cx, cz, worldInfo.minY(), worldInfo.maxY());
                        Random random = new Random(seed ^ ((long) cx * 341873128712L + (long) cz * 132897987541L));
                        generator.populateNoise(worldInfo, buffer, coord, mainStructures, random);
                        mainThreadChunksGenerated.incrementAndGet();
                        chunksReceived++;
                    } else {
                        // Worker finished - get from cache
                        cached = chunkCache.remove(coord);
                        if (cached != null) {
                            cacheHits.incrementAndGet();
                        }
                        chunksReceived++;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            } else {
                // Not in queue, not being generated - generate on main thread
                TestChunkBuffer buffer = new TestChunkBuffer(cx, cz, worldInfo.minY(), worldInfo.maxY());
                Random random = new Random(seed ^ ((long) cx * 341873128712L + (long) cz * 132897987541L));
                generator.populateNoise(worldInfo, buffer, coord, mainStructures, random);
                mainThreadChunksGenerated.incrementAndGet();
                chunksReceived++;
            }

            // Queue more chunks if needed
            while (chunksQueued < totalChunks && chunksToLoad.size() < 256) {
                int qx = chunksQueued % gridSize;
                int qz = chunksQueued / gridSize;
                ChunkCoordinate qcoord = ChunkCoordinate.fromChunkCoords(qx, qz);
                if (!chunkCache.containsKey(qcoord) && !chunksBeingLoaded.containsKey(qcoord)) {
                    chunksToLoad.offerFirst(qcoord);
                }
                chunksQueued++;
            }
        }

        // Stop workers
        stopWorkers.set(true);
        for (Thread worker : workers) {
            worker.interrupt();
            worker.join(1000);
        }

        long elapsed = System.nanoTime() - startTime;
        double elapsedMs = elapsed / 1_000_000.0;
        double chunksPerSec = totalChunks * 1_000_000_000.0 / elapsed;

        System.out.println();
        System.out.println("=== SHADOW STRESS TEST RESULTS ===");
        System.out.println("Total chunks: " + totalChunks);
        System.out.println("Worker-generated: " + workerChunksGenerated.get());
        System.out.println("Main thread-generated: " + mainThreadChunksGenerated.get());
        System.out.println("Cache hits: " + cacheHits.get());
        System.out.println("Errors: " + errors.get());
        System.out.printf("Time: %.2f ms%n", elapsedMs);
        System.out.printf("Throughput: %.1f chunks/sec%n", chunksPerSec);

        double workerEfficiency = 100.0 * workerChunksGenerated.get() / totalChunks;
        System.out.printf("Worker efficiency: %.1f%% (higher = workers doing more work)%n", workerEfficiency);

        if (errors.get() > 0) {
            System.err.println("FAILED: " + errors.get() + " error(s) occurred");
            return EXIT_ERROR;
        }

        System.out.println();
        System.out.println("SUCCESS: ShadowChunkGenerator simulation passed");
        return EXIT_SUCCESS;
    }
}
