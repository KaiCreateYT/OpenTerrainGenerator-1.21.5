package com.pg85.otg.config.settings.biome;

import com.pg85.otg.config.io.SettingsMap;
import com.pg85.otg.config.settingtype.Setting;
import com.pg85.otg.config.settingtype.Settings;
import com.pg85.otg.config.settings.ConfigSection;
import com.pg85.otg.util.biome.WeightedMobSpawnGroup;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class MobSettings extends ConfigSection {
    private final List<WeightedMobSpawnGroup> monsters;
    private final List<WeightedMobSpawnGroup> creatures;
    private final List<WeightedMobSpawnGroup> waterCreatures;
    private final List<WeightedMobSpawnGroup> ambientCreatures;
    private final List<WeightedMobSpawnGroup> waterAmbientCreatures;
    private final List<WeightedMobSpawnGroup> miscCreatures;
    private final String inheritMobsBiomeName;

    @Override
    public String getSectionName() {
        return "Biome Mob Settings";
    }

    public static final Setting<List<WeightedMobSpawnGroup>> SPAWN_MONSTERS = Settings.mobGroupListSetting(
            "SpawnMonsters",
            t -> ((MobSettings)t).getMonsters(),
            "The monsters (blazes, cave spiders, creepers, drowned, elder guardians, ender dragons, endermen, endermites, evokers, ghasts, giants,",
            "guardians, hoglins, husks, illusioners, magma cubes, phantoms, piglins, pillagers, ravagers, shulkers, silverfishes, skeletons, slimes,",
            "spiders, strays, vexes, vindicators, witches, zoglins, zombies, zombie villagers, zombified piglins) that spawn in this biome.",
            "For instance [{\"mob\": \"minecraft:spider\", \"weight\": 100, \"min\": 4, \"max\": 4}, {\"mob\": \"minecraft:zombie\", \"weight\": 100, \"min\": 4, \"max\": 4}]",
            "Use the \"/otg entities\" console command to get a list of possible mobs and mob categories.",
            "Use the \"/otg biome -m\" console command to get the list of registered mobs for a biome."
    );
    public static final Setting<List<WeightedMobSpawnGroup>> SPAWN_CREATURES = Settings.mobGroupListSetting(
            "SpawnCreatures",
            t -> ((MobSettings)t).getCreatures(),
            "The friendly creatures (bees, cats, chickens, cows, donkeys, foxes, horses, llama, mooshrooms, mules, ocelots, panda's, parrots,",
            "pigs, polar bears, rabbits, sheep, skeleton horses, striders, trader llama's, turtles, wandering traders, wolves, zombie horses)",
            "that spawn in this biome.",
            "For instance [{\"mob\": \"minecraft:sheep\", \"weight\": 12, \"min\": 4, \"max\": 4}, {\"mob\": \"minecraft:pig\", \"weight\": 10, \"min\": 4, \"max\": 4}]",
            "Use the \"/otg entities\" console command to get a list of possible mobs and mob categories.",
            "Use the \"/otg biome -m\" console command to get the list of registered mobs for a biome."
    );
    public static final Setting<List<WeightedMobSpawnGroup>> SPAWN_WATER_CREATURES = Settings.mobGroupListSetting(
            "SpawnWaterCreatures",
            t -> ((MobSettings)t).getWaterCreatures(),
            "The water creatures (squids and dolphins) that spawn in this biome",
            "For instance [{\"mob\": \"minecraft:squid\", \"weight\": 10, \"min\": 4, \"max\": 4}]",
            "Use the \"/otg entities\" console command to get a list of possible mobs and mob categories.",
            "Use the \"/otg biome -m\" console command to get the list of registered mobs for a biome."
    );
    public static final Setting<List<WeightedMobSpawnGroup>> SPAWN_AMBIENT_CREATURES = Settings.mobGroupListSetting(
            "SpawnAmbientCreatures",
            t -> ((MobSettings)t).getAmbientCreatures(),
            "The ambient creatures (only bats in vanila) that spawn in this biome",
            "For instance [{\"mob\": \"minecraft:bat\", \"weight\": 10, \"min\": 8, \"max\": 8}]",
            "Use the \"/otg entities\" console command to get a list of possible mobs and mob categories.",
            "Use the \"/otg biome -m\" console command to get the list of registered mobs for a biome."
    );
    public static final Setting<List<WeightedMobSpawnGroup>> SPAWN_WATER_AMBIENT_CREATURES = Settings.mobGroupListSetting(
            "SpawnWaterAmbientCreatures",
            t -> ((MobSettings)t).getWaterAmbientCreatures(),
            "The ambient water creatures (cod, pufferfish, salmon, tropical fish) that spawn in this biome",
            "For instance [{\"mob\": \"minecraft:cod\", \"weight\": 10, \"min\": 8, \"max\": 8}]",
            "Use the \"/otg entities\" console command to get a list of possible mobs and mob categories.",
            "Use the \"/otg biome -m\" console command to get the list of registered mobs for a biome."
    );
    public static final Setting<List<WeightedMobSpawnGroup>> SPAWN_MISC_CREATURES = Settings.mobGroupListSetting(
            "SpawnMiscCreatures",
            t -> ((MobSettings)t).getMiscCreatures(),
            "The miscellaneous creatures (iron golems, snow golems and villagers) that spawn in this biome",
            "For instance [{\"mob\": \"minecraft:villager\", \"weight\": 10, \"min\": 8, \"max\": 8}]",
            "Use the \"/otg entities\" console command to get a list of possible mobs and mob categories.",
            "Use the \"/otg biome -m\" console command to get the list of registered mobs for a biome."
    );
    public static final Setting<String> INHERIT_MOBS_BIOME_NAME = Settings.stringSetting(
            "InheritMobsBiomeName", "",
            t -> ((MobSettings)t).getInheritMobsBiomeName(),
            "Inherit the internal mobs list of another biome. Inherited mobs can be overridden using",
            "the mob spawn settings in this biome config. Any mob type defined in this biome config",
            "will override inherited mob settings for the same mob in the same mob category.",
            "Use this setting to inherit mob spawn lists from other biomes.",
            "Accepts both OTG and non-OTG (vanilla or other mods') biomes. See also: BiomeDictTags."
    );

    public static MobSettings getMobSettings(SettingsMap reader) {
        MobSettingsBuilder builder = MobSettings.builder();

        builder.monsters(reader.getSetting(SPAWN_MONSTERS));
        builder.creatures(reader.getSetting(SPAWN_CREATURES));
        builder.waterCreatures(reader.getSetting(SPAWN_WATER_CREATURES));
        builder.ambientCreatures(reader.getSetting(SPAWN_AMBIENT_CREATURES));
        builder.waterAmbientCreatures(reader.getSetting(SPAWN_WATER_AMBIENT_CREATURES));
        builder.miscCreatures(reader.getSetting(SPAWN_MISC_CREATURES));
        builder.inheritMobsBiomeName(reader.getSetting(INHERIT_MOBS_BIOME_NAME));

        return builder.build();
    }
}
