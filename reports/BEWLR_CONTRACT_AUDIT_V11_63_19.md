# Forge BEWLR contract audit v11.63.19

Каждая модель `builtin/entity` должна иметь зарегистрированный Item-класс, `initializeClient`, `IClientItemExtensions` и конкретный `BlockEntityWithoutLevelRenderer`. Поскольку рендереры этого порта сами обрабатывают `TransformType`, динамическая JSON-модель не должна дополнительно объявлять `display`.

## Итоги

- Динамических моделей: **38**
- Ожидаемых контрактов: **22**
- Полных контрактов: **22**
- Ошибок: **16**

## Карта рендереров

- **OK** `node_stabilizer` → `NodeStabilizerItem.java` → `NodeStabilizerItemRenderer`
- **OK** `advanced_node_stabilizer` → `NodeStabilizerItem.java` → `NodeStabilizerItemRenderer`
- **OK** `node_transducer` → `NodeTransducerItem.java` → `NodeTransducerItemRenderer`
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
- **OK** `alchemical_centrifuge` → `AlchemicalCentrifugeBlockItem.java` → `AlchemicalCentrifugeItemRenderer`
- **OK** `bellows` → `BellowsBlockItem.java` → `BellowsItemRenderer`
- **OK** `infusion_matrix` → `InfusionMatrixBlockItem.java` → `InfusionMatrixItemRenderer`
- **OK** `tc4_jar_brain` → `BrainJarBlockItem.java` → `BrainJarItemRenderer`
- **OK** `essentia_jar` → `EssentiaJarBlockItem.java` → `EssentiaJarItemRenderer`
- **OK** `filtered_essentia_jar` → `EssentiaJarBlockItem.java` → `EssentiaJarItemRenderer`
- **OK** `void_essentia_jar` → `EssentiaJarBlockItem.java` → `EssentiaJarItemRenderer`
- **OK** `tc4_travel_trunk` → `TravelingTrunkItem.java` → `TravelingTrunkItemRenderer`

## Ошибки

- **unmapped_dynamic_model** `tallow_candle` — builtin/entity
- **unmapped_dynamic_model** `tallow_candle_black` — builtin/entity
- **unmapped_dynamic_model** `tallow_candle_blue` — builtin/entity
- **unmapped_dynamic_model** `tallow_candle_brown` — builtin/entity
- **unmapped_dynamic_model** `tallow_candle_cyan` — builtin/entity
- **unmapped_dynamic_model** `tallow_candle_gray` — builtin/entity
- **unmapped_dynamic_model** `tallow_candle_green` — builtin/entity
- **unmapped_dynamic_model** `tallow_candle_light_blue` — builtin/entity
- **unmapped_dynamic_model** `tallow_candle_light_gray` — builtin/entity
- **unmapped_dynamic_model** `tallow_candle_lime` — builtin/entity
- **unmapped_dynamic_model** `tallow_candle_magenta` — builtin/entity
- **unmapped_dynamic_model** `tallow_candle_orange` — builtin/entity
- **unmapped_dynamic_model** `tallow_candle_pink` — builtin/entity
- **unmapped_dynamic_model** `tallow_candle_purple` — builtin/entity
- **unmapped_dynamic_model** `tallow_candle_red` — builtin/entity
- **unmapped_dynamic_model** `tallow_candle_yellow` — builtin/entity
