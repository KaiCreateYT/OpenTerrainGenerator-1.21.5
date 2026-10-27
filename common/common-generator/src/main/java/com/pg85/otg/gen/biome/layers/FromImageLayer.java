package com.pg85.otg.gen.biome.layers;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.ImageIO;

import com.pg85.otg.constants.settings.ImageMode;
import com.pg85.otg.constants.settings.ImageOrientation;
import com.pg85.otg.gen.biome.layers.type.ParentedLayer;
import com.pg85.otg.gen.biome.layers.util.LayerSampleContext;
import com.pg85.otg.interfaces.ILayerSampler;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.config.settings.preset.ImageSettings;
import com.pg85.otg.util.logging.LogCategory;

public class FromImageLayer implements ParentedLayer
{
	private static final Object cacheLock = new Object();
	private static final ConcurrentHashMap<File, ProcessedImageData> processedCache = new ConcurrentHashMap<>();

	private static class ProcessedImageData
	{
		// short[] halves memory vs int[] — biome IDs fit in short range
		final short[] biomeMap;
		final int imageWidth;
		final int imageHeight;
		final ImageOrientation orientation;

		ProcessedImageData(short[] biomeMap, int imageWidth, int imageHeight, ImageOrientation orientation)
		{
			this.biomeMap = biomeMap;
			this.imageWidth = imageWidth;
			this.imageHeight = imageHeight;
			this.orientation = orientation;
		}

		// Logical dimensions after rotation
		int mapWidth()
		{
			return (orientation == ImageOrientation.West || orientation == ImageOrientation.East)
					? imageHeight : imageWidth;
		}

		int mapHeight()
		{
			return (orientation == ImageOrientation.West || orientation == ImageOrientation.East)
					? imageWidth : imageHeight;
		}

		// Sample with rotation applied at read time — no rotated copy needed
		int getBiome(int mapX, int mapZ)
		{
			int ix, iz;
			switch (orientation)
			{
				case South:
					ix = imageWidth - 1 - mapX;
					iz = imageHeight - 1 - mapZ;
					break;
				case West:
					// CW rotation: logical (mapX, mapZ) -> image (mapZ, imageHeight-1-mapX)
					ix = mapZ;
					iz = imageHeight - 1 - mapX;
					break;
				case East:
					// CCW rotation: logical (mapX, mapZ) -> image (imageWidth-1-mapZ, mapX)
					ix = imageWidth - 1 - mapZ;
					iz = mapX;
					break;
				default: // North
					ix = mapX;
					iz = mapZ;
					break;
			}
			return biomeMap[iz * imageWidth + ix];
		}
	}

	private final BiomeLayerData data;
	private final ImageSettings imageSettings;
	private final ProcessedImageData imageData;
	private final int mapWidth;
	private final int mapHeight;

	FromImageLayer(BiomeLayerData data, ILogger logger)
	{
		this.data = data;
		this.imageSettings = data.imageSettings;

		final File image = new File(data.presetDir.toFile(), imageSettings.getImageFile());
		if (!image.exists())
		{
			logger.fatal(LogCategory.CONFIGS, "FromImageLayer encountered a critical error: {} does not exist", image.getAbsolutePath());
			throw new RuntimeException("FromImageLayer encountered a critical error: File does not exist");
		}

		ProcessedImageData cached = processedCache.get(image);
		if (cached == null)
		{
			synchronized (cacheLock)
			{
				cached = processedCache.get(image);
				if (cached == null)
				{
					cached = processImage(image, data, logger);
					processedCache.put(image, cached);
				}
			}
		}

		this.imageData = cached;
		this.mapWidth = cached.mapWidth();
		this.mapHeight = cached.mapHeight();
	}

	private static ProcessedImageData processImage(File image, BiomeLayerData data, ILogger logger)
	{
		final BufferedImage map;
		try
		{
			map = ImageIO.read(image);
		} catch (IOException e)
		{
			logger.error(LogCategory.CONFIGS, "FromImageLayer encountered a critical error", e);
			throw new RuntimeException("FromImageLayer encountered a critical error", e);
		}

		int width = map.getWidth(null);
		int height = map.getHeight(null);
		boolean continueNormal = data.imageSettings.getImageMode() == ImageMode.ContinueNormal;
		short fillBiome = (short) data.imageFillBiome;

		// Process image row-by-row using bulk getRGB to keep behavior identical
		// to original code while limiting peak memory to one row buffer (~40KB).
		// Uses short[] (200MB for 10K×10K) instead of int[] (400MB).
		// Rotation is handled at sample time, not here.
		short[] biomeMap = new short[height * width];
		int[] rowBuffer = new int[width];
		for (int z = 0; z < height; z++)
		{
			map.getRGB(0, z, width, 1, rowBuffer, 0, width);
			for (int x = 0; x < width; x++)
			{
				int color = rowBuffer[x] & 0x00FFFFFF;
				Integer biomeId = data.biomeColorMap.get(color);
				if (biomeId != null)
				{
					biomeMap[z * width + x] = (short) biomeId.intValue();
				} else
				{
					biomeMap[z * width + x] = continueNormal ? (short) -1 : fillBiome;
				}
			}
		}

		return new ProcessedImageData(biomeMap, width, height, data.imageSettings.getImageOrientation());
	}

	@Override
	public int sample(LayerSampleContext<?> context, ILayerSampler parent, int x, int z)
	{
		int bufX, bufZ;
		switch (this.imageSettings.getImageMode())
		{
			case Repeat:
				bufX = (x - this.imageSettings.getImageXOffset()) % this.mapWidth;
				bufZ = (z - this.imageSettings.getImageZOffset()) % this.mapHeight;
				if (bufX < 0) bufX += this.mapWidth;
				if (bufZ < 0) bufZ += this.mapHeight;
				return this.imageData.getBiome(bufX, bufZ);

			case Mirror:
				int bxq = (x - this.imageSettings.getImageXOffset()) % (2 * this.mapWidth);
				int bzq = (z - this.imageSettings.getImageZOffset()) % (2 * this.mapHeight);
				if (bxq < 0) bxq += 2 * this.mapWidth;
				if (bzq < 0) bzq += 2 * this.mapHeight;
				bufX = bxq % this.mapWidth;
				bufZ = bzq % this.mapHeight;
				if (bxq >= this.mapWidth) bufX = this.mapWidth - 1 - bufX;
				if (bzq >= this.mapHeight) bufZ = this.mapHeight - 1 - bufZ;
				return this.imageData.getBiome(bufX, bufZ);

			case ContinueNormal:
				bufX = x - this.imageSettings.getImageXOffset();
				bufZ = z - this.imageSettings.getImageZOffset();
				if (bufX < 0 || bufX >= this.mapWidth || bufZ < 0 || bufZ >= this.mapHeight)
				{
					return (parent != null) ? parent.sample(x, z) : this.data.imageFillBiome;
				}
				int biomeId = this.imageData.getBiome(bufX, bufZ);
				if (biomeId == -1)
				{
					return (parent != null) ? parent.sample(x, z) : this.data.imageFillBiome;
				}
				return biomeId;

			case FillEmpty:
				bufX = x - this.imageSettings.getImageXOffset();
				bufZ = z - this.imageSettings.getImageZOffset();
				if (bufX < 0 || bufX >= this.mapWidth || bufZ < 0 || bufZ >= this.mapHeight)
				{
					return this.data.imageFillBiome;
				}
				return this.imageData.getBiome(bufX, bufZ);
		}

		return parent.sample(x, z);
	}
}
