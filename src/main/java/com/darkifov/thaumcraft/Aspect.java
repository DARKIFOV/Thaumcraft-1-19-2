package com.darkifov.thaumcraft;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextColor;

import java.util.Locale;

public enum Aspect {
    AER("aer", "Air", ChatFormatting.YELLOW, 16777086, "", ""),
    TERRA("terra", "Earth", ChatFormatting.GREEN, 5685248, "", ""),
    IGNIS("ignis", "Fire", ChatFormatting.RED, 16734721, "", ""),
    AQUA("aqua", "Water", ChatFormatting.AQUA, 3986684, "", ""),
    ORDO("ordo", "Order", ChatFormatting.WHITE, 14013676, "", ""),
    PERDITIO("perditio", "Entropy", ChatFormatting.DARK_PURPLE, 4210752, "", ""),
    VACUOS("vacuos", "Void", ChatFormatting.DARK_GRAY, 8947848, "aer", "perditio"),
    LUX("lux", "Light", ChatFormatting.GOLD, 16774755, "aer", "ignis"),
    TEMPESTAS("tempestas", "Weather", ChatFormatting.WHITE, 16777215, "aer", "aqua"),
    MOTUS("motus", "Motion", ChatFormatting.LIGHT_PURPLE, 13487348, "aer", "ordo"),
    GELUM("gelum", "Ice", ChatFormatting.AQUA, 14811135, "ignis", "perditio"),
    VITREUS("vitreus", "Crystal", ChatFormatting.BLUE, 8454143, "terra", "ordo"),
    VICTUS("victus", "Life", ChatFormatting.GREEN, 14548997, "aqua", "terra"),
    VENENUM("venenum", "Poison", ChatFormatting.DARK_GREEN, 9039872, "aqua", "perditio"),
    POTENTIA("potentia", "Energy", ChatFormatting.DARK_RED, 12648447, "ordo", "ignis"),
    PERMUTATIO("permutatio", "Exchange", ChatFormatting.DARK_AQUA, 5735255, "perditio", "ordo"),
    METALLUM("metallum", "Metal", ChatFormatting.GRAY, 11908557, "terra", "vitreus"),
    MORTUUS("mortuus", "Death", ChatFormatting.DARK_GRAY, 8943496, "victus", "perditio"),
    VOLATUS("volatus", "Flight", ChatFormatting.YELLOW, 15198167, "aer", "motus"),
    TENEBRAE("tenebrae", "Darkness", ChatFormatting.DARK_GRAY, 2236962, "vacuos", "lux"),
    SPIRITUS("spiritus", "Soul", ChatFormatting.LIGHT_PURPLE, 15461371, "victus", "mortuus"),
    SANO("sano", "Heal", ChatFormatting.RED, 16723764, "victus", "ordo"),
    ITER("iter", "Travel", ChatFormatting.AQUA, 14702683, "motus", "terra"),
    ALIENIS("alienis", "Eldritch", ChatFormatting.DARK_PURPLE, 8409216, "vacuos", "tenebrae"),
    PRAECANTATIO("praecantatio", "Magic", ChatFormatting.LIGHT_PURPLE, 9896128, "vacuos", "potentia"),
    AURAM("auram", "Aura", ChatFormatting.GOLD, 16761087, "praecantatio", "aer"),
    VITIUM("vitium", "Taint", ChatFormatting.DARK_PURPLE, 8388736, "praecantatio", "perditio"),
    LIMUS("limus", "Slime", ChatFormatting.DARK_GREEN, 129024, "victus", "aqua"),
    HERBA("herba", "Plant", ChatFormatting.DARK_GREEN, 109568, "victus", "terra"),
    ARBOR("arbor", "Tree", ChatFormatting.GREEN, 8873265, "aer", "herba"),
    BESTIA("bestia", "Beast", ChatFormatting.GOLD, 10445833, "motus", "victus"),
    CORPUS("corpus", "Flesh", ChatFormatting.RED, 15615885, "mortuus", "bestia"),
    EXANIMIS("exanimis", "Undead", ChatFormatting.DARK_GRAY, 3817472, "motus", "mortuus"),
    COGNITIO("cognitio", "Mind", ChatFormatting.YELLOW, 16761523, "ignis", "spiritus"),
    SENSUS("sensus", "Senses", ChatFormatting.BLUE, 1038847, "aer", "spiritus"),
    HUMANUS("humanus", "Man", ChatFormatting.GOLD, 16766912, "bestia", "cognitio"),
    MESSIS("messis", "Crop", ChatFormatting.GREEN, 14791537, "herba", "humanus"),
    PERFODIO("perfodio", "Mine", ChatFormatting.GRAY, 14471896, "humanus", "terra"),
    INSTRUMENTUM("instrumentum", "Tool", ChatFormatting.DARK_AQUA, 4210926, "humanus", "ordo"),
    METO("meto", "Harvest", ChatFormatting.GOLD, 15641986, "messis", "instrumentum"),
    TELUM("telum", "Weapon", ChatFormatting.DARK_RED, 12603472, "instrumentum", "ignis"),
    TUTAMEN("tutamen", "Armor", ChatFormatting.DARK_GREEN, 49344, "instrumentum", "terra"),
    FAMES("fames", "Hunger", ChatFormatting.YELLOW, 10093317, "victus", "vacuos"),
    LUCRUM("lucrum", "Greed", ChatFormatting.GOLD, 15121988, "humanus", "fames"),
    FABRICO("fabrico", "Craft", ChatFormatting.DARK_AQUA, 8428928, "humanus", "instrumentum"),
    PANNUS("pannus", "Cloth", ChatFormatting.WHITE, 15395522, "instrumentum", "bestia"),
    MACHINA("machina", "Machine", ChatFormatting.GRAY, 8421536, "motus", "instrumentum"),
    VINCULUM("vinculum", "Trap", ChatFormatting.DARK_PURPLE, 10125440, "motus", "perditio");

