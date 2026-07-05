package com.darkifov.thaumcraft.aura;

import java.util.Locale;

public enum AuraNodeType {
    NORMAL(0xBFA6FF, "Normal"),
    PURE(0x9EEBFF, "Pure"),
    DARK(0x5F3E8A, "Dark"),
    UNSTABLE(0xFFB84A, "Unstable"),
    TAINTED(0x8F38B8, "Tainted"),
    HUNGRY(0x4F2A6E, "Hungry");

    private final int color;
    private final String displayName;

    AuraNodeType(int color, String displayName) {
        this.color = color;
        this.displayName = displayName;
    }

    public int color() {
        return color;
    }

    public String displayName() {
        return displayName;
    }

    public static AuraNodeType fromName(String value) {
        if (value == null || value.isBlank()) {
            return NORMAL;
        }

        try {
            return AuraNodeType.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return NORMAL;
        }
    }
}
