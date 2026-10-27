package com.pg85.otg.config.settings.biome.generated;

import com.pg85.otg.config.settings.ConfigSection;
import com.pg85.otg.config.settingtype.Settings;
import com.pg85.otg.config.settingtype.Setting;
import com.pg85.otg.config.io.SettingsMap;
import java.util.List;
import com.pg85.otg.config.settings.biome.BiomeStructureTagConfig;

public class BiomeStructureTagSettings extends ConfigSection {

	@Override
	public String getSectionName() {
		return "Biome Structure Tag Settings";
	}


	public static final Setting<Boolean> DISPLAY_ALL_STRUCTURE_TAGS = Settings.booleanSetting("DisplayAllStructureTags", false, t -> ((BiomeStructureTagConfig) t).isDisplayAllStructureTags(), "Set to true to display all biome tag settings for this biome", "By default, only the settings that differ from the default values are displayed.");
	public static final Setting<Boolean> ANCIENT_CITY = Settings.booleanSetting("AncientCity", false, t -> ((BiomeStructureTagConfig) t).isAncientCity(), "");
	public static final Setting<Boolean> BASTION_REMNANT = Settings.booleanSetting("BastionRemnant", false, t -> ((BiomeStructureTagConfig) t).isBastionRemnant(), "");
	public static final Setting<Boolean> BURIED_TREASURE = Settings.booleanSetting("BuriedTreasure", false, t -> ((BiomeStructureTagConfig) t).isBuriedTreasure(), "");
	public static final Setting<Boolean> DESERT_PYRAMID = Settings.booleanSetting("DesertPyramid", false, t -> ((BiomeStructureTagConfig) t).isDesertPyramid(), "");
	public static final Setting<Boolean> END_CITY = Settings.booleanSetting("EndCity", false, t -> ((BiomeStructureTagConfig) t).isEndCity(), "");
	public static final Setting<Boolean> IGLOO = Settings.booleanSetting("Igloo", false, t -> ((BiomeStructureTagConfig) t).isIgloo(), "");
	public static final Setting<Boolean> JUNGLE_TEMPLE = Settings.booleanSetting("JungleTemple", false, t -> ((BiomeStructureTagConfig) t).isJungleTemple(), "");
	public static final Setting<Boolean> MINESHAFT = Settings.booleanSetting("Mineshaft", false, t -> ((BiomeStructureTagConfig) t).isMineshaft(), "");
	public static final Setting<Boolean> MINESHAFT_MESA = Settings.booleanSetting("MineshaftMesa", false, t -> ((BiomeStructureTagConfig) t).isMineshaftMesa(), "");
	public static final Setting<Boolean> NETHER_FORTRESS = Settings.booleanSetting("NetherFortress", false, t -> ((BiomeStructureTagConfig) t).isNetherFortress(), "");
	public static final Setting<Boolean> NETHER_FOSSIL = Settings.booleanSetting("NetherFossil", false, t -> ((BiomeStructureTagConfig) t).isNetherFossil(), "");
	public static final Setting<Boolean> OCEAN_MONUMENT = Settings.booleanSetting("OceanMonument", false, t -> ((BiomeStructureTagConfig) t).isOceanMonument(), "");
	public static final Setting<Boolean> OCEAN_RUIN_COLD = Settings.booleanSetting("OceanRuinCold", false, t -> ((BiomeStructureTagConfig) t).isOceanRuinCold(), "");
	public static final Setting<Boolean> OCEAN_RUIN_WARM = Settings.booleanSetting("OceanRuinWarm", false, t -> ((BiomeStructureTagConfig) t).isOceanRuinWarm(), "");
	public static final Setting<Boolean> PILLAGER_OUTPOST = Settings.booleanSetting("PillagerOutpost", false, t -> ((BiomeStructureTagConfig) t).isPillagerOutpost(), "");
	public static final Setting<Boolean> RUINED_PORTAL_DESERT = Settings.booleanSetting("RuinedPortalDesert", false, t -> ((BiomeStructureTagConfig) t).isRuinedPortalDesert(), "");
	public static final Setting<Boolean> RUINED_PORTAL_JUNGLE = Settings.booleanSetting("RuinedPortalJungle", false, t -> ((BiomeStructureTagConfig) t).isRuinedPortalJungle(), "");
	public static final Setting<Boolean> RUINED_PORTAL_MOUNTAIN = Settings.booleanSetting("RuinedPortalMountain", false, t -> ((BiomeStructureTagConfig) t).isRuinedPortalMountain(), "");
	public static final Setting<Boolean> RUINED_PORTAL_NETHER = Settings.booleanSetting("RuinedPortalNether", false, t -> ((BiomeStructureTagConfig) t).isRuinedPortalNether(), "");
	public static final Setting<Boolean> RUINED_PORTAL_OCEAN = Settings.booleanSetting("RuinedPortalOcean", false, t -> ((BiomeStructureTagConfig) t).isRuinedPortalOcean(), "");
	public static final Setting<Boolean> RUINED_PORTAL_STANDARD = Settings.booleanSetting("RuinedPortalStandard", false, t -> ((BiomeStructureTagConfig) t).isRuinedPortalStandard(), "");
	public static final Setting<Boolean> RUINED_PORTAL_SWAMP = Settings.booleanSetting("RuinedPortalSwamp", false, t -> ((BiomeStructureTagConfig) t).isRuinedPortalSwamp(), "");
	public static final Setting<Boolean> SHIPWRECK = Settings.booleanSetting("Shipwreck", false, t -> ((BiomeStructureTagConfig) t).isShipwreck(), "");
	public static final Setting<Boolean> SHIPWRECK_BEACHED = Settings.booleanSetting("ShipwreckBeached", false, t -> ((BiomeStructureTagConfig) t).isShipwreckBeached(), "");
	public static final Setting<Boolean> STRONGHOLD = Settings.booleanSetting("Stronghold", false, t -> ((BiomeStructureTagConfig) t).isStronghold(), "");
	public static final Setting<Boolean> SWAMP_HUT = Settings.booleanSetting("SwampHut", false, t -> ((BiomeStructureTagConfig) t).isSwampHut(), "");
	public static final Setting<Boolean> TRAIL_RUINS = Settings.booleanSetting("TrailRuins", false, t -> ((BiomeStructureTagConfig) t).isTrailRuins(), "");
	public static final Setting<Boolean> TRIAL_CHAMBERS = Settings.booleanSetting("TrialChambers", false, t -> ((BiomeStructureTagConfig) t).isTrialChambers(), "");
	public static final Setting<Boolean> VILLAGE_DESERT = Settings.booleanSetting("VillageDesert", false, t -> ((BiomeStructureTagConfig) t).isVillageDesert(), "");
	public static final Setting<Boolean> VILLAGE_PLAINS = Settings.booleanSetting("VillagePlains", false, t -> ((BiomeStructureTagConfig) t).isVillagePlains(), "");
	public static final Setting<Boolean> VILLAGE_SAVANNA = Settings.booleanSetting("VillageSavanna", false, t -> ((BiomeStructureTagConfig) t).isVillageSavanna(), "");
	public static final Setting<Boolean> VILLAGE_SNOWY = Settings.booleanSetting("VillageSnowy", false, t -> ((BiomeStructureTagConfig) t).isVillageSnowy(), "");
	public static final Setting<Boolean> VILLAGE_TAIGA = Settings.booleanSetting("VillageTaiga", false, t -> ((BiomeStructureTagConfig) t).isVillageTaiga(), "");
	public static final Setting<Boolean> WOODLAND_MANSION = Settings.booleanSetting("WoodlandMansion", false, t -> ((BiomeStructureTagConfig) t).isWoodlandMansion(), "");
	public static final Setting<Boolean> WOODLAND_HOUSE = Settings.booleanSetting("WoodlandHouse", false, t -> ((BiomeStructureTagConfig) t).isWoodlandHouse(), "");

