package com.pg85.otg.interfaces;

import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.util.materials.LocalMaterialBase;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterialTag;

public interface IMaterialReader
{
	LocalMaterialData readMaterial(String material) throws InvalidConfigException;
	LocalMaterialTag readTag(String tag) throws InvalidConfigException;
	default LocalMaterialBase read(String input) throws InvalidConfigException {
		if (input.startsWith("#") || input.startsWith("otg:")) {
			return readTag(input);
		}
		return readMaterial(input);
//		LocalMaterialTag tag = readTag(input);
//		if(tag != null) {
//			return tag;
//		}
//		return readMaterial(input);
	}
}
