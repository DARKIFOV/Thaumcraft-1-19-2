# TC4 Mega Stage223-232 - Outer Lands Maze / Loot / Room Graph Parity

Target: Minecraft/Forge 1.19.2 port, compared against `Thaumcraft4-1.7.10-master.zip`.

This batch replaces the single-stage cadence with a controlled mega-stage covering ten internal steps:

- Stage223: `MazeHandler` / `CellLoc` / `Cell` packed-short mirror.
- Stage224: NBT-compatible `cells` list save/load helpers.
- Stage225: deterministic 7x7 portal labyrinth generation bridge.
- Stage226: live `generateEldritch` feature dispatch by original ids `1..14`.
- Stage227: portal arena room-selector ring now delegates through MazeHandler instead of fixed rooms.
- Stage228: player-area tick bridge for already-created Outer Lands maze cells.
- Stage229: GenCommon decoration processing adapter for library/nest/passage rooms.
- Stage230: tighter lootbag potion family mapping.
- Stage231: enchanted-book lootbag output mapping.
- Stage232: batch audit / next-chat handoff prompt.

Important parity anchors from original TC4:

- `MazeHandler.labyrinth` remains a concurrent map from `CellLoc` to packed `short`.
- `Cell.pack()` keeps low bits for north/south/east/west/above/below and high byte for `feature`.
- Feature dispatch mirrors original `MazeHandler.generateEldritch`:
  - `1` portal;
  - `2..5` boss rooms;
  - `6` key room;
  - `7` nest room;
  - `8` library room;
  - `9..14/default` passage / 2x2 fillers.
- Lootbags still use `8 + rand(5)` output count from Stage222, now with stronger potion/enchantment mapping.

Known limitation: full Gradle compile was not possible in this sandbox because the Gradle wrapper needs to download Gradle. Static Java/resource/parity checks were used instead.
