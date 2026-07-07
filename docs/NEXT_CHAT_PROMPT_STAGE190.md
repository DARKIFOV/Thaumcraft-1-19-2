Продолжи перенос оригинального Thaumcraft 4 с Minecraft 1.7.10 на Forge Minecraft 1.19.2. Продолжай строго с последнего архива Stage190 и не придумывай новые механики, предметы, рецепты, GUI, прогрессию, текстуры или поведение, пока не будет полностью перенесён оригинальный TC4.

Главное правило: это должен быть строгий оригинальный TC4 parity-порт. Если код 1.7.10 нельзя перенести напрямую, сделай адаптер под Forge 1.19.2, но сохрани оригинальные данные, research keys, страницы, иконки, аспекты, рецепты, координаты, флаги, родителей, hidden parents, siblings, warp, GUI-логику, звуки, текстуры и поведение.

Текущий статус:

* Stage147 вернул проект к строгому оригинальному TC4 parity.
* Stage148 завершил explicit research icon coverage.
* Stage149 завершил strict ResearchPage parity.
* Stage150 добавил strict research metadata parity.
* Stage151 добавил runtime progression bridge.
* Stage152 добавил strict recipe unlock parity.
* Stage153 материализовал дополнительные оригинальные infusion-рецепты и исправил infusion lookup по catalyst + component pedestals.
* Stage154 добавил dedicated TC4InfusionEnchantmentIndex.
* Stage155 расширил exact recipe resolver.
* Stage156 материализовал 10 exact original focus recipes.
* Stage157 добавил exact object/entity aspect database.
* Stage158 добавил Thaumometer entity scan runtime.
* Stage159 поднял сканы Thaumometer на уровень player thaum data.
* Stage160 добавил original aspect decomposition foundation.
* Stage161 заменил fixed rebuild note grid на TC4 axial hex grid.
* Stage162 сделал Research Note completion/consumption parity.
* Stage163 добавил Research Table ink/scribing tools validation adapter.
* Stage164 добавил Research Note GUI drag/drop and q/r axial-grid hit testing.
* Stage165 добавил persistent ResearchTableBlockEntity/Menu с оригинальными slot 0/slot 1.
* Stage166 добавил ResearchTableContainerScreen/actions and drift audit.
* Stage167 поднял Research Table visual parity.
* Stage168 исправил RESEARCHDUPE copy parity.
* Stage169 восстановил оригинальную модель TileResearchTable.bonusAspects.
* Stage170 синхронизировал bonus aspects и подключил их к расходу при research note placement.
* Stage171 начал wand focus behavior parity.
* Stage172 добавил base cooldowns/cost sync для основных foci.
* Stage173 добавил FocusUpgradeType и FocusUpgradeRuntime: оригинальные IDs, texture/name/text keys, 5 rank slots and original `upgrade` NBT list. Focus NBT теперь сохраняется в wand compound `focus`, как оригинальный ItemWandCasting.
* Stage174 добавил TC4FrostShardEntity, TC4ExplosiveOrbEntity, TC4ShockOrbEntity, TC4PrimalOrbEntity, регистрации entity/renderers и projectile foundation.
* Stage175 подключил potency/frugal/enlarge/extend/treasure/silktouch effects там, где оригинальные focus classes их используют.
* Stage176 улучшил projectile visual/sound/particle parity для FrostShard/ExplosiveOrb/ShockOrb/PrimalOrb.
* Stage177 добавил WandManager/IArchitect area/picked-block adapter.
* Stage178 добавил projectile behavior parity batch.
* Stage179 добавил architect overlay/keybind packet parity.
* Stage180 добавил continuous focus-use parity для Fire/Shock/Excavation.
* Stage181 добавил client focus FX adapter для lightning/sparkles/beamCont/excavateFX.
* Stage182 добавил focus animation/use-state parity и убрал vanilla bow-like use animation.
* Stage183 добавил wand focus item renderer/layer parity.
* Stage184 исправил remaining focus behavior drift.
* Stage185 добавил wand/staff component renderer parity.
* Stage186 добавил focus pouch/focus equip UI parity.
* Stage187 добавил wand crafting/sceptre table parity.
* Stage188 добавил original focus-selection packet/key flow parity.
* Stage189 вернул Arcane Workbench к original GUI/container flow: slots 0..8 grid, output 9, wand 10, без recipe browser/search/Craft button, с output preview/onTake crafting и aspect-cost positions.
* Stage190 добавил TC4ConfigRecipesWandIndex: original ConfigRecipes wand cap/greatwood rod/staff rod arcane component recipes, exact ConfigRecipes costs, generated recipe chain before ArcaneWandRecipe/ArcaneSceptreRecipe, prebuilt wand shortcuts marked as Forge 1.19.2 adapter drift.
* После Stage190 осталось примерно 2–25 stage до полного точного переноса оригинального TC4.

В следующем проходе попробуй сделать сразу Stage191 + Stage192:

1. Stage191: exact `SlotCraftingArcaneWorkbench` edge-case parity. Проверь и исправь vanilla crafting fallback, soft output updates, click restrictions, shift-click return behaviour, output stack count semantics, wand slot restrictions, `func_75144_a`/drag restrictions и remaining server/client sync drift. Используй оригинальные `ContainerArcaneWorkbench`, `SlotCraftingArcaneWorkbench`, `TileArcaneWorkbench`, `ThaumcraftCraftingManager`, `GuiArcaneWorkbench` как источник правды.
2. Stage192: final wand/focus regression audit. Проверь Fire/Frost/Shock/Excavation/Trade/PortableHole/Warding/Primal, FocusPouch, WandManager/IArchitect, projectile entities/renderers, wand/staff/sceptre renderer, rod/cap/sceptre NBT, focus upgrade NBT, ConfigRecipes component recipes and ArcaneWandRecipe/ArcaneSceptreRecipe interactions against original TC4. Исправь найденный drift или явно пометь как Forge 1.19.2 adapter.
3. Добавь аудиты `scripts/tc4_stage191_arcane_workbench_slotcrafting_audit.py` и `scripts/tc4_stage192_final_wand_focus_regression_audit.py`.
4. Не ломай golems, wands, aura/nodes, crucible, infusion, infusion enchantment index, taint, eldritch, worldgen, Thaumonomicon pages/icons/research progression/recipe gates/materialized recipes/object aspects/entity scan runtime/player scan knowledge/aspect decomposition/research note grid/completion/research table container/copy behavior/bonus aspects/wand focus base behavior/focus upgrade NBT/projectile entities/output textures/Arcane Workbench original GUI flow.
5. В конце выдай новый ZIP, напиши что сделал, что будешь делать в следующем stage, сколько stage ещё осталось, и обнови универсальный prompt для продолжения.
