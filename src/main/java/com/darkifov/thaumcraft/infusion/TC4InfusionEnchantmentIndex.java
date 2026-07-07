package com.darkifov.thaumcraft.infusion;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.recipe.TC4RecipeRequirementIndex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;

/**
 * Stage154 strict TC4 parity index for ConfigRecipes.addInfusionEnchantmentRecipe.
 *
 * <p>Important: these are not fake item recipes. In original Thaumcraft 4 the
 * infusion matrix treats them as a separate recipe family whose output is an
 * Enchantment and whose central pedestal item is the item being enchanted.
 * This index preserves the original key, research gate, obfuscated 1.7.10
 * Enchantment expression, aspects and instability while the 1.19.2 matrix
 * runtime is incrementally adapted around it.</p>
 */
public final class TC4InfusionEnchantmentIndex {
    public record Entry(
            String tc4Key,
            String research,
            String legacyEnchantmentExpression,
            String modernEnchantmentId,
            int instability,
            EnumMap<Aspect, Integer> aspects,
            String[] legacyComponentExpressions,
            boolean customThaumcraftEnchantment
    ) {
        public String requiredResearch() {
            return TC4RecipeRequirementIndex.requiredResearchForRuntimeRecipe(tc4Key, research);
        }

        public String stableId() {
            return tc4Key.toLowerCase(Locale.ROOT);
        }

        public int baseAspectTotal() {
            int total = 0;
            for (int amount : aspects.values()) {
                total += amount;
            }
            return total;
        }
    }

    private static final List<Entry> ENTRIES = new ArrayList<>();

    static {
        add("InfEnchRepair", "Enchantment.field_77331_b[ThaumcraftApi.enchantRepair]", "thaumcraft:repair", 4, true,
                new String[] {"Blocks.field_150467_bQ", "ConfigItems.itemResource:14"}, "PRAECANTATIO:8", "FABRICO:10", "ORDO:10");
        add("InfEnchHaste", "Enchantment.field_77331_b[ThaumcraftApi.enchantHaste]", "thaumcraft:haste", 3, true,
                new String[] {"ConfigItems.itemResource:1", "ConfigItems.itemResource:14"}, "PRAECANTATIO:4", "ITER:8", "VOLATUS:8");
        add("InfEnch0", "Enchantment.field_77332_c", "minecraft:protection", 1, false,
                new String[] {"Items.field_151042_j", "ConfigItems.itemResource:14"}, "PRAECANTATIO:4", "TUTAMEN:8");
        add("InfEnch1", "Enchantment.field_77329_d", "minecraft:fire_protection", 1, false,
                new String[] {"Items.field_151042_j", "Items.field_151064_bs", "ConfigItems.itemResource:14"}, "PRAECANTATIO:4", "TUTAMEN:4", "IGNIS:4");
        add("InfEnch2", "Enchantment.field_77327_f", "minecraft:blast_protection", 1, false,
                new String[] {"Items.field_151042_j", "Items.field_151016_H", "ConfigItems.itemResource:14"}, "PRAECANTATIO:4", "TUTAMEN:4", "PERDITIO:4");
        add("InfEnch3", "Enchantment.field_77328_g", "minecraft:projectile_protection", 1, false,
                new String[] {"Items.field_151042_j", "Items.field_151032_g", "ConfigItems.itemResource:14"}, "PRAECANTATIO:4", "TUTAMEN:4", "VOLATUS:4");
        add("InfEnch4", "Enchantment.field_77330_e", "minecraft:feather_falling", 1, false,
                new String[] {"Items.field_151008_G", "ConfigItems.itemResource:14"}, "PRAECANTATIO:4", "AER:4", "VOLATUS:4");
        add("InfEnch5", "Enchantment.field_77340_h", "minecraft:respiration", 2, false,
                new String[] {"Items.field_151120_aE", "ConfigItems.itemResource:14"}, "PRAECANTATIO:4", "AER:8", "AQUA:8");
        add("InfEnch6", "Enchantment.field_77341_i", "minecraft:aqua_affinity", 2, false,
                new String[] {"Items.field_151120_aE", "Items.field_151123_aH", "ConfigItems.itemResource:14"}, "PRAECANTATIO:4", "MOTUS:8", "AQUA:8");
        add("InfEnch7", "Enchantment.field_92091_k", "minecraft:thorns", 2, false,
                new String[] {"Blocks.field_150330_I", "Items.field_151128_bU", "ConfigItems.itemResource:14"}, "PRAECANTATIO:4", "TELUM:8", "HERBA:8");
        add("InfEnch8", "Enchantment.field_77338_j", "minecraft:sharpness", 2, false,
                new String[] {"Items.field_151040_l", "ConfigItems.itemResource:14"}, "PRAECANTATIO:4", "TELUM:8");
        add("InfEnch9", "Enchantment.field_77339_k", "minecraft:smite", 2, false,
                new String[] {"Items.field_151040_l", "Items.field_151114_aO", "ConfigItems.itemResource:14"}, "PRAECANTATIO:4", "TELUM:4", "EXANIMIS:4");
        add("InfEnch10", "Enchantment.field_77336_l", "minecraft:bane_of_arthropods", 2, false,
                new String[] {"Items.field_151040_l", "ConfigItems.itemResource:6", "ConfigItems.itemResource:14"}, "PRAECANTATIO:4", "TELUM:4", "BESTIA:4");
        add("InfEnch11", "Enchantment.field_77337_m", "minecraft:knockback", 1, false,
                new String[] {"Blocks.field_150331_J", "ConfigItems.itemResource:14"}, "PRAECANTATIO:4", "TELUM:3", "MOTUS:3");
        add("InfEnch12", "Enchantment.field_77334_n", "minecraft:fire_aspect", 3, false,
                new String[] {"Items.field_151040_l", "Items.field_151065_br", "ConfigItems.itemResource:14"}, "PRAECANTATIO:4", "TELUM:4", "IGNIS:8");
        add("InfEnch13", "Enchantment.field_77335_o", "minecraft:looting", 3, false,
                new String[] {"Items.field_151040_l", "Items.field_151045_i", "ConfigItems.itemResource:14"}, "PRAECANTATIO:4", "TELUM:4", "LUCRUM:8");
        add("InfEnch14", "Enchantment.field_77349_p", "minecraft:efficiency", 2, false,
                new String[] {"Items.field_151035_b", "ConfigItems.itemResource:14"}, "PRAECANTATIO:4", "INSTRUMENTUM:4", "ORDO:4");
        add("InfEnch15", "Enchantment.field_77348_q", "minecraft:silk_touch", 5, false,
                new String[] {"Items.field_151035_b", "Blocks.field_150321_G", "ConfigItems.itemResource:14"}, "PRAECANTATIO:16", "INSTRUMENTUM:16", "ORDO:16", "METO:16", "PERFODIO:16");
        add("InfEnch16", "Enchantment.field_77347_r", "minecraft:unbreaking", 2, false,
                new String[] {"Items.field_151035_b", "Blocks.field_150343_Z", "ConfigItems.itemResource:14"}, "PRAECANTATIO:4", "INSTRUMENTUM:4", "ORDO:8");
        add("InfEnch17", "Enchantment.field_77346_s", "minecraft:fortune", 3, false,
                new String[] {"Items.field_151035_b", "Items.field_151045_i", "ConfigItems.itemResource:14"}, "PRAECANTATIO:4", "INSTRUMENTUM:4", "LUCRUM:8");
        add("InfEnch18", "Enchantment.field_77345_t", "minecraft:power", 2, false,
                new String[] {"Items.field_151031_f", "ConfigItems.itemResource:14"}, "PRAECANTATIO:4", "TELUM:8");
        add("InfEnch19", "Enchantment.field_77344_u", "minecraft:punch", 2, false,
                new String[] {"Blocks.field_150331_J", "ConfigItems.itemResource:14"}, "PRAECANTATIO:4", "TELUM:3", "MOTUS:3");
        add("InfEnch20", "Enchantment.field_77343_v", "minecraft:flame", 3, false,
                new String[] {"Items.field_151031_f", "Items.field_151065_br", "ConfigItems.itemResource:14"}, "PRAECANTATIO:4", "TELUM:4", "IGNIS:8");
        add("InfEnch21", "Enchantment.field_77342_w", "minecraft:infinity", 5, false,
                new String[] {"Items.field_151031_f", "Items.field_151032_g", "ConfigItems.itemResource:14"}, "PRAECANTATIO:8", "TELUM:16", "VACUOS:16", "PERMUTATIO:16");
    }

