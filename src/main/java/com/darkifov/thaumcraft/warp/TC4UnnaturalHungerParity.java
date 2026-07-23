package com.darkifov.thaumcraft.warp;

/**
 * Complete source-level contract for Thaumcraft 4.2.3.5 Unnatural Hunger.
 *
 * <p>This class deliberately contains only constants and pure calculations so
 * the original timing, amplification and post-processing math can be verified
 * without bootstrapping Forge.</p>
 */
public final class TC4UnnaturalHungerParity {
    public static final String CONTRACT_VERSION = "11.64.13";

    public static final int EFFECT_COLOR = 0x446633;
    public static final int ICON_COLUMN = 7;
    public static final int ICON_ROW = 1;

    public static final float EXHAUSTION_PER_LEVEL_PER_TICK = 0.025F;
    public static final int FIRST_WARP_DURATION_TICKS = 5000;
    public static final int SECOND_WARP_DURATION_TICKS = 6000;
    public static final int MAX_AMPLIFIER = 3;
    public static final int WARP_PER_AMPLIFIER_STEP = 15;

    public static final int CURATIVE_DURATION_REDUCTION_TICKS = 600;
    public static final int CURATIVE_AMPLIFIER_REDUCTION = 1;

    public static final float GRAY_RED = 0.30F;
    public static final float GRAY_GREEN = 0.59F;
    public static final float GRAY_BLUE = 0.11F;
    public static final float COLOR_SCALE_RED = 1.00F;
    public static final float COLOR_SCALE_GREEN = 0.80F;
    public static final float COLOR_SCALE_BLUE = 0.80F;
    public static final float SATURATION = 1.10F;

    private TC4UnnaturalHungerParity() {
    }

    public static float exhaustionPerTick(int amplifier) {
        return EXHAUSTION_PER_LEVEL_PER_TICK * (Math.max(0, amplifier) + 1);
    }

    public static int warpAmplifier(int normalizedWarp) {
        return Math.min(MAX_AMPLIFIER, Math.max(0, normalizedWarp) / WARP_PER_AMPLIFIER_STEP);
    }

    public static Reduction afterCurative(int currentDuration, int currentAmplifier) {
        int duration = currentDuration - CURATIVE_DURATION_REDUCTION_TICKS;
        int amplifier = currentAmplifier - CURATIVE_AMPLIFIER_REDUCTION;
        return new Reduction(duration, amplifier, duration > 0 && amplifier >= 0);
    }

    /**
     * CPU reference for the exact TC4 hunger post shader. The framebuffer
     * performs the final channel clamp; the shader itself does not clamp.
     */
    public static Rgb transform(float red, float green, float blue) {
        float scaledRed = red * COLOR_SCALE_RED;
        float scaledGreen = green * COLOR_SCALE_GREEN;
        float scaledBlue = blue * COLOR_SCALE_BLUE;
        float luma = scaledRed * GRAY_RED + scaledGreen * GRAY_GREEN + scaledBlue * GRAY_BLUE;
        return new Rgb(
                (scaledRed - luma) * SATURATION + luma,
                (scaledGreen - luma) * SATURATION + luma,
                (scaledBlue - luma) * SATURATION + luma);
    }

    public record Reduction(int duration, int amplifier, boolean remainsActive) {
    }

    public record Rgb(float red, float green, float blue) {
    }
}
