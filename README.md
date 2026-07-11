# Thaumcraft Legacy Rebuild — v11.62.41

Source-driven port of Thaumcraft 4.2.3.5 to **Minecraft Forge 1.19.2 (43.5.2)** and Java 17.

This is a Forge project. It does not use NeoForge or TerraBlender.

## v11.62.41 focus

This batch continues the screenshot-driven repair pass:

- replaces ordinary non-occluding Greatwood/Silverwood leaf blocks with native 1.19.2 `LeavesBlock` implementations;
- restores leaf distance, persistence, decay and internal-face suppression so Silverwood canopies no longer become transparent blue lattices;
- restores original-style rain drips, rare Silverwood sparkles and Silverwood light level 7;
- fixes wand rod/cap rendering by reproducing the original `ModelWand` 64x32 UV atlas and model-box offsets;
- separates the original outer staff offset from the inner `ModelWand` staff offset so the rod no longer disappears while only the focus remains visible;
- replaces the approximate Research Table JSON world model with a native Forge block-entity renderer using TC4 `restable.png`, `restable2.png`, parchment and quill resources;
- ports the original 32-pixel two-table geometry, four orientations, inkwell, quill, parchment stack and research-note scroll;
- stores the original partner direction in a horizontal blockstate rather than losing the old TC4 metadata;
- keeps both physical table positions through `PRIMARY` master/partner states, routes either half to one inventory and removes the pair together;
- uses separate half-table collision shapes instead of a full invisible cube;
- installs the used Scribing Tools into the table's real slot during conversion, matching the original behavior;
- keeps a separate visible inventory model while preventing a duplicate world model behind the renderer;
- adds CI regression checks for leaves, ModelWand UVs and the Research Table renderer.

Detailed report: `REPORT_V11_62_41_LEAVES_WAND_RESEARCH_TABLE_FIXES.md`.

## Local static checks

```bash
python3 tools/forge_only_guard.py
python3 tools/java_syntax_guard.py
python3 tools/validate_json_resources.py
python3 tools/visual_parity_guard.py
python3 tools/research_table_guard.py
python3 tools/research_table_open_guard.py
python3 tools/worldgen_guard.py
python3 tools/thaumometer_scan_guard.py
python3 tools/runtime_visual_guard.py
python3 tools/leaves_wand_table_guard.py
python3 tools/audit_registry.py --fail-on-unexpected
```

## Forge build

```bash
chmod +x gradlew
./gradlew clean build --stacktrace --no-daemon
```

The compiled mod is written to `build/libs/`. Do not place multiple versions of this mod in the same `mods` directory.
