# Thaumcraft Legacy Rebuild v11.62.54-hotfix3 — полный экспертный технический отчёт, редакция R4

**Снимок кода:** v11.62.54  
**Исправленный пакет:** v11.62.54-hotfix3  
**Цель:** перенос поведения и визуального языка Thaumcraft 4.2.3.5 с Minecraft 1.7.10 на Minecraft 1.19.2 / Forge 43.5.2 / Java 17  
**Дата формирования отчёта:** 2026-07-12  
**Тип документа:** независимое сводное техническое досье по всему проекту, предназначенное для передачи стороннему эксперту.

> Важно: документ разделяет наличие кода, успешную статическую проверку, успешную компиляцию и реальное подтверждение в запущенной игре. Наличие класса или прошедшего guard-теста не считается доказательством полной runtime-паритетности.


## A. Инцидент GitHub Actions и исправление hotfix3

### A.1. Исходные материалы и воспроизведение

Для ревизии использованы два переданных архива:

- `logs_78987939722(2).zip` — журнал упавшего GitHub Actions job;
- `Thaumcraft_Legacy_Rebuild_Forge_1.19.2_v11.62.54_hotfix2_GitHub(1).zip` — исходное дерево репозитория.

Сбой воспроизводился на шаге **Validate magical leaves, wand UV and Research Table renderer** командой:

```bash
python3 tools/leaves_wand_table_guard.py
```

Фактический вывод CI:

```text
Leaves/Wand/Research Table guard: FAILED
 - research table BER is not registered on the Forge client bus
Process completed with exit code 1
```

До этого шага успешно прошли Forge-only, Java-text, JSON, visual-path, Research Table, worldgen, feature-cycle, Thaumometer и runtime-visual проверки. Gradle-сборка не запускалась, потому что job остановился на первом ошибочном guard.

### A.2. Диагноз

Сообщение guard было ложным. В `ClientModEvents.java` регистрация присутствовала:

```java
BlockEntityRenderers.register(
        ThaumcraftMod.RESEARCH_TABLE_BLOCK_ENTITY.get(),
        blockEntityRenderer(ResearchTableRenderer::new));
```

Однако `tools/leaves_wand_table_guard.py` искал только устаревший фрагмент прямой регистрации:

```text
RESEARCH_TABLE_BLOCK_ENTITY.get(), ResearchTableRenderer::new
```

Начиная с hotfix2 прямой constructor reference намеренно обёрнут в `blockEntityRenderer(...)`. Это обязательная защита от production `AbstractMethodError` на SRG-имени `BlockEntityRendererProvider#m_173570_`. Поэтому возврат к строке, которую ожидал старый guard, формально сделал бы CI зелёным, но восстановил бы реальный риск падения Minecraft.

### A.3. Исправление

Первичный guard исправлен без отката рабочей SRG-safe архитектуры. Теперь `tools/leaves_wand_table_guard.py`:

1. требует точную регистрацию `RESEARCH_TABLE_BLOCK_ENTITY` через `blockEntityRenderer(ResearchTableRenderer::new)`;
2. проверяет наличие фабрики `blockEntityRenderer(...)`;
3. проверяет явный анонимный `BlockEntityRendererProvider`;
4. проверяет override `create(Context)` и делегирование `factory.apply(context)`;
5. выдаёт отдельную ошибку при дрейфе регистрации и отдельные ошибки при повреждении adapter-контракта.

После устранения первого стоп-фактора были вручную выполнены все последующие CI-проверки. Это выявило ещё три класса устаревших требований в regression guards v11.62.46–v11.62.54:

- regex текущей версии принимал только `11.62.54`, но отвергал корректные semver-суффиксы `-hotfix2`/`-hotfix3`;
- workflow-проверки требовали старое условное имя `FULL_REPORT.md` или `THAUMCRAFT_V11_62_*_FULL_REPORT.md`, хотя репозиторий уже использовал единый экспертный отчёт с другим именем;
- v11.62.48 guard требовал прямую `AspectOrbRenderer::new` регистрацию, запрещённую hotfix2 по той же SRG-причине, и не принимал `entityRenderer(AspectOrbRenderer::new)`.

Эти guards обновлены так, чтобы они проверяли фактический текущий контракт:

- базовая версия извлекается из `major.minor.patch`, а опциональный prerelease/hotfix suffix разрешён;
- consolidated report определяется по устойчивому шаблону `THAUMCRAFT_LEGACY_REBUILD_..._EXPERT_FULL_TECHNICAL_REPORT_R*.md`;
- Aspect Orb обязан регистрироваться через явный `entityRenderer(...)` adapter;
- v11.62.54 guard больше не проходит случайно по compatibility-комментарию, а разбирает реальную строку версии.

Таким образом source guards и post-build bytecode audit теперь проверяют одну архитектуру, а не противоречат друг другу.

### A.4. Версионирование и release plumbing

Ревизия поднята до `11.62.54-hotfix3` в:

- `build.gradle`;
- `META-INF/mods.toml`;
- build/release workflows;
- README;
- имени consolidated expert report;
- имени release ZIP и release-JAR audit JSON.

Игровая логика, реестры, модели, текстуры и runtime-классы в этом hotfix не изменялись. Изменение относится к CI-контракту и выпускной маркировке.

### A.5. Фактическая проверка hotfix3

| Проверка | Результат | Доказательная граница |
| --- | --- | --- |
| Первичный `leaves_wand_table_guard.py` | **PASS** | Подтверждает SRG-safe регистрацию Research Table BER и структуру adapter. |
| Regression guards v11.62.45–v11.62.54 | **PASS** | Подтверждает отсутствие статического дрейфа заявленных контрактов. |
| JSON validation | **PASS: 1699 файлов** | Синтаксис и ссылки, но не runtime-загрузка ресурсов. |
| Original TC4 texture bank | **PASS: 940/940 exact** | Byte-exact наличие канонического банка. |
| Item visual audit | **PASS: 690 моделей; 0 missing; 0 unresolved** | Статические цепочки моделей/текстур. |
| Semantic model audit | **PASS: 690 моделей; 0 problems; 0 warnings** | Parent/display transforms. |
| BEWLR audit | **PASS: 8/8; 0 problems** | Статический Forge builtin/entity contract. |
| Aura-node parity audit | **PASS: 16/16** | Заявленные узловые контракты. |
| Registry audit | **PASS: 690 моделей; 0 clone leaks; 0 resource/unexpected problems** | Статический реестр и ресурсы. |
| Python syntax compilation изменённых guards | **PASS** | Все изменённые `.py` компилируются. |
| GitHub workflow YAML parse и referenced-file check | **PASS** | Структура YAML и наличие вызываемых локальных файлов. |
| Локальный `./gradlew clean build` | **НЕ ВЫПОЛНЕН ПО ИНФРАСТРУКТУРНОЙ ПРИЧИНЕ** | Wrapper остановился до конфигурации проекта на `UnknownHostException: services.gradle.org`; это не Java/Gradle compile error проекта. |
| Post-build `audit_runtime_sam_bridges.py` и `audit_release_jar.py` | **ОЖИДАЮТ JAR ОТ GITHUB ACTIONS** | Эти проверки находятся после Gradle build и должны выполняться на сетевом GitHub runner. |
| Запуск Minecraft-клиента | **НЕ ВЫПОЛНЯЛСЯ В ЭТОЙ РЕВИЗИИ** | Runtime-паритет не заявляется как подтверждённый. |

Локальная среда не имела DNS/сетевого доступа к Gradle Distribution Service. Поэтому в архив намеренно **не вложен непроверенный или вручную перепакованный JAR**. Передаваемый результат — полный source archive для GitHub, где штатный workflow выполнит чистую ForgeGradle-сборку и оба post-build аудита.

### A.6. Изменённые файлы

| Файл | Назначение изменения |
| --- | --- |
| `tools/leaves_wand_table_guard.py` | Исправлена ложная проверка Research Table BER; добавлена проверка SRG-safe adapter contract. |
| `tools/tc4_116246_audit_guard.py` … `tools/tc4_116254_focus_hud_cycle_guard.py` | Версия с hotfix suffix, актуальное имя единого отчёта, SRG-safe Aspect Orb registration, устранение прохождения по compatibility-комментарию. |
| `.github/workflows/build.yml` | Версия artifact/release audit `hotfix3`, единый R4 report. |
| `.github/workflows/release.yml` | Версия release bundle/audit `hotfix3`, единый R4 report. |
| `build.gradle` | Project version `11.62.54-hotfix3`. |
| `src/main/resources/META-INF/mods.toml` | Mod version и описание hotfix3. |
| `README.md` | Ожидаемый JAR, ссылка на R4 и пояснение hotfix3. |
| `THAUMCRAFT_LEGACY_REBUILD_V11_62_54_EXPERT_FULL_TECHNICAL_REPORT_R4.md` | Единый полный отчёт для независимой экспертизы. |

### A.7. Инструкция эксперту по воспроизведению

```bash
python3 tools/leaves_wand_table_guard.py
python3 tools/tc4_116246_audit_guard.py
python3 tools/tc4_116247_arcane_wand_guard.py
python3 tools/tc4_116248_node_orb_guard.py
python3 tools/tc4_116249_aspect_orb_parity_guard.py
python3 tools/tc4_116250_runtime_screenshot_guard.py
python3 tools/tc4_116251_research_scan_hud_guard.py
python3 tools/tc4_116252_runtime_risk_guard.py
python3 tools/tc4_116253_wand_beam_thaumometer_guard.py
python3 tools/tc4_116254_focus_hud_cycle_guard.py
python3 tools/audit_registry.py --version 11.62.54 --fail-on-unexpected
chmod +x gradlew
./gradlew clean build --stacktrace --no-daemon
```

После успешной сборки:

```bash
MAIN_JAR=$(find build/libs -maxdepth 1 -type f -name '*.jar' ! -name '*-sources.jar' ! -name '*-github.jar' | head -n 1)
python3 tools/audit_runtime_sam_bridges.py --jar "$MAIN_JAR"
python3 tools/audit_release_jar.py \
  --jar "$MAIN_JAR" \
  --version 11.62.54-hotfix3 \
  --report reports/release_jar_audit_v11.62.54-hotfix3.json
```

### A.8. Что именно должен показать GitHub Actions

Ожидаемая последовательность после загрузки архива в корень репозитория:

1. шаг **Validate magical leaves, wand UV and Research Table renderer** завершается строкой `Leaves/Wand/Research Table guard: OK`;
2. guards v11.62.46–v11.62.54 не отклоняют `11.62.54-hotfix3` и R4 report;
3. `Build Forge JAR` создаёт `thaumcraft_legacy_rebuild_1.19.2-11.62.54-hotfix3.jar`;
4. `Audit SRG-safe renderer and screen providers` подтверждает пять explicit SRG adapter overrides и отсутствие опасных SAM `invokedynamic`;
5. `Audit complete release JAR contents` создаёт `reports/release_jar_audit_v11.62.54-hotfix3.json`;
6. artifact публикуется под именем `thaumcraft-legacy-rebuild-v11.62.54-hotfix3`.



## 0. Аварийный runtime-аудит hotfix2

### 0.1. Наблюдаемая ошибка

Пользовательский клиент Forge 1.19.2 завершался во время начального `LoadingOverlay` со следующим корневым исключением:

```text
java.lang.AbstractMethodError:
ClientModEvents$$Lambda... does not define or inherit
BlockEntityRendererProvider.m_173570_(Context)
```

Стек проходил через `BlockEntityRenderers`, `BlockEntityRenderDispatcher` и initial resource reload. В отчёте загруженных модов присутствовал `thaumcraft 11.62.54-hotfix1`. Oculus и Rubidium находились в сборке, но receiver проблемного объекта принадлежал непосредственно `com.darkifov.thaumcraft.client.ClientModEvents`; поэтому они не являются первопричиной данного исключения.

### 0.2. Точная причина

`ClientModEvents` регистрировал block-entity renderers через прямые constructor references:

```java
BlockEntityRenderers.register(type, Renderer::new);
```

В development/official namespace SAM-метод `BlockEntityRendererProvider` называется `create`. В production SRG namespace Minecraft 1.19.2 тот же метод называется `m_173570_`. Полноценный ForgeGradle `reobfJar` умеет преобразовывать этот контракт, но использованный аварийный patched-production remapper изменял обычные method declarations/references и не менял имя `invokedynamic` call site. В результате JVM динамически создавала lambda-класс с методом `create`, тогда как runtime interface требовал `m_173570_`, что и дало `AbstractMethodError`.

Тот же класс содержал аналогичный риск для:

- `EntityRendererProvider#create` → `m_174009_`;
- `MenuScreens.ScreenConstructor#create` → `m_96214_`;
- `BlockColor#getColor` → `m_92566_`;
- `ItemColor#getColor` → `m_92671_`.

