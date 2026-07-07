Продолжи перенос оригинального Thaumcraft 4 с Minecraft 1.7.10 на Forge Minecraft 1.19.2. Продолжай строго с последнего архива Stage156 и не придумывай новые механики, предметы, рецепты, GUI, прогрессию, текстуры или поведение, пока не будет полностью перенесён оригинальный TC4.

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
- Stage155 расширил exact recipe resolver: focus items, bauble blank metadata, exact vanilla Blocks fields, safe ore-dict tokens and Stage121 TC4 block item carriers. OreTin/Silver/Lead, NBT/wildcard and dynamic WandCap/WandRod formulas остались unresolved.
- Stage156 поднял версию до `1.56.0` и материализовал 10 exact original focus recipes: 7 arcane focus recipes and 3 infusion focus recipes.
- Добавлены `scripts/tc4_stage155_recipe_resolver_audit.py`, `scripts/tc4_stage156_bulk_recipe_materialization_audit.py`, `tc4_stage155_recipe_resolver_exact_pass.json`, `tc4_stage156_bulk_recipe_materialization.json`.
- После Stage156 осталось примерно 36–61 stage до полного точного переноса оригинального TC4.

В следующем проходе попробуй сделать сразу Stage157 + Stage158:

1. Stage157: bulk original object/entity AspectList database parity. Используй оригинальный `ConfigAspects.java` и перенеси точные AspectList для предметов, блоков и сущностей.
2. Stage158: Thaumometer scanner mapping runtime. Подключи exact object/entity aspects к scan unlock/progression runtime из Stage151.
3. Не выдумывай аспекты и не делай approximate aspect lists. Всё, что не сопоставлено точно, оставляй unresolved.
4. Добавь аудиты `scripts/tc4_stage157_object_entity_aspect_parity_audit.py` и `scripts/tc4_stage158_thaumometer_scan_runtime_audit.py`.
5. Не ломай golems, wands, aura/nodes, crucible, infusion, infusion enchantment index, taint, eldritch, worldgen, Thaumonomicon pages/icons/research progression/recipe gates/materialized recipes and output textures.
6. В конце выдай новый ZIP, напиши что сделал, что будешь делать в следующем stage, сколько stage ещё осталось, и обнови универсальный prompt для продолжения.
