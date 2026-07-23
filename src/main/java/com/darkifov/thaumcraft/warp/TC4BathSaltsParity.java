package com.darkifov.thaumcraft.warp;

/**
 * Pure, dependency-free TC4 4.2.3.5 contract for Purifying Bath Salts,
 * Purifying Fluid and the Warp Ward granted by a source block.
 */
public final class TC4BathSaltsParity {
    public static final String CONTRACT_VERSION = "11.64.14";

    public static final int ITEM_ENTITY_LIFESPAN_TICKS = 200;
    public static final int FLUID_LIGHT_LEVEL = 10;
    public static final int FLUID_VISCOSITY = 1000;
    public static final int FLUID_DENSITY = 1000;
    public static final int FLUID_TICK_RATE = 5;
    public static final int FLUID_SLOPE_FIND_DISTANCE = 4;
    public static final int FLUID_LEVEL_DECREASE_PER_BLOCK = 1;

    public static final int WARD_DURATION_CAP_TICKS = 32000;
    public static final int WARD_DURATION_NUMERATOR = 200000;
    public static final int WARD_AMPLIFIER = 0;
    public static final int WARD_ICON_COLUMN = 3;
    public static final int WARD_ICON_ROW = 2;
    public static final int WARD_COLOR = 0xE0F2F7;
    public static final double WARD_EFFECTIVENESS = 0.25D;

    public static final int BUBBLE_PARTICLE_FIRST_FRAME = 16;
    public static final int BUBBLE_PARTICLE_LAST_FRAME = 18;
    public static final double BUBBLE_UPWARD_ACCELERATION = 0.002D;
    public static final double BUBBLE_VELOCITY_DAMPING = 0.8500000238418579D;
    public static final float BUBBLE_ALPHA = 0.25F;
    public static final float BUBBLE_RED = 1.0F;
    public static final float BUBBLE_GREEN = 1.0F;
    public static final float BUBBLE_BLUE = 1.0F;
    public static final int POP_SOUND_CHANCE_BOUND = 25;
    public static final float POP_VOLUME_BASE = 0.10F;
    public static final float POP_VOLUME_SPAN = 0.10F;
    public static final float POP_PITCH_BASE = 0.90F;
    public static final float POP_PITCH_SPAN = 0.15F;
    public static final double POP_SOUND_Y_OFFSET = 1.0D;

    public static final int RECIPE_COGNITIO = 6;
    public static final int RECIPE_AURAM = 6;
    public static final int RECIPE_ORDO = 6;
    public static final int RECIPE_SANO = 6;
    public static final int RESEARCH_COGNITIO = 3;
    public static final int RESEARCH_AURAM = 3;
    public static final int RESEARCH_ORDO = 3;
    public static final int RESEARCH_SANO = 3;
    public static final int RESEARCH_X = -4;
    public static final int RESEARCH_Y = -4;
    public static final int RESEARCH_COMPLEXITY = 2;

    public static final int WIZARD_TRADE_EMERALD_MIN = 5;
    public static final int WIZARD_TRADE_RANDOM_BOUND = 3;

    private TC4BathSaltsParity() {
    }

    public static boolean convertsExpiredItem(boolean exactVanillaWaterBlock, int legacyMetadata) {
        return exactVanillaWaterBlock && legacyMetadata == 0;
    }

    public static int wardDivisor(int permanentWarp) {
        if (permanentWarp <= 0) {
            return 1;
        }
        return Math.max(1, (int) Math.sqrt(permanentWarp));
    }

    public static int wardDurationTicks(int permanentWarp) {
        return Math.min(WARD_DURATION_CAP_TICKS,
                WARD_DURATION_NUMERATOR / wardDivisor(permanentWarp));
    }

    /** Original BlockFluidPure y = blockY + 0.125F * (8 - legacyMetadata). */
    public static double bubbleYOffset(int legacyMetadata) {
        return 0.125D * (8 - legacyMetadata);
    }

    /** Original lifetime: (int)(age + 2 + 8 / (random * 0.8 + 0.2)), age = 0 here. */
    public static int bubbleLifetime(double randomDouble) {
        if (randomDouble < 0.0D || randomDouble >= 1.0D) {
            throw new IllegalArgumentException("randomDouble must be in [0, 1)");
        }
        return (int) (2.0D + 8.0D / (randomDouble * 0.8D + 0.2D));
    }

    public static boolean playsPopSound(int nextInt25) {
        return nextInt25 == 0;
    }

    public static float popVolume(float randomFloat) {
        return POP_VOLUME_BASE + randomFloat * POP_VOLUME_SPAN;
    }

    public static float popPitch(float randomFloat) {
        return POP_PITCH_BASE + randomFloat * POP_PITCH_SPAN;
    }

    public static int wizardEmeraldCost(int nextInt3) {
        if (nextInt3 < 0 || nextInt3 >= WIZARD_TRADE_RANDOM_BOUND) {
            throw new IllegalArgumentException("nextInt3 must be in [0, 2]");
        }
        return WIZARD_TRADE_EMERALD_MIN + nextInt3;
    }
}
