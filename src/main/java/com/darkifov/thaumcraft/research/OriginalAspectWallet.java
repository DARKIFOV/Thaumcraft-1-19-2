package com.darkifov.thaumcraft.research;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class OriginalAspectWallet {
    public static final String[] PRIMAL = {"aer", "terra", "ignis", "aqua", "ordo", "perditio"};
    private static final String ROOT = "ThaumcraftOriginalAspectWallet";

    private OriginalAspectWallet() {
    }

    public static Map<String, Integer> get(Player player) {
        CompoundTag root = player.getPersistentData().getCompound(ROOT);
        Map<String, Integer> result = new LinkedHashMap<>();

        for (String aspect : PRIMAL) {
            result.put(aspect, Math.max(0, root.getInt(aspect)));
        }

        return result;
    }

    public static int get(Player player, String aspect) {
        return Math.max(0, player.getPersistentData().getCompound(ROOT).getInt(normalize(aspect)));
    }

    public static void add(Player player, String aspect, int amount) {
        if (amount <= 0) {
            return;
        }

        CompoundTag root = player.getPersistentData().getCompound(ROOT);
        String key = normalize(aspect);
        root.putInt(key, Math.max(0, root.getInt(key) + amount));
        player.getPersistentData().put(ROOT, root);
    }

    public static boolean consume(Player player, Map<String, Integer> costs) {
        for (Map.Entry<String, Integer> entry : costs.entrySet()) {
            if (get(player, entry.getKey()) < entry.getValue()) {
                return false;
            }
        }

        CompoundTag root = player.getPersistentData().getCompound(ROOT);
        for (Map.Entry<String, Integer> entry : costs.entrySet()) {
            String key = normalize(entry.getKey());
            root.putInt(key, Math.max(0, root.getInt(key) - entry.getValue()));
        }
        player.getPersistentData().put(ROOT, root);
        return true;
    }

    public static void seedIfEmpty(Player player) {
        CompoundTag root = player.getPersistentData().getCompound(ROOT);
        boolean empty = true;

        for (String aspect : PRIMAL) {
            if (root.getInt(aspect) > 0) {
                empty = false;
                break;
            }
        }

        if (empty) {
            for (String aspect : PRIMAL) {
                root.putInt(aspect, 5);
            }
            player.getPersistentData().put(ROOT, root);
        }
    }

    private static String normalize(String aspect) {
        return aspect == null ? "aer" : aspect.toLowerCase(Locale.ROOT);
    }
}
