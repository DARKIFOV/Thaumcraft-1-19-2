package com.darkifov.thaumcraft.jar;

/** Dependency-free TC4 4.2.3.5 constants and edge rules for fillable jars. */
public final class TC4EssentiaJarParity {
    public static final String CONTRACT_VERSION = "11.64.35";
    public static final int CAPACITY = 64;
    public static final int FILL_INTERVAL_TICKS = 5;
    public static final int PHIAL_TRANSFER = 8;
    public static final int NORMAL_SUCTION = 32;
    public static final int LABELLED_SUCTION = 64;
    public static final int VOID_LABELLED_SUCTION = 48;

    private TC4EssentiaJarParity() {}

    public static int minimumSuction(boolean voidJar, boolean labelled) {
        return labelled ? (voidJar ? VOID_LABELLED_SUCTION : LABELLED_SUCTION) : NORMAL_SUCTION;
    }

    public static int suctionAmount(boolean voidJar, boolean labelled, int amount) {
        int clamped = Math.max(0, amount);
        if (!voidJar) {
            return clamped < CAPACITY ? (labelled ? LABELLED_SUCTION : NORMAL_SUCTION) : 0;
        }
        if (labelled && clamped < CAPACITY) {
            return VOID_LABELLED_SUCTION;
        }
        return NORMAL_SUCTION;
    }

    public static int remainderAfterInsert(boolean voidJar, boolean sameAspectOrEmpty, int amount, int incoming) {
        if (incoming <= 0) return incoming;
        if (!sameAspectOrEmpty) return incoming;
        if (voidJar) return 0;
        int space = Math.max(0, CAPACITY - Math.max(0, amount));
        return incoming - Math.min(space, incoming);
    }

    /** Original BlockJar placement mapping: 0=NORTH, 1=EAST, 2=SOUTH, 3=WEST. */
    public static int labelFacingDataValue(float yawDegrees) {
        int quadrant = floor(yawDegrees * 4.0F / 360.0F + 0.5F) & 3;
        return switch (quadrant) {
            case 0 -> 2;
            case 1 -> 5;
            case 2 -> 3;
            default -> 4;
        };
    }

    /** Exact TileJarRenderer crooked-label expression. */
    public static int crookedLabelRotation(String aspectTag, int blockX, int facingDataValue) {
        String safe = aspectTag == null ? "" : aspectTag;
        return (safe.hashCode() + blockX + facingDataValue) % 4 - 2;
    }

    public static boolean canFillEmptyPhial(int amount) {
        return amount >= PHIAL_TRANSFER;
    }

    public static boolean canEmptyFilledPhial(int amount, boolean acceptsAspect) {
        return acceptsAspect && amount <= CAPACITY - PHIAL_TRANSFER;
    }

    private static int floor(float value) {
        int truncated = (int) value;
        return value < truncated ? truncated - 1 : truncated;
    }
}
