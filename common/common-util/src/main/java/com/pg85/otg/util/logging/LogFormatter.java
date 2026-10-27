package com.pg85.otg.util.logging;

import java.util.Arrays;
import java.util.IllegalFormatException;

/**
 * Formats log messages with parameter substitution.
 * Supports both {} (SLF4J-style, preferred) and %s (legacy String.format).
 * If message contains {}, uses {} replacement. Otherwise falls back to String.format.
 */
public final class LogFormatter {
	private LogFormatter() {}

	public static String format(String message, Object... args) {
		if (args == null || args.length == 0) {
			return message;
		}
		if (message.contains("{}")) {
			return replaceBraces(message, args);
		}
		try {
			return String.format(message, args);
		} catch (IllegalFormatException e) {
			return message + " " + Arrays.toString(args);
		}
	}

	private static String replaceBraces(String message, Object[] args) {
		StringBuilder sb = new StringBuilder(message.length() + 64);
		int argIdx = 0;
		int start = 0;
		int idx;
		while ((idx = message.indexOf("{}", start)) != -1 && argIdx < args.length) {
			sb.append(message, start, idx);
			sb.append(args[argIdx++]);
			start = idx + 2;
		}
		sb.append(message, start, message.length());
		return sb.toString();
	}
}
