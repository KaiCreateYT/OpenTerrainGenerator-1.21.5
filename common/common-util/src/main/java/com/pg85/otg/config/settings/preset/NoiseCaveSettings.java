package com.pg85.otg.config.settings.preset;

import com.pg85.otg.config.io.SettingsMap;
import com.pg85.otg.config.settingtype.Setting;
import com.pg85.otg.config.settingtype.Settings;
import com.pg85.otg.config.settings.ConfigSection;

import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
public class NoiseCaveSettings extends ConfigSection {
    private final double finalDensityOffset;
    private final double finalDensityScale;
    private final boolean cavesEnabled;
    private final double spaghetti2dScale;
    private final double spaghetti3dScale;
    private final double noodleScale;
    private final double pillarScale;
    private final boolean veinsEnabled;
    private final boolean aquifersEnabled;
    private final int veinMinY;
    private final int veinMaxY;
    private final int surfaceSuppressionRange;
    private final double surfaceBreakthroughChance;
    private final double surfaceBreakthroughScale;
    private final boolean debugCaveTypes;
    private final double undergroundBiomeCheeseDensityThreshold;

    private final int aquiferBarrierFirstOctave;
    private final List<Double> aquiferBarrierAmplitudes;
    private final int aquiferFloodednessFirstOctave;
    private final List<Double> aquiferFloodednessAmplitudes;
    private final int aquiferLavaFirstOctave;
    private final List<Double> aquiferLavaAmplitudes;
    private final int aquiferSpreadFirstOctave;
    private final List<Double> aquiferSpreadAmplitudes;

    private final int pillarFirstOctave;
    private final List<Double> pillarAmplitudes;
    private final int pillarRarenessFirstOctave;
    private final List<Double> pillarRarenessAmplitudes;
    private final int pillarThicknessFirstOctave;
    private final List<Double> pillarThicknessAmplitudes;

    private final int spaghetti2dFirstOctave;
    private final List<Double> spaghetti2dAmplitudes;
    private final int spaghetti2dElevationFirstOctave;
    private final List<Double> spaghetti2dElevationAmplitudes;
    private final int spaghetti2dModulatorFirstOctave;
    private final List<Double> spaghetti2dModulatorAmplitudes;
    private final int spaghetti2dThicknessFirstOctave;
    private final List<Double> spaghetti2dThicknessAmplitudes;

    private final int spaghetti3d1FirstOctave;
    private final List<Double> spaghetti3d1Amplitudes;
    private final int spaghetti3d2FirstOctave;
    private final List<Double> spaghetti3d2Amplitudes;
    private final int spaghetti3dRarityFirstOctave;
    private final List<Double> spaghetti3dRarityAmplitudes;
    private final int spaghetti3dThicknessFirstOctave;
    private final List<Double> spaghetti3dThicknessAmplitudes;

    private final int spaghettiRoughnessFirstOctave;
    private final List<Double> spaghettiRoughnessAmplitudes;
    private final int spaghettiRoughnessModulatorFirstOctave;
    private final List<Double> spaghettiRoughnessModulatorAmplitudes;

    private final int caveEntranceFirstOctave;
    private final List<Double> caveEntranceAmplitudes;
    private final int caveLayerFirstOctave;
    private final List<Double> caveLayerAmplitudes;
    private final int caveCheeseFirstOctave;
    private final List<Double> caveCheeseAmplitudes;

    private final int noodleFirstOctave;
    private final List<Double> noodleAmplitudes;
    private final int noodleThicknessFirstOctave;
    private final List<Double> noodleThicknessAmplitudes;
    private final int noodleRidgeAFirstOctave;
    private final List<Double> noodleRidgeAAmplitudes;
    private final int noodleRidgeBFirstOctave;
    private final List<Double> noodleRidgeBAmplitudes;

    private final int oreVeininessFirstOctave;
    private final List<Double> oreVeininessAmplitudes;
    private final int oreVeinAFirstOctave;
    private final List<Double> oreVeinAAmplitudes;
    private final int oreVeinBFirstOctave;
    private final List<Double> oreVeinBAmplitudes;
    private final int oreGapFirstOctave;
    private final List<Double> oreGapAmplitudes;

    public static final Setting<Double> FINAL_DENSITY_OFFSET = Settings.doubleSetting(
            "NoiseCaveFinalDensityOffset", 0.0, -5.0, 5.0,
            t -> ((NoiseCaveSettings) t).getFinalDensityOffset(),
            "Adds to final density; negative = więcej jaskiń (większe pustki), dodatnie = mniej jaskiń."
    );

