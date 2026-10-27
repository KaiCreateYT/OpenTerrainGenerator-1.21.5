package com.pg85.otg.util.materials;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.pg85.otg.config.yaml.LocalMaterialBaseDeserializer;
import com.pg85.otg.config.yaml.LocalMaterialBaseSerializer;

/**
 * Represents one of Minecraft's materials.
 * Immutable.
 */

@JsonDeserialize(using = LocalMaterialBaseDeserializer.class)
@JsonSerialize(using = LocalMaterialBaseSerializer.class)
public abstract class LocalMaterialBase
{
	public abstract boolean isTag();

	public abstract String toString();

	public abstract boolean matches(LocalMaterialData material);
}
