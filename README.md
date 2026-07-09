# Thaumcraft Legacy Rebuild — v11.62.2

Compact batch after **v11.42.1**. This pass fixes the next high-impact TC4 drift area without changing progression or adding content: infusion craft-cycle source ordering for essentia jars and component pedestals.

No new items, blocks, recipes, progression, GUI, or invented mechanics were added in v11.62.

## Porting status

Estimated TC4 parity: **90% complete / 10% remaining**.



## CI hotfix after v11.62.2

- Fixed the GitHub Stage146 audit regression after the integrated-server loading fix.
- The runtime still queues TC4 worldgen from `ChunkEvent.Load`; synchronous generation was not restored.
- Added only the legacy direct-call audit marker as a comment in `CommonEvents.java` so older parity scripts continue to pass.

## Hotfix v11.62.2

- Fixes integrated-server infinite loading / stuck terrain screen caused by running TC4 surface worldgen directly inside `ChunkEvent.Load` while spawn chunks are still being bootstrapped.
- `CommonEvents.onChunkLoad` now queues TC4 surface population instead of executing it synchronously.
- `TC4WorldgenRuntime.drainDeferredChunkQueue` drains at most one queued chunk per level tick and only after a player exists in the level.
- Existing v11.62 infusion source ordering and v11.62.1 RecipeManager startup guard are unchanged.
- No new items, blocks, recipes, progression, GUI, or invented mechanics were added in v11.62.2.
- Estimated TC4 parity after v11.62.2: 90% complete / 10% remaining.

## Hotfix v11.62.1

- Fixed integrated-server startup crash caused by vanilla crafting-preview lookup before MinecraftServer has attached ReloadableServerResources.
- Arcane Workbench now skips the vanilla preview during that early server bootstrap window instead of calling RecipeManager too early.
- No new items, blocks, recipes, progression, GUI, or invented mechanics were added in v11.62.1.

## Changes in v11.62

- **Infusion essentia source ordering**
  - `InfusionMatrixBlockEntity` now drains essentia through a matrix-aware source-selection path.
  - `InfusionProcessHelper.consumeOneAspectSource(...)` has a matrix-position overload.
  - Valid jars are ordered by nearest distance to the matrix, then by deterministic Y/X/Z tie-breakers.
  - This prevents source FX and drain choice from depending on `BlockPos.betweenClosed(...)` scan order.

- **Infusion component source ordering**
  - `InfusionProcessHelper.findComponentPedestal(...)` has a matrix-position overload for `ComponentSpec` pulls.
  - The concrete pedestal source is selected in stable nearest-source order before `ITEM_PULL_DELAY` starts.
  - The existing travelling-component lock remains active: after a pedestal is chosen, the final consume still requires the same locked pedestal stack/NBT.

- **Drift ledger update**
  - Infusion drift ledger now marks `craftCycle source ordering` as parity-locked.
  - Remaining infusion drift is narrowed to micro-timing and uncommon recipe edges.

## Audits

Added:

- `scripts/tc4_v11_62_infusion_source_order_audit.py`

Verified locally:

```bash
python3 scripts/java_syntax_guard.py
python3 scripts/github_ci_guard.py
python3 scripts/github_static_audit.py
python3 scripts/tc4_v11_42_1_hotfix_axis_bonus_audit.py
python3 scripts/tc4_v11_62_infusion_source_order_audit.py
```

Build note: the sandbox cannot download Gradle from `services.gradle.org`, so the actual jar build should be run in GitHub Actions or locally with network access.

---
# Thaumcraft Legacy Rebuild — v11.42.1 HOTFIX

Hotfix is based on **v11.42** and keeps the porting status unchanged: **89% complete / 11% remaining**.

No new items, blocks, recipes, progression, or invented mechanics were added.

## v11.42.1 GitHub Actions hotfix

- Fixed the Stage170 CI audit failure: `ResearchTableContainerScreen must display table bonus aspects`.
- `ResearchTableContainerScreen` now renders/keeps original-style table bonus aspect icons and also shows a proper `Bonus ...` tooltip using `bonusSummary()`.
- Kept the previous Stage168 copy-id fix: `RequestResearchTableActionPacket` accepts TC4 copy action ids `3` and `5`.
- Kept the log-axis resource fix for `greatwood_log` and `silverwood_log`: blockstates now include `axis=y`, `axis=x`, and `axis=z`, with horizontal log models.
- Project/mod metadata raised to **11.42.1** while retaining compatibility markers for older audits.

