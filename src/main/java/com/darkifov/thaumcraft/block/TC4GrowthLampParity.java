package com.darkifov.thaumcraft.block;

/** Dependency-free source contract for TC4 TileArcaneLampGrowth. */
public final class TC4GrowthLampParity {
    public static final String CONTRACT_VERSION = "11.64.29";
    public static final String RESEARCH = "LAMPGROWTH";
    public static final String NBT_ORIENTATION = "orientation";
    public static final String NBT_RESERVE = "reserve";
    public static final String NBT_CHARGES = "charges";

    public static final int RADIUS = 6;
    public static final int DIAMETER = RADIUS * 2 + 1;
    public static final int COLUMN_COUNT = DIAMETER * DIAMETER;
    public static final int CHARGES_PER_ESSENTIA = 100;
    public static final int DRAW_INTERVAL_TICKS = 5;
    public static final int SUCTION = 128;
    public static final int ACTIVE_LIGHT = 15;
    public static final int INACTIVE_LIGHT = 8;
    public static final int SPARKLE_COLOR = 4_259_648;
    public static final int SPARKLE_RANGE = 32;
    public static final boolean USES_SINGLE_NATURAL_TICK = true;
    public static final float BLOCK_HARDNESS = 3.0F;
    public static final float BLOCK_RESISTANCE = 17.0F;

    private TC4GrowthLampParity() {
    }

    public static boolean isActive(int charges) {
        return charges > 0;
    }

    public static int suction(boolean reserve, int charges, boolean requestedFace) {
        return requestedFace && (!reserve || charges <= 0) ? SUCTION : 0;
    }

    public static boolean insideSphere(int dx, int dy, int dz) {
        return dx * dx + dy * dy + dz * dz < RADIUS * RADIUS;
    }

    public static int columnCount() {
        return COLUMN_COUNT;
    }
}
