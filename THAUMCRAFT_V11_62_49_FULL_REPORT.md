# Thaumcraft Legacy Rebuild v11.62.49 — единый отчёт портирования, исправлений и аудита

## 1. Паспорт ревизии

| Параметр | Значение |
|---|---|
| Целевая игра | Minecraft 1.19.2 |
| Загрузчик | Minecraft Forge 43.5.2 |
| Java | 17 |
| Эталон поведения | Thaumcraft 4.2.3.5 для Minecraft 1.7.10 |
| Ревизия порта | 11.62.49 |
| Базовая ревизия | 11.62.48 |
| Основная тема | Точная сверка `EntityAspectOrb` и `RenderAspectOrb` после внешнего аудита |
| Графический Forge-клиент в этой среде | Не запускался |
| Production JAR | Не собран: Gradle Wrapper не может загрузить Gradle 7.5.1 из-за отсутствия сетевого доступа |
| Формат отчётности | Один объединённый Markdown-файл |

Проект остаётся исключительно **Forge 1.19.2**. Fabric, NeoForge и TerraBlender не используются. Ревизия сохраняет все ранее внесённые исправления Таумономикона, Таумометра, жезлов, узлов ауры, стабилизаторов, банки узла, магического верстака, расчёта вис и динамических item-рендеров.

## 2. Краткий результат

Внешний аудит v11.62.48 правильно обнаружил критическую ошибку в масштабе сферы аспекта и указал на необходимость перепроверить свет, cooldown и blending. Каждый пункт был повторно сопоставлен с двумя декомпиляциями оригинального TC4 и с текущим Forge-кодом.

В v11.62.49 выполнено следующее:

- масштаб сферы больше не использует целочисленный quotient и плавно уменьшается в течение всей жизни;
- освещение перенесено через точное прибавление `120` к сырой block-light координате диапазона `0..240` с сохранением sky-light;
- поле сущности переименовано в оригинальное `orbCooldown`, добавлены явные getter/setter;
- подтверждено и сохранено оригинальное двойное условие подбора: локальный `orbCooldown` **и** задержка опыта игрока;
- исправлено ранее пропущенное расхождение: специальная физика с подъёмом и fizz относится к **лаве**, а не к воде;
- старый `pushOutOfBlocks` сопоставлен с `moveTowardsClosestSpace` Forge/Minecraft 1.19.2;
- `orbCooldown` больше не сериализуется в NBT, как и в оригинальном `EntityAspectOrb`;
- подтверждено, что Entropy/Perditio и Void/Vacuos используют destination blend `771`, а остальные аспекты — additive destination blend `1`;
- добавлен отдельный regression guard v11.62.49 и подключён к build/release workflows;
- повторно прогнаны все guards, пять машинных аудитов, JSON-проверка и целевая Java-компиляция изменённых классов.

## 3. Разбор замечаний внешнего аудита

### 3.1. Масштаб сферы — замечание подтверждено и исправлено

В v11.62.48 выражение было ошибочно записано так:

```java
0.1F + 0.3F * ((MAX_AGE - age) / MAX_AGE)
```

Внутренние скобки заставляли Java сначала выполнить целочисленное деление. Получались практически два состояния: `0.4` в начале и `0.1` почти всю оставшуюся жизнь.

Оригинальная последовательность операторов TC4:

```java
0.1F + 0.3F * (orbMaxAge - orbAge) / orbMaxAge
```

Умножение и деление имеют одинаковый приоритет и выполняются слева направо. После умножения на `0.3F` вычисление уже является floating-point. В Forge-порте это теперь выражено явно:

```java
float remaining = (float) (AspectOrbEntity.MAX_AGE - orb.orbAge());
float scale = 0.1F + 0.3F * remaining / (float) AspectOrbEntity.MAX_AGE;
```

Примеры:

| Возраст | Масштаб |
|---:|---:|
| 0 | 0.400 |
| 25 | 0.350 |
| 50 | 0.300 |
| 75 | 0.250 |
| 100 | 0.200 |
| 125 | 0.150 |
| 149 | 0.102 |

Результат: уменьшение плавное, а guard запрещает возврат ошибочных внутренних скобок.

### 3.2. Освещение — замечание подтверждено и исправлено точно

Оригинальный `EntityAspectOrb#getBrightnessForRender`:

