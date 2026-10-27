package com.pg85.otg.config.settings.biome.generated;

import com.pg85.otg.config.settings.ConfigSection;
import com.pg85.otg.config.settingtype.Settings;
import com.pg85.otg.config.settingtype.Setting;
import com.pg85.otg.config.io.SettingsMap;
import java.util.List;
import com.pg85.otg.config.settings.biome.TemplateConfig;
import java.util.List;
import java.util.List;
import java.util.List;
import java.util.List;
import java.util.List;
import java.util.List;
import java.util.List;
import java.util.List;

public class TemplateSettings extends ConfigSection {

	@Override
	public String getSectionName() {
		return "Template Settings";
	}


	public static final Setting<String> FULL_TEMPLATE_NAME = Settings.stringSetting("FullTemplateName", "", t -> ((TemplateConfig) t).getFullTemplateName(), "");
	public static final Setting<List<String>> MOB_TEMPLATE_NAME = Settings.stringListSetting("MobTemplateName", new String[] {}, t -> ((TemplateConfig) t).getMobTemplateName(), "");
	public static final Setting<List<String>> PLACEMENT_TEMPLATE_NAME = Settings.stringListSetting("PlacementTemplateName", new String[] {}, t -> ((TemplateConfig) t).getPlacementTemplateName(), "");
	public static final Setting<List<String>> STRUCTURE_TEMPLATE_NAME = Settings.stringListSetting("StructureTemplateName", new String[] {}, t -> ((TemplateConfig) t).getStructureTemplateName(), "");
	public static final Setting<List<String>> TERRAIN_TEMPLATE_NAME = Settings.stringListSetting("TerrainTemplateName", new String[] {}, t -> ((TemplateConfig) t).getTerrainTemplateName(), "");
	public static final Setting<List<String>> VISUAL_TEMPLATE_NAME = Settings.stringListSetting("VisualTemplateName", new String[] {}, t -> ((TemplateConfig) t).getVisualTemplateName(), "");
	public static final Setting<List<String>> SURFACE_TEMPLATE_NAME = Settings.stringListSetting("SurfaceTemplateName", new String[] {}, t -> ((TemplateConfig) t).getSurfaceTemplateName(), "");
	public static final Setting<List<String>> RESOURCE_TEMPLATE_NAME = Settings.stringListSetting("ResourceTemplateName", new String[] {}, t -> ((TemplateConfig) t).getResourceTemplateName(), "");
	public static final Setting<List<String>> TAG_TEMPLATE_NAME = Settings.stringListSetting("TagTemplateName", new String[] {}, t -> ((TemplateConfig) t).getTagTemplateName(), "");

	public static TemplateConfig.TemplateConfigBuilder getBuilder(SettingsMap settingsMap) {
		var builder = TemplateConfig.builder();
		builder.fullTemplateName(settingsMap.getSetting(FULL_TEMPLATE_NAME));
		builder.mobTemplateName(settingsMap.getSetting(MOB_TEMPLATE_NAME));
		builder.placementTemplateName(settingsMap.getSetting(PLACEMENT_TEMPLATE_NAME));
		builder.structureTemplateName(settingsMap.getSetting(STRUCTURE_TEMPLATE_NAME));
		builder.terrainTemplateName(settingsMap.getSetting(TERRAIN_TEMPLATE_NAME));
		builder.visualTemplateName(settingsMap.getSetting(VISUAL_TEMPLATE_NAME));
		builder.surfaceTemplateName(settingsMap.getSetting(SURFACE_TEMPLATE_NAME));
		builder.resourceTemplateName(settingsMap.getSetting(RESOURCE_TEMPLATE_NAME));
		builder.tagTemplateName(settingsMap.getSetting(TAG_TEMPLATE_NAME));
		return builder;
	}


	public static List <Setting<?>> list = List.of(FULL_TEMPLATE_NAME, MOB_TEMPLATE_NAME, PLACEMENT_TEMPLATE_NAME, STRUCTURE_TEMPLATE_NAME, TERRAIN_TEMPLATE_NAME, VISUAL_TEMPLATE_NAME, SURFACE_TEMPLATE_NAME, RESOURCE_TEMPLATE_NAME, TAG_TEMPLATE_NAME);
}

