# Semantic item-model audit v11.63.28

Проверены цепочки `parent`, наследуемые `display`-трансформации, overrides и динамические `builtin/entity` модели.

## Итоги

- Моделей предметов: **726**
- Динамических `builtin/entity`: **38**
- Моделей с собственным `display`: **35**
- Моделей с эффективным `display` после наследования: **50**
- Моделей с overrides: **1**
- Максимальная глубина parent-цепочки: **2**
- Ошибок: **0**
- Предупреждений: **0**

## Покрытие контекстов

- `thirdperson_righthand`: **42** моделей
- `thirdperson_lefthand`: **41** моделей
- `firstperson_righthand`: **41** моделей
- `firstperson_lefthand`: **41** моделей
- `head`: **0** моделей
- `gui`: **13** моделей
- `ground`: **4** моделей
- `fixed`: **12** моделей

## Динамические модели

- `advanced_node_stabilizer` → `builtin/entity`
- `alchemical_centrifuge` → `builtin/entity`
- `alembic` → `builtin/entity`
- `avaritia_creative_wand` → `minecraft:builtin/entity`
- `bellows` → `builtin/entity`
- `essentia_jar` → `minecraft:builtin/entity`
- `filtered_essentia_jar` → `minecraft:builtin/entity`
- `greatwood_wand` → `minecraft:builtin/entity`
- `hungry_chest` → `builtin/entity`
- `infusion_matrix` → `builtin/entity`
- `iron_capped_wooden_wand` → `minecraft:builtin/entity`
- `node_jar` → `minecraft:builtin/entity`
- `node_stabilizer` → `builtin/entity`
- `node_transducer` → `minecraft:builtin/entity`
- `silverwood_wand` → `minecraft:builtin/entity`
- `tallow_candle` → `builtin/entity`
- `tallow_candle_black` → `builtin/entity`
- `tallow_candle_blue` → `builtin/entity`
- `tallow_candle_brown` → `builtin/entity`
- `tallow_candle_cyan` → `builtin/entity`
- `tallow_candle_gray` → `builtin/entity`
- `tallow_candle_green` → `builtin/entity`
- `tallow_candle_light_blue` → `builtin/entity`
- `tallow_candle_light_gray` → `builtin/entity`
- `tallow_candle_lime` → `builtin/entity`
- `tallow_candle_magenta` → `builtin/entity`
- `tallow_candle_orange` → `builtin/entity`
- `tallow_candle_pink` → `builtin/entity`
- `tallow_candle_purple` → `builtin/entity`
- `tallow_candle_red` → `builtin/entity`
- `tallow_candle_yellow` → `builtin/entity`
- `tc4_block_banner` → `minecraft:builtin/entity`
- `tc4_jar_brain` → `builtin/entity`
- `tc4_travel_trunk` → `minecraft:builtin/entity`
- `thaumometer` → `minecraft:builtin/entity`
- `vis_charge_relay` → `minecraft:builtin/entity`
- `vis_relay` → `builtin/entity`
- `void_essentia_jar` → `minecraft:builtin/entity`

## Ошибки

Нет: parent-цепочки разрешаются, циклов и некорректных display-векторов не найдено.

## Интерпретация

Отсутствие собственного `display` не является ошибкой: обычные generated/handheld модели используют стандартные трансформации Minecraft. В этом порте все восемь `builtin/entity` рендереров сами обрабатывают `TransformType`, поэтому собственный JSON `display` для них считается ошибкой двойного преобразования. Отдельный BEWLR-аудит проверяет Java-контракт.