1. получает packed light;
2. извлекает нижнюю block-light координату;
3. прибавляет `0.5 × 15 × 16 = 120`;
4. ограничивает результат значением `240`;
5. возвращает исходную sky-light координату без изменения.

В v11.62.48 это было аппроксимировано как `ambientBlock + 8` в современном диапазоне `0..15`. В v11.62.49 используется исходное пространство координат lightmap:

```java
int blockLightCoordinate = packedLight & 0xFFFF;
int skyLightCoordinate = packedLight & 0xFFFF0000;
int boostedLight = Math.min(240, blockLightCoordinate + 120)
        | skyLightCoordinate;
```

Это сохраняет оригинальное прибавление ровно `120`, включая половинный шаг относительно 15 дискретных уровней, и не превращает сферу в постоянный fullbright.

### 3.3. Задержка подбора — формулировка аудита уточнена

Предложение полностью убрать `player.takeXpDelay` не соответствует оригинальному коду TC4. Оригинальный `EntityAspectOrb#onCollideWithPlayer` проверяет одновременно:

```text
orbCooldown == 0
player.xpCooldown == 0
aspect is primal
wand slot exists
```

После успешного подбора оригинал выполняет:

```text
player.xpCooldown = 2
```

В Minecraft 1.19.2 соответствующее поле называется `Player#takeXpDelay`. Поэтому порт намеренно сохраняет оба механизма:

```java
if (level.isClientSide
        || orbCooldown > 0
        || player.takeXpDelay > 0
        || !getAspect().isPrimal()) {
    return;
}
...
player.takeXpDelay = 2;
```

Исправление v11.62.49 состоит не в удалении player-delay, а в восстановлении оригинального имени и жизненного цикла локального поля `orbCooldown`. Поле уменьшается каждый тик, имеет setter для будущих spawn-сценариев и не сохраняется в NBT.

### 3.4. Свободное место в жезле — реализация подтверждена

Оригинальная `InventoryUtils.isWandInHotbarWithRoom` вызывает пробное `addVis(..., false)` и принимает слот, если возвращённое непоместившееся количество меньше исходного количества. Следовательно, достаточно **любого** свободного места, а не ёмкости для всей сферы.

Forge-порт проверяет:

```java
current < wand.stackVisCapacity(stack)
```

При подборе фактический `WandItem.addVis` заполняет жезл до capacity. Если значение сферы больше оставшейся ёмкости, остаток теряется после удаления сферы, что соответствует TC4.

### 3.5. `nodeTick` и загрузка чанка — документировано

`AuraNodeBlockEntity#nodeTick` намеренно не сохраняется. После реконструкции block entity локальная фаза начинается с нуля. Это соответствует несериализуемому `TileNode#count` оригинала и устраняет прежнюю ошибку, когда периодические действия зависели от общего `level.getGameTime()`.

### 3.6. Blending — проверка показала, что текущая аспектная развилка правильна

Оригинальный renderer вызывает:

```text
blendFunc(GL_SRC_ALPHA, aspect.getBlend())
```

В TC4:

- большинство аспектов имеют `blend = 1` (`GL_ONE`) — additive;
- Entropy/Perditio имеет `blend = 771` (`GL_ONE_MINUS_SRC_ALPHA`);
- Void/Vacuos также имеет `blend = 771`.

Следовательно, утверждение «все сферы всегда additive» неверно. Текущий Forge-контракт сохранён:

```java
TC4NodeRenderTypes.node(
        PARTICLES,
        !aspect.usesAlphaBlend(),
        false
);
```

`Aspect#usesAlphaBlend()` возвращает `true` только для `PERDITIO` и `VACUOS`. Guard v11.62.49 теперь отдельно фиксирует это правило.

### 3.7. Защита `lock` — оставлена без изменения штатной вероятности

В обычном пути обработка выполняется только при `lock > 0`. Дополнительная защита знаменателя через `Math.max(1, ...)` является defensive-кодом для повреждённых данных или вмешательства сторонних модов. При штатных значениях lock формула и вероятность TC4 не изменяются.

## 4. Дополнительные ошибки, найденные при повторной сверке

### 4.1. Лава вместо воды

В v11.62.48 специальная физика сферы ошибочно была привязана к `isInWater()`. Оригинал проверяет материал `lava` в блоке, где находится сфера.

