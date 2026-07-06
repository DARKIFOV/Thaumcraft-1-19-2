# Stage123 — TC4 Infusion Altar Structure / Instability / FX Parity

Stage123 continues from Stage122 and focuses on making infusion feel and behave closer to Thaumcraft 4 1.7.10 instead of being only a recipe timer.

## Source-of-truth used

- `docs/source_refs/tc4_1710_original_source/thaumcraft/common/tiles/TileInfusionMatrix.java`
- TC4 `validLocation()`
- TC4 `getSurroundings()` symmetry penalty model
- TC4 `craftCycle()` 21-slot weighted instability switch

## Runtime changes

- Center catalyst pedestal is now checked at `matrix.below(2)`, matching TC4.
- Added `thaumcraft:infusion_pillar`.
- Valid infusion structure now requires the four diagonal TC4 pillar positions at matrixY-2.
- `Arcane Stone Bricks` still count as temporary compatibility pillars until the exact pillar model/tile renderer is complete.
- Pedestal scan radius moved closer to TC4: wide 8-block horizontal scan around the center pedestal.
- Stabilizer scan now uses a wider TC4-style 12-block scan around the matrix.
- Symmetry is now a TC4-like instability penalty, where asymmetrical pedestals/components increase instability and mirrored pairs reduce it.

## Instability changes

Added `InfusionInstabilityEvents`, adapted from the original TC4 weighted switch:

```text
case 0,2,10,13 -> eject item
case 6,17       -> eject item + flux goo
case 1,11       -> eject item + flux gas
case 3,8,14     -> zap one target
case 5,16       -> harm one target
case 12         -> zap all targets
case 19         -> delete/eject style component event
case 7          -> flux gas style component loss
case 4,15       -> pedestal explosion event
case 18         -> harm all targets
case 9          -> altar explosion
case 20         -> warp event
```

## Files changed

- `InfusionAltarStructure.java`
- `InfusionStructureReport.java`
- `InfusionInstabilityEvents.java`
- `InfusionProcessHelper.java`
- `InfusionMatrixBlockEntity.java`
- `ThaumcraftMod.java`
- blockstate/model/loot/texture resources for `infusion_pillar`

## Validation

```text
python scripts/java_syntax_guard.py — OK
python scripts/github_ci_guard.py — OK
python scripts/github_static_audit.py — OK
zip integrity test — OK
```

## Remaining work

This is closer to TC4, but not the final infusion layer yet. Remaining work includes exact `TileInfusionPillar` visual model, full runic matrix renderer animation, original sound event mapping, and exact flux goo/gas block behavior.
