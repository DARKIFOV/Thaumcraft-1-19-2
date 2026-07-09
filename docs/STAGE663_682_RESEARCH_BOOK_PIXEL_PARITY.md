# Stage663-682 — Research Table / Thaumonomicon visible parity cleanup

This batch continues strict TC4 1.7.10 parity work from Stage643-662. It does not add new mechanics, recipes, research keys, items, textures or GUI concepts.

Changes:

- Centralized more mouse hitboxes in `TC4ResearchTableParity`: note slot, scribing slot, copy icon, page arrows, combine arrow and aspect icons.
- Research Table container and legacy aspect screen now use those guarded hitbox helpers instead of repeated inline coordinates.
- Research Note screen now refuses client-side placement when the synced note is already solved, matching the server-side solved-note guard.
- Removed modern helper/instruction tooltips from Research Table / Research Note aspect page arrows and combine/copy/note hotzones. The visible table stays on the original `guiresearchtable2.png` parchment and item/aspect tooltip paths.
- Thaumonomicon browser no longer paints rebuild completion/availability counters, click-state text, raw aspect maps or warp/status lines on top of `gui_research.png`.
- Thaumonomicon research pages no longer draw raw page type labels or recipe debug panel overlays; page type and recipe gate data remain preserved in runtime/audit data.

Still adapter / not complete:

- Full `GuiResearchRecipe` pixel parity still needs screenshot-level comparison against TC4 1.7.10.
- Thaumonomicon still needs a complete ConfigResearch page/icon/gate sweep for every entry.
- Arcane Workbench, Infusion Matrix, Aura Nodes, wands/foci, golems, taint, eldritch and worldgen remain partially adapter-based.
