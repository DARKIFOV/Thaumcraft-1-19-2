# Stage220 — TC4 Eldritch Crab, Baked Models, Library/Nest Room Parity

Target remains **Minecraft/Forge 1.19.2**. This stage continues from Stage219 and keeps the implementation aligned with the original `Thaumcraft4-1.7.10-master.zip` source.

## Original TC4 anchors used

- `thaumcraft/common/entities/monster/EntityEldritchCrab.java`
- `thaumcraft/common/tiles/TileEldritchCrabSpawner.java`
- `thaumcraft/client/renderers/models/entities/ModelEldritchCrab.java`
- `thaumcraft/client/renderers/entity/RenderEldritchCrab.java`
- `thaumcraft/common/lib/world/dim/GenLibraryRoom.java`
- `thaumcraft/common/lib/world/dim/GenNestRoom.java`

## Ported in Stage220

- Added `EldritchCrabEntity` with TC4-sized `0.8F x 0.6F` entity registration.
- Preserved original crab behaviour anchors:
  - XP reward `6`;
  - health `20`, attack `4`;
  - leap strength `0.63F`;
  - HARD difficulty always helmed, otherwise 33% helm chance;
  - `Flags` byte NBT and helm bit `0x1`;
  - helm armor value `5` and movement `0.275D`, bare movement `0.30D`;
  - bare crab head-latch / riding pounce branch;
  - original sound keys `crabclaw`, `crabtalk`, `crabdeath`;
  - emerald death drop branch.
- Added `EldritchCrabSpawnerBlock` and `EldritchCrabSpawnerBlockEntity` as a 1.19.2-safe adapter for original `TileEldritchCrabSpawner`.
- Spawner parity anchors:
  - start count `150`;
  - warmup vent event at `count == 15`;
  - reset `50 + rand(50)` when inactive;
  - post-spawn reset `150 + rand(100)`;
  - activation requires nearest player within `16` blocks;
  - max entity cap uses `>5` crabs within `32` blocks;
  - spawner strips crab helm and launches it along stored facing;
  - persistent `facing` byte NBT equivalent.
- Added crab renderer with original texture `textures/models/crab.png` via active path `textures/original/thaumcraft4/models/crab.png`.
- Added baked `ModelPart` bridge for Eldritch Guardian/Warden/Golem instead of only manual cuboid render calls.
- Added `createCrabBodyLayer()` using original `ModelEldritchCrab` part names and texture atlas size `128x64`.
- Added first direct adapters for `GenLibraryRoom` and `GenNestRoom`.
- Palette codes `15` and `16` now place the real crab spawner block instead of falling back to Eldritch Stone.

## Known remaining drift

- The crab helm break currently drops vanilla iron chestplate as a temporary fallback because the dedicated crimson plate armor item registry has not been ported yet.
- `GenLibraryRoom` and `GenNestRoom` geometry now follows TC4 loops and key room features, but full TC4 block palette parity still needs dedicated cosmetic blocks, loot urns/crates and crystal tile orientation parity.
- Guardian/Warden/Golem now go through baked `ModelPart` roots, but the full decompiled model tree is not yet completely migrated.

## Verification

Run:

```bash
python scripts/java_syntax_guard.py
python scripts/github_static_audit.py
python scripts/github_ci_guard.py
python scripts/tc4_stage220_crab_model_rooms_audit.py
```

Gradle compile still requires internet in this sandbox because the wrapper downloads `gradle-7.5.1-bin.zip` from `services.gradle.org`.
