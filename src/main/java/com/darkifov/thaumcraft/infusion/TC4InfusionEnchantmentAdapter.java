package com.darkifov.thaumcraft.infusion;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.recipe.TC4RecipeItemResolver;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Stage208 adapter for original TC4 InfusionEnchantmentRecipe semantics.
 *
 * <p>TC4 keeps infusion enchantments outside the normal item-output recipe
 * table: recipeType == 1, output is an Enchantment, XP is drained one level at a
 * time before essentia, and craftingFinish increments the existing enchantment
 * level on the central pedestal stack. This class maps the carried TC4 index
 * into runtime InfusionRecipe records without adding fake item outputs.</p>
 */
public final class TC4InfusionEnchantmentAdapter {
    private TC4InfusionEnchantmentAdapter() {
    }

    public static List<InfusionRecipe> materializeRecipes() {
        List<InfusionRecipe> recipes = new ArrayList<>();
        for (TC4InfusionEnchantmentIndex.Entry entry : TC4InfusionEnchantmentIndex.entries()) {
            recipes.add(InfusionRecipe.enchantmentRecipe(entry, resolveComponents(entry)));
        }
        return recipes;
    }

    private static List<ResourceLocation> resolveComponents(TC4InfusionEnchantmentIndex.Entry entry) {
        List<ResourceLocation> out = new ArrayList<>();
        for (String expression : entry.legacyComponentExpressions()) {
            String resolved = resolveLegacyComponent(expression);
            if (!resolved.isBlank()) {
                out.add(new ResourceLocation(resolved));
            }
        }
        return out;
    }

    private static String resolveLegacyComponent(String expression) {
        if (expression == null || expression.isBlank()) {
            return "";
        }
        String trimmed = expression.trim();
        int colon = trimmed.lastIndexOf(':');
        if (trimmed.startsWith("ConfigItems.") && colon > 0) {
            String field = trimmed.substring("ConfigItems.".length(), colon);
            try {
                return TC4RecipeItemResolver.resolveConfigItem(field, Integer.parseInt(trimmed.substring(colon + 1)));
            } catch (NumberFormatException ignored) {
                return TC4RecipeItemResolver.resolveConfigItem(field);
            }
        }
        if (trimmed.startsWith("ConfigBlocks.") && colon > 0) {
            String field = trimmed.substring("ConfigBlocks.".length(), colon);
            try {
                return TC4RecipeItemResolver.resolveConfigBlock(field, Integer.parseInt(trimmed.substring(colon + 1)));
            } catch (NumberFormatException ignored) {
                return "";
            }
        }
        if (trimmed.startsWith("Items.")) {
            return TC4RecipeItemResolver.resolveVanillaItemField(trimmed.substring("Items.".length()));
        }
        if (trimmed.startsWith("Blocks.")) {
            return TC4RecipeItemResolver.resolveVanillaBlockField(trimmed.substring("Blocks.".length()));
        }
        return TC4RecipeItemResolver.resolveLegacyRecipeExpression(trimmed);
    }

    public static Enchantment enchantment(InfusionRecipe recipe) {
        if (recipe == null || recipe.enchantmentId() == null) {
            return null;
        }
        return ForgeRegistries.ENCHANTMENTS.getValue(recipe.enchantmentId());
    }

    public static boolean canApply(InfusionRecipe recipe, ItemStack central) {
        Enchantment enchantment = enchantment(recipe);
        if (enchantment == null || central == null || central.isEmpty()) {
            return false;
        }
        if (!enchantment.canEnchant(central) || !central.getItem().canApplyAtEnchantingTable(central, enchantment)) {
            return false;
        }
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(central);
        int currentLevel = enchantments.getOrDefault(enchantment, 0);
        if (currentLevel >= enchantment.getMaxLevel()) {
            return false;
        }
        for (Enchantment other : enchantments.keySet()) {
            if (other != enchantment && (!enchantment.isCompatibleWith(other) || !other.isCompatibleWith(enchantment))) {
                return false;
            }
        }
        return true;
    }

    public static int calcInstability(InfusionRecipe recipe, ItemStack central) {
        int totalLevels = 0;
        for (int level : EnchantmentHelper.getEnchantments(central).values()) {
            totalLevels += level;
        }
        return totalLevels / 2 + recipe.instability();
    }

    public static int calcXp(InfusionRecipe recipe, ItemStack central) {
        Enchantment enchantment = enchantment(recipe);
        if (enchantment == null) {
            return 0;
        }
        int base = Math.max(1, enchantment.getMinCost(1) / 3);
        int current = EnchantmentHelper.getItemEnchantmentLevel(enchantment, central);
        return base * (1 + current);
    }

    public static EnumMap<Aspect, Integer> scaledAspects(InfusionRecipe recipe, ItemStack central) {
        EnumMap<Aspect, Integer> out = new EnumMap<>(Aspect.class);
        Enchantment enchantment = enchantment(recipe);
        float mod = 0.0F;
        if (enchantment != null) {
            Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(central);
            for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                if (entry.getKey() == enchantment) {
                    mod += entry.getValue();
                } else {
                    mod += entry.getValue() * 0.1F;
                }
            }
        }
        for (Map.Entry<Aspect, Integer> entry : recipe.aspectCost().entrySet()) {
            int base = Math.max(0, entry.getValue());
            out.put(entry.getKey(), base + (int) (base * mod));
        }
        return out;
    }

    public static boolean applyOutput(InfusionRecipe recipe, ItemStack central) {
        Enchantment enchantment = enchantment(recipe);
        if (enchantment == null || central == null || central.isEmpty()) {
            return false;
        }
        int current = EnchantmentHelper.getItemEnchantmentLevel(enchantment, central);
        int next = Math.min(enchantment.getMaxLevel(), current + 1);
        Map<Enchantment, Integer> enchantments = new LinkedHashMap<>(EnchantmentHelper.getEnchantments(central));
        enchantments.put(enchantment, next);
        EnchantmentHelper.setEnchantments(enchantments, central);
        return true;
    }
}
