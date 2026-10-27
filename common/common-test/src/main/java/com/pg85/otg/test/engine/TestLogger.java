package com.pg85.otg.test.engine;

import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;

import java.util.EnumSet;

public class TestLogger implements ILogger {
	private LogLevel minLevel = LogLevel.INFO;
	private EnumSet<LogCategory> enabledCategories = EnumSet.of(LogCategory.MAIN, LogCategory.CONFIGS);

	@Override
	public void init(LogLevel level, EnumSet<LogCategory> enabledCategories, String logPresets) {
		this.minLevel = level;
		this.enabledCategories = EnumSet.of(LogCategory.MAIN);
		this.enabledCategories.addAll(enabledCategories);
	}

	@Override
	public boolean isEnabled(LogLevel level, LogCategory category) {
		return level.ordinal() >= minLevel.ordinal() && enabledCategories.contains(category);
	}

	@Override
	public boolean canLogForPreset(String presetFolderName) {
		return true;
	}

	@Override
	public void log(LogLevel level, LogCategory category, String message) {
		if (!isEnabled(level, category)) return;
		System.err.println(level.name() + " " + category.name() + " " + message);
	}
}
