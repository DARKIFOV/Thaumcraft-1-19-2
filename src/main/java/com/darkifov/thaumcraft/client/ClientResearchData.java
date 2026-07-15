package com.darkifov.thaumcraft.client;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class ClientResearchData {
    private static final Set<String> RESEARCH = new HashSet<>();
    private static int warp = 0;
    private static int permanentWarp = 0;
    private static int stickyWarp = 0;
    private static int temporaryWarp = 0;
    private static int warpCounter = 0;

    static {
        RESEARCH.add("FIRST_STEPS");
    }

    private ClientResearchData() {
    }

    public static void setResearch(Set<String> research, int totalWarp, int permanent,
                                   int sticky, int temporary, int counter) {
        RESEARCH.clear();
        RESEARCH.add("FIRST_STEPS");

        if (research != null) {
            RESEARCH.addAll(research);
        }

        warp = Math.max(0, totalWarp);
        permanentWarp = Math.max(0, permanent);
        stickyWarp = Math.max(0, sticky);
        temporaryWarp = Math.max(0, temporary);
        warpCounter = Math.max(0, counter);
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

    public static int permanentWarp() {
        return permanentWarp;
    }

    public static int stickyWarp() {
        return stickyWarp;
    }

    public static int temporaryWarp() {
        return temporaryWarp;
    }

    public static int warpCounter() {
        return warpCounter;
    }
}
