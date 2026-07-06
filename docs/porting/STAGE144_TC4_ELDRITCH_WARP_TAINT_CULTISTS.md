# Stage144 — TC4 Eldritch / Warp / Taint / Cultists pass

Stage144 continues the strict Thaumcraft 4 1.7.10 → Forge 1.19.2 rebuild after the Stage143 Golemancy sweep.  This pass focuses on the large Eldritch branch without touching the completed golem, research, wand, node, Crucible and Infusion code paths except where they already call common player warp data.

## Main fixes

- Replaced the single generic `Warp` runtime with TC4-style split buckets:
  - permanent warp (`Warp`, kept for old Stage1–143 save compatibility),
  - sticky warp (`WarpSticky`),
  - temporary warp (`WarpTemporary`),
  - warp counter (`WarpCounter`).
- Reworked warp events toward the original TC4 threshold flow:
  - actual warp excludes temporary warp,
  - hidden Eldritch unlocks follow TC4 gates: `BATHSALTS` > 10, `ELDRITCHMINOR` > 25, `ELDRITCHMAJOR` > 50,
  - temporary warp decays after events,
  - Warp Ward reduces event chance,
  - high-warp events can spawn taint, cave spiders, taint crawlers or Eldritch guardians.
- Added `TC4EldritchProgression` as the shared progression bridge for Crimson Rites, Eldritch Eye, altar and portal logic.
- Added item-use bridge for original TC4 preserved items:
  - `tc4_crimson_rites` unlocks `CRIMSON`,
  - `tc4_eldritch_object`, `_2`, `_3` perform Eldritch attunement.
- Replaced vanilla Enderman/Vex placeholder portal enemies with custom Stage144 mobs:
  - `eldritch_guardian`,
  - `crimson_cultist`,
  - `crimson_knight`,
  - `crimson_cleric`,
  - `crimson_praetor`.
- Reworked the Eldritch altar and portal checks to use TC4 Eldritch minor/major/start progression and actual warp instead of the old single-warp placeholder gate.
- Added a shared taint spread runtime used by tainted soil, taint crawler and taint seed.
- Hardened tainted soil strength toward the original TC4 taint block behavior.

## Key touched files

- `src/main/java/com/darkifov/thaumcraft/data/PlayerThaumData.java`
- `src/main/java/com/darkifov/thaumcraft/event/WarpEvents.java`
- `src/main/java/com/darkifov/thaumcraft/event/EldritchItemEvents.java`
- `src/main/java/com/darkifov/thaumcraft/eldritch/TC4EldritchProgression.java`
- `src/main/java/com/darkifov/thaumcraft/entity/CrimsonCultistEntity.java`
- `src/main/java/com/darkifov/thaumcraft/entity/EldritchGuardianEntity.java`
- `src/main/java/com/darkifov/thaumcraft/block/EldritchEyeItem.java`
- `src/main/java/com/darkifov/thaumcraft/block/EldritchAltarBlock.java`
- `src/main/java/com/darkifov/thaumcraft/block/EldritchPortalBlock.java`
- `src/main/java/com/darkifov/thaumcraft/blockentity/EldritchPortalBlockEntity.java`
- `src/main/java/com/darkifov/thaumcraft/taint/TaintSpreadRuntime.java`
- `src/main/java/com/darkifov/thaumcraft/block/TaintedSoilBlock.java`
- `src/main/java/com/darkifov/thaumcraft/block/TaintSeedItem.java`
- `src/main/java/com/darkifov/thaumcraft/entity/TaintCrawlerEntity.java`
- `src/main/java/com/darkifov/thaumcraft/client/render/TC4BlockMobRenderer.java`
- `src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java`
- `src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java`

## Validation performed in this sandbox

```bash
python3 scripts/java_syntax_guard.py
python3 scripts/github_static_audit.py
python3 scripts/tc4_stage143_golemancy_whole_sweep_audit.py
python3 scripts/tc4_wand_parity_audit.py
python3 scripts/tc4_book_table_workbench_audit.py
python3 scripts/tc4_full_parity_audit.py
python3 scripts/tc4_texture_audit.py
python3 scripts/tc4_stage144_eldritch_warp_taint_audit.py
```

The full Gradle build was attempted but could not run in the offline sandbox because the wrapper needs `services.gradle.org` to download Gradle 7.5.1.  This is an environment/network limitation, not a Java syntax failure.

## Known next work

- Replace block-placeholder renderers with real TC4 cultist/guardian models.
- Continue exact TC4 Outer Lands structure, boss and loot table parity.
- Deepen taint biome/worldgen spread and fibrous taint behavior.
- Add exact TC4 warp event localization/message keys and remaining potion/event variants.
