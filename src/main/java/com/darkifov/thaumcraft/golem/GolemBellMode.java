package com.darkifov.thaumcraft.golem;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

/** Bell modes mirroring the old TC4 bell workflow: assign home/marker, recall,
 * toggle waiting, and retask/filter owned golems. */
public enum GolemBellMode {
    HOME("home", ChatFormatting.GOLD),
    MARKER("marker", ChatFormatting.YELLOW),
    RECALL("recall", ChatFormatting.AQUA),
    WAIT("wait", ChatFormatting.GRAY),
    RETASK("retask", ChatFormatting.LIGHT_PURPLE),
    STATUS("status", ChatFormatting.GREEN);

    private final String id;
    private final ChatFormatting color;

    GolemBellMode(String id, ChatFormatting color) {
        this.id = id;
        this.color = color;
    }

    public String id() {
        return id;
    }

    public Component displayName() {
        return Component.literal(id.substring(0, 1).toUpperCase() + id.substring(1)).withStyle(color);
    }

    public static GolemBellMode byName(String id) {
        if (id != null) {
            for (GolemBellMode mode : values()) {
                if (mode.id.equalsIgnoreCase(id) || mode.name().equalsIgnoreCase(id)) {
                    return mode;
                }
            }
        }
        return HOME;
    }

    public GolemBellMode next() {
        GolemBellMode[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}
