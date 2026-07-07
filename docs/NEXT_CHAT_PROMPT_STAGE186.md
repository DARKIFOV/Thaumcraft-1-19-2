Продолжи перенос оригинального Thaumcraft 4 с Minecraft 1.7.10 на Forge Minecraft 1.19.2. Продолжай строго с последнего архива Stage186 и не придумывай новые механики, предметы, рецепты, GUI, прогрессию, текстуры или поведение, пока не будет полностью перенесён оригинальный TC4.

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
* Stage173 добавил FocusUpgradeType и FocusUpgradeRuntime: оригинальные IDs, texture/name/text keys, 5 rank slots and original `upgrade` NBT list. Focus NBT сохраняется в wand compound `focus`, как оригинальный ItemWandCasting.
* Stage174 добавил TC4FrostShardEntity, TC4ExplosiveOrbEntity, TC4ShockOrbEntity, TC4PrimalOrbEntity, регистрации entity/renderers и подключение Frost/Primal/Fireball/Earthshock к projectile foundation.
* Stage175 подключил wand focus upgrade effects potency/frugal/enlarge/extend/treasure/silktouch для оригинальных focus classes.
* Stage176 улучшил renderer/particles/sounds для FrostShard/ExplosiveOrb/ShockOrb/PrimalOrb ближе к оригинальным TC4 client renderers.
* Stage177 добавил WandManager/IArchitect area/picked-block adapter.
* Stage178 добавил projectile behavior parity batch.
* Stage179 добавил architect overlay/keybind packet parity.
* Stage180 добавил continuous focus-use parity для Fire/Shock/Excavation.
* Stage181 добавил client focus FX adapter для lightning/sparkle/beamCont/excavateFX.
* Stage182 добавил focus animation/use-state parity WAVE/CHARGE и убрал fake vanilla bow animation.
* Stage183 добавил focus item renderer/layer parity: focus cube, depth, ornament, rune texture and original focus colors.
* Stage184 исправил remaining focus behavior drift: own sounds, Equal Trade radius, Warding delay, Primal cost window, Fire range/event.
* Stage185 добавил wand/staff component renderer parity: ModelWand rod/cap/staff/sceptre transforms, glowing rod lightmap, primal staff runes, sceptre root NBT capacity/cost adapter.
* Stage186 добавил focus pouch/focus equip UI parity: original `Inventory` NBT, `ItemFocusPouchBauble`, `ContainerFocusPouch`, `GuiFocusPouch`, 18 focus slots and exact focus ItemStack/NBT cycle adapter.
* После Stage186 осталось примерно 6–29 stage до полного точного переноса оригинального TC4.

В следующем проходе попробуй сделать сразу Stage187 + Stage188:

1. Stage187: wand crafting/sceptre/staff table parity. Проверь original `ItemWandCasting`, `ConfigRecipes`, `ContainerArcaneWorkbench`, `GuiArcaneWorkbench`, wand rod/cap combinations, scepter root NBT `sceptre`, capacity/crafting discount and crafting-only focus restrictions. Исправь drift без добавления новых рецептов.
2. Stage188: original focus-selection packet/key flow parity. Подними `WandManager.changeFocus`, focus pouch/inventory/bauble scan order, packet/keybind semantics and camera tick sound closer к оригиналу, но через Forge 1.19.2 adapter.
3. Используй оригинальные `ItemWandCasting`, `WandManager`, `ItemFocusPouch`, `ItemFocusPouchBauble`, `ContainerFocusPouch`, `GuiFocusPouch`, `ItemFocusBasic` and focus classes как источник правды.
4. Добавь аудиты `scripts/tc4_stage187_wand_crafting_sceptre_audit.py` и `scripts/tc4_stage188_focus_selection_packet_audit.py`.
5. Продолжай проверять drift: если найдёшь временный адаптер, который отличается от оригинала, исправь или явно пометь как Forge 1.19.2 adapter.
6. Не ломай golems, wands, aura/nodes, crucible, infusion, infusion enchantment index, taint, eldritch, worldgen, Thaumonomicon pages/icons/research progression/recipe gates/materialized recipes/object aspects/entity scan runtime/player scan knowledge/aspect decomposition/research note grid/completion/research table container/copy behavior/bonus aspects/wand focus base behavior/focus upgrade NBT/projectile entities/output textures/focus pouch/wand renderer.
7. В конце выдай новый ZIP, напиши что сделал, что будешь делать в следующем stage, сколько stage ещё осталось, и обнови универсальный prompt для продолжения.
