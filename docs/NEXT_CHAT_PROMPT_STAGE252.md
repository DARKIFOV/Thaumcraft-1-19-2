Continue the Thaumcraft 4 → Minecraft Forge 1.19.2 port from:

`thaumcraft_legacy_rebuild_STAGE243_252_TC4_OUTER_LANDS_BOSS_DECOR_DIMENSION_BATCH_1192_PARITY.zip`

Reference sources available in the previous conversation:

- `Thaumcraft4-1.7.10-master.zip` — original TC4 source/decompiled reference.
- `ThaumicTinkerer-main.zip`, `ThaumicEnergistics-AE2-RV6.zip` — addons for later, not the current core priority.

Current policy:

- Continue in mega-stage batches of 5-10 internal stages per ZIP.
- Keep target strictly Minecraft/Forge 1.19.2.
- Do not import 1.7.10 APIs; port behavior through 1.19.2-safe adapters.
- Add a new `docs/NEXT_CHAT_PROMPT_STAGE###.md` in every output archive.
- Keep comparing against the original TC4 1.7.10 implementation and avoid invented mechanics.

Recently completed in Stage243-252:

- Version bumped to `2.52.0`.
- Restored TC4-style deferred Outer Lands decoration queues: `decoCommon`, `crabSpawner`, `decoUrn`.
- `GenCommon.placeBlock` now records decoration candidates; `FeatureSelector` processes them after each room.
- Added `TC4EldritchBlockVariantAdapter` for named `blockEldritch` metadata parity.
- Replaced center-box boss/key room logic with cell-local `GenBossRoom` / `GenKeyRoom` bridges.
- Boss room now generates the original `Gen2x2` quadrant before `PAT_DOORWAY`.
- Key room now uses original loop bounds and `generateConnections(..., 3, true)` plus permanent key item and guardians.
- Added `TC4OuterLandsDimensionAdapter`; portal-maze ticking is now separate from Overworld surface worldgen.

Recommended next mega-stage: Stage253-262.

Suggested focus:

1. Split `blockEldritch` metadata variants into real 1.19.2 blockstates/models where useful.
2. Add exact `TileEldritchLock`, `TileEldritchCap`, `TileEldritchCrystal`, `TileEldritchTrap` equivalents.
3. Port `GenLibraryRoom`, `GenNestRoom`, `GenPassage`, and `Gen2x2` deeper from original loop structures.
4. Continue baked model parity for Guardian/Warden/Golem/Crab using original `ModelEldritchGuardian`, `ModelEldritchGolem`, `ModelEldritchCrab` part trees.
5. Add a real Outer Lands dimension bootstrap if feasible, or a safe datapack-backed placeholder with runtime guard.
6. Extend the audit script and keep running `java_syntax_guard.py`, `github_static_audit.py`, `github_ci_guard.py`, and the latest stage audits.
