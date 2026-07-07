# Original TC4 Porting Status

- Current archive: Stage194.
- Version: `1.94.0`.
- Strict parity rule remains active: do not invent new mechanics, items, recipes, GUI, progression, textures or behavior while original TC4 content remains unported.
- Remaining estimate after Stage194: 1–21 stages.
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
