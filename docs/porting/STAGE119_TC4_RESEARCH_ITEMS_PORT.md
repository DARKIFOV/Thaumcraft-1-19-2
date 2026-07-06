# Stage119 — TC4 Research Item Registry Port

This stage starts moving the actual Thaumcraft 4 research/research-recipe **items** into the Forge 1.19.2 mod instead of keeping them only as book icons or raw text.

## What changed

- Registered 234 dedicated `thaumcraft:tc4_*` item ids for original TC4 item sprites used by research icons, recipe outputs, catalysts and components.
- Added `TC4ResearchComponentItem`, a runtime-safe 1.19.2 carrier item that stores the original TC4 source mapping in its tooltip.
- Added `TC4ResearchItems`, a de-metadata registry bridge for TC4 1.7.10 item metadata variants.
- Generated model JSON files for every registered research component item.
- Added lang entries so the items appear in the creative tab instead of showing raw translation keys.
- Added recipe-page registry hints: Thaumonomicon recipe pages now show `ConfigItems.*` expressions together with the resolved 1.19.2 `thaumcraft:tc4_*` registry id when known.

## Metadata split

TC4 1.7.10 used item metadata heavily. In 1.19.2 these were split into separate registry names:

```text
ConfigItems.itemResource variants: 19
ConfigItems.itemNugget variants: 15
ConfigItems.itemShard variants: 7
ConfigItems.itemWandCap variants: 9
ConfigItems.itemWandRod/staff variants: 17
ConfigItems.itemEldritchObject variants: 5
ConfigItems.itemGolemCore variants: 13
ConfigItems.itemGolemUpgrade variants: 7
```

## Important limitation

These items are now real 1.19.2 registry entries with TC4 textures and source mapping. Their special behavior is still ported item-by-item in later stages. This is intentional: adding behavior for every item at once would be less reliable than first making every research component addressable by registry id.
