package com.pg85.otg.config.biome;

import com.pg85.otg.util.biome.WeightedMobSpawnGroup;
import com.pg85.otg.util.materials.LocalMaterialData;

import java.util.ArrayList;
import java.util.List;

public class SettingsContainer {
    protected int configWaterLevelMax;
    protected int configWaterLevelMin;

    protected LocalMaterialData configWaterBlock;
    protected LocalMaterialData configIceBlock;
    protected LocalMaterialData configCooledLavaBlock;

    protected double volatilityRaw1;
    protected double volatilityRaw2;
    protected double volatilityWeightRaw1;
    protected double volatilityWeightRaw2;

    protected List<WeightedMobSpawnGroup> spawnMonsters = new ArrayList<WeightedMobSpawnGroup>();
    protected List<WeightedMobSpawnGroup> spawnCreatures = new ArrayList<WeightedMobSpawnGroup>();
    protected List<WeightedMobSpawnGroup> spawnWaterCreatures = new ArrayList<WeightedMobSpawnGroup>();
    protected List<WeightedMobSpawnGroup> spawnAmbientCreatures = new ArrayList<WeightedMobSpawnGroup>();
    protected List<WeightedMobSpawnGroup> spawnWaterAmbientCreatures = new ArrayList<WeightedMobSpawnGroup>();
    protected List<WeightedMobSpawnGroup> spawnMiscCreatures = new ArrayList<WeightedMobSpawnGroup>();
}
