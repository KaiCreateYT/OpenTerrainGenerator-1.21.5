package com.pg85.otg.config.yaml;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.util.OTGLog;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.materials.LocalMaterialData;

import java.io.IOException;

public class LocalMaterialDataDeserializer extends JsonDeserializer<LocalMaterialData> {
    private final IMaterialReader materialReader;

    public LocalMaterialDataDeserializer(IMaterialReader materialReader) {
        this.materialReader = materialReader;
    }

    @Override
    public LocalMaterialData deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText();
        try {
            return materialReader.readMaterial(value);
        } catch (InvalidConfigException e) {
            OTGLog.error(LogCategory.CONFIGS, "Invalid material {}. Exception: {}", value, e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
