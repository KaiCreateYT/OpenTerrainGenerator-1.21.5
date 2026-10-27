package com.pg85.otg.util.biome;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.pg85.otg.config.yaml.ColorSettingDeserializer;
import com.pg85.otg.config.yaml.ColorSettingSerializer;
import com.pg85.otg.config.yaml.ColorThresholdSerializer;
import com.pg85.otg.util.Color;
import lombok.Getter;

@Getter
public class ColorThreshold implements Comparable<ColorThreshold>
{
	@JsonProperty
	@JsonFormat(shape = JsonFormat.Shape.STRING)
	final Color color;

	@JsonProperty
	final float maxNoise;

	@JsonCreator
	public ColorThreshold(@JsonProperty Color color, @JsonProperty float maxNoise) {
		this.color = color;
		this.maxNoise = maxNoise;
	}

	public ColorThreshold(int color, float maxNoise) {
		this.color = new Color(color);
		this.maxNoise = maxNoise;
	}

    @Override
	public int compareTo(ColorThreshold that)
	{
		float delta = this.maxNoise - that.maxNoise;
		// The number 65565 is just randomly chosen, any positive number
		// works fine as long as it can represent the floating point delta
		// as an integer
		return (int) (delta * 65565);
	}
}
