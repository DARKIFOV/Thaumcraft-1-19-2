# Stage213 — TC4 Fortress Armor + Champion Runtime parity (Forge 1.19.2)

Base archive: `thaumcraft_legacy_rebuild_STAGE212_TC4_FORTRESS_MASK_CURIOS_1192_PARITY.zip`.
Reference source: `Thaumcraft4-1.7.10-master.zip`.

## Goals

Stage213 continues the 1.19.2 port with two runtime gaps that remained after Stage212:

1. `ItemFortressArmor` special armor behavior from TC4 1.7.10.
2. `ChampionModifier` hurt/tick hooks used by `EventHandlerRunic`.

The implementation remains Forge/Minecraft 1.19.2-native.  No 1.7.10 APIs are imported; original behavior is represented through adapters, persistent tags and flattened registry ids.

## Fortress armor parity

Added `TC4FortressArmorRuntime`:

- preserved TC4 set multiplier constants:
  - base `0.875D`;
  - `+0.125D` for legs/chest/helm fortress pieces;
  - `+0.05D` when a scanned piece has the `mask` tag;
- preserved TC4 special armor divisors:
  - ordinary damage: defense / `25.0D`;
  - bypass armor: defense / `35.0D`;
  - fire/explosion/magic: defense / `20.0D`;
- added `isFullFortressSet`, `isFortressPiece` and event-side remaining-damage mitigation.

`TC4FortressArmorItem` now exposes the runtime helpers and continues to preserve `goggles`, `mask` and `RS.HARDEN` NBT behavior.

## Champion modifier parity

Added `TC4ChampionModifierRuntime`:

- persistent bridge tag: `TC4ChampionMod`;
- TC4 ids/names/types restored for all original modifiers:
  - `0 bold`, `1 spine`, `2 armor`, `3 mighty`, `4 grim`, `5 warded`, `6 warp`, `7 undying`, `8 fiery`, `9 sickly`, `10 venomous`, `11 vampiric`, `12 infested`;
- added `setChampion` and `makeChampion` helpers for migrated/native mobs;
- added periodic type-0 handling:
  - `warded` absorption regeneration every 25 ticks;
  - `undying` healing every 20 ticks;
- added defensive type-2 hurt handling:
  - `spine` thorns damage;
  - `armor` damage reduction to `amount * 19 / 25`;
  - `infested` Taint Crawler spawn adapter;
- added offensive type-1 hit handling:
  - `grim` wither 200 ticks;
  - `warp` temporary warp `1..3`;
  - `fiery` fire 4 seconds;
  - `sickly` hunger 500 ticks;
  - `venomous` poison 100 ticks;
  - `vampiric` heal `max(2, damage / 2)`.

Stage212 shield FX branch now delegates champion id lookup to this shared runtime.

## Warping gear adapter

Added `TC4WarpingGearAdapter` for original `IWarpingGear` behavior without importing the old API:

- void gear;
- cultist gear;
- crimson/void/primal tools;
- warp tooltip support;
- warp ward discount bridge.

## Client fortress layer

Added `TC4FortressArmorLayer` and registered it through `EntityRenderersEvent.AddLayers`:

- binds active copied textures:
  - `textures/models/fortress_armor.png`;
  - `textures/models/runic_goggles.png`;
- uses helmet/chest/legs slot visibility like the TC4 `ModelFortressArmor` branch;
- renders goggles overlay when helmet NBT contains `goggles`.

## Audits

Added `scripts/tc4_stage213_fortress_champion_runtime_audit.py`.
Older Stage210–Stage212 audits were updated to accept Stage213 version `2.13.0`.

Passing checks:

- `scripts/java_syntax_guard.py`
- `scripts/github_static_audit.py`
- `scripts/github_ci_guard.py`
- `scripts/tc4_stage205_hard_parity_reset_audit.py`
- `scripts/tc4_stage206_original_parity_repair_audit.py`
- `scripts/tc4_stage207_infusion_matrix_parity_audit.py`
- `scripts/tc4_stage208_infusion_renderer_enchantment_audit.py`
- `scripts/tc4_stage209_extended_infusion_failure_audit.py`
- `scripts/tc4_stage210_runic_augment_1192_audit.py`
- `scripts/tc4_stage211_runic_shield_runtime_audit.py`
- `scripts/tc4_stage212_fortress_mask_curios_audit.py`
- `scripts/tc4_stage213_fortress_champion_runtime_audit.py`

Gradle build could not complete in the sandbox because the wrapper attempted to download Gradle 7.5.1 from `services.gradle.org` and outbound DNS/network is unavailable.

## Suggested Stage214

Continue with champion visual FX and entity-generation parity:

- champion particle/showFX adapters for all 13 modifiers;
- automatic champion spawning in native mob generation paths;
- Eldritch Guardian/Warden champion persistence review;
- TC4 champion name localization and boss display names;
- deeper compile pass once Gradle dependencies are available.