    private final String id;
    private final String displayName;
    private final ChatFormatting color;
    private final int colorValue;
    private final String componentA;
    private final String componentB;

    Aspect(String id, String displayName, ChatFormatting color, int colorValue, String componentA, String componentB) {
        this.id = id;
        this.displayName = displayName;
        this.color = color;
        this.colorValue = colorValue;
        this.componentA = componentA;
        this.componentB = componentB;
    }

    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public ChatFormatting color() {
        return color;
    }

    public int colorValue() {
        return colorValue;
    }

    public int nativeColor() {
        return colorValue & 0xFFFFFF;
    }

    public int argbColor() {
        return 0xFF000000 | nativeColor();
    }

    public TextColor textColor() {
        return TextColor.fromRgb(nativeColor());
    }

    public boolean isPrimal() {
        return componentA.isEmpty() || componentB.isEmpty();
    }

    public String componentA() {
        return componentA;
    }

    public String componentB() {
        return componentB;
    }

    public Aspect firstComponent() {
        return componentA.isEmpty() ? null : byId(componentA);
    }

    public Aspect secondComponent() {
        return componentB.isEmpty() ? null : byId(componentB);
    }

    public static Aspect byId(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }

        String normalized = id.toLowerCase(Locale.ROOT);

        for (Aspect aspect : values()) {
            if (aspect.id.equals(normalized) || aspect.name().equalsIgnoreCase(id)) {
                return aspect;
            }
        }

        return null;
    }

    public static Aspect require(String id) {
        Aspect aspect = byId(id);

        if (aspect == null) {
            throw new IllegalArgumentException("Unknown aspect: " + id);
        }

        return aspect;
    }

    public static boolean isPrimal(Aspect aspect) {
        return aspect != null && aspect.isPrimal();
    }
}
