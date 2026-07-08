# TC4 Mega Stage273-282 — Eldritch tile runtime, Mind Spider and split blockEldritch variants

Base archive: `thaumcraft_legacy_rebuild_STAGE263_272_TC4_PASSAGE_LIBRARY_NEST_CLEANUP_BATCH_1192_PARITY.zip`.
Target remains Forge/Minecraft 1.19.2. Reference source remains `Thaumcraft4-1.7.10-master.zip`.

## Ported in this mega-stage

- Added dedicated `MindSpiderEntity` instead of the Stage263-272 cave-spider spawner placeholder.
- Preserved TC4 `EntityMindSpider` data contract:
  - size `0.3F x 0.3F` via the 1.19.2 entity type;
  - XP reward `1`;
  - health `1`;
  - attack damage `1`;
  - follow range `12`;
  - `harmless` byte;
  - `viewer` string;
  - harmless lifespan `1200` ticks;
  - no vanilla drops;
  - no climbing.
- `GenPassage` feature `14` now creates a real `thaumcraft:mind_spider` spawner.
- Added block-entity runtime equivalents for:
  - `TileEldritchCap`;
  - `TileEldritchLock`;
  - `TileEldritchTrap`;
  - `TileEldritchCrystal`.
- `EldritchLock` now follows the original delayed lock cadence:
  - `count=-1` idle;
  - activation by key starts count at `0`;
  - pump sound every `5` ticks;
  - boss cycle after `100` ticks;
  - `BossMapData.bossCount` equivalent saved as `TC4OuterLandsBossCycleData`.
- `EldritchTrap` now follows the original ticking trap cadence:
  - initial `count=20`;
  - reset `10 + rand(25)`;
  - nearest player range `3`;
  - magic damage `2`;
  - 50% warp chance `1 + rand(2)`;
  - `PacketFXBlockZap` bridge.
- Split more flattened `blockEldritch` metadata variants into proper 1.19.2 ids:
  - meta `4` -> `eldritch_crust`;
  - meta `5` -> `eldritch_decorative`;
  - meta `7` -> `eldritch_door`;
  - meta `8` -> `eldritch_lock`;
  - meta `9` -> `eldritch_crab_spawner`;
  - meta `10` -> `eldritch_trap`.
- Extended the baked model tree for Eldritch Guardian/Warden/Golem with another set of original part names.
- Fixed a Stage220 crab spawner particle coordinate typo.

## Validation

The new audit is `scripts/tc4_stage273_282_mega_eldritch_tiles_mindspider_audit.py`.
It checks the new entity, block-entity registrations, split variant resources, feature-14 spawner target, boss cycle data and model-part expansion.

Gradle compilation still requires internet access for the wrapper distribution in this sandbox.
