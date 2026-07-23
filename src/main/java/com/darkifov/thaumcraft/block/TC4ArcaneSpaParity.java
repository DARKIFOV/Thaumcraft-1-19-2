package com.darkifov.thaumcraft.block;

/**
 * Dependency-free constants and decisions copied from TC4 4.2.3.5 TileSpa,
 * BlockStoneDevice metadata 12 and BlockStoneDeviceItem.
 */
public final class TC4ArcaneSpaParity {
    public static final String CONTRACT_VERSION = "11.64.23";

    public static final int CAPACITY_MB = 5000;
    public static final int BUCKET_MB = 1000;
    public static final int CHECK_INTERVAL_TICKS = 40;
    public static final int OUTPUT_RADIUS = 2;
    public static final int SLOT_COUNT = 1;
    public static final int BLOCK_USE_DISTANCE_SQUARED = 64;

    public static final float BLOCK_HARDNESS = 3.0F;
    public static final float BLOCK_RESISTANCE = 25.0F;
    public static final boolean CONSUME_BEFORE_PLACEMENT = true;

    public static final String NBT_MIX = "mix";
    public static final String NBT_ITEMS = "Items";
    public static final String LEGACY_PORT_NBT_MIX = "Mix";
    public static final String LEGACY_PORT_NBT_TANK = "Tank";
    public static final String LEGACY_PORT_NBT_SALTS = "Salts";

    private TC4ArcaneSpaParity() {
    }

    /** TC4 used {@code counter++ % 40 == 0}; a newly placed spa checks immediately. */
    public static boolean shouldRunCycle(int counterBeforeIncrement) {
        return counterBeforeIncrement % CHECK_INTERVAL_TICKS == 0;
    }

    /** ForgeDirection.UP has ordinal 1 in TC4; every other side exposes inventory and fluid I/O. */
    public static boolean exposesAutomationSide(int directionOrdinal) {
        return directionOrdinal != 1;
    }

    /** Direct filled-container use only required free space and an empty/same-fluid tank. */
    public static boolean canAcceptFilledContainer(int currentAmount, boolean emptyOrSameFluid) {
        return currentAmount < CAPACITY_MB && emptyOrSameFluid;
    }

    /** TC4 consumed the whole filled container even when only part of its fluid fitted. */
    public static int acceptedFluidAmount(int currentAmount, int offeredAmount) {
        if (currentAmount < 0 || offeredAmount <= 0 || currentAmount >= CAPACITY_MB) {
            return 0;
        }
        return Math.min(CAPACITY_MB - currentAmount, offeredAmount);
    }

    public static boolean canPlaceOutput(boolean supportTopSolid, boolean replaceable,
                                         boolean alreadySameTargetSource) {
        return supportTopSolid && replaceable && !alreadySameTargetSource;
    }

    /** Exact GuiSpa top mask after six complete 8x8 fluid tiles are drawn. */
    public static int emptyFluidMaskHeight(int amount, int pixels) {
        if (pixels <= 0) return 0;
        int clamped = Math.max(0, Math.min(CAPACITY_MB, amount));
        float ratio = (float) clamped / (float) CAPACITY_MB;
        return Math.max(0, Math.min(pixels, (int) (pixels - pixels * ratio)));
    }

    /** Original nested loop order: x=-2..2 outside, z=-2..2 inside. */
    public static int candidateOffsetX(int index) {
        return index / 5 - OUTPUT_RADIUS;
    }

    public static int candidateOffsetZ(int index) {
        return index % 5 - OUTPUT_RADIUS;
    }
}
