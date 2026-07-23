package com.darkifov.thaumcraft.client.screen;

import java.util.ArrayDeque;
import java.util.Deque;

/** GuiResearchRecipe.history compatibility for clickable item references. */
public final class TC4ThaumonomiconPageHistory {
    public record Entry(String researchKey, int page) {}
    private static final Deque<Entry> HISTORY = new ArrayDeque<>();

    private TC4ThaumonomiconPageHistory() {}

    public static void clear() { HISTORY.clear(); }
    public static boolean isEmpty() { return HISTORY.isEmpty(); }
    public static void push(String researchKey, int page) {
        if (researchKey != null && !researchKey.isBlank()) HISTORY.push(new Entry(researchKey, Math.max(0, page)));
    }
    public static Entry pop() { return HISTORY.pollFirst(); }
}
