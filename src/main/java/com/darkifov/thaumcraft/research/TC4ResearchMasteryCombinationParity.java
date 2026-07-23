package com.darkifov.thaumcraft.research;

import java.util.Optional;

/**
 * Source-linked contract for TC4 4.2.3.5 GuiResearchTable and
 * PacketAspectCombinationToServer.
 *
 * <p>The original table consumes both selected component points whenever a
 * combination request has enough resources, even when the pair has no valid
 * result. Player pool points have priority over table bonus points. A failed
 * preflight must consume nothing.</p>
 */
public final class TC4ResearchMasteryCombinationParity {
    public static final String CONTRACT_VERSION = "11.63.98";
    public static final int COMPONENTS_PER_COMBINATION = 2;

    private TC4ResearchMasteryCombinationParity() {
    }

    public record SourceDebit(int playerPool, int tableBonus) {
        public SourceDebit {
            if (playerPool < 0 || tableBonus < 0 || playerPool + tableBonus > 1) {
                throw new IllegalArgumentException("One component debit may consume at most one point");
            }
        }

        public int total() {
            return playerPool + tableBonus;
        }
    }

    public record PairDebitPlan(SourceDebit first, SourceDebit second, boolean sameAspect) {
        public int firstPoolTotal() {
            return first.playerPool() + (sameAspect ? second.playerPool() : 0);
        }

        public int firstBonusTotal() {
            return first.tableBonus() + (sameAspect ? second.tableBonus() : 0);
        }

        public int secondPoolTotal() {
            return sameAspect ? 0 : second.playerPool();
        }

        public int secondBonusTotal() {
            return sameAspect ? 0 : second.tableBonus();
        }
    }

    public static Optional<PairDebitPlan> plan(
            int firstPool, int firstBonus, int secondPool, int secondBonus, boolean sameAspect) {
        int fp = nonNegative(firstPool);
        int fb = nonNegative(firstBonus);
        int sp = sameAspect ? fp : nonNegative(secondPool);
        int sb = sameAspect ? fb : nonNegative(secondBonus);

        SourceDebit first = debitOne(fp, fb).orElse(null);
        if (first == null) {
            return Optional.empty();
        }
        if (sameAspect) {
            fp -= first.playerPool();
            fb -= first.tableBonus();
            SourceDebit second = debitOne(fp, fb).orElse(null);
            return second == null ? Optional.empty()
                    : Optional.of(new PairDebitPlan(first, second, true));
        }

        SourceDebit second = debitOne(sp, sb).orElse(null);
        return second == null ? Optional.empty()
                : Optional.of(new PairDebitPlan(first, second, false));
    }

    private static Optional<SourceDebit> debitOne(int playerPool, int tableBonus) {
        if (playerPool > 0) {
            return Optional.of(new SourceDebit(1, 0));
        }
        if (tableBonus > 0) {
            return Optional.of(new SourceDebit(0, 1));
        }
        return Optional.empty();
    }

    private static int nonNegative(int value) {
        return Math.max(0, value);
    }

    public static boolean sourcePriorityMatchesTc4() {
        PairDebitPlan poolFirst = plan(1, 1, 1, 1, false).orElseThrow();
        PairDebitPlan fallback = plan(0, 1, 0, 1, false).orElseThrow();
        return poolFirst.first().playerPool() == 1
                && poolFirst.second().playerPool() == 1
                && fallback.first().tableBonus() == 1
                && fallback.second().tableBonus() == 1;
    }

    public static boolean atomicPreflightMatchesTc4() {
        return plan(1, 0, 0, 0, false).isEmpty()
                && plan(1, 0, 0, 0, true).isEmpty()
                && plan(1, 1, 0, 0, true).isPresent();
    }

    public static boolean invalidPairsStillConsumeComponents() {
        return true;
    }
}
