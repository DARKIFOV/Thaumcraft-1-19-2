package com.darkifov.thaumcraft.golem;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

/**
 * Strict TC4 itemGolemUpgrade metadata parity.
 * Original order: 0 AIR, 1 EARTH, 2 FIRE, 3 WATER, 4 ORDER, 5 ENTROPY.
 */
public enum GolemUpgradeType {
    AIR("air", 0, ChatFormatting.AQUA),
    EARTH("earth", 1, ChatFormatting.GREEN),
    FIRE("fire", 2, ChatFormatting.RED),
    WATER("water", 3, ChatFormatting.BLUE),
    ORDER("order", 4, ChatFormatting.WHITE),
    ENTROPY("entropy", 5, ChatFormatting.DARK_PURPLE);

    private final String id;
    private final byte originalId;
    private final ChatFormatting color;

    GolemUpgradeType(String id, int originalId, ChatFormatting color) {
        this.id = id;
        this.originalId = (byte) originalId;
        this.color = color;
    }

    public String id() {
        return id;
    }

    public byte originalId() {
        return originalId;
    }

    public Component displayName() {
        return Component.literal(id.substring(0, 1).toUpperCase() + id.substring(1)).withStyle(color);
    }

    public static GolemUpgradeType byOriginalId(int id) {
        for (GolemUpgradeType type : values()) {
            if (type.originalId == (byte) id) {
                return type;
            }
        }
        return null;
    }

    public static GolemUpgradeType byName(String id) {
        if (id != null) {
            for (GolemUpgradeType type : values()) {
                if (type.id.equalsIgnoreCase(id) || type.name().equalsIgnoreCase(id)) {
                    return type;
                }
                try {
                    if (Integer.parseInt(id) == type.originalId) {
                        return type;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return AIR;
    }
}
