# Stage139 — TC4 Whole Port Broad Original-Parity Sweep

Goal: stop treating systems as isolated patches and push the current 1.19.2 port toward the original Thaumcraft 4 loop in one broad pass.

## What changed

### Arcane Workbench

- All 69 TC4 shaped arcane recipe JSON files now carry an explicit `key` map.
- `ArcaneWorkbenchRecipe` reads `key` / `symbol_map` from JSON before falling back to inference.
- Pattern recipes now preserve the original TC4 meaning of repeated symbols, for example `AAA / ASA / AAA` consumes 8 `A` items and uses `S` as catalyst.
- This makes the workbench less approximate and closer to original `ConfigRecipes.addArcaneCraftingRecipe` vararg patterns.

### Research Note / Research Table

- Aspect links are tightened from Stage137's lenient distance-2 rule to direct TC4 aspect links.
- A note path can no longer skip an intermediate aspect.
- UI text now marks valid placements as direct TC4 links.
- Research key lookup is now case-insensitive so old TC4 keys, JSON keys and user-facing selections do not silently fail because of case drift.

### Whole-mod sweep markers

- Added `TC4Stage139WholePortSweep` to declare the systems currently backed by original TC4 source/data and to keep remaining systems honest.
- Added `scripts/tc4_stage139_whole_port_sweep_audit.py`.
- GitHub Actions now runs the Stage139 whole-port sweep audit.

## Validation

The following checks must pass before accepting the stage:

```text
python scripts/java_syntax_guard.py
python scripts/github_ci_guard.py
python scripts/github_static_audit.py
python scripts/tc4_texture_audit.py
python scripts/tc4_full_parity_audit.py
python scripts/tc4_wand_parity_audit.py
python scripts/tc4_book_table_workbench_audit.py
python scripts/tc4_stage137_core_loop_audit.py
python scripts/tc4_stage138_arcane_pattern_audit.py
python scripts/tc4_stage139_whole_port_sweep_audit.py
```

## Still not final TC4 parity

This stage is a broad sweep, not a finished full port. Remaining major work:

- full golemancy AI/tasks/animation parity;
- full eldritch structures/dimension/boss/progression parity;
- exact focus upgrade UI and all old focus upgrades;
- remaining old metadata/OreDictionary recipes that still need exact 1.19.2 IDs;
- exact renderers for every old TC4 tile model.
