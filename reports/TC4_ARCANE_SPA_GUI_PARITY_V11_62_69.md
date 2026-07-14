# TC4 Arcane Spa GUI parity — 11.62.69

Статус: **STATIC PASS / COMPILE AND RUNTIME NOT TESTED**.

## 1. Эталон TC4 4.2.3.5

| Контракт | Исходный класс | Значение |
|---|---|---|
| Контейнер | `thaumcraft/common/container/ContainerSpa.java` | один слот `ItemBathSalts` в `(65,31)`, инвентарь игрока с `y=84`, hotbar с `y=142` |
| Переключатель режима | `ContainerSpa#func_75140_a` | button id `1`, вызов `TileSpa.toggleMix()` |
| GUI | `thaumcraft/client/gui/GuiSpa.java` | `gui_spa.png`, область `176×166` |
| Кнопка | `GuiSpa` | `(89,35)`, `8×8`, UV `208,16` или `208,32` |
| Шкала жидкости | `GuiSpa` | содержимое `(107,15)`, `10×48`; рамка `(106,11)`, UV `232,0`, `10×55` |
| Tooltip жидкости | `GuiSpa` | имя жидкости и объём в `mB` |
| Tooltip режима | `GuiSpa` | `text.spa.mix.true` / `text.spa.mix.false` |
| Звук кнопки | `GuiSpa#playButtonClick` | `thaumcraft:cameraclack`, volume `0.4`, pitch `1.0` |

## 2. Реализация 11.62.69

| Эталонный контракт | Реализация порта | Статус |
|---|---|---|
| Блок открывает GUI | `ArcaneSpaBlock#use` → `NetworkHooks.openScreen` | PASS статически |
| BlockEntity является `MenuProvider` | `ArcaneSpaBlockEntity` | PASS статически |
| Один слот Bath Salts | `ArcaneSpaMenu`, `SlotItemHandler(..., 0, 65, 31)` | PASS статически |
| Исходные координаты инвентаря | `ArcaneSpaMenu`, `y=84/142` | PASS статически |
| Shift-click соли | только Bath Salts переносится в слот Spa | PASS статически |
| Button id 1 | `ArcaneSpaMenu.BUTTON_TOGGLE_MIX` | PASS статически |
| Синхронизация режима | `ContainerData[0]` | PASS статически |
| Синхронизация объёма | `ContainerData[1]` | PASS статически |
| Синхронизация типа жидкости | `ContainerData[2]`, registry id | PASS статически |
| Исходная GUI-текстура | `assets/thaumcraft/textures/gui/gui_spa.png` | PASS статически |
| Кнопка и UV | `ArcaneSpaScreen` | PASS статически |
| Шкала и рамка | `ArcaneSpaScreen` | PASS статически |
| Динамическая текстура жидкости | `IClientFluidTypeExtensions`, block atlas, tint | PASS статически |
| Tooltip жидкости/режима | `ArcaneSpaScreen#render` | PASS статически |
| Звук `cameraclack` | `ArcaneSpaScreen#mouseClicked` | PASS статически |

## 3. Изменение управления

Временная схема 11.62.68 удалена:

- Shift + ПКМ больше не переключает режим;
- Bath Salts больше не вставляется прямым кликом по блоку;
- пустая рука больше не извлекает соль напрямую;
- прямой `FluidUtil`-обработчик удалён из `ArcaneSpaBlock`.

Обычный ПКМ теперь открывает контейнер. Режим переключается кнопкой, соль помещается в исходный слот, объём и тип жидкости показываются на шкале. Автоматизация через боковые Forge item/fluid capabilities сохранена.

## 4. Изоляция Purifying Fluid

Файл `data/minecraft/tags/fluids/water.json`, добавлявший очищающую жидкость в `#minecraft:water`, удалён. Вместо него создан отдельный тег:

`#thaumcraft:purifying_fluid`

с двумя значениями:

- `thaumcraft:purifying_fluid`;
- `thaumcraft:flowing_purifying_fluid`.

Это исключает наследование сторонних механик, которые проверяют именно ванильный water tag, сохраняя собственные свойства `FluidType` и логику блока.

## 5. Что остаётся проверить runtime

1. Открытие GUI в клиенте и на выделенном сервере.
2. Перемещение Bath Salts обычным кликом и Shift-click.
3. Обновление кнопки после сетевого ответа сервера.
4. Рендер воды, Purifying Fluid и сторонней жидкости в шкале.
5. Анимация `fluidpure.png.mcmeta` внутри GUI.
6. Tooltip имени/объёма.
7. Сохранение `Mix`, `Tank`, `Salts` после перезапуска.
8. Закрытие GUI при удалении блока или удалении игрока дальше 8 блоков.

Итог: **GUI/Container parity реализована статически; G/V/N остаются NOT TESTED до успешной сборки и запуска.**
