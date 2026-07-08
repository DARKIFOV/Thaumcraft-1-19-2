# Stage222 — TC4 Outer Lands Room Selector + Lootbag Parity (1.19.2)

Base: Stage221. Target: Minecraft/Forge 1.19.2. Reference: Thaumcraft4 1.7.10.

## Ported in this stage
- Routed the live portal arena builder through a Stage222 Outer Lands room selector ring.
- Connected `Gen2x2`, `GenPassage`, `GenLibraryRoom`, and `GenNestRoom` adapters to live placement rather than leaving them as isolated helpers.
- Tightened loot generation around the original `Utils.generateLoot(rarity, rand)` and `ThaumcraftApi.addLootBagItem` weighted-pool contract.
- Added right-click opening behavior for flattened TC4 lootbag items, with original count `8 + rand(5)`.
- Kept key-room permanent item / guardian spawn behavior from Stage218 and Stage221.
- Fixed Stage221 nest-room loot md selection so rare variant can actually be selected.

## Original anchors
- `thaumcraft/common/lib/utils/Utils.java#generateLoot`
- `thaumcraft/common/items/ItemLootBag.java#func_77659_a`
- `thaumcraft/common/config/Config.java` lootbag registrations around lines 600–630
- `thaumcraft/common/lib/world/dim/Gen2x2.java`
- `thaumcraft/common/lib/world/dim/GenPassage.java`
- `thaumcraft/common/lib/world/dim/GenLibraryRoom.java`
- `thaumcraft/common/lib/world/dim/GenNestRoom.java`

## Not yet complete
- Full real Outer Lands dimension/chunk provider graph is still not complete.
- Potion metadata, enchanted book output, and bauble gear are represented with 1.19.2-safe flattened equivalents / NBT breadcrumbs.
- Guardian/Golem baked model trees still need full cube-by-cube parity.
