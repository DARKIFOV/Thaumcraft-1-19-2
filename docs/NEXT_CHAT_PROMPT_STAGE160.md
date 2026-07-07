Продолжи перенос оригинального Thaumcraft 4 с Minecraft 1.7.10 на Forge Minecraft 1.19.2. Продолжай строго с последнего архива Stage160 и не придумывай новые механики, предметы, рецепты, GUI, прогрессию, текстуры или поведение, пока не будет полностью перенесён оригинальный TC4.

Главное правило: это должен быть строгий оригинальный TC4 parity-порт. Если код 1.7.10 нельзя перенести напрямую, сделай адаптер под Forge 1.19.2, но сохрани оригинальные данные, research keys, страницы, иконки, аспекты, рецепты, координаты, флаги, родителей, hidden parents, siblings, warp, GUI-логику, звуки, текстуры и поведение.

Текущий статус:

- Stage147 вернул проект к строгому оригинальному TC4 parity.
- Stage148 завершил explicit research icon coverage.
- Stage149 завершил strict ResearchPage parity.
- Stage150 добавил strict research metadata parity.
- Stage151 добавил runtime progression bridge.
- Stage152 добавил strict recipe unlock parity.
- Stage153 материализовал дополнительные оригинальные infusion-рецепты и исправил infusion lookup по catalyst + component pedestals.
- Stage154 добавил dedicated `TC4InfusionEnchantmentIndex` для всех 24 оригинальных infusion enchantment entries.
- Stage155 расширил exact recipe resolver.
- Stage156 материализовал 10 exact original focus recipes.
- Stage157 добавил exact object/entity aspect database.
- Stage158 добавил Thaumometer entity scan runtime и legacy scan trigger aliases.
- Stage159 поднял сканы Thaumometer на уровень player thaum data: `ScannedObjects`, `ScannedEntities`, `ScannedAspects` в `PlayerThaumData`, сохранив item NBT как compatibility mirror.
- Stage160 поднял версию до `1.60.0`, добавил `tc4_aspect_decomposition_stage160.json`, расширил `AspectCombinationRegistry` exact decomposition helpers и подключил Research Note linking к original direct component/decomposition links.
- После Stage160 осталось примерно 32–57 stage до полного точного переноса оригинального TC4.

В следующем проходе попробуй сделать сразу Stage161 + Stage162:

1. Stage161: Research Note grid/layout parity. Сверь текущий grid/slot layout с оригинальным TC4 research note GUI/logic и исправь без новых puzzle rules.
2. Stage162: Research Note completion/consumption parity. Проверь solve/complete behavior, aspect consumption/refund, sync, conversion to completed research, и все side effects.
3. Не придумывай новые rules. Бери оригинальный TC4 research note flow как источник правды.
4. Добавь аудиты `scripts/tc4_stage161_research_note_grid_parity_audit.py` и `scripts/tc4_stage162_research_note_completion_parity_audit.py`.
5. Не ломай golems, wands, aura/nodes, crucible, infusion, infusion enchantment index, taint, eldritch, worldgen, Thaumonomicon pages/icons/research progression/recipe gates/materialized recipes/object aspects/entity scan runtime/player scan knowledge/aspect decomposition/output textures.
6. В конце выдай новый ZIP, напиши что сделал, что будешь делать в следующем stage, сколько stage ещё осталось, и обнови универсальный prompt для продолжения.
