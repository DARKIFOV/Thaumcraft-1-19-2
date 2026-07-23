package com.darkifov.thaumcraft.taint;

import com.darkifov.thaumcraft.ThaumcraftMod;

/** v11.63.94 source-linked TC4 taint death-conversion and spread contract. */
public final class TC4TaintParity {
    public static final String CONTRACT_VERSION = "11.63.94";
    public static final int SPECIFIC_DEATH_CONVERSION_COUNT = 6;

    private TC4TaintParity() {}

    public static boolean deathConversionTableMatchesTc4() {
        return SPECIFIC_DEATH_CONVERSION_COUNT == 6
                && ThaumcraftMod.TAINT_CREEPER != null
                && ThaumcraftMod.TAINT_SHEEP != null
                && ThaumcraftMod.TAINT_COW != null
                && ThaumcraftMod.TAINT_PIG != null
                && ThaumcraftMod.TAINT_CHICKEN != null
                && ThaumcraftMod.TAINT_VILLAGER != null
                && ThaumcraftMod.THAUMIC_SLIME != null;
    }

    public static boolean spreadConstantsMatchTc4() {
        return TaintSpreadRuntime.ORIGINAL_HORIZONTAL_OFFSET_BOUND == 3
                && TaintSpreadRuntime.ORIGINAL_VERTICAL_OFFSET_BOUND == 5
                && TaintSpreadRuntime.ORIGINAL_VERTICAL_OFFSET_SHIFT == 3
                && TaintSpreadRuntime.ORIGINAL_SPORE_ROLL_BOUND == 200;
    }
}
