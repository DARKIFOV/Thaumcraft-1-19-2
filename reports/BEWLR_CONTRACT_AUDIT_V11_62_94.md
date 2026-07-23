# Forge BEWLR contract audit v11.62.95

Каждая модель `builtin/entity` должна иметь зарегистрированный Item-класс, `initializeClient`, `IClientItemExtensions` и конкретный `BlockEntityWithoutLevelRenderer`. Поскольку рендереры этого порта сами обрабатывают `TransformType`, динамическая JSON-модель не должна дополнительно объявлять `display`.

## Итоги

- Динамических моделей: **17**
- Ожидаемых контрактов: **17**
- Полных контрактов: **17**
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
- **OK** `tc4_block_banner` → `TC4BannerBlockItem.java` → `TC4BannerItemRenderer`
- **OK** `vis_charge_relay` → `VisChargeRelayBlockItem.java` → `VisChargeRelayItemRenderer`
- **OK** `vis_relay` → `VisRelayBlockItem.java` → `VisChargeRelayItemRenderer`
- **OK** `alembic` → `AlembicBlockItem.java` → `AlembicItemRenderer`
- **OK** `essentia_jar` → `EssentiaJarBlockItem.java` → `EssentiaJarItemRenderer`
- **OK** `filtered_essentia_jar` → `EssentiaJarBlockItem.java` → `EssentiaJarItemRenderer`
- **OK** `void_essentia_jar` → `EssentiaJarBlockItem.java` → `EssentiaJarItemRenderer`
- **OK** `tc4_travel_trunk` → `TravelingTrunkItem.java` → `TravelingTrunkItemRenderer`

## Ошибки

Нет. Все Forge runtime-контракты замкнуты.
