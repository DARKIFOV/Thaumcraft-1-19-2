package com.darkifov.thaumcraft.research;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class OriginalArcaneCostBridge {
    private OriginalArcaneCostBridge() {
    }

    public static int visCostFor(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }

        String id = stack.getItem().toString().toLowerCase(Locale.ROOT);

        if (id.contains("wand") || id.contains("focus")) {
            return 25;
        }
        if (id.contains("thaumium") || id.contains("void")) {
            return 15;
        }
        if (id.contains("jar") || id.contains("tube")) {
            return 10;
        }

        return 5;
    }

    public static Map<String, Integer> primalCostFor(ItemStack stack) {
        Map<String, Integer> costs = new LinkedHashMap<>();
        int vis = visCostFor(stack);

        if (vis <= 0) {
            return costs;
        }

        String id = stack.getItem().toString().toLowerCase(Locale.ROOT);
        if (id.contains("focus")) {
            costs.put("aer", 1);
            costs.put("ignis", 1);
        } else if (id.contains("jar") || id.contains("essentia")) {
            costs.put("aqua", 1);
            costs.put("ordo", 1);
        } else if (id.contains("thaumium") || id.contains("void")) {
            costs.put("terra", 1);
            costs.put("ordo", 1);
        } else {
            costs.put("aer", 1);
        }

        return costs;
    }

    public static boolean canCraft(Player player, ItemStack output) {
        OriginalAspectWallet.seedIfEmpty(player);
        return OriginalAspectWallet.get(player, "aer")
                + OriginalAspectWallet.get(player, "terra")
                + OriginalAspectWallet.get(player, "ignis")
                + OriginalAspectWallet.get(player, "aqua")
                + OriginalAspectWallet.get(player, "ordo")
                + OriginalAspectWallet.get(player, "perditio") >= Math.max(1, visCostFor(output) / 5);
    }
}
