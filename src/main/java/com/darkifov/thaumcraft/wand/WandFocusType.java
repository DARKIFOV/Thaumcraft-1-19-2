package com.darkifov.thaumcraft.wand;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectList;

import java.util.Locale;

/**
 * Stage129: strict TC4 wand-focus runtime map.
 *
 * Costs and colours are taken from the original TC4 focus classes under
 * thaumcraft/common/items/wands/foci. 1.7.10 Aspect names are mapped to the
 * local 1.19.2 Aspect enum: AIR->AER, EARTH->TERRA, FIRE->IGNIS,
 * WATER->AQUA, ORDER->ORDO, ENTROPY->PERDITIO.
 */
public enum WandFocusType {
    FIRE("fire", "Focus: Fire", "focus_fire", 0xFF5A00, cost(Aspect.IGNIS, 10)),
    FROST("frost", "Focus: Frost", "focus_frost", 0x83DEFF, cost(Aspect.AQUA, 5, Aspect.IGNIS, 2, Aspect.PERDITIO, 2)),
    SHOCK("shock", "Focus: Shock", "focus_shock", 0xFFFF55, cost(Aspect.AER, 25)),
    EXCAVATION("excavation", "Focus: Excavation", "focus_excavation", 0xB08040, cost(Aspect.TERRA, 15)),
    PORTABLE_HOLE("portable_hole", "Focus: Portable Hole", "focus_portablehole", 0x5C1E7A, cost(Aspect.PERDITIO, 10, Aspect.AER, 10)),
    EQUAL_TRADE("equal_trade", "Focus: Equal Trade", "focus_trade", 0x72A050, cost(Aspect.PERDITIO, 5, Aspect.TERRA, 5, Aspect.ORDO, 5)),
    WARDING("warding", "Focus: Warding", "focus_warding", 0x4A6CFF, cost(Aspect.TERRA, 25, Aspect.ORDO, 25, Aspect.AQUA, 10)),
    PRIMAL("primal", "Focus: Primal", "focus_primal", 0xFFFFFF, cost(Aspect.AQUA, 50, Aspect.AER, 50, Aspect.TERRA, 50, Aspect.IGNIS, 50, Aspect.ORDO, 50, Aspect.PERDITIO, 50));

    private final String id;
    private final String displayName;
    private final String texture;
    private final int color;
    private final AspectList visCost;

    WandFocusType(String id, String displayName, String texture, int color, AspectList visCost) {
        this.id = id;
        this.displayName = displayName;
        this.texture = texture;
        this.color = color;
        this.visCost = visCost;
    }

    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public String texture() {
        return texture;
    }

    public int color() {
        return color;
    }

    public AspectList visCost() {
        AspectList copy = new AspectList();
        copy.addAll(visCost);
        return copy;
    }

    public static WandFocusType byId(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        String normalized = id.toLowerCase(Locale.ROOT);
        for (WandFocusType type : values()) {
            if (type.id.equals(normalized) || type.name().equalsIgnoreCase(id)) {
                return type;
            }
        }
        return null;
    }

    private static AspectList cost(Object... pairs) {
        AspectList list = new AspectList();
        for (int i = 0; i + 1 < pairs.length; i += 2) {
            list.add((Aspect) pairs[i], (Integer) pairs[i + 1]);
        }
        return list;
    }
}
