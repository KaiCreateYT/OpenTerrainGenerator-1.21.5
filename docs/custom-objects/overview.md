# Custom Objects

Custom objects are user-defined structures that OTG places in the world during terrain generation. OTG supports three formats:

| Format | Use Case | Max Size | Branching | Collision Detection |
|--------|----------|----------|-----------|---------------------|
| [BO2](../custom-objects/bo3.md) | Legacy, simple structures | Unlimited | No | Basic % threshold |
| [BO3](../custom-objects/bo3.md) | Trees, buildings, decorations | ~30x30 | Yes (simple) | No |
| [BO4](../custom-objects/bo4.md) | Large structures, villages | 16x16 per piece | Yes (advanced tree) | Bounding box |

## Where to Place Object Files

Custom objects go in the preset's `CustomObjects/` folder:

```
DimensionPresets/MyPreset/
├── DimensionPresetConfig.ini
├── Biomes/
└── CustomObjects/
    ├── MyTree.bo3
    ├── Village_Center.bo4
    └── old_bush.bo2
```

## How Objects Are Spawned

Objects are referenced in biome `.bc` config files using resource directives:

### CustomObject

Spawns during chunk decoration. Uses the object's own `Frequency` and `Rarity` settings.

```
CustomObject(MyTree,MyRock,MyBush)
```

### CustomStructure

Spawns as a structure with branching support. Ignores the object's Frequency/Rarity — uses the chance value specified here.

```
CustomStructure(Village_Center,50,Tower,75)
```

Requires `CustomStructureType: BO3` or `CustomStructureType: BO4` in DimensionPresetConfig.ini.

### Tree

Spawns as a tree — supports sapling growth.

```
Tree(10,MyOak,60,MySpruce,40,GRASS_BLOCK,DIRT)
```

The object must have `Tree: true` in its config.

## BO3 vs BO4

The main architectural difference:

- **BO3** uses seed-based placement — objects are force-spawned at calculated positions. Simpler, faster, but no collision detection between structures.
- **BO4** uses a plotting system — structures are pre-calculated across undecorated chunks before spawning. Supports collision detection, smoothing areas, and complex branching hierarchies. Each piece is limited to 16x16 blocks.

Set `CustomStructureType` in DimensionPresetConfig.ini to choose which system is active for CustomStructure resources. A preset can only use one type at a time.
