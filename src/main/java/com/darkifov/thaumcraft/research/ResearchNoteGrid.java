package com.darkifov.thaumcraft.research;

import com.darkifov.thaumcraft.Aspect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Stage161: direct 1.19.2 adapter for the TC4 research note hex model.
 *
 * Original TC4 creates note hexes with:
 *   radius = 1 + min(3, research.getComplexity())
 *   HexUtils.generateHexes(radius)
 *   HexUtils.getRing(radius) for the fixed research-tag anchors
 *
 * The old rebuild used a fixed 19-slot shape and two fake anchors. This class now
 * exposes a stable indexed view over the original axial hex coordinates so the
 * modern network packets can keep using integer slot ids while the saved NBT
 * stores the same q/r semantics as the 1.7.10 note.
 */
public final class ResearchNoteGrid {
    public static final int MIN_RADIUS = 2;
    public static final int MAX_RADIUS = 4;
    public static final int SLOT_COUNT = 61; // radius 4: 1 + 3r(r+1)
    public static final int TYPE_EMPTY = 0;
    public static final int TYPE_RESEARCH_ANCHOR = 1;
    public static final int TYPE_PLACED = 2;

    private static final int[][] NEIGHBOURS = {
            {1, 0}, {1, -1}, {0, -1}, {-1, 0}, {-1, 1}, {0, 1}
    };

    private static final List<GridSlot> ALL_SLOTS = generateHexes(MAX_RADIUS);
    private static final Map<String, GridSlot> BY_KEY = new LinkedHashMap<>();

    static {
        for (GridSlot slot : ALL_SLOTS) {
            BY_KEY.put(key(slot.q(), slot.r()), slot);
        }
    }

    private ResearchNoteGrid() {
    }

    public static List<GridSlot> slots() {
        return Collections.unmodifiableList(ALL_SLOTS);
    }

    public static List<GridSlot> activeSlotsForRadius(int radius) {
        int actual = clampRadius(radius);
        List<GridSlot> result = new ArrayList<>();
        for (GridSlot slot : ALL_SLOTS) {
            if (distance(slot.q(), slot.r(), 0, 0) <= actual) {
                result.add(slot);
            }
        }
        return result;
    }

    public static GridSlot slot(int index) {
        if (index < 0 || index >= ALL_SLOTS.size()) {
            return ALL_SLOTS.get(0);
        }
        return ALL_SLOTS.get(index);
    }

    public static Optional<GridSlot> byHex(int q, int r) {
        return Optional.ofNullable(BY_KEY.get(key(q, r)));
    }

    public static int radiusForResearch(String researchKey) {
        int complexity = 1;
        if (researchKey != null && !researchKey.isBlank()) {
            Optional<ResearchEntry> entry = ResearchRegistry.byKey(researchKey);
            if (entry.isEmpty()) {
                entry = ResearchRegistry.byKey(researchKey.toUpperCase(Locale.ROOT));
            }
            if (entry.isPresent()) {
                complexity = entry.get().complexity();
            }
        }
        return clampRadius(1 + Math.min(3, Math.max(1, complexity)));
    }

    public static int clampRadius(int radius) {
        return Math.max(MIN_RADIUS, Math.min(MAX_RADIUS, radius));
    }

    public static List<GridSlot> ring(int radius) {
        int actual = clampRadius(radius);
        List<GridSlot> result = new ArrayList<>();
        int q = 0;
        int r = 0;
        for (int k = 0; k < actual; k++) {
            int[] d = NEIGHBOURS[4];
            q += d[0];
            r += d[1];
        }
        for (int direction = 0; direction < 6; direction++) {
            for (int step = 0; step < actual; step++) {
                byHex(q, r).ifPresent(result::add);
                int[] d = NEIGHBOURS[direction];
                q += d[0];
                r += d[1];
            }
        }
        return result;
    }

    public static List<GridSlot> distributeRing(int radius, int entries) {
        List<GridSlot> ring = ring(radius);
        if (entries <= 0 || ring.isEmpty()) {
            return List.of();
        }
        if (entries >= ring.size()) {
            return new ArrayList<>(ring);
        }
        List<GridSlot> result = new ArrayList<>();
        float spacing = ring.size() / (float) entries;
        float pos = 0.0F;
        for (int i = 0; i < entries; i++) {
            int idx = Math.max(0, Math.min(ring.size() - 1, Math.round(pos)));
            result.add(ring.get(idx));
            pos += spacing;
        }
        return result;
    }

    public static List<Integer> neighbors(int index) {
        GridSlot source = slot(index);
        List<Integer> result = new ArrayList<>();
        for (int[] d : NEIGHBOURS) {
            byHex(source.q() + d[0], source.r() + d[1]).ifPresent(slot -> result.add(slot.index()));
        }
        return result;
    }


    public static Optional<GridSlot> hitTest(int relativeX, int relativeY, int radius) {
        int actual = clampRadius(radius);
        GridSlot best = null;
        double bestDistance = Double.MAX_VALUE;
        for (GridSlot slot : activeSlotsForRadius(actual)) {
            int sx = x(slot.index());
            int sy = y(slot.index());
            double dx = relativeX - sx;
            double dy = relativeY - sy;
            double distance = dx * dx + dy * dy;
            if (distance < bestDistance) {
                bestDistance = distance;
                best = slot;
            }
        }
        return best != null && bestDistance <= 144.0D ? Optional.of(best) : Optional.empty();
    }

    public static int x(int index) {
        GridSlot slot = slot(index);
        return 128 + (int) Math.round(18.0D * slot.q());
    }

    public static int y(int index) {
        GridSlot slot = slot(index);
        return 112 + (int) Math.round(15.588D * (slot.r() + slot.q() / 2.0D));
    }

    public static int defaultStartSlot() {
        return distributeRing(MIN_RADIUS, 1).stream().findFirst().map(GridSlot::index).orElse(0);
    }

    public static int defaultEndSlot() {
        List<GridSlot> ring = distributeRing(MIN_RADIUS, 2);
        return ring.size() > 1 ? ring.get(1).index() : defaultStartSlot();
    }

    public static Aspect defaultStartAspect() {
        return Aspect.AER;
    }

    public static Aspect defaultEndAspect() {
        return Aspect.PRAECANTATIO;
    }

    public static String key(int q, int r) {
        return q + ":" + r;
    }

    public static int distance(int q1, int r1, int q2, int r2) {
        return (Math.abs(q1 - q2) + Math.abs(r1 - r2) + Math.abs(q1 + r1 - q2 - r2)) / 2;
    }

    private static List<GridSlot> generateHexes(int radius) {
        List<GridSlot> result = new ArrayList<>();
        Map<String, GridSlot> seen = new LinkedHashMap<>();
        add(seen, 0, 0);
        int q = 0;
        int r = 0;
        for (int k = 0; k < radius; k++) {
            int[] start = NEIGHBOURS[4];
            q += start[0];
            r += start[1];
            int hq = q;
            int hr = r;
            for (int direction = 0; direction < 6; direction++) {
                int[] d = NEIGHBOURS[direction];
                for (int step = 0; step <= k; step++) {
                    add(seen, hq, hr);
                    hq += d[0];
                    hr += d[1];
                }
            }
        }
        int index = 0;
        for (GridSlot slot : seen.values()) {
            result.add(new GridSlot(index++, slot.q(), slot.r()));
        }
        return result;
    }

    private static void add(Map<String, GridSlot> seen, int q, int r) {
        seen.putIfAbsent(key(q, r), new GridSlot(-1, q, r));
    }

    public record GridSlot(int index, int q, int r) {
    }
}
