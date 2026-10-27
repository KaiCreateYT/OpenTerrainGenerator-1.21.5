package com.pg85.otg.config.yaml;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.util.helpers.StringHelper;
import com.pg85.otg.util.materials.MaterialSet;

import java.io.IOException;

public class MaterialSetDeserializer extends JsonDeserializer<MaterialSet> {
    @Override
    public MaterialSet deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText();
        MaterialSet materialSet = new MaterialSet();
        for (String blockName : StringHelper.readCommaSeperatedString(value))
        {
            try {
                materialSet.parseAndAdd(blockName);
            } catch (InvalidConfigException e) {
                throw new RuntimeException(e);
            }
        }
        return materialSet;
    }
}
