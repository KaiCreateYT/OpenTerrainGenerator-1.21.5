package com.pg85.otg.constants.settings;

import com.pg85.otg.util.logging.LogLevel;

public enum LogLevels {
    Off(LogLevel.ERROR),
    Quiet(LogLevel.WARN),
    Standard(LogLevel.INFO);

    private final LogLevel marker;

    LogLevels(LogLevel marker) {
        this.marker = marker;
    }

    public LogLevel getLevel() {
        return this.marker;
    }
}
