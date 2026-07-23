package com.darkifov.thaumcraft.warp;

/**
 * Source-linked constants and pure calculations for TC4 warp runtime effects.
 *
 * <p>In TC4 the initial infectious-vis-exhaust instance created by a warp
 * event had its curatives cleared, but effects propagated to nearby entities
 * were plain PotionEffect instances and therefore retained the default milk
 * cure. Sanity Soap also decremented its stack unconditionally, including in
 * creative mode.</p>
 */
public final class TC4WarpRuntimeParity {
    public static final String CONTRACT_VERSION = "11.64.13";

    public static final int WARP_CHECK_INTERVAL_TICKS = 2000;
    public static final int INFECTIOUS_SPREAD_INTERVAL_TICKS = 40;
    public static final double INFECTIOUS_SPREAD_RADIUS = 4.0D;
    public static final int INFECTIOUS_SPREAD_DURATION_TICKS = 6000;

    public static final int SANITY_SOAP_USE_TICKS = 200;
    public static final int SANITY_SOAP_COMPLETION_THRESHOLD = 195;
    public static final float SANITY_SOAP_BASE_STICKY_CHANCE = 0.33F;
    public static final float SANITY_SOAP_WARD_BONUS = 0.25F;
    public static final float SANITY_SOAP_PURE_FLUID_BONUS = 0.25F;

    public static final int PURE_FLUID_WARD_NUMERATOR = 200000;
    public static final int PURE_FLUID_WARD_MAX_TICKS = 32000;

    public static final float SUN_SCORNED_BURN_BRIGHTNESS = 0.5F;
    public static final float SUN_SCORNED_HEAL_BRIGHTNESS = 0.25F;
    public static final float SUN_SCORNED_RANDOM_SCALE = 30.0F;
    public static final float SUN_SCORNED_BURN_OFFSET = 0.4F;
    public static final float SUN_SCORNED_CHANCE_MULTIPLIER = 2.0F;

    /** TC4 finishedUsingItem removed ten seconds from Unnatural Hunger. */
    public static final int UNNATURAL_HUNGER_CURATIVE_DURATION_REDUCTION_TICKS = TC4UnnaturalHungerParity.CURATIVE_DURATION_REDUCTION_TICKS;
    /** TC4 finishedUsingItem also reduced the potion amplifier by exactly one. */
    public static final int UNNATURAL_HUNGER_CURATIVE_AMPLIFIER_REDUCTION = TC4UnnaturalHungerParity.CURATIVE_AMPLIFIER_REDUCTION;

    /** Full TC4 Death Gaze cone aperture passed to EntityUtils.isVisibleTo. */
    public static final double DEATH_GAZE_APERTURE_RADIANS = 0.75D;

    /**
     * TC4 WarpEvents showed warp.text.8 ("...stop these headaches?") exactly
     * once, as the BATHSALTS milestone message when a player's actual warp
     * first exceeded 10 (checkWarpEvent, actualwarp > 10 branch). It is NOT the
     * blurred-vision event line; that event shows no chat text. v11.64.06
     * wrongly attached this key to the blurred-vision branch; v11.64.07 restores
     * the original BATHSALTS placement.
     */
    public static final String BATHSALTS_MILESTONE_MESSAGE_KEY = "warp.text.8";

    private TC4WarpRuntimeParity() {
    }

    public static SpreadResult infectiousSpread(int sourceAmplifier) {
        int amplifier = Math.max(0, sourceAmplifier);
        return amplifier > 0
                ? new SpreadResult(true, amplifier - 1, true)
                : new SpreadResult(false, 0, true);
    }

    public static float sanitySoapStickyChance(boolean hasWarpWard, boolean inPureFluid) {
        float chance = SANITY_SOAP_BASE_STICKY_CHANCE;
        if (hasWarpWard) {
            chance += SANITY_SOAP_WARD_BONUS;
        }
        if (inPureFluid) {
            chance += SANITY_SOAP_PURE_FLUID_BONUS;
        }
        return chance;
    }

    /** TC4 decremented the soap stack even when the player was in creative. */
    public static int sanitySoapConsumption(boolean creativeMode) {
        return 1;
    }