    public static final Setting<Double> FINAL_DENSITY_SCALE = Settings.doubleSetting(
            "NoiseCaveFinalDensityScale", 1.0, 0.1, 5.0,
            t -> ((NoiseCaveSettings) t).getFinalDensityScale(),
            "Mnożnik dla final density; >1 zagęszcza skałę, <1 powiększa jaskinie."
    );

    public static final Setting<Boolean> NOISE_CAVES_ENABLED = Settings.booleanSetting(
            "NoiseCavesEnabled", true,
            t -> ((NoiseCaveSettings) t).isCavesEnabled(),
            "Włącza/wyłącza wycinanie jaskiń szumowych (cheese/spaghetti/noodle)."
    );

    public static final Setting<Double> SPAGHETTI2D_SCALE = Settings.doubleSetting(
            "NoiseCaveSpaghetti2DScale", 1.0, 0.0, 5.0,
            t -> ((NoiseCaveSettings) t).getSpaghetti2dScale(),
            "Mnożnik grubości/siły spaghetti 2D."
    );

    public static final Setting<Double> SPAGHETTI3D_SCALE = Settings.doubleSetting(
            "NoiseCaveSpaghetti3DScale", 1.0, 0.0, 5.0,
            t -> ((NoiseCaveSettings) t).getSpaghetti3dScale(),
            "Mnożnik spaghetti 3D (entrances/rarity/thickness)."
    );

    public static final Setting<Double> NOODLE_SCALE = Settings.doubleSetting(
            "NoiseCaveNoodleScale", 1.0, 0.0, 5.0,
            t -> ((NoiseCaveSettings) t).getNoodleScale(),
            "Mnożnik gęstości tuneli noodle."
    );

    public static final Setting<Double> PILLAR_SCALE = Settings.doubleSetting(
            "NoiseCavePillarScale", 1.0, 0.0, 5.0,
            t -> ((NoiseCaveSettings) t).getPillarScale(),
            "Mnożnik grubości/ilości filarów w cheese caves."
    );

    public static final Setting<Boolean> VEINS_ENABLED = Settings.booleanSetting(
            "NoiseCaveVeinsEnabled", true,
            t -> ((NoiseCaveSettings) t).isVeinsEnabled(),
            "Włącza/wyłącza ore veins (1.18+)."
    );

    public static final Setting<Boolean> AQUIFERS_ENABLED = Settings.booleanSetting(
            "NoiseCaveAquifersEnabled", true,
            t -> ((NoiseCaveSettings) t).isAquifersEnabled(),
            "Włącza/wyłącza aquifery w jaskiniach szumowych."
    );

    public static final Setting<Integer> VEIN_MIN_Y = Settings.intSetting(
            "NoiseCaveVeinMinY", -60, -128, 320,
            t -> ((NoiseCaveSettings) t).getVeinMinY(),
            "Minimalny Y dla ore veins (vanilla: -60)."
    );

    public static final Setting<Integer> VEIN_MAX_Y = Settings.intSetting(
            "NoiseCaveVeinMaxY", 50, -128, 320,
            t -> ((NoiseCaveSettings) t).getVeinMaxY(),
            "Maksymalny Y dla ore veins (vanilla: 50)."
    );

    public static final Setting<Integer> SURFACE_SUPPRESSION_RANGE = Settings.intSetting(
            "NoiseCaveSurfaceSuppressionRange", 30, 5, 100,
            t -> ((NoiseCaveSettings) t).getSurfaceSuppressionRange(),
            "Ile bloków pod surface level zaczyna się tłumienie jaskiń. Mniejsza wartość = jaskinie kończą się bliżej powierzchni."
    );

    public static final Setting<Double> SURFACE_BREAKTHROUGH_CHANCE = Settings.doubleSetting(
            "NoiseCaveSurfaceBreakthroughChance", 0.1, 0.0, 1.0,
            t -> ((NoiseCaveSettings) t).getSurfaceBreakthroughChance(),
            "Jaki procent terenu pozwala jaskiniom przebić się na powierzchnię (0.0 = brak, 1.0 = wszędzie). Kontroluje próg noise."
    );

    public static final Setting<Double> SURFACE_BREAKTHROUGH_SCALE = Settings.doubleSetting(
            "NoiseCaveSurfaceBreakthroughScale", 128.0, 16.0, 512.0,
            t -> ((NoiseCaveSettings) t).getSurfaceBreakthroughScale(),
            "Skala noise dla regionów breakthrough. Większa = większe regiony z wejściami do jaskiń."
    );

