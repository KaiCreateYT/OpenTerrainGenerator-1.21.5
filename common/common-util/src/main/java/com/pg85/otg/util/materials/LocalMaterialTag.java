package com.pg85.otg.util.materials;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents one of Minecraft's material tags.
 * Immutable.
 */
public abstract class LocalMaterialTag extends LocalMaterialBase
{
	@JsonProperty("tag")
	protected final String name;

	@JsonCreator
	public LocalMaterialTag(@JsonProperty("tag") String name) {
		this.name = name;
	}

	@Override
	public boolean isTag()
	{
		return true;
	}
	
	@Override
	public boolean matches(LocalMaterialData material)
	{
		return material != null && material.isBlockTag(this);
	}

	@Override
	public String toString()
	{
		return this.name;
	}
}
