package com.darkifov.thaumcraft.client;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.research.ResearchNoteGrid;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ClientResearchNoteData {
    private static final Map<Integer, String> SLOTS = new LinkedHashMap<>();
    private static final Map<Integer, Integer> TYPES = new LinkedHashMap<>();
    private static String target = "";
    private static int progress;
    private static boolean solved;
    private static int radius = ResearchNoteGrid.MIN_RADIUS;

    private ClientResearchNoteData() {
    }

    public static void set(String newTarget, int newProgress, boolean newSolved, int newRadius,
                           Map<Integer, String> newSlots, Map<Integer, Integer> newTypes) {
        target = newTarget == null ? "" : newTarget;
        progress = newProgress;
        solved = newSolved;
        radius = ResearchNoteGrid.clampRadius(newRadius);
        SLOTS.clear();
        TYPES.clear();

        if (newSlots != null) {
            SLOTS.putAll(newSlots);
        }
        if (newTypes != null) {
            TYPES.putAll(newTypes);
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

    public static int radius() {
        return radius;
    }

    public static Map<Integer, String> slots() {
        return Collections.unmodifiableMap(SLOTS);
    }

    public static Map<Integer, Integer> types() {
        return Collections.unmodifiableMap(TYPES);
    }

    public static Aspect aspectAt(int slot) {
        return Aspect.byId(SLOTS.get(slot));
    }

    public static int typeAt(int slot) {
        return TYPES.getOrDefault(slot, -1);
    }

    public static boolean activeAt(int slot) {
        return TYPES.containsKey(slot);
    }

    public static boolean anchorAt(int slot) {
        return typeAt(slot) == ResearchNoteGrid.TYPE_RESEARCH_ANCHOR;
    }

    public static boolean placedAt(int slot) {
        return typeAt(slot) == ResearchNoteGrid.TYPE_PLACED;
    }

    public static boolean emptyAt(int slot) {
        return typeAt(slot) == ResearchNoteGrid.TYPE_EMPTY;
    }
}
