package com.darkifov.thaumcraft.client;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class ClientResearchData {
    private static final Set<String> RESEARCH = new HashSet<>();
    private static int warp = 0;

    static {
        RESEARCH.add("FIRST_STEPS");
    }

    private ClientResearchData() {
    }

    public static void setResearch(Set<String> research, int newWarp) {
        RESEARCH.clear();
        RESEARCH.add("FIRST_STEPS");

        if (research != null) {
            RESEARCH.addAll(research);
        }

        warp = Math.max(0, newWarp);
    }

    public static boolean hasResearch(String key) {
        return key == null || key.isEmpty() || RESEARCH.contains(key);
    }

    public static Set<String> research() {
        return Collections.unmodifiableSet(RESEARCH);
    }

    public static int warp() {
        return warp;
    }
}
