# Полный маппинг arcane-рецептов TC4 — порт 11.62.66

## Разрешение расхождения 89 против 109

В оригинальном `ConfigRecipes.initializeArcaneRecipes()` действительно находятся **84 буквальных вызова** `addArcaneCraftingRecipe` и **5 буквальных вызовов** `addShapelessArcaneCraftingRecipe`. Сложение 84 + 5 даёт 89 **точек вызова в исходнике**, но не количество созданных рецептов.

Два shaped-вызова находятся внутри циклов:

- `Banner_ + a` выполняется 16 раз, поэтому вместо одной точки вызова создаёт 16 регистраций: прирост относительно буквального подсчёта **+15**;
- `PrimalArrow_ + a` выполняется 6 раз: прирост **+5**.

Следовательно, исходное количество регистраций Arcane Workbench:

- shaped: **84 + 15 + 5 = 104**;
- shapeless: **5**;
- всего: **109**.

Число **89** было корректным только как количество буквальных arcane-вызовов. Число **109** является количеством регистраций после разворачивания циклов.

## Отдельно исключённые семейства

В том же Java-методе находятся элементы, не являющиеся рецептами Arcane Workbench:

- обычный crafting: `ArcaneStone2` (стр. 830), `ArcaneStone3` (стр. 835), `ArcaneStone4` (стр. 838);
- инфузионные зачарования: 24 записи от `InfEnchRepair` и `InfEnchHaste` до `InfEnch21`.

Они не входят в 109. Полный список инфузионных зачарований приведён в отдельном отчёте.

## Итог статического покрытия

| Показатель | Значение |
|---|---:|
| Буквальные shaped-вызовы | 84 |
| Shaped-регистрации после циклов | 104 |
| Shapeless-регистрации | 5 |
| Всего Arcane Workbench | 109 |
| Datapack JSON | 104 |
| Генерируемые Java-рецепты наконечников | 5 |
| Сопоставлено | 109 |
| Отсутствует | 0 |

Статус карты: **STATIC_MAPPING_COMPLETE**. Это подтверждает структуру исходников и ресурсов, но не заменяет компиляцию и runtime-тест.

## Поэлементная таблица 109 регистраций

