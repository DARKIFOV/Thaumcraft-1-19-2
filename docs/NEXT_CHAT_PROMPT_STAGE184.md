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
* Stage173 добавил FocusUpgradeType и FocusUpgradeRuntime: оригинальные IDs, texture/name/text keys, 5 rank slots and original `upgrade` NBT list.
* Stage174 добавил TC4FrostShardEntity, TC4ExplosiveOrbEntity, TC4ShockOrbEntity, TC4PrimalOrbEntity и projectile foundation.
* Stage175 подключил wand focus upgrade effect parity batch 1.
* Stage176 поднял projectile visual/sound/particle parity batch.
* Stage177 добавил WandManager/IArchitect area/picked-block adapter.
* Stage178 добавил projectile behavior parity batch.
* Stage179 добавил architect overlay/keybind packet parity.
* Stage180 добавил continuous focus-use parity для Fire/Shock/Excavation.
* Stage181 добавил focus client FX parity для beamCont/sparkle/excavateFX/FXLightningBolt adapter.
* Stage182 добавил focus animation/use-state parity и убрал fake vanilla bow animation.
* Stage183 добавил wand focus renderer/layer parity: focus cube, depth, ornament, rune texture, original focus colors.
* Stage186 исправил remaining focus behavior drift: own sounds, Equal Trade radius, Warding delay, Primal cost window, Fire range/event.
* После Stage186 осталось примерно 8–31 stage до полного точного переноса оригинального TC4.

В следующем проходе попробуй сделать сразу Stage185 + Stage186:

1. Stage185: wand/staff component renderer parity batch. Сверь `ItemWandRenderer`, `ModelWand`, wand/staff rod/cap/sceptre/runes transforms, staff scaling, cap stacking and glowing rod lightmap с оригинальным TC4. Не добавляй новые модели или текстуры.
2. Stage186: focus pouch/focus equip UI parity batch. Сверь `ItemFocusPouch`, `ItemFocusPouchBauble`, `GuiFocusPouch`, focus inventory behavior, pouch slots, equip/swap cycle and original NBT with текущим Forge 1.19.2 adapter.
3. Используй оригинальные `ItemWandCasting`, `ItemFocusBasic`, `ItemFocusPouch`, `ItemFocusPouchBauble`, `GuiFocusPouch`, `ItemWandRenderer`, `ModelWand` как источник правды.
4. Добавь аудиты `scripts/tc4_stage185_wand_component_renderer_audit.py` и `scripts/tc4_stage186_focus_pouch_equip_ui_audit.py`.
5. Продолжай проверять drift: если найдёшь временный адаптер, который отличается от оригинала, исправь или явно пометь как Forge 1.19.2 adapter.
6. Не ломай golems, wands, aura/nodes, crucible, infusion, infusion enchantment index, taint, eldritch, worldgen, Thaumonomicon pages/icons/research progression/recipe gates/materialized recipes/object aspects/entity scan runtime/player scan knowledge/aspect decomposition/research note grid/completion/research table container/copy behavior/bonus aspects/wand focus behavior/focus upgrade NBT/projectile entities/focus renderer/focus FX/output textures.
7. В конце выдай новый ZIP, напиши что сделал, что будешь делать в следующем stage, сколько stage ещё осталось, и обнови универсальный prompt для продолжения.
