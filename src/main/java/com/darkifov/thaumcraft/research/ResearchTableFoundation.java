package com.darkifov.thaumcraft.research;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectCombinationRegistry;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

public final class ResearchTableFoundation {
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
    }

    public static Optional<Aspect> combine(Player player, Aspect first, Aspect second) {
        if (player == null || first == null || second == null) {
            return Optional.empty();
        }

        PlayerAspectKnowledge.seedPrimals(player);
        if (!PlayerAspectKnowledge.knows(player, first) || !PlayerAspectKnowledge.knows(player, second)) {
            return Optional.empty();
        }

        // TC4 GuiResearchTable consumes both selected research points when the
        // request has enough resources, even when the pair is invalid. Commit
        // both debits as one server-side transaction so a stale menu/packet can
        // never destroy only the first component.
        if (!ResearchTableInventoryRuntime.consumeAspectPairAtomically(player, first, second)) {
            return Optional.empty();
        }

        Optional<Aspect> result = AspectCombinationRegistry.combine(first, second);
        if (result.isEmpty() || !AspectCombinationRegistry.isOriginalComponentPair(result.get(), first, second)) {
            return Optional.empty();
        }

        Aspect discovered = result.get();
        PlayerAspectKnowledge.discover(player, discovered);
        PlayerAspectKnowledge.addPool(player, discovered, 1);
        return result;
    }

}
