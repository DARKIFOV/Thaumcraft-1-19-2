package com.darkifov.thaumcraft.infusion;

import com.darkifov.thaumcraft.blockentity.ArcanePedestalBlockEntity;
import net.minecraft.network.chat.Component;

import java.util.List;

public record InfusionStructureReport(
        boolean valid,
        int pedestalCount,
        int componentPedestalCount,
        int symmetricalPairs,
        int missingSymmetry,
        int stabilizingBlocks,
        int matrixAccelerators,
        int matrixStabilizers,
        int speedMultiplier,
        int durationModifierPercent,
        int matrixStabilizationPercent,
        int stabilityScore,
        int instabilityPenalty,
        List<ArcanePedestalBlockEntity> componentPedestals,
        Component summary
) {
}
