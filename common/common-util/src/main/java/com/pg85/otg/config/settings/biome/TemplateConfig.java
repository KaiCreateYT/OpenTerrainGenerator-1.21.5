package com.pg85.otg.config.settings.biome;

import com.pg85.otg.config.annotation.Config;
import com.pg85.otg.config.annotation.Ignore;
import com.pg85.otg.config.io.SettingsMap;
import com.pg85.otg.config.settings.ConfigSection;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
@Config
public class TemplateConfig extends ConfigSection {

    String fullTemplateName;
    List<String> mobTemplateName;
    List<String> placementTemplateName;
    List<String> structureTemplateName;
    List<String> terrainTemplateName;
    List<String> visualTemplateName;
    List<String> surfaceTemplateName;
    List<String> resourceTemplateName;
    List<String> tagTemplateName;

    @Ignore
    BiomeSettings fullTemplate;
    @Ignore
    MobSettings mobTemplate;
    @Ignore
    BiomePlacementConfig placementTemplate;
    @Ignore
    BiomeStructureSettings structureTemplate;
    @Ignore
    BiomeTerrainSettings terrainTemplate;
    @Ignore
    BiomeVisualSettings visualTemplate;
    @Ignore
    SurfaceSettings surfaceTemplate;
    @Ignore
    BiomeResourceSettings resourceTemplate;
    @Ignore
    BiomeTagSettings tagTemplate;
    @Ignore
    BiomeStructureTagConfig structureTagTemplate;


    public static TemplateConfig buildTemplateSettings(SettingsMap settingsMap) {
        var builder = builder();

        return builder.build();
    }

    @Override
    public String getSectionName() {
        return "Biome Template Settings";
    }

    @Override
    public String[] getSectionComment() {
        return new String[]{"""
                To fetch base settings from a template, assign its name to a section template here.
                For example, you can design a flower forest like so:
                  FullTemplateName: NormalForest
                  ResourceTemplateName: NormalFlowers
                This will first pull all the settings from the NormalForest template, 
                    then add the resources from NormalFlowers.
                Alternatively, you can design a neon version of a plains biome like so:
                  FullTemplateName: Plains
                  MobTemplateName: Neon
                  PlacementTemplateName: Neon
                  VisualTemplateName: Neon
                This will first pull all settings from Plains, then overwrite with
                    mobs, visuals and biome placement and rarity from a Neon template.
                """};
    }
}
