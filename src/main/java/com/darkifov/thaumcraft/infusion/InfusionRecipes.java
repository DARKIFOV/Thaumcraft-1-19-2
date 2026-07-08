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
    private static final List<InfusionRecipe> STRICT_ORIGINAL = new ArrayList<>();
    private static final List<InfusionRecipe> NON_ORIGINAL = new ArrayList<>();
    private static final List<InfusionRecipe> ENCHANTMENT_RUNTIME = new ArrayList<>();
    private static final List<InfusionRecipe> RUNIC_RUNTIME = new ArrayList<>();

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
        List<InfusionRecipe> base;
        if (!STRICT_ORIGINAL.isEmpty()) {
            base = STRICT_ORIGINAL;
        } else if (!LOADED.isEmpty()) {
            base = LOADED;
        } else {
            base = FALLBACK;
        }

        List<InfusionRecipe> combined = new ArrayList<>(base);
        combined.addAll(enchantmentRuntimeRecipes());
        combined.addAll(runicRuntimeRecipes());
        return Collections.unmodifiableList(combined);
    }

    private static List<InfusionRecipe> enchantmentRuntimeRecipes() {
        if (ENCHANTMENT_RUNTIME.isEmpty()) {
            ENCHANTMENT_RUNTIME.addAll(TC4InfusionEnchantmentAdapter.materializeRecipes());
        }
        return ENCHANTMENT_RUNTIME;
    }

    private static List<InfusionRecipe> runicRuntimeRecipes() {
        if (RUNIC_RUNTIME.isEmpty()) {
            RUNIC_RUNTIME.add(TC4InfusionRunicAugmentAdapter.materializeRecipe());
        }
        return RUNIC_RUNTIME;
    }

    public static List<InfusionRecipe> nonOriginalRecipes() {
        return Collections.unmodifiableList(NON_ORIGINAL);
    }

    public static void setLoadedRecipes(List<InfusionRecipe> recipes) {
        LOADED.clear();
        STRICT_ORIGINAL.clear();
        NON_ORIGINAL.clear();
        LOADED.addAll(recipes);
        for (InfusionRecipe recipe : recipes) {
            if (!recipe.tc4Key().isBlank()) {
                STRICT_ORIGINAL.add(recipe);
            } else {
                NON_ORIGINAL.add(recipe);
            }
        }
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
