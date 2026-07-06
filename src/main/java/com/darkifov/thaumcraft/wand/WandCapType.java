package com.darkifov.thaumcraft.wand;

import com.darkifov.thaumcraft.Aspect;

import java.util.Locale;

/**
 * TC4 strict wand caps based on thaumcraft.common.config.ConfigItems and thaumcraft.api.wands.WandCap.
 * baseCostModifier is the original TC4 vis-cost multiplier; lower is better.
 */
public enum WandCapType {
    IRON("iron", "wand_cap_iron", 1.1F, 1, "CAP_iron"),
    GOLD("gold", "wand_cap_gold", 1.0F, 3, "CAP_gold"),
    THAUMIUM("thaumium", "wand_cap_thaumium", 0.9F, 6, "CAP_thaumium"),
    COPPER("copper", "wand_cap_copper", 1.1F, 2, "CAP_copper"),
    SILVER("silver", "wand_cap_silver", 1.0F, 4, "CAP_silver"),
    VOID("void", "wand_cap_void", 0.8F, 9, "CAP_void"),
    INFINITY("infinity", "cap_infinity", 0.0F, 0, "CREATIVE_ONLY");

    private final String id;
    private final String rendererTexture;
    private final float visCostModifier;
    private final int craftCost;
    private final String researchKey;

    WandCapType(String id, String rendererTexture, float visCostModifier, int craftCost, String researchKey) {
        this.id = id;
        this.rendererTexture = rendererTexture;
        this.visCostModifier = visCostModifier;
        this.craftCost = craftCost;
        this.researchKey = researchKey;
    }

    public String id() { return id; }
    public String rendererTexture() { return rendererTexture; }
    public float visCostModifier() { return visCostModifier; }

    /**
     * TC4 caps can have a base discount and, for copper/silver, special
     * aspect-specific modifiers. The values mirror ConfigItems:
     * copper is less bad for Ordo/Perditio, silver is better for primal
     * elemental aspects.
     */
    public float visCostModifier(Aspect aspect) {
        if (this == COPPER && (aspect == Aspect.ORDO || aspect == Aspect.PERDITIO)) {
            return 1.0F;
        }
        if (this == SILVER && (aspect == Aspect.AER || aspect == Aspect.TERRA || aspect == Aspect.IGNIS || aspect == Aspect.AQUA)) {
            return 0.95F;
        }
        return visCostModifier;
    }
    public int craftCost() { return craftCost; }
    public String researchKey() { return researchKey; }

    public static WandCapType fromId(String id) {
        if (id == null || id.isBlank()) return IRON;
        for (WandCapType type : values()) {
            if (type.id.equals(id.toLowerCase(Locale.ROOT))) return type;
        }
        return IRON;
    }
}
