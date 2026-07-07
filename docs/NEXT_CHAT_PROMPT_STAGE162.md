Продолжи перенос оригинального Thaumcraft 4 с Minecraft 1.7.10 на Forge Minecraft 1.19.2. Продолжай строго с последнего архива Stage162 и не придумывай новые механики, предметы, рецепты, GUI, прогрессию, текстуры или поведение, пока не будет полностью перенесён оригинальный TC4.

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
- Stage161 заменил fixed 19-slot/two-anchor rebuild note на TC4 axial hex grid: radius = 1 + complexity, 19/37/61 hexes, outer-ring anchors from all original ResearchItem tags, slot types 0/1/2.
- Stage162 поднял версию до 1.62.0 и сделал Research Note completion/consumption parity: all anchors must connect through exact original direct aspect links known to the player, placement consumes one pool aspect, clearing refunds one, solved notes prune disconnected non-anchor cells and convert via OriginalResearchBridge.
- Добавлены аудиты scripts/tc4_stage161_research_note_grid_parity_audit.py и scripts/tc4_stage162_research_note_completion_parity_audit.py.
- После Stage162 осталось примерно 30–55 stage до полного точного переноса оригинального TC4.

В следующем проходе попробуй сделать сразу Stage163 + Stage164:

1. Stage163: Research Table inventory/ink/scribing-tools parity. Перенеси оригинальное поведение стола: слот scribing tools, слот research note, ink consumption, bonus aspects, sync и невозможность редактировать note без инструментов.
2. Stage164: Research note GUI polish parity. Приведи экран ближе к оригинальному GuiResearchTable: drag/drop aspect placement, TC4 hex hit-test q/r, anchor/render colors, completion feedback and copy behavior where applicable.
3. Не придумывай новые research rules. Бери оригинальные TileResearchTable, GuiResearchTable, ResearchManager и HexUtils как источник правды.
4. Добавь аудиты scripts/tc4_stage163_research_table_inventory_ink_audit.py и scripts/tc4_stage164_research_note_gui_parity_audit.py.
5. Не ломай golems, wands, aura/nodes, crucible, infusion, infusion enchantment index, taint, eldritch, worldgen, Thaumonomicon pages/icons/research progression/recipe gates/materialized recipes/object aspects/entity scan runtime/player scan knowledge/aspect decomposition/research note grid/completion/output textures.
6. В конце выдай новый ZIP, напиши что сделал, что будешь делать в следующем stage, сколько stage ещё осталось, и обнови универсальный prompt для продолжения.
