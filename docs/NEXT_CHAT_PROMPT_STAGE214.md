Продолжай с архива `thaumcraft_legacy_rebuild_STAGE214_TC4_CHAMPION_GENERATION_FX_1192_PARITY.zip`.

Контекст: перенос идёт на Minecraft/Forge 1.19.2, reference — `Thaumcraft4-1.7.10-master.zip`.

Stage214 уже добавил:
- `TC4ChampionModifierRuntime.maybeMakeSpawnChampion(Entity)` через `EntityJoinLevelEvent`;
- mirror `ConfigEntities.championModWhitelist` на registry ids 1.19.2;
- `ThaumcraftConfig.CHAMPION_MOBS`;
- creeper->bold rule;
- champion display names через `Component.translatable("champion.mod.*")`;
- `PacketFXChampion`, `ThaumcraftNetwork.sendChampionFx(...)` и `TC4ClientChampionFx`;
- showFX branches для всех ids 0..12;
- аудит `tc4_stage214_champion_generation_fx_audit.py`.

Следующий Stage215: сделай расширенный перенос Eldritch entity/boss parity:
- dedicated Eldritch Warden/Golem entities вместо Guardian-only approximation;
- boss title arrays и exact `generateName` formatting для Warden/Golem/Cultist Leader;
- Outer Lands/maze/key-room champion spawn hooks;
- review TC4 `EntityUtils.CHAMPION_MOD` attribute semantics vs текущий 1.19.2 adapter;
- renderer/nameplate/FX audit for boss branches;
- отдельный Stage215 audit script;
- прогони java/static/ci и Stage205–Stage215 audits;
- упакуй новый ZIP.
