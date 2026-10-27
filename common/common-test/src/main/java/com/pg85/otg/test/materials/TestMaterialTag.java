package com.pg85.otg.test.materials;

import com.pg85.otg.util.materials.LocalMaterialTag;

/**
 * Simple LocalMaterialTag implementation for headless testing.
 * Just stores the tag name - no actual tag resolution.
 */
public class TestMaterialTag extends LocalMaterialTag {

    public TestMaterialTag(String name) {
        super(name);
    }

    @Override
    public String toString() {
        return name;
    }
}
