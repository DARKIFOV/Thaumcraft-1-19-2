# Stage643-662 — Research Note gate / Thaumonomicon visible parity

This batch continues strict TC4 1.7.10 parity work on the research path. It does not add new mechanics, new recipes, new GUI controls, or a second research-note workflow.

Changes:

- Research-note edit packets now resolve the note from the open Research Table slot 1 only. The older held-note fallback is no longer used for table edit/clear/solve packets.
- Added explicit table-only helpers in `ResearchTableInventoryRuntime` for slot 0/slot 1 lookup, so future stages do not reintroduce parallel inventory/hand editing.
- Research-note hex hit detection now uses the shared TC4 table parity ledger: 20x18 drawn hex and a tighter 10px radius hit circle. This is closer to the original GUI hex feel than the old broad 12px adapter radius.
- Completed/solved research notes can no longer be edited or cleared through the server solver. They must be completed/copied through the original-style table actions.
- The dragged aspect preview no longer draws a modern colored square behind the icon.
- Thaumonomicon keeps warp in research data/NBT, but no longer paints a raw `Warp: N` adapter label on the copied TC4 book texture.

Still adapter / not complete:

- Research Table still needs deeper pixel/mouse parity against original `GuiResearchTable` and `GuiResearchRecipe`.
- Full page/icon/recipe type sweep for all `ConfigResearch` entries still needs more passes.
- Arcane Workbench, Infusion Matrix, Aura Nodes, wands/foci, golems, taint, eldritch and worldgen still need more parity stages.
