package com.darkifov.thaumcraft.block;

/**
 * Dependency-free Thaumcraft 4.2.3.5 Arcane Bellows constants and formulas.
 *
 * <p>The legacy orientation ordinal follows ForgeDirection/Minecraft Direction:
 * DOWN=0, UP=1, NORTH=2, SOUTH=3, WEST=4, EAST=5.</p>
 */
public final class TC4ArcaneBellowsParity {
    public static final String CONTRACT_VERSION = "11.64.18";

    public static final float BLOCK_HARDNESS = 2.5F;
    public static final float BLOCK_RESISTANCE = 10.0F;
    public static final double SHAPE_MIN_XZ = 0.1D;
    public static final double SHAPE_MAX_XZ = 0.9D;
    public static final double SHAPE_MIN_Y = 0.0D;
    public static final double SHAPE_MAX_Y = 1.0D;

    public static final float INITIAL_INFLATION_BASE = 0.35F;
    public static final float INITIAL_INFLATION_RANGE = 0.55F;
    public static final float MIN_INFLATION = 0.35F;
    public static final float MAX_INFLATION = 1.0F;
    public static final float DEFLATE_STEP = 0.075F;
    public static final float INFLATE_STEP = 0.025F;
    public static final float SOUND_VOLUME = 0.01F;
    public static final float SOUND_PITCH_BASE = 0.5F;
    public static final float SOUND_PITCH_RANGE = 0.2F;

    public static final int VANILLA_FURNACE_DELAY_TICKS = 2;
    public static final int VANILLA_FURNACE_MIN_COOK_TIME_EXCLUSIVE = 0;
    public static final int VANILLA_FURNACE_MAX_COOK_TIME_EXCLUSIVE = 199;
    public static final int MAX_GENERIC_ATTACHED_BELLOWS = 6;
    public static final int MAX_INFERNAL_FURNACE_BELLOWS = 3;
    public static final int BUFFER_SUCTION_PER_BELLOWS = 32;
    public static final int CRUCIBLE_BASE_HEAT_GAIN = 1;
    public static final int CRUCIBLE_HEAT_GAIN_PER_BELLOWS = 2;
    public static final float ALCHEMICAL_FURNACE_TIME_REDUCTION_PER_BELLOWS = 0.125F;
    public static final int INFERNAL_FURNACE_NORMAL_COOK_TIME = 140;
    public static final int INFERNAL_FURNACE_SPEEDY_COOK_TIME = 80;
    public static final int INFERNAL_FURNACE_TIME_REDUCTION_PER_BELLOWS = 20;
    public static final float INFERNAL_FURNACE_BONUS_CHANCE_PER_BELLOWS = 0.44F;

    private TC4ArcaneBellowsParity() {
    }

    public static float initialInflation(float randomFloat) {
        return INITIAL_INFLATION_BASE + randomFloat * INITIAL_INFLATION_RANGE;
    }

    public static AnimationStep animationStep(float inflation, boolean expanding) {
        float next = inflation;
        boolean nextExpanding = expanding;
        boolean playSound = false;

        if (next > MIN_INFLATION && !nextExpanding) {
            next -= DEFLATE_STEP;
        }
        if (next <= MIN_INFLATION && !nextExpanding) {
            nextExpanding = true;
        }
        if (next < MAX_INFLATION && nextExpanding) {
            next += INFLATE_STEP;
        }
        if (next >= MAX_INFLATION && nextExpanding) {
            nextExpanding = false;
            playSound = true;
        }
        return new AnimationStep(next, nextExpanding, playSound);
    }

    public static float soundPitch(float firstRandom, float secondRandom) {
        return SOUND_PITCH_BASE + (firstRandom - secondRandom) * SOUND_PITCH_RANGE;
    }

    public static float inventoryInflation(int playerTicks) {
        return (float) Math.sin(playerTicks / 8.0F) * 0.3F + 0.7F;
    }

    public static boolean canAdvanceVanillaFurnace(int cookTime) {
        return cookTime > VANILLA_FURNACE_MIN_COOK_TIME_EXCLUSIVE
                && cookTime < VANILLA_FURNACE_MAX_COOK_TIME_EXCLUSIVE;
    }

    /**
     * TileBellows intentionally ignored vertical Y offsets when it looked up a
     * vanilla furnace during ticking. The placement flag could be true above or
     * below a furnace, but only horizontal orientations could actually boost it.
     */
    public static Offset legacyVanillaFurnaceOffset(int orientationOrdinal) {
        return switch (orientationOrdinal) {
            case 2 -> new Offset(0, 0, -1);
            case 3 -> new Offset(0, 0, 1);
            case 4 -> new Offset(-1, 0, 0);
            case 5 -> new Offset(1, 0, 0);
            default -> new Offset(0, 0, 0);
        };
    }

    public static int crucibleHeatGain(int bellows) {
        return CRUCIBLE_BASE_HEAT_GAIN + Math.max(0, bellows) * CRUCIBLE_HEAT_GAIN_PER_BELLOWS;
    }

    public static int alchemicalFurnaceSmeltTime(int visSize, int bellows) {
        int boundedBellows = Math.max(0, Math.min(MAX_GENERIC_ATTACHED_BELLOWS, bellows));
        return Math.max(1, (int) (Math.max(1, visSize) * 10.0F
                * (1.0F - ALCHEMICAL_FURNACE_TIME_REDUCTION_PER_BELLOWS * boundedBellows)));
    }

    public static int bufferSuction(int bellows, int choke) {
        if (bellows <= 0 || choke == 1) {
            return 1;
        }
        if (choke == 2) {
            return 0;
        }
        return bellows * BUFFER_SUCTION_PER_BELLOWS;
    }

    public static int infernalFurnaceCookTime(boolean speedy, int bellows) {
        int bounded = Math.max(0, Math.min(MAX_INFERNAL_FURNACE_BELLOWS, bellows));
        return (speedy ? INFERNAL_FURNACE_SPEEDY_COOK_TIME : INFERNAL_FURNACE_NORMAL_COOK_TIME)
                - INFERNAL_FURNACE_TIME_REDUCTION_PER_BELLOWS * bounded;
    }

    public record Offset(int x, int y, int z) {
    }

    public record AnimationStep(float inflation, boolean expanding, boolean playSound) {
    }
}
