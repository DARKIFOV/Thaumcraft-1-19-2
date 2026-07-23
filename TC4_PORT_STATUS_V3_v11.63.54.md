# Thaumcraft Legacy Rebuild 11.63.49 — объективный статус порта

**Дата:** 2026-07-19  
**Целевая платформа:** Minecraft 1.19.2, Forge 43.5.2, Java 17  
**Эталон:** Thaumcraft 4.2.3.5  
**Общий статус:** **FAIL**  
**Статус сборки:** **FAIL — успешный `gradlew build` не получен**  
**SHA-256 JAR:** `N/A — JAR не собран`

> Source/resource contracts, runtime и визуальная parity — разные уровни доказательств. Runtime/visual/network PASS допустим только при существующем артефакте с проверенным SHA-256, перечисленном в `runtime_artifacts/runtime_test_manifest.json`. Шаблон протокола проверяется `tools/validate_runtime_manifest.py`.

### Текущий этап 11.63.49

- Registry fallback остаётся закрыт: 0 предметных и 0 блочных generic fallback-ID.
- Оригинальный recipe-ledger закрыт полностью: 258/258 записей — 54 crucible, 104 arcane shaped, 5 arcane shapeless, 63 infusion, 24 infusion-enchantment и 8 smelting.
- Удалены 63 доказанных точных registry-дубля предметов и их дублирующие модели/локализации; datapack-ссылки переведены на канонические ID.
- Для старых сохранений добавлен Forge `MissingMappingsEvent`: удалённые legacy-ID перенаправляются на существующие канонические предметы с сохранением ItemStack NBT/capabilities при глубокой миграции.
- v11.63.51 имеет подтверждённый Java 17 build и 18/18 server GameTests PASS. Текущая v11.63.54 сохраняет registry purge и P0 runtime-контракты, добавляет четыре P1 block-entity контракта, поэтому старый PASS не переносится автоматически.
- Текущий обязательный пакет содержит 28 server-only GameTests и 356 runtime-сценариев; все десять новых после v11.63.51 тестов и повторный полный прогон v11.63.54 остаются `NOT_TESTED` до успешной Java 17 сборки.

---

## 1. Подтверждённые данные

| Показатель | Значение | Ограничение |
|---|---:|---|
| Успешная компиляция `gradlew build` | ❌ | Для этой версии подтверждённого build нет |
| SHA-256 JAR | `N/A — JAR не собран` | Вычисляется только после успешного Gradle-шага |
| Java-файлы | 776 | Статический count |
| Исследования | 201 / 201 | Runtime просмотрено 0 / 201 без manifest |
| Страницы исследований | 591 | Runtime просмотрено 0 без manifest |
| Аспекты | 48 / 48 | Runtime проверено 0 / 48 без manifest |
| JSON обычных рецептов | 136 | Runtime проверено 0 / 86 эталонных нединамических крафтов |
| Динамические этикетки | serializer присутствует | 48 аспектов + очистка требуют runtime |
| Arcane recipe JSON | 114 | Эталонный denominator должен быть зафиксирован отдельным source manifest |
| Alchemy recipe JSON | 70 | Эталонный denominator должен быть зафиксирован отдельным source manifest |
| Infusion recipe JSON | 78 | Materialized/JEI запись не является runtime PASS |
| Mod entity types | 50 / 50 | Полнота TC4 не подтверждена |
| BlockEntity types | 53 / 73 | Полнота TC4 не подтверждена |
| Entity renderer calls | 84 | Не эквивалентно числу оригинальных entity |
| BlockEntity renderer calls | 34 / 50 TESR reference | Runtime проверено 0 без manifest |
| Item model JSON | 663 | Парсинг модели не доказывает внешний вид |
| Runtime test cases | 0 / 356 | Учитываются только статусы с существующим SHA-256-проверенным артефактом |
| P0 source contracts | 0 complete + 0 partial / 7 | Runtime visual PASS: 0 / 7 |

### 1.1. Разделение области оценки

| Область | ID | Правило |
|---|---:|---|
| Core TC4 candidate objects | 255 | Только core-аудит; количество не означает готовность |
| Legacy aliases / migration IDs | 283 | Только миграция и отсутствие UI-дублей |
| Add-ons / compatibility branches | 170 | Исключены из оценки core TC4 |
| Всего уникальных предметов/блоков | 708 | Инвентаризация, не шкала готовности |

---

## 2. Матрица подсистем S/G/V/N/W/C

`S=PARTIAL` подтверждает только source-аудит. Для G/V/N/W/C статусы `PASS`, `PARTIAL` и `FAIL` принимаются только при существующем SHA-256-проверенном runtime-артефакте; иначе ставится `NOT TESTED`.