Теперь при нахождении в лаве каждый тик:

- вертикальная скорость становится `0.2`;
- X/Z получают два случайных float-компонента с диапазоном около `±0.2`;
- воспроизводится fizz со звуком, громкостью `0.4` и исходной формулой pitch.

Периодическое ограничение звука раз в 10 тиков удалено, потому что его нет в оригинале.

### 4.2. Выталкивание из блоков

Оригинал каждый update вызывает `pushOutOfBlocks` в средней точке bounding box. В 1.19.2 используется доступный эквивалент:

```java
AABB bounds = getBoundingBox();
moveTowardsClosestSpace(
        getX(),
        (bounds.minY + bounds.maxY) * 0.5D,
        getZ()
);
```

Это уменьшает риск застревания маленькой сущности внутри геометрии блока после spawn или движения.

### 4.3. NBT локального cooldown

Оригинальный NBT сохраняет только `Health`, `Age`, `Value` и `Aspect`. `orbCooldown` в NBT отсутствует. Запись и чтение `Cooldown`, добавленные в v11.62.48, удалены.

### 4.4. Удаление по возрасту на обеих сторонах

Сфера теперь вызывает `discard()` при достижении 150 тиков и на клиенте, и на сервере. Сервер остаётся авторитетным, но клиент не держит визуально просроченную сущность до следующего remove-пакета.

## 5. Сопоставление оригинала и Forge 1.19.2

| Оригинал TC4 | Forge 1.19.2 | Статус |
|---|---|---|
| `EntityAspectOrb#orbAge` / `orbMaxAge=150` | `orbAge` / `MAX_AGE=150` | Перенесено |
| `EntityAspectOrb#orbCooldown` | `orbCooldown` + getter/setter | Исправлено в v11.62.49 |
| `EntityPlayer#xpCooldown` | `Player#takeXpDelay` | Точный современный аналог сохранён |
| `Material.lava` | `Entity#isInLava()` | Исправлено в v11.62.49 |
| `pushOutOfBlocks(x, midY, z)` | `moveTowardsClosestSpace(x, midY, z)` | Адаптировано |
| `getBrightnessForRender`: raw `+120`, cap `240` | маскирование packed light + raw `+120` | Исправлено точно |
| `0.1 + 0.3 * remaining / maxAge` | явное float-деление | Исправлено точно |
| `GL_SRC_ALPHA, aspect.getBlend()` | аспектный `TC4NodeRenderTypes` | Подтверждено |
| `Aspect.ENTROPY/Void blend=771` | `usesAlphaBlend()` для Perditio/Vacuos | Подтверждено |
| `System.nanoTime()/25_000_000 % 16` | та же формула | Перенесено |
| `ParticleEngine.particleTexture` | оригинальный `particles.png` | Перенесено |
| `IEntityAdditionalSpawnData` | `SynchedEntityData` + `NetworkHooks` | Forge-адаптация |
| `InventoryUtils.isWandInHotbarWithRoom` | обход 9 hotbar-слотов и `current < capacity` | Семантика сохранена |
| NBT: Health/Age/Value/Aspect | те же четыре поля | Исправлено в v11.62.49 |

## 6. Текущее поведение сферы аспекта

### 6.1. Создание

Нестабильный незаблокированный узел по собственной локальной фазе может удалить одну единицу случайного доступного первичного аспекта и создать серверную `AspectOrbEntity` со значением 1 в центре блока.

### 6.2. Движение

- размер entity type: `0.125 × 0.125`;
- случайный начальный импульс;
- гравитация `-0.03`;
- базовое затухание `0.98`;
- Forge-aware трение блока на земле;
- вертикальный отскок с множителем `-0.9`;
- отдельная физика в лаве;
- выталкивание из блоков;
- срок жизни 150 тиков;
- здоровье 5.

### 6.3. Поиск цели

Сервер раз в 5 тиков ищет ближайшего живого не-spectator игрока в радиусе 8 блоков. Кандидат должен иметь в hotbar конечный жезл с хотя бы минимальным свободным местом для соответствующего primal-vis. Ссылка на цель хранится как entity ID и повторно проверяется при изменении дистанции, жизни или ёмкости.

### 6.4. Подбор

