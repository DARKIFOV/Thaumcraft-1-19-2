Продолжай перенос Thaumcraft 4 на Minecraft/Forge 1.19.2 от архива Stage215.  Сначала распакуй `thaumcraft_legacy_rebuild_STAGE215_TC4_ELDRITCH_BOSS_CHAMPION_1192_PARITY.zip`, затем сверяй с `Thaumcraft4-1.7.10-master.zip`.

Следующий приоритет Stage216:

1. Перенести более точный Eldritch Warden ranged attack/orb behavior (`EntityEldritchOrb`), включая arm-lift state и beam/arc FX packets.
2. Перенести Eldritch Golem headless renderer/model state глубже: headless visual branch, arcing lightning byte `19`, block-crack behavior, golem boss beam packet parity.
3. Добавить dedicated renderers/model adapters для Warden/Golem вместо временных block-mob renderers, используя оригинальные TC4 текстуры/геометрию там, где возможно.
4. Уточнить Outer Lands boss-room/key-room hooks: автоматический champion persistence, home radius, boss room spawn metadata.
5. Сохранить Forge 1.19.2 API guard: никаких `func_*`, `field_*`, `NBTTag*`, `DataWatcher`, `SharedMonsterAttributes` в новых runtime файлах.
6. Обновить version до `2.16.0`, добавить docs/report/audit script Stage216 и прогнать static/parity audits.