	public static BiomeStructureTagConfig.BiomeStructureTagConfigBuilder getBuilder(SettingsMap settingsMap) {
		var builder = BiomeStructureTagConfig.builder();
		builder.displayAllStructureTags(settingsMap.getSetting(DISPLAY_ALL_STRUCTURE_TAGS));
		builder.ancientCity(settingsMap.getSetting(ANCIENT_CITY));
		builder.bastionRemnant(settingsMap.getSetting(BASTION_REMNANT));
		builder.buriedTreasure(settingsMap.getSetting(BURIED_TREASURE));
		builder.desertPyramid(settingsMap.getSetting(DESERT_PYRAMID));
		builder.endCity(settingsMap.getSetting(END_CITY));
		builder.igloo(settingsMap.getSetting(IGLOO));
		builder.jungleTemple(settingsMap.getSetting(JUNGLE_TEMPLE));
		builder.mineshaft(settingsMap.getSetting(MINESHAFT));
		builder.mineshaftMesa(settingsMap.getSetting(MINESHAFT_MESA));
		builder.netherFortress(settingsMap.getSetting(NETHER_FORTRESS));
		builder.netherFossil(settingsMap.getSetting(NETHER_FOSSIL));
		builder.oceanMonument(settingsMap.getSetting(OCEAN_MONUMENT));
		builder.oceanRuinCold(settingsMap.getSetting(OCEAN_RUIN_COLD));
		builder.oceanRuinWarm(settingsMap.getSetting(OCEAN_RUIN_WARM));
		builder.pillagerOutpost(settingsMap.getSetting(PILLAGER_OUTPOST));
		builder.ruinedPortalDesert(settingsMap.getSetting(RUINED_PORTAL_DESERT));
		builder.ruinedPortalJungle(settingsMap.getSetting(RUINED_PORTAL_JUNGLE));
		builder.ruinedPortalMountain(settingsMap.getSetting(RUINED_PORTAL_MOUNTAIN));
		builder.ruinedPortalNether(settingsMap.getSetting(RUINED_PORTAL_NETHER));
		builder.ruinedPortalOcean(settingsMap.getSetting(RUINED_PORTAL_OCEAN));
		builder.ruinedPortalStandard(settingsMap.getSetting(RUINED_PORTAL_STANDARD));
		builder.ruinedPortalSwamp(settingsMap.getSetting(RUINED_PORTAL_SWAMP));
		builder.shipwreck(settingsMap.getSetting(SHIPWRECK));
		builder.shipwreckBeached(settingsMap.getSetting(SHIPWRECK_BEACHED));
		builder.stronghold(settingsMap.getSetting(STRONGHOLD));
		builder.swampHut(settingsMap.getSetting(SWAMP_HUT));
		builder.trailRuins(settingsMap.getSetting(TRAIL_RUINS));
		builder.trialChambers(settingsMap.getSetting(TRIAL_CHAMBERS));
		builder.villageDesert(settingsMap.getSetting(VILLAGE_DESERT));
		builder.villagePlains(settingsMap.getSetting(VILLAGE_PLAINS));
		builder.villageSavanna(settingsMap.getSetting(VILLAGE_SAVANNA));
		builder.villageSnowy(settingsMap.getSetting(VILLAGE_SNOWY));
		builder.villageTaiga(settingsMap.getSetting(VILLAGE_TAIGA));
		builder.woodlandMansion(settingsMap.getSetting(WOODLAND_MANSION));
		builder.woodlandHouse(settingsMap.getSetting(WOODLAND_HOUSE));
		return builder;
	}


	public static List <Setting<?>> list = List.of(DISPLAY_ALL_STRUCTURE_TAGS, ANCIENT_CITY, BASTION_REMNANT, BURIED_TREASURE, DESERT_PYRAMID, END_CITY, IGLOO, JUNGLE_TEMPLE, MINESHAFT, MINESHAFT_MESA, NETHER_FORTRESS, NETHER_FOSSIL, OCEAN_MONUMENT, OCEAN_RUIN_COLD, OCEAN_RUIN_WARM, PILLAGER_OUTPOST, RUINED_PORTAL_DESERT, RUINED_PORTAL_JUNGLE, RUINED_PORTAL_MOUNTAIN, RUINED_PORTAL_NETHER, RUINED_PORTAL_OCEAN, RUINED_PORTAL_STANDARD, RUINED_PORTAL_SWAMP, SHIPWRECK, SHIPWRECK_BEACHED, STRONGHOLD, SWAMP_HUT, TRAIL_RUINS, TRIAL_CHAMBERS, VILLAGE_DESERT, VILLAGE_PLAINS, VILLAGE_SAVANNA, VILLAGE_SNOWY, VILLAGE_TAIGA, WOODLAND_MANSION, WOODLAND_HOUSE);
}

