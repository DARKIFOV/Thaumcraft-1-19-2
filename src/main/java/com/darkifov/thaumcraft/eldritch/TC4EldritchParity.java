package com.darkifov.thaumcraft.eldritch;

/** v11.63.94 source-linked warp progression and Outer Lands contract. */
public final class TC4EldritchParity {
    public static final String CONTRACT_VERSION = "11.63.94";
    private TC4EldritchParity() {}

    public static boolean warpThresholdsAndCrimsonRitesMatchTc4() {
        return TC4EldritchProgression.BATHSALTS_WARP == 10
                && TC4EldritchProgression.ELDRITCH_MINOR_WARP == 25
                && TC4EldritchProgression.ELDRITCH_MAJOR_WARP == 50
                && TC4EldritchProgression.CRIMSON_RITES_STICKY_WARP == 1
                && TC4EldritchProgression.CRIMSON_RITES_ATTUNEMENT == 5;
    }

    /**
     * TC4 checkWarpEvent milestones: ELDRITCHMINOR (actual warp > 25) called
     * grantResearch(player, 10) and ELDRITCHMAJOR (actual warp > 50) called
     * grantResearch(player, 20), both without any chat line.
     */
    public static boolean eldritchMilestoneGrantsMatchTc4() {
        return TC4EldritchProgression.ELDRITCH_MINOR_RESEARCH_GRANTS == 10
                && TC4EldritchProgression.ELDRITCH_MAJOR_RESEARCH_GRANTS == 20;
    }

    public static boolean outerLandsConstantsMatchTc4() {
        return TC4OuterLandsDimensionParity.AVERAGE_GROUND_LEVEL == 50
                && !TC4OuterLandsDimensionParity.CAN_RESPAWN_HERE
                && !TC4OuterLandsDimensionParity.HAS_SKY_LIGHT
                && TC4OuterLandsDimensionParity.FOG_COLOR == 10518688
                && TC4OuterLandsDimensionAdapter.ORIGINAL_CELL_SIZE == 16;
    }
}
