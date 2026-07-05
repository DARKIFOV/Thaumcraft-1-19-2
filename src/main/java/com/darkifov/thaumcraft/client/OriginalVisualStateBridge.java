package com.darkifov.thaumcraft.client;

import net.minecraft.resources.ResourceLocation;

import java.util.Locale;

public final class OriginalVisualStateBridge {
    private OriginalVisualStateBridge() {
    }

    public static ResourceLocation jarModel(String aspect) {
        return new ResourceLocation("thaumcraft", "block/essentia_jar_" + normalize(aspect));
    }

    public static ResourceLocation nodeModel(String aspect) {
        return new ResourceLocation("thaumcraft", "block/aura_node_" + normalize(aspect));
    }

    public static ResourceLocation researchNoteModel(String state) {
        return new ResourceLocation("thaumcraft", "item/research_note_" + normalize(state));
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return "aer";
        }

        return value.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9_]", "");
    }
}
