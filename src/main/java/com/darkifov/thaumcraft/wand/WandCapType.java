package com.darkifov.thaumcraft.wand;

import java.util.Locale;

public enum WandCapType {
    IRON("iron", "cap_iron", 1.0F),
    GOLD("gold", "cap_gold", 0.9F),
    THAUMIUM("thaumium", "cap_thaumium", 0.8F),
    VOID("void", "cap_void", 0.7F),
    INFINITY("infinity", "cap_infinity", 0.0F);

    private final String id;
    private final String rendererTexture;
    private final float visCostModifier;

    WandCapType(String id, String rendererTexture, float visCostModifier) {
        this.id = id;
        this.rendererTexture = rendererTexture;
        this.visCostModifier = visCostModifier;
    }

    public String id() {
        return id;
    }

    public String rendererTexture() {
        return rendererTexture;
    }

    public float visCostModifier() {
        return visCostModifier;
    }

    public static WandCapType fromId(String id) {
        if (id == null || id.isBlank()) {
            return IRON;
        }

        for (WandCapType type : values()) {
            if (type.id.equals(id.toLowerCase(Locale.ROOT))) {
                return type;
            }
        }

        return IRON;
    }
}
