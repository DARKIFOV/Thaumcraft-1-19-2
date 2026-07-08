# TC4 Super-Mega Stage303-322 — Taintacle / Cultist Portal / Outer Lands dimension parity

Base archive: Stage283-302.
Target: Minecraft Forge 1.19.2.
Reference: Thaumcraft 4.2.3.5 / Minecraft 1.7.10.

## Ported in this batch

- Added dedicated `TaintacleEntity` equivalent for TC4 `EntityTaintacle`.
- Added dedicated `TaintacleSmallEntity` equivalent for TC4 `EntityTaintacleSmall`.
- Added entity registrations, attributes and client renderers for normal/small taintacles.
- Converted Giant Taintacle ranged anchor behavior to spawn actual small taintacles instead of only taint-fibre anchors.
- Added TC4 RenderCultistPortal frame/alpha/pulse adapters and public render-state getters.
- Added `TC4OuterLandsDimensionParity` for WorldProviderOuter constants.
- Added `TC4OuterLandsChunkProviderBridge` for ChunkProviderOuter chunk seed/populate contract.
- Added `TC4EldritchTileRenderProfile` for Eldritch Nothing/lock/cap/crystal render constants.

## Original parity anchors

- EntityTaintacle: size 0.66 x 3.0, XP 10, 50 HP, 7 attack, stationary movement, target scan based on height, ranged small-tentacle spawn.
- EntityTaintacleSmall: size 0.22 x 1.0, XP 0, 8 HP, 2 attack, 200 tick lifetime, no drops.
- RenderCultistPortal: 16-frame portal texture, pulse/hurt deformation, health alpha fade.
- WorldProviderOuter: name, no rain/ice/lightning, static celestial angle, ground level 50, sky color `10518688 * 0.15`.
- ChunkProviderOuter: chunk seed constants `341873128712L` and `132897987541L`.

## Notes

Gradle compile still needs network or a cached Gradle wrapper. Static guards and stage audits are used in the sandbox.
