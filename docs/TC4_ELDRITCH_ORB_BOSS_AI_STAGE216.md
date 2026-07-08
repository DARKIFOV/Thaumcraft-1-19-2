# Stage216 — TC4 Eldritch Orb / Boss AI Parity for Forge 1.19.2

Stage216 continues from Stage215 and targets the next drift cluster found by comparing the 1.19.2 port against original Thaumcraft 4.2.3.5 / Minecraft 1.7.10 sources.

## Original TC4 references checked

- `thaumcraft/common/entities/projectile/EntityEldritchOrb.java`
- `thaumcraft/common/entities/projectile/EntityGolemOrb.java`
- `thaumcraft/common/entities/monster/boss/EntityEldritchWarden.java`
- `thaumcraft/common/entities/monster/boss/EntityEldritchGolem.java`
- `thaumcraft/common/lib/world/dim/GenBossRoom.java`

## Ported in Stage216

### Eldritch Orb entity

Added `TC4EldritchOrbEntity` and registered it as `thaumcraft:eldritch_orb`.

TC4 parity carried over:

- zero gravity behavior;
- 100 tick timeout;
- radius-2 impact area;
- owner attack-damage multiplier `0.666F`;
- Wither for 160 ticks;
- byte-16 burst semantics via `PacketFXEldritchBoss` / `sendEldritchOrbBurst`.

### Golem Orb entity

Added `TC4GolemOrbEntity` and registered it as `thaumcraft:golem_orb`.

TC4 parity carried over:

- `red` flag and target id persistence;
- red lifetime 240 ticks, normal lifetime 160 ticks;
- original homing using distance-squared divisor;
- velocity clamp `±0.25`;
- damage multiplier `1.0F` for red and `0.6F` otherwise;
- projectile deflection from attacker look vector.

### Eldritch Warden ranged behavior

The Warden no longer applies direct fake damage for ranged attacks. It now spawns `TC4EldritchOrbEntity` with the original alternating arm offsets.

Added:

- 80% orb branch;
- 20% sonic branch;
- left/right arm-lift runtime state;
- status-byte packet bridge for arm FX;
- field-frenzy adapter;
- home teleport adapter during frenzy startup.

### Eldritch Golem headless behavior

The Golem headless beam now launches `TC4GolemOrbEntity` instead of directly damaging the target.

Added:

- 150 tick beam charge cadence;
- headless vent/electric FX;
- byte-19 arc FX bridge;
- arcing target NBT persistence.

### Outer Lands boss/key-room metadata

Added `TC4OuterLandsBossRoomMetadata` as a 1.19.2-safe structure metadata bridge for later dimension/structure stages.

Copied TC4 `GenBossRoom.PAT_DOORWAY` verbatim and added tags for:

- boss room marker;
- key room marker;
- feature id `2..5`;
- lock facing.

## New files

- `src/main/java/com/darkifov/thaumcraft/entity/projectile/TC4EldritchOrbEntity.java`
- `src/main/java/com/darkifov/thaumcraft/entity/projectile/TC4GolemOrbEntity.java`
- `src/main/java/com/darkifov/thaumcraft/network/PacketFXEldritchBoss.java`
- `src/main/java/com/darkifov/thaumcraft/client/fx/TC4ClientEldritchBossFx.java`
- `src/main/java/com/darkifov/thaumcraft/client/render/TC4EldritchOrbRenderer.java`
- `src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsBossRoomMetadata.java`
- `scripts/tc4_stage216_eldritch_orb_boss_ai_audit.py`
- `STAGE216_TC4_ELDRITCH_ORB_BOSS_AI_REPORT.json`

## Validation

Passed:

- `scripts/java_syntax_guard.py`
- `scripts/github_static_audit.py`
- `scripts/github_ci_guard.py`
- Stage205 through Stage216 parity audits

Gradle build remains blocked in this sandbox because Gradle wrapper cannot download `gradle-7.5.1-bin.zip` from `services.gradle.org` without internet.
