# Stage140 — TC4 Golemancy + Whole-Port Checkpoint

Stage140 starts the next broad parity block after the research/workbench/thaumonomicon stages. The focus is Golemancy, because TC4 golems are a full gameplay branch, not just one placeholder entity.

## Implemented in this pass

- Added explicit TC4-style golem material runtime profiles:
  - straw
  - wood
  - tallow
  - clay
  - flesh
  - stone
  - iron
  - thaumium
- Added explicit TC4-style golem core runtime modes:
  - gather
  - fill
  - empty
  - guard
  - harvest
  - lumber
  - use
  - sorting
- `GolemCoreItem` now stores selected material/core in NBT and spawns golems with that profile.
- Shift-use controls were added so the current single 1.19.2 item can emulate old TC4 metadata variants until every original placer/core item is fully split.
- `ThaumGolemEntity` now persists material/core, applies body stats, and runs per-core behavior.
- Added practical TC4-like runtime behaviors:
  - gather items
  - deliver inventory to nearby containers near home
  - fallback delivery to owner
  - guard owner/home area
  - harvest mature crops
  - cut logs
- Renderer now visually changes body block by golem material.

## Still not complete TC4

This stage is a big Golemancy foundation pass, but the following original TC4 details still need later exact parity:

- full seal GUI and seal filters;
- every original golem upgrade item and trait stack;
- golem animation/model renderer from TC4 instead of block-proxy renderer;
- advanced core behavior for fill/empty/use/sorting beyond the initial runtime skeleton;
- ward/permission edge cases for block manipulation;
- exact itemGolemPlacer and itemGolemCore metadata split into native 1.19.2 registry items.

## Why this was done now

The port had already reached a working core loop for Thaumonomicon, Research Table, Arcane Workbench, Crucible, Infusion, Wands and Aura Nodes. Golemancy was the next major TC4 system that needed real runtime behavior instead of placeholders.
