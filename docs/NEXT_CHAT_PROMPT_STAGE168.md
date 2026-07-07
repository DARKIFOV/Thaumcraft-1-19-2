Продолжи перенос оригинального Thaumcraft 4 с Minecraft 1.7.10 на Forge Minecraft 1.19.2. Продолжай строго с последнего архива Stage168 и не придумывай новые механики, предметы, рецепты, GUI, прогрессию, текстуры или поведение, пока не будет полностью перенесён оригинальный TC4.

Главное правило: это должен быть строгий оригинальный TC4 parity-порт. Если код 1.7.10 нельзя перенести напрямую, сделай адаптер под Forge 1.19.2, но сохрани оригинальные данные, research keys, страницы, иконки, аспекты, рецепты, координаты, флаги, родителей, hidden parents, siblings, warp, GUI-логику, звуки, текстуры и поведение.

Текущий статус:

- Stage147 вернул проект к строгому оригинальному TC4 parity.
- Stage148 завершил explicit research icon coverage.
- Stage149 завершил strict ResearchPage parity.
- Stage150 добавил strict research metadata parity.
- Stage151 добавил runtime progression bridge.
- Stage152 добавил strict recipe unlock parity.
- Stage153 материализовал дополнительные оригинальные infusion-рецепты и исправил infusion lookup по catalyst + component pedestals.
- Stage154 добавил dedicated TC4InfusionEnchantmentIndex.
- Stage155 расширил exact recipe resolver.
- Stage156 материализовал 10 exact original focus recipes.
- Stage157 добавил exact object/entity aspect database.
- Stage158 добавил Thaumometer entity scan runtime.
- Stage159 поднял сканы Thaumometer на уровень player thaum data.
- Stage160 добавил original aspect decomposition foundation.
- Stage161 заменил fixed rebuild note grid на TC4 axial hex grid.
- Stage162 сделал Research Note completion/consumption parity.
- Stage163 добавил Research Table ink/scribing tools validation adapter.
- Stage164 добавил Research Note GUI drag/drop and q/r axial-grid hit testing.
- Stage165 добавил persistent ResearchTableBlockEntity/Menu с оригинальными slot 0/slot 1.
- Stage166 добавил ResearchTableContainerScreen/actions and drift audit.
- Stage167 поднял Research Table visual parity: original `guiresearchtable2.png`, x/y size 255, original slot coords `(14,10)` and `(70,10)`, inventory `(48,175)` and hotbar `(48,233)`, copy icon `(37,5)`.
- Stage168 поднял версию до `1.68.0` и исправил RESEARCHDUPE copy parity: original action id 5, paper + ink sac + original research aspect costs, no scribing-tool ink cost, persistent `copies` counter and stack growth.
- После Stage168 осталось примерно 24–49 stage до полного точного переноса оригинального TC4.

В следующем проходе попробуй сделать сразу Stage169 + Stage170:

1. Stage169: Research Table bonus aspects parity. Перенеси ближе к оригинальному `TileResearchTable.recalculateBonus`: bonusAspects from environment, crystals, ores/materials, height/sky/nearby blocks, without fake aspect generation.
2. Stage170: Research Table packet/sync parity. Сделай real table bonus aspects sync and GUI display/consumption path, so placement can consume table bonus aspects before or alongside player aspect pool like original.
3. Продолжай проверять drift: если найдёшь временный адаптер, который отличается от оригинала, исправь или явно пометь как Forge 1.19.2 adapter.
4. Добавь аудиты `scripts/tc4_stage169_research_table_bonus_aspects_audit.py` и `scripts/tc4_stage170_research_table_bonus_sync_audit.py`.
5. Не ломай golems, wands, aura/nodes, crucible, infusion, infusion enchantment index, taint, eldritch, worldgen, Thaumonomicon pages/icons/research progression/recipe gates/materialized recipes/object aspects/entity scan runtime/player scan knowledge/aspect decomposition/research note grid/completion/research table container/copy behavior/output textures.
6. В конце выдай новый ZIP, напиши что сделал, что будешь делать в следующем stage, сколько stage ещё осталось, и обнови универсальный prompt для продолжения.
