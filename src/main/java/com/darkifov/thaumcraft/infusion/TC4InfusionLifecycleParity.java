package com.darkifov.thaumcraft.infusion;

/**
 * Small deterministic contract for the original TC4 Infusion Matrix lifecycle.
 *
 * <p>The original matrix rolls one weighted instability event when the locked
 * catalyst changes, then cancels the craft.  It does not add a second generic
 * failure-warp award.  Successful completion likewise has no unconditional
 * warp award.  Missing pedestal components leave the process waiting.</p>
 */
public final class TC4InfusionLifecycleParity {
    public static final int INVALID_CATALYST_EVENT_ROLLS = 1;

    private TC4InfusionLifecycleParity() {
    }

    public static boolean grantsAutomaticWarpOnSuccess() {
        return false;
    }

    public static boolean grantsTerminalWarpOnInvalidCatalyst() {
        return false;
    }

    public static boolean missingComponentCancelsCraft() {
        return false;
    }
}
