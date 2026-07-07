# TC4 full-port drift ledger — Stage194

This ledger is a strict parity guard for the Minecraft 1.7.10 Thaumcraft 4 → Forge 1.19.2 port. It does not authorize new mechanics. Every remaining drift item must be fixed by porting the original TC4 behavior or be kept as an explicitly named Forge 1.19.2 adapter.

## Runtime/audited systems

- **Wands/foci:** root NBT `rod`, `cap`, `sceptre`, `focus`; focus upgrade NBT list `upgrade`; Focus Pouch `Inventory`/`Slot`; WandManager/IArchitect NBT `areax`, `areay`, `areaz`, `aread`, `picked`; continuous use hooks; projectiles/renderers.
- **Arcane Workbench:** original slot layout grid `0–8`, output `9`, wand `10`; menu order output/wand/grid/player/hotbar; vanilla-first fallback; output-take crafting; right-click/drag restrictions; wand/staff/sceptre component recipes.
- **Research Table:** original slots, `bonusAspects`, note copy behavior, axial hex grid, note completion/consumption and GUI drag/drop hit testing.
- **Thaumonomicon/research:** original keys, categories, parents, hidden parents, siblings, coordinates, icons, warp, pages, progression and recipe gates.
- **Infusion:** catalyst/component lookup, enchantment index, instability events, unlock gates and materialized recipes.
- **Crucible:** boil/heat, thrown-item alchemy, aspect consumption, flux spill blocks and recipe gates.
- **Aura/nodes:** profiles, types/modifiers, scan runtime, node jar runtime, stabilizer/transducer registrations.
- **Taint:** block metadata split, fibres age bridge, spread adjacency and texture guard.

## Partial systems that still require original TC4 parity work

- **Golems:** exact AI task scheduler, core behavior matrix, upgrade stacking behavior, seal/marker parity.
- **Eldritch:** dimension/maze generation, ritual progression, boss/cultist AI, portal/event behavior.
- **Worldgen:** exact weighted feature placement, biome/dimension filters, structures/loot, ore/cluster positional parity.
- **Essentia transport:** exact suction network and edge-case tube/jar behavior still need a dedicated final audit beyond the existing runtime blocks.

## Stage193 cleanup result

Legacy Arcane Workbench browser-era packets/screens were removed from runtime code:

- `RequestArcaneCraftPacket`
- `RequestArcaneMenuCraftPacket`
- `OpenArcaneWorkbenchPacket`
- standalone `ArcaneWorkbenchScreen`

Arcane Workbench opening now stays on the original-style Forge 1.19.2 container path through `NetworkHooks.openScreen`, `ArcaneWorkbenchMenu` and `ArcaneWorkbenchContainerScreen`. The only remaining hidden slot is `SLOT_LEGACY_CATALYST = 11`, explicitly marked as save-migration-only for older Stage135–188 saves.

## Ledger resources

- Runtime class: `src/main/java/com/darkifov/thaumcraft/porting/TC4FullPortDriftLedger.java`
- Data resource: `src/main/resources/data/thaumcraft/tc4_drift/full_port_drift_ledger_stage194.json`
