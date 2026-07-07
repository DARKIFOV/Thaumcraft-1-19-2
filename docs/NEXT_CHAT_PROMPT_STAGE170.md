Продолжи перенос оригинального Thaumcraft 4 с Minecraft 1.7.10 на Forge Minecraft 1.19.2. Продолжай строго с последнего архива Stage170 и не придумывай новые механики, предметы, рецепты, GUI, прогрессию, текстуры или поведение, пока не будет полностью перенесён оригинальный TC4.

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
- Stage169 восстановил оригинальную модель `TileResearchTable.bonusAspects`: бонусные аспекты хранятся на столе, сохраняются в NBT, пересчитываются каждые 600 тиков через TC4-like `recalculateBonus`, и больше не сливаются сразу в player pool.
- Stage170 поднял версию до `1.70.0`: bonus aspects синхронизируются через block entity update tag/packet, отображаются в Research Table GUI, а placement research note потребляет player pool first или table bonus aspect, как в оригинальном TC4.
- После Stage170 осталось примерно 22–47 stage до полного точного переноса оригинального TC4.

В следующем проходе попробуй сделать сразу Stage171 + Stage172:

1. Stage171: Wand focus behavior parity batch — начать точный перенос поведения основных focus из оригинала: fire/frost/shock/excavation/trade/primal, с использованием оригинальных классов focus как источника правды.
2. Stage172: Wand focus cost/cooldown/effect sync parity — подключить vis cost, cooldown, particles/sounds/server effects ближе к TC4, без новых эффектов.
3. Не придумывай новые focus behavior. Всё брать из оригинальных wand/focus классов TC4 1.7.10.
4. Добавь аудиты `scripts/tc4_stage171_wand_focus_behavior_audit.py` и `scripts/tc4_stage172_wand_focus_cost_sync_audit.py`.
5. Продолжай проверять drift: если найдёшь временный адаптер, который отличается от оригинала, исправь или явно пометь как Forge 1.19.2 adapter.
6. Не ломай golems, wands, aura/nodes, crucible, infusion, infusion enchantment index, taint, eldritch, worldgen, Thaumonomicon pages/icons/research progression/recipe gates/materialized recipes/object aspects/entity scan runtime/player scan knowledge/aspect decomposition/research note grid/completion/research table container/copy behavior/bonus aspects/output textures.
7. В конце выдай новый ZIP, напиши что сделал, что будешь делать в следующем stage, сколько stage ещё осталось, и обнови универсальный prompt для продолжения.
