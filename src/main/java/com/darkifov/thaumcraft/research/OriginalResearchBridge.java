package com.darkifov.thaumcraft.research;

import com.darkifov.thaumcraft.data.PlayerThaumData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class OriginalResearchBridge {
    private OriginalResearchBridge() {
    }

    public static boolean canUnlock(Player player, ResearchEntry entry) {
        if (entry == null) {
            return false;
        }

        if (PlayerThaumData.hasResearch(player, entry.key())) {
            return false;
        }

        for (String requirement : entry.requirements()) {
            if (!PlayerThaumData.hasResearch(player, requirement)) {
                return false;
            }
        }

        for (String hiddenRequirement : entry.hiddenRequirements()) {
            if (!PlayerThaumData.hasResearch(player, hiddenRequirement)) {
                return false;
            }
        }

        return true;
    }

    public static boolean unlock(Player player, ResearchEntry entry) {
        if (!canUnlock(player, entry)) {
            return false;
        }

        PlayerThaumData.unlockResearch(player, entry.key());
        OriginalAspectWallet.seedIfEmpty(player);

        player.displayClientMessage(Component.literal("Research completed: ")
                .withStyle(ChatFormatting.GOLD)
                .append(Component.literal(entry.title()).withStyle(ChatFormatting.YELLOW)), true);
        return true;
    }

    public static Optional<ResearchEntry> firstAvailable(Player player) {
        for (ResearchEntry entry : ResearchRegistry.entries()) {
            if (canUnlock(player, entry)) {
                return Optional.of(entry);
            }
        }

        return Optional.empty();
    }

    public static Optional<ResearchEntry> selectedOrFirstAvailable(Player player) {
        Optional<ResearchEntry> selected = OriginalResearchSelection.getEntry(player);
        if (selected.isPresent() && canUnlock(player, selected.get())) {
            return selected;
        }

        return firstAvailable(player);
    }

    public static boolean select(Player player, String key) {
        Optional<ResearchEntry> entry = byKey(key);
        if (entry.isEmpty()) {
            return false;
        }

        OriginalResearchSelection.set(player, key);
        return true;
    }

    public static Optional<ResearchEntry> byKey(String key) {
        for (ResearchEntry entry : ResearchRegistry.entries()) {
            if (entry.key().equals(key)) {
                return Optional.of(entry);
            }
        }

        return Optional.empty();
    }

    public static Map<String, Integer> costsFor(ResearchEntry entry) {
        Map<String, Integer> costs = new LinkedHashMap<>();
        if (!entry.aspects().isEmpty()) {
            for (Map.Entry<String, Integer> aspect : entry.aspects().entrySet()) {
                costs.put(aspect.getKey().toLowerCase(), Math.max(1, aspect.getValue()));
            }
            return costs;
        }

        String key = entry.key().toUpperCase();

        int base = Math.max(1, entry.requirements().length + 1);

        if (key.contains("ALCHEMY") || key.contains("ESSENTIA") || key.contains("CRUCIBLE") || key.contains("JAR")) {
            costs.put("aqua", base);
            costs.put("ordo", 1);
        } else if (key.contains("INFUSION") || key.contains("WAND") || key.contains("FOCUS") || key.contains("VIS")) {
            costs.put("aer", base);
            costs.put("ordo", base);
        } else if (key.contains("ELDRITCH") || key.contains("WARP") || key.contains("TAINT")) {
            costs.put("perditio", base + 1);
            costs.put("ignis", 1);
        } else if (key.contains("GOLEM")) {
            costs.put("terra", base);
            costs.put("ordo", 1);
        } else {
            costs.put("aer", 1);
            costs.put("terra", 1);
        }

        return costs;
    }

    public static boolean completeWithAspectCost(Player player, ResearchEntry entry) {
        if (!canUnlock(player, entry)) {
            return false;
        }

        OriginalAspectWallet.seedIfEmpty(player);
        Map<String, Integer> costs = costsFor(entry);

        if (!OriginalAspectWallet.consume(player, costs)) {
            player.displayClientMessage(Component.literal("Not enough research aspects").withStyle(ChatFormatting.RED), true);
            return false;
        }

        return unlock(player, entry);
    }

    public static boolean completeSelectedOrFirst(Player player) {
        return selectedOrFirstAvailable(player)
                .map(entry -> completeWithAspectCost(player, entry))
                .orElse(false);
    }

    public static String statusFor(Player player, ResearchEntry entry) {
        if (entry == null) {
            return "missing";
        }

        if (PlayerThaumData.hasResearch(player, entry.key())) {
            return "complete";
        }

        return canUnlock(player, entry) ? "available" : "locked";
    }
}
