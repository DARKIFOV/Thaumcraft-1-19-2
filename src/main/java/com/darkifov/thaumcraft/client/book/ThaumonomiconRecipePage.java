package com.darkifov.thaumcraft.client.book;

public record ThaumonomiconRecipePage(
        String title,
        String research,
        String category,
        String catalyst,
        String[] ingredients,
        String[] aspects,
        String result,
        String note
) {
}
