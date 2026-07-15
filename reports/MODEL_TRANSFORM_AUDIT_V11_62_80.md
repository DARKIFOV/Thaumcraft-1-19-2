# Semantic item-model audit v11.62.80

Проверены цепочки `parent`, наследуемые `display`-трансформации, overrides и динамические `builtin/entity` модели.

## Итоги

- Моделей предметов: **721**
- Динамических `builtin/entity`: **15**
- Моделей с собственным `display`: **36**
- Моделей с эффективным `display` после наследования: **50**
- Моделей с overrides: **1**
- Максимальная глубина parent-цепочки: **2**
- Ошибок: **0**
- Предупреждений: **0**

## Покрытие контекстов

- `thirdperson_righthand`: **39** моделей
- `thirdperson_lefthand`: **38** моделей
- `firstperson_righthand`: **38** моделей
- `firstperson_lefthand`: **38** моделей
- `head`: **0** моделей
- `gui`: **13** моделей
- `ground`: **3** моделей
- `fixed`: **11** моделей

## Динамические модели

- `advanced_node_stabilizer` → `builtin/entity`
- `avaritia_creative_wand` → `minecraft:builtin/entity`
- `essentia_jar` → `minecraft:builtin/entity`
- `filtered_essentia_jar` → `minecraft:builtin/entity`
- `greatwood_wand` → `minecraft:builtin/entity`
- `hungry_chest` → `builtin/entity`
- `iron_capped_wooden_wand` → `minecraft:builtin/entity`
- `node_jar` → `minecraft:builtin/entity`
- `node_stabilizer` → `builtin/entity`
- `silverwood_wand` → `minecraft:builtin/entity`
- `tc4_block_banner` → `minecraft:builtin/entity`
- `tc4_travel_trunk` → `minecraft:builtin/entity`
- `thaumometer` → `minecraft:builtin/entity`
- `vis_charge_relay` → `minecraft:builtin/entity`
- `void_essentia_jar` → `minecraft:builtin/entity`

## Ошибки

Нет: parent-цепочки разрешаются, циклов и некорректных display-векторов не найдено.

## Интерпретация

Отсутствие собственного `display` не является ошибкой: обычные generated/handheld модели используют стандартные трансформации Minecraft. В этом порте все восемь `builtin/entity` рендереров сами обрабатывают `TransformType`, поэтому собственный JSON `display` для них считается ошибкой двойного преобразования. Отдельный BEWLR-аудит проверяет Java-контракт.
