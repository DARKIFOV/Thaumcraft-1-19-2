Продолжай перенос оригинального Thaumcraft 4 с Minecraft 1.7.10 на Forge Minecraft 1.19.2 строго поверх Stage403–422 ZIP. Главное правило: это строгий оригинальный TC4 parity-порт, без новых механик, предметов, рецептов, GUI, прогрессии, текстур или поведения, пока не перенесён оригинальный TC4.

Текущий последний stage: Stage403–422. В нём сделан визуально-модельный parity batch: Thaumometer теперь рендерится по численным данным original scanner.obj + scanner.png, Goggles of Revealing получили multi-plane Forge 1.19.2 adapter на original goggles.png, Aura Node HUD рисует original node_bubble.png + кадр из original misc/nodes.png, in-world Aura Node renderer очищен от fake aspect-icon orbitals, расширена проверка byte-identical original TC4 textures для GUI/core items/wand cap/core icons, добавлен audit `tc4_stage403_422_original_model_gui_texture_audit.py`.

Следующий stage должен продолжать без выдумок:
1. Aura Node renderer: сверить точный TileNodeRenderer 1.7.10 — frame row mapping nodes.png, additive/translucent blend, alpha, depth mask, type/modifier/energized/stabilized visuals, distance visibility. Не добавлять fake sprites/effects.
2. Aura Node HUD/Goggles/Thaumometer: довести координаты hud.png, текущий/base vis, type/modifier labels и aspect layout под оригинал, без debug boxes.
3. Thaumometer: проверить scanner.obj UV orientation/scale/hand transforms против RenderItemThaumometer; если требуется, поправить только adapter constants.
4. Goggles: портировать ближе к original ModelGoggles geometry, не vanilla armor.
5. Wands: сверить ModelWand geometry/UVs/cap offsets/focus/depth/ornament/runes, убрать визуальные drift-адаптеры или явно пометить как Forge 1.19.2 adapter.
6. Research Table: точные координаты guiresearchtable2.png, aspect tray, combine slots, copy behavior, note grid/thread rendering, scribing tools and note slot handling.
7. Arcane Workbench: точные slot positions gui_arcaneworkbench.png, wand slot, aspect costs, output preview, recipe gates.
8. Infusion Matrix: продолжить полный parity крафта на матрице — all ConfigRecipes, pedestal/source order, instability/failure events, essentia suction/drain, FX packets, output/research gates/enchantment edge cases.

Перед упаковкой обязательно прогнать:
- python scripts/java_syntax_guard.py
- python scripts/github_ci_guard.py
- python scripts/github_static_audit.py
- python scripts/tc4_stage323_342_super_mega_original_parity_cleanup_audit.py
- python scripts/tc4_stage343_362_super_mega_infusion_node_thaumonomicon_audit.py
- python scripts/tc4_stage363_382_original_visual_runtime_parity_audit.py
- python scripts/tc4_stage383_402_original_asset_gui_revealer_audit.py
- python scripts/tc4_stage403_422_original_model_gui_texture_audit.py
- новый audit следующего stage.

В конце выдать новый ZIP, отчёт, что сделано, что осталось, и честную оценку процента parity без выдумок.
