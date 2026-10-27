package com.pg85.otg.config.yaml;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.pg85.otg.util.Color;

import java.io.IOException;

public class ColorSettingSerializer extends StdSerializer<Color> {

    public ColorSettingSerializer() {
        this(null);
    }

    public ColorSettingSerializer(Class<Color> t) {
        super(t);
    }

    @Override
    public void serialize(Color value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeString(value.toString());
    }
}
