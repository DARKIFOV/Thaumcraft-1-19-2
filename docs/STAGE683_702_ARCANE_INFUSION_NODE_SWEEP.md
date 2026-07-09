# Stage683-702 — TC4 Arcane Workbench / Infusion Matrix / Aura Node Sweep

This stage continues from Stage663-682 and does not add new mechanics, new survival items, invented recipes, or invented research progression.

## Arcane Workbench

- Added `TC4ArcaneWorkbenchParity` as a shared 1.7.10 coordinate ledger for `GuiArcaneWorkbench` and `ContainerArcaneWorkbench`.
- Menu slot coordinates now read from the ledger: 3x3 grid, wand slot, output slot, player inventory and hotbar.
- Client GUI size/aspect icon coordinates now read from the same ledger.
- Arcane recipe preview no longer disappears only because the wand lacks vis. This is closer to the original table behavior: the output may be visible, but taking it is blocked server-side until the wand can pay.
- Removed the modern `Insufficient vis` text overlay from the GUI. The original-style signal remains the primal aspect cost icons and the output pickup gate.

## Infusion Matrix

- Added original-style snapshot NBT fields alongside the existing typed Forge state: `recipeingredients`, `recipeessentia`, and `recipeinstability`.
- These fields are migration/debug parity only and do not allow catalyst-only recipe matching.

## Aura Node / Revealer HUD

- Added `TC4AuraNodeHudParity` as a shared atlas ledger for `hud.png`, `node_bubble.png`, and `nodes.png`.
- In-world Aura Node renderer and Revealer HUD now use the same `32 frames / 64px cell / 2048px sheet / 256px bubble` constants and the same node-type strip mapping.

## Still not complete

This is still not 100% TC4 parity. Remaining work includes exact `ModelWand`/`ModelGoggles` UV parity, exact `GuiArcaneWorkbench` hover/click edge cases, full Infusion Matrix instability/failure/FX parity, and a larger sweep of all original `ConfigRecipes` and `ConfigResearch` materialized pages/gates.
