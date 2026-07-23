package com.darkifov.thaumcraft.research;

/**
 * Source-linked contract for TC4 4.2.3.5 Research Expertise/Mastery and
 * TileResearchTable bonus cadence.
 *
 * <p>Original anchors:</p>
 * <ul>
 *   <li>TileResearchTable.placeAspect: RESEARCHER2 free placement when roll &lt; 0.10.</li>
 *   <li>TileResearchTable.placeAspect: RESEARCHER1 clear refund when roll &lt; 0.25.</li>
 *   <li>TileResearchTable.placeAspect: RESEARCHER2 clear refund when roll &lt; 0.50.</li>
 *   <li>TileResearchTable.updateEntity/recalculateBonus: counter &gt; 600 and radius 8.</li>
 * </ul>
 */
public final class TC4ResearchEfficiencyParity {
    public static final String CONTRACT_VERSION = "11.63.96";

    public static final float MASTERY_FREE_PLACEMENT_CHANCE = 0.10F;
    public static final float EXPERTISE_CLEAR_REFUND_CHANCE = 0.25F;
    public static final float MASTERY_CLEAR_REFUND_CHANCE = 0.50F;

    public static final int BONUS_RECALCULATE_THRESHOLD_TICKS = 600;
    public static final int BONUS_SCAN_RADIUS = 8;

    private TC4ResearchEfficiencyParity() {
    }

    public static boolean masteryFreePlacement(boolean hasMastery, float roll) {
        return hasMastery && roll >= 0.0F && roll < MASTERY_FREE_PLACEMENT_CHANCE;
    }

    public static boolean clearedAspectRefund(boolean hasExpertise, boolean hasMastery, float roll) {
        if (roll < 0.0F) {
            return false;
        }
        if (hasMastery) {
            return roll < MASTERY_CLEAR_REFUND_CHANCE;
        }
        return hasExpertise && roll < EXPERTISE_CLEAR_REFUND_CHANCE;
    }

    /** Free Mastery placement is decided before checking player/table aspect stock. */
    public static boolean placementNeedsAspectSource(boolean freePlacement) {
        return !freePlacement;
    }

    public static boolean probabilityBoundariesMatchTc4() {
        return masteryFreePlacement(true, 0.0F)
                && masteryFreePlacement(true, 0.099999F)
                && !masteryFreePlacement(true, 0.10F)
                && !masteryFreePlacement(false, 0.0F)
                && clearedAspectRefund(true, false, 0.249999F)
                && !clearedAspectRefund(true, false, 0.25F)
                && clearedAspectRefund(false, true, 0.499999F)
                && !clearedAspectRefund(false, true, 0.50F);
    }

    public static boolean freePlacementOrderingMatchesTc4() {
        boolean free = masteryFreePlacement(true, 0.0F);
        return free && !placementNeedsAspectSource(free)
                && placementNeedsAspectSource(masteryFreePlacement(true, 0.10F));
    }

    public static boolean bonusCadenceAndRadiusMatchTc4() {
        return BONUS_RECALCULATE_THRESHOLD_TICKS == 600 && BONUS_SCAN_RADIUS == 8;
    }
}
