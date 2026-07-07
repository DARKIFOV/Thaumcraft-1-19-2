Продолжи перенос оригинального Thaumcraft 4 с Minecraft 1.7.10 на Forge Minecraft 1.19.2. Продолжай строго с последнего архива Stage153 и не придумывай новые механики, предметы, рецепты, GUI, прогрессию, текстуры или поведение, пока не будет полностью перенесён оригинальный TC4.

Главное правило: это должен быть строгий оригинальный TC4 parity-порт. Если код 1.7.10 нельзя перенести напрямую, сделай адаптер под Forge 1.19.2, но сохрани оригинальные данные, research keys, страницы, иконки, аспекты, рецепты, координаты, флаги, родителей, hidden parents, siblings, warp, GUI-логику, звуки, текстуры и поведение.

Текущий статус:

- Stage147 вернул проект к строгому оригинальному TC4 parity.
- Stage148 завершил explicit research icon coverage: все 201 оригинальных research key имеют явную TC4-иконку.
- Stage149 завершил strict ResearchPage parity: все 201 research keys и 591 original ResearchPage slots совпадают по порядку, типам и ключам.
- Stage150 добавил strict research metadata parity.
- Stage151 добавил runtime progression bridge.
- Stage152 добавил strict recipe unlock parity через TC4RecipeRequirementIndex.
- Stage153 поднял версию до `1.53.0`, добавил `tc4_original_recipe_materialization_stage153.json`, материализовал 17 дополнительных оригинальных infusion-рецептов из Stage121, изолировал non-original placeholder/addon recipe JSON из strict TC4 runtime paths и исправил infusion lookup: теперь выбор рецепта идёт по catalyst + component pedestals, а не только по catalyst.
- После Stage153 осталось примерно 39-64 stage до полного точного переноса оригинального TC4.

В следующем Stage154 сделай:

1. Продолжи точный перенос оригинального TC4, теперь infusion enchantment runtime/materialization parity.
2. Используй 24 `INFUSION_ENCHANTMENT` entries из `tc4_stage121_materialized_recipes.json` и исходный `ConfigRecipes.java`.
3. Не превращай infusion enchantments в fake item recipes. Сделай отдельный runtime bridge под 1.19.2 enchantment flow.
4. Сохрани оригинальные catalysts, components, aspects, instability и research gates.
5. Подключи проверку research через `TC4RecipeRequirementIndex`.
6. Добавь новый аудит `scripts/tc4_stage154_infusion_enchantment_parity_audit.py`.
7. Не ломай golems, wands, aura/nodes, crucible, infusion, taint, eldritch, worldgen, Thaumonomicon pages/icons/research progression/recipe gates/materialized recipes и output textures.
8. В конце выдай новый ZIP, напиши что сделал, что будешь делать в следующем stage, сколько stage ещё осталось, и обнови универсальный prompt для продолжения.
