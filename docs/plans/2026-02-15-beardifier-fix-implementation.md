# Beardifier Fix Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Port vanilla 1.21.1 Beardifier math to OTG's noise system to fix floating terrain near structures (villages).

**Architecture:** Move beard contribution before the `d/2 - d³/24` density compression, port vanilla's TerrainAdjustment types (BEARD_THIN/BEARD_BOX/BURY/ENCAPSULATE), add jigsaw junction support with 0.4x scaling. The core formula (`calculateNoiseWeight`) is mathematically identical to vanilla — the fix is about WHEN it's applied and supporting multiple adjustment types.

**Tech Stack:** Java 21, Gradle, Architectury (Fabric active, NeoForge disabled but maintained)

**Design doc:** `docs/plans/2026-02-15-beardifier-fix-design.md`

---

### Task 1: Add OTGTerrainAdjustment enum

**Files:**
- Create: `common/common-util/src/main/java/com/pg85/otg/util/gen/OTGTerrainAdjustment.java`

**Step 1: Create the enum**

```java
package com.pg85.otg.util.gen;

/**
 * Mirrors vanilla's TerrainAdjustment for OTG's noise system.
 * Controls how structures modify surrounding terrain density.
 */
public enum OTGTerrainAdjustment {
    /** Fills under structure, thins terrain above (villages, pillager outposts) */
    BEARD_THIN,
    /** Fills under structure using box distance for Y, not raw offset */
    BEARD_BOX,
    /** Buries structure in terrain using radial falloff (ancient cities, trial chambers) */
    BURY,
    /** Like BURY but at half scale with 0.8x multiplier */
    ENCAPSULATE
}
```

**Step 2: Commit**

```bash
git add common/common-util/src/main/java/com/pg85/otg/util/gen/OTGTerrainAdjustment.java
git commit -m "feat: add OTGTerrainAdjustment enum for vanilla-compatible terrain adaptation"
```

---

### Task 2: Add JigsawJunctionData record

**Files:**
- Create: `common/common-util/src/main/java/com/pg85/otg/util/gen/JigsawJunctionData.java`

**Step 1: Create the record**

```java
package com.pg85.otg.util.gen;

/**
 * Holds jigsaw junction point data for beard contribution.
 * Junctions are connection points between structure pieces,
 * receiving 0.4x beard scaling (vs 0.8x for full pieces).
 */
public record JigsawJunctionData(int sourceX, int sourceGroundY, int sourceZ) {
}
```

**Step 2: Commit**

```bash
git add common/common-util/src/main/java/com/pg85/otg/util/gen/JigsawJunctionData.java
git commit -m "feat: add JigsawJunctionData record for jigsaw junction beard support"
```

---

### Task 3: Extend JigsawStructureData with TerrainAdjustment and maxY

**Files:**
- Modify: `common/common-util/src/main/java/com/pg85/otg/util/gen/JigsawStructureData.java`

**Step 1: Add fields and update constructor**

Replace the entire file with:

```java
package com.pg85.otg.util.gen;

/**
 * Helper class to hold jigsaw structure data for density calculations.
 */
public class JigsawStructureData {
    public final int minX;
    public final int minY;
    public final int minZ;
    public final int maxX;
    public final int maxY;
    public final int delta;
    public final int maxZ;
    public final OTGTerrainAdjustment terrainAdjustment;

    public JigsawStructureData(
            int minX, int minY, int minZ,
            int maxX, int maxY, int delta, int maxZ,
            OTGTerrainAdjustment terrainAdjustment
    ) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.delta = delta;
        this.maxZ = maxZ;
        this.terrainAdjustment = terrainAdjustment;
    }
}
```

Key changes:
- Removed `useDelta` boolean — delta is always applied (vanilla always uses it for jigsaw pieces)
- Removed `sourceX`, `groundY`, `sourceZ` — moved to `JigsawJunctionData`
- Added `maxY` field (needed for BEARD_BOX Y distance calculation)
- Added `OTGTerrainAdjustment terrainAdjustment`

**Step 2: Commit**

