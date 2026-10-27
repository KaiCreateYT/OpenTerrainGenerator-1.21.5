package com.pg85.otg.gen.biome.layers.util;

import java.util.Random;

import com.pg85.otg.gen.noise.PerlinNoiseSampler;
import com.pg85.otg.util.helpers.MathHelper;

public class CachingLayerContext implements LayerSampleContext<CachingLayerSampler>
{
	private final int cacheCapacity;
	private final PerlinNoiseSampler noiseSampler;
	private final long worldSeed;
	// CRITICAL: localSeed must be ThreadLocal to prevent race conditions
	// when multiple worker threads call initSeed/nextInt concurrently!
	private final ThreadLocal<Long> localSeed = ThreadLocal.withInitial(() -> 0L);

	public CachingLayerContext(int cacheCapacity, long seed, long salt)
	{
	  this.worldSeed = addSalt(seed, salt);
	  this.noiseSampler = new PerlinNoiseSampler(new Random(seed));
	  this.cacheCapacity = cacheCapacity;
	}

	public CachingLayerSampler createSampler(LayerOperator layerOperator)
	{
	  return new CachingLayerSampler(this.cacheCapacity, layerOperator);
	}

	public CachingLayerSampler createSampler(LayerOperator layerOperator, CachingLayerSampler cachingLayerSampler)
	{
	  return new CachingLayerSampler(Math.min(1024, cachingLayerSampler.getCapacity() * 4), layerOperator);
	}

	public CachingLayerSampler createSampler(LayerOperator layerOperator, CachingLayerSampler cachingLayerSampler, CachingLayerSampler cachingLayerSampler2)
	{
	  return new CachingLayerSampler(Math.min(1024, Math.max(cachingLayerSampler.getCapacity(), cachingLayerSampler2.getCapacity()) * 4), layerOperator);
	}

	public void initSeed(long x, long y)
	{
	  long l = this.worldSeed;
	  l = MathHelper.mixSeed(l, x);
	  l = MathHelper.mixSeed(l, y);
	  l = MathHelper.mixSeed(l, x);
	  l = MathHelper.mixSeed(l, y);
	  this.localSeed.set(l);
	}

	public int nextInt(int bound)
	{
	  long seed = this.localSeed.get();
	  int i = (int)Math.floorMod(seed >> 24, (long)bound);
	  this.localSeed.set(MathHelper.mixSeed(seed, this.worldSeed));
	  return i;
	}

	public PerlinNoiseSampler getNoiseSampler()
	{
	  return this.noiseSampler;
	}

	private static long addSalt(long seed, long salt)
	{
	  long l = MathHelper.mixSeed(salt, salt);
	  l = MathHelper.mixSeed(l, salt);
	  l = MathHelper.mixSeed(l, salt);
	  long m = MathHelper.mixSeed(seed, l);
	  m = MathHelper.mixSeed(m, l);
	  m = MathHelper.mixSeed(m, l);
	  return m;
	}
}
