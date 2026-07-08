# Stage215 — TC4 Eldritch Boss + Champion Attribute Parity for Forge 1.19.2

Stage215 continues the port from Stage214 and keeps the target runtime on Minecraft/Forge 1.19.2.  This stage compares against the TC4 1.7.10 sources for `EntityThaumcraftBoss`, `EntityEldritchWarden`, `EntityEldritchGolem`, `EntityUtils`, `ChampionModifier`, and `ConfigEntities`.

## Added

- Dedicated `EldritchWardenEntity` registry entry and attributes.
- Dedicated `EldritchGolemEntity` registry entry and attributes.
- Shared `TC4ThaumcraftBossEntity` base adapter for the original boss behavior:
  - `HomeD`, `HomeX`, `HomeY`, `HomeZ` persistence;
  - `SpawnTimer` and `Anger` persistence;
  - boss despawn prevention;
  - spawn immunity;
  - passive boss regeneration cadence;
  - player-count HP/damage buff UUIDs matching TC4 `HPBUFF`/`DMGBUFF`.
- Warden parity:
  - title table from TC4 (`Aphoom-Zhah` through `Zushakon`);
  - title NBT key `title`;
  - 200 HP, 10 attack, 0.33 movement speed;
  - 150 tick spawn timer;
  - `0.66 * maxHealth` absorption spawn bonus;
  - champion boss name generation.
- Golem parity:
  - `headless` NBT key;
  - 250 HP, 10 attack, 0.30 movement speed;
  - lethal-hit headless transition;
  - 100 tick post-headless spawn timer;
  - 150 charge headless beam adapter;
  - champion boss name generation.
- Champion attribute parity improvements:
  - `tc.mobmod` persistent mirror;
  - `BOLDBUFF` UUID and speed modifier;
  - `MIGHTYBUFF` UUID and damage modifier;
  - warden/golem whitelist level `200`.

## 1.19.2 compatibility notes

The stage deliberately ports behavior through Forge 1.19.2 APIs.  It does not import old TC4/1.7.10 classes such as `NBTTag*`, `DataWatcher`, `SharedMonsterAttributes`, `func_*`, or `field_*` members.

## Verification

The new audit script is `scripts/tc4_stage215_eldritch_boss_champion_audit.py` and is added to GitHub Actions after the Stage214 audit.
