package com.darkifov.thaumcraft.research;

/** Dependency-free contracts copied from TC4 4.2.3.5 ItemThaumonomicon and both book GUIs. */
public final class TC4ThaumonomiconParity {
    public static final String CONTRACT_VERSION = "11.64.26";
    public static final int MAX_STACK_SIZE = 1;
    public static final int CHEAT_ASPECT_POOL = 50;
    public static final int BROWSER_WIDTH = 256;
    public static final int BROWSER_HEIGHT = 230;
    public static final int MAP_WIDTH = 224;
    public static final int MAP_HEIGHT = 196;
    public static final int RESEARCH_CELL = 24;
    public static final int DEFAULT_LAST_X = -5;
    public static final int DEFAULT_LAST_Y = -6;
    public static final int DEFAULT_PAN_X = DEFAULT_LAST_X * RESEARCH_CELL - 141 / 2 - 12;
    public static final int DEFAULT_PAN_Y = DEFAULT_LAST_Y * RESEARCH_CELL - 141 / 2;
    public static final float OPEN_PAGE_VOLUME = 1.0F;
    public static final float PAGE_TURN_VOLUME = 0.66F;
    public static final float CATEGORY_CLICK_VOLUME = 0.4F;
    public static final float GUI_SOUND_PITCH = 1.0F;

    private TC4ThaumonomiconParity() {}

    public static int spreadStart(int page) {
        int safe = Math.max(0, page);
        return (safe & 1) == 0 ? safe : safe - 1;
    }

    public static int previousSpread(int page) {
        return Math.max(0, spreadStart(page) - 2);
    }

    public static int nextSpread(int page, int totalPages) {
        int max = maxFirstPage(totalPages);
        return Math.min(max, spreadStart(page) + 2);
    }

    public static int maxFirstPage(int totalPages) {
        int pages = Math.max(1, totalPages);
        return Math.max(0, pages - 1 - (pages % 2 == 0 ? 1 : 0));
    }
}
