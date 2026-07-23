package com.darkifov.thaumcraft.client.screen;

import com.darkifov.thaumcraft.research.TC4ThaumonomiconParity;

/** GuiResearchBrowser static selectedCategory/lastX/lastY compatibility state. */
final class TC4ThaumonomiconClientState {
    private static OriginalResearchCategory category = OriginalResearchCategory.BASICS;
    private static int panX = TC4ThaumonomiconParity.DEFAULT_PAN_X;
    private static int panY = TC4ThaumonomiconParity.DEFAULT_PAN_Y;

    private TC4ThaumonomiconClientState() {}

    static OriginalResearchCategory category() { return category; }
    static int panX() { return panX; }
    static int panY() { return panY; }

    static void save(OriginalResearchCategory nextCategory, int nextPanX, int nextPanY) {
        if (nextCategory != null) category = nextCategory;
        panX = nextPanX;
        panY = nextPanY;
    }
}
