package com.darkifov.thaumcraft.research;

/**
 * Stage603-622 hard guard for original TC4 GuiResearchTable/TileResearchTable parity.
 *
 * The values below are not new mechanics. They are the active 1.19.2 adapter ledger for
 * the original 1.7.10 GUI coordinates and table actions, kept in one place so later
 * batches cannot drift back to placeholder buttons, inventory fallbacks, or duplicate
 * fake research-note workflows.
 */
public final class TC4ResearchTableParity {
    public static final int GUI_WIDTH = 255;
    public static final int GUI_HEIGHT = 255;

    public static final int SLOT_SCRIBING_TOOLS_X = 14;
    public static final int SLOT_SCRIBING_TOOLS_Y = 10;
    public static final int SLOT_RESEARCH_NOTE_X = 70;
    public static final int SLOT_RESEARCH_NOTE_Y = 10;
    public static final int SLOT_HIT_SIZE = 18;
    public static final int NOTE_HEX_DRAW_W = 20;
    public static final int NOTE_HEX_DRAW_H = 18;
    public static final double NOTE_HEX_HIT_RADIUS_SQ = 144.0D;

    public static final int COPY_ICON_X = 37;
    public static final int COPY_ICON_Y = 5;
    public static final int COPY_ICON_SIZE = 24;

    public static final int ASPECT_GRID_X = 10;
    public static final int ASPECT_GRID_Y = 40;
    public static final int ASPECT_GRID_COLUMNS = 5;
    public static final int ASPECT_GRID_ROWS = 5;
    public static final int ASPECT_GRID_STEP = 16;
    public static final int ASPECT_PAGE_SHIFT = 5;
    public static final int ASPECTS_PER_PAGE = ASPECT_GRID_COLUMNS * ASPECT_GRID_ROWS;

    public static final int PAGE_PREVIOUS_X = 27;
    public static final int PAGE_NEXT_X = 51;
    public static final int PAGE_ARROW_Y = 121;
    public static final int PAGE_ARROW_W = 24;
    public static final int PAGE_ARROW_H = 8;

    public static final int COMBINE_LEFT_X = 13;
    public static final int COMBINE_RIGHT_X = 71;
    public static final int COMBINE_ARROW_X = 35;
    public static final int COMBINE_Y = 139;
    public static final int COMBINE_ARROW_W = 32;
    public static final int COMBINE_ARROW_H = 16;

    public static final int PLAYER_INVENTORY_X = 48;
    public static final int PLAYER_INVENTORY_Y = 175;
    public static final int PLAYER_HOTBAR_Y = 233;

    public static final int ACTION_CREATE_NOTE = 0;
    public static final int ACTION_OPEN_NOTE = 1;
    public static final int ACTION_COMPLETE_SOLVED_NOTE = 2;
    public static final int ACTION_COPY_COMPLETED_NOTE = 3;
    /** Syncs the note NBT into the already-open research table without opening a second GUI. */
    public static final int ACTION_SYNC_NOTE = 4;
    public static final int ACTION_COPY_COMPLETED_NOTE_LEGACY = 5;

    private TC4ResearchTableParity() {
    }

    public static boolean isCopyAction(int action) {
        return action == ACTION_COPY_COMPLETED_NOTE || action == ACTION_COPY_COMPLETED_NOTE_LEGACY;
    }

    public static boolean isNoteSlotHit(double localMouseX, double localMouseY) {
        return inside(localMouseX, localMouseY, SLOT_RESEARCH_NOTE_X, SLOT_RESEARCH_NOTE_Y, SLOT_HIT_SIZE, SLOT_HIT_SIZE);
    }

    public static boolean isScribingToolsSlotHit(double localMouseX, double localMouseY) {
        return inside(localMouseX, localMouseY, SLOT_SCRIBING_TOOLS_X, SLOT_SCRIBING_TOOLS_Y, SLOT_HIT_SIZE, SLOT_HIT_SIZE);
    }

    public static boolean isCopyIconHit(double localMouseX, double localMouseY) {
        return inside(localMouseX, localMouseY, COPY_ICON_X, COPY_ICON_Y, COPY_ICON_SIZE, COPY_ICON_SIZE);
    }

    public static boolean isPreviousAspectPageHit(double localMouseX, double localMouseY) {
        return inside(localMouseX, localMouseY, PAGE_PREVIOUS_X, PAGE_ARROW_Y, PAGE_ARROW_W, PAGE_ARROW_H);
    }

    public static boolean isNextAspectPageHit(double localMouseX, double localMouseY) {
        return inside(localMouseX, localMouseY, PAGE_NEXT_X, PAGE_ARROW_Y, PAGE_ARROW_W, PAGE_ARROW_H);
    }

    public static boolean isCombineArrowHit(double localMouseX, double localMouseY) {
        return inside(localMouseX, localMouseY, COMBINE_ARROW_X, COMBINE_Y, COMBINE_ARROW_W, COMBINE_ARROW_H);
    }

    /**
     * TC4 fills the 5x5 aspect palette column-first and shifts only one column
     * (five aspects) per page. This intentionally creates the same 20-aspect
     * overlap between adjacent pages as GuiResearchTable 4.2.3.5.
     */
    public static int aspectPageStart(int page) {
        return Math.max(0, page) * ASPECT_PAGE_SHIFT;
    }

    public static int lastAspectPage(int aspectCount) {
        return Math.max(0, (aspectCount - 20) / ASPECT_PAGE_SHIFT);
    }

    public static int aspectX(int localAspectIndex) {
        return ASPECT_GRID_X + (localAspectIndex / ASPECT_GRID_ROWS) * ASPECT_GRID_STEP;
    }

    public static int aspectY(int localAspectIndex) {
        return ASPECT_GRID_Y + (localAspectIndex % ASPECT_GRID_ROWS) * ASPECT_GRID_STEP;
    }

    public static boolean isAspectIconHit(double localMouseX, double localMouseY, int localAspectIndex) {
        if (localAspectIndex < 0 || localAspectIndex >= ASPECTS_PER_PAGE) {
            return false;
        }
        return inside(localMouseX, localMouseY, aspectX(localAspectIndex), aspectY(localAspectIndex), 16, 16);
    }

    private static boolean inside(double localMouseX, double localMouseY, int x, int y, int w, int h) {
        return localMouseX >= x && localMouseX < x + w && localMouseY >= y && localMouseY < y + h;
    }
}
