package com.pg85.otg.config.settings.preset;

import com.pg85.otg.config.io.SettingsMap;
import com.pg85.otg.config.settingtype.Setting;
import com.pg85.otg.config.settingtype.Settings;
import com.pg85.otg.config.settings.ConfigSection;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class SpawnSettings extends ConfigSection {
    private final boolean spawnPointSet;
    private final int spawnPointX;
    private final int spawnPointY;
    private final int spawnPointZ;
    private final float spawnPointAngle;

    public static final Setting<Boolean> FIXED_SPAWN_POINT = Settings.booleanSetting(
            "FixedSpawnPoint", false,
            t -> ((SpawnSettings) t).isSpawnPointSet(),
            "Set this to true to enable SpawnPointX/SpawnPointY/SpawnPointZ/SpawnPointAngle."
    );
    public static final Setting<Integer> SPAWN_POINT_X = Settings.intSetting(
            "SpawnPointX", 0, Integer.MIN_VALUE, Integer.MAX_VALUE,
            t -> ((SpawnSettings) t).getSpawnPointX(),
            "When FixedSpawnPoint: true, this sets the world's spawn point."
    );
    public static final Setting<Integer> SPAWN_POINT_Y = Settings.intSetting(
            "SpawnPointY", 0, Integer.MIN_VALUE, Integer.MAX_VALUE,
            t -> ((SpawnSettings) t).getSpawnPointY(),
            "When FixedSpawnPoint: true, this sets the world's spawn point."
    );
    public static final Setting<Integer> SPAWN_POINT_Z = Settings.intSetting(
            "SpawnPointZ", 0, Integer.MIN_VALUE, Integer.MAX_VALUE,
            t -> ((SpawnSettings) t).getSpawnPointZ(),
            "When FixedSpawnPoint: true, this sets the world's spawn point."
    );
    public static final Setting<Float> SPAWN_POINT_ANGLE = Settings.floatSetting(
            "SpawnPointAngle", 0.0f, Integer.MIN_VALUE, Integer.MAX_VALUE,
            t -> ((SpawnSettings) t).getSpawnPointAngle(),
            "When FixedSpawnPoint: true, this sets the angle the player is looking when spawned at the spawn point."
    );


    public static SpawnSettings getSpawnSettings(SettingsMap reader) {
        var spawnSettingsBuilder = builder();

        spawnSettingsBuilder.spawnPointSet(reader.getSetting(FIXED_SPAWN_POINT));
        spawnSettingsBuilder.spawnPointX(reader.getSetting(SPAWN_POINT_X));
        spawnSettingsBuilder.spawnPointY(reader.getSetting(SPAWN_POINT_Y));
        spawnSettingsBuilder.spawnPointZ(reader.getSetting(SPAWN_POINT_Z));
        spawnSettingsBuilder.spawnPointAngle(reader.getSetting(SPAWN_POINT_ANGLE));
        return spawnSettingsBuilder.build();
    }

    @Override
    public String getSectionName() {
        return "Spawn Settings";
    }
}