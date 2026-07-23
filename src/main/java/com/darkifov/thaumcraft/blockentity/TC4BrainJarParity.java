package com.darkifov.thaumcraft.blockentity;

/**
 * Dependency-free contract for the complete TC4 4.2.3.5 Brain in a Jar cycle.
 *
 * <p>Every constant and formula in this class is taken from BlockJar metadata 1,
 * TileJarBrain, TileJarRenderer, ModelBrain, ModelJar, ConfigRecipes and
 * ConfigResearch. Production code is required to call this class rather than
 * duplicating the values.</p>
 */
public final class TC4BrainJarParity {
    public static final String CONTRACT_VERSION = "11.64.15";

    public static final int MAX_XP = 2000;
    public static final int INTERACTION_EAT_DELAY_TICKS = 40;
    public static final int RANDOM_RELEASE_MAX_EXCLUSIVE = 64;
    public static final double SEARCH_RADIUS = 6.0D;
    public static final double PULL_DIVISOR = 7.0D;
    public static final double PULL_HORIZONTAL = 0.15D;
    public static final double PULL_VERTICAL = 0.33D;
    public static final double TOUCH_MIN = -0.1D;
    public static final double TOUCH_MAX = 1.1D;

    public static final int AMBIENT_INITIAL_DELAY_MILLIS = 1500;
    public static final int AMBIENT_BASE_DELAY_MILLIS = 5000;
    public static final int AMBIENT_RANDOM_BOUND_MILLIS = 25000;
    public static final float AMBIENT_VOLUME = 0.15F;
    public static final float AMBIENT_PITCH_BASE = 0.8F;
    public static final float AMBIENT_PITCH_RANGE = 0.4F;
    public static final float JAR_SHAKE_VOLUME = 0.2F;
    public static final float JAR_SHAKE_PITCH = 1.0F;
    public static final float EAT_VOLUME = 0.1F;

    public static final float BLOCK_HARDNESS = 0.3F;
    public static final float BLOCK_EXPLOSION_RESISTANCE = 0.0F;
    public static final int BLOCK_LIGHT_LEVEL = 9;
    public static final float ENCHANT_POWER_BONUS = 2.0F;
    public static final int ITEM_MAX_STACK = 64;
    public static final int ITEM_WARP = 1;
    public static final int RESEARCH_WARP = 3;

    public static final int RECIPE_INSTABILITY = 4;
    public static final int RECIPE_COGNITIO = 10;
    public static final int RECIPE_SENSUS = 10;
    public static final int RECIPE_EXANIMIS = 20;
    public static final int RESEARCH_FAMES = 3;
    public static final int RESEARCH_COGNITIO = 3;
    public static final int RESEARCH_EXANIMIS = 3;
    public static final int RESEARCH_LUCRUM = 3;
    public static final int RESEARCH_X = -5;
    public static final int RESEARCH_Y = 9;
    public static final int RESEARCH_COMPLEXITY = 2;

    public static final double FULL_PARTICLE_XZ_MIN = 0.3D;
    public static final double FULL_PARTICLE_XZ_RANGE = 0.4D;
    public static final double FULL_PARTICLE_Y = 0.9D;
    public static final float FULL_PARTICLE_ALPHA = 0.5F;
    public static final float FULL_PARTICLE_RED = 0.0F;
    public static final float FULL_PARTICLE_GREEN_MIN = 0.4F;
    public static final float FULL_PARTICLE_GREEN_RANGE = 0.1F;
    public static final float FULL_PARTICLE_BLUE_MIN = 0.3F;
    public static final float FULL_PARTICLE_BLUE_RANGE = 0.2F;

    public static final float BRAIN_BOB_BASE = 0.03F;
    public static final float BRAIN_BOB_AMPLITUDE = 0.03F;
    public static final float BRAIN_BOB_DIVISOR = 14.0F;
    public static final float BRAIN_RENDER_SCALE = 0.4F;
    public static final float BRAIN_Y_OFFSET = -0.8F;
    public static final float ROTATION_LERP = 0.04F;
    public static final float IDLE_ROTATION_STEP = 0.01F;

    private TC4BrainJarParity() {
    }

    /** Original tick-start upper clamp. Negative corrupted values are intentionally not rewritten. */
    public static int clampAtTickStart(int xp) {
        return xp > MAX_XP ? MAX_XP : xp;
    }

    public static boolean mayAttract(int xp, int eatDelay) {
        return xp < MAX_XP && eatDelay == 0;
    }

    public static boolean mayAbsorb(int xp, int eatDelay) {
        return xp < MAX_XP && eatDelay == 0;
    }

    /** Exact nextInt bound used by BlockJar activation. */
    public static int randomReleaseBound(int xp) {
        return Math.min(xp + 1, RANDOM_RELEASE_MAX_EXCLUSIVE);
    }

    public static int comparatorOutput(int xp) {
        return (int) Math.floor((xp / (float) MAX_XP) * 14.0F) + (xp > 0 ? 1 : 0);
    }

    /** Returns the exact acceleration added to an XP orb by TileJarBrain. */
    public static Pull pull(double centerX, double centerY, double centerZ,
                            double orbX, double orbY, double orbZ) {
        double dx = (centerX - orbX) / PULL_DIVISOR;
        double dy = (centerY - orbY) / PULL_DIVISOR;
        double dz = (centerZ - orbZ) / PULL_DIVISOR;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        double strength = 1.0D - distance;
        if (strength <= 0.0D || distance == 0.0D) {
            return Pull.ZERO;
        }
        strength *= strength;
        return new Pull(dx / distance * strength * PULL_HORIZONTAL,
                dy / distance * strength * PULL_VERTICAL,
                dz / distance * strength * PULL_HORIZONTAL);
    }

    public static float fullParticleGreen(float randomUnit) {
        return FULL_PARTICLE_GREEN_MIN + randomUnit * FULL_PARTICLE_GREEN_RANGE;
    }

    public static float fullParticleBlue(float randomUnit) {
        return FULL_PARTICLE_BLUE_MIN + randomUnit * FULL_PARTICLE_BLUE_RANGE;
    }

    public static float bob(int playerTickCount) {
        return (float) Math.sin(playerTickCount / BRAIN_BOB_DIVISOR)
                * BRAIN_BOB_AMPLITUDE + BRAIN_BOB_BASE;
    }

    public record Pull(double x, double y, double z) {
        public static final Pull ZERO = new Pull(0.0D, 0.0D, 0.0D);
    }
}
