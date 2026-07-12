package com.darkifov.thaumcraft.research;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Generated flags, triggers and warp from TC4 4.2.3.5 ConfigResearch.java. */
public final class TC4ResearchMetadataIndex {
    private static final Set<String> AUTO_UNLOCK = set("BASICTHAUMATURGY", "CAP_iron", "ROD_wood", "BASICARTIFACE", "ARCANESTONE", "GRATE", "TABLE", "ARCTABLE", "RESTABLE", "THAUMOMETER", "PHIAL", "CRUCIBLE", "ASPECTS", "PECH", "NODES", "WARP", "RESEARCH", "KNOWFRAG", "THAUMONOMICON", "ORE", "PLANTS", "ENCHANT");
    private static final Map<String, List<String>> FLAGS = buildFlags();
    private static final Map<String, List<String>> ITEM_TRIGGERS = buildItemTriggers();
    private static final Map<String, List<String>> ENTITY_TRIGGERS = buildEntityTriggers();
    private static final Map<String, List<String>> ASPECT_TRIGGERS = buildAspectTriggers();
    private static final Map<String, Integer> WARP = buildWarp();
    private TC4ResearchMetadataIndex() {}

    private static Set<String> set(String... values) {
        Set<String> result = new LinkedHashSet<>();
        Collections.addAll(result, values);
        return Collections.unmodifiableSet(result);
    }

    private static void put(Map<String, List<String>> map, String key, String... values) {
        List<String> list = new ArrayList<>();
        Collections.addAll(list, values);
        map.put(key, Collections.unmodifiableList(list));
    }

