package com.pg85.otg.util.gen;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.util.ChunkCoordinate;

public class DecorationArea
{
	// Anything that uses decoration bounds should be using the methods in this class.
	// TODO: Refactor this, change the decorated area from 2x2 to 3x3 chunks and 
	// remove the +8 decoration offset used for resources. BO/Carver offsets are also
	// in here for clarity for the moment, should clean that up when resources are 
	// properly re-aligned.

	// Structure reference: chunkstatus stage 1, can fetch 7x7
	// Surface: chunkstatus stage 4, 3x3 is stage 3, can fetch 7x7
	// Carver: chunkstatus stage 5, 3x3 is stage 3, can fetch 7x7
	// Biome Decoration: chunkstatus stage 6, 3x3 is stage 6, can fetch 7x7
	// Mobs: chunkstatus stage 9, can only fetch 1x1

	public static final int DECORATION_OFFSET = 8;
	public static final int CARVER_OFFSET = 8;
	public static final int BO_CHUNK_CENTER_X = 8;
	public static final int BO_CHUNK_CENTER_Z = 7;

	private static final int WIDTH_IN_CHUNKS = 3;
	private static final int HEIGHT_IN_CHUNKS = 3;
	public static final int WIDTH = WIDTH_IN_CHUNKS * Constants.CHUNK_SIZE;
	public static final int HEIGHT = HEIGHT_IN_CHUNKS * Constants.CHUNK_SIZE;
	
	private final int minX;
	private final int maxX;
	private final int minZ;
	private final int maxZ;
	private final int width;
	private final int height;
	private final ChunkCoordinate chunkBeingDecorated;

	public DecorationArea(ChunkCoordinate chunkBeingDecorated)
	{
		int top = Constants.CHUNK_SIZE;
		int right = Constants.CHUNK_SIZE;
		int bottom = Constants.CHUNK_SIZE;
		int left = Constants.CHUNK_SIZE;
		this.width = left + right + Constants.CHUNK_SIZE;
		this.height = top + bottom + Constants.CHUNK_SIZE;
		this.minX = chunkBeingDecorated.getBlockX() - left;
		this.maxX = chunkBeingDecorated.getBlockX() + Constants.CHUNK_SIZE + right;
		this.minZ = chunkBeingDecorated.getBlockZ() - top;
		this.maxZ = chunkBeingDecorated.getBlockZ() + Constants.CHUNK_SIZE + bottom;
		this.chunkBeingDecorated = chunkBeingDecorated;
	}

	// TODO: Some resources don't check decoration bounds before calling getMaterial/setBlock,
	// which does the check and returns null/ignores the call. Making resources check preemptively
	// may be more efficient and would allow us to remove isInAreaBeingDecorated checks in
	// getMaterial/setBlock (except for debugging purposes).
	public boolean isInAreaBeingDecorated(int blockX, int blockZ)
	{
		return 
			blockX >= this.minX &&
			blockX < this.maxX &&
			blockZ >= this.minZ &&
			blockZ < this.maxZ
		;
	}

	public int getWidth()
	{
		return this.width;
	}

	public int getHeight()
	{
		return this.height;
	}

	public int getTop()
	{
		return this.minZ;
	}

	public int getBottom()
	{
		return this.minZ + this.height;
	}	
	
	public int getLeft()
	{
		return this.minX;
	}
	
	public int getRight()
	{
		return this.minX + this.width;
	}

	public ChunkCoordinate getChunkBeingDecorated()
	{
		return this.chunkBeingDecorated;
	}

	public int getChunkBeingDecoratedMinX()
	{
		return this.chunkBeingDecorated.getChunkX() * Constants.CHUNK_SIZE;
	}

	public int getChunkBeingDecoratedMinZ()
	{
		return this.chunkBeingDecorated.getChunkZ() * Constants.CHUNK_SIZE;
	}
}