    public static final Setting<Boolean> DEBUG_CAVE_TYPES = Settings.booleanSetting(
            "NoiseCaveDebugCaveTypes", false,
            t -> ((NoiseCaveSettings) t).isDebugCaveTypes(),
            "Replaces cave air with colored glass per cave type: yellow=cheese, red=spaghetti, blue=noodle."
    );

    public static final Setting<Double> UNDERGROUND_BIOME_CHEESE_THRESHOLD = Settings.doubleSetting(
            "UndergroundBiomeCheeseDensityThreshold", 0.0, -5.0, 5.0,
            t -> ((NoiseCaveSettings) t).getUndergroundBiomeCheeseDensityThreshold(),
            "Cheese cave density threshold for underground biome placement.",
            "Underground biomes only appear where cheese density < this threshold.",
            "0.0 = at cave boundary. Higher = extends biome into surrounding walls.",
            "Set to -999 to effectively disable cheese gating."
    );

    public static final Setting<Integer> AQUIFER_BARRIER_FIRST_OCTAVE = Settings.intSetting(
            "NoiseParamAquiferBarrierFirstOctave", -3, -64, 64,
            t -> ((NoiseCaveSettings) t).getAquiferBarrierFirstOctave(),
            "Aquifer barrier noise: first octave."
    );
    public static final Setting<List<String>> AQUIFER_BARRIER_AMPLITUDES = Settings.stringListSetting(
            "NoiseParamAquiferBarrierAmplitudes", new String[]{"1.0"}
    );
    public static final Setting<Integer> AQUIFER_FLOODEDNESS_FIRST_OCTAVE = Settings.intSetting(
            "NoiseParamAquiferFloodednessFirstOctave", -7, -64, 64,
            t -> ((NoiseCaveSettings) t).getAquiferFloodednessFirstOctave(),
            "Aquifer floodedness noise: first octave."
    );
    public static final Setting<List<String>> AQUIFER_FLOODEDNESS_AMPLITUDES = Settings.stringListSetting(
            "NoiseParamAquiferFloodednessAmplitudes", new String[]{"1.0"}
    );
    public static final Setting<Integer> AQUIFER_LAVA_FIRST_OCTAVE = Settings.intSetting(
            "NoiseParamAquiferLavaFirstOctave", -1, -64, 64,
            t -> ((NoiseCaveSettings) t).getAquiferLavaFirstOctave(),
            "Aquifer lava noise: first octave."
    );
    public static final Setting<List<String>> AQUIFER_LAVA_AMPLITUDES = Settings.stringListSetting(
            "NoiseParamAquiferLavaAmplitudes", new String[]{"1.0"}
    );
    public static final Setting<Integer> AQUIFER_SPREAD_FIRST_OCTAVE = Settings.intSetting(
            "NoiseParamAquiferSpreadFirstOctave", -5, -64, 64,
            t -> ((NoiseCaveSettings) t).getAquiferSpreadFirstOctave(),
            "Aquifer fluid level spread noise: first octave."
    );
    public static final Setting<List<String>> AQUIFER_SPREAD_AMPLITUDES = Settings.stringListSetting(
            "NoiseParamAquiferSpreadAmplitudes", new String[]{"1.0"}
    );

    public static final Setting<Integer> PILLAR_FIRST_OCTAVE = Settings.intSetting(
            "NoiseParamPillarFirstOctave", -7, -64, 64,
            t -> ((NoiseCaveSettings) t).getPillarFirstOctave(),
            "Pillar noise: first octave."
    );
    public static final Setting<List<String>> PILLAR_AMPLITUDES = Settings.stringListSetting(
            "NoiseParamPillarAmplitudes", new String[]{"1.0", "1.0"}
    );
    public static final Setting<Integer> PILLAR_RARENESS_FIRST_OCTAVE = Settings.intSetting(
            "NoiseParamPillarRarenessFirstOctave", -8, -64, 64,
            t -> ((NoiseCaveSettings) t).getPillarRarenessFirstOctave(),
            "Pillar rareness noise: first octave."
    );
    public static final Setting<List<String>> PILLAR_RARENESS_AMPLITUDES = Settings.stringListSetting(
            "NoiseParamPillarRarenessAmplitudes", new String[]{"1.0"}
    );
    public static final Setting<Integer> PILLAR_THICKNESS_FIRST_OCTAVE = Settings.intSetting(
            "NoiseParamPillarThicknessFirstOctave", -8, -64, 64,
            t -> ((NoiseCaveSettings) t).getPillarThicknessFirstOctave(),
            "Pillar thickness noise: first octave."
    );
    public static final Setting<List<String>> PILLAR_THICKNESS_AMPLITUDES = Settings.stringListSetting(
            "NoiseParamPillarThicknessAmplitudes", new String[]{"1.0"}
    );

