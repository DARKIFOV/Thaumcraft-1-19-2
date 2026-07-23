package com.darkifov.thaumcraft.arcane;

import com.darkifov.thaumcraft.Aspect;

/**
 * Stage683-702: shared coordinate ledger for original TC4 GuiArcaneWorkbench / ContainerArcaneWorkbench.
 * Source-of-truth notes: 1.7.10 GuiArcaneWorkbench uses gui_arcaneworkbench.png (190x234), a
 * 3x3 work matrix at 40/40 with 24px spacing, wand slot at 160/24 and output at 160/64.
 * Keep the constants here so client GUI and menu slot placement cannot drift apart again.
 */
public final class TC4ArcaneWorkbenchParity {
    public static final int SLOT_SIZE = 16;
    public static final int ASPECT_ICON_SIZE = 16;
    public static final int ASPECT_HOVER_RADIUS = 8;
    public static final int GUI_WIDTH = 190;
    public static final int GUI_HEIGHT = 234;
    public static final int PLAYER_INV_X = 16;
    public static final int PLAYER_INV_Y = 151;
    public static final int HOTBAR_Y = 209;
    public static final int GRID_X = 40;
    public static final int GRID_Y = 40;
    public static final int GRID_SPACING = 24;
    public static final int WAND_SLOT_X = 160;
    public static final int WAND_SLOT_Y = 24;
    public static final int OUTPUT_SLOT_X = 160;
    public static final int OUTPUT_SLOT_Y = 64;

    // TileArcaneWorkbenchRenderer: exact installed-wand transform.
    public static final double WAND_RENDER_X = 0.65D;
    public static final double WAND_RENDER_Y = 1.0625D;
    public static final double WAND_RENDER_Z = 0.25D;
    public static final float WAND_RENDER_X_ROTATION = 90.0F;
    public static final float WAND_RENDER_Z_ROTATION = 20.0F;
    public static final float TRANSFORM_CLICK_VOLUME = 0.15F;
    public static final float TRANSFORM_CLICK_PITCH = 0.5F;

    public static final Aspect[] PRIMALS = new Aspect[]{Aspect.AER, Aspect.TERRA, Aspect.IGNIS, Aspect.AQUA, Aspect.ORDO, Aspect.PERDITIO};
    public static final int[][] ASPECT_LOCS = new int[][]{
            {72, 21},   // Air
            {24, 43},   // Earth
            {24, 102},  // Fire
            {72, 124},  // Water
            {120, 102}, // Order
            {120, 43}   // Entropy
    };

    private TC4ArcaneWorkbenchParity() {
    }

    public static int gridSlotX(int col) {
        return GRID_X + col * GRID_SPACING;
    }

    public static int gridSlotY(int row) {
        return GRID_Y + row * GRID_SPACING;
    }

    public static boolean insideSlot(int mouseX, int mouseY, int slotX, int slotY) {
        return mouseX >= slotX && mouseX < slotX + SLOT_SIZE && mouseY >= slotY && mouseY < slotY + SLOT_SIZE;
    }

    public static boolean insideOutputSlot(int mouseX, int mouseY) {
        return insideSlot(mouseX, mouseY, OUTPUT_SLOT_X, OUTPUT_SLOT_Y);
    }

    public static boolean insideWandSlot(int mouseX, int mouseY) {
        return insideSlot(mouseX, mouseY, WAND_SLOT_X, WAND_SLOT_Y);
    }

    public static Aspect aspectAt(int mouseX, int mouseY) {
        for (int i = 0; i < PRIMALS.length; i++) {
            int x = ASPECT_LOCS[i][0] - ASPECT_HOVER_RADIUS;
            int y = ASPECT_LOCS[i][1] - ASPECT_HOVER_RADIUS;
            if (mouseX >= x && mouseX < x + ASPECT_ICON_SIZE && mouseY >= y && mouseY < y + ASPECT_ICON_SIZE) {
                return PRIMALS[i];
            }
        }
        return null;
    }
}

