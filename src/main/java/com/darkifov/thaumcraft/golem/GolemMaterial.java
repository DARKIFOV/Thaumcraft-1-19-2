package com.darkifov.thaumcraft.golem;

import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

/**
 * TC4-style golem body materials. Original TC4 used itemGolemPlacer metadata;
 * the 1.19.2 port stores the selected material on the placer/core NBT and then
 * copies it to the entity.
 */
public enum GolemMaterial {
    STRAW("straw", 12.0D, 0.30D, 0.0D, 9, ChatFormatting.YELLOW),
    WOOD("wood", 20.0D, 0.25D, 1.0D, 12, ChatFormatting.GOLD),
    TALLOW("tallow", 16.0D, 0.27D, 0.0D, 12, ChatFormatting.WHITE),
    CLAY("clay", 24.0D, 0.22D, 2.0D, 15, ChatFormatting.GRAY),
    FLESH("flesh", 28.0D, 0.24D, 1.5D, 15, ChatFormatting.LIGHT_PURPLE),
    STONE("stone", 34.0D, 0.19D, 5.0D, 18, ChatFormatting.DARK_GRAY),
    IRON("iron", 42.0D, 0.17D, 8.0D, 21, ChatFormatting.AQUA),
    THAUMIUM("thaumium", 48.0D, 0.21D, 6.0D, 27, ChatFormatting.DARK_PURPLE);

    private final String id;
    private final double maxHealth;
    private final double movementSpeed;
    private final double armor;
    private final int inventorySlots;
    private final ChatFormatting color;

    GolemMaterial(String id, double maxHealth, double movementSpeed, double armor, int inventorySlots, ChatFormatting color) {
        this.id = id;
        this.maxHealth = maxHealth;
        this.movementSpeed = movementSpeed;
        this.armor = armor;
        this.inventorySlots = inventorySlots;
        this.color = color;
    }

    public String id() {
        return id;
    }

    public double maxHealth() {
        return maxHealth;
    }

    public double movementSpeed() {
        return movementSpeed;
    }

    public double armor() {
        return armor;
    }

    public int inventorySlots() {
        return inventorySlots;
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
