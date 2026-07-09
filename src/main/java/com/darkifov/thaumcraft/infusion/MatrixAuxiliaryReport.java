package com.darkifov.thaumcraft.infusion;

public record MatrixAuxiliaryReport(
        int acceleratorCount,
        int stabilizerCount,
        int symmetricStabilizers,
        boolean aspectPowered,
        int unpairedStabilizers,
        String stabilizerSignature
) {
    public int speedMultiplier() {
        return Math.min(5, Math.max(1, 1 + acceleratorCount));
    }

    public int effectiveStabilizers() {
        if (!aspectPowered) {
            return 0;
        }

        return Math.min(TC4InfusionStabilityParity.MAX_EFFECTIVE_STABILIZER_PAIRS, Math.max(0, symmetricStabilizers));
    }

    public int stabilizationPercent() {
        return effectiveStabilizers() * 25;
    }

    public int unpairedInstabilityPenalty() {
        return Math.max(0, unpairedStabilizers / 2);
    }
}
