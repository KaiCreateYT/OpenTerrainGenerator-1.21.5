package com.pg85.otg.customobject.structures;

import com.pg85.otg.customobject.bofunctions.EntityFunction;
import com.pg85.otg.interfaces.IWorldGenRegion;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.bo3.Rotation;

public class EntitiesManager
{
	public void spawnEntities(IWorldGenRegion worldGenRegion, EntityFunction<?>[] entityDataInObject, CustomStructureCoordinate coordObject, ChunkCoordinate chunkCoordinate, CustomStructureCache structureCache)
	{
        for (EntityFunction<?> entityFunction : entityDataInObject) {
            EntityFunction<?> newEntityData = entityFunction.createNewInstance();

            if (coordObject.getRotation() != Rotation.NORTH) {
                int rotations = 0;
                // How many counter-clockwise rotations have to be applied?
                if (coordObject.getRotation() == Rotation.WEST) {
                    rotations = 1;
                } else if (coordObject.getRotation() == Rotation.SOUTH) {
                    rotations = 2;
                } else if (coordObject.getRotation() == Rotation.EAST) {
                    rotations = 3;
                }

                // Apply rotation
                if (rotations == 0) {
                    newEntityData.x = entityFunction.x;
                    newEntityData.z = entityFunction.z;
                }
                if (rotations == 1) {
                    newEntityData.x = entityFunction.z;
                    newEntityData.z = -entityFunction.x + 15;
                }
                if (rotations == 2) {
                    newEntityData.x = -entityFunction.x + 15;
                    newEntityData.z = -entityFunction.z + 15;
                }
                if (rotations == 3) {
                    newEntityData.x = -entityFunction.z + 15;
                    newEntityData.z = entityFunction.x;
                }
                newEntityData.y = coordObject.getY() + entityFunction.y;

                newEntityData.x = coordObject.getX() + newEntityData.x;
                newEntityData.z = coordObject.getZ() + newEntityData.z;

                newEntityData.name = entityFunction.name;
                newEntityData.resourceLocation = entityFunction.resourceLocation;
                newEntityData.groupSize = entityFunction.groupSize;
                newEntityData.nameTagOrNBTFileName = entityFunction.nameTagOrNBTFileName;
                newEntityData.namedBinaryTag = entityFunction.namedBinaryTag;
                newEntityData.rotation = rotations;

                worldGenRegion.spawnEntity(newEntityData);
            } else {

                newEntityData.y = coordObject.getY() + entityFunction.y;

                newEntityData.x = coordObject.getX() + entityFunction.x;
                newEntityData.z = coordObject.getZ() + entityFunction.z;

                newEntityData.name = entityFunction.name;
                newEntityData.resourceLocation = entityFunction.resourceLocation;
                newEntityData.groupSize = entityFunction.groupSize;
                newEntityData.nameTagOrNBTFileName = entityFunction.nameTagOrNBTFileName;
                newEntityData.namedBinaryTag = entityFunction.namedBinaryTag;
                newEntityData.rotation = 0;

                worldGenRegion.spawnEntity(newEntityData);
            }
        }
	}
}
