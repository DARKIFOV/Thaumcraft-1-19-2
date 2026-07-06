# Thaumcraft Legacy Rebuild — Stage144

Stage144 continues the strict TC4 1.7.10 → Forge 1.19.2 port. This checkpoint moves into Eldritch progression, Crimson cultists, split Warp buckets, portal/altar flow and taint spread while preserving the Stage143 Golemancy, research, wand, node, Crucible and Infusion work.

Validation: Java syntax guard, GitHub CI guard, GitHub static audit, Stage140–144 focused audits, Stage143 golemancy audit, wand audit, book/table/workbench audit, full parity audit, texture audit and Stage144 Eldritch/Warp/Taint audit pass. Full Gradle build was attempted, but the local sandbox cannot resolve services.gradle.org for Gradle wrapper download.

# Thaumcraft Legacy Rebuild — Stage138 TC4 Arcane Exact Pattern Completion

This package continues the strict Thaumcraft 4 port for Minecraft Forge 1.19.2. Stage138 focuses on the core progression loop and fixes exact TC4 shaped arcane recipe pattern handling.

Key Stage138 changes:

- Infers TC4 arcane pattern symbol maps from preserved pattern rows, catalyst and component lists.
- Validates shaped Arcane Workbench recipes by actual pattern slots instead of loose one-of-each ingredient checks.
- Consumes repeated pattern symbols correctly, for example `AAA / ASA / AAA` now requires all repeated `A` slots.
- Keeps the compatibility catalyst slot, but only for the inferred catalyst symbol.
- Uses the same inferred symbol map in the Arcane Workbench ghost layout and Thaumonomicon recipe pages.
- Adds Stage138 audit coverage in GitHub Actions.

Build target: Forge 1.19.2, Java 17.


Forge 1.19.2 strict Thaumcraft 4 porting branch.

## Stage136 — TC4 Core Research UX Finish Pass

This stage continues the same core loop from Stage135: Arcane Workbench, Research Table, Research Notes and Thaumonomicon.

### Main changes

- Arcane Workbench recipe sync now sends exact recipe vis costs instead of showing `Ordo 2` for every synced recipe.
- Arcane Workbench recipe sync now sends TC4 `tc4_key`, `tc4_kind` and shaped pattern rows.
- Arcane Workbench screen renders shaped pattern previews when TC4 pattern data exists.
- Research Table no longer grants starter primal research pool points every time it is clicked.
- Research Notes no longer silently overwrite filled aspect slots and waste aspect pool points.
- Research Note progress is recalculated from required aspects + connected path progress.
- Research Note screen shows a required-aspect checklist.
- Thaumonomicon recipe pages now show pattern/component/aspect-cost visuals instead of only text fields.

### Validation

```bash
python scripts/java_syntax_guard.py
python scripts/github_ci_guard.py
python scripts/github_static_audit.py
python scripts/tc4_texture_audit.py
python scripts/tc4_full_parity_audit.py
python scripts/tc4_wand_parity_audit.py
python scripts/tc4_book_table_workbench_audit.py
```

Gradle build still requires internet access for the Gradle wrapper and Forge dependencies.


## Stage137 — TC4 Core Loop Precision Pass

Stage137 tightens the main TC4 progression loop instead of adding a new system:

- Research Notes now support editable slot clearing with aspect refund.
- Placement must connect to a compatible neighbouring aspect.
- The note screen shows valid green placement slots and a TC4 aspect path hint.
- Arcane Workbench now enforces exact shaped 3x3 layouts when the TC4 materialized recipe data supports it.
- Versions are bumped to 1.37.0.

## Stage140 — TC4 Golemancy checkpoint

Stage140 begins the broad original Thaumcraft 4 Golemancy parity pass. It adds TC4-style golem body materials, core modes, NBT profile storage, material-dependent stats, container dropoff, crop harvesting, lumber behavior, guard behavior and material-aware renderer feedback.

This is still a dev-stage, not a finished release. The next passes should continue exact seal/filter GUI, all upgrades, advanced core behavior and final TC4 model renderer parity.



## Stage141 — TC4 Golemancy controls / filters / upgrades

Stage141 continues the Golemancy branch by adding TC4-style golem upgrades, filters, task markers and bell modes. Golems now store complete configuration in NBT, support input/output/guard/work markers, can be retasked with the bell, and include additional original core modes such as bodyguard, butcher, fish, liquid, essentia and patrol.
