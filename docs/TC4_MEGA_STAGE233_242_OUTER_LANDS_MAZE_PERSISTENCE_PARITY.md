# TC4 Mega Stage 233–242 — Outer Lands Maze/Persistence Parity

Target: Minecraft/Forge 1.19.2. Reference: original Thaumcraft 4.2.3.5 / 1.7.10 source.

## Scope

This mega-stage replaces the simplified Stage223–232 Outer Lands maze ring with a closer port of the original TC4 `MazeGenerator`, `MazeThread`, `MazeHandler`, and `GenCommon.PAT_CONNECT` behavior.

## Internal stage map

- Stage233: replace deterministic 7×7 ring with TC4-style `MazeGenerator` DFS/backtracking algorithm.
- Stage234: port `MazeThread.run` coordinate copy contract into synchronous 1.19.2-safe generation.
- Stage235: preserve TC4 width/height selection using odd maze sizes `15 + rand(8) * 2` for altar/portal generated mazes.
- Stage236: add `TC4OuterLandsMazeSavedData` as the 1.19.2 equivalent of TC4 `labyrinth.dat` / `Data.cells`.
- Stage237: persist portal origins and generated cells to avoid destructive re-generation after reload.
- Stage238: move TC4 feature-id dispatch into `TC4OuterLandsFeatureSelector` while preserving `MazeHandler.generateEldritch` behavior.
- Stage239: replace simple doorway carving with exact `GenCommon.PAT_CONNECT` numeric pattern and depth/justTheTip loops.
- Stage240: add dedicated invisible `EldritchNothingBlock` for TC4 `blockEldritchNothing` instead of creative-visible barrier placeholders.
- Stage241: update live player-area generation to expand around the player’s current maze cells instead of only around the portal center.
- Stage242: add mega-stage audit/report/handoff prompt for continuation.

## Notes

The stage still avoids importing 1.7.10 classes directly. Old concepts are expressed through 1.19.2-safe adapters and persistent NBT-compatible payloads.

Gradle compile was not run in this sandbox because the wrapper needs to download Gradle from `services.gradle.org`.
