Продолжи перенос оригинального Thaumcraft 4 с Minecraft 1.7.10 на Forge Minecraft 1.19.2. Продолжай строго с последнего архива Stage151 и не придумывай новые механики, предметы, рецепты, GUI, прогрессию, текстуры или поведение, пока не будет полностью перенесён оригинальный TC4.

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
- Stage149 завершил strict ResearchPage parity: все 201 research keys и 591 original ResearchPage slots совпадают по порядку, типам и ключам.
- Stage150 добавил strict research metadata parity: category, coords, complexity, aspects, parents, hidden parents, siblings, flags, warp и scan triggers для всех 201 research keys.
- Stage151 добавил runtime progression bridge: auto_unlock seeding, Thaumometer scan triggers, original parent checks and research warp side effects.
- Версия проекта: `1.51.0`.
- После Stage151 осталось примерно 41-66 stage до полного точного переноса оригинального TC4.

В следующем Stage152 сделай:

1. Продолжи точный перенос оригинального TC4, теперь recipe unlock / recipe requirement parity.
2. Сравни текущие recipe gates и recipe page resolution с оригинальным `ConfigRecipes.java`.
3. Проверь arcane crafting, normal crafting, crucible, infusion и infusion enchantment recipe keys.
4. Убери placeholder/fallback recipes там, где оригинальный TC4 имеет точные recipes.
5. Проверь, что research pages из Stage149 реально открывают правильные рецепты из оригинального recipe bridge.
6. Добавь новый аудит `scripts/tc4_stage152_recipe_unlock_parity_audit.py`.
7. Не ломай уже сделанные golems, wands, aura/nodes, crucible, infusion, taint, eldritch, worldgen, Thaumonomicon pages/icons и output textures.
8. В конце выдай новый ZIP, напиши что сделал, что будешь делать в следующем stage, сколько stage ещё осталось, и обнови универсальный prompt для продолжения.
