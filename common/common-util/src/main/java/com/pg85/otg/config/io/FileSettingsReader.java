package com.pg85.otg.config.io;

import com.pg85.otg.config.io.RawSettingValue.ValueType;
import com.pg85.otg.util.OTGLog;
import com.pg85.otg.util.logging.LogCategory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * A settings reader that reads from a file.
 *
 */
public class FileSettingsReader
{
	/**
	 * Reads a file.
	 *
	 * @param configName The name of the config file. For worlds, use the world
	 *                   name, for biomes, use the biome name, etc.
	 * @param file       The file to read from.
	 * @return The settings in the file.
	 */
	public static SettingsMap read(String configName, File file)
	{
		SettingsMap settings = new SimpleSettingsMap(configName, file.toPath());
		if (!file.exists())
		{
			return settings;
		}

		try (BufferedReader reader = new BufferedReader(new FileReader(file)))
		{
			readIntoMap(settings, reader);
		} catch (IOException e) {
			OTGLog.error(LogCategory.CONFIGS, "Could not read file", e);
		}
		return settings;
	}

	/**
	 * Reads all settings in the file into the given settings map.
	 * @param settings	 The settings map.
	 * @param fileContents The contents of the file. The stream will be fully
	 *					 read, but you'll have to close the stream yourself.
	 * @throws IOException If an IO error occurs.
	 */
	private static void readIntoMap(SettingsMap settings, BufferedReader fileContents) throws IOException
	{
		int lineNumber = 0;
		String thisLine;
		while ((thisLine = fileContents.readLine()) != null)
		{
			lineNumber++;
			thisLine = thisLine.trim();
			if (thisLine.isEmpty())
			{
				// Empty line, ignore
			}
			else if (thisLine.startsWith("#") || thisLine.startsWith("<"))
			{
				// Comment, ignore
			}
			else if (thisLine.contains(":") || thisLine.toLowerCase().contains("("))
			{
				settings.addRawSetting(getRawSetting(thisLine).withLineNumber(lineNumber));
			}
			else
			{
				OTGLog.warn(LogCategory.CONFIGS, "Invalid line: {} in file {} on line {}", thisLine, settings.getName(), lineNumber);
			}
		}
	}

	private static RawSettingValue getRawSetting(String thisLine) {
		// Setting or resource
		if (
				thisLine.contains("(") &&
						(
								!thisLine.contains(":") ||
										thisLine.indexOf('(') < thisLine.indexOf(':')
						)
		)
		{
			// ( is first, so it's a resource
			return RawSettingValue.create(ValueType.FUNCTION, thisLine);
		} else {
			// : is first, so it's a setting
			return RawSettingValue.create(ValueType.PLAIN_SETTING, thisLine);
		}
	}
}
