Продолжи перенос Thaumcraft 4 на Minecraft/Forge 1.19.2 строго от архива:
`thaumcraft_legacy_rebuild_STAGE223_232_TC4_OUTER_LANDS_MAZE_LOOT_BATCH_1192_PARITY.zip`.

Сверочный оригинал: `Thaumcraft4-1.7.10-master.zip`.

Уже сделано к Stage232:
- Stage206-222: infusion matrix, runic shield, fortress armor/masks, champion mobs, Eldritch Warden/Golem/Crab, Outer Lands room/loot beginnings.
- Mega Stage223-232: добавлены `TC4OuterLandsMazeCell`, `TC4OuterLandsMazeCellLoc`, `TC4OuterLandsMazeGenerator`, `TC4OuterLandsMazeHandler`, `TC4OuterLandsDecorationAdapter`, `TC4LootPotionEnchantAdapter`.
- `TC4OuterLandsBossRoomPlacer.placeRoomSelectorRing` теперь использует MazeHandler / packed Cell feature graph.
- `TC4WorldgenRuntime.tickPlayerArea` вызывает live maze tick для созданных портал-лабиринтов.
- Lootbag potion/enchantment branches стали ближе к TC4 `Utils.generateLoot`.

Следующий mega-stage лучше делать как Stage233-242 в одном архиве:
1. перенести больше реального `MazeGenerator` алгоритма: dead ends, feature placement, `above/below` bits;
2. добавить точные `GenBossRoom`, `GenKeyRoom`, `GenLibraryRoom`, `GenNestRoom` block patterns вместо bridge shell;
3. продолжить `GenCommon.processDecorations` и `PAT_CONNECT`/`PAT_DOORWAY` parity;
4. заменить временные barrier placements для `blockEldritchNothing` на dedicated 1.19.2 block;
5. добавить real Outer Lands dimension/chunk feature integration;
6. продолжить baked model tree для Guardian/Warden/Golem/Crab;
7. прогнать все stage audits и добавить `docs/NEXT_CHAT_PROMPT_STAGE242.md`.

Не отходи от оригинала TC4 1.7.10. Не добавляй новые механики без TC4-аналога. Сохраняй Forge 1.19.2 совместимость: не использовать `NBTTag*`, `func_*`, old World/Block API.
