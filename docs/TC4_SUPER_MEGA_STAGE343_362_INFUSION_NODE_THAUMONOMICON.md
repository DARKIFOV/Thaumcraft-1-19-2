# TC4 Stage343-362 — Infusion, Aura Node HUD, Thaumonomicon cleanup

Base: Stage323-342 HOTFIX3.  Target: strict original Thaumcraft 4 parity on Forge Minecraft 1.19.2.

## What changed

- Infusion Matrix now stores a locked catalyst id and legacy-compatible `recipeobject` debug NBT once crafting starts.
- Infusion Matrix validates the original recipe lock against catalyst + component pedestals before the first component is consumed. This prevents catalyst-only or wrong-recipe drift for recipes that share the same catalyst.
- Component pull order is routed through `TC4InfusionRuntime.orderedComponentPullList`, preserving the original `ConfigRecipes` component order.
- The infusion overlay reads live pending essentia, component count and instability from the matrix block entity.
- Added `TC4RevealerHudAdapter`, a client-side Forge 1.19.2 adapter for the original TC4 revealer/goggles Aura Node HUD.
- Node HUD now uses real Aura Node block entity data and original aspect icon texture paths.
- Aura Node scan interaction now checks actual registered items only: Thaumometer, Goggles of Revealing, Helmet of Revealing.
- Thaumonomicon entry page no longer uses modern Minecraft `Button` widgets inside the original book. It uses manual page/back hotzones over the original texture.
- Removed unused duplicate `com.darkifov.thaumcraft.item.ThaumometerItem`; the active registered Thaumometer class is unchanged.

## Explicit adapters, not new mechanics

- `TC4InfusionRuntime.orderedComponentPullList` is only a 1.19.2 adapter for original recipe component order.
- `lockedCatalystId` / `recipeobject` are NBT-safe state for the original matrix recipe lock concept.
- `TC4RevealerHudAdapter` is a client render adapter for already-existing node data.
- `TC4ResearchPageScreen` manual hotzones are a screen adapter to avoid modern widgets inside the copied original book texture.

## Still temporary / remaining drift

- Infusion visuals still need exact TC4 beam, item travel and instability rendering.
- Aura Node visuals still need exact original noise/alpha/size matching.
- Goggles overlay still needs full block/entity aspect display parity beyond node HUD.
- Thaumonomicon needs a full original ConfigResearch audit for every page, icon, coordinate, parent, hidden parent, sibling, warp and recipe gate.
- Wand focus effects and projectile behaviour still need the remaining original focus classes as source of truth.
