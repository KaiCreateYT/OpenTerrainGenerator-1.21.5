package com.pg85.otg.test.materials;

import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.util.LRUCache;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterialTag;
import com.pg85.otg.util.materials.LocalMaterials;
import com.pg85.otg.util.minecraft.BlockNames;

import java.util.HashMap;
import java.util.Map;

/**
 * IMaterialReader implementation for headless testing.
 * Parses material strings into TestMaterialData without Minecraft dependencies.
 *
 * Supported formats:
 * - "minecraft:stone" -> TestMaterialData("minecraft:stone")
 * - "stone" -> TestMaterialData("minecraft:stone")
 * - "STONE" (legacy uppercase) -> TestMaterialData("minecraft:stone")
 * - "1" (legacy block ID) -> TestMaterialData("minecraft:stone")
 * - "12:1" (legacy ID:data) -> TestMaterialData("minecraft:sand") with variant handling
 * - "blank" -> special blank marker
 * - "#minecraft:logs" -> TestMaterialTag
 */
public class TestMaterialReader implements IMaterialReader {

    private final LRUCache<String, LocalMaterialData> cachedMaterials = new LRUCache<>(4096);
    private final LRUCache<String, LocalMaterialTag> cachedTags = new LRUCache<>(4096);

    // Legacy uppercase block name mapping
    private static final Map<String, String> LEGACY_NAMES = new HashMap<>();

    static {
        // Common legacy uppercase names to modern lowercase
        LEGACY_NAMES.put("STONE", "stone");
        LEGACY_NAMES.put("GRASS", "grass_block");
        LEGACY_NAMES.put("GRASS_BLOCK", "grass_block");
        LEGACY_NAMES.put("DIRT", "dirt");
        LEGACY_NAMES.put("COBBLESTONE", "cobblestone");
        LEGACY_NAMES.put("SAND", "sand");
        LEGACY_NAMES.put("GRAVEL", "gravel");
        LEGACY_NAMES.put("GOLD_ORE", "gold_ore");
        LEGACY_NAMES.put("IRON_ORE", "iron_ore");
        LEGACY_NAMES.put("COAL_ORE", "coal_ore");
        LEGACY_NAMES.put("LOG", "oak_log");
        LEGACY_NAMES.put("OAK_LOG", "oak_log");
        LEGACY_NAMES.put("LEAVES", "oak_leaves");
        LEGACY_NAMES.put("OAK_LEAVES", "oak_leaves");
        LEGACY_NAMES.put("WATER", "water");
        LEGACY_NAMES.put("STATIONARY_WATER", "water");
        LEGACY_NAMES.put("LAVA", "lava");
        LEGACY_NAMES.put("STATIONARY_LAVA", "lava");
        LEGACY_NAMES.put("BEDROCK", "bedrock");
        LEGACY_NAMES.put("CLAY", "clay");
        LEGACY_NAMES.put("SANDSTONE", "sandstone");
        LEGACY_NAMES.put("SNOW", "snow");
        LEGACY_NAMES.put("SNOW_BLOCK", "snow_block");
        LEGACY_NAMES.put("ICE", "ice");
        LEGACY_NAMES.put("PACKED_ICE", "packed_ice");
        LEGACY_NAMES.put("NETHERRACK", "netherrack");
        LEGACY_NAMES.put("SOUL_SAND", "soul_sand");
        LEGACY_NAMES.put("GLOWSTONE", "glowstone");
        LEGACY_NAMES.put("END_STONE", "end_stone");
        LEGACY_NAMES.put("OBSIDIAN", "obsidian");
        LEGACY_NAMES.put("MYCELIUM", "mycelium");
        LEGACY_NAMES.put("PODZOL", "podzol");
        LEGACY_NAMES.put("TERRACOTTA", "terracotta");
        LEGACY_NAMES.put("HARDENED_CLAY", "terracotta");
        LEGACY_NAMES.put("STAINED_CLAY", "terracotta");
        LEGACY_NAMES.put("AIR", "air");
        LEGACY_NAMES.put("CAVE_AIR", "cave_air");
        LEGACY_NAMES.put("DEEPSLATE", "deepslate");
        LEGACY_NAMES.put("TUFF", "tuff");
        LEGACY_NAMES.put("DRIPSTONE_BLOCK", "dripstone_block");
        LEGACY_NAMES.put("CALCITE", "calcite");
        LEGACY_NAMES.put("AMETHYST_BLOCK", "amethyst_block");
        LEGACY_NAMES.put("SMOOTH_BASALT", "smooth_basalt");
    }

