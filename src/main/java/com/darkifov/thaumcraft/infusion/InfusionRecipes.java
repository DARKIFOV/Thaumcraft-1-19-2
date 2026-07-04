package com.darkifov.thaumcraft.infusion;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class InfusionRecipes {
    private static final List<InfusionRecipe> FALLBACK = new ArrayList<>();
    private static final List<InfusionRecipe> LOADED = new ArrayList<>();

    static {
        FALLBACK.add(new InfusionRecipe(
                new ResourceLocation(ThaumcraftMod.MOD_ID, "fallback_infusion_core"),
                new ResourceLocation(ThaumcraftMod.MOD_ID, "balanced_shard"),
                new ResourceLocation(ThaumcraftMod.MOD_ID, "infusion_core"),
                1,
                1,
                "INFUSION"
        ).component(new ResourceLocation(ThaumcraftMod.MOD_ID, "thaumium_plate"))
                .component(new ResourceLocation(ThaumcraftMod.MOD_ID, "nitor"))
                .component(new ResourceLocation(ThaumcraftMod.MOD_ID, "alchemy_dust"))
                .require(Aspect.PRAECANTATIO, 6)
                .require(Aspect.ORDO, 4)
                .require(Aspect.AER, 2));

        FALLBACK.add(new InfusionRecipe(
                new ResourceLocation(ThaumcraftMod.MOD_ID, "fallback_void_metal_ingot"),
                new ResourceLocation(ThaumcraftMod.MOD_ID, "thaumium_ingot"),
                new ResourceLocation(ThaumcraftMod.MOD_ID, "void_metal_ingot"),
                1,
                3,
                "INFUSION"
        ).component(new ResourceLocation(ThaumcraftMod.MOD_ID, "flux_crystal"))
                .component(new ResourceLocation(ThaumcraftMod.MOD_ID, "perditio_shard"))
                .component(new ResourceLocation(ThaumcraftMod.MOD_ID, "alchemy_dust"))
                .require(Aspect.PERDITIO, 6)
                .require(Aspect.VACUOS, 4)
                .require(Aspect.PRAECANTATIO, 4));
    }

    private InfusionRecipes() {
    }

    public static List<InfusionRecipe> recipes() {
        if (LOADED.isEmpty()) {
            return Collections.unmodifiableList(FALLBACK);
        }

        return Collections.unmodifiableList(LOADED);
    }

    public static void setLoadedRecipes(List<InfusionRecipe> recipes) {
        LOADED.clear();
        LOADED.addAll(recipes);
    }

    public static InfusionRecipe find(ItemStack catalyst) {
        for (InfusionRecipe recipe : recipes()) {
            if (recipe.catalystMatches(catalyst)) {
                return recipe;
            }
        }

        return null;
    }

    public static InfusionRecipe findById(ResourceLocation id) {
        for (InfusionRecipe recipe : recipes()) {
            if (recipe.id().equals(id)) {
                return recipe;
            }
        }

        return null;
    }
}
