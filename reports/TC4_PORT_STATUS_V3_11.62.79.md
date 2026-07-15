# Thaumcraft Legacy Rebuild 11.62.79 — объективный статус порта

**Дата:** 2026-07-15  
**Целевая платформа:** Minecraft 1.19.2, Forge 43.5.2, Java 17  
**Эталон:** Thaumcraft 4.2.3.5  
**Общий статус:** **FAIL**  
**Статус сборки:** **FAIL — успешный `gradlew build` не получен**  
**SHA-256 JAR:** `N/A — JAR не собран`

> Source/resource contracts, runtime и визуальная parity — разные уровни доказательств. Runtime/visual/network PASS допустим только при существующем артефакте с проверенным SHA-256, перечисленном в `runtime_artifacts/runtime_test_manifest.json`. Шаблон протокола проверяется `tools/validate_runtime_manifest.py`.

---

## 1. Подтверждённые данные

| Показатель | Значение | Ограничение |
|---|---:|---|
| Успешная компиляция `gradlew build` | ❌ | Для этой версии подтверждённого build нет |
| SHA-256 JAR | `N/A — JAR не собран` | Вычисляется только после успешного Gradle-шага |
| Java-файлы | 589 | Статический count |
| Исследования | 201 / 201 | Runtime просмотрено 0 / 201 без manifest |
| Страницы исследований | 591 | Runtime просмотрено 0 без manifest |
| Аспекты | 48 / 48 | Runtime проверено 0 / 48 без manifest |
| JSON обычных рецептов | 136 | Runtime проверено 0 / 86 эталонных нединамических крафтов |
| Динамические этикетки | serializer присутствует | 48 аспектов + очистка требуют runtime |
| Arcane recipe JSON | 114 | Эталонный denominator должен быть зафиксирован отдельным source manifest |
| Alchemy recipe JSON | 70 | Эталонный denominator должен быть зафиксирован отдельным source manifest |
| Infusion recipe JSON | 78 | Materialized/JEI запись не является runtime PASS |
| Mod entity types | 27 / 50 | Полнота TC4 не подтверждена |
| BlockEntity types | 37 / 73 | Полнота TC4 не подтверждена |
| Entity renderer calls | 51 | Не эквивалентно числу оригинальных entity |
| BlockEntity renderer calls | 24 / 50 TESR reference | Runtime проверено 0 без manifest |
| Item model JSON | 721 | Парсинг модели не доказывает внешний вид |
| Runtime test cases | 0 / 53 | Учитываются только статусы с существующим SHA-256-проверенным артефактом |
| P0 source contracts | 6 complete + 1 partial / 7 | Runtime visual PASS: 0 / 7 |

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
| Аспекты и tags | PARTIAL | NOT TESTED | NOT TESTED | NOT TESTED | N/A | NOT TESTED | `reports/TC4_11.62.79_FULL_STATIC_CI.log`, `reports/visual_parity_audit_v11.62.79.json`; 48 source entries; scan/tag runtime required |
| Таумометр | PARTIAL | NOT TESTED | NOT TESTED | NOT TESTED | N/A | NOT TESTED | `reports/TC4_11.62.79_FULL_STATIC_CI.log`, `reports/visual_parity_audit_v11.62.79.json`; scan/overlay source guards; range/sound/repeat runtime required |
| Таумономикон | PARTIAL | NOT TESTED | NOT TESTED | NOT TESTED | N/A | NOT TESTED | `reports/TC4_11.62.79_FULL_STATIC_CI.log`, `reports/visual_parity_audit_v11.62.79.json`; 201 entries and 591 pages source audit; GUI runtime required |
| Research Table | PARTIAL | NOT TESTED | NOT TESTED | NOT TESTED | N/A | NOT TESTED | `reports/TC4_11.62.79_FULL_STATIC_CI.log`, `reports/visual_parity_audit_v11.62.79.json`; source transaction guards; Expertise/Mastery/save runtime required |
| Arcane Workbench | PARTIAL | NOT TESTED | NOT TESTED | NOT TESTED | N/A | NOT TESTED | `reports/TC4_11.62.79_FULL_STATIC_CI.log`, `reports/visual_parity_audit_v11.62.79.json`; recipe/cost source guards; vis/research/GUI runtime required |
| Жезлы/фокусы | PARTIAL | NOT TESTED | NOT TESTED | NOT TESTED | N/A | NOT TESTED | `reports/TC4_11.62.79_FULL_STATIC_CI.log`, `reports/visual_parity_audit_v11.62.79.json`; renderer and cost source guards; all contexts/network runtime required |
| Узлы/Node Jar | PARTIAL | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | `reports/TC4_11.62.79_FULL_STATIC_CI.log`, `reports/visual_parity_audit_v11.62.79.json`; node source audit; six types/three modifiers runtime required |
| Essentia jars | PARTIAL | NOT TESTED | NOT TESTED | NOT TESTED | N/A | NOT TESTED | `reports/TC4_11.62.79_FULL_STATIC_CI.log`, `reports/visual_parity_audit_v11.62.79.json`; NBT-aware BEWLR source contract; 48 labels runtime required |
| Essentia transport | PARTIAL | NOT TESTED | NOT TESTED | NOT TESTED | N/A | NOT TESTED | `reports/TC4_11.62.79_FULL_STATIC_CI.log`, `reports/visual_parity_audit_v11.62.79.json`; v11.62.79 source audit: reservoir suction 24/six faces/active pull and buffer real-suction arbitration; conflict/rollback/soak runtime required |
| Furnace/alembics/centrifuge | PARTIAL | NOT TESTED | NOT TESTED | NOT TESTED | N/A | NOT TESTED | `reports/TC4_11.62.79_FULL_STATIC_CI.log`, `reports/visual_parity_audit_v11.62.79.json`; v11.62.79 source audit: centrifuge queues input while output is occupied or redstone-paused; complete chain runtime required |
| Infusion Matrix | PARTIAL | NOT TESTED | NOT TESTED | NOT TESTED | N/A | NOT TESTED | `reports/TC4_11.62.79_FULL_STATIC_CI.log`, `reports/visual_parity_audit_v11.62.79.json`; recipe and renderer guards; stability/events/save runtime required |
| JEI | PARTIAL | NOT TESTED | NOT TESTED | N/A | N/A | NOT TESTED | `reports/TC4_11.62.79_FULL_STATIC_CI.log`, `reports/visual_parity_audit_v11.62.79.json`; source plugin/recipe registration; JEI and no-JEI runtime required |
| Bone Bow | PARTIAL | NOT TESTED | NOT TESTED | NOT TESTED | N/A | NOT TESTED | `reports/TC4_11.62.79_FULL_STATIC_CI.log`, `reports/visual_parity_audit_v11.62.79.json`; charge/item-model source contract; arrows/enchants/visual runtime required |
| Traveling Trunk | PARTIAL | NOT TESTED | NOT TESTED | NOT TESTED | N/A | NOT TESTED | `reports/TC4_11.62.79_FULL_STATIC_CI.log`, `reports/visual_parity_audit_v11.62.79.json`; entity/inventory/model/capability source contract; runtime required |
| Crimson cultists | PARTIAL | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | `reports/TC4_11.62.79_FULL_STATIC_CI.log`, `reports/visual_parity_audit_v11.62.79.json`; humanoid renderer source contract; four-role side-by-side runtime required |
| Fortress Armor | PARTIAL | NOT TESTED | NOT TESTED | NOT TESTED | N/A | NOT TESTED | `reports/TC4_11.62.79_FULL_STATIC_CI.log`, `reports/visual_parity_audit_v11.62.79.json`; dedicated model source contract; slim/default/masks runtime required |
| Големы | PARTIAL | NOT TESTED | NOT TESTED | NOT TESTED | N/A | NOT TESTED | `reports/TC4_11.62.79_FULL_STATIC_CI.log`, `reports/visual_parity_audit_v11.62.79.json`; USE marker-side/empty-hand handling, weighted fishing quality and priority creeper avoidance are source-guarded; all materials/cores/upgrades/markers still require runtime |
| Warp/Eldritch | PARTIAL | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | `reports/TC4_11.62.79_FULL_STATIC_CI.log`, `reports/visual_parity_audit_v11.62.79.json`; source effects/progression fragments; warp/network runtime required |
| Taint/Eerie/Forest | PARTIAL | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | `reports/TC4_11.62.79_FULL_STATIC_CI.log`, `reports/visual_parity_audit_v11.62.79.json`; source worldgen/features; spread/cleanse/structures runtime required |
| Outer Lands | PARTIAL | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | `reports/TC4_11.62.79_FULL_STATIC_CI.log`, `reports/visual_parity_audit_v11.62.79.json`; aligned portal maze, TC4 portal-room geometry and lock-gated boss cycle in source; traversal/save/return runtime required |
| Mirrors | PARTIAL | NOT TESTED | NOT TESTED | NOT TESTED | N/A | NOT TESTED | `reports/TC4_11.62.79_FULL_STATIC_CI.log`, `reports/visual_parity_audit_v11.62.79.json`; source mirror contracts; cross-dimension/save/automation runtime required |
| Brain in a Jar | PARTIAL | NOT TESTED | NOT TESTED | NOT TESTED | N/A | NOT TESTED | `reports/TC4_11.62.79_FULL_STATIC_CI.log`, `reports/visual_parity_audit_v11.62.79.json`; source XP/renderer contract; comparator/NBT/visual runtime required |
| Миграция миров | PARTIAL | NOT TESTED | N/A | NOT TESTED | NOT TESTED | N/A | `reports/TC4_11.62.79_FULL_STATIC_CI.log`, `reports/visual_parity_audit_v11.62.79.json`; legacy aliases/source mapping; five-version migration runtime required |
| Dedicated server | PARTIAL | NOT TESTED | N/A | NOT TESTED | NOT TESTED | N/A | `reports/TC4_11.62.79_FULL_STATIC_CI.log`, `reports/visual_parity_audit_v11.62.79.json`; server-safe source guards; two-client runtime required |

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
| P1 | Свечи | Геометрия/частицы/стабилизация | PARTIAL | NOT TESTED |
| P1 | Hungry Chest | Модель/pickup behavior | PARTIAL | NOT TESTED |
| P1 | Thaumonomicon GUI | Координаты без полной клиентской проверки | PARTIAL | NOT TESTED |
| P1 | Infusion/JEI | Статический набор без клиентской полноты | PARTIAL | NOT TESTED |

Source status не закрывает P0. Требуются build PASS и runtime PASS всех обязательных состояний.

---

## 4. Runtime-протокол

### 4.1. Выполненные тесты

| Тест | Результат | Артефакт |
|---|---|---|
| `gradlew build` | FAIL / NOT OBTAINED | `reports/TC4_11.62.79_LOCAL_GRADLE_BUILD_ATTEMPT.log` |
| Клиент | NOT TESTED | runtime manifest |
| Dedicated server | NOT TESTED | runtime manifest |
| P0 visuals | 0 / 7 PASS | runtime manifest + screenshots |
| Миграция | NOT TESTED | runtime manifest |
| JEI | NOT TESTED | runtime manifest |

### 4.2. Блокирующие тесты

- [ ] Build/compileJava Forge 43.5.2 на Java 17 и SHA-256 JAR.
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