### 0.3. Исправление

Hotfix2 не оставляет lambda-класс непосредственной реализацией обфусцируемого Minecraft interface. Вместо этого используется двухступенчатый контракт:

1. constructor reference/lambda реализует стабильный JDK `Function` либо внутренний project interface;
2. явный анонимный adapter implements Minecraft interface и содержит настоящий override-метод.

Пример принципа:

```java
private static <T extends BlockEntity> BlockEntityRendererProvider<T> blockEntityRenderer(
        Function<BlockEntityRendererProvider.Context, ? extends BlockEntityRenderer<T>> factory) {
    return new BlockEntityRendererProvider<>() {
        @Override
        public BlockEntityRenderer<T> create(BlockEntityRendererProvider.Context context) {
            return factory.apply(context);
        }
    };
}
```

После reobfuscation override в adapter-классе имеет имя `m_173570_`, поэтому production JVM видит корректную реализацию interface. Аналогичные adapters добавлены для entity renderers, menu screens, block colors и item colors.

### 0.4. Проверка исправления

Изменённый `ClientModEvents.java` скомпилирован под Java 17 против mapped Forge 1.19.2 classpath:

- ошибок: **0**;
- предупреждений: **9**, все относятся к уже существующему deprecated `ItemBlockRenderTypes#setRenderLayer`;
- сгенерировано: **9 class-файлов**;
- fallback reobf: **8 references** и **5 override declarations**;
- SRG-методы adapters подтверждены: `m_173570_`, `m_174009_`, `m_96214_`, `m_92566_`, `m_92671_`;
- dangerous `invokedynamic` call sites, возвращающие Minecraft renderer/screen/color SAM interfaces: **0**;
- контрольный patched JAR проходит ZIP integrity и новый `audit_runtime_sam_bridges.py`.

В GitHub Actions после `./gradlew build` добавлен обязательный post-build audit готового reobfuscated JAR. Публиковать следует именно артефакт GitHub Actions, а не старый hotfix1 JAR.

### 0.5. Что hotfix2 не меняет

Hotfix2 является загрузочным исправлением. Он не изменяет баланс, данные мира, исследовательскую логику или визуальный паритет. Подтверждённый дефект D-001 с предварительной симуляцией места при смене фокуса остаётся отдельной gameplay-задачей следующей ревизии.

## 1. Краткое резюме

Проект представляет собой крупный Forge-мод с единым `modId=thaumcraft`, который переносит ядро TC4, часть аддонов и значительный объём оригинальных ресурсов. В исходном снимке находятся 508 Java-файлов и 6120 ресурсных файлов. Зарегистрированы сотни предметов и блоков, 28 типов block entity, 26 типов сущностей, 13 меню и 37 сетевых сообщений. В исследовательском мосте определена 201 запись в шести оригинальных категориях.

Статические аудиты показывают целостность JSON-моделей и ссылок на текстуры, наличие всех восьми динамических BEWLR-контрактов, отсутствие видимых утечек реестровых клонов и соответствие 940 оригинальных файлов каноническому банку. Исходный v11.62.54 JAR не проходил Forge mod discovery из-за синтаксически недопустимой строки в `mods.toml`; это исправлено в hotfix1. После этого runtime-тест hotfix1 выявил второй независимый загрузочный дефект: `AbstractMethodError` в `BlockEntityRendererProvider` во время начальной перезагрузки ресурсов. Hotfix2 заменяет прямые lambda/method-reference реализации обфусцируемых Minecraft SAM-интерфейсов на явные bridge-объекты с настоящими override-методами, которые корректно reobf-ятся в SRG namespace.

При этом проект нельзя считать полностью готовым портом TC4. Последний фактически показанный runtime-проход был выполнен на более ранней v11.62.49 и обнаружил крупные визуальные и функциональные ошибки. Версии v11.62.50–v11.62.54 исправлялись по исходникам, аудитам и скриншотам, но их полный клиентский и dedicated-server runtime не подтверждён. Кроме того, в v11.62.54 подтверждена ошибка порядка транзакционной смены фокуса, описанная ниже как D-001.

## 2. Границы доказательности

| Уровень | Что подтверждает | Ограничение |
| --- | --- | --- |
| E1 — исходник | Класс, ресурс или алгоритм реально присутствует в проекте. | Не доказывает компиляцию и работу в игре. |
| E2 — статический аудит | JSON, ссылки, registry-контракты или маркеры прошли автоматическую проверку. | Guard может проверять только заранее заданные условия. |
| E3 — компиляция/reobf | Изменённые классы были скомпилированы и remap-нуты. | Не гарантирует загрузку всего мода или отсутствие mixin/registry/runtime ошибок. |
| E4 — запуск клиента | Конкретная сборка загрузилась и была проверена в мире. | Подтверждено пользователем для v11.62.49; поздние ревизии требуют повторного теста. |
| E5 — dedicated server | Поведение подтверждено на отдельном сервере. | Для v11.62.54 не выполнено. |

## 3. Платформа, сборка и зависимости

| Параметр | Значение |
| --- | --- |
| Minecraft | 1.19.2 |
| Forge | 43.5.2 |
| ForgeGradle | 5.1.76 |
| Java | 17 |
| Mappings | official 1.19.2 |
| Mod ID | `thaumcraft` |
| Архивное имя | `thaumcraft_legacy_rebuild_1.19.2` |
| Лицензия в `mods.toml` | All Rights Reserved |
| Обязательные зависимости | Forge 43+, Minecraft [1.19.2, 1.20) |
| Внешние runtime-библиотеки | В `build.gradle` не объявлены; проект ориентирован на самодостаточный Forge JAR. |

### 3.1. Нормальная полная сборка

```bash
chmod +x gradlew
./gradlew clean build --stacktrace --no-daemon
```

Ожидаемый результат:

```text
build/libs/thaumcraft_legacy_rebuild_1.19.2-11.62.54-hotfix3.jar
```

### 3.2. Фактически использованный release-процесс v11.62.54

Полный `gradlew build` в рабочей среде не был выполнен из-за невозможности загрузить Gradle 7.5.1. Release-JAR создавался контролируемым patched-production процессом на базе предыдущего JAR: управляемые ресурсы удалялись, все 6120 текущих ресурсов добавлялись заново, новые классы reobf-ились и заменялись, manifest и `mods.toml` обновлялись, после чего содержимое сравнивалось с исходным деревом.

Это существенно надёжнее простого переименования старого JAR, но всё равно не эквивалентно чистой сборке всего графа Gradle. Чистая сборка остаётся обязательной перед стабильным релизом.

## 4. Размер и состав кодовой базы

| Категория | Количество |
| --- | --- |
| Java-файлы | 508 |
| Java-файлы внутри основного пакета | 508 |
| Ресурсные файлы | 6120 |
| JSON | 1699 |
| PNG | 3770 |
| OGG | 222 |
| Item-модели | 690 |
| Block-модели | 191 |
| Blockstates | 167 |
| Текстуры предметов | 614 |
| Текстуры блоков | 459 |
| GUI-текстуры | 86 |
| Model-текстуры | 168 |
| Языковые файлы | 26 |
| Обычные recipes JSON | 79 |
| Alchemy recipes | 67 |
| Arcane Workbench recipes | 98 |
| Infusion recipes | 73 |
| Loot tables | 160 |
| Source-mapping файлов | 95 |

### 4.1. Java-пакеты

| Пакет | Java-файлов |
| --- | --- |
| (корневой пакет) | 8 |
| alchemy | 3 |
| arcane | 6 |
| aura | 9 |
| block | 122 |
| blockentity | 28 |
| client | 105 |
| compat | 1 |
| config | 1 |
| data | 2 |
| eldritch | 23 |
| entity | 25 |
| essentia | 9 |
| event | 3 |
| golem | 10 |
| infusion | 18 |
| item | 3 |
| jar | 1 |
| menu | 16 |
| network | 38 |
| porting | 16 |
| recipe | 4 |
| research | 23 |
| runic | 6 |
| source | 2 |
| taint | 1 |
| thaumicenergistics | 4 |
| wand | 14 |
| ward | 1 |
| world | 6 |

## 5. Реестры Forge

| Реестр | Количество | Комментарий |
| --- | --- | --- |
| Предметные `RegistryObject`-поля | 166 | Включают реальные предметы, block-item carriers, compatibility/legacy carriers и служебные позиции. |
| Блочные `RegistryObject`-поля | 168 | Включают функциональные, декоративные, eldritch, taint и compatibility-блоки. |
| Block entity types | 28 | Контейнеры, трубы, узлы, матрица, столы, порталы и другие tile-системы. |
| Entity types | 26 | Мобы, голем, Aspect Orb и focus projectiles. |
| Menu types | 13 | Серверно-авторитетные контейнеры и GUI. |
| Recipe serializers | 1 | Отдельно зарегистрирован counted smelting; остальные custom recipes загружаются собственными менеджерами. |
| Сетевые сообщения | 37 | SimpleChannel protocol version 2. |

Крупное число item-моделей (690) больше числа статически обнаруженных зарегистрированных моделей. Это объясняется сохранёнными legacy/meta carriers, визуальными алиасами и карантином старых ID. Аудит сообщает 384 статически обнаруженные зарегистрированные модели, 122 точных карантинных ID и 31 quarantined prefix; видимых clone leaks не найдено.

## 6. Архитектура переноса

### 6.1. Принцип source-driven parity

Проект не является простым ресурс-паком. Логика переносится из исходного TC4 в адаптеры Minecraft 1.19.2. Для различий API используются отдельные мосты с префиксами `TC4`, `Original` и `Runtime`. В `data/thaumcraft/tc4_source_mapping` хранится 95 машинных файлов с извлечёнными аспектами, рецептами, исследованиями, соответствиями классов, метаданными фокусов и другим материалом для проверки дрейфа.

### 6.2. Замена архитектуры 1.7.10 на Forge 1.19.2

| TC4 / 1.7.10 | Адаптация 1.19.2 |
| --- | --- |
| GameRegistry / статические ID | DeferredRegister + RegistryObject |
| TileEntity | BlockEntityType + BlockEntity |
| IInventory/Container | MenuType, AbstractContainerMenu, server-authoritative packets |
| NBT предметов | CompoundTag с миграцией legacy-ключей |
| RenderItem/Tile renderer | BEWLR, BlockEntityRenderer, EntityRenderer, RenderType |
| SimpleNetworkWrapper | Forge SimpleChannel, protocol `2` |
| Player persistent data | Forge persistent NBT и явные sync packets |
| Metadata variants | Раздельные item/block IDs, aliases и source-mapping |
| World generation hooks | Runtime chunk queue и Forge events; не полностью data-driven worldgen |

## 7. Система аспектов

В `Aspect` определено **48 аспектов**: шесть primal и 42 compound. Каждый аспект хранит ID, отображаемое имя, цвет, два компонента и правило смешивания. Perditio и Vacuos помечены как аспекты с `ONE_MINUS_SRC_ALPHA`, остальные используют additive-подход в соответствующих эффектах.

Основные классы:

- `Aspect`, `AspectList`, `AspectStack`, `AspectDatabase` — модель данных и объектные теги.
- `AspectCombinationRegistry` — комбинации составных аспектов.
- `PlayerAspectKnowledge` — известные аспекты и research pool игрока.
- `AspectKnowledgeSyncPacket` — синхронизация известных аспектов и очков.
- `OriginalAspectWallet` / `OriginalArcaneCostBridge` — совместимость с исходной экономикой исследований и vis.

При первой инициализации игроку выдаются по 10 единиц шести primal-аспектов, используя одноразовый маркер `StarterPrimalsSeeded`. Данные копируются при clone/death event. Старые миры, уже получившие ошибочную дополнительную выдачу, автоматически не нормализуются, чтобы не отнимать честно заработанные очки.

## 8. Исследования и Таумономикон

В `TC4ResearchRuntimeBridge` материализована **201 исследовательская запись** в шести категориях: BASICS, THAUMATURGY, ALCHEMY, ARTIFICE, GOLEMANCY и ELDRITCH.

### 8.1. Серверная модель

- `ResearchRegistry` хранит дерево и проверяет зависимости.
- `OriginalResearchProgression`, `ResearchLocks`, `OriginalResearchSelection` управляют выбором, доступностью и завершением.
- `ResearchNoteState`, `ResearchNoteGrid`, `ResearchNoteSolver` реализуют исследовательские записки и соединение аспектов.
- `ResearchTableBlockEntity`, `ResearchTableMenu` и пакеты действий обеспечивают серверно-авторитетный стол исследований.
- `ResearchSyncPacket`, `ResearchNoteSyncPacket` и запросные пакеты синхронизируют клиента.

