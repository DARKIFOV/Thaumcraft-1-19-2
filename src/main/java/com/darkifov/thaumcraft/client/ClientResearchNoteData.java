package com.darkifov.thaumcraft.client;

import com.darkifov.thaumcraft.Aspect;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ClientResearchNoteData {
    private static final Map<Integer, String> SLOTS = new LinkedHashMap<>();
    private static String target = "";
    private static int progress;
    private static boolean solved;

    private ClientResearchNoteData() {
    }

    public static void set(String newTarget, int newProgress, boolean newSolved, Map<Integer, String> newSlots) {
        target = newTarget == null ? "" : newTarget;
        progress = newProgress;
        solved = newSolved;
        SLOTS.clear();

        if (newSlots != null) {
            SLOTS.putAll(newSlots);
        }
    }

    public static String target() {
        return target;
    }

    public static int progress() {
        return progress;
    }

    public static boolean solved() {
        return solved;
    }

    public static Map<Integer, String> slots() {
        return Collections.unmodifiableMap(SLOTS);
    }

    public static Aspect aspectAt(int slot) {
        return Aspect.byId(SLOTS.get(slot));
    }
}
