package com.darkifov.thaumcraft.alchemy;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class AlchemyRecipes {
    private static final List<AlchemyRecipe> FALLBACK_RECIPES = new ArrayList<>();
    private static final List<AlchemyRecipe> LOADED_RECIPES = new ArrayList<>();
    private static final List<AlchemyRecipe> STRICT_ORIGINAL_RECIPES = new ArrayList<>();
    private static final List<AlchemyRecipe> NON_ORIGINAL_RECIPES = new ArrayList<>();

    static {
        FALLBACK_RECIPES.add(new AlchemyRecipe(
                new ResourceLocation(ThaumcraftMod.MOD_ID, "fallback_thaumium_ingot"),
                new ResourceLocation("minecraft", "iron_ingot"),
                new ResourceLocation(ThaumcraftMod.MOD_ID, "thaumium_ingot"),
                1
        ).require(Aspect.PRAECANTATIO, 4));

        FALLBACK_RECIPES.add(new AlchemyRecipe(
                new ResourceLocation(ThaumcraftMod.MOD_ID, "fallback_nitor"),
                new ResourceLocation("minecraft", "glowstone_dust"),
                new ResourceLocation(ThaumcraftMod.MOD_ID, "nitor"),
                1
        ).require(Aspect.IGNIS, 3)
                .require(Aspect.POTENTIA, 3)
                .require(Aspect.LUX, 3));

        FALLBACK_RECIPES.add(new AlchemyRecipe(
                new ResourceLocation(ThaumcraftMod.MOD_ID, "fallback_alchemy_dust"),
                new ResourceLocation("minecraft", "redstone"),
                new ResourceLocation(ThaumcraftMod.MOD_ID, "alchemy_dust"),
                2
        ).require(Aspect.PRAECANTATIO, 2)
                .require(Aspect.ORDO, 1)
                .require(Aspect.PERDITIO, 1));
    }

    public static List<AlchemyRecipe> fallbackOnly() { return Collections.unmodifiableList(FALLBACK_RECIPES); }

    private AlchemyRecipes() {
    }

    public static List<AlchemyRecipe> recipes() {
        if (!STRICT_ORIGINAL_RECIPES.isEmpty()) {
            return Collections.unmodifiableList(STRICT_ORIGINAL_RECIPES);
        }

        if (!LOADED_RECIPES.isEmpty()) {
            return Collections.unmodifiableList(LOADED_RECIPES);
        }

        return Collections.unmodifiableList(FALLBACK_RECIPES);
    }

    public static List<AlchemyRecipe> nonOriginalRecipes() {
        return Collections.unmodifiableList(NON_ORIGINAL_RECIPES);
    }

    public static void setLoadedRecipes(List<AlchemyRecipe> recipes) {
        LOADED_RECIPES.clear();
        STRICT_ORIGINAL_RECIPES.clear();
        NON_ORIGINAL_RECIPES.clear();
        LOADED_RECIPES.addAll(recipes);
        for (AlchemyRecipe recipe : recipes) {
            if (!recipe.tc4Key().isBlank()) {
                STRICT_ORIGINAL_RECIPES.add(recipe);
            } else {
                NON_ORIGINAL_RECIPES.add(recipe);
            }
        }
    }

    public static AlchemyRecipe findByCatalyst(ItemStack catalyst) {
        for (AlchemyRecipe recipe : recipes()) {
            if (recipe.catalystMatches(catalyst)) {
                return recipe;
            }
        }

        return null;
    }
}
