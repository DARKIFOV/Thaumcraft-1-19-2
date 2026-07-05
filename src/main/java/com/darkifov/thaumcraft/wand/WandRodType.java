package com.darkifov.thaumcraft.wand;

import java.util.Locale;

public enum WandRodType {
    WOOD("wood", "rod_wood", 50),
    GREATWOOD("greatwood", "rod_greatwood", 150),
    SILVERWOOD("silverwood", "rod_silverwood", 300),
    CREATIVE("creative", "rod_creative_infinity", Integer.MAX_VALUE / 8);

    private final String id;
    private final String rendererTexture;
    private final int baseCapacity;

    WandRodType(String id, String rendererTexture, int baseCapacity) {
        this.id = id;
        this.rendererTexture = rendererTexture;
        this.baseCapacity = baseCapacity;
    }

    public String id() {
        return id;
    }

    public String rendererTexture() {
        return rendererTexture;
    }

    public int baseCapacity() {
        return baseCapacity;
    }

    public static WandRodType fromId(String id) {
        if (id == null || id.isBlank()) {
            return WOOD;
        }

        for (WandRodType type : values()) {
            if (type.id.equals(id.toLowerCase(Locale.ROOT))) {
                return type;
            }
        }

        return WOOD;
    }
}
