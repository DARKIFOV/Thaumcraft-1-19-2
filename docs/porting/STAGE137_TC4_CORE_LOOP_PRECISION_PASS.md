# Stage137 — TC4 Core Loop Precision Pass

This stage continues the strict Thaumcraft 4 port work around the core loop:
Thaumonomicon → Research Note / Research Table → Arcane Workbench.

## Research Note / Research Table

- Added server-side removal/refund for editable research note slots.
- Right-clicking an editable placed aspect in the Research Note screen clears it and returns the aspect point to the player's research pool.
- Start and end anchors remain fixed and cannot be removed.
- Aspect placement now requires a compatible neighbouring aspect, matching the TC4 note puzzle more closely than the earlier free-placement grid.
- Added shortest-path helpers over the original TC4 aspect graph.
- The Research Note screen now shows valid green placement slots and a path hint between anchor aspects.
- The screen now draws diagonal/hex links between nodes instead of rectangular one-pixel boxes.

## Arcane Workbench

- Shaped TC4 recipe rows are now enforced when the materialized recipe data contains a one-to-one pattern/ingredient list.
- Pattern-shaped recipes consume the exact pattern slots rather than using only a loose count-based matcher.
- Legacy/materialized recipes with incomplete symbol mapping still fall back to Stage136 matching instead of being rejected.

## Prior-system safety

This stage intentionally avoids changing Crucible, Infusion, Aura Node and wand runtime behaviour. It adds precision around the research/crafting loop and keeps the previous parity audits runnable.
