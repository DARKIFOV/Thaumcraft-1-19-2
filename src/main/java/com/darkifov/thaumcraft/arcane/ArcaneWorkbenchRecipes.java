package com.darkifov.thaumcraft.arcane;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.wand.WandCraftingRuntime;
import com.darkifov.thaumcraft.wand.TC4ConfigRecipesWandIndex;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ArcaneWorkbenchRecipes {
    private static final List<ArcaneWorkbenchRecipe> FALLBACK = new ArrayList<>();
    private static final List<ArcaneWorkbenchRecipe> LOADED = new ArrayList<>();
    private static final List<ArcaneWorkbenchRecipe> STRICT_ORIGINAL = new ArrayList<>();
    private static final List<ArcaneWorkbenchRecipe> NON_ORIGINAL = new ArrayList<>();
    private static final List<ArcaneWorkbenchRecipe> WITH_WAND_ASSEMBLY = new ArrayList<>();

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
        if (!STRICT_ORIGINAL.isEmpty()) {
            return Collections.unmodifiableList(withWandAssembly(STRICT_ORIGINAL));
        }

        if (!LOADED.isEmpty()) {
            return Collections.unmodifiableList(withWandAssembly(LOADED));
        }

        return Collections.unmodifiableList(withWandAssembly(FALLBACK));
    }

    private static List<ArcaneWorkbenchRecipe> withWandAssembly(List<ArcaneWorkbenchRecipe> base) {
        // The data pack still contains a few historical materialized copies of
        // generated wand-component recipes. Merge by original TC4 key so the
        // authoritative runtime recipe replaces the stale copy instead of JEI
        // showing two recipes with different costs or ingredients.
        Map<String, ArcaneWorkbenchRecipe> merged = new LinkedHashMap<>();
        mergeRecipes(merged, base);
        mergeRecipes(merged, TC4ConfigRecipesWandIndex.generatedArcaneComponentRecipes());
        mergeRecipes(merged, WandCraftingRuntime.generatedWandAssemblyRecipes());

        WITH_WAND_ASSEMBLY.clear();
        WITH_WAND_ASSEMBLY.addAll(merged.values());
        return WITH_WAND_ASSEMBLY;
    }

    private static void mergeRecipes(Map<String, ArcaneWorkbenchRecipe> merged, List<ArcaneWorkbenchRecipe> recipes) {
        for (ArcaneWorkbenchRecipe recipe : recipes) {
            String key = recipe.tc4Key().isBlank() ? "id:" + recipe.id() : "tc4:" + recipe.tc4Key();
            merged.put(key, recipe);
        }
    }

    public static List<ArcaneWorkbenchRecipe> nonOriginalRecipes() {
        return Collections.unmodifiableList(NON_ORIGINAL);
    }

    public static void setLoadedRecipes(List<ArcaneWorkbenchRecipe> recipes) {
        LOADED.clear();
        STRICT_ORIGINAL.clear();
        NON_ORIGINAL.clear();
        LOADED.addAll(recipes);
        for (ArcaneWorkbenchRecipe recipe : recipes) {
            if (!recipe.tc4Key().isBlank()) {
                STRICT_ORIGINAL.add(recipe);
            } else {
                NON_ORIGINAL.add(recipe);
            }
        }
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
