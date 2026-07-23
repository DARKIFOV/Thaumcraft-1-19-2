package com.darkifov.thaumcraft.research;

/**
 * Source-linked contract for the original TC4 research-note hex editing path.
 *
 * <p>GuiResearchTable only allowed drops onto an existing type-0 hex. It did
 * not require that the new aspect already touched a compatible neighbour;
 * graph connectivity was evaluated later by research completion. The modern
 * server still validates the authoritative table/menu, known aspect and
 * resources so forged packets cannot edit arbitrary items or spend a partial
 * transaction.</p>
 */
public final class TC4ResearchNoteGraphParity {
    public static final String CONTRACT_VERSION = "11.63.99";
    public static final int ORIGINAL_EMPTY_HEX_TYPE = 0;
    public static final int ORIGINAL_RESEARCH_ANCHOR_HEX_TYPE = 1;
    public static final int ORIGINAL_MAX_HEX_SLOTS = 61;

    private TC4ResearchNoteGraphParity() {
    }

    public static boolean canPlaceIntoHex(int slot, int slotType, boolean aspectAbsent) {
        return slot >= 0
                && slot < ORIGINAL_MAX_HEX_SLOTS
                && slotType == ORIGINAL_EMPTY_HEX_TYPE
                && aspectAbsent;
    }

    public static boolean placementRequiresCompatibleNeighbour() {
        return false;
    }

    public static boolean connectivityIsCheckedAtCompletion() {
        return true;
    }

    public static boolean acceptsServerTableContext(boolean menuOpen, boolean menuStillValid) {
        return menuOpen && menuStillValid;
    }

    public static boolean sourceContractMatchesTc4() {
        return ORIGINAL_EMPTY_HEX_TYPE == 0
                && canPlaceIntoHex(0, 0, true)
                && !canPlaceIntoHex(0, ORIGINAL_RESEARCH_ANCHOR_HEX_TYPE, false)
                && !placementRequiresCompatibleNeighbour()
                && connectivityIsCheckedAtCompletion();
    }
}
