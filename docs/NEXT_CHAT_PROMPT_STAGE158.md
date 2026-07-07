Продолжи перенос оригинального Thaumcraft 4 с Minecraft 1.7.10 на Forge Minecraft 1.19.2. Продолжай строго с последнего архива Stage158 и не придумывай новые механики, предметы, рецепты, GUI, прогрессию, текстуры или поведение, пока не будет полностью перенесён оригинальный TC4.

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
- Stage157 добавил `tc4_original_aspect_database_stage157.json`, сохранил 190 exact object aspect entries и расширил entity aspect runtime до 67 entries без fake replacements.
- Stage158 поднял версию до `1.58.0`, добавил Thaumometer entity scan runtime, `ScannedEntities` NBT и legacy scan trigger aliases для `Enderman`, `Thaumcraft.BrainyZombie`, `Thaumcraft.GiantBrainyZombie`, `Thaumcraft.Firebat`, `Thaumcraft.PrimalOrb`.
- Добавлены аудиты `scripts/tc4_stage157_object_entity_aspect_parity_audit.py` и `scripts/tc4_stage158_thaumometer_scan_runtime_audit.py`.
- После Stage158 осталось примерно 34–59 stage до полного точного переноса оригинального TC4.

В следующем проходе попробуй сделать сразу Stage159 + Stage160:

1. Stage159: exact aspect discovery/player scan knowledge parity. Сделай персистентное хранение открытых объектов/сущностей/аспектов на уровне player thaum data, а не только NBT конкретного Thaumometer item.
2. Stage160: Research Table / aspect decomposition foundation. Подключи оригинальную AspectCombinationRegistry/Aspect decomposition к research-note flow как в TC4, без новых puzzle rules.
3. Не выдумывай аспекты, scan triggers или research behavior. Всё брать из `ConfigAspects.java`, `ConfigResearch.java` и оригинальных API-смыслов TC4.
4. Добавь аудиты `scripts/tc4_stage159_player_scan_knowledge_audit.py` и `scripts/tc4_stage160_research_table_aspect_foundation_audit.py`.
5. Не ломай golems, wands, aura/nodes, crucible, infusion, infusion enchantment index, taint, eldritch, worldgen, Thaumonomicon pages/icons/research progression/recipe gates/materialized recipes/object aspects/entity scan runtime/output textures.
6. В конце выдай новый ZIP, напиши что сделал, что будешь делать в следующем stage, сколько stage ещё осталось, и обнови универсальный prompt для продолжения.
