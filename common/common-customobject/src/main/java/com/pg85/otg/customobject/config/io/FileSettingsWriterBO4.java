package com.pg85.otg.customobject.config.io;

import com.pg85.otg.config.settingtype.Setting;
import com.pg85.otg.constants.settings.ConfigMode;
import com.pg85.otg.customobject.bo4.BO4Config;
import com.pg85.otg.customobject.bofunctions.BlockFunction;
import com.pg85.otg.customobject.bofunctions.BranchFunction;
import com.pg85.otg.customobject.config.CustomObjectConfigFile;
import com.pg85.otg.customobject.config.CustomObjectConfigFunction;
import com.pg85.otg.customobject.config.CustomObjectResourcesManager;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.util.OTGLog;
import com.pg85.otg.util.logging.LogCategory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileSettingsWriterBO4 implements SettingsWriterBO4
{
	private final File file;
	private boolean writeComments;
	private BufferedWriter writer;
	
	private FileSettingsWriterBO4(File file)
	{
		this.file = file;
	}
	
	/**
	 * Writes this configuration settings to a file. It will use the file
	 * returned by {@link CustomObjectConfigFile#getFile()}. If writing fails, the error
	 * is logged.
	 *
	 * @param config	 The configuration to write to disk.
	 * @param configMode The configuration mode. If this is set to
	 * WriteDisable, this method does nothing.
	 */
	public static void writeToFile(CustomObjectConfigFile config, ConfigMode configMode,  IMaterialReader materialReader, CustomObjectResourcesManager manager)
	{
		writeToFile(config, config.getFile(), configMode,  materialReader, manager);
	}

	/**
	 * Writes the configuration settings to the given file. If writing fails,
	 * the error is logged.
	 *
	 * @param config	 The configuration to write to disk.
	 * @param file		The file to write to.
	 * @param configMode The configuration mode. If this is set to
	 * WriteDisable, this method does nothing.
	 */
	public static void writeToFile(CustomObjectConfigFile config, File file, ConfigMode configMode,  IMaterialReader materialReader, CustomObjectResourcesManager manager)
	{
		if (configMode == ConfigMode.WriteDisable)
		{
			return;
		}

		try
		{
			SettingsWriterBO4 writer = new FileSettingsWriterBO4(file);
			config.write(writer, configMode,  materialReader, manager);
		} catch (IOException e) {
			OTGLog.error(LogCategory.CONFIGS, "Failed to write to file", e);
		}
	}

	/** Writes a BO4 to file with extra data; used by /otg export
	 *
	 * @param config The BO4 Config to be written
	 * @param blocksList The list of blocks to be written to the config
	 * @param branchesList The list of branches to be written to the config
	 */
	public static void writeToFileWithData(BO4Config config, List<BlockFunction<?>> blocksList, List<BranchFunction<?>> branchesList,  IMaterialReader materialReader, CustomObjectResourcesManager manager)
	{
		FileSettingsWriterBO4 writer = new FileSettingsWriterBO4(config.getFile());
		try
		{
			config.writeWithData
				(
					writer,
					blocksList == null ? new ArrayList<>() : blocksList,
					branchesList == null ? new ArrayList<>() : branchesList,
					
					materialReader,
					manager
				);
		} catch (IOException e) {
			OTGLog.error(LogCategory.CONFIGS, "Failed to write BO4 config", e);
		}
	}

	@Override
	public void bigTitle(String title) throws IOException
	{
		checkState();
		writer.newLine();
		writer.write("#######################################################################");
		writer.newLine();
		writer.write("# +-----------------------------------------------------------------+ #");
		writer.newLine();
		StringBuilder builder = new StringBuilder(title);
		builder.insert(0, ' ');
		builder.append(' ');
		boolean flag = true;
		while (builder.length() < 65)
		{
			if (flag)
				builder.insert(0, ' ');
			else
				builder.append(' ');
			flag = !flag;
		}
		writer.write("# |" + builder.toString() + "| #");
		writer.newLine();
		writer.write("# +-----------------------------------------------------------------+ #");
		writer.newLine();
		writer.write("#######################################################################");
		writer.newLine();
		writer.newLine();
	}

	private void checkState() throws IllegalStateException
	{
		if (writer == null)
		{
			throw new IllegalStateException("Not started writing yet");
		}
	}

	@Override
	public void close()
	{
		if (writer == null)
		{
			// Can happen if file opening failed, ignore
			return;
		}

		try
		{
			writer.flush();
			writer.close();
		}
		catch (IOException e)
		{
			OTGLog.error(LogCategory.CONFIGS, "Failed to close file {}", file.getAbsolutePath(), e);
		}
		writer = null;
	}

	@Override
	public void comment(String comment) throws IOException
	{
		checkState();
		if (!this.writeComments)
			return;
		if (!comment.isEmpty())
			writer.write("# " + comment);
		writer.newLine();
	}

	@Override
	public void function(CustomObjectConfigFunction<?> function) throws IOException
	{
		checkState();
		writer.write(function.write());
		writer.newLine();
	}

	@Override
	public File getFile()
	{
		return file;
	}

	@Override
	public void open() throws IOException
	{
		file.getParentFile().mkdirs();
		writer = new BufferedWriter(new FileWriter(file));
	}

	@Override
	public void setConfigMode(ConfigMode configMode)
	{
		if (configMode == ConfigMode.WriteAll)
		{
			this.writeComments = true;
		} else if (configMode == ConfigMode.WriteWithoutComments)
		{
			this.writeComments = false;
		} else
		{
			throw new IllegalArgumentException("Invalid config mode: " + configMode);
		}
	}

	@Override
	public <T> void setting(Setting<T> setting, T value) throws IOException
	{
		checkState();
		writer.write(setting.getName() + ": " + setting.write(value));
		writer.newLine();
		writer.newLine();
	}

	@Override
	public void smallTitle(String title) throws IOException
	{
		checkState();
		int titleLength = title.length();
		StringBuilder rowBuilder = new StringBuilder(titleLength + 4);
        rowBuilder.append("#".repeat(Math.max(0, titleLength + 4)));
		writer.write(rowBuilder.toString());
		writer.newLine();
		writer.write("# " + title + " #");
		writer.newLine();
		writer.write(rowBuilder.toString());
		writer.newLine();
		writer.newLine();
	}
}
