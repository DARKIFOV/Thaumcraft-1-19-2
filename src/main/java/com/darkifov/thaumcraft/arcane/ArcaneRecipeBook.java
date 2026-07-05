package com.darkifov.thaumcraft.arcane;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ArcaneRecipeBook {
    private static final List<ArcaneRecipeBookEntry> ENTRIES = new ArrayList<>();

    static {
        add("thaumcraft:thaumometer_original_style",
                "Thaumometer",
                "FIRST_STEPS",
                "Glass Pane",
                new String[]{"Gold Ingot", "Gold Ingot", "Aer Shard", "Terra Shard"},
                "Thaumometer",
                "Ordo 2",
                "A thaumometer reveals aspects and can scan aura nodes.");

        add("thaumcraft:goggles_of_revealing_original_style",
                "Goggles of Revealing",
                "PRIMAL_ASPECTS",
                "Leather Helmet",
                new String[]{"Thaumometer", "Gold Ingot", "Gold Ingot", "Ordo Shard", "Aer Shard"},
                "Goggles of Revealing",
                "Ordo 2",
                "Useful goggles for seeing magical details.");

        add("thaumcraft:greatwood_wand_original_tc4_style",
                "Greatwood Wand",
                "ARCANE_WORKBENCH",
                "Greatwood Wand Core",
                new String[]{"Gold Wand Cap", "Gold Wand Cap"},
                "Greatwood Wand",
                "Ordo 2",
                "Uses proper wand cap and core progression.");

        add("thaumcraft:silverwood_wand_original_tc4_style",
                "Silverwood Wand",
                "ARCANE_WORKBENCH",
                "Silverwood Wand Core",
                new String[]{"Thaumium Wand Cap", "Thaumium Wand Cap"},
                "Silverwood Wand",
                "Ordo 2",
                "A stronger wand body for later progression.");

        add("thaumcraft:thaumium_plate_original_style",
                "Thaumium Plate",
                "THAUMIUM",
                "Thaumium Ingot",
                new String[]{"Thaumium Ingot", "Thaumium Ingot"},
                "Thaumium Plate",
                "Ordo 2",
                "A common thaumic component.");

        add("thaumcraft:golem_core_original_style",
                "Golem Core",
                "GOLEMS",
                "Infusion Core",
                new String[]{"Thaumium Ingot", "Balanced Shard", "Ordo Shard", "Pumpkin"},
                "Golem Core",
                "Ordo 2",
                "Requires golemancy research.");
    }

    private ArcaneRecipeBook() {
    }

    private static void add(String id, String title, String research, String catalyst, String[] ingredients, String result, String visCost, String note) {
        ENTRIES.add(new ArcaneRecipeBookEntry(new ResourceLocation(id), title, research, catalyst, ingredients, result, visCost, note));
    }

    public static List<ArcaneRecipeBookEntry> entries() {
        return Collections.unmodifiableList(ENTRIES);
    }

    public static int size() {
        return ENTRIES.size();
    }

    public static ArcaneRecipeBookEntry get(int index) {
        if (ENTRIES.isEmpty()) {
            return null;
        }

        return ENTRIES.get(Math.max(0, Math.min(index, ENTRIES.size() - 1)));
    }

    public static ArcaneRecipeBookEntry byId(ResourceLocation id) {
        for (ArcaneRecipeBookEntry entry : ENTRIES) {
            if (entry.id().equals(id)) {
                return entry;
            }
        }

        return null;
    }
}
