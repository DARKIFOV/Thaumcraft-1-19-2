Продолжи перенос оригинального Thaumcraft 4 с Minecraft 1.7.10 на Forge Minecraft 1.19.2. Продолжай строго с последнего архива Stage176 и не придумывай новые механики, предметы, рецепты, GUI, прогрессию, текстуры или поведение, пока не будет полностью перенесён оригинальный TC4.

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
* Stage174 поднял версию до `1.74.0`: добавил TC4FrostShardEntity, TC4ExplosiveOrbEntity, TC4ShockOrbEntity, TC4PrimalOrbEntity, регистрации entity/renderers и подключение Frost/Primal/Fireball/Earthshock к projectile foundation.
* Stage175 поднял focus upgrade effect parity batch 1: подключил оригинальные эффекты potency/frugal/enlarge/extend/treasure/silktouch к vis cost/cooldowns/projectile damage/area/duration/radius/drop handling там, где это используют оригинальные ItemFocus* классы. Treasure/Silk Touch excavation сделан как Forge 1.19.2 loot-context adapter, не fake drop.
* Stage176 поднял projectile visual/sound/particle parity batch: убран blank renderer, FrostShard/ExplosiveOrb/ShockOrb/PrimalOrb используют оригинальные TC4 texture sheets/frame coordinates/sound keys/particle-style behavior; исправлены drift значения ExplosiveOrb strength 1.0, ShockOrb area 4/damage 5, PrimalOrb maxLife 5000, убрано неоригинальное FrostShard snow placement.
* После Stage176 осталось примерно 16–39 stage до полного точного переноса оригинального TC4.

В следующем проходе попробуй сделать сразу Stage177 + Stage178:

1. Stage177: focus architect/area parity batch. Подключи оригинальное architect/enlarge поведение и preview/selection logic для ItemFocusTrade и ItemFocusWarding там, где это возможно в Forge 1.19.2, без новых GUI/механик. Если оригинальный client preview нельзя перенести напрямую, сделай явно помеченный adapter, сохрани размеры area, ray distance, side-plane logic, cost rules и NBT upgrades.
2. Stage178: focus projectile/entity behavior parity batch 2. Доведи FrostShard bounce/fragile, ExplosiveOrb impact/bat-back semantics, ShockOrb block/area pulse и PrimalOrb seeker/impact side effects ближе к оригинальным entity classes. Не добавляй taint/node side effects, пока aura/nodes/taint не готовы; пометь такие места как deferred original dependency.
3. Используй оригинальные ItemFocusBasic, FocusUpgradeType, ItemFocusFire/Frost/Shock/Excavation/Trade/PortableHole/Warding/Primal и projectile entity/renderer classes как источник правды.
4. Добавь аудиты `scripts/tc4_stage177_focus_architect_area_audit.py` и `scripts/tc4_stage178_focus_projectile_behavior_audit.py`.
5. Продолжай проверять drift: если найдёшь временный адаптер, который отличается от оригинала, исправь или явно пометь как Forge 1.19.2 adapter.
6. Не ломай golems, wands, aura/nodes, crucible, infusion, infusion enchantment index, taint, eldritch, worldgen, Thaumonomicon pages/icons/research progression/recipe gates/materialized recipes/object aspects/entity scan runtime/player scan knowledge/aspect decomposition/research note grid/completion/research table container/copy behavior/bonus aspects/wand focus base behavior/focus upgrade NBT/projectile entities/output textures.
7. В конце выдай новый ZIP, напиши что сделал, что будешь делать в следующем stage, сколько stage ещё осталось, и обнови универсальный prompt для продолжения.
