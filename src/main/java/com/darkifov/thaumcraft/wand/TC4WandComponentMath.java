package com.darkifov.thaumcraft.wand;

/** Dependency-free formulas used by the real rod/cap production path. */
public final class TC4WandComponentMath {
    public static final String CONTRACT_VERSION = "11.64.32";
    public static final int NORMAL_CAPACITY_MULTIPLIER = 100;
    public static final int SCEPTRE_CAPACITY_MULTIPLIER = 150;
    public static final float SCEPTRE_DISCOUNT = 0.1F;
    public static final float MINIMUM_COST_MODIFIER = 0.1F;
    public static final int REGEN_THRESHOLD_DIVISOR = 10;
    public static final int ONE_VIS_CENTIVIS = 100;

    private TC4WandComponentMath() {}

    public static int capacityCentivis(int rodCapacity, boolean sceptre) {
        if (rodCapacity < 0) throw new IllegalArgumentException("rodCapacity");
        return Math.multiplyExact(rodCapacity, sceptre ? SCEPTRE_CAPACITY_MULTIPLIER : NORMAL_CAPACITY_MULTIPLIER);
    }

    public static float consumptionModifier(float capModifier, boolean sceptre) {
        float adjusted = sceptre ? capModifier - SCEPTRE_DISCOUNT : capModifier;
        return Math.max(adjusted, MINIMUM_COST_MODIFIER);
    }

    public static int regenerationThresholdCentivis(int maximumCentivis) {
        return maximumCentivis / REGEN_THRESHOLD_DIVISOR;
    }

    public static int regenerationDeltaCentivis(int displayedVis) {
        return Math.multiplyExact(displayedVis, ONE_VIS_CENTIVIS);
    }
}
