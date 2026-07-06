# Stage142 — TC4 Golemancy GUI / Live Configuration / Renderer Finish Pass

Goal: continue the Stage140–141 Golemancy port toward original Thaumcraft 4 behavior instead of leaving golems as task mobs with fixed NBT.

## Added / changed

- Existing golems can now be configured after spawning by directly right-clicking the golem.
- Golem Bell now has a `status` mode that prints owned golem state, material, core, slots, work range, priority and wait state.
- Golem Task Marker now stores work radius and priority metadata, not only a position.
- Golem entity persists task radius and priority in NBT.
- Golem renderer is no longer a single block cube. It renders body, head, arms and legs with material-based scale and subtle animation.
- Live interactions support:
  - marker assignment;
  - filter assignment/clearing;
  - upgrade installation;
  - profile retasking from a configured Golem Core;
  - shift-empty/bell wait toggle and status feedback.

## Still not perfect TC4

This stage improves control and visual parity, but the old TC4 seal GUI/grid is not fully reproduced yet. The next Golemancy-focused step should add a proper configuration screen for seals/filters and exact animated golem models rather than block-part rendering.
