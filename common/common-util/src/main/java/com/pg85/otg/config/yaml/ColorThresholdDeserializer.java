package com.pg85.otg.config.yaml;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.pg85.otg.util.Color;
import com.pg85.otg.util.biome.ColorThreshold;

import java.io.IOException;

public class ColorThresholdDeserializer extends JsonDeserializer<ColorThreshold> {
    @Override
    public ColorThreshold deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = p.getCodec().readTree(p);
        Color color = new Color(node.get("color").asText());
        float maxNoise = (float) node.get("maxNoise").asDouble();
        return new ColorThreshold(color, maxNoise);
    }
}
