Продолжи перенос оригинального Thaumcraft 4 с Minecraft 1.7.10 на Forge Minecraft 1.19.2. Продолжай строго с последнего архива Stage152 и не придумывай новые механики, предметы, рецепты, GUI, прогрессию, текстуры или поведение, пока не будет полностью перенесён оригинальный TC4.

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
- Stage152 поднял версию до `1.52.0` и добавил strict recipe unlock parity: 281 Thaumonomicon recipe page slots / 280 unique recipe keys связаны с оригинальными research gates через `TC4RecipeRequirementIndex`.
- Stage152 подключил original recipe gates к Thaumonomicon recipe pages, Arcane Workbench, Infusion Matrix и direct Crucible crafting.
- После Stage152 осталось примерно 40-65 stage до полного точного переноса оригинального TC4.

В следующем Stage153 сделай:

1. Продолжи точный перенос оригинального TC4, теперь recipe materialization parity.
2. Сравни текущие JSON recipes с оригинальным `ConfigRecipes.java`, `tc4_stage121_materialized_recipes.json` и `tc4_stage121_unresolved_recipes.json`.
3. Удали или изолируй non-original placeholder/addon recipes из strict TC4 runtime path, но не удаляй данные без проверки.
4. Сгенерируй недостающие original arcane/crucible/infusion recipe JSON там, где Stage121 уже имеет точные modern item IDs.
5. Для unresolved ore dictionary/material recipes оставь явный unresolved mapping, не придумывай fake replacements.
6. Добавь новый аудит `scripts/tc4_stage153_recipe_materialization_parity_audit.py`.
7. Не ломай уже сделанные golems, wands, aura/nodes, crucible, infusion, taint, eldritch, worldgen, Thaumonomicon pages/icons/research progression/recipe unlock gates и output textures.
8. В конце выдай новый ZIP, напиши что сделал, что будешь делать в следующем stage, сколько stage ещё осталось, и обнови универсальный prompt для продолжения.
