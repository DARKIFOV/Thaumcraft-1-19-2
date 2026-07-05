package com.darkifov.thaumcraft.client.arcane;

public record ClientArcaneRecipePage(
        String id,
        String title,
        String research,
        String catalyst,
        String catalystId,
        String[] ingredients,
        String[] ingredientIds,
        String result,
        String resultId,
        String visCost,
        String note
) {
}
