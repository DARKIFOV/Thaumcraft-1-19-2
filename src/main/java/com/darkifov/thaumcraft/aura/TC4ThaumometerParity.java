package com.darkifov.thaumcraft.aura;

/** Dependency-free transcription of ItemThaumometer and ScanManager constants/formulas. */
public final class TC4ThaumometerParity {
    public static final String CONTRACT_VERSION = "11.64.25";
    public static final int MAX_STACK_SIZE = 1;
    public static final int USE_DURATION_TICKS = 25;
    public static final int COMPLETION_REMAINING_TICKS = 5;
    public static final int REQUIRED_STABLE_TICKS = USE_DURATION_TICKS - COMPLETION_REMAINING_TICKS;
    public static final int CAMERA_TICK_INTERVAL = 2;
    public static final float CAMERA_TICK_VOLUME = 0.20F;
    public static final float CAMERA_TICK_PITCH_BASE = 0.45F;
    public static final float CAMERA_TICK_PITCH_RANDOM_SPAN = 0.10F;
    public static final double ENTITY_SCAN_RANGE = 10.0D;
    public static final double ENTITY_TARGET_EXPAND = 0.5D;
    public static final int ASPECT_TOTAL_CAP = 100;
    public static final int ASPECT_HARD_CAP = 125;
    public static final double NODE_VIEW_DISTANCE = 48.0D;
    public static final float NODE_VIEW_CONE_DOT = 0.44F;
    public static final int NODE_TYPE_TEXT_COLOR = 15642134;
    public static final float TITLE_BASE_SCALE = 0.005F;
    public static final float TITLE_SHRINK_PER_PIXEL = 0.000025F;
    public static final int TITLE_SHRINK_THRESHOLD = 90;
    public static final int MAX_RENDERED_ASPECTS = 15;

    public enum TargetPriority { NONE, BLOCK, ENTITY }
    private TC4ThaumometerParity() {}

    public static int remainingUseTicks(long elapsedTicks) {
        return (int)Math.max(0L, USE_DURATION_TICKS - Math.max(0L, elapsedTicks));
    }
    public static boolean shouldCompleteAfterElapsed(long elapsedTicks) {
        return remainingUseTicks(elapsedTicks) <= COMPLETION_REMAINING_TICKS;
    }
    public static boolean shouldPlayCameraTickAfterElapsed(long elapsedTicks) {
        int remaining=remainingUseTicks(elapsedTicks);
        return remaining>COMPLETION_REMAINING_TICKS && remaining%CAMERA_TICK_INTERVAL==0;
    }
    public static boolean mayStartHandheldScan(boolean alreadyScannedByAnyPath){return !alreadyScannedByAnyPath;}
    public static TargetPriority targetPriority(boolean entityAvailable, boolean blockAvailable){return entityAvailable?TargetPriority.ENTITY:(blockAvailable?TargetPriority.BLOCK:TargetPriority.NONE);}

    /** checkAndSyncAspectKnowledge: discovery bonus first, then the 100/125 pool caps. */
    public static int cappedAspectReward(int currentPool, int rawAmount, boolean discoveredNow) {
        int amount=Math.max(0,rawAmount)+(discoveredNow?2:0);
        if(currentPool>=ASPECT_TOTAL_CAP) amount=(int)Math.sqrt(amount);
        if(amount>1 && currentPool>=ASPECT_HARD_CAP) amount=1;
        return Math.max(0,amount);
    }
    public static float titleScale(int pixelWidth){return TITLE_BASE_SCALE-TITLE_SHRINK_PER_PIXEL*Math.max(0,pixelWidth-TITLE_SHRINK_THRESHOLD);}
    public static int rowCapacity(int row){return Math.max(1,5-row);}

    public static boolean timingContractMatchesTc4(){return REQUIRED_STABLE_TICKS==20&&remainingUseTicks(19)==6&&remainingUseTicks(20)==5&&!shouldCompleteAfterElapsed(19)&&shouldCompleteAfterElapsed(20);}
    public static boolean soundContractMatchesTc4(){int n=0;for(int e=0;e<=REQUIRED_STABLE_TICKS;e++)if(shouldPlayCameraTickAfterElapsed(e))n++;return n==10&&!shouldPlayCameraTickAfterElapsed(0)&&shouldPlayCameraTickAfterElapsed(1)&&shouldPlayCameraTickAfterElapsed(19)&&!shouldPlayCameraTickAfterElapsed(20);}
    public static boolean targetingContractMatchesTc4(){return ENTITY_SCAN_RANGE==10.0D&&ENTITY_TARGET_EXPAND==0.5D&&targetPriority(true,true)==TargetPriority.ENTITY&&targetPriority(false,true)==TargetPriority.BLOCK;}
}
