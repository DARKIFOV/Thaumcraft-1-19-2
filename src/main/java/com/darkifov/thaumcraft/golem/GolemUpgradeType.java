package com.darkifov.thaumcraft.golem;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

/**
 * TC4-style golem upgrades. The original 1.7.10 implementation stored these as
 * metadata on itemGolemUpgrade. The 1.19.2 port keeps them explicit and copies
 * them into the golem entity NBT.
 */
public enum GolemUpgradeType {
    AIR("air", ChatFormatting.AQUA),
    FIRE("fire", ChatFormatting.RED),
    WATER("water", ChatFormatting.BLUE),
    EARTH("earth", ChatFormatting.GREEN),
    ORDER("order", ChatFormatting.WHITE),
    ENTROPY("entropy", ChatFormatting.DARK_PURPLE);

    private final String id;
    private final ChatFormatting color;

    GolemUpgradeType(String id, ChatFormatting color) {
        this.id = id;
        this.color = color;
    }

    public String id() {
        return id;
    }

    public Component displayName() {
        return Component.literal(id.substring(0, 1).toUpperCase() + id.substring(1)).withStyle(color);
    }

    public static GolemUpgradeType byName(String id) {
        if (id != null) {
            for (GolemUpgradeType type : values()) {
                if (type.id.equalsIgnoreCase(id) || type.name().equalsIgnoreCase(id)) {
                    return type;
                }
            }
        }
        return AIR;
    }
}
