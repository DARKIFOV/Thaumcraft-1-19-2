package com.darkifov.thaumcraft.research;

/**
 * Source-linked contract for TC4 4.2.3.5 research-completion warp.
 *
 * <p>ResearchManager.completeResearch split every warp value above one into
 * permanent and sticky buckets: {@code sticky = warp / 2} and
 * {@code permanent = warp - sticky}. A one-point research grant remained
 * entirely permanent.</p>
 */
public final class TC4ResearchCompletionWarpParity {
    public static final String CONTRACT_VERSION = "11.64.02";

    private TC4ResearchCompletionWarpParity() {
    }

    public record WarpSplit(int permanent, int sticky) {
        public WarpSplit {
            permanent = Math.max(0, permanent);
            sticky = Math.max(0, sticky);
        }

        public int total() {
            return permanent + sticky;
        }
    }

    public static WarpSplit splitResearchWarp(int rawWarp) {
        int warp = Math.max(0, rawWarp);
        if (warp <= 1) {
            return new WarpSplit(warp, 0);
        }
        int sticky = warp / 2;
        return new WarpSplit(warp - sticky, sticky);
    }
}
