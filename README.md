# Thaumcraft Legacy Rebuild — Stage194

Strict original Thaumcraft 4 parity-port to Forge Minecraft 1.19.2.

Version: `1.94.0`.

This archive continues directly from Stage192 and adds Stage193 + Stage194 without adding new mechanics, GUI progression, non-original textures or invented behavior.

## Stage193

Arcane Workbench legacy packet/browser cleanup:

- Removed old browser-era runtime packet/screen files:
  - `RequestArcaneCraftPacket`
  - `RequestArcaneMenuCraftPacket`
  - `OpenArcaneWorkbenchPacket`
  - standalone `ArcaneWorkbenchScreen`
- Removed client helper methods for fake recipe-button crafting.
- Kept Arcane Workbench opening on the strict container path:
  - `NetworkHooks.openScreen(serverPlayer, workbench)`
  - `ArcaneWorkbenchMenu`
  - `ArcaneWorkbenchContainerScreen`
- Updated the TC4 class mapping so `GuiArcaneWorkbench` now points to `ArcaneWorkbenchContainerScreen`, not the deleted standalone screen.
- Preserved `ArcaneRecipeSyncPacket` only for client-side display/vis cost lookup; it no longer provides a craft button path.
- Kept `SLOT_LEGACY_CATALYST = 11` only as a clearly marked Stage135–188 save-migration adapter.

## Stage194

Consolidated full-port drift ledger:

- Added `TC4FullPortDriftLedger` runtime guard class.
- Added JSON ledger resource: `data/thaumcraft/tc4_drift/full_port_drift_ledger_stage194.json`.
- Added human-readable ledger: `docs/TC4_FULL_PORT_DRIFT_LEDGER_STAGE194.md`.
- Covered major systems:
  - golems;
  - wands/foci;
  - aura/nodes;
  - crucible;
  - infusion;
  - taint;
  - eldritch;
  - worldgen;
  - Thaumonomicon/research;
  - research table;
  - Arcane Workbench.

## New audits

- `scripts/tc4_stage193_arcane_cleanup_audit.py`
- `scripts/tc4_stage194_full_port_drift_ledger_audit.py`

## Notes

The only remaining Arcane Workbench non-original compatibility surface is the hidden save-migration slot for older Stage135–188 worlds. It is explicitly marked as a Forge 1.19.2 migration adapter and is not part of the strict original TC4 crafting flow.
