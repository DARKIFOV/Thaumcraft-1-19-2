package com.darkifov.thaumcraft.research;

/**
 * Source-linked behavioral contract for TC4 4.2.3.5 TileResearchTable.
 *
 * <p>Original anchors:</p>
 * <ul>
 *   <li>updateEntity uses post-increment semantics: {@code nextRecalc++ > 600}.</li>
 *   <li>writeCustomNBT emits at most one {@code bonusAspects} entry per positive aspect type.</li>
 *   <li>readCustomNBT reconstructs one point for each serialized aspect entry.</li>
 * </ul>
 */
public final class TC4ResearchTableBehaviorParity {
    public static final String CONTRACT_VERSION = "11.63.97";

    public static final String NEXT_RECALC_TAG = "nextRecalc";
    public static final String BONUS_ASPECTS_TAG = "bonusAspects";
    public static final String BONUS_ASPECT_TAG = "tag";

    private TC4ResearchTableBehaviorParity() {
    }

    /** Evaluates the original comparison before the post-increment side effect. */
    public static boolean shouldRecalculate(int counterBeforeTick) {
        return counterBeforeTick > TC4ResearchEfficiencyParity.BONUS_RECALCULATE_THRESHOLD_TICKS;
    }

    /** Mirrors {@code nextRecalc++ > 600}: reset on a firing tick, increment otherwise. */
    public static int counterAfterTick(int counterBeforeTick) {
        return shouldRecalculate(counterBeforeTick) ? 0 : counterBeforeTick + 1;
    }

    /** TC4 writes one NBT list record per positive aspect type, not per stored point. */
    public static int serializedCopiesForAmount(int amount) {
        return amount > 0 ? 1 : 0;
    }

    /** Normalizes duplicate entries from older port saves to TC4's one-point-per-type persistence. */
    public static boolean shouldLoadSerializedType(boolean alreadyLoaded) {
        return !alreadyLoaded;
    }

    public static boolean cadenceBoundaryMatchesTc4() {
        return !shouldRecalculate(600)
                && counterAfterTick(600) == 601
                && shouldRecalculate(601)
                && counterAfterTick(601) == 0;
    }

    public static boolean persistenceCardinalityMatchesTc4() {
        return serializedCopiesForAmount(0) == 0
                && serializedCopiesForAmount(1) == 1
                && serializedCopiesForAmount(7) == 1
                && shouldLoadSerializedType(false)
                && !shouldLoadSerializedType(true);
    }
}
