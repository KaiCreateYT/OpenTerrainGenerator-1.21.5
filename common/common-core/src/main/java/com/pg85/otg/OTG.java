package com.pg85.otg;

/**
 * Main entry-point. Used to access OTGEngine.
 * OTGEngine is implemented and provided by the platform-specific
 * layer and holds any objects and methods used during a session.
 * For logging, use {@link com.pg85.otg.util.OTGLog} directly.
 */
public class OTG
{
	private static OTGEngine Engine;

	private OTG() { }

	// Engine

	public static void startEngine(OTGEngine engine)
	{
		if (Engine != null)
		{
			throw new IllegalStateException("Engine is already set.");
		}

		Engine = engine;
		engine.onStart();
	}

	public static OTGEngine getEngine()
	{
		if (Engine == null)
		{
			throw new IllegalStateException("Engine is not started.");
		}
		return Engine;
	}

	public static void stopEngine()
	{
		Engine.onShutdown();
		Engine = null;
	}

}
