# Thaumcraft Legacy Rebuild — Forge 1.19.2

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
