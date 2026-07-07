package com.darkifov.thaumcraft.research;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectCombinationRegistry;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public final class ResearchAspectGraph {
    private static final Map<Aspect, Set<Aspect>> LINKS = new EnumMap<>(Aspect.class);

    static {
        for (Aspect aspect : Aspect.values()) {
            LINKS.put(aspect, EnumSet.noneOf(Aspect.class));
        }

        for (Aspect aspect : Aspect.values()) {
            if (!aspect.isPrimal()) {
                link(aspect, aspect.firstComponent());
                link(aspect, aspect.secondComponent());
            }
        }

        for (Aspect first : Aspect.values()) {
            for (Aspect second : Aspect.values()) {
                AspectCombinationRegistry.combine(first, second).ifPresent(result -> {
                    link(first, result);
                    link(second, result);
                });
            }
        }
    }

    private ResearchAspectGraph() {
    }

    private static void link(Aspect first, Aspect second) {
        if (first == null || second == null) {
            return;
        }

        LINKS.get(first).add(second);
        LINKS.get(second).add(first);
    }

    public static Set<Aspect> neighbors(Aspect aspect) {
        return aspect == null ? Set.of() : new LinkedHashSet<>(LINKS.getOrDefault(aspect, Set.of()));
    }

    public static int distance(Aspect start, Aspect target) {
        if (start == null || target == null) {
            return 999;
        }

        if (start == target) {
            return 0;
        }

        Queue<Aspect> queue = new ArrayDeque<>();
        Map<Aspect, Integer> distance = new EnumMap<>(Aspect.class);

        queue.add(start);
        distance.put(start, 0);

        while (!queue.isEmpty()) {
            Aspect current = queue.remove();
            int nextDistance = distance.get(current) + 1;

            for (Aspect next : LINKS.getOrDefault(current, Set.of())) {
                if (distance.containsKey(next)) {
                    continue;
                }

                if (next == target) {
                    return nextDistance;
                }

                distance.put(next, nextDistance);
                queue.add(next);
            }
        }

        return 999;
    }

    public static List<Aspect> shortestPath(Aspect start, Aspect target) {
        if (start == null || target == null) {
            return List.of();
        }

        if (start == target) {
            return List.of(start);
        }

        Queue<Aspect> queue = new ArrayDeque<>();
        Map<Aspect, Aspect> previous = new EnumMap<>(Aspect.class);
        Set<Aspect> seen = EnumSet.noneOf(Aspect.class);

        queue.add(start);
        seen.add(start);

        while (!queue.isEmpty()) {
            Aspect current = queue.remove();

            for (Aspect next : LINKS.getOrDefault(current, Set.of())) {
                if (!seen.add(next)) {
                    continue;
                }

                previous.put(next, current);

                if (next == target) {
                    List<Aspect> path = new ArrayList<>();
                    Aspect cursor = target;

                    while (cursor != null) {
                        path.add(cursor);
                        if (cursor == start) {
                            break;
                        }
                        cursor = previous.get(cursor);
                    }

                    Collections.reverse(path);
                    return path;
                }

                queue.add(next);
            }
        }

        return List.of();
    }

    public static Aspect suggestedConnector(Aspect start, Aspect target) {
        List<Aspect> path = shortestPath(start, target);
        return path.size() >= 2 ? path.get(1) : null;
    }

    /**
     * Stage139: TC4 research notes link aspects only through an immediate
     * parent/component relationship. Stage137 allowed distance <= 2, which made
     * notes too forgiving and let paths skip one original TC4 aspect.
     */
    public static boolean canConnect(Aspect first, Aspect second) {
        return AspectCombinationRegistry.isOriginalDirectLink(first, second);
    }

    public static boolean isDirect(Aspect first, Aspect second) {
        return AspectCombinationRegistry.isOriginalDirectLink(first, second);
    }

    public static boolean canHintThroughOneMissingAspect(Aspect first, Aspect second) {
        return distance(first, second) <= 2;
    }
}
