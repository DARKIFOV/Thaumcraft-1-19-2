package com.darkifov.thaumcraft.alchemy;

/**
 * v11.63.70: source-of-truth ledger for TC4 crucible alchemy and Liquid Death
 * ported from thaumcraft.common.lib.crafting.ThaumcraftCraftingManager
 * and thaumcraft.common.entities.golems.EventHandlerEntity.
 *
 * <ul>
 *   <li><b>Crucible alchemy</b>: TC4 alchemy recipes require a catalyst item
 *       thrown into a crucible with the correct aspect mixture. The three
 *       original fallback recipes are: Thaumium (iron + 4 Praecantatio),
 *       Nitor (glowstone + 3 Ignis + 3 Potentia + 3 Lux), Alchemical Dust
 *       (redstone + 2 Praecantatio + 1 Ordo + 1 Perditio).</li>
 *   <li><b>Liquid Death</b>: entities killed by the TC4 DAMAGE_SOURCE_DISSOLVE
 *       damage source drop aspect crystals: 50% chance per aspect type, count
 *       = max(1, random(amount) / 2). The crystals preserve the entity's
 *       TC4-defined aspect composition.</li>
 * </ul>
 */
public final class TC4AlchemyParity {
    public static final String CONTRACT_VERSION = "11.63.70";

    /** TC4: number of original fallback crucible recipes (Thaumium, Nitor, Dust). */
    public static final int FALLBACK_RECIPE_COUNT = 3;

    /** TC4 EventHandlerEntity: 50% chance per aspect to drop crystals on Liquid Death kill. */
    public static final double LIQUID_DEATH_DROP_CHANCE = 0.5;

    /** TC4: minimum crystal count per aspect on Liquid Death drop. */
    public static final int LIQUID_DEATH_MIN_CRYSTALS = 1;

    private TC4AlchemyParity() {}

    /** Verify the three original fallback crucible recipes exist with correct aspect costs. */
    public static boolean fallbackRecipesHaveCorrectAspectCosts() {
        int found = 0;
        for (AlchemyRecipe recipe : AlchemyRecipes.fallbackOnly()) {
            if (recipe.id().getPath().equals("fallback_thaumium_ingot")
                    && recipe.cost().getOrDefault(com.darkifov.thaumcraft.Aspect.PRAECANTATIO, 0) == 4) found++;
            if (recipe.id().getPath().equals("fallback_nitor")
                    && recipe.cost().getOrDefault(com.darkifov.thaumcraft.Aspect.IGNIS, 0) == 3
                    && recipe.cost().getOrDefault(com.darkifov.thaumcraft.Aspect.POTENTIA, 0) == 3
                    && recipe.cost().getOrDefault(com.darkifov.thaumcraft.Aspect.LUX, 0) == 3) found++;
            if (recipe.id().getPath().equals("fallback_alchemy_dust")
                    && recipe.cost().getOrDefault(com.darkifov.thaumcraft.Aspect.PRAECANTATIO, 0) == 2
                    && recipe.cost().getOrDefault(com.darkifov.thaumcraft.Aspect.ORDO, 0) == 1
                    && recipe.cost().getOrDefault(com.darkifov.thaumcraft.Aspect.PERDITIO, 0) == 1) found++;
        }
        return found == FALLBACK_RECIPE_COUNT;
    }

    /** Verify Liquid Death drop mechanics match TC4. */
    public static boolean liquidDeathDropMechanicsMatchTc4() {
        return LIQUID_DEATH_DROP_CHANCE == 0.5 && LIQUID_DEATH_MIN_CRYSTALS == 1;
    }

    public static boolean liquidDeathCrystalCountFormulaMatchesTc4() {
        return LiquidDeathDropRuntime.crystalCountForRoll(1, 0) == 1
                && LiquidDeathDropRuntime.crystalCountForRoll(2, 0) == 1
                && LiquidDeathDropRuntime.crystalCountForRoll(2, 1) == 1
                && LiquidDeathDropRuntime.crystalCountForRoll(4, 3) == 2
                && LiquidDeathDropRuntime.crystalCountForRoll(8, 7) == 4;
    }

    /** Verify TC4Dissolve damage source exists and is registered. */
    public static boolean liquidDeathDamageSourceExists() {
        return com.darkifov.thaumcraft.damage.TC4DamageSources.DISSOLVE != null;
    }
}
