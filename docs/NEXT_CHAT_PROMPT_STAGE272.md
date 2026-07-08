Продолжи портирование Thaumcraft 4 на Minecraft/Forge 1.19.2 строго по оригиналу TC4 1.7.10.

База для следующего чата:
`thaumcraft_legacy_rebuild_STAGE263_272_TC4_PASSAGE_LIBRARY_NEST_CLEANUP_BATCH_1192_PARITY.zip`

Что уже сделано в Stage263-272:
- версия поднята до `2.72.0`;
- добавлен `TC4OuterLandsPassageFeatureAdapter`;
- перенесены хвостовые feature-ветки `GenPassage` для `11/12/13/14`:
  - trapped passage;
  - flesh/organic pocket;
  - taint fibres pocket;
  - vishroom/MindSpider spawner equivalent;
- `generatePassage` теперь вызывает feature-tail adapter после structural pass;
- `GenLibraryRoom` получил TC4-style pedestal/slab anchors;
- `GenNestRoom` получил random crystal/protrusion parity ближе к оригиналу;
- физически удалены quarantined garbage recipe JSON files, но registry ids оставлены ради save/data compatibility;
- manifest удаления лежит в `docs/TC4_STAGE263_272_REMOVED_GARBAGE_RECIPES.json`;
- новый audit: `scripts/tc4_stage263_272_mega_passage_cleanup_audit.py`.

Перед началом следующего batch обязательно запусти:
```bash
python3 scripts/java_syntax_guard.py
python3 scripts/github_static_audit.py
python3 scripts/github_ci_guard.py
python3 scripts/tc4_stage253_262_full_stage_cleanup_audit.py
python3 scripts/tc4_stage263_272_mega_passage_cleanup_audit.py
```

Следующий mega-stage Stage273-282:
- dedicated `EntityMindSpider` вместо CaveSpider breadcrumb;
- точнее перенести `TileEldritchCap`, `TileEldritchLock`, `TileEldritchCrystal`, `TileEldritchTrap` runtime/renderer behavior;
- добрать split decorative `blockEldritch` variants для meta `4/5/7/10` вместо частичной flattening;
- продолжить baked model tree Guardian/Warden/Golem/Crab;
- расширить Outer Lands dimension/chunk integration и regression-cleanup audit.

Не отходи от оригинала TC4 1.7.10. Если нужен компромисс из-за 1.19.2 API, добавляй adapter с явным `TC4Original` breadcrumb и описывай drift в report.
