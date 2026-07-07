package com.darkifov.thaumcraft.golem;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

/**
 * Strict TC4 1.7.10 EnumGolemType parity table.
 *
 * Source of truth: thaumcraft.common.entities.golems.EnumGolemType
 * STRAW(10,0,0.38,false,1,1,75,0), WOOD(20,6,0.35,false,1,4,75,1),
 * TALLOW(20,9,0.33,false,2,8,75,2), CLAY(25,9,0.33,true,1,8,100,2),
 * FLESH(15,6,0.35,false,2,4,40,1), STONE(30,12,0.32,true,1,16,100,3),
 * IRON(35,15,0.31,true,1,32,125,4), THAUMIUM(40,15,0.32,true,2,32,100,4).
 */
public enum GolemMaterial {
    STRAW("straw", 10, 0, 0.38F, false, 1, 1, 75, 0, ChatFormatting.YELLOW),
    WOOD("wood", 20, 6, 0.35F, false, 1, 4, 75, 1, ChatFormatting.GOLD),
    TALLOW("tallow", 20, 9, 0.33F, false, 2, 8, 75, 2, ChatFormatting.WHITE),
    CLAY("clay", 25, 9, 0.33F, true, 1, 8, 100, 2, ChatFormatting.GRAY),
    FLESH("flesh", 15, 6, 0.35F, false, 2, 4, 40, 1, ChatFormatting.LIGHT_PURPLE),
    STONE("stone", 30, 12, 0.32F, true, 1, 16, 100, 3, ChatFormatting.DARK_GRAY),
    IRON("iron", 35, 15, 0.31F, true, 1, 32, 125, 4, ChatFormatting.AQUA),
    THAUMIUM("thaumium", 40, 15, 0.32F, true, 2, 32, 100, 4, ChatFormatting.DARK_PURPLE);

    private final String id;
    private final int health;
    private final int armor;
    private final float speed;
    private final boolean fireResist;
    private final int upgradeSlots;
    private final int carry;
    private final int regenDelay;
    private final int strength;
    private final ChatFormatting color;

    GolemMaterial(String id, int health, int armor, float speed, boolean fireResist, int upgradeSlots,
                  int carry, int regenDelay, int strength, ChatFormatting color) {
        this.id = id;
        this.health = health;
        this.armor = armor;
        this.speed = speed;
        this.fireResist = fireResist;
        this.upgradeSlots = upgradeSlots;
        this.carry = carry;
        this.regenDelay = regenDelay;
        this.strength = strength;
        this.color = color;
    }

    public String id() {
        return id;
    }

    public int health() {
        return health;
    }

    public double maxHealth() {
        return health;
    }

    public int armorValue() {
        return armor;
    }

    public double armor() {
        return armor;
    }

    public float speed() {
        return speed;
    }

    public double movementSpeed() {
        return speed;
    }

    public boolean fireResist() {
        return fireResist;
    }

    public int upgradeSlots(boolean advanced) {
        return upgradeSlots + (advanced ? 1 : 0);
    }

    public int baseCarry() {
        return carry;
    }

    public int regenDelay() {
        return regenDelay;
    }

    public int strength() {
        return strength;
    }

    /** TC4 carry capacity after earth-upgrade metadata 1. */
    public int carryLimit(int earthUpgradeAmount) {
        int base = carry;
        base += Math.min(16, Math.max(4, base)) * Math.max(0, earthUpgradeAmount);
        return base;
    }

    /** Forge adapter name kept for older stage code. TC4 does not size inventory by body material directly. */
    public int inventorySlots() {
        return carry;
    }

    public Component displayName() {
        return Component.literal(id.substring(0, 1).toUpperCase() + id.substring(1)).withStyle(color);
    }

    public static GolemMaterial byName(String id) {
        if (id != null) {
            for (GolemMaterial material : values()) {
                if (material.id.equalsIgnoreCase(id) || material.name().equalsIgnoreCase(id)) {
                    return material;
                }
            }
        }
        return WOOD;
    }

    public GolemMaterial next() {
        GolemMaterial[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}