### 8.2. Клиентская модель

- `ThaumonomiconScreen` отображает категории и дерево исследований.
- `TC4ResearchPageScreen` отображает страницы текста, рецептов, infusion/crucible/compound-схемы.
- Фон дерева использует оригинальные координаты, warp-узлы используют `nodes.png`, скрытые/secondary/special рамки разделены.
- Legacy выражения `ConfigItems` и `ConfigBlocks` разрешаются через `TC4ResearchItems` и runtime recipe bridge.

### 8.3. Ограничения

В метаданных исследований широко встречается флаг `stub`. Он используется и для исходных служебных/auto-unlock записей, и для ещё неполностью материализованных страниц. Поэтому наличие всех 201 записей не означает, что каждая страница, рецепт и награда полностью идентичны TC4. Главное дерево и все внутренние страницы требуют скрин-в-скрин runtime-проверки.

## 9. Таумометр и система сканирования

`ThaumometerItem` реализует удержание предмета, pending target, серверную проверку дистанции/существования, block/entity/node scan, выдачу аспектов и запись scan knowledge. Клиент передаёт конкретную цель через `RequestThaumometerScanPacket`, а сервер не повторяет каждый тик нестабильный точный crosshair ray, но продолжает проверять допустимость цели.

Алгоритм ручного сканирования использует TC4-путь `@`: игрок получает фактическое количество аспектов объекта и бонус при первом открытии аспекта; составной аспект нельзя понять до знания обоих компонентов. Отдельные `ScanKnowledgeSyncPacket` и NBT-списки сохраняют уже сканированные блоки, сущности и узлы.

Рендер Таумометра является одним из восьми BEWLR-контрактов. Корпус использует окружающий свет; пульсация lightmap применяется только к стеклу/readout. После v11.62.49 пользователь сообщал, что сканирование не работало; исправления v11.62.50–v11.62.53 требуют повторного runtime-подтверждения.

## 10. Жезлы, посохи, скипетры, vis и фокусы

В проекте определено **19 типов стержней**, **7 типов наконечников** и **10 основных runtime-типов фокусов**. Дополнительные предметные фокусы аддонов присутствуют отдельно и не все входят в enum базового runtime.

### 10.1. Vis и компоненты

- `WandItem` хранит primal vis в centivis, мигрирует старые NBT-форматы и ограничивает заряд ёмкостью стержня.
- `WandRodType` задаёт ёмкость, стоимость, staff-флаг, glow и регенерацию элементальных стержней.
- `WandCapType` задаёт базовую и aspect-specific скидку.
- `WandComponentData` хранит выбранные rod/cap и compatibility-данные.
- `WandFocusRuntime`, `WandFocusType`, `WandFocusUpgradeRuntime` обслуживают активный фокус, стоимость, cooldown и улучшения.
- Скидки брони/bauble-адаптеров подключаются к crafting и casting через модификаторы потребления.

### 10.2. HUD и рендер

v11.62.54 восстанавливает 32×32 wand dial из `hud.png`: шесть primal reservoirs, cost/change markers, Shift-значения, иконку фокуса, Equal Trade block и cooldown. Положение регулируется `wandDialBottom`. Динамический рендер жезлов выполняет `WandItemRenderer`; first-person положение, луч узла и lightmap менялись в последних ревизиях и требуют проверки в игре.

### 10.3. Подтверждённый дефект D-001 — неверный порядок смены фокуса

В текущем `WandManagerRuntime.changeFocus` выбранный новый фокус извлекается вызовом `selectedLocation.take(player)` **до** проверки, можно ли сохранить установленный старый фокус. Только затем вызывается `tryStoreFocus(player, installed)`. Это расходится с требуемой строгой транзакцией и с экспертным замечанием по оригинальному `WandManager.changeFocus`.

Текущий порядок v11.62.54:

```text
1. Выбрать location нового фокуса.
2. Извлечь новый фокус и изменить inventory/pouch.
3. Попытаться сохранить старый фокус.
4. При неудаче откатывать новый в освобождённый слот или fallback.
```

Требуемый порядок:

```text
1. Выбрать location нового фокуса, ничего не изменяя.
2. Симулировать сохранение старого фокуса по оригинальному порядку storage.
3. Зарезервировать/проверить конкретное место назначения старого фокуса.
4. Только после успешного preflight извлечь новый фокус.
5. Фактически сохранить старый фокус в зарезервированное место.
6. Установить новый фокус на жезл.
7. При любой гонке выполнить полный rollback без drop и без изменения жезла.
```

Статус D-001: **подтверждён, не исправлен в v11.62.54**. Формулировка старого отчёта, называющая существующий порядок точным соответствием TC4, неверна.

## 11. Узлы ауры и связанные устройства

Подсистема включает `AuraNodeBlockEntity`, типы и модификаторы узлов, локальный tick-counter, хранение аспектов, высасывание vis, stabilizer/transducer, jar capture, Aspect Orb и визуальный рендер.

Реализованные контракты:

- 32-кадровый atlas `nodes.png` и `System.nanoTime()/40_000_000` для анимации.
- Отдельные blend rules обычных, DARK/TAINTED и Perditio/Vacuos слоёв.
- World-space aspect tags для revealing gear.
- Луч высасывания на `wispy.png` с двумя пересекающимися полосами.
- Один tap каждые 5 тиков и усиление Node Tapper исследованиями.
- Aspect Orb для нестабильных узлов: возраст, притяжение, hotbar wand capacity, lava physics, собственный cooldown и player XP gate.
- Node Jar renderer, node stabilizer original mesh, overlay lightmap и additive bubble field.
- Vis relay network и node transducer существуют как отдельные runtime-системы.

Аудит узлов содержит 16/16 пройденных статических проверок. Это не подтверждает правильный размер, прозрачность, позицию GUI-тегов, точку луча или скорость в реальном клиенте.

## 12. Магический верстак и рецепты

`ArcaneWorkbenchBlockEntity`, `ArcaneWorkbenchMenu`, `ArcaneWorkbenchRecipe`, менеджер и client registry реализуют собственный recipe pipeline. В data pack находится 98 arcane-workbench JSON. Стоимость vis рассчитывается сервером с учётом wand cap и скидок экипировки.

В предыдущих ревизиях исправлялись exact occupied-slot matching, catalyst normalization, скрытые migration catalysts, server-authoritative ghost output и повторное вычисление shaped layout после consumption. Геометрия и UV стола/мистического верстака заменены на оригинальные atlas-модели, но после v11.62.50 визуально не подтверждены пользователем.

## 13. Алхимия и essentia

В проекте присутствуют crucible, alchemical furnace, alembic, centrifuge, crystalizer, essentia jars, filtered/void jars, tubes, valves, reservoirs, thaumatorium и связанные block entities.

Ключевые элементы:

- 67 alchemy recipe JSON и собственный `AlchemyRecipeManager`.
- Essentia transport через `EssentiaTubeBlockEntity`, suction resolver и tube subtypes.
- Jar labels, suction/top-side взаимодействия и revealing HUD.
- Thaumatorium menu/formula packets и server-side recipe execution.
- Alchemical furnace + alembic separation, centrifuge и crystalizer runtime classes.

Подсистема большая, но комплексный runtime-тест сети труб, соседних чанков, сохранения NBT, фильтров, клапанов и automation отсутствует. В коде встречаются compatibility stubs и fallback-пути; эксперт должен рассматривать эту часть как функционально частичную.

## 14. Инфузия

В data pack присутствует 73 infusion recipe JSON. `InfusionMatrixBlockEntity` содержит state machine активации, поиск pedestal-компонентов, essentia requirements, instability, source effects, failure handling и завершение рецепта. Есть отдельные классы matching, failure parity, enchantment runtime, auxiliary blocks и FX packets.

Руническая матрица в клиенте переведена на baked cuboid model layer с отдельными base/overlay UV из `infuser.png`. Визуальная текстура исправлялась по runtime-скриншотам, но полный процесс — запуск, всасывание компонентов, essentia, instability events, interruption, save/reload и multiplayer — не подтверждён. Статус: **реализовано значительным объёмом, runtime-паритет не доказан**.

## 15. Големы

Пакет `golem` содержит типы cores, upgrades, decorations, task markers, filters и runtime helpers. `ThaumGolemEntity` реализует множество original-like задач: перенос, сбор, сортировку, охрану, butcher, harvest, lumber, fishing, liquid handling, use markers и ranged decorations.

Наличие большой state machine не доказывает корректную навигацию, marker semantics, sided inventories и поведение в unloaded chunks. В коде есть source-driven адаптации, но полный набор TC4 големов, AI edge cases и визуальное оснащение требует dedicated-server и multiplayer тестов.

## 16. Сущности, боссы, снаряды и champion modifiers

Зарегистрировано **26 EntityType**. Среди них: голем, Pech, taint crawler, eldritch guardian/crab/warden/golem, crimson cultists, cultist portal, три taintacle-варианта, Aspect Orb, Firebat и focus projectiles.

FX передаются отдельными пакетами `PacketFX*`, `PacketRunicCharge` и клиентскими FX-классами. Некоторые client FX прямо описаны в исходнике как `approximation`, поэтому визуальное равенство оригиналу не заявляется. Champion behavior и boss FX должны проверяться на latency и tracking range.

## 17. Eldritch, Outer Lands и taint

В проекте имеются dimension/dimension_type JSON, eldritch portal/altar/locks/traps/crystals, Outer Lands adapters, guardians/warden/golem, crimson cultists и taint blocks/entities.

Однако отдельный текст в Thaumonomicon прямо обозначает текущую arena-механику портала как placeholder для будущего полноценного dungeon/dimension gameplay. Outer Lands decoration/generation использует adapter-классы, а не доказанную копию полного генератора TC4. Taint присутствует как блоки, жидкости/газ, fibres, soil и сущности, но распространение и экология требуют отдельного аудита.

## 18. Генерация мира и биомы

`TC4WorldgenRuntime` использует очередь загруженных/новых чанков и Forge events. Он генерирует руды, greatwood/silverwood, infused crystals и taint pockets; условия биомов адаптированы по пути modern biome IDs. Magical Forest зарегистрирован в `TC4Biomes`, но генерация не построена как полный современный набор configured/placed features.

В комментариях исходника greatwood/silverwood conditions обозначены как source-driven approximations. Требуются проверки seed-determinism, повторной обработки чанка, границ чанков, server restart, flat world, custom dimensions и взаимодействия с другими worldgen-модами.

## 19. Аддоны и совместимость

В проект включены элементы Thaumic Tinkerer, Thaumic Energistics, Thaumcraft Extras и KAMI/ichor content. Они размещены в отдельных пакетах и compatibility items/blocks.

Не все элементы являются полными портами. Подтверждённые примеры незавершённости:

- Golem Wireless Backpack описан как placeholder/diagnostic item.
- Focus Ender Chest описан как remote-storage placeholder.
- Некоторые старые generic utility items помечены как deprecated placeholders и заменены dedicated items.
- Полная AE2-интеграция не может считаться доказанной без соответствующей внешней зависимости и integration test.

## 20. Клиентский слой

Пакет `client` содержит **105 Java-файлов**: screens, renderers, models, overlays и FX.

### 20.1. Динамические модели BEWLR

Аудит подтверждает 8/8 контрактов:

- `node_stabilizer`
- `advanced_node_stabilizer`
- `iron_capped_wooden_wand`
- `greatwood_wand`
- `silverwood_wand`
- `avaritia_creative_wand`
- `thaumometer`
- `node_jar`

### 20.2. Риски визуального слоя

Статический аудит гарантирует наличие PNG и разрешимость model parents, но не гарантирует правильные UV, scale, lightmap, blending, z-order, GUI scissor и first-person transforms. Пользовательский runtime v11.62.49 уже показал, что при 940/940 текстурах столы, матрица, жезл, GUI и узлы могли выглядеть неправильно. Поэтому массовый screenshot-аудит остаётся обязательным.

## 21. Сеть и авторитетность сервера

`ThaumcraftNetwork` регистрирует **37 сообщений** в SimpleChannel protocol `2`. Канал проверяет точное совпадение версии клиента и сервера.

Пакеты покрывают исследования, знания аспектов, scan knowledge, Таумометр, research notes, Thaumatorium, essentia terminals/drives, osmotic enchanter, transvector, wand architect/focus changes и клиентские FX.

Большинство изменяющих мир действий направляется на сервер и повторно валидируется. Тем не менее dedicated-server тест не выполнен; необходимо проверить side-only class loading, packet ordering, malicious payload bounds, menu distance checks, player disconnect, chunk unload и high latency.

## 22. Ресурсы, модели и текстуры

