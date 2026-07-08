Продолжи перенос Thaumcraft Legacy Rebuild на Minecraft/Forge 1.19.2 с архива `thaumcraft_legacy_rebuild_STAGE303_322_TC4_SUPER_MEGA_TAINTACLE_DIMENSION_BATCH_1192_PARITY.zip`.

Обязательные правила:
1. Сверяй перенос с оригинальным `Thaumcraft4-1.7.10-master.zip` / TC4 4.2.3.5.
2. Не добавляй новые фантазийные механики — только 1.19.2-safe адаптеры оригинальной логики.
3. Делай следующий super/mega batch в одном ZIP и положи новый `docs/NEXT_CHAT_PROMPT_STAGE###.md`.
4. Перед упаковкой запускай `scripts/java_syntax_guard.py`, `scripts/github_static_audit.py`, `scripts/github_ci_guard.py` и актуальные TC4 stage audits.
5. Не возвращай мусорные debug/addon placeholders в creative tab или loot pools.

Текущее состояние Stage303-322:
- Версия `3.22.0`.
- Добавлены dedicated `TaintacleEntity` и `TaintacleSmallEntity` по TC4 `EntityTaintacle` / `EntityTaintacleSmall`.
- Giant Taintacle теперь спавнит настоящие small taintacles при дальнем ответе.
- Cultist Portal renderer получил TC4 frame/pulse/alpha bridge.
- Добавлены `TC4OuterLandsDimensionParity`, `TC4OuterLandsChunkProviderBridge`, `TC4EldritchTileRenderProfile`.

Следующий рекомендуемый super-mega Stage323-342:
- Перенести full `ModelTaintacle` в baked `ModelPart` вместо billboard segments.
- Реализовать отдельные renderers для Eldritch Nothing / Obelisk / Lock / Crystal с более точными original FX.
- Подключить `TC4OuterLandsChunkProviderBridge.populateLikeTC4` к live chunk/generation event осторожно и gated-by-dimension.
- Углубить `GenLibraryRoom`, `GenNestRoom`, `GenBossRoom`, `GenKeyRoom` по оригинальным loop bodies.
- Провести full registry/data cleanup повторно и убедиться, что no quarantined garbage recipes вернулись.
