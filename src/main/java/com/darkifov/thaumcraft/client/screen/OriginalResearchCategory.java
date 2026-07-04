package com.darkifov.thaumcraft.client.screen;

enum OriginalResearchCategory {
    BASICS("Basic Information", 0xB08A4D),
    THAUMATURGY("Thaumaturgy", 0x6F4D9B),
    ALCHEMY("Alchemy", 0x9A8B35),
    ARTIFICE("Artifice", 0x3E7A91),
    GOLEMANCY("Golemancy", 0x8A6A3E),
    ELDRITCH("Eldritch", 0x5B2A6D),
    ADDONS("Addons", 0x477547);

    private final String title;
    private final int color;

    OriginalResearchCategory(String title, int color) {
        this.title = title;
        this.color = color;
    }

    String title() {
        return title;
    }

    int color() {
        return color;
    }
}
