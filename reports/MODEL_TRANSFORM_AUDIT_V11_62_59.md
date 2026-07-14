# Semantic item-model audit v11.62.59

Проверены цепочки `parent`, наследуемые `display`-трансформации, overrides и динамические `builtin/entity` модели.

## Итоги

- Моделей предметов: **690**
- Динамических `builtin/entity`: **8**
- Моделей с собственным `display`: **35**
- Моделей с эффективным `display` после наследования: **49**
- Моделей с overrides: **0**
- Максимальная глубина parent-цепочки: **2**
- Ошибок: **0**
- Предупреждений: **0**

## Покрытие контекстов

- `thirdperson_righthand`: **38** моделей
- `thirdperson_lefthand`: **34** моделей
- `firstperson_righthand`: **37** моделей
- `firstperson_lefthand`: **34** моделей
- `head`: **0** моделей
- `gui`: **16** моделей
- `ground`: **3** моделей
- `fixed`: **14** моделей

## Динамические модели

- `advanced_node_stabilizer` → `builtin/entity`
- `avaritia_creative_wand` → `minecraft:builtin/entity`
- `greatwood_wand` → `minecraft:builtin/entity`
- `iron_capped_wooden_wand` → `minecraft:builtin/entity`
- `node_jar` → `minecraft:builtin/entity`
- `node_stabilizer` → `builtin/entity`
- `silverwood_wand` → `minecraft:builtin/entity`
- `thaumometer` → `minecraft:builtin/entity`

## Ошибки

Нет: parent-цепочки разрешаются, циклов и некорректных display-векторов не найдено.

## Интерпретация

Отсутствие собственного `display` не является ошибкой: обычные generated/handheld модели используют стандартные трансформации Minecraft. В этом порте все восемь `builtin/entity` рендереров сами обрабатывают `TransformType`, поэтому собственный JSON `display` для них считается ошибкой двойного преобразования. Отдельный BEWLR-аудит проверяет Java-контракт.
