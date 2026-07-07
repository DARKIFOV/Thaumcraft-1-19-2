package com.darkifov.thaumcraft.research;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectList;
import com.darkifov.thaumcraft.AspectCombinationRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

public final class ResearchTableFoundation {
    private static final String FOUNDATION_POOL_SEEDED = "ThaumcraftResearchFoundationPoolSeeded";

    private ResearchTableFoundation() {
    }

    public static boolean componentsKnown(Player player, Aspect aspect) {
        if (player == null || aspect == null) {
            return false;
        }
        return aspect.isPrimal() || (PlayerAspectKnowledge.knows(player, aspect.firstComponent())
                && PlayerAspectKnowledge.knows(player, aspect.secondComponent()));
    }

    public static Optional<Aspect[]> decompose(Player player, Aspect compound) {
        if (player == null || compound == null || compound.isPrimal()) {
            return Optional.empty();
        }

        PlayerAspectKnowledge.seedPrimals(player);
        if (!PlayerAspectKnowledge.knows(player, compound)) {
            return Optional.empty();
        }

        Aspect[] parts = AspectCombinationRegistry.decompose(compound).orElse(null);
        return parts == null ? Optional.empty() : Optional.of(parts);
    }

    public static void seed(Player player) {
        PlayerAspectKnowledge.seedPrimals(player);
        OriginalResearchProgression.seedAutoUnlocks(player);

        // Stage136: TC4 gives the player primal understanding as the foundation of research,
        // but it must not duplicate free research pool points every time the table is clicked.
        // The old Stage135 behavior could be spammed; now the starter pool is granted once per player.
        if (player.getPersistentData().getBoolean(FOUNDATION_POOL_SEEDED)) {
            return;
        }

        for (Aspect aspect : Aspect.values()) {
            if (aspect.isPrimal()) {
                PlayerAspectKnowledge.addPool(player, aspect, 3);
            }
        }

        player.getPersistentData().putBoolean(FOUNDATION_POOL_SEEDED, true);
    }

    public static Optional<Aspect> combine(Player player, Aspect first, Aspect second) {
        if (player == null || first == null || second == null) {
            return Optional.empty();
        }

        PlayerAspectKnowledge.seedPrimals(player);

        if (!PlayerAspectKnowledge.knows(player, first) || !PlayerAspectKnowledge.knows(player, second)) {
            player.displayClientMessage(Component.literal("You do not understand those aspects yet.").withStyle(ChatFormatting.RED), false);
            return Optional.empty();
        }

        Optional<Aspect> result = AspectCombinationRegistry.combine(first, second);

        if (result.isEmpty()) {
            player.displayClientMessage(Component.literal("The aspects do not combine.").withStyle(ChatFormatting.GRAY), false);
            return Optional.empty();
        }

        Aspect discovered = result.get();

        if (!AspectCombinationRegistry.isOriginalComponentPair(discovered, first, second)) {
            return Optional.empty();
        }

        if (!player.getAbilities().instabuild) {
            AspectList pool = PlayerAspectKnowledge.pool(player);

            if (!pool.contains(first, 1) || !pool.contains(second, 1)) {
                player.displayClientMessage(Component.literal("Not enough aspect notes in the research pool.").withStyle(ChatFormatting.RED), false);
                return Optional.empty();
            }

            PlayerAspectKnowledge.consumePool(player, first, 1);
            PlayerAspectKnowledge.consumePool(player, second, 1);
        }

        boolean newDiscovery = PlayerAspectKnowledge.discover(player, discovered);
        PlayerAspectKnowledge.addPool(player, discovered, 1);

        if (newDiscovery) {
            player.displayClientMessage(Component.literal("Discovered aspect: ")
                    .withStyle(ChatFormatting.GOLD)
                    .append(Component.literal(discovered.displayName()).withStyle(style -> style.withColor(discovered.textColor()))), false);
        } else {
            player.displayClientMessage(Component.literal("Aspect reinforced: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(discovered.displayName()).withStyle(style -> style.withColor(discovered.textColor()))), false);
        }

        return result;
    }

    public static Component knowledgeSummary(Player player) {
        PlayerAspectKnowledge.seedPrimals(player);
        return Component.literal("Aspects known: " + PlayerAspectKnowledge.knownCount(player) + " / " + Aspect.values().length)
                .withStyle(ChatFormatting.LIGHT_PURPLE);
    }
}
