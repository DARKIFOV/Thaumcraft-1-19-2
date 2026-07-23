package com.darkifov.thaumcraft.wand;

import com.darkifov.thaumcraft.block.FocusPouchItem;

/** Source-linked aggregate contract for the ten original TC4 wand foci. */
public final class TC4WandFociFullClosureParity {
    public static final String CONTRACT_VERSION = TC4WandFocusContract.CONTRACT_VERSION;

    private TC4WandFociFullClosureParity() {
    }

    public static boolean cataloguesMatchOriginal() {
        return WandFocusType.values().length == TC4WandFocusContract.ORIGINAL_FOCUS_TYPES
                && FocusPouchItem.maxFocusSlots() == TC4WandFocusContract.FOCUS_POUCH_SLOTS
                && FocusUpgradeRuntime.MAX_RANK == TC4WandFocusContract.FOCUS_UPGRADE_RANKS;
    }

    public static boolean nbtKeysMatchOriginal() {
        return TC4WandFocusContract.FOCUS_STACK_NBT.equals(WandFocusRuntime.TAG_FOCUS_STACK)
                && TC4WandFocusContract.POUCH_INVENTORY_NBT.equals(FocusPouchItem.TAG_INVENTORY)
                && TC4WandFocusContract.UPGRADE_LIST_NBT.equals(FocusUpgradeRuntime.TAG_UPGRADE)
                && TC4WandFocusContract.REMOVE_SENTINEL.equals(WandManagerRuntime.REMOVE);
    }

    public static boolean radialGeometryMatchesOriginal(int focusCount) {
        return TC4WandFocusContract.radialRadius(focusCount) == 16.0F + focusCount * 2.5F
                && (focusCount == 0 || TC4WandFocusContract.radialSliceDegrees(focusCount) == 360.0F / focusCount)
                && TC4WandFocusContract.radialAngleDegrees(0, Math.max(1, focusCount), 1.0F) == -90.0F;
    }

    public static boolean soundContractMatchesOriginal() {
        return TC4WandFocusContract.CAMERA_TICKS_VOLUME == 0.3F
                && TC4WandFocusContract.REMOVE_SOUND_PITCH == 0.9F
                && TC4WandFocusContract.CHANGE_SOUND_PITCH == 1.0F;
    }
}
