Продолжи перенос оригинального Thaumcraft 4 с Minecraft 1.7.10 на Forge Minecraft 1.19.2 строго от Stage543–562. Никаких новых механик, предметов, рецептов, GUI, текстур, прогрессии или поведения, пока не будет полностью перенесён оригинальный TC4.

Главное правило: строгий TC4 parity-port. Если код 1.7.10 нельзя перенести напрямую, делай Forge 1.19.2 adapter, но явно помечай его и сохраняй оригинальные данные, research keys, страницы, иконки, аспекты, рецепты, координаты, флаги, родителей, hidden parents, siblings, warp, GUI-логику, звуки, текстуры и поведение.

Текущее состояние Stage543–562:
- Thaumatorium formula icons стали кликабельными hotzones через RequestThaumatoriumFormulaPacket, без vanilla Button.
- Формулы в GUI показывают output item, а не одинаковый catalyst.
- ThaumatoriumBlockEntity получил selectFormulaIndex/selectedFormulaIndex.
- Успешный крафт Thaumatorium даёт оригинальный TC4 sound key craftstart + минимальный Forge 1.19.2 FX adapter.
- Выбор формулы даёт оригинальный TC4 sound key brain.
- TC4EssentiaNetworkRuntime стал строже учитывать one-way tube direction: output toward consumer + upstream input/output checks.
- Drain order теперь source-priority: alembic -> reservoir -> jar -> buffer, чтобы не было flat-network drift.

Следующий Stage563–582:
1. Делать Thaumatorium OBJ/renderer parity по original thaumatorium.obj/thaumatorium.png.
2. Довести Mnemonic Matrix visual/runtime parity: brainbox/brain2, original sounds, exact adjacency rules.
3. Продолжить tube suction parity: filter/restrict/oneway/buffer/valve/choked sides, без плоской сети.
4. Проверить точные рецепты алхимии из ConfigRecipes и убрать любые fallback/non-original survival recipes.
5. Не ломать golems, wands, aura/nodes, crucible, infusion, taint, eldritch, worldgen, Thaumonomicon pages/icons/research progression/recipe gates/materialized recipes/object aspects/entity scan runtime/player scan knowledge/aspect decomposition/research note grid/completion/research table container/copy behavior/bonus aspects/wand focus base behavior/focus upgrade NBT/projectile entities/output textures.

В конце выдай ZIP, отчёт, сколько реально осталось до 100%, и новый NEXT_CHAT_PROMPT.

Compatibility marker: Stage543-562
Compatibility marker: Stage563-582
