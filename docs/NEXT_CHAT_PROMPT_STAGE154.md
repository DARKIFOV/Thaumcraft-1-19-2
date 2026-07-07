Продолжи перенос оригинального Thaumcraft 4 с Minecraft 1.7.10 на Forge Minecraft 1.19.2. Продолжай строго с последнего архива Stage154 и не придумывай новые механики, предметы, рецепты, GUI, прогрессию, текстуры или поведение, пока не будет полностью перенесён оригинальный TC4.

Главное правило: это должен быть строгий оригинальный TC4 parity-порт. Если код 1.7.10 нельзя перенести напрямую, сделай адаптер под Forge 1.19.2, но сохрани оригинальные данные, research keys, страницы, иконки, аспекты, рецепты, координаты, флаги, родителей, hidden parents, siblings, warp, GUI-логику, звуки, текстуры и поведение.

Текущий статус:

- Stage147 вернул проект к строгому оригинальному TC4 parity.
- Stage148 завершил explicit research icon coverage.
- Stage149 завершил strict ResearchPage parity.
- Stage150 добавил strict research metadata parity.
- Stage151 добавил runtime progression bridge.
- Stage152 добавил strict recipe unlock parity.
- Stage153 материализовал дополнительные оригинальные infusion-рецепты и исправил infusion lookup по catalyst + component pedestals.
- Stage154 поднял версию до 1.54.0, добавил dedicated TC4InfusionEnchantmentIndex для всех 24 оригинальных addInfusionEnchantmentRecipe entries, не превращая их в fake item recipes. 22 vanilla enchantments mapped to modern ids, 2 custom TC4 enchantments preserved as thaumcraft:repair and thaumcraft:haste. Добавлен scripts/tc4_stage154_infusion_enchantment_parity_audit.py и docs/STAGE154_ACCELERATION_STRATEGY.md.
- После Stage154 осталось примерно 38–63 stage до полного точного переноса оригинального TC4.

Дальше можно ускоряться пакетами. В следующем проходе попробуй сделать сразу Stage155+156:

1. Stage155: exact remaining recipe resolver pass — улучшить TC4RecipeItemResolver для unresolved ore-dict/material expressions без fake replacements.
2. Stage156: bulk materialize exact recipes whose ids are now resolved; unresolved entries оставить явно unresolved.
3. Не ломай golems, wands, aura/nodes, crucible, infusion, infusion enchantment index, taint, eldritch, worldgen, Thaumonomicon pages/icons/research progression/recipe gates/materialized recipes и output textures.
4. Добавь аудиты scripts/tc4_stage155_recipe_resolver_audit.py и scripts/tc4_stage156_bulk_recipe_materialization_audit.py.
5. В конце выдай новый ZIP, напиши что сделал, что будешь делать в следующем stage, сколько stage ещё осталось, и обнови универсальный prompt для продолжения.
