package com.darkifov.thaumcraft.research;

/** Pure constants/invariants for TC4 4.2.3.5 research-system full closure. */
public final class TC4ResearchSystemFullClosureParity {
    public static final String CONTRACT_VERSION = "11.64.27";
    public static final int ORIGINAL_RESEARCH_COUNT = 201;
    public static final int PRIMAL_DISCOVERY_POOL = 0;
    public static final int RESEARCH_TABLE_SLOT_COUNT = 2;
    public static final int SCRIBING_TOOLS_SLOT = 0;
    public static final int RESEARCH_NOTE_SLOT = 1;
    public static final int NOTE_PAPER_COST = 1;
    public static final int NOTE_INK_COST = 1;
    public static final int DUPLICATE_PAPER_COST = 1;
    public static final int DUPLICATE_INK_SAC_COST = 1;
    public static final int MAX_COMPLEXITY = 3;
    public static final int MIN_NOTE_RADIUS = 2;
    public static final int MAX_NOTE_RADIUS = 4;
    public static final int BONUS_RECALCULATE_THRESHOLD = 600;
    public static final int BONUS_SCAN_RADIUS = 8;
    public static final boolean CREATIVE_BYPASSES_RESEARCH_COSTS = false;
    public static final boolean TABLE_BUTTON_CREATES_NOTE = false;
    public static final boolean TABLE_BUTTON_COMPLETES_NOTE = false;
    public static final boolean EXISTING_NOTE_PREVENTS_NEW_COST = true;
    public static final boolean DUPLICATE_RESPECTS_ITEM_MAX_STACK = false;

    private TC4ResearchSystemFullClosureParity() {}

    public static int noteRadius(int complexity) {
        return 1 + Math.min(MAX_COMPLEXITY, Math.max(1, complexity));
    }

    public static int duplicateAspectCost(int researchTagAmount, int copiesAlreadyMade) {
        return Math.max(0, researchTagAmount) + Math.max(0, copiesAlreadyMade);
    }

    public static boolean primalsStartKnownButEmpty() {
        return PRIMAL_DISCOVERY_POOL == 0;
    }

    public static boolean tableSlotsMatchOriginal() {
        return RESEARCH_TABLE_SLOT_COUNT == 2 && SCRIBING_TOOLS_SLOT == 0 && RESEARCH_NOTE_SLOT == 1;
    }

    public static boolean noteCreationAndLearningPathsMatchOriginal() {
        return !CREATIVE_BYPASSES_RESEARCH_COSTS
                && !TABLE_BUTTON_CREATES_NOTE
                && !TABLE_BUTTON_COMPLETES_NOTE
                && EXISTING_NOTE_PREVENTS_NEW_COST
                && !DUPLICATE_RESPECTS_ITEM_MAX_STACK;
    }
}
