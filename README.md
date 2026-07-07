# Thaumcraft Legacy Rebuild — Stage204

Version: `2.04.0`  
Minecraft/Forge: `1.19.2 / Forge 43.5.2`

## Current Stage204 focus

Stage203 + Stage204 were applied on top of Stage202. This remains a strict original Thaumcraft 4 parity-port pass. No new mechanics, progression, recipes, GUI concepts, or textures were invented.

### Stage203 — Golem ghost-slot parity

- Restored original `ContainerGolem` / `ContainerGhostSlots` behavior for golem GUI slots.
- Golem inventory slots are now copy-only ghost slots, not real storage transfer slots.
- Implemented `SlotGhost` semantics: no pickup, no player item consumption, copied filter stacks stored in golem `Inventory` NBT.
- Implemented `SlotGhostFluid` adapter with Forge fluid-handler validation for liquid-core golems.
- Preserved original limits: fill core ghost slots can hold count `256`; all other ghost slots use count `1`.
- Restored click semantics: empty-hand count +/-1, shift-left clear, shift-right +16, throw clears, clone copies.
- Preserved scroll `66/67`, toggle `50..57`, color cycling and original `guigolem.png` rendering path.

### Stage204 — Jar/tube transfer edge cases

- Added original-style `TileJarFillable.addToContainer` / `takeFromContainer` helpers.
- Void jar overflow now consumes excess essentia while storing only up to capacity, like `TileJarFillableVoid`.
- Restored original suction edge cases: normal jar `32`, labelled jar `64`, void jar `32`, labelled void jar `48`.
- Phial pour/take and tube transfer now share the original jar add/take helpers.
- Tube destination lookup now uses jar original suction instead of a fixed generic table.
- Added one-way tube direction gates: input from opposite facing, output through facing, network traversal constrained to those sides.
- Kept filter/restrict/buffer subtype checks and original NBT names: `AspectFilter`, `Aspect`, `Amount`, `facing`, `open`, `choke`, `buffer`.
- Kept the Stage198/Stage200 resource pack and texture hardening: `pack.mcmeta` stays valid and model texture references are audited.

## Verification

```bash
python3 scripts/java_syntax_guard.py
python3 scripts/github_static_audit.py
python3 scripts/github_ci_guard.py
python3 scripts/tc4_stage203_golem_ghost_slot_audit.py
python3 scripts/tc4_stage204_jar_tube_edge_cases_audit.py
```

If Minecraft still shows missing textures, remove old jars such as `1.94.0` from the `mods` folder and install only the jar built from this Stage204 source.

## Stage204 GitHub audit hotfix

Fixed GitHub Actions `Static source/resource audit` failure from the stale Stage161 version guard. The Stage161 research-note audit now accepts current Stage204 version `2.04.0` via semantic version parsing instead of old hard-coded version literals.

### Stage204 GitHub Compile Hotfix 2

- Fixed GitHub `compileJava` failure in `JarTubeInteractionRuntime.java`: lambda captured reassigned local `aspect`; now snapshots `final Aspect finalAspect` before `withStyle(...)`.
- No TC4 mechanics changed; Stage204 parity/resource pack fixes remain intact.
