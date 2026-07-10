package com.darkifov.thaumcraft.golem;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

/**
 * Strict TC4 itemGolemCore metadata parity.
 * Original metadata: 0 fill, 1 empty, 2 gather, 3 harvest, 4 guard, 5 liquid,
 * 6 essentia, 7 lumber, 8 use, 9 butcher, 10 sorting, 11 fish, 100 blank.
 */
public enum GolemCoreType {
    /** Original EntityGolemBase state before a functional core is installed. */
    BLANK("blank", -1, false, false, false, ChatFormatting.GRAY),
    FILL("fill", 0, true, true, true, ChatFormatting.AQUA),
    EMPTY("empty", 1, true, true, true, ChatFormatting.BLUE),
    GATHER("gather", 2, true, true, true, ChatFormatting.GREEN),
    HARVEST("harvest", 3, false, false, false, ChatFormatting.YELLOW),
    GUARD("guard", 4, true, false, false, ChatFormatting.RED),
    LIQUID("liquid", 5, true, false, true, ChatFormatting.AQUA),
    ESSENTIA("essentia", 6, false, false, false, ChatFormatting.DARK_PURPLE),
    LUMBER("lumber", 7, false, false, false, ChatFormatting.GOLD),
    USE("use", 8, true, true, true, ChatFormatting.LIGHT_PURPLE),
    BUTCHER("butcher", 9, false, false, false, ChatFormatting.RED),
    SORTING("sorting", 10, true, true, false, ChatFormatting.DARK_AQUA),
    FISH("fish", 11, false, false, false, ChatFormatting.BLUE),

    /** Existing pre-Stage195 migration adapter: not an original TC4 itemGolemCore metadata. */
    BODYGUARD("bodyguard", -2, false, false, false, ChatFormatting.DARK_RED),
    /** Existing pre-Stage195 migration adapter: not an original TC4 itemGolemCore metadata. */
    PATROL("patrol", -3, false, false, false, ChatFormatting.GRAY);

    private final String id;
    private final int originalId;
    private final boolean hasGui;
    private final boolean canSort;
    private final boolean hasInventory;
    private final ChatFormatting color;

    GolemCoreType(String id, int originalId, boolean hasGui, boolean canSort, boolean hasInventory, ChatFormatting color) {
        this.id = id;
        this.originalId = originalId;
        this.hasGui = hasGui;
        this.canSort = canSort;
        this.hasInventory = hasInventory;
        this.color = color;
    }

    public String id() {
        return id;
    }

    public int originalId() {
        return originalId;
    }

    public boolean originalTc4Core() {
        return originalId >= 0;
    }

    public boolean hasGui() {
        return hasGui;
    }

    public boolean canSort() {
        return canSort;
    }

    public boolean hasInventory() {
        return hasInventory;
    }

    public int baseInventorySize() {
        if (this == LIQUID) {
            return 1;
        }
        return hasInventory ? 6 : 0;
    }

    public Component displayName() {
        return Component.literal(id.substring(0, 1).toUpperCase() + id.substring(1)).withStyle(color);
    }

    public static GolemCoreType byOriginalId(int metadata) {
        for (GolemCoreType type : values()) {
            if (type.originalId == metadata) {
                return type;
            }
        }
        return metadata == -1 ? BLANK : GATHER;
    }

    public static GolemCoreType byName(String id) {
        if (id != null) {
            for (GolemCoreType type : values()) {
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
        return GATHER;
    }

    public GolemCoreType next() {
        GolemCoreType[] values = originalValues();
        int index = 0;
        for (int i = 0; i < values.length; i++) {
            if (values[i] == this) {
                index = i;
                break;
            }
        }
        return values[(index + 1) % values.length];
    }

    public static GolemCoreType[] originalValues() {
        return new GolemCoreType[]{FILL, EMPTY, GATHER, HARVEST, GUARD, LIQUID, ESSENTIA, LUMBER, USE, BUTCHER, SORTING, FISH};
    }
}