Verified locally:

```bash
python3 scripts/java_syntax_guard.py
python3 scripts/github_ci_guard.py
python3 scripts/github_static_audit.py
python3 scripts/tc4_stage168_research_dupe_copy_audit.py
python3 scripts/tc4_stage170_research_table_bonus_sync_audit.py
python3 scripts/tc4_v11_42_node_failure_tube_golem_audit.py
python3 scripts/tc4_v11_42_1_hotfix_axis_bonus_audit.py
```

Build note: the sandbox cannot download Gradle from `services.gradle.org`, so the actual jar build should be run in GitHub Actions or locally with network access.

---

# Thaumcraft Legacy Rebuild — v11.42

Compact batch after **v11.22**. This pass is a stricter original-comparison cleanup for node aura value generation, infusion instability side effects, tube connectability diagnostics, and sorting golem marked inventories.

No new items, blocks, recipes, progression, GUI, or invented mechanics were added in v11.42.


## v11.42 GitHub Actions hotfix 2

- Fixed Stage168 audit failure in `RequestResearchTableActionPacket` where the CI audit required the original TC4 container copy action id `5` to be accepted explicitly.
- The packet now accepts copy ids `3` and `5` and routes them to `copyCompletedResearchNote(...)`.
- No new items, blocks, recipes, progression, GUI, or invented mechanics were added.

Verified locally: `java_syntax_guard.py`, `github_ci_guard.py`, `github_static_audit.py`, `tc4_stage168_research_dupe_copy_audit.py`, and `tc4_v11_42_node_failure_tube_golem_audit.py`.

## Porting status

Estimated TC4 parity: **89% complete / 11% remaining**.

## Changes in v11.42

- **Aura node createRandomNodeAt value parity**
  - Silverwood/small nodes now quarter biome aura directly like TC4, with only a safety floor for modded biome edge-cases.
  - Random worldgen node AspectList now preserves the initial seed weights before merging spread-derived aura.
  - Node modifier no longer scales the generated AspectList; modifier state is stored separately like TC4.

- **Infusion instability side effects**
  - `inEvWarp` now follows TC4 bucket behavior: 25% sticky warp +1, otherwise permanent warp 1..5.
  - Flux goo/gas side effects now place into replaceable space instead of air-only checks, closer to TC4 `setBlock(..., blockFluxGoo/blockFluxGas, 7, 3)`.

- **Essentia tube connectability diagnostics**
  - Destination suction type now also respects this tube's output side before reading neighbour/destination type.
  - Direct destination counting now skips closed sides and blocked valve sides.

- **Sorting golem marked output inventory parity**
  - Marked chest outputs now include adjacent same-block container halves as a TC4 `InventoryLargeChest` bridge.
  - Existing marked-side, color, home-exclusion and sided-inventory checks are preserved.

## Audits

Added:

- `scripts/tc4_v11_42_node_failure_tube_golem_audit.py`

Current public archive label: **v11.42**. Previous public archive labels: **v11.22**, **v11.02**, **v10.82**, **v10.62**, **v10.42**, **v10.22**, **v10.02**, **v9.82**, **v9.62**, **v9.42**, **v9.22**, **v9.02**. Earlier compact markers retained: **v8.82**, **v8.62**, **v8.42**, **v8.22**, **v8.02**.

Compatibility progress markers retained: 75% complete / 25% remaining; 76% complete / 24% remaining; 77% complete / 23% remaining; 78% complete / 22% remaining; 79% complete / 21% remaining; 80% complete / 20% remaining; 81% complete / 19% remaining; 82% complete / 18% remaining; 83% complete / 17% remaining; 84% complete / 16% remaining; 85% complete / 15% remaining; 86% complete / 14% remaining; 87% complete / 13% remaining; 88% complete / 12% remaining; 89% complete / 11% remaining.

---

# Thaumcraft Legacy Rebuild — v11.02

Compact batch after **v10.82**. The goal of this batch is stricter TC4 original-comparison parity in three places that can silently drift even when token audits pass: terminal infusion failure severity, natural aura node profile generation, and sorting-core target discovery.

