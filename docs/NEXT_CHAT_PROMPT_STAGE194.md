Продолжи перенос оригинального Thaumcraft 4 с Minecraft 1.7.10 на Forge Minecraft 1.19.2. Продолжай строго с последнего архива Stage194 и не придумывай новые механики, предметы, рецепты, GUI, прогрессию, текстуры или поведение, пока не будет полностью перенесён оригинальный TC4.

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
* Stage174 добавил TC4FrostShardEntity, TC4ExplosiveOrbEntity, TC4ShockOrbEntity, TC4PrimalOrbEntity, registrations/renderers and projectile foundation.
* Stage175 подключил potency/frugal/enlarge/extend/treasure/silktouch upgrade effects там, где оригинальные focus classes их используют.
* Stage176 улучшил projectile visual/sound/particle parity для FrostShard/ExplosiveOrb/ShockOrb/PrimalOrb.
* Stage177 добавил WandManager/IArchitect area/picked block adapter with `areax/areay/areaz/aread/picked` NBT.
* Stage178 добавил projectile behavior parity batch.
* Stage179 добавил architect overlay/keybind packet parity.
* Stage180 добавил continuous focus-use parity для Fire/Shock/Excavation.
* Stage181 добавил focus client FX adapter: FXLightningBolt/beamCont/sparkle/excavateFX.
* Stage182 добавил focus animation/use-state parity WAVE/CHARGE.
* Stage183 добавил focus item renderer/layer parity.
* Stage184 исправил remaining focus behavior drift.
* Stage185 добавил wand/staff component renderer parity.
* Stage186 добавил focus pouch/focus equip UI parity.
* Stage187 добавил wand crafting/sceptre table parity.
* Stage188 добавил original focus-selection packet/key flow parity.
* Stage189 восстановил original Arcane Workbench GUI/container flow без browser/search/Craft button.
* Stage190 добавил ConfigRecipes wand component recipes.
* Stage191 добавил exact SlotCraftingArcaneWorkbench edge-case parity.
* Stage192 добавил final wand/focus regression audit.
* Stage193 удалил legacy Arcane Workbench browser-era packet/screen craft paths и оставил только original-style container flow plus explicit migration adapter.
* Stage194 добавил consolidated full-port drift ledger по крупным системам: golems, wands/foci, aura/nodes, crucible, infusion, taint, eldritch, worldgen, Thaumonomicon/research, research table, Arcane Workbench.
* После Stage194 осталось примерно 1–21 stage до полного точного переноса оригинального TC4.

В следующем проходе попробуй сделать сразу Stage195 + Stage196:

1. Stage195: golem core/AI parity batch 1. Используй оригинальные `EntityGolemBase`, `ItemGolemBell`, `ItemGolemCore`, `ItemGolemUpgrade`, golem inventory/task targeting/core behavior tables как источник правды. Не придумывай новые core/upgrades/tasks.
2. Stage196: essentia transport suction parity batch. Используй оригинальные `TileTube`, `TileTubeValve`, `TileJar`, suction/filter/label/transfer priority/tick timing как источник правды. Не делай simplified pipe network, если он отличается от TC4.
3. Используй `docs/TC4_FULL_PORT_DRIFT_LEDGER_STAGE194.md` и `src/main/resources/data/thaumcraft/tc4_drift/full_port_drift_ledger_stage194.json` как список оставшегося drift, но исправляй только через оригинальный TC4 parity-port или явно помеченный Forge 1.19.2 adapter.
4. Добавь аудиты `scripts/tc4_stage195_golem_core_ai_audit.py` и `scripts/tc4_stage196_essentia_suction_audit.py`.
5. Продолжай проверять drift: если найдёшь временный адаптер, который отличается от оригинала, исправь или явно пометь как Forge 1.19.2 adapter.
6. Не ломай golems, wands, aura/nodes, crucible, infusion, infusion enchantment index, taint, eldritch, worldgen, Thaumonomicon pages/icons/research progression/recipe gates/materialized recipes/object aspects/entity scan runtime/player scan knowledge/aspect decomposition/research note grid/completion/research table container/copy behavior/bonus aspects/wand focus base behavior/focus upgrade NBT/projectile entities/output textures/Arcane Workbench slot and crafting flow/full-port drift ledger.
7. В конце выдай новый ZIP, напиши что сделал, что будешь делать в следующем stage, сколько stage ещё осталось, и обнови универсальный prompt для продолжения.
