# Stage120 — TC4 ConfigRecipes Materialization Pass 1

This stage continues from Stage119 without preparing a GitHub upload. The goal is to move closer to **actual TC4 1.7.10 gameplay recipes** on Forge 1.19.2.

## What changed

- Parsed the Stage119 `TC4RecipeRuntimeBridge` generated from original `ConfigRecipes.java`.
- Generated real data files for resolvable TC4 recipes:
  - `42` crucible/alchemy recipes in `data/thaumcraft/thaumcraft_alchemy/tc4_*.json`.
  - `73` arcane workbench recipes in `data/thaumcraft/thaumcraft_arcane_workbench/tc4_*.json`.
  - `31` infusion recipes in `data/thaumcraft/thaumcraft_infusion/tc4_*.json`.
  - `8` vanilla smelting recipes in `data/thaumcraft/recipes/tc4_*.json`.
- Added `TC4RecipeItemResolver` for old `ConfigItems.*` / MCP item field mapping.
- Upgraded `ArcaneWorkbenchRecipe` so generated recipes preserve and load original TC4 aspect costs.
- Updated arcane crafting execution so it consumes the original aspect costs from the wand instead of the temporary fixed Ordo-only cost.

## Why some recipes are still deferred

`79` records remain unresolved/deferred. They are mostly recipes whose result or ingredients depend on:

- old block metadata variants not yet split into 1.19.2 registry blocks;
- wildcard metas such as `32767`;
- loop variables such as `a`;
- old OreDictionary entries that need tag-based recipes instead of one fixed item.

They are not ignored: see `tc4_stage120_unresolved_recipes.json`.

## Stage boundary

This is a recipe materialization pass, not a release stage. The user requested to keep building locally and try GitHub again around Stage140–159.
