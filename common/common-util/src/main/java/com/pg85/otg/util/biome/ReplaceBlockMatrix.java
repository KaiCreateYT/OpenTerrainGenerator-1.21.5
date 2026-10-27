package com.pg85.otg.util.biome;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.util.OTGLog;
import com.pg85.otg.util.OTGMaterialReader;
import com.pg85.otg.util.helpers.StringHelper;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterialTag;
import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ReplaceBlockMatrix
{
    /**
     *  Gets an immutable list of all ReplacedBlocks instructions.
     *  Note that the returned list is immutable, see setInstructions
     */
    @Getter
    @JsonProperty
	private List<ReplacedBlocksInstruction> instructions;

	public boolean initialised = false;

	private static final String NO_REPLACE = "None";

	// All ReplacedBlocksInstructions must have maxHeight smaller than or equal to this.
	private final int maxHeight = Constants.WORLD_END_MAX_Y;
	private final int minHeight = Constants.WORLD_START_MIN_Y;

	Int2ObjectAVLTreeMap<List<ReplacedBlocksInstruction>> targetsAtHeights = new Int2ObjectAVLTreeMap<>();

	public boolean replacesCooledLava = false;
	public boolean replacesIce = false;
	public boolean replacesPackedIce = false;
	public boolean replacesSnow = false;
	public boolean replacesWater = false;
	public boolean replacesStone = false;
	public boolean replacesGround = false;
	public boolean replacesSurface = false;
	public boolean replacesUnderWaterSurface = false;
	public boolean replacesBedrock = false;
	public boolean replacesSandStone = false;
	public boolean replacesRedSandStone = false;

	public ReplaceBlockMatrix(@JsonProperty List<ReplacedBlocksInstruction> instructions) {
		setInstructions(instructions);
	}

	public ReplaceBlockMatrix(String setting) throws InvalidConfigException
	{
		
		// Parse
		if (setting.isEmpty() || setting.equalsIgnoreCase(NO_REPLACE))
		{
			setInstructions(Collections.emptyList());
			return;
		}

		List<ReplacedBlocksInstruction> instructions = new ArrayList<>();
		String[] keys = StringHelper.readCommaSeperatedString(setting);

		for (String key : keys)
		{
			int start = key.indexOf('(');
			int end = key.lastIndexOf(')');
			if (start != -1 && end != -1)
			{
				String keyWithoutBraces = key.substring(start + 1, end);
				instructions.add(new ReplacedBlocksInstruction(keyWithoutBraces, OTGMaterialReader.get()));
			} else {
				throw new InvalidConfigException("One of the parts is missing braces around it.");
			}
		}

		// Set
		setInstructions(instructions);		
	}
	
	public void init(LocalMaterialData biomeCooledLavaBlock, LocalMaterialData biomeIceBlock, LocalMaterialData biomePackedIceBlock, LocalMaterialData biomeSnowBlock, LocalMaterialData biomeWaterBlock, LocalMaterialData biomeStoneBlock, LocalMaterialData biomeGroundBlock, LocalMaterialData biomeSurfaceBlock, LocalMaterialData biomeUnderWaterSurfaceBlock, LocalMaterialData biomeBedrockBlock, LocalMaterialData biomeSandStoneBlock, LocalMaterialData biomeRedSandStoneBlock)
	{
		// Fill maps for faster access
		for(ReplacedBlocksInstruction instruction : this.instructions)
		{
			for(int y = instruction.minHeight; y <= instruction.maxHeight; y++)
			{
				if(y > this.maxHeight)
				{
					break;
				}
				if(y < this.minHeight)
				{
					continue;
				}
				List<ReplacedBlocksInstruction> targetsAtHeight = this.targetsAtHeights.get(y);
				if(targetsAtHeight == null)
				{
					targetsAtHeight = new ArrayList<>();
					this.targetsAtHeights.put(y, targetsAtHeight);
				}
				
				// Users can chain replacedblocks to replace replacedblocks, instead of actually replacing the 
				// same block to different materials multiple times, we'll calculate the end result in advance.
				for(ReplacedBlocksInstruction existing : targetsAtHeight)
				{
					// If this instruction replaces the output of a previously added
					// instruction, override the output of the previous instruction.
					LocalMaterialData existingTo = existing.to;
					if(instruction.from instanceof LocalMaterialData data)
					{
						if(
							(data.isDefaultState() && data.getRegistryName().equals(existingTo.getRegistryName())) ||
							(!data.isDefaultState() && data.hashCode() == existingTo.hashCode())
						)
						{
							existing.to = instruction.to;
						}
					} else if (instruction.from instanceof LocalMaterialTag tag) {
						if(instruction.from.isTag() && existingTo.isBlockTag(tag))
						{
							existing.to = instruction.to;
						}
					} else {
						OTGLog.error(LogCategory.CONFIGS, "Unknown type of material: {}", instruction.from);
					}
				}
				targetsAtHeight.add(instruction.copyInstruction());
			}
		}
		
		for(ReplacedBlocksInstruction instruction : this.instructions)
		{
			if(instruction.from.matches(biomeCooledLavaBlock))
			{
				this.replacesCooledLava = true;
			}
			if(instruction.from.matches(biomeIceBlock))
			{
				this.replacesIce = true;
			}
			if(instruction.from.matches(biomePackedIceBlock))
			{
				this.replacesPackedIce = true;
			}
			if(instruction.from.matches(biomeSnowBlock))
			{
				this.replacesSnow = true;
			}			
			if(instruction.from.matches(biomeWaterBlock))	
			{
				this.replacesWater = true;
			}
			if(instruction.from.matches(biomeStoneBlock))
			{
				this.replacesStone = true;
			}
			if(instruction.from.matches(biomeGroundBlock))
			{
				this.replacesGround = true;
			}
			if(instruction.from.matches(biomeSurfaceBlock))
			{
				this.replacesSurface = true;
			}
			if(instruction.from.matches(biomeUnderWaterSurfaceBlock))
			{
				this.replacesUnderWaterSurface = true;
			}
			if(instruction.from.matches(biomeBedrockBlock))
			{
				this.replacesBedrock = true;
			}
			if(instruction.from.matches(biomeSandStoneBlock))
			{
				this.replacesSandStone = true;
			}
			if(instruction.from.matches(biomeRedSandStoneBlock))
			{
				this.replacesRedSandStone = true;
			}
		}
	}
  
	public boolean replacesBlock(LocalMaterialData targetBlock)
	{				
		for(ReplacedBlocksInstruction instruction : this.instructions)
		{
			if(instruction.from.matches(targetBlock))
			{
				return true;
			}
		}
		return false;
	}	
	
	public LocalMaterialData replaceBlock(int y, LocalMaterialData material)
	{
		List<ReplacedBlocksInstruction> targetsAtHeight = targetsAtHeights.get(y);
		if(targetsAtHeight != null)
		{
			for(ReplacedBlocksInstruction instruction : targetsAtHeight)
			{			
				if(instruction.from.matches(material))
				{
					return instruction.to;
				}
			}
		}
		return material;
	}

	/**
	 * Gets whether this biome has replace settings set. If this returns true,
	 * the {@link #compiledInstructions} array won't be null.
	 * 
	 * @return Whether this biome has replace settings set.
	 */
	public boolean hasReplaceSettings()
	{
		return this.instructions != null && !this.instructions.isEmpty();
	}

    /**
	 * Sets the ReplacedBlocks instructions. This method will update the
	 * {@link #compiledInstructions} array.
	 * 
	 * @param instructions The new instructions.
	 */
	public void setInstructions(Collection<ReplacedBlocksInstruction> instructions)
	{
		this.instructions = List.copyOf(instructions);
	}

	public String toString()
	{
		if (!this.hasReplaceSettings())
		{
			// No replace setting
			return NO_REPLACE;
		}

		StringBuilder builder = new StringBuilder();
		for (ReplacedBlocksInstruction instruction : getInstructions())
		{
			builder.append('(');
			builder.append(instruction.from);
			builder.append(',').append(instruction.to);
			if (instruction.getMinHeight() != this.minHeight || instruction.getMaxHeight() != this.maxHeight)
			{
				// Add custom height setting
				builder.append(',').append(instruction.getMinHeight());
				builder.append(',').append(instruction.getMaxHeight());
			}
			builder.append(')').append(',');
		}

		// Remove last ',' and return the result
		return builder.substring(0, builder.length() - 1);
	}

	/**
	 * Creates an empty matrix.
	 *
	 * @return The empty matrix.
	 */
	public static ReplaceBlockMatrix createEmptyMatrix()
	{
		try {
			return new ReplaceBlockMatrix(NO_REPLACE);
		} catch (InvalidConfigException e) {
			throw new AssertionError(e); // Should never happen
		}
	}
}
