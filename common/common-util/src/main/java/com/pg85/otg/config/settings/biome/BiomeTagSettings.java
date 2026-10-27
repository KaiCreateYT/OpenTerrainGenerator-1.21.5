package com.pg85.otg.config.settings.biome;

import com.pg85.otg.config.annotation.*;
import com.pg85.otg.config.io.SettingsMap;
import com.pg85.otg.config.settings.ConfigSection;
import com.pg85.otg.config.settingtype.Setting;
import com.pg85.otg.config.settingtype.Settings;
import com.pg85.otg.constants.settings.BiomeType;
import com.pg85.otg.util.minecraft.WoodType;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
public class BiomeTagSettings extends ConfigSection {
    @Override
    public String[] getSectionComment() {
        return new String[]{
                "Settings for biome tags, used by datagen tag generation.",
                "Will be available at runtime, but can be inaccurate if not set correctly."
        };
    }

    public static final Setting<List<String>> BIOME_TAGS = Settings.stringListSetting(
            "BiomeTags",
            new String[]{""},
            t -> ((BiomeTagSettings)t).getBiomeTags(),
            "Biome tags used by other mods to identify a biome and",
            "place modded blocks, items and mobs in it.", "Example: HOT, DRY, SANDY, OVERWORLD",
            "Should match common modded biome tags."
    );
    public static final Setting<BiomeType> BIOME_TYPE = Settings.enumSetting(
            "BiomeType",
            BiomeType.OVERWORLD,
            t -> ((BiomeTagSettings)t).getBiomeType(),
            "If this is a biome config for an overworld biome, set this to Overworld. STONE is used for base terrain generation.",
            "If this is a biome config for a nether biome, set this to Nether. NETHERRACK is used for base terrain generation.",
            "If this is a biome config for an end biome, set this to End. END_STONE is used for base terrain generation"
    );

    private final BiomeType biomeType;
    private final List<String> biomeTags;

    // biome properties
    private final boolean aquatic;
    private final boolean aquaticIcy;
    private final boolean badlands;
    private final boolean beach;
    private final boolean birchForest;
    private final boolean cave;
    private final boolean cold;
    private final boolean coniferousTrees;
    private final boolean darkForest;
    private final boolean dead;
    private final boolean deciduousTrees;
    private final boolean deepOcean;
    private final boolean denseVegetation;
    private final boolean desert;
    private final boolean dry;
    private final boolean forest;
    private final boolean floral;
    private final boolean flowerForest;
    private final boolean hill;
    private final boolean hot;
    private final boolean icy;
    private final boolean jungle;
    private final boolean jungleTrees;
    private final boolean lush;
    private final boolean magical;
    private final boolean mountain;
    private final boolean mushroom;
    private final boolean peak;
    private final boolean slope;
    private final boolean netherForest;
    private final boolean ocean;
    private final boolean oldGrowth;
    private final boolean outerEndIsland;
    private final boolean plains;
    private final boolean plateau;
    private final boolean rare;
    private final boolean river;
    private final boolean sandy;
    private final boolean savanna;
    private final boolean savannaTrees;
    private final boolean shallowOcean;
    private final boolean snowy;
    private final boolean snowyPlains;
    private final boolean sparseVegetation;
    private final boolean spooky;
    private final boolean stonyShores;
    private final boolean swamp;
    private final boolean taiga;
    private final boolean temperate;
    private final boolean theVoid;
    private final boolean underground;
    private final boolean wasteland;
    private final boolean wet;
    private final boolean windswept;
    private final boolean noDefaultMonsters;
    private final boolean hiddenFromLocatorSelection;
    @EnumSetting("NONE")
    WoodType primaryWoodType;

    @Override
    public String getSectionName() {
        return "Biome Tag Settings";
    }

    public static BiomeTagSettings getBiomeTagConfig(SettingsMap reader, IdentitySettings legacy) {
        BiomeTagSettingsBuilder builder = builder();

        builder.biomeType(reader.getSetting(BiomeTagSettings.BIOME_TYPE));
        ArrayList<String> tags = new ArrayList<>(reader.getSetting(BiomeTagSettings.BIOME_TAGS));
        builder.biomeTags(tags);
        tags.add(reader.getSetting(OutdatedSettings.BIOME_CATEGORY));

        for (String dictTag : builder.biomeTags) {
            handleDictTag(dictTag, builder);
        }

        return builder.build();
    }

