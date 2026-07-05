package com.darkifov.thaumcraft.client.arcane;

import com.darkifov.thaumcraft.arcane.ArcaneRecipeBook;
import com.darkifov.thaumcraft.arcane.ArcaneRecipeBookEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ClientArcaneRecipeRegistry {
    private ClientArcaneRecipeRegistry() {
    }

    public static boolean usingSyncedJson() {
        return ClientSyncedArcaneRecipes.synced() && ClientSyncedArcaneRecipes.size() > 0;
    }

    public static int size() {
        if (usingSyncedJson()) {
            return ClientSyncedArcaneRecipes.size();
        }

        return ArcaneRecipeBook.size();
    }

    public static ClientArcaneRecipePage get(int index) {
        if (usingSyncedJson()) {
            return ClientSyncedArcaneRecipes.get(index);
        }

        ArcaneRecipeBookEntry entry = ArcaneRecipeBook.get(index);

        if (entry == null) {
            return null;
        }

        return fromFallback(entry);
    }

    public static List<ClientArcaneRecipePage> pages() {
        if (usingSyncedJson()) {
            return ClientSyncedArcaneRecipes.recipes();
        }

        List<ClientArcaneRecipePage> pages = new ArrayList<>();

        for (ArcaneRecipeBookEntry entry : ArcaneRecipeBook.entries()) {
            pages.add(fromFallback(entry));
        }

        return Collections.unmodifiableList(pages);
    }

    private static ClientArcaneRecipePage fromFallback(ArcaneRecipeBookEntry entry) {
        return new ClientArcaneRecipePage(
                entry.id().toString(),
                entry.title(),
                entry.research(),
                entry.catalyst(),
                guessId(entry.catalyst()),
                entry.ingredients(),
                guessIds(entry.ingredients()),
                entry.result(),
                guessId(entry.result()),
                entry.visCost(),
                entry.note()
        );
    }

    private static String[] guessIds(String[] names) {
        String[] ids = new String[names.length];

        for (int i = 0; i < names.length; i++) {
            ids[i] = guessId(names[i]);
        }

        return ids;
    }

    private static String guessId(String display) {
        String clean = display.toLowerCase()
                .replace(" x2", "")
                .replace(" x3", "")
                .replace(" x4", "")
                .replace(" x5", "")
                .replace(" ", "_");

        if (clean.contains("gold_ingot")) return "minecraft:gold_ingot";
        if (clean.contains("iron_ingot")) return "minecraft:iron_ingot";
        if (clean.contains("glass_pane")) return "minecraft:glass_pane";
        if (clean.contains("leather_helmet")) return "minecraft:leather_helmet";
        if (clean.contains("pumpkin")) return "minecraft:pumpkin";

        if (!clean.contains(":")) {
            clean = "thaumcraft:" + clean;
        }

        return clean;
    }
}