| № | TC4 key | Тип | Строка TC4 | Вариант цикла | Реализация порта | Статус |
|---:|---|---|---:|---|---|---|
| 1 | `Banner_0` | ARCANE_SHAPED | 628 | 0 | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_banner_0.json` | MAPPED |
| 2 | `Banner_1` | ARCANE_SHAPED | 628 | 1 | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_banner_1.json` | MAPPED |
| 3 | `Banner_2` | ARCANE_SHAPED | 628 | 2 | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_banner_2.json` | MAPPED |
| 4 | `Banner_3` | ARCANE_SHAPED | 628 | 3 | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_banner_3.json` | MAPPED |
| 5 | `Banner_4` | ARCANE_SHAPED | 628 | 4 | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_banner_4.json` | MAPPED |
| 6 | `Banner_5` | ARCANE_SHAPED | 628 | 5 | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_banner_5.json` | MAPPED |
| 7 | `Banner_6` | ARCANE_SHAPED | 628 | 6 | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_banner_6.json` | MAPPED |
| 8 | `Banner_7` | ARCANE_SHAPED | 628 | 7 | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_banner_7.json` | MAPPED |
| 9 | `Banner_8` | ARCANE_SHAPED | 628 | 8 | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_banner_8.json` | MAPPED |
| 10 | `Banner_9` | ARCANE_SHAPED | 628 | 9 | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_banner_9.json` | MAPPED |
| 11 | `Banner_10` | ARCANE_SHAPED | 628 | 10 | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_banner_10.json` | MAPPED |
| 12 | `Banner_11` | ARCANE_SHAPED | 628 | 11 | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_banner_11.json` | MAPPED |
| 13 | `Banner_12` | ARCANE_SHAPED | 628 | 12 | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_banner_12.json` | MAPPED |
| 14 | `Banner_13` | ARCANE_SHAPED | 628 | 13 | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_banner_13.json` | MAPPED |
| 15 | `Banner_14` | ARCANE_SHAPED | 628 | 14 | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_banner_14.json` | MAPPED |
| 16 | `Banner_15` | ARCANE_SHAPED | 628 | 15 | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_banner_15.json` | MAPPED |
| 17 | `PrimalCharm` | ARCANE_SHAPED | 637 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_primalcharm.json` | MAPPED |
| 18 | `ArcaneDoor` | ARCANE_SHAPED | 654 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_arcanedoor.json` | MAPPED |
| 19 | `WardedGlass` | ARCANE_SHAPED | 668 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_wardedglass.json` | MAPPED |
| 20 | `IronKey` | ARCANE_SHAPED | 680 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_ironkey.json` | MAPPED |
| 21 | `FluxScrubber` | ARCANE_SHAPED | 688 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_fluxscrubber.json` | MAPPED |
| 22 | `GoldKey` | ARCANE_SHAPED | 699 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_goldkey.json` | MAPPED |
| 23 | `ArcanePressurePlate` | ARCANE_SHAPED | 710 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_arcanepressureplate.json` | MAPPED |
| 24 | `NodeStabilizer` | ARCANE_SHAPED | 740 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_nodestabilizer.json` | MAPPED |
| 25 | `NodeTransducer` | ARCANE_SHAPED | 755 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_nodetransducer.json` | MAPPED |
| 26 | `NodeRelay` | ARCANE_SHAPED | 770 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_node_relay.json` | MAPPED |
| 27 | `NodeChargeRelay` | ARCANE_SHAPED | 778 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_nodechargerelay.json` | MAPPED |
| 28 | `FocalManipulator` | ARCANE_SHAPED | 788 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_focalmanipulator.json` | MAPPED |
| 29 | `GolemFetter` | ARCANE_SHAPED | 801 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_golemfetter.json` | MAPPED |
| 30 | `ArcaneStone1` | ARCANE_SHAPED | 822 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_arcanestone1.json` | MAPPED |
| 31 | `PaveTravel` | ARCANE_SHAPED | 843 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_pavetravel.json` | MAPPED |
| 32 | `ArcaneLamp` | ARCANE_SHAPED | 859 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_arcanelamp.json` | MAPPED |
| 33 | `ArcaneSpa` | ARCANE_SHAPED | 879 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_arcanespa.json` | MAPPED |
| 34 | `PaveWard` | ARCANE_SHAPED | 890 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_paveward.json` | MAPPED |
| 35 | `Levitator` | ARCANE_SHAPED | 907 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_levitator.json` | MAPPED |
| 36 | `ArcaneEar` | ARCANE_SHAPED | 927 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_arcaneear.json` | MAPPED |
| 37 | `MirrorGlass` | ARCANE_SHAPELESS | 953 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_mirrorglass.json` | MAPPED |
| 38 | `BoneBow` | ARCANE_SHAPED | 960 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_bonebow.json` | MAPPED |
| 39 | `PrimalArrow_0` | ARCANE_SHAPED | 975 | 0 | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_primalarrow_0.json` | MAPPED |
| 40 | `PrimalArrow_1` | ARCANE_SHAPED | 975 | 1 | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_primalarrow_1.json` | MAPPED |
| 41 | `PrimalArrow_2` | ARCANE_SHAPED | 975 | 2 | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_primalarrow_2.json` | MAPPED |
| 42 | `PrimalArrow_3` | ARCANE_SHAPED | 975 | 3 | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_primalarrow_3.json` | MAPPED |
| 43 | `PrimalArrow_4` | ARCANE_SHAPED | 975 | 4 | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_primalarrow_4.json` | MAPPED |
| 44 | `PrimalArrow_5` | ARCANE_SHAPED | 975 | 5 | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_primalarrow_5.json` | MAPPED |
| 45 | `InfusionMatrix` | ARCANE_SHAPED | 997 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_infusionmatrix.json` | MAPPED |
| 46 | `ArcanePedestal` | ARCANE_SHAPED | 1016 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_arcanepedestal.json` | MAPPED |
| 47 | `WardedJar` | ARCANE_SHAPED | 1029 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_wardedjar.json` | MAPPED |
| 48 | `JarVoid` | ARCANE_SHAPED | 1039 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_jar_void.json` | MAPPED |
| 49 | `WandCapGold` | ARCANE_SHAPED | 1058 | — | `src/main/java/com/darkifov/thaumcraft/wand/TC4ConfigRecipesWandIndex.java` | MAPPED |
| 50 | `WandCapCopper` | ARCANE_SHAPED | 1078 | — | `src/main/java/com/darkifov/thaumcraft/wand/TC4ConfigRecipesWandIndex.java` | MAPPED |
| 51 | `WandCapSilverInert` | ARCANE_SHAPED | 1097 | — | `src/main/java/com/darkifov/thaumcraft/wand/TC4ConfigRecipesWandIndex.java` | MAPPED |
| 52 | `WandCapThaumiumInert` | ARCANE_SHAPED | 1115 | — | `src/main/java/com/darkifov/thaumcraft/wand/TC4ConfigRecipesWandIndex.java` | MAPPED |
| 53 | `WandCapVoidInert` | ARCANE_SHAPED | 1132 | — | `src/main/java/com/darkifov/thaumcraft/wand/TC4ConfigRecipesWandIndex.java` | MAPPED |
| 54 | `WandRodGreatwood` | ARCANE_SHAPED | 1144 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_wandrodgreatwood.json` | MAPPED |
| 55 | `WandRodGreatwoodStaff` | ARCANE_SHAPED | 1152 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_wandrodgreatwoodstaff.json` | MAPPED |
| 56 | `WandRodObsidianStaff` | ARCANE_SHAPED | 1161 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_wandrodobsidianstaff.json` | MAPPED |
| 57 | `WandRodSilverwoodStaff` | ARCANE_SHAPED | 1170 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_wandrodsilverwoodstaff.json` | MAPPED |
| 58 | `WandRodIceStaff` | ARCANE_SHAPED | 1179 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_wandrodicestaff.json` | MAPPED |
| 59 | `WandRodQuartzStaff` | ARCANE_SHAPED | 1188 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_wandrodquartzstaff.json` | MAPPED |
| 60 | `WandRodReedStaff` | ARCANE_SHAPED | 1197 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_wandrodreedstaff.json` | MAPPED |
| 61 | `WandRodBlazeStaff` | ARCANE_SHAPED | 1206 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_wandrodblazestaff.json` | MAPPED |
| 62 | `WandRodBoneStaff` | ARCANE_SHAPED | 1215 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_wandrodbonestaff.json` | MAPPED |
| 63 | `FocusFire` | ARCANE_SHAPED | 1226 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_focusfire.json` | MAPPED |
| 64 | `FocusFrost` | ARCANE_SHAPED | 1237 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_focusfrost.json` | MAPPED |
| 65 | `FocusShock` | ARCANE_SHAPED | 1249 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_focusshock.json` | MAPPED |
| 66 | `FocusTrade` | ARCANE_SHAPED | 1261 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_focustrade.json` | MAPPED |
| 67 | `FocusExcavation` | ARCANE_SHAPED | 1276 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_focusexcavation.json` | MAPPED |
| 68 | `FocusPrimal` | ARCANE_SHAPED | 1288 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_focusprimal.json` | MAPPED |
| 69 | `FocusPouch` | ARCANE_SHAPED | 1303 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_focuspouch.json` | MAPPED |
| 70 | `Deconstructor` | ARCANE_SHAPED | 1320 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_deconstructor.json` | MAPPED |
| 71 | `ArcaneBoreBase` | ARCANE_SHAPED | 1335 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_arcaneborebase.json` | MAPPED |
| 72 | `EnchantedFabric` | ARCANE_SHAPED | 1351 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_enchantedfabric.json` | MAPPED |
| 73 | `RobeChest` | ARCANE_SHAPED | 1370 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_robechest.json` | MAPPED |
| 74 | `RobeLegs` | ARCANE_SHAPED | 1376 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_robelegs.json` | MAPPED |
| 75 | `RobeBoots` | ARCANE_SHAPED | 1382 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_robeboots.json` | MAPPED |
| 76 | `Goggles` | ARCANE_SHAPED | 1393 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_goggles.json` | MAPPED |
| 77 | `HungryChest` | ARCANE_SHAPED | 1408 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_hungrychest.json` | MAPPED |
| 78 | `GolemBell` | ARCANE_SHAPED | 1419 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_golembell.json` | MAPPED |
| 79 | `CoreBlank` | ARCANE_SHAPED | 1429 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_coreblank.json` | MAPPED |
| 80 | `UpgradeAir` | ARCANE_SHAPED | 1451 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_upgradeair.json` | MAPPED |
| 81 | `UpgradeEarth` | ARCANE_SHAPED | 1459 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_upgradeearth.json` | MAPPED |
| 82 | `UpgradeFire` | ARCANE_SHAPED | 1467 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_upgradefire.json` | MAPPED |
| 83 | `UpgradeWater` | ARCANE_SHAPED | 1475 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_upgradewater.json` | MAPPED |
| 84 | `UpgradeOrder` | ARCANE_SHAPED | 1483 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_upgradeorder.json` | MAPPED |
| 85 | `UpgradeEntropy` | ARCANE_SHAPED | 1491 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_upgradeentropy.json` | MAPPED |
| 86 | `TinyHat` | ARCANE_SHAPED | 1500 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_tinyhat.json` | MAPPED |
| 87 | `TinyFez` | ARCANE_SHAPED | 1511 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_tinyfez.json` | MAPPED |
| 88 | `TinyBowtie` | ARCANE_SHAPED | 1522 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_tinybowtie.json` | MAPPED |
| 89 | `TinyGlasses` | ARCANE_SHAPED | 1532 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_tinyglasses.json` | MAPPED |
| 90 | `TinyDart` | ARCANE_SHAPED | 1542 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_tinydart.json` | MAPPED |
| 91 | `TinyVisor` | ARCANE_SHAPED | 1551 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_tinyvisor.json` | MAPPED |
| 92 | `TinyArmor` | ARCANE_SHAPED | 1558 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_tinyarmor.json` | MAPPED |
| 93 | `TinyHammer` | ARCANE_SHAPED | 1565 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_tinyhammer.json` | MAPPED |
| 94 | `Filter` | ARCANE_SHAPED | 1571 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_filter.json` | MAPPED |
| 95 | `AlchemyFurnace` | ARCANE_SHAPED | 1580 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_alchemyfurnace.json` | MAPPED |
| 96 | `Alembic` | ARCANE_SHAPED | 1591 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_alembic.json` | MAPPED |
| 97 | `Bellows` | ARCANE_SHAPED | 1614 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_bellows.json` | MAPPED |
| 98 | `Tube` | ARCANE_SHAPED | 1628 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_tube.json` | MAPPED |
| 99 | `Resonator` | ARCANE_SHAPED | 1639 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_resonator.json` | MAPPED |
| 100 | `TubeValve` | ARCANE_SHAPELESS | 1648 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_tubevalve.json` | MAPPED |
| 101 | `TubeFilter` | ARCANE_SHAPELESS | 1656 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_tubefilter.json` | MAPPED |
| 102 | `TubeRestrict` | ARCANE_SHAPELESS | 1667 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_tuberestrict.json` | MAPPED |
| 103 | `TubeOneway` | ARCANE_SHAPELESS | 1675 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_tubeoneway.json` | MAPPED |
| 104 | `TubeBuffer` | ARCANE_SHAPED | 1685 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_tubebuffer.json` | MAPPED |
| 105 | `AlchemicalConstruct` | ARCANE_SHAPED | 1697 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_alchemicalconstruct.json` | MAPPED |
| 106 | `AdvAlchemyConstruct` | ARCANE_SHAPED | 1709 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_advalchemyconstruct.json` | MAPPED |
| 107 | `Centrifuge` | ARCANE_SHAPED | 1719 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_centrifuge.json` | MAPPED |
| 108 | `EssentiaCrystalizer` | ARCANE_SHAPED | 1729 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_essentia_crystalizer.json` | MAPPED |
| 109 | `MnemonicMatrix` | ARCANE_SHAPED | 1740 | — | `src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_mnemonicmatrix.json` | MAPPED |

## Ограничения

- Маппинг не подтверждает успешную загрузку JSON, списание vis, research gating, NBT-результаты и отображение JEI.
- Пять наконечников жезлов формируются `TC4ConfigRecipesWandIndex` и требуют Java-компиляции.
- Общий статус версии остаётся **PARTIAL / STATIC PASS** до сборки и проверки в клиенте/на выделенном сервере.
