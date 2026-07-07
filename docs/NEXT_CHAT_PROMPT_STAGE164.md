Продолжи перенос оригинального Thaumcraft 4 с Minecraft 1.7.10 на Forge Minecraft 1.19.2. Продолжай строго с последнего архива Stage164 и не придумывай новые механики, предметы, рецепты, GUI, прогрессию, текстуры или поведение, пока не будет полностью перенесён оригинальный TC4.

Главное правило: это должен быть строгий оригинальный TC4 parity-порт. Если код 1.7.10 нельзя перенести напрямую, сделай адаптер под Forge 1.19.2, но сохрани оригинальные данные, research keys, страницы, иконки, аспекты, рецепты, координаты, флаги, родителей, hidden parents, siblings, warp, GUI-логику, звуки, текстуры и поведение.

Текущий статус:

- Stage147 вернул проект к строгому оригинальному TC4 parity.
- Stage148 завершил explicit research icon coverage.
- Stage149 завершил strict ResearchPage parity.
- Stage150 добавил strict research metadata parity.
- Stage151 добавил runtime progression bridge.
- Stage152 добавил strict recipe unlock parity.
- Stage153 материализовал дополнительные оригинальные infusion-рецепты и исправил infusion lookup по catalyst + component pedestals.
- Stage154 добавил dedicated TC4InfusionEnchantmentIndex для всех 24 оригинальных infusion enchantment entries.
- Stage155 расширил exact recipe resolver.
- Stage156 материализовал 10 exact original focus recipes.
- Stage157 добавил exact object/entity aspect database.
- Stage158 добавил Thaumometer entity scan runtime и legacy scan trigger aliases.
- Stage159 поднял сканы Thaumometer на уровень player thaum data.
- Stage160 добавил original aspect decomposition foundation.
- Stage161 заменил fixed rebuild note grid на TC4 axial hex grid.
- Stage162 сделал Research Note completion/consumption parity.
- Stage163 поднял Research Table inventory/ink/scribing tools parity: `ResearchTableInventoryRuntime`, slot 0 scribing tools, slot 1 research note, ink gate on create/edit/place/clear, TC4-style RESEARCHER1/RESEARCHER2 clear refund chance.
- Stage164 поднял версию до `1.64.0` и добавил Research Note GUI parity: drag/drop aspect placement and `ResearchNoteGrid.hitTest(...)` q/r axial-grid hit testing.
- После Stage164 осталось примерно 28–53 stage до полного точного переноса оригинального TC4.

В следующем проходе попробуй сделать сразу Stage165 + Stage166:

1. Stage165: persistent Research Table block entity/container parity. Замени временный virtual-slot adapter на настоящий block entity inventory с двумя слотами, как оригинальный `TileResearchTable`: slot 0 scribing tools, slot 1 research note. Сохрани поведение Stage163 как shared validation/runtime.
2. Stage166: original GuiResearchTable visual/copy polish. Перенеси больше поведения оригинального `GuiResearchTable`: completed-note copy behavior, RESEARCHDUPE checks, no-ink feedback, closer original layout, and sync from the real table container.
3. Не придумывай новые rules. Бери оригинальные `TileResearchTable`, `ContainerResearchTable`, `GuiResearchTable`, `ResearchManager` и `HexUtils` как источник правды.
4. Добавь аудиты `scripts/tc4_stage165_research_table_block_entity_audit.py` и `scripts/tc4_stage166_research_table_gui_copy_audit.py`.
5. Не ломай golems, wands, aura/nodes, crucible, infusion, infusion enchantment index, taint, eldritch, worldgen, Thaumonomicon pages/icons/research progression/recipe gates/materialized recipes/object aspects/entity scan runtime/player scan knowledge/aspect decomposition/research note grid/completion/research table ink adapter/research note GUI/output textures.
6. В конце выдай новый ZIP, напиши что сделал, что будешь делать в следующем stage, сколько stage ещё осталось, и обнови универсальный prompt для продолжения.
