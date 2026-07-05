package com.darkifov.thaumcraft.research;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectCombinationRegistry;

import java.util.ArrayDeque;
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

    public static boolean canConnect(Aspect first, Aspect second) {
        return distance(first, second) <= 2;
    }

    public static boolean isDirect(Aspect first, Aspect second) {
        return distance(first, second) <= 1;
    }
}