    public static int purifyingFluidWardDuration(int permanentWarp) {
        int divisor = Math.max(1, (int) Math.floor(Math.sqrt(Math.max(0, permanentWarp))));
        return Math.min(PURE_FLUID_WARD_MAX_TICKS, PURE_FLUID_WARD_NUMERATOR / divisor);
    }

    /**
     * Exact PotionSunScorned bright-side predicate. The caller must sample the
     * entity/world brightness table (modern getLightLevelDependentMagicValue),
     * not linearly divide the raw light level by 15.
     */
    public static boolean sunScornedBurns(float brightness, float randomRoll, boolean canSeeSky) {
        return brightness > SUN_SCORNED_BURN_BRIGHTNESS
                && randomRoll * SUN_SCORNED_RANDOM_SCALE
                < (brightness - SUN_SCORNED_BURN_OFFSET) * SUN_SCORNED_CHANCE_MULTIPLIER
                && canSeeSky;
    }

    /** Exact PotionSunScorned dark-side healing predicate. */
    public static boolean sunScornedHeals(float brightness, float randomRoll) {
        return brightness < SUN_SCORNED_HEAL_BRIGHTNESS
                && randomRoll > brightness * SUN_SCORNED_CHANCE_MULTIPLIER;
    }

    /**
     * Exact TC4 PlayerUseItemEvent.Finish reduction for rotten flesh or a
     * zombie brain while Unnatural Hunger is active. The current effect is
     * removed first and only re-added when both reduced values remain valid.
     */
    public static UnnaturalHungerReduction unnaturalHungerAfterCurative(
            int currentDuration, int currentAmplifier) {
        TC4UnnaturalHungerParity.Reduction reduction =
                TC4UnnaturalHungerParity.afterCurative(currentDuration, currentAmplifier);
        return new UnnaturalHungerReduction(
                reduction.duration(), reduction.amplifier(), reduction.remainsActive());
    }

    /**
     * TC4 multiplied a 7..24 magnitude by an inclusive random integer in
     * {@code [-1, 1]}. Zero is therefore an intentional outcome, not an error.
     * Values outside the tri-state range are normalized by sign so callers
     * cannot accidentally amplify the selected magnitude.
     */
    public static int signedSpawnOffset(int magnitude, int signRoll) {
        int distance = Math.max(0, magnitude);
        return distance * Integer.compare(signRoll, 0);
    }

    /**
     * TC4 accepted a spawn point from the candidate entity's real bounding
     * box: solid top surface below, no block/entity collision and no liquid.
     * It did not require a hard-coded two-block air column, which would reject
     * valid low-clearance positions for the 0.3-block-tall Mind Spider.
     */
    public static boolean acceptsEntitySpawnCandidate(
            boolean solidTopSurface, boolean collisionFree, boolean containsLiquid) {
        return solidTopSurface && collisionFree && !containsLiquid;
    }

    /**
     * Exact pure-math form of TC4 EntityUtils.isVisibleTo(0.75F, ...).
     *
     * <p>The legacy helper treated {@code 0.75} as the full cone aperture in
     * radians, compared the normalized vectors against {@code cos(aperture/2)},
     * and clipped the cone with a round cap by axial projection. It did not use
     * {@code dot >= 0.75} and did not impose a separate spherical-distance gate.</p>
     */
    public static boolean deathGazeConeContains(
            double targetX, double targetY, double targetZ,
            double lookX, double lookY, double lookZ,
            double range) {
        if (range <= 0.0D) {
            return false;
        }

        double targetMagnitude = Math.sqrt(targetX * targetX + targetY * targetY + targetZ * targetZ);
        double lookMagnitude = Math.sqrt(lookX * lookX + lookY * lookY + lookZ * lookZ);
        if (targetMagnitude <= 1.0E-12D || lookMagnitude <= 1.0E-12D) {
            return false;
        }

        double dot = targetX * lookX + targetY * lookY + targetZ * lookZ;
        double cosine = dot / targetMagnitude / lookMagnitude;
        if (!(cosine > Math.cos(DEATH_GAZE_APERTURE_RADIANS / 2.0D))) {
            return false;
        }

        double axialProjection = dot / lookMagnitude;
        return axialProjection < range;
    }

    public record SpreadResult(boolean infectious, int amplifier, boolean keepsDefaultCuratives) {
    }

    public record UnnaturalHungerReduction(int duration, int amplifier, boolean remainsActive) {
    }
}