    private TC4InfusionEnchantmentIndex() {
    }

    public static List<Entry> entries() {
        return Collections.unmodifiableList(ENTRIES);
    }

    public static Entry byKey(String tc4Key) {
        if (tc4Key == null || tc4Key.isBlank()) {
            return null;
        }
        for (Entry entry : ENTRIES) {
            if (entry.tc4Key().equals(tc4Key)) {
                return entry;
            }
        }
        return null;
    }

    public static List<Entry> vanillaEntries() {
        List<Entry> out = new ArrayList<>();
        for (Entry entry : ENTRIES) {
            if (!entry.customThaumcraftEnchantment()) {
                out.add(entry);
            }
        }
        return Collections.unmodifiableList(out);
    }

    public static List<Entry> customThaumcraftEntries() {
        List<Entry> out = new ArrayList<>();
        for (Entry entry : ENTRIES) {
            if (entry.customThaumcraftEnchantment()) {
                out.add(entry);
            }
        }
        return Collections.unmodifiableList(out);
    }

    private static void add(String key, String legacyEnchantment, String modernEnchantmentId, int instability, boolean custom,
                            String[] legacyComponents, String... aspectTokens) {
        EnumMap<Aspect, Integer> aspects = new EnumMap<>(Aspect.class);
        for (String token : aspectTokens) {
            String[] parts = token.split(":", 2);
            if (parts.length != 2) {
                continue;
            }
            try {
                Aspect aspect = Aspect.valueOf(parts[0]);
                int amount = Integer.parseInt(parts[1]);
                if (amount > 0) {
                    aspects.put(aspect, amount);
                }
            } catch (IllegalArgumentException ignored) {
            }
        }
        ENTRIES.add(new Entry(key, "INFUSIONENCHANTMENT", legacyEnchantment, modernEnchantmentId, instability, aspects, legacyComponents, custom));
    }
}
