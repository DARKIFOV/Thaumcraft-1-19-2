Продолжи перенос оригинального Thaumcraft 4 с Minecraft 1.7.10 на Forge Minecraft 1.19.2. Продолжай строго с последнего архива Stage166 и не придумывай новые механики, предметы, рецепты, GUI, прогрессию, текстуры или поведение, пока не будет полностью перенесён оригинальный TC4.

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
- Stage165 исправил отход от оригинала: добавлен persistent ResearchTableBlockEntity/Menu, primary slot 0 = scribing tools, slot 1 = research note, как в оригинальном TileResearchTable.
- Stage166 поднял версию до 1.66.0, добавил ResearchTableContainerScreen, RequestResearchTableActionPacket, create/open/copy actions, RESEARCHDUPE copy gate и original drift audit.
- После Stage166 осталось примерно 26–51 stage до полного точного переноса оригинального TC4.

В следующем проходе попробуй сделать сразу Stage167 + Stage168:

1. Stage167: finish original GuiResearchTable visual parity. Улучшить layout/texture regions/slot positions/labels/no-ink feedback ближе к оригинальному `GuiResearchTable`, но без новых research rules.
2. Stage168: Research Note copy/dupe edge parity. Проверить оригинальное поведение `RESEARCHDUPE`, completed notes, copied notes, solved/unsolved NBT states and server sync.
3. Продолжай проверять drift: если найдёшь временный адаптер, который отличается от оригинала, исправь или явно пометь как Forge 1.19.2 adapter.
4. Добавь аудиты `scripts/tc4_stage167_gui_research_table_visual_audit.py`, `scripts/tc4_stage168_research_dupe_copy_audit.py` и обнови drift audit.
5. Не ломай golems, wands, aura/nodes, crucible, infusion, infusion enchantment index, taint, eldritch, worldgen, Thaumonomicon pages/icons/research progression/recipe gates/materialized recipes/object aspects/entity scan runtime/player scan knowledge/aspect decomposition/research note grid/completion/research table container/output textures.
6. В конце выдай новый ZIP, напиши что сделал, что будешь делать в следующем stage, сколько stage ещё осталось, и обнови универсальный prompt для продолжения.
