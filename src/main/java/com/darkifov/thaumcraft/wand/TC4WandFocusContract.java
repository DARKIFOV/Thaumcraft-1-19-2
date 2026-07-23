package com.darkifov.thaumcraft.wand;

/** Dependency-free constants and radial geometry from TC4 4.2.3.5 wand foci. */
public final class TC4WandFocusContract {
    public static final String CONTRACT_VERSION = "11.64.33";
    public static final String FOCUS_STACK_NBT = "focus";
    public static final String LEGACY_FOCUS_ID_NBT = "Focus";
    public static final String POUCH_INVENTORY_NBT = "Inventory";
    public static final String POUCH_SLOT_NBT = "Slot";
    public static final String UPGRADE_LIST_NBT = "upgrade";
    public static final String REMOVE_SENTINEL = "REMOVE";
    public static final int ORIGINAL_FOCUS_TYPES = 10;
    public static final int FOCUS_POUCH_SLOTS = 18;
    public static final int FOCUS_UPGRADE_RANKS = 5;
    public static final float REMOVE_SOUND_PITCH = 0.9F;
    public static final float CHANGE_SOUND_PITCH = 1.0F;
    public static final float CAMERA_TICKS_VOLUME = 0.3F;

    private TC4WandFocusContract() {
    }

    public static float radialRadius(int focusCount) {
        return 16.0F + Math.max(0, focusCount) * 2.5F;
    }

    public static float radialSliceDegrees(int focusCount) {
        return focusCount <= 0 ? 0.0F : 360.0F / focusCount;
    }

    public static float radialAngleDegrees(int index, int focusCount, float scale) {
        if (focusCount <= 0) {
            return 0.0F;
        }
        return -90.0F * clamp01(scale) + Math.floorMod(index, focusCount) * radialSliceDegrees(focusCount);
    }

    public static float clamp01(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }
}
