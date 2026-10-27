package com.pg85.otg.config.yaml;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.pg85.otg.util.materials.MaterialSet;

import java.io.IOException;

public class MaterialSetSerializer extends StdSerializer<MaterialSet> {

    public MaterialSetSerializer() {
        this(null);
    }

    public MaterialSetSerializer(Class<MaterialSet> t) {
        super(t);
    }

    @Override
    public void serialize(MaterialSet value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeString(value.toString());
    }
}