    private static Map<String, List<String>> buildFlags() {
        Map<String, List<String>> map = new LinkedHashMap<>();
        put(map, "BASICTHAUMATURGY", "auto_unlock", "stub", "round");
        put(map, "FOCUSFROST", "secondary", "concealed");
        put(map, "FOCUSHELLBAT", "hidden");
        put(map, "FOCUSEXCAVATION", "concealed");
        put(map, "FOCUSWARDING", "concealed");
        put(map, "FOCUSSHOCK", "secondary", "concealed");
        put(map, "FOCUSTRADE", "concealed");
        put(map, "FOCUSPORTABLEHOLE", "concealed");
        put(map, "FOCUSPOUCH", "secondary");
        put(map, "CAP_iron", "auto_unlock");
        put(map, "CAP_silver", "concealed");
        put(map, "ROD_wood", "auto_unlock");
        put(map, "ROD_reed", "secondary", "concealed");
        put(map, "ROD_blaze", "secondary", "concealed");
        put(map, "ROD_obsidian", "secondary", "concealed");
        put(map, "ROD_ice", "secondary", "concealed");
        put(map, "ROD_quartz", "secondary", "concealed");
        put(map, "ROD_bone", "secondary", "concealed");
        put(map, "SCEPTRE", "concealed");
        put(map, "ROD_reed_staff", "secondary", "concealed");
        put(map, "ROD_blaze_staff", "secondary", "concealed");
        put(map, "ROD_obsidian_staff", "secondary", "concealed");
        put(map, "ROD_ice_staff", "secondary", "concealed");
        put(map, "ROD_quartz_staff", "secondary", "concealed");
        put(map, "ROD_bone_staff", "secondary", "concealed");
        put(map, "ROD_silverwood_staff", "secondary", "concealed");
        put(map, "WANDPED", "concealed");
        put(map, "VISAMULET", "concealed");
        put(map, "WANDPEDFOC", "secondary", "concealed");
        put(map, "NODESTABILIZERADV", "secondary", "concealed");
        put(map, "VISPOWER", "special");
        put(map, "VISCHARGERELAY", "secondary", "concealed");
        put(map, "VAMPBAT", "secondary");
        put(map, "BASICARTIFACE", "auto_unlock", "stub", "round");
        put(map, "ARCANESTONE", "auto_unlock", "stub", "round");
        put(map, "GRATE", "auto_unlock", "stub", "round");
        put(map, "TABLE", "auto_unlock", "stub", "round");
        put(map, "ARCTABLE", "auto_unlock", "stub", "round");
        put(map, "RESTABLE", "auto_unlock", "stub", "round");
        put(map, "THAUMOMETER", "auto_unlock", "stub", "round");
        put(map, "PAVETRAVEL", "secondary");
        put(map, "PAVEWARD", "secondary");
        put(map, "GOGGLES", "concealed");
        put(map, "ARCANEEAR", "concealed");
        put(map, "SINSTONE", "concealed");
        put(map, "LEVITATOR", "concealed");
        put(map, "INFERNALFURNACE", "concealed");
        put(map, "BELLOWS", "secondary", "concealed");
        put(map, "ENCHFABRIC", "secondary");
        put(map, "INFUSION", "concealed");
        put(map, "FLUXSCRUB", "secondary");
        put(map, "RUNICARMOR", "concealed");
        put(map, "RUNICCHARGED", "secondary", "concealed");
        put(map, "RUNICHEALING", "secondary", "concealed");
        put(map, "RUNICKINETIC", "secondary", "concealed");
        put(map, "RUNICEMERGENCY", "secondary", "concealed");
        put(map, "BANNERS", "secondary", "hidden");
        put(map, "RUNICAUGMENTATION", "concealed");
        put(map, "BOOTSTRAVELLER", "concealed");
        put(map, "HOVERHARNESS", "concealed");
        put(map, "HOVERGIRDLE", "secondary", "hidden");
        put(map, "MIRROR", "hidden");
        put(map, "MIRRORHAND", "secondary", "concealed");
        put(map, "MIRRORESSENTIA", "secondary", "concealed");
        put(map, "ARCANEBORE", "concealed");
        put(map, "ARCANELAMP", "secondary");
        put(map, "LAMPGROWTH", "hidden");
        put(map, "LAMPFERTILITY", "hidden");
        put(map, "BONEBOW", "hidden");
        put(map, "PRIMALARROW", "concealed");
        put(map, "ELEMENTALAXE", "concealed");
        put(map, "ELEMENTALPICK", "concealed");
        put(map, "ELEMENTALSWORD", "concealed");
        put(map, "ELEMENTALSHOVEL", "concealed");
        put(map, "ELEMENTALHOE", "concealed");
        put(map, "JARBRAIN", "hidden");
        put(map, "INFUSIONENCHANTMENT", "concealed");
        put(map, "ARMORFORTRESS", "hidden");
        put(map, "HELMGOGGLES", "secondary", "concealed");
        put(map, "MASKGRINNINGDEVIL", "secondary", "concealed");
        put(map, "MASKANGRYGHOST", "secondary", "concealed");
        put(map, "MASKSIPPINGFIEND", "secondary", "concealed");
        put(map, "PHIAL", "auto_unlock", "stub", "round");
        put(map, "CRUCIBLE", "auto_unlock", "stub");
        put(map, "ALCHEMICALDUPLICATION", "secondary", "concealed");
        put(map, "ALCHEMICALMANUFACTURE", "secondary", "concealed");
        put(map, "ENTROPICPROCESSING", "secondary", "concealed");
        put(map, "LIQUIDDEATH", "hidden");
        put(map, "BOTTLETAINT", "hidden");
        put(map, "THAUMIUM", "hidden");
        put(map, "PUREIRON", "concealed");
        put(map, "PUREGOLD", "secondary", "concealed");
        put(map, "PURECOPPER", "secondary", "concealed");
        put(map, "PURETIN", "secondary", "concealed");
        put(map, "PURESILVER", "secondary", "concealed");
        put(map, "PURELEAD", "secondary", "concealed");
        put(map, "TRANSIRON", "concealed");
        put(map, "TRANSGOLD", "secondary", "concealed");
        put(map, "TRANSCOPPER", "secondary", "concealed");
        put(map, "TRANSTIN", "secondary", "concealed");
        put(map, "TRANSSILVER", "secondary", "concealed");
        put(map, "TRANSLEAD", "secondary", "concealed");
        put(map, "ETHEREALBLOOM", "concealed", "hidden");
        put(map, "BATHSALTS", "hidden");
        put(map, "ARCANESPA", "secondary");
        put(map, "JARLABEL", "stub", "round");
        put(map, "JARVOID", "secondary", "concealed");
        put(map, "TUBES", "secondary", "concealed");
        put(map, "TUBEFILTER", "secondary", "concealed");
        put(map, "ESSENTIACRYSTAL", "concealed");
        put(map, "CENTRIFUGE", "concealed");
        put(map, "THAUMATORIUM", "concealed");
        put(map, "HUNGRYCHEST", "secondary");
        put(map, "GOLEMFETTER", "secondary");
        put(map, "TRAVELTRUNK", "concealed");
        put(map, "GOLEMWOOD", "secondary");
        put(map, "GOLEMTALLOW", "concealed");
        put(map, "GOLEMCLAY", "secondary", "concealed");
        put(map, "GOLEMFLESH", "concealed");
        put(map, "GOLEMSTONE", "secondary", "concealed");
        put(map, "GOLEMIRON", "secondary", "concealed");
        put(map, "GOLEMTHAUMIUM", "concealed");
        put(map, "GOLEMBELL", "stub");
        put(map, "COREGATHER", "stub", "concealed");
        put(map, "COREFILL", "secondary", "concealed");
        put(map, "COREEMPTY", "secondary", "concealed");
        put(map, "CORESORTING", "secondary", "concealed");
        put(map, "COREUSE", "concealed");
        put(map, "COREHARVEST", "concealed");
        put(map, "COREFISHING", "secondary", "concealed");
        put(map, "CORELUMBER", "secondary", "concealed");
        put(map, "COREGUARD", "concealed");
        put(map, "COREBUTCHER", "secondary", "concealed");
        put(map, "CORELIQUID", "concealed");
        put(map, "COREALCHEMY", "secondary", "concealed");
        put(map, "UPGRADEAIR", "secondary", "concealed");
        put(map, "UPGRADEEARTH", "secondary", "concealed");
        put(map, "UPGRADEFIRE", "secondary", "concealed");
        put(map, "UPGRADEWATER", "secondary", "concealed");
        put(map, "UPGRADEORDER", "secondary", "concealed");
        put(map, "UPGRADEENTROPY", "secondary", "concealed");
        put(map, "ADVANCEDGOLEM", "concealed");
        put(map, "TINYHAT", "secondary", "hidden");
        put(map, "TINYGLASSES", "secondary", "hidden");
        put(map, "TINYBOWTIE", "secondary", "hidden");
        put(map, "TINYFEZ", "secondary", "hidden");
        put(map, "TINYDART", "secondary", "hidden");
        put(map, "TINYVISOR", "secondary", "hidden");
        put(map, "TINYARMOR", "secondary", "hidden");
        put(map, "TINYHAMMER", "secondary", "hidden");
        put(map, "ASPECTS", "auto_unlock", "stub", "round");
        put(map, "PECH", "auto_unlock", "stub", "round");
        put(map, "NODES", "auto_unlock", "stub", "round");
        put(map, "WARP", "auto_unlock", "stub", "round");
        put(map, "RESEARCH", "auto_unlock", "stub", "round");
        put(map, "KNOWFRAG", "auto_unlock", "stub", "round");
        put(map, "THAUMONOMICON", "auto_unlock", "stub", "round");
        put(map, "ORE", "auto_unlock", "stub", "round");
        put(map, "PLANTS", "auto_unlock", "stub", "round");
        put(map, "ENCHANT", "auto_unlock", "stub", "round");
        put(map, "NODETAPPER1", "round");
        put(map, "NODEPRESERVE", "round");
        put(map, "NODEJAR", "concealed");
        put(map, "NODETAPPER2", "round", "special");
        put(map, "RESEARCHER1", "round");
        put(map, "DECONSTRUCTOR", "round");
        put(map, "RESEARCHER2", "round", "special");
        put(map, "RESEARCHDUPE", "round");
        put(map, "CRIMSON", "stub", "round", "special", "hidden");
        put(map, "ELDRITCHMINOR", "round", "special", "hidden");
        put(map, "ELDRITCHMAJOR", "stub", "round", "special", "hidden");
        put(map, "OCULUS", "round", "special", "concealed");
        put(map, "ENTEROUTER", "stub", "round", "hidden");
        put(map, "OUTERREV", "secondary", "special", "lost");
        put(map, "PRIMPEARL", "secondary", "special", "lost");
        put(map, "PRIMNODE", "secondary");
        put(map, "ADVALCHEMYFURNACE", "secondary");
        put(map, "PRIMALCRUSHER", "concealed");
        put(map, "CAP_void", "concealed");
        put(map, "ARMORVOIDFORTRESS", "secondary", "concealed");
        put(map, "FOCUSPRIMAL", "concealed");
        put(map, "ROD_primal_staff", "hidden");
        return Collections.unmodifiableMap(map);
    }

