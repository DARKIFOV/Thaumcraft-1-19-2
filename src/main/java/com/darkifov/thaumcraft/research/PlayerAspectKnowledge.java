package com.darkifov.thaumcraft.research;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Unified TC4 player aspect knowledge and research-point pool.
 *
 * <p>Original PlayerKnowledge stored discovery and pool amount in one AspectList:
 * primal aspects were known with amount zero and no free research points were
 * minted when the player or Research Table was initialised.</p>
 */
public final class PlayerAspectKnowledge {
    private static final String ROOT = "ThaumcraftAspectKnowledge";
    private static final String KNOWN = "Known";
    private static final String POOL = "Pool";
    private static final String LEGACY_WALLET_ROOT = "ThaumcraftOriginalAspectWallet";
    private static final String LEGACY_WALLET_MIGRATED = "LegacyOriginalWalletMigrated";

    private PlayerAspectKnowledge() {
    }

    /** Mark the six primals as discovered with a zero pool, exactly like TC4. */
    public static void seedPrimals(Player player) {
        if (player == null) {
            return;
        }
        migrateLegacyWallet(player);
        CompoundTag root = root(player);
        CompoundTag known = root.getCompound(KNOWN);
        for (Aspect aspect : Aspect.values()) {
            if (aspect.isPrimal()) {
                known.putBoolean(aspect.id(), true);
            }
        }
        root.put(KNOWN, known);
        player.getPersistentData().put(ROOT, root);
    }

    /**
     * v11.64.27 save migration: older builds kept a second primal-only wallet.
     * The two stores represented the same logical TC4 pool, so migration takes
     * the larger amount rather than summing and duplicating research points.
     */
    public static void migrateLegacyWallet(Player player) {
        if (player == null) {
            return;
        }
        CompoundTag persistent = player.getPersistentData();
        CompoundTag root = persistent.getCompound(ROOT);
        if (root.getBoolean(LEGACY_WALLET_MIGRATED)) {
            persistent.remove(LEGACY_WALLET_ROOT);
            return;
        }
        AspectList pool = new AspectList();
        pool.load(root.getCompound(POOL));
        CompoundTag legacy = persistent.getCompound(LEGACY_WALLET_ROOT);
        for (Aspect aspect : Aspect.values()) {
            if (!aspect.isPrimal()) {
                continue;
            }
            int merged = Math.max(pool.get(aspect), Math.max(0, legacy.getInt(aspect.id())));
            setAmount(pool, aspect, merged);
        }
        root.put(POOL, pool.save());
        root.putBoolean(LEGACY_WALLET_MIGRATED, true);
        persistent.put(ROOT, root);
        persistent.remove(LEGACY_WALLET_ROOT);
    }

    public static void copyFrom(Player from, Player to) {
        if (from == null || to == null) {
            return;
        }
        seedPrimals(from);
        to.getPersistentData().put(ROOT, root(from).copy());
        to.getPersistentData().remove(LEGACY_WALLET_ROOT);
    }

    public static boolean knows(Player player, Aspect aspect) {
        if (player == null || aspect == null) {
            return false;
        }
        seedPrimals(player);
        return aspect.isPrimal() || root(player).getCompound(KNOWN).getBoolean(aspect.id());
    }

    public static boolean discover(Player player, Aspect aspect) {
        if (player == null || aspect == null) {
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
        seedPrimals(player);
        AspectList list = new AspectList();
        list.load(root(player).getCompound(POOL));
        return list;
    }

    public static void addPool(Player player, Aspect aspect, int amount) {
        if (player == null || aspect == null || amount <= 0) {
            return;
        }
        seedPrimals(player);
        CompoundTag root = root(player);
        AspectList list = new AspectList();
        list.load(root.getCompound(POOL));
        list.add(aspect, amount);
        root.put(POOL, list.save());
        player.getPersistentData().put(ROOT, root);
    }

    public static boolean consumePool(Player player, Aspect aspect, int amount) {
        if (player == null || aspect == null || amount <= 0) {
            return amount <= 0;
        }
        seedPrimals(player);
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

    public static void setPoolAmount(Player player, Aspect aspect, int amount) {
        setPoolAmountForTransaction(player, aspect, amount);
    }

    static void setPoolAmountForTransaction(Player player, Aspect aspect, int amount) {
        if (player == null || aspect == null) {
            return;
        }
        seedPrimals(player);
        CompoundTag root = root(player);
        AspectList list = new AspectList();
        list.load(root.getCompound(POOL));
        setAmount(list, aspect, Math.max(0, amount));
        root.put(POOL, list.save());
        player.getPersistentData().put(ROOT, root);
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

    private static void setAmount(AspectList list, Aspect aspect, int amount) {
        int current = list.get(aspect);
        if (current > 0) {
            list.remove(aspect, current);
        }
        if (amount > 0) {
            list.add(aspect, amount);
        }
    }

    private static CompoundTag root(Player player) {
        return player.getPersistentData().getCompound(ROOT);
    }
}