```bash
git add common/common-util/src/main/java/com/pg85/otg/util/gen/JigsawStructureData.java
git commit -m "refactor: extend JigsawStructureData with TerrainAdjustment and maxY"
```

---

### Task 4: Add helper methods to MathHelper

**Files:**
- Modify: `common/common-util/src/main/java/com/pg85/otg/util/helpers/MathHelper.java`

**Step 1: Add clampedMap and length methods**

Add these methods (before `fastInverseSqrt` at line 131):

```java
/**
 * Maps value from [min1, max1] to [min2, max2], clamped to [min2, max2].
 * Equivalent to vanilla Mth.clampedMap().
 */
public static double clampedMap(double value, double min1, double max1, double min2, double max2) {
    double t = clamp((value - min1) / (max1 - min1), 0.0, 1.0);
    return lerp(t, min2, max2);
}

/**
 * Returns the length of a 3D vector. Equivalent to vanilla Mth.length().
 */
public static double length(double x, double y, double z) {
    return Math.sqrt(x * x + y * y + z * z);
}

/**
 * Returns the squared length of a 3D vector.
 */
public static double lengthSquared(double x, double y, double z) {
    return x * x + y * y + z * z;
}
```

Also add a `clamp(double, double, double)` overload if it doesn't exist (currently only `float` and `int` versions exist). Check first — it's referenced in `OTGChunkGenerator.java:677` as `MathHelper.clamp` with doubles. If it's missing:

```java
public static double clamp(double check, double min, double max) {
    return check > max ? max : (Math.max(check, min));
}
```

**Step 2: Commit**

```bash
git add common/common-util/src/main/java/com/pg85/otg/util/helpers/MathHelper.java
git commit -m "feat: add clampedMap, length, lengthSquared to MathHelper"
```

---

### Task 5: Rewrite OTGChunkGenerator beard system

This is the core task. All changes are in `common/common-core/src/main/java/com/pg85/otg/gen/OTGChunkGenerator.java`.

**Files:**
- Modify: `common/common-core/src/main/java/com/pg85/otg/gen/OTGChunkGenerator.java`

**Step 1: Replace NOISE_WEIGHT_TABLE with BEARD_KERNEL**

Replace lines 59-69 (the `NOISE_WEIGHT_TABLE` static initializer):

```java
// Vanilla 1.21.1-compatible beard kernel: stores ONLY the Gaussian component.
// exp(-(x² + (y+0.5)² + z²) / 16.0)
// The directional factor is computed separately per-invocation to support
// BEARD_BOX where kernel Y differs from raw Y offset.
private static final float[] BEARD_KERNEL = make(
        new float[24 * 24 * 24], (array) -> {
            for (int i = 0; i < 24; ++i) {
                for (int j = 0; j < 24; ++j) {
                    for (int k = 0; k < 24; ++k) {
                        array[i * 24 * 24 + j * 24 + k] = (float) computeBeardKernel(j - 12, k - 12, i - 12);
                    }
                }
            }
        }
);
```

**Step 2: Replace calculateNoiseWeight and getNoiseWeight with vanilla-compatible methods**

Remove `calculateNoiseWeight()` (lines 190-208) and `getNoiseWeight()` (lines 173-188). Replace with:

