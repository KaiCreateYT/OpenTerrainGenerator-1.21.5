package com.pg85.otg.util;

import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.util.helpers.StringHelper;
import lombok.Getter;

@Getter
public class Color extends Number {
    private final int color;

    public Color (int color) {
        this.color = color;
    }

    public Color (String color) {
        try {
            this.color = StringHelper.readColor(color);
        } catch (InvalidConfigException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int intValue() {
        return color;
    }

    @Override
    public long longValue() {
        return color;
    }

    @Override
    public float floatValue() {
        return color;
    }

    @Override
    public double doubleValue() {
        return color;
    }

    @Override
    public String toString() {
        return "0x" + Integer.toHexString(color | 0x1000000).substring(1).toUpperCase();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Color other) {
            return this.color == other.color;
        }
        return false;
    }
}
