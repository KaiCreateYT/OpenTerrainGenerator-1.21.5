package com.pg85.otg.config.yaml;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.util.Color;
import com.pg85.otg.util.helpers.StringHelper;

import java.io.IOException;

public class ColorSettingDeserializer extends JsonDeserializer<Color> {

    @Override
    public Color deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return new Color(p.getValueAsString());
    }
}
