package com.darkifov.thaumcraft.infusion;

public record MatrixAuxiliaryReport(
        int acceleratorCount,
        int stabilizerCount,
        int symmetricStabilizers,
        boolean aspectPowered
) {
    public int speedMultiplier() {
        return Math.min(5, Math.max(1, 1 + acceleratorCount));
    }

    public int effectiveStabilizers() {
        if (!aspectPowered) {
            return 0;
        }

        return Math.min(4, Math.max(0, symmetricStabilizers));
    }

    public int stabilizationPercent() {
        return effectiveStabilizers() * 25;
    }
}
