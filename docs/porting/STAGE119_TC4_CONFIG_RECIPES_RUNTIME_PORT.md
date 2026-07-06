# Stage119 — TC4 ConfigRecipes Runtime Port

Stage119 moves the port forward from research data to recipe parity.

## Source of truth

Original file:

`docs/source_refs/tc4_1710_original_source/thaumcraft/common/config/ConfigRecipes.java`

This file cannot be compiled directly on Forge 1.19.2 because it depends on 1.7.10 MCP/FML classes such as `GameRegistry`, `ItemStack` metadata constructors, old `Blocks.field_*` names, OreDictionary recipe classes, and the original Thaumcraft API recipe objects.

## What was extracted

Generated files:

- `src/main/resources/data/thaumcraft/tc4_original_recipes_stage119.json`
- `src/main/resources/data/thaumcraft/tc4_original_recipes_stage119_summary.json`
- `docs/porting/tc4_original_recipes_stage119.csv`
- `src/main/java/com/darkifov/thaumcraft/recipe/TC4RecipeRuntimeBridge.java`

Extracted counts:

- 295 total recipe records
- 54 compound/wand-trigger entries
- 49 crucible recipes
- 84 shaped arcane recipes
- 5 shapeless arcane recipes
- 63 infusion crafting recipes
- 24 infusion enchantment recipes
- 8 smelting entries
- 8 normal crafting entries

## Runtime behavior

`TC4RecipeRuntimeBridge` now exposes original recipe records by recipe key and research key. Thaumonomicon recipe pages use this bridge, so pages no longer show the old placeholder text saying that `ConfigRecipes` is still required.

Each bridged recipe preserves:

- original recipe key used by `ConfigResearch.recipes.put(...)`
- recipe type
- research unlock key
- result expression
- catalyst/central item expression
- infusion instability value
- TC4 aspect cost list, normalized to the 1.19.2 `Aspect` enum names where possible
- crafting pattern and component expressions
- source registration method

## Remaining work for full 1.7.10 parity

This stage makes TC4 recipe data visible and queryable in 1.19.2 runtime. The next stage must convert metadata-heavy TC4 item expressions such as `ConfigItems.itemResource, meta 15` and old MCP names such as `Items.field_151042_j` into actual modern `ResourceLocation`/`ItemStack` adapters so arcane/crucible/infusion blocks can craft every original recipe, not just display them.
