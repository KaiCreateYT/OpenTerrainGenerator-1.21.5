package com.pg85.otg.customobject.bo4;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

import com.pg85.otg.customobject.CustomObjectManager;
import com.pg85.otg.customobject.config.CustomObjectResourcesManager;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IModLoadedChecker;
import com.pg85.otg.util.OTGLog;
import com.pg85.otg.util.logging.LogCategory;

public class BO4Data
{
	public static boolean bo4DataExists(BO4Config config)
	{
		String filePath = 
			config.getFile().getAbsolutePath().endsWith(".BO4") ? config.getFile().getAbsolutePath().replace(".BO4", ".BO4Data") :
			config.getFile().getAbsolutePath().endsWith(".bo4") ? config.getFile().getAbsolutePath().replace(".bo4", ".BO4Data") :
			config.getFile().getAbsolutePath().endsWith(".BO3") ? config.getFile().getAbsolutePath().replace(".BO3", ".BO4Data") :
			config.getFile().getAbsolutePath().endsWith(".bo3") ? config.getFile().getAbsolutePath().replace(".bo3", ".BO4Data") :
			config.getFile().getAbsolutePath();

		File file = new File(filePath);
		return file.exists();
	}
	
	public static void generateBO4Data(BO4Config config, String presetFolderName, Path otgRootFolder,  CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		//write to disk
		String filePath = 
			config.getFile().getAbsolutePath().endsWith(".BO4") ? config.getFile().getAbsolutePath().replace(".BO4", ".BO4Data") :
			config.getFile().getAbsolutePath().endsWith(".bo4") ? config.getFile().getAbsolutePath().replace(".bo4", ".BO4Data") :
			config.getFile().getAbsolutePath().endsWith(".BO3") ? config.getFile().getAbsolutePath().replace(".BO3", ".BO4Data") :
			config.getFile().getAbsolutePath().endsWith(".bo3") ? config.getFile().getAbsolutePath().replace(".bo3", ".BO4Data") :
			config.getFile().getAbsolutePath();

		File file = new File(filePath);
		if(!file.exists())
		{
			try {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				DataOutputStream dos = new DataOutputStream(bos);
				BO4DataSerializer.writeToStream(config, dos, presetFolderName, otgRootFolder, customObjectManager, materialReader, manager, modLoadedChecker);
				byte[] compressedBytes = com.pg85.otg.util.CompressionUtils.compress(bos.toByteArray());
				dos.close();
				FileOutputStream fos = new FileOutputStream(file);
				DataOutputStream dos2 = new DataOutputStream(fos);
				dos2.write(compressedBytes, 0, compressedBytes.length);
				dos2.close();
			}
			catch (IOException e)
			{
				OTGLog.error(LogCategory.CUSTOM_OBJECTS, "Failed to write BO4 data file", e);
			}
		}
	}
}
