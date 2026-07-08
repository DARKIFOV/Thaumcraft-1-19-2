# Stage214 — TC4 champion generation + showFX parity (Forge 1.19.2)

Base archive: `thaumcraft_legacy_rebuild_STAGE213_TC4_FORTRESS_CHAMPION_RUNTIME_1192_PARITY.zip`.
Reference source: `Thaumcraft4-1.7.10-master.zip`.

## Goals

Stage214 continues the 1.19.2 port by covering the champion pieces that remained after Stage213:

1. `RenderEventHandler` champion `showFX` dispatch.
2. `EventHandlerEntity` automatic champion mob generation.
3. `EntityUtils.makeChampion` display-name/persistence behavior.
4. TC4 `champion.mod.*` localization and boss/native display-name bridge.

No TC4 1.7.10 API imports were introduced.  The implementation is expressed as Forge 1.19.2 events, packets, persistent tags and vanilla particles.

## Champion generation parity

`TC4ChampionModifierRuntime` now includes a `ConfigEntities.championModWhitelist` mirror keyed by 1.19.2 registry ids:

- vanilla: zombie, spider, blaze, enderman, skeleton, witch;
- Thaumcraft port mobs: eldritch guardian, taint crawler, crimson cultist/knight/cleric/praetor.

The runtime now exposes `maybeMakeSpawnChampion(Entity)` and `CommonEvents` hooks it from `EntityJoinLevelEvent`.

The TC4 probability branches are represented:

- `Config.championMobs` -> `ThaumcraftConfig.CHAMPION_MOBS`;
- Easy difficulty penalty;
- Hard difficulty bonus;
- Nether/End/Outer/Eldritch dimension bonus;
- dangerous block proximity bonus for taint/eldritch/flux blocks;
- whitelist-level subtraction;
- boss-like persistence for praetor/warden/golem/taintacle-style entities.

`makeChampion` now keeps the TC4 creeper rule: creepers always become `bold` champions.

## Display name and persistence parity

Stage214 keeps the Stage213 NBT bridge tags and adds generation sentinel:

- `TC4ChampionMod`;
- `TC4ChampionPersist`;
- `TC4ChampionName`;
- `TC4ChampionGenerationChecked`.

Champion display names are refreshed with 1.19.2 `Component.translatable(...)`, using preserved keys such as `champion.mod.bold`, `champion.mod.spine`, etc.  Additional port-side display patterns were added for generic champions, Eldritch Guardian and Crimson Praetor/Cultist branches.

## showFX parity

TC4 called `ChampionModifier.mods[t].effect.showFX(mob)` client-side in `RenderEventHandler.livingTick`.

Stage214 mirrors this with:

- `PacketFXChampion`;
- `ThaumcraftNetwork.sendChampionFx(...)`;
- `TC4ClientChampionFx`.

`TC4ClientChampionFx` contains branches for every original champion id:

- `0 bold`;
- `1 spine`;
- `2 armor`;
- `3 mighty`;
- `4 grim`;
- `5 warded`;
- `6 warp`;
- `7 undying`;
- `8 fiery`;
- `9 sickly`;
- `10 venomous`;
- `11 vampiric`;
- `12 infested`.

The particles use 1.19.2 vanilla particle types to approximate the original TC4 generic particle and spark calls.

## Audits

Added `scripts/tc4_stage214_champion_generation_fx_audit.py`.
Older Stage210–Stage213 audits were adjusted to accept Stage214 version `2.14.0`.

Passing checks:

- `scripts/java_syntax_guard.py`;
- `scripts/github_static_audit.py`;
- `scripts/github_ci_guard.py`;
- `scripts/tc4_stage205_hard_parity_reset_audit.py`;
- `scripts/tc4_stage206_original_parity_repair_audit.py`;
- `scripts/tc4_stage207_infusion_matrix_parity_audit.py`;
- `scripts/tc4_stage208_infusion_renderer_enchantment_audit.py`;
- `scripts/tc4_stage209_extended_infusion_failure_audit.py`;
- `scripts/tc4_stage210_runic_augment_1192_audit.py`;
- `scripts/tc4_stage211_runic_shield_runtime_audit.py`;
- `scripts/tc4_stage212_fortress_mask_curios_audit.py`;
- `scripts/tc4_stage213_fortress_champion_runtime_audit.py`;
- `scripts/tc4_stage214_champion_generation_fx_audit.py`.

Gradle build could not complete in the sandbox because the wrapper attempted to download Gradle 7.5.1 from `services.gradle.org` and outbound DNS/network is unavailable.

## Suggested Stage215

Continue with Eldritch entity parity:

- restore dedicated Eldritch Warden/Golem entities instead of only Guardian approximation;
- port boss title arrays and exact `generateName` formatting;
- review Outer Lands/maze champion generation hooks;
- add champion aura/attribute parity close to TC4 `EntityUtils.CHAMPION_MOD` instead of the current 1.19.2 attribute adapter;
- add renderer nameplate/FX checks for boss branches.
