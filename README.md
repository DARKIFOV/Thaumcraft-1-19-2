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
build/libs/thaumcraft_legacy_rebuild_1.19.2-11.62.54.jar
```

See `THAUMCRAFT_V11_62_54_FULL_REPORT.md` for the complete audit, runtime checklist and remaining work.