| Метрика | Значение |
| --- | --- |
| Item-модели | 690 |
| Ошибки JSON item-моделей | 0 |
| Динамические builtin/entity | 8 |
| Ссылки на текстуры | 523 |
| Внешние namespace-ссылки | 0 |
| Пропавшие ссылки | 0 |
| Неразрешённые `#texture` | 0 |
| Точные совпадения с оригиналом | 388 |
| Custom/adapted ссылки | 135 |
| Канонический оригинальный банк | 940 |
| Группы одинаковых используемых текстур | 57 |
| Parent cycles | 0 |
| Missing parents | 0 |
| Explicit display models | 35 |
| Effective display models | 48 |
| Проблемы transform-аудита | 0 |

57 групп одинаковых используемых текстур не следует автоматически удалять: часть является намеренными legacy aliases или общими исходными иконками. Оптимизация допустима только после проверки registry/source mappings и save compatibility.

## 23. Автоматические проверки

| Проверка | Результат |
| --- | --- |
| Item visual audit | 690 моделей, 0 missing/unresolved |
| Model transform audit | 0 problems, max parent depth 2 |
| BEWLR contract audit | 8/8 |
| Aura node parity audit | 16/16 |
| Registry audit | 0 visible clone leaks; 21 exact JSON duplicate group; 62 visual duplicate group |
| Release JAR audit | 6120 resources, 503 primary classes, 0 mismatches |

Guards являются регрессионными маркерами конкретных контрактов. Они полезны для предотвращения возврата известных ошибок, но не заменяют unit/integration/GameTest тесты и реальный client rendering.

## 24. Известные незавершённые/приближённые участки

| ID | Подсистема | Проблема | Требуемое действие | Приоритет |
| --- | --- | --- | --- | --- |
| D-001 | Смена фокуса | Новый фокус извлекается до preflight сохранения старого. | Подтверждённый дефект; исправить до следующего релиза. | Высокий |
| R-002 | Поздние runtime-фиксы | v11.62.50–54 не прошли полный пользовательский runtime. | Нужен новый мир/клиентский тест. | Высокий |
| R-003 | Чистая сборка | JAR создан patched-production pipeline. | Выполнить `gradlew clean build` и сравнить SHA/entries. | Высокий |
| R-004 | Dedicated server | Нет подтверждённого server-only запуска. | Проверить class loading, пакеты, menus, persistence. | Высокий |
| R-005 | Визуальные модели | 0 missing PNG не гарантирует правильные UV/display. | Полный creative/JEI screenshot sweep. | Высокий |
| R-006 | Research stubs | 201 entry, но часть страниц/флагов служебная или неполная. | Сравнить каждую категорию и страницу с TC4. | Средний |
| R-007 | Outer Lands | Текущий portal arena прямо помечен placeholder. | Перенести полноценную структуру/dimension progression. | Высокий |
| R-008 | Worldgen | Greatwood/silverwood conditions — approximation. | Seed и biome compatibility tests. | Средний |
| R-009 | FX | Некоторые focus/champion/runic FX — approximation. | Видео/кадровое сравнение. | Средний |
| R-010 | Addon placeholders | Есть диагностические/placeholder предметы TT/TE. | Выделить из core release или допортировать. | Средний |
| R-011 | Deprecated API | Часть Forge API помечена deprecated. | Постепенно заменить без изменения 1.19.2 поведения. | Низкий |

## 25. Runtime-план для эксперта

### 25.1. Минимальный smoke test

1. Чистый Forge 43.5.2 профиль, только этот мод.
2. Новый мир survival и отдельный creative мир.
3. Проверка загрузки registry, recipes, tags, sounds и lang без ошибок.
4. Перезаход, смерть игрока, смена измерения, сохранение/загрузка чанка.
5. Повторить на dedicated server с двумя игроками.

### 25.2. Критические функциональные сценарии

1. Получить Thaumonomicon, открыть шесть категорий, пройти несколько зависимых исследований.
2. Новый игрок: ровно по 10 primal research points; стол не добавляет лишние +3.
3. Сканировать блок, entity, dropped item и node; проверить server rewards и повторное сканирование.
4. Собрать wand из разных rod/cap, зарядить из node, проверить 5-tick drain и Node Tapper.
5. Проверить D-001: полный inventory/pouch, direct focus, pouch focus, Shift+F, внешнее изменение слота.
6. Arcane Workbench: shaped/shapeless, catalyst present/embedded, insufficient vis, full inventory.
7. Crucible/furnace/alembic/tubes/jars/thaumatorium: NBT save/load и chunk boundaries.
8. Infusion: successful recipe, missing component, instability, interruption, logout, reload.
9. Golem: каждый core, marker/filter/sided inventory, unload/reload.
10. Portal/eldritch/taint/worldgen на нескольких seeds.

### 25.3. Визуальная матрица

Для каждого блока/предмета проверять GUI, ground, item frame, fixed, first-person left/right, third-person left/right, block placement, particles и breaking texture. Для GUI повторить GUI Scale 1/2/3/Auto и несколько разрешений окна.

## 26. Предлагаемый порядок дальнейшей разработки

1. Исправить D-001 через двухфазную storage simulation/reservation API.
2. Сделать чистую Gradle-сборку и зафиксировать reproducible artifact.
3. Провести runtime smoke test v11.62.54/55 и не добавлять новые системы до закрытия crash/visual blockers.
4. Массово проверить 690 item-моделей и 191 block-модель.
5. Закрыть Таумономикон/Таумометр/жезл/node как вертикальный core slice.
6. Затем стабилизировать arcane/alchemy/essentia/infusion.
7. После ядра — golems, entities, eldritch, taint и worldgen.
8. Аддоны отделить от core readiness и вести отдельную матрицу завершённости.

## 27. История последних ревизий

| Версия | Основной пакет |
| --- | --- |
| 11.62.45 | Таумономикон transaction, layout Таумометра, node animation precision, wand packed light. |
| 11.62.46 | Семантический model audit, 8 BEWLR, stabilizer blend/lightmap, node jar rendering. |
| 11.62.47 | Arcane Workbench occupied slots/catalyst и vis discounts. |
| 11.62.48 | Unstable node loop и Aspect Orb entity. |
| 11.62.49 | Aspect Orb float scale, raw lightmap, lava physics, cooldowns. |
| 11.62.50 | Runtime screenshot fixes: world node tags, first-person wand, tables, matrix, starter aspects, Thaumometer, research pages. |
| 11.62.51 | Research rewards, warp animation, browser background/lines, node tag behavior. |
| 11.62.52 | Time-normalized node tags и полный ресурсный JAR audit. |
| 11.62.53 | Original-like wand-node floaty beam и Thaumometer lighting. |
| 11.62.54 | Wand dial и focus cycling; обнаружен D-001 в порядке транзакции. |
| 11.62.54-hotfix1 | Исправлен синтаксис `mods.toml`, вызывавший Forge Exit Code 1 на mod discovery. |
| 11.62.54-hotfix2 | Добавлены SRG-safe adapters для renderer/screen/color SAM interfaces. |
| 11.62.54-hotfix3 | Исправлены ложные и устаревшие GitHub guards; CI и release metadata согласованы с hotfix2 architecture. |

## 28. Исторические контрольные суммы базовой ревизии v11.62.54

```text
98d681ff2a8fa96096dfc5e3241fb0b4deb5fb84133c55199291d1d8c1521753  thaumcraft-legacy-rebuild-v11.62.54.zip
cffd688df8b819443abb42195ad6271f23c901f6d0fac648088382576728afd7  thaumcraft_legacy_rebuild_1.19.2-11.62.54.jar
47c1104735a9b19b84e2a08d3445660ea9c428e5bb64243cfac8ee44e18d2d3b  Thaumcraft_Legacy_Rebuild_Forge_1.19.2_v11.62.54_source.zip
0264cfe61022b76493b866d7003a25c671fb0b06571832a5b806cc4f88c96f82  Thaumcraft_Legacy_Rebuild_Forge_1.19.2_v11.62.54_patch.zip
984a703ee05546afd9768f4dab4aa969c64cd70a9ec2b7d93eec1e663e1eedae  THAUMCRAFT_V11_62_54_FULL_REPORT.md
d3dcaa67dd79ba77d7cde85f0a9eb3e993ba2b6ec7638c6cec8ba0dc3a4f073a  CHANGED_FILES_V11_62_54.txt
```

## 29. Вопросы для независимого эксперта

1. Насколько корректна общая стратегия source-driven адаптации TC4 к 1.19.2 без прямого API-совместимого слоя?
2. Достаточно ли persistent player NBT и текущих sync packets, или следует перейти на Forge Capability?
3. Есть ли у SimpleChannel handlers достаточные server-side bounds/permission checks?
4. Может ли runtime chunk queue повторно модифицировать чанки или конфликтовать с другими генераторами?
5. Корректна ли модель storage reservation для будущего исправления D-001 при модифицируемых контейнерах?
6. Какие части InfusionMatrixBlockEntity следует разделить для тестируемости и crash isolation?
7. Можно ли считать оригинальные ресурсы и текущую лицензию допустимыми для предполагаемого способа распространения?
8. Какие compatibility aliases безопасно удалить без повреждения старых сохранений и research recipe mappings?
9. Какие GameTests целесообразно добавить первыми?
10. Какие подсистемы стоит исключить из первого стабильного core release и оставить experimental?

## 30. Итоговый статус

Проект является большим, содержательно богатым и технически серьёзным портом, а не макетом. В нём присутствуют реальные серверные системы, сотни content registrations, stateful block entities, сущности, сеть, custom recipes, research progression и значительный клиентский renderer layer.

Одновременно текущий снимок нельзя объявлять полным или стабильным в смысле TC4 parity. Главные причины: отсутствие чистой сборки и dedicated-server прогона, отсутствие полного runtime-теста поздних ревизий, визуальные проблемы, ранее проявлявшиеся при идеальных статических метриках, явные approximation/placeholder участки и подтверждённый D-001 в смене фокуса.

Рекомендуемый официальный статус кода v11.62.54 и пакета v11.62.54-hotfix1: **experimental parity port / expert review build**. Он пригоден для технического аудита и последовательного runtime-тестирования, но не для заявления о полном переносе Thaumcraft 4.2.3.5.


## 30.1. Исправления редакции R2 и причина Exit Code 1

Повторный подсчёт выполнен непосредственно по generic-типам `RegistryObject` в `ThaumcraftMod.java`, а не по объединённому текстовому списку:

| Категория | Точное количество |
| --- | ---: |
| `RegistryObject<Block>` | 168 |
| `RegistryObject<BlockEntityType<...>>` | 28 |
| `RegistryObject<EntityType<...>>` | 26 |
| `RegistryObject<MenuType<...>>` | 13 |

В первой редакции отчёта список C.2 ошибочно содержал 28 block entity-констант перед 26 настоящими entity-константами. Из-за этого число EntityType было ошибочно указано как 54. Приложение G также содержало 28 block entity-констант и одновременно пропускало 33 настоящих блока. Поэтому простое удаление 28 строк дало бы неполный список из 135 элементов; корректный заново сгенерированный список содержит 168 реальных блоков.

Отдельно обнаружена конкретная причина аварийного завершения Forge с общим сообщением лаунчера **Exit Code 1**: в `META-INF/mods.toml` v11.62.54 после последней таблицы зависимостей находилась необрамлённая строка обычного текста, начинавшаяся с `v11.62.54 restores...`. Она не является допустимой TOML-записью. Парсер останавливался на строке 155 с ошибкой `Expected '=' after a key in a key/value pair`, то есть мод не мог пройти даже стадию discovery.

Пакет `v11.62.54-hotfix1` переносит этот текст внутрь многострочного `description`, исправляет `version` и проходит независимый TOML-парсинг. Игровые class-файлы в hotfix1 не менялись. Подтверждённая ошибка D-001 с порядком смены фокуса остаётся открытой и должна исправляться отдельной кодовой ревизией.

---

# Приложение A. Список типов аспектов

`AER`, `TERRA`, `IGNIS`, `AQUA`, `ORDO`, `PERDITIO`, `VACUOS`, `LUX`, `TEMPESTAS`, `MOTUS`, `GELUM`, `VITREUS`, `VICTUS`, `VENENUM`, `POTENTIA`, `PERMUTATIO`, `METALLUM`, `MORTUUS`, `VOLATUS`, `TENEBRAE`, `SPIRITUS`, `SANO`, `ITER`, `ALIENIS`, `PRAECANTATIO`, `AURAM`, `VITIUM`, `LIMUS`, `HERBA`, `ARBOR`, `BESTIA`, `CORPUS`, `EXANIMIS`, `COGNITIO`, `SENSUS`, `HUMANUS`, `MESSIS`, `PERFODIO`, `INSTRUMENTUM`, `METO`, `TELUM`, `TUTAMEN`, `FAMES`, `LUCRUM`, `FABRICO`, `PANNUS`, `MACHINA`, `VINCULUM`

