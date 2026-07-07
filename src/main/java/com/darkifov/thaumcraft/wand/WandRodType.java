package com.darkifov.thaumcraft.wand;

import com.darkifov.thaumcraft.Aspect;

import java.util.Locale;

/**
 * TC4 strict wand/staff rods based on thaumcraft.common.config.ConfigItems and thaumcraft.api.wands.WandRod/StaffRod.
 */
public enum WandRodType {
    WOOD("wood", "wand_rod_wood", 25, 1, "ROD_wood", false, false),
    GREATWOOD("greatwood", "wand_rod_greatwood", 50, 3, "ROD_greatwood", false, false),
    OBSIDIAN("obsidian", "wand_rod_obsidian", 75, 6, "ROD_obsidian", false, false),
    BLAZE("blaze", "wand_rod_blaze", 75, 6, "ROD_blaze", true, false),
    ICE("ice", "wand_rod_ice", 75, 6, "ROD_ice", false, false),
    QUARTZ("quartz", "wand_rod_quartz", 75, 6, "ROD_quartz", false, false),
    BONE("bone", "wand_rod_bone", 75, 6, "ROD_bone", false, false),
    REED("reed", "wand_rod_reed", 75, 6, "ROD_reed", false, false),
    SILVERWOOD("silverwood", "wand_rod_silverwood", 100, 9, "ROD_silverwood", false, false),
    GREATWOOD_STAFF("greatwood_staff", "wand_rod_greatwood", 125, 8, "ROD_greatwood_staff", false, true),
    OBSIDIAN_STAFF("obsidian_staff", "wand_rod_obsidian", 175, 14, "ROD_obsidian_staff", false, true),
    BLAZE_STAFF("blaze_staff", "wand_rod_blaze", 175, 14, "ROD_blaze_staff", true, true),
    ICE_STAFF("ice_staff", "wand_rod_ice", 175, 14, "ROD_ice_staff", false, true),
    QUARTZ_STAFF("quartz_staff", "wand_rod_quartz", 175, 14, "ROD_quartz_staff", false, true),
    BONE_STAFF("bone_staff", "wand_rod_bone", 175, 14, "ROD_bone_staff", false, true),
    REED_STAFF("reed_staff", "wand_rod_reed", 175, 14, "ROD_reed_staff", false, true),
    SILVERWOOD_STAFF("silverwood_staff", "wand_rod_silverwood", 250, 24, "ROD_silverwood_staff", false, true),
    PRIMAL_STAFF("primal_staff", "wand_rod_primal", 250, 32, "ROD_primal_staff", true, true),
    CREATIVE("creative", "rod_creative_infinity", Integer.MAX_VALUE / 8, 0, "CREATIVE_ONLY", true, false);

    private final String id;
    private final String rendererTexture;
    private final int baseCapacity;
    private final int craftCost;
    private final String researchKey;
    private final boolean glowing;
    private final boolean staff;

    WandRodType(String id, String rendererTexture, int baseCapacity, int craftCost, String researchKey, boolean glowing, boolean staff) {
        this.id = id;
        this.rendererTexture = rendererTexture;
        this.baseCapacity = baseCapacity;
        this.craftCost = craftCost;
        this.researchKey = researchKey;
        this.glowing = glowing;
        this.staff = staff;
    }

    public String id() { return id; }
    public String rendererTexture() { return rendererTexture; }
    public int baseCapacity() { return baseCapacity; }
    public int craftCost() { return craftCost; }
    public String researchKey() { return researchKey; }
    public boolean glowing() { return glowing; }
    public boolean staff() { return staff; }

    /**
     * Original TC4 WandRodPrimalOnUpdate behavior: elemental rods slowly
     * regenerate one matching primal aspect up to 10% capacity. The primal
     * staff rod can regenerate any primal aspect up to that same threshold.
     */
    public Aspect regeneratedAspect() {
        return switch (this) {
            case OBSIDIAN, OBSIDIAN_STAFF -> Aspect.TERRA;
            case BLAZE, BLAZE_STAFF -> Aspect.IGNIS;
            case ICE, ICE_STAFF -> Aspect.AQUA;
            case QUARTZ, QUARTZ_STAFF -> Aspect.ORDO;
            case BONE, BONE_STAFF -> Aspect.PERDITIO;
            case REED, REED_STAFF -> Aspect.AER;
            default -> null;
        };
    }

    public boolean regeneratesAllPrimals() {
        return this == PRIMAL_STAFF;
    }

    public boolean hasRodRegen() {
        return regeneratedAspect() != null || regeneratesAllPrimals();
    }


    /** Stage187: original TC4 WandRod#getTag strings are the root NBT values. */
    public String originalTag() {
        return id;
    }

    public static WandRodType fromOriginalTag(String tag) {
        if (tag == null || tag.isBlank()) return WOOD;
        String normalized = tag.toLowerCase(Locale.ROOT);
        for (WandRodType type : values()) {
            if (type.originalTag().equals(normalized) || type.id.equals(normalized)) return type;
        }
        return fromId(tag);
    }

    public static WandRodType fromId(String id) {
        if (id == null || id.isBlank()) return WOOD;
        for (WandRodType type : values()) {
            if (type.id.equals(id.toLowerCase(Locale.ROOT))) return type;
        }
        return WOOD;
    }
}