Подбор допускается только на сервере, только для primal-аспекта и только если оба cooldown равны нулю. Вис добавляется в найденный жезл, затем устанавливается `takeXpDelay=2`, проигрывается оригинально параметризованный orb pickup sound и сущность удаляется.

### 6.5. Renderer

- 16 кадров исходного ряда `particles.png`;
- длительность кадра 25 мс;
- billboard по ориентации камеры;
- оригинальные UV `v=0.5..0.5625`;
- native RGB аспекта;
- alpha 128;
- плавный scale `0.4 → 0.1`;
- raw lightmap boost `+120`, cap `240`;
- Perditio/Vacuos используют source-alpha destination, остальные — additive.

## 7. Единая таблица аудита ресурсов и моделей

Данные ниже получены из пяти JSON-аудитов, повторно сгенерированных для v11.62.49.

| Метрика | Результат |
|---|---:|
| Item-модели | **690** |
| Ошибки парсинга item-моделей | **0** |
| `builtin/entity`-модели | **8** |
| Полные Forge BEWLR-контракты | **8/8** |
| Все ссылки на текстуры | **523** |
| Ссылки в namespace Thaumcraft | **523** |
| Внешние ссылки на текстуры | **0** |
| Отсутствующие текстуры | **0** |
| Неразрешённые `#texture` | **0** |
| Точные ссылки на оригинальные PNG | **388** |
| Same-path original references | **2** |
| Custom/adapted references | **135** |
| Канонический оригинальный банк | **940/940** |
| Группы используемых byte-identical PNG | **57** |
| Placeholder-имена | **2**, оба классифицированы |
| Подозрительные placeholder-имена | **0** |
| Модели с собственным `display` | **35** |
| Модели с эффективным `display` | **48** |
| Модели с overrides | **0** |
| Максимальная глубина parent-цепочки | **2** |
| Отсутствующие parent | **0** |
| Parent-циклы | **0** |
| Семантические проблемы моделей | **0** |
| Предупреждения model audit | **0** |
| Точные JSON-дубли моделей | **21** групп |
| Визуально эквивалентные модели | **62** группы |
| Дубли PNG во всём `textures/item` | **92** группы |
| Видимые clone leaks | **0** |
| Проверки узла/банки/стабилизатора | **16/16** |
| JSON-ресурсы проекта | **1699/1699** |

Термины не смешиваются:

- **57** — группы хеш-одинаковых текстур среди реально разрешённых ссылок item-моделей;
- **92** — одинаковые PNG во всём каталоге item-текстур, включая неиспользуемые/алиасные ресурсы;
- **21** — полностью одинаковые JSON item-модели;
- **62** — модели с одинаковым итоговым визуальным разрешением после parent/texture/display анализа.

## 8. BEWLR и динамические модели

Обнаружены и подтверждены восемь динамических моделей:

1. `advanced_node_stabilizer`;
2. `avaritia_creative_wand`;
3. `greatwood_wand`;
4. `iron_capped_wooden_wand`;
5. `node_jar`;
6. `node_stabilizer`;
7. `silverwood_wand`;
8. `thaumometer`.

Для всех 8/8 подтверждена цепочка:

```text
item model builtin/entity
→ зарегистрированный Item
→ initializeClient
→ IClientItemExtensions
→ конкретный BlockEntityWithoutLevelRenderer
```

Моделей с одновременно активными JSON display-трансформациями и самостоятельной BEWLR-transform логикой не обнаружено.

## 9. Полный прогон проверок v11.62.49

```text
Forge-only guard: OK (Forge 1.19.2 / 43.5.2; no NeoForge or TerraBlender)
Java syntax guard: OK
JSON resource validation: OK (1699 files)
TC4 visual parity guard: OK
Research Table integration guard: OK
Research Table open guard: OK
Magical Forest worldgen guard: OK
Feature cycle guard: OK
Thaumometer scan guard: OK
Runtime visual guard: OK
Leaves/Wand/Research Table guard: OK
TC4 original texture bank guard: OK — 940 / 940 exact textures
TC4 runtime visual parity guard: OK
Runtime fix guard: OK
v11.62.45 parity guard: OK
v11.62.46 audit guard: OK
v11.62.47 arcane/wand guard: OK
v11.62.48 node/orb baseline guard: OK
v11.62.49 Aspect Orb parity guard: OK
Item visual audit: 690 models, 0 missing, 0 unresolved, 388 exact-original refs
Semantic model audit: 690 models, 0 problems, 0 warnings
BEWLR audit: 8/8 complete, 0 problems
Aura-node parity audit: 16/16 passed
Registry audit: 0 visible clone leaks, 0 resource problems, 0 unexpected problems
GitHub Actions build.yml: YAML OK
GitHub Actions release.yml: YAML OK
```

