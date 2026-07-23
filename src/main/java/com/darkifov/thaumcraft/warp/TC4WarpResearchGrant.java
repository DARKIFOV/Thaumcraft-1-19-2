package com.darkifov.thaumcraft.warp;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import com.darkifov.thaumcraft.research.PlayerAspectKnowledge;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared production port of TC4 {@code WarpEvents.grantResearch}.
 *
 * <p>The original selected {@code 1 + rand.nextInt(times)} primal aspects,
 * added one point for every selection and immediately sent the updated aspect
 * pool to the affected player.  The 1.19.2 port batches the final client sync
 * into one packet, but preserves the awarded points and primal selection
 * order exactly.</p>
 */
public final class TC4WarpResearchGrant {
    public static final String CONTRACT_VERSION = "11.64.09";
    private static final List<Aspect> PRIMAL_ORDER = List.of(
            Aspect.AER,
            Aspect.TERRA,
            Aspect.IGNIS,
            Aspect.AQUA,
            Aspect.ORDO,
            Aspect.PERDITIO
    );

    private TC4WarpResearchGrant() {
    }

    /**
     * Rolls the exact TC4 award before mutating player state.
     */
    public static GrantRoll roll(RandomSource random, int times) {
        if (random == null) {
            throw new IllegalArgumentException("random");
        }
        if (times <= 0) {
            throw new IllegalArgumentException("times must be positive");
        }

        int amount = 1 + random.nextInt(times);
        List<Aspect> aspects = new ArrayList<>(amount);
        for (int i = 0; i < amount; i++) {
            aspects.add(PRIMAL_ORDER.get(random.nextInt(PRIMAL_ORDER.size())));
        }
        return new GrantRoll(aspects);
    }

    /**
     * Applies a rolled TC4 award and synchronizes the changed pool to the
     * client. TC4 emitted one PacketAspectPool per point; the modern protocol
     * sends one aggregate AspectKnowledgeSyncPacket after the complete batch.
     */
    public static GrantRoll grantAndSync(ServerPlayer player, int times) {
        GrantRoll roll = roll(player.getRandom(), times);
        for (Aspect aspect : roll.aspects()) {
            PlayerAspectKnowledge.addPool(player, aspect, 1);
        }
        ThaumcraftNetwork.syncAspectKnowledge(player);
        return roll;
    }

    public static List<Aspect> primalOrder() {
        return PRIMAL_ORDER;
    }

    public record GrantRoll(List<Aspect> aspects) {
        public GrantRoll {
            aspects = List.copyOf(aspects);
        }

        public int amount() {
            return aspects.size();
        }
    }
}
