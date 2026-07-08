# Original TC4 Porting Status

- Current archive: Stage204.
- Version: `2.04.0`.
- Strict parity rule remains active: do not invent new mechanics, items, recipes, GUI, progression, textures or behavior while original TC4 content remains unported.
- Remaining estimate after Stage204: 1–11 stages.
- Historical audit estimate tokens retained: 1–23; 2–25; 4–27; legacy Stage147 token: 8–31.
- Current consolidated drift ledger: `docs/TC4_FULL_PORT_DRIFT_LEDGER_STAGE194.md`.

## Completed highlights

- Stage147: returned project to strict original TC4 parity.
- Stage148: explicit research icon coverage.
- Stage149: strict ResearchPage parity.
- Stage150: strict research metadata parity.
- Stage151: runtime progression bridge.
- Stage152: strict recipe unlock parity.
- Stage153: additional original infusion recipes and catalyst/component lookup.
- Stage154: dedicated TC4InfusionEnchantmentIndex.
- Stage155: exact recipe resolver.
- Stage156: materialized 10 exact original focus recipes.
- Stage157: exact object/entity aspect database.
- Stage158: Thaumometer entity scan runtime.
- Stage159: player thaum data scan knowledge.
- Stage160: original aspect decomposition foundation.
- Stage161: TC4 axial hex research-note grid.
- Stage162: Research Note completion/consumption parity.
- Stage163: Research Table ink/scribing tools validation adapter.
- Stage164: Research Note GUI drag/drop and q/r axial-grid hit testing.
- Stage165: persistent ResearchTableBlockEntity/Menu with original slot 0/slot 1.
- Stage166: Research Table container screen/actions and drift audit.
- Stage167: Research Table visual parity.
- Stage168: RESEARCHDUPE copy parity.
- Stage169: original TileResearchTable.bonusAspects model.
- Stage170: bonus aspects sync and placement consumption.
- Stage171: wand focus behavior parity foundation.
- Stage172: base cooldown/cost sync for core foci.
- Stage173: FocusUpgradeType and FocusUpgradeRuntime original IDs/NBT rank list.
- Stage174: focus projectile entity foundation.
- Stage175: focus upgrade effect parity batch 1.
- Stage176: focus projectile visual/sound/particle parity batch.
- Stage177: WandManager/IArchitect area/picked-block adapter.
- Stage178: projectile behavior parity batch.
- Stage179: architect overlay/keybind packet parity.
- Stage180: continuous focus-use parity for Fire/Shock/Excavation.
- Stage181: client focus FX adapter for Shock lightning/sparkles and Excavation beamCont/excavateFX.
- Stage182: focus animation/use-state parity, removing vanilla bow-like drift.
- Stage183: wand focus item renderer/layer parity: focus cube, depth, ornament, rune texture and original focus colors.
- Stage184: remaining focus behavior drift: own sounds, Equal Trade radius, Warding delay, Primal cost window, Fire range/event.
- Stage185: wand/staff component renderer parity: original ModelWand rod/cap/staff/sceptre transforms, glowing rod lightmap, primal staff runes, sceptre capacity/cost root NBT.
- Stage186: focus pouch/focus equip UI parity: original Inventory NBT, 18 focus slots, ContainerFocusPouch layout, GuiFocusPouch texture/size/highlight, ItemFocusPouchBauble adapter.
- Stage187: wand crafting/sceptre table parity: original ArcaneWandRecipe/ArcaneSceptreRecipe patterns, root rod/cap/sceptre NBT, primal cost formulas and sceptre focus restrictions.
- Stage188: original focus-selection packet/key flow parity: WandManager.changeFocus adapter, PacketFocusChangeToServer adapter, F/shift+F semantics, pouch/inventory scan order and cameraticks sound.
- Stage189: original Arcane Workbench GUI/container flow parity: original slot order, output preview/onTake crafting, no recipe browser/search/Craft button, aspect-cost locations and insufficient-vis adapter.
- Stage190: original ConfigRecipes wand component recipes: caps, greatwood rod, staff rods, ConfigRecipes costs, generated assembly chain and prebuilt wand shortcuts marked as adapter drift.
- Stage191: exact SlotCraftingArcaneWorkbench edge cases: vanilla-first fallback, original matrix consumption with container items, shift-click routing, click/drag restrictions and server-side staff rejection.
- Stage192: final wand/focus regression audit across focus runtime, pouch/equip, architect NBT, projectiles/renderers, wand/staff/sceptre NBT, ConfigRecipes and Arcane Workbench interactions.
- Stage193: removed legacy Arcane Workbench browser-era packet/screen craft paths and kept only the original-style container flow plus explicit save-migration adapter.
- Stage194: added consolidated full-port drift ledger covering golems, wands/foci, aura/nodes, crucible, infusion, taint, eldritch, worldgen, Thaumonomicon/research, research table and Arcane Workbench.

## Current next work

Stage195 + Stage196 should use the Stage194 drift ledger to attack remaining high-impact drift without inventing systems:

- Stage195: golem core/AI parity batch 1 — port original golem inventory/task targeting/core behavior tables, bell/marker semantics and upgrade slots from TC4 source.
- Stage196: essentia transport suction parity batch — jar/tube/valve suction, label/filter behavior, transfer priorities and tick timing against original TC4 TileTube/TileJar logic.

