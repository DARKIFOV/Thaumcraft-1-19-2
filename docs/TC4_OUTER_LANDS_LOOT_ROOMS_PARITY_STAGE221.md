# Stage221 — TC4 Outer Lands loot/room parity for 1.19.2

Source checked: `Thaumcraft4-1.7.10-master.zip`.

Implemented in this stage:
- Replaced the Eldritch Crab half-health helmet fallback with a registered crimson/cultist plate chest item, matching the original `ConfigItems.itemChestCultistPlate` drop path.
- Added TC4 crimson/cultist plate armor item bridge for helm/chest/legs/boots, tied to original item texture names.
- Added `TC4LootBlock` for Outer Lands urn/crate behaviour with metadata-equivalent `variant=0..2` rarity.
- Added `TC4OuterLandsLootAdapter.generateLoot(int, RandomSource)` as a 1.19.2 mirror of `Utils.generateLoot` with common/uncommon/rare pools and rarity-scaled gear chance.
- Updated `GenNestRoom` adapter to place urn/crate loot blocks instead of stone placeholders.
- Added `TC4OuterLandsRoomAdapter` containing direct 1.19.2-safe adapters for TC4 `Gen2x2` and `GenPassage` loop bounds/palette codes.

Static parity anchors:
- `EntityEldritchCrab#attackEntityFrom` helm break: `itemChestCultistPlate`.
- `BlockLoot#getDrops`: `1 + md + rand(3)` calls to `Utils.generateLoot(md, rand)`.
- `GenNestRoom` loot placement: 15% chance in -5..5 area, urn/crate split, metadata rarity.
- `Gen2x2` and `GenPassage` loop boundaries preserved as explicit code.

Not yet complete:
- Exact TC4 `WeightedRandomLoot` registration table is approximated through available 1.19.2 items and TC4 research-item proxies.
- Full Outer Lands maze graph/chunk integration is still partial; Stage222 should connect `Gen2x2`/`GenPassage` adapters into the live room selector.
