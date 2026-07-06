# Stage135 — TC4 Workbench / Research Table / Thaumonomicon Completion Pass

Goal: stop adding broad placeholder systems and finish the current core TC4 progression loop.

## Arcane Workbench

Original TC4 reference classes:

- `thaumcraft/client/gui/GuiArcaneWorkbench.java`
- `thaumcraft/common/container/ContainerArcaneWorkbench.java`
- `thaumcraft/common/tiles/TileArcaneWorkbench.java`

Changes ported/adapted:

- Slot spacing moved closer to TC4: 3x3 grid uses 24px spacing, wand/output positions match the old visual layout more closely.
- The old Stage120/121 separate catalyst slot remains for compatibility, but Stage135 also accepts the catalyst directly from the 3x3 grid.
- Recipe ghost rendering is visible in the menu: catalyst, ingredients, result, research lock and vis cost.
- Wand vis consumption still uses Stage134 cap modifiers, so cost display and crafting remain tied to the repaired wand system.

## Research Table

Original TC4 reference classes:

- `thaumcraft/client/gui/GuiResearchTable.java`
- `thaumcraft/common/items/ItemResearchNotes.java`

Changes ported/adapted:

- Right-clicking a research note on the table opens the note puzzle instead of incrementing a shortcut progress counter.
- Solved notes can be converted into completed research from the table.
- Invalid note placements no longer consume aspect pool entries.
- Paper + scribing tools flow remains targeted to the selected/first-available TC4 research.

## Thaumonomicon

Original TC4 reference classes:

- `thaumcraft/client/gui/GuiResearchBrowser.java`
- `thaumcraft/client/gui/GuiResearchRecipe.java`
- `thaumcraft/client/gui/GuiResearchPopup.java`

Changes ported/adapted:

- Category header shows completed/available counts.
- Research pages show page ranges and mouse-wheel navigation.
- Recipe pages attempt to render resolved TC4 research-item icons from the metadata-split registry mapping.
- Existing TC4 map positions, categories, parents, hidden parents, pages and recipe keys remain source-driven by Stage116/118 bridges.

## Validation

Run:

```bash
python scripts/java_syntax_guard.py
python scripts/github_ci_guard.py
python scripts/github_static_audit.py
python scripts/tc4_texture_audit.py
python scripts/tc4_full_parity_audit.py
python scripts/tc4_book_table_workbench_audit.py
```

Expected Stage135 result: all checks pass and missing texture references remain zero.
