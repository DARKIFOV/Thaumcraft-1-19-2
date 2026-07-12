# Thaumcraft Legacy Rebuild — v11.62.54-hotfix8

Target: **Minecraft 1.19.2 / Forge 43.5.2 / Java 17**.

This revision continues the source-driven Thaumcraft 4.2.3.5 port. It restores the original 32×32 wand vis dial and fixes focus cycling/storage semantics.

## v11.62.54 focus

- original top-left wand dial (`wandDialBottom=false`), with optional bottom-left placement;
- original six primal reservoirs, cost/change markers and sneaking values;
- focus icon or Equal Trade picked block in the dial centre;
- focus cooldown seconds in the original half-scale position;
- client HUD state reset on disconnect;
- original `TreeMap` focus sort/overwrite behaviour;
- transactional focus swaps: a full pouch/inventory cannot eject or lose the installed focus;
- exact rollback to the source inventory/pouch slot if a swap cannot complete.

## Validation

```bash
python3 tools/tc4_116254_focus_hud_cycle_guard.py
python3 tools/tc4_item_visual_audit.py --version 11.62.54 --fail-on-missing
python3 tools/model_transform_audit.py --version 11.62.54 --fail-on-problems
python3 tools/bewlr_contract_audit.py --version 11.62.54 --fail-on-problems
python3 tools/aura_node_parity_audit.py --version 11.62.54 --fail-on-problems
python3 tools/audit_registry.py --version 11.62.54 --fail-on-unexpected
```

## Full build

```bash
chmod +x gradlew
./gradlew build --stacktrace --no-daemon
```

Expected output:

```text
build/libs/thaumcraft_legacy_rebuild_1.19.2-11.62.54-hotfix8.jar
```

See `THAUMCRAFT_LEGACY_REBUILD_V11_62_54_EXPERT_FULL_TECHNICAL_REPORT_R9.md` for the consolidated expert report, crash diagnosis and remaining work.

## v11.62.54-hotfix1

This packaging hotfix repairs an invalid standalone prose line in `META-INF/mods.toml` that caused Forge to abort mod discovery and the launcher to report exit code 1. Gameplay classes are unchanged from v11.62.54. The known focus-transaction ordering defect D-001 remains open.


## v11.62.54-hotfix2

This source hotfix addresses the startup crash reported as `AbstractMethodError` in `BlockEntityRendererProvider`. Direct renderer, entity-renderer, menu-screen and colour-handler lambdas no longer implement obfuscated Minecraft interfaces themselves. They now target stable JDK/project interfaces and are forwarded through explicit adapter overrides that ForgeGradle can reobfuscate safely.

The crash was in the mod bootstrap bytecode, not in Oculus, Rubidium or the world save. Build this source through the included GitHub Actions workflow and use the produced reobfuscated JAR. The known focus transaction ordering issue D-001 is still tracked separately.

## v11.62.54-hotfix3

This CI/release hotfix repairs a stale source guard that rejected the required SRG-safe Research Table renderer adapter. The guard now verifies the wrapped registration and the explicit `BlockEntityRendererProvider` bridge instead of demanding the forbidden direct Minecraft SAM constructor reference. Gameplay code and assets are unchanged from hotfix2.


## v11.62.54-hotfix4

Adds an Alchemical Furnace menu/screen and revises the Crucible, Arcane Pedestal and Arcane Workbench block models after runtime screenshot review. Adds a complete item-model/texture-reference audit.


## v11.62.54-hotfix5

Fixes research puzzle parity: identical aspects no longer connect, new placements must touch a compatible occupied hex, Research Expertise reveals compound components and keeps its 25% removal refund, while Research Mastery keeps the 50% refund, adds the original 10% free placement chance and Shift-click automatic combination shortcut.

## v11.62.54-hotfix6

- Generates the exact 201-entry TC4 4.2.3.5 research graph, metadata and all 591 ordered ResearchPage declarations from the committed original source map.
- Audits every research icon, both language sets and the original Thaumonomicon GUI textures.
- Restores original concealed-page removal, dynamic known-aspect pages and research tooltip cues.
- Re-centres all WandItem BEWLR meshes so iron, greatwood, silverwood and creative wands render in inventory and first person.

## v11.62.54-hotfix7

- Removes the duplicated `PRIMPEARL` item trigger from both committed source maps and generated runtime metadata.
- Regenerates the machine-readable full research audit with duplicate-trigger detection.
- Normalizes the consolidated report revision history, numbering and hotfix appendices.
- Clarifies that `gui_research.png` supplies frames/tabs while research icon content may come from an `ItemStack` or a standalone resource texture.


## v11.62.54-hotfix8

- Fixes GitHub Actions failure `ModuleNotFoundError: No module named 'PIL'`.
- Removes the undeclared Pillow dependency from `tc4_full_research_thaumonomicon_audit.py`.
- Reads and validates PNG width/height directly from the standard PNG `IHDR` header using only Python's standard library.
- Keeps corrupted/truncated PNGs as explicit audit failures instead of crashing before a report is written.
