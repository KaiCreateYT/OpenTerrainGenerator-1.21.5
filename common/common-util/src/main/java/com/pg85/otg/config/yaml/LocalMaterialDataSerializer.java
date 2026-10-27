package com.pg85.otg.config.yaml;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.pg85.otg.util.materials.LocalMaterialData;

import java.io.IOException;

public class LocalMaterialDataSerializer extends StdSerializer<LocalMaterialData> {

    public LocalMaterialDataSerializer() {
        this(null);
    }

    public LocalMaterialDataSerializer(Class<LocalMaterialData> t) {
        super(t);
    }

    @Override
    public void serialize(LocalMaterialData value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeString(value.toString());
    }
}
