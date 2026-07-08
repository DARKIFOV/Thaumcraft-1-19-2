Продолжай с архива `thaumcraft_legacy_rebuild_STAGE213_TC4_FORTRESS_CHAMPION_RUNTIME_1192_PARITY.zip`.

Контекст: перенос идёт на Minecraft/Forge 1.19.2, reference — `Thaumcraft4-1.7.10-master.zip`.

Stage213 уже добавил:
- `TC4FortressArmorRuntime` с TC4 `ItemFortressArmor` special armor ratio/set bonus;
- `TC4ChampionModifierRuntime` с ids/types/effects 0..12;
- `TC4WarpingGearAdapter`;
- `TC4FortressArmorLayer` и активные `textures/models/fortress_armor.png`, `runic_goggles.png`;
- аудит `tc4_stage213_fortress_champion_runtime_audit.py`.

Следующий Stage214: сделай расширенный перенос champion visual FX/showFX и entity champion generation parity:
- particle/showFX adapters для всех 13 `ChampionMod*`;
- автоматическое `makeChampion` в native mob spawn paths там, где TC4 делал champion mobs;
- сохранение champion persistence/display names для Eldritch Guardian/Warden/Cultist Leader branches;
- localization keys `champion.mod.*`;
- отдельный audit script Stage214;
- прогони java/static/ci и Stage205–Stage214 audits;
- упакуй новый ZIP.
