package com.pg85.otg.config.settingtype;

import com.pg85.otg.config.ConfigFunction;
import com.pg85.otg.config.settings.ConfigSection;
import com.pg85.otg.exceptions.InvalidConfigException;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.function.Function;

public class BiomeResourceSetting<T extends ConfigFunction<?>> extends Setting<T> {
    T defaultValue;

    protected BiomeResourceSetting(String name, List<String> defaultParameters, Class<T> clazz, Function<ConfigSection, T> getter, String... description) throws Exception {
        super(name, getter, description);
        this.defaultValue = createDefaultInstance(clazz, defaultParameters);
    }

    private T createDefaultInstance(Class<T> clazz, List<String> parameters) throws Exception {
        Constructor<T> constructor = clazz.getConstructor(List.class);
        return constructor.newInstance(parameters);
    }

    @Override
    public T getDefaultValue() {
        return defaultValue;
    }

    @Override
    public T read(String string) throws InvalidConfigException {
        return null;
    }

    @Override
    public String getTypeAsString() {
        return "object";
    }

    @Override
    public String getComplexTypeSchema() {
        return super.getComplexTypeSchema();
    }
}
