package com.darkifov.thaumcraft.infusion;

/**
 * Stage703-722: small source-of-truth ledger for TC4 TileInfusionMatrix craftCycle timings,
 * sound keys and non-debug display policy.  This is not a new mechanic: it keeps the
 * Forge 1.19.2 block entity from scattering hard-coded adapter values across methods.
 */
public final class TC4InfusionCraftCycleParity {
    public static final int CRAFT_CYCLE_DELAY = TC4InfusionRuntime.CRAFT_CYCLE_DELAY;
    public static final int ITEM_PULL_DELAY = TC4InfusionRuntime.ITEM_PULL_DELAY;
    public static final int ENCHANTMENT_XP_DELAY = 20;
    public static final int WAITING_RETRY_DELAY = 10;
    public static final boolean SHOW_WAITING_DEBUG_MESSAGES = false;
    public static final boolean SHOW_START_COMPLETE_DEBUG_MESSAGES = false;

    public static final String SOUND_MATRIX_START = "craftstart";
    public static final String SOUND_COMPONENT_PULL = "craftstart";
    public static final String SOUND_ESSENTIA_DRAIN = "infuser";
    public static final String SOUND_FINISH = "infuserstart";
    public static final String SOUND_FAIL = "craftfail";

    private TC4InfusionCraftCycleParity() {
    }
}
