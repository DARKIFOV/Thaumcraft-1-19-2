package com.darkifov.thaumcraft.golem;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

/** Marker roles used by the 1.19.2 golem runtime. */
public enum GolemMarkerMode {
    HOME("home", ChatFormatting.GOLD),
    INPUT("input", ChatFormatting.GREEN),
    OUTPUT("output", ChatFormatting.BLUE),
    GUARD("guard", ChatFormatting.RED),
    WORK("work", ChatFormatting.YELLOW);

    private final String id;
    private final ChatFormatting color;

    GolemMarkerMode(String id, ChatFormatting color) {
        this.id = id;
        this.color = color;
    }

    public String id() {
        return id;
    }

    public Component displayName() {
        return Component.literal(id.substring(0, 1).toUpperCase() + id.substring(1)).withStyle(color);
    }

    public static GolemMarkerMode byName(String id) {
        if (id != null) {
            for (GolemMarkerMode mode : values()) {
                if (mode.id.equalsIgnoreCase(id) || mode.name().equalsIgnoreCase(id)) {
                    return mode;
                }
            }
        }
        return HOME;
    }

    public GolemMarkerMode next() {
        GolemMarkerMode[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}
