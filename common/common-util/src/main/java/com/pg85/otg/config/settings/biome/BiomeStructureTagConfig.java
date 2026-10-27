package com.pg85.otg.config.settings.biome;

import com.pg85.otg.config.annotation.Config;
import com.pg85.otg.config.annotation.LongDescription;
import com.pg85.otg.config.io.SettingsMap;
import com.pg85.otg.config.settings.ConfigSection;
import com.pg85.otg.config.settings.biome.generated.BiomeStructureTagSettings;
import com.pg85.otg.config.settingtype.Setting;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Builder
@Getter
@Config
@LongDescription({
        "Settings for biome structure tags, used by datagen tag generation.",
        "This is used for spawning vanilla structures in a biome",
        "Will be available at runtime, but can be inaccurate if not updated correctly."
})
public class BiomeStructureTagConfig extends ConfigSection {

    @Override
    public String getSectionName() {
        return "Biome Structure Tag Settings";
    }

    @Override
    public Map<String, Setting<?>> getSettings() {
        return ConfigSection.getSettings(BiomeStructureTagSettings.class);
    }

    @LongDescription({
            "Set to true to display all biome tag settings for this biome",
            "By default, only the settings that differ from the default values are displayed."
    })
    private final boolean displayAllStructureTags;

    private final boolean ancientCity;
    private final boolean bastionRemnant;
    private final boolean buriedTreasure;
    private final boolean desertPyramid;
    private final boolean endCity;
    private final boolean igloo;
    private final boolean jungleTemple;
    private final boolean mineshaft;
    private final boolean mineshaftMesa;
    private final boolean netherFortress;
    private final boolean netherFossil;
    private final boolean oceanMonument;
    private final boolean oceanRuinCold;
    private final boolean oceanRuinWarm;
    private final boolean pillagerOutpost;
    private final boolean ruinedPortalDesert;
    private final boolean ruinedPortalJungle;
    private final boolean ruinedPortalMountain;
    private final boolean ruinedPortalNether;
    private final boolean ruinedPortalOcean;
    private final boolean ruinedPortalStandard;
    private final boolean ruinedPortalSwamp;
    private final boolean shipwreck;
    private final boolean shipwreckBeached;
    private final boolean stronghold;
    private final boolean swampHut;
    private final boolean trailRuins;
    private final boolean trialChambers;
    private final boolean villageDesert;
    private final boolean villagePlains;
    private final boolean villageSavanna;
    private final boolean villageSnowy;
    private final boolean villageTaiga;
    private final boolean woodlandMansion;
    private final boolean woodlandHouse;

    public static BiomeStructureTagConfig getBiomeStructureTagConfig(SettingsMap reader, BiomeStructureSettings legacy) {

        BiomeStructureTagConfigBuilder builder = BiomeStructureTagSettings.getBuilder(reader);

        if (legacy.isStrongholdsEnabled()) builder.stronghold(true);
        if (legacy.isOceanMonumentsEnabled()) builder.oceanMonument(true);
        if (legacy.isWoodlandMansionsEnabled()) builder.woodlandMansion(true);
        if (legacy.isNetherFortressesEnabled()) builder.netherFortress(true);

        switch (legacy.getVillageType()) {
            case wood -> builder.villagePlains(true);
            case sandstone -> builder.villageDesert(true);
            case taiga -> builder.villageTaiga(true);
            case savanna -> builder.villageSavanna(true);
            case snowy -> builder.villageSnowy(true);
        }

        switch (legacy.getMineshaftType()) {
            case normal -> builder.mineshaft(true);
            case mesa -> builder.mineshaftMesa(true);
        }

        switch (legacy.getRareBuildingType()) {
            case desertPyramid -> builder.desertPyramid(true);
            case jungleTemple -> builder.jungleTemple(true);
            case swampHut -> builder.swampHut(true);
            case igloo -> builder.igloo(true);
        }

        if (legacy.isBuriedTreasureEnabled()) builder.buriedTreasure(true);
        if (legacy.isShipWreckEnabled()) builder.shipwreck(true);
        if (legacy.isShipWreckBeachedEnabled()) builder.shipwreckBeached(true);
        if (legacy.isPillagerOutpostEnabled()) builder.pillagerOutpost(true);
        if (legacy.isBastionRemnantEnabled()) builder.bastionRemnant(true);
        if (legacy.isNetherFossilEnabled()) builder.netherFossil(true);
        if (legacy.isEndCityEnabled()) builder.endCity(true);

        switch (legacy.getRuinedPortalType()) {
            case desert -> builder.ruinedPortalDesert(true);
            case jungle -> builder.ruinedPortalJungle(true);
            case mountain -> builder.ruinedPortalMountain(true);
            case nether -> builder.ruinedPortalNether(true);
            case ocean -> builder.ruinedPortalOcean(true);
            case normal -> builder.ruinedPortalStandard(true);
            case swamp -> builder.ruinedPortalSwamp(true);
        }

        switch (legacy.getOceanRuinsType()) {
            case cold -> builder.oceanRuinCold(true);
            case warm -> builder.oceanRuinWarm(true);
        }

        return builder.build();
    }

    public void writeConfig(SettingsMap writer) {
        writer.header2("Biome Structure Tag Settings");

        if (displayAllStructureTags) {
            // If displayAll is true, write all settings regardless of their values
            for (Setting<?> setting : BiomeStructureTagSettings.list) {
                writer.putSetting(setting, this);
            }
            return; // All settings written, no need to check altered settings
        }

        List<Setting<?>> alteredSettings = getAlteredSettings();

        // DisplayAll will always be default here, so let's manually write it
        writer.putSetting(BiomeStructureTagSettings.DISPLAY_ALL_STRUCTURE_TAGS, this);

        for (Setting<?> setting : alteredSettings) {
            writer.putSetting(setting, this);
        }
    }
}
