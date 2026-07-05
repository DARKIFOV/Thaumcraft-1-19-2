# Stage 111 — TC4 source mapping / strict port start

## What changed

This stage switches the project from the old broad scaffold workflow to a source-driven strict port workflow. The uploaded `Thaumcraft4-1.7.10-master` archive is treated as the primary TC4 1.7.10 source reference. Current Forge 1.19.2 code remains the working target.

## Source archive roles

| Archive | Role | Java files | Asset files | Decision |
|---|---:|---:|---:|---|
| Thaumcraft4-1.7.10-master | Primary original TC4/decompiled source and original assets | 859 | 1090 | Authoritative source for strict port |
| thaumcraft_legacy_rebuild_STAGE110... | Current Forge 1.19.2 working project | 226 | 2771 | Main target project |
| ThaumicTinkerer-main | Addon source reference | 151 | 162 | Later addon parity, not core-first |
| ThaumicEnergistics-AE2-RV6 | Addon source reference | 134 | 76 | Later addon parity, not core-first |
| thaumcraftreboot-main | Earlier reboot/reference project | 4 | 13 | Reference only |
| thaumcraft-reboot-fbe41... | Earlier minimal reboot/reference project | 2 | 0 | Reference only |

## New files added

- `src/main/java/com/darkifov/thaumcraft/porting/TC4AspectBridge.java` — exact source-derived TC4 aspect lock: original constant name, tag, color, chat color code, blend, components and texture id.
- `src/main/java/com/darkifov/thaumcraft/porting/TC4SourceMap.java` — stable source-to-target roadmap used by later port stages.
- `src/main/resources/data/thaumcraft/tc4_source_mapping/aspects_from_original_source.json` — generated from `thaumcraft/api/aspects/Aspect.java`.
- `src/main/resources/data/thaumcraft/tc4_source_mapping/source_inventory_summary.json` — summary of all uploaded source/reference archives.
- `src/main/resources/data/thaumcraft/tc4_source_mapping/tc4_to_1192_class_map.json` — machine-readable class/domain map.
- `docs/porting/tc4_source_inventory_summary.json` and `docs/porting/tc4_source_inventory_full.json` — generated audit inventory.
- `docs/porting/tc4_to_1192_class_map.csv` — human-editable mapping table.
- `docs/porting/STAGE111_STRICT_TC4_SOURCE_MAPPING.md` — this report.

## Core mapping decisions

| TC4 source area | Forge 1.19.2 target | Priority | Rule |
|---|---|---:|---|
| `thaumcraft/api/aspects` | `Aspect`, `AspectList`, `porting.TC4AspectBridge` | P0 | Exact tags/colors/components first |
| `thaumcraft/common/config/ConfigResearch.java` | `data/thaumcraft/research`, `research` package | P0 | Research keys/pages/parents copied from source data, not invented |
| `thaumcraft/common/config/ConfigRecipes.java` | `data/thaumcraft/recipes`, recipe managers | P0 | Arcane/crucible/infusion source mappings only |
| `thaumcraft/common/blocks` | `block` + `blockentity` packages | P0/P1 | Port behavior by original class/subtype, not by current placeholder names |
| `thaumcraft/common/items/wands` | `wand` package + `WandItem` | P0 | Wand vis/focus/cap/rod logic must follow source |
| `thaumcraft/client/gui` | `client.screen` | P0 | Original GUI interaction/layout parity |
| `assets/thaumcraft` | `src/main/resources/assets/thaumcraft` | P0 | Original assets and lang keys win over generated placeholders |

## Immediate strict-port corrections identified

1. `AspectDatabase` currently uses name heuristics for object aspects. That remains only a temporary fallback. Next stage should port `ConfigAspects.java` / `ThaumcraftApiHelper.getObjectAspects` mappings into data-driven exact tables.
2. Current block classes are split by 1.19.2 concepts, while TC4 often used metadata/subtypes (`BlockMetalDevice`, `BlockStoneDevice`, `BlockTable`, `BlockJar`, `BlockTube`). The mapping table now records those source-domain links so behavior is ported from the original classes.
3. Addon files should not drive core TC4 behavior. `ThaumicTinkerer` and `ThaumicEnergistics` are kept as reference sources for later addon parity after core TC4 objects/research/recipes are locked.

## Verification notes

- JSON files were generated from the uploaded source trees.
- `scripts/java_syntax_guard.py` should remain valid for the added files.
- Full ForgeGradle compilation depends on a Gradle wrapper / network cache; this stage adds the Gradle wrapper files from the uploaded reboot reference so the project is structurally closer to a GitHub-ready checkout.