# Приложение B. Wand runtime types

## B.1. Rods

`WOOD`, `GREATWOOD`, `OBSIDIAN`, `BLAZE`, `ICE`, `QUARTZ`, `BONE`, `REED`, `SILVERWOOD`, `GREATWOOD_STAFF`, `OBSIDIAN_STAFF`, `BLAZE_STAFF`, `ICE_STAFF`, `QUARTZ_STAFF`, `BONE_STAFF`, `REED_STAFF`, `SILVERWOOD_STAFF`, `PRIMAL_STAFF`, `CREATIVE`

## B.2. Caps

`IRON`, `GOLD`, `THAUMIUM`, `COPPER`, `SILVER`, `VOID`, `INFINITY`

## B.3. Base focus enum

`FIRE`, `FROST`, `SHOCK`, `EXCAVATION`, `PORTABLE_HOLE`, `EQUAL_TRADE`, `WARDING`, `HELLBAT`, `PECH_CURSE`, `PRIMAL`

# Приложение C. Block entity, entity и menu registry constants

## C.1. Block entities

- `TEMPORARY_HOLE_BLOCK_ENTITY`
- `WARDED_BLOCK_ENTITY`
- `ARCANE_WORKBENCH_BLOCK_ENTITY`
- `FOCAL_MANIPULATOR_BLOCK_ENTITY`
- `RESEARCH_TABLE_BLOCK_ENTITY`
- `DECONSTRUCTION_TABLE_BLOCK_ENTITY`
- `CRUCIBLE_BLOCK_ENTITY`
- `ESSENTIA_JAR_BLOCK_ENTITY`
- `ESSENTIA_RESERVOIR_BLOCK_ENTITY`
- `ALEMBIC_BLOCK_ENTITY`
- `ALCHEMICAL_CENTRIFUGE_BLOCK_ENTITY`
- `ESSENTIA_CRYSTALIZER_BLOCK_ENTITY`
- `ESSENTIA_TUBE_BLOCK_ENTITY`
- `ALCHEMICAL_FURNACE_BLOCK_ENTITY`
- `THAUMATORIUM_BLOCK_ENTITY`
- `ARCANE_PEDESTAL_BLOCK_ENTITY`
- `INFUSION_MATRIX_BLOCK_ENTITY`
- `ELDRITCH_PORTAL_BLOCK_ENTITY`
- `ELDRITCH_CRAB_SPAWNER_BLOCK_ENTITY`
- `ELDRITCH_CAP_BLOCK_ENTITY`
- `ELDRITCH_LOCK_BLOCK_ENTITY`
- `ELDRITCH_TRAP_BLOCK_ENTITY`
- `ELDRITCH_CRYSTAL_BLOCK_ENTITY`
- `AURA_NODE_BLOCK_ENTITY`
- `NODE_STABILIZER_BLOCK_ENTITY`
- `NODE_TRANSDUCER_BLOCK_ENTITY`
- `ESSENTIA_DRIVE_BLOCK_ENTITY`
- `TRANSVECTOR_INTERFACE_BLOCK_ENTITY`

## C.2. Entities

- `THAUM_GOLEM`
- `TAINT_CRAWLER`
- `PECH`
- `ELDRITCH_GUARDIAN`
- `ELDRITCH_CRAB`
- `MIND_SPIDER`
- `ELDRITCH_WARDEN`
- `ELDRITCH_GOLEM`
- `CRIMSON_CULTIST`
- `CRIMSON_KNIGHT`
- `CRIMSON_CLERIC`
- `CRIMSON_PRAETOR`
- `CULTIST_PORTAL`
- `TAINTACLE`
- `TAINTACLE_SMALL`
- `TAINTACLE_GIANT`
- `ASPECT_ORB`
- `FIREBAT`
- `FOCUS_PECH_BLAST`
- `FOCUS_EMBER`
- `FOCUS_FROST_SHARD`
- `FOCUS_EXPLOSIVE_ORB`
- `FOCUS_SHOCK_ORB`
- `FOCUS_PRIMAL_ORB`
- `ELDRITCH_ORB`
- `GOLEM_ORB`

## C.3. Menus

- `ARCANE_WORKBENCH_MENU`
- `FOCAL_MANIPULATOR_MENU`
- `RESEARCH_TABLE_MENU`
- `DECONSTRUCTION_TABLE_MENU`
- `THAUMATORIUM_MENU`
- `PECH_TRADE_MENU`
- `ESSENTIA_TERMINAL_MENU`
- `ESSENTIA_DRIVE_MENU`
- `OSMOTIC_ENCHANTER_MENU`
- `TRANSVECTOR_INTERFACE_MENU`
- `BOTTOMLESS_POUCH_MENU`
- `FOCUS_POUCH_MENU`
- `GOLEM_MENU`

# Приложение D. Сетевые сообщения

- `ArcaneRecipeSyncPacket`
- `AspectKnowledgeSyncPacket`
- `OpenResearchNotePacket`
- `OpenResearchTablePacket`
- `PacketFXBlockZap`
- `PacketFXChampion`
- `PacketFXEldritchBoss`
- `PacketFXInfusionSource`
- `PacketFXShield`
- `PacketRunicCharge`
- `RequestClearResearchNoteSlotPacket`
- `RequestCombineAspectsPacket`
- `RequestCompleteSelectedResearchPacket`
- `RequestEssentiaDriveScanPacket`
- `RequestEssentiaInventoryScanPacket`
- `RequestEssentiaTerminalFilteredScanPacket`
- `RequestEssentiaTerminalScanPacket`
- `RequestFocusChangePacket`
- `RequestOsmoticEnchantPacket`
- `RequestOsmoticStructureCheckPacket`
- `RequestPechGiftPacket`
- `RequestPechTradePacket`
- `RequestPlaceResearchNoteAspectPacket`
- `RequestResearchTableActionPacket`
- `RequestResearchUnlockPacket`
- `RequestSelectResearchPacket`
- `RequestSolveResearchNotePacket`
- `RequestThaumatoriumFormulaPacket`
- `RequestThaumometerScanPacket`
- `RequestTransvectorActionPacket`
- `RequestTransvectorClearPacket`
- `RequestTransvectorInspectPacket`
- `RequestTransvectorStatusPacket`
- `RequestWandArchitectTogglePacket`
- `ResearchNoteSyncPacket`
- `ResearchSyncPacket`
- `ScanKnowledgeSyncPacket`

# Приложение E. Инвентаризация Java-классов по пакетам

## E.1. `(root)` — 8 файлов

- `Aspect`
- `AspectColor`
- `AspectCombinationRegistry`
- `AspectDatabase`
- `AspectList`
- `AspectStack`
- `AspectVisuals`
- `ThaumcraftMod`

## E.2. `alchemy` — 3 файлов

- `alchemy/AlchemyRecipe`
- `alchemy/AlchemyRecipeManager`
- `alchemy/AlchemyRecipes`

## E.3. `arcane` — 6 файлов

- `arcane/ArcaneRecipeBook`
- `arcane/ArcaneRecipeBookEntry`
- `arcane/ArcaneWorkbenchRecipe`
- `arcane/ArcaneWorkbenchRecipeManager`
- `arcane/ArcaneWorkbenchRecipes`
- `arcane/TC4ArcaneWorkbenchParity`

## E.4. `aura` — 9 файлов

- `aura/AuraNodeModifier`
- `aura/AuraNodeProfile`
- `aura/AuraNodeScan`
- `aura/AuraNodeType`
- `aura/AuraNodeWorldRuntime`
- `aura/AuraVisRelayNetwork`
- `aura/TC4AuraNodeScanParity`
- `aura/TC4NodeJarRuntime`
- `aura/TC4ThaumometerTargeting`

## E.5. `block` — 122 файлов

- `block/AddonCompletionLedgerItem`
- `block/AdvancedNodeStabilizerBlock`
- `block/AlchemicalCentrifugeBlock`
- `block/AlchemicalFurnaceBlock`
- `block/AlembicBlock`
- `block/ArcanePedestalBlock`
- `block/ArcaneWorkbenchBlock`
- `block/AspectCrystalItem`
- `block/AuraNodeBlock`
- `block/AvaritiaCreativeWandItem`
- `block/BellowsBlock`
- `block/BottomlessPouchItem`
- `block/CreativeThaumonomiconItem`
- `block/CrucibleBlock`
- `block/DeconstructionTableBlock`
- `block/EldritchAltarBlock`
- `block/EldritchCapBlock`
- `block/EldritchCrabSpawnerBlock`
- `block/EldritchCrystalBlock`
- `block/EldritchEyeItem`
- `block/EldritchLockBlock`
- `block/EldritchNothingBlock`
- `block/EldritchPortalBlock`
- `block/EldritchTrapBlock`
- `block/ElectricShockBlock`
- `block/EncodedEssentiaPatternItem`
- `block/EssentiaCellItem`
- `block/EssentiaCrystalItem`
- `block/EssentiaCrystalizerBlock`
- `block/EssentiaDriveBlock`
- `block/EssentiaJarBlock`
- `block/EssentiaPartitionCardItem`
- `block/EssentiaPhialItem`
- `block/EssentiaReservoirBlock`
- `block/EssentiaTubeBlock`
- `block/EssentiaUpgradeCardItem`
- `block/EssentiaValveBlock`
- `block/EtherealPlatformBlock`
- `block/ExperienceExtractorItem`
- `block/ExperienceShardItem`
- `block/FilteredEssentiaJarBlock`
- `block/FluxGasBlock`
- `block/FluxGooBlock`
- `block/FocalManipulatorBlock`
- `block/FocusPouchBaubleItem`
- `block/FocusPouchItem`
- `block/FumeDissipatorBlock`
- `block/GogglesOfRevealingItem`
- `block/GolemBellItem`
- `block/GolemCoreItem`
- `block/GolemDecorationItem`
- `block/GolemFilterItem`
- `block/GolemSealCollectItem`
- `block/GolemTaskMarkerItem`
- `block/GolemUpgradeItem`
- `block/HelmetOfRevealingItem`
- `block/IchorArmorItem`
- `block/IchorArmorMaterial`
- `block/IchorGearItem`
- `block/IchorPickaxeItem`
- `block/IchorSwordItem`
- `block/IchorToolTier`
- `block/InfusionMatrixAuxiliaryBlock`
- `block/InfusionMatrixBlock`
- `block/JarLabelItem`
- `block/KamiResearchCoreItem`
- `block/MatrixAuxiliaryBlock`
- `block/NitorItem`
- `block/NitorLightBlock`
- `block/NodeJarItem`
- `block/NodeStabilizerBlock`
- `block/NodeStabilizerItem`
- `block/NodeTransducerBlock`
- `block/OsmoticEnchantmentHelper`
- `block/PechLedgerItem`
- `block/PechTradeTokenItem`
- `block/PortingLedgerItem`
- `block/ResearchNoteItem`
- `block/ResearchPointItem`
- `block/ResearchTableBlock`
- `block/SanitySoapItem`
- `block/ScribingToolsItem`
- `block/ShardItem`
- `block/TC4CrimsonPlateArmorItem`
- `block/TC4CrimsonPlateArmorMaterial`
- `block/TC4FortressArmorItem`
- `block/TC4FortressArmorMaterial`
- `block/TC4FortressMaskItem`
- `block/TC4LootBlock`
- `block/TC4MagicalLeavesBlock`
- `block/TC4SaplingBlock`
- `block/TableBlock`
- `block/TaintBlock`
- `block/TaintFibresBlock`
- `block/TaintSeedItem`
- `block/TaintedSoilBlock`
- `block/TemporaryHoleBlock`
- `block/ThaumatoriumBlock`
- `block/ThaumcraftExtrasElementalBlock`
- `block/ThaumcraftExtrasFocusItem`
- `block/ThaumcraftExtrasParityBlock`
- `block/ThaumcraftExtrasParityItem`
- `block/ThaumicAeGridToolItem`
- `block/ThaumicEnergisticsCardItem`
- `block/ThaumicEnergisticsDeviceBlock`
- `block/ThaumicEnergisticsUtilityItem`
- `block/ThaumicTinkererDeviceBlock`
- `block/ThaumicTinkererParityBlock`
- `block/ThaumicTinkererParityItem`
- `block/ThaumicTinkererUtilityItem`
- `block/ThaumometerItem`
- `block/ThaumonomiconItem`
- `block/TransvectorBinderItem`
- `block/TransvectorInterfaceBlock`
- `block/VisRelayBlock`
- `block/VoidEssentiaJarBlock`
- `block/WandFocusItem`
- `block/WandItem`
- `block/WardedBlock`
- `block/WarpCharmItem`
- `block/WarpWardTalismanItem`
- `block/WirelessEssentiaTerminalItem`

