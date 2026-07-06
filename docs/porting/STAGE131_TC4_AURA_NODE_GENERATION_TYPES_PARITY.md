# Stage131 — TC4 Aura Node Generation / Type / Modifier Parity

This stage continues from Stage130 and focuses on aura nodes rather than moving to a new unrelated system.

## Implemented

- Added TC4-style node modifiers:
  - Normal
  - Bright
  - Pale
  - Fading
- Expanded node type behavior:
  - Normal
  - Pure
  - Tainted
  - Hungry
  - Dark
  - Unstable
- Added deterministic aura node profile generation from world position.
- Added early natural node seeding around loaded player chunks.
- Added server-side node behavior:
  - Pure nodes cleanse taint and remove harmful effects.
  - Tainted nodes spread tainted soil if unstabilized.
  - Hungry nodes pull items/entities and absorb thrown item aspects into the node.
  - Dark nodes apply darkness/weakness around the node.
  - Unstable nodes shuffle primal vis and lose stability if not stabilized.
- Added node modifier persistence and client sync.
- Improved Thaumometer/Goggles scan text:
  - modifier + type
  - current/base vis
  - stability
- Improved node renderer:
  - type-specific sprite texture selection
  - modifier-scaled node size
  - fading nodes render dimmer
  - dark/unstable color support

## Notes

This is not yet a complete worldgen port of TC4's original aura system. It is now much closer than Stage130 because nodes are no longer just a fixed placed block with primal vis storage. The next pass should finish node manipulation blocks: stabilizer, transducer, node jar capture/release, and energized node conversion.
