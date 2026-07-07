Продолжи перенос оригинального Thaumcraft 4 с Minecraft 1.7.10 на Forge Minecraft 1.19.2. Продолжай строго с последнего архива Stage192 и не придумывай новые механики, предметы, рецепты, GUI, прогрессию, текстуры или поведение, пока не будет полностью перенесён оригинальный TC4.

Главное правило: это должен быть строгий original TC4 parity-port. Если код 1.7.10 нельзя перенести напрямую, сделай Forge 1.19.2 adapter, но сохрани оригинальные данные, research keys, страницы, иконки, аспекты, рецепты, координаты, флаги, родителей, hidden parents, siblings, warp, GUI-логику, звуки, текстуры и поведение.

Текущий статус:

* Stage147 вернул проект к строгому оригинальному TC4 parity.
* Stage148 завершил explicit research icon coverage.
* Stage149 завершил strict ResearchPage parity.
* Stage150 добавил strict research metadata parity.
* Stage151 добавил runtime progression bridge.
* Stage152 добавил strict recipe unlock parity.
* Stage153 материализовал дополнительные оригинальные infusion-рецепты и исправил infusion lookup.
* Stage154 добавил dedicated TC4InfusionEnchantmentIndex.
* Stage155 расширил exact recipe resolver.
* Stage156 материализовал 10 exact original focus recipes.
* Stage157 добавил exact object/entity aspect database.
* Stage158–160 подняли thaumometer scan runtime/player knowledge/aspect decomposition foundation.
* Stage161–170 восстановили research note/research table parity включая bonus aspects.
* Stage171–184 подняли wand focus behavior, upgrade NBT, projectile entities/visuals/behavior, architect area/overlay, continuous focus use, client FX, animation, renderer layers and remaining focus behavior drift.
* Stage185–188 подняли wand/staff renderer, focus pouch GUI/NBT, wand/sceptre crafting and original focus-selection packet/key flow.
* Stage189–190 восстановили Arcane Workbench GUI/container flow and ConfigRecipes wand component recipes.
* Stage191 восстановил exact SlotCraftingArcaneWorkbench edge cases: vanilla-first fallback, original matrix consumption/container items, shift-click routing, click/drag restrictions, staff rejection.
* Stage192 добавил final wand/focus regression audit across foci, pouch, architect, projectiles, renderer, rod/cap/sceptre NBT, ConfigRecipes and Arcane Workbench interactions.

В следующем проходе попробуй сделать сразу Stage193 + Stage194:

1. Stage193: remove/quarantine remaining legacy Arcane Workbench packets/browser-era code paths. Проверь `RequestArcaneCraftPacket`, `RequestArcaneMenuCraftPacket`, старые screen/browser references и оставь их только как явно помеченные Forge 1.19.2 migration/compat adapters, если они действительно нужны.
2. Stage194: consolidated full-port drift ledger. Создай итоговый audit/report по всем крупным системам: golems, wands/foci, aura/nodes, crucible, infusion, infusion enchantment index, taint, eldritch, worldgen, Thaumonomicon pages/icons/research progression/recipe gates/materialized recipes/object aspects/entity scan runtime/player scan knowledge/aspect decomposition/research note grid/completion/research table container/copy behavior/bonus aspects/Arcane Workbench.
3. Добавь аудиты `scripts/tc4_stage193_arcane_legacy_path_audit.py` и `scripts/tc4_stage194_full_port_drift_ledger_audit.py`.
4. Продолжай проверять drift: если найдёшь временный adapter, который отличается от оригинала, исправь или явно пометь как Forge 1.19.2 adapter.
5. Не ломай существующие systems и output textures.
6. В конце выдай новый ZIP, напиши что сделал, что будешь делать в следующем stage, сколько stage ещё осталось, и обнови universal prompt для продолжения.