    // Materials that are liquids
    private static final String[] LIQUID_MATERIALS = {
        "water", "lava", "flowing_water", "flowing_lava"
    };

    // Materials that are air-like
    private static final String[] AIR_MATERIALS = {
        "air", "cave_air", "void_air", "structure_void"
    };

    // Materials that are non-solid (plants, decorations)
    private static final String[] NON_SOLID_MATERIALS = {
        "grass", "tall_grass", "fern", "large_fern", "dead_bush",
        "dandelion", "poppy", "blue_orchid", "allium", "azure_bluet",
        "red_tulip", "orange_tulip", "white_tulip", "pink_tulip", "oxeye_daisy",
        "cornflower", "lily_of_the_valley", "wither_rose", "sunflower",
        "lilac", "rose_bush", "peony", "torch", "wall_torch", "redstone_torch",
        "snow", "vine", "lily_pad", "sugar_cane", "kelp", "kelp_plant",
        "seagrass", "tall_seagrass", "sea_pickle", "bamboo", "bamboo_sapling",
        "sweet_berry_bush", "red_mushroom", "brown_mushroom", "crimson_fungus",
        "warped_fungus", "crimson_roots", "warped_roots", "nether_sprouts",
        "twisting_vines", "weeping_vines", "glow_lichen", "moss_carpet",
        "hanging_roots", "small_dripleaf", "big_dripleaf", "spore_blossom",
        	"azalea", "flowering_azalea",
        	"bush", "leaf_litter", "cactus_flower", "firefly_bush", "wildflowers",
        	"short_dry_grass", "tall_dry_grass", "pale_hanging_moss"
    };

    @Override
    public LocalMaterialData readMaterial(String material) throws InvalidConfigException {
        if (material == null || material.trim().isEmpty()) {
            return null;
        }

        // Check cache first
        LocalMaterialData cached = cachedMaterials.get(material);
        if (cached != null) {
            return cached;
        }
        if (cachedMaterials.containsKey(material)) {
            // Cached as null = invalid
            return null;
        }

        LocalMaterialData result = parseMaterial(material);
        cachedMaterials.put(material, result);
        return result;
    }

    @Override
    public LocalMaterialTag readTag(String tag) throws InvalidConfigException {
        if (tag == null || tag.trim().isEmpty()) {
            return null;
        }

        LocalMaterialTag cached = cachedTags.get(tag);
        if (cached != null) {
            return cached;
        }

        // Normalize tag format
        String normalized = tag;
        if (normalized.startsWith("#")) {
            normalized = normalized.substring(1);
        }
        if (!normalized.contains(":")) {
            normalized = "minecraft:" + normalized;
        }

        TestMaterialTag result = new TestMaterialTag(normalized);
        cachedTags.put(tag, result);
        return result;
    }