Новый guard v11.62.49 проверяет кодовыми инвариантами:

- наличие явного float ratio;
- отсутствие ошибочного integer quotient;
- raw block-light `+120` и отсутствие `ambientBlock + 8`;
- аспектный blend вместо принудительного additive;
- оба pickup gate;
- lava path вместо water path;
- современный push-out вызов;
- отсутствие `Cooldown` в NBT;
- подключение guard и единого отчёта к обеим CI-схемам.

## 10. Компиляция

### 10.1. Целевая Java-компиляция

На имеющемся mapped Forge/Minecraft 1.19.2 classpath успешно скомпилированы:

- `AspectOrbEntity.java`;
- `AspectOrbRenderer.java`.

Результат `javac --release 17`: exit code 0. Созданы оба `.class`. Выдано 12 compile-time предупреждений из-за неполного compile-only classpath и deprecated-конструктора `ResourceLocation`; ошибок нет.

Эта проверка подтверждает сигнатуры используемых 1.19.2 API, но не заменяет полный ForgeGradle task graph.

### 10.2. Полный Gradle build

Попытка:

```bash
./gradlew compileJava --offline --no-daemon --stacktrace
```

завершилась до конфигурации проекта, потому что wrapper всё равно должен получить отсутствующий дистрибутив Gradle 7.5.1:

```text
Downloading https://services.gradle.org/distributions/gradle-7.5.1-bin.zip
java.net.UnknownHostException: services.gradle.org
```

Поэтому production JAR не создан и старый бинарник не переименовывается под новую версию.

В среде с сетевым доступом требуется:

```bash
chmod +x gradlew
./gradlew clean build --stacktrace --no-daemon
```

Ожидаемый артефакт:

```text
build/libs/thaumcraft_legacy_rebuild_1.19.2-11.62.49.jar
```

## 11. Изменённые файлы v11.62.48 → v11.62.49

Патч содержит 19 файлов, включая единый отчёт и пять machine-readable JSON-аудитов:

- `.github/workflows/build.yml`
- `.github/workflows/release.yml`
- `README.md`
- `THAUMCRAFT_V11_62_49_FULL_REPORT.md`
- `build.gradle`
- `reports/aura_node_parity_audit_v11.62.49.json`
- `reports/bewlr_contract_audit_v11.62.49.json`
- `reports/item_visual_audit_v11.62.49.json`
- `reports/model_transform_audit_v11.62.49.json`
- `reports/registry_audit_v11.62.49.json`
- `src/main/java/com/darkifov/thaumcraft/client/render/AspectOrbRenderer.java`
- `src/main/java/com/darkifov/thaumcraft/entity/AspectOrbEntity.java`
- `src/main/resources/META-INF/mods.toml`
- `tools/audit_registry.py`
- `tools/aura_node_parity_audit.py`
- `tools/bewlr_contract_audit.py`
- `tools/model_transform_audit.py`
- `tools/tc4_116248_node_orb_guard.py`
- `tools/tc4_116249_aspect_orb_parity_guard.py`

Отдельные Markdown-аудиты генераторов не входят в выдаваемые архивы. Человеко-читаемый отчёт ревизии ровно один — этот файл. JSON-аудиты оставлены как машинные доказательства и вход для CI.

## 12. Что обязательно проверить в запущенном Forge-клиенте

### 12.1. Scale и кадры

1. Создать сферу с возрастом 0 и подтвердить визуальный scale около 0.4.
2. Снять кадры на возрастах 25/50/75/100/125 и подтвердить плавное уменьшение без скачка после первого тика.
3. Проверить все 16 кадров исходного UV-ряда.
4. Сравнить скорость 25 мс/кадр со старым клиентом TC4.

### 12.2. Свет и blend