| Подсистема | S | G | V | N | W | C | Доказательство S / требуемый runtime |
|---|---|---|---|---|---|---|---|
| Аспекты и tags | PARTIAL | NOT TESTED | NOT TESTED | NOT TESTED | N/A | NOT TESTED | `reports/TC4_11.63.49_FULL_STATIC_CI.log`, `reports/visual_parity_audit_v11.63.49.json`; 48 source entries; scan/tag runtime required |
| Таумометр | PARTIAL | NOT TESTED | NOT TESTED | NOT TESTED | N/A | NOT TESTED | `reports/TC4_11.63.49_FULL_STATIC_CI.log`, `reports/visual_parity_audit_v11.63.49.json`; scan/overlay source guards; range/sound/repeat runtime required |
| Таумономикон | PARTIAL | NOT TESTED | NOT TESTED | NOT TESTED | N/A | NOT TESTED | `reports/TC4_11.63.49_FULL_STATIC_CI.log`, `reports/visual_parity_audit_v11.63.49.json`; 201 entries/591 pages plus v11.62.82 targeted-note inventory flow source audit; browser popup, GUI scales and note-creation runtime required |
| Research Table | PARTIAL | NOT TESTED | NOT TESTED | NOT TESTED | N/A | NOT TESTED | `reports/TC4_11.63.49_FULL_STATIC_CI.log`, `reports/visual_parity_audit_v11.63.49.json`; v11.62.82 source audit: Thaumonomicon-only note creation, table-only unfinished-note editing, completed item learning, fixed inventory and bookshelf/Brain Jar bonus sources; Expertise/Mastery/duplication/save runtime required |
| Arcane Workbench | PARTIAL | NOT TESTED | NOT TESTED | NOT TESTED | N/A | NOT TESTED | `reports/TC4_11.63.49_FULL_STATIC_CI.log`, `reports/visual_parity_audit_v11.63.49.json`; recipe/cost source guards; vis/research/GUI runtime required |
| Жезлы/фокусы | PARTIAL | NOT TESTED | NOT TESTED | NOT TESTED | N/A | NOT TESTED | `reports/TC4_11.63.49_FULL_STATIC_CI.log`, `reports/visual_parity_audit_v11.63.49.json`; renderer and cost source guards; all contexts/network runtime required |
| Узлы/Node Jar | PARTIAL | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | `reports/TC4_11.63.49_FULL_STATIC_CI.log`, `reports/visual_parity_audit_v11.63.49.json`; v11.63.41 source audit: exact 3x4x3 capture ritual, atomic six-primal cost, exact profile/NodeId transport, filled block/item and unjarred wand release; six types/modifiers, save/reload and multiplayer runtime required |
| Essentia jars | PARTIAL | NOT TESTED | NOT TESTED | NOT TESTED | N/A | NOT TESTED | `reports/TC4_11.63.49_FULL_STATIC_CI.log`, `reports/visual_parity_audit_v11.63.49.json`; NBT-aware BEWLR source contract; 48 labels runtime required |
| Essentia transport | PARTIAL | NOT TESTED | NOT TESTED | NOT TESTED | N/A | NOT TESTED | `reports/TC4_11.63.49_FULL_STATIC_CI.log`, `reports/visual_parity_audit_v11.63.49.json`; v11.63.50 source ledger is closed at 258/258 exact original recipe records; v11.63.52 keeps canonical-only item IDs, while full matrix/transport conflict/rollback/soak runtime is still required |
| Furnace/alembics/centrifuge | PARTIAL | NOT TESTED | NOT TESTED | NOT TESTED | N/A | NOT TESTED | `reports/TC4_11.63.49_FULL_STATIC_CI.log`, `reports/visual_parity_audit_v11.63.49.json`; v11.62.79 source audit: centrifuge queues input while output is occupied or redstone-paused; complete chain runtime required |
| Alchemy/Liquid Death | PARTIAL | NOT TESTED | NOT TESTED | NOT TESTED | N/A | NOT TESTED | `reports/TC4_11.63.49_FULL_STATIC_CI.log`, `reports/visual_parity_audit_v11.63.49.json`; v11.63.10 source audit: registered Liquid Death source/flowing pair and bucket, dissolve damage ladder, named TC4 damage sources and aspect-crystal drops; finite-fluid conservation and runtime/network behavior require verification |
| Infusion Matrix | PARTIAL | NOT TESTED | NOT TESTED | NOT TESTED | N/A | NOT TESTED | `reports/TC4_11.63.49_FULL_STATIC_CI.log`, `reports/visual_parity_audit_v11.63.49.json`; recipe and renderer guards; stability/events/save runtime required |
| JEI | PARTIAL | NOT TESTED | NOT TESTED | N/A | N/A | NOT TESTED | `reports/TC4_11.63.49_FULL_STATIC_CI.log`, `reports/visual_parity_audit_v11.63.49.json`; source plugin/recipe registration; JEI and no-JEI runtime required |
| Bone Bow | PARTIAL | NOT TESTED | NOT TESTED | NOT TESTED | N/A | NOT TESTED | `reports/TC4_11.63.49_FULL_STATIC_CI.log`, `reports/visual_parity_audit_v11.63.49.json`; charge/item-model source contract; arrows/enchants/visual runtime required |
| Traveling Trunk | PARTIAL | NOT TESTED | NOT TESTED | NOT TESTED | N/A | NOT TESTED | `reports/TC4_11.63.49_FULL_STATIC_CI.log`, `reports/visual_parity_audit_v11.63.49.json`; entity/inventory/model/capability source contract; runtime required |
| Special item entities | PARTIAL | NOT TESTED | NOT TESTED | NOT TESTED | N/A | NOT TESTED | `reports/TC4_11.63.49_FULL_STATIC_CI.log`, `reports/visual_parity_audit_v11.63.49.json`; v11.63.10 source audit: 50/50 entity registrations, protected/permanent/following item behavior and four production paths; exact additive renderer and multiplayer runtime required |
| Crimson cultists | PARTIAL | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | `reports/TC4_11.63.49_FULL_STATIC_CI.log`, `reports/visual_parity_audit_v11.63.49.json`; humanoid renderer source contract; four-role side-by-side runtime required |
| Fortress Armor | PARTIAL | NOT TESTED | NOT TESTED | NOT TESTED | N/A | NOT TESTED | `reports/TC4_11.63.49_FULL_STATIC_CI.log`, `reports/visual_parity_audit_v11.63.49.json`; dedicated model source contract; slim/default/masks runtime required |
| Големы | PARTIAL | NOT TESTED | NOT TESTED | NOT TESTED | N/A | NOT TESTED | `reports/TC4_11.63.49_FULL_STATIC_CI.log`, `reports/visual_parity_audit_v11.63.49.json`; USE marker-side/empty-hand handling, weighted fishing quality and priority creeper avoidance are source-guarded; all materials/cores/upgrades/markers still require runtime |
| Warp/Eldritch | PARTIAL | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | `reports/TC4_11.63.49_FULL_STATIC_CI.log`, `reports/visual_parity_audit_v11.63.49.json`; v11.62.81 source audit: effect-only Warp Ward authority with legacy NBT migration, separate sticky-event decay, full bucket/counter sync, TC4 spawn search and 0.75 Death Gaze cone; runtime/network/visual proof required |
| Taint/Eerie/Forest | PARTIAL | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | `reports/TC4_11.63.49_FULL_STATIC_CI.log`, `reports/visual_parity_audit_v11.63.49.json`; v11.63.42 source audit: TC4 Mana Pods now restore ten Magical Forest attempts, age 0-7 growth, aspect breeding/drops and dynamic rendering; Falling Taint ecology remains source-complete, while chunk density, random ticks, save/reload and multiplayer still require runtime verification |
| Outer Lands | PARTIAL | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | `reports/TC4_11.63.49_FULL_STATIC_CI.log`, `reports/visual_parity_audit_v11.63.49.json`; v11.63.10 one-room synchronous entry generation, bounded landing chunk load and progressive one-room-per-pass maze population; traversal/save/return runtime required |
| Mirrors | PARTIAL | NOT TESTED | NOT TESTED | NOT TESTED | N/A | NOT TESTED | `reports/TC4_11.63.49_FULL_STATIC_CI.log`, `reports/visual_parity_audit_v11.63.49.json`; source mirror contracts; cross-dimension/save/automation runtime required |
| Brain in a Jar | PARTIAL | NOT TESTED | NOT TESTED | NOT TESTED | N/A | NOT TESTED | `reports/TC4_11.63.49_FULL_STATIC_CI.log`, `reports/visual_parity_audit_v11.63.49.json`; source XP/renderer contract; comparator/NBT/visual runtime required |
| Миграция миров | PARTIAL | NOT TESTED | N/A | NOT TESTED | NOT TESTED | N/A | `reports/TC4_11.63.49_FULL_STATIC_CI.log`, `reports/visual_parity_audit_v11.63.49.json`; v11.63.43 source audit: serialized-stack ID rewrite, player/Ender/equipment/entity/container/block-entity/nested-handler coverage, deferred two-chunk budget and schema SavedData ledger; old-world fixtures, restart idempotence and five-version runtime still required |
| Dedicated server | PARTIAL | NOT TESTED | N/A | NOT TESTED | NOT TESTED | N/A | `reports/TC4_11.63.49_FULL_STATIC_CI.log`, `reports/visual_parity_audit_v11.63.49.json`; server-safe source guards; two-client runtime required |