```java
/**
 * Computes the Gaussian kernel value for the beard lookup table.
 * Only the radial falloff — directional factor is computed separately.
 * Matches vanilla Beardifier.computeBeardContribution().
 */
private static double computeBeardKernel(int x, int y, int z) {
    double dy = (double) y + 0.5;
    double lengthSq = MathHelper.lengthSquared((double) x, dy, (double) z);
    return Math.pow(Math.E, -lengthSq / 16.0);
}

private static boolean isInKernelRange(int i) {
    return i >= 0 && i < 24;
}

/**
 * Computes beard contribution for a structure piece.
 * Matches vanilla Beardifier.getBeardContribution(i, j, k, l).
 *
 * @param dx      X distance from structure bounding box (0 if inside)
 * @param kernelY Y value for kernel lookup (varies by TerrainAdjustment type)
 * @param dz      Z distance from structure bounding box (0 if inside)
 * @param rawY    Raw Y offset from structure bottom + delta (always used for directional factor)
 */
private static double getBeardContribution(int dx, int kernelY, int dz, int rawY) {
    int kx = dx + 12;
    int ky = kernelY + 12;
    int kz = dz + 12;
    if (isInKernelRange(kx) && isInKernelRange(ky) && isInKernelRange(kz)) {
        double d = (double) rawY + 0.5;
        double lengthSq = MathHelper.lengthSquared((double) dx, d, (double) dz);
        double f = -d * MathHelper.fastInverseSqrt(lengthSq / 2.0) / 2.0;
        return f * (double) BEARD_KERNEL[kz * 24 * 24 + kx * 24 + ky];
    }
    return 0.0;
}

/**
 * Computes bury contribution for BURY/ENCAPSULATE terrain adjustment.
 * Matches vanilla Beardifier.getBuryContribution().
 * Returns 1.0 at center, linearly fading to 0.0 at distance 6.
 */
private static double getBuryContribution(double dx, double dy, double dz) {
    double length = MathHelper.length(dx, dy, dz);
    return MathHelper.clampedMap(length, 0.0, 6.0, 1.0, 0.0);
}
```

**Step 3: Update populateNoise() signature**

Change the method signature to accept junctions:

```java
public void populateNoise(
        OTGWorldInfo worldHeight,
        ChunkBuffer buffer,
        ChunkCoordinate chunkCoord,
        ObjectList<JigsawStructureData> structures,
        ObjectList<JigsawJunctionData> junctions,
        Random random
)
```

Add import for `JigsawJunctionData`:
```java
import com.pg85.otg.util.gen.JigsawJunctionData;
import com.pg85.otg.util.gen.OTGTerrainAdjustment;
```

Add junction iterator at line 548 (after structure iterator):
```java
ObjectListIterator<JigsawStructureData> structureIterator = structures.iterator();
ObjectListIterator<JigsawJunctionData> junctionIterator = junctions.iterator();
```

**Step 4: Rewrite the beard application block (lines 682-694)**

Replace the entire for loop (lines 684-694) with the new vanilla-compatible logic:

```java
// ---- Beard: apply BEFORE d/2 - d³/24 transformation ----
// In vanilla 1.18+, Beardifier contributions are added to raw density.
// OTG's d/2-d³/24 compression was applied BEFORE beard in 1.16 style,
// making negative beard too powerful. Now beard is added first.
double beardContribution = 0.0;
while (structureIterator.hasNext()) {
    structure = structureIterator.next();
    int dx = Math.max(0, Math.max(structure.minX - realX, realX - structure.maxX));
    int dz = Math.max(0, Math.max(structure.minZ - realZ, realZ - structure.maxZ));
    int baseY = structure.minY + structure.delta;
    int rawYOffset = realY - baseY;

    switch (structure.terrainAdjustment) {
        case BEARD_THIN:
            beardContribution += getBeardContribution(dx, rawYOffset, dz, rawYOffset) * 0.8;
            break;
        case BEARD_BOX:
            int boxDistY = Math.max(0, Math.max(baseY - realY, realY - structure.maxY));
            beardContribution += getBeardContribution(dx, boxDistY, dz, rawYOffset) * 0.8;
            break;
        case BURY:
            beardContribution += getBuryContribution((double) dx, (double) rawYOffset / 2.0, (double) dz);
            break;
        case ENCAPSULATE:
            int encapY = Math.max(0, Math.max(structure.minY - realY, realY - structure.maxY));
            beardContribution += getBuryContribution((double) dx / 2.0, (double) encapY / 2.0, (double) dz / 2.0) * 0.8;
            break;
    }
}
structureIterator.back(structures.size());

// Jigsaw junctions: 0.4x scaling (connection points between structure pieces)
while (junctionIterator.hasNext()) {
    JigsawJunctionData junction = junctionIterator.next();
    int jdx = realX - junction.sourceX();
    int jdy = realY - junction.sourceGroundY();
    int jdz = realZ - junction.sourceZ();
    beardContribution += getBeardContribution(jdx, jdy, jdz, jdy) * 0.4;
}
junctionIterator.back(junctions.size());

// Add beard to pre-transformation density, clamp, then apply d/2-d³/24
density += beardContribution;
density = MathHelper.clamp(density, -1.0, 1.0);
density = density / 2.0D - density * density * density / 24.0D;
```

