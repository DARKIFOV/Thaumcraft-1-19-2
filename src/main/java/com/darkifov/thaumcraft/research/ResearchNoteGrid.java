package com.darkifov.thaumcraft.research;

import com.darkifov.thaumcraft.Aspect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ResearchNoteGrid {
    public static final int SLOT_COUNT = 19;

    private static final List<GridSlot> SLOTS = List.of(
            new GridSlot(0, 0, -2),
            new GridSlot(1, -1, -1),
            new GridSlot(2, 0, -1),
            new GridSlot(3, 1, -1),
            new GridSlot(4, -2, 0),
            new GridSlot(5, -1, 0),
            new GridSlot(6, 0, 0),
            new GridSlot(7, 1, 0),
            new GridSlot(8, 2, 0),
            new GridSlot(9, -2, 1),
            new GridSlot(10, -1, 1),
            new GridSlot(11, 0, 1),
            new GridSlot(12, 1, 1),
            new GridSlot(13, -1, 2),
            new GridSlot(14, 0, 2),
            new GridSlot(15, 1, 2),
            new GridSlot(16, -1, -2),
            new GridSlot(17, 1, -2),
            new GridSlot(18, 0, 3)
    );

    private ResearchNoteGrid() {
    }

    public static List<GridSlot> slots() {
        return Collections.unmodifiableList(SLOTS);
    }

    public static GridSlot slot(int index) {
        if (index < 0 || index >= SLOTS.size()) {
            return SLOTS.get(6);
        }

        return SLOTS.get(index);
    }

    public static List<Integer> neighbors(int index) {
        GridSlot source = slot(index);
        List<Integer> result = new ArrayList<>();

        for (GridSlot target : SLOTS) {
            if (target.index() == index) {
                continue;
            }

            int dq = Math.abs(source.q() - target.q());
            int dr = Math.abs(source.r() - target.r());
            int ds = Math.abs((-source.q() - source.r()) - (-target.q() - target.r()));

            if (Math.max(dq, Math.max(dr, ds)) <= 1) {
                result.add(target.index());
            }
        }

        return result;
    }

    public static int x(int index) {
        GridSlot slot = slot(index);
        return 128 + slot.q() * 28 + slot.r() * 14;
    }

    public static int y(int index) {
        GridSlot slot = slot(index);
        return 82 + slot.r() * 24;
    }

    public static int defaultStartSlot() {
        return 0;
    }

    public static int defaultEndSlot() {
        return 18;
    }

    public static Aspect defaultStartAspect() {
        return Aspect.AER;
    }

    public static Aspect defaultEndAspect() {
        return Aspect.PRAECANTATIO;
    }

    public record GridSlot(int index, int q, int r) {
    }
}
