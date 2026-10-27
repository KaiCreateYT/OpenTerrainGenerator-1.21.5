package com.pg85.otg.config.yaml;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.pg85.otg.util.Color;
import com.pg85.otg.util.biome.ColorThreshold;

import java.io.IOException;

public class ColorThresholdSerializer extends JsonSerializer<ColorThreshold> {
    @Override
    public void serialize(ColorThreshold value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        // Serialize the color using the custom ColorSettingSerializer
        gen.writeFieldName("color");
        serializers.findValueSerializer(Color.class).serialize(value.getColor(), gen, serializers);
        // Serialize the maxNoise field
        gen.writeNumberField("maxNoise", value.getMaxNoise());
        gen.writeEndObject();
    }
}
