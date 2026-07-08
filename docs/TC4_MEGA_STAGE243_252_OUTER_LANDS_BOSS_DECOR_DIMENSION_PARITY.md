# TC4 Mega Stage243-252 — Outer Lands boss/key/decor/dimension parity

Base archive: `STAGE233_242_TC4_OUTER_LANDS_MAZE_PERSISTENCE_BATCH_1192_PARITY`.
Target runtime: Minecraft Forge 1.19.2.
Reference source: `Thaumcraft4-1.7.10-master.zip`.

## Scope

This batch intentionally stays inside the original Thaumcraft 4 Outer Lands path:

- `GenCommon.placeBlock`
- `GenCommon.processDecorations`
- `GenBossRoom.generateRoom`
- `GenKeyRoom.generateRoom`
- `MazeHandler.generateEldritch`
- TC4 Eldritch dimension/chunk integration contract

## Stage243-252 changes

1. Added `TC4OuterLandsDecorationAdapter` as a deferred queue bridge for original `decoCommon`, `crabSpawner` and `decoUrn`.
2. `TC4OuterLandsGenCommonAdapter.placeBlock` now records decoration candidates during palette placement.
3. `TC4OuterLandsFeatureSelector` now calls `beginRoom()` before room generation and `processDecorations()` after the room is complete.
4. `processDecorations` now mirrors the original exposed-side checks before replacing queued blocks.
5. Added `TC4EldritchBlockVariantAdapter` to keep old `blockEldritch` metadata values named under 1.19.2 flattened blocks.
6. Boss rooms now use `placeBossRoomCell`, which follows the original cell-local `GenBossRoom` flow: generate the correct `Gen2x2` quadrant, then place `PAT_DOORWAY`.
7. Key rooms now use `placeKeyRoomCell`, which ports the original `a/b/c` loop bounds and `generateConnections(..., 3, true)` flow.
8. Key-room permanent item and guardian spawning remain in the original location contract: center `itemEldritchObject:2`, then 2/3/4 guardians by difficulty.
9. Added `TC4OuterLandsDimensionAdapter` for the future real Outer Lands dimension id while retaining the Overworld portal harness.
10. `TC4WorldgenRuntime` now separates portal-maze ticking from Overworld surface worldgen, so the Outer Lands maze path can run in a dedicated future dimension without spawning normal ores/trees.

## Intentional notes

- The old TC4 `decoUrn` ternary had an ordering bug where metadata `2` was unreachable in that specific decoration path. Stage243-252 preserves that ordering for `decoUrn`; nest-room direct loot blocks from earlier stages still use reachable 0/1/2 variants.
- `TC4EldritchBlockVariantAdapter` does not add new registry ids yet. It names and centralizes the metadata mapping until a later batch splits all `blockEldritch` decorative variants into distinct 1.19.2 blockstates/models.
- Gradle compile still requires internet access to download the Forge/Gradle wrapper dependencies in this sandbox.
