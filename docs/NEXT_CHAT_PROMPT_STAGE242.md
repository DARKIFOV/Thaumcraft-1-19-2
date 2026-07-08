Продолжи порт Thaumcraft 4 на Minecraft/Forge 1.19.2 строго по оригиналу 1.7.10.

База: `thaumcraft_legacy_rebuild_STAGE233_242_TC4_OUTER_LANDS_MAZE_PERSISTENCE_BATCH_1192_PARITY.zip`.
Сверочный оригинал: `Thaumcraft4-1.7.10-master.zip`.

Уже сделано к Stage242:
- версия проекта `2.42.0`;
- Stage223–232 уже добавил Outer Lands room selector / lootbag / Gen2x2 / GenPassage / GenLibraryRoom / GenNestRoom adapters;
- Stage233–242 заменил упрощённый maze на TC4-style `MazeGenerator` DFS/backtracking;
- добавлен `MazeThread.run` copy contract в 1.19.2-safe `generateAt`;
- добавлен `TC4OuterLandsMazeSavedData` как SavedData-эквивалент `labyrinth.dat`;
- `cells`, `portalOrigins`, `generatedCells` сохраняются в NBT;
- `GenCommon.PAT_CONNECT` перенесён как точная 11×11 матрица;
- doorway generation использует original depth/justTheTip loops;
- добавлен dedicated invisible `EldritchNothingBlock` вместо barrier placeholder;
- `TC4OuterLandsFeatureSelector` держит dispatch по feature ids 1..14;
- live generation теперь расширяется вокруг player chunk в лабиринте.

Следующий mega-stage Stage243–252:
1. Глубже портировать `GenBossRoom` и `GenKeyRoom` loop/palette вместо текущих room-shell adapters.
2. Добрать точные `GenLibraryRoom` / `GenNestRoom` decorative queues из `GenCommon.processDecorations`.
3. Добавить реальный equivalent для `blockEldritch` metadata variants 0..15, а не маппинг всего на пару блоков.
4. Продолжить baked model parity Eldritch Guardian/Warden/Golem/Crab.
5. Начать full Outer Lands dimension/chunk integration без tick-only bridge, если возможно без ломки Forge 1.19.2.

Обязательно: класть новый prompt в архив как `docs/NEXT_CHAT_PROMPT_STAGE252.md`; делать mega-stage по 5–10 внутренних stage; сохранять 1.19.2 API, не использовать 1.7.10 `func_*`, `NBTTag*`, `World` напрямую.
