Продолжи перенос оригинального Thaumcraft 4 с Minecraft 1.7.10 на Forge Minecraft 1.19.2. Продолжай строго с последнего архива Stage172 и не придумывай новые механики, предметы, рецепты, GUI, прогрессию, текстуры или поведение, пока не будет полностью перенесён оригинальный TC4.

Главное правило: это должен быть строгий оригинальный TC4 parity-порт. Если код 1.7.10 нельзя перенести напрямую, сделай адаптер под Forge 1.19.2, но сохрани оригинальные данные, research keys, страницы, иконки, аспекты, рецепты, координаты, флаги, родителей, hidden parents, siblings, warp, GUI-логику, звуки, текстуры и поведение.

Текущий статус:

- Stage147 вернул проект к строгому оригинальному TC4 parity.
- Stage148 завершил explicit research icon coverage.
- Stage149 завершил strict ResearchPage parity.
- Stage150 добавил strict research metadata parity.
- Stage151 добавил runtime progression bridge.
- Stage152 добавил strict recipe unlock parity.
- Stage153 материализовал дополнительные оригинальные infusion-рецепты и исправил infusion lookup по catalyst + component pedestals.
- Stage154 добавил dedicated TC4InfusionEnchantmentIndex.
- Stage155 расширил exact recipe resolver.
- Stage156 материализовал 10 exact original focus recipes.
- Stage157 добавил exact object/entity aspect database.
- Stage158 добавил Thaumometer entity scan runtime.
- Stage159 поднял сканы Thaumometer на уровень player thaum data.
- Stage160 добавил original aspect decomposition foundation.
- Stage161 заменил fixed rebuild note grid на TC4 axial hex grid.
- Stage162 сделал Research Note completion/consumption parity.
- Stage163 добавил Research Table ink/scribing tools validation adapter.
- Stage164 добавил Research Note GUI drag/drop and q/r axial-grid hit testing.
- Stage165 добавил persistent ResearchTableBlockEntity/Menu с оригинальными slot 0/slot 1.
- Stage166 добавил ResearchTableContainerScreen/actions and drift audit.
- Stage167 поднял Research Table visual parity.
- Stage168 исправил RESEARCHDUPE copy parity.
- Stage169 восстановил оригинальную модель TileResearchTable.bonusAspects.
- Stage170 синхронизировал bonus aspects и подключил их к расходу при research note placement.
- Stage171 начал wand focus behavior parity: fire/frost/shock/excavation/equal trade/primal теперь используют TC4-like base behavior adapters вместо старых грубых действий.
- Stage172 поднял версию до `1.72.0`, добавил base cooldowns из оригинальных focus classes, primal random vis cost 50..250 per primal aspect, failure sound path и audits `tc4_stage171_wand_focus_behavior_audit.py`, `tc4_stage172_wand_focus_cost_sync_audit.py`.
- После Stage172 осталось примерно 20–45 stage до полного точного переноса оригинального TC4.

В следующем проходе попробуй сделать сразу Stage173 + Stage174:

1. Stage173: Focus upgrade NBT/rank parity foundation. Перенеси оригинальные FocusUpgradeType IDs/ranks/textures and focus upgrade NBT structure без включения fake upgrade effects.
2. Stage174: Projectile entity parity foundation for foci. Начни перенос оригинальных entity adapters для EntityFrostShard, EntityExplosiveOrb, EntityShockOrb and EntityPrimalOrb под 1.19.2, чтобы focus behavior перестал быть только ray/beam adapter.
3. Не придумывай новые focus behavior. Всё брать из оригинальных wand/focus/entity classes TC4 1.7.10.
4. Добавь аудиты `scripts/tc4_stage173_focus_upgrade_nbt_audit.py` и `scripts/tc4_stage174_focus_projectile_entity_audit.py`.
5. Продолжай проверять drift: если найдёшь временный адаптер, который отличается от оригинала, исправь или явно пометь как Forge 1.19.2 adapter.
6. Не ломай golems, wands, aura/nodes, crucible, infusion, infusion enchantment index, taint, eldritch, worldgen, Thaumonomicon pages/icons/research progression/recipe gates/materialized recipes/object aspects/entity scan runtime/player scan knowledge/aspect decomposition/research note grid/completion/research table container/copy behavior/bonus aspects/wand focus base behavior/output textures.
7. В конце выдай новый ZIP, напиши что сделал, что будешь делать в следующем stage, сколько stage ещё осталось, и обнови универсальный prompt для продолжения.
