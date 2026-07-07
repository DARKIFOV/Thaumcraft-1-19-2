Продолжи перенос оригинального Thaumcraft 4 с Minecraft 1.7.10 на Forge Minecraft 1.19.2. Продолжай строго с последнего архива Stage149 и не придумывай новые механики, предметы, рецепты, GUI, прогрессию, текстуры или поведение, пока не будет полностью перенесён оригинальный TC4.

Главное правило: это должен быть строгий оригинальный TC4 parity-порт. Если код 1.7.10 нельзя перенести напрямую, сделай адаптер под Forge 1.19.2, но сохрани оригинальные данные, research keys, страницы, иконки, аспекты, рецепты, координаты, флаги, родителей, hidden parents, siblings, warp, GUI-логику, звуки, текстуры и поведение.

Используй как источник правды оригинальный Thaumcraft4-1.7.10-master, особенно:

- thaumcraft/common/config/ConfigResearch.java
- thaumcraft/common/config/ConfigRecipes.java
- thaumcraft/common/config/ConfigAspects.java
- thaumcraft/common/config/ConfigBlocks.java
- thaumcraft/common/config/ConfigItems.java
- thaumcraft/common/config/ConfigEntities.java
- assets/thaumcraft/**
- оригинальные GUI/entity/tile/worldgen/wand/focus классы.

Текущий статус:

- Stage147 вернул проект к строгому оригинальному TC4 parity.
- Stage148 завершил explicit research icon coverage: все 201 оригинальных research key имеют явную TC4-иконку.
- Stage149 поднял версию до `1.49.0`.
- Stage149 завершил strict ResearchPage parity: все 201 research keys и 591 original ResearchPage slots совпадают по порядку, типам и ключам.
- Добавлен `src/main/resources/data/thaumcraft/tc4_source_mapping/tc4_original_research_pages_stage149.json`.
- Добавлен `scripts/tc4_stage149_research_page_parity_audit.py`.
- После Stage149 осталось примерно 43-68 stage до полного точного переноса оригинального TC4.

В следующем Stage150 сделай:

1. Продолжи точный перенос Thaumonomicon, теперь research metadata parity.
2. Сравни каждый оригинальный `ResearchItem` из `ConfigResearch.java` с `TC4ResearchRuntimeBridge`.
3. Проверь и исправь parents, parentsHidden, siblings, flags, warp, entity triggers, item triggers, aspect triggers, complexity и coordinates.
4. Проверь, что `OriginalResearchBridge` открывает/скрывает исследования так же, как оригинальный TC4.
5. Добавь новый аудит `scripts/tc4_stage150_research_metadata_parity_audit.py`.
6. Не трогай и не ломай уже сделанные golems, wands, aura/nodes, crucible, infusion, taint, eldritch, worldgen и output textures без необходимости.
7. В конце выдай новый ZIP, напиши что сделал, что будешь делать в следующем stage, сколько stage ещё осталось, и обнови универсальный prompt для продолжения.
