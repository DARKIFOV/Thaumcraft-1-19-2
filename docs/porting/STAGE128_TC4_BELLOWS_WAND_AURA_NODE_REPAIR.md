# Stage128 — TC4 Bellows + Wand/Aura Node Repair

Goal: continue from Stage127 and close the next TC4 parity gap instead of moving to unrelated systems.

## Crucible / Bellows

Original TC4 references:
- `TileCrucible#getBellows()` counts adjacent `TileBellows` on horizontal sides.
- `TileCrucible#spillRemnants()` drains water/aspects and spills flux/taint remnants.
- `BlockMetalDevice` calls `spillRemnants()` on wand interaction.

Ported to 1.19.2:
- Added real `thaumcraft:bellows` block.
- Bellows uses a horizontal facing blockstate.
- Crucible scans adjacent bellows that face the crucible.
- Each bellows increases crucible heating speed.
- Crucible stores/syncs bellows count.
- Sneak-right-click with any wand calls `spillRemnants()`.
- Spill clears water/aspects and pushes flux into the world as `flux_goo` / `flux_gas`.

## Wand / Aura Node

Original TC4 behavior target:
- Wands store and spend primal vis.
- Nodes supply primal aspects and regenerate slowly toward their base values.
- Wand/node interaction should not use all compound aspects as wand vis.

Ported to 1.19.2:
- Wands now charge from Aura Nodes using only primal aspects: Aer, Terra, Ignis, Aqua, Ordo, Perditio.
- Aura Nodes reject compound-aspect wand drain.
- Aura Nodes now store base aspect values and slowly regenerate toward those base primal amounts.
- Node scanned/stability state is now correctly saved/loaded.
- Right-clicking an Aura Node with a wand draws primal vis and plays TC4 wand feedback.

## Remaining non-ideal parts

This stage repairs the core interaction loop, but more stages are still needed:
- full TC4 wand focus system;
- wand recharge animation/beam renderer;
- exact node generation from biome/dimension tables;
- node bullying/merging and advanced stabilizer/transducer behavior;
- arcane furnace bellows support.