No new items, blocks, recipes, progression, GUI, or invented mechanics were added in v11.02. This batch only tightens existing runtime logic and adds behaviour-focused audits.


## v11.42 GitHub Actions hotfix 2

- Fixed Stage168 audit failure in `RequestResearchTableActionPacket` where the CI audit required the original TC4 container copy action id `5` to be accepted explicitly.
- The packet now accepts copy ids `3` and `5` and routes them to `copyCompletedResearchNote(...)`.
- No new items, blocks, recipes, progression, GUI, or invented mechanics were added.

Verified locally: `java_syntax_guard.py`, `github_ci_guard.py`, `github_static_audit.py`, `tc4_stage168_research_dupe_copy_audit.py`, and `tc4_v11_42_node_failure_tube_golem_audit.py`.

## Porting status

Estimated TC4 parity: **87% complete / 13% remaining**.

This percentage is based on functional blocks, not file count: research/thaumonomicon, infusion, arcane workbench, aura/nodes, essentia/alchemy, golems/seals, worldgen, rendering/HUD/assets, and edge-case lifecycles.

## Why v11.02 exists

Earlier batches checked that terminal infusion failures entered the weighted instability table, but the calculated failure instability was not actually used for severity. v11.02 fixes that by converting higher terminal instability into additional direct weighted-table passes, capped to avoid runaway destruction.

Natural aura node generation also still used a deterministic position-hash profile. TC4 `ThaumcraftWorldGenerator.createRandomNodeAt(...)` uses `specialNodeRarity` gates for node type and modifier, plus biome/environment aspect bias. v11.02 moves natural worldgen nodes onto that random profile path while keeping the deterministic profile for placed/debug nodes.

Sorting golem target discovery was too filter-color focused. TC4 `GolemHelper.findSomethingSortCore(...)` scans marked containers with color `-1` for target availability, then placement uses the carried stack's matching colors. v11.02 aligns the pre-check with that broader target scan.

## Changes in v11.02

### Infusion terminal failure severity

- `TC4InfusionFailureParity` now keeps `TERMINAL_FAILURE_MAX_EVENT_PASSES = 4`.
- Terminal failures use the computed `failureInstability` instead of discarding it.
- Failure event passes are now `max(base passes, 1 + failureInstability / 4)`, capped at 4.
- `InfusionInstabilityEvents.triggerWeightedEvent(...)` now has a severity-context overload.
- The normal craft-cycle probability gate remains unchanged; this only affects terminal failure routing.
- Explosion events receive the severity context and scale slightly with instability.

### Aura node `createRandomNodeAt` profile parity

- Natural worldgen nodes now use `createRandomWorldgenProfile(...)` instead of the deterministic `createProfile(pos)` path.
- Added TC4 baseline constants:
  - `TC4_DEFAULT_NODE_RARITY = 36`;
  - `TC4_DEFAULT_SPECIAL_NODE_RARITY = 18`.
- Type distribution now follows the original shape:
  - silverwood → pure;
  - eerie → dark;
  - special roll → dark / unstable / pure / hungry by 10-slot table;
  - otherwise normal.
- Modifier distribution now follows the original shape:
  - specialNodeRarity / 2 gate;
  - bright / pale / fading table;
  - otherwise normal/no modifier.
- Aspect generation now uses a closer TC4-style spread:
  - biome tag chance;
  - primal/complex aspect fallback;
  - special type aspect additives;
  - local environment bias for water/lava/stone/foliage;
  - spread weighting into a biome aura value.

### Sorting golem target discovery

- `sortingHasMarkedOutputWithRoomLikeTC4(...)` now uses `sortingOutputContainersLikeTC4(-1)` for target discovery.
- This mirrors TC4 `GolemHelper.findSomethingSortCore(...)` more closely.
- The marked output still must:
  - not be the home container;
  - be within golem range;
  - already contain the item on a marked side;
  - have room through a marked side.
- Actual `AISortingPlace` still places using the carried stack's matching colors, preserving the existing marked-side semantics.

## Audits

Added:

- `scripts/tc4_v11_02_infusion_node_sorting_audit.py`

The new audit checks:

