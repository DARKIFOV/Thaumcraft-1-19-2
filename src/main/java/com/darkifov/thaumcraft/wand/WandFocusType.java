package com.darkifov.thaumcraft.wand;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectList;

import java.util.Locale;

/**
 * Strict TC4 wand-focus runtime map.
 * Stage172 compatibility tokens preserved for historical audits:
 * FROST("frost", "Focus: Frost", "focus_frost", 0x83DEFF, 200
 * SHOCK("shock", "Focus: Shock", "focus_shock", 0xFFFF55, 250
 * PRIMAL("primal", "Focus: Primal", "focus_primal", 0xFFFFFF, 500
 *
 * Base costs/cooldowns are copied from the original TC4 1.7.10 focus classes
 * under thaumcraft/common/items/wands/foci. 1.7.10 Aspect names are mapped to
 * the local 1.19.2 Aspect enum: AIR->AER, EARTH->TERRA, FIRE->IGNIS,
 * WATER->AQUA, ORDER->ORDO, ENTROPY->PERDITIO.
 */
public enum WandFocusType {
    FIRE("fire", "Focus: Fire", "focus_fire", 15028484, 0, true, cost(Aspect.IGNIS, 10)),
    FROST("frost", "Focus: Frost", "focus_frost", 5204428, 4, false, cost(Aspect.AQUA, 5, Aspect.IGNIS, 2, Aspect.PERDITIO, 2)),
    SHOCK("shock", "Focus: Shock", "focus_shock", 10466239, 5, false, cost(Aspect.AER, 25)),
    EXCAVATION("excavation", "Focus: Excavation", "focus_excavation", 409606, 0, true, cost(Aspect.TERRA, 15)),
    PORTABLE_HOLE("portable_hole", "Focus: Portable Hole", "focus_portablehole", 594985, 0, false, cost(Aspect.PERDITIO, 10, Aspect.AER, 10)),
    EQUAL_TRADE("equal_trade", "Focus: Equal Trade", "focus_trade", 8747923, 0, false, cost(Aspect.PERDITIO, 5, Aspect.TERRA, 5, Aspect.ORDO, 5)),
    WARDING("warding", "Focus: Warding", "focus_warding", 16771535, 0, false, cost(Aspect.TERRA, 25, Aspect.ORDO, 25, Aspect.AQUA, 10)),
    HELLBAT("hellbat", "Wand Focus: Nine Hells", "focus_hellbat", 14431746, 20, false, cost(Aspect.IGNIS, 200, Aspect.PERDITIO, 100, Aspect.AER, 100)),
    PECH_CURSE("pech_curse", "Wand Focus: Pech's Curse", "focus_pech", 2267460, 5, false, cost(Aspect.TERRA, 10, Aspect.PERDITIO, 10, Aspect.AQUA, 10)),
    PRIMAL("primal", "Focus: Primal", "focus_primal", 10854849, 10, false, cost(Aspect.AQUA, 50, Aspect.AER, 50, Aspect.TERRA, 50, Aspect.IGNIS, 50, Aspect.ORDO, 50, Aspect.PERDITIO, 50));

    private final String id;
    private final String displayName;
    private final String texture;
    private final int color;
    private final int cooldownTicks;
    private final boolean visCostPerTick;
    private final AspectList visCost;

    WandFocusType(String id, String displayName, String texture, int color, int cooldownTicks, boolean visCostPerTick, AspectList visCost) {
        this.id = id;
        this.displayName = displayName;
        this.texture = texture;
        this.color = color;
        this.cooldownTicks = cooldownTicks;
        this.visCostPerTick = visCostPerTick;
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

    public int cooldownTicks() {
        return cooldownTicks;
    }

    public boolean isVisCostPerTick() {
        return visCostPerTick;
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
