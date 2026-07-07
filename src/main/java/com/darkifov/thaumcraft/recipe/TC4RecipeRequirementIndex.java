package com.darkifov.thaumcraft.recipe;

import com.darkifov.thaumcraft.data.PlayerThaumData;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Stage152: strict original TC4 recipe-unlock index.
 *
 * This is generated from the original ConfigResearch.java recipe pages and
 * the ConfigRecipes.java bridge. It does not invent modern recipe gates:
 * every entry records which original research unlocks a Thaumonomicon recipe page.
 */
public final class TC4RecipeRequirementIndex {
    public record Requirement(String pageResearch, int pageIndex, String pageType, String recipeKey, String originalKind, String originalResearch, String requiredResearch, String resolution) {}

    private static final Map<String, Requirement> BY_RECIPE_KEY = buildByRecipeKey();
    private static final Map<String, List<Requirement>> BY_REQUIRED_RESEARCH = buildByRequiredResearch();

    private TC4RecipeRequirementIndex() {}

    private static Map<String, Requirement> buildByRecipeKey() {
        Map<String, Requirement> map = new LinkedHashMap<>();
        put(map, new Requirement("BASICTHAUMATURGY", 2, "NORMAL_CRAFTING", "WandCapIron", "COMPOUND", "", "BASICTHAUMATURGY", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("BASICTHAUMATURGY", 3, "NORMAL_CRAFTING", "WandBasic", "COMPOUND", "", "BASICTHAUMATURGY", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("FOCUSFIRE", 2, "ARCANE_CRAFTING", "FocusFire", "ARCANE_SHAPED", "FOCUSFIRE", "FOCUSFIRE", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("FOCUSFROST", 1, "ARCANE_CRAFTING", "FocusFrost", "ARCANE_SHAPED", "FOCUSFROST", "FOCUSFROST", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("FOCUSHELLBAT", 1, "INFUSION_CRAFTING", "FocusHellbat", "INFUSION", "FOCUSHELLBAT", "FOCUSHELLBAT", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("FOCUSEXCAVATION", 1, "ARCANE_CRAFTING", "FocusExcavation", "ARCANE_SHAPED", "FOCUSEXCAVATION", "FOCUSEXCAVATION", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("FOCUSWARDING", 1, "INFUSION_CRAFTING", "FocusWarding", "INFUSION", "FOCUSWARDING", "FOCUSWARDING", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("FOCUSSHOCK", 1, "ARCANE_CRAFTING", "FocusShock", "ARCANE_SHAPED", "FOCUSSHOCK", "FOCUSSHOCK", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("FOCUSTRADE", 1, "ARCANE_CRAFTING", "FocusTrade", "ARCANE_SHAPED", "FOCUSTRADE", "FOCUSTRADE", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("FOCUSPORTABLEHOLE", 1, "INFUSION_CRAFTING", "FocusPortableHole", "INFUSION", "FOCUSPORTABLEHOLE", "FOCUSPORTABLEHOLE", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("FOCUSPOUCH", 1, "ARCANE_CRAFTING", "FocusPouch", "ARCANE_SHAPED", "FOCUSPOUCH", "FOCUSPOUCH", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("CAP_gold", 1, "ARCANE_CRAFTING", "WandCapGold", "ARCANE_SHAPED", "CAP_gold", "CAP_gold", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("CAP_thaumium", 1, "ARCANE_CRAFTING", "WandCapThaumiumInert", "ARCANE_SHAPED", "CAP_thaumium", "CAP_thaumium", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("CAP_thaumium", 2, "INFUSION_CRAFTING", "WandCapThaumium", "INFUSION", "CAP_thaumium", "CAP_thaumium", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("CAP_copper", 1, "ARCANE_CRAFTING", "WandCapCopper", "ARCANE_SHAPED", "CAP_copper", "CAP_copper", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("CAP_silver", 1, "ARCANE_CRAFTING", "WandCapSilverInert", "ARCANE_SHAPED", "CAP_silver", "CAP_silver", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("CAP_silver", 2, "INFUSION_CRAFTING", "WandCapSilver", "INFUSION", "CAP_silver", "CAP_silver", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("ROD_greatwood", 1, "ARCANE_CRAFTING", "WandRodGreatwood", "ARCANE_SHAPED", "ROD_greatwood", "ROD_greatwood", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("ROD_reed", 1, "INFUSION_CRAFTING", "WandRodReed", "INFUSION", "ROD_reed", "ROD_reed", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("ROD_blaze", 1, "INFUSION_CRAFTING", "WandRodBlaze", "INFUSION", "ROD_blaze", "ROD_blaze", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("ROD_obsidian", 1, "INFUSION_CRAFTING", "WandRodObsidian", "INFUSION", "ROD_obsidian", "ROD_obsidian", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("ROD_ice", 1, "INFUSION_CRAFTING", "WandRodIce", "INFUSION", "ROD_ice", "ROD_ice", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("ROD_quartz", 1, "INFUSION_CRAFTING", "WandRodQuartz", "INFUSION", "ROD_quartz", "ROD_quartz", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("ROD_bone", 1, "INFUSION_CRAFTING", "WandRodBone", "INFUSION", "ROD_bone", "ROD_bone", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("ROD_silverwood", 1, "INFUSION_CRAFTING", "WandRodSilverwood", "INFUSION", "ROD_silverwood", "ROD_silverwood", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("SCEPTRE", 1, "ARCANE_CRAFTING", "RESEARCH:SCEPTRE", "DYNAMIC_RESEARCH_GROUP", "SCEPTRE", "SCEPTRE", "research_dynamic_group"));
        put(map, new Requirement("ROD_greatwood_staff", 2, "ARCANE_CRAFTING", "WandRodGreatwoodStaff", "ARCANE_SHAPED", "ROD_greatwood_staff", "ROD_greatwood_staff", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("ROD_reed_staff", 1, "ARCANE_CRAFTING", "WandRodReedStaff", "ARCANE_SHAPED", "ROD_reed_staff", "ROD_reed_staff", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("ROD_blaze_staff", 1, "ARCANE_CRAFTING", "WandRodBlazeStaff", "ARCANE_SHAPED", "ROD_blaze_staff", "ROD_blaze_staff", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("ROD_obsidian_staff", 1, "ARCANE_CRAFTING", "WandRodObsidianStaff", "ARCANE_SHAPED", "ROD_obsidian_staff", "ROD_obsidian_staff", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("ROD_ice_staff", 1, "ARCANE_CRAFTING", "WandRodIceStaff", "ARCANE_SHAPED", "ROD_ice_staff", "ROD_ice_staff", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("ROD_quartz_staff", 1, "ARCANE_CRAFTING", "WandRodQuartzStaff", "ARCANE_SHAPED", "ROD_quartz_staff", "ROD_quartz_staff", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("ROD_bone_staff", 1, "ARCANE_CRAFTING", "WandRodBoneStaff", "ARCANE_SHAPED", "ROD_bone_staff", "ROD_bone_staff", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("ROD_silverwood_staff", 1, "ARCANE_CRAFTING", "WandRodSilverwoodStaff", "ARCANE_SHAPED", "ROD_silverwood_staff", "ROD_silverwood_staff", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("WANDPED", 1, "INFUSION_CRAFTING", "WandPed", "INFUSION", "WANDPED", "WANDPED", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("VISAMULET", 1, "INFUSION_CRAFTING", "VisAmulet", "INFUSION", "VISAMULET", "VISAMULET", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("WANDPEDFOC", 1, "INFUSION_CRAFTING", "WandPedFocus", "INFUSION", "WANDPEDFOC", "WANDPEDFOC", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("NODESTABILIZER", 1, "ARCANE_CRAFTING", "NodeStabilizer", "ARCANE_SHAPED", "NODESTABILIZER", "NODESTABILIZER", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("NODESTABILIZERADV", 1, "INFUSION_CRAFTING", "NodeStabilizerAdv", "INFUSION", "NODESTABILIZERADV", "NODESTABILIZERADV", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("VISPOWER", 1, "ARCANE_CRAFTING", "NodeTransducer", "ARCANE_SHAPED", "VISPOWER", "VISPOWER", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("VISPOWER", 4, "ARCANE_CRAFTING", "NodeRelay", "ARCANE_SHAPED", "VISPOWER", "VISPOWER", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("VISCHARGERELAY", 1, "ARCANE_CRAFTING", "NodeChargeRelay", "ARCANE_SHAPED", "VISCHARGERELAY", "VISCHARGERELAY", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("FOCALMANIPULATION", 1, "ARCANE_CRAFTING", "FocalManipulator", "ARCANE_SHAPED", "FOCALMANIPULATION", "FOCALMANIPULATION", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("BASICARTIFACE", 1, "ARCANE_CRAFTING", "PrimalCharm", "ARCANE_SHAPED", "BASICARTIFACE", "BASICARTIFACE", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("BASICARTIFACE", 2, "NORMAL_CRAFTING", "MundaneAmulet", "COMPOUND", "", "BASICARTIFACE", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("BASICARTIFACE", 3, "NORMAL_CRAFTING", "MundaneRing", "COMPOUND", "", "BASICARTIFACE", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("BASICARTIFACE", 4, "NORMAL_CRAFTING", "MundaneBelt", "COMPOUND", "", "BASICARTIFACE", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("BASICARTIFACE", 5, "ARCANE_CRAFTING", "MirrorGlass", "ARCANE_SHAPELESS", "BASICARTIFACE", "BASICARTIFACE", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("ARCANESTONE", 1, "ARCANE_CRAFTING", "ArcaneStone1", "ARCANE_SHAPED", "ARCANESTONE", "ARCANESTONE", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("ARCANESTONE", 2, "NORMAL_CRAFTING", "ArcaneStone2", "COMPOUND", "", "ARCANESTONE", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("ARCANESTONE", 3, "NORMAL_CRAFTING", "ArcaneStone3", "COMPOUND", "", "ARCANESTONE", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("ARCANESTONE", 4, "NORMAL_CRAFTING", "ArcaneStone4", "COMPOUND", "", "ARCANESTONE", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("GRATE", 1, "NORMAL_CRAFTING", "Grate", "COMPOUND", "", "GRATE", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("TABLE", 1, "NORMAL_CRAFTING", "Table", "COMPOUND", "", "TABLE", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("ARCTABLE", 1, "COMPOUND_CRAFTING", "ArcTable", "COMPOUND", "", "ARCTABLE", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("RESTABLE", 1, "COMPOUND_CRAFTING", "ResTable", "COMPOUND", "", "RESTABLE", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("THAUMOMETER", 1, "NORMAL_CRAFTING", "Thaumometer", "COMPOUND", "", "THAUMOMETER", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("PAVETRAVEL", 1, "ARCANE_CRAFTING", "PaveTravel", "ARCANE_SHAPED", "PAVETRAVEL", "PAVETRAVEL", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("PAVEWARD", 1, "ARCANE_CRAFTING", "PaveWard", "ARCANE_SHAPED", "PAVEWARD", "PAVEWARD", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("GOGGLES", 1, "ARCANE_CRAFTING", "Goggles", "ARCANE_SHAPED", "GOGGLES", "GOGGLES", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("ARCANEEAR", 1, "ARCANE_CRAFTING", "ArcaneEar", "ARCANE_SHAPED", "ARCANEEAR", "ARCANEEAR", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("SINSTONE", 1, "INFUSION_CRAFTING", "SinStone", "INFUSION", "SINSTONE", "SINSTONE", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("LEVITATOR", 1, "ARCANE_CRAFTING", "Levitator", "ARCANE_SHAPED", "LEVITATOR", "LEVITATOR", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("INFERNALFURNACE", 1, "COMPOUND_CRAFTING", "InfernalFurnace", "COMPOUND", "", "INFERNALFURNACE", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("BELLOWS", 1, "ARCANE_CRAFTING", "Bellows", "ARCANE_SHAPED", "BELLOWS", "BELLOWS", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("ENCHFABRIC", 1, "ARCANE_CRAFTING", "EnchantedFabric", "ARCANE_SHAPED", "ENCHFABRIC", "ENCHFABRIC", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("ENCHFABRIC", 3, "ARCANE_CRAFTING", "RobeChest", "ARCANE_SHAPED", "ENCHFABRIC", "ENCHFABRIC", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("ENCHFABRIC", 4, "ARCANE_CRAFTING", "RobeLegs", "ARCANE_SHAPED", "ENCHFABRIC", "ENCHFABRIC", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("ENCHFABRIC", 5, "ARCANE_CRAFTING", "RobeBoots", "ARCANE_SHAPED", "ENCHFABRIC", "ENCHFABRIC", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("INFUSION", 1, "ARCANE_CRAFTING", "InfusionMatrix", "ARCANE_SHAPED", "INFUSION", "INFUSION", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("INFUSION", 2, "ARCANE_CRAFTING", "ArcanePedestal", "ARCANE_SHAPED", "INFUSION", "INFUSION", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("INFUSION", 4, "COMPOUND_CRAFTING", "InfusionAltar", "COMPOUND", "", "INFUSION", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("FLUXSCRUB", 1, "ARCANE_CRAFTING", "FluxScrubber", "ARCANE_SHAPED", "FLUXSCRUB", "FLUXSCRUB", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("RUNICARMOR", 2, "INFUSION_CRAFTING", "RunicRing", "INFUSION", "RUNICARMOR", "RUNICARMOR", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("RUNICARMOR", 3, "INFUSION_CRAFTING", "RunicAmulet", "INFUSION", "RUNICARMOR", "RUNICARMOR", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("RUNICARMOR", 4, "INFUSION_CRAFTING", "RunicGirdle", "INFUSION", "RUNICARMOR", "RUNICARMOR", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("RUNICCHARGED", 1, "INFUSION_CRAFTING", "RunicRingCharged", "INFUSION", "RUNICCHARGED", "RUNICCHARGED", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("RUNICHEALING", 1, "INFUSION_CRAFTING", "RunicRingHealing", "INFUSION", "RUNICHEALING", "RUNICHEALING", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("RUNICKINETIC", 1, "INFUSION_CRAFTING", "RunicGirdleKinetic", "INFUSION", "RUNICKINETIC", "RUNICKINETIC", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("RUNICEMERGENCY", 1, "INFUSION_CRAFTING", "RunicAmuletEmergency", "INFUSION", "RUNICEMERGENCY", "RUNICEMERGENCY", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("BANNERS", 1, "ARCANE_CRAFTING", "RESEARCH:BANNERS", "DYNAMIC_RESEARCH_GROUP", "BANNERS", "BANNERS", "research_dynamic_group"));
        put(map, new Requirement("RUNICAUGMENTATION", 1, "INFUSION_CRAFTING", "RESEARCH:RUNICAUGMENTATION", "DYNAMIC_RESEARCH_GROUP", "RUNICAUGMENTATION", "RUNICAUGMENTATION", "research_dynamic_group"));
        put(map, new Requirement("BOOTSTRAVELLER", 1, "INFUSION_CRAFTING", "BootsTraveller", "INFUSION", "BOOTSTRAVELLER", "BOOTSTRAVELLER", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("HOVERHARNESS", 1, "INFUSION_CRAFTING", "HoverHarness", "INFUSION", "HOVERHARNESS", "HOVERHARNESS", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("HOVERGIRDLE", 1, "INFUSION_CRAFTING", "HoverGirdle", "INFUSION", "HOVERGIRDLE", "HOVERGIRDLE", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("MIRROR", 2, "INFUSION_CRAFTING", "Mirror", "INFUSION", "MIRROR", "MIRROR", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("MIRRORHAND", 1, "INFUSION_CRAFTING", "MirrorHand", "INFUSION", "MIRRORHAND", "MIRRORHAND", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("MIRRORESSENTIA", 1, "INFUSION_CRAFTING", "MirrorEssentia", "INFUSION", "MIRRORESSENTIA", "MIRRORESSENTIA", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("ARCANEBORE", 1, "INFUSION_CRAFTING", "ArcaneBore", "INFUSION", "ARCANEBORE", "ARCANEBORE", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("ARCANEBORE", 3, "ARCANE_CRAFTING", "ArcaneBoreBase", "ARCANE_SHAPED", "ARCANEBORE", "ARCANEBORE", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("ARCANELAMP", 1, "ARCANE_CRAFTING", "ArcaneLamp", "ARCANE_SHAPED", "ARCANELAMP", "ARCANELAMP", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("LAMPGROWTH", 1, "INFUSION_CRAFTING", "LampGrowth", "INFUSION", "LAMPGROWTH", "LAMPGROWTH", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("LAMPFERTILITY", 1, "INFUSION_CRAFTING", "LampFertility", "INFUSION", "LAMPFERTILITY", "LAMPFERTILITY", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("BONEBOW", 1, "ARCANE_CRAFTING", "BoneBow", "ARCANE_SHAPED", "BONEBOW", "BONEBOW", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("PRIMALARROW", 1, "ARCANE_CRAFTING", "RESEARCH:PRIMALARROW", "DYNAMIC_RESEARCH_GROUP", "PRIMALARROW", "PRIMALARROW", "research_dynamic_group"));
        put(map, new Requirement("ELEMENTALAXE", 1, "INFUSION_CRAFTING", "ElementalAxe", "INFUSION", "ELEMENTALAXE", "ELEMENTALAXE", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("ELEMENTALPICK", 1, "INFUSION_CRAFTING", "ElementalPick", "INFUSION", "ELEMENTALPICK", "ELEMENTALPICK", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("ELEMENTALSWORD", 1, "INFUSION_CRAFTING", "ElementalSword", "INFUSION", "ELEMENTALSWORD", "ELEMENTALSWORD", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("ELEMENTALSHOVEL", 1, "INFUSION_CRAFTING", "ElementalShovel", "INFUSION", "ELEMENTALSHOVEL", "ELEMENTALSHOVEL", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("ELEMENTALHOE", 1, "INFUSION_CRAFTING", "ElementalHoe", "INFUSION", "ELEMENTALHOE", "ELEMENTALHOE", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("WARDEDARCANA", 1, "ARCANE_CRAFTING", "ArcaneDoor", "ARCANE_SHAPED", "WARDEDARCANA", "WARDEDARCANA", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("WARDEDARCANA", 3, "ARCANE_CRAFTING", "IronKey", "ARCANE_SHAPED", "WARDEDARCANA", "WARDEDARCANA", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("WARDEDARCANA", 4, "ARCANE_CRAFTING", "GoldKey", "ARCANE_SHAPED", "WARDEDARCANA", "WARDEDARCANA", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("WARDEDARCANA", 6, "ARCANE_CRAFTING", "ArcanePressurePlate", "ARCANE_SHAPED", "WARDEDARCANA", "WARDEDARCANA", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("WARDEDARCANA", 8, "ARCANE_CRAFTING", "WardedGlass", "ARCANE_SHAPED", "WARDEDARCANA", "WARDEDARCANA", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("JARBRAIN", 1, "INFUSION_CRAFTING", "JarBrain", "INFUSION", "JARBRAIN", "JARBRAIN", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("INFUSIONENCHANTMENT", 3, "INFUSION_ENCHANTMENT", "InfEnchRepair", "INFUSION_ENCHANTMENT", "INFUSIONENCHANTMENT", "INFUSIONENCHANTMENT", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("INFUSIONENCHANTMENT", 4, "INFUSION_ENCHANTMENT", "InfEnchHaste", "INFUSION_ENCHANTMENT", "INFUSIONENCHANTMENT", "INFUSIONENCHANTMENT", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("INFUSIONENCHANTMENT", 5, "INFUSION_ENCHANTMENT", "InfEnch0", "INFUSION_ENCHANTMENT", "INFUSIONENCHANTMENT", "INFUSIONENCHANTMENT", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("INFUSIONENCHANTMENT", 6, "INFUSION_ENCHANTMENT", "InfEnch1", "INFUSION_ENCHANTMENT", "INFUSIONENCHANTMENT", "INFUSIONENCHANTMENT", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("INFUSIONENCHANTMENT", 7, "INFUSION_ENCHANTMENT", "InfEnch2", "INFUSION_ENCHANTMENT", "INFUSIONENCHANTMENT", "INFUSIONENCHANTMENT", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("INFUSIONENCHANTMENT", 8, "INFUSION_ENCHANTMENT", "InfEnch3", "INFUSION_ENCHANTMENT", "INFUSIONENCHANTMENT", "INFUSIONENCHANTMENT", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("INFUSIONENCHANTMENT", 9, "INFUSION_ENCHANTMENT", "InfEnch4", "INFUSION_ENCHANTMENT", "INFUSIONENCHANTMENT", "INFUSIONENCHANTMENT", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("INFUSIONENCHANTMENT", 10, "INFUSION_ENCHANTMENT", "InfEnch5", "INFUSION_ENCHANTMENT", "INFUSIONENCHANTMENT", "INFUSIONENCHANTMENT", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("INFUSIONENCHANTMENT", 11, "INFUSION_ENCHANTMENT", "InfEnch6", "INFUSION_ENCHANTMENT", "INFUSIONENCHANTMENT", "INFUSIONENCHANTMENT", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("INFUSIONENCHANTMENT", 12, "INFUSION_ENCHANTMENT", "InfEnch7", "INFUSION_ENCHANTMENT", "INFUSIONENCHANTMENT", "INFUSIONENCHANTMENT", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("INFUSIONENCHANTMENT", 13, "INFUSION_ENCHANTMENT", "InfEnch8", "INFUSION_ENCHANTMENT", "INFUSIONENCHANTMENT", "INFUSIONENCHANTMENT", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("INFUSIONENCHANTMENT", 14, "INFUSION_ENCHANTMENT", "InfEnch9", "INFUSION_ENCHANTMENT", "INFUSIONENCHANTMENT", "INFUSIONENCHANTMENT", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("INFUSIONENCHANTMENT", 15, "INFUSION_ENCHANTMENT", "InfEnch10", "INFUSION_ENCHANTMENT", "INFUSIONENCHANTMENT", "INFUSIONENCHANTMENT", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("INFUSIONENCHANTMENT", 16, "INFUSION_ENCHANTMENT", "InfEnch11", "INFUSION_ENCHANTMENT", "INFUSIONENCHANTMENT", "INFUSIONENCHANTMENT", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("INFUSIONENCHANTMENT", 17, "INFUSION_ENCHANTMENT", "InfEnch12", "INFUSION_ENCHANTMENT", "INFUSIONENCHANTMENT", "INFUSIONENCHANTMENT", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("INFUSIONENCHANTMENT", 18, "INFUSION_ENCHANTMENT", "InfEnch13", "INFUSION_ENCHANTMENT", "INFUSIONENCHANTMENT", "INFUSIONENCHANTMENT", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("INFUSIONENCHANTMENT", 19, "INFUSION_ENCHANTMENT", "InfEnch14", "INFUSION_ENCHANTMENT", "INFUSIONENCHANTMENT", "INFUSIONENCHANTMENT", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("INFUSIONENCHANTMENT", 20, "INFUSION_ENCHANTMENT", "InfEnch15", "INFUSION_ENCHANTMENT", "INFUSIONENCHANTMENT", "INFUSIONENCHANTMENT", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("INFUSIONENCHANTMENT", 21, "INFUSION_ENCHANTMENT", "InfEnch16", "INFUSION_ENCHANTMENT", "INFUSIONENCHANTMENT", "INFUSIONENCHANTMENT", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("INFUSIONENCHANTMENT", 22, "INFUSION_ENCHANTMENT", "InfEnch17", "INFUSION_ENCHANTMENT", "INFUSIONENCHANTMENT", "INFUSIONENCHANTMENT", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("INFUSIONENCHANTMENT", 23, "INFUSION_ENCHANTMENT", "InfEnch18", "INFUSION_ENCHANTMENT", "INFUSIONENCHANTMENT", "INFUSIONENCHANTMENT", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("INFUSIONENCHANTMENT", 24, "INFUSION_ENCHANTMENT", "InfEnch19", "INFUSION_ENCHANTMENT", "INFUSIONENCHANTMENT", "INFUSIONENCHANTMENT", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("INFUSIONENCHANTMENT", 25, "INFUSION_ENCHANTMENT", "InfEnch20", "INFUSION_ENCHANTMENT", "INFUSIONENCHANTMENT", "INFUSIONENCHANTMENT", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("INFUSIONENCHANTMENT", 26, "INFUSION_ENCHANTMENT", "InfEnch21", "INFUSION_ENCHANTMENT", "INFUSIONENCHANTMENT", "INFUSIONENCHANTMENT", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("ARMORFORTRESS", 2, "INFUSION_CRAFTING", "ThaumiumFortressHelm", "INFUSION", "ARMORFORTRESS", "ARMORFORTRESS", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("ARMORFORTRESS", 3, "INFUSION_CRAFTING", "ThaumiumFortressChest", "INFUSION", "ARMORFORTRESS", "ARMORFORTRESS", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("ARMORFORTRESS", 4, "INFUSION_CRAFTING", "ThaumiumFortressLegs", "INFUSION", "ARMORFORTRESS", "ARMORFORTRESS", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("HELMGOGGLES", 1, "INFUSION_CRAFTING", "HelmGoggles", "INFUSION", "HELMGOGGLES", "HELMGOGGLES", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("MASKGRINNINGDEVIL", 1, "INFUSION_CRAFTING", "MaskGrinningDevil", "INFUSION", "MASKGRINNINGDEVIL", "MASKGRINNINGDEVIL", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("MASKANGRYGHOST", 1, "INFUSION_CRAFTING", "MaskAngryGhost", "INFUSION", "MASKANGRYGHOST", "MASKANGRYGHOST", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("MASKSIPPINGFIEND", 1, "INFUSION_CRAFTING", "MaskSippingFiend", "INFUSION", "MASKSIPPINGFIEND", "MASKSIPPINGFIEND", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("PHIAL", 1, "NORMAL_CRAFTING", "Phial", "COMPOUND", "", "PHIAL", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("CRUCIBLE", 3, "COMPOUND_CRAFTING", "Crucible", "COMPOUND", "", "CRUCIBLE", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("CRUCIBLE", 5, "CRUCIBLE_CRAFTING", "RESEARCH:CRUCIBLE", "DYNAMIC_RESEARCH_GROUP", "CRUCIBLE", "CRUCIBLE", "research_dynamic_group"));
        put(map, new Requirement("CRUCIBLE", 7, "ITEMSTACK_PAGE", "new ItemStack(ConfigItems.itemShard, 1, 6)", "ITEMSTACK_PAGE", "CRUCIBLE", "CRUCIBLE", "itemstack_page"));
        put(map, new Requirement("NITOR", 1, "CRUCIBLE_CRAFTING", "Nitor", "CRUCIBLE", "NITOR", "NITOR", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("ALUMENTUM", 1, "CRUCIBLE_CRAFTING", "Alumentum", "CRUCIBLE", "ALUMENTUM", "ALUMENTUM", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("ALCHEMICALDUPLICATION", 1, "CRUCIBLE_CRAFTING", "AltGunpowder", "CRUCIBLE", "ALCHEMICALDUPLICATION", "ALCHEMICALDUPLICATION", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("ALCHEMICALDUPLICATION", 2, "CRUCIBLE_CRAFTING", "AltSlime", "CRUCIBLE", "ALCHEMICALDUPLICATION", "ALCHEMICALDUPLICATION", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("ALCHEMICALDUPLICATION", 3, "CRUCIBLE_CRAFTING", "AltClay", "CRUCIBLE", "ALCHEMICALDUPLICATION", "ALCHEMICALDUPLICATION", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("ALCHEMICALDUPLICATION", 4, "CRUCIBLE_CRAFTING", "AltGlowstone", "CRUCIBLE", "ALCHEMICALDUPLICATION", "ALCHEMICALDUPLICATION", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("ALCHEMICALDUPLICATION", 5, "CRUCIBLE_CRAFTING", "AltInk", "CRUCIBLE", "ALCHEMICALDUPLICATION", "ALCHEMICALDUPLICATION", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("ALCHEMICALMANUFACTURE", 1, "CRUCIBLE_CRAFTING", "AltWeb", "CRUCIBLE", "ALCHEMICALMANUFACTURE", "ALCHEMICALMANUFACTURE", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("ALCHEMICALMANUFACTURE", 2, "CRUCIBLE_CRAFTING", "AltMossyCobble", "CRUCIBLE", "ALCHEMICALMANUFACTURE", "ALCHEMICALMANUFACTURE", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("ALCHEMICALMANUFACTURE", 3, "CRUCIBLE_CRAFTING", "AltIce", "CRUCIBLE", "ALCHEMICALMANUFACTURE", "ALCHEMICALMANUFACTURE", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("ENTROPICPROCESSING", 1, "CRUCIBLE_CRAFTING", "AltCrackedBrick", "CRUCIBLE", "ENTROPICPROCESSING", "ENTROPICPROCESSING", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("ENTROPICPROCESSING", 2, "CRUCIBLE_CRAFTING", "AltBonemeal", "CRUCIBLE", "ENTROPICPROCESSING", "ENTROPICPROCESSING", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("LIQUIDDEATH", 1, "CRUCIBLE_CRAFTING", "LiquidDeath", "CRUCIBLE", "LIQUIDDEATH", "LIQUIDDEATH", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("BOTTLETAINT", 1, "CRUCIBLE_CRAFTING", "BottleTaint", "CRUCIBLE", "BOTTLETAINT", "BOTTLETAINT", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("THAUMIUM", 1, "CRUCIBLE_CRAFTING", "Thaumium", "CRUCIBLE", "THAUMIUM", "THAUMIUM", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("THAUMIUM", 2, "NORMAL_CRAFTING", "ThaumiumAxe", "COMPOUND", "", "THAUMIUM", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("THAUMIUM", 3, "NORMAL_CRAFTING", "ThaumiumSword", "COMPOUND", "", "THAUMIUM", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("THAUMIUM", 4, "NORMAL_CRAFTING", "ThaumiumPick", "COMPOUND", "", "THAUMIUM", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("THAUMIUM", 5, "NORMAL_CRAFTING", "ThaumiumShovel", "COMPOUND", "", "THAUMIUM", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("THAUMIUM", 6, "NORMAL_CRAFTING", "ThaumiumHoe", "COMPOUND", "", "THAUMIUM", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("THAUMIUM", 7, "NORMAL_CRAFTING", "ThaumiumHelm", "COMPOUND", "", "THAUMIUM", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("THAUMIUM", 8, "NORMAL_CRAFTING", "ThaumiumChest", "COMPOUND", "", "THAUMIUM", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("THAUMIUM", 9, "NORMAL_CRAFTING", "ThaumiumLegs", "COMPOUND", "", "THAUMIUM", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("THAUMIUM", 10, "NORMAL_CRAFTING", "ThaumiumBoots", "COMPOUND", "", "THAUMIUM", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("PUREIRON", 1, "CRUCIBLE_CRAFTING", "PureIron", "CRUCIBLE", "PUREIRON", "PUREIRON", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("PUREGOLD", 1, "CRUCIBLE_CRAFTING", "PureGold", "CRUCIBLE", "PUREGOLD", "PUREGOLD", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("PURECOPPER", 1, "CRUCIBLE_CRAFTING", "PureCopper", "CRUCIBLE", "PURECOPPER", "PURECOPPER", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("PURETIN", 1, "CRUCIBLE_CRAFTING", "PureTin", "CRUCIBLE", "PURETIN", "PURETIN", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("PURESILVER", 1, "CRUCIBLE_CRAFTING", "PureSilver", "CRUCIBLE", "PURESILVER", "PURESILVER", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("PURELEAD", 1, "CRUCIBLE_CRAFTING", "PureLead", "CRUCIBLE", "PURELEAD", "PURELEAD", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("TRANSIRON", 1, "CRUCIBLE_CRAFTING", "TransIron", "CRUCIBLE", "TRANSIRON", "TRANSIRON", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("TRANSGOLD", 1, "CRUCIBLE_CRAFTING", "TransGold", "CRUCIBLE", "TRANSGOLD", "TRANSGOLD", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("TRANSCOPPER", 1, "CRUCIBLE_CRAFTING", "TransCopper", "CRUCIBLE", "TRANSCOPPER", "TRANSCOPPER", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("TRANSTIN", 1, "CRUCIBLE_CRAFTING", "TransTin", "CRUCIBLE", "TRANSTIN", "TRANSTIN", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("TRANSSILVER", 1, "CRUCIBLE_CRAFTING", "TransSilver", "CRUCIBLE", "TRANSSILVER", "TRANSSILVER", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("TRANSLEAD", 1, "CRUCIBLE_CRAFTING", "TransLead", "CRUCIBLE", "TRANSLEAD", "TRANSLEAD", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("TALLOW", 1, "CRUCIBLE_CRAFTING", "Tallow", "CRUCIBLE", "TALLOW", "TALLOW", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("TALLOW", 2, "NORMAL_CRAFTING", "TallowCandle", "COMPOUND", "", "TALLOW", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("ETHEREALBLOOM", 1, "CRUCIBLE_CRAFTING", "EtherealBloom", "CRUCIBLE", "ETHEREALBLOOM", "ETHEREALBLOOM", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("BATHSALTS", 1, "CRUCIBLE_CRAFTING", "BathSalts", "CRUCIBLE", "BATHSALTS", "BATHSALTS", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("SANESOAP", 1, "CRUCIBLE_CRAFTING", "SaneSoap", "CRUCIBLE", "SANESOAP", "SANESOAP", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("ARCANESPA", 1, "ARCANE_CRAFTING", "ArcaneSpa", "ARCANE_SHAPED", "ARCANESPA", "ARCANESPA", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("DISTILESSENTIA", 1, "ARCANE_CRAFTING", "AlchemyFurnace", "ARCANE_SHAPED", "DISTILESSENTIA", "DISTILESSENTIA", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("DISTILESSENTIA", 3, "ARCANE_CRAFTING", "Filter", "ARCANE_SHAPED", "DISTILESSENTIA", "DISTILESSENTIA", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("DISTILESSENTIA", 4, "ARCANE_CRAFTING", "Alembic", "ARCANE_SHAPED", "DISTILESSENTIA", "DISTILESSENTIA", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("DISTILESSENTIA", 5, "ARCANE_CRAFTING", "AlchemicalConstruct", "ARCANE_SHAPED", "DISTILESSENTIA", "DISTILESSENTIA", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("JARLABEL", 1, "ARCANE_CRAFTING", "WardedJar", "ARCANE_SHAPED", "DISTILESSENTIA", "DISTILESSENTIA", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("JARLABEL", 3, "NORMAL_CRAFTING", "JarLabel", "COMPOUND", "", "JARLABEL", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("JARLABEL", 5, "NORMAL_CRAFTING", "RESEARCH:JARLABEL", "DYNAMIC_RESEARCH_GROUP", "JARLABEL", "JARLABEL", "research_dynamic_group"));
        put(map, new Requirement("JARLABEL", 6, "NORMAL_CRAFTING", "JarLabelNull", "COMPOUND", "", "JARLABEL", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("JARVOID", 1, "ARCANE_CRAFTING", "JarVoid", "ARCANE_SHAPED", "JARVOID", "JARVOID", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("TUBES", 1, "ARCANE_CRAFTING", "Tube", "ARCANE_SHAPED", "TUBES", "TUBES", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("TUBES", 3, "ARCANE_CRAFTING", "TubeValve", "ARCANE_SHAPELESS", "TUBES", "TUBES", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("TUBES", 5, "ARCANE_CRAFTING", "Resonator", "ARCANE_SHAPED", "TUBES", "TUBES", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("TUBEFILTER", 1, "ARCANE_CRAFTING", "TubeFilter", "ARCANE_SHAPELESS", "TUBEFILTER", "TUBEFILTER", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("TUBEFILTER", 3, "ARCANE_CRAFTING", "TubeRestrict", "ARCANE_SHAPELESS", "TUBEFILTER", "TUBEFILTER", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("TUBEFILTER", 4, "ARCANE_CRAFTING", "TubeOneway", "ARCANE_SHAPELESS", "TUBEFILTER", "TUBEFILTER", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("ESSENTIACRYSTAL", 1, "ARCANE_CRAFTING", "EssentiaCrystalizer", "ARCANE_SHAPED", "ESSENTIACRYSTAL", "ESSENTIACRYSTAL", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("CENTRIFUGE", 1, "ARCANE_CRAFTING", "Centrifuge", "ARCANE_SHAPED", "CENTRIFUGE", "CENTRIFUGE", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("CENTRIFUGE", 4, "ARCANE_CRAFTING", "TubeBuffer", "ARCANE_SHAPED", "CENTRIFUGE", "CENTRIFUGE", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("THAUMATORIUM", 1, "COMPOUND_CRAFTING", "Thaumatorium", "COMPOUND", "", "THAUMATORIUM", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("THAUMATORIUM", 4, "ARCANE_CRAFTING", "MnemonicMatrix", "ARCANE_SHAPED", "THAUMATORIUM", "THAUMATORIUM", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("HUNGRYCHEST", 1, "ARCANE_CRAFTING", "HungryChest", "ARCANE_SHAPED", "HUNGRYCHEST", "HUNGRYCHEST", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("GOLEMFETTER", 1, "ARCANE_CRAFTING", "GolemFetter", "ARCANE_SHAPED", "GOLEMFETTER", "GOLEMFETTER", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("TRAVELTRUNK", 1, "INFUSION_CRAFTING", "TravelTrunk", "INFUSION", "TRAVELTRUNK", "TRAVELTRUNK", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("GOLEMSTRAW", 2, "CRUCIBLE_CRAFTING", "GolemStraw", "CRUCIBLE", "GOLEMSTRAW", "GOLEMSTRAW", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("GOLEMWOOD", 1, "CRUCIBLE_CRAFTING", "GolemWood", "CRUCIBLE", "GOLEMWOOD", "GOLEMWOOD", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("GOLEMTALLOW", 1, "NORMAL_CRAFTING", "BlockTallow", "COMPOUND", "", "GOLEMTALLOW", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("GOLEMTALLOW", 2, "CRUCIBLE_CRAFTING", "GolemTallow", "CRUCIBLE", "GOLEMTALLOW", "GOLEMTALLOW", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("GOLEMCLAY", 1, "CRUCIBLE_CRAFTING", "GolemClay", "CRUCIBLE", "GOLEMCLAY", "GOLEMCLAY", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("GOLEMFLESH", 1, "NORMAL_CRAFTING", "BlockFlesh", "COMPOUND", "", "GOLEMFLESH", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("GOLEMFLESH", 2, "CRUCIBLE_CRAFTING", "GolemFlesh", "CRUCIBLE", "GOLEMFLESH", "GOLEMFLESH", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("GOLEMSTONE", 1, "CRUCIBLE_CRAFTING", "GolemStone", "CRUCIBLE", "GOLEMSTONE", "GOLEMSTONE", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("GOLEMIRON", 1, "CRUCIBLE_CRAFTING", "GolemIron", "CRUCIBLE", "GOLEMIRON", "GOLEMIRON", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("GOLEMTHAUMIUM", 1, "NORMAL_CRAFTING", "BlockThaumium", "COMPOUND", "", "GOLEMTHAUMIUM", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("GOLEMTHAUMIUM", 2, "CRUCIBLE_CRAFTING", "GolemThaumium", "CRUCIBLE", "GOLEMTHAUMIUM", "GOLEMTHAUMIUM", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("GOLEMBELL", 2, "ARCANE_CRAFTING", "GolemBell", "ARCANE_SHAPED", "GOLEMBELL", "GOLEMBELL", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("COREGATHER", 1, "ARCANE_CRAFTING", "CoreBlank", "ARCANE_SHAPED", "COREGATHER", "COREGATHER", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("COREGATHER", 3, "CRUCIBLE_CRAFTING", "CoreGather", "CRUCIBLE", "COREGATHER", "COREGATHER", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("COREFILL", 1, "CRUCIBLE_CRAFTING", "CoreFill", "CRUCIBLE", "COREFILL", "COREFILL", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("COREEMPTY", 1, "CRUCIBLE_CRAFTING", "CoreEmpty", "CRUCIBLE", "COREEMPTY", "COREEMPTY", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("CORESORTING", 1, "INFUSION_CRAFTING", "CoreSorting", "INFUSION", "CORESORTING", "CORESORTING", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("COREUSE", 2, "INFUSION_CRAFTING", "CoreUse", "INFUSION", "COREUSE", "COREUSE", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("COREHARVEST", 1, "CRUCIBLE_CRAFTING", "CoreHarvest", "CRUCIBLE", "COREHARVEST", "COREHARVEST", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("COREFISHING", 1, "INFUSION_CRAFTING", "CoreFishing", "INFUSION", "COREFISHING", "COREFISHING", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("CORELUMBER", 1, "INFUSION_CRAFTING", "CoreLumber", "INFUSION", "CORELUMBER", "CORELUMBER", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("COREGUARD", 1, "CRUCIBLE_CRAFTING", "CoreGuard", "CRUCIBLE", "COREGUARD", "COREGUARD", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("COREBUTCHER", 1, "CRUCIBLE_CRAFTING", "CoreButcher", "CRUCIBLE", "COREBUTCHER", "COREBUTCHER", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("CORELIQUID", 1, "CRUCIBLE_CRAFTING", "CoreLiquid", "CRUCIBLE", "CORELIQUID", "CORELIQUID", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("COREALCHEMY", 1, "INFUSION_CRAFTING", "CoreAlchemy", "INFUSION", "COREALCHEMY", "COREALCHEMY", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("UPGRADEAIR", 1, "ARCANE_CRAFTING", "UpgradeAir", "ARCANE_SHAPED", "UPGRADEAIR", "UPGRADEAIR", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("UPGRADEEARTH", 1, "ARCANE_CRAFTING", "UpgradeEarth", "ARCANE_SHAPED", "UPGRADEEARTH", "UPGRADEEARTH", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("UPGRADEFIRE", 1, "ARCANE_CRAFTING", "UpgradeFire", "ARCANE_SHAPED", "UPGRADEFIRE", "UPGRADEFIRE", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("UPGRADEWATER", 1, "ARCANE_CRAFTING", "UpgradeWater", "ARCANE_SHAPED", "UPGRADEWATER", "UPGRADEWATER", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("UPGRADEORDER", 1, "ARCANE_CRAFTING", "UpgradeOrder", "ARCANE_SHAPED", "UPGRADEORDER", "UPGRADEORDER", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("UPGRADEENTROPY", 1, "ARCANE_CRAFTING", "UpgradeEntropy", "ARCANE_SHAPED", "UPGRADEENTROPY", "UPGRADEENTROPY", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("ADVANCEDGOLEM", 1, "INFUSION_CRAFTING", "AdvancedGolem", "INFUSION", "ADVANCEDGOLEM", "ADVANCEDGOLEM", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("TINYHAT", 1, "ARCANE_CRAFTING", "TinyHat", "ARCANE_SHAPED", "TINYHAT", "TINYHAT", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("TINYGLASSES", 1, "ARCANE_CRAFTING", "TinyGlasses", "ARCANE_SHAPED", "TINYGLASSES", "TINYGLASSES", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("TINYBOWTIE", 1, "ARCANE_CRAFTING", "TinyBowtie", "ARCANE_SHAPED", "TINYBOWTIE", "TINYBOWTIE", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("TINYFEZ", 1, "ARCANE_CRAFTING", "TinyFez", "ARCANE_SHAPED", "TINYFEZ", "TINYFEZ", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("TINYDART", 1, "ARCANE_CRAFTING", "TinyDart", "ARCANE_SHAPED", "TINYDART", "TINYDART", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("TINYVISOR", 1, "ARCANE_CRAFTING", "TinyVisor", "ARCANE_SHAPED", "TINYVISOR", "TINYVISOR", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("TINYARMOR", 1, "ARCANE_CRAFTING", "TinyArmor", "ARCANE_SHAPED", "TINYARMOR", "TINYARMOR", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("TINYHAMMER", 1, "ARCANE_CRAFTING", "TinyHammer", "ARCANE_SHAPED", "TINYHAMMER", "TINYHAMMER", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("RESEARCH", 5, "NORMAL_CRAFTING", "Scribe1", "COMPOUND", "", "RESEARCH", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("RESEARCH", 6, "NORMAL_CRAFTING", "Scribe2", "COMPOUND", "", "RESEARCH", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("RESEARCH", 7, "NORMAL_CRAFTING", "Scribe3", "COMPOUND", "", "RESEARCH", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("KNOWFRAG", 1, "NORMAL_CRAFTING", "KnowFrag", "COMPOUND", "", "KNOWFRAG", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("THAUMONOMICON", 1, "COMPOUND_CRAFTING", "Thaumonomicon", "COMPOUND", "", "THAUMONOMICON", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("ORE", 2, "NORMAL_CRAFTING", "RESEARCH:ORE", "DYNAMIC_RESEARCH_GROUP", "ORE", "ORE", "research_dynamic_group"));
        put(map, new Requirement("PLANTS", 1, "NORMAL_CRAFTING", "PlankGreatwood", "COMPOUND", "", "PLANTS", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("PLANTS", 3, "NORMAL_CRAFTING", "PlankSilverwood", "COMPOUND", "", "PLANTS", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("NODEJAR", 1, "COMPOUND_CRAFTING", "NodeJar", "COMPOUND", "", "NODEJAR", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("DECONSTRUCTOR", 1, "ARCANE_CRAFTING", "Deconstructor", "ARCANE_SHAPED", "DECONSTRUCTOR", "DECONSTRUCTOR", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("ELDRITCHMINOR", 1, "CRUCIBLE_CRAFTING", "VoidSeed", "CRUCIBLE", "VOIDMETAL", "VOIDMETAL", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("OCULUS", 1, "INFUSION_CRAFTING", "EldritchEye", "INFUSION", "OCULUS", "OCULUS", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("ADVALCHEMYFURNACE", 1, "ARCANE_CRAFTING", "AdvAlchemyConstruct", "ARCANE_SHAPED", "ADVALCHEMYFURNACE", "ADVALCHEMYFURNACE", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("ADVALCHEMYFURNACE", 3, "COMPOUND_CRAFTING", "AdvAlchemyFurnace", "COMPOUND", "", "ADVALCHEMYFURNACE", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("PRIMALCRUSHER", 1, "INFUSION_CRAFTING", "PrimalCrusher", "INFUSION", "PRIMALCRUSHER", "PRIMALCRUSHER", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("SANITYCHECK", 1, "INFUSION_CRAFTING", "SanityCheck", "INFUSION", "SANITYCHECK", "SANITYCHECK", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("VOIDMETAL", 1, "CRUCIBLE_CRAFTING", "VoidMetal", "CRUCIBLE", "VOIDMETAL", "VOIDMETAL", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("VOIDMETAL", 3, "NORMAL_CRAFTING", "VoidAxe", "COMPOUND", "", "VOIDMETAL", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("VOIDMETAL", 4, "NORMAL_CRAFTING", "VoidSword", "COMPOUND", "", "VOIDMETAL", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("VOIDMETAL", 5, "NORMAL_CRAFTING", "VoidPick", "COMPOUND", "", "VOIDMETAL", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("VOIDMETAL", 6, "NORMAL_CRAFTING", "VoidShovel", "COMPOUND", "", "VOIDMETAL", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("VOIDMETAL", 7, "NORMAL_CRAFTING", "VoidHoe", "COMPOUND", "", "VOIDMETAL", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("VOIDMETAL", 8, "NORMAL_CRAFTING", "VoidHelm", "COMPOUND", "", "VOIDMETAL", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("VOIDMETAL", 9, "NORMAL_CRAFTING", "VoidChest", "COMPOUND", "", "VOIDMETAL", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("VOIDMETAL", 10, "NORMAL_CRAFTING", "VoidLegs", "COMPOUND", "", "VOIDMETAL", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("VOIDMETAL", 11, "NORMAL_CRAFTING", "VoidBoots", "COMPOUND", "", "VOIDMETAL", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("ESSENTIARESERVOIR", 1, "INFUSION_CRAFTING", "EssentiaReservoir", "INFUSION", "ESSENTIARESERVOIR", "ESSENTIARESERVOIR", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("CAP_void", 1, "ARCANE_CRAFTING", "WandCapVoidInert", "ARCANE_SHAPED", "CAP_void", "CAP_void", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("CAP_void", 2, "INFUSION_CRAFTING", "WandCapVoid", "INFUSION", "CAP_void", "CAP_void", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("ARMORVOIDFORTRESS", 1, "INFUSION_CRAFTING", "VoidRobeHelm", "INFUSION", "ARMORVOIDFORTRESS", "ARMORVOIDFORTRESS", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("ARMORVOIDFORTRESS", 2, "INFUSION_CRAFTING", "VoidRobeChest", "INFUSION", "ARMORVOIDFORTRESS", "ARMORVOIDFORTRESS", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("ARMORVOIDFORTRESS", 3, "INFUSION_CRAFTING", "VoidRobeLegs", "INFUSION", "ARMORVOIDFORTRESS", "ARMORVOIDFORTRESS", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("FOCUSPRIMAL", 1, "ARCANE_CRAFTING", "FocusPrimal", "ARCANE_SHAPED", "FOCUSPRIMAL", "FOCUSPRIMAL", "tc4_recipe_runtime_bridge"));
        put(map, new Requirement("ROD_primal_staff", 1, "INFUSION_CRAFTING", "WandRodPrimalStaff", "INFUSION", "ROD_primal_staff", "ROD_primal_staff", "tc4_recipe_runtime_bridge"));
        return Collections.unmodifiableMap(map);
    }

    private static Map<String, List<Requirement>> buildByRequiredResearch() {
        Map<String, List<Requirement>> map = new LinkedHashMap<>();
        for (Requirement requirement : BY_RECIPE_KEY.values()) {
            String key = normalize(requirement.requiredResearch());
            if (key.isBlank()) {
                continue;
            }
            map.computeIfAbsent(key, ignored -> new ArrayList<>()).add(requirement);
        }
        Map<String, List<Requirement>> frozen = new LinkedHashMap<>();
        for (Map.Entry<String, List<Requirement>> entry : map.entrySet()) {
            frozen.put(entry.getKey(), Collections.unmodifiableList(entry.getValue()));
        }
        return Collections.unmodifiableMap(frozen);
    }

    private static void put(Map<String, Requirement> map, Requirement requirement) {
        map.put(normalize(requirement.recipeKey()), requirement);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    public static Collection<Requirement> all() {
        return BY_RECIPE_KEY.values();
    }

    public static Optional<Requirement> byRecipeKey(String recipeKey) {
        return Optional.ofNullable(BY_RECIPE_KEY.get(normalize(recipeKey)));
    }

    public static List<Requirement> recipesUnlockedBy(String researchKey) {
        return BY_REQUIRED_RESEARCH.getOrDefault(normalize(researchKey), List.of());
    }

    public static String requiredResearchFor(String recipeKey, String fallbackResearch) {
        if (recipeKey != null && recipeKey.startsWith("RESEARCH:")) {
            return recipeKey.substring("RESEARCH:".length());
        }
        Requirement requirement = BY_RECIPE_KEY.get(normalize(recipeKey));
        if (requirement != null && !requirement.requiredResearch().isBlank()) {
            return requirement.requiredResearch();
        }
        return fallbackResearch == null ? "" : fallbackResearch;
    }

    public static String requiredResearchForRuntimeRecipe(String tc4Key, String declaredResearch) {
        if (tc4Key != null && !tc4Key.isBlank()) {
            Requirement requirement = BY_RECIPE_KEY.get(normalize(tc4Key));
            if (requirement != null && !requirement.requiredResearch().isBlank()) {
                return requirement.requiredResearch();
            }
        }
        return declaredResearch == null ? "" : declaredResearch;
    }

    public static boolean isUnlocked(String recipeKey, String fallbackResearch, Predicate<String> hasResearch) {
        String required = requiredResearchFor(recipeKey, fallbackResearch);
        return required.isBlank() || hasResearch.test(required);
    }

    public static boolean isRuntimeRecipeUnlocked(String tc4Key, String declaredResearch, Predicate<String> hasResearch) {
        String required = requiredResearchForRuntimeRecipe(tc4Key, declaredResearch);
        return required.isBlank() || hasResearch.test(required);
    }

    public static boolean isUnlocked(Player player, String recipeKey, String fallbackResearch) {
        return isUnlocked(recipeKey, fallbackResearch, key -> PlayerThaumData.hasResearch(player, key));
    }

    public static boolean isRuntimeRecipeUnlocked(Player player, String tc4Key, String declaredResearch) {
        return isRuntimeRecipeUnlocked(tc4Key, declaredResearch, key -> PlayerThaumData.hasResearch(player, key));
    }
}
