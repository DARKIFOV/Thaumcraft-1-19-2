# Thaumcraft Legacy Rebuild — v11.62.49

Source-driven port of Thaumcraft 4.2.3.5 to **Minecraft Forge 1.19.2 (Forge 43.5.2)** and Java 17.

This is a Forge project. It does not use Fabric, NeoForge or TerraBlender.

## v11.62.49 focus

This pass hardens the Forge 1.19.2 port of the TC4 Aspect Orb against four renderer/entity parity errors:

- lifetime scale now follows the original floating-point operator order and shrinks smoothly from `0.4` toward `0.1`;
- block light receives the exact raw `+120` lightmap-coordinate boost, capped at `240`, instead of an approximate `+8` modern light levels;
- lava, not water, triggers the original upward random motion and fizz sound;
- the old `pushOutOfBlocks` call is mapped to `moveTowardsClosestSpace`.

The original dual pickup gate is intentionally retained: the orb-local `orbCooldown` **and** the player's vanilla XP pickup delay must both be clear. Entropy and Void intentionally keep ordinary source-alpha blending because TC4 assigns those two aspects blend constant `771`; the other aspects remain additive.

## Consolidated report

The release uses one consolidated report:

- `THAUMCRAFT_V11_62_49_FULL_REPORT.md`

## Local static checks

```bash
python3 tools/forge_only_guard.py
python3 tools/java_syntax_guard.py
python3 tools/validate_json_resources.py
python3 tools/visual_parity_guard.py
python3 tools/research_table_guard.py
python3 tools/research_table_open_guard.py
python3 tools/worldgen_guard.py
python3 tools/feature_cycle_guard.py
python3 tools/thaumometer_scan_guard.py
python3 tools/runtime_visual_guard.py
python3 tools/leaves_wand_table_guard.py
python3 tools/tc4_original_asset_guard.py
python3 tools/tc4_runtime_visual_guard.py
python3 tools/runtime_fix_guard.py
python3 tools/tc4_116245_parity_guard.py
python3 tools/tc4_116246_audit_guard.py
python3 tools/tc4_116247_arcane_wand_guard.py
python3 tools/tc4_116248_node_orb_guard.py
python3 tools/tc4_116249_aspect_orb_parity_guard.py
python3 tools/tc4_item_visual_audit.py --version 11.62.49 --fail-on-missing
python3 tools/model_transform_audit.py --version 11.62.49 --fail-on-problems
python3 tools/bewlr_contract_audit.py --version 11.62.49 --fail-on-problems
python3 tools/aura_node_parity_audit.py --version 11.62.49 --fail-on-problems
python3 tools/audit_registry.py --version 11.62.49 --fail-on-unexpected
```

## Forge build

```bash
chmod +x gradlew
./gradlew build --stacktrace --no-daemon
```

The compiled mod is written to `build/libs/thaumcraft_legacy_rebuild_1.19.2-11.62.49.jar`. Do not place multiple versions of this mod in the same `mods` directory.
