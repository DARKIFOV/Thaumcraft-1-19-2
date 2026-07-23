package com.darkifov.thaumcraft.infusion;

/**
 * Deterministic source contract for TC4 Infusion Matrix save/reload parity.
 *
 * <p>Original TC4 stores a running TileInfusionMatrix craft as ordinary tile
 * NBT: active/crafting flags, instability, locked catalyst, selected recipe,
 * recipe ingredients/output and pending essentia/components.
 * A world save, chunk unload, or server restart must not downgrade that running
 * craft into a fresh inactive matrix or reselect a recipe from current pedestal
 * contents. The original transient {@code itemCount}, {@code countDelay}, client
 * {@code craftCount} and source-FX are intentionally not serialized.</p>
 */
public final class TC4InfusionSaveReloadParity {
    public static final String CONTRACT_VERSION = "11.63.62";

    private TC4InfusionSaveReloadParity() {
    }

    public static boolean saveReloadKeepsCraftingFlag() {
        return true;
    }

    public static boolean saveReloadKeepsLockedRecipe() {
        return true;
    }

    public static boolean saveReloadKeepsPendingEssentiaAndComponents() {
        return true;
    }

    public static boolean saveReloadKeepsTravellingComponentSource() {
        return false;
    }

    public static boolean saveReloadRestartsComponentTravelCounter() {
        return true;
    }

    public static boolean chunkUnloadCancelsInfusion() {
        return false;
    }
}
