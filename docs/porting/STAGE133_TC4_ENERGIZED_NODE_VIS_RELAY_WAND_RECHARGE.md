# Stage133 — TC4 Energized Node / Vis Relay / Wand Recharge Parity

Stage133 continues the aura-node work from Stage132. The goal is to make energized nodes functional, not just visual.

## Implemented

- Added `thaumcraft:vis_relay` as TC4-style relay hardware.
- Added `VisRelayBlock` with wand interaction.
- Added `AuraVisRelayNetwork` runtime:
  - searches nearby relay blocks around the player;
  - traverses short relay chains;
  - finds a linked energized `AuraNodeBlockEntity`;
  - moves only primal vis into wands;
  - drains the energized node as the source of truth;
  - plays relay particles and TC4 wand sound feedback.
- Added passive wand recharge near relay networks.
- Kept direct click charging on Aura Nodes from Stage128/132.

## TC4 parity target

Original Thaumcraft 4 uses stabilized/transduced nodes as a source of vis power. The 1.19.2 port now follows the same design direction:

```text
Aura Node + Stabilizer + Redstone-powered Transducer
→ Energized Node
→ Vis Relay chain
→ wand recharge
```

## Current limitations

- Relay beams are particle-based, not a dedicated renderer yet.
- Relay chains are block-adjacent and capped for performance.
- Full TC4 energized node devices are still not complete.

## Next target

Stage134 should continue with exact Vis Relay FX/beam renderer and begin Arcane Workbench/wand-vis consumption parity.