    private static void handleDictTag(String dictTag, BiomeTagSettingsBuilder builder) {
        switch (dictTag.toLowerCase()) {
            // Temperature based
            case "hot" -> builder.hot(true);
            case "cold" -> builder.cold(true);

            // Vegetation
            case "sparse" -> builder.sparseVegetation(true);
            case "dense", "dense_vegetation" -> builder.denseVegetation(true);

            // Wetness
            case "wet" -> builder.wet(true);
            case "dry" -> builder.dry(true);

            // Tree type
            case "savanna_trees" -> builder.savannaTrees(true);
            case "jungle_trees" -> builder.jungleTrees(true);
            case "coniferous", "coniferous_trees" -> builder.coniferousTrees(true);
            case "deciduous","deciduous_trees" -> builder.deciduousTrees(true);


            // Vibe
            case "spooky" -> builder.spooky(true);
            case "dead" -> builder.dead(true);
            case "lush" -> builder.lush(true);
            case "mushroom" -> builder.mushroom(true);
            case "magical" -> builder.magical(true);
            case "rare" -> builder.rare(true);
            case "plateau" -> builder.plateau(true);
            case "modified" -> {} // can't find a fitting tag

            // Water tags
            case "ocean" -> builder.ocean(true);
            case "river" -> builder.river(true);
            case "water", "aquatic" -> builder.aquatic(true);

            // Generic biome types
            case "mesa" -> builder.badlands(true);
            case "forest" -> builder.forest(true);
            case "plains" -> builder.plains(true);
            case "mountain" -> builder.mountain(true);
            case "hills", "hill" -> builder.hill(true);
            case "swamp" -> builder.swamp(true);
            case "sandy" -> builder.sandy(true);
            case "snowy" -> builder.snowy(true);
            case "wasteland" -> builder.wasteland(true);
            case "beach" -> builder.beach(true);
            case "void", "the_void" -> builder.theVoid(true);

            // Dimension tags
            case "overworld" -> builder.biomeType(BiomeType.OVERWORLD);
            case "nether" -> builder.biomeType(BiomeType.NETHER);
            case "end", "the_end" -> builder.biomeType(BiomeType.END);

            // Biome category not covered elsewhere
            case "none" -> {}
            case "taiga" -> builder.taiga(true);
            case "extreme_hills" -> {
                builder.hill(true);
                builder.mountain(true);
            }
            case "icy" -> builder.icy(true);
            case "desert" -> builder.desert(true);

            case "aquatic_icy" -> builder.aquaticIcy(true);
            case "badlands" -> builder.badlands(true);
            case "birch_forest" -> builder.birchForest(true);
            case "cave" -> builder.cave(true);
            case "dark_forest" -> builder.darkForest(true);
            case "deep_ocean" -> builder.deepOcean(true);
            case "floral" -> builder.floral(true);
            case "flower_forest" -> builder.flowerForest(true);
            case "jungle" -> builder.jungle(true);
            case "peak" -> builder.peak(true);
            case "slope" -> builder.slope(true);
            case "nether_forest" -> builder.netherForest(true);
            case "old_growth" -> builder.oldGrowth(true);
            case "outer_end_island" -> builder.outerEndIsland(true);
            case "savanna" -> builder.savanna(true);
            case "shallow_ocean" -> builder.shallowOcean(true);
            case "snowy_plains" -> builder.snowyPlains(true);
            case "sparse_vegetation" -> builder.sparseVegetation(true);
            case "stony_shores" -> builder.stonyShores(true);
            case "temperate" -> builder.temperate(true);
            case "underground" -> builder.underground(true);
            case "windswept" -> builder.windswept(true);
            case "no_default_monsters" -> builder.noDefaultMonsters(true);
            case "hidden_from_locator_selection" -> builder.hiddenFromLocatorSelection(true);
        }
    }

    public void writeConfig(SettingsMap writer) {
        for (Setting<?> s : this.getSettingsList()) {
            writer.putSetting(s, this);
        }
    }
}
