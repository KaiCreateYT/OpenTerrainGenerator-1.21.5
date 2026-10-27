package com.pg85.otg.customobject.structures.bo4.smoothing;

import com.pg85.otg.customobject.structures.bo4.BO4CustomStructureCoordinate;

record BlockCoordsAndNeighbours(BO4CustomStructureCoordinate bO3, int blockX, short blockY, int blockZ,
								boolean smoothInDirection1, boolean smoothInDirection2, boolean smoothInDirection3,
								boolean smoothInDirection4) {
}