## E.6. `blockentity` — 28 файлов

- `blockentity/AlchemicalCentrifugeBlockEntity`
- `blockentity/AlchemicalFurnaceBlockEntity`
- `blockentity/AlembicBlockEntity`
- `blockentity/ArcanePedestalBlockEntity`
- `blockentity/ArcaneWorkbenchBlockEntity`
- `blockentity/AuraNodeBlockEntity`
- `blockentity/CrucibleBlockEntity`
- `blockentity/DeconstructionTableBlockEntity`
- `blockentity/EldritchCapBlockEntity`
- `blockentity/EldritchCrabSpawnerBlockEntity`
- `blockentity/EldritchCrystalBlockEntity`
- `blockentity/EldritchLockBlockEntity`
- `blockentity/EldritchPortalBlockEntity`
- `blockentity/EldritchTrapBlockEntity`
- `blockentity/EssentiaCrystalizerBlockEntity`
- `blockentity/EssentiaDriveBlockEntity`
- `blockentity/EssentiaJarBlockEntity`
- `blockentity/EssentiaReservoirBlockEntity`
- `blockentity/EssentiaTubeBlockEntity`
- `blockentity/FocalManipulatorBlockEntity`
- `blockentity/InfusionMatrixBlockEntity`
- `blockentity/NodeStabilizerBlockEntity`
- `blockentity/NodeTransducerBlockEntity`
- `blockentity/ResearchTableBlockEntity`
- `blockentity/TemporaryHoleBlockEntity`
- `blockentity/ThaumatoriumBlockEntity`
- `blockentity/TransvectorInterfaceBlockEntity`
- `blockentity/WardedBlockEntity`

## E.7. `client` — 105 файлов

- `client/ClientAspectData`
- `client/ClientHooks`
- `client/ClientModEvents`
- `client/ClientResearchData`
- `client/ClientResearchNoteData`
- `client/ClientScanData`
- `client/ClientWandArchitectEvents`
- `client/ClientWandArchitectKeybinds`
- `client/EldritchOverlayEvents`
- `client/EssentiaOverlayEvents`
- `client/HelmetRevealingOverlayEvents`
- `client/InfusionOverlayEvents`
- `client/OriginalVisualStateBridge`
- `client/RunicShieldClientState`
- `client/RunicShieldOverlayEvents`
- `client/TC4AuraNodeHudParity`
- `client/TC4RevealerHudAdapter`
- `client/WandVisOverlayEvents`
- `client/arcane/ClientArcaneRecipePage`
- `client/arcane/ClientArcaneRecipeRegistry`
- `client/arcane/ClientSyncedArcaneRecipes`
- `client/book/ThaumonomiconRecipePage`
- `client/book/ThaumonomiconRecipeRegistry`
- `client/fx/TC4ClientChampionFx`
- `client/fx/TC4ClientEldritchBossFx`
- `client/fx/TC4ClientFocusFx`
- `client/fx/TC4ClientInfusionFx`
- `client/fx/TC4ClientRunicShieldFx`
- `client/render/AlchemicalCentrifugeRenderer`
- `client/render/AlembicRenderer`
- `client/render/ArcanePedestalRenderer`
- `client/render/AspectOrbRenderer`
- `client/render/AuraNodeRenderer`
- `client/render/CrucibleRenderer`
- `client/render/EssentiaCrystalizerRenderer`
- `client/render/EssentiaJarRenderer`
- `client/render/EssentiaReservoirRenderer`
- `client/render/EssentiaTubeRenderer`
- `client/render/FocalManipulatorRenderer`
- `client/render/InfusionMatrixRenderer`
- `client/render/NodeJarItemRenderer`
- `client/render/NodeStabilizerItemRenderer`
- `client/render/NodeStabilizerRenderer`
- `client/render/NodeTransducerRenderer`
- `client/render/PechRenderer`
- `client/render/ResearchTableRenderer`
- `client/render/RevealerAspectTagRenderer`
- `client/render/TC4BlockMobRenderer`
- `client/render/TC4CultistPortalRenderer`
- `client/render/TC4EldritchBossModelParity`
- `client/render/TC4EldritchCrabRenderer`
- `client/render/TC4EldritchGolemRenderer`
- `client/render/TC4EldritchGuardianRenderer`
- `client/render/TC4EldritchOrbRenderer`
- `client/render/TC4EldritchTileRenderProfile`
- `client/render/TC4EldritchTileRenderer`
- `client/render/TC4EldritchWardenRenderer`
- `client/render/TC4FireBatRenderer`
- `client/render/TC4FocusProjectileRenderer`
- `client/render/TC4FortressArmorLayer`
- `client/render/TC4FrostShardRenderer`
- `client/render/TC4GogglesLayer`
- `client/render/TC4GolemAccessoriesLayer`
- `client/render/TC4GolemCarriedItemLayer`
- `client/render/TC4GolemDamageLayer`
- `client/render/TC4MindSpiderRenderer`
- `client/render/TC4NodeRenderTypes`
- `client/render/TC4TaintacleGiantRenderer`
- `client/render/TC4TaintacleRenderer`
- `client/render/TaintCrawlerRenderer`
- `client/render/ThaumGolemRenderer`
- `client/render/ThaumometerItemRenderer`
- `client/render/WandItemRenderer`
- `client/render/WardedBlockRenderer`
- `client/render/model/TC4BakedEldritchModel`
- `client/render/model/TC4EldritchBossLayerDefinitions`
- `client/render/model/TC4EldritchCrabModel`
- `client/render/model/TC4FireBatModel`
- `client/render/model/TC4FrostShardModel`
- `client/render/model/TC4GolemAccessoriesModel`
- `client/render/model/TC4InfusionMatrixModel`
- `client/render/model/TC4NodeStabilizerModel`
- `client/render/model/TC4ThaumGolemModel`
- `client/screen/ArcaneWorkbenchContainerScreen`
- `client/screen/BottomlessPouchScreen`
- `client/screen/DeconstructionTableScreen`
- `client/screen/EssentiaDriveScreen`
- `client/screen/EssentiaTerminalScreen`
- `client/screen/FocalManipulatorScreen`
- `client/screen/FocusPouchScreen`
- `client/screen/GolemScreen`
- `client/screen/OriginalClientResearchSelection`
- `client/screen/OriginalGuiTextures`
- `client/screen/OriginalResearchCategory`
- `client/screen/OriginalResearchLayout`
- `client/screen/OsmoticEnchanterScreen`
- `client/screen/PechTradeScreen`
- `client/screen/ResearchNoteScreen`
- `client/screen/ResearchTableContainerScreen`
- `client/screen/TC4ResearchIconMap`
- `client/screen/TC4ResearchPageScreen`
- `client/screen/TC4ResearchText`
- `client/screen/ThaumatoriumScreen`
- `client/screen/ThaumonomiconScreen`
- `client/screen/TransvectorInterfaceScreen`

## E.8. `compat` — 1 файлов

- `compat/ThaumcraftCompatIds`

## E.9. `config` — 1 файлов

- `config/ThaumcraftConfig`

## E.10. `data` — 2 файлов

- `data/NodeScanData`
- `data/PlayerThaumData`

## E.11. `eldritch` — 23 файлов

- `eldritch/TC4EldritchBlockVariantAdapter`
- `eldritch/TC4EldritchLockBossSpawner`
- `eldritch/TC4EldritchProgression`
- `eldritch/TC4LootPotionEnchantAdapter`
- `eldritch/TC4OuterLandsBossCycleData`
- `eldritch/TC4OuterLandsBossRoomMetadata`
- `eldritch/TC4OuterLandsBossRoomPlacer`
- `eldritch/TC4OuterLandsChunkProviderBridge`
- `eldritch/TC4OuterLandsDecorationAdapter`
- `eldritch/TC4OuterLandsDimensionAdapter`
- `eldritch/TC4OuterLandsDimensionParity`
- `eldritch/TC4OuterLandsFeatureSelector`
- `eldritch/TC4OuterLandsGenCommonAdapter`
- `eldritch/TC4OuterLandsLivePopulateAdapter`
- `eldritch/TC4OuterLandsLootAdapter`
- `eldritch/TC4OuterLandsMazeCell`
- `eldritch/TC4OuterLandsMazeCellLoc`
- `eldritch/TC4OuterLandsMazeGenerator`
- `eldritch/TC4OuterLandsMazeHandler`
- `eldritch/TC4OuterLandsMazeSavedData`
- `eldritch/TC4OuterLandsPassageFeatureAdapter`
- `eldritch/TC4OuterLandsRoomAdapter`
- `eldritch/TC4OuterLandsTeleporter`

## E.12. `entity` — 25 файлов

- `entity/AspectOrbEntity`
- `entity/CrimsonCultistEntity`
- `entity/CultistPortalEntity`
- `entity/EldritchCrabEntity`
- `entity/EldritchGolemEntity`
- `entity/EldritchGuardianEntity`
- `entity/EldritchWardenEntity`
- `entity/MindSpiderEntity`
- `entity/PechEntity`
- `entity/TC4FireBatEntity`
- `entity/TC4ThaumcraftBossEntity`
- `entity/TaintCrawlerEntity`
- `entity/TaintacleEntity`
- `entity/TaintacleGiantEntity`
- `entity/TaintacleSmallEntity`
- `entity/ThaumGolemEntity`
- `entity/projectile/TC4EldritchOrbEntity`
- `entity/projectile/TC4EmberEntity`
- `entity/projectile/TC4ExplosiveOrbEntity`
- `entity/projectile/TC4FocusProjectileEntity`
- `entity/projectile/TC4FrostShardEntity`
- `entity/projectile/TC4GolemOrbEntity`
- `entity/projectile/TC4PechBlastEntity`
- `entity/projectile/TC4PrimalOrbEntity`
- `entity/projectile/TC4ShockOrbEntity`

## E.13. `essentia` — 9 файлов

- `essentia/EssentiaBackflowResult`
- `essentia/EssentiaSuction`
- `essentia/EssentiaSuctionPath`
- `essentia/EssentiaSuctionResolver`
- `essentia/EssentiaTubeConnections`
- `essentia/EssentiaTubeSubtype`
- `essentia/TC4DistillationRuntime`
- `essentia/TC4EssentiaNetworkRuntime`
- `essentia/TC4ItemTransferRuntime`

## E.14. `event` — 3 файлов

- `event/CommonEvents`
- `event/EldritchItemEvents`
- `event/WarpEvents`

## E.15. `golem` — 10 файлов

- `golem/GolemBellMarkerRuntime`
- `golem/GolemBellMode`
- `golem/GolemCoreType`
- `golem/GolemDecorationType`
- `golem/GolemItemHandlerContainerAdapter`
- `golem/GolemMarkerMode`
- `golem/GolemMaterial`
- `golem/GolemOriginalRuntime`
- `golem/GolemTaskAIRuntime`
- `golem/GolemUpgradeType`

## E.16. `infusion` — 18 файлов

- `infusion/InfusionAltarStructure`
- `infusion/InfusionInstabilityEvents`
- `infusion/InfusionMatrixAuxiliaryHelper`
- `infusion/InfusionProcessHelper`
- `infusion/InfusionRecipe`
- `infusion/InfusionRecipeManager`
- `infusion/InfusionRecipes`
- `infusion/InfusionStructureReport`
- `infusion/MatrixAuxiliaryReport`
- `infusion/TC4InfusionCraftCycleParity`
- `infusion/TC4InfusionEnchantmentAdapter`
- `infusion/TC4InfusionEnchantmentIndex`
- `infusion/TC4InfusionFailureParity`
- `infusion/TC4InfusionItemMatcher`
- `infusion/TC4InfusionRunicAugmentAdapter`
- `infusion/TC4InfusionRuntime`
- `infusion/TC4InfusionStabilityParity`
- `infusion/TC4RunicArmorHelper`

## E.17. `item` — 3 файлов

- `item/TC4GolemCoreComponentItem`
- `item/TC4GolemPlacerItem`
- `item/TC4ResearchComponentItem`

## E.18. `jar` — 1 файлов

- `jar/JarTubeInteractionRuntime`

## E.19. `menu` — 16 файлов

- `menu/ArcaneWorkbenchMenu`
- `menu/BottomlessPouchContainer`
- `menu/BottomlessPouchMenu`
- `menu/DeconstructionTableMenu`
- `menu/EssentiaDriveMenu`
- `menu/EssentiaTerminalMenu`
- `menu/FocalManipulatorMenu`
- `menu/FocusPouchContainer`
- `menu/FocusPouchMenu`
- `menu/GolemInventoryContainer`
- `menu/GolemMenu`
- `menu/OsmoticEnchanterMenu`
- `menu/PechTradeMenu`
- `menu/ResearchTableMenu`
- `menu/ThaumatoriumMenu`
- `menu/TransvectorInterfaceMenu`

