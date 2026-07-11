# Thaumcraft Legacy Rebuild — v11.62.44

Source-driven port of Thaumcraft 4.2.3.5 to **Minecraft Forge 1.19.2 (43.5.2)** and Java 17.

This is a Forge project. It does not use NeoForge or TerraBlender.

## v11.62.44 focus

This runtime repair batch fixes held wand placement, standalone aura-node blend/depth rendering, reliable server-side Thaumometer scan completion, the functional complete-knowledge Thaumonomicon, and invisible particle-based TC4 Nitor.

It retains the v11.62.43 original-resource and runtime-render audit:

This batch is a strict original-resource and runtime-render audit:

- restores the missing original `misc/wispy.png` used by the aura-node wand drain beam;
- replaces the altered scanner texture with the byte-exact original `models/scanner.png` including its alpha channel;
- verifies all **940 original TC4 texture files** byte-for-byte in CI;
- restores the Thaumometer's original alphabetical aspect order and 5/4/3/2/1 layout for up to 15 aspects;
- removes the non-original fake node sprite from the scanner glass;
- removes invented energized-size and recently-drained sprite overlays from aura nodes;
- restores the independent ten-rune sceptre orbit and original animated rune colours;
- remaps Goggles of Revealing through the original logical 64x32 armor UV net instead of stretched arbitrary atlas strips;
- binds Thaumonomicon browser and page rendering directly to the canonical original GUI files;
- locks Research Table and Arcane Workbench slot coordinates to the original 1.7.10 values;
- audits all **689 item model JSON files**, including hidden compatibility aliases.

Detailed reports:

- `REPORT_V11_62_43_TC4_VISUAL_TEXTURE_AUDIT.md`
- `REPORT_V11_62_44_RUNTIME_FIXES.md`
- `PORT_STATUS_V11_62_43.md`
- `reports/ITEM_VISUAL_AUDIT_V11_62_43.md`

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
python3 tools/tc4_item_visual_audit.py --version 11.62.44 --fail-on-missing
python3 tools/runtime_fix_guard.py
python3 tools/audit_registry.py --fail-on-unexpected
```

## Forge build

```bash
chmod +x gradlew
./gradlew build --stacktrace --no-daemon
```

The compiled mod is written to `build/libs/`. Do not use `clean build` in the GitHub workflow and do not place multiple versions of this mod in the same `mods` directory.