---

## 3. P0/P1

| Приоритет | Объект | Проблема | Source status | Runtime status |
|---|---|---|---|---|
| P0 | Essentia jars item contexts | Статическая block model не показывала NBT-content/filter/label | SOURCE CONTRACT COMPLETE | NOT TESTED |
| P0 | Aura node item | Обычный debug BlockItem вместо original player path | SOURCE CONTRACT COMPLETE | NOT TESTED |
| P0 | Bone Bow | Плоский idle sprite без pull/gameplay parity | SOURCE CONTRACT COMPLETE | NOT TESTED |
| P0 | Traveling Trunk | Плоский recipe item без сущности | SOURCE CONTRACT COMPLETE | NOT TESTED |
| P0 | Crimson cultists | Block placeholders вместо humanoid renderer | SOURCE CONTRACT COMPLETE | NOT TESTED |
| P0 | Fortress Armor | Vanilla HumanoidModel вместо dedicated geometry | SOURCE CONTRACT COMPLETE | NOT TESTED |
| P0 | Outer Lands | Отдельные файлы ошибочно считались готовой системой | STATIC PARTIAL | NOT TESTED |
| P1 | Свечи | Геометрия/частицы/стабилизация | SOURCE CONTRACT COMPLETE | NOT TESTED |
| P1 | Hungry Chest | Инвентарь/pickup/крышка/рендер | SOURCE CONTRACT COMPLETE | NOT TESTED |
| P1 | Thaumonomicon GUI | Координаты без полной клиентской проверки | PARTIAL | NOT TESTED |
| P1 | Infusion/JEI | Статический набор без клиентской полноты | PARTIAL | NOT TESTED |

