package com.darkifov.thaumcraft.research;

/**
 * Source-linked contract for TC4 4.2.3.5 Research Note completion and
 * completed-discovery consumption.
 *
 * <p>Original sources:
 * ResearchManager.checkResearchCompletion (lines 255-286),
 * TileResearchTable (lines 191-196), and ItemResearchNotes (lines 72-92).
 * Completion itself consumes no extra ink: the accepted edit already consumed
 * it. A completed discovery always consumes one note, including creative mode,
 * and eligible siblings are completed after the target.</p>
 */
public final class TC4ResearchNoteCompletionParity {
    public static final String CONTRACT_VERSION = "11.64.01";
    public static final int ORIGINAL_DISCOVERY_DAMAGE = 64;
    public static final int COMPLETED_DISCOVERY_CONSUME_COUNT = 1;

    private TC4ResearchNoteCompletionParity() {
    }

    public static boolean acceptsCompletionContext(
            boolean menuOpen, boolean menuStillValid, boolean sameNoteIdentity) {
        return menuOpen && menuStillValid && sameNoteIdentity;
    }

    public static boolean canCommitCompletion(
            boolean alreadySolved, boolean exactSnapshot, boolean graphSolvedForPlayer) {
        return !alreadySolved && exactSnapshot && graphSolvedForPlayer;
    }

    public static boolean completionConsumesAdditionalInk() {
        return false;
    }

    public static boolean completedDiscoveryConsumedInCreative() {
        return true;
    }

    public static boolean shouldUnlockSibling(
            boolean targetUnlocked, boolean siblingAlreadyComplete, boolean siblingRequisitesMet) {
        return targetUnlocked && !siblingAlreadyComplete && siblingRequisitesMet;
    }

    public static int completedDiscoveryConsumeCount() {
        return COMPLETED_DISCOVERY_CONSUME_COUNT;
    }
}
