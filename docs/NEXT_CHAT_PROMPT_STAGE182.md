Продолжи перенос оригинального Thaumcraft 4 с Minecraft 1.7.10 на Forge Minecraft 1.19.2. Продолжай строго с последнего архива Stage182 и не придумывай новые механики, предметы, рецепты, GUI, прогрессию, текстуры или поведение, пока не будет полностью перенесён оригинальный TC4.

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
* Stage174 добавил TC4FrostShardEntity, TC4ExplosiveOrbEntity, TC4ShockOrbEntity, TC4PrimalOrbEntity, регистрации entity/renderers и projectile foundation.
* Stage175 подключил potency/frugal/enlarge/extend/treasure/silktouch effects там, где оригинальные focus classes их используют.
* Stage176 улучшил projectile renderer/particles/sounds для FrostShard/ExplosiveOrb/ShockOrb/PrimalOrb.
* Stage177 добавил WandManager/IArchitect NBT area adapter: `areax`, `areay`, `areaz`, `aread`, `picked`.
* Stage178 улучшил projectile behavior parity для FrostShard/ExplosiveOrb/ShockOrb/PrimalOrb.
* Stage179 добавил client architect overlay/keybind packet parity.
* Stage180 добавил continuous focus-use parity batch для Fire/Shock/Excavation, включая TC4EmberEntity, fireloop/shock/rumble timing, breakcount/last target tracking.
* Stage181 добавил client focus FX adapter для original `FXLightningBolt`, `beamCont`, `sparkle`, `excavateFX` call sites.
* Stage182 добавил original `WandFocusAnimation.WAVE/CHARGE` adapter и убрал fake vanilla `UseAnim.BOW` drift.
* После Stage182 осталось примерно 10–33 stage до полного точного переноса оригинального TC4.

В следующем проходе попробуй сделать сразу Stage183 + Stage186:

1. Stage183: focus item renderer/layer parity batch. Сверь `ItemWandRenderer`, focus depth/ornament layers, wand/focus model transforms и rendering offsets с оригиналом TC4. Не добавляй новых моделей/текстур; используй оригинальные TC4 assets или явно пометь Forge 1.19.2 adapter.
2. Stage186: remaining focus behavior drift batch. Проверь Fire/Frost/Shock/Excavation/Trade/PortableHole/Warding/Primal против оригинальных ItemFocus* классов и исправь оставшийся drift по range, target picking, sound events, per-tick vis consumption, upgrade gates, block restrictions and cooldowns.
3. Используй оригинальные `ItemFocusBasic`, `ItemFocusFire`, `ItemFocusFrost`, `ItemFocusShock`, `ItemFocusExcavation`, `ItemFocusTrade`, `ItemFocusPortableHole`, `ItemFocusWarding`, `ItemFocusPrimal`, `ItemWandRenderer`, `FXBeamWand`, `FXLightningBolt` как источник правды.
4. Добавь аудиты `scripts/tc4_stage183_focus_item_renderer_audit.py` и `scripts/tc4_stage186_focus_behavior_drift_audit.py`.
5. Продолжай проверять drift: если найдёшь временный адаптер, который отличается от оригинала, исправь или явно пометь как Forge 1.19.2 adapter.
6. Не ломай golems, wands, aura/nodes, crucible, infusion, infusion enchantment index, taint, eldritch, worldgen, Thaumonomicon pages/icons/research progression/recipe gates/materialized recipes/object aspects/entity scan runtime/player scan knowledge/aspect decomposition/research note grid/completion/research table container/copy behavior/bonus aspects/wand focus base behavior/focus upgrade NBT/projectile entities/output textures.
7. В конце выдай новый ZIP, напиши что сделал, что будешь делать в следующем stage, сколько stage ещё осталось, и обнови универсальный prompt для продолжения.
