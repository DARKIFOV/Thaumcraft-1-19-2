package com.darkifov.thaumcraft.client.screen;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;
/**
 * Stage118: research icon resolver extracted from original TC4 ConfigResearch.java.
 * It maps each ResearchItem icon ItemStack expression to a 1.19.2 texture where
 * the original sprite was transferred. Unknown or vanilla icons fall back to the
 * same aspect/category icon resolver used by the browser.
 */
public final class TC4ResearchIconMap {
    private static final Map<String, Entry> ICONS = build();

    private TC4ResearchIconMap() {}

    public static Entry get(String researchKey) {
        return ICONS.get(researchKey);
    }

    public static ResourceLocation texture(String researchKey) {
        Entry entry = ICONS.get(researchKey);
        return entry == null ? null : entry.texture();
    }

    public static int mappedCount() { return ICONS.size(); }

    private static Map<String, Entry> build() {
        Map<String, Entry> map = new LinkedHashMap<>();
        map.put("BASICTHAUMATURGY", new Entry(new ResourceLocation("thaumcraft", "textures/item/iron_capped_wooden_wand.png"), "wand"));
        map.put("FOCUSFIRE", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/focus_fire.png"), "new ItemStack(ConfigItems.itemFocusFire)"));
        map.put("FOCUSFROST", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/focus_frost.png"), "new ItemStack(ConfigItems.itemFocusFrost)"));
        map.put("FOCUSHELLBAT", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/focus_hellbat.png"), "new ItemStack(ConfigItems.itemFocusHellbat)"));
        map.put("FOCUSEXCAVATION", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/focus.png"), "new ItemStack(ConfigItems.itemFocusExcavation)"));
        map.put("FOCUSWARDING", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/focus_warding.png"), "new ItemStack(ConfigItems.itemFocusWarding)"));
        map.put("FOCUSSHOCK", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/focus_shock.png"), "new ItemStack(ConfigItems.itemFocusShock)"));
        map.put("FOCUSTRADE", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/focus_trade.png"), "new ItemStack(ConfigItems.itemFocusTrade)"));
        map.put("FOCUSPORTABLEHOLE", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/focus_portablehole.png"), "new ItemStack(ConfigItems.itemFocusPortableHole)"));
        map.put("FOCUSPOUCH", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/focuspouch.png"), "new ItemStack(ConfigItems.itemFocusPouch)"));
        map.put("CAP_gold", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/wand_cap_gold.png"), "new ItemStack(ConfigItems.itemWandCap, 1, 1)"));
        map.put("CAP_thaumium", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/wand_cap_thaumium.png"), "new ItemStack(ConfigItems.itemWandCap, 1, 2)"));
        map.put("CAP_copper", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/wand_cap_copper.png"), "new ItemStack(ConfigItems.itemWandCap, 1, 3)"));
        map.put("CAP_silver", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/wand_cap_silver.png"), "new ItemStack(ConfigItems.itemWandCap, 1, 4)"));
        map.put("ROD_greatwood", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/wand_rod_greatwood.png"), "new ItemStack(ConfigItems.itemWandRod, 1, 0)"));
        map.put("ROD_reed", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/wand_rod_reed.png"), "new ItemStack(ConfigItems.itemWandRod, 1, 5)"));
        map.put("ROD_blaze", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/wand_rod_blaze.png"), "new ItemStack(ConfigItems.itemWandRod, 1, 6)"));
        map.put("ROD_obsidian", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/wand_rod_obsidian.png"), "new ItemStack(ConfigItems.itemWandRod, 1, 1)"));
        map.put("ROD_ice", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/wand_rod_quartz.png"), "new ItemStack(ConfigItems.itemWandRod, 1, 3)"));
        map.put("ROD_quartz", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/wand_rod_silverwood.png"), "new ItemStack(ConfigItems.itemWandRod, 1, 4)"));
        map.put("ROD_bone", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/wand_rod_ice.png"), "new ItemStack(ConfigItems.itemWandRod, 1, 7)"));
        map.put("ROD_silverwood", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/wand_rod_bone.png"), "new ItemStack(ConfigItems.itemWandRod, 1, 2)"));
        map.put("ROD_greatwood_staff", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/wand_rod_greatwood.png"), "new ItemStack(ConfigItems.itemWandRod, 1, 50)"));
        map.put("ROD_reed_staff", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/wand_rod_greatwood.png"), "new ItemStack(ConfigItems.itemWandRod, 1, 55)"));
        map.put("ROD_blaze_staff", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/wand_rod_greatwood.png"), "new ItemStack(ConfigItems.itemWandRod, 1, 56)"));
        map.put("ROD_obsidian_staff", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/wand_rod_greatwood.png"), "new ItemStack(ConfigItems.itemWandRod, 1, 51)"));
        map.put("ROD_ice_staff", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/wand_rod_greatwood.png"), "new ItemStack(ConfigItems.itemWandRod, 1, 53)"));
        map.put("ROD_quartz_staff", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/wand_rod_greatwood.png"), "new ItemStack(ConfigItems.itemWandRod, 1, 54)"));
        map.put("ROD_bone_staff", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/wand_rod_greatwood.png"), "new ItemStack(ConfigItems.itemWandRod, 1, 57)"));
        map.put("ROD_silverwood_staff", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/wand_rod_greatwood.png"), "new ItemStack(ConfigItems.itemWandRod, 1, 52)"));
        map.put("VISAMULET", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/vis_amulet.png"), "new ItemStack(ConfigItems.itemAmuletVis, 1, 1)"));
        map.put("BASICARTIFACE", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/charm.png"), "new ItemStack(ConfigItems.itemResource, 1, 15)"));
        map.put("ARCANESTONE", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/blocks/arcane_stone.png"), "new ItemStack(ConfigBlocks.blockCosmeticSolid, 1, 6)"));
        map.put("PAVETRAVEL", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/blocks/arcane_stone.png"), "new ItemStack(ConfigBlocks.blockCosmeticSolid, 1, 2)"));
        map.put("PAVEWARD", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/blocks/arcane_stone.png"), "new ItemStack(ConfigBlocks.blockCosmeticSolid, 1, 3)"));
        map.put("GOGGLES", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/gogglesrevealing.png"), "new ItemStack(ConfigItems.itemGoggles)"));
        map.put("SINSTONE", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/sinister_stone.png"), "new ItemStack(ConfigItems.itemCompassStone, 1, 1)"));
        map.put("ENCHFABRIC", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/cloth.png"), "new ItemStack(ConfigItems.itemResource, 1, 7)"));
        map.put("RUNICARMOR", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/runic_ring.png"), "new ItemStack(ConfigItems.itemRingRunic, 1, 1)"));
        map.put("RUNICCHARGED", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/runic_ring.png"), "new ItemStack(ConfigItems.itemRingRunic, 1, 2)"));
        map.put("RUNICHEALING", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/runic_ring.png"), "new ItemStack(ConfigItems.itemRingRunic, 1, 3)"));
        map.put("RUNICKINETIC", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/runic_girdle.png"), "new ItemStack(ConfigItems.itemGirdleRunic, 1, 1)"));
        map.put("RUNICEMERGENCY", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/runic_amulet.png"), "new ItemStack(ConfigItems.itemAmuletRunic, 1, 1)"));
        map.put("BOOTSTRAVELLER", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/bootstraveler.png"), "new ItemStack(ConfigItems.itemBootsTraveller)"));
        map.put("HOVERHARNESS", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/hoverharness.png"), "new ItemStack(ConfigItems.itemHoverHarness)"));
        map.put("MIRRORHAND", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/mirrorhand.png"), "new ItemStack(ConfigItems.itemHandMirror)"));
        map.put("BONEBOW", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/bonebow.png"), "new ItemStack(ConfigItems.itemBowBone)"));
        map.put("ELEMENTALAXE", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/elementalaxe.png"), "new ItemStack(ConfigItems.itemAxeElemental)"));
        map.put("ELEMENTALPICK", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/elementalpick.png"), "new ItemStack(ConfigItems.itemPickElemental)"));
        map.put("ELEMENTALSWORD", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/elementalsword.png"), "new ItemStack(ConfigItems.itemSwordElemental)"));
        map.put("ELEMENTALSHOVEL", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/elementalshovel.png"), "new ItemStack(ConfigItems.itemShovelElemental)"));
        map.put("ELEMENTALHOE", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/elementalhoe.png"), "new ItemStack(ConfigItems.itemHoeElemental)"));
        map.put("HELMGOGGLES", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/gogglesrevealing.png"), "new ItemStack(ConfigItems.itemGoggles)"));
        map.put("NITOR", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/nitor.png"), "new ItemStack(ConfigItems.itemResource, 1, 1)"));
        map.put("ALUMENTUM", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/alumentum.png"), "new ItemStack(ConfigItems.itemResource, 1, 0)"));
        map.put("LIQUIDDEATH", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/bucket_death.png"), "new ItemStack(ConfigItems.itemBucketDeath)"));
        map.put("BOTTLETAINT", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/bottle_taint.png"), "new ItemStack(ConfigItems.itemBottleTaint)"));
        map.put("THAUMIUM", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/thaumiumingot.png"), "new ItemStack(ConfigItems.itemResource, 1, 2)"));
        map.put("PUREIRON", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/nuggetthaumium.png"), "new ItemStack(ConfigItems.itemNugget, 1, 16)"));
        map.put("PUREGOLD", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/nuggetthaumium.png"), "new ItemStack(ConfigItems.itemNugget, 1, 31)"));
        map.put("PURECOPPER", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/nuggetthaumium.png"), "new ItemStack(ConfigItems.itemNugget, 1, 17)"));
        map.put("PURETIN", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/nuggetthaumium.png"), "new ItemStack(ConfigItems.itemNugget, 1, 18)"));
        map.put("PURESILVER", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/nuggetthaumium.png"), "new ItemStack(ConfigItems.itemNugget, 1, 19)"));
        map.put("PURELEAD", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/nuggetthaumium.png"), "new ItemStack(ConfigItems.itemNugget, 1, 20)"));
        map.put("TRANSIRON", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/nuggetthaumium.png"), "new ItemStack(ConfigItems.itemNugget, 1, 0)"));
        map.put("TRANSCOPPER", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/nuggetthaumium.png"), "new ItemStack(ConfigItems.itemNugget, 1, 1)"));
        map.put("TRANSTIN", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/nuggetthaumium.png"), "new ItemStack(ConfigItems.itemNugget, 1, 2)"));
        map.put("TRANSSILVER", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/nuggetthaumium.png"), "new ItemStack(ConfigItems.itemNugget, 1, 3)"));
        map.put("TRANSLEAD", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/nuggetthaumium.png"), "new ItemStack(ConfigItems.itemNugget, 1, 4)"));
        map.put("TALLOW", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/tallow.png"), "new ItemStack(ConfigItems.itemResource, 1, 4)"));
        map.put("ETHEREALBLOOM", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/blocks/cinderpearl.png"), "new ItemStack(ConfigBlocks.blockCustomPlant, 1, 4)"));
        map.put("BATHSALTS", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/bath_salts.png"), "new ItemStack(ConfigItems.itemBathSalts)"));
        map.put("SANESOAP", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/soap.png"), "new ItemStack(ConfigItems.itemSanitySoap)"));
        map.put("GOLEMFETTER", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/blocks/arcane_stone.png"), "new ItemStack(ConfigBlocks.blockCosmeticSolid, 1, 9)"));
        map.put("GOLEMSTRAW", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/golem_wood.png"), "new ItemStack(ConfigItems.itemGolemPlacer, 1, 0)"));
        map.put("GOLEMWOOD", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/golem_wood.png"), "new ItemStack(ConfigItems.itemGolemPlacer, 1, 1)"));
        map.put("GOLEMTALLOW", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/golem_wood.png"), "new ItemStack(ConfigItems.itemGolemPlacer, 1, 2)"));
        map.put("GOLEMCLAY", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/golem_wood.png"), "new ItemStack(ConfigItems.itemGolemPlacer, 1, 3)"));
        map.put("GOLEMFLESH", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/golem_wood.png"), "new ItemStack(ConfigItems.itemGolemPlacer, 1, 4)"));
        map.put("GOLEMSTONE", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/golem_wood.png"), "new ItemStack(ConfigItems.itemGolemPlacer, 1, 5)"));
        map.put("GOLEMIRON", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/golem_wood.png"), "new ItemStack(ConfigItems.itemGolemPlacer, 1, 6)"));
        map.put("GOLEMTHAUMIUM", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/golem_wood.png"), "new ItemStack(ConfigItems.itemGolemPlacer, 1, 7)"));
        map.put("GOLEMBELL", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/ironbell.png"), "new ItemStack(ConfigItems.itemGolemBell)"));
        map.put("COREGATHER", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/golem_core_blank.png"), "new ItemStack(ConfigItems.itemGolemCore, 1, 2)"));
        map.put("COREFILL", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/golem_core_blank.png"), "new ItemStack(ConfigItems.itemGolemCore, 1, 0)"));
        map.put("COREEMPTY", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/golem_core_blank.png"), "new ItemStack(ConfigItems.itemGolemCore, 1, 1)"));
        map.put("CORESORTING", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/golem_core_blank.png"), "new ItemStack(ConfigItems.itemGolemCore, 1, 10)"));
        map.put("COREUSE", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/golem_core_blank.png"), "new ItemStack(ConfigItems.itemGolemCore, 1, 8)"));
        map.put("COREHARVEST", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/golem_core_blank.png"), "new ItemStack(ConfigItems.itemGolemCore, 1, 3)"));
        map.put("COREFISHING", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/golem_core_blank.png"), "new ItemStack(ConfigItems.itemGolemCore, 1, 11)"));
        map.put("CORELUMBER", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/golem_core_blank.png"), "new ItemStack(ConfigItems.itemGolemCore, 1, 7)"));
        map.put("COREGUARD", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/golem_core_blank.png"), "new ItemStack(ConfigItems.itemGolemCore, 1, 4)"));
        map.put("COREBUTCHER", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/golem_core_blank.png"), "new ItemStack(ConfigItems.itemGolemCore, 1, 9)"));
        map.put("CORELIQUID", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/golem_core_blank.png"), "new ItemStack(ConfigItems.itemGolemCore, 1, 5)"));
        map.put("COREALCHEMY", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/golem_core_blank.png"), "new ItemStack(ConfigItems.itemGolemCore, 1, 6)"));
        map.put("TINYHAT", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/golemdecotophat.png"), "new ItemStack(ConfigItems.itemGolemDecoration, 1, 0)"));
        map.put("TINYGLASSES", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/golemdecotophat.png"), "new ItemStack(ConfigItems.itemGolemDecoration, 1, 1)"));
        map.put("TINYBOWTIE", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/golemdecotophat.png"), "new ItemStack(ConfigItems.itemGolemDecoration, 1, 2)"));
        map.put("TINYFEZ", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/golemdecotophat.png"), "new ItemStack(ConfigItems.itemGolemDecoration, 1, 3)"));
        map.put("TINYDART", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/golemdecotophat.png"), "new ItemStack(ConfigItems.itemGolemDecoration, 1, 4)"));
        map.put("TINYVISOR", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/golemdecotophat.png"), "new ItemStack(ConfigItems.itemGolemDecoration, 1, 5)"));
        map.put("TINYARMOR", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/golemdecotophat.png"), "new ItemStack(ConfigItems.itemGolemDecoration, 1, 6)"));
        map.put("TINYHAMMER", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/golemdecotophat.png"), "new ItemStack(ConfigItems.itemGolemDecoration, 1, 7)"));
        map.put("RESEARCH", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/inkwell.png"), "new ItemStack(ConfigItems.itemInkwell)"));
        map.put("KNOWFRAG", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/knowledgefragment.png"), "new ItemStack(ConfigItems.itemResource, 1, 9)"));
        map.put("THAUMONOMICON", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/thaumonomicon.png"), "new ItemStack(ConfigItems.itemThaumonomicon)"));
        map.put("PLANTS", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/blocks/cinderpearl.png"), "new ItemStack(ConfigBlocks.blockCustomPlant, 1, 0)"));
        map.put("CRIMSON", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/eldritch_object.png"), "new ItemStack(ConfigItems.itemEldritchObject, 1, 1)"));
        map.put("OCULUS", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/eldritch_object.png"), "new ItemStack(ConfigItems.itemEldritchObject, 1, 0)"));
        map.put("PRIMPEARL", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/eldritch_object.png"), "new ItemStack(ConfigItems.itemEldritchObject, 1, 3)"));
        map.put("PRIMALCRUSHER", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/primal_crusher.png"), "new ItemStack(ConfigItems.itemPrimalCrusher)"));
        map.put("VOIDMETAL", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/voidingot.png"), "new ItemStack(ConfigItems.itemResource, 1, 16)"));
        map.put("CAP_void", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/wand_cap_iron.png"), "new ItemStack(ConfigItems.itemWandCap, 1, 7)"));
        map.put("FOCUSPRIMAL", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/focus_primal.png"), "new ItemStack(ConfigItems.itemFocusPrimal)"));
        map.put("ROD_primal_staff", new Entry(new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/items/wand_rod_greatwood.png"), "new ItemStack(ConfigItems.itemWandRod, 1, 100)"));
        return Map.copyOf(map);
    }

    public record Entry(ResourceLocation texture, String originalExpression) {}
}
