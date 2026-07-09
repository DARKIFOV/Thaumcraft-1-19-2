Продолжай от Stage583–602. Делай следующий batch Stage603–622 строго как TC4 1.7.10 parity-port на Forge 1.19.2. Не добавляй новые предметы, GUI, рецепты, research keys, прогрессию, текстуры или поведение.

Фокус Stage603–622:
1. Research Table pixel/mouse parity: сверить GuiResearchTable координаты, draw order, aspect palette, combine slots, copy icon, note slot, tooltips, правый/левый клик.
2. Scribing Tools: ink/damage/NBT/costs как в оригинале; paper/ink расход на создание/копирование/редактирование note.
3. Research Note: точнее оригинальный note grid/RNG/слоты/удаление blank hexes/complete flow/copy behavior.
4. ConfigResearch audit: keys, categories, coords, parents, hiddenParents, siblings, flags, warp, pages, icons, recipe gates.
5. Не ломай Thaumonomicon, Arcane Workbench, Infusion Matrix, aura/nodes, wands, foci, essentia, golems, eldritch, taint/worldgen.

Перед упаковкой прогони:
- python scripts/java_syntax_guard.py
- python scripts/github_ci_guard.py
- python scripts/github_static_audit.py
- python scripts/tc4_stage583_602_research_config_parity_audit.py
- новый audit Stage603–622

В конце выдай ZIP, отчёт, что сделано, что осталось и честную оценку процента parity.