This replaces the old for-loop (lines 684-694) which was:
```java
for (
    density = density / 2.0D - density * density * density / 24.0D;
    structureIterator.hasNext();
    density += getNoiseWeight(structureX, structureY, structureZ) * 0.8D
) { ... }
structureIterator.back(structures.size());
```

Note: also remove the now-unused variable declarations for `structureX`, `structureY`, `structureZ` (lines 617-619) — they're replaced by local variables inside the beard block.

**Step 5: Commit**

```bash
git add common/common-core/src/main/java/com/pg85/otg/gen/OTGChunkGenerator.java
git commit -m "feat: port vanilla 1.21.1 Beardifier math to OTG noise system

Move beard contribution before d/2-d³/24 density compression to fix
floating terrain near structures. Port TerrainAdjustment types
(BEARD_THIN/BOX/BURY/ENCAPSULATE) and jigsaw junction support."
```

---

### Task 6: Update platform generators (Fabric + NeoForge)

**Files:**
- Modify: `platforms/fabric/src/main/java/com/pg85/otg/fabric/gen/OTGFabricChunkGenerator.java` (lines 407-454)
- Modify: `platforms/neoforge/src/main/java/com/pg85/otg/neoforge/gen/OTGNeoForgeChunkGenerator.java` (lines 422-470)

Both files get identical changes. For each:

**Step 1: Add imports**

```java
import com.pg85.otg.util.gen.JigsawJunctionData;
import com.pg85.otg.util.gen.OTGTerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pools.JigsawJunction;
```

**Step 2: Rewrite structure collection in fillFromNoise()**

Replace the structure collection block. The key changes:
1. Remove the `&& s.terrainAdaptation() != TerrainAdjustment.BURY` filter — we now handle BURY
2. Map vanilla `TerrainAdjustment` to `OTGTerrainAdjustment`
3. Collect jigsaw junctions from RIGID pieces
4. Pass both lists to `populateNoise()`
5. Also handle non-jigsaw structure pieces (vanilla does this too, line 57-58 of Beardifier.java)

```java
ObjectList<JigsawStructureData> structures = new ObjectArrayList<>(10);
ObjectList<JigsawJunctionData> junctions = new ObjectArrayList<>(32);
ChunkPos pos = chunkAccess.getPos();
int chunkMinX = pos.getMinBlockX();
int chunkMinZ = pos.getMinBlockZ();

for (StructureStart start : structureManager.startsForStructure(pos,
        s -> s.terrainAdaptation() != TerrainAdjustment.NONE)) {
    if (!start.isValid()) continue;
    TerrainAdjustment vanillaAdjustment = start.getStructure().terrainAdaptation();
    OTGTerrainAdjustment otgAdjustment = mapTerrainAdjustment(vanillaAdjustment);

    for (StructurePiece piece : start.getPieces()) {
        if (!piece.isCloseToChunk(pos, 12)) continue;

        if (piece instanceof PoolElementStructurePiece poolPiece) {
            if (poolPiece.getElement().getProjection() == StructureTemplatePool.Projection.RIGID) {
                BoundingBox box = piece.getBoundingBox();
                structures.add(new JigsawStructureData(
                        box.minX(), box.minY(), box.minZ(),
                        box.maxX(), box.maxY(),
                        poolPiece.getGroundLevelDelta(), box.maxZ(),
                        otgAdjustment));

                // Collect jigsaw junctions from RIGID pieces
                for (JigsawJunction junction : poolPiece.getJunctions()) {
                    int jx = junction.getSourceX();
                    int jz = junction.getSourceZ();
                    if (jx > chunkMinX - 12 && jz > chunkMinZ - 12
                            && jx < chunkMinX + 15 + 12 && jz < chunkMinZ + 15 + 12) {
                        junctions.add(new JigsawJunctionData(
                                jx, junction.getSourceGroundY(), jz));
                    }
                }
            }
        } else {
            // Non-jigsaw structure pieces (e.g. strongholds, mineshafts)
            BoundingBox box = piece.getBoundingBox();
            structures.add(new JigsawStructureData(
                    box.minX(), box.minY(), box.minZ(),
                    box.maxX(), box.maxY(), 0, box.maxZ(),
                    otgAdjustment));
        }
    }
}
```