    public static final Setting<Integer> SPAGHETTI2D_FIRST_OCTAVE = Settings.intSetting(
            "NoiseParamSpaghetti2DFirstOctave", -7, -64, 64,
            t -> ((NoiseCaveSettings) t).getSpaghetti2dFirstOctave(),
            "Spaghetti 2D noise: first octave."
    );
    public static final Setting<List<String>> SPAGHETTI2D_AMPLITUDES = Settings.stringListSetting(
            "NoiseParamSpaghetti2DAmplitudes", new String[]{"1.0"}
    );
    public static final Setting<Integer> SPAGHETTI2D_ELEVATION_FIRST_OCTAVE = Settings.intSetting(
            "NoiseParamSpaghetti2DElevationFirstOctave", -8, -64, 64,
            t -> ((NoiseCaveSettings) t).getSpaghetti2dElevationFirstOctave(),
            "Spaghetti 2D elevation noise: first octave."
    );
    public static final Setting<List<String>> SPAGHETTI2D_ELEVATION_AMPLITUDES = Settings.stringListSetting(
            "NoiseParamSpaghetti2DElevationAmplitudes", new String[]{"1.0"}
    );
    public static final Setting<Integer> SPAGHETTI2D_MODULATOR_FIRST_OCTAVE = Settings.intSetting(
            "NoiseParamSpaghetti2DModulatorFirstOctave", -11, -64, 64,
            t -> ((NoiseCaveSettings) t).getSpaghetti2dModulatorFirstOctave(),
            "Spaghetti 2D modulator noise: first octave."
    );
    public static final Setting<List<String>> SPAGHETTI2D_MODULATOR_AMPLITUDES = Settings.stringListSetting(
            "NoiseParamSpaghetti2DModulatorAmplitudes", new String[]{"1.0"}
    );
    public static final Setting<Integer> SPAGHETTI2D_THICKNESS_FIRST_OCTAVE = Settings.intSetting(
            "NoiseParamSpaghetti2DThicknessFirstOctave", -11, -64, 64,
            t -> ((NoiseCaveSettings) t).getSpaghetti2dThicknessFirstOctave(),
            "Spaghetti 2D thickness noise: first octave."
    );
    public static final Setting<List<String>> SPAGHETTI2D_THICKNESS_AMPLITUDES = Settings.stringListSetting(
            "NoiseParamSpaghetti2DThicknessAmplitudes", new String[]{"1.0"}
    );

    public static final Setting<Integer> SPAGHETTI3D1_FIRST_OCTAVE = Settings.intSetting(
            "NoiseParamSpaghetti3D1FirstOctave", -7, -64, 64,
            t -> ((NoiseCaveSettings) t).getSpaghetti3d1FirstOctave(),
            "Spaghetti 3D noise #1: first octave."
    );
    public static final Setting<List<String>> SPAGHETTI3D1_AMPLITUDES = Settings.stringListSetting(
            "NoiseParamSpaghetti3D1Amplitudes", new String[]{"1.0"}
    );
    public static final Setting<Integer> SPAGHETTI3D2_FIRST_OCTAVE = Settings.intSetting(
            "NoiseParamSpaghetti3D2FirstOctave", -7, -64, 64,
            t -> ((NoiseCaveSettings) t).getSpaghetti3d2FirstOctave(),
            "Spaghetti 3D noise #2: first octave."
    );
    public static final Setting<List<String>> SPAGHETTI3D2_AMPLITUDES = Settings.stringListSetting(
            "NoiseParamSpaghetti3D2Amplitudes", new String[]{"1.0"}
    );
    public static final Setting<Integer> SPAGHETTI3D_RARITY_FIRST_OCTAVE = Settings.intSetting(
            "NoiseParamSpaghetti3DRarityFirstOctave", -11, -64, 64,
            t -> ((NoiseCaveSettings) t).getSpaghetti3dRarityFirstOctave(),
            "Spaghetti 3D rarity noise: first octave."
    );
    public static final Setting<List<String>> SPAGHETTI3D_RARITY_AMPLITUDES = Settings.stringListSetting(
            "NoiseParamSpaghetti3DRarityAmplitudes", new String[]{"1.0"}
    );
    public static final Setting<Integer> SPAGHETTI3D_THICKNESS_FIRST_OCTAVE = Settings.intSetting(
            "NoiseParamSpaghetti3DThicknessFirstOctave", -8, -64, 64,
            t -> ((NoiseCaveSettings) t).getSpaghetti3dThicknessFirstOctave(),
            "Spaghetti 3D thickness noise: first octave."
    );
    public static final Setting<List<String>> SPAGHETTI3D_THICKNESS_AMPLITUDES = Settings.stringListSetting(
            "NoiseParamSpaghetti3DThicknessAmplitudes", new String[]{"1.0"}
    );

