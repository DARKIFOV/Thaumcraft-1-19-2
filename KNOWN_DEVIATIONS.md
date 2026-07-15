# Known deviations from Thaumcraft 4.2.3.5

This file records intentional or currently unavoidable differences. An entry is
not a waiver for release readiness; each deviation must remain visible in
`TC4_PORT_STATUS_V3.md` until accepted or removed.

## Active deviations


### Research-note and table runtime proof

- **Original:** primary research is selected in the Thaumonomicon, creates a targeted note in player inventory, and the hex puzzle is edited only inside the two-slot Research Table; an unfinished note does nothing useful in hand, while a completed discovery is learned by item use.
- **Forge port:** v11.62.82 restores that source-level flow, rejects legacy table-side note creation/learning actions, retains paper + scribing-tool ink consumption, and restores the bookshelf/Brain-in-a-Jar bonus sources.
- **Remaining deviations:** no built-client evidence yet covers note creation, insertion/removal, hex edits, Expertise/Mastery probabilities, duplication, bonus persistence or completed-discovery learning.
- **Release impact:** Research Table remains `PARTIAL / NOT TESTED`.

### Warp event visuals and runtime distribution

- **Original:** a single potion effect is authoritative for Warp Ward; warp events use a separate counter, signed three-axis valid-position searches, a 0.75 forward Death Gaze visibility cone and client-only `PacketMiscEvent` post-processing.
- **Forge port:** v11.62.81 migrates old persistent ward timers into the real effect, keeps the rare sticky cleanse from resetting the counter, restores valid guardian/spider spawn searches and the Death Gaze cone, and synchronizes all warp buckets/counter.
- **Remaining deviations:** the original warp pulse/mist GLSL/post-processing remains approximated by particles and GUI overlays; event frequency/distribution, milk removal, client bucket synchronization and two-player behavior have no runtime artifacts.
- **Release impact:** Warp/Eldritch remains `PARTIAL / NOT TESTED`.

### Essentia transport pressure and processing

- **Original:** reservoir suction 24 on one selectable six-direction face; direct five-tick pull; buffer side suction derived from bellows/chokes and compared with the real neighbour suction/minimum; centrifuge input may queue while output is occupied or redstone pauses processing.
- **Forge port:** v11.62.79 restores those source-level rules, preserves face-aware buffer arbitration and rolls rejected transfers back.
- **Remaining deviation:** no built-client suction-conflict test, rollback fault injection, redstone/output queue test or long-running network soak artifact exists.
- **Release impact:** essentia transport and processing remain `PARTIAL / NOT TESTED`.


### Golem task adapters

- **Original:** independent priority-based AI goals for every core, including `AIAvoidCreeperSwell`, marker-side-aware `AIUseItem` and water-quality-dependent `AIFish`.
- **Forge port:** v11.62.78 restores the marker-side/empty-hand USE path, first by-product carry behavior, priority creeper avoidance and TC4 fishing probability order/quality modifiers at source level.
- **Remaining deviation:** the complete materials × cores × upgrades × decorations × markers task matrix has not been executed in a built client or dedicated server.
- **Release impact:** golems remain `PARTIAL / NOT TESTED`; the three new runtime cases and the two existing broad golem cases require SHA-256-backed artifacts.


### Raw aura node item

- **Original player path:** capture and transport through Node in a Jar.
- **Forge port:** the direct `aura_node` item is migration-only and converts
  stored legacy node NBT to Node in a Jar when possible.
- **Reason:** prevents a static/debug node item from being mistaken for the
  original dynamic node-item renderer.
- **Release impact:** acceptable only after Node Jar runtime tests pass for all
  six node types and all three modifiers.

### Outer Lands generation adapter

- **Original:** dedicated dimension provider, maze generation lifecycle,
  rooms, locks, traps, boss progression and return path.
- **Forge port:** cell-aligned entry maze, TC4 portal-room geometry, existing-maze-only chunk population, Eldritch Lock-gated boss spawn and SavedData bridge are present at source level.
- **Reason:** the complete dimension bootstrap, room/loot coverage and runtime progression lifecycle are still unverified.
- **Release impact:** P0 remains open. No release PASS is permitted without the
  complete portal → maze → boss → save/reload → Overworld test evidence.

### Visual difference metrics

- **Port tool:** `tools/compare_visual_artifacts.py` produces MAE, RMSE, exact
  pixel ratio, global SSIM and an amplified difference image.
- **Deviation:** those metrics cannot automatically prove visual parity because
  UI scaling, interpolation and lighting can change pixels without changing the
  intended TC4 appearance.
- **Release impact:** human side-by-side review remains mandatory.

### Taint ecology and biome feedback

- **Original:** taint spread is coupled to the Tainted Lands biome state, includes the five fibre/stalk stages, mature spores, taint spiders, taint-specific poison and a flying Spore Swarmer path from crust.
- **Forge port:** v11.62.80 restores source-level column persistence, crust/soil thresholds and decay, five distinct fibre/stalk states, anchored growing spores, taint-spider geometry/eyes and the original player/mob poison probabilities.
- **Remaining deviations:** the SavedData column bridge does not yet reproduce the complete biome palette, weather and biome-replacement feedback; the flying Spore Swarmer entity is not implemented; the spider currently drops canonical tainted slime only because the taint-tendril item remains a migration mapping; vanilla Poison is used until a dedicated Taint Poison effect is implemented.
- **Release impact:** Taint remains `PARTIAL / NOT TESTED`. Spread, persistence, visuals, poison, drops and dedicated-server synchronization require SHA-256-backed runtime artifacts.