## E.20. `network` — 38 файлов

- `network/ArcaneRecipeSyncPacket`
- `network/AspectKnowledgeSyncPacket`
- `network/OpenResearchNotePacket`
- `network/OpenResearchTablePacket`
- `network/PacketFXBlockZap`
- `network/PacketFXChampion`
- `network/PacketFXEldritchBoss`
- `network/PacketFXInfusionSource`
- `network/PacketFXShield`
- `network/PacketRunicCharge`
- `network/RequestClearResearchNoteSlotPacket`
- `network/RequestCombineAspectsPacket`
- `network/RequestCompleteSelectedResearchPacket`
- `network/RequestEssentiaDriveScanPacket`
- `network/RequestEssentiaInventoryScanPacket`
- `network/RequestEssentiaTerminalFilteredScanPacket`
- `network/RequestEssentiaTerminalScanPacket`
- `network/RequestFocusChangePacket`
- `network/RequestOsmoticEnchantPacket`
- `network/RequestOsmoticStructureCheckPacket`
- `network/RequestPechGiftPacket`
- `network/RequestPechTradePacket`
- `network/RequestPlaceResearchNoteAspectPacket`
- `network/RequestResearchTableActionPacket`
- `network/RequestResearchUnlockPacket`
- `network/RequestSelectResearchPacket`
- `network/RequestSolveResearchNotePacket`
- `network/RequestThaumatoriumFormulaPacket`
- `network/RequestThaumometerScanPacket`
- `network/RequestTransvectorActionPacket`
- `network/RequestTransvectorClearPacket`
- `network/RequestTransvectorInspectPacket`
- `network/RequestTransvectorStatusPacket`
- `network/RequestWandArchitectTogglePacket`
- `network/ResearchNoteSyncPacket`
- `network/ResearchSyncPacket`
- `network/ScanKnowledgeSyncPacket`
- `network/ThaumcraftNetwork`

## E.21. `porting` — 16 файлов

- `porting/Provided1192BaseMap`
- `porting/TC4AspectBridge`
- `porting/TC4ClientFocusFxBridge`
- `porting/TC4FullParityIndex`
- `porting/TC4FullPortDriftLedger`
- `porting/TC4LegacyDuplicateItemMigrator`
- `porting/TC4OriginalAssetIndex`
- `porting/TC4OriginalBlockMap`
- `porting/TC4OriginalItemMap`
- `porting/TC4OriginalResearchMap`
- `porting/TC4OriginalWandComponentMap`
- `porting/TC4RegistryGarbageGuard`
- `porting/TC4ResearchItems`
- `porting/TC4Sounds`
- `porting/TC4SourceMap`
- `porting/TC4Stage139WholePortSweep`

## E.22. `recipe` — 4 файлов

- `recipe/CountedSmeltingRecipeSerializer`
- `recipe/TC4RecipeItemResolver`
- `recipe/TC4RecipeRequirementIndex`
- `recipe/TC4RecipeRuntimeBridge`

## E.23. `research` — 23 файлов

- `research/OriginalArcaneCostBridge`
- `research/OriginalAspectWallet`
- `research/OriginalResearchBridge`
- `research/OriginalResearchProgression`
- `research/OriginalResearchSelection`
- `research/PlayerAspectKnowledge`
- `research/ResearchAspectGraph`
- `research/ResearchEntry`
- `research/ResearchLocks`
- `research/ResearchNoteGrid`
- `research/ResearchNoteRequirements`
- `research/ResearchNoteSolver`
- `research/ResearchNoteState`
- `research/ResearchRegistry`
- `research/ResearchTableBonusRuntime`
- `research/ResearchTableFoundation`
- `research/ResearchTableInventoryRuntime`
- `research/TC4ResearchCategoryRegistry`
- `research/TC4ResearchFlagPolicy`
- `research/TC4ResearchMetadataIndex`
- `research/TC4ResearchNoteCreator`
- `research/TC4ResearchRuntimeBridge`
- `research/TC4ResearchTableParity`

## E.24. `runic` — 6 файлов

- `runic/TC4BaubleSlotAdapter`
- `runic/TC4ChampionModifierRuntime`
- `runic/TC4FortressArmorRuntime`
- `runic/TC4FortressMaskRuntime`
- `runic/TC4RunicShieldRuntime`
- `runic/TC4WarpingGearAdapter`

## E.25. `source` — 2 файлов

- `source/TC4EntityAspectRegistry`
- `source/TC4ObjectAspectRegistry`

## E.26. `taint` — 1 файлов

- `taint/TaintSpreadRuntime`

## E.27. `thaumicenergistics` — 4 файлов

- `thaumicenergistics/ThaumicAeGrid`
- `thaumicenergistics/ThaumicAeGridReport`
- `thaumicenergistics/ThaumicEnergisticsNetwork`
- `thaumicenergistics/ThaumicEnergisticsRecipeBook`

## E.28. `wand` — 14 файлов

- `wand/EqualTradeSwapRuntime`
- `wand/FocusArchitectRuntime`
- `wand/FocusUpgradeRuntime`
- `wand/FocusUpgradeType`
- `wand/TC4ConfigRecipesWandIndex`
- `wand/TC4VisDiscountGear`
- `wand/TC4VisDiscountRuntime`
- `wand/WandCapType`
- `wand/WandComponentData`
- `wand/WandCraftingRuntime`
- `wand/WandFocusRuntime`
- `wand/WandFocusType`
- `wand/WandManagerRuntime`
- `wand/WandRodType`

## E.29. `ward` — 1 файлов

- `ward/WardedBlockRuntime`

## E.30. `world` — 6 файлов

- `world/MagicalForestWorldgenDiagnostics`
- `world/MagicalForestWorldgenInstaller`
- `world/TC4Biomes`
- `world/TC4TreeGenerator`
- `world/TC4WorldgenRuntime`
- `world/TC4WorldgenSavedData`

# Приложение F. RegistryObject item constants

Ниже перечислены 166 item-ориентированных `RegistryObject`-поля, обнаруженных в `ThaumcraftMod.java`. Список включает пользовательские предметы, block items и compatibility carriers.

- `ADDON_COMPLETION_LEDGER`
- `THAUMONOMICON`
- `THAUMONOMICON_CHEAT`
- `PORTING_LEDGER`
- `NODE_JAR`
- `IRON_CAPPED_WOODEN_WAND`
- `GREATWOOD_WAND`
- `SILVERWOOD_WAND`
- `AVARITIA_CREATIVE_WAND`
- `THAUMOMETER`
- `GOGGLES_OF_REVEALING`
- `RESEARCH_NOTE`
- `ESSENTIA_CRYSTAL`
- `RESEARCH_POINT`
- `SCRIBING_TOOLS`
- `GOLD_WAND_CAP`
- `WOODEN_WAND_CORE`
- `SILVERWOOD_WAND_CORE`
- `AER_SHARD`
- `TERRA_SHARD`
- `IGNIS_SHARD`
- `AQUA_SHARD`
- `ORDO_SHARD`
- `PERDITIO_SHARD`
- `BALANCED_SHARD`
- `VITREUS_SHARD`
- `METALLUM_SHARD`
- `PRAECANTATIO_SHARD`
- `VACUOS_SHARD`
- `HERBA_SHARD`
- `LUX_SHARD`
- `POTENTIA_SHARD`
- `OSMOTIC_ENCHANTMENT_FOCUS`
- `TRANSVECTOR_BINDER`
- `TT_FOCUS_TELEKINESIS`
- `TT_FOCUS_DEFLECT`
- `TT_FOCUS_XP_DRAIN`
- `TT_FOCUS_SHADOWBEAM`
- `TT_PROTOCLAY`
- `TT_PLACEMENT_MIRROR`
- `TT_CLEANSING_TALISMAN`
- `TT_INFUSED_INKWELL`
- `TT_GAS_REMOVER`
- `TT_MOB_DISPLAY`
- `TT_DARK_QUARTZ`
- `TT_INFUSED_GRAIN`
- `TT_ICHOR_AXE`
- `TT_ICHOR_GEM_HELM`
- `TT_ICHOR_GEM_LEGS`
- `TT_ICHOR_WAND_ROD`
- `TT_BRIGHT_NITOR`
- `TCE_MAGIC_WRENCH`
- `TCE_DARK_THAUMIUM_SHOVEL`
- `TCE_WAND_CAP`
- `TCE_COLOR_POUCH`
- `TCE_INFO_BOOK`
- `TCE_DARK_CRYSTAL`
- `TCE_DARK_NUGGET`
- `TCE_API_SHARD`
- `TOME_OF_KNOWLEDGE_SHARING`
- `INFUSED_SCRIBING_TOOLS`
- `BOTTOMLESS_POUCH`
- `HELMET_OF_REVEALING`
- `ICHORCLOTH`
- `KAMI_RESEARCH_CORE`
- `ICHOR_PICKAXE`
- `ICHOR_SWORD`
- `ICHORCLOTH_HOOD`
- `ICHORCLOTH_ROBE`
- `ICHORCLOTH_LEGGINGS`
- `ICHORCLOTH_BOOTS`
- `ASPECT_AER`
- `ASPECT_TERRA`
- `ASPECT_IGNIS`
- `ASPECT_AQUA`
- `ASPECT_ORDO`
- `ASPECT_PERDITIO`
- `ASPECT_VITREUS`
- `ASPECT_METALLUM`
- `ASPECT_PRAECANTATIO`
- `THAUMIUM_NUGGET`
- `VOID_METAL_INGOT`
- `QUICKSILVER_DROP`
- `NITOR`
- `ESSENTIA_PHIAL`
- `FLUX_CRYSTAL`
- `UNSTABLE_SINGULARITY`
- `WARP_CHARM`
- `WARP_WARD_TALISMAN`
- `SANITY_SOAP`
- `ELDRITCH_EYE`
- `AWAKENED_CRIMSON_KEY`
- `CRIMSON_PLATE_HELM`
- `CRIMSON_PLATE_CHEST`
- `CRIMSON_PLATE_LEGS`
- `CRIMSON_PLATE_BOOTS`
- `ELDRITCH_GUARDIAN_CORE`
- `GOLEM_CORE`
- `GOLEM_BELL`
- `GOLEM_SEAL_COLLECT`
- `GOLEM_TASK_MARKER`
- `GOLEM_FILTER`
- `GOLEM_UPGRADE_AIR`
- `GOLEM_UPGRADE_FIRE`
- `GOLEM_UPGRADE_WATER`
- `GOLEM_UPGRADE_EARTH`
- `GOLEM_UPGRADE_ORDER`
- `GOLEM_UPGRADE_ENTROPY`
- `GOLEM_DECO_ARMOR`
- `GOLEM_DECO_TOP_HAT`
- `GOLEM_DECO_FEZ`
- `GOLEM_DECO_VISOR`
- `GOLEM_DECO_GLASSES`
- `GOLEM_DECO_BOWTIE`
- `GOLEM_DECO_DART_LAUNCHER`
- `GOLEM_DECO_MACE`
- `GOLEM_WIRELESS_BACKPACK`
- `JAR_LABEL`
- `TAINT_SEED`
- `FOCUS_FROST`
- `FOCUS_EXCAVATION`
- `FOCUS_EQUAL_TRADE`
- `FOCUS_PRIMAL`
- `FOCUS_POUCH`
- `FOCUS_ARROW`
- `FOCUS_SPEED`
- `FOCUS_EXPERIENCE`
- `FOCUS_EXCHANGE`
- `FOCUS_DISPEL`
- `FOCUS_FREEZE`
- `PECH_TRADE_TIER_2`
- `PECH_TRADE_TIER_4`
- `IGNIS_FUEL`
- `EXPERIENCE_SHARD`
- `EXPERIENCE_EXTRACTOR`
- `PECH_LEDGER`
- `ESSENTIA_STORAGE_COMPONENT_1K`
- `ESSENTIA_STORAGE_COMPONENT_16K`
- `ENCODED_ESSENTIA_PATTERN`
- `WIRELESS_ESSENTIA_TERMINAL`
- `ESSENTIA_PARTITION_CARD`
- `ESSENTIA_VIEW_CARD`
- `ESSENTIA_SPEED_CARD`
- `ADVANCED_ESSENTIA_SPEED_CARD`
- `ESSENTIA_ACCELERATION_CARD`
- `THAUMIC_COPROCESSOR_CARD`
- `ESSENTIA_FUZZY_CARD`
- `DIGITAL_ESSENTIA_CELL_1K`
- `DIGITAL_ESSENTIA_CELL_4K`
- `DIGITAL_ESSENTIA_CELL_16K`
- `DIGITAL_ESSENTIA_CELL_64K`
- `CREATIVE_ESSENTIA_CELL`
- `ESSENTIA_STORAGE_COMPONENT_64K`
- `ESSENTIA_CELL_CASING`
- `FOCUS_AE_WRENCH`
- `KNOWLEDGE_CORE`
- `COALESCENCE_CORE`
- `DIFFUSION_CORE`
- `IRON_GEAR`
- `CRAFTING_ASPECT`
- `THAUMIC_GRID_TOOL`
- `THAUMIC_CHANNEL_CORE`
- `ELDRITCH_CAP_ITEM`
- `ELDRITCH_LOCK_ITEM`
- `ELDRITCH_TRAP_ITEM`
- `ELDRITCH_CRYSTAL_ITEM`