    private LocalMaterialData parseMaterial(String input) {
        String trimmed = input.trim();

        // Handle "blank" special marker
        if (trimmed.equalsIgnoreCase("blank")) {
            return createBlankMaterial();
        }

        // Handle minecraft:name:data format (strip extra data)
        if (trimmed.matches("minecraft:[A-Za-z_]+:[0-9]+")) {
            String[] parts = trimmed.split(":");
            trimmed = parts[1] + ":" + parts[2];
        }

        String blockName = trimmed.toLowerCase();

        // Try to parse as legacy block ID
        if (!blockName.contains(":") && !blockName.contains("[")) {
            // Try legacy uppercase name first
            String legacyMapped = LEGACY_NAMES.get(trimmed.toUpperCase());
            if (legacyMapped != null) {
                return createMaterial("minecraft:" + legacyMapped);
            }

            // Try parsing as numeric block ID
            try {
                // Handle accidental floats like "1.0"
                String numStr = blockName;
                if (numStr.endsWith(".0")) {
                    numStr = numStr.substring(0, numStr.length() - 2);
                }
                int blockId = Integer.parseInt(numStr);
                String fromLegacyId = BlockNames.blockNameFromLegacyBlockId(blockId);
                if (fromLegacyId != null) {
                    return createMaterial("minecraft:" + fromLegacyId);
                }
            } catch (NumberFormatException ignored) {
                // Not a number, continue
            }
        }

        // Handle legacy block:data format (e.g., "12:1" for red sand, "SAND:1")
        if (blockName.contains(":") && !blockName.startsWith("minecraft:")) {
            String[] parts = blockName.split(":");
            if (parts.length == 2) {
                try {
                    int data = Integer.parseInt(parts[1]);
                    String baseName = parts[0];

                    // Try as numeric ID first
                    try {
                        int blockId = Integer.parseInt(baseName);
                        String fromLegacyId = BlockNames.blockNameFromLegacyBlockId(blockId);
                        if (fromLegacyId != null) {
                            baseName = fromLegacyId;
                        }
                    } catch (NumberFormatException ignored) {
                        // Not numeric, use as-is
                    }

                    // Handle data variants
                    String variantName = resolveDataVariant(baseName, data);
                    if (variantName != null) {
                        return createMaterial("minecraft:" + variantName);
                    }

                    // Just use the base name
                    return createMaterial("minecraft:" + baseName.toLowerCase());
                } catch (NumberFormatException ignored) {
                    // Data wasn't numeric, might be minecraft:blockname format
                }
            }
        }

        // Strip block state properties [property=value] - we don't track them in test mode
        if (blockName.contains("[")) {
            blockName = blockName.substring(0, blockName.indexOf("["));
        }

        // Add minecraft: prefix if missing
        if (!blockName.contains(":")) {
            blockName = "minecraft:" + blockName;
        }

        return createMaterial(blockName);
    }

    /**
     * Creates a material with proper solid/liquid/air classification.
     */
    private LocalMaterialData createMaterial(String registryName) {
        String simpleName = registryName.replace("minecraft:", "");

        // Check if it's a liquid
        for (String liquid : LIQUID_MATERIALS) {
            if (simpleName.equals(liquid)) {
                return TestMaterialData.liquid(registryName);
            }
        }

        // Check if it's air
        for (String air : AIR_MATERIALS) {
            if (simpleName.equals(air)) {
                return TestMaterialData.air(registryName);
            }
        }

        // Check if it's non-solid
        for (String nonSolid : NON_SOLID_MATERIALS) {
            if (simpleName.equals(nonSolid)) {
                return TestMaterialData.nonSolid(registryName);
            }
        }

        // Default to solid
        return TestMaterialData.solid(registryName);
    }

    /**
     * Creates a blank material (used as placeholder in BO4).
     */
    private LocalMaterialData createBlankMaterial() {
        TestMaterialData blank = TestMaterialData.air("minecraft:air");
        // Mark as blank via internal state if needed
        return blank;
    }

