package com.darkifov.thaumcraft.blockentity;

/**
 * Dependency-free contract for the complete TC4 4.2.3.5 Hungry Chest cycle.
 *
 * <p>The constants and formulas are taken from {@code BlockChestHungry},
 * {@code TileChestHungry}, {@code BlockChestHungryRenderer},
 * {@code TileChestHungryRenderer}, {@code ConfigRecipes} and
 * {@code ConfigResearch}. Production code calls this class so the original
 * boundaries, lid motion and break-drop distribution are not duplicated.</p>
 */
public final class TC4HungryChestParity {
    public static final String CONTRACT_VERSION = "11.64.16";

    public static final int INVENTORY_SIZE = 27;
    public static final int MAX_STACK_SIZE = 64;

    public static final double HORIZONTAL_MIN = 1.0D / 16.0D;
    public static final double HORIZONTAL_MAX = 15.0D / 16.0D;
    public static final double OUTLINE_Y_MAX = 14.0D / 16.0D;
    public static final double COLLISION_Y_MAX = 15.0D / 16.0D;

    public static final float BLOCK_HARDNESS = 2.5F;
    /** setHardness(2.5F) in 1.7.10 raises resistance to hardness * 5. */
    public static final float BLOCK_EXPLOSION_RESISTANCE = 12.5F;

    public static final float LID_STEP = 0.1F;
    public static final float LID_CLOSE_SOUND_THRESHOLD = 0.5F;
    public static final int OPENERS_EVENT_ID = 1;
    public static final int EAT_EVENT_ID = 2;
    public static final int EAT_EVENT_DATA = 2;
    public static final float EAT_LID_NUDGE = 0.2F;

    public static final float CHEST_SOUND_VOLUME = 0.5F;
    public static final float CHEST_SOUND_PITCH_BASE = 0.9F;
    public static final float CHEST_SOUND_PITCH_RANGE = 0.1F;
    public static final float EAT_SOUND_VOLUME = 0.25F;
    public static final float EAT_SOUND_PITCH_RANGE = 0.2F;

    public static final int DROP_MIN = 10;
    public static final int DROP_RANDOM_BOUND = 21;
    public static final float DROP_POSITION_MIN = 0.1F;
    public static final float DROP_POSITION_RANGE = 0.8F;
    public static final float DROP_MOTION_SIGMA = 0.05F;
    public static final float DROP_MOTION_Y_BIAS = 0.2F;

    public static final int RECIPE_AER = 5;
    public static final int RECIPE_ORDO = 3;
    public static final int RECIPE_PERDITIO = 3;
    public static final int RESEARCH_FAMES = 3;
    public static final int RESEARCH_VACUOS = 3;
    public static final int RESEARCH_X = -1;
    public static final int RESEARCH_Y = 0;
    public static final int RESEARCH_COMPLEXITY = 1;

    private TC4HungryChestParity() {
    }

    public static float nextLidAngle(float current, int openers) {
        if ((openers == 0 && current > 0.0F) || (openers > 0 && current < 1.0F)) {
            current += openers > 0 ? LID_STEP : -LID_STEP;
        }
        if (current > 1.0F) {
            return 1.0F;
        }
        return Math.max(0.0F, current);
    }

    public static boolean shouldPlayOpenSound(int openers, float current) {
        return openers > 0 && current == 0.0F;
    }

    public static boolean shouldPlayCloseSound(float previous, float current) {
        return current < LID_CLOSE_SOUND_THRESHOLD && previous >= LID_CLOSE_SOUND_THRESHOLD;
    }

    public static float chestSoundPitch(float randomUnit) {
        return randomUnit * CHEST_SOUND_PITCH_RANGE + CHEST_SOUND_PITCH_BASE;
    }

    public static float eatSoundPitch(float firstRandomUnit, float secondRandomUnit) {
        return (firstRandomUnit - secondRandomUnit) * EAT_SOUND_PITCH_RANGE + 1.0F;
    }

    public static float easedLid(float lidAngle) {
        float inverse = 1.0F - lidAngle;
        return 1.0F - inverse * inverse * inverse;
    }

    public static int nextDropCount(int remaining, int randomValueZeroToTwenty) {
        int requested = randomValueZeroToTwenty + DROP_MIN;
        return Math.min(remaining, requested);
    }

    public static boolean intersectsCollision(double entityMinX, double entityMinY, double entityMinZ,
                                              double entityMaxX, double entityMaxY, double entityMaxZ,
                                              int blockX, int blockY, int blockZ) {
        double minX = blockX + HORIZONTAL_MIN;
        double minY = blockY;
        double minZ = blockZ + HORIZONTAL_MIN;
        double maxX = blockX + HORIZONTAL_MAX;
        double maxY = blockY + COLLISION_Y_MAX;
        double maxZ = blockZ + HORIZONTAL_MAX;
        return entityMaxX > minX && entityMinX < maxX
                && entityMaxY > minY && entityMinY < maxY
                && entityMaxZ > minZ && entityMinZ < maxZ;
    }
}