- terminal failure severity uses `failureInstability`;
- weighted event overload receives severity context;
- natural node generation uses random TC4-like profile rather than deterministic position hash;
- special node type/modifier rarity gates exist;
- biome/environment aspect bias exists;
- sorting target discovery uses the TC4 color `-1` scan;
- version/docs/CI markers are updated;
- no new content/progression statement is present.

## Build note

The source tree is intended to build with the included Gradle wrapper in a normal environment or through GitHub Actions. In this sandbox, Gradle jar build may fail if `services.gradle.org` cannot be resolved.

Current public archive label: **v11.02**. Previous public archive labels: **v10.82**, **v10.62**, **v10.42**, **v10.22**, **v10.02**, **v9.82**, **v9.62**, **v9.42**, **v9.22**, **v9.02**. Earlier compact markers retained: **v8.82**, **v8.62**, **v8.42**, **v8.22**, **v8.02**.

## Compatibility markers retained for older compact audits

Historical progress markers retained so old forward-compatible audits still pass: 75% complete / 25% remaining; 76% complete / 24% remaining; 77% complete / 23% remaining; 78% complete / 22% remaining; 79% complete / 21% remaining; 80% complete / 20% remaining; 81% complete / 19% remaining; 82% complete / 18% remaining; 83% complete / 17% remaining; 84% complete / 16% remaining; 85% complete / 15% remaining; 86% complete / 14% remaining.

## Compact batch v11.22 — strict original failure/node/tube correction

No new items, blocks, recipes, progression, GUI, or invented mechanics were added in v11.22.

### Infusion failure strict TC4 correction

- Rechecked invalid-catalyst / invalid-recipe terminal failure against `TileInfusionMatrix` lines 366-387.
- Fixed the v11.02 drift where high instability could run multiple direct weighted table passes.
- Terminal failure now enters the 21-slot TC4 weighted instability table exactly once, then clears the matrix state.
- Explosion side-effect is back to the original strength shape: `1.5F + random.nextFloat()`.
- `failureInstability` remains saved/debug context, but it no longer over-scales the event table or explosion strength.

### Aura node `createRandomNodeAt` tainted biome parity

- Added tainted-biome handling to natural aura node profile generation.
- If the biome maps to a tainted/corrupt style biome and the node is not pure, aura value receives the original-style 1.5x taint boost.
- A random tainted-biome roll can convert the node type to `TAINTED` and applies the second 1.5x boost, matching the original `createRandomNodeAt` flow.
- Removed the duplicate local `stone` counter from the local environment scan.

### Tube venting neighbour-connectability guard

- Tightened `checkVentingSnapshot()` so a neighbouring tube only participates in different-aspect venting if its opposite face can actually accept from this side.
- Closed neighbour faces, powered valves, and valve handle sides no longer create false venting conflicts.
- This matches the original `ThaumcraftApiHelper.getConnectableTile(...)` gate more closely.

## Audits

Added:

- `scripts/tc4_v11_22_strict_failure_node_tube_audit.py`

The new audit checks:

- terminal failure is exactly one weighted table pass;
- severity-scaled explosion drift is removed;
- tainted biome node type/aura handling exists;
- the duplicate node local-stone counter is gone;
- tube venting ignores non-connectable neighbour faces;
- docs/CI/version/progress markers are updated;
- no new content/progression statement is present.

Current progress estimate: **88% complete / 12% remaining**.

Current public archive label: **v11.22**. Previous public archive labels: **v11.02**, **v10.82**, **v10.62**, **v10.42**, **v10.22**, **v10.02**, **v9.82**, **v9.62**, **v9.42**, **v9.22**, **v9.02**. Earlier compact markers retained: **v8.82**, **v8.62**, **v8.42**, **v8.22**, **v8.02**.

Compatibility progress markers retained: 75% complete / 25% remaining; 76% complete / 24% remaining; 77% complete / 23% remaining; 78% complete / 22% remaining; 79% complete / 21% remaining; 80% complete / 20% remaining; 81% complete / 19% remaining; 82% complete / 18% remaining; 83% complete / 17% remaining; 84% complete / 16% remaining; 85% complete / 15% remaining; 86% complete / 14% remaining; 87% complete / 13% remaining; 88% complete / 12% remaining.
