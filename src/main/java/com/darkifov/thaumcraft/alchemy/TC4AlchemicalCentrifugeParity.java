package com.darkifov.thaumcraft.alchemy;

/** Dependency-free constants and state transitions copied from TC4 4.2.3.5 TileCentrifuge. */
public final class TC4AlchemicalCentrifugeParity {
    public static final String CONTRACT_VERSION = "11.64.38";
    public static final int INPUT_SUCTION = 64;
    public static final int PROCESS_START = 39;
    public static final int DRAW_INTERVAL_TICKS = 5;
    public static final int OUTPUT_CAPACITY = 1;
    public static final int DEFAULT_FACING_ORDINAL = 2;
    public static final float MAX_ROTATION_SPEED = 20.0F;
    public static final float ROTATION_ACCELERATION = 2.0F;
    public static final float ROTATION_DECELERATION = 0.5F;

    private TC4AlchemicalCentrifugeParity() {}

    public static int tickProcess(int process, boolean powered) {
        return powered ? Math.max(0, process) : Math.max(0, process - 1);
    }

    public static boolean shouldProcess(int process, boolean powered, boolean hasInput, boolean hasOutput) {
        return !powered && !hasOutput && hasInput && process <= 0;
    }

    public static float nextRotationSpeed(float speed, boolean spinning) {
        if (spinning && speed < MAX_ROTATION_SPEED) {
            return Math.min(MAX_ROTATION_SPEED, speed + ROTATION_ACCELERATION);
        }
        if (!spinning && speed > 0.0F) {
            return Math.max(0.0F, speed - ROTATION_DECELERATION);
        }
        return speed;
    }

    public static boolean shouldPlayPump(float previousRotation, float currentRotation, float speed) {
        int previousWhole = (int) previousRotation;
        return currentRotation % 180.0F <= 20.0F && previousWhole % 180 >= 160 && speed > 0.0F;
    }

    public static int componentIndex(int randomValue) {
        return Math.floorMod(randomValue, 2);
    }
}
