package com.darkifov.thaumcraft.block;

/** Dependency-free numeric contract extracted from TC4 TileArcaneLamp and TileArcaneBore. */
public final class TC4ArcaneLampParity {
    public static final String CONTRACT_VERSION = "11.64.22";
    public static final int LIGHT_RADIUS = 15;
    public static final int RANDOM_BOUND = 16;
    public static final int MINIMUM_Y = 5;
    public static final int SURFACE_MARGIN = 4;
    public static final int LAMP_DARKNESS_THRESHOLD = 9;
    public static final int BORE_DARKNESS_THRESHOLD = 15;
    public static final int BORE_DISTANCE_BOUND = 32;
    public static final int BORE_DISTANCE_STEP = 2;
    public static final int BORE_LATERAL_OFFSET = 3;
    public static final int LIGHT_LEVEL = 15;
    public static final float BLOCK_HARDNESS = 3.0F;
    public static final float BLOCK_RESISTANCE = 17.0F;
    public static final double SHAPE_MIN_XZ = 0.25D;
    public static final double SHAPE_MIN_Y = 0.125D;
    public static final double SHAPE_MAX_XZ = 0.75D;
    public static final double SHAPE_MAX_Y = 0.875D;

    private TC4ArcaneLampParity() {}

    public static int triangularOffset(int first, int second) {
        requireRandom(first, RANDOM_BOUND);
        requireRandom(second, RANDOM_BOUND);
        return first - second;
    }

    public static int clampSampledY(int sampledY, int surfaceHeight, int minBuildHeight) {
        return Math.max(Math.max(MINIMUM_Y, minBuildHeight),
                Math.min(sampledY, surfaceHeight + SURFACE_MARGIN));
    }

    public static boolean insideBuildHeight(int y, int minBuildHeight, int maxBuildHeight) {
        return y >= minBuildHeight && y < maxBuildHeight;
    }

    public static boolean shouldPlaceLampLight(boolean targetIsAir, boolean targetIsOwnMarker,
                                                int brightness, boolean insideBuildHeight) {
        return insideBuildHeight && targetIsAir && !targetIsOwnMarker
                && brightness < LAMP_DARKNESS_THRESHOLD;
    }

    public static int boreDistance(int randomValue) {
        requireRandom(randomValue, BORE_DISTANCE_BOUND);
        return randomValue * BORE_DISTANCE_STEP;
    }

    public static int borePhase(int distance) {
        if (distance < 0 || distance % BORE_DISTANCE_STEP != 0) {
            throw new IllegalArgumentException("distance must be a non-negative even value");
        }
        return (distance / BORE_DISTANCE_STEP) % 4;
    }

    public static int boreLateralOffset(int distance) {
        return switch (borePhase(distance)) {
            case 0 -> BORE_LATERAL_OFFSET;
            case 2 -> -BORE_LATERAL_OFFSET;
            default -> 0;
        };
    }

    public static int boreVerticalOffset(int distance, boolean verticalFacing) {
        return borePhase(distance) == 3 && !verticalFacing ? -2 : 0;
    }

    public static boolean shouldPlaceBoreLight(boolean targetIsAir, boolean targetIsOwnMarker,
                                                int brightness, boolean insideBuildHeight) {
        return insideBuildHeight && targetIsAir && !targetIsOwnMarker
                && brightness < BORE_DARKNESS_THRESHOLD;
    }

    private static void requireRandom(int value, int bound) {
        if (value < 0 || value >= bound) {
            throw new IllegalArgumentException("random value outside [0," + bound + ")");
        }
    }
}
