# Thaumcraft Legacy Rebuild — v11.62.54

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
build/libs/thaumcraft_legacy_rebuild_1.19.2-11.62.54-hotfix2.jar
```

See `THAUMCRAFT_LEGACY_REBUILD_V11_62_54_EXPERT_FULL_TECHNICAL_REPORT_R3.md` for the consolidated expert report, crash diagnosis and remaining work.

## v11.62.54-hotfix1

This packaging hotfix repairs an invalid standalone prose line in `META-INF/mods.toml` that caused Forge to abort mod discovery and the launcher to report exit code 1. Gameplay classes are unchanged from v11.62.54. The known focus-transaction ordering defect D-001 remains open.


## v11.62.54-hotfix2

This source hotfix addresses the startup crash reported as `AbstractMethodError` in `BlockEntityRendererProvider`. Direct renderer, entity-renderer, menu-screen and colour-handler lambdas no longer implement obfuscated Minecraft interfaces themselves. They now target stable JDK/project interfaces and are forwarded through explicit adapter overrides that ForgeGradle can reobfuscate safely.

The crash was in the mod bootstrap bytecode, not in Oculus, Rubidium or the world save. Build this source through the included GitHub Actions workflow and use the produced reobfuscated JAR. The known focus transaction ordering issue D-001 is still tracked separately.
