# TC4 visual parity audit v11.63.23

`SOURCE_CONTRACT_COMPLETE` означает только замкнутый source/resource contract. `STATIC_PARTIAL` означает неполную систему. Runtime и визуальный PASS не присваиваются без артефактов.

- Item model JSON: **726**
- P0 source contracts complete: **6 / 7**
- P0 static partial: **1 / 7**
- P0 runtime visual PASS: **0 / 7**

## P0

### Essentia jars in item contexts

- Static: **SOURCE_CONTRACT_COMPLETE**
- Runtime visual: **NOT TESTED**
- TC4 contract: ItemJarFilledRenderer: temporary TileJar, liquid/aspect/filter/label and jar shell

### Aura node item policy

- Static: **SOURCE_CONTRACT_COMPLETE**
- Runtime visual: **NOT TESTED**
- TC4 contract: ItemNodeRenderer or the normal player-facing Node in a Jar path

### Bone Bow

- Static: **SOURCE_CONTRACT_COMPLETE**
- Runtime visual: **NOT TESTED**
- TC4 contract: ItemBowBoneRenderer plus 0/8/13-tick icon stages and faster ItemBowBone projectile curve

### Traveling Trunk

- Static: **SOURCE_CONTRACT_COMPLETE**
- Runtime visual: **NOT TESTED**
- TC4 contract: ItemTrunkSpawnerRenderer + EntityTravelingTrunk, ModelTrunk, inventory/owner/lid behavior

### Crimson cultists

- Static: **SOURCE_CONTRACT_COMPLETE**
- Runtime visual: **NOT TESTED**
- TC4 contract: RenderCultist ModelBiped base, original skin, role equipment/armor layers and 1.25 leader scale

### Fortress Armor

- Static: **SOURCE_CONTRACT_COMPLETE**
- Runtime visual: **NOT TESTED**
- TC4 contract: ModelFortressArmor with custom 128x64 geometry, three NBT masks, goggles and set-dependent ornaments

### Outer Lands

- Static: **STATIC_PARTIAL**
- Runtime visual: **NOT TESTED**
- TC4 contract: ChunkProviderOuter, MazeHandler rooms/passages/loot, portal progression, Warden and persistent return path
