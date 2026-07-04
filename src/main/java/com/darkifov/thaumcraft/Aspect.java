package com.darkifov.thaumcraft;

import net.minecraft.ChatFormatting;

public enum Aspect {
    AER("Aer", ChatFormatting.YELLOW),
    TERRA("Terra", ChatFormatting.GREEN),
    IGNIS("Ignis", ChatFormatting.RED),
    AQUA("Aqua", ChatFormatting.AQUA),
    ORDO("Ordo", ChatFormatting.WHITE),
    PERDITIO("Perditio", ChatFormatting.DARK_PURPLE),
    VITREUS("Vitreus", ChatFormatting.BLUE),
    METALLUM("Metallum", ChatFormatting.GRAY),
    PRAECANTATIO("Praecantatio", ChatFormatting.LIGHT_PURPLE),
    VACUOS("Vacuos", ChatFormatting.DARK_GRAY),
    HERBA("Herba", ChatFormatting.DARK_GREEN),
    LUX("Lux", ChatFormatting.GOLD),
    POTENTIA("Potentia", ChatFormatting.DARK_RED);

    private final String displayName;
    private final ChatFormatting color;

    Aspect(String displayName, ChatFormatting color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String displayName() {
        return displayName;
    }

    public ChatFormatting color() {
        return color;
    }
}
