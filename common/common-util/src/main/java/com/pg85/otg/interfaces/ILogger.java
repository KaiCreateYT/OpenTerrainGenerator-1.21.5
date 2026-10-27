package com.pg85.otg.interfaces;

import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogFormatter;
import com.pg85.otg.util.logging.LogLevel;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.EnumSet;

public interface ILogger {

	void init(LogLevel level, EnumSet<LogCategory> enabledCategories, String logPresets);

	void log(LogLevel level, LogCategory category, String message);

	boolean isEnabled(LogLevel level, LogCategory category);

	boolean canLogForPreset(String presetFolderName);

	// --- Convenience: with category ---

	default void info(LogCategory category, String message, Object... args) {
		if (isEnabled(LogLevel.INFO, category)) {
			log(LogLevel.INFO, category, LogFormatter.format(message, args));
		}
	}

	default void warn(LogCategory category, String message, Object... args) {
		if (isEnabled(LogLevel.WARN, category)) {
			log(LogLevel.WARN, category, LogFormatter.format(message, args));
		}
	}

	default void error(LogCategory category, String message, Object... args) {
		if (isEnabled(LogLevel.ERROR, category)) {
			log(LogLevel.ERROR, category, LogFormatter.format(message, args));
		}
	}

	default void fatal(LogCategory category, String message, Object... args) {
		if (isEnabled(LogLevel.FATAL, category)) {
			log(LogLevel.FATAL, category, LogFormatter.format(message, args));
		}
	}

	// --- Convenience: implicit MAIN category ---

	default void info(String message, Object... args) {
		info(LogCategory.MAIN, message, args);
	}

	default void warn(String message, Object... args) {
		warn(LogCategory.MAIN, message, args);
	}

	default void error(String message, Object... args) {
		error(LogCategory.MAIN, message, args);
	}

	default void fatal(String message, Object... args) {
		fatal(LogCategory.MAIN, message, args);
	}

	// --- Exception logging ---

	default void error(LogCategory category, String message, Exception e) {
		if (isEnabled(LogLevel.ERROR, category)) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			log(LogLevel.ERROR, category, message + "\n" + sw);
		}
	}

	default void error(String message, Exception e) {
		error(LogCategory.MAIN, message, e);
	}

}
