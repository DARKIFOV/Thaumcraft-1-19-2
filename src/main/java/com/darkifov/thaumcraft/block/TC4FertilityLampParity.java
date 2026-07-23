package com.darkifov.thaumcraft.block;

/** Dependency-free source contract for TC4 TileArcaneLampFertility. */
public final class TC4FertilityLampParity {
    public static final String CONTRACT_VERSION = "11.64.30";
    public static final String RESEARCH = "LAMPFERTILITY";
    public static final String NBT_ORIENTATION = "orientation";
    public static final String NBT_CHARGES = "charges";

    public static final int RADIUS = 7;
    public static final int MAX_CHARGES = 4;
    public static final int BREEDING_COST = 2;
    public static final int BREEDING_INTERVAL_TICKS = 300;
    public static final int DRAW_INTERVAL_TICKS = 5;
    public static final int SUCTION_BASE = 128;
    public static final int SUCTION_PER_CHARGE = 10;
    public static final int MAX_EXISTING_SAME_CLASS = 7;
    public static final int ACTIVE_LIGHT = 15;
    public static final int INACTIVE_LIGHT = 8;
    public static final float BLOCK_HARDNESS = 3.0F;
    public static final float BLOCK_RESISTANCE = 17.0F;

    private TC4FertilityLampParity() {}

    public static boolean isActive(int charges) { return charges > 0; }
    public static boolean canDraw(int charges) { return charges < MAX_CHARGES; }
    public static boolean canBreed(int charges) { return charges > 1; }
    public static boolean isBreedingTick(int counter) { return counter % BREEDING_INTERVAL_TICKS == 0; }
    public static boolean eligibleAnimal(int age, boolean inLove) { return age == 0 && !inLove; }
    public static boolean populationAllowed(int exactClassCount) { return exactClassCount <= MAX_EXISTING_SAME_CLASS; }
    public static int suction(int charges, boolean requestedFace) {
        return requestedFace ? Math.max(0, SUCTION_BASE - charges * SUCTION_PER_CHARGE) : 0;
    }
}