**Step 3: Add the mapping helper method** (as a private static method in the class)

```java
private static OTGTerrainAdjustment mapTerrainAdjustment(TerrainAdjustment vanilla) {
    return switch (vanilla) {
        case BEARD_THIN -> OTGTerrainAdjustment.BEARD_THIN;
        case BEARD_BOX -> OTGTerrainAdjustment.BEARD_BOX;
        case BURY -> OTGTerrainAdjustment.BURY;
        case ENCAPSULATE -> OTGTerrainAdjustment.ENCAPSULATE;
        default -> OTGTerrainAdjustment.BEARD_THIN; // fallback for NONE (shouldn't reach here)
    };
}
```

**Step 4: Update populateNoise() call**

```java
this.internalGenerator.populateNoise(otgWorldInfo, buffer,
        buffer.getChunkCoordinate(), structures, junctions, random);
```

**Step 5: Remove the old TODO comment** (lines 414-421 in Fabric, 430-437 in NeoForge) — it's been resolved.

**Step 6: Commit**

```bash
git add platforms/fabric/src/main/java/com/pg85/otg/fabric/gen/OTGFabricChunkGenerator.java
git add platforms/neoforge/src/main/java/com/pg85/otg/neoforge/gen/OTGNeoForgeChunkGenerator.java
git commit -m "feat: collect TerrainAdjustment types and jigsaw junctions in platform generators"
```

---

### Task 7: Update ShadowChunkGenerator if it calls populateNoise

**Files:**
- Check: `common/common-core/src/main/java/com/pg85/otg/gen/ShadowChunkGenerator.java` (or similar)

**Step 1: Find all callers of populateNoise**

```bash
grep -rn "populateNoise" common/ platforms/
```

If `ShadowChunkGenerator` or any other class calls `populateNoise()`, update it to pass an empty junctions list:

```java
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
// ...
generator.populateNoise(worldHeight, buffer, chunkCoord, structures,
        new ObjectArrayList<>(), random);
```

**Step 2: Commit** (only if changes were needed)

```bash
git add -A && git commit -m "fix: update all populateNoise callers with new junction parameter"
```

---

### Task 8: Build and verify compilation

**Step 1: Build the project**

```bash
./gradlew build
```

Expected: BUILD SUCCESSFUL

**Step 2: Fix any compilation errors**

Common issues to watch for:
- Missing imports for `OTGTerrainAdjustment`, `JigsawJunctionData`
- Old `JigsawStructureData` constructor calls with wrong parameter count
- `NOISE_WEIGHT_TABLE` references that weren't updated to `BEARD_KERNEL`

**Step 3: Commit fixes if needed**

```bash
git add -A && git commit -m "fix: resolve compilation issues from beardifier refactor"
```

---

### Task 9: Final cleanup

**Step 1: Remove dead code**

In `OTGChunkGenerator.java`:
- Remove `NOISE_WEIGHT_TABLE` if still referenced anywhere (search for it)
- Remove old `calculateNoiseWeight()` if still present
- Remove old `getNoiseWeight()` if still present
- Clean up unused variable declarations (`structureX`, `structureY`, `structureZ`)

**Step 2: Verify build**

```bash
./gradlew build
```

**Step 3: Final commit**

```bash
git add -A && git commit -m "chore: remove dead code from old beard system"
```
