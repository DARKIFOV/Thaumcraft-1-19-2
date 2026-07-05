package com.darkifov.thaumcraft.essentia;

import com.darkifov.thaumcraft.Aspect;

public record EssentiaSuction(Aspect aspect, int strength, String reason) {
    public static final int SOURCE_NONE = 0;
    public static final int JAR_NORMAL = 32;
    public static final int JAR_FILTERED = 48;
    public static final int JAR_VOID = 64;
    public static final int ALEMBIC_SOURCE_PRIORITY = 96;
    public static final int FURNACE_SOURCE_PRIORITY = 48;

    public boolean beats(EssentiaSuction other) {
        return other == null || strength > other.strength;
    }
}
