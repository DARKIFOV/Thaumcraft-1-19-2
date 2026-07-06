package com.darkifov.thaumcraft.golem;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

/**
 * TC4-style golem cores. TC4 1.7.10 used itemGolemCore metadata; the port uses
 * explicit modes while keeping the original core names.
 */
public enum GolemCoreType {
    GATHER("gather", ChatFormatting.GREEN),
    FILL("fill", ChatFormatting.AQUA),
    EMPTY("empty", ChatFormatting.BLUE),
    GUARD("guard", ChatFormatting.RED),
    HARVEST("harvest", ChatFormatting.YELLOW),
    LUMBER("lumber", ChatFormatting.GOLD),
    USE("use", ChatFormatting.LIGHT_PURPLE),
    SORTING("sorting", ChatFormatting.DARK_AQUA),
    BODYGUARD("bodyguard", ChatFormatting.DARK_RED),
    BUTCHER("butcher", ChatFormatting.RED),
    FISH("fish", ChatFormatting.BLUE),
    LIQUID("liquid", ChatFormatting.AQUA),
    ESSENTIA("essentia", ChatFormatting.DARK_PURPLE),
    PATROL("patrol", ChatFormatting.GRAY);

    private final String id;
    private final ChatFormatting color;

    GolemCoreType(String id, ChatFormatting color) {
        this.id = id;
        this.color = color;
    }

    public String id() {
        return id;
    }

    public Component displayName() {
        return Component.literal(id.substring(0, 1).toUpperCase() + id.substring(1)).withStyle(color);
    }

    public static GolemCoreType byName(String id) {
        if (id != null) {
            for (GolemCoreType type : values()) {
                if (type.id.equalsIgnoreCase(id) || type.name().equalsIgnoreCase(id)) {
                    return type;
                }
            }
        }
        return GATHER;
    }

    public GolemCoreType next() {
        GolemCoreType[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}