    public static final Setting<Integer> SPAGHETTI_ROUGHNESS_FIRST_OCTAVE = Settings.intSetting(
            "NoiseParamSpaghettiRoughnessFirstOctave", -5, -64, 64,
            t -> ((NoiseCaveSettings) t).getSpaghettiRoughnessFirstOctave(),
            "Spaghetti roughness noise: first octave."
    );
    public static final Setting<List<String>> SPAGHETTI_ROUGHNESS_AMPLITUDES = Settings.stringListSetting(
            "NoiseParamSpaghettiRoughnessAmplitudes", new String[]{"1.0"}
    );
    public static final Setting<Integer> SPAGHETTI_ROUGHNESS_MODULATOR_FIRST_OCTAVE = Settings.intSetting(
            "NoiseParamSpaghettiRoughnessModulatorFirstOctave", -8, -64, 64,
            t -> ((NoiseCaveSettings) t).getSpaghettiRoughnessModulatorFirstOctave(),
            "Spaghetti roughness modulator noise: first octave."
    );
    public static final Setting<List<String>> SPAGHETTI_ROUGHNESS_MODULATOR_AMPLITUDES = Settings.stringListSetting(
            "NoiseParamSpaghettiRoughnessModulatorAmplitudes", new String[]{"1.0"}
    );

    public static final Setting<Integer> CAVE_ENTRANCE_FIRST_OCTAVE = Settings.intSetting(
            "NoiseParamCaveEntranceFirstOctave", -7, -64, 64,
            t -> ((NoiseCaveSettings) t).getCaveEntranceFirstOctave(),
            "Cave entrance noise: first octave."
    );
    public static final Setting<List<String>> CAVE_ENTRANCE_AMPLITUDES = Settings.stringListSetting(
            "NoiseParamCaveEntranceAmplitudes", new String[]{"0.4", "0.5", "1.0"}
    );
    public static final Setting<Integer> CAVE_LAYER_FIRST_OCTAVE = Settings.intSetting(
            "NoiseParamCaveLayerFirstOctave", -8, -64, 64,
            t -> ((NoiseCaveSettings) t).getCaveLayerFirstOctave(),
            "Cave layer noise: first octave."
    );
    public static final Setting<List<String>> CAVE_LAYER_AMPLITUDES = Settings.stringListSetting(
            "NoiseParamCaveLayerAmplitudes", new String[]{"1.0"}
    );
    public static final Setting<Integer> CAVE_CHEESE_FIRST_OCTAVE = Settings.intSetting(
            "NoiseParamCaveCheeseFirstOctave", -8, -64, 64,
            t -> ((NoiseCaveSettings) t).getCaveCheeseFirstOctave(),
            "Cave cheese noise: first octave."
    );
    public static final Setting<List<String>> CAVE_CHEESE_AMPLITUDES = Settings.stringListSetting(
            "NoiseParamCaveCheeseAmplitudes", new String[]{"0.5", "1.0", "2.0", "1.0", "2.0", "1.0", "0.0", "2.0", "0.0"}
    );

