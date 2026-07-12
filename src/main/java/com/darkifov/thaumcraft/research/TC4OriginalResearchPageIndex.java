package com.darkifov.thaumcraft.research;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Exact ordered ResearchPage declarations extracted from TC4 4.2.3.5. */
public final class TC4OriginalResearchPageIndex {
    public record PageSpec(String type, String textKey, String unlockResearch,
                           String rawExpression, String[] recipeKeys) {
        public PageSpec {
            type = type == null ? "TEXT" : type;
            textKey = textKey == null ? "" : textKey;
            unlockResearch = unlockResearch == null ? "" : unlockResearch;
            rawExpression = rawExpression == null ? "" : rawExpression;
            recipeKeys = recipeKeys == null ? new String[0] : recipeKeys.clone();
        }

        @Override public String[] recipeKeys() { return recipeKeys.clone(); }

        public boolean isRecipePage() {
            String upper = type.toUpperCase(Locale.ROOT);
            return upper.contains("CRAFT") || upper.contains("RECIPE")
                    || upper.contains("INFUSION") || upper.contains("CRUCIBLE")
                    || upper.contains("SMELT") || upper.contains("COMPOUND")
                    || upper.contains("ITEMSTACK") || upper.equals("UNKNOWN");
        }
    }

    private static final Map<String, List<PageSpec>> PAGES = build();

    private TC4OriginalResearchPageIndex() {}

    public static List<PageSpec> pages(String researchKey) {
        if (researchKey == null) return List.of();
        return PAGES.getOrDefault(researchKey, List.of());
    }

    public static int researchCount() { return PAGES.size(); }
    public static int pageCount() {
        int count = 0;
        for (List<PageSpec> pages : PAGES.values()) count += pages.size();
        return count;
    }

    private static PageSpec page(String type, String textKey, String unlockResearch,
                                 String rawExpression, String... recipeKeys) {
        return new PageSpec(type, textKey, unlockResearch, rawExpression, recipeKeys);
    }

    private static void put(Map<String, List<PageSpec>> map, String key, PageSpec... pages) {
        List<PageSpec> copy = new ArrayList<>(pages.length);
        Collections.addAll(copy, pages);
        map.put(key, Collections.unmodifiableList(copy));
    }

    private static Map<String, List<PageSpec>> build() {
        Map<String, List<PageSpec>> map = new LinkedHashMap<>(201);
        addTHAUMATURGY(map);
        addARTIFICE(map);
        addALCHEMY(map);
        addGOLEMANCY(map);
        addBASICS(map);
        addELDRITCH(map);
        return Collections.unmodifiableMap(map);
    }

