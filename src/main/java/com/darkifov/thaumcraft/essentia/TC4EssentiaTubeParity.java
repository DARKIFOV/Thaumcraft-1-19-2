package com.darkifov.thaumcraft.essentia;

/** Dependency-free Thaumcraft 4.2.3.5 tube, valve and buffer constants. */
public final class TC4EssentiaTubeParity {
    public static final String CONTRACT_VERSION = "11.64.36";
    public static final int SUCTION_RECALC_INTERVAL_TICKS = 2;
    public static final int TRANSFER_INTERVAL_TICKS = 5;
    public static final int BUFFER_BELLOWS_REFRESH_TICKS = 20;
    public static final int TRANSFER_AMOUNT = 1;
    public static final int BUFFER_CAPACITY = 8;
    public static final int MINIMUM_SUCTION = 0;
    public static final int VENTING_SERVER_TICKS = 40;
    public static final int VENTING_CLIENT_TICKS = 50;
    public static final int VALVE_ROTATION_STEP = 20;
    public static final int VALVE_ROTATION_MAX = 360;
    public static final float VALVE_RENDER_ROTATION_MULTIPLIER = 1.5F;
    public static final float VALVE_RENDER_TRAVEL = 0.12F;
    public static final double CORE_MIN = 0.34375D;
    public static final double CORE_MAX = 0.65625D;
    public static final double ARM_MIN = 0.42D;
    public static final double ARM_MAX = 0.58D;

    private TC4EssentiaTubeParity() {}

    public static int propagatedSuction(int neighbourSuction, boolean restricted) {
        if (neighbourSuction <= 0) return 0;
        return restricted ? neighbourSuction / 2 : neighbourSuction - 1;
    }

    public static int bufferSuction(int bellows, int choke) {
        if (bellows <= 0 || choke == 1) return 1;
        if (choke == 2) return 0;
        return bellows * 32;
    }

    public static int bufferComparator(int amount) {
        int clamped = Math.max(0, Math.min(BUFFER_CAPACITY, amount));
        return clamped == 0 ? 0 : (int) Math.floor(clamped / 8.0F * 14.0F) + 1;
    }

    public static float nextValveRotation(float rotation, boolean allowFlow) {
        if (!allowFlow && rotation < VALVE_ROTATION_MAX) {
            return Math.min(VALVE_ROTATION_MAX, rotation + VALVE_ROTATION_STEP);
        }
        if (allowFlow && rotation > 0.0F) {
            return Math.max(0.0F, rotation - VALVE_ROTATION_STEP);
        }
        return rotation;
    }

    public static int nextChoke(int current) {
        return (Math.max(0, current) + 1) % 3;
    }
}
