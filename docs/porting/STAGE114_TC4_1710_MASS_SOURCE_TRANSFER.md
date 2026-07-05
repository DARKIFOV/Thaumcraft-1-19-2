# Stage114 — TC4 1.7.10 mass source transfer

This stage begins the strict TC4 port by importing the complete original TC4 1.7.10 source/assets into the 1.19.2 project as reference/source-of-truth material, while only compiling runtime-safe bridge code.

## Imported source/assets

- TC4 Java/source reference files: **859**
- TC4 asset files: **1079**
- TC4 block registry entries extracted from `ConfigBlocks.java`: **45**
- TC4 item fields/entries extracted from `ConfigItems.java`: **111**
- TC4 tile entities extracted from `ConfigBlocks.java`: **73**
- TC4 wand caps: **6**
- TC4 wand rods/staff rods: **18**
- TC4 OreDictionary entries: **42**
- TC4 research categories: **6**
- TC4 research item declarations: **201**
- TC4 recipes indexed from `ConfigRecipes.java`: **274**

## Why the original source is under docs

The original TC4 Java source targets Minecraft 1.7.10/FML/MCP. Copying it directly into `src/main/java` would break the Forge 1.19.2 build because classes like `cpw.mods.fml.*`, old MCP names, and 1.7.10 rendering/tile APIs no longer exist. Stage114 therefore imports the original source as strict reference and exposes generated maps in modern Java. Each real system should now be ported class-by-class from those source refs.

## Runtime-safe strict changes in this stage

- `WandCapType` now uses TC4 cap values: iron 1.1, gold 1.0, thaumium 0.9, void 0.8, plus copper/silver metadata.
- `WandRodType` now uses TC4 rod/staff capacities: wood 25, greatwood 50, primal staff 250, etc.
- `TC4OriginalBlockMap`, `TC4OriginalItemMap`, `TC4OriginalWandComponentMap`, `TC4OriginalResearchMap`, and `TC4OriginalAssetIndex` were generated from original TC4 source.
- Original TC4 `en_US.lang` keys are merged into modern `en_us.json` without deleting existing 1.19.2 keys.

## Next strict port target

The next meaningful gameplay stage should port one original subsystem end-to-end, not add fake placeholders. Recommended order:

1. `thaumcraft.api.aspects` + `ConfigAspects` object tags.
2. `ConfigItems` resource/shard/wand/focus items with metadata split into 1.19.2 registry entries.
3. `ConfigBlocks` core blocks: table, crucible, arcane workbench, jars, tubes, nodes.
4. `ConfigResearch` research graph/pages and Thaumonomicon GUI.
5. `ConfigRecipes` arcane/crucible/infusion recipes.
