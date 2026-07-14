package com.darkifov.thaumcraft.recipe;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Stage121: expanded TC4 1.7.10 ConfigRecipes expression resolver.
 * This class records de-metadata mapping decisions used by generated data.
 */
public final class TC4RecipeItemResolver {
    private static final Map<String, String> ITEM_META = new LinkedHashMap<>();
    private static final Map<String, String> ITEM_DIRECT = new LinkedHashMap<>();
    private static final Map<String, String> BLOCK_META = new LinkedHashMap<>();
    private static final Map<String, String> VANILLA_ITEMS = new LinkedHashMap<>();
    private static final Map<String, String> VANILLA_BLOCKS = new LinkedHashMap<>();
    private static final Map<String, String> ORE_DICT_EXACT = new LinkedHashMap<>();
    static {
        ITEM_META.put("itemAmuletVis:0", "thaumcraft:tc4_vis_amulet_lesser");
        ITEM_META.put("itemAmuletVis:1", "thaumcraft:tc4_vis_amulet");
        ITEM_META.put("itemEldritchObject:0", "thaumcraft:tc4_eldritch_object");
        ITEM_META.put("itemEldritchObject:1", "thaumcraft:tc4_crimson_rites");
        ITEM_META.put("itemEldritchObject:2", "thaumcraft:tc4_eldritch_object_2");
        ITEM_META.put("itemEldritchObject:3", "thaumcraft:tc4_eldritch_object_3");
        ITEM_META.put("itemEldritchObject:4", "thaumcraft:tc4_ob_placer");
        ITEM_META.put("itemGolemCore:0", "thaumcraft:tc4_golem_core_fill");
        ITEM_META.put("itemGolemCore:1", "thaumcraft:tc4_golem_core_empty");
        ITEM_META.put("itemGolemCore:2", "thaumcraft:tc4_golem_core_gather");
        ITEM_META.put("itemGolemCore:3", "thaumcraft:tc4_golem_core_harvest");
        ITEM_META.put("itemGolemCore:4", "thaumcraft:tc4_golem_core_guard");
        ITEM_META.put("itemGolemCore:5", "thaumcraft:tc4_golem_core_liquid");
        ITEM_META.put("itemGolemCore:6", "thaumcraft:tc4_golem_core_essentia");
        ITEM_META.put("itemGolemCore:7", "thaumcraft:tc4_golem_core_lumber");
        ITEM_META.put("itemGolemCore:8", "thaumcraft:tc4_golem_core_use");
        ITEM_META.put("itemGolemCore:9", "thaumcraft:tc4_golem_core_butcher");
        ITEM_META.put("itemGolemCore:10", "thaumcraft:tc4_golem_core_sorting");
        ITEM_META.put("itemGolemCore:11", "thaumcraft:tc4_golem_core_fish");
        ITEM_META.put("itemGolemCore:100", "thaumcraft:tc4_golem_core_blank");
        ITEM_META.put("itemGolemPlacer:0", "thaumcraft:tc4_golem_straw");
        ITEM_META.put("itemGolemPlacer:1", "thaumcraft:tc4_golem_wood");
        ITEM_META.put("itemGolemPlacer:2", "thaumcraft:tc4_golem_tallow");
        ITEM_META.put("itemGolemPlacer:3", "thaumcraft:tc4_golem_clay");
        ITEM_META.put("itemGolemPlacer:4", "thaumcraft:tc4_golem_flesh");
        ITEM_META.put("itemGolemPlacer:5", "thaumcraft:tc4_golem_stone");
        ITEM_META.put("itemGolemPlacer:6", "thaumcraft:tc4_golem_iron");
        ITEM_META.put("itemGolemPlacer:7", "thaumcraft:tc4_golem_thaumium");
        ITEM_META.put("itemGolemUpgrade:-1", "thaumcraft:tc4_golem_upgrade_empty");
        ITEM_META.put("itemGolemUpgrade:0", "thaumcraft:tc4_golem_upgrade_air");
        ITEM_META.put("itemGolemUpgrade:1", "thaumcraft:tc4_golem_upgrade_earth");
        ITEM_META.put("itemGolemUpgrade:2", "thaumcraft:tc4_golem_upgrade_fire");
        ITEM_META.put("itemGolemUpgrade:3", "thaumcraft:tc4_golem_upgrade_water");
        ITEM_META.put("itemGolemUpgrade:4", "thaumcraft:tc4_golem_upgrade_order");
        ITEM_META.put("itemGolemUpgrade:5", "thaumcraft:tc4_golem_upgrade_entropy");
        ITEM_META.put("itemNugget:0", "minecraft:iron_nugget");
        ITEM_META.put("itemNugget:1", "thaumcraft:tc4_nuggetcopper");
        ITEM_META.put("itemNugget:2", "thaumcraft:tc4_nuggettin");
        ITEM_META.put("itemNugget:3", "thaumcraft:tc4_nuggetsilver");
        ITEM_META.put("itemNugget:4", "thaumcraft:tc4_nuggetlead");
        ITEM_META.put("itemNugget:5", "thaumcraft:tc4_nuggetquicksilver");
        ITEM_META.put("itemNugget:6", "thaumcraft:thaumium_nugget");
        ITEM_META.put("itemNugget:7", "thaumcraft:tc4_nuggetvoid");
        ITEM_META.put("itemNugget:16", "thaumcraft:tc4_clusteriron");
        ITEM_META.put("itemNugget:17", "thaumcraft:tc4_clustercopper");
        ITEM_META.put("itemNugget:18", "thaumcraft:tc4_clustertin");
        ITEM_META.put("itemNugget:19", "thaumcraft:tc4_clustersilver");
        ITEM_META.put("itemNugget:20", "thaumcraft:tc4_clusterlead");
        ITEM_META.put("itemNugget:21", "thaumcraft:tc4_clustercinnabar");
        ITEM_META.put("itemNugget:31", "thaumcraft:tc4_clustergold");
        ITEM_META.put("itemPrimalArrow:0", "thaumcraft:tc4_el_arrow_air");
        ITEM_META.put("itemPrimalArrow:1", "thaumcraft:tc4_el_arrow_fire");
        ITEM_META.put("itemPrimalArrow:2", "thaumcraft:tc4_el_arrow_water");
        ITEM_META.put("itemPrimalArrow:3", "thaumcraft:tc4_el_arrow_earth");
        ITEM_META.put("itemPrimalArrow:4", "thaumcraft:tc4_el_arrow_order");
        ITEM_META.put("itemPrimalArrow:5", "thaumcraft:tc4_el_arrow_entropy");
        ITEM_META.put("itemResource:0", "thaumcraft:tc4_alumentum");
        ITEM_META.put("itemResource:1", "thaumcraft:tc4_nitor");
        ITEM_META.put("itemResource:2", "thaumcraft:thaumium_ingot");
        ITEM_META.put("itemResource:3", "thaumcraft:quicksilver_drop");
        ITEM_META.put("itemResource:4", "thaumcraft:tc4_tallow");
        ITEM_META.put("itemResource:5", "thaumcraft:tc4_brain");
        ITEM_META.put("itemResource:6", "thaumcraft:tc4_amber");
        ITEM_META.put("itemResource:7", "thaumcraft:tc4_cloth");
        ITEM_META.put("itemResource:8", "thaumcraft:tc4_filter");
        ITEM_META.put("itemResource:9", "thaumcraft:tc4_knowledgefragment");
        ITEM_META.put("itemResource:10", "thaumcraft:tc4_mirrorglass");
        ITEM_META.put("itemResource:11", "thaumcraft:tc4_taint_slime");
        ITEM_META.put("itemResource:12", "thaumcraft:tc4_taint_tendril");
        ITEM_META.put("itemResource:13", "thaumcraft:tc4_label");
        ITEM_META.put("itemResource:14", "thaumcraft:tc4_dust");
        ITEM_META.put("itemResource:15", "thaumcraft:tc4_charm");
        ITEM_META.put("itemResource:16", "thaumcraft:void_metal_ingot");
        ITEM_META.put("itemResource:17", "thaumcraft:tc4_voidseed");
        ITEM_META.put("itemResource:18", "thaumcraft:tc4_coin");
        ITEM_META.put("itemShard:0", "thaumcraft:aer_shard");
        ITEM_META.put("itemShard:1", "thaumcraft:ignis_shard");
        ITEM_META.put("itemShard:2", "thaumcraft:aqua_shard");
        ITEM_META.put("itemShard:3", "thaumcraft:terra_shard");
        ITEM_META.put("itemShard:4", "thaumcraft:ordo_shard");
        ITEM_META.put("itemShard:5", "thaumcraft:perditio_shard");
        ITEM_META.put("itemShard:6", "thaumcraft:balanced_shard");
        ITEM_META.put("itemWandCap:0", "thaumcraft:tc4_wand_cap_iron");
        ITEM_META.put("itemWandCap:1", "thaumcraft:tc4_wand_cap_gold");
        ITEM_META.put("itemWandCap:2", "thaumcraft:tc4_wand_cap_thaumium");
        ITEM_META.put("itemWandCap:3", "thaumcraft:tc4_wand_cap_copper");
        ITEM_META.put("itemWandCap:4", "thaumcraft:tc4_wand_cap_silver");
        ITEM_META.put("itemWandCap:5", "thaumcraft:tc4_wand_cap_silver_inert");
        ITEM_META.put("itemWandCap:6", "thaumcraft:tc4_wand_cap_thaumium_inert");
        ITEM_META.put("itemWandCap:7", "thaumcraft:tc4_wand_cap_void");
        ITEM_META.put("itemWandCap:8", "thaumcraft:tc4_wand_cap_void_inert");
        ITEM_META.put("itemWandRod:0", "thaumcraft:tc4_wand_rod_greatwood");
        ITEM_META.put("itemWandRod:1", "thaumcraft:tc4_wand_rod_obsidian");
        ITEM_META.put("itemWandRod:2", "thaumcraft:tc4_wand_rod_silverwood");
        ITEM_META.put("itemWandRod:3", "thaumcraft:tc4_wand_rod_ice");
        ITEM_META.put("itemWandRod:4", "thaumcraft:tc4_wand_rod_quartz");
        ITEM_META.put("itemWandRod:5", "thaumcraft:tc4_wand_rod_reed");
        ITEM_META.put("itemWandRod:6", "thaumcraft:tc4_wand_rod_blaze");
        ITEM_META.put("itemWandRod:7", "thaumcraft:tc4_wand_rod_bone");
        ITEM_META.put("itemWandRod:50", "thaumcraft:tc4_staff_rod_greatwood");
        ITEM_META.put("itemWandRod:51", "thaumcraft:tc4_staff_rod_obsidian");
        ITEM_META.put("itemWandRod:52", "thaumcraft:tc4_staff_rod_silverwood");
        ITEM_META.put("itemWandRod:53", "thaumcraft:tc4_staff_rod_ice");
        ITEM_META.put("itemWandRod:54", "thaumcraft:tc4_staff_rod_quartz");
        ITEM_META.put("itemWandRod:55", "thaumcraft:tc4_staff_rod_reed");
        ITEM_META.put("itemWandRod:56", "thaumcraft:tc4_staff_rod_blaze");
        ITEM_META.put("itemWandRod:57", "thaumcraft:tc4_staff_rod_bone");
        ITEM_META.put("itemWandRod:100", "thaumcraft:tc4_staff_rod_primal");
        ITEM_DIRECT.put("itemAmuletRunic", "thaumcraft:tc4_runic_amulet");
        ITEM_DIRECT.put("itemArcaneDoor", "thaumcraft:tc4_arcanedoor");
        ITEM_DIRECT.put("itemAxeElemental", "thaumcraft:tc4_elementalaxe");
        ITEM_DIRECT.put("itemAxeThaumium", "thaumcraft:tc4_thaumiumaxe");
        ITEM_DIRECT.put("itemAxeVoid", "thaumcraft:tc4_voidaxe");
        ITEM_DIRECT.put("itemBathSalts", "thaumcraft:tc4_bath_salts");
        ITEM_META.put("itemBaubleBlanks:0", "thaumcraft:tc4_bauble_amulet");
        ITEM_META.put("itemBaubleBlanks:1", "thaumcraft:tc4_bauble_ring");
        ITEM_META.put("itemBaubleBlanks:2", "thaumcraft:tc4_bauble_belt");
        ITEM_DIRECT.put("itemBaubleBlanks", "thaumcraft:tc4_bauble_amulet");
        ITEM_DIRECT.put("itemBootsRobe", "thaumcraft:tc4_clothboots");
        ITEM_DIRECT.put("itemBootsThaumium", "thaumcraft:tc4_thaumiumboots");
        ITEM_DIRECT.put("itemBootsTraveller", "thaumcraft:tc4_bootstraveler");
        ITEM_DIRECT.put("itemBootsVoid", "thaumcraft:tc4_voidboots");
        ITEM_DIRECT.put("itemBootsVoidRobe", "thaumcraft:tc4_voidrobeboots");
        ITEM_DIRECT.put("itemBottleTaint", "thaumcraft:tc4_bottle_taint");
        ITEM_DIRECT.put("itemBowBone", "thaumcraft:tc4_bonebow");
        ITEM_DIRECT.put("itemBucketDeath", "thaumcraft:tc4_bucket_death");
        ITEM_DIRECT.put("itemChestFortress", "thaumcraft:tc4_thaumiumfortresschest");
        ITEM_DIRECT.put("itemChestRobe", "thaumcraft:tc4_clothchest");
        ITEM_DIRECT.put("itemChestThaumium", "thaumcraft:tc4_thaumiumchest");
        ITEM_DIRECT.put("itemChestVoid", "thaumcraft:tc4_voidchest");
        ITEM_DIRECT.put("itemChestVoidRobe", "thaumcraft:tc4_voidrobechest");
        ITEM_DIRECT.put("itemCompassStone", "thaumcraft:tc4_sinister_stone");
        ITEM_DIRECT.put("itemCrystalEssence", "thaumcraft:tc4_crystalessence");
        ITEM_DIRECT.put("itemEssence", "thaumcraft:tc4_essence");
        ITEM_DIRECT.put("itemFocusExcavation", "thaumcraft:focus_excavation");
        ITEM_DIRECT.put("itemFocusFire", "thaumcraft:focus_fire");
        ITEM_DIRECT.put("itemFocusFrost", "thaumcraft:focus_frost");
        ITEM_DIRECT.put("itemFocusHellbat", "thaumcraft:tc4_focus_hellbat");
        ITEM_DIRECT.put("itemFocusPortableHole", "thaumcraft:focus_portable_hole");
        ITEM_DIRECT.put("itemFocusPouch", "thaumcraft:focus_pouch");
        ITEM_DIRECT.put("itemFocusPrimal", "thaumcraft:focus_primal");
        ITEM_DIRECT.put("itemFocusShock", "thaumcraft:focus_shock");
        ITEM_DIRECT.put("itemFocusTrade", "thaumcraft:focus_equal_trade");
        ITEM_DIRECT.put("itemFocusWarding", "thaumcraft:focus_warding");
        ITEM_DIRECT.put("itemGirdleHover", "thaumcraft:tc4_hovergirdle");
        ITEM_DIRECT.put("itemGirdleRunic", "thaumcraft:tc4_runic_girdle");
        ITEM_DIRECT.put("itemGoggles", "thaumcraft:goggles_of_revealing");
        ITEM_DIRECT.put("itemGolemBell", "thaumcraft:tc4_ironbell");
        ITEM_DIRECT.put("itemGolemDecoration", "thaumcraft:tc4_golemdecoarmor");
        ITEM_DIRECT.put("itemGolemPlacer", "thaumcraft:tc4_golem_wood");
        ITEM_DIRECT.put("itemHandMirror", "thaumcraft:tc4_mirrorhand");
        ITEM_DIRECT.put("itemHelmetFortress", "thaumcraft:tc4_thaumiumfortresshelm");
        ITEM_DIRECT.put("itemHelmetThaumium", "thaumcraft:tc4_thaumiumhelm");
        ITEM_DIRECT.put("itemHelmetVoid", "thaumcraft:tc4_voidhelm");
        ITEM_DIRECT.put("itemHelmetVoidRobe", "thaumcraft:tc4_voidrobehelm");
        ITEM_DIRECT.put("itemHoeElemental", "thaumcraft:tc4_elementalhoe");
        ITEM_DIRECT.put("itemHoeThaumium", "thaumcraft:tc4_thaumiumhoe");
        ITEM_DIRECT.put("itemHoeVoid", "thaumcraft:tc4_voidhoe");
        ITEM_DIRECT.put("itemHoverHarness", "thaumcraft:tc4_hoverharness");
        ITEM_DIRECT.put("itemInkwell", "thaumcraft:tc4_inkwell");
        ITEM_DIRECT.put("itemKey", "thaumcraft:tc4_keyiron");
        ITEM_DIRECT.put("itemLegsFortress", "thaumcraft:tc4_thaumiumfortresslegs");
        ITEM_DIRECT.put("itemLegsRobe", "thaumcraft:tc4_clothlegs");
        ITEM_DIRECT.put("itemLegsThaumium", "thaumcraft:tc4_thaumiumlegs");
        ITEM_DIRECT.put("itemLegsVoid", "thaumcraft:tc4_voidlegs");
        ITEM_DIRECT.put("itemLegsVoidRobe", "thaumcraft:tc4_voidrobelegs");
        ITEM_DIRECT.put("itemLootbag", "thaumcraft:tc4_lootbag");
        ITEM_DIRECT.put("itemPickElemental", "thaumcraft:tc4_elementalpick");
        ITEM_DIRECT.put("itemPickThaumium", "thaumcraft:tc4_thaumiumpick");
        ITEM_DIRECT.put("itemPickVoid", "thaumcraft:tc4_voidpick");
        ITEM_DIRECT.put("itemPrimalCrusher", "thaumcraft:tc4_primal_crusher");
        ITEM_DIRECT.put("itemResearchNotes", "thaumcraft:tc4_researchnotes");
        ITEM_DIRECT.put("itemResonator", "thaumcraft:tc4_resonator");
        ITEM_DIRECT.put("itemRingRunic", "thaumcraft:tc4_runic_ring");
        ITEM_DIRECT.put("itemSanityChecker", "thaumcraft:tc4_sanitychecker");
        ITEM_DIRECT.put("itemSanitySoap", "thaumcraft:tc4_soap");
        ITEM_DIRECT.put("itemShovelElemental", "thaumcraft:tc4_elementalshovel");
        ITEM_DIRECT.put("itemShovelThaumium", "thaumcraft:tc4_thaumiumshovel");
        ITEM_DIRECT.put("itemShovelVoid", "thaumcraft:tc4_voidshovel");
        ITEM_DIRECT.put("itemSwordElemental", "thaumcraft:tc4_elementalsword");
        ITEM_DIRECT.put("itemSwordThaumium", "thaumcraft:tc4_thaumiumsword");
        ITEM_DIRECT.put("itemSwordVoid", "thaumcraft:tc4_voidsword");
        ITEM_DIRECT.put("itemThaumometer", "thaumcraft:thaumometer");
        ITEM_DIRECT.put("itemThaumonomicon", "thaumcraft:thaumonomicon");
        ITEM_DIRECT.put("itemTrunkSpawner", "thaumcraft:tc4_travel_trunk");
        ITEM_DIRECT.put("itemWispEssence", "thaumcraft:tc4_wispessence");
        ITEM_DIRECT.put("itemZombieBrain", "thaumcraft:tc4_brain");
        BLOCK_META.put("blockCosmeticSolid:0", "thaumcraft:tc4_block_obsidian_tile");
        BLOCK_META.put("blockCosmeticSolid:1", "thaumcraft:tc4_block_obsidian_totem_base");
        BLOCK_META.put("blockCosmeticSolid:2", "thaumcraft:tc4_block_obsidian_totem_1");
        BLOCK_META.put("blockCosmeticSolid:3", "thaumcraft:tc4_block_obsidian_totem_2");
        BLOCK_META.put("blockCosmeticSolid:4", "thaumcraft:tc4_block_thaumium");
        BLOCK_META.put("blockCosmeticSolid:5", "thaumcraft:tc4_block_tallow");
        BLOCK_META.put("blockCosmeticSolid:6", "thaumcraft:tc4_block_arcane_stone");
        BLOCK_META.put("blockCosmeticSolid:7", "thaumcraft:tc4_block_paving_travel");
        BLOCK_META.put("blockCosmeticSolid:8", "thaumcraft:tc4_block_paving_warding");
        BLOCK_META.put("blockCosmeticSolid:9", "thaumcraft:tc4_block_thaumium");
        BLOCK_META.put("blockCosmeticSolid:10", "thaumcraft:tc4_block_tallow");
        BLOCK_META.put("blockCosmeticSolid:13", "thaumcraft:tc4_block_arcane_stone");
        BLOCK_META.put("blockCrystal:6", "thaumcraft:tc4_block_crystal_cluster");
        BLOCK_META.put("blockCustomPlant:0", "thaumcraft:tc4_block_greatwood_sapling");
        BLOCK_META.put("blockCustomPlant:1", "thaumcraft:tc4_block_silverwood_sapling");
        BLOCK_META.put("blockCustomPlant:2", "thaumcraft:tc4_block_shimmerleaf");
        BLOCK_META.put("blockCustomPlant:3", "thaumcraft:tc4_block_cinderpearl");
        BLOCK_META.put("blockCustomPlant:4", "thaumcraft:tc4_block_ethereal_bloom");
        BLOCK_META.put("blockCustomPlant:5", "thaumcraft:tc4_block_vishroom");
        BLOCK_META.put("blockCosmeticOpaque:2", "thaumcraft:warded_glass");
        BLOCK_META.put("blockJar:1", "thaumcraft:tc4_jar_brain");
        BLOCK_META.put("blockJar:3", "thaumcraft:void_essentia_jar");
        BLOCK_META.put("blockChestHungry:*", "thaumcraft:hungry_chest");
        BLOCK_META.put("blockMirror:0", "thaumcraft:tc4_mirrorframe");
        BLOCK_META.put("blockMirror:6", "thaumcraft:tc4_mirrorframe2");
        BLOCK_META.put("blockMetalDevice:2", "thaumcraft:vis_charge_relay");
        BLOCK_META.put("blockMetalDevice:14", "thaumcraft:vis_relay");
        BLOCK_META.put("blockStoneDevice:0", "thaumcraft:alchemical_furnace");
        BLOCK_META.put("blockEssentiaReservoir:*", "thaumcraft:essentia_reservoir");
        BLOCK_META.put("blockJar:0", "thaumcraft:essentia_jar");
        BLOCK_META.put("blockLifter:*", "thaumcraft:tc4_block_levitator");
        BLOCK_META.put("blockMagicalLeaves:0", "thaumcraft:tc4_block_greatwood_leaves");
        BLOCK_META.put("blockMagicalLeaves:1", "thaumcraft:tc4_block_silverwood_leaves");
        BLOCK_META.put("blockMagicalLog:0", "thaumcraft:greatwood_log");
        BLOCK_META.put("blockMagicalLog:1", "thaumcraft:silverwood_log");
        BLOCK_META.put("blockMetalDevice:0", "thaumcraft:tc4_block_metal_base");
        BLOCK_META.put("blockMetalDevice:1", "thaumcraft:alembic");
        BLOCK_META.put("blockMetalDevice:3", "thaumcraft:advanced_alchemical_furnace");
        BLOCK_META.put("blockMetalDevice:7", "thaumcraft:tc4_block_arcane_lamp");
        BLOCK_META.put("blockMetalDevice:8", "thaumcraft:tc4_block_lamp_growth");
        BLOCK_META.put("blockMetalDevice:9", "thaumcraft:tc4_block_alchemical_construct");
        BLOCK_META.put("blockMetalDevice:10", "thaumcraft:thaumatorium");
        BLOCK_META.put("blockMetalDevice:11", "thaumcraft:thaumatorium");
        BLOCK_META.put("blockMetalDevice:12", "thaumcraft:mnemonic_matrix"); // Stage523-542 runtime replacement for TC4 mnemonic matrix
        BLOCK_META.put("blockMetalDevice:13", "thaumcraft:tc4_block_lamp_fertility");
        BLOCK_META.put("blockSlabStone:0", "thaumcraft:tc4_block_arcane_stone_slab");
        BLOCK_META.put("blockStoneDevice:1", "thaumcraft:arcane_pedestal");
        BLOCK_META.put("blockStoneDevice:2", "thaumcraft:infusion_matrix");
        BLOCK_META.put("blockStoneDevice:5", "thaumcraft:tc4_block_wand_pedestal");
        BLOCK_META.put("blockStoneDevice:8", "thaumcraft:tc4_block_wand_pedestal_focus");
        BLOCK_META.put("blockStoneDevice:9", "thaumcraft:node_stabilizer");
        BLOCK_META.put("blockStoneDevice:10", "thaumcraft:advanced_node_stabilizer");
        BLOCK_META.put("blockStoneDevice:11", "thaumcraft:node_transducer");
        BLOCK_META.put("blockStoneDevice:12", "thaumcraft:arcane_spa");
        BLOCK_META.put("blockStoneDevice:13", "thaumcraft:focal_manipulator");
        BLOCK_META.put("blockStoneDevice:14", "thaumcraft:tc4_block_flux_scrubber");
        BLOCK_META.put("blockTable:14", "thaumcraft:deconstruction_table");
        BLOCK_META.put("blockTable:*", "thaumcraft:table");
        BLOCK_META.put("blockTube:0", "thaumcraft:essentia_tube");
        BLOCK_META.put("blockTube:2", "thaumcraft:alchemical_centrifuge");
        BLOCK_META.put("blockTube:7", "thaumcraft:essentia_crystalizer");
        BLOCK_META.put("blockTube:1", "thaumcraft:essentia_valve");
        BLOCK_META.put("blockTube:4", "thaumcraft:essentia_tube_filter");
        BLOCK_META.put("blockWoodenDevice:0", "thaumcraft:tc4_block_bellows");
        BLOCK_META.put("blockWoodenDevice:1", "thaumcraft:tc4_block_arcane_ear");
        BLOCK_META.put("blockWoodenDevice:2", "thaumcraft:tc4_block_arcane_pressure_plate");
        BLOCK_META.put("blockWoodenDevice:4", "thaumcraft:tc4_block_arcane_bore_base");
        BLOCK_META.put("blockWoodenDevice:5", "thaumcraft:tc4_block_arcane_bore");
        BLOCK_META.put("blockWoodenDevice:6", "thaumcraft:greatwood_planks");
        BLOCK_META.put("blockWoodenDevice:7", "thaumcraft:silverwood_planks");
        BLOCK_META.put("blockWoodenDevice:8", "thaumcraft:tc4_block_banner");
        VANILLA_ITEMS.put("field_151005_D", "minecraft:golden_pickaxe");
        VANILLA_ITEMS.put("field_151006_E", "minecraft:golden_axe");
        VANILLA_ITEMS.put("field_151007_F", "minecraft:string");
        VANILLA_ITEMS.put("field_151008_G", "minecraft:feather");
        VANILLA_ITEMS.put("field_151014_N", "minecraft:wheat_seeds");
        VANILLA_ITEMS.put("field_151015_O", "minecraft:wheat");
        VANILLA_ITEMS.put("field_151016_H", "minecraft:gunpowder");
        VANILLA_ITEMS.put("field_151021_T", "minecraft:leather_boots");
        VANILLA_ITEMS.put("field_151027_R", "minecraft:leather_chestplate");
        VANILLA_ITEMS.put("field_151028_Y", "minecraft:iron_helmet");
        VANILLA_ITEMS.put("field_151030_Z", "minecraft:iron_chestplate");
        VANILLA_ITEMS.put("field_151031_f", "minecraft:bow");
        VANILLA_ITEMS.put("field_151032_g", "minecraft:arrow");
        VANILLA_ITEMS.put("field_151033_d", "minecraft:flint_and_steel");
        VANILLA_ITEMS.put("field_151035_b", "minecraft:iron_pickaxe");
        VANILLA_ITEMS.put("field_151036_c", "minecraft:iron_axe");
        VANILLA_ITEMS.put("field_151040_l", "minecraft:iron_sword");
        VANILLA_ITEMS.put("field_151042_j", "minecraft:iron_ingot");
        VANILLA_ITEMS.put("field_151043_k", "minecraft:gold_ingot");
        VANILLA_ITEMS.put("field_151044_h", "minecraft:coal");
        VANILLA_ITEMS.put("field_151045_i", "minecraft:diamond");
        VANILLA_ITEMS.put("field_151046_w", "minecraft:diamond_pickaxe");
        VANILLA_ITEMS.put("field_151047_v", "minecraft:diamond_shovel");
        VANILLA_ITEMS.put("field_151055_y", "minecraft:stick");
        VANILLA_ITEMS.put("field_151059_bz", "minecraft:fire_charge");
        VANILLA_ITEMS.put("field_151061_bv", "minecraft:ender_eye");
        VANILLA_ITEMS.put("field_151064_bs", "minecraft:magma_cream");
        VANILLA_ITEMS.put("field_151065_br", "minecraft:blaze_powder");
        VANILLA_ITEMS.put("field_151068_bn", "minecraft:potion");
        VANILLA_ITEMS.put("field_151070_bp", "minecraft:spider_eye");
        VANILLA_ITEMS.put("field_151072_bj", "minecraft:blaze_rod");
        VANILLA_ITEMS.put("field_151073_bk", "minecraft:ghast_tear");
        VANILLA_ITEMS.put("field_151074_bl", "minecraft:gold_nugget");
        VANILLA_ITEMS.put("field_151078_bh", "minecraft:rotten_flesh");
        VANILLA_ITEMS.put("field_151079_bi", "minecraft:ender_pearl");
        VANILLA_ITEMS.put("field_151097_aZ", "minecraft:shears");
        VANILLA_ITEMS.put("field_151100_aR", "minecraft:black_dye");
        VANILLA_ITEMS.put("field_151103_aS", "minecraft:bone");
        VANILLA_ITEMS.put("field_151111_aL", "minecraft:compass");
        VANILLA_ITEMS.put("field_151112_aM", "minecraft:fishing_rod");
        VANILLA_ITEMS.put("field_151114_aO", "minecraft:glowstone_dust");
        VANILLA_ITEMS.put("field_151115_aP", "minecraft:cod");
        VANILLA_ITEMS.put("field_151116_aA", "minecraft:leather");
        VANILLA_ITEMS.put("field_151117_aB", "minecraft:milk_bucket");
        VANILLA_ITEMS.put("field_151118_aC", "minecraft:brick");
        VANILLA_ITEMS.put("field_151119_aD", "minecraft:clay_ball");
        VANILLA_ITEMS.put("field_151120_aE", "minecraft:sugar_cane");
        VANILLA_ITEMS.put("field_151121_aF", "minecraft:paper");
        VANILLA_ITEMS.put("field_151123_aH", "minecraft:slime_ball");
        VANILLA_ITEMS.put("field_151128_bU", "minecraft:quartz");
        VANILLA_ITEMS.put("field_151131_as", "minecraft:water_bucket");
        VANILLA_ITEMS.put("field_151132_bS", "minecraft:comparator");
        VANILLA_ITEMS.put("field_151133_ar", "minecraft:bucket");
        VANILLA_ITEMS.put("field_151137_ax", "minecraft:redstone");
        VANILLA_ITEMS.put("field_151144_bL", "minecraft:skeleton_skull");
        VANILLA_ITEMS.put("field_151145_ak", "minecraft:flint");
        VANILLA_ITEMS.put("field_151148_bJ", "minecraft:map");
        VANILLA_ITEMS.put("field_151156_bN", "minecraft:nether_star");
        VANILLA_ITEMS.put("field_151166_bC", "minecraft:emerald");
        VANILLA_ITEMS.put("field_151170_bI", "minecraft:poisonous_potato");
        VANILLA_ITEMS.put("field_151172_bF", "minecraft:carrot");
        VANILLA_ITEMS.put("field_151174_bG", "minecraft:potato");
        VANILLA_BLOCKS.put("field_150331_J", "minecraft:piston");
        VANILLA_BLOCKS.put("field_150343_Z", "minecraft:obsidian");
        VANILLA_BLOCKS.put("field_150359_w", "minecraft:glass");
        VANILLA_BLOCKS.put("field_150367_z", "minecraft:dispenser");
        VANILLA_BLOCKS.put("field_150442_at", "minecraft:lever");
        VANILLA_BLOCKS.put("field_150453_bW", "minecraft:daylight_detector");
        VANILLA_BLOCKS.put("field_150460_al", "minecraft:furnace");
        VANILLA_BLOCKS.put("field_150467_bQ", "minecraft:anvil");
        VANILLA_BLOCKS.put("field_150415_aT", "minecraft:oak_trapdoor");
        ORE_DICT_EXACT.put("dustGlowstone", "minecraft:glowstone_dust");
        ORE_DICT_EXACT.put("gemDiamond", "minecraft:diamond");
        ORE_DICT_EXACT.put("gemEmerald", "minecraft:emerald");
        ORE_DICT_EXACT.put("ingotGold", "minecraft:gold_ingot");
        ORE_DICT_EXACT.put("ingotIron", "minecraft:iron_ingot");
        ORE_DICT_EXACT.put("ingotThaumium", "thaumcraft:thaumium_ingot");
        ORE_DICT_EXACT.put("ingotVoid", "thaumcraft:void_metal_ingot");
        ORE_DICT_EXACT.put("nuggetCopper", "thaumcraft:tc4_nuggetcopper");
        ORE_DICT_EXACT.put("nuggetSilver", "thaumcraft:tc4_nuggetsilver");
        ORE_DICT_EXACT.put("nuggetThaumium", "thaumcraft:thaumium_nugget");
        ORE_DICT_EXACT.put("nuggetVoid", "thaumcraft:tc4_nuggetvoid");
        ORE_DICT_EXACT.put("plankWood", "minecraft:oak_planks");
        ORE_DICT_EXACT.put("stickWood", "minecraft:stick");
        ORE_DICT_EXACT.put("stone", "minecraft:stone");
        ORE_DICT_EXACT.put("dyeBlue", "minecraft:blue_dye");
    }