5. Проверить сферу при block light 0, 4, 8, 12 и 15.
6. Убедиться, что прибавка выглядит как TC4, но не даёт абсолютный fullbright во всех условиях.
7. Проверить неизменность sky-light днём и ночью.
8. Сравнить Aer/Terra/Ignis/Aqua/Ordo в additive-режиме.
9. Отдельно сравнить Perditio и Vacuos с alpha destination blend 771.

### 12.3. Pickup

10. Сфера с `orbCooldown > 0` не подбирается.
11. Сфера не подбирается, пока `takeXpDelay > 0`.
12. После успешного подбора устанавливается двухтиковая задержка игрока.
13. Две сферы не должны поглощаться в один и тот же игровой тик.
14. Игрок без hotbar-жезла не притягивает сферу.
15. Полный жезл не притягивает сферу.
16. Жезл с 1 centivis свободного места допускает подбор, заполняется, остаток сферы теряется.
17. Творческий бесконечный жезл не поглощает сферу.

### 12.4. Физика и NBT

18. В лаве сфера получает подъём, случайное X/Z движение и fizz каждый update.
19. В воде не должен включаться лавовый path.
20. Сфера, появившаяся внутри блока, должна пытаться выйти в ближайшее свободное пространство.
21. Сохранить и загрузить сферу: сохраняются Health/Age/Value/Aspect.
22. После загрузки локальный `orbCooldown` начинается со штатного runtime-значения, а не читается из NBT.
23. Сфера удаляется после 150 тиков и после суммарного урона 5.
24. Повторить на dedicated server и убедиться в корректной синхронизации аспекта/значения.

## 13. Дальнейший план портирования

### P0 — runtime-подтверждение v11.62.49

- выполнить матрицу раздела 12 на integrated и dedicated server;
- получить side-by-side кадры TC4 и Forge 1.19.2 для scale/light/blend;
- собрать production JAR через GitHub Actions или локальный Gradle с сетью;
- записать найденные runtime-отклонения в следующую единую ревизию отчёта.

### P1 — узлы ауры

- сверить все типы и модификаторы узлов: normal, pure, dark, hungry, unstable, tainted;
- проверить число billboard-плоскостей, размер каждого аспектного слоя и белого type-layer;
- проверить видимость обычным взглядом, через Goggles и через Таумометр;
- восстановить и проверить луч высасывания вис: начало, конец, толщина, цвет, интерполяция;
- проверить стабилизатор и advanced stabilizer под всеми состояниями узла;
- проверить банку узла в мире, GUI, ground, fixed и обеих руках.

### P2 — Таумометр, жезлы и Arcane Workbench

- runtime-проверка раскладки аспектов 5/4/3/2/1;
- проверка scan hold 25/20 тиков и server ray validation;
- жезлы, скипетры, посохи и фокусы во всех transform contexts;
- проверка exact shapeless matching, catalyst normalization и скидок вис;
- проверка GUI Scale, слотов, UV и server-authoritative craft transaction.

### P3 — Таумономикон и исследования

- обычные, secondary, hidden и round исследования;
- списание аспектов и создание research note;
- Eldritch gating;
- сетевой выбор точного research key;
- масштабирование, scissor, линии, рамки и отсутствие второго GUI.

### P4 — оставшиеся системы TC4

- инфузия, pedestals, instability и эффекты;
- големы, ядра, маркеры и задачи;
- сущности, снаряды, чемпионы и боссы;
- структуры, Magical Forest и оставшийся worldgen;
- безопасная миграция legacy alias-ID только после проверки старых сохранений;
- замена остаточных parity/placeholder-классов специализированным поведением.

## 14. Итоговый статус

**v11.62.49 завершена как статически проверенная ревизия исходников Forge 1.19.2.** Критическая ошибка ступенчатого масштаба исправлена. Свет перенесён в точном сыром формате lightmap. Подтверждено оригинальное сочетание локального orb cooldown и player XP delay. Исправлены лава, push-out и NBT. Аспектное смешивание оставлено раздельным на основании `Aspect#getBlend` TC4.

Все guards, ресурсные аудиты, модельные parent/display проверки, BEWLR-контракты, node parity checks и YAML-проверки проходят. Изменённые классы компилируются на mapped Forge 1.19.2 classpath.

Статус production-ready пока не присваивается: отсутствуют полный ForgeGradle build и игровой runtime-прогон. Это ограничение явно зафиксировано, и неподтверждённый старый JAR не выдаётся как новая сборка.