Source status не закрывает P0. Требуются build PASS и runtime PASS всех обязательных состояний.

---

## 4. Runtime-протокол

### 4.1. Выполненные тесты

| Тест | Результат | Артефакт |
|---|---|---|
| `gradlew build` | FAIL / NOT OBTAINED | `reports/TC4_11.63.49_LOCAL_GRADLE_BUILD_ATTEMPT.log` |
| Клиент | NOT TESTED | runtime manifest |
| Dedicated server | NOT TESTED | runtime manifest |
| P0 visuals | 0 / 7 PASS | runtime manifest + screenshots |
| Миграция | NOT TESTED | runtime manifest |
| JEI | NOT TESTED | runtime manifest |

### 4.2. Блокирующие тесты

- [ ] Повторный build v11.63.54 на Java 17, SHA-256 JAR и 28/28 GameTests.
- [ ] Чистый клиент и dedicated server.
- [ ] 201 исследований/591 страниц на нескольких GUI Scale.
- [ ] 86 normal, 49 label/cleaning и все arcane/alchemy/infusion рецепты.
- [ ] P0 в GUI/ground/fixed/first-person/third-person/world.
- [ ] 48 аспектов в filled/filtered/labeled jars.
- [ ] Essentia reservoir 24-suction/six-face active pull, buffer conflict/rollback/soak and centrifuge redstone/output queue.
- [ ] Mirrors и Outer Lands с save/reload/return.
- [ ] Migration 11.62.58/60/62/73/74/75/76/77.
- [ ] Golem USE-core side/empty-hand, weighted fishing and creeper-swell avoidance scenarios.
- [ ] Запуск без JEI.

---

## 5. Критерии готовности к релизу

- [ ] Успешная сборка — **FAIL**.
- [ ] Все P0 runtime-подтверждены — **FAIL**.
- [ ] Runtime-протокол завершён — **FAIL**.
- [ ] Миграция подтверждена — **FAIL**.
- [ ] JEI/no-JEI подтверждены — **FAIL**.

**Итог: 0 из 5 критериев выполнены.**  
**Вердикт: НЕ ГОТОВ К РЕЛИЗУ.**

---

## 6. Следующие шаги

1. Получить CI build и compiler output. До появления JAR поле build остаётся FAIL независимо от статических guards.
2. Исправить все compiler/runtime ошибки.
3. Заполнить `screenshots/` и `runtime_artifacts/runtime_test_manifest.json` реальными файлами.
4. Не закрывать Outer Lands до полного игрового цикла.
5. Зафиксировать exact source denominators рецептов.
6. Выполнить migration matrix.
7. Повторно сгенерировать этот отчёт; субъективные проценты запрещены.
