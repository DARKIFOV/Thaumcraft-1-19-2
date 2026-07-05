# Thaumcraft Legacy Rebuild — Stage118 TC4 Research Runtime Port

Stage118 ports the original TC4 `ConfigResearch.java` research graph into runtime-safe Forge 1.19.2 classes. The Thaumonomicon/progression registry now starts from the original TC4 categories, keys, coordinates, parents, aspects, pages and warp metadata.

Strict source-driven port of **Thaumcraft 4** to **Minecraft Forge 1.19.2**. This repository is not a reimagining: original TC4 source/assets/research/recipes/GUI behavior/mechanics are treated as the source of truth and mapped into the modern Forge project stage by stage.

Current stage: **Stage 112 — GitHub build hardening / base CI fixes**.

## GitHub build

After pushing this folder to GitHub, Actions should run automatically on `main`/`master`, pull requests, or manual `workflow_dispatch`.

The workflow does four things before producing jars:

1. Runs `scripts/java_syntax_guard.py`.
2. Runs `scripts/github_ci_guard.py`.
3. Runs `scripts/github_static_audit.py`.
4. Builds through the checked-in Gradle wrapper: `./gradlew --no-daemon clean build --stacktrace`.

Built jars are uploaded as a GitHub Actions artifact named:

`thaumcraft-legacy-rebuild-stage112-jars`

## Local commands

```bash
chmod +x ./gradlew
python scripts/java_syntax_guard.py
python scripts/github_ci_guard.py
python scripts/github_static_audit.py
./gradlew --no-daemon clean build --stacktrace
```

## Porting source map

See:

- `docs/porting/STAGE111_STRICT_TC4_SOURCE_MAPPING.md`
- `docs/porting/tc4_to_1192_class_map.csv`
- `docs/porting/tc4_source_inventory_summary.json`
- `docs/porting/tc4_source_inventory_full.json`
- `src/main/java/com/darkifov/thaumcraft/porting/TC4AspectBridge.java`
- `src/main/resources/data/thaumcraft/tc4_source_mapping/aspects_from_original_source.json`

## Source of truth

The uploaded `Thaumcraft4-1.7.10-master` source is treated as the primary source of truth for tags, assets, research, recipes, GUI behavior, blocks, items, tile entities and progression. Placeholder or heuristic systems are to be replaced by source-mapped ports stage by stage.


## Stage113 — Provided 1.19.2 reboot base integration

This stage records and integrates the provided Minecraft 1.19.2 Thaumcraft reboot archives as the modern Forge base/reference layer.
The active project is not blindly replaced because the provided 1.19.2 reboots are very small skeletons compared to the current working tree.

See: `docs/porting/STAGE113_PROVIDED_1192_REBOOT_BASE_INTEGRATION.md`.


## Stage114 — TC4 1.7.10 mass source transfer

Stage114 imports the original TC4 1.7.10 decompiled source and assets as the strict porting source-of-truth. The original Java is intentionally stored under `docs/source_refs/tc4_1710_original_source/` and not compiled directly, because 1.7.10 FML/MCP classes cannot compile on Forge 1.19.2 without explicit porting. Runtime-safe bridges now expose original block/item/wand/research/asset maps. Original TC4 language keys are merged into `assets/thaumcraft/lang/en_us.json`, and original TC4 assets are mirrored under `assets/thaumcraft/original_tc4_1710/` plus normalized texture mirrors under `textures/block/tc4`, `textures/item/tc4`, and `textures/original/thaumcraft4`.


## Stage115 — TC4 ConfigAspects runtime port

Stage115 connects original TC4 `ConfigAspects` data to the 1.19.2 runtime. Thaumometer, crucible, alchemical furnace and research table now check generated TC4 object-aspect mappings before falling back to the older temporary name-based logic. Raw TC4 object/entity aspect registrations are preserved under `data/thaumcraft/tc4_source_mapping/`.

## Stage118

TC4 Thaumonomicon browser port: original 256x230 research browser, original category tabs/backgrounds, original display coordinates, draggable map, TC4 node sprites, hidden parent visibility and TC4-style research page viewer.


## Stage118 research parity

This build continues the strict TC4 research port: original research icons, targeted research note creation from paper + scribing tools, original AspectList note requirements, and mixed text/recipe page rendering in the Thaumonomicon.
