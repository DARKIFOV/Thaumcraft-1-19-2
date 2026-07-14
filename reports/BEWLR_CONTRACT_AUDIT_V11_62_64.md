# Forge BEWLR contract audit v11.62.64

Каждая модель `builtin/entity` должна иметь зарегистрированный Item-класс, `initializeClient`, `IClientItemExtensions` и конкретный `BlockEntityWithoutLevelRenderer`. Поскольку рендереры этого порта сами обрабатывают `TransformType`, динамическая JSON-модель не должна дополнительно объявлять `display`.

## Итоги

- Динамических моделей: **9**
- Ожидаемых контрактов: **9**
- Полных контрактов: **9**
- Ошибок: **0**

## Карта рендереров

- **OK** `node_stabilizer` → `NodeStabilizerItem.java` → `NodeStabilizerItemRenderer`
- **OK** `advanced_node_stabilizer` → `NodeStabilizerItem.java` → `NodeStabilizerItemRenderer`
- **OK** `iron_capped_wooden_wand` → `WandItem.java` → `WandItemRenderer`
- **OK** `greatwood_wand` → `WandItem.java` → `WandItemRenderer`
- **OK** `silverwood_wand` → `WandItem.java` → `WandItemRenderer`
- **OK** `avaritia_creative_wand` → `WandItem.java` → `WandItemRenderer`
- **OK** `thaumometer` → `ThaumometerItem.java` → `ThaumometerItemRenderer`
- **OK** `node_jar` → `NodeJarItem.java` → `NodeJarItemRenderer`
- **OK** `hungry_chest` → `HungryChestBlockItem.java` → `HungryChestItemRenderer`

## Ошибки

Нет. Все Forge runtime-контракты замкнуты.
