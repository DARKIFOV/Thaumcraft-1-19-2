# Stage217 — TC4 Eldritch Renderer + Outer Lands Room Parity (Forge 1.19.2)

## Scope
Continued from Stage216.  This stage compares the port against the original TC4 1.7.10 Eldritch boss renderer and room-generation sources, then ports the next runtime block without importing legacy APIs.

## Original TC4 anchors checked
- `RenderEldritchGuardian` uses `textures/models/eldritch_warden.png`, alpha blending, Warden 1.5 scale and a 150 tick spawn-sink offset.
- `RenderEldritchGolem` uses `textures/models/eldritch_golem.png`, alpha blending and 2.15 boss scale.
- `GenBossRoom.PAT_DOORWAY` is already mirrored by Stage216 and is now consumed by real 1.19.2 placement code.

## Ported in Stage217
- Added `TC4EldritchWardenRenderer`:
  - dedicated Warden renderer registration;
  - original TC4 Warden texture path;
  - 1.5 scale;
  - alpha/translucent render pass;
  - spawn-timer sink using `getSpawnTimer() / 150.0F`;
  - left/right arm-lift render state from Stage216 status packets;
  - anger/spawn glow overlay.
- Added `TC4EldritchGolemRenderer`:
  - dedicated Golem renderer registration;
  - original TC4 Golem texture path;
  - 2.15 scale;
  - alpha/translucent render pass;
  - headless renderer branch;
  - beam-charge glow using the 150 tick charge ratio.
- Added client FX state bridges:
  - `EldritchWardenEntity.clientArmLift(boolean)`;
  - `EldritchGolemEntity.clientStartArc(BlockPos)`;
  - packet handler now updates those render states when TC4 status bytes `15/16/19` arrive.
- Added `TC4OuterLandsBossRoomPlacer`:
  - `placeBossRoom`;
  - `placeKeyRoom`;
  - `placeDoorway` consuming `PAT_DOORWAY`;
  - Warden/Golem boss spawn hooks;
  - altar lock-cell placement;
  - 1.19.2-safe block placement instead of direct 1.7.10 worldgen imports.
- Integrated the room placer into the current Eldritch portal arena bridge so starting an encounter now also lays down a first boss/key-room placement stub.

## Compatibility notes
- The full 1.7.10 `ModelRenderer` trees are not copied verbatim yet.  Stage217 uses cuboid bridge models with the original textures and exact scale/render-state contracts.
- No `GL11`, `func_*`, `NBTTag*`, `DataWatcher`, `ForgeDirection`, or `IEntityAdditionalSpawnData` APIs were introduced in Stage217 classes.
- The Gradle wrapper still cannot download Gradle in this sandbox, so full compilation remains blocked by network access rather than code-path intent.

## Added files
- `src/main/java/com/darkifov/thaumcraft/client/render/TC4EldritchWardenRenderer.java`
- `src/main/java/com/darkifov/thaumcraft/client/render/TC4EldritchGolemRenderer.java`
- `src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsBossRoomPlacer.java`
- `scripts/tc4_stage217_eldritch_render_room_audit.py`
- `docs/TC4_ELDRITCH_RENDER_ROOM_STAGE217.md`
- `docs/NEXT_CHAT_PROMPT_STAGE217.md`
- `STAGE217_TC4_ELDRITCH_RENDER_ROOM_REPORT.json`
