package com.darkifov.thaumcraft.aura;

import java.util.Locale;

public enum AuraNodeModifier {
    NORMAL(1.0F, 1.0F, 1.0F, "Normal"),
    BRIGHT(1.20F, 1.20F, 1.25F, "Bright"),
    PALE(0.75F, 0.80F, 0.75F, "Pale"),
    FADING(0.55F, 0.50F, 0.35F, "Fading");

    private final float sizeScale;
    private final float capacityScale;
    private final float regenScale;
    private final String displayName;

    AuraNodeModifier(float sizeScale, float capacityScale, float regenScale, String displayName) {
        this.sizeScale = sizeScale;
        this.capacityScale = capacityScale;
        this.regenScale = regenScale;
        this.displayName = displayName;
    }

    public float sizeScale() {
        return sizeScale;
    }

    public float capacityScale() {
        return capacityScale;
    }

    public float regenScale() {
        return regenScale;
    }

    public String displayName() {
        return displayName;
    }

    public static AuraNodeModifier fromName(String value) {
        if (value == null || value.isBlank()) {
            return NORMAL;
        }

        try {
            return AuraNodeModifier.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return NORMAL;
        }
    }
}