    public static final Setting<Integer> NOODLE_FIRST_OCTAVE = Settings.intSetting(
            "NoiseParamNoodleFirstOctave", -8, -64, 64,
            t -> ((NoiseCaveSettings) t).getNoodleFirstOctave(),
            "Noodle noise: first octave."
    );
    public static final Setting<List<String>> NOODLE_AMPLITUDES = Settings.stringListSetting(
            "NoiseParamNoodleAmplitudes", new String[]{"1.0"}
    );
    public static final Setting<Integer> NOODLE_THICKNESS_FIRST_OCTAVE = Settings.intSetting(
            "NoiseParamNoodleThicknessFirstOctave", -8, -64, 64,
            t -> ((NoiseCaveSettings) t).getNoodleThicknessFirstOctave(),
            "Noodle thickness noise: first octave."
    );
    public static final Setting<List<String>> NOODLE_THICKNESS_AMPLITUDES = Settings.stringListSetting(
            "NoiseParamNoodleThicknessAmplitudes", new String[]{"1.0"}
    );
    public static final Setting<Integer> NOODLE_RIDGE_A_FIRST_OCTAVE = Settings.intSetting(
            "NoiseParamNoodleRidgeAFirstOctave", -7, -64, 64,
            t -> ((NoiseCaveSettings) t).getNoodleRidgeAFirstOctave(),
            "Noodle ridge A noise: first octave."
    );
    public static final Setting<List<String>> NOODLE_RIDGE_A_AMPLITUDES = Settings.stringListSetting(
            "NoiseParamNoodleRidgeAAmplitudes", new String[]{"1.0"}
    );
    public static final Setting<Integer> NOODLE_RIDGE_B_FIRST_OCTAVE = Settings.intSetting(
            "NoiseParamNoodleRidgeBFirstOctave", -7, -64, 64,
            t -> ((NoiseCaveSettings) t).getNoodleRidgeBFirstOctave(),
            "Noodle ridge B noise: first octave."
    );
    public static final Setting<List<String>> NOODLE_RIDGE_B_AMPLITUDES = Settings.stringListSetting(
            "NoiseParamNoodleRidgeBAmplitudes", new String[]{"1.0"}
    );

    public static final Setting<Integer> ORE_VEININESS_FIRST_OCTAVE = Settings.intSetting(
            "NoiseParamOreVeininessFirstOctave", -8, -64, 64,
            t -> ((NoiseCaveSettings) t).getOreVeininessFirstOctave(),
            "Ore veininess noise: first octave."
    );
    public static final Setting<List<String>> ORE_VEININESS_AMPLITUDES = Settings.stringListSetting(
            "NoiseParamOreVeininessAmplitudes", new String[]{"1.0"}
    );
    public static final Setting<Integer> ORE_VEIN_A_FIRST_OCTAVE = Settings.intSetting(
            "NoiseParamOreVeinAFirstOctave", -7, -64, 64,
            t -> ((NoiseCaveSettings) t).getOreVeinAFirstOctave(),
            "Ore vein A noise: first octave."
    );
    public static final Setting<List<String>> ORE_VEIN_A_AMPLITUDES = Settings.stringListSetting(
            "NoiseParamOreVeinAAmplitudes", new String[]{"1.0"}
    );
    public static final Setting<Integer> ORE_VEIN_B_FIRST_OCTAVE = Settings.intSetting(
            "NoiseParamOreVeinBFirstOctave", -7, -64, 64,
            t -> ((NoiseCaveSettings) t).getOreVeinBFirstOctave(),
            "Ore vein B noise: first octave."
    );
    public static final Setting<List<String>> ORE_VEIN_B_AMPLITUDES = Settings.stringListSetting(
            "NoiseParamOreVeinBAmplitudes", new String[]{"1.0"}
    );
    public static final Setting<Integer> ORE_GAP_FIRST_OCTAVE = Settings.intSetting(
            "NoiseParamOreGapFirstOctave", -5, -64, 64,
            t -> ((NoiseCaveSettings) t).getOreGapFirstOctave(),
            "Ore gap noise: first octave."
    );
    public static final Setting<List<String>> ORE_GAP_AMPLITUDES = Settings.stringListSetting(
            "NoiseParamOreGapAmplitudes", new String[]{"1.0"}
    );

