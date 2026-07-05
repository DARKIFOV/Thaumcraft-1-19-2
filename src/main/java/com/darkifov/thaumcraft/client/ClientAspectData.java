package com.darkifov.thaumcraft.client;

import com.darkifov.thaumcraft.Aspect;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class ClientAspectData {
    private static final Set<String> KNOWN = new HashSet<>();
    private static final Map<String, Integer> POOL = new HashMap<>();

    static {
        seedPrimals();
    }

    private ClientAspectData() {
    }

    public static void set(Set<String> known, Map<String, Integer> pool) {
        KNOWN.clear();
        POOL.clear();
        seedPrimals();

        if (known != null) {
            KNOWN.addAll(known);
        }

        if (pool != null) {
            POOL.putAll(pool);
        }
    }

    public static boolean knows(Aspect aspect) {
        return aspect != null && (aspect.isPrimal() || KNOWN.contains(aspect.id()));
    }

    public static int pool(Aspect aspect) {
        return aspect == null ? 0 : POOL.getOrDefault(aspect.id(), 0);
    }

    public static Set<String> knownIds() {
        return Collections.unmodifiableSet(KNOWN);
    }

    public static Map<String, Integer> poolView() {
        return Collections.unmodifiableMap(POOL);
    }

    public static int knownCount() {
        return KNOWN.size();
    }

    private static void seedPrimals() {
        for (Aspect aspect : Aspect.values()) {
            if (aspect.isPrimal()) {
                KNOWN.add(aspect.id());
            }
        }
    }
}
