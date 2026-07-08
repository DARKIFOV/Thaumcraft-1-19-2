# TC4 Legacy Rebuild — Stage323–342 Super Mega Original Parity Cleanup

Base archive: `thaumcraft_legacy_rebuild_STAGE303_322_TC4_SUPER_MEGA_TAINTACLE_DIMENSION_BATCH_1192_PARITY.zip`.
Target: Forge Minecraft 1.19.2.
Rule: strict original Thaumcraft 4 parity. No new mechanics, fake effects, fake recipes, fake GUI/progression, or renamed behaviour were intentionally added in this batch.

## What changed in Stage323–342

### Taintacle renderer parity cleanup
- Replaced the flat temporary Taintacle billboard-style render path with a segmented numeric 1.19.2 adapter that follows the original TC4 `ModelTaintacle` intent more closely.
- The renderer now uses normal/small segment counts, tapered prism radii, wave/flail offsets, and original taintacle texture paths instead of a generic plane.
- This is explicitly marked as a Forge 1.19.2 adapter because the original 1.7.10 model renderer cannot be copied into modern Mojang/Forge render classes verbatim.

Files:
- `src/main/java/com/darkifov/thaumcraft/client/render/TC4TaintacleRenderer.java`

### Outer Lands live chunk-provider bridge
- Added `TC4OuterLandsLivePopulateAdapter` and wired it into the existing server tick worldgen runtime.
- The adapter safely calls the Stage303–322 `TC4OuterLandsChunkProviderBridge.populateLikeTC4(...)` around players in Outer Lands chunks.
- Populated chunks are remembered so the 1.19.2 tick bridge does not repeatedly spam the same population pass.

Files:
- `src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsLivePopulateAdapter.java`
- `src/main/java/com/darkifov/thaumcraft/world/TC4WorldgenRuntime.java`

### Research note visual parity
- Replaced rigid straight note-connection lines with a deterministic sagging/thread-like adapter.
- Endpoints and research graph data remain unchanged; only the rendering path was adjusted to better match the original TC4 hanging string look.
- This does not alter research completion, grid coordinates, hidden parents, siblings, or page data.

Files:
- `src/main/java/com/darkifov/thaumcraft/client/screen/ResearchNoteScreen.java`

### Research table GUI parity pass
- Kept the original `guiresearchtable2.png` layout as the background source.
- Restored the visible TC4-style known-aspect palette, selected aspect slots, combine button region, preview aspect, and page arrows on the original coordinate grid.
- Combination still routes through the existing 1.19.2 network packet instead of inventing a new client-only mechanic.

Files:
- `src/main/java/com/darkifov/thaumcraft/client/screen/ResearchTableContainerScreen.java`

### Research table block/model cleanup
- Replaced the placeholder cube-style table models with element models using top slab + four legs.
- The research-table model now points at the original `tablequill` texture copy.

Files:
- `src/main/resources/assets/thaumcraft/models/block/table.json`
- `src/main/resources/assets/thaumcraft/models/block/research_table.json`
- `src/main/resources/assets/thaumcraft/textures/block/tc4/tablequill.png`

### Original texture remap cleanup
Copied original TC4 texture sources into the active 1.19.2 runtime paths for key broken-looking items:
- Thaumonomicon
- Goggles of Revealing
- Research Notes
- Scribing Tools
- Thaumometer
- Research Table table/quill top

Files include:
- `src/main/resources/assets/thaumcraft/textures/item/thaumonomicon.png`
- `src/main/resources/assets/thaumcraft/textures/item/goggles_of_revealing.png`
- `src/main/resources/assets/thaumcraft/textures/item/research_note.png`
- `src/main/resources/assets/thaumcraft/textures/item/scribing_tools.png`
- `src/main/resources/assets/thaumcraft/textures/item/thaumometer.png`

### Recipe/materialization cleanup
- Fixed active focus recipes so the normal TC4 foci point to the real runtime focus ids instead of older `tc4_*` mirror items.
- Fixed shard/quicksilver/charm resolver mappings so regenerated/materialized recipes do not drift back to mirror ingredient ids.
- Active arcane/alchemy/infusion recipe JSON was cleaned for the standard focus and shard ids.
- `ItemFocusHellbat` is left as a source-mapped original TC4 placeholder until its actual focus class is ported; it is not treated as a new mechanic in this batch.

Files:
- `src/main/java/com/darkifov/thaumcraft/recipe/TC4RecipeItemResolver.java`
- `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/*.json`
- `src/main/resources/data/thaumcraft/thaumcraft_alchemy/*.json`
- `src/main/resources/data/thaumcraft/thaumcraft_infusion/*.json`

### Duplicate item cleanup gate
- Added the duplicated old focus/goggles mirror ids to the creative/loot/report quarantine guard.
- The old ids are not force-deleted from registry code because deleting ids can break old saves and generated source-mapping data; they are hidden/quarantined instead until the port no longer needs compatibility aliases.

Files:
- `src/main/java/com/darkifov/thaumcraft/porting/TC4RegistryGarbageGuard.java`

### Audit/cleanup checks
Added a Stage323–342 audit script that checks:
- project version bump to 3.42.0 while preserving Stage322 compatibility markers;
- Taintacle numeric adapter is present and the old quad/billboard call is gone;
- Outer Lands live populate adapter exists and is wired;
- research note thread adapter exists;
- research table GUI uses original background + aspect palette/combine controls;
- key original textures exist in active runtime paths;
- active standard focus/shard recipes no longer point to old mirror ids;
- duplicate mirror foci/goggles are quarantined.

File:
- `scripts/tc4_stage323_342_super_mega_original_parity_cleanup_audit.py`

## Drift notes

- Forge 1.19.2 adapters remain necessary for renderers, menus, networking, and server tick wiring because original 1.7.10 classes cannot be dropped in directly.
- Adapters in this batch are marked as adapters and preserve original data/coordinates/texture identity wherever possible.
- No new research keys, new progression, new recipes, or new fake focus effects were intentionally introduced.

## Not finished yet

These are still not complete original TC4 parity and should be next-stage priorities:
1. Infusion Matrix GUI/renderer/tick/instability parity.
2. Aura Node GUI/overlay and real node interaction parity.
3. Goggles of Revealing overlay/render checks.
4. Full Thaumonomicon page/icon/recipe-gate texture audit.
5. Remaining recipe materialization from original `ConfigRecipes` with no mirror-id drift.
6. Wand focus class behaviour and projectile entities for every original focus class.
7. Research table container/copy behaviour deep parity against original `GuiResearchTable` and `ContainerResearchTable`.
8. Golems, wand/aura/crucible/infusion/taint/eldritch/worldgen final parity passes.

## Estimated remaining batches

A full strict TC4 parity port is still large. After Stage323–342, expect roughly 18–25 more large parity batches if each batch continues to fix complete systems rather than small cosmetic fragments.