    private static Map<String, List<String>> buildItemTriggers() {
        Map<String, List<String>> map = new LinkedHashMap<>();
        put(map, "BANNERS", "ConfigBlocks.blockWoodenDevice, 1, 8");
        put(map, "MIRROR", "ConfigBlocks.blockMirror, 1, 0", "Items.field_151079_bi", "Blocks.field_150427_aO, 1, 32767", "Blocks.field_150384_bq, 1, 32767", "Blocks.field_150378_br, 1, 32767");
        put(map, "BONEBOW", "ConfigItems.itemBowBone", "Items.field_151031_f, 1, 32767", "Items.field_151103_aS");
        put(map, "JARBRAIN", "ConfigBlocks.blockJar, 1, 1", "ConfigItems.itemResource, 1, 3");
        put(map, "TINYHAT", "ConfigItems.itemGolemDecoration, 1, 0", "Blocks.field_150325_L, 1, 32767");
        put(map, "TINYGLASSES", "ConfigItems.itemGolemDecoration, 1, 1", "Blocks.field_150325_L, 1, 32767");
        put(map, "TINYBOWTIE", "ConfigItems.itemGolemDecoration, 1, 2", "Blocks.field_150325_L, 1, 32767");
        put(map, "TINYFEZ", "ConfigItems.itemGolemDecoration, 1, 3", "Blocks.field_150325_L, 1, 32767");
        put(map, "OUTERREV", "ConfigBlocks.blockEldritch, 1, 5", "ConfigBlocks.blockEldritch, 1, 10");
        put(map, "PRIMPEARL", "ConfigItems.itemEldritchObject, 1, 3");
        put(map, "ROD_primal_staff", "ConfigItems.itemWandRod, 1, 100", "ConfigItems.itemFocusPrimal");
        return Collections.unmodifiableMap(map);
    }

