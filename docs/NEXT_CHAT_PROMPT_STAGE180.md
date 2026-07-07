Продолжи перенос оригинального Thaumcraft 4 с Minecraft 1.7.10 на Forge Minecraft 1.19.2. Продолжай строго с последнего архива Stage180 и не придумывай новые механики, предметы, рецепты, GUI, прогрессию, текстуры или поведение, пока не будет полностью перенесён оригинальный TC4.

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
* Stage174 поднял projectile foundation: TC4FrostShardEntity, TC4ExplosiveOrbEntity, TC4ShockOrbEntity, TC4PrimalOrbEntity, entity/render registrations и подключение Frost/Primal/Fireball/Earthshock.
* Stage175 подключил wand focus upgrade effect parity batch 1: potency/frugal/enlarge/extend/treasure/silktouch там, где оригинальные focus classes их используют.
* Stage176 поднял focus projectile visual/sound/particle parity batch для FrostShard/ExplosiveOrb/ShockOrb/PrimalOrb.
* Stage177 добавил original WandManager/IArchitect area runtime: wand NBT `areax/areay/areaz/aread`, picked block NBT, Equal Trade connected/architect selection, Warding architect ward/unward, и original dependent upgrade gates.
* Stage178 добавил projectile behavior parity batch 2: projectile gravity/lifecycle hook, FrostShard fragile/bounce behavior, ExplosiveOrb hit damage/explosion behavior, ShockOrb AoE/LOS burst, PrimalOrb seeker/non-seeker movement, water-amplified impact, taint/aura-node special outcome adapter.
* Stage179 добавил client architect overlay/keybind packet parity: original G-key misc wand toggle packet, architect HUD line, picked block display and block-outline preview for Equal Trade/Warding architect areas.
* Stage180 добавил continuous focus-use parity batch: held-use hooks for Fire/Shock/Excavation, TC4EmberEntity, fireloop/shock/rumble timing, excavation breakcount/last target tracking. Frost and Portable Hole remain one-shot because original TC4 classes do not implement `onUsingFocusTick`.
* После Stage180 осталось примерно 12–35 stage до полного точного переноса оригинального TC4.

В следующем проходе попробуй сделать сразу Stage181 + Stage182:

1. Stage181: exact focus client beam FX parity batch. Используй оригинальные `FXLightningBolt`, `beamCont`, `sparkle`, `excavateFX`, `REHWandHandler` как источник правды. Перенеси визуал Fire/Shock/Excavation continuous beam ближе к оригиналу через Forge 1.19.2 client adapter, без новых эффектов.
2. Stage182: focus animation/use state parity. Подключи оригинальные `WandFocusAnimation.CHARGE/WAVE` semantics к WandItem renderer/use state, чтобы убрать fake vanilla bow-like use animation и приблизить held wand visuals к оригинальному ItemFocusBasic.
3. Используй оригинальные ItemFocusBasic, WandManager, IArchitect, ItemFocusFire/Shock/Excavation/Frost/PortableHole/Trade/Warding/Primal и projectile entity/client FX classes как источник правды.
4. Добавь аудиты `scripts/tc4_stage181_focus_beam_fx_audit.py` и `scripts/tc4_stage182_focus_animation_state_audit.py`.
5. Продолжай проверять drift: если найдёшь временный адаптер, который отличается от оригинала, исправь или явно пометь как Forge 1.19.2 adapter.
6. Не ломай golems, wands, aura/nodes, crucible, infusion, infusion enchantment index, taint, eldritch, worldgen, Thaumonomicon pages/icons/research progression/recipe gates/materialized recipes/object aspects/entity scan runtime/player scan knowledge/aspect decomposition/research note grid/completion/research table container/copy behavior/bonus aspects/wand focus base behavior/focus upgrade NBT/projectile entities/output textures.
7. В конце выдай новый ZIP, напиши что сделал, что будешь делать в следующем stage, сколько stage ещё осталось, и обнови универсальный prompt для продолжения.
