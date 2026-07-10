# Thaumcraft Legacy Rebuild — v11.62.26

Forge 1.19.2 rebuild of Thaumcraft 4 mechanics, progressed subsystem by subsystem against the supplied TC4 4.2.3.5 source.

## Runtime hotfix v11.62.26

- fixed Forge startup failure `Duplicate registration tc4_crystalessence`;
- the functional essentia crystal now owns the registry id exactly once;
- the legacy research-item lookup reuses that existing registry object;
- added a duplicate-registry regression audit before Gradle compilation.

See `REPORT_V11_62_26_DUPLICATE_REGISTRY_RUNTIME_FIX.md`.

## Current large batch

**Research Matrix + Thaumonomicon + Infusion Matrix + Deconstruction + Crystalizer + Advanced Furnace Controller Original Parity**:

- persisted randomized research-note seeds;
- restored TC4 `hexgrid` q/r/type/aspect NBT and legacy note import;
- restored complexity radius, stable ring anchors with the original unused random-start quirk, and protected blank removal;
- made paper/ink/note creation transactional;
- added real 224×196 Thaumonomicon scissor, proportional background and per-category pan memory;
- fixed accidental research activation while dragging;
- fixed the Infusion Matrix so crafting can start before all essentia is present;
- expanded infusion essentia sources to jars, reservoirs, alembics, buffers, centrifuges and advanced furnaces;
- added the functional Deconstruction Table and original 40-tick primal-knowledge cycle;
- added the functional Essentia Crystalizer, direct jar/reservoir/alembic/centrifuge/furnace input, aspect crystals, suction, Terra acceleration and item output;
- ported the Advanced Alchemical Furnace controller power/storage/item-intake core;
- restored exact Deconstructor and Crystalizer recipes and resolver metadata;
- added the v11.62.24 source mapping and regression audit.

See `REPORT_V11_62_24_RESEARCH_MATRIX_THAUMONOMICON_INFUSION_MATRIX_MACHINES_ORIGINAL_PARITY.md` for exact behavior, limitations and completion estimates.

## Build

GitHub Actions runs all preserved guards and 22 subsystem audits through v11.62.26, then Forge Gradle on Java 17. The playable artifact contains only:

```text
build/libs/*-github.jar
```

Do not place multiple jars of this mod in the same `mods` directory.

<!-- v11.62.23 compatibility marker: Essentia Transport / Thaumatorium / Alchemical Infrastructure -->
<!-- v11.62.22 compatibility marker: Golemancy Progression + Infrastructure -->
<!-- v11.62.21 compatibility marker: Golemancy Core Original Parity -->
<!-- v11.62.20 compatibility marker: Nine Hells / Pech's Curse -->
<!-- v11.62.19 compatibility marker: Equal Trade / Portable Hole / Warding / counted clusters -->
<!-- v11.62.18 compatibility marker: Focus Excavation original parity -->
<!-- v11.62.2 compatibility marker: integrated-server infinite loading -->