    public static NoiseCaveSettings getNoiseCaveSettings(SettingsMap reader) {
        var builder = builder();
        builder.finalDensityOffset(reader.getSetting(FINAL_DENSITY_OFFSET));
        builder.finalDensityScale(reader.getSetting(FINAL_DENSITY_SCALE));
        builder.cavesEnabled(reader.getSetting(NOISE_CAVES_ENABLED));
        builder.spaghetti2dScale(reader.getSetting(SPAGHETTI2D_SCALE));
        builder.spaghetti3dScale(reader.getSetting(SPAGHETTI3D_SCALE));
        builder.noodleScale(reader.getSetting(NOODLE_SCALE));
        builder.pillarScale(reader.getSetting(PILLAR_SCALE));
        builder.veinsEnabled(reader.getSetting(VEINS_ENABLED));
        builder.aquifersEnabled(reader.getSetting(AQUIFERS_ENABLED));
        int minY = reader.getSetting(VEIN_MIN_Y);
        int maxY = reader.getSetting(VEIN_MAX_Y);
        if (maxY < minY) {
            maxY = minY;
        }
        builder.veinMinY(minY);
        builder.veinMaxY(maxY);
        builder.surfaceSuppressionRange(reader.getSetting(SURFACE_SUPPRESSION_RANGE));
        builder.surfaceBreakthroughChance(reader.getSetting(SURFACE_BREAKTHROUGH_CHANCE));
        builder.surfaceBreakthroughScale(reader.getSetting(SURFACE_BREAKTHROUGH_SCALE));
        builder.debugCaveTypes(reader.getSetting(DEBUG_CAVE_TYPES));
        builder.undergroundBiomeCheeseDensityThreshold(reader.getSetting(UNDERGROUND_BIOME_CHEESE_THRESHOLD));

        builder.aquiferBarrierFirstOctave(reader.getSetting(AQUIFER_BARRIER_FIRST_OCTAVE));
        builder.aquiferBarrierAmplitudes(parseAmplitudes(reader, AQUIFER_BARRIER_AMPLITUDES));
        builder.aquiferFloodednessFirstOctave(reader.getSetting(AQUIFER_FLOODEDNESS_FIRST_OCTAVE));
        builder.aquiferFloodednessAmplitudes(parseAmplitudes(reader, AQUIFER_FLOODEDNESS_AMPLITUDES));
        builder.aquiferLavaFirstOctave(reader.getSetting(AQUIFER_LAVA_FIRST_OCTAVE));
        builder.aquiferLavaAmplitudes(parseAmplitudes(reader, AQUIFER_LAVA_AMPLITUDES));
        builder.aquiferSpreadFirstOctave(reader.getSetting(AQUIFER_SPREAD_FIRST_OCTAVE));
        builder.aquiferSpreadAmplitudes(parseAmplitudes(reader, AQUIFER_SPREAD_AMPLITUDES));

        builder.pillarFirstOctave(reader.getSetting(PILLAR_FIRST_OCTAVE));
        builder.pillarAmplitudes(parseAmplitudes(reader, PILLAR_AMPLITUDES));
        builder.pillarRarenessFirstOctave(reader.getSetting(PILLAR_RARENESS_FIRST_OCTAVE));
        builder.pillarRarenessAmplitudes(parseAmplitudes(reader, PILLAR_RARENESS_AMPLITUDES));
        builder.pillarThicknessFirstOctave(reader.getSetting(PILLAR_THICKNESS_FIRST_OCTAVE));
        builder.pillarThicknessAmplitudes(parseAmplitudes(reader, PILLAR_THICKNESS_AMPLITUDES));

        builder.spaghetti2dFirstOctave(reader.getSetting(SPAGHETTI2D_FIRST_OCTAVE));
        builder.spaghetti2dAmplitudes(parseAmplitudes(reader, SPAGHETTI2D_AMPLITUDES));
        builder.spaghetti2dElevationFirstOctave(reader.getSetting(SPAGHETTI2D_ELEVATION_FIRST_OCTAVE));
        builder.spaghetti2dElevationAmplitudes(parseAmplitudes(reader, SPAGHETTI2D_ELEVATION_AMPLITUDES));
        builder.spaghetti2dModulatorFirstOctave(reader.getSetting(SPAGHETTI2D_MODULATOR_FIRST_OCTAVE));
        builder.spaghetti2dModulatorAmplitudes(parseAmplitudes(reader, SPAGHETTI2D_MODULATOR_AMPLITUDES));
        builder.spaghetti2dThicknessFirstOctave(reader.getSetting(SPAGHETTI2D_THICKNESS_FIRST_OCTAVE));
        builder.spaghetti2dThicknessAmplitudes(parseAmplitudes(reader, SPAGHETTI2D_THICKNESS_AMPLITUDES));

        builder.spaghetti3d1FirstOctave(reader.getSetting(SPAGHETTI3D1_FIRST_OCTAVE));
        builder.spaghetti3d1Amplitudes(parseAmplitudes(reader, SPAGHETTI3D1_AMPLITUDES));
        builder.spaghetti3d2FirstOctave(reader.getSetting(SPAGHETTI3D2_FIRST_OCTAVE));
        builder.spaghetti3d2Amplitudes(parseAmplitudes(reader, SPAGHETTI3D2_AMPLITUDES));
        builder.spaghetti3dRarityFirstOctave(reader.getSetting(SPAGHETTI3D_RARITY_FIRST_OCTAVE));
        builder.spaghetti3dRarityAmplitudes(parseAmplitudes(reader, SPAGHETTI3D_RARITY_AMPLITUDES));
        builder.spaghetti3dThicknessFirstOctave(reader.getSetting(SPAGHETTI3D_THICKNESS_FIRST_OCTAVE));
        builder.spaghetti3dThicknessAmplitudes(parseAmplitudes(reader, SPAGHETTI3D_THICKNESS_AMPLITUDES));

        builder.spaghettiRoughnessFirstOctave(reader.getSetting(SPAGHETTI_ROUGHNESS_FIRST_OCTAVE));
        builder.spaghettiRoughnessAmplitudes(parseAmplitudes(reader, SPAGHETTI_ROUGHNESS_AMPLITUDES));
        builder.spaghettiRoughnessModulatorFirstOctave(reader.getSetting(SPAGHETTI_ROUGHNESS_MODULATOR_FIRST_OCTAVE));
        builder.spaghettiRoughnessModulatorAmplitudes(parseAmplitudes(reader, SPAGHETTI_ROUGHNESS_MODULATOR_AMPLITUDES));

        builder.caveEntranceFirstOctave(reader.getSetting(CAVE_ENTRANCE_FIRST_OCTAVE));
        builder.caveEntranceAmplitudes(parseAmplitudes(reader, CAVE_ENTRANCE_AMPLITUDES));
        builder.caveLayerFirstOctave(reader.getSetting(CAVE_LAYER_FIRST_OCTAVE));
        builder.caveLayerAmplitudes(parseAmplitudes(reader, CAVE_LAYER_AMPLITUDES));
        builder.caveCheeseFirstOctave(reader.getSetting(CAVE_CHEESE_FIRST_OCTAVE));
        builder.caveCheeseAmplitudes(parseAmplitudes(reader, CAVE_CHEESE_AMPLITUDES));

        builder.noodleFirstOctave(reader.getSetting(NOODLE_FIRST_OCTAVE));
        builder.noodleAmplitudes(parseAmplitudes(reader, NOODLE_AMPLITUDES));
        builder.noodleThicknessFirstOctave(reader.getSetting(NOODLE_THICKNESS_FIRST_OCTAVE));
        builder.noodleThicknessAmplitudes(parseAmplitudes(reader, NOODLE_THICKNESS_AMPLITUDES));
        builder.noodleRidgeAFirstOctave(reader.getSetting(NOODLE_RIDGE_A_FIRST_OCTAVE));
        builder.noodleRidgeAAmplitudes(parseAmplitudes(reader, NOODLE_RIDGE_A_AMPLITUDES));
        builder.noodleRidgeBFirstOctave(reader.getSetting(NOODLE_RIDGE_B_FIRST_OCTAVE));
        builder.noodleRidgeBAmplitudes(parseAmplitudes(reader, NOODLE_RIDGE_B_AMPLITUDES));

        builder.oreVeininessFirstOctave(reader.getSetting(ORE_VEININESS_FIRST_OCTAVE));
        builder.oreVeininessAmplitudes(parseAmplitudes(reader, ORE_VEININESS_AMPLITUDES));
        builder.oreVeinAFirstOctave(reader.getSetting(ORE_VEIN_A_FIRST_OCTAVE));
        builder.oreVeinAAmplitudes(parseAmplitudes(reader, ORE_VEIN_A_AMPLITUDES));
        builder.oreVeinBFirstOctave(reader.getSetting(ORE_VEIN_B_FIRST_OCTAVE));
        builder.oreVeinBAmplitudes(parseAmplitudes(reader, ORE_VEIN_B_AMPLITUDES));
        builder.oreGapFirstOctave(reader.getSetting(ORE_GAP_FIRST_OCTAVE));
        builder.oreGapAmplitudes(parseAmplitudes(reader, ORE_GAP_AMPLITUDES));

        return builder.build();
    }

    private static List<Double> parseAmplitudes(SettingsMap reader, Setting<List<String>> setting) {
        List<String> values = reader.getSetting(setting);
        List<Double> amplitudes = new ArrayList<>(values.size());
        for (String value : values) {
            if (value == null) {
                continue;
            }
            String trimmed = value.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            try {
                amplitudes.add(Double.parseDouble(trimmed));
            } catch (NumberFormatException ignored) {
                // Keep defaults if malformed input is provided.
            }
        }
        if (amplitudes.isEmpty()) {
            amplitudes.add(1.0);
        }
        return List.copyOf(amplitudes);
    }

    @Override
    public String getSectionName() {
        return "Noise Cave Settings";
    }
}
