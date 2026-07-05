package com.darkifov.thaumcraft.arcane;

import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ArcaneWorkbenchRecipes {
    private static final List<ArcaneWorkbenchRecipe> FALLBACK = new ArrayList<>();
    private static final List<ArcaneWorkbenchRecipe> LOADED = new ArrayList<>();

    static {
        FALLBACK.add(new ArcaneWorkbenchRecipe(
                new ResourceLocation(ThaumcraftMod.MOD_ID, "fallback_greatwood_wand"),
                new ResourceLocation(ThaumcraftMod.MOD_ID, "balanced_shard"),
                new ResourceLocation(ThaumcraftMod.MOD_ID, "greatwood_wand"),
                1,
                "FIRST_STEPS"
        ).ingredient(new ResourceLocation(ThaumcraftMod.MOD_ID, "greatwood_planks"))
                .ingredient(new ResourceLocation(ThaumcraftMod.MOD_ID, "thaumium_nugget")));

        FALLBACK.add(new ArcaneWorkbenchRecipe(
                new ResourceLocation(ThaumcraftMod.MOD_ID, "fallback_research_note"),
                new ResourceLocation("minecraft", "paper"),
                new ResourceLocation(ThaumcraftMod.MOD_ID, "research_note"),
                1,
                "FIRST_STEPS"
        ).ingredient(new ResourceLocation(ThaumcraftMod.MOD_ID, "alchemy_dust")));
    }

    private ArcaneWorkbenchRecipes() {
    }

    public static List<ArcaneWorkbenchRecipe> recipes() {
        if (LOADED.isEmpty()) {
            return Collections.unmodifiableList(FALLBACK);
        }

        return Collections.unmodifiableList(LOADED);
    }

    public static void setLoadedRecipes(List<ArcaneWorkbenchRecipe> recipes) {
        LOADED.clear();
        LOADED.addAll(recipes);
    }

    public static ArcaneWorkbenchRecipe find(ItemStack catalyst) {
        for (ArcaneWorkbenchRecipe recipe : recipes()) {
            if (recipe.catalystMatches(catalyst)) {
                return recipe;
            }
        }

        return null;
    }

    public static ArcaneWorkbenchRecipe findById(ResourceLocation id) {
        for (ArcaneWorkbenchRecipe recipe : recipes()) {
            if (recipe.id().equals(id)) {
                return recipe;
            }
        }

        return null;
    }
}
