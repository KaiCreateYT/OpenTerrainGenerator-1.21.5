package com.pg85.otg.util.biome;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ColorSet
{
	@JsonProperty
	protected List<ColorThreshold> layers = new ArrayList<>();

	@JsonCreator
	public ColorSet(@JsonProperty List<ColorThreshold> layers) {
		this.layers = layers;
	}

	public ColorSet() {

	}

    public int getColor(double noise, int def)
	{
		if (this.layers.isEmpty())
		{
			return def;
		}
		for (ColorThreshold color : this.layers)
		{
			if (noise <= color.maxNoise)
			{
				return color.getColor().intValue();
			}
		}
		return def;
	}

	@Override
	public String toString()
	{
		// Make sure that empty name is written to the config files
		return "";
	}
}
