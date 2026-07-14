# TC4 Warp runtime parity — 11.62.67

Статус: **STATIC PASS / RUNTIME NOT TESTED**.

## Сопоставление с TC4 4.2.3.5

| Узел оригинала | Реализация 11.62.67 | Статус |
|---|---|---|
| `EventHandlerEntity`: вызов раз в 2000 тиков, пропуск при Warp Ward | `WarpEvents.onPlayerTick`, `CHECK_INTERVAL = 2000`, `hasWarpWard` | PASS |
| `WarpEvents.checkWarpEvent`: `nextInt(100) <= sqrt(counter)` | Та же формула и нормализация severity | PASS |
| Уменьшение counter на `max(5, sqrt(counter)*2)` | Перенесено без изменения | PASS |
| Учитывается предмет в руке, 4 брони и 4 Baubles | `TC4WarpingGearAdapter.getEquippedWarp` + опциональный Curios/Baubles bridge | PASS статически |
| 25 диапазонов/пробелов таблицы Warp событий | Восстановлены диапазоны 1–4 … 93+, включая пустые 5–8, 73–75 и 85–88 | PASS |
| Временный Warp уменьшается после каждого незащищённого check | `decayTemporaryWarp(player, 1)` | PASS |
| Death Gaze раз в 10 тиков | `checkDeathGaze` с LOS, PvP и Wither 80 тиков | PASS |
| Нереальные Mind Spider видит только жертва | viewer NBT + фильтр renderer | PASS статически |
| 8 эффектов TC4 | 8 `MobEffect`, исходные цвета и 18×18 иконки | PASS |
| Flux Flu/Phage повышают расход vis | +10 процентных пунктов за уровень в `WandItem` | PASS |
| Sanity Soap: 200 тиков, срабатывание после 195, весь temp Warp, шанс sticky | `SanitySoapItem` | PASS статически |

## Эффекты

| ID | Оригинальное действие | Порт |
|---|---|---|
| `vis_exhaust` | marker, штраф расхода vis | зарегистрирован, подключён к жезлам и flux-блокам |
| `infectious_vis_exhaust` | распространение каждые 40 тиков | радиус 4, понижение amplifier, затем обычный Flux Flu |
| `unnatural_hunger` | exhaustion каждый тик | перенесено; curatives: rotten flesh и brain |
| `death_gaze` | серверная проверка каждые 10 тиков | перенесено |
| `blurred_vision` | клиентский визуальный эффект | современный overlay-адаптер |
| `sun_scorned` | ожог при свете, лечение в темноте | перенесено в современную световую проверку |
| `thaumarhia` | периодическое создание flux goo | перенесено |
| `warp_ward` | блокирует Warp check | зарегистрированный beneficial effect + legacy save bridge |

## Ограничения

- `compileJava` не запущен: Gradle Wrapper не смог разрешить `services.gradle.org`.
- Gameplay, renderer, сохранение эффектов и выделенный сервер не проверены.
- Purifying Fluid остаётся отдельной следующей задачей; hook для бонуса Sanity Soap уже добавлен.
