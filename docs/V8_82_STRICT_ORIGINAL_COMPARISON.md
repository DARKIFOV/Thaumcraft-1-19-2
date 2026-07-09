# v8.82 — strict original TC4 comparison pass

## Why this batch exists
The v8.62 checks were too shallow for worldgen: they verified that Greatwood/Silverwood were not called from `seedChunkOnce(...)`, but they did not prove that no other player-tick/lazy backfill hook could still place world content. The new audit is lifecycle-based: it checks the event path, call sites, and forbidden player-tick placement bridges.

## Original TC4 points used for comparison
- `ThaumcraftWorldGenerator.generate(...)` runs from Minecraft world generation/population, not from player movement.
- Surface pass order in TC4 is vegetation first, then ores, then aura nodes/structures.
- Greatwood chance is `random.nextInt(25) == 7`, gated by biome support.
- Silverwood chance is `random.nextInt(60) == 3`, gated by magical/jungle/roofed-style biome support.
- Default natural aura node rarity is `Config.nodeRarity = 36`.

## Fixed in v8.82
- Removed active player-tick worldgen placement from `CommonEvents.onLevelTick(...)`.
- `TC4WorldgenRuntime.tickPlayerArea(...)` no longer places vegetation, ores, taint pockets, nodes, or Outer Lands content.
- `generateNewChunk(...)` is now the strict entrypoint for chunk-load/new-chunk placement.
- Greatwood/Silverwood are generated only through the new-chunk surface path.
- Natural aura nodes moved from player-proximity seeding to new-chunk seeding using TC4's default rarity baseline `36`.
- Outer Lands populate/maze generation moved behind the new-chunk path instead of the player tick bridge.
- Research note hex hit radius corrected to `12px^2 = 144.0D`.
- Stage167 and Stage643-662 audits were made semantic/forward-compatible instead of stale literal-token checks.

## Current port estimate
`76% complete / 24% remaining`.

## Still not 100%
Remaining high-risk areas: exact 1.19.2 configured/placed feature integration, full Greatwood/Silverwood shape parity, essentia tubes/jar suction priority, golem AI/seal behavior, research table visual polish, and deeper Thaumonomicon payload drift checks.