## Historical drift guard phrases retained for audits

- Stage166/Stage168 Drift check retained: persistent ResearchTableBlockEntity is the primary path and compatibility fallback is only allowed where explicitly marked as a Forge 1.19.2 adapter.
- Stage189 drift guard retained: Arcane Workbench must not reintroduce recipe browser/search/Craft button because original `GuiArcaneWorkbench` renders only the workbench texture, output preview and aspect costs.
- Stage194 drift ledger guard: remaining differences must be fixed by original TC4 porting or explicitly marked as Forge 1.19.2 adapters.

## Stage195 + Stage196

- Stage195 restored strict TC4 golem metadata foundations: original `EnumGolemType`, `ItemGolemCore` metadata, `ItemGolemUpgrade` metadata, core capability flags, original golem NBT names, carry/inventory/attribute formula adapters.
- Stage196 restored strict TC4 essentia suction foundations: original `TileTube` state/NBT keys, open side tracking, suction snapshot cadence, jar suction values and `TileJarFillable` NBT compatibility.
- Remaining golem drift: full original AI task classes are still adapted through `ThaumGolemEntity` tick methods and need Stage197 task-table parity.
- Remaining essentia drift: exact tube renderer animation/filter GUI polish remains; Stage198 added subclass state and registered block variants.


## Stage198 resource pack metadata adapter

Added Forge 1.19.2 `pack.mcmeta` with pack_format 9 to stop resource-pack metadata load warnings. This is packaging-only; no TC4 behavior drift introduced.


## Stage199-200 status

- Stage199: added original `ItemGolemBell` marker NBT parity adapter (`golemid`, `golemhomex/y/z`, `golemhomeface`, `markers`). Bell can bind to a golem and sync original marker lists into the Forge 1.19.2 golem task runtime.
- Stage200: added tube/jar renderer and visual label/filter parity batch. Tube subtype models/items now use subtype-specific textures, jar filter labels render with original TC4 `label.png`, and resource-pack texture audits now guard `pack.mcmeta` plus model texture references.
- Texture warning note: old Stage194/1.94.0 builds could show broken textures because Minecraft could not load valid mod resource-pack metadata. Current Stage200 keeps `pack.mcmeta` with `pack_format: 9` for MC 1.19.2 and verifies it in audits.

Remaining estimate after Stage200: roughly 1-15 stages, depending on how deep the final golem GUI, jar/tube interaction, renderer polish, and full regression sweep go.

## Stage201–Stage202

- Stage201: `GuiGolem` / `ContainerGolem` parity adapter added with original texture, slots, scroll, toggles, color semantics and golem pause behavior.
- Stage202: jar/tube `AspectFilter` interaction parity adapter added for labels/phials and original NBT names.
- Resource pack metadata and texture audit from Stage198/200 remains enforced.


## Stage203–Stage204

- Stage203: `ContainerGolem` ghost-slot parity restored. Golem GUI inventory slots now behave like original `SlotGhost`/`SlotGhostFluid`: copy-only filter stacks, no real item consumption, fill-core stack limit 256, liquid-core fluid-container validation, shift-click/empty-hand count semantics and color/toggle behavior preserved.
- Stage204: jar/tube transfer edge cases tightened against original `TileJarFillable`, `TileJarFillableVoid`, `TileTubeFilter`, `TileTubeRestrict`, `TileTubeOneway` and `TileTubeBuffer`: original add/take return semantics, void jar overflow consumption, jar suction values, one-way direction gates and subtype filter/restrict/buffer flow checks.
- Resource-pack metadata remains enforced through `pack.mcmeta` with `pack_format: 9`; jar/tube/golem GUI texture audits remain active.

Remaining estimate after Stage204: roughly 1–11 stages, mostly final golem AI/renderer polish, exact tube raytrace/subHit side interaction, remaining jar/tube renderer details and full regression sweep.

## Stage205–Stage207

- Stage205: hard parity reset removed/disabled several fake player-facing paths: rebuild/debug research-table buttons, fake reveal progression side effects, duplicate shard creative clutter and wrong fallback recipes; original TC4 resources/config became the mandatory source of truth.
- Stage206: original reveal gear/aura-node parity repair. Goggles/Helmet of Revealing now keep TC4 reveal semantics instead of fake scan/research/warp effects; node renderer uses original `textures/misc/nodes.png`; Research Table/Research Note visuals moved back toward original textures and coordinates; Goggles/Thaumometer/InfusionMatrix recipes were corrected from `ConfigRecipes.java`.
- Stage207: Infusion Matrix parity start. Matrix activation now mirrors original TC4 two-phase wand flow (`active` first, `crafting` second), validates center pedestal + four diagonal pillars before activation, stores original NBT aliases, exposes original symmetry penalty, separates essentia range 12 from craft-cycle delay 10, and delays component consumption by the original five-tick source-FX travel step.

Remaining estimate after Stage207: roughly 1–9 stages, mostly Infusion Matrix renderer/output/enchantment parity, exact source FX packets, remaining failure effects, exact component ItemStack matching, and final full-regression sweep.
