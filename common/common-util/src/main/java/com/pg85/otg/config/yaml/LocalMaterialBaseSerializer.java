package com.pg85.otg.config.yaml;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.pg85.otg.util.materials.LocalMaterialBase;

import java.io.IOException;

public class LocalMaterialBaseSerializer extends StdSerializer<LocalMaterialBase> {

    public LocalMaterialBaseSerializer() {
        this(null);
    }

    public LocalMaterialBaseSerializer(Class<LocalMaterialBase> t) {
        super(t);
    }

    @Override
    public void serialize(LocalMaterialBase value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeString(value.toString());
    }
}
