Продолжи перенос оригинального Thaumcraft 4 с Minecraft 1.7.10 на Forge Minecraft 1.19.2. Продолжай строго с последнего архива Stage196 и не придумывай новые механики, предметы, рецепты, GUI, прогрессию, текстуры или поведение, пока не будет полностью перенесён оригинальный TC4.

Главное правило: это должен быть строгий оригинальный TC4 parity-порт. Если код 1.7.10 нельзя перенести напрямую, сделай адаптер под Forge 1.19.2, но сохрани оригинальные данные, research keys, страницы, иконки, аспекты, рецепты, координаты, флаги, родителей, hidden parents, siblings, warp, GUI-логику, звуки, текстуры и поведение.

Текущий статус: Stage195 восстановил original EnumGolemType, ItemGolemCore metadata, ItemGolemUpgrade metadata, original EntityGolemBase NBT keys и golem carry/inventory/attribute formulas batch 1. Stage196 восстановил TileTube/TileJarFillable suction/NBT state batch: type/amount/side/open/stype/samount/venting, jar suction 32/64/32/48 и side-gated traversal adapter.

В следующем проходе сделай Stage197 + Stage198:

1. Stage197: golem task AI parity batch 2. Перенеси ближе к оригиналу AIHomeReplace/AIHomeTake/AIHomeDrop/AIItemPickup/AIFillGoto/AIFillTake/AIEmptyGoto/AIEmptyPlace/AISortingPlace таблицы и priorities без новых задач.
2. Stage198: essentia tube subclass parity batch. Добавь строгие adapter-реализации для TileTubeFilter, TileTubeRestrict, TileTubeOneway, TileTubeBuffer и valve facing/openSides wand toggle behavior, используя оригинальные NBT keys и suction rules.
3. Добавь аудиты `scripts/tc4_stage197_golem_task_ai_audit.py` и `scripts/tc4_stage198_tube_subclass_audit.py`.
4. Продолжай проверять drift: если найдёшь временный адаптер, который отличается от оригинала, исправь или явно пометь как Forge 1.19.2 adapter.
5. Не ломай golems, essentia suction/tubes/jars, wands, aura/nodes, crucible, infusion, taint, eldritch, worldgen, Thaumonomicon pages/icons/research progression/recipe gates/materialized recipes/object aspects/entity scan runtime/player scan knowledge/aspect decomposition/research note grid/completion/research table container/copy behavior/bonus aspects/wand focus base behavior/focus upgrade NBT/projectile entities/output textures/Arcane Workbench parity.
6. В конце выдай новый ZIP, напиши что сделал, что будешь делать в следующем stage, сколько stage ещё осталось, и обнови универсальный prompt для продолжения.
