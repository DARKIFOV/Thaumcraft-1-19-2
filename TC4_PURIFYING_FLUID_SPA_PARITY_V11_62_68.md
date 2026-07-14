# TC4 Purifying Fluid / Bath Salts / Arcane Spa parity — 11.62.68

Статус: **STATIC PASS / COMPILE AND RUNTIME NOT TESTED**.

## 1. Источники эталона TC4 4.2.3.5

| Узел | Исходный файл | Ключевой контракт |
|---|---|---|
| Bath Salts | `thaumcraft/common/items/ItemBathSalts.java:35–38` | выброшенный предмет живёт 200 тиков |
| Растворение соли | `thaumcraft/common/lib/events/EventHandlerEntity.java:725–739` | после истечения срока соль заменяет только источник обычной воды на `blockFluidPure` |
| Purifying Fluid | `thaumcraft/common/blocks/BlockFluidPure.java:47–92` | источник выдаёт Warp Ward по permanent Warp, удаляется, создаёт белые пузырьки и редкий `lavapop` |
| Arcane Spa | `thaumcraft/common/tiles/TileSpa.java` | 5000 mB, один слот соли, режим mix, период 40 тиков, redstone stop, размещение жидкости в зоне 5×5 |
| GUI | `thaumcraft/client/gui/GuiSpa.java`; `thaumcraft/common/container/ContainerSpa.java` | слот соли, шкала жидкости, кнопка mix/dispense |
| Рецепты | `thaumcraft/common/config/ConfigRecipes.java:604,879` | Bath Salts — crucible; Arcane Spa — точный arcane shaped `QIQ/SJS/SPS` |

## 2. Сопоставление реализации

| Поведение оригинала | Реализация 11.62.68 | Статус |
|---|---|---|
| `ItemBathSalts#getEntityLifespan = 200` | `BathSaltsItem.DISSOLVE_TICKS = 200` | PASS статически |
| Соль превращает только source-блок ванильной воды | `CommonEvents.onBathSaltsExpire` + `ItemExpireEvent` | PASS статически |
| Реальная жидкость, source/flowing, ведро и блок | `FluidType`, две `ForgeFlowingFluid`, `PurifyingFluidBlock`, `tc4_bucket_pure` | PASS статически |
| Светимость 10, вязкость 1000, редкость rare | свойства `PURIFYING_FLUID_TYPE` и блока | PASS статически |
| Warp Ward: `div=max(1,floor(sqrt(permanentWarp)))`; `min(32000,200000/div)` | `PurifyingFluidBlock#entityInside` | PASS статически |
| Эффект выдаёт только source и расходует его | проверка `isSource()` и замена блока воздухом | PASS статически |
| Белый пузырь + `lavapop` с шансом 1/25 и pitch 0.9–1.05 | `PurifyingFluidBlock#animateTick` | PASS статически |
| Spa: tank 5000 mB + один слот Bath Salts | `ArcaneSpaBlockEntity` | PASS статически |
| Проверка раз в 40 тиков, redstone отключает | `serverTick` | PASS статически |
| Mix: 1000 mB vanilla water + 1 salt → Purifying Fluid | `tryDispense` | PASS статически |
| Dispense: размещение другой пригодной жидкости | `mixing=false` ветка | PASS статически |
| Центр либо расширение по поддерживаемой зоне 5×5 | `findOutput`, диапазоны `-2..2`, соседний source | PASS статически |
| Вода не размещается в ultra-warm измерениях | `dimensionType().ultraWarm()` | PASS статически |
| Автоматизация не через верхнюю грань | item/fluid capabilities при `side != UP` | PASS статически |
| NBT и сетевой update для mix/tank/salts | `saveAdditional`, `load`, update packet | PASS статически |
| Исходная текстура жидкости и модели Spa | ресурсы `fluidpure`, `spa_side`, `spa_top`, `pedestal_top` | PASS статически |
| Sanity Soap получает исходный бонус в Purifying Fluid | существующий `SanitySoapItem#isPurifyingFluid` теперь разрешает реальный блок | PASS статически |

## 3. Исправление Arcane Spa recipe

В 11.62.67 JSON `tc4_arcanespa.json` был фактически нерабочим и не соответствовал строке 879 оригинала:

- `catalyst` указывал на обсидиан, которого нет в шаблоне;
- в `key` отсутствовали `P` и `J`;
- значения `Q`, `I`, `S` были сдвинуты;
- из-за отдельной проверки catalyst рецепт не мог корректно совпасть с сеткой.

В 11.62.68 восстановлена точная таблица:

| Символ | Оригинал | Порт |
|---|---|---|
| `P` | piston | `minecraft:piston` |
| `J` | warded jar meta 0 | `thaumcraft:essentia_jar` |
| `S` | arcane stone block meta 6 | `thaumcraft:tc4_block_arcane_stone` |
| `Q` | quartz block | `minecraft:quartz_block` |
| `I` | iron bars | `minecraft:iron_bars` |

Шаблон: `QIQ / SJS / SPS`. Стоимость: `AQUA 16 + ORDO 8 + TERRA 4`. Исследование: `ARCANESPA`.

Рецепт Bath Salts сохранён как исходная crucible-регистрация: `tc4_dust` и `COGNITIO/AURAM/ORDO/SANO` по 6. Оба рецепта уже загружаются существующими менеджерами Arcane/Alchemy и передаются в соответствующие категории JEI.

## 4. Осознанная временная адаптация

Оригинальный `GuiSpa` и `ContainerSpa` пока не перенесены. Чтобы функциональность можно было проверить после сборки, добавлено временное прямое управление блоком:

- Shift + ПКМ переключает mix/dispense;
- Bath Salts в руке вставляет одну соль;
- fluid-container interaction заполняет или опустошает tank через Forge Fluid API;
- пустая рука забирает соль.

Это **не считается полной GUI parity**. Следующая визуально-функциональная задача для этой подсистемы — отдельные Menu/Screen, исходная `gui_spa.png`, шкала 0–5000 mB, слот соли и синхронизированная кнопка режима со звуком `cameraclack`.

## 5. Статический аудит

`tools/tc4_116268_purifying_spa_guard.py` проверяет 32 контракта. Результат: **32/32 PASS**.

Дополнительно:

- JSON resources: **1910**, ошибок 0;
- Java files: **561**;
- item models: **716**, missing/unresolved 0;
- model transforms: 0 проблем, 0 предупреждений;
- BEWLR contracts: 11/11;
- registry/resource audit: 0 проблем;
- точная таблица символов Arcane Spa: PASS.

## 6. Что не доказано

- `compileJava` не стартовал: Gradle Wrapper не смог разрешить `services.gradle.org` и скачать Gradle 7.5.1.
- Не проверены в клиенте fluid rendering, анимация `fluidpure.png.mcmeta`, bucket placement/pickup и частицы.
- Не проверены gameplay и сохранение Arcane Spa после перезапуска мира.
- Не проверены hopper/pipe automation и block-entity synchronization на выделенном сервере.
- Оригинальный GUI отсутствует.

Итоговая оценка подсистемы: **S PASS; G/V/N NOT TESTED; C STATIC PASS; общий статус PARTIAL**.
