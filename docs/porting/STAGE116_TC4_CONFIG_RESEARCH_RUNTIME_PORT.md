# Stage116 — TC4 ConfigResearch Runtime Port

This stage moves beyond sparse source inventory and ports the original Thaumcraft 4 research graph from `thaumcraft/common/config/ConfigResearch.java` into runtime-safe Forge 1.19.2 structures.

## What was extracted

- Original research categories: **6**
- Original research entries: **201**
- Entries with pages: **199**
- Total page references: **591**
- Entries with normal parents: **166**
- Entries with hidden parents: **16**
- Entries with original warp: **20**

## Category counts

- `ALCHEMY`: 35
- `ARTIFICE`: 50
- `BASICS`: 19
- `ELDRITCH`: 16
- `GOLEMANCY`: 39
- `THAUMATURGY`: 42

## Runtime changes

- `ResearchEntry` now has TC4 fields: category, display column/row, complexity, aspect tags, hidden parents, siblings, flags, page text keys, page types, recipe keys, triggers and warp.
- `TC4ResearchRuntimeBridge` exposes the extracted original graph to 1.19.2 code.
- `TC4ResearchCategoryRegistry` exposes category icons/backgrounds from `initCategories()`.
- `ResearchRegistry` now seeds original TC4 research first, then keeps old rebuild/addon entries only if their keys do not collide.
- `OriginalResearchBridge.costsFor()` now uses original TC4 aspect tags when present instead of the old key-name heuristic.

## Important porting rule

The old `ConfigResearch.java` source is still kept as reference, but it is not executed directly because it depends on Minecraft 1.7.10 MCP/FML classes. Stage116 converts its data into 1.19.2-safe Java and JSON.
