package com.darkifov.thaumcraft.client;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Client mirror of the original TC4 per-player scan ledger.
 *
 * <p>Scan knowledge belongs to the player, not to one physical Thaumometer.
 * Earlier rebuild stages only mirrored it into the held item's NBT, which made
 * the glass forget aspects after switching to another scanner. The server now
 * synchronizes the authoritative object/entity/node sets into this cache.</p>
 */
public final class ClientScanData {
    private static final Set<String> OBJECTS = new LinkedHashSet<>();
    private static final Set<String> ENTITIES = new LinkedHashSet<>();
    private static final Set<String> NODES = new LinkedHashSet<>();

    private ClientScanData() {
    }

    public static void set(Set<String> objects, Set<String> entities, Set<String> nodes) {
        replace(OBJECTS, objects);
        replace(ENTITIES, entities);
        replace(NODES, nodes);
    }

    private static void replace(Set<String> target, Set<String> source) {
        target.clear();
        if (source != null) {
            target.addAll(source);
        }
    }

    public static boolean hasObject(String key) {
        return key != null && OBJECTS.contains(key.trim());
    }

    public static boolean hasEntity(String key) {
        return key != null && ENTITIES.contains(key.trim());
    }

    public static boolean hasNode(String key) {
        return key != null && NODES.contains(key.trim());
    }

    public static Set<String> objects() {
        return Collections.unmodifiableSet(OBJECTS);
    }

    public static Set<String> entities() {
        return Collections.unmodifiableSet(ENTITIES);
    }

    public static Set<String> nodes() {
        return Collections.unmodifiableSet(NODES);
    }
}