    /**
     * Resolves legacy block:data variants to modern block names.
     * For example: sand:1 -> red_sand, log:1 -> spruce_log
     */
    private String resolveDataVariant(String baseName, int data) {
        String lowerName = baseName.toLowerCase();

        // Sand variants
        if (lowerName.equals("sand") && data == 1) {
            return "red_sand";
        }

        // Dirt variants
        if (lowerName.equals("dirt")) {
            return switch (data) {
                case 1 -> "coarse_dirt";
                case 2 -> "podzol";
                default -> "dirt";
            };
        }

        // Stone variants
        if (lowerName.equals("stone")) {
            return switch (data) {
                case 1 -> "granite";
                case 2 -> "polished_granite";
                case 3 -> "diorite";
                case 4 -> "polished_diorite";
                case 5 -> "andesite";
                case 6 -> "polished_andesite";
                default -> "stone";
            };
        }

        // Log variants
        if (lowerName.equals("log")) {
            return switch (data & 3) { // Lower 2 bits = wood type
                case 0 -> "oak_log";
                case 1 -> "spruce_log";
                case 2 -> "birch_log";
                case 3 -> "jungle_log";
                default -> "oak_log";
            };
        }

        // Log2 variants (acacia, dark oak)
        if (lowerName.equals("log2")) {
            return switch (data & 1) {
                case 0 -> "acacia_log";
                case 1 -> "dark_oak_log";
                default -> "acacia_log";
            };
        }

        // Leaves variants
        if (lowerName.equals("leaves")) {
            return switch (data & 3) {
                case 0 -> "oak_leaves";
                case 1 -> "spruce_leaves";
                case 2 -> "birch_leaves";
                case 3 -> "jungle_leaves";
                default -> "oak_leaves";
            };
        }

        // Leaves2 variants
        if (lowerName.equals("leaves2")) {
            return switch (data & 1) {
                case 0 -> "acacia_leaves";
                case 1 -> "dark_oak_leaves";
                default -> "acacia_leaves";
            };
        }

        // Sandstone variants
        if (lowerName.equals("sandstone")) {
            return switch (data) {
                case 1 -> "chiseled_sandstone";
                case 2 -> "cut_sandstone";
                default -> "sandstone";
            };
        }

        // Terracotta / stained clay
        if (lowerName.equals("stained_clay") || lowerName.equals("stained_hardened_clay")) {
            return switch (data) {
                case 0 -> "white_terracotta";
                case 1 -> "orange_terracotta";
                case 2 -> "magenta_terracotta";
                case 3 -> "light_blue_terracotta";
                case 4 -> "yellow_terracotta";
                case 5 -> "lime_terracotta";
                case 6 -> "pink_terracotta";
                case 7 -> "gray_terracotta";
                case 8 -> "light_gray_terracotta";
                case 9 -> "cyan_terracotta";
                case 10 -> "purple_terracotta";
                case 11 -> "blue_terracotta";
                case 12 -> "brown_terracotta";
                case 13 -> "green_terracotta";
                case 14 -> "red_terracotta";
                case 15 -> "black_terracotta";
                default -> "terracotta";
            };
        }

        // Wool colors
        if (lowerName.equals("wool")) {
            return switch (data) {
                case 0 -> "white_wool";
                case 1 -> "orange_wool";
                case 2 -> "magenta_wool";
                case 3 -> "light_blue_wool";
                case 4 -> "yellow_wool";
                case 5 -> "lime_wool";
                case 6 -> "pink_wool";
                case 7 -> "gray_wool";
                case 8 -> "light_gray_wool";
                case 9 -> "cyan_wool";
                case 10 -> "purple_wool";
                case 11 -> "blue_wool";
                case 12 -> "brown_wool";
                case 13 -> "green_wool";
                case 14 -> "red_wool";
                case 15 -> "black_wool";
                default -> "white_wool";
            };
        }

        // Planks
        if (lowerName.equals("planks") || lowerName.equals("wood")) {
            return switch (data) {
                case 0 -> "oak_planks";
                case 1 -> "spruce_planks";
                case 2 -> "birch_planks";
                case 3 -> "jungle_planks";
                case 4 -> "acacia_planks";
                case 5 -> "dark_oak_planks";
                default -> "oak_planks";
            };
        }

        // Sapling
        if (lowerName.equals("sapling")) {
            return switch (data & 7) {
                case 0 -> "oak_sapling";
                case 1 -> "spruce_sapling";
                case 2 -> "birch_sapling";
                case 3 -> "jungle_sapling";
                case 4 -> "acacia_sapling";
                case 5 -> "dark_oak_sapling";
                default -> "oak_sapling";
            };
        }

        // No special mapping
        return null;
    }
}
