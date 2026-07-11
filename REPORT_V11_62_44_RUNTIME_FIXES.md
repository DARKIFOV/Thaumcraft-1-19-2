# Thaumcraft Legacy Rebuild v11.62.44

Runtime repair batch for Forge 1.19.2 / Java 17.

## Fixed

- Recentered held wands, staffs and sceptres. The Forge renderer no longer reapplies the old `+1Y` transform after hand positioning.
- Replaced `RenderType.eyes` on world aura nodes with standalone TC4 additive/alpha render types using LEQUAL or reveal-through-walls depth state and no depth writes.
- Removed invented vanilla enchant/end-rod particles from aura-node blocks.
- Added an authoritative player-tick fallback for the Thaumometer. Cancelled chest/entity interactions now retain and complete the original 20 stable tick scan on the server.
- Restored a visible functional `thaumonomicon_cheat` item. Right-clicking it unlocks every registered research and aspect, then opens the Thaumonomicon.
- Migrates the hidden legacy `tc4_thaumonomiconcheat` item to the functional replacement.
- Restored TC4 Nitor as an invisible full-light block represented by magical particles rather than a crossed item texture.

## Verification

- Java compilation succeeds against Minecraft 1.19.2 / Forge 43.5.2.
- JSON and all item model chains validate.
- The runtime regression guard covers all five repaired systems.

Client-side in-game calibration is still required for exact wand hand placement on every player skin/FOV combination.