# Приложение G. RegistryObject block constants

Ниже перечислены **168** фактических `RegistryObject<Block>`-поля, заново извлечённых из `ThaumcraftMod.java`. Имена `BlockEntityType` в этот список не включаются.

- `ARCANE_STONE`
- `ARCANE_STONE_BRICKS`
- `INFUSION_PILLAR`
- `ELDRITCH_STONE`
- `ELDRITCH_OBELISK`
- `ELDRITCH_ALTAR`
- `ELDRITCH_PORTAL`
- `ELDRITCH_NOTHING`
- `ELDRITCH_CAP`
- `ELDRITCH_LOCK`
- `ELDRITCH_TRAP`
- `ELDRITCH_CRYSTAL`
- `ELDRITCH_CRUST`
- `ELDRITCH_DECORATIVE`
- `ELDRITCH_DOOR`
- `ELDRITCH_CRAB_SPAWNER`
- `OUTER_LANDS_LOOT_URN`
- `OUTER_LANDS_LOOT_CRATE`
- `AMBER_ORE`
- `CINNABAR_ORE`
- `TAINTED_SOIL`
- `TAINT_CRUST`
- `TAINT_SOIL`
- `FLESH_BLOCK`
- `TAINT_FIBRES`
- `FLUX_GOO`
- `FLUX_GAS`
- `TEMPORARY_HOLE`
- `WARDED_BLOCK`
- `ELECTRIC_SHOCK`
- `GREATWOOD_LOG`
- `SILVERWOOD_LOG`
- `GREATWOOD_LEAVES`
- `SILVERWOOD_LEAVES`
- `GREATWOOD_SAPLING`
- `SILVERWOOD_SAPLING`
- `GREATWOOD_PLANKS`
- `SILVERWOOD_PLANKS`
- `TABLE`
- `RESEARCH_TABLE`
- `DECONSTRUCTION_TABLE`
- `ARCANE_WORKBENCH`
- `FOCAL_MANIPULATOR`
- `CRUCIBLE`
- `BELLOWS`
- `ESSENTIA_JAR`
- `FILTERED_ESSENTIA_JAR`
- `VOID_ESSENTIA_JAR`
- `ESSENTIA_RESERVOIR`
- `ALEMBIC`
- `ALCHEMICAL_CENTRIFUGE`
- `ESSENTIA_CRYSTALIZER`
- `ESSENTIA_TUBE`
- `ESSENTIA_TUBE_FILTER`
- `ESSENTIA_TUBE_RESTRICT`
- `ESSENTIA_TUBE_ONEWAY`
- `ESSENTIA_TUBE_BUFFER`
- `ESSENTIA_VALVE`
- `ALCHEMICAL_FURNACE`
- `ADVANCED_ALCHEMICAL_FURNACE`
- `THAUMATORIUM`
- `MNEMONIC_MATRIX`
- `EXTRAS_FIRE_BLOCK`
- `EXTRAS_AIR_BLOCK`
- `EXTRAS_WATER_BLOCK`
- `EXTRAS_EARTH_BLOCK`
- `EXTRAS_LIGHT_BLOCK`
- `EXTRAS_ENDER_BLOCK`
- `RESEARCH_CACHE_BLOCK`
- `ESSENTIA_TERMINAL`
- `ESSENTIA_STORAGE_BUS`
- `ESSENTIA_IMPORT_BUS`
- `ESSENTIA_EXPORT_BUS`
- `ESSENTIA_INTERFACE`
- `ARCANE_PATTERN_ENCODER`
- `ARCANE_PATTERN_PROVIDER`
- `OBSIDIAN_TILE`
- `OBSIDIAN_TOTEM`
- `NITOR_LIGHT`
- `TT_MOB_MAGNET`
- `TT_REPAIRER`
- `TT_ASPECT_ANALYZER`
- `TT_ENCHANTER`
- `TT_GOLEM_CONNECTOR`
- `TT_FUNNEL`
- `TT_FORCEFIELD`
- `TT_SUMMON_TABLET`
- `TT_REMOTE_PLACER`
- `TT_MOBILIZER`
- `TT_MOBILIZER_RELAY`
- `TT_TRANSVECTOR_DISLOCATOR`
- `TT_WARP_GATE`
- `TT_BEDROCK_PORTAL`
- `TT_DARK_QUARTZ_BLOCK`
- `TT_GASEOUS_LIGHT`
- `TT_GASEOUS_SHADOW`
- `TT_NITOR_GAS`
- `TT_FIRE_AIR`
- `TT_FIRE_WATER`
- `TT_FIRE_EARTH`
- `TT_FIRE_ORDER`
- `TT_FIRE_CHAOS`
- `TT_ANIMATION_TABLET`
- `TT_INFUSED_FARMLAND`
- `TCE_CHARGER`
- `TCE_EXCHANGER`
- `TCE_DARK_INFUSER`
- `TCE_MAGIC_GENERATOR`
- `TCE_MAGIC_SOLAR_PANEL`
- `TCE_MAGIC_CHARGER`
- `TCE_TELEPORTER`
- `TCE_TESLA`
- `TCE_COLOR_BLOCK`
- `TCE_HIDDEN_WARDED`
- `TCE_OPEN_WARDED`
- `TCE_WARDED_GLASS`
- `TCE_WARDED_PILLAR`
- `TCE_WARDED_SLAB`
- `TCE_WARDED_WALL`
- `TCE_WARDED_CARPET`
- `TCE_WARDED_COVER`
- `TCE_CACTUS`
- `TCE_DARK_SILVERWOOD`
- `TCE_DARK_SILVERWOOD_PLANKS`
- `TCE_IGNIS_FUEL_BLOCK`
- `TCE_INFUSION_INFO`
- `TCE_LAVA_BLOCK`
- `TCE_CABLE`
- `TCE_CLEAR_GLASS`
- `OSMOTIC_ENCHANTER`
- `ETHEREAL_PLATFORM`
- `FUME_DISSIPATOR`
- `TRANSVECTOR_INTERFACE`
- `ESSENTIA_DRIVE`
- `ESSENTIA_STORAGE_MONITOR`
- `ARCANE_ASSEMBLER`
- `ESSENTIA_PROVIDER`
- `INFUSION_PROVIDER`
- `ESSENTIA_CELL_WORKBENCH`
- `DISTILLATION_PATTERN_ENCODER`
- `ESSENTIA_VIBRATION_CHAMBER`
- `GEAR_BOX`
- `GOLEM_GEAR_BOX`
- `KNOWLEDGE_INSCRIBER`
- `ARCANE_CRAFTING_TERMINAL`
- `ESSENTIA_LEVEL_EMITTER`
- `ESSENTIA_CONVERSION_MONITOR`
- `VIS_INTERFACE`
- `THAUMIC_ME_CONTROLLER`
- `THAUMIC_ME_CABLE`
- `THAUMIC_CRAFTING_CPU`
- `THAUMIC_ENERGY_ACCEPTOR`
- `ARCANE_PEDESTAL`
- `INFUSION_MATRIX`
- `MATRIX_ACCELERATOR`
- `MATRIX_STABILIZER`
- `GOLEM_SEAL_COLLECT_BLOCK`
- `AURA_NODE`
- `NODE_STABILIZER`
- `ADVANCED_NODE_STABILIZER`
- `NODE_TRANSDUCER`
- `VIS_RELAY`
- `AER_CRYSTAL`
- `TERRA_CRYSTAL`
- `IGNIS_CRYSTAL`
- `AQUA_CRYSTAL`
- `ORDO_CRYSTAL`
- `PERDITIO_CRYSTAL`

# Приложение H. Маркеры незавершённости в исходниках

## H.1. `placeholder` — 46 упоминаний в 29 файлах

- `block/NodeStabilizerItem.java`
- `block/ThaumcraftExtrasFocusItem.java`
- `block/ThaumicEnergisticsUtilityItem.java`
- `block/ThaumicTinkererParityItem.java`
- `block/ThaumicTinkererUtilityItem.java`
- `block/ThaumometerItem.java`
- `eldritch/TC4EldritchBlockVariantAdapter.java`
- `eldritch/TC4EldritchLockBossSpawner.java`
- `eldritch/TC4OuterLandsDecorationAdapter.java`
- `eldritch/TC4OuterLandsGenCommonAdapter.java`
- `entity/CultistPortalEntity.java`
- `infusion/TC4InfusionFailureParity.java`
- `porting/TC4OriginalBlockMap.java`
- `porting/TC4RegistryGarbageGuard.java`
- `porting/TC4ResearchItems.java`
- `porting/TC4Sounds.java`
- `research/TC4ResearchFlagPolicy.java`
- `research/TC4ResearchNoteCreator.java`
- `research/TC4ResearchTableParity.java`
- `world/TC4WorldgenRuntime.java`
- `client/book/ThaumonomiconRecipeRegistry.java`
- `client/render/NodeStabilizerRenderer.java`
- `client/render/TC4EldritchGuardianRenderer.java`
- `client/render/TC4EldritchTileRenderer.java`
- `client/render/TC4MindSpiderRenderer.java`
- `client/render/TC4TaintacleRenderer.java`
- `client/render/ThaumometerItemRenderer.java`
- `client/render/WandItemRenderer.java`
- `client/screen/TC4ResearchPageScreen.java`

## H.2. `approximation` — 6 упоминаний в 5 файлах

- `world/TC4WorldgenRuntime.java`
- `client/fx/TC4ClientChampionFx.java`
- `client/fx/TC4ClientFocusFx.java`
- `client/fx/TC4ClientRunicShieldFx.java`
- `client/render/ResearchTableRenderer.java`

## H.3. `stub` — 60 упоминаний в 6 файлах

- `blockentity/EssentiaTubeBlockEntity.java`
- `essentia/EssentiaSuctionResolver.java`
- `research/TC4ResearchFlagPolicy.java`
- `research/TC4ResearchMetadataIndex.java`
- `research/TC4ResearchRuntimeBridge.java`
- `world/TC4TreeGenerator.java`

Маркеры необходимо интерпретировать вручную: часть слов встречается в комментариях о уже удалённых placeholder-реализациях или в compatibility guards. Они не означают автоматически, что весь файл неработоспособен.

# Приложение I. Машинные аудиты, включённые в source snapshot

- `reports/aura_node_parity_audit_v11.62.54.json`
- `reports/bewlr_contract_audit_v11.62.54.json`
- `reports/item_visual_audit_v11.62.54.json`
- `reports/model_transform_audit_v11.62.54.json`
- `reports/registry_audit_v11.62.54.json`
- `reports/release_jar_audit_v11.62.54.json`

# Приложение J. Основные исходные точки входа для ревью

| Область | Файл |
| --- | --- |
| Bootstrap и реестры | `src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java` |
| Клиентская регистрация | `client/ClientModEvents.java` |
| Сеть | `network/ThaumcraftNetwork.java` |
| Аспекты игрока | `research/PlayerAspectKnowledge.java` |
| Research materialization | `research/TC4ResearchRuntimeBridge.java` |
| Таумометр | `block/ThaumometerItem.java` |
| Жезл и vis | `block/WandItem.java` |
| Смена фокуса / D-001 | `wand/WandManagerRuntime.java` |
| Узел ауры | `blockentity/AuraNodeBlockEntity.java` |
| Рендер узла | `client/render/AuraNodeRenderer.java` |
| Arcane Workbench | `blockentity/ArcaneWorkbenchBlockEntity.java` и `arcane/ArcaneWorkbenchRecipe.java` |
| Essentia transport | `blockentity/EssentiaTubeBlockEntity.java` |
| Infusion state machine | `blockentity/InfusionMatrixBlockEntity.java` |
| Golem AI | `entity/ThaumGolemEntity.java` |
| Worldgen | `world/TC4WorldgenRuntime.java` |
| Research browser | `client/screen/ThaumonomiconScreen.java` |
| Research pages | `client/screen/TC4ResearchPageScreen.java` |
