package com.pg85.otg.util;

import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;

import java.util.EnumSet;

public final class OTGLog {
	private static ILogger logger = new FallbackLogger();

	public static void setLogger(ILogger logger) {
		OTGLog.logger = logger;
	}

	public static ILogger getLogger() {
		return OTGLog.logger;
	}

	// --- Core ---

	public static void log(LogLevel level, LogCategory category, String message) {
		logger.log(level, category, message);
	}

	public static boolean isEnabled(LogLevel level, LogCategory category) {
		return logger.isEnabled(level, category);
	}

	public static boolean canLogForPreset(String presetFolderName) {
		return logger.canLogForPreset(presetFolderName);
	}

	// --- Convenience: with category ---

	public static void info(LogCategory category, String message, Object... args) {
		logger.info(category, message, args);
	}

	public static void warn(LogCategory category, String message, Object... args) {
		logger.warn(category, message, args);
	}

	public static void error(LogCategory category, String message, Object... args) {
		logger.error(category, message, args);
	}

	public static void fatal(LogCategory category, String message, Object... args) {
		logger.fatal(category, message, args);
	}

	// --- Convenience: implicit MAIN ---

	public static void info(String message, Object... args) {
		logger.info(message, args);
	}

	public static void warn(String message, Object... args) {
		logger.warn(message, args);
	}

	public static void error(String message, Object... args) {
		logger.error(message, args);
	}

	public static void fatal(String message, Object... args) {
		logger.fatal(message, args);
	}

	// --- Exception logging ---

	public static void error(LogCategory category, String message, Exception e) {
		logger.error(category, message, e);
	}

	public static void error(String message, Exception e) {
		logger.error(message, e);
	}

	// --- Fallback logger (pre-engine startup) ---

	private static class FallbackLogger implements ILogger {
		private LogLevel minLevel = LogLevel.INFO;

		@Override
		public void init(LogLevel level, EnumSet<LogCategory> enabledCategories, String logPresets) {
			this.minLevel = level;
		}

		@Override
		public boolean isEnabled(LogLevel level, LogCategory category) {
			return level.ordinal() >= minLevel.ordinal();
		}

		@Override
		public boolean canLogForPreset(String presetFolderName) {
			return true;
		}

		@Override
		public void log(LogLevel level, LogCategory category, String message) {
			if (!isEnabled(level, category)) return;
			if (level.ordinal() >= LogLevel.WARN.ordinal()) {
				System.err.println("[OTG] " + level.name() + " " + category.getLogTag() + " " + message);
			} else {
				System.out.println("[OTG] " + level.name() + " " + category.getLogTag() + " " + message);
			}
		}
	}
}
