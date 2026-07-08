Продолжи портирование Thaumcraft Legacy Rebuild строго на Minecraft/Forge 1.19.2, не отходя от оригинального Thaumcraft 4 для Minecraft 1.7.10.

Базовый архив для продолжения:
`thaumcraft_legacy_rebuild_STAGE218_TC4_BOSS_MODEL_KEYROOM_PARITY_1192.zip`

Оригинал для сверки:
`Thaumcraft4-1.7.10-master.zip`

Что уже сделано к Stage218:
- Stage205–206: hard parity reset и repair против TC4 original.
- Stage207–209: Infusion Matrix two-phase wand activation, renderer/enchantment, instability/failure/FX parity.
- Stage210–213: runic augmentation/shield runtime, fortress mask/armor, champion runtime.
- Stage214: champion generation/showFX/localization.
- Stage215–217: Eldritch Warden/Golem boss entities, Eldritch/Golem orb projectiles, boss-room bridge, dedicated renderers, original textures.
- Stage218: boss model metadata bridge from `ModelEldritchGuardian` / `ModelEldritchGolem`, 1.19.2 `ServerBossEvent` boss bar adapter, original-style boss death loot adapter, and original `GenKeyRoom` behaviour: permanent `itemEldritchObject:2` item plus 2/3/4 guardians and champion-first rule on Hard.

Обязательные правила продолжения:
1. Цель остаётся Minecraft/Forge 1.19.2. Не вставлять 1.7.10-only API (`func_*`, `DataWatcher`, `NBTTag*`, `ForgeDirection`, прямой LWJGL/GL11`).
2. Любую механику сначала сверять с `Thaumcraft4-1.7.10-master.zip` и переносить через 1.19.2-safe adapters.
3. Каждый следующий архив должен содержать новый `docs/NEXT_CHAT_PROMPT_STAGE###.md`, чтобы перенос можно было продолжить в другом чате.
4. Перед упаковкой запускать минимум:
   - `python scripts/java_syntax_guard.py`
   - `python scripts/github_static_audit.py`
   - `python scripts/github_ci_guard.py`
   - последний stage audit
5. Gradle build может не пройти в sandbox без интернета из-за скачивания Gradle wrapper; это не считать логической ошибкой патча, но честно указывать в отчёте.

Следующий рекомендуемый Stage219:
- продолжить прямой перенос `ModelEldritchGuardian` и `ModelEldritchGolem` из bridge-метаданных в настоящие 1.19.2 `ModelPart`/`LayerDefinition` classes;
- добавить renderer для обычного `EldritchGuardianEntity` вместо block-placeholder;
- перенести client particle cadence `wispFXEG`, `smokeSpiral`, `arcLightning`, `drawVentParticles` более точно;
- начать перенос `GenCommon`, `Gen2x2`, `GenPassage`, `GenPortal` для полноценной Outer Lands structure integration;
- проверить boss/key-room spawn against original `Cell` feature and connection flags.
