package com.pg85.otg.customobject.util;

public class BO3Enums
{
	// The spawn height
	public enum SpawnHeightEnum
	{
		randomY,
		highestBlock,
		highestSolidBlock
	}

	// How an object should be extended to a surface
	public enum ExtrudeMode
	{
		None,
		BottomDown,
		TopUp
	}

	// What to do when outside the source block
	public enum OutsideSourceBlock
	{
		dontPlace,
		placeAnyway
	}
}
