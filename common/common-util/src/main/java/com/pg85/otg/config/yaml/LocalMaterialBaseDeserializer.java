package com.pg85.otg.config.yaml;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.util.OTGLog;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.materials.LocalMaterialBase;

import java.io.IOException;

public class LocalMaterialBaseDeserializer extends JsonDeserializer<LocalMaterialBase> {
    private final IMaterialReader materialReader;

    public LocalMaterialBaseDeserializer(IMaterialReader materialReader) {
        this.materialReader = materialReader;
    }

    @Override
    public LocalMaterialBase deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText();
        try {
            return materialReader.read(value);
        } catch (InvalidConfigException e) {
            OTGLog.error(LogCategory.CONFIGS, "Invalid material {}. Exception: {}", value, e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
