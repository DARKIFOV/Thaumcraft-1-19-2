# Stage219 — TC4 Guardian Renderer, ModelPart Layer Anchors, GenPortal Adapter

Target: Minecraft/Forge 1.19.2.
Original source checked: `Thaumcraft4-1.7.10-master.zip` / TC4 4.2.3.5.
Base archive: Stage218.

## What changed

### Eldritch Guardian rendering

Stage218 still rendered the normal Eldritch Guardian through a block-placeholder renderer. Stage219 replaces that with `TC4EldritchGuardianRenderer`, anchored to original `RenderEldritchGuardian`:

- texture `textures/models/eldritch_guardian.png`;
- shadow radius `0.5F`;
- alpha-blended render path;
- non-Outer-Lands distance fade:
  - close range: `base = 0.6F`;
  - Hard max distance: `576.0F`;
  - other difficulties max distance: `1024.0F`;
- Outer Lands full-alpha branch.

Warden/Golem renderers remain the Stage217/218 bridge renderers, but now the regular Guardian is no longer a fake block mob.

### ModelPart/LayerDefinition anchors

Added `TC4EldritchBossLayerDefinitions`, a Forge 1.19.2 layer-definition anchor for high-impact parts from:

- `ModelEldritchGuardian`;
- `ModelEldritchGolem`.

The class registers baked-layer contracts for:

- `eldritch_guardian`;
- `eldritch_warden`;
- `eldritch_golem`.

It includes original part names and rotations such as `HoodEye`, `Hood4`, `LegpanelC3`, `Torso`, `Head`, `CollarL`, and `HeadlessVent`. This is not the final full model tree yet, but it removes the previous “metadata only” state and gives the client a stable 1.19.2 ModelPart registration contract for Stage220 to consume directly.

### Outer Lands GenPortal / GenCommon bridge

Added `TC4OuterLandsGenCommonAdapter`:

- preserves the original numeric `GenCommon.placeBlock` palette codes: `1`, `2`, `8`, `9`, `10`, `11`, `15`, `16`, `17`, `18`, `19`, `20`, `21`, `99`;
- ports the visible `GenPortal.generatePortal` 16×16×13 loop structure;
- keeps the original `generateConnections(..., 3, true)` contract;
- places the center altar/portal/obelisk using current 1.19.2 blocks;
- avoids importing old 1.7.10 APIs like `ForgeDirection`, `World`, `func_*`, or `NBTTag*`.

`EldritchPortalBlockEntity#buildArena` now calls `TC4OuterLandsBossRoomPlacer.placePortalRoom(...)` before the boss/key-room bridge, so the portal encounter starts using the new GenPortal adapter rather than only the small Stage217 arena shell.

## Known gaps for Stage220

- Use the registered `ModelLayerLocation` in Warden/Golem/Guardian renderers instead of the low-level cuboid `VertexConsumer` bridge.
- Continue direct port of remaining `ModelEldritchGuardian` and `ModelEldritchGolem` parts.
- Add dedicated `EntityEldritchCrab` and `TileEldritchCrabSpawner` equivalents used by `GenCommon` decoration queues.
- Expand `Gen2x2`, `GenPassage`, `GenLibraryRoom`, and `GenNestRoom` from source instead of placing only portal/boss/key-room bridges.

## Verification

Run from repo root:

```bash
python scripts/java_syntax_guard.py
python scripts/github_static_audit.py
python scripts/github_ci_guard.py
python scripts/tc4_stage219_guardian_model_portal_audit.py
```