    private static Map<String, List<String>> buildEntityTriggers() {
        Map<String, List<String>> map = new LinkedHashMap<>();
        put(map, "FOCUSHELLBAT", "Thaumcraft.Firebat");
        put(map, "MIRROR", "Enderman");
        put(map, "JARBRAIN", "Thaumcraft.BrainyZombie", "Thaumcraft.GiantBrainyZombie");
        put(map, "ROD_primal_staff", "Thaumcraft.PrimalOrb");
        return Collections.unmodifiableMap(map);
    }

    private static Map<String, List<String>> buildAspectTriggers() {
        Map<String, List<String>> map = new LinkedHashMap<>();
        put(map, "FOCUSHELLBAT", "IGNIS");
        put(map, "HOVERGIRDLE", "VOLATUS");
        put(map, "LAMPGROWTH", "LUX", "MESSIS");
        put(map, "LAMPFERTILITY", "LUX", "VICTUS");
        put(map, "ARMORFORTRESS", "TUTAMEN");
        put(map, "LIQUIDDEATH", "MORTUUS", "VENENUM");
        put(map, "BOTTLETAINT", "VITIUM");
        put(map, "THAUMIUM", "METALLUM");
        put(map, "ETHEREALBLOOM", "VITIUM");
        put(map, "TINYHAT", "PANNUS");
        put(map, "TINYGLASSES", "PANNUS");
        put(map, "TINYBOWTIE", "PANNUS");
        put(map, "TINYFEZ", "PANNUS");
        put(map, "TINYDART", "TELUM");
        put(map, "TINYVISOR", "TUTAMEN");
        put(map, "TINYARMOR", "TUTAMEN");
        put(map, "TINYHAMMER", "TELUM");
        return Collections.unmodifiableMap(map);
    }

