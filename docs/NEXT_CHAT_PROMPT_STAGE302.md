Продолжи портирование Thaumcraft 4 на Minecraft/Forge 1.19.2 строго от архива:

`thaumcraft_legacy_rebuild_STAGE283_302_TC4_SUPER_MEGA_ELDRITCH_BOSS_TILE_RENDER_BATCH_1192_PARITY.zip`

Сверочный оригинал:

`Thaumcraft4-1.7.10-master.zip`

Текущая точка:

- Проект поднят до версии `3.02.0`.
- Последний batch — super-mega Stage283–302.
- Уже есть `EntityCultistPortal` adapter с `stage/stagecounter`, staged cultist spawning, loot pulses, contact zap, boss bar.
- Уже есть `TaintacleGiantEntity` adapter с `Anger`, 35 damage cap, 200 tick enrage, champion-on-spawn, passive regen, stationary tentacle target logic.
- `TC4EldritchLockBossSpawner` теперь spawn-ит реальные `CultistPortalEntity` и `TaintacleGiantEntity`, а не placeholder Praetor/Warden equivalents.
- Добавлены renderers: `TC4CultistPortalRenderer`, `TC4TaintacleGiantRenderer`, `TC4MindSpiderRenderer`, `TC4EldritchTileRenderer`.
- Добавлен `TC4OuterLandsLootAdapter.dropBossDeathLoot(LivingEntity, ...)` для non-`TC4ThaumcraftBossEntity` boss drops.
- В архиве есть audit: `scripts/tc4_stage283_302_super_mega_eldritch_boss_audit.py`.

Следующий mega-stage Stage303-312 лучше делать так:

1. Перенести `EntityTaintacle` и `EntityTaintacleSmall` как отдельные 1.19.2 entities, чтобы Giant больше не держал всю tentacle-логику внутри себя.
2. Сделать `TC4TaintacleModel` / baked `LayerDefinition` по оригинальному `ModelTaintacle` и заменить billboard renderer.
3. Углубить `RenderCultistPortal`: точнее оригинальный pulse, alpha, quad rotation, death explosion FX.
4. Перенести `TileEldritchNothingRenderer` / `TileEldritchObeliskRenderer` equivalents; сейчас Stage283-302 покрыл только BE render bridge для cap/lock/trap/crystal.
5. Начать настоящий Outer Lands dimension/chunk-provider bridge: `BiomeGenEldritch`, chunk coordinate feature placement, provider-safe generated-cells persistence.
6. Продолжить cleanup: проверять, что quarantined addon/debug предметы не попадают в creative, loot, recipes, research.
7. Обязательно положить в архив новый `docs/NEXT_CHAT_PROMPT_STAGE312.md`.

Перед сборкой прогонять:

- `python3 scripts/java_syntax_guard.py`
- `python3 scripts/github_static_audit.py`
- `python3 scripts/github_ci_guard.py`
- последние mega-audit scripts, включая Stage283-302

Gradle build в текущем sandbox может не пройти из-за отсутствия интернета и скачивания `gradle-7.5.1-bin.zip`; это не считать успешной компиляцией.
