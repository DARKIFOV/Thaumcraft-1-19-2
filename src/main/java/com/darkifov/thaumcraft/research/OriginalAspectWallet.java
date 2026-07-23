package com.darkifov.thaumcraft.research;

import com.darkifov.thaumcraft.Aspect;
import net.minecraft.world.entity.player.Player;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/** Compatibility facade for old callers; all values now use PlayerAspectKnowledge. */
public final class OriginalAspectWallet {
    public static final String[] PRIMAL = {"aer", "terra", "ignis", "aqua", "ordo", "perditio"};

    private OriginalAspectWallet() {
    }

    public static Map<String, Integer> get(Player player) {
        PlayerAspectKnowledge.seedPrimals(player);
        Map<String, Integer> result = new LinkedHashMap<>();
        for (String id : PRIMAL) {
            result.put(id, get(player, id));
        }
        return result;
    }

    public static int get(Player player, String aspect) {
        Aspect resolved = resolve(aspect);
        return resolved == null ? 0 : PlayerAspectKnowledge.pool(player).get(resolved);
    }

    public static void add(Player player, String aspect, int amount) {
        Aspect resolved = resolve(aspect);
        if (resolved != null && amount > 0) {
            PlayerAspectKnowledge.addPool(player, resolved, amount);
        }
    }

    public static boolean consume(Player player, Map<String, Integer> costs) {
        if (player == null || costs == null) {
            return false;
        }
        Map<Aspect, Integer> resolved = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : costs.entrySet()) {
            Aspect aspect = resolve(entry.getKey());
            int amount = Math.max(0, entry.getValue());
            if (aspect == null) {
                return false;
            }
            resolved.merge(aspect, amount, Integer::sum);
        }
        for (Map.Entry<Aspect, Integer> entry : resolved.entrySet()) {
            if (PlayerAspectKnowledge.pool(player).get(entry.getKey()) < entry.getValue()) {
                return false;
            }
        }
        for (Map.Entry<Aspect, Integer> entry : resolved.entrySet()) {
            if (entry.getValue() > 0 && !PlayerAspectKnowledge.consumePool(player, entry.getKey(), entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    /** TC4 only discovers the six primals; it does not grant free pool points. */
    public static void seedIfEmpty(Player player) {
        PlayerAspectKnowledge.seedPrimals(player);
    }

    private static Aspect resolve(String id) {
        return Aspect.byId(id == null ? "" : id.toLowerCase(Locale.ROOT));
    }
}
