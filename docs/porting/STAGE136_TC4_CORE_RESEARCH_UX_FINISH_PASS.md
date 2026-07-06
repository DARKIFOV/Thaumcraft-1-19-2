# Stage136 — TC4 Core Research UX Finish Pass

Goal: keep finishing the core Thaumcraft 4 progression loop instead of jumping to a new system.

## Arcane Workbench

- Server-to-client recipe sync now sends the **real arcane vis cost** from loaded recipe JSON instead of always showing `Ordo 2`.
- Server-to-client recipe sync now also sends TC4 metadata:
  - `tc4_key`
  - `tc4_kind`
  - shaped `pattern` rows
- The Arcane Workbench screen renders shaped TC4 recipe rows when pattern data exists.
- The screen still keeps the compatibility catalyst preview, but now shows the original TC4 recipe kind/key in the synced recipe note.

## Research Table / Research Note

- Research foundation primal pool is now granted once per player instead of every table interaction.
- Research Note placement no longer silently overwrites an existing aspect and consumes a new pool point.
- Research Note progress is now recalculated from required aspect coverage and connected path progress instead of simply adding `+1` per click.
- Research Note screen now shows a required-aspect checklist using original TC4 aspect requirements.

## Thaumonomicon

- Recipe pages now render a richer recipe visual area:
  - shaped pattern cards when TC4 pattern rows exist;
  - component previews for infusion/crucible/other recipe types;
  - aspect cost icons with quantities.
- Existing TC4 text pages, category progress, recipe keys and item icon resolver are kept.

## Validation

Expected checks:

```bash
python scripts/java_syntax_guard.py
python scripts/github_ci_guard.py
python scripts/github_static_audit.py
python scripts/tc4_texture_audit.py
python scripts/tc4_full_parity_audit.py
python scripts/tc4_wand_parity_audit.py
python scripts/tc4_book_table_workbench_audit.py
```

All checks should pass. Gradle still requires internet access for the wrapper/dependencies.
