package com.darkifov.thaumcraft.research;

import com.darkifov.thaumcraft.data.PlayerThaumData;
import net.minecraft.world.entity.player.Player;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class OriginalResearchBridge {
    private OriginalResearchBridge() {
    }

    public static boolean canUnlock(Player player, ResearchEntry entry) {
        if (entry == null || byKey(entry.key()).isEmpty()) {
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

        if (!PlayerThaumData.unlockResearch(player, entry.key())) {
            return false;
        }
        OriginalResearchProgression.applyUnlockSideEffects(player, entry);
        PlayerAspectKnowledge.seedPrimals(player);
        return true;
    }

    /** Complete siblings exactly after the target, as ItemResearchNotes did. */
    public static int unlockEligibleSiblings(Player player, ResearchEntry target) {
        if (player == null || target == null || !PlayerThaumData.hasResearch(player, target.key())) {
            return 0;
        }
        int unlocked = 0;
        for (String siblingKey : target.siblings()) {
            Optional<ResearchEntry> sibling = byKey(siblingKey);
            if (sibling.isEmpty()) {
                continue;
            }
            boolean alreadyComplete = PlayerThaumData.hasResearch(player, sibling.get().key());
            boolean requisitesMet = canUnlock(player, sibling.get());
            if (TC4ResearchNoteCompletionParity.shouldUnlockSibling(
                    true, alreadyComplete, requisitesMet)
                    && unlock(player, sibling.get())) {
                unlocked++;
            }
        }
        return unlocked;
    }

    public static Optional<ResearchEntry> firstAvailable(Player player) {
        OriginalResearchProgression.seedAutoUnlocks(player);
        for (ResearchEntry entry : ResearchRegistry.originalEntries()) {
            if (canUnlock(player, entry) && TC4ResearchFlagPolicy.canCreateNormalResearchNote(player, entry)) {
                return Optional.of(entry);
            }
        }

        return Optional.empty();
    }

    public static Optional<ResearchEntry> selectedOrFirstAvailable(Player player) {
        Optional<ResearchEntry> selected = OriginalResearchSelection.getEntry(player);
        if (selected.isPresent()
                && canUnlock(player, selected.get())
                && TC4ResearchFlagPolicy.canCreateNormalResearchNote(player, selected.get())) {
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

    public static boolean canSelectForResearchTable(Player player, ResearchEntry entry) {
        return canUnlock(player, entry) && TC4ResearchFlagPolicy.canCreateNormalResearchNote(player, entry);
    }

    public static boolean selectForResearchTable(Player player, String key) {
        Optional<ResearchEntry> entry = byKey(key);
        if (entry.isEmpty() || !canSelectForResearchTable(player, entry.get())) {
            return false;
        }
        OriginalResearchSelection.set(player, entry.get().key());
        return true;
    }

    public static Optional<ResearchEntry> byKey(String key) {
        if (key == null) {
            return Optional.empty();
        }
        for (ResearchEntry entry : ResearchRegistry.originalEntries()) {
            if (entry.key().equals(key) || entry.key().equalsIgnoreCase(key)) {
                return Optional.of(entry);
            }
        }

        return Optional.empty();
    }

    /** Secondary research cost is exactly ResearchItem.tags; no inferred fallback aspects. */
    public static Map<String, Integer> costsFor(ResearchEntry entry) {
        Map<String, Integer> costs = new LinkedHashMap<>();
        if (entry == null) {
            return costs;
        }
        for (Map.Entry<String, Integer> aspect : entry.aspects().entrySet()) {
            costs.put(aspect.getKey().toLowerCase(), Math.max(0, aspect.getValue()));
        }
        return costs;
    }

    public static boolean completeWithAspectCost(Player player, ResearchEntry entry) {
        if (!canUnlock(player, entry)) {
            return false;
        }

        PlayerAspectKnowledge.seedPrimals(player);
        return OriginalAspectWallet.consume(player, costsFor(entry)) && unlock(player, entry);
    }

    /** PacketPlayerCompleteToServer type 0: silent aspect purchase plus eligible siblings. */
    public static boolean completeSecondaryFromThaumonomicon(Player player, ResearchEntry entry) {
        if (!canUnlock(player, entry)) return false;
        PlayerAspectKnowledge.seedPrimals(player);
        if (!OriginalAspectWallet.consume(player, costsFor(entry))) return false;
        if (!PlayerThaumData.unlockResearch(player, entry.key())) return false;
        OriginalResearchProgression.applyUnlockSideEffects(player, entry);
        for (String siblingKey : entry.siblings()) {
            Optional<ResearchEntry> sibling = byKey(siblingKey);
            if (sibling.isPresent() && canUnlock(player, sibling.get())
                    && PlayerThaumData.unlockResearch(player, sibling.get().key())) {
                OriginalResearchProgression.applyUnlockSideEffects(player, sibling.get());
            }
        }
        return true;
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