    private static void addTHAUMATURGY(Map<String, List<PageSpec>> map) {
        put(map, "BASICTHAUMATURGY",
                page("TEXT", "tc.research_page.BASICTHAUMATURGY.1", "", "'tc.research_page.BASICTHAUMATURGY.1'"),
                page("TEXT", "tc.research_page.BASICTHAUMATURGY.2", "", "'tc.research_page.BASICTHAUMATURGY.2'"),
                page("NORMAL_CRAFTING", "", "", "(IRecipe)recipes.get('WandCapIron')", "WandCapIron"),
                page("NORMAL_CRAFTING", "", "", "(IRecipe)recipes.get('WandBasic')", "WandBasic"));
        put(map, "FOCUSFIRE",
                page("TEXT", "tc.research_page.FOCUSFIRE.1", "", "'tc.research_page.FOCUSFIRE.1'"),
                page("TEXT", "tc.research_page.FOCUSFIRE.2", "", "'tc.research_page.FOCUSFIRE.2'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('FocusFire')", "FocusFire"));
        put(map, "FOCUSFROST",
                page("TEXT", "tc.research_page.FOCUSFROST.1", "", "'tc.research_page.FOCUSFROST.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('FocusFrost')", "FocusFrost"));
        put(map, "FOCUSHELLBAT",
                page("TEXT", "tc.research_page.FOCUSHELLBAT.1", "", "'tc.research_page.FOCUSHELLBAT.1'"),
                page("INFUSION_CRAFTING", "", "", "(InfusionRecipe)recipes.get('FocusHellbat')", "FocusHellbat"));
        put(map, "FOCUSEXCAVATION",
                page("TEXT", "tc.research_page.FOCUSEXCAVATION.1", "", "'tc.research_page.FOCUSEXCAVATION.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('FocusExcavation')", "FocusExcavation"));
        put(map, "FOCUSWARDING",
                page("TEXT", "tc.research_page.FOCUSWARDING.1", "", "'tc.research_page.FOCUSWARDING.1'"),
                page("INFUSION_CRAFTING", "", "", "(InfusionRecipe)recipes.get('FocusWarding')", "FocusWarding"));
        put(map, "FOCUSSHOCK",
                page("TEXT", "tc.research_page.FOCUSSHOCK.1", "", "'tc.research_page.FOCUSSHOCK.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('FocusShock')", "FocusShock"));
        put(map, "FOCUSTRADE",
                page("TEXT", "tc.research_page.FOCUSTRADE.1", "", "'tc.research_page.FOCUSTRADE.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('FocusTrade')", "FocusTrade"));
        put(map, "FOCUSPORTABLEHOLE",
                page("TEXT", "tc.research_page.FOCUSPORTABLEHOLE.1", "", "'tc.research_page.FOCUSPORTABLEHOLE.1'"),
                page("INFUSION_CRAFTING", "", "", "(InfusionRecipe)recipes.get('FocusPortableHole')", "FocusPortableHole"));
        put(map, "FOCUSPOUCH",
                page("TEXT", "tc.research_page.FOCUSPOUCH.1", "", "'tc.research_page.FOCUSPOUCH.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('FocusPouch')", "FocusPouch"));
        put(map, "CAP_iron",
        put(map, "CAP_gold",
                page("TEXT", "tc.research_page.CAP_gold.1", "", "'tc.research_page.CAP_gold.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('WandCapGold')", "WandCapGold"));
        put(map, "CAP_thaumium",
                page("TEXT", "tc.research_page.CAP_thaumium.1", "", "'tc.research_page.CAP_thaumium.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('WandCapThaumiumInert')", "WandCapThaumiumInert"),
                page("INFUSION_CRAFTING", "", "", "(InfusionRecipe)recipes.get('WandCapThaumium')", "WandCapThaumium"));
        put(map, "CAP_copper",
                page("TEXT", "tc.research_page.CAP_copper.1", "", "'tc.research_page.CAP_copper.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('WandCapCopper')", "WandCapCopper"));
        put(map, "CAP_silver",
                page("TEXT", "tc.research_page.CAP_silver.1", "", "'tc.research_page.CAP_silver.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('WandCapSilverInert')", "WandCapSilverInert"),
                page("INFUSION_CRAFTING", "", "", "(InfusionRecipe)recipes.get('WandCapSilver')", "WandCapSilver"));
        put(map, "ROD_wood",
        put(map, "ROD_greatwood",
                page("TEXT", "tc.research_page.ROD_greatwood.1", "", "'tc.research_page.ROD_greatwood.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('WandRodGreatwood')", "WandRodGreatwood"));
        put(map, "ROD_reed",
                page("TEXT", "tc.research_page.ROD_reed.1", "", "'tc.research_page.ROD_reed.1'"),
                page("INFUSION_CRAFTING", "", "", "(InfusionRecipe)recipes.get('WandRodReed')", "WandRodReed"));
        put(map, "ROD_blaze",
                page("TEXT", "tc.research_page.ROD_blaze.1", "", "'tc.research_page.ROD_blaze.1'"),
                page("INFUSION_CRAFTING", "", "", "(InfusionRecipe)recipes.get('WandRodBlaze')", "WandRodBlaze"));
        put(map, "ROD_obsidian",
                page("TEXT", "tc.research_page.ROD_obsidian.1", "", "'tc.research_page.ROD_obsidian.1'"),
                page("INFUSION_CRAFTING", "", "", "(InfusionRecipe)recipes.get('WandRodObsidian')", "WandRodObsidian"));
        put(map, "ROD_ice",
                page("TEXT", "tc.research_page.ROD_ice.1", "", "'tc.research_page.ROD_ice.1'"),
                page("INFUSION_CRAFTING", "", "", "(InfusionRecipe)recipes.get('WandRodIce')", "WandRodIce"));
        put(map, "ROD_quartz",
                page("TEXT", "tc.research_page.ROD_quartz.1", "", "'tc.research_page.ROD_quartz.1'"),
                page("INFUSION_CRAFTING", "", "", "(InfusionRecipe)recipes.get('WandRodQuartz')", "WandRodQuartz"));
        put(map, "ROD_bone",
                page("TEXT", "tc.research_page.ROD_bone.1", "", "'tc.research_page.ROD_bone.1'"),
                page("INFUSION_CRAFTING", "", "", "(InfusionRecipe)recipes.get('WandRodBone')", "WandRodBone"));
        put(map, "ROD_silverwood",
                page("TEXT", "tc.research_page.ROD_silverwood.1", "", "'tc.research_page.ROD_silverwood.1'"),
                page("INFUSION_CRAFTING", "", "", "(InfusionRecipe)recipes.get('WandRodSilverwood')", "WandRodSilverwood"));
        put(map, "SCEPTRE",
                page("TEXT", "tc.research_page.SCEPTRE.1", "", "'tc.research_page.SCEPTRE.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe[])scer.toArray(new IArcaneRecipe[0])"));
        put(map, "ROD_greatwood_staff",
                page("TEXT", "tc.research_page.ROD_greatwood_staff.1", "", "'tc.research_page.ROD_greatwood_staff.1'"),
                page("TEXT", "tc.research_page.ROD_greatwood_staff.2", "", "'tc.research_page.ROD_greatwood_staff.2'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('WandRodGreatwoodStaff')", "WandRodGreatwoodStaff"));
        put(map, "ROD_reed_staff",
                page("TEXT", "tc.research_page.ROD_reed_staff.1", "", "'tc.research_page.ROD_reed_staff.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('WandRodReedStaff')", "WandRodReedStaff"));
        put(map, "ROD_blaze_staff",
                page("TEXT", "tc.research_page.ROD_blaze_staff.1", "", "'tc.research_page.ROD_blaze_staff.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('WandRodBlazeStaff')", "WandRodBlazeStaff"));
        put(map, "ROD_obsidian_staff",
                page("TEXT", "tc.research_page.ROD_obsidian_staff.1", "", "'tc.research_page.ROD_obsidian_staff.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('WandRodObsidianStaff')", "WandRodObsidianStaff"));
        put(map, "ROD_ice_staff",
                page("TEXT", "tc.research_page.ROD_ice_staff.1", "", "'tc.research_page.ROD_ice_staff.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('WandRodIceStaff')", "WandRodIceStaff"));
        put(map, "ROD_quartz_staff",
                page("TEXT", "tc.research_page.ROD_quartz_staff.1", "", "'tc.research_page.ROD_quartz_staff.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('WandRodQuartzStaff')", "WandRodQuartzStaff"));
        put(map, "ROD_bone_staff",
                page("TEXT", "tc.research_page.ROD_bone_staff.1", "", "'tc.research_page.ROD_bone_staff.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('WandRodBoneStaff')", "WandRodBoneStaff"));
        put(map, "ROD_silverwood_staff",
                page("TEXT", "tc.research_page.ROD_silverwood_staff.1", "", "'tc.research_page.ROD_silverwood_staff.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('WandRodSilverwoodStaff')", "WandRodSilverwoodStaff"));
        put(map, "WANDPED",
                page("TEXT", "tc.research_page.WANDPED.1", "", "'tc.research_page.WANDPED.1'"),
                page("INFUSION_CRAFTING", "", "", "(InfusionRecipe)recipes.get('WandPed')", "WandPed"));
        put(map, "VISAMULET",
                page("TEXT", "tc.research_page.VISAMULET.1", "", "'tc.research_page.VISAMULET.1'"),
                page("INFUSION_CRAFTING", "", "", "(InfusionRecipe)recipes.get('VisAmulet')", "VisAmulet"),
                page("TEXT", "tc.research_page.VISAMULET.2", "", "'tc.research_page.VISAMULET.2'"));
        put(map, "WANDPEDFOC",
                page("TEXT", "tc.research_page.WANDPEDFOC.1", "", "'tc.research_page.WANDPEDFOC.1'"),
                page("INFUSION_CRAFTING", "", "", "(InfusionRecipe)recipes.get('WandPedFocus')", "WandPedFocus"));
        put(map, "NODESTABILIZER",
                page("TEXT", "tc.research_page.NODESTABILIZER.1", "", "'tc.research_page.NODESTABILIZER.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('NodeStabilizer')", "NodeStabilizer"),
                page("TEXT", "tc.research_page.NODESTABILIZER.2", "", "'tc.research_page.NODESTABILIZER.2'"));
        put(map, "NODESTABILIZERADV",
                page("TEXT", "tc.research_page.NODESTABILIZERADV.1", "", "'tc.research_page.NODESTABILIZERADV.1'"),
                page("INFUSION_CRAFTING", "", "", "(InfusionRecipe)recipes.get('NodeStabilizerAdv')", "NodeStabilizerAdv"));
        put(map, "VISPOWER",
                page("TEXT", "tc.research_page.VISPOWER.1", "", "'tc.research_page.VISPOWER.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('NodeTransducer')", "NodeTransducer"),
                page("TEXT", "tc.research_page.VISPOWER.2", "", "'tc.research_page.VISPOWER.2'"),
                page("TEXT", "tc.research_page.VISPOWER.3", "", "'tc.research_page.VISPOWER.3'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('NodeRelay')", "NodeRelay"),
                page("TEXT", "tc.research_page.VISPOWER.4", "", "'tc.research_page.VISPOWER.4'"),
                page("TEXT", "tc.research_page.VISPOWER.5", "", "'tc.research_page.VISPOWER.5'"));
        put(map, "VISCHARGERELAY",
                page("TEXT", "tc.research_page.VISCHARGERELAY.1", "", "'tc.research_page.VISCHARGERELAY.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('NodeChargeRelay')", "NodeChargeRelay"));
        put(map, "FOCALMANIPULATION",
                page("TEXT", "tc.research_page.FOCALMANIPULATION.1", "", "'tc.research_page.FOCALMANIPULATION.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('FocalManipulator')", "FocalManipulator"),
                page("TEXT", "tc.research_page.FOCALMANIPULATION.2", "", "'tc.research_page.FOCALMANIPULATION.2'"));
        put(map, "VAMPBAT",
                page("TEXT", "focus.upgrade.vampirebats.text", "", "'focus.upgrade.vampirebats.text'"));
    }

    private static void addARTIFICE(Map<String, List<PageSpec>> map) {
        put(map, "BASICARTIFACE",
                page("TEXT", "tc.research_page.BASICARTIFACE.1", "", "'tc.research_page.BASICARTIFACE.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('PrimalCharm')", "PrimalCharm"),
                page("NORMAL_CRAFTING", "", "", "(IRecipe)recipes.get('MundaneAmulet')", "MundaneAmulet"),
                page("NORMAL_CRAFTING", "", "", "(IRecipe)recipes.get('MundaneRing')", "MundaneRing"),
                page("NORMAL_CRAFTING", "", "", "(IRecipe)recipes.get('MundaneBelt')", "MundaneBelt"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('MirrorGlass')", "MirrorGlass"));
        put(map, "ARCANESTONE",
                page("TEXT", "tc.research_page.ARCANESTONE.1", "", "'tc.research_page.ARCANESTONE.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('ArcaneStone1')", "ArcaneStone1"),
                page("NORMAL_CRAFTING", "", "", "(IRecipe)recipes.get('ArcaneStone2')", "ArcaneStone2"),
                page("NORMAL_CRAFTING", "", "", "(IRecipe)recipes.get('ArcaneStone3')", "ArcaneStone3"),
                page("NORMAL_CRAFTING", "", "", "(IRecipe)recipes.get('ArcaneStone4')", "ArcaneStone4"));
        put(map, "GRATE",
                page("TEXT", "tc.research_page.GRATE.1", "", "'tc.research_page.GRATE.1'"),
                page("NORMAL_CRAFTING", "", "", "(IRecipe)recipes.get('Grate')", "Grate"));
        put(map, "TABLE",
                page("TEXT", "tc.research_page.TABLE.1", "", "'tc.research_page.TABLE.1'"),
                page("NORMAL_CRAFTING", "", "", "(IRecipe)recipes.get('Table')", "Table"));
        put(map, "ARCTABLE",
                page("TEXT", "tc.research_page.ARCTABLE.1", "", "'tc.research_page.ARCTABLE.1'"),
                page("NORMAL_CRAFTING", "", "", "(List)recipes.get('ArcTable')", "ArcTable"));
        put(map, "RESTABLE",
                page("TEXT", "tc.research_page.RESTABLE.1", "", "'tc.research_page.RESTABLE.1'"),
                page("NORMAL_CRAFTING", "", "", "(List)recipes.get('ResTable')", "ResTable"));
        put(map, "THAUMOMETER",
                page("TEXT", "tc.research_page.THAUMOMETER.1", "", "'tc.research_page.THAUMOMETER.1'"),
                page("NORMAL_CRAFTING", "", "", "(IRecipe)recipes.get('Thaumometer')", "Thaumometer"));
        put(map, "PAVETRAVEL",
                page("TEXT", "tc.research_page.PAVETRAVEL.1", "", "'tc.research_page.PAVETRAVEL.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('PaveTravel')", "PaveTravel"));
        put(map, "PAVEWARD",
                page("TEXT", "tc.research_page.PAVEWARD.1", "", "'tc.research_page.PAVEWARD.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('PaveWard')", "PaveWard"),
                page("TEXT", "tc.research_page.PAVEWARD.2", "", "'tc.research_page.PAVEWARD.2'"));
        put(map, "GOGGLES",
                page("TEXT", "tc.research_page.GOGGLES.1", "", "'tc.research_page.GOGGLES.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('Goggles')", "Goggles"));
        put(map, "ARCANEEAR",
                page("TEXT", "tc.research_page.ARCANEEAR.1", "", "'tc.research_page.ARCANEEAR.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('ArcaneEar')", "ArcaneEar"));
        put(map, "SINSTONE",
                page("TEXT", "tc.research_page.SINSTONE.1", "", "'tc.research_page.SINSTONE.1'"),
                page("INFUSION_CRAFTING", "", "", "(InfusionRecipe)recipes.get('SinStone')", "SinStone"));
        put(map, "LEVITATOR",
                page("TEXT", "tc.research_page.LEVITATOR.1", "", "'tc.research_page.LEVITATOR.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('Levitator')", "Levitator"));
        put(map, "INFERNALFURNACE",
                page("TEXT", "tc.research_page.INFERNALFURNACE.1", "", "'tc.research_page.INFERNALFURNACE.1'"),
                page("NORMAL_CRAFTING", "", "", "(List)recipes.get('InfernalFurnace')", "InfernalFurnace"),
                page("TEXT", "tc.research_page.INFERNALFURNACE.2", "", "'tc.research_page.INFERNALFURNACE.2'"));
        put(map, "BELLOWS",
                page("TEXT", "tc.research_page.BELLOWS.1", "", "'tc.research_page.BELLOWS.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('Bellows')", "Bellows"),
                page("TEXT", "tc.research_page.BELLOWS.2", "", "'tc.research_page.BELLOWS.2'"));
        put(map, "ENCHFABRIC",
                page("TEXT", "tc.research_page.ENCHFABRIC.1", "", "'tc.research_page.ENCHFABRIC.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('EnchantedFabric')", "EnchantedFabric"),
                page("TEXT", "tc.research_page.ENCHFABRIC.2", "", "'tc.research_page.ENCHFABRIC.2'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('RobeChest')", "RobeChest"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('RobeLegs')", "RobeLegs"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('RobeBoots')", "RobeBoots"));
        put(map, "INFUSION",
                page("TEXT", "tc.research_page.INFUSION.1", "", "'tc.research_page.INFUSION.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('InfusionMatrix')", "InfusionMatrix"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('ArcanePedestal')", "ArcanePedestal"),
                page("TEXT", "tc.research_page.INFUSION.2", "", "'tc.research_page.INFUSION.2'"),
                page("NORMAL_CRAFTING", "", "", "(List)recipes.get('InfusionAltar')", "InfusionAltar"),
                page("TEXT", "tc.research_page.INFUSION.3", "", "'tc.research_page.INFUSION.3'"),
                page("TEXT", "tc.research_page.INFUSION.4", "", "'tc.research_page.INFUSION.4'"),
                page("TEXT", "tc.research_page.INFUSION.5", "", "'tc.research_page.INFUSION.5'"));
        put(map, "FLUXSCRUB",
                page("TEXT", "tc.research_page.FLUXSCRUB.1", "", "'tc.research_page.FLUXSCRUB.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('FluxScrubber')", "FluxScrubber"));
        put(map, "RUNICARMOR",
                page("TEXT", "tc.research_page.RUNICARMOR.1", "", "'tc.research_page.RUNICARMOR.1'"),
                page("TEXT", "tc.research_page.RUNICARMOR.2", "", "'tc.research_page.RUNICARMOR.2'"),
                page("INFUSION_CRAFTING", "", "", "(InfusionRecipe)recipes.get('RunicRing')", "RunicRing"),
                page("INFUSION_CRAFTING", "", "", "(InfusionRecipe)recipes.get('RunicAmulet')", "RunicAmulet"),
                page("INFUSION_CRAFTING", "", "", "(InfusionRecipe)recipes.get('RunicGirdle')", "RunicGirdle"));
        put(map, "RUNICCHARGED",
                page("TEXT", "tc.research_page.RUNICCHARGED.1", "", "'tc.research_page.RUNICCHARGED.1'"),
                page("INFUSION_CRAFTING", "", "", "(InfusionRecipe)recipes.get('RunicRingCharged')", "RunicRingCharged"));
        put(map, "RUNICHEALING",
                page("TEXT", "tc.research_page.RUNICHEALING.1", "", "'tc.research_page.RUNICHEALING.1'"),
                page("INFUSION_CRAFTING", "", "", "(InfusionRecipe)recipes.get('RunicRingHealing')", "RunicRingHealing"));
        put(map, "RUNICKINETIC",
                page("TEXT", "tc.research_page.RUNICKINETIC.1", "", "'tc.research_page.RUNICKINETIC.1'"),
                page("INFUSION_CRAFTING", "", "", "(InfusionRecipe)recipes.get('RunicGirdleKinetic')", "RunicGirdleKinetic"));
        put(map, "RUNICEMERGENCY",
                page("TEXT", "tc.research_page.RUNICEMERGENCY.1", "", "'tc.research_page.RUNICEMERGENCY.1'"),
                page("INFUSION_CRAFTING", "", "", "(InfusionRecipe)recipes.get('RunicAmuletEmergency')", "RunicAmuletEmergency"));
        put(map, "BANNERS",
                page("TEXT", "tc.research_page.BANNERS.1", "", "'tc.research_page.BANNERS.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe[])banners.toArray(new IArcaneRecipe[0])"));
        put(map, "RUNICAUGMENTATION",
                page("TEXT", "tc.research_page.RUNICAUGMENTATION.1", "", "'tc.research_page.RUNICAUGMENTATION.1'"),
                page("INFUSION_CRAFTING", "", "", "(InfusionRecipe[])raug.toArray(new InfusionRecipe[0])"),
                page("TEXT", "tc.research_page.RUNICAUGMENTATION.2", "", "'tc.research_page.RUNICAUGMENTATION.2'"));
        put(map, "BOOTSTRAVELLER",
                page("TEXT", "tc.research_page.BOOTSTRAVELLER.1", "", "'tc.research_page.BOOTSTRAVELLER.1'"),
                page("INFUSION_CRAFTING", "", "", "(InfusionRecipe)recipes.get('BootsTraveller')", "BootsTraveller"));
        put(map, "HOVERHARNESS",
                page("TEXT", "tc.research_page.HOVERHARNESS.1", "", "'tc.research_page.HOVERHARNESS.1'"),
                page("INFUSION_CRAFTING", "", "", "(InfusionRecipe)recipes.get('HoverHarness')", "HoverHarness"),
                page("TEXT", "tc.research_page.HOVERHARNESS.2", "", "'tc.research_page.HOVERHARNESS.2'"));
        put(map, "HOVERGIRDLE",
                page("TEXT", "tc.research_page.HOVERGIRDLE.1", "", "'tc.research_page.HOVERGIRDLE.1'"),
                page("INFUSION_CRAFTING", "", "", "(InfusionRecipe)recipes.get('HoverGirdle')", "HoverGirdle"));
        put(map, "MIRROR",
                page("TEXT", "tc.research_page.MIRROR.1", "", "'tc.research_page.MIRROR.1'"),
                page("TEXT", "tc.research_page.MIRROR.2", "", "'tc.research_page.MIRROR.2'"),
                page("INFUSION_CRAFTING", "", "", "(InfusionRecipe)recipes.get('Mirror')", "Mirror"),
                page("TEXT", "tc.research_page.MIRROR.3", "", "'tc.research_page.MIRROR.3'"));
        put(map, "MIRRORHAND",
                page("TEXT", "tc.research_page.MIRRORHAND.1", "", "'tc.research_page.MIRRORHAND.1'"),
                page("INFUSION_CRAFTING", "", "", "(InfusionRecipe)recipes.get('MirrorHand')", "MirrorHand"));
        put(map, "MIRRORESSENTIA",
                page("TEXT", "tc.research_page.MIRRORESSENTIA.1", "", "'tc.research_page.MIRRORESSENTIA.1'"),
                page("INFUSION_CRAFTING", "", "", "(InfusionRecipe)recipes.get('MirrorEssentia')", "MirrorEssentia"),
                page("TEXT", "tc.research_page.MIRRORESSENTIA.2", "", "'tc.research_page.MIRRORESSENTIA.2'"));
        put(map, "ARCANEBORE",
                page("TEXT", "tc.research_page.ARCANEBORE.1", "", "'tc.research_page.ARCANEBORE.1'"),
                page("INFUSION_CRAFTING", "", "", "(InfusionRecipe)recipes.get('ArcaneBore')", "ArcaneBore"),
                page("TEXT", "tc.research_page.ARCANEBORE.2", "", "'tc.research_page.ARCANEBORE.2'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('ArcaneBoreBase')", "ArcaneBoreBase"),
                page("TEXT", "tc.research_page.ARCANEBORE.3", "", "'tc.research_page.ARCANEBORE.3'"));
        put(map, "ARCANELAMP",
                page("TEXT", "tc.research_page.ARCANELAMP.1", "", "'tc.research_page.ARCANELAMP.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('ArcaneLamp')", "ArcaneLamp"),
                page("TEXT_CONCEALED", "tc.research_page.ARCANELAMP.2", "ARCANEBORE", "'ARCANEBORE', 'tc.research_page.ARCANELAMP.2'"));
        put(map, "LAMPGROWTH",
                page("TEXT", "tc.research_page.LAMPGROWTH.1", "", "'tc.research_page.LAMPGROWTH.1'"),
                page("INFUSION_CRAFTING", "", "", "(InfusionRecipe)recipes.get('LampGrowth')", "LampGrowth"));
        put(map, "LAMPFERTILITY",
                page("TEXT", "tc.research_page.LAMPFERTILITY.1", "", "'tc.research_page.LAMPFERTILITY.1'"),
                page("INFUSION_CRAFTING", "", "", "(InfusionRecipe)recipes.get('LampFertility')", "LampFertility"));
        put(map, "BONEBOW",
                page("TEXT", "tc.research_page.BONEBOW.1", "", "'tc.research_page.BONEBOW.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('BoneBow')", "BoneBow"));
        put(map, "PRIMALARROW",
                page("TEXT", "tc.research_page.PRIMALARROW.1", "", "'tc.research_page.PRIMALARROW.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe[])rcbb.toArray(new IArcaneRecipe[0])"),
                page("TEXT", "tc.research_page.PRIMALARROW.2", "", "'tc.research_page.PRIMALARROW.2'"),
                page("TEXT", "tc.research_page.PRIMALARROW.3", "", "'tc.research_page.PRIMALARROW.3'"));
        put(map, "ELEMENTALAXE",
                page("TEXT", "tc.research_page.ELEMENTALAXE.1", "", "'tc.research_page.ELEMENTALAXE.1'"),
                page("INFUSION_CRAFTING", "", "", "(InfusionRecipe)recipes.get('ElementalAxe')", "ElementalAxe"),
                page("TEXT", "tc.research_page.ELEMENTALAXE.2", "", "'tc.research_page.ELEMENTALAXE.2'"));
        put(map, "ELEMENTALPICK",
                page("TEXT", "tc.research_page.ELEMENTALPICK.1", "", "'tc.research_page.ELEMENTALPICK.1'"),
                page("INFUSION_CRAFTING", "", "", "(InfusionRecipe)recipes.get('ElementalPick')", "ElementalPick"),
                page("TEXT", "tc.research_page.ELEMENTALPICK.2", "", "'tc.research_page.ELEMENTALPICK.2'"));
        put(map, "ELEMENTALSWORD",
                page("TEXT", "tc.research_page.ELEMENTALSWORD.1", "", "'tc.research_page.ELEMENTALSWORD.1'"),
                page("INFUSION_CRAFTING", "", "", "(InfusionRecipe)recipes.get('ElementalSword')", "ElementalSword"));
        put(map, "ELEMENTALSHOVEL",
                page("TEXT", "tc.research_page.ELEMENTALSHOVEL.1", "", "'tc.research_page.ELEMENTALSHOVEL.1'"),
                page("INFUSION_CRAFTING", "", "", "(InfusionRecipe)recipes.get('ElementalShovel')", "ElementalShovel"),
                page("TEXT", "tc.research_page.ELEMENTALSHOVEL.2", "", "'tc.research_page.ELEMENTALSHOVEL.2'"));
        put(map, "ELEMENTALHOE",
                page("TEXT", "tc.research_page.ELEMENTALHOE.1", "", "'tc.research_page.ELEMENTALHOE.1'"),
                page("INFUSION_CRAFTING", "", "", "(InfusionRecipe)recipes.get('ElementalHoe')", "ElementalHoe"));
        put(map, "WARDEDARCANA",
                page("TEXT", "tc.research_page.WARDEDARCANA.1", "", "'tc.research_page.WARDEDARCANA.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('ArcaneDoor')", "ArcaneDoor"),
                page("TEXT", "tc.research_page.WARDEDARCANA.2", "", "'tc.research_page.WARDEDARCANA.2'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('IronKey')", "IronKey"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('GoldKey')", "GoldKey"),
                page("TEXT", "tc.research_page.WARDEDARCANA.3", "", "'tc.research_page.WARDEDARCANA.3'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('ArcanePressurePlate')", "ArcanePressurePlate"),
                page("TEXT", "tc.research_page.WARDEDARCANA.4", "", "'tc.research_page.WARDEDARCANA.4'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('WardedGlass')", "WardedGlass"));
        put(map, "JARBRAIN",
                page("TEXT", "tc.research_page.JARBRAIN.1", "", "'tc.research_page.JARBRAIN.1'"),
                page("INFUSION_CRAFTING", "", "", "(InfusionRecipe)recipes.get('JarBrain')", "JarBrain"));
        put(map, "INFUSIONENCHANTMENT",
                page("TEXT", "tc.research_page.INFUSIONENCHANTMENT.1", "", "'tc.research_page.INFUSIONENCHANTMENT.1'"),
                page("TEXT", "tc.research_page.INFUSIONENCHANTMENT.2", "", "'tc.research_page.INFUSIONENCHANTMENT.2'"),
                page("TEXT", "tc.research_page.INFUSIONENCHANTMENT.3", "", "'tc.research_page.INFUSIONENCHANTMENT.3'"),
                page("INFUSION_ENCHANTMENT", "", "", "(InfusionEnchantmentRecipe)recipes.get('InfEnchRepair')", "InfEnchRepair"),
                page("INFUSION_ENCHANTMENT", "", "", "(InfusionEnchantmentRecipe)recipes.get('InfEnchHaste')", "InfEnchHaste"),
                page("INFUSION_ENCHANTMENT", "", "", "(InfusionEnchantmentRecipe)recipes.get('InfEnch0')", "InfEnch0"),
                page("INFUSION_ENCHANTMENT", "", "", "(InfusionEnchantmentRecipe)recipes.get('InfEnch1')", "InfEnch1"),
                page("INFUSION_ENCHANTMENT", "", "", "(InfusionEnchantmentRecipe)recipes.get('InfEnch2')", "InfEnch2"),
                page("INFUSION_ENCHANTMENT", "", "", "(InfusionEnchantmentRecipe)recipes.get('InfEnch3')", "InfEnch3"),
                page("INFUSION_ENCHANTMENT", "", "", "(InfusionEnchantmentRecipe)recipes.get('InfEnch4')", "InfEnch4"),
                page("INFUSION_ENCHANTMENT", "", "", "(InfusionEnchantmentRecipe)recipes.get('InfEnch5')", "InfEnch5"),
                page("INFUSION_ENCHANTMENT", "", "", "(InfusionEnchantmentRecipe)recipes.get('InfEnch6')", "InfEnch6"),
                page("INFUSION_ENCHANTMENT", "", "", "(InfusionEnchantmentRecipe)recipes.get('InfEnch7')", "InfEnch7"),
                page("INFUSION_ENCHANTMENT", "", "", "(InfusionEnchantmentRecipe)recipes.get('InfEnch8')", "InfEnch8"),
                page("INFUSION_ENCHANTMENT", "", "", "(InfusionEnchantmentRecipe)recipes.get('InfEnch9')", "InfEnch9"),
                page("INFUSION_ENCHANTMENT", "", "", "(InfusionEnchantmentRecipe)recipes.get('InfEnch10')", "InfEnch10"),
                page("INFUSION_ENCHANTMENT", "", "", "(InfusionEnchantmentRecipe)recipes.get('InfEnch11')", "InfEnch11"),
                page("INFUSION_ENCHANTMENT", "", "", "(InfusionEnchantmentRecipe)recipes.get('InfEnch12')", "InfEnch12"),
                page("INFUSION_ENCHANTMENT", "", "", "(InfusionEnchantmentRecipe)recipes.get('InfEnch13')", "InfEnch13"),
                page("INFUSION_ENCHANTMENT", "", "", "(InfusionEnchantmentRecipe)recipes.get('InfEnch14')", "InfEnch14"),
                page("INFUSION_ENCHANTMENT", "", "", "(InfusionEnchantmentRecipe)recipes.get('InfEnch15')", "InfEnch15"),
                page("INFUSION_ENCHANTMENT", "", "", "(InfusionEnchantmentRecipe)recipes.get('InfEnch16')", "InfEnch16"),
                page("INFUSION_ENCHANTMENT", "", "", "(InfusionEnchantmentRecipe)recipes.get('InfEnch17')", "InfEnch17"),
                page("INFUSION_ENCHANTMENT", "", "", "(InfusionEnchantmentRecipe)recipes.get('InfEnch18')", "InfEnch18"),
                page("INFUSION_ENCHANTMENT", "", "", "(InfusionEnchantmentRecipe)recipes.get('InfEnch19')", "InfEnch19"),
                page("INFUSION_ENCHANTMENT", "", "", "(InfusionEnchantmentRecipe)recipes.get('InfEnch20')", "InfEnch20"),
                page("INFUSION_ENCHANTMENT", "", "", "(InfusionEnchantmentRecipe)recipes.get('InfEnch21')", "InfEnch21"));
        put(map, "ARMORFORTRESS",
                page("TEXT", "tc.research_page.ARMORFORTRESS.1", "", "'tc.research_page.ARMORFORTRESS.1'"),
                page("TEXT", "tc.research_page.ARMORFORTRESS.2", "", "'tc.research_page.ARMORFORTRESS.2'"),
                page("INFUSION_CRAFTING", "", "", "(InfusionRecipe)recipes.get('ThaumiumFortressHelm')", "ThaumiumFortressHelm"),
                page("INFUSION_CRAFTING", "", "", "(InfusionRecipe)recipes.get('ThaumiumFortressChest')", "ThaumiumFortressChest"),
                page("INFUSION_CRAFTING", "", "", "(InfusionRecipe)recipes.get('ThaumiumFortressLegs')", "ThaumiumFortressLegs"));
        put(map, "HELMGOGGLES",
                page("TEXT", "tc.research_page.HELMGOGGLES.1", "", "'tc.research_page.HELMGOGGLES.1'"),
                page("INFUSION_CRAFTING", "", "", "(InfusionRecipe)recipes.get('HelmGoggles')", "HelmGoggles"));
        put(map, "MASKGRINNINGDEVIL",
                page("TEXT", "tc.research_page.MASKGRINNINGDEVIL.1", "", "'tc.research_page.MASKGRINNINGDEVIL.1'"),
                page("INFUSION_CRAFTING", "", "", "(InfusionRecipe)recipes.get('MaskGrinningDevil')", "MaskGrinningDevil"));
        put(map, "MASKANGRYGHOST",
                page("TEXT", "tc.research_page.MASKANGRYGHOST.1", "", "'tc.research_page.MASKANGRYGHOST.1'"),
                page("INFUSION_CRAFTING", "", "", "(InfusionRecipe)recipes.get('MaskAngryGhost')", "MaskAngryGhost"));
        put(map, "MASKSIPPINGFIEND",
                page("TEXT", "tc.research_page.MASKSIPPINGFIEND.1", "", "'tc.research_page.MASKSIPPINGFIEND.1'"),
                page("INFUSION_CRAFTING", "", "", "(InfusionRecipe)recipes.get('MaskSippingFiend')", "MaskSippingFiend"));
    }

    private static void addALCHEMY(Map<String, List<PageSpec>> map) {
        put(map, "PHIAL",
                page("TEXT", "tc.research_page.PHIAL.1", "", "'tc.research_page.PHIAL.1'"),
                page("NORMAL_CRAFTING", "", "", "(IRecipe)recipes.get('Phial')", "Phial"));
        put(map, "CRUCIBLE",
                page("TEXT", "tc.research_page.CRUCIBLE.1", "", "'tc.research_page.CRUCIBLE.1'"),
                page("TEXT", "tc.research_page.CRUCIBLE.2", "", "'tc.research_page.CRUCIBLE.2'"),
                page("TEXT", "tc.research_page.CRUCIBLE.3", "", "'tc.research_page.CRUCIBLE.3'"),
                page("NORMAL_CRAFTING", "", "", "(List)recipes.get('Crucible')", "Crucible"),
                page("TEXT", "tc.research_page.CRUCIBLE.4", "", "'tc.research_page.CRUCIBLE.4'"),
                page("CRUCIBLE_CRAFTING", "", "", "(CrucibleRecipe[])cruc.toArray(new CrucibleRecipe[0])"),
                page("TEXT", "tc.research_page.CRUCIBLE.5", "", "'tc.research_page.CRUCIBLE.5'"),
                page("UNKNOWN", "", "", "new ItemStack(ConfigItems.itemShard, 1, 6)"));
        put(map, "NITOR",
                page("TEXT", "tc.research_page.NITOR.1", "", "'tc.research_page.NITOR.1'"),
                page("CRUCIBLE_CRAFTING", "", "", "(CrucibleRecipe)recipes.get('Nitor')", "Nitor"));
        put(map, "ALUMENTUM",
                page("TEXT", "tc.research_page.ALUMENTUM.1", "", "'tc.research_page.ALUMENTUM.1'"),
                page("CRUCIBLE_CRAFTING", "", "", "(CrucibleRecipe)recipes.get('Alumentum')", "Alumentum"));
        put(map, "ALCHEMICALDUPLICATION",
                page("TEXT", "tc.research_page.ALCHEMICALDUPLICATION.1", "", "'tc.research_page.ALCHEMICALDUPLICATION.1'"),
                page("CRUCIBLE_CRAFTING", "", "", "(CrucibleRecipe)recipes.get('AltGunpowder')", "AltGunpowder"),
                page("CRUCIBLE_CRAFTING", "", "", "(CrucibleRecipe)recipes.get('AltSlime')", "AltSlime"),
                page("CRUCIBLE_CRAFTING", "", "", "(CrucibleRecipe)recipes.get('AltClay')", "AltClay"),
                page("CRUCIBLE_CRAFTING", "", "", "(CrucibleRecipe)recipes.get('AltGlowstone')", "AltGlowstone"),
                page("CRUCIBLE_CRAFTING", "", "", "(CrucibleRecipe)recipes.get('AltInk')", "AltInk"));
        put(map, "ALCHEMICALMANUFACTURE",
                page("TEXT", "tc.research_page.ALCHEMICALMANUFACTURE.1", "", "'tc.research_page.ALCHEMICALMANUFACTURE.1'"),
                page("CRUCIBLE_CRAFTING", "", "", "(CrucibleRecipe)recipes.get('AltWeb')", "AltWeb"),
                page("CRUCIBLE_CRAFTING", "", "", "(CrucibleRecipe)recipes.get('AltMossyCobble')", "AltMossyCobble"),
                page("CRUCIBLE_CRAFTING", "", "", "(CrucibleRecipe)recipes.get('AltIce')", "AltIce"));
        put(map, "ENTROPICPROCESSING",
                page("TEXT", "tc.research_page.ENTROPICPROCESSING.1", "", "'tc.research_page.ENTROPICPROCESSING.1'"),
                page("CRUCIBLE_CRAFTING", "", "", "(CrucibleRecipe)recipes.get('AltCrackedBrick')", "AltCrackedBrick"),
                page("CRUCIBLE_CRAFTING", "", "", "(CrucibleRecipe)recipes.get('AltBonemeal')", "AltBonemeal"));
        put(map, "LIQUIDDEATH",
                page("TEXT", "tc.research_page.LIQUIDDEATH.1", "", "'tc.research_page.LIQUIDDEATH.1'"),
                page("CRUCIBLE_CRAFTING", "", "", "(CrucibleRecipe)recipes.get('LiquidDeath')", "LiquidDeath"));
        put(map, "BOTTLETAINT",
                page("TEXT", "tc.research_page.BOTTLETAINT.1", "", "'tc.research_page.BOTTLETAINT.1'"),
                page("CRUCIBLE_CRAFTING", "", "", "(CrucibleRecipe)recipes.get('BottleTaint')", "BottleTaint"));
        put(map, "THAUMIUM",
                page("TEXT", "tc.research_page.THAUMIUM.1", "", "'tc.research_page.THAUMIUM.1'"),
                page("CRUCIBLE_CRAFTING", "", "", "(CrucibleRecipe)recipes.get('Thaumium')", "Thaumium"),
                page("NORMAL_CRAFTING", "", "", "(IRecipe)recipes.get('ThaumiumAxe')", "ThaumiumAxe"),
                page("NORMAL_CRAFTING", "", "", "(IRecipe)recipes.get('ThaumiumSword')", "ThaumiumSword"),
                page("NORMAL_CRAFTING", "", "", "(IRecipe)recipes.get('ThaumiumPick')", "ThaumiumPick"),
                page("NORMAL_CRAFTING", "", "", "(IRecipe)recipes.get('ThaumiumShovel')", "ThaumiumShovel"),
                page("NORMAL_CRAFTING", "", "", "(IRecipe)recipes.get('ThaumiumHoe')", "ThaumiumHoe"),
                page("NORMAL_CRAFTING", "", "", "(IRecipe)recipes.get('ThaumiumHelm')", "ThaumiumHelm"),
                page("NORMAL_CRAFTING", "", "", "(IRecipe)recipes.get('ThaumiumChest')", "ThaumiumChest"),
                page("NORMAL_CRAFTING", "", "", "(IRecipe)recipes.get('ThaumiumLegs')", "ThaumiumLegs"),
                page("NORMAL_CRAFTING", "", "", "(IRecipe)recipes.get('ThaumiumBoots')", "ThaumiumBoots"));
        put(map, "PUREIRON",
                page("TEXT", "tc.research_page.PUREIRON.1", "", "'tc.research_page.PUREIRON.1'"),
                page("CRUCIBLE_CRAFTING", "", "", "(CrucibleRecipe)recipes.get('PureIron')", "PureIron"));
        put(map, "PUREGOLD",
                page("TEXT", "tc.research_page.PUREGOLD.1", "", "'tc.research_page.PUREGOLD.1'"),
                page("CRUCIBLE_CRAFTING", "", "", "(CrucibleRecipe)recipes.get('PureGold')", "PureGold"));
        put(map, "PURECOPPER",
                page("TEXT", "tc.research_page.PURECOPPER.1", "", "'tc.research_page.PURECOPPER.1'"),
                page("CRUCIBLE_CRAFTING", "", "", "(CrucibleRecipe)recipes.get('PureCopper')", "PureCopper"));
        put(map, "PURETIN",
                page("TEXT", "tc.research_page.PURETIN.1", "", "'tc.research_page.PURETIN.1'"),
                page("CRUCIBLE_CRAFTING", "", "", "(CrucibleRecipe)recipes.get('PureTin')", "PureTin"));
        put(map, "PURESILVER",
                page("TEXT", "tc.research_page.PURESILVER.1", "", "'tc.research_page.PURESILVER.1'"),
                page("CRUCIBLE_CRAFTING", "", "", "(CrucibleRecipe)recipes.get('PureSilver')", "PureSilver"));
        put(map, "PURELEAD",
                page("TEXT", "tc.research_page.PURELEAD.1", "", "'tc.research_page.PURELEAD.1'"),
                page("CRUCIBLE_CRAFTING", "", "", "(CrucibleRecipe)recipes.get('PureLead')", "PureLead"));
        put(map, "TRANSIRON",
                page("TEXT", "tc.research_page.TRANSIRON.1", "", "'tc.research_page.TRANSIRON.1'"),
                page("CRUCIBLE_CRAFTING", "", "", "(CrucibleRecipe)recipes.get('TransIron')", "TransIron"));
        put(map, "TRANSGOLD",
                page("TEXT", "tc.research_page.TRANSGOLD.1", "", "'tc.research_page.TRANSGOLD.1'"),
                page("CRUCIBLE_CRAFTING", "", "", "(CrucibleRecipe)recipes.get('TransGold')", "TransGold"));
        put(map, "TRANSCOPPER",
                page("TEXT", "tc.research_page.TRANSCOPPER.1", "", "'tc.research_page.TRANSCOPPER.1'"),
                page("CRUCIBLE_CRAFTING", "", "", "(CrucibleRecipe)recipes.get('TransCopper')", "TransCopper"));
        put(map, "TRANSTIN",
                page("TEXT", "tc.research_page.TRANSTIN.1", "", "'tc.research_page.TRANSTIN.1'"),
                page("CRUCIBLE_CRAFTING", "", "", "(CrucibleRecipe)recipes.get('TransTin')", "TransTin"));
        put(map, "TRANSSILVER",
                page("TEXT", "tc.research_page.TRANSSILVER.1", "", "'tc.research_page.TRANSSILVER.1'"),
                page("CRUCIBLE_CRAFTING", "", "", "(CrucibleRecipe)recipes.get('TransSilver')", "TransSilver"));
        put(map, "TRANSLEAD",
                page("TEXT", "tc.research_page.TRANSLEAD.1", "", "'tc.research_page.TRANSLEAD.1'"),
                page("CRUCIBLE_CRAFTING", "", "", "(CrucibleRecipe)recipes.get('TransLead')", "TransLead"));
        put(map, "TALLOW",
                page("TEXT", "tc.research_page.TALLOW.1", "", "'tc.research_page.TALLOW.1'"),
                page("CRUCIBLE_CRAFTING", "", "", "(CrucibleRecipe)recipes.get('Tallow')", "Tallow"),
                page("NORMAL_CRAFTING", "", "", "(IRecipe)recipes.get('TallowCandle')", "TallowCandle"));
        put(map, "ETHEREALBLOOM",
                page("TEXT", "tc.research_page.ETHEREALBLOOM.1", "", "'tc.research_page.ETHEREALBLOOM.1'"),
                page("CRUCIBLE_CRAFTING", "", "", "(CrucibleRecipe)recipes.get('EtherealBloom')", "EtherealBloom"),
                page("TEXT", "tc.research_page.ETHEREALBLOOM.2", "", "'tc.research_page.ETHEREALBLOOM.2'"));
        put(map, "BATHSALTS",
                page("TEXT", "tc.research_page.BATHSALTS.1", "", "'tc.research_page.BATHSALTS.1'"),
                page("CRUCIBLE_CRAFTING", "", "", "(CrucibleRecipe)recipes.get('BathSalts')", "BathSalts"));
        put(map, "SANESOAP",
                page("TEXT", "tc.research_page.SANESOAP.1", "", "'tc.research_page.SANESOAP.1'"),
                page("CRUCIBLE_CRAFTING", "", "", "(CrucibleRecipe)recipes.get('SaneSoap')", "SaneSoap"));
        put(map, "ARCANESPA",
                page("TEXT", "tc.research_page.ARCANESPA.1", "", "'tc.research_page.ARCANESPA.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('ArcaneSpa')", "ArcaneSpa"));
        put(map, "DISTILESSENTIA",
                page("TEXT", "tc.research_page.DISTILESSENTIA.1", "", "'tc.research_page.DISTILESSENTIA.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('AlchemyFurnace')", "AlchemyFurnace"),
                page("TEXT", "tc.research_page.DISTILESSENTIA.2", "", "'tc.research_page.DISTILESSENTIA.2'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('Filter')", "Filter"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('Alembic')", "Alembic"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('AlchemicalConstruct')", "AlchemicalConstruct"));
        put(map, "JARLABEL",
                page("TEXT", "tc.research_page.JARLABEL.1", "", "'tc.research_page.JARLABEL.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('WardedJar')", "WardedJar"),
                page("TEXT", "tc.research_page.JARLABEL.2", "", "'tc.research_page.JARLABEL.2'"),
                page("NORMAL_CRAFTING", "", "", "(IRecipe)recipes.get('JarLabel')", "JarLabel"),
                page("TEXT", "tc.research_page.JARLABEL.3", "", "'tc.research_page.JARLABEL.3'"),
                page("NORMAL_CRAFTING", "", "", "(IRecipe[])rc.toArray(new IRecipe[0])"),
                page("NORMAL_CRAFTING", "", "", "(IRecipe)recipes.get('JarLabelNull')", "JarLabelNull"));
        put(map, "JARVOID",
                page("TEXT", "tc.research_page.JARVOID.1", "", "'tc.research_page.JARVOID.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('JarVoid')", "JarVoid"));
        put(map, "TUBES",
                page("TEXT", "tc.research_page.TUBES.1", "", "'tc.research_page.TUBES.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('Tube')", "Tube"),
                page("TEXT", "tc.research_page.TUBES.2", "", "'tc.research_page.TUBES.2'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('TubeValve')", "TubeValve"),
                page("TEXT", "tc.research_page.TUBES.3", "", "'tc.research_page.TUBES.3'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('Resonator')", "Resonator"),
                page("TEXT", "tc.research_page.TUBES.4", "", "'tc.research_page.TUBES.4'"));
        put(map, "TUBEFILTER",
                page("TEXT", "tc.research_page.TUBEFILTER.1", "", "'tc.research_page.TUBEFILTER.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('TubeFilter')", "TubeFilter"),
                page("TEXT", "tc.research_page.TUBEFILTER.2", "", "'tc.research_page.TUBEFILTER.2'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('TubeRestrict')", "TubeRestrict"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('TubeOneway')", "TubeOneway"));
        put(map, "ESSENTIACRYSTAL",
                page("TEXT", "tc.research_page.ESSENTIACRYSTAL.1", "", "'tc.research_page.ESSENTIACRYSTAL.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('EssentiaCrystalizer')", "EssentiaCrystalizer"));
        put(map, "CENTRIFUGE",
                page("TEXT", "tc.research_page.CENTRIFUGE.1", "", "'tc.research_page.CENTRIFUGE.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('Centrifuge')", "Centrifuge"),
                page("TEXT", "tc.research_page.CENTRIFUGE.2", "", "'tc.research_page.CENTRIFUGE.2'"),
                page("TEXT", "tc.research_page.CENTRIFUGE.3", "", "'tc.research_page.CENTRIFUGE.3'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('TubeBuffer')", "TubeBuffer"));
        put(map, "THAUMATORIUM",
                page("TEXT", "tc.research_page.THAUMATORIUM.1", "", "'tc.research_page.THAUMATORIUM.1'"),
                page("NORMAL_CRAFTING", "", "", "(List)recipes.get('Thaumatorium')", "Thaumatorium"),
                page("TEXT", "tc.research_page.THAUMATORIUM.2", "", "'tc.research_page.THAUMATORIUM.2'"),
                page("TEXT", "tc.research_page.THAUMATORIUM.3", "", "'tc.research_page.THAUMATORIUM.3'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('MnemonicMatrix')", "MnemonicMatrix"));
    }

    private static void addGOLEMANCY(Map<String, List<PageSpec>> map) {
        put(map, "HUNGRYCHEST",
                page("TEXT", "tc.research_page.HUNGRYCHEST.1", "", "'tc.research_page.HUNGRYCHEST.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('HungryChest')", "HungryChest"));
        put(map, "GOLEMFETTER",
                page("TEXT", "tc.research_page.GOLEMFETTER.1", "", "'tc.research_page.GOLEMFETTER.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('GolemFetter')", "GolemFetter"));
        put(map, "TRAVELTRUNK",
                page("TEXT", "tc.research_page.TRAVELTRUNK.1", "", "'tc.research_page.TRAVELTRUNK.1'"),
                page("INFUSION_CRAFTING", "", "", "(InfusionRecipe)recipes.get('TravelTrunk')", "TravelTrunk"),
                page("TEXT", "tc.research_page.TRAVELTRUNK.2", "", "'tc.research_page.TRAVELTRUNK.2'"),
                page("TEXT_CONCEALED", "tc.research_page.TRAVELTRUNK.UAI", "UPGRADEAIR", "'UPGRADEAIR', 'tc.research_page.TRAVELTRUNK.UAI'"),
                page("TEXT_CONCEALED", "tc.research_page.TRAVELTRUNK.UEA", "UPGRADEEARTH", "'UPGRADEEARTH', 'tc.research_page.TRAVELTRUNK.UEA'"),
                page("TEXT_CONCEALED", "tc.research_page.TRAVELTRUNK.UFI", "UPGRADEFIRE", "'UPGRADEFIRE', 'tc.research_page.TRAVELTRUNK.UFI'"),
                page("TEXT_CONCEALED", "tc.research_page.TRAVELTRUNK.UWA", "UPGRADEWATER", "'UPGRADEWATER', 'tc.research_page.TRAVELTRUNK.UWA'"),
                page("TEXT_CONCEALED", "tc.research_page.TRAVELTRUNK.UOR", "UPGRADEORDER", "'UPGRADEORDER', 'tc.research_page.TRAVELTRUNK.UOR'"),
                page("TEXT_CONCEALED", "tc.research_page.TRAVELTRUNK.UEN", "UPGRADEENTROPY", "'UPGRADEENTROPY', 'tc.research_page.TRAVELTRUNK.UEN'"));
        put(map, "GOLEMSTRAW",
                page("TEXT", "tc.research_page.GOLEMSTRAW.1", "", "'tc.research_page.GOLEMSTRAW.1'"),
                page("TEXT", "tc.research_page.GOLEMSTRAW.2", "", "'tc.research_page.GOLEMSTRAW.2'"),
                page("CRUCIBLE_CRAFTING", "", "", "(CrucibleRecipe)recipes.get('GolemStraw')", "GolemStraw"),
                page("TEXT", "tc.research_page.GOLEMSTRAW.3", "", "'tc.research_page.GOLEMSTRAW.3'"));
        put(map, "GOLEMWOOD",
                page("TEXT", "tc.research_page.GOLEMWOOD.1", "", "'tc.research_page.GOLEMWOOD.1'"),
                page("CRUCIBLE_CRAFTING", "", "", "(CrucibleRecipe)recipes.get('GolemWood')", "GolemWood"));
        put(map, "GOLEMTALLOW",
                page("TEXT", "tc.research_page.GOLEMTALLOW.1", "", "'tc.research_page.GOLEMTALLOW.1'"),
                page("NORMAL_CRAFTING", "", "", "(IRecipe)recipes.get('BlockTallow')", "BlockTallow"),
                page("CRUCIBLE_CRAFTING", "", "", "(CrucibleRecipe)recipes.get('GolemTallow')", "GolemTallow"));
        put(map, "GOLEMCLAY",
                page("TEXT", "tc.research_page.GOLEMCLAY.1", "", "'tc.research_page.GOLEMCLAY.1'"),
                page("CRUCIBLE_CRAFTING", "", "", "(CrucibleRecipe)recipes.get('GolemClay')", "GolemClay"));
        put(map, "GOLEMFLESH",
                page("TEXT", "tc.research_page.GOLEMFLESH.1", "", "'tc.research_page.GOLEMFLESH.1'"),
                page("NORMAL_CRAFTING", "", "", "(IRecipe)recipes.get('BlockFlesh')", "BlockFlesh"),
                page("CRUCIBLE_CRAFTING", "", "", "(CrucibleRecipe)recipes.get('GolemFlesh')", "GolemFlesh"));
        put(map, "GOLEMSTONE",
                page("TEXT", "tc.research_page.GOLEMSTONE.1", "", "'tc.research_page.GOLEMSTONE.1'"),
                page("CRUCIBLE_CRAFTING", "", "", "(CrucibleRecipe)recipes.get('GolemStone')", "GolemStone"));
        put(map, "GOLEMIRON",
                page("TEXT", "tc.research_page.GOLEMIRON.1", "", "'tc.research_page.GOLEMIRON.1'"),
                page("CRUCIBLE_CRAFTING", "", "", "(CrucibleRecipe)recipes.get('GolemIron')", "GolemIron"));
        put(map, "GOLEMTHAUMIUM",
                page("TEXT", "tc.research_page.GOLEMTHAUMIUM.1", "", "'tc.research_page.GOLEMTHAUMIUM.1'"),
                page("NORMAL_CRAFTING", "", "", "(IRecipe)recipes.get('BlockThaumium')", "BlockThaumium"),
                page("CRUCIBLE_CRAFTING", "", "", "(CrucibleRecipe)recipes.get('GolemThaumium')", "GolemThaumium"));
        put(map, "GOLEMBELL",
                page("TEXT", "tc.research_page.GOLEMBELL.1", "", "'tc.research_page.GOLEMBELL.1'"),
                page("TEXT", "tc.research_page.GOLEMBELL.2", "", "'tc.research_page.GOLEMBELL.2'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('GolemBell')", "GolemBell"));
        put(map, "COREGATHER",
                page("TEXT", "tc.research_page.COREGATHER.1", "", "'tc.research_page.COREGATHER.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('CoreBlank')", "CoreBlank"),
                page("TEXT", "tc.research_page.COREGATHER.2", "", "'tc.research_page.COREGATHER.2'"),
                page("CRUCIBLE_CRAFTING", "", "", "(CrucibleRecipe)recipes.get('CoreGather')", "CoreGather"));
        put(map, "COREFILL",
                page("TEXT", "tc.research_page.COREFILL.1", "", "'tc.research_page.COREFILL.1'"),
                page("CRUCIBLE_CRAFTING", "", "", "(CrucibleRecipe)recipes.get('CoreFill')", "CoreFill"));
        put(map, "COREEMPTY",
                page("TEXT", "tc.research_page.COREEMPTY.1", "", "'tc.research_page.COREEMPTY.1'"),
                page("CRUCIBLE_CRAFTING", "", "", "(CrucibleRecipe)recipes.get('CoreEmpty')", "CoreEmpty"));
        put(map, "CORESORTING",
                page("TEXT", "tc.research_page.CORESORTING.1", "", "'tc.research_page.CORESORTING.1'"),
                page("INFUSION_CRAFTING", "", "", "(InfusionRecipe)recipes.get('CoreSorting')", "CoreSorting"));
        put(map, "COREUSE",
                page("TEXT", "tc.research_page.COREUSE.1", "", "'tc.research_page.COREUSE.1'"),
                page("TEXT", "tc.research_page.COREUSE.2", "", "'tc.research_page.COREUSE.2'"),
                page("INFUSION_CRAFTING", "", "", "(InfusionRecipe)recipes.get('CoreUse')", "CoreUse"),
                page("TEXT_CONCEALED", "tc.research_page.COREUSE.3", "UPGRADEAIR", "'UPGRADEAIR', 'tc.research_page.COREUSE.3'"));
        put(map, "COREHARVEST",
                page("TEXT", "tc.research_page.COREHARVEST.1", "", "'tc.research_page.COREHARVEST.1'"),
                page("CRUCIBLE_CRAFTING", "", "", "(CrucibleRecipe)recipes.get('CoreHarvest')", "CoreHarvest"),
                page("TEXT_CONCEALED", "tc.research_page.COREHARVEST.2", "UPGRADEORDER", "'UPGRADEORDER', 'tc.research_page.COREHARVEST.2'"));
        put(map, "COREFISHING",
                page("TEXT", "tc.research_page.COREFISHING.1", "", "'tc.research_page.COREFISHING.1'"),
                page("INFUSION_CRAFTING", "", "", "(InfusionRecipe)recipes.get('CoreFishing')", "CoreFishing"),
                page("TEXT_CONCEALED", "tc.research_page.COREFISHING.2", "UPGRADEAIR", "'UPGRADEAIR', 'tc.research_page.COREFISHING.2'"),
                page("TEXT_CONCEALED", "tc.research_page.COREFISHING.3", "UPGRADEFIRE", "'UPGRADEFIRE', 'tc.research_page.COREFISHING.3'"),
                page("TEXT_CONCEALED", "tc.research_page.COREFISHING.4", "UPGRADEORDER", "'UPGRADEORDER', 'tc.research_page.COREFISHING.4'"),
                page("TEXT_CONCEALED", "tc.research_page.COREFISHING.5", "UPGRADEENTROPY", "'UPGRADEENTROPY', 'tc.research_page.COREFISHING.5'"));
        put(map, "CORELUMBER",
                page("TEXT", "tc.research_page.CORELUMBER.1", "", "'tc.research_page.CORELUMBER.1'"),
                page("INFUSION_CRAFTING", "", "", "(InfusionRecipe)recipes.get('CoreLumber')", "CoreLumber"));
        put(map, "COREGUARD",
                page("TEXT", "tc.research_page.COREGUARD.1", "", "'tc.research_page.COREGUARD.1'"),
                page("CRUCIBLE_CRAFTING", "", "", "(CrucibleRecipe)recipes.get('CoreGuard')", "CoreGuard"),
                page("TEXT_CONCEALED", "tc.research_page.COREGUARD.2", "UPGRADEORDER", "'UPGRADEORDER', 'tc.research_page.COREGUARD.2'"));
        put(map, "COREBUTCHER",
                page("TEXT", "tc.research_page.COREBUTCHER.1", "", "'tc.research_page.COREBUTCHER.1'"),
                page("CRUCIBLE_CRAFTING", "", "", "(CrucibleRecipe)recipes.get('CoreButcher')", "CoreButcher"));
        put(map, "CORELIQUID",
                page("TEXT", "tc.research_page.CORELIQUID.1", "", "'tc.research_page.CORELIQUID.1'"),
                page("CRUCIBLE_CRAFTING", "", "", "(CrucibleRecipe)recipes.get('CoreLiquid')", "CoreLiquid"),
                page("TEXT_CONCEALED", "tc.research_page.CORELIQUID.2", "UPGRADEENTROPY", "'UPGRADEENTROPY', 'tc.research_page.CORELIQUID.2'"));
        put(map, "COREALCHEMY",
                page("TEXT", "tc.research_page.COREALCHEMY.1", "", "'tc.research_page.COREALCHEMY.1'"),
                page("INFUSION_CRAFTING", "", "", "(InfusionRecipe)recipes.get('CoreAlchemy')", "CoreAlchemy"),
                page("TEXT", "tc.research_page.COREALCHEMY.2", "", "'tc.research_page.COREALCHEMY.2'"));
        put(map, "UPGRADEAIR",
                page("TEXT", "tc.research_page.UPGRADEAIR.1", "", "'tc.research_page.UPGRADEAIR.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('UpgradeAir')", "UpgradeAir"));
        put(map, "UPGRADEEARTH",
                page("TEXT", "tc.research_page.UPGRADEEARTH.1", "", "'tc.research_page.UPGRADEEARTH.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('UpgradeEarth')", "UpgradeEarth"));
        put(map, "UPGRADEFIRE",
                page("TEXT", "tc.research_page.UPGRADEFIRE.1", "", "'tc.research_page.UPGRADEFIRE.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('UpgradeFire')", "UpgradeFire"));
        put(map, "UPGRADEWATER",
                page("TEXT", "tc.research_page.UPGRADEWATER.1", "", "'tc.research_page.UPGRADEWATER.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('UpgradeWater')", "UpgradeWater"));
        put(map, "UPGRADEORDER",
                page("TEXT", "tc.research_page.UPGRADEORDER.1", "", "'tc.research_page.UPGRADEORDER.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('UpgradeOrder')", "UpgradeOrder"));
        put(map, "UPGRADEENTROPY",
                page("TEXT", "tc.research_page.UPGRADEENTROPY.1", "", "'tc.research_page.UPGRADEENTROPY.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('UpgradeEntropy')", "UpgradeEntropy"));
        put(map, "ADVANCEDGOLEM",
                page("TEXT", "tc.research_page.ADVANCEDGOLEM.1", "", "'tc.research_page.ADVANCEDGOLEM.1'"),
                page("INFUSION_CRAFTING", "", "", "(InfusionRecipe)recipes.get('AdvancedGolem')", "AdvancedGolem"));
        put(map, "TINYHAT",
                page("TEXT", "tc.research_page.TINYHAT.1", "", "'tc.research_page.TINYHAT.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('TinyHat')", "TinyHat"));
        put(map, "TINYGLASSES",
                page("TEXT", "tc.research_page.TINYGLASSES.1", "", "'tc.research_page.TINYGLASSES.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('TinyGlasses')", "TinyGlasses"));
        put(map, "TINYBOWTIE",
                page("TEXT", "tc.research_page.TINYBOWTIE.1", "", "'tc.research_page.TINYBOWTIE.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('TinyBowtie')", "TinyBowtie"));
        put(map, "TINYFEZ",
                page("TEXT", "tc.research_page.TINYFEZ.1", "", "'tc.research_page.TINYFEZ.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('TinyFez')", "TinyFez"));
        put(map, "TINYDART",
                page("TEXT", "tc.research_page.TINYDART.1", "", "'tc.research_page.TINYDART.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('TinyDart')", "TinyDart"));
        put(map, "TINYVISOR",
                page("TEXT", "tc.research_page.TINYVISOR.1", "", "'tc.research_page.TINYVISOR.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('TinyVisor')", "TinyVisor"));
        put(map, "TINYARMOR",
                page("TEXT", "tc.research_page.TINYARMOR.1", "", "'tc.research_page.TINYARMOR.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('TinyArmor')", "TinyArmor"));
        put(map, "TINYHAMMER",
                page("TEXT", "tc.research_page.TINYHAMMER.1", "", "'tc.research_page.TINYHAMMER.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('TinyHammer')", "TinyHammer"));
    }

    private static void addBASICS(Map<String, List<PageSpec>> map) {
        put(map, "ASPECTS",
                page("TEXT", "tc.research_page.ASPECTS.1", "", "'tc.research_page.ASPECTS.1'"),
                page("TEXT", "tc.research_page.ASPECTS.2", "", "'tc.research_page.ASPECTS.2'"),
                page("TEXT", "tc.research_page.ASPECTS.3", "", "'tc.research_page.ASPECTS.3'"));
        put(map, "PECH",
                page("TEXT", "tc.research_page.PECH.1", "", "'tc.research_page.PECH.1'"),
                page("TEXT", "tc.research_page.PECH.2", "", "'tc.research_page.PECH.2'"));
        put(map, "NODES",
                page("TEXT", "tc.research_page.NODES.1", "", "'tc.research_page.NODES.1'"),
                page("TEXT", "tc.research_page.NODES.2", "", "'tc.research_page.NODES.2'"),
                page("TEXT", "tc.research_page.NODES.3", "", "'tc.research_page.NODES.3'"));
        put(map, "WARP",
                page("TEXT", "tc.research_page.WARP.1", "", "'tc.research_page.WARP.1'"),
                page("TEXT", "tc.research_page.WARP.2", "", "'tc.research_page.WARP.2'"),
                page("TEXT", "tc.research_page.WARP.3", "", "'tc.research_page.WARP.3'"));
        put(map, "RESEARCH",
                page("TEXT", "tc.research_page.RESEARCH.1", "", "'tc.research_page.RESEARCH.1'"),
                page("TEXT", "tc.research_page.RESEARCH.2", "", "'tc.research_page.RESEARCH.2'"),
                page("NORMAL_CRAFTING", "", "", "(IRecipe)recipes.get('Thaumometer')", "Thaumometer"),
                page("TEXT", "tc.research_page.RESEARCH.3", "", "'tc.research_page.RESEARCH.3'"),
                page("TEXT", "tc.research_page.RESEARCH.4", "", "'tc.research_page.RESEARCH.4'"),
                page("NORMAL_CRAFTING", "", "", "(IRecipe)recipes.get('Scribe1')", "Scribe1"),
                page("NORMAL_CRAFTING", "", "", "(IRecipe)recipes.get('Scribe2')", "Scribe2"),
                page("NORMAL_CRAFTING", "", "", "(IRecipe)recipes.get('Scribe3')", "Scribe3"),
                page("TEXT", "tc.research_page.RESEARCH.5", "", "'tc.research_page.RESEARCH.5'"),
                page("TEXT", "tc.research_page.RESEARCH.6", "", "'tc.research_page.RESEARCH.6'"),
                page("TEXT", "tc.research_page.RESEARCH.7", "", "'tc.research_page.RESEARCH.7'"),
                page("TEXT", "tc.research_page.RESEARCH.8", "", "'tc.research_page.RESEARCH.8'"),
                page("TEXT", "tc.research_page.RESEARCH.9", "", "'tc.research_page.RESEARCH.9'"),
                page("TEXT", "tc.research_page.RESEARCH.10", "", "'tc.research_page.RESEARCH.10'"),
                page("TEXT", "tc.research_page.RESEARCH.11", "", "'tc.research_page.RESEARCH.11'"),
                page("TEXT", "tc.research_page.RESEARCH.12", "", "'tc.research_page.RESEARCH.12'"));
        put(map, "KNOWFRAG",
                page("TEXT", "tc.research_page.KNOWFRAG.1", "", "'tc.research_page.KNOWFRAG.1'"),
                page("NORMAL_CRAFTING", "", "", "(IRecipe)recipes.get('KnowFrag')", "KnowFrag"));
        put(map, "THAUMONOMICON",
                page("TEXT", "tc.research_page.THAUMONOMICON.1", "", "'tc.research_page.THAUMONOMICON.1'"),
                page("NORMAL_CRAFTING", "", "", "(List)recipes.get('Thaumonomicon')", "Thaumonomicon"));
        put(map, "ORE",
                page("TEXT", "tc.research_page.ORE.1", "", "'tc.research_page.ORE.1'"),
                page("TEXT", "tc.research_page.ORE.2", "", "'tc.research_page.ORE.2'"),
                page("NORMAL_CRAFTING", "", "", "(IRecipe[])rc.toArray(new IRecipe[0])"),
                page("TEXT", "tc.research_page.ORE.3", "", "'tc.research_page.ORE.3'"),
                page("TEXT", "tc.research_page.ORE.4", "", "'tc.research_page.ORE.4'"));
        put(map, "PLANTS",
                page("TEXT", "tc.research_page.PLANTS.1", "", "'tc.research_page.PLANTS.1'"),
                page("NORMAL_CRAFTING", "", "", "(IRecipe)recipes.get('PlankGreatwood')", "PlankGreatwood"),
                page("TEXT", "tc.research_page.PLANTS.2", "", "'tc.research_page.PLANTS.2'"),
                page("NORMAL_CRAFTING", "", "", "(IRecipe)recipes.get('PlankSilverwood')", "PlankSilverwood"),
                page("TEXT", "tc.research_page.PLANTS.3", "", "'tc.research_page.PLANTS.3'"),
                page("TEXT", "tc.research_page.PLANTS.4", "", "'tc.research_page.PLANTS.4'"),
                page("TEXT", "tc.research_page.PLANTS.5", "", "'tc.research_page.PLANTS.5'"),
                page("TEXT", "tc.research_page.PLANTS.6", "", "'tc.research_page.PLANTS.6'"));
        put(map, "ENCHANT",
                page("TEXT", "tc.research_page.ENCHANT.1", "", "'tc.research_page.ENCHANT.1'"),
                page("TEXT", "tc.research_page.ENCHANT.2", "", "'tc.research_page.ENCHANT.2'"));
        put(map, "NODETAPPER1",
                page("TEXT", "tc.research_page.NODETAPPER1.1", "", "'tc.research_page.NODETAPPER1.1'"));
        put(map, "NODEPRESERVE",
                page("TEXT", "tc.research_page.NODEPRESERVE", "", "'tc.research_page.NODEPRESERVE'"));
        put(map, "NODEJAR",
                page("TEXT", "tc.research_page.NODEJAR.1", "", "'tc.research_page.NODEJAR.1'"),
                page("NORMAL_CRAFTING", "", "", "(List)recipes.get('NodeJar')", "NodeJar"),
                page("TEXT", "tc.research_page.NODEJAR.2", "", "'tc.research_page.NODEJAR.2'"));
        put(map, "NODETAPPER2",
                page("TEXT", "tc.research_page.NODETAPPER2.1", "", "'tc.research_page.NODETAPPER2.1'"));
        put(map, "RESEARCHER1",
                page("TEXT", "tc.research_page.RESEARCHER1.1", "", "'tc.research_page.RESEARCHER1.1'"));
        put(map, "DECONSTRUCTOR",
                page("TEXT", "tc.research_page.DECONSTRUCTOR.1", "", "'tc.research_page.DECONSTRUCTOR.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('Deconstructor')", "Deconstructor"),
                page("TEXT", "tc.research_page.DECONSTRUCTOR.2", "", "'tc.research_page.DECONSTRUCTOR.2'"));
        put(map, "RESEARCHER2",
                page("TEXT", "tc.research_page.RESEARCHER2.1", "", "'tc.research_page.RESEARCHER2.1'"));
        put(map, "RESEARCHDUPE",
                page("TEXT", "tc.research_page.RESEARCHDUPE.1", "", "'tc.research_page.RESEARCHDUPE.1'"));
        put(map, "CRIMSON",
                page("TEXT", "tc.research_page.CRIMSON.1", "", "'tc.research_page.CRIMSON.1'"));
    }

    private static void addELDRITCH(Map<String, List<PageSpec>> map) {
        put(map, "ELDRITCHMINOR",
                page("TEXT", "tc.research_page.ELDRITCHMINOR.1", "", "'tc.research_page.ELDRITCHMINOR.1'"),
                page("CRUCIBLE_CRAFTING", "", "", "(CrucibleRecipe)recipes.get('VoidSeed')", "VoidSeed"));
        put(map, "ELDRITCHMAJOR",
                page("TEXT", "tc.research_page.ELDRITCHMAJOR.1", "", "'tc.research_page.ELDRITCHMAJOR.1'"),
                page("TEXT", "tc.research_page.ELDRITCHMAJOR.2", "", "'tc.research_page.ELDRITCHMAJOR.2'"));
        put(map, "OCULUS",
                page("TEXT", "tc.research_page.OCULUS.1", "", "'tc.research_page.OCULUS.1'"),
                page("INFUSION_CRAFTING", "", "", "(InfusionRecipe)recipes.get('EldritchEye')", "EldritchEye"),
                page("TEXT", "tc.research_page.OCULUS.2", "", "'tc.research_page.OCULUS.2'"));
        put(map, "ENTEROUTER",
                page("TEXT", "tc.research_page.ENTEROUTER.1", "", "'tc.research_page.ENTEROUTER.1'"));
        put(map, "OUTERREV",
                page("TEXT", "tc.research_page.OUTERREV.1", "", "'tc.research_page.OUTERREV.1'"));
        put(map, "PRIMPEARL",
                page("TEXT", "tc.research_page.PRIMPEARL.1", "", "'tc.research_page.PRIMPEARL.1'"),
                page("TEXT", "tc.research_page.PRIMPEARL.2", "", "'tc.research_page.PRIMPEARL.2'"));
        put(map, "PRIMNODE",
                page("TEXT", "tc.research_page.PRIMNODE.1", "", "'tc.research_page.PRIMNODE.1'"));
        put(map, "ADVALCHEMYFURNACE",
                page("TEXT", "tc.research_page.ADVALCHEMYFURNACE.1", "", "'tc.research_page.ADVALCHEMYFURNACE.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('AdvAlchemyConstruct')", "AdvAlchemyConstruct"),
                page("TEXT", "tc.research_page.ADVALCHEMYFURNACE.2", "", "'tc.research_page.ADVALCHEMYFURNACE.2'"),
                page("NORMAL_CRAFTING", "", "", "(List)recipes.get('AdvAlchemyFurnace')", "AdvAlchemyFurnace"));
        put(map, "PRIMALCRUSHER",
                page("TEXT", "tc.research_page.PRIMALCRUSHER.1", "", "'tc.research_page.PRIMALCRUSHER.1'"),
                page("INFUSION_CRAFTING", "", "", "(InfusionRecipe)recipes.get('PrimalCrusher')", "PrimalCrusher"),
                page("TEXT", "tc.research_page.PRIMALCRUSHER.2", "", "'tc.research_page.PRIMALCRUSHER.2'"));
        put(map, "SANITYCHECK",
                page("TEXT", "tc.research_page.SANITYCHECK.1", "", "'tc.research_page.SANITYCHECK.1'"),
                page("INFUSION_CRAFTING", "", "", "(InfusionRecipe)recipes.get('SanityCheck')", "SanityCheck"));
        put(map, "VOIDMETAL",
                page("TEXT", "tc.research_page.VOIDMETAL.1", "", "'tc.research_page.VOIDMETAL.1'"),
                page("CRUCIBLE_CRAFTING", "", "", "(CrucibleRecipe)recipes.get('VoidMetal')", "VoidMetal"),
                page("TEXT", "tc.research_page.VOIDMETAL.2", "", "'tc.research_page.VOIDMETAL.2'"),
                page("NORMAL_CRAFTING", "", "", "(IRecipe)recipes.get('VoidAxe')", "VoidAxe"),
                page("NORMAL_CRAFTING", "", "", "(IRecipe)recipes.get('VoidSword')", "VoidSword"),
                page("NORMAL_CRAFTING", "", "", "(IRecipe)recipes.get('VoidPick')", "VoidPick"),
                page("NORMAL_CRAFTING", "", "", "(IRecipe)recipes.get('VoidShovel')", "VoidShovel"),
                page("NORMAL_CRAFTING", "", "", "(IRecipe)recipes.get('VoidHoe')", "VoidHoe"),
                page("NORMAL_CRAFTING", "", "", "(IRecipe)recipes.get('VoidHelm')", "VoidHelm"),
                page("NORMAL_CRAFTING", "", "", "(IRecipe)recipes.get('VoidChest')", "VoidChest"),
                page("NORMAL_CRAFTING", "", "", "(IRecipe)recipes.get('VoidLegs')", "VoidLegs"),
                page("NORMAL_CRAFTING", "", "", "(IRecipe)recipes.get('VoidBoots')", "VoidBoots"));
        put(map, "ESSENTIARESERVOIR",
                page("TEXT", "tc.research_page.ESSENTIARESERVOIR.1", "", "'tc.research_page.ESSENTIARESERVOIR.1'"),
                page("INFUSION_CRAFTING", "", "", "(InfusionRecipe)recipes.get('EssentiaReservoir')", "EssentiaReservoir"),
                page("TEXT", "tc.research_page.ESSENTIARESERVOIR.2", "", "'tc.research_page.ESSENTIARESERVOIR.2'"));
        put(map, "CAP_void",
                page("TEXT", "tc.research_page.CAP_void.1", "", "'tc.research_page.CAP_void.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('WandCapVoidInert')", "WandCapVoidInert"),
                page("INFUSION_CRAFTING", "", "", "(InfusionRecipe)recipes.get('WandCapVoid')", "WandCapVoid"));
        put(map, "ARMORVOIDFORTRESS",
                page("TEXT", "tc.research_page.ARMORVOIDFORTRESS.1", "", "'tc.research_page.ARMORVOIDFORTRESS.1'"),
                page("INFUSION_CRAFTING", "", "", "(InfusionRecipe)recipes.get('VoidRobeHelm')", "VoidRobeHelm"),
                page("INFUSION_CRAFTING", "", "", "(InfusionRecipe)recipes.get('VoidRobeChest')", "VoidRobeChest"),
                page("INFUSION_CRAFTING", "", "", "(InfusionRecipe)recipes.get('VoidRobeLegs')", "VoidRobeLegs"));
        put(map, "FOCUSPRIMAL",
                page("TEXT", "tc.research_page.FOCUSPRIMAL.1", "", "'tc.research_page.FOCUSPRIMAL.1'"),
                page("ARCANE_CRAFTING", "", "", "(IArcaneRecipe)recipes.get('FocusPrimal')", "FocusPrimal"));
        put(map, "ROD_primal_staff",
                page("TEXT", "tc.research_page.ROD_primal_staff.1", "", "'tc.research_page.ROD_primal_staff.1'"),
                page("INFUSION_CRAFTING", "", "", "(InfusionRecipe)recipes.get('WandRodPrimalStaff')", "WandRodPrimalStaff"));
    }

}
