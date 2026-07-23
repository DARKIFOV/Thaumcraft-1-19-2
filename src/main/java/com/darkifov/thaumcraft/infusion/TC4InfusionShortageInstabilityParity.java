package com.darkifov.thaumcraft.infusion;

/**
 * v11.63.65: source-of-truth ledger for TC4 TileInfusionMatrix.craftCycle
 * shortage handling (lines 415-490 of the original 1.7.10 tile).
 *
 * <p>The original matrix never cancels a locked craft just because essentia or
 * a component is briefly unavailable -- it waits and keeps rolling small,
 * precisely-scaled instability creep every cycle until the shortage clears:</p>
 *
 * <ul>
 *   <li><b>Enchantment XP shortage</b> (recipeType == 1, recipeXP &gt; 0): once
 *       every pending player is drained, a 1-in-3 chance ({@link #XP_REFUND_GATE_BOUND})
 *       refunds one essentia point, gated further by
 *       {@link TC4InfusionRuntime#failedComponentRollBound(int)} (50 - instability*2)
 *       for the instability creep itself.</li>
 *   <li><b>Essentia shortage</b> (recipeEssentia non-empty): TC4 attempts to
 *       drain <i>every</i> pending aspect in the same tick, not only the first
 *       one -- it only stops once one aspect actually drains. Each aspect that
 *       fails to drain rolls its own independent
 *       {@link TC4InfusionRuntime#failedEssentiaRollBound(int)} (100 -
 *       instability*3) instability-creep chance. Once every pending aspect has
 *       failed this cycle, TC4 forces an immediate surroundings re-scan
 *       ({@code checkSurroundings = true}) -- component shortage does not do
 *       this.</li>
 *   <li><b>Component shortage</b> (recipeIngredients non-empty): TC4 rolls the
 *       essentia-refund gate once per unmatched ingredient index inside the
 *       same loop, using {@link TC4InfusionRuntime#componentShortageEssentiaRefundRollBound(int)}
 *       (nextInt(1 + a)). The first unmatched ingredient in a cycle (a == 0)
 *       always refunds; the odds shrink for later unmatched indices reached
 *       within that same cycle. The instability-creep roll
 *       (50 - instability*2) is nested <i>inside</i> that refund gate, not
 *       independent of it.</li>
 * </ul>
 *
 * <p>None of these three shortage branches abort the craft, clear the locked
 * recipe/catalyst, or grant an automatic warp/failure event -- they simply
 * re-arm a short retry delay and wait for the next cycle.</p>
 */
public final class TC4InfusionShortageInstabilityParity {
    public static final int XP_REFUND_GATE_BOUND = 3;

    private TC4InfusionShortageInstabilityParity() {
    }

    public static boolean essentiaShortageCancelsCraft() {
        return false;
    }

    public static boolean componentShortageCancelsCraft() {
        return false;
    }

    public static boolean essentiaShortageAttemptsEveryPendingAspectPerCycle() {
        return true;
    }

    public static boolean essentiaShortageForcesSurroundingsResurvey() {
        return true;
    }

    public static boolean componentShortageForcesSurroundingsResurvey() {
        return false;
    }

    /** nextInt(1 + a): a == 0 (first unmatched ingredient this cycle) always refunds. */
    public static boolean componentShortageFirstUnmatchedIngredientAlwaysRefunds() {
        return TC4InfusionRuntime.componentShortageEssentiaRefundRollBound(0) == 1;
    }

    public static int componentShortageRefundRollBoundForIndex(int ingredientIndex) {
        return TC4InfusionRuntime.componentShortageEssentiaRefundRollBound(ingredientIndex);
    }

    public static int essentiaShortageInstabilityRollBound(int recipeInstability) {
        return TC4InfusionRuntime.failedEssentiaRollBound(recipeInstability);
    }

    public static int componentShortageInstabilityRollBound(int recipeInstability) {
        return TC4InfusionRuntime.failedComponentRollBound(recipeInstability);
    }
}
