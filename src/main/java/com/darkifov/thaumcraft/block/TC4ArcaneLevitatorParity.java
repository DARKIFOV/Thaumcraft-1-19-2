package com.darkifov.thaumcraft.block;

/**
 * Dependency-free source contract for Thaumcraft 4.2.3.5 BlockLifter/TileLifter.
 * Runtime code delegates its constants and boundary math here so the original
 * behavior can be compiled and tested with plain Java 17.
 */
public final class TC4ArcaneLevitatorParity {
    public static final String CONTRACT_VERSION = "11.64.20";

    public static final float BLOCK_HARDNESS = 2.5F;
    public static final float BLOCK_EXPLOSION_RESISTANCE = 15.0F;
    public static final int REFRESH_INTERVAL_TICKS = 100;
    public static final int BASE_RANGE = 10;
    public static final int RANGE_PER_LOWER_LEVITATOR = 10;

    public static final double MAX_UPWARD_VELOCITY = 0.3499999940395355D;
    public static final double LIFT_INCREMENT = 0.10000000149011612D;
    public static final double SNEAK_DESCENT_MULTIPLIER = 0.8999999761581421D;

    public static final int INVENTORY_TOP_GLOW = 0x00A000;
    public static final int INVENTORY_SIDE_GLOW = 0xEECCFF;
    public static final int WORLD_TOP_GLOW = 0x00A000;
    public static final int WORLD_SIDE_GLOW = 0xDD11FF;
    public static final int ACTIVE_GLOW_LEGACY_BRIGHTNESS = 180;
    public static final int ACTIVE_GLOW_BLOCK_LIGHT = ACTIVE_GLOW_LEGACY_BRIGHTNESS >> 4;

    public static final int PARTICLE_COLOR_TYPE = 3;
    public static final int PARTICLE_GATE_DENOMINATOR = 6;
    public static final int PARTICLE_ALL_THRESHOLD = 4;
    public static final int PARTICLE_DECREASED_THRESHOLD = 2;
    public static final int PARTICLE_MINIMAL_THRESHOLD = 0;
    public static final int PARTICLE_MULTIPLIER = 6;
    public static final int PARTICLE_LIFETIME = 3 * PARTICLE_MULTIPLIER;
    public static final float PARTICLE_SIZE = 1.0F;
    public static final float PARTICLE_GRAVITY = -0.3F;

    public static final int RESEARCH_MOTUS = 3;
    public static final int RESEARCH_VOLATUS = 3;
    public static final int RESEARCH_AER = 3;
    public static final int RECIPE_AER = 10;
    public static final int RECIPE_TERRA = 5;

    private TC4ArcaneLevitatorParity() {
    }

    /** Original maximum column height before obstruction clipping. */
    public static int stackedMaximumRange(int continuousUnpoweredLowerLevitators) {
        return BASE_RANGE + Math.max(0, continuousUnpoweredLowerLevitators) * RANGE_PER_LOWER_LEVITATOR;
    }

    /** Original vertical motion update from TileLifter. */
    public static double nextVerticalVelocity(double currentY, boolean sneaking) {
        if (sneaking) {
            return currentY < 0.0D ? currentY * SNEAK_DESCENT_MULTIPLIER : currentY;
        }
        return currentY < MAX_UPWARD_VELOCITY ? currentY + LIFT_INCREMENT : currentY;
    }

    /** Original lower-stack condition checks redstone at that lower block position only. */
    public static boolean lowerSegmentContributes(boolean lowerPositionPowered) {
        return !lowerPositionPowered;
    }

    /** Original entity admission: item OR pushable OR horse. */
    public static boolean admitsEntity(boolean item, boolean pushable, boolean horse) {
        return item || pushable || horse;
    }

    /** Legacy Forge side values 0/1 are vertical; only 2..5 connect. */
    public static boolean connectsRedstoneOnLegacySide(int legacySide) {
        return legacySide > 1 && legacySide < 6;
    }

    /** Original type-3 sparkle color. */
    public static float particleRed() {
        return 0.2F;
    }

    public static float particleGreen(float randomUnit) {
        return 0.7F + clampUnit(randomUnit) * 0.3F;
    }

    public static float particleBlue() {
        return 0.2F;
    }

    private static float clampUnit(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }
}
