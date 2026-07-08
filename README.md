# Thaumcraft Legacy Rebuild — Stage209

Version: `2.09.0`  
Minecraft/Forge: `1.19.2 / Forge 43.5.2`

## Current Stage209 focus

Stage203 + Stage205 were applied on top of Stage202. This remains a strict original Thaumcraft 4 parity-port pass. No new mechanics, progression, recipes, GUI concepts, or textures were invented.

### Stage203 — Golem ghost-slot parity

- Restored original `ContainerGolem` / `ContainerGhostSlots` behavior for golem GUI slots.
- Golem inventory slots are now copy-only ghost slots, not real storage transfer slots.
- Implemented `SlotGhost` semantics: no pickup, no player item consumption, copied filter stacks stored in golem `Inventory` NBT.
- Implemented `SlotGhostFluid` adapter with Forge fluid-handler validation for liquid-core golems.
- Preserved original limits: fill core ghost slots can hold count `256`; all other ghost slots use count `1`.
- Restored click semantics: empty-hand count +/-1, shift-left clear, shift-right +16, throw clears, clone copies.
- Preserved scroll `66/67`, toggle `50..57`, color cycling and original `guigolem.png` rendering path.

### Stage205 — Jar/tube transfer edge cases

- Added original-style `TileJarFillable.addToContainer` / `takeFromContainer` helpers.
- Void jar overflow now consumes excess essentia while storing only up to capacity, like `TileJarFillableVoid`.
- Restored original suction edge cases: normal jar `32`, labelled jar `64`, void jar `32`, labelled void jar `48`.
- Phial pour/take and tube transfer now share the original jar add/take helpers.
- Tube destination lookup now uses jar original suction instead of a fixed generic table.
- Added one-way tube direction gates: input from opposite facing, output through facing, network traversal constrained to those sides.
- Kept filter/restrict/buffer subtype checks and original NBT names: `AspectFilter`, `Aspect`, `Amount`, `facing`, `open`, `choke`, `buffer`.
- Kept the Stage198/Stage200 resource pack and texture hardening: `pack.mcmeta` stays valid and model texture references are audited.


## Stage209 — TC4 Infusion Renderer + Enchantment Output Parity

Version: `2.09.0`

Stage209 continues the strict original Thaumcraft 4 Infusion Matrix parity pass. Original 1.7.10 `TileRunicMatrixRenderer`, `ModelCube`, `TileInfusionMatrix.craftingFinish` and `InfusionEnchantmentRecipe` were used as the source of truth before writing Forge 1.19.2 adapters.

- Infusion Matrix now uses an animated block-entity renderer instead of fake/static `cube_all` geometry.
- Renderer uses original `textures/models/infuser.png`, eight cubelets, startup yaw/pitch/roll, active instability wobble, translucent glow and crafting halo adapter.
- Runtime recipes now carry TC4 `recipeType`, enchantment output ids and NBTBase-style output labels/tags.
- Added `TC4InfusionEnchantmentAdapter` for original infusion enchantment matching, instability, essentia scaling, XP scaling and enchantment application.
- Matrix craft cycle now drains enchantment XP one level at a time before essentia, with radius-10 player search, magic damage and source FX.
- `craftingFinish` now supports normal `ItemStack`, enchantment output and labelled NBT output adapters.
- Component validation/pull now uses recipe-aware matching and preserves crafting container items.

Stage209 should focus on exact failure event weights, packet FX parity, runic augment dynamic NBT/components and full ItemStack damage/OreDictionary/NBT matching.

## Verification

```bash
python3 scripts/java_syntax_guard.py
python3 scripts/github_static_audit.py
python3 scripts/github_ci_guard.py
python3 scripts/tc4_stage203_golem_ghost_slot_audit.py
python3 scripts/tc4_stage205_jar_tube_edge_cases_audit.py
```

If Minecraft still shows missing textures, remove old jars such as `1.94.0` from the `mods` folder and install only the jar built from this Stage205 source.

## Stage205 GitHub audit hotfix

Fixed GitHub Actions `Static source/resource audit` failure from the stale Stage161 version guard. The Stage161 research-note audit now accepts current Stage205 version `2.05.0` via semantic version parsing instead of old hard-coded version literals.

### Stage205 GitHub Compile Hotfix 2

- Fixed GitHub `compileJava` failure in `JarTubeInteractionRuntime.java`: lambda captured reassigned local `aspect`; now snapshots `final Aspect finalAspect` before `withStyle(...)`.
- No TC4 mechanics changed; Stage205 parity/resource pack fixes remain intact.


## Stage205 hard parity reset

Stage205 adds a hard rule: original Thaumcraft 4 1.7.10 is the source of truth. Fake GUI buttons, duplicate creative-tab shards, overlarge tree adapters, fake Thaumonomicon visibility and non-original texture overrides are being removed/quarantined as drift.

## Stage206 — TC4 original parity repair

Version: `2.09.0`

Stage206 continues the hard parity reset. Original Thaumcraft 4 1.7.10 source/assets/config are now explicitly treated as the source of truth before writing any Forge 1.19.2 adapter.

- Added `docs/TC4_ORIGINAL_PARITY_RULES_STAGE206.md`.
- Cleaned Goggles/Helmet of Revealing: no fake scan, no research unlock ticking, no fake Research/Warp HUD; goggles now expose TC4-style reveal semantics and 5% vis discount adapter.
- Added reveal-only aura node HUD when looking at an aura node with revealing gear.
- Switched aura node rendering to original `textures/misc/nodes.png` sprite sheet and moved old fake node sprites to `compatibility_quarantine/stage206_fake_node_sprites/`.
- Reworked standalone Research Table screen toward original `GuiResearchTable`: original `guiresearchtable2.png`, 5x5 aspect page, selected aspect slots, combine icon and page arrows.
- Research Note grid now uses original `hex1.png` / `hex2.png` instead of square fake cells.
- Corrected key TC4 recipes from `ConfigRecipes.java`: Goggles, Thaumometer, Infusion Matrix.
- Disabled wrong fallback/original-style placeholder recipes through quarantine conditions.

Stage207 should start full Infusion Matrix parity: multiblock, pedestal ring, activation, instability, particles, sounds, crafting state and failure effects.
