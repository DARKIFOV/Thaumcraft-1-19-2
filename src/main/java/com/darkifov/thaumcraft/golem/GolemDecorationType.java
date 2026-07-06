package com.darkifov.thaumcraft.golem;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

/**
 * TC4 golem decoration metadata split into explicit 1.19.2 items.
 * Original TC4 stored these as itemGolemDecoration metadata variants.
 */
public enum GolemDecorationType {
    ARMOR("armor", ChatFormatting.GRAY),
    TOP_HAT("tophat", ChatFormatting.BLACK),
    FEZ("fez", ChatFormatting.RED),
    VISOR("visor", ChatFormatting.AQUA),
    GLASSES("glasses", ChatFormatting.YELLOW),
    BOWTIE("bowtie", ChatFormatting.LIGHT_PURPLE),
    DART_LAUNCHER("dart", ChatFormatting.DARK_GREEN),
    MACE("mace", ChatFormatting.DARK_RED),
    WIRELESS_BACKPACK("wireless_backpack", ChatFormatting.GOLD);

    private final String id;
    private final ChatFormatting color;

    GolemDecorationType(String id, ChatFormatting color) {
        this.id = id;
        this.color = color;
    }

    public String id() {
        return id;
    }

    public Component displayName() {
        String text = id.replace('_', ' ');
        return Component.literal(text.substring(0, 1).toUpperCase() + text.substring(1)).withStyle(color);
    }

    public static GolemDecorationType byName(String id) {
        if (id != null) {
            for (GolemDecorationType type : values()) {
                if (type.id.equalsIgnoreCase(id) || type.name().equalsIgnoreCase(id)) {
                    return type;
                }
            }
        }
        return ARMOR;
    }
}
