# Stage154 Acceleration Strategy

We can accelerate the TC4 1.7.10 -> Forge 1.19.2 port, but only safely by batching exact source-of-truth data.

## Rules

- Do not invent recipes, items, mechanics, textures, GUI or behaviour.
- Batch only areas where original TC4 data has already been extracted or can be extracted automatically.
- Keep unresolved ore dictionary/material mappings explicitly unresolved.
- Each bulk pass still gets a focused audit so GitHub fails fast if a batch breaks parity.

## Faster stage cadence

Recommended from here:

- Stage155+156 together: remaining recipe resolver/materialization pass.
- Stage157+158 together: object/entity aspect database and scanner unlocks.
- Stage159+160 together: Thaumometer scanning UI/runtime and research trigger polish.
- Stage161+162 together: wand focus exact behaviour batch.
- Stage163+165 together: aura/node mechanics batch.

This should reduce the remaining estimate from very slow one-by-one work to about 38-63 more stages, assuming build errors are fixed immediately when they appear.
