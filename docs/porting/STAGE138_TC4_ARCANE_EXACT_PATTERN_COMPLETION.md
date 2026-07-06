# Stage138 — TC4 Arcane Exact Pattern Completion Pass

Goal: continue the Stage135–137 core loop work without jumping to a new system. This stage fixes an important parity gap in Arcane Workbench and Thaumonomicon recipe rendering: original Thaumcraft 4 shaped arcane recipes use character patterns such as `AAA / ASA / AAA`, where each symbol maps to an ingredient or catalyst.

## Problem fixed

Stage120–137 preserved the TC4 pattern rows and the list of components, but many old `ConfigRecipes` entries did not have a persisted explicit symbol map. The previous runtime could therefore fall back to loose ingredient checking, allowing recipes that should require eight repeated items to consume only one, or displaying the wrong ghost item in the book/workbench preview.

## What changed

- `ArcaneWorkbenchRecipe` now infers a deterministic TC4 symbol map from pattern rows, catalyst, and component list.
- `ArcaneWorkbenchBlockEntity` now validates and consumes shaped recipes by pattern slots and repeated symbols.
- Optional legacy catalyst slot is still supported, but only for the inferred catalyst symbol.
- `ArcaneWorkbenchContainerScreen` now uses the same inferred symbol map for ghost layout rendering.
- `TC4ResearchPageScreen` now uses the same idea for Thaumonomicon recipe pages, so recipe pages display catalyst/ingredients in the correct pattern positions.
- Added `scripts/tc4_stage138_arcane_pattern_audit.py` and workflow coverage for this stage.

## Example

Original TC4 style:

```text
AAA
ASA
AAA
A = primal shard
S = arrow catalyst
```

Stage138 now treats this as eight shard positions plus one catalyst position instead of one loose shard plus catalyst.

## Still to finish later

- Some recipes still need a richer materialized symbol map when the original source has more complex object arrays.
- Normal crafting and infusion pages should receive the same level of exact rendered slot parity.
- The research-note puzzle still needs exact old TC4 randomness/point tuning.
