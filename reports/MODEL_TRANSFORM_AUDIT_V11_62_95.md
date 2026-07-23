# Semantic item-model audit v11.62.95

Проверены цепочки `parent`, наследуемые `display`-трансформации, overrides и динамические `builtin/entity` модели.

## Итоги

- Моделей предметов: **723**
- Динамических `builtin/entity`: **17**
- Моделей с собственным `display`: **37**
- Моделей с эффективным `display` после наследования: **50**
- Моделей с overrides: **1**
- Максимальная глубина parent-цепочки: **2**
- Ошибок: **0**
- Предупреждений: **0**

## Покрытие контекстов

- `thirdperson_righthand`: **40** моделей
- `thirdperson_lefthand`: **39** моделей
- `firstperson_righthand`: **39** моделей
- `firstperson_lefthand`: **39** моделей
- `head`: **0** моделей
- `gui`: **13** моделей
- `ground`: **4** моделей
- `fixed`: **12** моделей

## Динамические модели

- `advanced_node_stabilizer` → `builtin/entity`
- `alembic` → `builtin/entity`
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
- `vis_relay` → `builtin/entity`
- `void_essentia_jar` → `minecraft:builtin/entity`

## Ошибки

Нет: parent-цепочки разрешаются, циклов и некорректных display-векторов не найдено.

## Интерпретация

Отсутствие собственного `display` не является ошибкой: обычные generated/handheld модели используют стандартные трансформации Minecraft. В этом порте все восемь `builtin/entity` рендереров сами обрабатывают `TransformType`, поэтому собственный JSON `display` для них считается ошибкой двойного преобразования. Отдельный BEWLR-аудит проверяет Java-контракт.