    private static Map<String, Integer> buildWarp() {
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("FOCUSHELLBAT", 2);
        map.put("ROD_bone", 1);
        map.put("ROD_bone_staff", 1);
        map.put("SINSTONE", 2);
        map.put("INFERNALFURNACE", 2);
        map.put("JARBRAIN", 3);
        map.put("MASKANGRYGHOST", 1);
        map.put("MASKSIPPINGFIEND", 1);
        map.put("LIQUIDDEATH", 3);
        map.put("BOTTLETAINT", 2);
        map.put("GOLEMFLESH", 3);
        map.put("COREBUTCHER", 1);
        map.put("ADVANCEDGOLEM", 5);
        map.put("RESEARCHER2", 1);
        map.put("CRIMSON", 3);
        map.put("OCULUS", 6);
        map.put("PRIMNODE", 1);
        map.put("CAP_void", 1);
        map.put("FOCUSPRIMAL", 2);
        map.put("ROD_primal_staff", 3);
        return Collections.unmodifiableMap(map);
    }

    public static Set<String> autoUnlockKeys() { return AUTO_UNLOCK; }
    public static boolean isAutoUnlock(String key) { return AUTO_UNLOCK.contains(normalizeKey(key)); }
    public static List<String> flags(String key) { return FLAGS.getOrDefault(normalizeKey(key), List.of()); }
    public static List<String> itemTriggers(String key) { return ITEM_TRIGGERS.getOrDefault(normalizeKey(key), List.of()); }
    public static List<String> entityTriggers(String key) { return ENTITY_TRIGGERS.getOrDefault(normalizeKey(key), List.of()); }
    public static List<String> aspectTriggers(String key) { return ASPECT_TRIGGERS.getOrDefault(normalizeKey(key), List.of()); }
    public static int warp(String key) { return WARP.getOrDefault(normalizeKey(key), 0); }
    public static Map<String, List<String>> itemTriggerMap() { return ITEM_TRIGGERS; }
    public static Map<String, List<String>> entityTriggerMap() { return ENTITY_TRIGGERS; }
    public static Map<String, List<String>> aspectTriggerMap() { return ASPECT_TRIGGERS; }

    public static List<String> researchKeysForItemTrigger(String expression) {
        return reverseLookup(ITEM_TRIGGERS, expression, false);
    }

    public static List<String> researchKeysForEntityTrigger(String entityId) {
        return reverseLookup(ENTITY_TRIGGERS, entityId, false);
    }

    public static List<String> researchKeysForAspectTrigger(String aspectId) {
        return reverseLookup(ASPECT_TRIGGERS, aspectId, true);
    }

    private static List<String> reverseLookup(Map<String, List<String>> source, String value, boolean aspect) {
        String normalized = aspect ? normalizeAspect(value) : normalizeTrigger(value);
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : source.entrySet()) {
            for (String trigger : entry.getValue()) {
                String candidate = aspect ? normalizeAspect(trigger) : normalizeTrigger(trigger);
                if (candidate.equals(normalized)) { result.add(entry.getKey()); break; }
            }
        }
        return result;
    }

    private static String normalizeKey(String value) { return value == null ? "" : value.trim(); }
    private static String normalizeAspect(String value) { return value == null ? "" : value.trim(); }
    private static String normalizeTrigger(String value) {
        return value == null ? "" : value.replace(" ", "").trim().toLowerCase(Locale.ROOT);
    }
}
