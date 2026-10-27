package com.pg85.otg.util.biome;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.util.helpers.StringHelper;
import com.pg85.otg.util.materials.LocalMaterialBase;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterialTag;
import lombok.Getter;

@Getter
public class ReplacedBlocksInstruction {
    @JsonProperty
    protected final LocalMaterialBase from;
    @JsonProperty
    protected LocalMaterialData to;
    @JsonProperty
    protected final int minHeight;
    @JsonProperty
    protected final int maxHeight;

    /**
     * Parses the given instruction string.
     *
     * @param instruction The instruction string.
     * @throws InvalidConfigException If the instruction is formatted incorrectly.
     */
    ReplacedBlocksInstruction(String instruction, IMaterialReader materialReader) throws InvalidConfigException {
        String[] values = instruction.split(",(?![^\\(\\[]*[\\]\\)])"); // Splits on any comma not inside brackets
        if (values.length == 5) {
            // Replace in TC 2.3 style found
            values = new String[]{values[0], values[1] + ":" + values[2], values[3], "" + (Integer.parseInt(values[4]) - 1)};
        }

        if (values.length != 2 && values.length != 4) {
            throw new InvalidConfigException("Replace parts must be in the format (from,to) or (from,to,minHeight,maxHeight)");
        }

        LocalMaterialTag tag = materialReader.readTag(values[0]);
        if (tag != null) {
            this.from = tag;
        } else {
            this.from = materialReader.readMaterial(values[0]);
        }
        this.to = materialReader.readMaterial(values[1]);

        if (values.length == 4) {
            this.minHeight = StringHelper.readInt(values[2], Constants.WORLD_START_MIN_Y, Constants.WORLD_END_MAX_Y);
            this.maxHeight = StringHelper.readInt(values[3], this.minHeight, Constants.WORLD_END_MAX_Y);
        } else {
            this.minHeight = Constants.WORLD_START_MIN_Y;
            this.maxHeight = Constants.WORLD_END_MAX_Y;
        }
    }

    /**
     * Creates a ReplacedBlocksInstruction with the given parameters.
     * Parameters may not be null.
     *
     * @param from      The block that will be replaced.
     * @param to        The block that from will be replaced to.
     * @param minHeight Minimum height for this replace, inclusive. Must be smaller than or equal to 0.
     * @param maxHeight Maximum height for this replace, inclusive. Must not be larger than {@link ReplaceBlockMatrix#maxHeight}.
     */
    public ReplacedBlocksInstruction(@JsonProperty LocalMaterialBase from, @JsonProperty LocalMaterialData to, @JsonProperty int minHeight, @JsonProperty int maxHeight) {
        this.from = from;
        this.to = to;
        this.minHeight = minHeight;
        this.maxHeight = maxHeight;
    }

    public ReplacedBlocksInstruction copyInstruction() {
        return new ReplacedBlocksInstruction(this.from, this.to, this.minHeight, this.maxHeight);
    }

}
