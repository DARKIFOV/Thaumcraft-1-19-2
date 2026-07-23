package com.darkifov.thaumcraft.research;

/**
 * Source-linked contract for clearing placed Research Note hexes in TC4 4.2.3.5.
 *
 * <p>Original anchors:</p>
 * <ul>
 *   <li>GuiResearchTable.checkClickedHex: only type-2 placed hexes send a null-aspect clear request.</li>
 *   <li>TileResearchTable.placeAspect(null): one ink is consumed for every accepted clear.</li>
 *   <li>RESEARCHER1 refunds at roll &lt; 0.25; RESEARCHER2 refunds at roll &lt; 0.50.</li>
 *   <li>Creative mode has no implicit refund bypass in the original branch.</li>
 * </ul>
 */
public final class TC4ResearchNoteClearParity {
    public static final String CONTRACT_VERSION = "11.64.00";
    public static final int ORIGINAL_CLEARABLE_HEX_TYPE = ResearchNoteGrid.TYPE_PLACED;
    public static final int INK_PER_ACCEPTED_CLEAR = 1;

    private TC4ResearchNoteClearParity() {
    }

    public static boolean canClearHex(boolean active, int type, boolean hasAspect) {
        return active && type == ORIGINAL_CLEARABLE_HEX_TYPE && hasAspect;
    }

    public static boolean shouldRefundClearedAspect(
            boolean hasExpertise, boolean hasMastery, boolean creative, float roll) {
        // TC4 did not special-case creative here. Keep the parameter explicit so
        // future adapters cannot silently reintroduce the old port-only bypass.
        return TC4ResearchEfficiencyParity.clearedAspectRefund(
                hasExpertise, hasMastery, roll);
    }

    public static boolean creativeHasImplicitRefund() {
        return false;
    }

    public static boolean boundariesMatchTc4() {
        return shouldRefundClearedAspect(true, false, false, 0.249999F)
                && !shouldRefundClearedAspect(true, false, false, 0.25F)
                && shouldRefundClearedAspect(false, true, false, 0.499999F)
                && !shouldRefundClearedAspect(false, true, false, 0.50F)
                && !shouldRefundClearedAspect(false, false, true, 0.0F);
    }
}
