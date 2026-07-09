Продолжай перенос оригинального Thaumcraft 4 с Minecraft 1.7.10 на Forge Minecraft 1.19.2 строго поверх Stage383–402 ZIP. Главное правило: это строгий оригинальный TC4 parity-порт, без новых механик, предметов, рецептов, GUI, прогрессии, текстур или поведения, пока не перенесён оригинальный TC4.

Текущий последний stage: Stage383–402. В нём сделан визуальный parity-batch: оригинальные TC4 GUI/texture aliases для ключевых экранов и предметов, runtime wand textures из оригинального TC4 ModelWand, custom Thaumometer scanner renderer на original scanner.png, player goggles render layer на original goggles.png, Arcane Workbench aspect-cost icons через original aspect textures, audit `tc4_stage383_402_original_asset_gui_revealer_audit.py`.

Следующий stage должен продолжать не косметику, а реальные parity-узлы:
1. Aura Node renderer: проверить row/frame mapping оригинального `textures/misc/nodes.png`, node type/modifier alpha, blend, size, distance visibility, energized/stabilized визуалы. Не придумывать sprites; если нужен adapter — явно пометить Forge 1.19.2 adapter.
2. Aura Node HUD: довести расположение `hud.png` под оригинальный `GuiIngameForge`/IRevealer вид, с аспектами, type/modifier, current/base vis; убрать любые debug text/modern boxes.
3. Thaumometer: заменить временный flat scanner adapter на полноценный OBJ/model-port adapter из original `scanner.obj`/`scanner.png` либо максимально точное Forge 1.19.2 geometry port; не добавлять fake scan effects.
4. Goggles of Revealing: довести custom layer под original goggles geometry, не vanilla armor helmet; проверить overlay activation только по IRevealer/IGoggles rules.
5. Wands: проверить `ModelWand` UV, cap/rod scale, top/bottom cap offsets, focus cube/depth/ornament/runes; теперь textures/entity/wand заполнены original model textures, дальше нужно geometry parity.
6. Research Table: сверить `guiresearchtable2.png` coordinates, aspect palette, combine slots, copy icon, scribing tools/notes slots, note grid/thread visuals, drag/drop/copy behavior.
7. Arcane Workbench: сверить `gui_arcaneworkbench.png` slot coordinates, primal aspect icons/costs, wand slot, output behavior, no modern labels/buttons.
8. Infusion Matrix: продолжать полный parity крафта на матрице — все рецепты из ConfigRecipes, component pedestal order, instability/failure events, essentia suction/drain, FX packets, output/research gates/enchantment edge cases.

Перед упаковкой обязательно прогнать:
- python scripts/java_syntax_guard.py
- python scripts/github_ci_guard.py
- python scripts/github_static_audit.py
- python scripts/tc4_stage323_342_super_mega_original_parity_cleanup_audit.py
- python scripts/tc4_stage343_362_super_mega_infusion_node_thaumonomicon_audit.py
- python scripts/tc4_stage363_382_original_visual_runtime_parity_audit.py
- python scripts/tc4_stage383_402_original_asset_gui_revealer_audit.py
- новый audit следующего stage.

В конце выдать новый ZIP, отчёт, что сделано, что осталось, и честную оценку процента parity без выдумок.
