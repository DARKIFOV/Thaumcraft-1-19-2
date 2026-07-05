package com.darkifov.thaumcraft.client.arcane;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ClientSyncedArcaneRecipes {
    private static final List<ClientArcaneRecipePage> RECIPES = new ArrayList<>();
    private static boolean synced = false;

    private ClientSyncedArcaneRecipes() {
    }

    public static void set(List<ClientArcaneRecipePage> recipes) {
        RECIPES.clear();
        RECIPES.addAll(recipes);
        synced = true;
    }

    public static boolean synced() {
        return synced;
    }

    public static int size() {
        return RECIPES.size();
    }

    public static ClientArcaneRecipePage get(int index) {
        if (RECIPES.isEmpty()) {
            return null;
        }

        int safe = Math.max(0, Math.min(index, RECIPES.size() - 1));
        return RECIPES.get(safe);
    }

    public static List<ClientArcaneRecipePage> recipes() {
        return Collections.unmodifiableList(RECIPES);
    }

    public static ClientArcaneRecipePage byId(ResourceLocation id) {
        for (ClientArcaneRecipePage page : RECIPES) {
            if (new ResourceLocation(page.id()).equals(id)) {
                return page;
            }
        }

        return null;
    }
}
