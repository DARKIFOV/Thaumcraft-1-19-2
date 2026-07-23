package com.darkifov.thaumcraft.alchemy;

/** Dependency-free formulas and constants copied from TC4 4.2.3.5 TileAlchemyFurnace/TileAlembic. */
public final class TC4AlchemicalFurnaceParity {
    public static final String CONTRACT_VERSION = "11.64.37";
    public static final int FURNACE_CAPACITY = 50;
    public static final int ALEMBIC_CAPACITY = 32;
    public static final int MAX_ALEMBICS = 5;
    public static final int NORMAL_INTERVAL = 40;
    public static final int ALUMENTUM_INTERVAL = 20;
    public static final int DISTILLATION_STEP = 1;
    public static final int ALUMENTUM_BURN_TIME = 6400;
    public static final int BURNING_LIGHT = 12;
    public static final int BASE_SMELT_TICKS_PER_VIS = 10;
    public static final double BELLOWS_REDUCTION = 0.125D;
    public static final int MAX_BELLOWS = 6;

    private TC4AlchemicalFurnaceParity() {}

    public static int smeltTime(int vis, int bellows) {
        int safeBellows = Math.max(0, Math.min(MAX_BELLOWS, bellows));
        return Math.max(1, (int) (Math.max(0, vis) * BASE_SMELT_TICKS_PER_VIS
                * (1.0D - BELLOWS_REDUCTION * safeBellows)));
    }

    public static int distillationInterval(boolean speedBoost) {
        return speedBoost ? ALUMENTUM_INTERVAL : NORMAL_INTERVAL;
    }

    /** Returns the original localized fill-message suffix 1..5. */
    public static int alembicFillMessage(int amount) {
        int safe = Math.max(0, Math.min(ALEMBIC_CAPACITY, amount));
        if (safe == 0) return 1;
        if (safe < ALEMBIC_CAPACITY * 0.4D) return 2;
        if (safe < ALEMBIC_CAPACITY * 0.8D) return 3;
        if (safe < ALEMBIC_CAPACITY) return 4;
        return 5;
    }

    public static int comparator(int amount) {
        if (amount <= 0) return 0;
        return Math.min(15, (int) Math.floor((amount / (double) ALEMBIC_CAPACITY) * 14.0D) + 1);
    }
}
