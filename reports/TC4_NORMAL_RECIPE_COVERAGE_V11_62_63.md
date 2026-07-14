# Покрытие `ConfigRecipes.initializeNormalRecipes` — 11.62.63

**Статус:** STATIC PASS

## Правильная методика подсчёта

Оригинальные **135 регистраций** состоят из **86 обычных регистраций**, **48 NBT-рецептов назначения аспекта этикетке** и **1 рецепта очистки этикетки**. В современном порте 49 NBT-вариантов намеренно свёрнуты в один сериализатор `thaumcraft:jar_label`, а JEI показывает 48 назначений и 1 очистку. Поэтому сравнение `135` с числом JSON-файлов некорректно.

| Показатель | Значение |
|---|---:|
| Оригинальные регистрации после разворачивания циклов | 135 |
| Оригинальные нединамические регистрации | 86 |
| Сопоставлено нединамических регистраций | 86 |
| Динамические назначения этикеток | 48 |
| Очистка этикетки | 1 |
| Современные runtime-сериализаторы для семейства этикеток | 1 |
| JEI-представления семейства этикеток | 49 |

## Поэлементный маппинг 86 нединамических регистраций

| № | Оригинальная регистрация | Рецепт порта | Статус |
|---:|---|---|---|
| 1 | `3207 Iron nuggets` | `tc4_iron_nuggets_from_ingot.json` | MAPPED |
| 2 | `3210 Thaumium nuggets` | `tc4_thaumium_nuggets_from_ingot.json` | MAPPED |
| 3 | `3213 Void nuggets` | `tc4_void_nuggets_from_ingot.json` | MAPPED |
| 4 | `3216 Iron ingot` | `tc4_iron_ingot_from_nuggets.json` | MAPPED |
| 5 | `3219 Thaumium ingot` | `tc4_thaumium_ingot_from_nuggets.json` | MAPPED |
| 6 | `3222 Quicksilver drop` | `tc4_quicksilver_drop_from_nuggets.json` | MAPPED |
| 7 | `3225 Quicksilver nuggets` | `tc4_quicksilver_nuggets_from_drop.json` | MAPPED |
| 8 | `3228 Void ingot` | `tc4_void_ingot_from_nuggets.json` | MAPPED |
| 9 | `3234 MundaneAmulet` | `tc4_mundane_amulet.json` | MAPPED |
| 10 | `3243 MundaneRing` | `tc4_mundane_ring.json` | MAPPED |
| 11 | `3250 MundaneBelt` | `tc4_mundane_belt.json` | MAPPED |
| 12 | `3261 TripleMeatTreat A` | `tc4_triple_meat_treat_beef_chicken_pork.json` | MAPPED |
| 13 | `3263 TripleMeatTreat B` | `tc4_triple_meat_treat_beef_chicken_fish.json` | MAPPED |
| 14 | `3265 TripleMeatTreat C` | `tc4_triple_meat_treat_beef_fish_pork.json` | MAPPED |
| 15 | `3267 TripleMeatTreat D` | `tc4_triple_meat_treat_fish_chicken_pork.json` | MAPPED |
| 16 | `3271 Shimmerleaf` | `tc4_quicksilver_from_shimmerleaf.json` | MAPPED |
| 17 | `3275 Cinderpearl` | `tc4_blaze_powder_from_cinderpearl.json` | MAPPED |
| 18 | `3281 JarLabel` | `jar_label.json` | MAPPED |
| 19 | `3312 WandBasic` | `basic_wand_original_tc4.json` | MAPPED |
| 20 | `3320 WandCapIron` | `iron_wand_cap_original_tc4.json` | MAPPED |
| 21 | `3327 KnowFrag` | `knowledge_fragments_to_unknown_note_original_tc4.json` | MAPPED |
| 22 | `3335 PlankGreatwood` | `tc4_greatwood_planks.json` | MAPPED |
| 23 | `3339 PlankSilverwood` | `tc4_silverwood_planks.json` | MAPPED |
| 24 | `3345 Greatwood stairs` | `greatwood_stairs_original_tc4.json` | MAPPED |
| 25 | `3347 Silverwood stairs` | `silverwood_stairs_original_tc4.json` | MAPPED |
| 26 | `3351 Greatwood slab` | `greatwood_slab_original_tc4.json` | MAPPED |
| 27 | `3353 Silverwood slab` | `silverwood_slab_original_tc4.json` | MAPPED |
| 28 | `3358 BlockFlesh` | `tc4_flesh_block.json` | MAPPED |
| 29 | `3363 BlockThaumium` | `tc4_thaumium_block.json` | MAPPED |
| 30 | `3369 Thaumium unpack` | `tc4_thaumium_from_block.json` | MAPPED |
| 31 | `3373 BlockTallow` | `tc4_tallow_block.json` | MAPPED |
| 32 | `3377 Tallow unpack` | `tc4_tallow_from_block.json` | MAPPED |
| 33 | `3383 Clusters Aer` | `aer_crystal.json` | MAPPED |
| 34 | `3383 Clusters Terra` | `terra_crystal.json` | MAPPED |
| 35 | `3383 Clusters Ignis` | `ignis_crystal.json` | MAPPED |
| 36 | `3383 Clusters Aqua` | `aqua_crystal.json` | MAPPED |
| 37 | `3383 Clusters Ordo` | `ordo_crystal.json` | MAPPED |
| 38 | `3383 Clusters Perditio` | `perditio_crystal.json` | MAPPED |
| 39 | `3394 Clusters6` | `balanced_crystal_original_tc4.json` | MAPPED |
| 40 | `3406 Amber block` | `amber_block_original_tc4.json` | MAPPED |
| 41 | `3409 Amber bricks` | `amber_bricks_original_tc4.json` | MAPPED |
| 42 | `3412 Obsidian tile` | `obsidian_tile.json` | MAPPED |
| 43 | `3415 Amber unpack block` | `amber_from_block_original_tc4.json` | MAPPED |
| 44 | `3422 Amber unpack bricks` | `amber_from_bricks_original_tc4.json` | MAPPED |
| 45 | `3431 Grate` | `item_grate_original_tc4.json` | MAPPED |
| 46 | `3440 Phial` | `essentia_phial_original_style.json` | MAPPED |
| 47 | `3447 Table` | `table_original_tc4_style.json` | MAPPED |
| 48 | `3456 Scribe1` | `scribing_tools_from_phial_original_tc4.json` | MAPPED |
| 49 | `3462 Scribe2` | `scribing_tools_original_tc4_style.json` | MAPPED |
| 50 | `3467 Scribe3` | `scribing_tools_refill_original_tc4_style.json` | MAPPED |
| 51 | `3476 Thaumometer` | `thaumometer.json` | MAPPED |
| 52 | `Thaumium helm` | `tc4_thaumium_helm.json` | MAPPED |
| 53 | `Thaumium chest` | `tc4_thaumium_chest.json` | MAPPED |
| 54 | `Thaumium legs` | `tc4_thaumium_legs.json` | MAPPED |
| 55 | `Thaumium boots` | `tc4_thaumium_boots.json` | MAPPED |
| 56 | `Thaumium shovel` | `tc4_thaumium_shovel.json` | MAPPED |
| 57 | `Thaumium pick` | `tc4_thaumium_pick.json` | MAPPED |
| 58 | `Thaumium axe` | `tc4_thaumium_axe.json` | MAPPED |
| 59 | `Thaumium hoe` | `tc4_thaumium_hoe.json` | MAPPED |
| 60 | `Thaumium sword` | `tc4_thaumium_sword.json` | MAPPED |
| 61 | `Void helm` | `tc4_void_helm.json` | MAPPED |
| 62 | `Void chest` | `tc4_void_chest.json` | MAPPED |
| 63 | `Void legs` | `tc4_void_legs.json` | MAPPED |
| 64 | `Void boots` | `tc4_void_boots.json` | MAPPED |
| 65 | `Void shovel` | `tc4_void_shovel.json` | MAPPED |
| 66 | `Void pick` | `tc4_void_pick.json` | MAPPED |
| 67 | `Void axe` | `tc4_void_axe.json` | MAPPED |
| 68 | `Void hoe` | `tc4_void_hoe.json` | MAPPED |
| 69 | `Void sword` | `tc4_void_sword.json` | MAPPED |
| 70 | `3602 TallowCandle` | `tallow_candle_original_tc4.json` | MAPPED |
| 71 | `3609 Candle orange` | `tallow_candle_orange_from_white_original_tc4.json` | MAPPED |
| 72 | `3609 Candle magenta` | `tallow_candle_magenta_from_white_original_tc4.json` | MAPPED |
| 73 | `3609 Candle light_blue` | `tallow_candle_light_blue_from_white_original_tc4.json` | MAPPED |
| 74 | `3609 Candle yellow` | `tallow_candle_yellow_from_white_original_tc4.json` | MAPPED |
| 75 | `3609 Candle lime` | `tallow_candle_lime_from_white_original_tc4.json` | MAPPED |
| 76 | `3609 Candle pink` | `tallow_candle_pink_from_white_original_tc4.json` | MAPPED |
| 77 | `3609 Candle gray` | `tallow_candle_gray_from_white_original_tc4.json` | MAPPED |
| 78 | `3609 Candle light_gray` | `tallow_candle_light_gray_from_white_original_tc4.json` | MAPPED |
| 79 | `3609 Candle cyan` | `tallow_candle_cyan_from_white_original_tc4.json` | MAPPED |
| 80 | `3609 Candle purple` | `tallow_candle_purple_from_white_original_tc4.json` | MAPPED |
| 81 | `3609 Candle blue` | `tallow_candle_blue_from_white_original_tc4.json` | MAPPED |
| 82 | `3609 Candle brown` | `tallow_candle_brown_from_white_original_tc4.json` | MAPPED |
| 83 | `3609 Candle green` | `tallow_candle_green_from_white_original_tc4.json` | MAPPED |
| 84 | `3609 Candle red` | `tallow_candle_red_from_white_original_tc4.json` | MAPPED |
| 85 | `3609 Candle black` | `tallow_candle_black_from_white_original_tc4.json` | MAPPED |
| 86 | `3614 Candle whitewash` | `tallow_candle_whitewash_original_tc4.json` | MAPPED |

## Ограничение

Этот документ подтверждает наличие и тип рецептов статически. Он не заменяет компиляцию Forge, загрузку datapack-рецептов сервером и просмотр в JEI-клиенте.
