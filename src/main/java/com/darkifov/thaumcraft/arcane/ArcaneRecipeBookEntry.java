package com.darkifov.thaumcraft.arcane;

import net.minecraft.resources.ResourceLocation;

public record ArcaneRecipeBookEntry(
        ResourceLocation id,
        String title,
        String research,
        String catalyst,
        String[] ingredients,
        String result,
        String visCost,
        String note
) {
}
