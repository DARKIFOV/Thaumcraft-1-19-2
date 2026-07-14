# TC4 construction recipes and JEI audit — 11.62.62

**Status:** PASS (static audit)

## Measured result

| Check | Result |
|---|---:|
| Release recipe entries checked | 40 |
| New functional block items | 26 |
| Vanilla shaped recipes | 87 |
| Vanilla shapeless recipes | 38 |
| Standard recipes visible to JEI crafting category | 125 |
| Item models audited | 713 |
| Missing or unresolved item resources | 0 |
| Semantic model problems/warnings | 0 / 0 |
| JSON resources parsed | 1865 |

## High-risk parity assertions

- Essentia Phial uses pattern `C/G G/ G`, clay ball and three glass, with output count 8.
- Table uses `minecraft:wooden_slabs` and `minecraft:planks` tags.
- Each primal crystal cluster consumes six identical shards.
- Balanced Crystal Cluster consumes all six primal shards.
- Six non-original reverse cluster recipes are absent.
- All 16 tallow-candle variants are included in `thaumcraft:tallow_candles`.
- Every new placeable has a blockstate, item model and self-drop loot table.
- Arcane, Alchemy and Infusion JEI category registrations remain present.

## Static limitation

The Forge Java compiler did not run because Gradle Wrapper could not resolve `services.gradle.org`. PASS therefore means resource/registry/recipe/source-contract validation, not a compiled or runtime-tested release.
