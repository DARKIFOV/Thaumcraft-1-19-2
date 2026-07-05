package com.darkifov.thaumcraft.client.screen;

final class OriginalClientResearchSelection {
    private static String selectedKey = "";

    private OriginalClientResearchSelection() {
    }

    static void set(String key) {
        selectedKey = key == null ? "" : key;
    }

    static String get() {
        return selectedKey;
    }

    static boolean hasSelection() {
        return selectedKey != null && !selectedKey.isBlank();
    }
}
