# Porting progress — v8.22

Estimated TC4 parity completion: **73%**.

Estimated remaining: **27%**.

## What this estimate means

This is based on functional parity blocks, not raw file count:

- Research / Thaumonomicon / unlock flow
- Arcane Workbench / wand crafting
- Infusion Matrix / instability / component and essentia flow
- Aura nodes / scanning / goggles HUD
- Essentia, jars, tubes, alchemy and thaumatorium
- Golems and seals/tasks
- Eldritch, taint, worldgen and boss structures
- Rendering, GUI, assets and save/load edge cases

## v8.22 contribution

v8.22 is a compact edge-case batch, so the estimate moves from **72%** to **73%**.

Completed in this batch:

1. Infusion travelling components now lock to a concrete ItemStack snapshot while travelling from a source pedestal.
2. Infusion travelling component state is saved and loaded for reload-safe craftCycle parity.
3. Arcane Workbench vanilla crafting now uses recipe-provided remaining items instead of only per-item container fallback.
4. Thaumometer scan constants now match TC4's 25 tick / 10 block behavior ledger.
5. Aura node scan text and Revealer HUD now share aspect ordering.
6. Goggles/Thaumometer HUD node targeting now uses a 10 block scan ray before fallback.

## Remaining high-risk areas

- Thaumometer still needs a deeper hold-to-scan lifecycle, not only constants/range guards.
- Infusion needs a stricter component spec ledger for same item id with different damage/NBT across the full pending list.
- Research and Thaumonomicon still need another fake/stub/drift cleanup pass.
- Essentia/alchemy/tube suction need more TC4 corner-case audits.
- Golem behavior and seals need larger runtime parity passes.