    private TC4RecipeItemResolver() {}

    public static String resolveConfigItem(String field, int meta) {
        String exact = ITEM_META.get(field + ":" + meta);
        if (exact != null) return exact;
        return ITEM_DIRECT.getOrDefault(field, "");
    }

    public static String resolveConfigItem(String field) {
        return ITEM_DIRECT.getOrDefault(field, resolveConfigItem(field, 0));
    }

    public static String resolveConfigBlock(String field, int meta) {
        String exact = BLOCK_META.get(field + ":" + meta);
        if (exact != null) return exact;
        return BLOCK_META.getOrDefault(field + ":*", "");
    }

    public static String resolveVanillaItemField(String field) {
        return VANILLA_ITEMS.getOrDefault(field, "");
    }

    public static String resolveVanillaBlockField(String field) {
        return VANILLA_BLOCKS.getOrDefault(field, "");
    }

    public static String resolveOreDictionaryToken(String token) {
        if (token == null) return "";
        String trimmed = token.trim();
        if (trimmed.length() >= 2 && trimmed.startsWith("'") && trimmed.endsWith("'")) {
            trimmed = trimmed.substring(1, trimmed.length() - 1);
        }
        return ORE_DICT_EXACT.getOrDefault(trimmed, "");
    }

    /**
     * Stage155: conservative 1.7.10 -> 1.19.2 recipe expression resolver.
     * It resolves only exact, already-carried TC4 ids and vanilla ids. Ore-dict
     * names without an exact single modern item remain unresolved on purpose.
     */
    public static String resolveLegacyRecipeExpression(String expression) {
        if (expression == null || expression.isBlank()) return "";
        String compact = expression.replace(" ", "");
        java.util.regex.Matcher itemMatcher = java.util.regex.Pattern.compile("ConfigItems\\.(item[A-Za-z0-9_]+)(?:,1,(-?\\d+))?").matcher(compact);
        if (itemMatcher.find()) {
            String field = itemMatcher.group(1);
            int meta = 0;
            if (itemMatcher.group(2) != null) {
                try { meta = Integer.parseInt(itemMatcher.group(2)); } catch (NumberFormatException ignored) { meta = 0; }
            }
            String resolved = resolveConfigItem(field, meta);
            if (!resolved.isBlank()) return resolved;
        }
        java.util.regex.Matcher blockMatcher = java.util.regex.Pattern.compile("ConfigBlocks\\.(block[A-Za-z0-9_]+)(?:,1,(-?\\d+))?").matcher(compact);
        if (blockMatcher.find()) {
            String field = blockMatcher.group(1);
            int meta = 0;
            if (blockMatcher.group(2) != null) {
                try { meta = Integer.parseInt(blockMatcher.group(2)); } catch (NumberFormatException ignored) { meta = 0; }
            }
            String resolved = resolveConfigBlock(field, meta);
            if (!resolved.isBlank()) return resolved;
        }
        java.util.regex.Matcher vanillaItemMatcher = java.util.regex.Pattern.compile("Items\\.(field_[0-9]+_[A-Za-z0-9]+)").matcher(compact);
        if (vanillaItemMatcher.find()) {
            String resolved = resolveVanillaItemField(vanillaItemMatcher.group(1));
            if (!resolved.isBlank()) return resolved;
        }
        java.util.regex.Matcher vanillaBlockMatcher = java.util.regex.Pattern.compile("Blocks\\.(field_[0-9]+_[A-Za-z0-9]+)").matcher(compact);
        if (vanillaBlockMatcher.find()) {
            String resolved = resolveVanillaBlockField(vanillaBlockMatcher.group(1));
            if (!resolved.isBlank()) return resolved;
        }
        java.util.regex.Matcher oreMatcher = java.util.regex.Pattern.compile("'([^']+)'").matcher(expression);
        if (oreMatcher.matches()) {
            return resolveOreDictionaryToken(oreMatcher.group(1));
        }
        return "";
    }

    public static int mappedTc4ItemCount() { return ITEM_META.size() + ITEM_DIRECT.size(); }
    public static int mappedTc4BlockVariantCount() { return BLOCK_META.size(); }
    public static int mappedVanillaBlockCount() { return VANILLA_BLOCKS.size(); }
    public static int mappedExactOreDictTokenCount() { return ORE_DICT_EXACT.size(); }
}
