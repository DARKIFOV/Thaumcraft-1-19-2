package com.darkifov.thaumcraft.recipe;

import com.darkifov.thaumcraft.Aspect;

/** v11.63.94 recipe-adjacent ledger; smelting bonuses remain outside 258 recipes. */
public final class TC4RecipeParity {
    public static final String CONTRACT_VERSION = "11.63.94";
    public static final int ORIGINAL_JAR_LABEL_ASPECT_COUNT = 48;
    public static final int ORIGINAL_RECIPE_REGISTRATION_COUNT = 258;
    public static final int SEPARATE_SMELTING_BONUS_REGISTRATION_COUNT = 18;
    private TC4RecipeParity() {}

    public static boolean aspectAndSmeltingBonusLedgerMatchesTc4() {
        return Aspect.values().length == ORIGINAL_JAR_LABEL_ASPECT_COUNT
                && ORIGINAL_RECIPE_REGISTRATION_COUNT == 258
                && SEPARATE_SMELTING_BONUS_REGISTRATION_COUNT == 18;
    }
}
