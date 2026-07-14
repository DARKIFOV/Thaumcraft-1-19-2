package com.darkifov.thaumcraft.research;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class PlayerAspectKnowledge {
    private static final String ROOT = "ThaumcraftAspectKnowledge";
    private static final String KNOWN = "Known";
    private static final String POOL = "Pool";
    private static final String STARTER_PRIMALS_SEEDED = "StarterPrimalsSeeded";
    private static final int STARTER_PRIMAL_AMOUNT = 10;

    private PlayerAspectKnowledge() {
    }

    public static void seedPrimals(Player player) {
        CompoundTag root = root(player);
        CompoundTag known = root.getCompound(KNOWN);

        AspectList pool = new AspectList();
        pool.load(root.getCompound(POOL));
        boolean seedPool = !root.getBoolean(STARTER_PRIMALS_SEEDED);

        for (Aspect aspect : Aspect.values()) {
            if (!aspect.isPrimal()) {
                continue;
            }
            known.putBoolean(aspect.id(), true);
            if (seedPool) {
                int missing = STARTER_PRIMAL_AMOUNT - pool.get(aspect);
                if (missing > 0) {
                    pool.add(aspect, missing);
                }
            }
        }

        root.put(KNOWN, known);
        if (seedPool) {
            root.put(POOL, pool.save());
            root.putBoolean(STARTER_PRIMALS_SEEDED, true);
        }
        player.getPersistentData().put(ROOT, root);
    }

    public static void copyFrom(Player from, Player to) {
        if (from == null || to == null) {
            return;
        }
        to.getPersistentData().put(ROOT, root(from).copy());
    }

    public static boolean knows(Player player, Aspect aspect) {
        if (aspect == null) {
            return false;
        }

        if (aspect.isPrimal()) {
            return true;
        }

        return root(player).getCompound(KNOWN).getBoolean(aspect.id());
    }

    public static boolean discover(Player player, Aspect aspect) {
        if (aspect == null) {
            return false;
        }

        seedPrimals(player);
        CompoundTag root = root(player);
        CompoundTag known = root.getCompound(KNOWN);

        boolean changed = !known.getBoolean(aspect.id());
        known.putBoolean(aspect.id(), true);
        root.put(KNOWN, known);
        player.getPersistentData().put(ROOT, root);
        return changed;
    }

    public static Set<Aspect> knownAspects(Player player) {
        seedPrimals(player);
        Set<Aspect> result = new LinkedHashSet<>();
        CompoundTag known = root(player).getCompound(KNOWN);

        for (Aspect aspect : Aspect.values()) {
            if (aspect.isPrimal() || known.getBoolean(aspect.id())) {
                result.add(aspect);
            }
        }

        return result;
    }

    public static AspectList pool(Player player) {
        AspectList list = new AspectList();
        list.load(root(player).getCompound(POOL));
        return list;
    }

    public static void addPool(Player player, Aspect aspect, int amount) {
        if (aspect == null || amount <= 0) {
            return;
        }

        CompoundTag root = root(player);
        AspectList list = new AspectList();
        list.load(root.getCompound(POOL));
        list.add(aspect, amount);
        root.put(POOL, list.save());
        player.getPersistentData().put(ROOT, root);
    }

    public static boolean consumePool(Player player, Aspect aspect, int amount) {
        if (aspect == null || amount <= 0) {
            return true;
        }

        CompoundTag root = root(player);
        AspectList list = new AspectList();
        list.load(root.getCompound(POOL));

        if (!list.remove(aspect, amount)) {
            return false;
        }

        root.put(POOL, list.save());
        player.getPersistentData().put(ROOT, root);
        return true;
    }

    public static Set<String> knownAspectIds(Player player) {
        Set<String> result = new LinkedHashSet<>();

        for (Aspect aspect : knownAspects(player)) {
            result.add(aspect.id());
        }

        return result;
    }

    public static Map<String, Integer> poolAmounts(Player player) {
        Map<String, Integer> result = new LinkedHashMap<>();
        AspectList pool = pool(player);

        for (Map.Entry<Aspect, Integer> entry : pool.entries().entrySet()) {
            result.put(entry.getKey().id(), entry.getValue());
        }

        return result;
    }

    public static int knownCount(Player player) {
        return knownAspects(player).size();
    }

    private static CompoundTag root(Player player) {
        return player.getPersistentData().getCompound(ROOT);
    }
}
