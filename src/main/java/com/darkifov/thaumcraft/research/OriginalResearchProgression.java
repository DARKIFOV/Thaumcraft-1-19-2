package com.darkifov.thaumcraft.research;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.data.PlayerThaumData;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Stage151: original TC4 research progression side-effects.
 *
 * The data remains extracted from ConfigResearch.java. This runtime layer applies
 * auto-unlock starter research, scan trigger unlocks and warp grants from the
 * original metadata without inventing new research graph rules.
 */
public final class OriginalResearchProgression {
    private OriginalResearchProgression() {}

    public static int seedAutoUnlocks(Player player) {
        if (player == null) {
            return 0;
        }
        int unlocked = 0;
        for (String key : TC4ResearchMetadataIndex.autoUnlockKeys()) {
            Optional<ResearchEntry> entry = ResearchRegistry.byKey(key);
            if (entry.isPresent() && PlayerThaumData.unlockResearch(player, key)) {
                applyUnlockSideEffects(player, entry.get());
                unlocked++;
            }
        }
        return unlocked;
    }

    public static boolean parentsComplete(Player player, ResearchEntry entry) {
        if (player == null || entry == null) {
            return false;
        }
        for (String requirement : entry.requirements()) {
            if (!PlayerThaumData.hasResearch(player, requirement)) {
                return false;
            }
        }
        for (String requirement : entry.hiddenRequirements()) {
            if (!PlayerThaumData.hasResearch(player, requirement)) {
                return false;
            }
        }
        return true;
    }

    public static int applyScanTriggers(Player player, String itemExpression, Iterable<Aspect> aspects, String entityId) {
        if (player == null) {
            return 0;
        }
        seedAutoUnlocks(player);
        Set<String> candidates = new HashSet<>();
        if (itemExpression != null && !itemExpression.isBlank()) {
            candidates.addAll(TC4ResearchMetadataIndex.researchKeysForItemTrigger(itemExpression));
        }
        if (entityId != null && !entityId.isBlank()) {
            candidates.addAll(TC4ResearchMetadataIndex.researchKeysForEntityTrigger(entityId));
        }
        if (aspects != null) {
            for (Aspect aspect : aspects) {
                if (aspect != null) {
                    candidates.addAll(TC4ResearchMetadataIndex.researchKeysForAspectTrigger(aspect.name()));
                    candidates.addAll(TC4ResearchMetadataIndex.researchKeysForAspectTrigger(aspect.id()));
                }
            }
        }

        int unlocked = 0;
        for (String key : candidates) {
            if (tryUnlockTriggered(player, key)) {
                unlocked++;
            }
        }

        if (unlocked > 0 && player instanceof ServerPlayer serverPlayer) {
            ThaumcraftNetwork.syncResearch(serverPlayer);
        }
        return unlocked;
    }

    public static boolean tryUnlockTriggered(Player player, String key) {
        Optional<ResearchEntry> maybe = ResearchRegistry.byKey(key);
        if (maybe.isEmpty()) {
            return false;
        }
        ResearchEntry entry = maybe.get();
        if (!parentsComplete(player, entry)) {
            return false;
        }
        if (!PlayerThaumData.unlockResearch(player, entry.key())) {
            return false;
        }
        applyUnlockSideEffects(player, entry);
        player.displayClientMessage(Component.literal("Research discovered: ")
                .withStyle(ChatFormatting.GOLD)
                .append(Component.literal(entry.title()).withStyle(ChatFormatting.YELLOW)), false);
        return true;
    }

    public static void applyUnlockSideEffects(Player player, ResearchEntry entry) {
        if (player == null || entry == null) {
            return;
        }
        int warp = Math.max(0, entry.warp());
        if (warp > 0) {
            PlayerThaumData.addWarpPermanent(player, warp);
        }
    }
}
